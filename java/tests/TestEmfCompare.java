import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.AttributeChange;
import org.eclipse.emf.compare.ReferenceChange;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.ecore.proxymanagement.IProxyResolverService;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 测试 EMF Compare 在 AUTOSAR 模型上的差异检测能力
 * 用法: java TestEmfCompare <rootPackageClassName> <inputDir> <outputDir>
 *   rootPackageClassName: 例如 autosar40.util.Autosar40Package
 *   inputDir: 包含 .arxml 文件的目录
 *   outputDir: 保存修改后模型的目录
 *
 * 程序会运行 5 个测试场景：
 *   场景 1：比较相同的模型（应找到 0 个差异）
 *   场景 2：修改属性后比较
 *   场景 3：添加元素后比较
 *   场景 4：移除元素后比较
 *   场景 5：比较两个不同的 arxml 文件
 */
public class TestEmfCompare {
    // ===== 全局统计字段 =====
    static int totalFiles = 0;
    static long sumScenario1 = 0;
    static long sumScenario2 = 0;
    static long sumScenario3 = 0;
    static long sumScenario4 = 0;
    static int countScenario1 = 0;
    static int countScenario2 = 0;
    static int countScenario3 = 0;
    static int countScenario4 = 0;
    static int scenario5Differences = -1;

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("用法: java TestEmfCompare <rootPackageClassName> <inputDir> <outputDir>");
            System.out.println("  rootPackageClassName: 例如 autosar40.util.Autosar40Package");
            System.out.println("  inputDir: 包含 .arxml 文件的目录");
            System.out.println("  outputDir: 保存修改后模型的目录");
            return;
        }

        String rootPackageClassName = args[0];
        String inputDir = args[1];
        String outputDir = args[2];

        System.out.println("Root package class: " + rootPackageClassName);
        System.out.println("Input directory: " + inputDir);
        System.out.println("Output directory: " + outputDir);

        // 初始化（复刻 TestExactDemoInit 的模式，使用反射以兼容官方和 dummy jar）
        ExtendedResourceSetImpl rs = initialize(rootPackageClassName);
        new File(outputDir).mkdirs();

        // 获取 arxml 文件列表，按文件大小排序（最小的优先）
        File[] files = new File(inputDir).listFiles((d, name) -> name.endsWith(".arxml"));
        if (files == null || files.length == 0) {
            System.out.println("未找到 arxml 文件");
            printSummary();
            return;
        }
        Arrays.sort(files, (a, b) -> {
            int cmp = Long.compare(a.length(), b.length());
            if (cmp != 0) return cmp;
            return a.getName().compareTo(b.getName());
        });

        // 选择第一个能成功加载的小文件（优先 10KB 以下）
        Resource firstResource = null;
        File firstFile = null;
        for (File f : files) {
            try {
                URI uri = URI.createFileURI(f.getAbsolutePath());
                Resource resource = rs.getResource(uri, true);
                if (!resource.getContents().isEmpty()) {
                    firstResource = resource;
                    firstFile = f;
                    System.out.println("选择测试文件: " + f.getName() + " (" + f.length() + " bytes)");
                    totalFiles++;
                    break;
                } else {
                    resource.unload();
                    rs.getResources().remove(resource);
                }
            } catch (Exception e) {
                // 加载失败，尝试下一个文件
            }
        }

        if (firstResource == null) {
            System.out.println("没有可加载的 arxml 文件");
            printSummary();
            return;
        }

        // 创建 EMF Compare 比较器
        EMFCompare comparator = EMFCompare.builder().build();

        // 运行场景 1-4
        System.out.println("\n=== Scenario 1: Compare identical models ===");
        runScenario1(comparator, firstResource, outputDir, rs);

        System.out.println("\n=== Scenario 2: Compare after attribute modification ===");
        runScenario2(comparator, firstResource, outputDir, rs);

        System.out.println("\n=== Scenario 3: Compare after adding an element ===");
        runScenario3(comparator, firstResource, outputDir, rs);

        System.out.println("\n=== Scenario 4: Compare after removing an element ===");
        runScenario4(comparator, firstResource, outputDir, rs);

        // 场景 5：比较两个不同的 arxml 文件
        System.out.println("\n=== Scenario 5: Compare two different arxml files ===");
        runScenario5(comparator, files, firstFile, rs);

        // 打印总结
        System.out.println();
        printSummary();
    }

    // ===== 初始化（复刻 TestExactDemoInit 的模式，使用反射以兼容官方和 dummy jar）=====
    static ExtendedResourceSetImpl initialize(String rootPackageClassName) throws Exception {
        // 通过反射加载根包类并触发初始化
        Class<?> rootPkgClass = Class.forName(rootPackageClassName);
        rootPkgClass.getField("eINSTANCE").get(null);

        // 注册 XML 类型包（加载 arxml 必需）
        EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);

        // 通过反射创建资源工厂（兼容 autosar40.util.Autosar40ResourceFactoryImpl）
        Class<?> factoryClass = Class.forName("autosar40.util.Autosar40ResourceFactoryImpl");
        Resource.Factory factory = (Resource.Factory) factoryClass.newInstance();
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("arxml", factory);

        // 通过反射注册元模型描述符
        Class<?> descClass = Class.forName("autosar40.util.Autosar40ReleaseDescriptor");
        Object desc = descClass.getField("INSTANCE").get(null);
        MetaModelDescriptorRegistry.INSTANCE.addDescriptor((IMetaModelDescriptor) desc);

        System.out.println("初始化完成");

        // 创建 ExtendedResourceSetImpl，代理解析服务返回 null（与 TestExactDemoInit 一致）
        return new ExtendedResourceSetImpl() {
            @Override
            protected IProxyResolverService getProxyResolverService(IMetaModelDescriptor descriptor) {
                return null;
            }
        };
    }

    // ===== 场景 1：比较相同的模型（应找到 0 个差异）=====
    static void runScenario1(EMFCompare comparator, Resource resourceA,
            String outputDir, ExtendedResourceSetImpl rs) {
        Resource resourceB = null;
        try {
            // 创建 Resource B，使用 EcoreUtil.copyAll 深拷贝 A 的所有 EObject
            resourceB = rs.createResource(URI.createFileURI(
                    new File(outputDir, "scenario1_B.arxml").getAbsolutePath()));
            Collection<EObject> copies = EcoreUtil.copyAll(resourceA.getContents());
            resourceB.getContents().addAll(copies);

            // 比较两个资源
            IComparisonScope scope = new DefaultComparisonScope(resourceA, resourceB, null);
            Comparison comparison = comparator.compare(scope, new BasicMonitor());

            int diffs = comparison.getDifferences().size();
            System.out.println("Scenario 1 (identical): differences=" + diffs + " (expected 0)");

            sumScenario1 += diffs;
            countScenario1++;
        } catch (Exception e) {
            System.out.println("Scenario 1 失败: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            // 清理 Resource B
            if (resourceB != null) {
                resourceB.unload();
                rs.getResources().remove(resourceB);
            }
        }
    }

    // ===== 场景 2：修改属性后比较 =====
    static void runScenario2(EMFCompare comparator, Resource resourceA,
            String outputDir, ExtendedResourceSetImpl rs) {
        Resource resourceB = null;
        try {
            // 创建 Resource B，深拷贝 A 的所有 EObject
            resourceB = rs.createResource(URI.createFileURI(
                    new File(outputDir, "scenario2_B.arxml").getAbsolutePath()));
            Collection<EObject> copies = EcoreUtil.copyAll(resourceA.getContents());
            resourceB.getContents().addAll(copies);

            // 在 B 中查找有 String EAttribute 的 EObject 并修改其值
            boolean modified = false;
            for (Iterator<EObject> it = EcoreUtil.getAllContents(resourceB, true); it.hasNext(); ) {
                EObject obj = it.next();
                for (EAttribute attr : obj.eClass().getEAllAttributes()) {
                    if (attr.isChangeable() && !attr.isMany() && !attr.isDerived()
                            && attr.getEType() != null
                            && attr.getEType().getInstanceClass() == String.class) {
                        Object oldValue = obj.eGet(attr);
                        String newValue = (oldValue == null) ? "test_value" : oldValue + "_modified";
                        obj.eSet(attr, newValue);
                        System.out.println("修改属性: " + obj.eClass().getName() + "." + attr.getName()
                                + " 旧值=" + oldValue + " 新值=" + newValue);
                        modified = true;
                        break;
                    }
                }
                if (modified) break;
            }

            if (!modified) {
                System.out.println("未找到可修改的 String 属性");
                return;
            }

            // 保存修改后的模型（失败不影响比较）
            try {
                resourceB.save(Collections.emptyMap());
            } catch (Exception ex) {
                System.out.println("保存修改模型失败（不影响比较）: " + ex.getMessage());
            }

            // 比较
            IComparisonScope scope = new DefaultComparisonScope(resourceA, resourceB, null);
            Comparison comparison = comparator.compare(scope, new BasicMonitor());

            int diffs = comparison.getDifferences().size();
            System.out.println("Scenario 2 (attribute change): differences=" + diffs);
            printDifferences(comparison);

            sumScenario2 += diffs;
            countScenario2++;
        } catch (Exception e) {
            System.out.println("Scenario 2 失败: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            if (resourceB != null) {
                resourceB.unload();
                rs.getResources().remove(resourceB);
            }
        }
    }

    // ===== 场景 3：添加元素后比较 =====
    static void runScenario3(EMFCompare comparator, Resource resourceA,
            String outputDir, ExtendedResourceSetImpl rs) {
        Resource resourceB = null;
        try {
            // 创建 Resource B，深拷贝 A 的所有 EObject
            resourceB = rs.createResource(URI.createFileURI(
                    new File(outputDir, "scenario3_B.arxml").getAbsolutePath()));
            Collection<EObject> copies = EcoreUtil.copyAll(resourceA.getContents());
            resourceB.getContents().addAll(copies);

            // 在 B 中查找有多值包含引用的 EObject 并添加新子对象
            boolean modified = false;
            for (Iterator<EObject> it = EcoreUtil.getAllContents(resourceB, true); it.hasNext(); ) {
                EObject obj = it.next();
                for (EReference ref : obj.eClass().getEAllContainments()) {
                    if (ref.isChangeable() && ref.isMany() && !ref.isDerived()) {
                        EClass refClass = (EClass) ref.getEType();
                        // 跳过抽象类和接口，无法实例化
                        if (refClass.isAbstract() || refClass.isInterface()) {
                            continue;
                        }
                        // 使用 EcoreUtil.create 创建新子对象（内部使用 EFactory）
                        EObject child = EcoreUtil.create(refClass);
                        @SuppressWarnings("unchecked")
                        List<EObject> list = (List<EObject>) obj.eGet(ref);
                        list.add(child);
                        System.out.println("添加元素: " + obj.eClass().getName() + "." + ref.getName()
                                + " += " + child.eClass().getName());
                        modified = true;
                        break;
                    }
                }
                if (modified) break;
            }

            if (!modified) {
                System.out.println("未找到可添加的包含引用");
                return;
            }

            // 保存修改后的模型（失败不影响比较）
            try {
                resourceB.save(Collections.emptyMap());
            } catch (Exception ex) {
                System.out.println("保存修改模型失败（不影响比较）: " + ex.getMessage());
            }

            // 比较
            IComparisonScope scope = new DefaultComparisonScope(resourceA, resourceB, null);
            Comparison comparison = comparator.compare(scope, new BasicMonitor());

            int diffs = comparison.getDifferences().size();
            System.out.println("Scenario 3 (element added): differences=" + diffs);
            printDifferences(comparison);

            sumScenario3 += diffs;
            countScenario3++;
        } catch (Exception e) {
            System.out.println("Scenario 3 失败: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            if (resourceB != null) {
                resourceB.unload();
                rs.getResources().remove(resourceB);
            }
        }
    }

    // ===== 场景 4：移除元素后比较 =====
    static void runScenario4(EMFCompare comparator, Resource resourceA,
            String outputDir, ExtendedResourceSetImpl rs) {
        Resource resourceB = null;
        try {
            // 创建 Resource B，深拷贝 A 的所有 EObject
            resourceB = rs.createResource(URI.createFileURI(
                    new File(outputDir, "scenario4_B.arxml").getAbsolutePath()));
            Collection<EObject> copies = EcoreUtil.copyAll(resourceA.getContents());
            resourceB.getContents().addAll(copies);

            // 在 B 中查找有多值包含引用且有子对象的 EObject，移除一个子对象
            boolean modified = false;
            for (Iterator<EObject> it = EcoreUtil.getAllContents(resourceB, true); it.hasNext(); ) {
                EObject obj = it.next();
                for (EReference ref : obj.eClass().getEAllContainments()) {
                    if (ref.isChangeable() && ref.isMany() && !ref.isDerived()) {
                        @SuppressWarnings("unchecked")
                        List<EObject> list = (List<EObject>) obj.eGet(ref);
                        if (!list.isEmpty()) {
                            EObject child = list.get(0);
                            list.remove(0);
                            System.out.println("移除元素: " + obj.eClass().getName() + "." + ref.getName()
                                    + " -= " + child.eClass().getName());
                            modified = true;
                            break;
                        }
                    }
                }
                if (modified) break;
            }

            if (!modified) {
                System.out.println("未找到可移除的包含引用");
                return;
            }

            // 保存修改后的模型（失败不影响比较）
            try {
                resourceB.save(Collections.emptyMap());
            } catch (Exception ex) {
                System.out.println("保存修改模型失败（不影响比较）: " + ex.getMessage());
            }

            // 比较
            IComparisonScope scope = new DefaultComparisonScope(resourceA, resourceB, null);
            Comparison comparison = comparator.compare(scope, new BasicMonitor());

            int diffs = comparison.getDifferences().size();
            System.out.println("Scenario 4 (element removed): differences=" + diffs);
            printDifferences(comparison);

            sumScenario4 += diffs;
            countScenario4++;
        } catch (Exception e) {
            System.out.println("Scenario 4 失败: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            if (resourceB != null) {
                resourceB.unload();
                rs.getResources().remove(resourceB);
            }
        }
    }

    // ===== 场景 5：比较两个不同的 arxml 文件 =====
    static void runScenario5(EMFCompare comparator, File[] files, File firstFile,
            ExtendedResourceSetImpl rs) {
        try {
            // 查找第二个不同的文件（优先小文件）
            File secondFile = null;
            for (File f : files) {
                if (!f.equals(firstFile)) {
                    secondFile = f;
                    break;
                }
            }

            if (secondFile == null) {
                System.out.println("没有第二个文件可用于比较");
                return;
            }

            System.out.println("文件1: " + firstFile.getName());
            System.out.println("文件2: " + secondFile.getName());

            // 加载两个文件
            Resource resourceA = rs.getResource(URI.createFileURI(firstFile.getAbsolutePath()), true);
            Resource resourceB = rs.getResource(URI.createFileURI(secondFile.getAbsolutePath()), true);
            totalFiles++;

            // 比较
            IComparisonScope scope = new DefaultComparisonScope(resourceA, resourceB, null);
            Comparison comparison = comparator.compare(scope, new BasicMonitor());

            int diffs = comparison.getDifferences().size();
            System.out.println("Scenario 5 (different files): differences=" + diffs);

            scenario5Differences = diffs;
        } catch (Exception e) {
            System.out.println("Scenario 5 失败: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    // ===== 打印差异列表 =====
    // 遍历 comparison.getDifferences()，打印每个差异的详细信息
    static void printDifferences(Comparison comparison) {
        int i = 1;
        for (Diff diff : comparison.getDifferences()) {
            EObject eObject = null;
            EStructuralFeature feature = null;
            Object value = null;

            // 根据差异类型提取 eObject、feature 和 value
            if (diff instanceof AttributeChange) {
                AttributeChange ac = (AttributeChange) diff;
                feature = ac.getAttribute();
                value = ac.getValue();
                Match match = ac.getMatch();
                if (match != null) {
                    // 优先取右侧（修改方），其次左侧
                    eObject = match.getRight();
                    if (eObject == null) eObject = match.getLeft();
                }
            } else if (diff instanceof ReferenceChange) {
                ReferenceChange rc = (ReferenceChange) diff;
                feature = rc.getReference();
                value = rc.getValue();
                Match match = rc.getMatch();
                if (match != null) {
                    eObject = match.getRight();
                    if (eObject == null) eObject = match.getLeft();
                }
            }

            String eObjStr = (eObject != null) ? eObject.eClass().getName() : "null";
            String featStr = (feature != null) ? feature.getName() : "null";
            String valStr = (value != null) ? value.toString() : "null";

            System.out.println("  Diff " + i + ": kind=" + diff.getKind()
                    + ", eObject=" + eObjStr
                    + ", feature=" + featStr
                    + ", value=" + valStr);
            i++;
        }
    }

    // ===== 打印总结 =====
    static void printSummary() {
        System.out.println("=== EMF Compare Summary ===");
        System.out.println("Files tested: " + totalFiles);
        System.out.println("Scenario 1 (identical): avg differences=" + avg(sumScenario1, countScenario1));
        System.out.println("Scenario 2 (attribute change): avg differences=" + avg(sumScenario2, countScenario2));
        System.out.println("Scenario 3 (element added): avg differences=" + avg(sumScenario3, countScenario3));
        System.out.println("Scenario 4 (element removed): avg differences=" + avg(sumScenario4, countScenario4));
        System.out.println("Scenario 5 (different files): differences="
                + (scenario5Differences >= 0 ? scenario5Differences : "N/A"));
    }

    // 计算平均值
    static String avg(long sum, int count) {
        if (count == 0) return "N/A";
        return String.valueOf((double) sum / count);
    }
}
