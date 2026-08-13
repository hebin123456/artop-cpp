package artop.demo;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;

import autosar40.util.Autosar40Package;
import autosar40.util.Autosar40ResourceFactoryImpl;
import gautosar.util.GAutosarPackage;

/**
 * 诊断 NPE v5：遍历所有 EObject，检查 feature 的 containingClass 和 EPackage
 */
public class DiagnoseNpe5 {

    public static void main(String[] args) throws Exception {
        Autosar40Package.eINSTANCE.getClass();
        GAutosarPackage.eINSTANCE.getClass();

        Autosar40ResourceFactoryImpl factory = new Autosar40ResourceFactoryImpl();

        // 加载 arxml
        java.io.File arxmlFile = new java.io.File("/workspace/decompiler/autosar448/model/library/AISpecification_DataConstr_Blueprint.arxml");
        org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI.createFileURI(arxmlFile.getAbsolutePath());
        Resource resource = factory.createResource(uri);

        java.util.Map<Object, Object> loadOptions = new java.util.HashMap<>();
        loadOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
        loadOptions.put(XMLResource.OPTION_USE_LEXICAL_HANDLER, Boolean.TRUE);

        try (java.io.FileInputStream fis = new java.io.FileInputStream(arxmlFile)) {
            resource.load(fis, loadOptions);
        }
        System.out.println("Loaded: " + resource.getContents().size() + " roots, " +
            resource.getErrors().size() + " errors");

        // 遍历所有 EObject，检查 EClass 和 feature
        System.out.println("\n=== Checking all EObjects ===");
        int count = 0;
        int problems = 0;
        java.util.Set<EClass> checkedClasses = new java.util.HashSet<>();

        for (java.util.Iterator<EObject> it = resource.getAllContents(); it.hasNext(); ) {
            EObject obj = it.next();
            count++;
            EClass eClass = obj.eClass();

            // 检查 EClass 的 EPackage
            if (eClass.getEPackage() == null) {
                System.out.println("  PROBLEM: EClass " + eClass.getName() + " has null EPackage! obj=" + obj);
                problems++;
            }

            // 检查所有 feature
            for (EStructuralFeature feature : eClass.getEAllStructuralFeatures()) {
                EClass containingClass = feature.getEContainingClass();
                if (containingClass == null) {
                    System.out.println("  PROBLEM: Feature " + feature.getName() + " on " + eClass.getName() + " has null containingClass!");
                    problems++;
                    continue;
                }
                EPackage featurePkg = containingClass.getEPackage();
                if (featurePkg == null) {
                    System.out.println("  PROBLEM: Feature " + feature.getName() + " on " + eClass.getName() +
                        " containingClass=" + containingClass.getName() + " has null EPackage!");
                    problems++;
                }
            }

            // 检查所有 attribute 的 EDataType
            for (EAttribute attr : eClass.getEAllAttributes()) {
                EDataType dt = attr.getEAttributeType();
                if (dt != null && dt.getEPackage() == null) {
                    System.out.println("  PROBLEM: Attr " + attr.getName() + " on " + eClass.getName() +
                        " has EDataType " + dt.getName() + " with null EPackage!");
                    problems++;
                }
            }
        }

        System.out.println("Checked " + count + " EObjects, found " + problems + " problems");

        // 尝试保存
        System.out.println("\n=== Saving ===");
        try {
            java.io.File outFile = new java.io.File("/tmp/test_output.arxml");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile)) {
                resource.save(fos, null);
            }
            System.out.println("Saved: " + outFile.length() + " bytes");
        } catch (Exception e) {
            System.out.println("Save FAILED: " + e.getMessage());
        }
    }
}
