// Java EMF 纯 ecore XMI 序列化/反序列化基准测试 —— 对齐 C++ EcoreXmiBenchmark
// 使用 EMF 原始 Ecore 元模型，不涉及 artop 扩展。
package artop.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

public class EcoreXmiBenchmark {
    public static void main(String[] args) throws Exception {
        String inputFile = args.length > 0 ? args[0] : "/workspace/benchmark/data/xmi/ecore_5m.xmi";
        String outputFile = args.length > 1 ? args[1] : "/tmp/bench_ecore_out_java.xmi";
        int iterations = args.length > 2 ? Integer.parseInt(args[2]) : 3;

        // 注册 Ecore 元模型包（必须先触发 eINSTANCE）
        EPackage.Registry.INSTANCE.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
        // 注册 ecore resource factory
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new EcoreResourceFactoryImpl());

        long fileSize = new File(inputFile).length();
        System.out.println("=== Java EMF Ecore XMI Benchmark ===");
        System.out.println("File: " + inputFile);
        System.out.printf("Size: %.4f MB (%d bytes)%n", fileSize / 1024.0 / 1024.0, fileSize);
        System.out.println("Iterations: " + iterations);
        System.out.println();

        double[] loadTimes = new double[iterations];
        double[] saveTimes = new double[iterations];

        for (int i = 0; i < iterations; i++) {
            ResourceSetImpl rs = new ResourceSetImpl();
            long loadStart = System.nanoTime();
            URI inputUri = URI.createFileURI(new File(inputFile).getAbsolutePath());
            Resource inputResource = rs.getResource(inputUri, true);
            long loadEnd = System.nanoTime();
            double loadMs = (loadEnd - loadStart) / 1_000_000.0;
            loadTimes[i] = loadMs;

            int rootCount = inputResource.getContents().size();
            int errCount = inputResource.getErrors().size();

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
