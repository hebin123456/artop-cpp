import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.sphinx.emf.ecore.proxymanagement.IProxyResolverService;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl;
import autosar40.util.Autosar40Package;
import autosar40.util.Autosar40ReleaseDescriptor;
import autosar40.util.Autosar40ResourceFactoryImpl;
import java.io.File;
import java.lang.reflect.*;
import java.util.*;

public class TestFindMixedProblem {
    public static void main(String[] args) throws Exception {
        Autosar40Package.eINSTANCE.getClass();
        EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);
        Autosar40ResourceFactoryImpl resourceFactory = new Autosar40ResourceFactoryImpl();
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("arxml", resourceFactory);
        MetaModelDescriptorRegistry.INSTANCE.addDescriptor(Autosar40ReleaseDescriptor.INSTANCE);

        // 遍历所有 EClass，找出 mixed content 类，并验证 eGet(mixedFeature) 返回类型
        ExtendedMetaData emd = ExtendedMetaData.INSTANCE;
        int mixedCount = 0;
        int problemCount = 0;
        List<String> problems = new ArrayList<>();

        for (Object pkgObj : EPackage.Registry.INSTANCE.values()) {
            if (!(pkgObj instanceof EPackage)) continue;
            EPackage pkg = (EPackage) pkgObj;
            String nsUri = pkg.getNsURI();
            if (nsUri == null) continue;
            if (!nsUri.startsWith("http://autosar.org/schema/r4.0/autosar40")) continue;

            for (EClassifier ec : pkg.getEClassifiers()) {
                if (!(ec instanceof EClass)) continue;
                EClass eClass = (EClass) ec;
                if (eClass.isAbstract()) continue;
                int contentKind = emd.getContentKind(eClass);
                if (contentKind != ExtendedMetaData.MIXED_CONTENT && contentKind != ExtendedMetaData.SIMPLE_CONTENT) continue;
                mixedCount++;
                EAttribute mixedFeature = emd.getMixedFeature(eClass);
                if (mixedFeature == null) {
                    problems.add("NO_MIXED_FEATURE: " + nsUri + "::" + eClass.getName());
                    continue;
                }
                // 检查 mixedFeature 的 featureKind
                int featureKind = emd.getFeatureKind(mixedFeature);
                // 检查 mixedFeature 所属的 EClass
                EClass featureOwner = (EClass) mixedFeature.getEContainingClass();
                // 检查 eClass.getFeatureID(mixedFeature)
                int featureID = eClass.getFeatureID(mixedFeature);
                // 检查 eClass.getEAllStructuralFeatures() 中 mixedFeature 的位置
                List<EStructuralFeature> allFeatures = eClass.getEAllStructuralFeatures();
                int allIdx = -1;
                for (int i = 0; i < allFeatures.size(); i++) {
                    if (allFeatures.get(i) == mixedFeature) { allIdx = i; break; }
                }
                // 检查 eClass.getEID(eAttribute)
                String ownerInfo = featureOwner != null ? featureOwner.getName() : "null";
                String info = String.format(
                    "EClass=%s (nsURI=%s), contentKind=%d, mixedFeature=%s, featureKind=%d, owner=%s, featureID=%d, allIdx=%d, allFeaturesSize=%d",
                    eClass.getName(), nsUri.substring(nsUri.lastIndexOf('/') + 1), contentKind,
                    mixedFeature.getName(), featureKind, ownerInfo, featureID, allIdx, allFeatures.size());
                if (featureKind != 5 /*ExtendedMetaData.ELEMENT_WILDCARD*/) {
                    problems.add("WRONG_FEATURE_KIND: " + info);
                    problemCount++;
                }
            }
        }
        System.out.println("Total mixed/simple content classes: " + mixedCount);
        System.out.println("Problems: " + problemCount);
        for (String p : problems) {
            System.out.println("  " + p);
        }

        // 现在尝试创建每个 mixed content 类的实例，并调用 eGet(mixedFeature)
        System.out.println("\n=== Testing eGet(mixedFeature) for each mixed content class ===");
        int createFail = 0;
        int getFail = 0;
        for (Object pkgObj : EPackage.Registry.INSTANCE.values()) {
            if (!(pkgObj instanceof EPackage)) continue;
            EPackage pkg = (EPackage) pkgObj;
            String nsUri = pkg.getNsURI();
            if (nsUri == null) continue;
            if (!nsUri.startsWith("http://autosar.org/schema/r4.0/autosar40")) continue;

            for (EClassifier ec : pkg.getEClassifiers()) {
                if (!(ec instanceof EClass)) continue;
                EClass eClass = (EClass) ec;
                if (eClass.isAbstract()) continue;
                int contentKind = emd.getContentKind(eClass);
                if (contentKind != ExtendedMetaData.MIXED_CONTENT && contentKind != ExtendedMetaData.SIMPLE_CONTENT) continue;
                EAttribute mixedFeature = emd.getMixedFeature(eClass);
                if (mixedFeature == null) continue;

                EObject obj;
                try {
                    obj = eClass.getEPackage().getEFactoryInstance().create(eClass);
                } catch (Throwable t) {
                    System.out.println("CREATE_FAIL: " + eClass.getName() + " - " + t.getMessage());
                    createFail++;
                    continue;
                }
                try {
                    Object result = obj.eGet(mixedFeature);
                    if (!(result instanceof org.eclipse.emf.ecore.util.FeatureMap)) {
                        System.out.println("GET_WRONG_TYPE: " + eClass.getName() +
                            " mixedFeature=" + mixedFeature.getName() +
                            " resultType=" + result.getClass().getName() +
                            " featureID=" + eClass.getFeatureID(mixedFeature) +
                            " owner=" + (mixedFeature.getEContainingClass() != null ? mixedFeature.getEContainingClass().getName() : "null"));
                        getFail++;
                    }
                } catch (Throwable t) {
                    System.out.println("GET_EXCEPTION: " + eClass.getName() + " - " + t.getClass().getName() + ": " + t.getMessage());
                    getFail++;
                }
            }
        }
        System.out.println("\nCreate failures: " + createFail);
        System.out.println("Get failures: " + getFail);
    }
}
