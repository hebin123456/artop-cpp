// AcceleoEngine.cpp — 模板求值引擎
// 对齐 Java: org.eclipse.acceleo.engine.generation.AcceleoEngine
//           + org.eclipse.acceleo.engine.service.AcceleoService
//
// 实现要点：
//   1. evalBlocks：遍历块序列，文本块原样输出，表达式块求值后输出
//   2. evalExpr：AQL 子集求值，基于 emf::ecore EObject 反射
//      - VarExpr 'self' → 上下文 self 对象
//      - NavExpr expr.name → eGet(EStructuralFeature)
//      - CallExpr ->size() → 集合大小；->collect(e|...)；->select(e|cond) 等
//   3. ForBlock：迭代集合，每次设变量后求值 body
//   4. IfBlock：求值 cond，选分支求值
//   5. LetBlock：设变量后求值 body
//   6. FileBlock：求值 body 后写入文件（支持 protected 区保留）
//   7. ProtectedBlock：在已存在文件中查找同名区，复用其内容
#include "emf/acceleo/AcceleoEngine.h"
#include "emf/acceleo/AcceleoParser.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/common/EList.h"
#include "emf/common/EObject.h"
#include <fstream>
#include <sstream>
#include <filesystem>
#include <algorithm>
#include <regex>

