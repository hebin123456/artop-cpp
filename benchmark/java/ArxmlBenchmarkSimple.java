// Java EMF arxml roundtrip benchmark（纯 EMF，不依赖 sphinx/artop standalone OSGi）
// 对齐 C++ ArxmlBenchmark，测量 load/save 耗时。
// 用纯 EMF ResourceSetImpl + Autosar40ResourceFactoryImpl，
// 避免 sphinx MetaModelDescriptorRegistry 触发 OSGi Platform 依赖。
package artop.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import autosar40.util.Autosar40Package;
import autosar40.util.Autosar40ResourceFactoryImpl;

public class ArxmlBenchmarkSimple {
    public static void main(String[] args) throws Exception {
        String inputFile = args.length > 0 ? args[0]
            : "/workspace/java/demo/output/ECUConfigurationParameters.arxml";
        String outputFile = args.length > 1 ? args[1]
            : "/tmp/bench_arxml_out_java.arxml";
        int iterations = args.length > 2 ? Integer.parseInt(args[2]) : 3;

        // 初始化：触发 Autosar40Package.eINSTANCE（注册 AUTOSAR 元模型包）
        Autosar40Package.eINSTANCE.getClass();
        // 注册 Autosar40ResourceFactoryImpl 到 "arxml" 扩展名
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("arxml", new Autosar40ResourceFactoryImpl());

        long fileSize = new File(inputFile).length();
        System.out.println("=== Java EMF Arxml Benchmark (pure EMF) ===");
        System.out.println("File: " + inputFile);
        System.out.printf("Size: %.4f MB (%d bytes)%n", fileSize / 1024.0 / 1024.0, fileSize);
        System.out.println("Iterations: " + iterations);
        System.out.println();

        double[] loadTimes = new double[iterations];
        double[] saveTimes = new double[iterations];

        for (int i = 0; i < iterations; i++) {
            // 每轮新建 ResourceSet 避免缓存影响
            ResourceSetImpl loadRs = new ResourceSetImpl();
            long loadStart = System.nanoTime();
            URI inputUri = URI.createFileURI(new File(inputFile).getAbsolutePath());
            Resource inputResource = loadRs.getResource(inputUri, true);
            long loadEnd = System.nanoTime();
            double loadMs = (loadEnd - loadStart) / 1_000_000.0;
            loadTimes[i] = loadMs;

            int rootCount = inputResource.getContents().size();
            int errCount = inputResource.getErrors().size();

            // Save
            long saveStart = System.nanoTime();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            inputResource.save(baos, Collections.emptyMap());
            long saveEnd = System.nanoTime();
            double saveMs = (saveEnd - saveStart) / 1_000_000.0;
            saveTimes[i] = saveMs;

            long outBytes = baos.size();
            if (i == iterations - 1) {
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFile)) {
                    baos.writeTo(fos);
                }
            } else {
                new File(outputFile).delete();
            }

            System.out.printf("Iter %d: load=%.0f ms, save=%.0f ms, total=%.0f ms | roots=%d, errors=%d, out=%d bytes%n",
                    i + 1, loadMs, saveMs, loadMs + saveMs, rootCount, errCount, outBytes);
        }

        double avgLoad, avgSave;
        if (iterations > 1) {
            double sumLoad = 0, sumSave = 0;
            for (int i = 1; i < iterations; i++) { sumLoad += loadTimes[i]; sumSave += saveTimes[i]; }
            avgLoad = sumLoad / (iterations - 1);
            avgSave = sumSave / (iterations - 1);
        } else {
            avgLoad = loadTimes[0]; avgSave = saveTimes[0];
        }
        System.out.println();
        System.out.println("=== Summary (excl. warmup) ===");
        System.out.printf("Avg load: %.0f ms (%.1f MB/s)%n", avgLoad, fileSize / 1024.0 / avgLoad);
        System.out.printf("Avg save: %.0f ms (%.1f MB/s)%n", avgSave, fileSize / 1024.0 / avgSave);
        System.out.printf("Avg total: %.0f ms%n", avgLoad + avgSave);
        System.out.println("=== DONE ===");
    }
}
