import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TestGautosarNsUri {
    public static void main(String[] args) throws Exception {
        ResourceSetImpl rs = new ResourceSetImpl();
        Map<String, Object> extToFactory = rs.getResourceFactoryRegistry().getExtensionToFactoryMap();
        extToFactory.put("ecore", new EcoreResourceFactoryImpl());

        // 加载 dummy.ecore
        Resource res = rs.getResource(org.eclipse.emf.common.util.URI.createURI(
            new File("/workspace/decompiler/autosar448/model/dummy.ecore").toURI().toString()), true);

        // 遍历所有 EClass，找 GAUTOSAR
        for (var iter = rs.getAllContents(); iter.hasNext(); ) {
            var obj = iter.next();
            if (obj instanceof EClass) {
                EClass ec = (EClass) obj;
                if (ec.getName().equals("GAUTOSAR") || ec.getName().equals("GARObject")) {
                    EPackage pkg = ec.getEPackage();
                    System.out.println("EClass: " + ec.getName());
                    System.out.println("  package: " + pkg.getName());
                    System.out.println("  nsURI: " + pkg.getNsURI());
                    System.out.println("  nsPrefix: " + pkg.getNsPrefix());
                    System.out.println("  features: " + ec.getEAllStructuralFeatures().size());
                    for (var f : ec.getEAllStructuralFeatures()) {
                        System.out.println("    " + f.getName() + " (containingClass.pkg.nsURI=" +
                            f.getEContainingClass().getEPackage().getNsURI() + ")");
                    }
                }
            }
        }

        // 也检查 AUTOSAR 的 supertypes
        for (var iter = rs.getAllContents(); iter.hasNext(); ) {
            var obj = iter.next();
            if (obj instanceof EClass) {
                EClass ec = (EClass) obj;
                if (ec.getName().equals("AUTOSAR")) {
                    System.out.println("\nAUTOSAR supertypes:");
                    for (EClass sup : ec.getESuperTypes()) {
                        EPackage pkg = sup.getEPackage();
                        System.out.println("  " + sup.getName() + " | pkg=" + pkg.getName() +
                            " | nsURI=" + pkg.getNsURI() + " | features=" + sup.getEAllStructuralFeatures().size());
                    }
                }
            }
        }
    }
}
