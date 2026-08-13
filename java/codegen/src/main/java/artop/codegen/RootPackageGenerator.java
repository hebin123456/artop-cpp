package artop.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;

/**
 * 根 Package 生成器
 *
 * 生成 artop 4.13 风格的根 Package 类（如 autosar40.util.Autosar40Package 或
 * gautosar.util.GAutosarPackage），包含：
 * - 继承 org.eclipse.emf.ecore.impl.EPackageImpl
 * - eNAME / eNS_URI / eCONTENT_TYPE 常量
 * - eINSTANCE 单例
 * - init() 方法
 * - getEClass(String) / getEClass(Class) 方法
 * - getESubClasses(EClass) / getEAllSubClasses(EClass) 方法
 * - getEClassifiers() 方法
 *
 * 这个类是整个元模型的入口点，管理所有子包。
 *
 * 命名规则：
 * - autosar40 -> Autosar40Package（首字母大写）
 * - gautosar -> GAutosarPackage（G 大写，A 大写 - 单字母前缀特殊处理）
 */
public final class RootPackageGenerator {

    private final CodegenContext ctx;

    public RootPackageGenerator(CodegenContext ctx) {
        this.ctx = ctx;
    }

    public void generate() {
        EPackage root = ctx.rootPackage;
        String javaPkg = TypeUtils.packagePath(root) + ".util";
        String rootName = rootPackageClassName(root.getName());
        String className = rootName + "Package";

        StringBuilder sb = new StringBuilder();
        sb.append("/**\n * <!-- begin-user-doc -->\n * <!-- end-user-doc -->\n");
        sb.append(" * @generated\n */\n");
        sb.append("package ").append(javaPkg).append(";\n\n");

        TreeSet<String> imports = new TreeSet<>();
        imports.add("java.util.ArrayList");
        imports.add("java.util.Collection");
        imports.add("java.util.HashMap");
        imports.add("java.util.List");
        imports.add("java.util.Map");
        imports.add("org.eclipse.emf.common.util.BasicEList");
        imports.add("org.eclipse.emf.common.util.EList");
        imports.add("org.eclipse.emf.ecore.EClass");
        imports.add("org.eclipse.emf.ecore.EClassifier");
        imports.add("org.eclipse.emf.ecore.EPackage");
        imports.add("org.eclipse.emf.ecore.impl.EPackageImpl");
        for (String imp : imports) {
            sb.append("import ").append(imp).append(";\n");
        }
        sb.append("\n");

        sb.append("/**\n");
        sb.append(" * <!-- begin-user-doc -->\n");
        sb.append(" * The <b>Package</b> for the model.\n");
        sb.append(" * It contains accessors for the meta objects.\n");
        sb.append(" * <!-- end-user-doc -->\n");
        sb.append(" * @generated\n");
        sb.append(" */\n");
        sb.append("public class ").append(className).append(" extends EPackageImpl {\n\n");

        // 常量
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The package name.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("public static final String eNAME = \"").append(NamingUtils.escapeJavaString(root.getName())).append("\";\n\n");

        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The package namespace URI.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        // eNS_URI：对齐 artop 官方，去掉根包名后缀。
        // ecore 中根包 nsURI 形如 "http://autosar.org/schema/r4.0/autosar40"，
        // 但 artop 生成的 Autosar40Package.eNS_URI 是 "http://autosar.org/schema/r4.0"，
        // 以匹配 Autosar40ReleaseDescriptor.getNamespace()。
        // 否则 AutosarResourceFactoryImpl 构造时 initRootEPackage() 找不到根包。
        String rootNsUri = root.getNsURI();
        String rootPkgName = root.getName();
        if (rootNsUri != null && rootPkgName != null && rootNsUri.endsWith("/" + rootPkgName)) {
            rootNsUri = rootNsUri.substring(0, rootNsUri.length() - rootPkgName.length() - 1);
        }
        sb.append(FileWriter.INDENT).append("public static final String eNS_URI = \"").append(NamingUtils.escapeJavaString(rootNsUri)).append("\";\n\n");

        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The package content type.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("public static final String eCONTENT_TYPE = \"").append(NamingUtils.escapeJavaString(ctx.contentType)).append("\";\n\n");

        // eINSTANCE - 非 final，在 init() 内部提前赋值，避免子包初始化时访问 null
        // 注意：不能写 `eINSTANCE = init()`，因为该赋值只在 init() 返回后才生效，
        // 而 init() 执行期间子包会访问 eINSTANCE，此时仍为 null。
        // 改为：字段不初始化，在 init() 内部创建包后立即赋值给 eINSTANCE。
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The singleton instance of the package.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("public static ").append(className).append(" eINSTANCE;\n\n");
        sb.append(FileWriter.INDENT).append("private static volatile boolean initializing = false;\n\n");

        // isInited 标志，防止 init() 循环递归
        sb.append(FileWriter.INDENT).append("private static boolean isInited = false;\n\n");

        // 字段
        sb.append(FileWriter.INDENT).append("private volatile EList<EClassifier> eClassifiers;\n");
        sb.append(FileWriter.INDENT).append("private volatile Map<Class<?>, EClass> javaClass2eClass;\n");
        sb.append(FileWriter.INDENT).append("private volatile Map<String, EClass> name2eClass;\n");
        sb.append(FileWriter.INDENT).append("private volatile Map<EClass, List<EClass>> eSubClasses;\n");
        // 标志：init() 完成前为 false，getEClassifiers() 返回 super 的直接 classifiers（含 stub）；
        // init() 完成后为 true，getEClassifiers() 合并所有子包的 classifiers。
        sb.append(FileWriter.INDENT).append("private volatile boolean classifiersMerged = false;\n\n");

        // init() 方法
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("public static ").append(className).append(" init() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (isInited) return (").append(className).append(")EPackage.Registry.INSTANCE.getEPackage(eNS_URI);\n\n");

        // 先创建并注册根包，设置 isInited=true，防止子包 init() 回调时无限递归
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("// Create and register the package\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(className).append(" the").append(className)
          .append(" = new ").append(className).append("();\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("the").append(className).append(".setName(eNAME);\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("the").append(className).append(".setNsURI(eNS_URI);\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("EPackage.Registry.INSTANCE.put(eNS_URI, the").append(className).append(");\n\n");
        // 关键：在初始化子包之前，先把 eINSTANCE 指向已创建的包实例。
        // 子包的 initializePackageContents() 会访问 Autosar40Package.eINSTANCE，
        // 若此时仍为 null 会抛 NPE。
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("eINSTANCE = the").append(className).append(";\n\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("isInited = true;\n\n");

        // 关键：在初始化子包之前，先创建外部类 stub（gautosar 引用）。
        // 子包的 initializePackageContents() 会通过 getEClassifier("GConfigReferenceValue")
        // 查找这些 stub，若此时还没创建会返回 null，导致 ESuperTypes.add(null) 抛异常。
        //
        // 同时，eClassifierRef 现在对 gautosar 类生成 Registry.getEPackage(nsURI).getEClassifier(name)
        // 形式的引用，需要 gautosar 包已注册到 Registry。这里强制初始化 gautosar 包。
        if (!ctx.externalClasses.isEmpty()) {
            // 强制初始化 gautosar 根包及其所有子包，使它们全部注册到 EPackage.Registry。
            // gautosar 包的 EClass（如 GAUTOSAR）包含 gChecksum/gTimestamp 等 feature，
            // 必须加载真实 EClass 而非空 stub，否则 feature ID 不匹配。
            //
            // 关键点：GAutosarPackage.eINSTANCE 只注册根包（nsURI=http://artop.org/gautosar），
            // 子包（如 http://artop.org/gautosar/pd）的注册发生在其 eINSTANCE 静态字段初始化时。
            // GAutosarPackage.getEClassifiers() 会聚合所有子包的 eINSTANCE，触发它们的静态初始化，
            // 从而将所有子包注册到 Registry。仅调用 eINSTANCE.getClass() 不会触发子包初始化。
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("// Force gautosar package initialization so real EClasses are in Registry\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("try {\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("// 触发根包静态初始化（注册 http://artop.org/gautosar）\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("gautosar.util.GAutosarPackage.eINSTANCE.getClass();\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("// 触发 getEClassifiers()，聚合所有子包 eINSTANCE，将子包注册到 Registry\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("// （如 http://artop.org/gautosar/pd, http://artop.org/gautosar/gs/in 等）\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("gautosar.util.GAutosarPackage.eINSTANCE.getEClassifiers().size();\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("} catch (Throwable t) { /* ignore if gautosar not available */ }\n\n");

            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("// Create external class stubs (gautosar references) - MUST be before subpackage init\n");
            for (EClass ext : ctx.externalClasses.values()) {
                sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("{\n");
                sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("EClass __stub = org.eclipse.emf.ecore.EcoreFactory.eINSTANCE.createEClass();\n");
                sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("__stub.setName(\"").append(NamingUtils.escapeJavaString(ext.getName())).append("\");\n");
                sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("__stub.setAbstract(").append(ext.isAbstract()).append(");\n");
                sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("__stub.setInterface(").append(ext.isInterface()).append(");\n");
                sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("the").append(className).append(".getEClassifiers().add(__stub);\n");
                sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
            }
            sb.append("\n");
        }

        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("// Initialize all subpackages\n");

        // 初始化所有子包
        for (EPackage sub : root.getESubpackages()) {
            String subImplFq = TypeUtils.packagePath(sub) + ".impl." + NamingUtils.packageImplClassName(sub.getName());
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(subImplFq).append(".init();\n");
        }
        sb.append("\n");

        // 注册子包
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("// Register subpackages\n");
        for (EPackage sub : root.getESubpackages()) {
            String subPkgFq = TypeUtils.packagePath(sub) + "." + NamingUtils.packageClassName(sub.getName());
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("the").append(className).append(".getESubpackages().add(").append(subPkgFq).append(".eINSTANCE);\n");
        }
        sb.append("\n");

        // 两阶段初始化：所有子包 init() 只注册自己到 Registry，不创建 EClassifiers。
        // 这里统一调用 createPackageContents → initializePackageContents → freeze，
        // 确保所有包的 EClassifiers 先全部创建，再统一设置跨包引用。
        // 只处理 EPackageInitImpl 实例（生成的子包），跳过 gautosar 等外部包。
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("// Phase 1: createPackageContents for all subpackages\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("for (Object pkgObj : EPackage.Registry.INSTANCE.values()) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (pkgObj instanceof org.artop.eel.common.ecore.EPackageInitImpl) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("((org.artop.eel.common.ecore.EPackageInitImpl)pkgObj).createPackageContents();\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n\n");

        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("// Phase 2: initializePackageContents for all subpackages\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("for (Object pkgObj : EPackage.Registry.INSTANCE.values()) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (pkgObj instanceof org.artop.eel.common.ecore.EPackageInitImpl) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("((org.artop.eel.common.ecore.EPackageInitImpl)pkgObj).initializePackageContents();\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n\n");

        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("// Phase 3: freeze all packages (root + subpackages)\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("the").append(className).append(".freeze();\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("for (Object pkgObj : EPackage.Registry.INSTANCE.values()) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (pkgObj instanceof org.eclipse.emf.ecore.impl.EPackageImpl) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("((org.eclipse.emf.ecore.impl.EPackageImpl)pkgObj).freeze();\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n\n");

        // 标记初始化完成，之后 getEClassifiers() 会合并所有子包的 classifiers
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("the").append(className).append(".classifiersMerged = true;\n\n");

        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return the").append(className).append(";\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // 静态初始化块：类加载时触发 init()，等价于原来的 `eINSTANCE = init()`
        // 但 init() 内部会先赋值 eINSTANCE，避免子包初始化时 NPE
        sb.append(FileWriter.INDENT).append("static {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("init();\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // 构造器
        // 必须传入 Factory.eINSTANCE，否则 getEFactoryInstance() 返回 null，
        // 导致 SAXXMLHandler 的 getFactoryForPrefix 返回 null，helper.getType(null, name) 返回 null，
        // 抛出 ClassNotFoundException。
        String factoryClass = rootName + "Factory";
        sb.append(FileWriter.INDENT).append("private ").append(className).append("() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("super(eNS_URI, ").append(factoryClass).append(".eINSTANCE);\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // getEClass(String) - 基于 getEClassifiers() 查找
        // getEClassifiers() 在 init() 完成后会合并 Registry 中所有子包的 classifiers，
        // 所以这里直接遍历 getEClassifiers() 即可找到所有 EClass。
        sb.append(FileWriter.INDENT).append("public EClass getEClass(String name) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (name2eClass == null) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("synchronized (this) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (name2eClass == null) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("Map<String, EClass> map = new HashMap<>();\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("for (EClassifier c : getEClassifiers()) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (c instanceof EClass) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("map.put(c.getName(), (EClass) c);\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("name2eClass = map;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return name2eClass.get(name);\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // getEClassifier override - 基于 getEClassifiers() 查找
        // SAXXMLHandler 通过 ExtendedMetaData.getType -> EPackage.getEClassifiers() 查找 EClass。
        // getEClassifier(String) 也被 EMF 内部使用，这里统一基于 getEClassifiers() 查找。
        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("public EClassifier getEClassifier(String name) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("for (EClassifier c : getEClassifiers()) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (name.equals(c.getName())) return c;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return null;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // getEClass(Class) - 基于 getEClassifiers() 查找
        sb.append(FileWriter.INDENT).append("public EClass getEClass(Class<?> javaClass) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (javaClass2eClass == null) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("synchronized (this) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (javaClass2eClass == null) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("Map<Class<?>, EClass> map = new HashMap<>();\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("for (EClassifier c : getEClassifiers()) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (c instanceof EClass) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("Class<?> jc = c.getInstanceClass();\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (jc != null) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("map.put(jc, (EClass) c);\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("javaClass2eClass = map;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return javaClass2eClass.get(javaClass);\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // getESubClasses - 基于 getEClassifiers() 构建
        sb.append(FileWriter.INDENT).append("public Collection<EClass> getESubClasses(EClass eClass) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (eSubClasses == null) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("synchronized (this) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (eSubClasses == null) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("Map<EClass, List<EClass>> map = new HashMap<>();\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("for (EClassifier c : getEClassifiers()) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (c instanceof EClass) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("EClass subClass = (EClass) c;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("for (EClass sup : subClass.getESuperTypes()) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("map.computeIfAbsent(sup, k -> new ArrayList<>()).add(subClass);\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("eSubClasses = map;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("List<EClass> result = eSubClasses.get(eClass);\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return result == null ? java.util.Collections.emptyList() : result;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // getEAllSubClasses
        sb.append(FileWriter.INDENT).append("public Collection<EClass> getEAllSubClasses(EClass eClass) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("List<EClass> result = new ArrayList<>();\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("collectAllSubClasses(eClass, result);\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return result;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // getEClassifiers - 懒加载合并 Registry 中所有属于本模型的包的 classifiers
        // 官方 artop 根包的 getEClassifiers() 遍历所有层级子包，合并 classifiers 到一个列表。
        // SAXXMLHandler 通过 ExtendedMetaData.getType -> EPackage.getEClassifiers() 查找 EClass，
        // 只搜索根包的直接 classifiers，所以必须把所有子包的 classifiers 合并到根包。
        // 注意：init() 期间（classifiersMerged=false）返回 super.getEClassifiers()（含外部类 stub），
        // 避免 stub 创建时触发懒加载导致合并空列表。
        // init() 完成后（classifiersMerged=true），遍历 EPackage.Registry.INSTANCE，
        // 合并所有 nsURI 以 eNS_URI/eNAME 开头的包的 classifiers（即所有 autosar40 子包）。
        // 不使用 getESubpackages() 递归遍历，因为深层子包可能不在子包树中但在 Registry 中。
        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("public EList<EClassifier> getEClassifiers() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (!classifiersMerged) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("return super.getEClassifiers();\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (eClassifiers == null) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("synchronized (this) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (eClassifiers == null) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("EList<EClassifier> list = new BasicEList<EClassifier>();\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("// 先添加根包自己的直接 classifiers（包括外部类 stub）\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("list.addAll(super.getEClassifiers());\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("// 遍历 Registry，合并所有属于本模型的子包的 classifiers\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("// 过滤条件：nsURI 以 eNS_URI/eNAME 开头（如 http://autosar.org/schema/r4.0/autosar40）\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("String prefix = eNS_URI + \"/\" + eNAME;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("for (Object pkgObj : EPackage.Registry.INSTANCE.values()) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (pkgObj == this) continue;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (pkgObj instanceof EPackage) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("EPackage p = (EPackage) pkgObj;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("String nsUri = p.getNsURI();\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (nsUri != null && nsUri.startsWith(prefix)) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("list.addAll(p.getEClassifiers());\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("eClassifiers = list;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return eClassifiers;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        sb.append(FileWriter.INDENT).append("private void collectAllSubClasses(EClass eClass, List<EClass> result) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("Collection<EClass> direct = getESubClasses(eClass);\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("for (EClass sub : direct) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (!result.contains(sub)) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("result.add(sub);\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("collectAllSubClasses(sub, result);\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        sb.append("} //").append(className).append("\n");

        String filePath = FileWriter.javaFilePath(ctx.outputDir, javaPkg, className, null);
        FileWriter.write(filePath, sb.toString());
    }

    /**
     * 根据根包名生成根 Package 类名前缀
     * 规则（对齐 artop 4.13）：
     * - autosar40 -> Autosar40（首字母大写）
     * - gautosar -> GAutosar（单字母前缀 G + 大写 A，符合 artop 命名）
     */
    static String rootPackageClassName(String rootPackageName) {
        if (rootPackageName == null || rootPackageName.isEmpty()) return rootPackageName;
        // gautosar 特殊处理：G + Autosar = GAutosar
        if (rootPackageName.equals("gautosar")) {
            return "GAutosar";
        }
        // 其他情况：直接首字母大写
        return NamingUtils.capitalize(rootPackageName);
    }
}
