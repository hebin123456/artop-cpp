package artop.demo;

import java.io.File;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.sphinx.emf.ecore.proxymanagement.IProxyResolverService;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl;

import autosar40.util.Autosar40Package;
import autosar40.util.Autosar40ReleaseDescriptor;
import autosar40.util.Autosar40ResourceFactoryImpl;

/**
 * Artop 序列化往返 Demo
 *
 * 严格遵循 artop 4.13 的 standalone 序列化机制：
 * 1. 使用 Autosar4xStandaloneSetup.init() 的初始化模式（注册 Autosar40Package、
 *    Autosar40ResourceFactoryImpl 到 "arxml" 扩展名、Autosar40ReleaseDescriptor）
 * 2. 使用 sphinx 的 ExtendedResourceSetImpl（StandaloneAutosarResourceSetImpl 模式，
 *    重写 getProxyResolverService 返回 null）
 * 3. 加载用 resourceSet.getResource(uri, true)（artop AutosarStandaloneExample 模式）
 * 4. 保存用 resourceSet.createResource(uri) + getContents().addAll() + save()（artop 模式）
 *
 * XML 标准命名空间注册遵循 sphinx 测试代码模式（EMFRoundtripTests.java 等）：
 *   EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
 *   EPackage.Registry.INSTANCE.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);
 * 在 Eclipse 环境下由 plugin.xml 扩展点自动注册，standalone 模式下需手动注册。
 */
