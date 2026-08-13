package artop.codegen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;

/**
 * 主程序入口 - ecore 转 artop 静态模型代码生成器
 *
 * 用法：
 *   java -cp <classpath> artop.codegen.Main <input.ecore> <outputDir> [contentType]
 *
 * 示例：
 *   java -cp "lib/*:build" artop.codegen.Main \
 *     opensourse/artop-4.19.0/core-4.19.0/aal/plugins/org.artop.aal.autosar448/model/autosar448.ecore \
 *     output \
 *     org.artop.aal.autosar448
 *
 *   java -cp "lib/*:build" artop.codegen.Main \
 *     opensourse/artop-4.19.0/core-4.19.0/aal/plugins/org.artop.aal.gautosar/model/gautosar.ecore \
 *     output_gautosar \
 *     org.artop.aal.gautosar
 *
 * 生成 artop 4.13 风格的静态模型代码，每个 EPackage 生成 6 个文件：
 * - <Pkg>Package.java          (Package 接口)
 * - <Pkg>Factory.java          (Factory 接口)
 * - impl/<Pkg>PackageImpl.java (Package 实现类)
 * - impl/<Pkg>FactoryImpl.java (Factory 实现类)
 * - util/<Pkg>AdapterFactory.java (AdapterFactory)
 * - util/<Pkg>Switch.java      (Switch)
 *
 * 每个 EClass 生成 2 个文件：
 * - <ClassName>.java           (接口)
 * - impl/<ClassName>Impl.java  (实现类)
 *
 * 根 Package 生成 <rootPkg>.util.<RootPkg>Package 类（如 autosar40.util.Autosar40Package
 * 或 gautosar.util.GAutosarPackage）。
 */
