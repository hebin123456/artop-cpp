// JavaEMFRoundtrip.java —— 用 Java EMF 加载 + 保存 GenericsGoCrazy.ecore，
// 输出 canonical Java EMF 序列化结果，作为 C++ 端字节级对比的 ground truth。
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

public class JavaEMFRoundtrip {
    public static void main(String[] args) throws Exception {
        EcorePackage.eINSTANCE.eClass();
        // 注册 EcoreResourceFactoryImpl 用于 .ecore 文件
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMLResourceFactoryImpl());

        String ecorePath = args.length >= 1 ? args[0]
            : "/workspace/libs/opensource/emf/tests/org.eclipse.emf.test.examples/data/htmlExporter/GenericsGoCrazy.ecore";

        URI uri = URI.createFileURI(ecorePath);
        Resource res = rs.createResource(uri);
        java.io.File f = new java.io.File(ecorePath);
        try (InputStream in = Files.newInputStream(Paths.get(ecorePath))) {
            res.load(in, Collections.emptyMap());
        }

        System.err.println("Loaded: " + res.getContents().size() + " root objects");
        if (!res.getContents().isEmpty() && res.getContents().get(0) instanceof EPackage) {
            EPackage pkg = (EPackage) res.getContents().get(0);
            System.err.println("  pkg name=" + pkg.getName() + " nsURI=" + pkg.getNsURI() + " classifiers=" + pkg.getEClassifiers().size());
        }

        // 保存
        Map<String, Object> opts = new HashMap<>();
        if (args.length >= 3) {
            // 显式覆盖 lineWidth
            opts.put(org.eclipse.emf.ecore.xmi.XMLResource.OPTION_LINE_WIDTH, Integer.valueOf(args[2]));
            System.err.println("Set OPTION_LINE_WIDTH=" + args[2]);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        res.save(out, opts);
        String result = out.toString("UTF-8");

        String outPath = args.length >= 2 ? args[1] : "/workspace/cpp/emf-cpp/GenericsGoCrazy.java-roundtrip.ecore";
        Files.write(Paths.get(outPath), result.getBytes(StandardCharsets.UTF_8));
        System.err.println("Wrote: " + outPath + " (" + result.length() + " chars)");
        // 打印到 stdout 前 200 字符
        String preview = result.length() > 200 ? result.substring(0, 200) + "..." : result;
        System.out.println(preview);
    }
}
