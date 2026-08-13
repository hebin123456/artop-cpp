import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import autosar40.util.Autosar40Package;
import java.util.*;

public class TestInheritanceHierarchy {
    public static void main(String[] args) throws Exception {
        Autosar40Package.eINSTANCE.getClass();

        String[] targets = {"LLongName", "LParagraph", "LOverviewParagraph", "LVerbatim", "LPlainText",
                            "MixedContentForLongName", "MixedContentForParagraph", "MixedContentForOverviewParagraph",
                            "MixedContentForVerbatim", "LanguageSpecific"};

        for (Object pkgObj : EPackage.Registry.INSTANCE.values()) {
            if (!(pkgObj instanceof EPackage)) continue;
            EPackage pkg = (EPackage) pkgObj;
            String nsUri = pkg.getNsURI();
            if (nsUri == null || !nsUri.startsWith("http://autosar.org/schema/r4.0/autosar40")) continue;

            for (EClassifier ec : pkg.getEClassifiers()) {
                if (!(ec instanceof EClass)) continue;
                EClass eClass = (EClass) ec;
                for (String target : targets) {
                    if (!eClass.getName().equals(target)) continue;
                    System.out.println("\n=== " + target + " (nsURI=" + nsUri + ") ===");
                    System.out.println("  SuperTypes:");
                    for (EClass st : eClass.getESuperTypes()) {
                        System.out.println("    " + st.getName() + " (from " + (st.getEPackage() != null ? st.getEPackage().getNsURI() : "?") + ")");
                    }
                    System.out.println("  AllSuperTypes:");
                    Set<EClass> visited = new HashSet<>();
                    printAllSuperTypes(eClass, "    ", visited);
                    System.out.println("  AllStructuralFeatures (own + inherited):");
                    List<EStructuralFeature> all = eClass.getEAllStructuralFeatures();
                    for (int i = 0; i < all.size(); i++) {
                        EStructuralFeature f = all.get(i);
                        EClass owner = (EClass) f.getEContainingClass();
                        String ownerName = owner != null ? owner.getName() : "null";
                        String kind = "";
                        if (f instanceof EAttribute) {
                            int fk = ExtendedMetaData.INSTANCE.getFeatureKind(f);
                            kind = " [Attr kind=" + fk + "]";
                        } else {
                            kind = " [Ref]";
                        }
                        int fid = eClass.getFeatureID(f);
                        System.out.println("    [" + i + "] fid=" + fid + " " + f.getName() + " owner=" + ownerName + kind);
                    }
                    break;
                }
            }
        }
    }

    static void printAllSuperTypes(EClass eClass, String indent, Set<EClass> visited) {
        if (!visited.add(eClass)) return;
        for (EClass st : eClass.getESuperTypes()) {
            System.out.println(indent + st.getName() + " (from " + (st.getEPackage() != null ? st.getEPackage().getNsURI() : "?") + ")");
            printAllSuperTypes(st, indent + "  ", visited);
        }
    }
}