public final class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java artop.codegen.Main <input.ecore> <outputDir> [contentType]");
            System.err.println("Example:");
            System.err.println("  java -cp \"lib/*:build\" artop.codegen.Main autosar448.ecore output org.artop.aal.autosar448");
            System.exit(1);
        }

        String ecorePath = args[0];
        String outputDirPath = args[1];
        // 内容类型：默认根据 ecore 文件名推断
        String contentType = args.length >= 3 ? args[2] : inferContentType(ecorePath);

        System.out.println("[codegen] Loading ecore: " + ecorePath);
        System.out.println("[codegen] Content type: " + contentType);
        EPackage rootPackage = EcoreLoader.load(ecorePath);

        File outputDir = new File(outputDirPath);
        outputDir.mkdirs();

        CodegenContext ctx = new CodegenContext(rootPackage, outputDir);
        ctx.contentType = contentType;
        ctx.collect();
        System.out.println(ctx.stats());

        // 初始化所有生成器
        PackageInterfaceGenerator pkgInterfaceGen = new PackageInterfaceGenerator(ctx);
        PackageImplGenerator pkgImplGen = new PackageImplGenerator(ctx);
        FactoryInterfaceGenerator factoryInterfaceGen = new FactoryInterfaceGenerator(ctx);
        FactoryImplGenerator factoryImplGen = new FactoryImplGenerator(ctx);
        ClassInterfaceGenerator classInterfaceGen = new ClassInterfaceGenerator(ctx);
        ClassImplGenerator classImplGen = new ClassImplGenerator(ctx);
        AdapterFactoryGenerator adapterFactoryGen = new AdapterFactoryGenerator(ctx);
        SwitchGenerator switchGen = new SwitchGenerator(ctx);
        RootPackageGenerator rootPackageGen = new RootPackageGenerator(ctx);
        RootFactoryGenerator rootFactoryGen = new RootFactoryGenerator(ctx);
        ReleaseDescriptorGenerator releaseDescriptorGen = new ReleaseDescriptorGenerator(ctx);
        ResourceImplGenerator resourceImplGen = new ResourceImplGenerator(ctx);
        ResourceFactoryImplGenerator resourceFactoryImplGen = new ResourceFactoryImplGenerator(ctx);

        // 1. 生成根 Package 类（如 autosar40.util.Autosar40Package 或 gautosar.util.GAutosarPackage）
        System.out.println("[codegen] Generating root package class...");
        rootPackageGen.generate();
        rootFactoryGen.generate();

        // 1b. 生成根包的 ReleaseDescriptor、ResourceImpl、ResourceFactoryImpl
        //     这些是 artop 序列化套件的入口类，对齐 artop 官方 jar 中的根包 util 类
        System.out.println("[codegen] Generating release descriptor, resource impl, resource factory impl...");
        releaseDescriptorGen.generate();
        resourceImplGen.generate();
        resourceFactoryImplGen.generate();

        // 2. 生成所有子包的 Package/Factory/AdapterFactory/Switch
        int pkgCount = 0;
        for (EPackage pkg : ctx.sortedPackages) {
            if (pkg == ctx.rootPackage) continue; // 根包不生成子包的6文件
            pkgInterfaceGen.generate(pkg);
            pkgImplGen.generate(pkg);
            factoryInterfaceGen.generate(pkg);
            factoryImplGen.generate(pkg);
            adapterFactoryGen.generate(pkg);
            switchGen.generate(pkg);
            pkgCount++;
            if (pkgCount % 50 == 0) {
                System.out.println("[codegen] Generated " + pkgCount + " packages...");
            }
        }
        System.out.println("[codegen] Generated " + pkgCount + " packages (6 files each)");

        // 3. 生成所有 EClass 的接口和实现类
        // 按包路径排序，让输出更稳定
        List<EClass> classes = new ArrayList<>(ctx.allClasses.values());
        Collections.sort(classes, (a, b) -> {
            String pa = TypeUtils.packagePath(a.getEPackage());
            String pb = TypeUtils.packagePath(b.getEPackage());
            int c = pa.compareTo(pb);
            if (c != 0) return c;
            return a.getName().compareTo(b.getName());
        });

        int classCount = 0;
        for (EClass c : classes) {
            classInterfaceGen.generate(c);
            classImplGen.generate(c);
            classCount++;
            if (classCount % 500 == 0) {
                System.out.println("[codegen] Generated " + classCount + " classes...");
            }
        }
        System.out.println("[codegen] Generated " + classCount + " classes (2 files each)");

        // 4. 生成枚举类
        int enumCount = 0;
        for (org.eclipse.emf.ecore.EEnum e : ctx.allEnums.values()) {
            generateEnum(ctx, e);
            enumCount++;
        }
        System.out.println("[codegen] Generated " + enumCount + " enums");

        System.out.println("[codegen] Done. Output: " + outputDir.getAbsolutePath());
        System.out.println("[codegen] Total files: " + countJavaFiles(outputDir));
    }

    /**
     * 根据 ecore 文件路径推断内容类型
     * 如 .../org.artop.aal.autosar448/model/autosar448.ecore -> org.artop.aal.autosar448
     * 如 .../org.artop.aal.gautosar/model/gautosar.ecore -> org.artop.aal.gautosar
     */
    private static String inferContentType(String ecorePath) {
        java.io.File f = new java.io.File(ecorePath);
        java.io.File parent = f.getParentFile();
        if (parent != null) {
            java.io.File pluginDir = parent.getParentFile();
            if (pluginDir != null) {
                String pluginName = pluginDir.getName();
                // 匹配 org.artop.aal.xxx 模式
                if (pluginName.startsWith("org.artop.aal.")) {
                    return pluginName;
                }
            }
        }
        // 回退：使用文件名（去掉 .ecore）
        String fileName = f.getName();
        if (fileName.endsWith(".ecore")) {
            fileName = fileName.substring(0, fileName.length() - 6);
        }
        return "org.artop.aal." + fileName;
    }

    /**
     * 生成枚举类
     */
    private static void generateEnum(CodegenContext ctx, org.eclipse.emf.ecore.EEnum e) {
        EPackage pkg = e.getEPackage();
        String javaPkg = TypeUtils.packagePath(pkg);
        String name = e.getName();

        StringBuilder sb = new StringBuilder();
        sb.append("/**\n * <!-- begin-user-doc -->\n * <!-- end-user-doc -->\n");
        sb.append(" * @generated\n */\n");
        sb.append("package ").append(javaPkg).append(";\n\n");
        sb.append("import java.util.Arrays;\n");
        sb.append("import java.util.Collections;\n");
        sb.append("import java.util.List;\n");
        sb.append("import org.eclipse.emf.common.util.Enumerator;\n\n");

        sb.append("public enum ").append(name).append(" implements Enumerator {\n");

        List<org.eclipse.emf.ecore.EEnumLiteral> literals = e.getELiterals();
        for (int i = 0; i < literals.size(); i++) {
            org.eclipse.emf.ecore.EEnumLiteral lit = literals.get(i);
            String literalName = NamingUtils.enumLiteralName(lit.getName());
            sb.append(FileWriter.INDENT).append(literalName).append("(").append(lit.getValue()).append(", \"")
              .append(NamingUtils.escapeJavaString(lit.getName())).append("\", \"")
              .append(NamingUtils.escapeJavaString(lit.getLiteral() != null ? lit.getLiteral() : lit.getName())).append("\")");
            if (i < literals.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(";\n\n");

        sb.append(FileWriter.INDENT).append("public static final int ").append(name.toUpperCase()).append("_VALUES_BEGIN = 0;\n");
        sb.append(FileWriter.INDENT).append("private final int value;\n");
        sb.append(FileWriter.INDENT).append("private final String name;\n");
        sb.append(FileWriter.INDENT).append("private final String literal;\n\n");

        sb.append(FileWriter.INDENT).append("private ").append(name).append("(int value, String name, String literal) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("this.value = value;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("this.name = name;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("this.literal = literal;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("public int getValue() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return value;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("public String getName() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return name;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("public String getLiteral() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return literal;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        sb.append(FileWriter.INDENT).append("@Override\n");
        sb.append(FileWriter.INDENT).append("public String toString() {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return literal;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        sb.append(FileWriter.INDENT).append("public static ").append(name).append(" get(int value) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("switch (value) {\n");
        for (org.eclipse.emf.ecore.EEnumLiteral lit : literals) {
            sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT)
              .append("case ").append(lit.getValue()).append(": return ").append(NamingUtils.enumLiteralName(lit.getName())).append(";\n");
        }
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return null;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        sb.append(FileWriter.INDENT).append("public static ").append(name).append(" getByName(String name) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("for (").append(name).append(" v : values()) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (v.getName().equals(name)) return v;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return null;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        sb.append(FileWriter.INDENT).append("public static ").append(name).append(" getByLiteral(String literal) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("for (").append(name).append(" v : values()) {\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append(FileWriter.INDENT).append("if (v.getLiteral().equals(literal)) return v;\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("}\n");
        sb.append(FileWriter.INDENT).append(FileWriter.INDENT).append("return null;\n");
        sb.append(FileWriter.INDENT).append("}\n\n");

        sb.append(FileWriter.INDENT).append("public static List<").append(name).append("> VALUES = Collections.unmodifiableList(Arrays.asList(values()));\n\n");

        sb.append("}\n");

        String filePath = FileWriter.javaFilePath(ctx.outputDir, javaPkg, name, null);
        FileWriter.write(filePath, sb.toString());
    }

    /**
     * 统计 Java 文件数
     */
    private static int countJavaFiles(File dir) {
        int count = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    count += countJavaFiles(f);
                } else if (f.getName().endsWith(".java")) {
                    count++;
                }
            }
        }
        return count;
    }
}
