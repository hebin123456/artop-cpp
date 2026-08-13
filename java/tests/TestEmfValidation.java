import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import org.eclipse.emf.validation.model.EvaluationMode;
import org.eclipse.emf.validation.service.IBatchValidator;
import org.eclipse.emf.validation.service.ILiveValidator;
import org.eclipse.emf.validation.service.IValidator;
import org.eclipse.emf.validation.service.ModelValidationService;
import org.eclipse.sphinx.emf.ecore.proxymanagement.IProxyResolverService;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 测试 EMF Validation 在 AUTOSAR 模型上的应用
 * 包含静态批量验证（Diagnostician + IBatchValidator）和动态实时验证（ILiveValidator）
 * 用法: java TestEmfValidation <rootPackageClassName> <inputDir>
 *   rootPackageClassName: 例如 autosar40.util.Autosar40Package
 *   inputDir: 包含 .arxml 文件的目录
 */
public class TestEmfValidation {
    // ===== 全局统计字段 =====
    static int totalFiles = 0;
    static int totalEObjects = 0;
    static int totalDiagDiagnostics = 0;
    static int totalDiagErrors = 0;
    static int totalDiagWarnings = 0;
    static int totalEmfValBatchDiagnostics = 0;
    static int dynamicValidations = 0;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("用法: java TestEmfValidation <rootPackageClassName> <inputDir>");
            System.out.println("  rootPackageClassName: 例如 autosar40.util.Autosar40Package");
            System.out.println("  inputDir: 包含 .arxml 文件的目录");
            return;
        }

        String rootPackageClassName = args[0];
        String inputDir = args[1];

        System.out.println("Root package class: " + rootPackageClassName);
        System.out.println("Input directory: " + inputDir);

        // 初始化（复刻 TestExactDemoInit 的模式，使用反射）
        ExtendedResourceSetImpl rs = initialize(rootPackageClassName);

        // Part 1: 静态批量验证
        System.out.println("\n=== Part 1: Static Batch Validation ===");
        Resource firstResource = staticBatchValidation(rs, inputDir);

        // Part 2: 动态实时验证
        System.out.println("\n=== Part 2: Dynamic Live Validation ===");
        dynamicLiveValidation(rs, firstResource, inputDir);

        // Part 3: 总结
        System.out.println("\n=== Part 3: Summary ===");
        printSummary();
    }

    // ===== 初始化（复刻 TestExactDemoInit 的模式，使用反射以兼容官方和 dummy jar）=====
    static ExtendedResourceSetImpl initialize(String rootPackageClassName) throws Exception {
        // 通过反射加载根包类并触发初始化
        Class<?> rootPkgClass = Class.forName(rootPackageClassName);
        Object eINSTANCE = rootPkgClass.getField("eINSTANCE").get(null);
        // 触发类初始化
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

    // ===== Part 1: 静态批量验证 =====
    static Resource staticBatchValidation(ExtendedResourceSetImpl rs, String inputDir) {
        File[] files = new File(inputDir).listFiles((d, name) -> name.endsWith(".arxml"));
        if (files == null || files.length == 0) {
            System.out.println("未找到 arxml 文件");
            return null;
        }
        Arrays.sort(files);

        Resource firstResource = null;

        // 尝试创建 EMF Validation 批量验证器（可能需要 OSGi 环境，失败则跳过）
        IBatchValidator batchValidator = null;
        try {
            ModelValidationService service = ModelValidationService.getInstance();
            batchValidator = (IBatchValidator) service.newValidator(EvaluationMode.BATCH);
            // 设置遍历策略为默认策略
            batchValidator.setOption(IBatchValidator.OPTION_TRAVERSAL_STRATEGY,
                    batchValidator.getDefaultTraversalStrategy());
            System.out.println("EMF Validation 批量验证器创建成功");
        } catch (Throwable e) {
            System.out.println("EMF Validation 批量验证器创建失败（可能需要 OSGi 环境）: "
                    + e.getClass().getName() + ": " + e.getMessage());
        }

        for (File f : files) {
            try {
                URI uri = URI.createFileURI(f.getAbsolutePath());
                Resource resource = rs.getResource(uri, true);

                // 保留第一个成功加载的资源供 Part 2 使用
                if (firstResource == null) {
                    firstResource = resource;
                }

                // 统计 EObject 数量
                int objectCount = countEObjects(resource);

                // 使用 EMF 核心 Diagnostician 验证所有根 EObject
                int[] diagCounts = {0, 0, 0}; // [errors, warnings, total]
                for (EObject root : resource.getContents()) {
                    EObject container = EcoreUtil.getRootContainer(root);
                    Diagnostic diag = Diagnostician.INSTANCE.validate(container);
                    countDiagnostic(diag, diagCounts);
                }

                // 使用 EMF Validation 框架的 IBatchValidator 验证
                int emfValCount = 0;
                if (batchValidator != null) {
                    try {
                        for (EObject root : resource.getContents()) {
                            EObject container = EcoreUtil.getRootContainer(root);
                            IStatus status = batchValidator.validate(container);
                            emfValCount += countStatus(status);
                        }
                    } catch (Throwable e) {
                        System.out.println("  [" + f.getName() + "] EMF Validation 批量验证失败: "
                                + e.getClass().getName() + ": " + e.getMessage());
                    }
                }

                // 打印每个文件的汇总
                System.out.println(f.getName() + ": objects=" + objectCount
                        + ", diagnostics=" + diagCounts[2]
                        + " (errors=" + diagCounts[0] + ", warnings=" + diagCounts[1] + ")"
                        + (emfValCount > 0 ? ", emfVal=" + emfValCount : ""));

                // 更新全局统计
                totalFiles++;
                totalEObjects += objectCount;
                totalDiagDiagnostics += diagCounts[2];
                totalDiagErrors += diagCounts[0];
                totalDiagWarnings += diagCounts[1];
                totalEmfValBatchDiagnostics += emfValCount;

                // 卸载非第一个资源以节省内存
                if (resource != firstResource) {
                    resource.unload();
                    rs.getResources().remove(resource);
                }
            } catch (Exception e) {
                System.out.println(f.getName() + ": 加载失败 - " + e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        return firstResource;
    }

    // ===== Part 2: 动态实时验证 =====
    static void dynamicLiveValidation(ExtendedResourceSetImpl rs, Resource resource, String inputDir) {
        // 如果没有可用的资源，尝试重新加载第一个成功的 arxml 文件
        if (resource == null) {
            File[] files = new File(inputDir).listFiles((d, name) -> name.endsWith(".arxml"));
            if (files != null) {
                Arrays.sort(files);
                for (File f : files) {
                    try {
                        URI uri = URI.createFileURI(f.getAbsolutePath());
                        resource = rs.getResource(uri, true);
                        break;
                    } catch (Exception e) {
                        // 继续尝试下一个文件
                    }
                }
            }
        }

        if (resource == null) {
            System.out.println("没有可用的资源进行动态验证");
            return;
        }

        System.out.println("使用资源: " + resource.getURI().lastSegment());

        // 创建实时验证器（可能需要 OSGi 环境，失败则跳过）
        ILiveValidator liveValidator = null;
        try {
            ModelValidationService service = ModelValidationService.getInstance();
            liveValidator = (ILiveValidator) service.newValidator(EvaluationMode.LIVE);

            // 尝试设置 live validation 选项（使用反射，因为 OPTION_LIVE_VALIDATION 可能不存在）
            try {
                java.lang.reflect.Field optField = ILiveValidator.class.getField("OPTION_LIVE_VALIDATION");
                Object opt = optField.get(null);
                if (opt != null) {
                    // 通过反射调用 setOption 以避免泛型类型问题
                    java.lang.reflect.Method m = liveValidator.getClass().getMethod(
                            "setOption",
                            IValidator.Option.class,
                            Object.class);
                    m.invoke(liveValidator, opt, Boolean.TRUE);
                    System.out.println("已设置 OPTION_LIVE_VALIDATION = true");
                }
            } catch (NoSuchFieldException e) {
                System.out.println("OPTION_LIVE_VALIDATION 不存在，跳过（live validator 默认即启用实时验证）");
            } catch (Throwable e) {
                System.out.println("设置 OPTION_LIVE_VALIDATION 失败: " + e.getMessage());
            }

            System.out.println("实时验证器创建成功: " + liveValidator.getClass().getName());
        } catch (Throwable e) {
            System.out.println("实时验证器创建失败（可能需要 OSGi 环境）: "
                    + e.getClass().getName() + ": " + e.getMessage());
        }

        // 注册适配器到资源集，用于拦截通知（演示用途）
        Adapter adapter = new AdapterImpl() {
            @Override
            public void notifyChanged(Notification notification) {
                System.out.println("  [Adapter] 收到通知: type=" + notification.getEventType()
                        + ", feature=" + notification.getFeature());
            }
        };
        rs.eAdapters().add(adapter);
        System.out.println("已注册适配器到资源集");

        if (liveValidator == null) {
            System.out.println("无法进行动态验证（实时验证器不可用）");
            return;
        }

        // a. 修改属性
        testModifyAttribute(resource, liveValidator);

        // b. 添加引用
        testAddReference(resource, liveValidator);

        // c. 移除引用
        testRemoveReference(resource, liveValidator);

        // d. 设置无效值
        testSetInvalidValue(resource, liveValidator);
    }

    // --- a. 修改属性 ---
    static void testModifyAttribute(Resource resource, ILiveValidator liveValidator) {
        System.out.println("\n--- a. 修改属性 ---");
        try {
            // 查找有可修改 String 属性的 EObject
            for (Iterator<EObject> it = EcoreUtil.getAllContents(resource, true); it.hasNext(); ) {
                EObject obj = it.next();
                for (EAttribute attr : obj.eClass().getEAllAttributes()) {
                    if (attr.isChangeable() && !attr.isMany()
                            && attr.getEType() != null
                            && attr.getEType().getInstanceClass() == String.class) {
                        Object oldValue = obj.eGet(attr);
                        String newValue = (oldValue == null) ? "test_value" : oldValue + "_modified";

                        // 实际修改属性值
                        obj.eSet(attr, newValue);

                        // 创建通知并传递给实时验证器
                        Notification notification = new ENotificationImpl(
                                (InternalEObject) obj, Notification.SET, attr, oldValue, newValue);
                        IStatus status = liveValidator.validate(notification);

                        System.out.println("  对象: " + obj.eClass().getName()
                                + ", 属性: " + attr.getName()
                                + ", 旧值: " + oldValue + ", 新值: " + newValue);
                        printStatusResult(status);

                        dynamicValidations++;
                        return;
                    }
                }
            }
            System.out.println("  未找到可修改的 String 属性");
        } catch (Throwable e) {
            System.out.println("  修改属性失败: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    // --- b. 添加引用 ---
    static void testAddReference(Resource resource, ILiveValidator liveValidator) {
        System.out.println("\n--- b. 添加引用 ---");
        try {
            for (Iterator<EObject> it = EcoreUtil.getAllContents(resource, true); it.hasNext(); ) {
                EObject obj = it.next();
                for (EReference ref : obj.eClass().getEAllContainments()) {
                    if (ref.isChangeable() && ref.isMany()) {
                        EClass refClass = (EClass) ref.getEType();
                        // 跳过抽象类和接口，无法实例化
                        if (refClass.isAbstract() || refClass.isInterface()) {
                            continue;
                        }

                        // 创建新的子对象
                        EObject child = refClass.getEPackage().getEFactoryInstance().create(refClass);

                        @SuppressWarnings("unchecked")
                        List<EObject> list = (List<EObject>) obj.eGet(ref);

                        // 实际添加子对象
                        list.add(child);

                        // 创建通知并传递给实时验证器
                        Notification notification = new ENotificationImpl(
                                (InternalEObject) obj, Notification.ADD, ref, null, child);
                        IStatus status = liveValidator.validate(notification);

                        System.out.println("  对象: " + obj.eClass().getName()
                                + ", 引用: " + ref.getName()
                                + ", 添加: " + child.eClass().getName());
                        printStatusResult(status);

                        dynamicValidations++;
                        return;
                    }
                }
            }
            System.out.println("  未找到可添加的包含引用");
        } catch (Throwable e) {
            System.out.println("  添加引用失败: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    // --- c. 移除引用 ---
    static void testRemoveReference(Resource resource, ILiveValidator liveValidator) {
        System.out.println("\n--- c. 移除引用 ---");
        try {
            for (Iterator<EObject> it = EcoreUtil.getAllContents(resource, true); it.hasNext(); ) {
                EObject obj = it.next();
                for (EReference ref : obj.eClass().getEAllContainments()) {
                    if (ref.isChangeable() && ref.isMany()) {
                        @SuppressWarnings("unchecked")
                        List<EObject> list = (List<EObject>) obj.eGet(ref);
                        if (!list.isEmpty()) {
                            EObject child = list.get(0);

                            // 实际移除子对象
                            list.remove(0);

                            // 创建通知并传递给实时验证器
                            Notification notification = new ENotificationImpl(
                                    (InternalEObject) obj, Notification.REMOVE, ref, child, null);
                            IStatus status = liveValidator.validate(notification);

                            System.out.println("  对象: " + obj.eClass().getName()
                                    + ", 引用: " + ref.getName()
                                    + ", 移除: " + child.eClass().getName());
                            printStatusResult(status);

                            dynamicValidations++;
                            return;
                        }
                    }
                }
            }
            System.out.println("  未找到可移除的包含引用");
        } catch (Throwable e) {
            System.out.println("  移除引用失败: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    // --- d. 设置无效值 ---
    static void testSetInvalidValue(Resource resource, ILiveValidator liveValidator) {
        System.out.println("\n--- d. 设置无效值 ---");
        try {
            // 首先尝试查找有必填属性（lowerBound > 0）的 EObject，设置为 null
            for (Iterator<EObject> it = EcoreUtil.getAllContents(resource, true); it.hasNext(); ) {
                EObject obj = it.next();
                for (EAttribute attr : obj.eClass().getEAllAttributes()) {
                    if (attr.isChangeable() && !attr.isMany() && attr.getLowerBound() > 0) {
                        Object oldValue = obj.eGet(attr);

                        // 创建通知并验证（设置为 null，违反必填约束）
                        Notification notification = new ENotificationImpl(
                                (InternalEObject) obj, Notification.SET, attr, oldValue, null);
                        IStatus status = liveValidator.validate(notification);

                        System.out.println("  对象: " + obj.eClass().getName()
                                + ", 必填属性: " + attr.getName()
                                + ", 旧值: " + oldValue + ", 新值: null");
                        printStatusResult(status);

                        dynamicValidations++;
                        return;
                    }
                }
            }

            // 如果没有必填属性，尝试将 String 属性设置为 null
            for (Iterator<EObject> it = EcoreUtil.getAllContents(resource, true); it.hasNext(); ) {
                EObject obj = it.next();
                for (EAttribute attr : obj.eClass().getEAllAttributes()) {
                    if (attr.isChangeable() && !attr.isMany()
                            && attr.getEType() != null
                            && attr.getEType().getInstanceClass() == String.class) {
                        Object oldValue = obj.eGet(attr);
                        if (oldValue != null) {
                            // 创建通知并验证（设置为 null）
                            Notification notification = new ENotificationImpl(
                                    (InternalEObject) obj, Notification.SET, attr, oldValue, null);
                            IStatus status = liveValidator.validate(notification);

                            System.out.println("  对象: " + obj.eClass().getName()
                                    + ", 属性: " + attr.getName()
                                    + ", 旧值: " + oldValue + ", 新值: null (尝试无效值)");
                            printStatusResult(status);

                            dynamicValidations++;
                            return;
                        }
                    }
                }
            }
            System.out.println("  未找到可设置无效值的属性");
        } catch (Throwable e) {
            System.out.println("  设置无效值失败: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    // ===== 辅助方法 =====

    // 统计资源中的 EObject 数量
    static int countEObjects(Resource resource) {
        int count = 0;
        for (Iterator<EObject> it = EcoreUtil.getAllContents(resource, true); it.hasNext(); ) {
            it.next();
            count++;
        }
        return count;
    }

    // 递归统计 Diagnostic（errors, warnings, total）
    static void countDiagnostic(Diagnostic diag, int[] counts) {
        counts[2]++; // total
        int sev = diag.getSeverity();
        if (sev == Diagnostic.ERROR) {
            counts[0]++;
        } else if (sev == Diagnostic.WARNING) {
            counts[1]++;
        }
        for (Diagnostic child : diag.getChildren()) {
            countDiagnostic(child, counts);
        }
    }

    // 递归统计 IStatus 数量
    static int countStatus(IStatus status) {
        if (status == null) {
            return 0;
        }
        int count = 1;
        for (IStatus child : status.getChildren()) {
            count += countStatus(child);
        }
        return count;
    }

    // 打印 IStatus 验证结果
    static void printStatusResult(IStatus status) {
        if (status == null) {
            System.out.println("    结果: null");
            return;
        }
        String severity;
        switch (status.getSeverity()) {
            case IStatus.OK: severity = "OK"; break;
            case IStatus.INFO: severity = "INFO"; break;
            case IStatus.WARNING: severity = "WARNING"; break;
            case IStatus.ERROR: severity = "ERROR"; break;
            case IStatus.CANCEL: severity = "CANCEL"; break;
            default: severity = "UNKNOWN(" + status.getSeverity() + ")";
        }
        System.out.println("    严重级别: " + severity
                + ", 消息: " + status.getMessage()
                + ", 子状态数: " + status.getChildren().length);
        if (status.isMultiStatus()) {
            for (IStatus child : status.getChildren()) {
                String childSev;
                switch (child.getSeverity()) {
                    case IStatus.OK: childSev = "OK"; break;
                    case IStatus.INFO: childSev = "INFO"; break;
                    case IStatus.WARNING: childSev = "WARNING"; break;
                    case IStatus.ERROR: childSev = "ERROR"; break;
                    case IStatus.CANCEL: childSev = "CANCEL"; break;
                    default: childSev = "UNKNOWN";
                }
                System.out.println("      - [" + childSev + "] " + child.getMessage());
            }
        }
    }

    // 打印总结
    static void printSummary() {
        System.out.println("=== EMF Validation Summary ===");
        System.out.println("Files validated: " + totalFiles);
        System.out.println("Total EObjects: " + totalEObjects);
        System.out.println("Total diagnostics (Diagnostician): " + totalDiagDiagnostics
                + " (errors=" + totalDiagErrors + ", warnings=" + totalDiagWarnings + ")");
        System.out.println("Total diagnostics (EMF Validation batch): " + totalEmfValBatchDiagnostics);
        System.out.println("Dynamic validations: " + dynamicValidations + " modifications tested");
    }
}
