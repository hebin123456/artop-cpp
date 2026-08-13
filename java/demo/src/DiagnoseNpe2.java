package artop.demo;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;

import autosar40.util.Autosar40Package;
import autosar40.util.Autosar40ResourceFactoryImpl;
import gautosar.util.GAutosarPackage;

/**
 * 诊断 NPE v2：注册根包后测试保存
 */
public class DiagnoseNpe2 {

    public static void main(String[] args) throws Exception {
        // 1. 初始化并注册根包
        System.out.println("=== Initializing packages ===");
        Autosar40Package.eINSTANCE.getClass();
        GAutosarPackage.eINSTANCE.getClass();

        // 显式注册根包到全局 Registry
        EPackage.Registry.INSTANCE.put(Autosar40Package.eINSTANCE.getNsURI(), Autosar40Package.eINSTANCE);
        EPackage.Registry.INSTANCE.put(GAutosarPackage.eINSTANCE.getNsURI(), GAutosarPackage.eINSTANCE);

        // 递归注册所有子包
        registerAllSubpackages(Autosar40Package.eINSTANCE);
        registerAllSubpackages(GAutosarPackage.eINSTANCE);

        System.out.println("Autosar40 nsURI: " + Autosar40Package.eINSTANCE.getNsURI());
        System.out.println("GAutosar nsURI: " + GAutosarPackage.eINSTANCE.getNsURI());
        checkRegistry();

        // 2. 加载 arxml
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

        // 3. 保存
        System.out.println("\n=== Saving ===");
        try {
            java.io.File outFile = new java.io.File("/tmp/test_output.arxml");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile)) {
                resource.save(fos, null);
            }
            System.out.println("Saved: " + outFile.length() + " bytes");
            System.out.println("SUCCESS!");
        } catch (Exception e) {
            System.out.println("Save FAILED: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("  " + key + " => " + (val == null ? "NULL" : val.getClass().getSimpleName()));
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
