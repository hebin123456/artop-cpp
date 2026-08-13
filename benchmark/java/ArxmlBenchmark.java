// Java ARTOP arxml roundtrip benchmark —— 对齐 C++ ArxmlBenchmark.cpp
// 测量 load（反序列化）和 save（序列化）的耗时。
//
// 用法：java -cp <classpath> ArxmlBenchmark [input.arxml] [output.arxml] [iterations]
// 默认：/workspace/benchmark/data/large_100m.arxml, 3 iterations
package artop.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.sphinx.emf.ecore.proxymanagement.IProxyResolverService;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl;

import autosar40.util.Autosar40Package;
import autosar40.util.Autosar40ReleaseDescriptor;
import autosar40.util.Autosar40ResourceFactoryImpl;

public class ArxmlBenchmark {

    public static void main(String[] args) throws Exception {
        String inputFile = (args.length > 0) ? args[0]
            : "/workspace/benchmark/data/large_100m.arxml";
        String outputFile = (args.length > 1) ? args[1]
            : "/workspace/benchmark/data/large_100m_out_java.arxml";
        int iterations = (args.length > 2) ? Integer.parseInt(args[2]) : 3;

        // ============================================================
        // 初始化：严格遵循 artop Autosar4xStandaloneSetup.init() 模式
        // ============================================================
        // 1. 触发 Autosar40Package.eINSTANCE（注册 AUTOSAR 元模型包）
        Autosar40Package.eINSTANCE.getClass();

        // 2. 注册 XML 标准命名空间包（standalone 模式需手动注册）
        EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);

        // 3. 注册 Autosar40ResourceFactoryImpl 到 "arxml" 扩展名
        Autosar40ResourceFactoryImpl resourceFactory = new Autosar40ResourceFactoryImpl();
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("arxml", resourceFactory);

        // 4. 注册 release descriptor
        MetaModelDescriptorRegistry.INSTANCE.addDescriptor(Autosar40ReleaseDescriptor.INSTANCE);

        // 获取文件大小
        long fileSize = new File(inputFile).length();

        System.out.println("=== Java ARTOP Arxml Benchmark ===");
        System.out.println("File: " + inputFile);
        System.out.printf("Size: %.4f MB (%d bytes)%n", fileSize / 1024.0 / 1024.0, fileSize);
        System.out.println("Iterations: " + iterations);
        System.out.println();

        // 创建 ResourceSet：使用 sphinx ExtendedResourceSetImpl
        // （StandaloneAutosarResourceSetImpl 模式：重写 getProxyResolverService 返回 null）
        ExtendedResourceSetImpl resourceSet = new ExtendedResourceSetImpl() {
            @Override
            protected IProxyResolverService getProxyResolverService(IMetaModelDescriptor descriptor) {
                return null;
            }
        };

        double[] loadTimes = new double[iterations];
        double[] saveTimes = new double[iterations];

        for (int i = 0; i < iterations; i++) {
            // ===== Load =====
            long loadStart = System.nanoTime();
            URI inputUri = URI.createFileURI(new File(inputFile).getAbsolutePath());
            // 每轮新建 resourceSet 避免缓存影响（对齐 C++ 每轮新建 Resource）
            ExtendedResourceSetImpl loadRs = new ExtendedResourceSetImpl() {
                @Override
                protected IProxyResolverService getProxyResolverService(IMetaModelDescriptor descriptor) {
                    return null;
                }
            };
            Resource inputResource = loadRs.getResource(inputUri, true);
            long loadEnd = System.nanoTime();
            double loadMs = (loadEnd - loadStart) / 1_000_000.0;
            loadTimes[i] = loadMs;

            int rootCount = inputResource.getContents().size();
            int errCount = inputResource.getErrors().size();

            // ===== Save =====
            long saveStart = System.nanoTime();
            // 保存到 ByteArrayOutputStream（对齐 C++ 的 ostringstream）
            URI outputUri = URI.createFileURI(new File(outputFile).getAbsolutePath());
            Resource outputResource = loadRs.createResource(outputUri);
            outputResource.getContents().addAll(inputResource.getContents());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            outputResource.save(baos, Collections.emptyMap());
            long saveEnd = System.nanoTime();
            double saveMs = (saveEnd - saveStart) / 1_000_000.0;
            saveTimes[i] = saveMs;

            long outBytes = baos.size();
            // 仅最后一轮落盘（供验证 save 正确性），中间轮不写文件避免磁盘累积
            if (i == iterations - 1) {
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFile)) {
                    baos.writeTo(fos);
                }
            } else {
                // 非最后一轮：程序退出时清理输出文件
                new File(outputFile).delete();
            }

            System.out.printf("Iter %d: load=%.0f ms, save=%.0f ms, total=%.0f ms | roots=%d, errors=%d, out=%d bytes%n",
                    i + 1, loadMs, saveMs, loadMs + saveMs, rootCount, errCount, outBytes);
        }

        // 汇总（去掉第一轮 warmup）
        double avgLoad, avgSave;
        if (iterations > 1) {
            double sumLoad = 0, sumSave = 0;
            for (int i = 1; i < iterations; i++) {
                sumLoad += loadTimes[i];
                sumSave += saveTimes[i];
            }
            avgLoad = sumLoad / (iterations - 1);
            avgSave = sumSave / (iterations - 1);
        } else {
            avgLoad = loadTimes[0];
            avgSave = saveTimes[0];
        }

        System.out.println();
        System.out.println("=== Summary (excl. warmup) ===");
        System.out.printf("Avg load: %.0f ms (%.1f MB/s)%n", avgLoad, fileSize / 1024.0 / avgLoad);
        System.out.printf("Avg save: %.0f ms (%.1f MB/s)%n", avgSave, fileSize / 1024.0 / avgSave);
        System.out.printf("Avg total: %.0f ms%n", avgLoad + avgSave);
        System.out.println("=== DONE ===");
    }
}
