import org.eclipse.emf.ecore.*;
import org.eclipse.emf.common.util.EList;
import autosar40.util.Autosar40Package;
import autosar40.genericstructure.generaltemplateclasses.documentation.textmodel.languagedatamodel.*;

public class TestRuntimeFeatureIds {
    public static void main(String[] args) throws Exception {
        // Force initialization
        Autosar40Package.eINSTANCE.getClass();

        // Get LLongName EClass
        EClass lLongName = LanguagedatamodelPackage.eINSTANCE.getLLongName();
        System.out.println("=== LLongName EClass ===");
        System.out.println("  name: " + lLongName.getName());
        System.out.println("  supertypes: " + lLongName.getESuperTypes());
        System.out.println("  featureCount: " + lLongName.getFeatureCount());

        // Print all features with their IDs
        EList<EStructuralFeature> allFeatures = lLongName.getEAllStructuralFeatures();
        System.out.println("  all features (" + allFeatures.size() + "):");
        for (int i = 0; i < allFeatures.size(); i++) {
            EStructuralFeature f = allFeatures.get(i);
            int featureID = lLongName.getFeatureID(f);
            System.out.println("    [" + i + "] featureID=" + featureID + " name=" + f.getName()
                + " containingClass=" + f.getEContainingClass().getName()
                + " type=" + (f instanceof EAttribute ? "Attr" : "Ref"));
        }

        // Check the mixed feature specifically
        EAttribute mixedFeature = null;
        for (EAttribute a : lLongName.getEAllAttributes()) {
            if (a.getName().equals("mixed")) {
                mixedFeature = a;
                break;
            }
        }
        if (mixedFeature != null) {
            System.out.println("\n  mixed feature:");
            System.out.println("    name: " + mixedFeature.getName());
            System.out.println("    featureID: " + lLongName.getFeatureID(mixedFeature));
            System.out.println("    featureKind: " + org.eclipse.emf.ecore.util.ExtendedMetaData.INSTANCE.getFeatureKind(mixedFeature));
            System.out.println("    containingClass: " + mixedFeature.getEContainingClass().getName());
        }

        // Check content kind
        int contentKind = org.eclipse.emf.ecore.util.ExtendedMetaData.INSTANCE.getContentKind(lLongName);
        System.out.println("\n  contentKind: " + contentKind + " (0=empty, 1=simple, 2=elementOnly, 3=mixed)");

        // Create an instance and test eGet
        System.out.println("\n=== Creating LLongName instance ===");
        LLongName obj = LanguagedatamodelFactory.eINSTANCE.createLLongName();
        Object result = obj.eGet(mixedFeature);
        System.out.println("  eGet(mixedFeature) returned: " + result);
        System.out.println("  type: " + result.getClass().getName());
        System.out.println("  is FeatureMap: " + (result instanceof org.eclipse.emf.ecore.util.FeatureMap));
    }
}
