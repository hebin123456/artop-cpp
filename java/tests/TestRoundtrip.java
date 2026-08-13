import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.ecore.proxymanagement.IProxyResolverService;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;

/**
 * 模拟 demo 的完整流程：加载→保存→重新加载
 * 用法: java TestRoundtrip <rootPackageClassName> <inputDir> <outputDir>
 */
public class TestRoundtrip {
    static int success = 0;
    static int failed = 0;
    static int identical = 0;

    public static void main(String[] args) throws Exception {
        String rootPackageClassName = args[0];
        String inputDir = args[1];
        String outputDir = args[2];

        Class<?> rootPkgClass = Class.forName(rootPackageClassName);
        Object eINSTANCE = rootPkgClass.getField("eINSTANCE").get(null);
        EPackage rootPkg = (EPackage) eINSTANCE;
        // 不调用 getEClassifiers().size()，模拟 demo 的行为
        System.out.println("Initialized: " + rootPackageClassName);

        EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);

        Class<?> factoryClass = Class.forName("autosar40.util.Autosar40ResourceFactoryImpl");
        Resource.Factory factory = (Resource.Factory) factoryClass.newInstance();
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("arxml", factory);

        Class<?> descClass = Class.forName("autosar40.util.Autosar40ReleaseDescriptor");
        Object desc = descClass.getField("INSTANCE").get(null);
        MetaModelDescriptorRegistry.INSTANCE.addDescriptor((IMetaModelDescriptor) desc);

        ExtendedResourceSetImpl rs = new ExtendedResourceSetImpl() {
            @Override
            protected IProxyResolverService getProxyResolverService(IMetaModelDescriptor descriptor) {
                return null;
            }
        };

        File[] files = new File(inputDir).listFiles((d, name) -> name.endsWith(".arxml"));
        Arrays.sort(files);
        new File(outputDir).mkdirs();
        System.out.println("Found " + files.length + " files");

        for (File f : files) {
            System.out.print(f.getName() + ": ");
            try {
                URI inputUri = URI.createFileURI(f.getAbsolutePath());
                Resource inputResource = rs.getResource(inputUri, true);

                File outputFile = new File(outputDir, f.getName());
                URI outputUri = URI.createFileURI(outputFile.getAbsolutePath());
                Resource outputResource = rs.createResource(outputUri);
                outputResource.getContents().addAll(inputResource.getContents());
                outputResource.save(Collections.emptyMap());

                // 重新加载
                Resource reloadedResource = rs.getResource(outputUri, true);

                // 字节比较
                boolean byteIdentical = compareFiles(f, outputFile);

                System.out.println("OK (save=" + outputFile.length() + " bytes, identical=" + byteIdentical
                    + ", reloadErrors=" + reloadedResource.getErrors().size() + ")");
                success++;
                if (byteIdentical) identical++;

                // 卸载
                inputResource.unload();
                outputResource.unload();
                reloadedResource.unload();
            } catch (Exception e) {
                System.out.println("FAILED: " + e.getMessage());
                failed++;
            }
        }

        System.out.println("\nSuccess: " + success + "/" + (success + failed));
        System.out.println("Failed:  " + failed + "/" + (success + failed));
        System.out.println("Byte identical: " + identical + "/" + (success + failed));
    }

    static boolean compareFiles(File f1, File f2) throws Exception {
        if (f1.length() != f2.length()) return false;
        byte[] b1 = java.nio.file.Files.readAllBytes(f1.toPath());
        byte[] b2 = java.nio.file.Files.readAllBytes(f2.toPath());
        return java.util.Arrays.equals(b1, b2);
    }
}
