package artop.codegen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * 类型映射工具类 - 处理 EDataType/EEnum/EClass 到 Java 类型的映射
 *
 * artop 4.13 中的 EDataType instanceClassName 类型集合：
 * - java.lang.Boolean, java.lang.Double, java.lang.Integer, java.lang.Long
 * - java.lang.String, java.math.BigInteger, java.net.URI, java.util.Map$Entry
 * - javax.xml.datatype.XMLGregorianCalendar
 * - org.artop.aal.common.datatypes.IntegerDatatype
 * - org.artop.aal.common.datatypes.PositiveIntegerDatatype
 * - org.artop.aal.common.datatypes.UnlimitedIntegerDatatype
 * - org.eclipse.emf.common.util.Enumerator
 */
public final class TypeUtils {

    private TypeUtils() {}

    /** EMF Ecore 内置类型到 Java 类型的映射 */
    public static final Map<String, String> ECORE_TYPE_MAP = new HashMap<>();
    static {
        ECORE_TYPE_MAP.put("EBoolean", "boolean");
        ECORE_TYPE_MAP.put("EBooleanObject", "Boolean");
        ECORE_TYPE_MAP.put("EInt", "int");
        ECORE_TYPE_MAP.put("EIntegerObject", "Integer");
        ECORE_TYPE_MAP.put("ELong", "long");
        ECORE_TYPE_MAP.put("ELongObject", "Long");
        ECORE_TYPE_MAP.put("EShort", "short");
        ECORE_TYPE_MAP.put("EShortObject", "Short");
        ECORE_TYPE_MAP.put("EByte", "byte");
        ECORE_TYPE_MAP.put("EByteObject", "Byte");
        ECORE_TYPE_MAP.put("EFloat", "float");
        ECORE_TYPE_MAP.put("EFloatObject", "Float");
        ECORE_TYPE_MAP.put("EDouble", "double");
        ECORE_TYPE_MAP.put("EDoubleObject", "Double");
        ECORE_TYPE_MAP.put("EString", "String");
        ECORE_TYPE_MAP.put("EChar", "char");
        ECORE_TYPE_MAP.put("ECharacterObject", "Character");
        ECORE_TYPE_MAP.put("EBigInteger", "java.math.BigInteger");
        ECORE_TYPE_MAP.put("EBigDecimal", "java.math.BigDecimal");
        ECORE_TYPE_MAP.put("EDate", "java.util.Date");
        ECORE_TYPE_MAP.put("EJavaObject", "java.lang.Object");
        ECORE_TYPE_MAP.put("EJavaClass", "java.lang.Class<?>");
        ECORE_TYPE_MAP.put("EDiagnosticChain", "org.eclipse.emf.common.util.DiagnosticChain");
        ECORE_TYPE_MAP.put("EEList", "org.eclipse.emf.common.util.EList");
        ECORE_TYPE_MAP.put("EMap", "org.eclipse.emf.common.util.EMap");
        ECORE_TYPE_MAP.put("ETreeIterator", "org.eclipse.emf.common.util.TreeIterator");
        ECORE_TYPE_MAP.put("EFeatureMap", "org.eclipse.emf.ecore.util.FeatureMap");
        ECORE_TYPE_MAP.put("EFeatureMapEntry", "org.eclipse.emf.ecore.util.FeatureMap.Entry");
        ECORE_TYPE_MAP.put("EEnumerator", "org.eclipse.emf.common.util.Enumerator");
        ECORE_TYPE_MAP.put("EStringToStringMapEntry", "java.util.Map.Entry<String, String>");
    }

    /** Ecore 元模型类型集合（EObject 子类） */
    public static final Set<String> ECORE_METAMODEL_TYPES = new HashSet<>();
    static {
        ECORE_METAMODEL_TYPES.add("EObject");
        ECORE_METAMODEL_TYPES.add("EClass");
        ECORE_METAMODEL_TYPES.add("EPackage");
        ECORE_METAMODEL_TYPES.add("EFactory");
        ECORE_METAMODEL_TYPES.add("EEnum");
        ECORE_METAMODEL_TYPES.add("EEnumLiteral");
        ECORE_METAMODEL_TYPES.add("EOperation");
        ECORE_METAMODEL_TYPES.add("EParameter");
        ECORE_METAMODEL_TYPES.add("ETypeParameter");
        ECORE_METAMODEL_TYPES.add("EAnnotation");
        ECORE_METAMODEL_TYPES.add("EAttribute");
        ECORE_METAMODEL_TYPES.add("EReference");
        ECORE_METAMODEL_TYPES.add("EDataType");
        ECORE_METAMODEL_TYPES.add("EStructuralFeature");
        ECORE_METAMODEL_TYPES.add("EModelElement");
        ECORE_METAMODEL_TYPES.add("ENamedElement");
        ECORE_METAMODEL_TYPES.add("ETypedElement");
        ECORE_METAMODEL_TYPES.add("EValidator");
    }

