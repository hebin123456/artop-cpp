package artop.demo;

import java.io.File;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.ecore.proxymanagement.IProxyResolverService;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl;

import autosar40.util.Autosar40Package;
import autosar40.util.Autosar40ReleaseDescriptor;
import autosar40.util.Autosar40ResourceFactoryImpl;

/**
 * 验证 artop 官方 standalone 模式（不注册 XML 命名空间）是否也会 NPE
 *
 * 严格按照 Autosar4xStandaloneSetup.init() 的模式，不添加任何额外注册。
 * 如果 NPE，说明 artop 官方 standalone 也有这个问题（standalone 固有）。
 * 如果不 NPE，说明我之前的诊断有误，需要重新排查。
 */
public class VerifyStandaloneNpe {

    public static void main(String[] args) throws Exception {
        String inputFile = "/workspace/decompiler/autosar448/model/library/AISpecification_BaseTypes_Standard.arxml";
        if (args.length >= 1) inputFile = args[0];

        System.out.println("[verify] 严格按照 Autosar4xStandaloneSetup.init() 模式，不注册 XML 命名空间");
        System.out.println("[verify] Input: " + inputFile);

        // 严格按 Autosar4xStandaloneSetup.init() 模式
        Autosar40Package.eINSTANCE.getClass();
        Autosar40ResourceFactoryImpl resourceFactory = new Autosar40ResourceFactoryImpl();
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("arxml", resourceFactory);
        MetaModelDescriptorRegistry.INSTANCE.addDescriptor(Autosar40ReleaseDescriptor.INSTANCE);

        // 检查 XML 命名空间是否已注册（不应该注册）
        Object xmlNs = EPackage.Registry.INSTANCE.get("http://www.w3.org/XML/1998/namespace");
        System.out.println("[verify] Registry['http://www.w3.org/XML/1998/namespace'] = " + xmlNs);

        ExtendedResourceSetImpl resourceSet = new ExtendedResourceSetImpl() {
            @Override
            protected IProxyResolverService getProxyResolverService(IMetaModelDescriptor descriptor) {
                return null;
            }
        };

        try {
            URI inputUri = URI.createFileURI(new File(inputFile).getAbsolutePath());
            Resource inputResource = resourceSet.getResource(inputUri, true);
            System.out.println("[verify] Loaded: " + inputResource.getContents().size() + " root objects, " +
                inputResource.getErrors().size() + " errors");

            File outputFile = new File("/tmp/verify_standalone_output.arxml");
            URI outputUri = URI.createFileURI(outputFile.getAbsolutePath());
            Resource outputResource = resourceSet.createResource(outputUri);
            outputResource.getContents().addAll(inputResource.getContents());

            System.out.println("[verify] Saving (expecting NPE if standalone has the bug)...");
            outputResource.save(Collections.emptyMap());
            System.out.println("[verify] Saved successfully: " + outputFile.length() + " bytes");
            System.out.println("[verify] RESULT: NO NPE - artop standalone works without XML namespace registration");
        } catch (NullPointerException e) {
            System.out.println("[verify] RESULT: NPE occurred - artop standalone has the same bug");
            System.out.println("[verify] NPE: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("[verify] RESULT: Other exception: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
