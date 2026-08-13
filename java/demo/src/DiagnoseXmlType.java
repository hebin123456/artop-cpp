package artop.demo;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import autosar40.util.Autosar40Package;

/**
 * 检查 XMLTypePackage 的注册情况
 */
public class DiagnoseXmlType {

    public static void main(String[] args) throws Exception {
        System.out.println("[diag] Initializing Autosar40Package...");
        Autosar40Package.eINSTANCE.getClass();

        System.out.println("[diag] Initializing XMLTypePackage...");
        XMLTypePackage xmlTypePkg = XMLTypePackage.eINSTANCE;
        System.out.println("[diag]   XMLTypePackage.nsURI: " + xmlTypePkg.getNsURI());
        System.out.println("[diag]   XMLTypePackage.nsPrefix: " + xmlTypePkg.getNsPrefix());

        // 检查 registry 中是否有 http://www.w3.org/XML/1998/namespace
        System.out.println("\n[diag] Checking EPackage.Registry.INSTANCE:");
        String[] keysToCheck = {
            "http://www.w3.org/XML/1998/namespace",
            "http://www.eclipse.org/emf/2003/XMLType",
            XMLTypePackage.eNS_URI
        };
        for (String key : keysToCheck) {
            Object pkg = EPackage.Registry.INSTANCE.get(key);
            System.out.println("[diag]   Registry['" + key + "'] = " + pkg);
        }

        // 尝试手动注册
        System.out.println("\n[diag] Manually registering XMLTypePackage to http://www.w3.org/XML/1998/namespace...");
        EPackage.Registry.INSTANCE.put("http://www.w3.org/XML/1998/namespace", xmlTypePkg);

        // 再次检查
        Object pkg = EPackage.Registry.INSTANCE.get("http://www.w3.org/XML/1998/namespace");
        System.out.println("[diag]   After register: Registry['http://www.w3.org/XML/1998/namespace'] = " + pkg);

        // 检查 xmlSpace feature
        System.out.println("\n[diag] Checking xmlSpace feature:");
        org.eclipse.emf.ecore.EClass whitespaceControlled = null;
        // 遍历所有 EPackage 查找 WhitespaceControlled
        for (Object value : EPackage.Registry.INSTANCE.values()) {
            if (value instanceof EPackage) {
                EPackage ep = (EPackage) value;
                for (Object classifier : ep.getEClassifiers()) {
                    if (classifier instanceof org.eclipse.emf.ecore.EClass) {
                        org.eclipse.emf.ecore.EClass ec = (org.eclipse.emf.ecore.EClass) classifier;
                        if ("WhitespaceControlled".equals(ec.getName())) {
                            whitespaceControlled = ec;
                            System.out.println("[diag]   Found WhitespaceControlled in: " + ep.getName() + " (" + ep.getNsURI() + ")");
                            for (org.eclipse.emf.ecore.EStructuralFeature f : ec.getEAllStructuralFeatures()) {
                                System.out.println("[diag]     Feature: " + f.getName());
                                if ("xmlSpace".equals(f.getName())) {
                                    org.eclipse.emf.ecore.util.ExtendedMetaData emd = org.eclipse.emf.ecore.util.ExtendedMetaData.INSTANCE;
                                    System.out.println("[diag]       emd.namespace: " + emd.getNamespace(f));
                                    System.out.println("[diag]       emd.name: " + emd.getName(f));
                                    System.out.println("[diag]       emd.featureKind: " + emd.getFeatureKind(f));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