    /**
     * 判断是否是 Ecore 内置类型
     */
    public static boolean isEcoreBuiltinType(String typeName) {
        return ECORE_TYPE_MAP.containsKey(typeName) || ECORE_METAMODEL_TYPES.contains(typeName);
    }

    /**
     * 获取 EDataType 对应的 Java 类型
     * 优先使用 instanceClassName，其次使用 name 映射
     */
    public static String javaType(EDataType dataType) {
        if (dataType == null) return "java.lang.Object";

        // 优先使用 instanceClassName
        String instanceClassName = dataType.getInstanceClassName();
        if (instanceClassName != null && !instanceClassName.isEmpty()) {
            // 处理内部类：Map$Entry -> Map.Entry
            return instanceClassName.replace('$', '.');
        }

        // 使用 name 映射
        String name = dataType.getName();
        if (ECORE_TYPE_MAP.containsKey(name)) {
            return ECORE_TYPE_MAP.get(name);
        }

        // EEnum：使用枚举类的全限定名
        if (dataType instanceof EEnum) {
            EEnum eEnum = (EEnum) dataType;
            EPackage pkg = eEnum.getEPackage();
            if (pkg != null) {
                return packagePath(pkg) + "." + name;
            }
            return name;
        }

        return "java.lang.Object";
    }

    /**
     * 获取 EDataType 的默认值
     */
    public static String defaultValue(EDataType dataType) {
        if (dataType == null) return "null";
        String name = dataType.getName();
        if (name == null) return "null";

        if (name.equals("EBoolean") || name.equals("EBooleanObject")) return "false";
        if (name.equals("EString")) return "\"\"";
        if (name.equals("EInt") || name.equals("EIntegerObject") || name.equals("EShort") || name.equals("EByte")) return "0";
        if (name.equals("ELong") || name.equals("ELongObject")) return "0L";
        if (name.equals("EFloat") || name.equals("EFloatObject")) return "0.0f";
        if (name.equals("EDouble") || name.equals("EDoubleObject")) return "0.0";
        if (name.equals("EChar") || name.equals("ECharacterObject")) return "'\\0'";
        if (name.equals("EBigInteger")) return "java.math.BigInteger.ZERO";
        if (name.equals("EBigDecimal")) return "java.math.BigDecimal.ZERO";
        return "null";
    }

    /**
     * 判断是否是基本类型（primitive）
     */
    public static boolean isPrimitive(EDataType dataType) {
        if (dataType == null) return false;
        String name = dataType.getName();
        if (name == null) return false;
        return name.equals("EBoolean") || name.equals("EInt") || name.equals("ELong")
            || name.equals("EShort") || name.equals("EByte") || name.equals("EFloat")
            || name.equals("EDouble") || name.equals("EChar");
    }

    /**
     * 判断是否是 FeatureMap 类型
     */
    public static boolean isFeatureMapType(EDataType dataType) {
        if (dataType == null) return false;
        String name = dataType.getName();
        if (name == null) return false;
        return name.contains("FeatureMap");
    }

    /**
     * 获取 EPackage 的完整Java包路径
     */
    public static String packagePath(EPackage pkg) {
        if (pkg == null) return "";
        EPackage parent = pkg.getESuperPackage();
        if (parent == null) {
            return pkg.getName();
        }
        String parentPath = packagePath(parent);
        return parentPath.isEmpty() ? pkg.getName() : parentPath + "." + pkg.getName();
    }

