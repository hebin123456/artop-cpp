package artop.codegen;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * 调试程序 - 检查加载 dummy.ecore 时 AUTOSAR 类的 feature 列表
 */
public class DebugFeatures {

    public static void main(String[] args) throws Exception {
        String ecorePath = args.length > 0 ? args[0] : "/workspace/decompiler/autosar448/model/dummy.ecore";

        System.setProperty("jdk.xml.maxGeneralEntitySizeLimit", "0");
        System.setProperty("jdk.xml.entityExpansionLimit", "0");
        System.setProperty("jdk.xml.maxXMLNameLimit", "0");
        System.setProperty("jdk.xml.totalEntitySizeLimit", "0");

        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());

        Map<String, Object> loadOpts = new HashMap<>();
        loadOpts.put("http://www.eclipse.org/emf/ecore/xmi/ProcessDanglingReference", "drop");
        rs.getLoadOptions().putAll(loadOpts);

        URI uri = URI.createFileURI(ecorePath);
        Resource res = rs.getResource(uri, true);
        EPackage rootPkg = (EPackage) res.getContents().get(0);

        // 查找 AUTOSAR 类
        EClass autosarClass = findClass(rootPkg, "AUTOSAR");
        if (autosarClass == null) {
            System.out.println("AUTOSAR class not found!");
            return;
        }

        System.out.println("=== AUTOSAR class ===");
        System.out.println("Name: " + autosarClass.getName());
        System.out.println("InstanceClassName: " + autosarClass.getInstanceClassName());
        System.out.println("Abstract: " + autosarClass.isAbstract());

        System.out.println("\n--- eSuperTypes ---");
        for (EClass sup : autosarClass.getESuperTypes()) {
            System.out.println("  " + sup.getName() + " (package: " + (sup.getEPackage() != null ? sup.getEPackage().getNsURI() : "null") + ")");
            System.out.println("    features: " + sup.getEStructuralFeatures());
            for (EStructuralFeature f : sup.getEStructuralFeatures()) {
                System.out.println("      " + f.getName() + " (container: " + (f.getEContainingClass() != null ? f.getEContainingClass().getName() : "null") + ")");
            }
        }

        System.out.println("\n--- getEStructuralFeatures() (direct) ---");
        for (EStructuralFeature f : autosarClass.getEStructuralFeatures()) {
            System.out.println("  " + f.getName() + " (container: " + (f.getEContainingClass() != null ? f.getEContainingClass().getName() : "null") + ")");
        }

        System.out.println("\n--- getEAllStructuralFeatures() (all) ---");
        int idx = 0;
        for (EStructuralFeature f : autosarClass.getEAllStructuralFeatures()) {
            System.out.println("  [" + idx + "] " + f.getName() + " (container: " + (f.getEContainingClass() != null ? f.getEContainingClass().getName() : "null") + ")");
            idx++;
        }

        System.out.println("\n--- getEAllSuperTypes() ---");
        for (EClass sup : autosarClass.getEAllSuperTypes()) {
            System.out.println("  " + sup.getName() + " (package: " + (sup.getEPackage() != null ? sup.getEPackage().getNsURI() : "null") + ")");
        }

        // 查找 ARObject 类
        EClass arObjectClass = findClass(rootPkg, "ARObject");
        if (arObjectClass != null) {
            System.out.println("\n=== ARObject class ===");
            System.out.println("\n--- eSuperTypes ---");
            for (EClass sup : arObjectClass.getESuperTypes()) {
                System.out.println("  " + sup.getName() + " (package: " + (sup.getEPackage() != null ? sup.getEPackage().getNsURI() : "null") + ")");
            }
            System.out.println("\n--- getEStructuralFeatures() (direct) ---");
            for (EStructuralFeature f : arObjectClass.getEStructuralFeatures()) {
                System.out.println("  " + f.getName() + " (container: " + (f.getEContainingClass() != null ? f.getEContainingClass().getName() : "null") + ")");
            }
            System.out.println("\n--- getEAllStructuralFeatures() (all) ---");
            idx = 0;
            for (EStructuralFeature f : arObjectClass.getEAllStructuralFeatures()) {
                System.out.println("  [" + idx + "] " + f.getName() + " (container: " + (f.getEContainingClass() != null ? f.getEContainingClass().getName() : "null") + ")");
                idx++;
            }
        }
    }

    private static EClass findClass(EPackage pkg, String name) {
        for (EClassifier c : pkg.getEClassifiers()) {
            if (c instanceof EClass && c.getName().equals(name)) {
                return (EClass) c;
            }
        }
        for (EPackage sub : pkg.getESubpackages()) {
            EClass found = findClass(sub, name);
            if (found != null) return found;
        }
        return null;
    }
}
