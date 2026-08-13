package artop.demo;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

/**
 * Java artop arxml 读写 benchmark，对齐 C++ benchmark/cpp/arxml_benchmark。
 * 测量 load（反序列化）和 save（序列化）的耗时，多轮取均值（去掉 warmup）。
 */
public class ArxmlBenchmark {

    public static void main(String[] args) throws Exception {
        String inputFile = (args.length >= 1) ? args[0]
            : "/workspace/java/demo/output/ECUConfigurationParameters.arxml";
        int iterations = (args.length >= 2) ? Integer.parseInt(args[1]) : 4;

        java.io.File f = new java.io.File(inputFile);
        double fileSize = f.length();

        // 初始化：严格遵循 artop Autosar4xStandaloneSetup.init() 模式
        Autosar40Package.eINSTANCE.getClass();
        EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
            .put("arxml", new Autosar40ResourceFactoryImpl());
        // 注意：standalone 模式下 MetaModelDescriptorRegistry.addDescriptor 会触发
        // org.eclipse.core.runtime.Platform 静态初始化（需 OSGi），此处跳过。
        // Autosar40ReleaseDescriptor 仅用于元模型识别/scoping，加载序列化不强制需要。

        System.out.println("=== Java artop Arxml Benchmark ===");
        System.out.println("File: " + inputFile);
        System.out.printf("Size: %.1f MB (%d bytes)%n", fileSize / 1024.0 / 1024.0, (long) fileSize);
        System.out.println("Iterations: " + iterations);
        System.out.println();

        long[] loadTimes = new long[iterations];
        long[] saveTimes = new long[iterations];
        int rootCount = 0;
        long outBytes = 0;

        for (int i = 0; i < iterations; i++) {
            // 每轮新建 ResourceSet（对齐 C++ 每轮新建 resource）
            // standalone 模式下 ExtendedResourceSetImpl 会触发 Platform 静态初始化（需 OSGi），
            // 改用纯 EMF ResourceSetImpl + 手动注册的 Autosar40ResourceFactoryImpl。
            org.eclipse.emf.ecore.resource.impl.ResourceSetImpl resourceSet =
                new org.eclipse.emf.ecore.resource.impl.ResourceSetImpl();

            // ===== Load =====
            long loadStart = System.nanoTime();
            URI inputUri = URI.createFileURI(inputFile);
            Resource resource = resourceSet.getResource(inputUri, true);
            long loadEnd = System.nanoTime();
            long loadMs = (loadEnd - loadStart) / 1_000_000;
            loadTimes[i] = loadMs;
            rootCount = resource.getContents().size();

            // ===== Save =====
            long saveStart = System.nanoTime();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            resource.save(out, Collections.emptyMap());
            long saveEnd = System.nanoTime();
            long saveMs = (saveEnd - saveStart) / 1_000_000;
            saveTimes[i] = saveMs;
            outBytes = out.size();

            resource.unload();
            resourceSet.getResources().clear();

            System.out.printf("Iter %d: load=%d ms, save=%d ms, total=%d ms | roots=%d, out=%d bytes%n",
                i + 1, loadMs, saveMs, loadMs + saveMs, rootCount, outBytes);
        }

        // 汇总（去掉第一轮 warmup）
        double avgLoad, avgSave;
        if (iterations > 1) {
            long sumLoad = 0, sumSave = 0;
            for (int i = 1; i < iterations; i++) {
                sumLoad += loadTimes[i];
                sumSave += saveTimes[i];
            }
            avgLoad = sumLoad / (double) (iterations - 1);
            avgSave = sumSave / (double) (iterations - 1);
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
