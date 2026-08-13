package artop.demo;

import java.io.File;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
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
 * 双轮往返测试：加载→保存→加载→保存
 * 验证 artop 自身的往返是否稳定（第二轮是否与第一轮一致）
 */
public class DiagnoseDoubleRoundtrip {

    public static void main(String[] args) throws Exception {
        File arxmlFile = new File(args[0]);
        System.out.println("[diag] File: " + arxmlFile.getName());

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

        // 第一轮：加载原文件 → 保存到 round1.arxml
        URI inputUri = URI.createFileURI(arxmlFile.getAbsolutePath());
        Resource r1 = resourceSet.getResource(inputUri, true);
        File f1 = new File("/tmp/diag_round1.arxml");
        Resource out1 = resourceSet.createResource(URI.createFileURI(f1.getAbsolutePath()));
        out1.getContents().addAll(r1.getContents());
        out1.save(Collections.emptyMap());
        System.out.println("[diag] Round1 saved: " + f1.length() + " bytes (orig: " + arxmlFile.length() + ")");
        r1.unload(); out1.unload();
        resourceSet.getResources().remove(r1);
        resourceSet.getResources().remove(out1);

        // 第二轮：加载 round1.arxml → 保存到 round2.arxml
        Resource r2 = resourceSet.getResource(URI.createFileURI(f1.getAbsolutePath()), true);
        File f2 = new File("/tmp/diag_round2.arxml");
        Resource out2 = resourceSet.createResource(URI.createFileURI(f2.getAbsolutePath()));
        out2.getContents().addAll(r2.getContents());
        out2.save(Collections.emptyMap());
        System.out.println("[diag] Round2 saved: " + f2.length() + " bytes");
        r2.unload(); out2.unload();

        // 比较 round1 和 round2
        String c1 = readFile(f1);
        String c2 = readFile(f2);
        if (c1.equals(c2)) {
            System.out.println("[diag] Round1 == Round2: artop self-roundtrip is STABLE");
        } else {
            System.out.println("[diag] Round1 != Round2: artop self-roundtrip is UNSTABLE");
            // 找第一个差异
            int minLen = Math.min(c1.length(), c2.length());
            for (int i = 0; i < minLen; i++) {
                if (c1.charAt(i) != c2.charAt(i)) {
                    int start = Math.max(0, i - 50);
                    System.out.println("[diag]   First diff at char " + i + ":");
                    System.out.println("[diag]   r1: ..." + c1.substring(start, Math.min(i + 80, c1.length())) + "...");
                    System.out.println("[diag]   r2: ..." + c2.substring(start, Math.min(i + 80, c2.length())) + "...");
                    break;
                }
            }
        }

        // 统计空标签
        System.out.println("[diag] Empty <LITERALS/> count:");
        System.out.println("[diag]   orig: " + countEmpty(arxmlFile));
        System.out.println("[diag]   round1: " + countEmpty(f1));
        System.out.println("[diag]   round2: " + countEmpty(f2));
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

    private static int countEmpty(File f) throws Exception {
        String c = readFile(f);
        int count = 0;
        int idx = 0;
        while ((idx = c.indexOf("<LITERALS/>", idx)) >= 0) {
            count++;
            idx += 11;
        }
        return count;
    }
}