    /**
     * 获取 EClass 的全限定 Java 类名（用于 import）
     * 通过 CodegenContext 查找 EClass 所在的 EPackage，构建 Java 包路径
     * 如果找不到（如 gautosar 外部类），返回 null
     */
    public static String classFq(EClass eClass, CodegenContext ctx) {
        if (eClass == null) return null;
        String name = eClass.getName();
        if (name == null) return null;
        // EStringToStringMapEntry 是 EMF 的特殊 Map Entry EClass，
        // 对应的 Java 类型是 java.util.Map.Entry<String, String>
        if ("EStringToStringMapEntry".equals(name)) {
            return "java.util.Map.Entry<String, String>";
        }
        // Ecore 元模型内置类型（EObject, EClass 等）：使用 org.eclipse.emf.ecore 全限定名
        // 避免返回 "ecore.EObject" 这样的错误路径
        if (ECORE_METAMODEL_TYPES.contains(name)) {
            return "org.eclipse.emf.ecore." + name;
        }
        // 内部类：在 ctx.allClasses 中查找
        if (ctx.allClasses.containsKey(name)) {
            EClass internal = ctx.allClasses.get(name);
            EPackage pkg = internal.getEPackage();
            if (pkg != null) {
                return packagePath(pkg) + "." + name;
            }
        }
        // 外部类：使用 eClass 自身的 EPackage
        EPackage pkg = eClass.getEPackage();
        if (pkg != null) {
            return packagePath(pkg) + "." + name;
        }
        return null;
    }

    /**
     * 判断 EClass 名称是否是 EMF 特殊的 Map Entry 类型
     * （如 EStringToStringMapEntry，在 Java 中映射为 java.util.Map.Entry）
     */
    public static boolean isMapEntryEClass(String name) {
        return "EStringToStringMapEntry".equals(name);
    }

    /**
     * 判断 EClass 是否是 Map Entry 类型（instanceClassName 为 java.util.Map$Entry）
     * 或是 EMF 内置的 EStringToStringMapEntry
     */
    public static boolean isMapEntryEClass(EClass eClass) {
        if (eClass == null) return false;
        if ("EStringToStringMapEntry".equals(eClass.getName())) return true;
        String icn = eClass.getInstanceClassName();
        return icn != null && (icn.equals("java.util.Map$Entry") || icn.equals("java.util.Map.Entry"));
    }

    /**
     * 获取 Map Entry EClass 的 key 的 Java 类型
     * Map Entry EClass 通常有一个名为 "key" 的 EStructuralFeature
     */
    public static String mapEntryKeyType(EClass eClass) {
        if (eClass == null) return "java.lang.String";
        // EStringToStringMapEntry: key 是 String
        if ("EStringToStringMapEntry".equals(eClass.getName())) return "java.lang.String";
        // 查找名为 "key" 的 feature
        for (EStructuralFeature f : eClass.getEStructuralFeatures()) {
            if ("key".equals(f.getName())) {
                if (f instanceof EAttribute) {
                    return javaType(((EAttribute) f).getEAttributeType());
                } else if (f instanceof EReference) {
                    EClass t = ((EReference) f).getEReferenceType();
                    if (t != null) return "org.eclipse.emf.ecore.EObject";
                }
            }
        }
        return "java.lang.String";
    }

    /**
     * 获取 Map Entry EClass 的 value 的 Java 类型
     * Map Entry EClass 通常有一个名为 "value" 的 EStructuralFeature；
     * 如果没有，value 类型为 EObject
     */
    public static String mapEntryValueType(EClass eClass) {
        if (eClass == null) return "org.eclipse.emf.ecore.EObject";
        // EStringToStringMapEntry: value 是 String
        if ("EStringToStringMapEntry".equals(eClass.getName())) return "java.lang.String";
        // 查找名为 "value" 的 feature
        for (EStructuralFeature f : eClass.getEStructuralFeatures()) {
            if ("value".equals(f.getName())) {
                if (f instanceof EAttribute) {
                    return javaType(((EAttribute) f).getEAttributeType());
                } else if (f instanceof EReference) {
                    EClass t = ((EReference) f).getEReferenceType();
                    if (t != null) {
                        if (isEcoreBuiltinType(t.getName())) return "org.eclipse.emf.ecore." + t.getName();
                        return t.getName();
                    }
                }
            }
        }
        // 没有 value feature，value 类型为 EObject
        return "org.eclipse.emf.ecore.EObject";
    }
}
