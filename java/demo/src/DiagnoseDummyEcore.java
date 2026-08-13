package artop.demo;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

/**
 * 检查 dummy.ecore 加载后 EAttribute 的 eType 是否为 null（断链引用）
 */
public class DiagnoseDummyEcore {

    public static void main(String[] args) throws Exception {
        String ecorePath = args[0];
        System.setProperty("jdk.xml.maxGeneralEntitySizeLimit", "0");
        System.setProperty("jdk.xml.entityExpansionLimit", "0");
        System.setProperty("jdk.xml.maxXMLNameLimit", "0");
        System.setProperty("jdk.xml.totalEntitySizeLimit", "0");

        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
        java.util.Map<String, Object> loadOpts = new java.util.HashMap<>();
        loadOpts.put("http://www.eclipse.org/emf/ecore/xmi/ProcessDanglingReference", "drop");
        rs.getLoadOptions().putAll(loadOpts);

        org.eclipse.emf.ecore.resource.Resource res = rs.getResource(
            org.eclipse.emf.common.util.URI.createFileURI(ecorePath), true);
        EPackage root = (EPackage) res.getContents().get(0);

        System.out.println("[diag] Root package: " + root.getName() + " nsURI=" + root.getNsURI());

        int totalAttr = 0, nullTypeAttr = 0, nullTypeRef = 0;
        java.util.Map<String, Integer> nullTypeNames = new java.util.TreeMap<>();

        java.util.List<int[]> stats = new java.util.ArrayList<>();
        stats.add(new int[]{0, 0, 0}); // totalAttr, nullAttr, nullRef
        collectStats(root, stats);

        totalAttr = stats.get(0)[0];
        nullTypeAttr = stats.get(0)[1];
        nullTypeRef = stats.get(0)[2];

        // 重新遍历统计 null eType 的 attribute 名字
        java.util.List<String> nullAttrDetails = new java.util.ArrayList<>();
        collectNullAttrs(root, nullAttrDetails);

        System.out.println("[diag] Total EAttributes: " + totalAttr);
        System.out.println("[diag] EAttributes with null eType: " + nullAttrDetails.size());
        System.out.println("[diag] EReferences with null eType: " + nullTypeRef);

        // 统计 null eType 的 attribute 名字分布
        java.util.Map<String, Integer> attrNameCount = new java.util.TreeMap<>();
        for (String s : nullAttrDetails) {
            attrNameCount.merge(s, 1, Integer::sum);
        }
        System.out.println("[diag] Null eType attribute name distribution:");
        for (var e : attrNameCount.entrySet()) {
            System.out.println("[diag]   " + e.getKey() + ": " + e.getValue());
        }
    }

    interface Callback { void found(String s); }

    private static void checkPackage(EPackage pkg, Callback cb) {}

    private static void collectNullAttrs(EPackage pkg, java.util.List<String> details) {
        for (EClassifier c : pkg.getEClassifiers()) {
            if (c instanceof EClass) {
                for (EStructuralFeature f : ((EClass) c).getEStructuralFeatures()) {
                    if (f instanceof EAttribute && f.getEType() == null) {
                        details.add(f.getName());
                    }
                }
            }
        }
        for (EPackage sub : pkg.getESubpackages()) {
            collectNullAttrs(sub, details);
        }
    }

    private static void collectStats(EPackage pkg, java.util.List<int[]> stats) {
        int[] s = stats.get(0);
        for (EClassifier c : pkg.getEClassifiers()) {
            if (c instanceof EClass) {
                for (EStructuralFeature f : ((EClass) c).getEStructuralFeatures()) {
                    if (f instanceof EAttribute) {
                        s[0]++;
                        if (f.getEType() == null) s[1]++;
                    } else if (f instanceof EReference) {
                        if (f.getEType() == null) s[2]++;
                    }
                }
            }
        }
        for (EPackage sub : pkg.getESubpackages()) {
            collectStats(sub, stats);
        }
    }
}
