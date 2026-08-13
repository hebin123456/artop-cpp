package artop.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;

/**
 * Factory 接口生成器
 *
 * 生成 artop 4.13 风格的 <Pkg>Factory 接口，包含：
 * - eINSTANCE 单例
 * - create<ClassName> 方法（每个非抽象 EClass）
 * - get<Pkg>Package 方法
 */
public final class FactoryInterfaceGenerator {

    private final CodegenContext ctx;

    public FactoryInterfaceGenerator(CodegenContext ctx) {
        this.ctx = ctx;
    }

    public void generate(EPackage pkg) {
        String javaPkg = TypeUtils.packagePath(pkg);
        String pkgName = pkg.getName();
        String factoryName = NamingUtils.factoryClassName(pkgName);
        String packageClassName = NamingUtils.packageClassName(pkgName);
        String factoryImplName = NamingUtils.factoryImplClassName(pkgName);
        String factoryImplFq = javaPkg + ".impl." + factoryImplName;

        StringBuilder sb = new StringBuilder();
        sb.append("/**\n * <!-- begin-user-doc -->\n * <!-- end-user-doc -->\n");
        sb.append(" * @generated\n */\n");
        sb.append("package ").append(javaPkg).append(";\n\n");

        TreeSet<String> imports = new TreeSet<>();
        imports.add("org.eclipse.emf.ecore.EFactory");
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
        sb.append(" * The <b>Factory</b> for the model.\n");
        sb.append(" * It provides a create method for each non-abstract class of the model.\n");
        sb.append(" * <!-- begin-user-doc -->\n");
        sb.append(" * <!-- end-user-doc -->\n");
        sb.append(" * @generated\n");
        sb.append(" */\n");
        sb.append("public interface ").append(factoryName).append(" extends EFactory {\n\n");

        // eINSTANCE
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The singleton instance of the factory.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append(factoryName).append(" eINSTANCE = ").append(factoryImplFq).append(".init();\n\n");

        // create 方法
        List<EClass> eclasses = new ArrayList<>();
        for (EClassifier c : pkg.getEClassifiers()) {
            if (c instanceof EClass) eclasses.add((EClass) c);
        }

        for (EClass c : eclasses) {
            sb.append(FileWriter.INDENT).append("/**\n");
            sb.append(FileWriter.INDENT).append(" * Returns a new object of class '<em>").append(c.getName()).append("</em>'.\n");
            sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * @return a new object of class '<em>").append(c.getName()).append("</em>'.\n");
            sb.append(FileWriter.INDENT).append(" * @generated\n");
            sb.append(FileWriter.INDENT).append(" */\n");
            sb.append(FileWriter.INDENT).append(c.getName()).append(" ").append(NamingUtils.createMethodName(c.getName())).append("();\n\n");
        }

        // getPackage 方法
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * Returns the package supported by this factory.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @return the package supported by this factory.\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append(packageClassName).append(" get").append(packageClassName).append("();\n\n");

        sb.append("} //").append(factoryName).append("\n");

        String filePath = FileWriter.javaFilePath(ctx.outputDir, javaPkg, factoryName, null);
        FileWriter.write(filePath, sb.toString());
    }
}
