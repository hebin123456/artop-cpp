import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.ecore.proxymanagement.IProxyResolverService;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 改进版 EMF Compare 测试：使用真正的 containment reference 和不同的文件
 * 用法: java TestEmfCompare2 <rootPackageClassName> <inputDir>
 */
public class TestEmfCompare2 {
    public static void main(String[] args) throws Exception {
        String rootPackageClassName = args[0];
        String inputDir = args[1];

        // 初始化（反射，兼容两种 jar）
        Class<?> rootPkgClass = Class.forName(rootPackageClassName);
        rootPkgClass.getField("eINSTANCE").get(null);
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

        // 选择测试文件（按大小排序，选前几个小的）
        File[] files = new File(inputDir).listFiles((d, name) -> name.endsWith(".arxml"));
        Arrays.sort(files, (a, b) -> Long.compare(a.length(), b.length()));
        System.out.println("选择最小的文件: " + files[0].getName() + " (" + files[0].length() + " bytes)");

        // 加载模型
        Resource resA = rs.getResource(URI.createFileURI(files[0].getAbsolutePath()), true);
        if (resA.getContents().isEmpty()) {
            System.out.println("模型 A 为空");
            return;
        }
        EObject rootA = resA.getContents().get(0);
        System.out.println("根对象: " + rootA.eClass().getName());

        EMFCompare comparator = EMFCompare.builder().build();

        // === 场景 1: 相同模型对比 ===
        System.out.println("\n=== 场景 1: 相同模型对比 ===");
        EObject copyA = EcoreUtil.copy(rootA);
        IComparisonScope scope1 = new DefaultComparisonScope(rootA, copyA, null);
        Comparison comp1 = comparator.compare(scope1, new BasicMonitor());
        System.out.println("差异数: " + comp1.getDifferences().size() + " (预期 0)");

        // === 场景 2: 属性修改对比 ===
        System.out.println("\n=== 场景 2: 属性修改对比 ===");
        EObject copyB = EcoreUtil.copy(rootA);
        // 找一个 String 属性来修改
        EAttribute targetAttr = null;
        Object oldVal = null;
        for (EAttribute attr : copyB.eClass().getEAllAttributes()) {
            if (attr.getUpperBound() == 1 && "java.lang.String".equals(attr.getEAttributeType().getInstanceClassName())) {
                oldVal = copyB.eGet(attr);
                copyB.eSet(attr, "_test_modified_value_");
                targetAttr = attr;
                break;
            }
        }
        if (targetAttr != null) {
            System.out.println("修改属性: " + copyB.eClass().getName() + "." + targetAttr.getName() + " 旧值=" + oldVal + " 新值=_test_modified_value_");
            IComparisonScope scope2 = new DefaultComparisonScope(rootA, copyB, null);
            Comparison comp2 = comparator.compare(scope2, new BasicMonitor());
            System.out.println("差异数: " + comp2.getDifferences().size());
            for (Diff d : comp2.getDifferences()) {
                System.out.println("  Diff: kind=" + d.getKind() + ", " + d.toString().substring(0, Math.min(120, d.toString().length())));
            }
        } else {
            System.out.println("未找到可修改的 String 属性");
        }

        // === 场景 3: 添加 containment 子元素 ===
        System.out.println("\n=== 场景 3: 添加 containment 子元素 ===");
        EObject copyC = EcoreUtil.copy(rootA);
        // 找一个多值 containment reference
        EReference targetRef = null;
        for (EReference ref : copyC.eClass().getEAllReferences()) {
            if (ref.isContainment() && ref.getUpperBound() != 1) {
                Object val = copyC.eGet(ref);
                if (val instanceof EList) {
                    targetRef = ref;
                    break;
                }
            }
        }
        if (targetRef != null) {
            EClass childType = targetRef.getEReferenceType();
            EObject newChild = EcoreUtil.create(childType);
            @SuppressWarnings("unchecked")
            EList<EObject> list = (EList<EObject>) copyC.eGet(targetRef);
            list.add(newChild);
            System.out.println("添加元素: " + copyC.eClass().getName() + "." + targetRef.getName() + " += " + childType.getName());
            IComparisonScope scope3 = new DefaultComparisonScope(rootA, copyC, null);
            Comparison comp3 = comparator.compare(scope3, new BasicMonitor());
            System.out.println("差异数: " + comp3.getDifferences().size());
            for (Diff d : comp3.getDifferences()) {
                System.out.println("  Diff: kind=" + d.getKind() + ", " + d.toString().substring(0, Math.min(120, d.toString().length())));
            }
        } else {
            System.out.println("未找到多值 containment reference，尝试递归查找...");
            // 递归查找子对象中的 containment reference
            boolean found = false;
            for (EObject child : copyC.eContents()) {
                for (EReference ref : child.eClass().getEAllReferences()) {
                    if (ref.isContainment() && ref.getUpperBound() != 1) {
                        Object val = child.eGet(ref);
                        if (val instanceof EList) {
                            EClass childType = ref.getEReferenceType();
                            EObject newChild = EcoreUtil.create(childType);
                            @SuppressWarnings("unchecked")
                            EList<EObject> list = (EList<EObject>) val;
                            list.add(newChild);
                            System.out.println("添加元素: " + child.eClass().getName() + "." + ref.getName() + " += " + childType.getName());
                            IComparisonScope scope3 = new DefaultComparisonScope(rootA, copyC, null);
                            Comparison comp3 = comparator.compare(scope3, new BasicMonitor());
                            System.out.println("差异数: " + comp3.getDifferences().size());
                            for (Diff d : comp3.getDifferences()) {
                                System.out.println("  Diff: kind=" + d.getKind() + ", " + d.toString().substring(0, Math.min(120, d.toString().length())));
                            }
                            found = true;
                            break;
                        }
                    }
                }
                if (found) break;
            }
            if (!found) System.out.println("未找到任何多值 containment reference");
        }

        // === 场景 4: 移除 containment 子元素 ===
        System.out.println("\n=== 场景 4: 移除 containment 子元素 ===");
        EObject copyD = EcoreUtil.copy(rootA);
        // 找一个有多值的 containment reference 并移除一个元素
        boolean removed = false;
        for (EReference ref : copyD.eClass().getEAllReferences()) {
            if (ref.isContainment() && ref.getUpperBound() != 1) {
                Object val = copyD.eGet(ref);
                if (val instanceof EList && !((EList<?>) val).isEmpty()) {
                    EList<EObject> list = (EList<EObject>) val;
                    EObject removedChild = list.get(0);
                    list.remove(0);
                    System.out.println("移除元素: " + copyD.eClass().getName() + "." + ref.getName() + " -= " + removedChild.eClass().getName());
                    IComparisonScope scope4 = new DefaultComparisonScope(rootA, copyD, null);
                    Comparison comp4 = comparator.compare(scope4, new BasicMonitor());
                    System.out.println("差异数: " + comp4.getDifferences().size());
                    for (Diff d : comp4.getDifferences()) {
                        System.out.println("  Diff: kind=" + d.getKind() + ", " + d.toString().substring(0, Math.min(120, d.toString().length())));
                    }
                    removed = true;
                    break;
                }
            }
        }
        if (!removed) System.out.println("未找到可移除的 containment 子元素");

        // === 场景 5: 对比两个不同的文件 ===
        System.out.println("\n=== 场景 5: 对比两个不同的文件 ===");
        // 找两个内容不同的文件
        File file1 = null, file2 = null;
        for (int i = 0; i < files.length && file1 == null; i++) {
            for (int j = i + 1; j < files.length && file1 == null; j++) {
                if (files[i].length() != files[j].length()) {
                    file1 = files[i];
                    file2 = files[j];
                }
            }
        }
        if (file1 != null && file2 != null) {
            System.out.println("文件1: " + file1.getName() + " (" + file1.length() + " bytes)");
            System.out.println("文件2: " + file2.getName() + " (" + file2.length() + " bytes)");
            Resource resFile1 = rs.getResource(URI.createFileURI(file1.getAbsolutePath()), true);
            Resource resFile2 = rs.getResource(URI.createFileURI(file2.getAbsolutePath()), true);
            if (!resFile1.getContents().isEmpty() && !resFile2.getContents().isEmpty()) {
                IComparisonScope scope5 = new DefaultComparisonScope(resFile1, resFile2, null);
                Comparison comp5 = comparator.compare(scope5, new BasicMonitor());
                System.out.println("差异数: " + comp5.getDifferences().size());
                int count = 0;
                for (Diff d : comp5.getDifferences()) {
                    if (count++ < 5) {
                        System.out.println("  Diff: kind=" + d.getKind() + ", " + d.toString().substring(0, Math.min(120, d.toString().length())));
                    }
                }
                if (comp5.getDifferences().size() > 5) {
                    System.out.println("  ... 共 " + comp5.getDifferences().size() + " 个差异");
                }
            }
        } else {
            System.out.println("所有文件大小相同，无法测试不同文件对比");
        }

        System.out.println("\n=== 测试完成 ===");
    }
}
