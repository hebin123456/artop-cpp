package com.example.arxml.validation.headless;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
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
 * OSGi 环境下的 artop arxml 读写 benchmark，对齐 C++ benchmark/cpp/arxml_benchmark。
 * 在 Equinox OSGi 内运行（Platform 可初始化），用完整的 artop 序列化器。
 *
 * 参数：-input <path> [-iterations N] [-output <path>]
 *   -input      输入 arxml 文件
 *   -iterations 轮数（默认 4，去掉 warmup）
 *   -output     最后一轮落盘到该路径（用于互读写测试，默认不落盘）
 */
public class ArxmlBenchmarkApplication implements IApplication {

    @Override
    public Object start(IApplicationContext ctx) throws Exception {
        String[] args = (String[]) ctx.getArguments().get("application.args");
        String inputFile = null;
        String outputFile = null;
        int iterations = 4;
        for (int i = 0; args != null && i < args.length; i++) {
            if ("-input".equals(args[i]) && i + 1 < args.length) inputFile = args[++i];
            else if ("-iterations".equals(args[i]) && i + 1 < args.length) iterations = Integer.parseInt(args[++i]);
            else if ("-output".equals(args[i]) && i + 1 < args.length) outputFile = args[++i];
        }
        if (inputFile == null) {
            System.err.println("Usage: -input <arxml> [-iterations N] [-output <path>]");
            return Integer.valueOf(1);
        }

        double fileSize = new File(inputFile).length();

        // 初始化：artop Autosar4xStandaloneSetup.init() 模式（OSGi 下完整可用）
        Autosar40Package.eINSTANCE.getClass();
        EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
            .put("arxml", new Autosar40ResourceFactoryImpl());
        MetaModelDescriptorRegistry.INSTANCE.addDescriptor(Autosar40ReleaseDescriptor.INSTANCE);

        System.out.println("=== Java artop Arxml Benchmark (OSGi) ===");
        System.out.println("File: " + inputFile);
        System.out.printf("Size: %.1f MB (%d bytes)%n", fileSize / 1024.0 / 1024.0, (long) fileSize);
        System.out.println("Iterations: " + iterations);
        System.out.println();

        long[] loadTimes = new long[iterations];
        long[] saveTimes = new long[iterations];
        int rootCount = 0;
        long outBytes = 0;

        for (int i = 0; i < iterations; i++) {
            ExtendedResourceSetImpl resourceSet = new ExtendedResourceSetImpl() {
                @Override
                protected IProxyResolverService getProxyResolverService(IMetaModelDescriptor descriptor) {
                    return null;
                }
            };

            // ===== Load =====
            long loadStart = System.nanoTime();
            URI inputUri = URI.createFileURI(inputFile);
            Resource resource = resourceSet.getResource(inputUri, true);
            long loadEnd = System.nanoTime();
            long loadMs = (loadEnd - loadStart) / 1_000_000;
            loadTimes[i] = loadMs;
            rootCount = resource.getContents().size();
            int errs = resource.getErrors().size();

            // ===== Save =====
            long saveStart = System.nanoTime();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            resource.save(out, Collections.emptyMap());
            long saveEnd = System.nanoTime();
            long saveMs = (saveEnd - saveStart) / 1_000_000;
            saveTimes[i] = saveMs;
            outBytes = out.size();

            // 最后一轮落盘（用于互读写测试）
            if (i == iterations - 1 && outputFile != null) {
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(out.toByteArray());
                }
            }

            resource.unload();
            resourceSet.getResources().clear();

            System.out.printf("Iter %d: load=%d ms, save=%d ms, total=%d ms | roots=%d, errors=%d, out=%d bytes%n",
                i + 1, loadMs, saveMs, loadMs + saveMs, rootCount, errs, outBytes);
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
        return IApplication.EXIT_OK;
    }

    @Override
    public void stop() {}
}