namespace emf::acceleo {

namespace fs = std::filesystem;

// ===== std::any 类型擦除辅助 =====
// 问题：std::any 保存的是放入时的精确类型。例如把 EClass* 放入 std::any，
//       则 any.type() == typeid(EClass*)，而不是 typeid(EObject*)。
//       直接 any_cast<EObject*>(any) 会抛 bad_any_cast。
// 解决：对 emf::ecore 中所有 EObject 派生类的指针类型逐一尝试 any_cast，
//       再 dynamic_cast 回 EObject*。这是一个封闭集合（emf::ecore 元模型自身）。
// 对齐 Java：Java 端 EObject 是接口，所有 EClass 实例都 is-a EObject，
//           不存在此类型擦除问题。C++ 端用此 helper 模拟同样的多态性。
namespace {

// 从 std::any 中提取 EObject*（支持所有 emf::ecore 元模型派生类指针）
::emf::common::EObject* asEObject(const std::any& v) {
    if (!v.has_value()) return nullptr;
    const std::type_info& ti = v.type();
    // 先尝试 EObject* 自身
    if (ti == typeid(::emf::common::EObject*)) {
        return std::any_cast<::emf::common::EObject*>(v);
    }
    // emf::ecore 中所有 EObject 派生类指针。dynamic_cast 利用虚继承的 RTTI。
    // 用宏展开避免重复样板。
    #define EMF_TRY_DCAST(Type) \
        if (ti == typeid(::emf::ecore::Type*)) { \
            auto* p = std::any_cast<::emf::ecore::Type*>(v); \
            return dynamic_cast<::emf::common::EObject*>(p); \
        }
    EMF_TRY_DCAST(EModelElement)
    EMF_TRY_DCAST(ENamedElement)
    EMF_TRY_DCAST(ETypedElement)
    EMF_TRY_DCAST(EClassifier)
    EMF_TRY_DCAST(EClass)
    EMF_TRY_DCAST(EDataType)
    EMF_TRY_DCAST(EEnum)
    EMF_TRY_DCAST(EEnumLiteral)
    EMF_TRY_DCAST(EStructuralFeature)
    EMF_TRY_DCAST(EAttribute)
    EMF_TRY_DCAST(EReference)
    EMF_TRY_DCAST(EOperation)
    EMF_TRY_DCAST(EParameter)
    EMF_TRY_DCAST(ETypeParameter)
    EMF_TRY_DCAST(EGenericType)
    EMF_TRY_DCAST(EAnnotation)
    EMF_TRY_DCAST(EPackage)
    EMF_TRY_DCAST(EFactory)
    #undef EMF_TRY_DCAST
    return nullptr;
}

// 从 std::any 中提取 EObject 集合（支持 std::vector<派生类*> 与 EList<EObject*>*）
// eGet 对多值 feature 返回的是精确的 std::vector<EClassifier*> 等，
// 不是 std::vector<EObject*>，所以需要同样的类型枚举。
std::vector<::emf::common::EObject*> asEObjectVector(const std::any& v) {
    std::vector<::emf::common::EObject*> result;
    if (!v.has_value()) return result;
    const std::type_info& ti = v.type();
    if (ti == typeid(std::vector<::emf::common::EObject*>)) {
        for (auto* o : std::any_cast<std::vector<::emf::common::EObject*>>(v)) result.push_back(o);
        return result;
    }
    if (ti == typeid(::emf::common::EList<::emf::common::EObject*>*)) {
        auto* list = std::any_cast<::emf::common::EList<::emf::common::EObject*>*>(v);
        if (list) for (auto* o : *list) result.push_back(o);
        return result;
    }
    #define EMF_TRY_VEC_DCAST(Type) \
        if (ti == typeid(std::vector<::emf::ecore::Type*>)) { \
            for (auto* p : std::any_cast<std::vector<::emf::ecore::Type*>>(v)) { \
                result.push_back(dynamic_cast<::emf::common::EObject*>(p)); \
            } \
            return result; \
        }
    EMF_TRY_VEC_DCAST(EModelElement)
    EMF_TRY_VEC_DCAST(ENamedElement)
    EMF_TRY_VEC_DCAST(ETypedElement)
    EMF_TRY_VEC_DCAST(EClassifier)
    EMF_TRY_VEC_DCAST(EClass)
    EMF_TRY_VEC_DCAST(EDataType)
    EMF_TRY_VEC_DCAST(EEnum)
    EMF_TRY_VEC_DCAST(EEnumLiteral)
    EMF_TRY_VEC_DCAST(EStructuralFeature)
    EMF_TRY_VEC_DCAST(EAttribute)
    EMF_TRY_VEC_DCAST(EReference)
    EMF_TRY_VEC_DCAST(EOperation)
    EMF_TRY_VEC_DCAST(EParameter)
    EMF_TRY_VEC_DCAST(ETypeParameter)
    EMF_TRY_VEC_DCAST(EGenericType)
    EMF_TRY_VEC_DCAST(EAnnotation)
    EMF_TRY_VEC_DCAST(EPackage)
    EMF_TRY_VEC_DCAST(EFactory)
    #undef EMF_TRY_VEC_DCAST
    return result;
}

// 检查 std::any 是否持有 EObject 派生类指针（按类型判断，不依赖值是否为 null）
bool holdsEObject(const std::any& v) {
    if (!v.has_value()) return false;
    const std::type_info& ti = v.type();
    if (ti == typeid(::emf::common::EObject*)) return true;
    #define EMF_CHECK(Type) if (ti == typeid(::emf::ecore::Type*)) return true;
    EMF_CHECK(EModelElement)
    EMF_CHECK(ENamedElement)
    EMF_CHECK(ETypedElement)
    EMF_CHECK(EClassifier)
    EMF_CHECK(EClass)
    EMF_CHECK(EDataType)
    EMF_CHECK(EEnum)
    EMF_CHECK(EEnumLiteral)
    EMF_CHECK(EStructuralFeature)
    EMF_CHECK(EAttribute)
    EMF_CHECK(EReference)
    EMF_CHECK(EOperation)
    EMF_CHECK(EParameter)
    EMF_CHECK(ETypeParameter)
    EMF_CHECK(EGenericType)
    EMF_CHECK(EAnnotation)
    EMF_CHECK(EPackage)
    EMF_CHECK(EFactory)
    #undef EMF_CHECK
    return false;
}

// std::any → bool（对齐 AQL truthiness）
// bool 直接取；非空字符串/非零数/EObject 视为 true；空值视为 false。
bool toBool(const std::any& v) {
    if (!v.has_value()) return false;
    const std::type_info& ti = v.type();
    if (ti == typeid(bool)) return std::any_cast<bool>(v);
    if (ti == typeid(std::string)) return !std::any_cast<std::string>(v).empty();
    if (ti == typeid(long)) return std::any_cast<long>(v) != 0;
    if (ti == typeid(int)) return std::any_cast<int>(v) != 0;
    if (ti == typeid(int64_t)) return std::any_cast<int64_t>(v) != 0;
    if (ti == typeid(double)) return std::any_cast<double>(v) != 0.0;
    if (ti == typeid(float)) return std::any_cast<float>(v) != 0.0f;
    if (ti == typeid(::emf::common::EObject*)) return std::any_cast<::emf::common::EObject*>(v) != nullptr;
    // EObject 派生类指针
    if (auto* o = asEObject(v)) return true;
    return false;
}

}  // anonymous namespace

AcceleoEngine::AcceleoEngine() {
    ::emf::ecore::EcorePackage::initialize();
}

void AcceleoEngine::registerService(const std::string& name, ServiceFn fn) {
    services_[name] = std::move(fn);
}

bool AcceleoEngine::hasService(const std::string& name) const {
    return services_.find(name) != services_.end();
}

ServiceFn AcceleoEngine::getService(const std::string& name) const {
    auto it = services_.find(name);
    if (it == services_.end()) return nullptr;
    return it->second;
}

void AcceleoEngine::setModuleQueries(const std::vector<std::shared_ptr<Query>>& queries) {
    queries_.clear();
    for (auto& q : queries) {
        if (q) queries_[q->name] = q;
    }
}

const Query* AcceleoEngine::lookupQuery(const std::string& name) const {
    // 先查本模块 queries_
    auto it = queries_.find(name);
    if (it != queries_.end()) return it->second.get();
    // 沿 extends 链查找（对齐 Java: 子模块可调用父模块的 query）
    if (currentModule_) {
        for (const auto& parentName : currentModule_->extends) {
            auto mit = registeredModules_.find(parentName);
            if (mit == registeredModules_.end()) continue;
            for (auto& q : mit->second->queries) {
                if (q && q->name == name) return q.get();
            }
        }
    }
    return nullptr;
}

void AcceleoEngine::registerModule(const std::shared_ptr<Module>& m) {
    if (m) registeredModules_[m->name] = m;
}

void AcceleoEngine::setCurrentModule(const std::shared_ptr<Module>& m) {
    currentModule_ = m;
    // 同时绑定本模块 queries
    if (m) setModuleQueries(m->queries);
}

std::shared_ptr<Template> AcceleoEngine::lookupTemplate(const std::string& name) const {
    // 先查本模块
    if (currentModule_) {
        for (auto& t : currentModule_->templates) {
            if (t && t->name == name) return t;
        }
        // 沿 extends 链查找（对齐 Java: 子模块可调用父模块的 template）
        for (const auto& parentName : currentModule_->extends) {
            auto mit = registeredModules_.find(parentName);
            if (mit == registeredModules_.end()) continue;
            for (auto& t : mit->second->templates) {
                if (t && t->name == name) return t;
            }
        }
    }
    return nullptr;
}

std::string AcceleoEngine::anyToString(const std::any& v) {
    if (!v.has_value()) return "";
    if (v.type() == typeid(std::string)) return std::any_cast<std::string>(v);
    if (v.type() == typeid(const char*)) return std::any_cast<const char*>(v);
    if (v.type() == typeid(bool)) return std::any_cast<bool>(v) ? "true" : "false";
    if (v.type() == typeid(int)) return std::to_string(std::any_cast<int>(v));
    if (v.type() == typeid(long)) return std::to_string(std::any_cast<long>(v));
    if (v.type() == typeid(int64_t)) return std::to_string(std::any_cast<int64_t>(v));
    if (v.type() == typeid(double)) {
        double d = std::any_cast<double>(v);
        std::ostringstream oss; oss << d; return oss.str();
    }
    if (v.type() == typeid(float)) {
        float f = std::any_cast<float>(v);
        std::ostringstream oss; oss << f; return oss.str();
    }
    // collection: collect 返回 vector<any>
    // 对齐 AQL Sequence/Bag toString: Sequence{a, b, c}
    if (v.type() == typeid(std::vector<std::any>)) {
        const auto& col = std::any_cast<std::vector<std::any>>(v);
        std::string s = "Sequence{";
        for (size_t i = 0; i < col.size(); ++i) {
            if (i) s += ", ";
            s += anyToString(col[i]);
        }
        s += "}";
        return s;
    }
    // EObject 集合（eGet 多值 feature 返回的精确 vector<派生类*>）
    // 对齐 AQL: 直接输出 collection 的 toString
    if (v.type() == typeid(std::vector<::emf::common::EObject*>)) {
        const auto& col = std::any_cast<std::vector<::emf::common::EObject*>>(v);
        std::string s = "Sequence{";
        for (size_t i = 0; i < col.size(); ++i) {
            if (i) s += ", ";
            // 取每个 EObject 的 name 属性
            auto* cls = col[i] ? col[i]->eClass() : nullptr;
            if (cls) {
                auto* nameFeat = cls->getEStructuralFeature("name");
                if (nameFeat) {
                    auto nv = col[i]->eGet(nameFeat);
                    if (nv.type() == typeid(std::string)) s += std::any_cast<std::string>(nv);
                    else s += "<EObject>";
                } else s += "<EObject>";
            } else s += "<null>";
        }
        s += "}";
        return s;
    }
    if (v.type() == typeid(::emf::common::EObject*)) {
        auto* obj = std::any_cast<::emf::common::EObject*>(v);
        if (!obj) return "";
        // EObject 默认 toString：取 name 属性若存在
        auto* cls = obj->eClass();
        if (cls) {
            auto* nameFeat = cls->getEStructuralFeature("name");
            if (nameFeat) {
                auto nameVal = obj->eGet(nameFeat);
                if (nameVal.type() == typeid(std::string)) {
                    return std::any_cast<std::string>(nameVal);
                }
            }
        }
        return "<EObject>";
    }
    // EObject 派生类指针（EClass*/EPackage*/...）：std::any 保存精确类型，
    // 走 asEObject 提取为 EObject* 后反射取 name。
    if (auto* obj = asEObject(v)) {
        auto* cls = obj->eClass();
        if (cls) {
            auto* nameFeat = cls->getEStructuralFeature("name");
            if (nameFeat) {
                auto nameVal = obj->eGet(nameFeat);
                if (nameVal.type() == typeid(std::string)) {
                    return std::any_cast<std::string>(nameVal);
                }
            }
        }
        return "<EObject>";
    }
    return "<unknown>";
}

std::any AcceleoEngine::evalExpr(const Expr& e, EvalContext& ctx) {
    return std::visit([&](auto& n) -> std::any {
        using T = std::decay_t<decltype(n)>;
        if constexpr (std::is_same_v<T, VarExpr>) {
            if (n.name == "self" || n.name == "Self") {
                const std::any* s = ctx.lookup("self");
                return s ? *s : std::any{};
            }
            const std::any* v = ctx.lookup(n.name);
            if (v) return *v;
            // 未知变量 → 空
            return std::any{};
        } else if constexpr (std::is_same_v<T, StringLitExpr>) {
            return std::any{n.value};
        } else if constexpr (std::is_same_v<T, IntLitExpr>) {
            return std::any{n.value};
        } else if constexpr (std::is_same_v<T, BoolLitExpr>) {
            return std::any{n.value};
        } else if constexpr (std::is_same_v<T, NavExpr>) {
            // expr.name → eGet(feature)
            std::any target = n.target ? evalExpr(*n.target, ctx) : std::any{};
            // target 可能持有 EObject* 或其派生类指针（EClass*/EPackage*/...）
            // 用 asEObject 统一提取为 EObject*，再走反射 eGet
            if (auto* obj = asEObject(target)) {
                auto* cls = obj->eClass();
                if (cls) {
                    auto* feat = cls->getEStructuralFeature(n.name);
                    if (feat) return obj->eGet(feat);
                }
            }
            return std::any{};
        } else if constexpr (std::is_same_v<T, CallExpr>) {
            // ->name(args) 或 .name(args)
            std::any target = n.target ? evalExpr(*n.target, ctx) : std::any{};
            // 内置集合操作
            if (n.arrow) {
                if (n.name == "size") {
                    // 字符串长度
                    if (target.type() == typeid(std::string)) {
                        return std::any{static_cast<long>(std::any_cast<std::string>(target).size())};
                    }
                    // EObject 集合（含派生类 vector 与 EList<EObject*>*）
                    auto col = asEObjectVector(target);
                    if (!col.empty() || holdsEObject(target) ||
                        target.type() == typeid(std::vector<::emf::common::EObject*>) ||
                        target.type() == typeid(::emf::common::EList<::emf::common::EObject*>*)) {
                        return std::any{static_cast<long>(col.size())};
                    }
                    return std::any{0L};
                }
                if (n.name == "isEmpty") {
                    if (target.type() == typeid(std::string)) {
                        return std::any{std::any_cast<std::string>(target).empty()};
                    }
                    auto col = asEObjectVector(target);
                    return std::any{col.empty()};
                }
                if (n.name == "first" || n.name == "last") {
                    auto col = asEObjectVector(target);
                    if (col.empty()) return std::any{};
                    // 返回统一为 EObject*，避免下游再处理派生类型
                    return std::any{static_cast<::emf::common::EObject*>(
                        n.name == "first" ? col.front() : col.back())};
                }
                if (n.name == "collect" || n.name == "select" || n.name == "reject" ||
                    n.name == "forAll" || n.name == "exists") {
                    // 这些操作需要 lambda 参数：collect(e | expr) / select(e | cond) 等
                    // 对齐 Java AQL Set/Bag 的 collect/select/reject/forAll/exists
                    auto col = asEObjectVector(target);
                    // 期望首个参数为 LambdaExpr
                    if (n.args.empty() || !n.args[0]) {
                        // 无 lambda：按操作语义降级
                        if (n.name == "forAll") return std::any{true};
                        if (n.name == "exists") return std::any{false};
                        return std::any{col};
                    }
                    auto& arg = n.args[0]->node;
                    auto* lam = std::get_if<LambdaExpr>(&arg);
                    if (!lam) {
                        // 参数不是 lambda：降级
                        if (n.name == "forAll") return std::any{true};
                        if (n.name == "exists") return std::any{false};
                        return std::any{col};
                    }
                    const std::string& varName = lam->varName;
                    ExprPtr body = lam->body;

                    if (n.name == "collect") {
                        // collect(e | expr) → 对每个元素求值 body，收集结果
                        // 对齐 AQL: collect 返回 collection，元素类型可以是任意
                        // （EObject、String、Integer 等）。C++ 端用 vector<any> 保存。
                        std::vector<std::any> out;
                        for (auto* elem : col) {
                            EvalContext child; child.parent = &ctx;
                            child.set(varName, std::any{elem});
                            std::any v = body ? evalExpr(*body, child) : std::any{};
                            if (v.has_value()) out.push_back(v);
                        }
                        return std::any{out};
                    }
                    if (n.name == "select") {
                        // select(e | cond) → 保留 cond 为 true 的元素
                        std::vector<::emf::common::EObject*> out;
                        for (auto* elem : col) {
                            EvalContext child; child.parent = &ctx;
                            child.set(varName, std::any{elem});
                            std::any v = body ? evalExpr(*body, child) : std::any{};
                            if (toBool(v)) out.push_back(elem);
                        }
                        return std::any{out};
                    }
                    if (n.name == "reject") {
                        // reject(e | cond) → 排除 cond 为 true 的元素
                        std::vector<::emf::common::EObject*> out;
                        for (auto* elem : col) {
                            EvalContext child; child.parent = &ctx;
                            child.set(varName, std::any{elem});
                            std::any v = body ? evalExpr(*body, child) : std::any{};
                            if (!toBool(v)) out.push_back(elem);
                        }
                        return std::any{out};
                    }
                    if (n.name == "forAll") {
                        // forAll(e | cond) → 所有元素 cond 都为 true
                        for (auto* elem : col) {
                            EvalContext child; child.parent = &ctx;
                            child.set(varName, std::any{elem});
                            std::any v = body ? evalExpr(*body, child) : std::any{};
                            if (!toBool(v)) return std::any{false};
                        }
                        return std::any{true};
                    }
                    if (n.name == "exists") {
                        // exists(e | cond) → 至少一个元素 cond 为 true
                        for (auto* elem : col) {
                            EvalContext child; child.parent = &ctx;
                            child.set(varName, std::any{elem});
                            std::any v = body ? evalExpr(*body, child) : std::any{};
                            if (toBool(v)) return std::any{true};
                        }
                        return std::any{false};
                    }
                }
            }
            // 服务调用
            if (n.name == "==" || n.name == "!=") {
                // 相等比较（对齐 AQL = / == / <> / !=）
                // 注意：parser 把二元运算符构造成 CallExpr{nullptr, op, {left, right}, false}
                // （target=nullptr，两个操作数在 args[0] 和 args[1]），
                // 而非 CallExpr{left, op, {right}}。这里按 args 取两边的值。
                std::any l = (n.args.size() >= 1) ? evalExpr(*n.args[0], ctx) : std::any{};
                std::any r = (n.args.size() >= 2) ? evalExpr(*n.args[1], ctx)
                          : (n.target ? evalExpr(*n.target, ctx) : std::any{});
                bool eq = false;
                // 字符串比较
                if (l.type() == typeid(std::string) && r.type() == typeid(std::string)) {
                    eq = std::any_cast<std::string>(l) == std::any_cast<std::string>(r);
                } else if (holdsEObject(l) && holdsEObject(r)) {
                    // EObject 派生类指针比较（统一提为 EObject* 后比地址）
                    eq = asEObject(l) == asEObject(r);
                } else if (l.type() == typeid(bool) && r.type() == typeid(bool)) {
                    eq = std::any_cast<bool>(l) == std::any_cast<bool>(r);
                } else if (l.type() == typeid(long) && r.type() == typeid(long)) {
                    eq = std::any_cast<long>(l) == std::any_cast<long>(r);
                } else if (l.type() == typeid(int) && r.type() == typeid(int)) {
                    eq = std::any_cast<int>(l) == std::any_cast<int>(r);
                } else if (l.type() == typeid(long) && r.type() == typeid(int)) {
                    eq = std::any_cast<long>(l) == static_cast<long>(std::any_cast<int>(r));
                } else if (l.type() == typeid(int) && r.type() == typeid(long)) {
                    eq = static_cast<long>(std::any_cast<int>(l)) == std::any_cast<long>(r);
                } else {
                    eq = !l.has_value() && !r.has_value();
                }
                return std::any{n.name == "==" ? eq : !eq};
            }
            if (n.name == "and") {
                // and/or 同样是 {left, right} 形式
                std::any l = (n.args.size() >= 1) ? evalExpr(*n.args[0], ctx) : std::any{};
                bool lb = false;
                if (l.type() == typeid(bool)) lb = std::any_cast<bool>(l);
                else if (l.has_value()) lb = true;
                if (!lb) return std::any{false};
                std::any r = (n.args.size() >= 2) ? evalExpr(*n.args[1], ctx) : std::any{};
                bool rb = false;
                if (r.type() == typeid(bool)) rb = std::any_cast<bool>(r);
                else if (r.has_value()) rb = true;
                return std::any{rb};
            }
            if (n.name == "or") {
                std::any l = (n.args.size() >= 1) ? evalExpr(*n.args[0], ctx) : std::any{};
                bool lb = false;
                if (l.type() == typeid(bool)) lb = std::any_cast<bool>(l);
                else if (l.has_value()) lb = true;
                if (lb) return std::any{true};
                std::any r = (n.args.size() >= 2) ? evalExpr(*n.args[1], ctx) : std::any{};
                bool rb = false;
                if (r.type() == typeid(bool)) rb = std::any_cast<bool>(r);
                else if (r.has_value()) rb = true;
                return std::any{rb};
            }
            // 字符串拼接：+ 用于字符串
            // 注意：parser 把 +/- 构造成 CallExpr{left, op, {right}}（target=left，args[0]=right），
            // 与 == / and / or 的 {left, right} 形式不同。
            if (n.name == "+" && n.target) {
                std::any l = evalExpr(*n.target, ctx);
                std::any r = n.args.empty() ? std::any{} : evalExpr(*n.args[0], ctx);
                if (l.type() == typeid(std::string) || r.type() == typeid(std::string)) {
                    return std::any{anyToString(l) + anyToString(r)};
                }
                // 数字加法
                if (l.type() == typeid(long) && r.type() == typeid(long)) {
                    return std::any{std::any_cast<long>(l) + std::any_cast<long>(r)};
                }
                if (l.type() == typeid(int) && r.type() == typeid(int)) {
                    return std::any{std::any_cast<int>(l) + std::any_cast<int>(r)};
                }
                return std::any{anyToString(l) + anyToString(r)};
            }
            if (n.name == "-" && n.target) {
                std::any l = evalExpr(*n.target, ctx);
                std::any r = n.args.empty() ? std::any{} : evalExpr(*n.args[0], ctx);
                if (l.type() == typeid(long) && r.type() == typeid(long)) {
                    return std::any{std::any_cast<long>(l) - std::any_cast<long>(r)};
                }
                if (l.type() == typeid(int) && r.type() == typeid(int)) {
                    return std::any{std::any_cast<int>(l) - std::any_cast<int>(r)};
                }
                return std::any{0L};
            }
            // 服务调用
            if (hasService(n.name)) {
                std::vector<std::any> args;
                for (auto& a : n.args) args.push_back(evalExpr(*a, ctx));
                return getService(n.name)(args, ctx);
            }
            // 模块级 query 调用：queryName(args)
            // 对齐 Java: 模块内 query 可被模板表达式调用，按位置绑定参数后求值 body
            if (!n.target) {
                const Query* q = lookupQuery(n.name);
                if (q) {
                    // 求值参数
                    std::vector<std::any> args;
                    for (auto& a : n.args) args.push_back(evalExpr(*a, ctx));
                    // 绑定参数到子作用域
                    EvalContext child; child.parent = &ctx;
                    for (size_t i = 0; i < q->params.size() && i < args.size(); ++i) {
                        child.set(q->params[i].name, args[i]);
                    }
                    // query body 必须存在
                    if (q->body) return evalExpr(*q->body, child);
                    return std::any{};
                }
            }
            // 模板调用：[templateName(args)/] 块的 expr 是 CallExpr{nullptr, name, args}
            // 对齐 Java: 模板可调用本模块或 extends 链上的其他模板
            if (!n.target) {
                auto tpl = lookupTemplate(n.name);
                if (tpl) {
                    std::vector<std::any> args;
                    for (auto& a : n.args) args.push_back(evalExpr(*a, ctx));
                    // 求值被调用模板，返回生成的文本
                    std::string sub = evaluate(*tpl, args);
                    return std::any{sub};
                }
            }
            // .name(args) on EObject：尝试 eInvoke（罕见，忽略）
            return std::any{};
        } else if constexpr (std::is_same_v<T, CollectionLitExpr>) {
            // 集合字面量：返回 vector<any>
            std::vector<std::any> vals;
            for (auto& el : n.elements) vals.push_back(evalExpr(*el, ctx));
            return std::any{vals};
        } else if constexpr (std::is_same_v<T, IfExpr>) {
            std::any c = evalExpr(*n.cond, ctx);
            bool isTrue = false;
            if (c.type() == typeid(bool)) isTrue = std::any_cast<bool>(c);
            else if (c.type() == typeid(std::string)) isTrue = !std::any_cast<std::string>(c).empty();
            else if (c.has_value()) isTrue = true;
            return isTrue ? (n.thenExpr ? evalExpr(*n.thenExpr, ctx) : std::any{})
                          : (n.elseExpr ? evalExpr(*n.elseExpr, ctx) : std::any{});
        }
        return std::any{};
    }, e.node);
}

void AcceleoEngine::evalBlock(const Block& b, EvalContext& ctx, std::string& out) {
    std::visit([&](auto& n) {
        using T = std::decay_t<decltype(n)>;
        if constexpr (std::is_same_v<T, TextBlock>) {
            out += n.text;
        } else if constexpr (std::is_same_v<T, ExprBlock>) {
            if (n.expr) out += anyToString(evalExpr(*n.expr, ctx));
        } else if constexpr (std::is_same_v<T, ForBlock>) {
            // 求值集合
            std::vector<::emf::common::EObject*> col;
            if (n.collection) {
                std::any c = evalExpr(*n.collection, ctx);
                // eGet 多值 feature 返回精确 std::vector<派生类*>，
                // 走 asEObjectVector 统一提取为 vector<EObject*>。
                col = asEObjectVector(c);
            }
            // 子作用域
            EvalContext child; child.parent = &ctx;
            for (size_t i = 0; i < col.size(); ++i) {
                // 绑定迭代变量为 EObject*（统一类型，下游 NavExpr 走 asEObject 反射）
                child.set(n.varName, std::any{col[i]});
                std::string body;
                evalBlocks(n.body, child, body);
                out += body;
                // separator
                if (n.hasSeparator && i + 1 < col.size()) {
                    out += n.separator;
                }
            }
        } else if constexpr (std::is_same_v<T, IfBlock>) {
            std::any c = n.cond ? evalExpr(*n.cond, ctx) : std::any{};
            bool isTrue = false;
            if (c.type() == typeid(bool)) isTrue = std::any_cast<bool>(c);
            else if (c.type() == typeid(std::string)) isTrue = !std::any_cast<std::string>(c).empty();
            else if (c.has_value()) isTrue = true;
            if (isTrue) {
                evalBlocks(n.thenBody, ctx, out);
            } else {
                bool done = false;
                for (auto& [condExpr, body] : n.elseIfs) {
                    std::any c2 = evalExpr(*condExpr, ctx);
                    bool t2 = false;
                    if (c2.type() == typeid(bool)) t2 = std::any_cast<bool>(c2);
                    else if (c2.has_value()) t2 = true;
                    if (t2) { evalBlocks(body, ctx, out); done = true; break; }
                }
                if (!done) evalBlocks(n.elseBody, ctx, out);
            }
        } else if constexpr (std::is_same_v<T, LetBlock>) {
            EvalContext child; child.parent = &ctx;
            if (n.value) child.set(n.varName, evalExpr(*n.value, ctx));
            evalBlocks(n.body, child, out);
        } else if constexpr (std::is_same_v<T, FileBlock>) {
            // 求值 body，写入文件（不走 out）
            // body 内的 [protected] 块会通过 ProtectedBlock 分支输出带标记的内容；
            // 若目标文件已存在，则用旧文件的 protected 区内容替换新生成的同名区。
            std::string body;
            EvalContext child; child.parent = &ctx;
            evalBlocks(n.body, child, body);
            // path
            std::string path;
            if (n.path) path = anyToString(evalExpr(*n.path, ctx));
            if (!path.empty() && !outDir_.empty()) {
                fs::path full = fs::path(outDir_) / path;
                fs::create_directories(full.parent_path());
                if (n.append) {
                    // append 模式：直接追加，不做 protected 区合并
                    std::ofstream f(full, std::ios::app);
                    if (f) f << body;
                } else {
                    // 非 append：若文件已存在，合并 protected 区
                    std::string merged = body;
                    if (fs::exists(full)) {
                        std::ifstream oldFile(full);
                        if (oldFile) {
                            std::ostringstream oss;
                            oss << oldFile.rdbuf();
                            std::string oldContent = oss.str();
                            merged = mergeProtectedRegions(body, oldContent);
                        }
                    }
                    std::ofstream f(full, std::ios::out);
                    if (f) f << merged;
                }
            }
        } else if constexpr (std::is_same_v<T, ProtectedBlock>) {
            // 受保护区：用 BEGIN/END 标记包围 body 内容输出。
            // 对齐 Java Acceleo: protected 区在文件中以
            //   // BEGIN Begin Protected Region ID[id]
            //   <用户/生成内容>
            //   // END End Protected Region ID[id]
            // 形式存在。再次生成时，mergeProtectedRegions 会从旧文件提取该区内容。
            std::string id = n.id;
            if (id.empty()) {
                // 默认 id：用上下文中的模板名（若没有则用计数）
                const std::any* self = ctx.lookup("self");
                (void)self;
                id = "default";
            }
            out += "// BEGIN Begin Protected Region ID[" + id + "]\n";
            std::string body;
            evalBlocks(n.body, ctx, body);
            out += body;
            out += "// END End Protected Region ID[" + id + "]\n";
        }
    }, b.node);
}

void AcceleoEngine::evalBlocks(const std::vector<BlockPtr>& blocks,
                               EvalContext& ctx, std::string& out) {
    for (auto& b : blocks) {
        if (b) evalBlock(*b, ctx, out);
    }
}

std::string AcceleoEngine::evaluate(const Template& tpl,
                                    const std::vector<std::any>& args) {
    EvalContext ctx;
    // 绑定参数
    for (size_t i = 0; i < tpl.params.size() && i < args.size(); ++i) {
        ctx.set(tpl.params[i].name, args[i]);
    }
    // 若有 self 未绑定，用第一个参数（对齐 Java 行为）
    if (!ctx.lookup("self") && !args.empty()) {
        ctx.set("self", args[0]);
    }
    std::string out;
    evalBlocks(tpl.body, ctx, out);
    return out;
}

void AcceleoEngine::doGenerate(const std::shared_ptr<Module>& module,
                               ::emf::common::EObject* model,
                               const std::string& outDir) {
    outDir_ = outDir;
    if (!fs::exists(outDir_)) fs::create_directories(outDir_);
    // 设置当前主模块（建立 extends 链查找上下文 + 绑定 queries）
    setCurrentModule(module);
    // 查找主模板（通常是第一个模板，或标记为 main 的）
    for (auto& t : module->templates) {
        std::vector<std::any> args;
        if (model) args.push_back(std::any{model});
        evaluate(*t, args);
    }
}

// 合并 protected 区。
// 对齐 Java org.eclipse.acceleo.engine.generation.AcceleoEngine 的 protected area 合并：
//   新生成内容中每个 ID[id] 的 BEGIN/END 区，若旧文件存在同 ID 区，
//   则用旧文件中区的内容（用户手改的部分）替换新生成的内容。
// BEGIN/END 标记格式（与 ProtectedBlock 分支输出一致）：
//   // BEGIN Begin Protected Region ID[id]
//   <内容>
//   // END End Protected Region ID[id]
std::string AcceleoEngine::mergeProtectedRegions(const std::string& newContent,
                                                  const std::string& oldContent) {
    // 1. 从 oldContent 提取所有 ID[id] → 区内容
    std::unordered_map<std::string, std::string> oldRegions;
    const std::string beginPrefix = "BEGIN Begin Protected Region ID[";
    const std::string endPrefix = "END End Protected Region ID[";
    size_t searchFrom = 0;
    while (true) {
        size_t b = oldContent.find(beginPrefix, searchFrom);
        if (b == std::string::npos) break;
        // 提取 id：从 b + beginPrefix.size() 到 ]
        size_t idStart = b + beginPrefix.size();
        size_t idEnd = oldContent.find(']', idStart);
        if (idEnd == std::string::npos) break;
        std::string id = oldContent.substr(idStart, idEnd - idStart);
        // 区内容从 idEnd+1 后的换行开始
        size_t contentStart = idEnd + 1;
        if (contentStart < oldContent.size() && oldContent[contentStart] == '\n') ++contentStart;
        else if (contentStart + 1 < oldContent.size() &&
                 oldContent[contentStart] == '\r' && oldContent[contentStart + 1] == '\n') {
            contentStart += 2;
        }
        // 找对应 END（同 id）
        std::string endMarker = endPrefix + id + "]";
        size_t e = oldContent.find(endMarker, contentStart);
        if (e == std::string::npos) { searchFrom = idEnd + 1; continue; }
        // 区内容：contentStart 到 e，去掉结尾换行
        size_t contentEnd = e;
        if (contentEnd > 0 && oldContent[contentEnd - 1] == '\n') --contentEnd;
        if (contentEnd > 0 && oldContent[contentEnd - 1] == '\r') --contentEnd;
        // 去掉行首的注释前缀（// ）
        size_t lineStart = contentEnd;
        while (lineStart > contentStart && oldContent[lineStart - 1] != '\n') --lineStart;
        // 如果行首是 "// END"（END 标记所在行），则 contentEnd 退到此行首
        // 简化：直接取 contentStart 到 contentEnd
        oldRegions[id] = oldContent.substr(contentStart, contentEnd - contentStart);
        searchFrom = e + endMarker.size();
    }

    if (oldRegions.empty()) return newContent;

    // 2. 在 newContent 中查找同 ID 区，用 oldRegions[id] 替换内容
    std::string result;
    size_t pos = 0;
    while (pos < newContent.size()) {
        size_t b = newContent.find(beginPrefix, pos);
        if (b == std::string::npos) {
            result += newContent.substr(pos);
            break;
        }
        // 追加 b 之前的内容
        result += newContent.substr(pos, b - pos);
        // 提取 id
        size_t idStart = b + beginPrefix.size();
        size_t idEnd = newContent.find(']', idStart);
        if (idEnd == std::string::npos) {
            result += newContent.substr(b);
            break;
        }
        std::string id = newContent.substr(idStart, idEnd - idStart);
        // 找 END
        std::string endMarker = endPrefix + id + "]";
        size_t e = newContent.find(endMarker, idEnd);
        if (e == std::string::npos) {
            result += newContent.substr(b);
            break;
        }
        // 输出 BEGIN 标记行
        size_t beginLineEnd = newContent.find('\n', b);
        if (beginLineEnd == std::string::npos || beginLineEnd > e) beginLineEnd = idEnd;
        result += newContent.substr(b, beginLineEnd - b + 1);
        // 输出区内容：优先用 old，没有则用 new 的内容
        auto it = oldRegions.find(id);
        if (it != oldRegions.end()) {
            result += it->second;
            if (!it->second.empty() && it->second.back() != '\n') result += '\n';
        } else {
            // new 内容
            size_t contentStart = beginLineEnd + 1;
            size_t contentEnd = e;
            if (contentEnd > 0 && newContent[contentEnd - 1] == '\n') --contentEnd;
            result += newContent.substr(contentStart, contentEnd - contentStart);
            if (contentEnd > contentStart && newContent[contentEnd] != '\n') result += '\n';
        }
        // 找 END 标记行尾
        size_t endLineEnd = newContent.find('\n', e);
        if (endLineEnd == std::string::npos) {
            result += newContent.substr(e);
            break;
        }
        result += newContent.substr(e, endLineEnd - e + 1);
        pos = endLineEnd + 1;
    }
    return result;
}

// ===== AcceleoService =====

AcceleoService::AcceleoService() {
    ::emf::ecore::EcorePackage::initialize();
}

void AcceleoService::registerService(const std::string& name, ServiceFn fn) {
    engine_.registerService(name, std::move(fn));
}

void AcceleoService::doGenerate(const std::string& mtlSource,
                                ::emf::common::EObject* model,
                                const std::string& outDir) {
    auto module = AcceleoParser::parse(mtlSource);
    // 设置当前主模块（绑定 queries + 建立 extends 链上下文）
    engine_.setCurrentModule(module);
    engine_.doGenerate(module, model, outDir);
}

std::string AcceleoService::evaluateTemplate(const std::string& mtlSource,
                                             const std::string& templateName,
                                             const std::vector<std::any>& args) {
    auto module = AcceleoParser::parse(mtlSource);
    // 设置当前主模块（绑定 queries + 建立 extends 链上下文）
    engine_.setCurrentModule(module);
    for (auto& t : module->templates) {
        if (t->name == templateName) {
            return engine_.evaluate(*t, args);
        }
    }
    throw AcceleoParseException("template not found: " + templateName);
}

}  // namespace emf::acceleo
