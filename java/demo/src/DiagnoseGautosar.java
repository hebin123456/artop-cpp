package artop.demo;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

import gautosar.ggenericstructure.ginfrastructure.GARObject;
import gautosar.util.GAutosarPackage;

/**
 * 诊断 gautosar 包结构
 */
public class DiagnoseGautosar {

    public static void main(String[] args) {
        System.out.println("=== GAutosarPackage ===");
        GAutosarPackage pkg = GAutosarPackage.eINSTANCE;
        System.out.println("eNS_URI: " + pkg.getNsURI());
        System.out.println("eNAME: " + pkg.getName());
        System.out.println("Classifiers: " + pkg.getEClassifiers().size());
        System.out.println("Subpackages: " + pkg.getESubpackages().size());

        System.out.println("\n=== GARObject EClass ===");
        System.out.println("GARObject class: " + GARObject.class.getName());

        // Check if GARObject's EClass is in GAutosarPackage
        for (Object o : pkg.getEClassifiers()) {
            if (o instanceof EClass) {
                EClass ec = (EClass) o;
                if (ec.getName().contains("ARObject") || ec.getName().equals("GARObject")) {
                    System.out.println("Found: " + ec.getName() + " in package " + ec.getEPackage().getNsURI());
                    for (var attr : ec.getEAttributes()) {
                        System.out.println("  Attr: " + attr.getName() + " containingClass.pkg=" +
                            attr.getEContainingClass().getEPackage().getNsURI());
                    }
                }
            }
        }

        System.out.println("\n=== Recursive subpackages ===");
        printSubpackages(pkg, 0);

        System.out.println("\n=== EPackage.Registry.INSTANCE keys (gautosar) ===");
        for (String key : EPackage.Registry.INSTANCE.keySet()) {
            if (key.contains("gautosar") || key.contains("artop")) {
                System.out.println("  " + key);
            }
        }
    }

    private static void printSubpackages(EPackage pkg, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + pkg.getName() + " (" + pkg.getNsURI() + ") classifiers=" + pkg.getEClassifiers().size());
        for (EPackage sub : pkg.getESubpackages()) {
            printSubpackages(sub, depth + 1);
        }
    }
}
