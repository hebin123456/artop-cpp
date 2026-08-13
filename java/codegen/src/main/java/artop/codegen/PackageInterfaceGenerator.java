package artop.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypeParameter;

/**
 * Package 接口生成器
 *
 * 生成 artop 4.13 风格的 <Pkg>Package 接口，包含：
 * - eNAME / eNS_URI / eNS_PREFIX 常量
 * - eINSTANCE 单例
 * - classifier ID 常量（如 AUTOSAR, FILE_INFO_COMMENT）
 * - feature ID 常量（如 AUTOSAR__AR_PACKAGES, AUTOSAR__GCHECKSUM）
 * - feature count 常量（如 AUTOSAR_FEATURE_COUNT）
 * - classifier 访问器方法（如 getAUTOSAR()）
 * - feature 访问器方法（如 getAUTOSAR_ArPackages()）
 */
public final class PackageInterfaceGenerator {

    private final CodegenContext ctx;

    public PackageInterfaceGenerator(CodegenContext ctx) {
        this.ctx = ctx;
    }

    /**
     * 生成 Package 接口
     */
    public void generate(EPackage pkg) {
        String javaPkg = TypeUtils.packagePath(pkg);
        String pkgName = pkg.getName();
        String className = NamingUtils.packageClassName(pkgName);
        String factoryName = NamingUtils.factoryClassName(pkgName);
        String implClassName = NamingUtils.packageImplClassName(pkgName);
        String fqImplClass = javaPkg + ".impl." + implClassName;

        StringBuilder sb = new StringBuilder();
        sb.append("/**\n * <!-- begin-user-doc -->\n * <!-- end-user-doc -->\n");
        sb.append(" * @generated\n */\n");
        sb.append("package ").append(javaPkg).append(";\n\n");

        // imports
        TreeSet<String> imports = new TreeSet<>();
        imports.add("org.eclipse.emf.ecore.EAttribute");
        imports.add("org.eclipse.emf.ecore.EClass");
        imports.add("org.eclipse.emf.ecore.EClassifier");
        imports.add("org.eclipse.emf.ecore.EDataType");
        imports.add("org.eclipse.emf.ecore.EEnum");
        imports.add("org.eclipse.emf.ecore.EOperation");
        imports.add("org.eclipse.emf.ecore.EPackage");
        imports.add("org.eclipse.emf.ecore.EReference");
        imports.add("org.eclipse.emf.ecore.ETypeParameter");
        // 子包的 Package 类需要 import
        for (EPackage sub : pkg.getESubpackages()) {
            String subPkgClass = NamingUtils.packageClassName(sub.getName());
            String subPkgFq = TypeUtils.packagePath(sub) + "." + subPkgClass;
            imports.add(subPkgFq);
        }
        for (String imp : imports) {
            sb.append("import ").append(imp).append(";\n");
        }
        sb.append("\n");

        // 类声明
        sb.append("public interface ").append(className).append(" extends EPackage {\n\n");

        // eNAME / eNS_URI / eNS_PREFIX
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The package name.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("String eNAME = \"").append(NamingUtils.escapeJavaString(pkgName)).append("\";\n\n");

        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The package namespace URI.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("String eNS_URI = \"").append(NamingUtils.escapeJavaString(pkg.getNsURI())).append("\";\n\n");

        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The package namespace name.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("String eNS_PREFIX = \"").append(NamingUtils.escapeJavaString(pkg.getNsPrefix() == null ? "" : pkg.getNsPrefix())).append("\";\n\n");

        // eINSTANCE - 非 final，在 impl 的 init() 中提前赋值，避免子包循环依赖时 NPE
        // 接口字段默认 public static final，这里显式改为 public static（非 final），
        // 让 impl 的 init() 能在创建包后立即设置 eINSTANCE，而不是等 init() 返回后。
        // 注意：声明时仍调用 impl.init() 触发初始化，但 init() 内部会先赋值 eINSTANCE。
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The singleton instance of the package.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("static ").append(className).append(" eINSTANCE = ").append(fqImplClass).append(".init();\n\n");

        // 分类 classifier
        List<EClass> eclasses = new ArrayList<>();
        List<EEnum> eenums = new ArrayList<>();
        List<EDataType> edatatypes = new ArrayList<>();
        for (EClassifier c : pkg.getEClassifiers()) {
            if (c instanceof EClass) eclasses.add((EClass) c);
            else if (c instanceof EEnum) eenums.add((EEnum) c);
            else if (c instanceof EDataType) edatatypes.add((EDataType) c);
        }

        // classifier ID 常量 + feature ID 常量
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The meta-object id for the '{@link #").append("").append("}' class.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");

        int classifierIdx = 0;
        for (EClass c : eclasses) {
            int baseId = classifierIdx;
            sb.append(FileWriter.INDENT).append("int ").append(NamingUtils.classifierIdConstant(c.getName()))
              .append(" = ").append(baseId).append(";\n\n");
            classifierIdx++;
        }
        for (EEnum e : eenums) {
            sb.append(FileWriter.INDENT).append("int ").append(NamingUtils.classifierIdConstant(e.getName()))
              .append(" = ").append(classifierIdx++).append(";\n\n");
        }
        for (EDataType d : edatatypes) {
            sb.append(FileWriter.INDENT).append("int ").append(NamingUtils.classifierIdConstant(d.getName()))
              .append(" = ").append(classifierIdx++).append(";\n\n");
        }

        // feature ID 常量（每个 EClass 的所有 feature，包括继承的）
        for (EClass c : eclasses) {
            generateFeatureIds(sb, c);
        }

        // classifier 访问器方法
        for (EClass c : eclasses) {
            sb.append(FileWriter.INDENT).append("/**\n");
            sb.append(FileWriter.INDENT).append(" * Returns the meta object for class '{@link ").append(javaPkg).append(".").append(c.getName()).append(" <b>").append(c.getName()).append("</b>}'.\n");
            sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * @return the meta object for class '<b>").append(c.getName()).append("</b>'.\n");
            sb.append(FileWriter.INDENT).append(" * @see ").append(javaPkg).append(".").append(c.getName()).append("\n");
            sb.append(FileWriter.INDENT).append(" * @generated\n");
            sb.append(FileWriter.INDENT).append(" */\n");
            sb.append(FileWriter.INDENT).append("EClass ").append(NamingUtils.classifierGetterName(c.getName())).append("();\n\n");
        }
        for (EEnum e : eenums) {
            sb.append(FileWriter.INDENT).append("/**\n");
            sb.append(FileWriter.INDENT).append(" * Returns the meta object for enum '{@link ").append(javaPkg).append(".").append(e.getName()).append(" <b>").append(e.getName()).append("</b>}'.\n");
            sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * @generated\n");
            sb.append(FileWriter.INDENT).append(" */\n");
            sb.append(FileWriter.INDENT).append("EEnum ").append(NamingUtils.classifierGetterName(e.getName())).append("();\n\n");
        }
        for (EDataType d : edatatypes) {
            sb.append(FileWriter.INDENT).append("/**\n");
            sb.append(FileWriter.INDENT).append(" * Returns the meta object for data type '{@link ").append(TypeUtils.javaType(d)).append(" <b>").append(d.getName()).append("</b>}'.\n");
            sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * @generated\n");
            sb.append(FileWriter.INDENT).append(" */\n");
            sb.append(FileWriter.INDENT).append("EDataType ").append(NamingUtils.classifierGetterName(d.getName())).append("();\n\n");
        }

        // feature 访问器方法
        for (EClass c : eclasses) {
            generateFeatureGetters(sb, c, javaPkg);
        }

        // Factory 访问器
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * Returns the factory that creates the instances of the model.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @return the factory that creates the instances of the model.\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append(factoryName).append(" get").append(factoryName).append("();\n\n");

        // 子包接口（如果有子包，生成 get<SubPackage> 方法）
        for (EPackage sub : pkg.getESubpackages()) {
            String subPkgClass = NamingUtils.packageClassName(sub.getName());
            String subPkgFq = TypeUtils.packagePath(sub) + "." + subPkgClass;
            sb.append(FileWriter.INDENT).append("/**\n");
            sb.append(FileWriter.INDENT).append(" * Returns the meta object for package '{@link ").append(subPkgFq).append(" <b>").append(sub.getName()).append("</b>}'.\n");
            sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * @generated\n");
            sb.append(FileWriter.INDENT).append(" */\n");
            sb.append(FileWriter.INDENT).append(subPkgClass).append(" get").append(NamingUtils.capitalize(sub.getName())).append("();\n\n");
        }

        // Literals 内部类
        generateLiteralsInnerClass(sb, pkg, eclasses, eenums, edatatypes, javaPkg);

        sb.append("} //").append(className).append("\n");

        String filePath = FileWriter.javaFilePath(ctx.outputDir, javaPkg, className, null);
        FileWriter.write(filePath, sb.toString());
    }

