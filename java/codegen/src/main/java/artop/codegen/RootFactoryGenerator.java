package artop.codegen;

import org.eclipse.emf.ecore.EPackage;

/**
 * 根 Factory 生成器
 *
 * 生成 artop 4.13 风格的根 Factory 类（如 autosar40.util.Autosar40Factory），
 * 对齐官方 autosar448 jar 中的 Autosar40Factory：
 * - 继承 org.eclipse.emf.ecore.impl.EFactoryImpl
 * - eINSTANCE 单例
 * - getEPackage() 懒加载返回根 Package
 * - create(EClass) 委托给 EClass 所属子包的 Factory
 *
 * 根包 Package 构造函数需要传入 Factory.eINSTANCE 作为 EFactory，
 * 否则 SAXXMLHandler 的 getFactoryForPrefix 返回 null，导致 ClassNotFoundException。
 */
public final class RootFactoryGenerator {

    private final CodegenContext ctx;

    public RootFactoryGenerator(CodegenContext ctx) {
        this.ctx = ctx;
    }

    public void generate() {
        EPackage root = ctx.rootPackage;
        String javaPkg = TypeUtils.packagePath(root) + ".util";
        String rootName = RootPackageGenerator.rootPackageClassName(root.getName());
        String packageClass = rootName + "Package";
        String factoryClass = rootName + "Factory";

        StringBuilder sb = new StringBuilder();
        sb.append("/**\n * <!-- begin-user-doc -->\n * <!-- end-user-doc -->\n");
        sb.append(" * @generated\n */\n");
        sb.append("package ").append(javaPkg).append(";\n\n");

        sb.append("import org.eclipse.emf.ecore.EClass;\n");
        sb.append("import org.eclipse.emf.ecore.EObject;\n");
        sb.append("import org.eclipse.emf.ecore.EPackage;\n");
        sb.append("import org.eclipse.emf.ecore.impl.EFactoryImpl;\n\n");

        sb.append("/**\n");
        sb.append(" * <!-- begin-user-doc -->\n");
        sb.append(" * The <b>Factory</b> for the model.\n");
        sb.append(" * It serves as a delegate factory that forwards create(EClass) to the\n");
        sb.append(" * sub-package factory that owns the EClass.\n");
        sb.append(" * <!-- end-user-doc -->\n");
        sb.append(" * @generated\n");
        sb.append(" */\n");
        sb.append("public class ").append(factoryClass).append(" extends EFactoryImpl {\n\n");

        // eINSTANCE
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The singleton instance of the factory.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("public static final ").append(factoryClass).append(" eINSTANCE = new ").append(factoryClass).append("();\n\n");

        // ePackage 字段（懒加载）
        sb.append(FileWriter.INDENT).append("private EPackage ePackage;\n\n");

        // 构造器
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("public ").append(factoryClass).append("() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("super();\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // getEPackage()
        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("public EPackage getEPackage() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (ePackage == null) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("ePackage = ").append(packageClass).append(".eINSTANCE;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return ePackage;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // create(EClass) - 委托给 EClass 所属子包的 Factory
        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("public EObject create(EClass eClass) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("EObject result = null;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (eClass.eContainer() instanceof EPackage) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("result = ((EPackage) eClass.eContainer()).getEFactoryInstance().create(eClass);\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return result;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        sb.append("} //").append(factoryClass).append("\n");

        String filePath = FileWriter.javaFilePath(ctx.outputDir, javaPkg, factoryClass, null);
        FileWriter.write(filePath, sb.toString());
    }
}
