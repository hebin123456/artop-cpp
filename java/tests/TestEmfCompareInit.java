import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.compare.Comparison;

/**
 * 测试 EMF Compare 能否在非 OSGi 环境下初始化和运行
 */
public class TestEmfCompareInit {
    public static void main(String[] args) {
        try {
            System.out.println("Creating EMFCompare builder...");
            EMFCompare comparator = EMFCompare.builder().build();
            System.out.println("OK: " + comparator.getClass().getName());

            // 创建两个简单的 ResourceSet 进行对比测试
            ResourceSet rs1 = new ResourceSetImpl();
            ResourceSet rs2 = new ResourceSetImpl();
            rs1.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
            rs2.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());

            System.out.println("Creating comparison scope...");
            IComparisonScope scope = new DefaultComparisonScope(rs1, rs2, null);
            System.out.println("OK: " + scope.getClass().getName());

            System.out.println("Running compare...");
            Comparison comparison = comparator.compare(scope, new BasicMonitor());
            System.out.println("OK: comparison result, differences=" + comparison.getDifferences().size());

            System.out.println("\nAll EMF Compare initialization OK");
        } catch (Throwable e) {
            System.out.println("FAILED: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
