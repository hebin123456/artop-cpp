import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.impl.BasicEObjectImpl;
import autosar40.util.Autosar40Package;
import autosar40.genericstructure.generaltemplateclasses.documentation.textmodel.languagedatamodel.*;
import java.lang.reflect.*;

public class TestFeatureIdResolution {
    public static void main(String[] args) throws Exception {
        Autosar40Package.eINSTANCE.getClass();
        ExtendedMetaData emd = ExtendedMetaData.INSTANCE;

        EClass lLongNameClass = LanguagedatamodelPackage.eINSTANCE.getLLongName();
        EObject obj = lLongNameClass.getEPackage().getEFactoryInstance().create(lLongNameClass);
        EAttribute mixedFeature = emd.getMixedFeature(lLongNameClass);

        System.out.println("=== Feature ID resolution ===");
        System.out.println("eClass().getFeatureID(mixedFeature) = " + lLongNameClass.getFeatureID(mixedFeature));

        // Check eDerivedStructuralFeatureID
        try {
            Method m = obj.getClass().getDeclaredMethod("eDerivedStructuralFeatureID", EStructuralFeature.class);
            m.setAccessible(true);
            Object result = m.invoke(obj, mixedFeature);
            System.out.println("eDerivedStructuralFeatureID(mixedFeature) = " + result);
        } catch (NoSuchMethodException e) {
            System.out.println("eDerivedStructuralFeatureID(EStructuralFeature) not overridden");
            // Call the inherited version
            int fid = obj.eClass().getFeatureID(mixedFeature);
            System.out.println("  inherited returns: " + fid);
        }

        // Check if eBaseStructuralFeatureID/eDerivedStructuralFeatureID(int, Class) exist
        System.out.println("\n=== Checking eBaseStructuralFeatureID/eDerivedStructuralFeatureID overrides ===");
        for (Method m : obj.getClass().getDeclaredMethods()) {
            if (m.getName().equals("eBaseStructuralFeatureID") || m.getName().equals("eDerivedStructuralFeatureID")) {
                System.out.println("  " + m);
            }
        }

        // Check what eGet(EStructuralFeature, boolean, boolean) does
        System.out.println("\n=== Checking eGet(EStructuralFeature, ...) overrides ===");
        for (Method m : obj.getClass().getMethods()) {
            if (m.getName().equals("eGet") && m.getParameterCount() >= 1) {
                Class<?>[] params = m.getParameterTypes();
                if (params[0] == EStructuralFeature.class) {
                    System.out.println("  " + m + " (declared in " + m.getDeclaringClass().getSimpleName() + ")");
                }
            }
        }
        for (Method m : obj.getClass().getSuperclass().getDeclaredMethods()) {
            if (m.getName().equals("eGet") && m.getParameterCount() >= 1) {
                Class<?>[] params = m.getParameterTypes();
                if (params[0] == EStructuralFeature.class) {
                    System.out.println("  superclass: " + m + " (declared in " + m.getDeclaringClass().getSimpleName() + ")");
                }
            }
        }

        // Check the superclass hierarchy
        System.out.println("\n=== Class hierarchy ===");
        Class<?> c = obj.getClass();
        while (c != null) {
            System.out.println("  " + c.getName());
            c = c.getSuperclass();
        }

        // Try calling eGet with different signatures
        System.out.println("\n=== Direct eGet calls ===");
        // eGet(EStructuralFeature)
        Object r1 = obj.eGet(mixedFeature);
        System.out.println("eGet(EStructuralFeature) = " + r1 + " (" + r1.getClass().getSimpleName() + ")");

        // eGet(EStructuralFeature, boolean)
        Object r2 = obj.eGet(mixedFeature, true);
        System.out.println("eGet(EStructuralFeature, true) = " + r2 + " (" + r2.getClass().getSimpleName() + ")");

        // eGet(EStructuralFeature, boolean, boolean)
        Object r3 = ((BasicEObjectImpl)obj).eGet(mixedFeature, true, true);
        System.out.println("eGet(EStructuralFeature, true, true) = " + r3 + " (" + r3.getClass().getSimpleName() + ")");

        // eGet(int, boolean, boolean)
        Method eGetInt = obj.getClass().getMethod("eGet", int.class, boolean.class, boolean.class);
        Object r4 = eGetInt.invoke(obj, 10, true, true);
        System.out.println("eGet(10, true, true) = " + r4 + " (" + r4.getClass().getSimpleName() + ")");

        // Check what eDerivedStructuralFeatureID returns via reflection
        System.out.println("\n=== eDerivedStructuralFeatureID via reflection ===");
        try {
            Method m = BasicEObjectImpl.class.getDeclaredMethod("eDerivedStructuralFeatureID", EStructuralFeature.class);
            m.setAccessible(true);
            Object result = m.invoke(obj, mixedFeature);
            System.out.println("BasicEObjectImpl.eDerivedStructuralFeatureID(mixedFeature) = " + result);
        } catch (Throwable t) {
            System.out.println("Failed: " + t);
        }
    }
}
