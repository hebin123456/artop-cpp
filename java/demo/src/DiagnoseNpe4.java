package artop.demo;

import java.lang.reflect.Field;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;

import autosar40.util.Autosar40Package;
import autosar40.util.Autosar40ResourceFactoryImpl;
import gautosar.util.GAutosarPackage;

/**
 * 诊断 NPE v4：检查 autosarRelease 和 getNamespace(EPackage)
 */
public class DiagnoseNpe4 {

    public static void main(String[] args) throws Exception {
        Autosar40Package.eINSTANCE.getClass();
        GAutosarPackage.eINSTANCE.getClass();

        Autosar40ResourceFactoryImpl factory = new Autosar40ResourceFactoryImpl();

        // 反射获取 autosarRelease
        Field arField = factory.getClass().getSuperclass().getDeclaredField("autosarRelease");
        arField.setAccessible(true);
        Object autosarRelease = arField.get(factory);
        System.out.println("autosarRelease class: " + autosarRelease.getClass().getName());
        System.out.println("autosarRelease: " + autosarRelease);

        // 获取 getNamespace() 方法
        java.lang.reflect.Method getNsMethod = autosarRelease.getClass().getMethod("getNamespace");
        String releaseNs = (String) getNsMethod.invoke(autosarRelease);
        System.out.println("autosarRelease.getNamespace(): " + releaseNs);

        // 获取 getRootEPackage() 方法
        java.lang.reflect.Method getRootPkgMethod = autosarRelease.getClass().getMethod("getRootEPackage");
        EPackage rootPkg = (EPackage) getRootPkgMethod.invoke(autosarRelease);
        System.out.println("autosarRelease.getRootEPackage(): " + rootPkg + " nsURI=" + (rootPkg != null ? rootPkg.getNsURI() : "null"));

        // 获取 extendedMetaData
        Field emdField = factory.getClass().getSuperclass().getDeclaredField("extendedMetaData");
        emdField.setAccessible(true);
        ExtendedMetaData extendedMetaData = (ExtendedMetaData) emdField.get(factory);

        // 测试 getNamespace(EPackage) 直接调用
        System.out.println("\n=== getNamespace(EPackage) direct calls ===");
        EPackage atlsPkg = findPackageByNsURI(Autosar40Package.eINSTANCE, "http://autosar.org/schema/r4.0/autosar40/atls");
        if (atlsPkg != null) {
            String ns = extendedMetaData.getNamespace(atlsPkg);
            System.out.println("getNamespace(atlsPkg) = " + ns);
        }
        EPackage ginfrastructurePkg = EPackage.Registry.INSTANCE.getEPackage("http://artop.org/gautosar/gs/in");
        if (ginfrastructurePkg != null) {
            String ns = extendedMetaData.getNamespace(ginfrastructurePkg);
            System.out.println("getNamespace(ginfrastructurePkg) = " + ns);
        }

        // 加载 arxml 后再测试
        System.out.println("\n=== Loading arxml ===");
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

        // 获取 helper 的 extendedMetaData
        System.out.println("\n=== Checking helper's extendedMetaData ===");
        if (resource instanceof XMLResource) {
            XMLResource xmlRes = (XMLResource) resource;
            Object helperEmd = xmlRes.getDefaultLoadOptions().get(XMLResource.OPTION_EXTENDED_META_DATA);
            System.out.println("Load option EMD: " + (helperEmd == extendedMetaData ? "SAME as factory" : "DIFFERENT! " + helperEmd));
            Object saveEmd = xmlRes.getDefaultSaveOptions().get(XMLResource.OPTION_EXTENDED_META_DATA);
            System.out.println("Save option EMD: " + (saveEmd == extendedMetaData ? "SAME as factory" : "DIFFERENT! " + saveEmd));
        }

        // 再次测试 getNamespace(EPackage)
        System.out.println("\n=== After loading: getNamespace(EPackage) ===");
        atlsPkg = findPackageByNsURI(Autosar40Package.eINSTANCE, "http://autosar.org/schema/r4.0/autosar40/atls");
        if (atlsPkg == null) {
            atlsPkg = EPackage.Registry.INSTANCE.getEPackage("http://autosar.org/schema/r4.0/autosar40/atls");
        }
        if (atlsPkg != null) {
            String ns = extendedMetaData.getNamespace(atlsPkg);
            System.out.println("getNamespace(atlsPkg=" + atlsPkg.getNsURI() + ") = " + ns);
        }
        ginfrastructurePkg = EPackage.Registry.INSTANCE.getEPackage("http://artop.org/gautosar/gs/in");
        if (ginfrastructurePkg != null) {
            String ns = extendedMetaData.getNamespace(ginfrastructurePkg);
            System.out.println("getNamespace(ginfrastructurePkg=" + ginfrastructurePkg.getNsURI() + ") = " + ns);
        }

        // 测试 feature 的 getNamespace
        System.out.println("\n=== Feature getNamespace ===");
        if (!resource.getContents().isEmpty()) {
            org.eclipse.emf.ecore.EObject root = resource.getContents().get(0);
            org.eclipse.emf.ecore.EClass eClass = root.eClass();
            for (org.eclipse.emf.ecore.EAttribute attr : eClass.getEAllAttributes()) {
                String ns = extendedMetaData.getNamespace(attr);
                String name = extendedMetaData.getName(attr);
                System.out.println("  " + attr.getName() + ": name=" + name + " namespace=" + ns +
                    " kind=" + extendedMetaData.getFeatureKind(attr));
            }
        }
    }

    static EPackage findPackageByNsURI(EPackage root, String nsURI) {
        if (nsURI.equals(root.getNsURI())) return root;
        for (EPackage sub : root.getESubpackages()) {
            EPackage found = findPackageByNsURI(sub, nsURI);
            if (found != null) return found;
        }
        return null;
    }
}
