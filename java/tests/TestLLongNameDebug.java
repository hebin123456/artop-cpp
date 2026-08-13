import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import autosar40.util.Autosar40Package;
import autosar40.genericstructure.generaltemplateclasses.documentation.textmodel.languagedatamodel.*;
import java.lang.reflect.*;

public class TestLLongNameDebug {
    public static void main(String[] args) throws Exception {
        Autosar40Package.eINSTANCE.getClass();
        ExtendedMetaData emd = ExtendedMetaData.INSTANCE;

        EClass lLongNameClass = LanguagedatamodelPackage.eINSTANCE.getLLongName();
        System.out.println("EClass: " + lLongNameClass.getName());
        System.out.println("InstanceClass: " + lLongNameClass.getInstanceClass());
        System.out.println("InstanceClassName: " + lLongNameClass.getInstanceClassName());

        // Create instance
        EObject obj = lLongNameClass.getEPackage().getEFactoryInstance().create(lLongNameClass);
        System.out.println("Impl class: " + obj.getClass().getName());

        // Get mixed feature
        EAttribute mixedFeature = emd.getMixedFeature(lLongNameClass);
        System.out.println("mixedFeature: " + mixedFeature);
        System.out.println("mixedFeature name: " + mixedFeature.getName());
        System.out.println("mixedFeature owner: " + mixedFeature.getEContainingClass());
        System.out.println("mixedFeature featureID in LLongName: " + lLongNameClass.getFeatureID(mixedFeature));
        System.out.println("mixedFeature featureID in owner: " + mixedFeature.getFeatureID());

        // Check eStaticFeatureCount
        try {
            Method m = obj.getClass().getMethod("eStaticFeatureCount");
            Object result = m.invoke(obj);
            System.out.println("eStaticFeatureCount: " + result);
        } catch (NoSuchMethodException e) {
            System.out.println("eStaticFeatureCount: not overridden (inherited)");
        }

        // Check feature ID constant
        try {
            Field f = LanguagedatamodelPackage.class.getField("LLONG_NAME__MIXED");
            System.out.println("LLONG_NAME__MIXED constant: " + f.get(null));
        } catch (NoSuchFieldException e) {
            System.out.println("LLONG_NAME__MIXED: not found as field");
        }

        // Direct call eGet(10)
        try {
            Method eGetInt = obj.getClass().getMethod("eGet", int.class, boolean.class, boolean.class);
            Object result10 = eGetInt.invoke(obj, 10, true, true);
            System.out.println("eGet(10, true, true) = " + result10);
            System.out.println("  type: " + (result10 != null ? result10.getClass().getName() : "null"));
        } catch (Throwable t) {
            System.out.println("eGet(10) FAILED: " + t);
        }

        // Call eGet(mixedFeature)
        try {
            Object result = obj.eGet(mixedFeature);
            System.out.println("eGet(mixedFeature) = " + result);
            System.out.println("  type: " + (result != null ? result.getClass().getName() : "null"));
        } catch (Throwable t) {
            System.out.println("eGet(mixedFeature) FAILED: " + t);
        }

        // Call getMixed() directly
        try {
            LLongName lln = (LLongName) obj;
            Object mixed = lln.getMixed();
            System.out.println("getMixed() = " + mixed);
            System.out.println("  type: " + (mixed != null ? mixed.getClass().getName() : "null"));
        } catch (Throwable t) {
            System.out.println("getMixed() FAILED: " + t);
        }

        // Check all features and their feature IDs
        System.out.println("\n=== All features of LLongName ===");
        for (EStructuralFeature f : lLongNameClass.getEAllStructuralFeatures()) {
            int fid = lLongNameClass.getFeatureID(f);
            EClass owner = (EClass) f.getEContainingClass();
            System.out.println("  fid=" + fid + " " + f.getName() + " owner=" +
                (owner != null ? owner.getName() : "null") + " type=" + f.getClass().getSimpleName());
        }

        // Check eStaticClass
        try {
            Method m = obj.getClass().getDeclaredMethod("eStaticClass");
            m.setAccessible(true);
            Object result = m.invoke(obj);
            System.out.println("\neStaticClass: " + result);
        } catch (Throwable t) {
            System.out.println("\neStaticClass check failed: " + t);
        }
    }
}
