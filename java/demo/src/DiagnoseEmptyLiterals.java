package artop.demo;

import java.io.File;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
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

/**
 * 检查空 LITERALS 的 eIsSet 状态
 */
public class DiagnoseEmptyLiterals {

    public static void main(String[] args) throws Exception {
        File arxmlFile = new File(args[0]);

        Autosar40Package.eINSTANCE.getClass();
        EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("arxml", new Autosar40ResourceFactoryImpl());
        MetaModelDescriptorRegistry.INSTANCE.addDescriptor(Autosar40ReleaseDescriptor.INSTANCE);

        ExtendedResourceSetImpl resourceSet = new ExtendedResourceSetImpl() {
            @Override
            protected IProxyResolverService getProxyResolverService(IMetaModelDescriptor descriptor) {
                return null;
            }
        };

        URI inputUri = URI.createFileURI(arxmlFile.getAbsolutePath());
        Resource inputResource = resourceSet.getResource(inputUri, true);
        System.out.println("[diag] Loaded: " + inputResource.getErrors().size() + " errors");

        // 遍历所有 EcucEnumerationParamDef，检查 LITERALS feature 的 eIsSet
        int total = 0;
        int emptyAndSet = 0;
        int emptyAndUnset = 0;
        int nonEmpty = 0;
        for (EObject root : inputResource.getContents()) {
            java.util.List<EObject> all = new java.util.ArrayList<>();
            collectAll(root, all);
            for (EObject e : all) {
                if ("EcucEnumerationParamDef".equals(e.eClass().getName())) {
                    total++;
                    EStructuralFeature literalsFeat = null;
                    for (EStructuralFeature f : e.eClass().getEAllStructuralFeatures()) {
                        if ("literals".equalsIgnoreCase(f.getName())) {
                            literalsFeat = f;
                            break;
                        }
                    }
                    if (literalsFeat == null) {
                        System.out.println("[diag] WARNING: no literals feature on " + e.eClass().getName());
                        continue;
                    }
                    boolean isSet = e.eIsSet(literalsFeat);
                    Object val = e.eGet(literalsFeat);
                    int size = (val instanceof java.util.List) ? ((java.util.List<?>) val).size() : -1;
                    if (size == 0) {
                        if (isSet) {
                            emptyAndSet++;
                            if (emptyAndSet <= 3) {
                                System.out.println("[diag] EMPTY & SET: " + e.eClass().getName());
                            }
                        } else {
                            emptyAndUnset++;
                            if (emptyAndUnset <= 3) {
                                System.out.println("[diag] EMPTY & UNSET: " + e.eClass().getName());
                            }
                        }
                    } else {
                        nonEmpty++;
                    }
                }
            }
        }
        System.out.println("[diag] Total EcucEnumerationParamDef: " + total);
        System.out.println("[diag]   non-empty LITERALS: " + nonEmpty);
        System.out.println("[diag]   empty & SET (should output <LITERALS/>): " + emptyAndSet);
        System.out.println("[diag]   empty & UNSET (EMF skips): " + emptyAndUnset);
    }

    private static void collectAll(EObject e, java.util.List<EObject> out) {
        out.add(e);
        for (EObject c : e.eContents()) {
            collectAll(c, out);
        }
    }
}
