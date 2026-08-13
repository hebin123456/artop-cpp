import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emf.ecore.util.FeatureMapUtil.Validator;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import java.util.List;
import java.lang.reflect.Field;

/**
 * 对比官方 autosar448 jar 和 dummy jar 的 mixed feature validator 行为
 * 用法: java TestValidatorCompare <rootPackageClassName>
 *   官方: org.artop.aal.autosar448.autosar40.Autosar448Package
 *   dummy: autosar40.Autosar40Package
 */
public class TestValidatorCompare {
    public static void main(String[] args) throws Exception {
        String rootPackageClassName = args[0];
        Class<?> rootPkgClass = Class.forName(rootPackageClassName);
        Object eINSTANCE = rootPkgClass.getField("eINSTANCE").get(null);
        System.out.println("Initialized: " + rootPackageClassName);

        EPackage rootPkg = (EPackage) eINSTANCE;
        System.out.println("Root package: " + rootPkg.getName() + " nsURI=" + rootPkg.getNsURI());
        System.out.println("Classifier count: " + rootPkg.getEClassifiers().size());

        testClass("http://autosar.org/schema/r4.0/autosar40/gs/gtc/d/tm/itm", "MixedContentForLongName");
        testClass("http://autosar.org/schema/r4.0/autosar40/gs/gtc/d/tm/ldm", "LLongName");
        testClass("http://autosar.org/schema/r4.0/autosar40/gs/gtc/d/tm/itm", "MixedContentForParagraph");
        testClass("http://autosar.org/schema/r4.0/autosar40/gs/gtc/d/tm/itm", "MixedContentForPlainText");
    }

    static void testClass(String nsURI, String className) {
        System.out.println("\n=== " + className + " (nsURI=" + nsURI + ") ===");
        EPackage pkg = EPackage.Registry.INSTANCE.getEPackage(nsURI);
        if (pkg == null) {
            System.out.println("Package not found!");
            return;
        }
        EClass eClass = (EClass) pkg.getEClassifier(className);
        if (eClass == null) {
            System.out.println("EClass not found!");
            return;
        }

        int contentKind = ExtendedMetaData.INSTANCE.getContentKind(eClass);
        System.out.println("ContentKind: " + contentKind + " (0=UNSPECIFIED,1=EMPTY,2=SIMPLE,3=MIXED,4=ELEMENT)");

        EAttribute mixedFeature = ExtendedMetaData.INSTANCE.getMixedFeature(eClass);
        System.out.println("getMixedFeature: " + mixedFeature);

        // 查找 mixed feature
        for (EStructuralFeature f : eClass.getEAllStructuralFeatures()) {
            if (f.getName().equals("mixed")) {
                System.out.println("'mixed' feature: " + f);
                System.out.println("  == getMixedFeature? " + (f == mixedFeature));
                System.out.println("  featureKind: " + ExtendedMetaData.INSTANCE.getFeatureKind(f)
                    + " (0=UNSPEC,1=ATTR,2=SIMPLE,3=GROUP,4=ELEMENT,5=ELEMENT_WILDCARD)");
                System.out.println("  wildcards: " + ExtendedMetaData.INSTANCE.getWildcards(f));
                System.out.println("  containingClass: " + f.getEContainingClass().getName());

                Validator validator = FeatureMapUtil.getValidator(eClass, f);
                System.out.println("Validator class: " + validator.getClass().getName());

                // 通过反射检查 BasicValidator 内部状态
                printValidatorInternals(validator);

                EAttribute textFeature = XMLTypePackage.eINSTANCE.getXMLTypeDocumentRoot_Text();
                System.out.println("TEXT feature: " + textFeature);
                System.out.println("  TEXT namespace: " + ExtendedMetaData.INSTANCE.getNamespace(textFeature));
                System.out.println("  TEXT featureKind: " + ExtendedMetaData.INSTANCE.getFeatureKind(textFeature));
                System.out.println("isValid(TEXT): " + validator.isValid(textFeature));

                // 测试 element features
                for (EStructuralFeature sf : eClass.getEAllStructuralFeatures()) {
                    int kind = ExtendedMetaData.INSTANCE.getFeatureKind(sf);
                    if (kind == 4 || kind == 5) {
                        System.out.println("isValid(" + sf.getName() + " kind=" + kind + "): " + validator.isValid(sf));
                    }
                }

                break;
            }
        }

        // 检查 getAllElements
        List<EStructuralFeature> allElements = ExtendedMetaData.INSTANCE.getAllElements(eClass);
        System.out.println("getAllElements count: " + allElements.size());
        for (EStructuralFeature f : allElements) {
            System.out.println("  " + f.getName() + " kind=" + ExtendedMetaData.INSTANCE.getFeatureKind(f)
                + " ns=" + ExtendedMetaData.INSTANCE.getNamespace(f));
        }
    }

    static void printValidatorInternals(Validator validator) {
        try {
            Class<?> cls = validator.getClass();
            Field wildcardsField = cls.getDeclaredField("wildcards");
            wildcardsField.setAccessible(true);
            Object wildcards = wildcardsField.get(validator);
            System.out.println("  [internal] wildcards: " + wildcards);

            Field groupMembersField = cls.getDeclaredField("groupMembers");
            groupMembersField.setAccessible(true);
            Object groupMembers = groupMembersField.get(validator);
            System.out.println("  [internal] groupMembers: " + groupMembers);

            Field isElementField = cls.getDeclaredField("isElement");
            isElementField.setAccessible(true);
            Object isElement = isElementField.get(validator);
            System.out.println("  [internal] isElement: " + isElement);

            Field containingClassField = cls.getDeclaredField("containingClass");
            containingClassField.setAccessible(true);
            Object containingClass = containingClassField.get(validator);
            System.out.println("  [internal] containingClass: " + containingClass);

            Field eStructuralFeatureField = cls.getDeclaredField("eStructuralFeature");
            eStructuralFeatureField.setAccessible(true);
            Object eStructuralFeature = eStructuralFeatureField.get(validator);
            System.out.println("  [internal] eStructuralFeature: " + eStructuralFeature);
        } catch (Exception e) {
            System.out.println("  [internal] Failed to read: " + e.getMessage());
        }
    }
}
