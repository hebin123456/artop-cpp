import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.ecore.proxymanagement.IProxyResolverService;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import java.util.Map;
import java.util.HashMap;

/**
 * 加载单个 arxml 文件，捕获详细错误
 * 用法: java TestLoadSingle <rootPackageClassName> <arxmlFile>
 */
public class TestLoadSingle {
    public static void main(String[] args) throws Exception {
        String rootPackageClassName = args[0];
        String arxmlFile = args[1];

        Class<?> rootPkgClass = Class.forName(rootPackageClassName);
        Object eINSTANCE = rootPkgClass.getField("eINSTANCE").get(null);
        EPackage rootPkg = (EPackage) eINSTANCE;
        rootPkg.getEClassifiers().size();
        System.out.println("Initialized: " + rootPackageClassName);

        // 注册 XML 标准命名空间包
        EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);

        // 注册 Autosar40ResourceFactoryImpl
        Class<?> factoryClass = Class.forName("autosar40.util.Autosar40ResourceFactoryImpl");
        Resource.Factory factory = (Resource.Factory) factoryClass.newInstance();
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("arxml", factory);

        // 注册 release descriptor
        Class<?> descClass = Class.forName("autosar40.util.Autosar40ReleaseDescriptor");
        Object desc = descClass.getField("INSTANCE").get(null);
        MetaModelDescriptorRegistry.INSTANCE.addDescriptor((IMetaModelDescriptor) desc);

        // 使用 ExtendedResourceSetImpl（和 demo 一样）
        ExtendedResourceSetImpl rs = new ExtendedResourceSetImpl() {
            @Override
            protected IProxyResolverService getProxyResolverService(IMetaModelDescriptor descriptor) {
                return null;
            }
        };

        // 加载文件
        URI uri = URI.createFileURI(arxmlFile);
        System.out.println("Loading: " + uri);

        try {
            Resource r = rs.getResource(uri, true);
            System.out.println("Loaded! Contents: " + r.getContents().size() + " errors: " + r.getErrors().size());
            for (EObject o : r.getContents()) {
                System.out.println("  " + o.eClass().getName());
            }
            if (!r.getErrors().isEmpty()) {
                System.out.println("First error: " + r.getErrors().get(0));
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getClass().getName() + ": " + e.getMessage());
            Throwable cause = e;
            while (cause != null) {
                if (cause != e) {
                    System.out.println("Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
                }
                StackTraceElement[] st = cause.getStackTrace();
                for (int i = 0; i < Math.min(st.length, 20); i++) {
                    System.out.println("  at " + st[i]);
                }
                cause = cause.getCause();
                if (cause != null) System.out.println();
            }
        }
    }
}