    /**
     * 生成 feature ID 常量
     * artop 风格：包含继承的 feature，feature ID 从 classifier ID 开始递增
     */
    private void generateFeatureIds(StringBuilder sb, EClass c) {
        String className = c.getName();
        // 收集所有 feature（包括继承的）
        List<EStructuralFeature> allFeatures = new ArrayList<>(c.getEAllStructuralFeatures());

        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The feature id for the '<b>").append(className).append("</b>' class.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");

        int featureIdx = 0;
        for (EStructuralFeature f : allFeatures) {
            String constName = NamingUtils.featureIdConstant(className, f.getName());
            sb.append(FileWriter.INDENT).append("int ").append(constName).append(" = ").append(featureIdx).append(";\n\n");
            featureIdx++;
        }
        // feature count
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The number of structural features of the '<b>").append(className).append("</b>' class.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("int ").append(NamingUtils.featureCountConstant(className))
          .append(" = ").append(featureIdx).append(";\n\n");
    }

    /**
     * 生成 feature 访问器方法
     */
    private void generateFeatureGetters(StringBuilder sb, EClass c, String javaPkg) {
        String className = c.getName();
        // 只生成本类直接定义的 feature 的访问器（不生成继承的）
        for (EStructuralFeature f : c.getEStructuralFeatures()) {
            String featureName = f.getName();
            String getterName = NamingUtils.featureGetterName(className, featureName);

            sb.append(FileWriter.INDENT).append("/**\n");
            if (f instanceof EAttribute) {
                sb.append(FileWriter.INDENT).append(" * Returns the meta object for the attribute '{@link ").append(javaPkg).append(".").append(className).append("#get").append(NamingUtils.capitalize(featureName)).append(" <b>").append(featureName).append("</b>}'.\n");
            } else if (f instanceof EReference) {
                EReference r = (EReference) f;
                if (r.isContainment()) {
                    sb.append(FileWriter.INDENT).append(" * Returns the meta object for the containment reference '{@link ").append(javaPkg).append(".").append(className).append("#get").append(NamingUtils.capitalize(featureName)).append(" <b>").append(featureName).append("</b>}'.\n");
                } else {
                    sb.append(FileWriter.INDENT).append(" * Returns the meta object for the reference '{@link ").append(javaPkg).append(".").append(className).append("#get").append(NamingUtils.capitalize(featureName)).append(" <b>").append(featureName).append("</b>}'.\n");
                }
            }
            sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * @generated\n");
            sb.append(FileWriter.INDENT).append(" */\n");

            if (f instanceof EAttribute) {
                sb.append(FileWriter.INDENT).append("EAttribute ").append(getterName).append("();\n\n");
            } else {
                sb.append(FileWriter.INDENT).append("EReference ").append(getterName).append("();\n\n");
            }
        }
    }

