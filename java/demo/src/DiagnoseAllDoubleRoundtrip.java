package artop.demo;

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

/**
 * 双轮往返测试：对所有 53 个文件
 * 验证 artop 自身往返是否稳定（round1 == round2）
 * 如果稳定，说明差异来自原文件（非 artop 生成），不是我们的 bug
 */
public class DiagnoseAllDoubleRoundtrip {

    public static void main(String[] args) throws Exception {
        File inputDir = new File(args[0]);

        Autosar40Package.eINSTANCE.getClass();
        EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("arxml", new Autosar40ResourceFactoryImpl());
        MetaModelDescriptorRegistry.INSTANCE.addDescriptor(Autosar40ReleaseDescriptor.INSTANCE);

        ExtendedResourceSetImpl resourceSet = new ExtendedResourceSetImpl() {
            @Override
            protected IProxyResolverService getProxyResolverService(IMetaModelDescriptor descriptor) {
                return null;
            }
        };

        File[] files = inputDir.listFiles((d, n) -> n.endsWith(".arxml"));
        int stableCount = 0;
        int unstableCount = 0;
        int totalEmptyTagsLost = 0;

        for (File f : files) {
            String name = f.getName();
            try {
                // round1
                Resource r1 = resourceSet.getResource(URI.createFileURI(f.getAbsolutePath()), true);
                File f1 = new File("/tmp/diag_r1.arxml");
                f1.delete();
                Resource out1 = resourceSet.createResource(URI.createFileURI(f1.getAbsolutePath()));
                out1.getContents().addAll(r1.getContents());
                out1.save(Collections.emptyMap());
                r1.unload(); out1.unload();
                resourceSet.getResources().remove(r1);
                resourceSet.getResources().remove(out1);

                // round2
                Resource r2 = resourceSet.getResource(URI.createFileURI(f1.getAbsolutePath()), true);
                File f2 = new File("/tmp/diag_r2.arxml");
                f2.delete();
                Resource out2 = resourceSet.createResource(URI.createFileURI(f2.getAbsolutePath()));
                out2.getContents().addAll(r2.getContents());
                out2.save(Collections.emptyMap());
                r2.unload(); out2.unload();
                resourceSet.getResources().remove(r2);
                resourceSet.getResources().remove(out2);

                String c1 = readFile(f1);
                String c2 = readFile(f2);
                boolean stable = c1.equals(c2);

                // 统计原文件中空标签数量（形如 <XXX/>）
                int origEmpty = countSelfClosing(f);
                int r1Empty = countSelfClosing(f1);

                if (stable) {
                    stableCount++;
                    int lost = origEmpty - r1Empty;
                    if (lost > 0) {
                        totalEmptyTagsLost += lost;
                        System.out.println("[OK-stable] " + name + " | empty tags: orig=" + origEmpty + " r1=" + r1Empty + " (lost " + lost + ")");
                    } else {
                        System.out.println("[OK-stable] " + name + " | empty tags: orig=" + origEmpty + " r1=" + r1Empty);
                    }
                } else {
                    unstableCount++;
                    System.out.println("[UNSTABLE]  " + name + " | round1 != round2 (artop self-roundtrip unstable!)");
                }
            } catch (Exception e) {
                System.out.println("[ERROR]     " + name + ": " + e.getMessage());
                unstableCount++;
            }
        }

        System.out.println("\n=== Summary ===");
        System.out.println("Stable (round1==round2): " + stableCount + "/" + files.length);
        System.out.println("Unstable: " + unstableCount + "/" + files.length);
        System.out.println("Total empty tags lost (orig->r1): " + totalEmptyTagsLost);
    }

    private static String readFile(File f) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (java.io.FileInputStream fis = new java.io.FileInputStream(f)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = fis.read(buf)) > 0) {
                sb.append(new String(buf, 0, len, "UTF-8"));
            }
        }
        return sb.toString();
    }

    private static int countSelfClosing(File f) throws Exception {
        String c = readFile(f);
        int count = 0;
        int idx = 0;
        while ((idx = c.indexOf("/>", idx)) >= 0) {
            count++;
            idx += 2;
        }
        return count;
    }
}
