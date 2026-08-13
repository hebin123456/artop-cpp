package artop.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;

/**
 * FactoryImpl 生成器
 *
 * 生成 artop 4.13 风格的 <Pkg>FactoryImpl 类，包含：
 * - 继承 org.eclipse.emf.ecore.impl.EFactoryImpl
 * - 实现 <Pkg>Factory 接口
 * - init() 静态方法
 * - create(EClass) 方法
 * - create<ClassName> 方法
 * - get<Pkg>Package 方法
 */
public final class FactoryImplGenerator {

    private final CodegenContext ctx;

    public FactoryImplGenerator(CodegenContext ctx) {
        this.ctx = ctx;
    }

    public void generate(EPackage pkg) {
        String javaPkg = TypeUtils.packagePath(pkg);
        String pkgName = pkg.getName();
        String factoryName = NamingUtils.factoryClassName(pkgName);
        String factoryImplName = NamingUtils.factoryImplClassName(pkgName);
        String packageClassName = NamingUtils.packageClassName(pkgName);
        String packageClassFq = javaPkg + "." + packageClassName;

        StringBuilder sb = new StringBuilder();
        sb.append("/**\n * <!-- begin-user-doc -->\n * <!-- end-user-doc -->\n");
        sb.append(" * @generated\n */\n");
        sb.append("package ").append(javaPkg).append(".impl;\n\n");

        TreeSet<String> imports = new TreeSet<>();
        imports.add("org.eclipse.emf.ecore.EClass");
        imports.add("org.eclipse.emf.ecore.EObject");
        imports.add("org.eclipse.emf.ecore.EPackage");
        imports.add("org.eclipse.emf.ecore.impl.EFactoryImpl");
        imports.add("org.eclipse.emf.ecore.plugin.EcorePlugin");
        imports.add(javaPkg + "." + factoryName);
        imports.add(javaPkg + "." + packageClassName);
        for (EClassifier c : pkg.getEClassifiers()) {
            if (c instanceof EClass) {
                imports.add(javaPkg + "." + c.getName());
            }
        }
        for (String imp : imports) {
            sb.append("import ").append(imp).append(";\n");
        }
        sb.append("\n");

        sb.append("/**\n");
        sb.append(" * <!-- begin-user-doc -->\n");
        sb.append(" * An implementation of the model <b>Factory</b>.\n");
        sb.append(" * <!-- end-user-doc -->\n");
        sb.append(" * @generated\n");
        sb.append(" */\n");
        sb.append("public class ").append(factoryImplName).append(" extends EFactoryImpl implements ").append(factoryName).append(" {\n\n");

        // init() 方法
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * Creates the default factory implementation.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("public static ").append(factoryName).append(" init() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("try {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(factoryName).append(" the").append(factoryImplName)
          .append(" = (").append(factoryName).append(")EPackage.Registry.INSTANCE.getEFactory(\"").append(NamingUtils.escapeJavaString(pkg.getNsURI())).append("\");\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (the").append(factoryImplName).append(" != null) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("return the").append(factoryImplName).append(";\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("} catch (Exception exception) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("EcorePlugin.INSTANCE.log(exception);\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return new ").append(factoryImplName).append("();\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // 构造器
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * Creates an instance of the factory.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("public ").append(factoryImplName).append("() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("super();\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // create(EClass) 方法
        List<EClass> eclasses = new ArrayList<>();
        for (EClassifier c : pkg.getEClassifiers()) {
            if (c instanceof EClass) eclasses.add((EClass) c);
        }

        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("public EObject create(EClass eClass) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("switch (eClass.getClassifierID()) {\n");
        for (EClass c : eclasses) {
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("case ")
              .append(packageClassName).append(".").append(NamingUtils.classifierIdConstant(c.getName())).append(": return create").append(c.getName()).append("();\n");
        }
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return super.create(eClass);\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // create<ClassName> 方法
        for (EClass c : eclasses) {
            String implFq = javaPkg + ".impl." + c.getName() + "Impl";
            sb.append(FileWriter.INDENT).append("/**\n");
            sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * @generated\n");
            sb.append(FileWriter.INDENT).append(" */\n");
            sb.append(FileWriter.INDENT).append("@Override\n");
            sb.append(FileWriter.INDENT).append("public ").append(c.getName()).append(" create").append(c.getName()).append("() {\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(c.getName()).append("Impl ").append(NamingUtils.fieldName(c.getName()))
              .append(" = new ").append(c.getName()).append("Impl();\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return ").append(NamingUtils.fieldName(c.getName())).append(";\n");
            sb.append(FileWriter.INDENT).append("}\n\n");
        }

        // getPackage 方法
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("public ").append(packageClassName).append(" get").append(packageClassName).append("() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return (").append(packageClassName).append(")getEPackage();\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // getPackage 静态方法
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @deprecated\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("@Deprecated\n");
        sb.append(FileWriter.INDENT).append("public static ").append(packageClassName).append(" getPackage() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return ").append(packageClassFq).append(".eINSTANCE;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        sb.append("} //").append(factoryImplName).append("\n");

        String filePath = FileWriter.javaFilePath(ctx.outputDir, javaPkg, factoryImplName, "impl");
        FileWriter.write(filePath, sb.toString());
    }
}
