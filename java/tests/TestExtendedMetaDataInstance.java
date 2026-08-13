import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EStructuralFeature;

public class TestExtendedMetaDataInstance {
    public static void main(String[] args) throws Exception {
        Class.forName("autosar40.util.Autosar40Package").getField("eINSTANCE").get(null);
        EPackage rootPkg = (EPackage) Class.forName("autosar40.util.Autosar40Package").getField("eINSTANCE").get(null);
        rootPkg.getEClassifiers().size();

        System.out.println("ExtendedMetaData.INSTANCE class: " + ExtendedMetaData.INSTANCE.getClass().getName());

        // 检查 LLongName 的 mixed feature
        EPackage pkg = EPackage.Registry.INSTANCE.getEPackage("http://autosar.org/schema/r4.0/autosar40/gs/gtc/d/tm/ldm");
        EClass lLongName = (EClass) pkg.getEClassifier("LLongName");
        EAttribute mixedFeature = ExtendedMetaData.INSTANCE.getMixedFeature(lLongName);
        System.out.println("getMixedFeature(LLongName): " + mixedFeature);
        System.out.println("  containingClass: " + (mixedFeature != null ? mixedFeature.getEContainingClass().getName() : "null"));

        // 检查 BasicValidator 的行为
        for (EStructuralFeature f : lLongName.getEAllStructuralFeatures()) {
            if (f.getName().equals("mixed")) {
                System.out.println("mixed featureKind: " + ExtendedMetaData.INSTANCE.getFeatureKind(f));
                System.out.println("mixed wildcards: " + ExtendedMetaData.INSTANCE.getWildcards(f));

                // 创建 validator
                org.eclipse.emf.ecore.util.FeatureMapUtil.Validator validator =
                    org.eclipse.emf.ecore.util.FeatureMapUtil.getValidator(lLongName, f);
                System.out.println("validator class: " + validator.getClass().getName());

                // 测试 TEXT
                org.eclipse.emf.ecore.xml.type.XMLTypePackage xtp = org.eclipse.emf.ecore.xml.type.XMLTypePackage.eINSTANCE;
                EAttribute textFeature = xtp.getXMLTypeDocumentRoot_Text();
                System.out.println("isValid(TEXT): " + validator.isValid(textFeature));
                break;
            }
        }
    }
}
