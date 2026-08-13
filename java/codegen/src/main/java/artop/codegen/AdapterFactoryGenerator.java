package artop.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;

/**
 * AdapterFactory 生成器
 *
 * 生成 artop 4.13 风格的 <Pkg>AdapterFactory 类，对齐 autosar40.jar 中的实现：
 * - 继承 org.eclipse.emf.common.notify.impl.AdapterFactoryImpl
 * - modelPackage 静态字段
 * - modelSwitch 字段（匿名 Switch 子类，重写 case<ClassName> 和 defaultCase）
 * - isFactoryForType 方法
 * - createAdapter 方法（调用 modelSwitch.doSwitch）
 * - create<ClassName>Adapter 方法（每个类一个）
 * - createEObjectAdapter 方法（默认情况）
 */
public final class AdapterFactoryGenerator {

    private final CodegenContext ctx;

    public AdapterFactoryGenerator(CodegenContext ctx) {
        this.ctx = ctx;
    }

    public void generate(EPackage pkg) {
        String javaPkg = TypeUtils.packagePath(pkg);
        String pkgName = pkg.getName();
        String adapterFactoryName = NamingUtils.adapterFactoryClassName(pkgName);
        String packageClassName = NamingUtils.packageClassName(pkgName);
        String packageClassFq = javaPkg + "." + packageClassName;
        String switchClassName = NamingUtils.switchClassName(pkgName);
        String switchClassFq = javaPkg + ".util." + switchClassName;

        // 收集本包内的 EClass 列表
        List<EClass> eclasses = new ArrayList<>();
        for (EClassifier c : pkg.getEClassifiers()) {
            if (c instanceof EClass) eclasses.add((EClass) c);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("/**\n * <!-- begin-user-doc -->\n * <!-- end-user-doc -->\n");
        sb.append(" * @generated\n */\n");
        sb.append("package ").append(javaPkg).append(".util;\n\n");

        TreeSet<String> imports = new TreeSet<>();
        imports.add("org.eclipse.emf.common.notify.Adapter");
        imports.add("org.eclipse.emf.common.notify.Notifier");
        imports.add("org.eclipse.emf.common.notify.impl.AdapterFactoryImpl");
        imports.add("org.eclipse.emf.ecore.EObject");
        imports.add(packageClassFq);
        imports.add(switchClassFq);
        // 收集所有 case 方法涉及到的类型（本包类 + 父类，跨包也要 import）
        for (EClass c : eclasses) {
            imports.add(javaPkg + "." + c.getName());
        }
        for (EClass c : eclasses) {
            for (EClass sup : c.getESuperTypes()) {
                String supFq = TypeUtils.classFq(sup, ctx);
                if (supFq != null) {
                    imports.add(supFq);
                }
            }
        }
        for (String imp : imports) {
            sb.append("import ").append(imp).append(";\n");
        }
        sb.append("\n");

        sb.append("/**\n");
        sb.append(" * <!-- begin-user-doc -->\n");
        sb.append(" * The <b>Adapter Factory</b> for the model.\n");
        sb.append(" * It provides an adapter <code>createXXX</code> method for each class of the model.\n");
        sb.append(" * <!-- end-user-doc -->\n");
        sb.append(" * @see ").append(packageClassName).append("\n");
        sb.append(" * @generated\n");
        sb.append(" */\n");
        sb.append("public class ").append(adapterFactoryName).append(" extends AdapterFactoryImpl {\n\n");

        // modelPackage 静态字段
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The cached model package.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("protected static ").append(packageClassName).append(" modelPackage;\n\n");

        // 构造函数（仅初始化 modelPackage，对齐 artop 官方）
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * Creates an instance of the adapter factory.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("public ").append(adapterFactoryName).append("() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (modelPackage == null) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("modelPackage = ").append(packageClassName).append(".eINSTANCE;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // isFactoryForType
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * Returns whether this factory is applicable for the type of the object.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @return whether this factory is applicable for the type of the object.\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("public boolean isFactoryForType(Object object) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (object == modelPackage) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("return true;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (object instanceof EObject) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("return ((EObject)object).eClass().getEPackage() == modelPackage;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return false;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // modelSwitch 字段（匿名 Switch 子类，对齐 artop 官方模式）
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * The switch that delegates to the <code>createXXX</code> methods.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("protected ").append(switchClassName).append("<Adapter> modelSwitch =\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("new ").append(switchClassName).append("<Adapter>() {\n");
        for (EClass c : eclasses) {
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("@Override\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("public Adapter case").append(c.getName()).append("(").append(c.getName()).append(" object) {\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("return create").append(c.getName()).append("Adapter();\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        }
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("public Adapter defaultCase(EObject object) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("return createEObjectAdapter();\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("};\n\n");

        // createAdapter 方法（调用 modelSwitch.doSwitch）
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * Creates an adapter for the <code>target</code>.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @param target the object to adapt.\n");
        sb.append(FileWriter.INDENT).append(" * @return the adapter for the <code>target</code>.\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("public Adapter createAdapter(Notifier target) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return modelSwitch.doSwitch((EObject)target);\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        // create<ClassName>Adapter 方法（每个类一个）
        for (EClass c : eclasses) {
            sb.append(FileWriter.INDENT).append("/**\n");
            sb.append(FileWriter.INDENT).append(" * Creates a new adapter for an object of class '").append(c.getName()).append("'.\n");
            sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * This default implementation returns null so that we can easily ignore cases;\n");
            sb.append(FileWriter.INDENT).append(" * it's useful to ignore a case when inheritance will catch all the cases anyway.\n");
            sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
            sb.append(FileWriter.INDENT).append(" * @return the new adapter.\n");
            sb.append(FileWriter.INDENT).append(" * @generated\n");
            sb.append(FileWriter.INDENT).append(" */\n");
            sb.append(FileWriter.INDENT).append("public Adapter create").append(c.getName()).append("Adapter() {\n");
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return null;\n");
            sb.append(FileWriter.INDENT).append("}\n\n");
        }

        // createEObjectAdapter 方法（默认情况）
        sb.append(FileWriter.INDENT).append("/**\n");
        sb.append(FileWriter.INDENT).append(" * Creates a new adapter for the default case.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- begin-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * This default implementation returns null.\n");
        sb.append(FileWriter.INDENT).append(" * <!-- end-user-doc -->\n");
        sb.append(FileWriter.INDENT).append(" * @return the new adapter.\n");
        sb.append(FileWriter.INDENT).append(" * @generated\n");
        sb.append(FileWriter.INDENT).append(" */\n");
        sb.append(FileWriter.INDENT).append("public Adapter createEObjectAdapter() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return null;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        sb.append("} //").append(adapterFactoryName).append("\n");

        String filePath = FileWriter.javaFilePath(ctx.outputDir, javaPkg, adapterFactoryName, "util");
        FileWriter.write(filePath, sb.toString());
    }
}
