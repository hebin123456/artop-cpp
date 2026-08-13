package com.example.emfdemo;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 演示 XMI 序列化与反序列化。
 *
 *   Library (EObject)  --save-->  model.xmi  --load-->  Library (EObject)
 */
public final class XmiSerializerDemo {

    private XmiSerializerDemo() {}

    public static void run(String xmiFilePath) throws IOException {
        System.out.println("\n========== Demo 2: XMI 序列化/反序列化 ==========");

        // 1) 构建 Ecore 元模型（内存），并注册到全局 Registry，
        //    以便反序列化时能找到 NS URI 对应的 EClass 实例。
        EPackage libraryPkg = EcoreModelBuilder.build();
        EPackage.Registry.INSTANCE.put(EcoreModelBuilder.NS_URI, libraryPkg);

        // 2) 构造数据实例
        EObject library = DynamicInstanceFactory.createSampleLibrary(libraryPkg);
        System.out.println("[XmiSerializerDemo] In-memory library tree:");
        DynamicInstanceFactory.dump(library, 0);

        // 3) 保存为 XMI
        ResourceSet rs = DynamicInstanceFactory.createResourceSet();
        URI uri = DynamicInstanceFactory.createFileURI(xmiFilePath);
        Resource res = rs.createResource(uri);
        res.getContents().add(library);
        res.save(null);
        System.out.println("[XmiSerializerDemo] Saved XMI to: " + xmiFilePath);

        // 4) 从 XMI 加载回来
        ResourceSet rs2 = DynamicInstanceFactory.createResourceSet();
        URI uri2 = DynamicInstanceFactory.createFileURI(xmiFilePath);
        Resource loaded = rs2.getResource(uri2, true);
        EObject restored = loaded.getContents().get(0);
        System.out.println("[XmiSerializerDemo] Loaded back root: "
                + restored.eClass().getName()
                + " name=" + restored.eGet(restored.eClass().getEStructuralFeature("name")));

        // 5) 校验：再次打印
        System.out.println("[XmiSerializerDemo] Restored tree:");
        DynamicInstanceFactory.dump(restored, 0);

        // 6) 校验：原始与反序列化后的引用结构是否一致
        assertSameStructure(library, restored);
        System.out.println("[XmiSerializerDemo] 结构一致 ✓");
    }

    /** 简单的结构对比：递归比较所有 containment 引用。
     *  注意：EClass 实例可能不同（每次 reload 都是新的 EClass 对象），
     *  所以按 name 比较。 */
    private static void assertSameStructure(EObject a, EObject b) {
        if (!a.eClass().getName().equals(b.eClass().getName())) {
            throw new AssertionError("Class mismatch: "
                    + a.eClass().getName() + " vs " + b.eClass().getName());
        }
        for (EAttribute attr : a.eClass().getEAllAttributes()) {
            Object va = a.eGet(attr);
            Object vb = b.eGet(attr);
            if (va == null ? vb != null : !va.equals(vb)) {
                throw new AssertionError("Attribute mismatch on " + attr.getName()
                        + ": " + va + " vs " + vb);
            }
        }
        for (EReference ref : a.eClass().getEAllReferences()) {
            if (ref.isContainment()) {
                Object ra = a.eGet(ref);
                Object rb = b.eGet(ref);
                if (ref.isMany()) {
                    EList<?> ca = (EList<?>) ra;
                    EList<?> cb = (EList<?>) rb;
                    if (ca.size() != cb.size()) {
                        throw new AssertionError("Containment size mismatch on " + ref.getName()
                                + ": " + ca.size() + " vs " + cb.size());
                    }
                    for (int i = 0; i < ca.size(); i++) {
                        assertSameStructure((EObject) ca.get(i), (EObject) cb.get(i));
                    }
                } else {
                    if (ra instanceof EObject) {
                        // 同样的对象 identity 比较不靠谱：递归比结构
                        assertSameStructure((EObject) ra, (EObject) rb);
                    } else if (ra == null ? rb != null : !ra.equals(rb)) {
                        throw new AssertionError("Containment mismatch on " + ref.getName()
                                + ": " + ra + " vs " + rb);
                    }
                }
            }
        }
    }
}