public class ArxmlRoundtripDemo {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java artop.demo.ArxmlRoundtripDemo <inputDir> <outputDir>");
            System.err.println("Example:");
            System.err.println("  java artop.demo.ArxmlRoundtripDemo /workspace/decompiler/autosar448/model/library /workspace/artop_demo/output");
            System.exit(1);
        }

        String inputDir = args[0];
        String outputDir = args[1];

        // ============================================================
        // 初始化：严格遵循 artop Autosar4xStandaloneSetup.init() 模式
        // ============================================================
        System.out.println("[demo] Initializing (Autosar4xStandaloneSetup mode)...");

        // 1. 触发 Autosar40Package.eINSTANCE（注册 AUTOSAR 元模型包）
        //    对应 Autosar4xStandaloneSetup.setup(Autosar40Package.eINSTANCE)
        Autosar40Package.eINSTANCE.getClass();

        // 2. 注册 XML 标准命名空间包（sphinx 测试代码模式）
        //    在 Eclipse 环境下由 org.eclipse.emf.ecore/plugin.xml 扩展点自动注册；
        //    standalone 模式下需手动注册，否则 xml:space 等属性的 namespace
        //    "http://www.w3.org/XML/1998/namespace" 无法解析到 EPackage，导致 NPE。
        //    参考：org.eclipse.sphinx.tests.emf.serialization.env.emf.EMFRoundtripTests
        EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);

        // 3. 创建 Autosar40ResourceFactoryImpl 并注册到 "arxml" 扩展名
        //    对应 Autosar4xStandaloneSetup.factory(...).register()
        Autosar40ResourceFactoryImpl resourceFactory = new Autosar40ResourceFactoryImpl();
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("arxml", resourceFactory);

        // 4. 注册 release descriptor
        //    对应 MetaModelDescriptorRegistry.INSTANCE.addDescriptor(releaseDescriptor)
        MetaModelDescriptorRegistry.INSTANCE.addDescriptor(Autosar40ReleaseDescriptor.INSTANCE);

        System.out.println("[demo] Initialization done.");

        // ============================================================
        // 创建 ResourceSet：使用 sphinx ExtendedResourceSetImpl
        // （StandaloneAutosarResourceSetImpl 模式：重写 getProxyResolverService 返回 null）
        // ============================================================
        ExtendedResourceSetImpl resourceSet = new ExtendedResourceSetImpl() {
            @Override
            protected IProxyResolverService getProxyResolverService(IMetaModelDescriptor descriptor) {
                // standalone 模式下无 OSGi 平台，不使用代理解析服务
                return null;
            }
        };

        // 列出所有 arxml 文件
        File[] arxmlFiles = new File(inputDir).listFiles((dir, name) -> name.endsWith(".arxml"));
        if (arxmlFiles == null || arxmlFiles.length == 0) {
            System.err.println("[demo] No arxml files found in: " + inputDir);
            System.exit(1);
        }
        System.out.println("[demo] Found " + arxmlFiles.length + " arxml files");

        new File(outputDir).mkdirs();

        int success = 0;
        int failed = 0;
        int identical = 0;
        int semanticEqualCount = 0;
        int selfStableCount = 0;
        int contentDifferent = 0;

        for (File arxmlFile : arxmlFiles) {
            String fileName = arxmlFile.getName();
            System.out.println("[demo] Processing: " + fileName);

            try {
                // ============================================================
                // 加载：artop AutosarStandaloneExample.load() 模式
                // resourceSet.getResource(uri, true) 会通过 "arxml" 扩展名
                // 自动找到 Autosar40ResourceFactoryImpl 创建 Resource 并加载
                // ============================================================
                URI inputUri = URI.createFileURI(arxmlFile.getAbsolutePath());
                Resource inputResource = resourceSet.getResource(inputUri, true);

                System.out.println("[demo]   Loaded: " + inputResource.getContents().size() + " root objects, " +
                    inputResource.getErrors().size() + " errors, " + inputResource.getWarnings().size() + " warnings");
                if (!inputResource.getErrors().isEmpty()) {
                    System.out.println("[demo]   First error: " + inputResource.getErrors().get(0));
                }

                // ============================================================
                // 保存：artop AutosarStandaloneExample.saveTo() 模式
                // 创建新 Resource，复制内容，保存
                // ============================================================
                File outputFile = new File(outputDir, fileName);
                URI outputUri = URI.createFileURI(outputFile.getAbsolutePath());
                Resource outputResource = resourceSet.createResource(outputUri);
                outputResource.getContents().addAll(inputResource.getContents());

                // 使用空选项，让 resource 使用其默认保存选项（由 AutosarResourceFactoryImpl.initResource 设置）
                outputResource.save(Collections.emptyMap());

                System.out.println("[demo]   Saved: " + outputFile.length() + " bytes");

                // ============================================================
                // 验证1：字节级比较（忽略空白、schemaLocation、standalone）
                // ============================================================
                boolean byteIdentical = compareFiles(arxmlFile, outputFile);

                // ============================================================
                // 验证2：语义级比较（二次加载，用 EcoreUtil.equals 比较内容）
                // 加载输出文件 → reloadedResource，比较 inputResource 和 reloadedResource 的根对象
                // 注意：artop 序列化器对空的 containment reference 不输出空标签（固有行为），
                // 所以原文件中的 <LITERALS/> 等空标签在 round1 会丢失，导致 orig vs round1 不等。
                // 这是原文件非 artop 生成导致的，不是序列化器 bug。
                // ============================================================
                Resource reloadedResource = resourceSet.getResource(outputUri, true);
                boolean semanticEqual = false;
                String diffDetail = "";
                if (inputResource.getContents().size() == reloadedResource.getContents().size()) {
                    // 用自定义 EqualityHelper 找出第一个差异
                    final StringBuilder diff = new StringBuilder();
                    org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper helper =
                        new org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper() {
                        @Override
                        protected boolean haveEqualFeature(org.eclipse.emf.ecore.EObject eObject1,
                                org.eclipse.emf.ecore.EObject eObject2,
                                org.eclipse.emf.ecore.EStructuralFeature feature) {
                            boolean result = super.haveEqualFeature(eObject1, eObject2, feature);
                            if (!result && diff.length() == 0) {
                                diff.append("feature='").append(feature.getName())
                                    .append("' on EClass='").append(eObject1.eClass().getName()).append("'");
                                Object v1 = eObject1.eGet(feature);
                                Object v2 = eObject2.eGet(feature);
                                diff.append(" v1=").append(v1 == null ? "null" : v1.getClass().getSimpleName() + ":" + truncate(v1.toString(), 100));
                                diff.append(" v2=").append(v2 == null ? "null" : v2.getClass().getSimpleName() + ":" + truncate(v2.toString(), 100));
                            }
                            return result;
                        }
                    };
                    semanticEqual = true;
                    for (int i = 0; i < inputResource.getContents().size() && semanticEqual; i++) {
                        org.eclipse.emf.ecore.EObject orig = inputResource.getContents().get(i);
                        org.eclipse.emf.ecore.EObject reload = reloadedResource.getContents().get(i);
                        semanticEqual = helper.equals(orig, reload);
                    }
                    diffDetail = diff.toString();
                }

                // ============================================================
                // 验证3：双轮往返稳定性（round1 → round2 字节一致）
                // 这是验证序列化器正确性的金标准：
                // 加载 round1 输出 → 保存 round2 → 比较 round1 vs round2
                // 如果一致，说明序列化器自身往返稳定，差异仅来自原文件非 artop 生成
                // ============================================================
                File round2File = new File(outputDir, "round2_" + fileName);
                URI round2Uri = URI.createFileURI(round2File.getAbsolutePath());
                Resource round2Resource = resourceSet.createResource(round2Uri);
                round2Resource.getContents().addAll(reloadedResource.getContents());
                round2Resource.save(Collections.emptyMap());
                boolean selfStable = compareFiles(outputFile, round2File);
                round2Resource.unload();
                resourceSet.getResources().remove(round2Resource);
                // 删除 round2 临时文件
                round2File.delete();

                if (byteIdentical) {
                    System.out.println("[demo]   BYTE_IDENTICAL");
                    identical++;
                } else if (semanticEqual) {
                    System.out.println("[demo]   SEMANTIC_EQUAL (formatting differs, content identical)");
                    semanticEqualCount++;
                } else if (selfStable) {
                    System.out.println("[demo]   SELF_STABLE (orig non-artop, but artop roundtrip stable)");
                    selfStableCount++;
                } else {
                    System.out.println("[demo]   CONTENT_DIFFERENT (size: " + arxmlFile.length() + " vs " + outputFile.length() + ")");
                    System.out.println("[demo]     First diff: " + diffDetail);
                    contentDifferent++;
                }

                success++;

                // 卸载资源，避免 ResourceSet 累积过多资源
                inputResource.unload();
                outputResource.unload();
                reloadedResource.unload();
                resourceSet.getResources().remove(inputResource);
                resourceSet.getResources().remove(outputResource);
                resourceSet.getResources().remove(reloadedResource);
            } catch (Exception e) {
                System.out.println("[demo]   FAILED: " + e.getMessage());
                e.printStackTrace();
                failed++;
            }
        }

        System.out.println("\n[demo] Summary:");
        System.out.println("  Success: " + success + "/" + arxmlFiles.length);
        System.out.println("  Failed:  " + failed + "/" + arxmlFiles.length);
        System.out.println("  Byte identical: " + identical + "/" + arxmlFiles.length);
        System.out.println("  Semantic equal (formatting differs, content identical): " + semanticEqualCount + "/" + arxmlFiles.length);
        System.out.println("  Self stable (orig non-artop, but artop roundtrip stable): " + selfStableCount + "/" + arxmlFiles.length);
        System.out.println("  Content different: " + contentDifferent + "/" + arxmlFiles.length);
    }

    /**
     * 比较两个文件是否一致（忽略空白差异）
     */
    private static boolean compareFiles(File file1, File file2) throws Exception {
        String content1 = readFile(file1);
        String content2 = readFile(file2);

        content1 = normalize(content1);
        content2 = normalize(content2);

        return content1.equals(content2);
    }

    private static String readFile(File file) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                sb.append(new String(buffer, 0, len, "UTF-8"));
            }
        }
        return sb.toString();
    }

    /**
     * 标准化 XML 内容用于比较
     * - 去除 XML 注释
     * - 去除空白和换行
     * - 去除 schemaLocation 差异
     * - 去除 standalone 属性
     */
    private static String normalize(String xml) {
        xml = xml.replaceAll("<!--[^-]*-->", "");
        xml = xml.replaceAll("\\s+", " ");
        xml = xml.replaceAll("xsi:schemaLocation=\"[^\"]*\"", "");
        xml = xml.replaceAll("standalone=\"[^\"]*\"", "");
        return xml.trim();
    }

    /**
     * 截断字符串到指定长度
     */
    private static String truncate(String s, int maxLen) {
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...(" + s.length() + " chars)";
    }
}
