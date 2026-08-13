import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.impl.BasicEObjectImpl;
import autosar40.util.Autosar40Package;
import autosar40.genericstructure.generaltemplateclasses.documentation.textmodel.languagedatamodel.*;
import autosar40.genericstructure.generaltemplateclasses.documentation.textmodel.inlinetextmodel.*;
import java.lang.reflect.*;

public class TestDebug2 {
    public static void main(String[] args) throws Exception {
        Autosar40Package.eINSTANCE.getClass();
        ExtendedMetaData emd = ExtendedMetaData.INSTANCE;

        EClass lLongNameClass = LanguagedatamodelPackage.eINSTANCE.getLLongName();
        EObject obj = lLongNameClass.getEPackage().getEFactoryInstance().create(lLongNameClass);
        EAttribute mixedFeature = emd.getMixedFeature(lLongNameClass);

        // Check the owner EClass
        EClass ownerClass = (EClass) mixedFeature.getEContainingClass();
        System.out.println("ownerClass: " + ownerClass.getName());
        System.out.println("ownerClass.getInstanceClass(): " + ownerClass.getInstanceClass());
        System.out.println("ownerClass.getInstanceClassName(): " + ownerClass.getInstanceClassName());

        // Check if the Impl class is loaded
        try {
            Class<?> implClass = Class.forName("autosar40.genericstructure.generaltemplateclasses.documentation.textmodel.inlinetextmodel.impl.MixedContentForLongNameImpl");
            System.out.println("MixedContentForLongNameImpl loaded: " + implClass);
        } catch (Throwable t) {
            System.out.println("MixedContentForLongNameImpl NOT loaded: " + t);
        }

        // Check if the interface class is loaded
        try {
            Class<?> ifaceClass = Class.forName("autosar40.genericstructure.generaltemplateclasses.documentation.textmodel.inlinetextmodel.MixedContentForLongName");
            System.out.println("MixedContentForLongName (interface) loaded: " + ifaceClass);
        } catch (Throwable t) {
            System.out.println("MixedContentForLongName (interface) NOT loaded: " + t);
        }

        // Check feature ID
        System.out.println("\nmixedFeature.getFeatureID(): " + mixedFeature.getFeatureID());
        System.out.println("lLongNameClass.getFeatureID(mixedFeature): " + lLongNameClass.getFeatureID(mixedFeature));

        // Check eDerivedStructuralFeatureID via different paths
        System.out.println("\n=== eDerivedStructuralFeatureID checks ===");

        // 1. Call eDerivedStructuralFeatureID(EStructuralFeature) via reflection
        try {
            Method m = BasicEObjectImpl.class.getDeclaredMethod("eDerivedStructuralFeatureID", EStructuralFeature.class);
            m.setAccessible(true);
            Object result = m.invoke(obj, mixedFeature);
            System.out.println("eDerivedStructuralFeatureID(EStructuralFeature) = " + result);
        } catch (Throwable t) {
            System.out.println("eDerivedStructuralFeatureID(EStructuralFeature) FAILED: " + t);
        }

        // 2. Call eDerivedStructuralFeatureID(int, Class) with Impl class
        try {
            Class<?> implClass = Class.forName("autosar40.genericstructure.generaltemplateclasses.documentation.textmodel.inlinetextmodel.impl.MixedContentForLongNameImpl");
            Method m = obj.getClass().getMethod("eDerivedStructuralFeatureID", int.class, Class.class);
            Object result = m.invoke(obj, 9, implClass);
            System.out.println("eDerivedStructuralFeatureID(9, MixedContentForLongNameImpl.class) = " + result);
        } catch (Throwable t) {
            System.out.println("eDerivedStructuralFeatureID(9, Impl.class) FAILED: " + t);
        }

        // 3. Call eDerivedStructuralFeatureID(int, Class) with interface class
        try {
            Class<?> ifaceClass = Class.forName("autosar40.genericstructure.generaltemplateclasses.documentation.textmodel.inlinetextmodel.MixedContentForLongName");
            Method m = obj.getClass().getMethod("eDerivedStructuralFeatureID", int.class, Class.class);
            Object result = m.invoke(obj, 9, ifaceClass);
            System.out.println("eDerivedStructuralFeatureID(9, MixedContentForLongName.class) = " + result);
        } catch (Throwable t) {
            System.out.println("eDerivedStructuralFeatureID(9, Iface.class) FAILED: " + t);
        }

        // 4. List all declared methods of LLongNameImpl
        System.out.println("\n=== Declared methods of LLongNameImpl ===");
        for (Method m : obj.getClass().getDeclaredMethods()) {
            if (m.getName().contains("eDerived") || m.getName().contains("eBase")) {
                System.out.println("  " + m);
            }
        }

        // 5. Check what getInstanceClass returns and compare
        System.out.println("\n=== Class comparison ===");
        Class<?> instanceClass = ownerClass.getInstanceClass();
        System.out.println("instanceClass == MixedContentForLongNameImpl.class? " +
            (instanceClass == autosar40.genericstructure.generaltemplateclasses.documentation.textmodel.inlinetextmodel.impl.MixedContentForLongNameImpl.class));
    }
}