    /**
     * 生成 Literals 内部类（artop 4.13 风格）
     * 包含 classifier 字面量和 feature 字面量
     */
    private void generateLiteralsInnerClass(StringBuilder sb, EPackage pkg,
            List<EClass> eclasses, List<EEnum> eenums, List<EDataType> edatatypes, String javaPkg) {
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * Provides literals for the meta objects.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("interface Literals {\n\n");

        // EClass 字面量 + feature 字面量
        for (EClass c : eclasses) {
            String className = c.getName();
            String classConst = NamingUtils.classifierIdConstant(className);
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("/**\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(" * The meta object literal for the '{@link ").append(javaPkg).append(".").append(className).append(" <b>").append(className).append("</b>}' class.\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(" * @generated\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(" */\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("EClass ").append(classConst)
              .append(" = eINSTANCE.get").append(className).append("();\n\n");

            // feature 字面量（只包含本类直接定义的 feature）
            for (EStructuralFeature f : c.getEStructuralFeatures()) {
                String featureName = f.getName();
                String featureConst = NamingUtils.featureIdConstant(className, featureName);
                String getterName = NamingUtils.featureGetterName(className, featureName);
                String featureType = (f instanceof EAttribute) ? "EAttribute" : "EReference";
                sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(featureType).append(" ").append(featureConst)
                  .append(" = eINSTANCE.").append(getterName).append("();\n\n");
            }
        }
        // EEnum 字面量
        for (EEnum e : eenums) {
            String enumConst = NamingUtils.classifierIdConstant(e.getName());
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("EEnum ").append(enumConst)
              .append(" = eINSTANCE.get").append(e.getName()).append("();\n\n");
        }
        // EDataType 字面量
        for (EDataType d : edatatypes) {
            String dtConst = NamingUtils.classifierIdConstant(d.getName());
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("EDataType ").append(dtConst)
              .append(" = eINSTANCE.get").append(d.getName()).append("();\n\n");
        }

        sb.append(FileWriter.INDENT).append("} //Literals\n\n");
    }
}
