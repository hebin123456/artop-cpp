package artop.codegen;

import org.eclipse.emf.ecore.EPackage;

/**
 * ResourceImpl 生成器
 *
 * 生成 artop 4.13 风格的 ResourceImpl 类（如 autosar40.util.Autosar40ResourceImpl），
 * 继承 org.artop.aal.common.resource.impl.AutosarXMLResourceImpl。
 *
 * 该类是 arxml 文件的 Resource 实现，通过 SerializationFactory 创建 XMLLoad/XMLSave/XMLHelper。
 *
 * 生成内容（对齐 artop 官方 Autosar40ResourceImpl）：
 * - 继承 AutosarXMLResourceImpl
 * - 构造函数：super(uri)
 * - createXMLLoad()：SerializationFactory.createXMLLoad(XxxPackage.eINSTANCE, this)
 * - createXMLSave()：SerializationFactory.createXMLSave(this)
 * - createXMLHelper()：SerializationFactory.createXMLHelper(this)
 */
public final class ResourceImplGenerator {

    private final CodegenContext ctx;

    public ResourceImplGenerator(CodegenContext ctx) {
        this.ctx = ctx;
    }

    public void generate() {
        EPackage root = ctx.rootPackage;
        String javaPkg = TypeUtils.packagePath(root) + ".util";
        String rootName = RootPackageGenerator.rootPackageClassName(root.getName());
        String className = rootName + "ResourceImpl";
        String packageClassName = rootName + "Package";

        StringBuilder sb = new StringBuilder();
        sb.append("/**\n * <!-- begin-user-doc -->\n * <!-- end-user-doc -->\n");
        sb.append(" * @generated\n */\n");
        sb.append("package ").append(javaPkg).append(";\n\n");

        sb.append("import org.artop.aal.common.resource.impl.AutosarXMLResourceImpl;\n");
        sb.append("import org.artop.aal.serialization.SerializationFactory;\n");
        sb.append("import org.eclipse.emf.common.util.URI;\n");
        sb.append("import org.eclipse.emf.ecore.xmi.XMLHelper;\n");
        sb.append("import org.eclipse.emf.ecore.xmi.XMLLoad;\n");
        sb.append("import org.eclipse.emf.ecore.xmi.XMLSave;\n\n");

        sb.append("/**\n");
        sb.append(" * <!-- begin-user-doc -->\n");
        sb.append(" * The Resource implementation for the model.\n");
        sb.append(" * <!-- end-user-doc -->\n");
        sb.append(" * @generated\n");
        sb.append(" */\n");
        sb.append("public class ").append(className).append(" extends AutosarXMLResourceImpl {\n\n");

        // 构造函数
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("public ").append(className).append("(URI uri) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("super(uri);\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // createXMLLoad
        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("protected XMLLoad createXMLLoad() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return SerializationFactory.createXMLLoad(").append(packageClassName).append(".eINSTANCE, this);\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // createXMLSave
        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("protected XMLSave createXMLSave() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return SerializationFactory.createXMLSave(this);\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // createXMLHelper
        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("protected XMLHelper createXMLHelper() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return SerializationFactory.createXMLHelper(this);\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        sb.append("} //").append(className).append("\n");

        String filePath = FileWriter.javaFilePath(ctx.outputDir, javaPkg, className, null);
        FileWriter.write(filePath, sb.toString());
    }
}
