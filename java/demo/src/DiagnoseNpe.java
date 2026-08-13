package artop.demo;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;

import autosar40.util.Autosar40Package;
import autosar40.util.Autosar40ResourceFactoryImpl;

/**
 * 诊断 NPE：检查 gautosar 包是否在 demo 初始化后注册
 */
public class DiagnoseNpe {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Before Autosar40Package init ===");
        checkRegistry();

        System.out.println("\n=== After Autosar40Package.eINSTANCE ===");
        Autosar40Package.eINSTANCE.getClass();
        checkRegistry();

        System.out.println("\n=== After registerAllSubpackages ===");
        registerAllSubpackages(Autosar40Package.eINSTANCE);
        checkRegistry();

        // 尝试加载一个 arxml 并保存
        System.out.println("\n=== Loading arxml ===");
        Autosar40ResourceFactoryImpl factory = new Autosar40ResourceFactoryImpl();
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

        System.out.println("\n=== After loading arxml ===");
        checkRegistry();

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
            // 打印 gautosar 包的状态
            System.out.println("\n=== Registry at failure ===");
            checkRegistry();
            throw e;
        }
    }

    static void checkRegistry() {
        String[] keys = {
            "http://artop.org/gautosar",
            "http://artop.org/gautosar/gs/in",
            "http://autosar.org/schema/r4.0/autosar40",
            "http://autosar.org/schema/r4.0/autosar40/atls"
        };
        for (String key : keys) {
            Object val = EPackage.Registry.INSTANCE.get(key);
            System.out.println("  " + key + " => " + (val == null ? "NULL" : val.getClass().getName()));
        }
    }

    static void registerAllSubpackages(EPackage pkg) {
        if (pkg.getNsURI() != null) {
            EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
        }
        for (EPackage sub : pkg.getESubpackages()) {
            registerAllSubpackages(sub);
        }
    }
}
