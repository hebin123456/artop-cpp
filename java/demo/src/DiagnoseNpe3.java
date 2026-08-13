package artop.demo;

import java.lang.reflect.Field;
import java.util.Map;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;

import autosar40.util.Autosar40Package;
import autosar40.util.Autosar40ResourceFactoryImpl;
import gautosar.util.GAutosarPackage;

/**
 * 诊断 NPE v3：用反射测试 extendedMetaData.getPackage()
 */
public class DiagnoseNpe3 {

    static ExtendedMetaData extendedMetaData;

    public static void main(String[] args) throws Exception {
        // 初始化
        Autosar40Package.eINSTANCE.getClass();
        GAutosarPackage.eINSTANCE.getClass();

        // 创建 factory 并获取 extendedMetaData
        Autosar40ResourceFactoryImpl factory = new Autosar40ResourceFactoryImpl();
        Field emdField = factory.getClass().getSuperclass().getDeclaredField("extendedMetaData");
        emdField.setAccessible(true);
        extendedMetaData = (ExtendedMetaData) emdField.get(factory);
        System.out.println("extendedMetaData class: " + extendedMetaData.getClass().getName());

        // 测试 getPackage 和 getNamespace
        System.out.println("\n=== Before loading ===");
        testNamespaces();

        // 加载 arxml
        System.out.println("\n=== Loading arxml ===");
        java.io.File arxmlFile = new java.io.File("/workspace/decompiler/autosar448/model/library/AISpecification_DataConstr_Blueprint.arxml");
        org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI.createFileURI(arxmlFile.getAbsolutePath());
        Resource resource = factory.createResource(uri);

        Map<Object, Object> loadOptions = new java.util.HashMap<>();
        loadOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
        loadOptions.put(XMLResource.OPTION_USE_LEXICAL_HANDLER, Boolean.TRUE);

        try (java.io.FileInputStream fis = new java.io.FileInputStream(arxmlFile)) {
            resource.load(fis, loadOptions);
        }
        System.out.println("Loaded: " + resource.getContents().size() + " roots, " +
            resource.getErrors().size() + " errors");

        // 测试 getPackage 和 getNamespace
        System.out.println("\n=== After loading ===");
        testNamespaces();

        // 检查 root EObject 的所有 attribute 的 namespace
        System.out.println("\n=== Feature namespaces ===");
        if (!resource.getContents().isEmpty()) {
            org.eclipse.emf.ecore.EObject root = resource.getContents().get(0);
            org.eclipse.emf.ecore.EClass eClass = root.eClass();
            System.out.println("Root EClass: " + eClass.getName() + " pkg=" + eClass.getEPackage().getNsURI());
            for (org.eclipse.emf.ecore.EAttribute attr : eClass.getEAllAttributes()) {
                String ns = extendedMetaData.getNamespace(attr);
                EPackage pkg = ns != null ? extendedMetaData.getPackage(ns) : null;
                System.out.println("  Attr: " + attr.getName() +
                    " containingClass=" + attr.getEContainingClass().getName() +
                    " containingClass.pkg=" + (attr.getEContainingClass().getEPackage() != null ? attr.getEContainingClass().getEPackage().getNsURI() : "null") +
                    " namespace=" + ns +
                    " pkg=" + (pkg != null ? pkg.getNsURI() : "NULL!!!"));
            }
        }

        // 保存
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

    static void testNamespaces() {
        String[] namespaces = {
            "http://autosar.org/schema/r4.0",
            "http://autosar.org/schema/r4.0/autosar40",
            "http://autosar.org/schema/r4.0/autosar40/atls",
            "http://artop.org/gautosar",
            "http://artop.org/gautosar/gs/in",
            "http://www.eclipse.org/emf/2002/Ecore",
            "http://www.eclipse.org/emf/2003/XMLType"
        };
        for (String ns : namespaces) {
            EPackage pkg = extendedMetaData.getPackage(ns);
            System.out.println("  getPackage(\"" + ns + "\") => " + (pkg != null ? pkg.getNsURI() : "NULL"));
        }
    }
}
