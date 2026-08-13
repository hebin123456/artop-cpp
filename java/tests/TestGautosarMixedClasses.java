import org.eclipse.emf.ecore.*;
import org.eclipse.emf.common.util.EList;
import autosar40.util.Autosar40Package;
import java.util.*;

public class TestGautosarMixedClasses {
    public static void main(String[] args) throws Exception {
        Autosar40Package.eINSTANCE.getClass();

        // Check gautosar classes
        List<EPackage> allPkgs = new ArrayList<>();
        for (Object pkgObj : EPackage.Registry.INSTANCE.values()) {
            if (pkgObj instanceof EPackage) allPkgs.add((EPackage) pkgObj);
        }
        for (EPackage pkg : allPkgs) {
            String nsUri = pkg.getNsURI();
            if (nsUri == null || !nsUri.startsWith("http://artop.org/gautosar")) continue;

            for (EClassifier c : pkg.getEClassifiers()) {
                if (!(c instanceof EClass)) continue;
                EClass cls = (EClass) c;
                int contentKind = org.eclipse.emf.ecore.util.ExtendedMetaData.INSTANCE.getContentKind(cls);
                if (contentKind != 3) continue;

                System.out.println("=== gautosar mixed class: " + cls.getName() + " (nsURI=" + nsUri + ") ===");
                System.out.println("  abstract=" + cls.isAbstract() + " interface=" + cls.isInterface());

                EAttribute mixedFeature = org.eclipse.emf.ecore.util.ExtendedMetaData.INSTANCE.getMixedFeature(cls);
                if (mixedFeature == null) {
                    System.out.println("  getMixedFeature returned null!");
                    continue;
                }
                System.out.println("  mixedFeature: " + mixedFeature.getName() + " featureID=" + cls.getFeatureID(mixedFeature));

                if (!cls.isAbstract() && !cls.isInterface()) {
                    try {
                        EObject obj = pkg.getEFactoryInstance().create(cls);
                        Object result = obj.eGet(mixedFeature);
                        System.out.println("  eGet(mixed) returned: " + result.getClass().getName());
                        System.out.println("  is FeatureMap: " + (result instanceof org.eclipse.emf.ecore.util.FeatureMap));
                    } catch (Exception e) {
                        System.out.println("  ERROR: " + e);
                    }
                }
            }
        }

        // Also check XMLTypePackage classes
        System.out.println("\n=== Checking XMLTypePackage ===");
        EPackage xmlTypePkg = EPackage.Registry.INSTANCE.getEPackage("http://www.eclipse.org/emf/2003/XMLType");
        if (xmlTypePkg != null) {
            for (EClassifier c : xmlTypePkg.getEClassifiers()) {
                if (!(c instanceof EClass)) continue;
                EClass cls = (EClass) c;
                int contentKind = org.eclipse.emf.ecore.util.ExtendedMetaData.INSTANCE.getContentKind(cls);
                System.out.println("  " + cls.getName() + ": contentKind=" + contentKind);
                if (contentKind == 3) {
                    EAttribute mixedFeature = org.eclipse.emf.ecore.util.ExtendedMetaData.INSTANCE.getMixedFeature(cls);
                    if (mixedFeature != null) {
                        System.out.println("    mixedFeature: " + mixedFeature.getName());
                    }
                }
            }
        }

        System.out.println("\nDone.");
    }
}
