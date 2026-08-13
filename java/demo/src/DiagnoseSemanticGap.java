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
 * 诊断 autosar448 语义差异根因
 *
 * 流程：
 * 1. 加载原文件 → inputResource
 * 2. 统计 inputResource 中 LITERALS 等关键标签的对象数量
 * 3. 保存到输出文件
 * 4. 重新加载输出文件 → reloadedResource
 * 5. 统计 reloadedResource 中对应对象数量
 * 6. 比较两次统计，定位是反序列化丢还是序列化丢
 */
public class DiagnoseSemanticGap {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java artop.demo.DiagnoseSemanticGap <arxmlFile>");
            System.exit(1);
        }

        File arxmlFile = new File(args[0]);
        System.out.println("[diag] File: " + arxmlFile.getName());

        // 初始化
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

        // 1. 加载原文件
        URI inputUri = URI.createFileURI(arxmlFile.getAbsolutePath());
        Resource inputResource = resourceSet.getResource(inputUri, true);
        System.out.println("[diag] Loaded: " + inputResource.getContents().size() + " roots, " +
            inputResource.getErrors().size() + " errors, " + inputResource.getWarnings().size() + " warnings");
        for (Object e : inputResource.getErrors()) {
            System.out.println("[diag]   ERROR: " + e);
        }

        // 2. 统计内存对象
        System.out.println("\n[diag] === After load (in-memory) ===");
        countFeatures(inputResource);

        // 3. 保存
        File outputFile = new File("/tmp/diag_output.arxml");
        URI outputUri = URI.createFileURI(outputFile.getAbsolutePath());
        Resource outputResource = resourceSet.createResource(outputUri);
        outputResource.getContents().addAll(inputResource.getContents());
        outputResource.save(Collections.emptyMap());
        System.out.println("[diag] Saved: " + outputFile.length() + " bytes (orig: " + arxmlFile.length() + ")");

        // 4. 重新加载
        Resource reloadedResource = resourceSet.getResource(outputUri, true);
        System.out.println("[diag] Reloaded: " + reloadedResource.getContents().size() + " roots, " +
            reloadedResource.getErrors().size() + " errors");
        for (Object e : reloadedResource.getErrors()) {
            System.out.println("[diag]   RELOAD ERROR: " + e);
        }

        System.out.println("\n[diag] === After reload (in-memory) ===");
        countFeatures(reloadedResource);

        // 5. 比较内存对象
        System.out.println("\n[diag] === Memory equality ===");
        if (inputResource.getContents().size() == reloadedResource.getContents().size()) {
            for (int i = 0; i < inputResource.getContents().size(); i++) {
                EObject o1 = inputResource.getContents().get(i);
                EObject o2 = reloadedResource.getContents().get(i);
                boolean eq = org.eclipse.emf.ecore.util.EcoreUtil.equals(o1, o2);
                System.out.println("[diag]   root[" + i + "] equal: " + eq);
                if (!eq) {
                    findFirstDiff(o1, o2, 0);
                }
            }
        }
    }

    /**
     * 递归统计关键标签的对象数量
     */
    private static void countFeatures(Resource r) {
        java.util.Map<String, int[]> counts = new java.util.TreeMap<>();
        for (EObject root : r.getContents()) {
            countRecursive(root, counts);
        }
        // 输出所有非零的统计
        System.out.println("[diag]   Total EObject count by class:");
        int total = 0;
        for (java.util.Map.Entry<String, int[]> e : counts.entrySet()) {
            total += e.getValue()[0];
        }
        System.out.println("[diag]   TOTAL: " + total);
        // 关键标签
        for (java.util.Map.Entry<String, int[]> e : counts.entrySet()) {
            String name = e.getKey();
            if (name.contains("Literal") || name.contains("Supported") || name.contains("Variant") ||
                name.contains("Enumeration") || name.contains("Parameter")) {
                System.out.println("[diag]   " + name + ": " + e.getValue()[0]);
            }
        }
    }

    private static void countRecursive(EObject obj, java.util.Map<String, int[]> counts) {
        String cls = obj.eClass().getName();
        counts.computeIfAbsent(cls, k -> new int[1])[0]++;
        for (EObject child : obj.eContents()) {
            countRecursive(child, counts);
        }
    }

    /**
     * 找出第一个差异
     */
    private static void findFirstDiff(EObject o1, EObject o2, int depth) {
        if (o1 == null || o2 == null) return;
        if (!o1.eClass().getName().equals(o2.eClass().getName())) {
            System.out.println("[diag]     EClass diff: " + o1.eClass().getName() + " vs " + o2.eClass().getName());
            return;
        }
        for (EStructuralFeature f : o1.eClass().getEAllStructuralFeatures()) {
            Object v1 = o1.eGet(f);
            Object v2 = o2.eGet(f);
            if (v1 == null && v2 == null) continue;
            if (v1 == null || v2 == null) {
                System.out.println("[diag]     " + indent(depth) + "feature '" + f.getName() +
                    "' on " + o1.eClass().getName() + ": v1=" + summarize(v1) + " v2=" + summarize(v2));
                return;
            }
            if (f.isMany()) {
                java.util.List<?> l1 = (java.util.List<?>) v1;
                java.util.List<?> l2 = (java.util.List<?>) v2;
                if (l1.size() != l2.size()) {
                    System.out.println("[diag]     " + indent(depth) + "feature '" + f.getName() +
                        "' on " + o1.eClass().getName() + ": list size " + l1.size() + " vs " + l2.size());
                    System.out.println("[diag]       v1[0]: " + (l1.isEmpty() ? "empty" : summarize(l1.get(0))));
                    System.out.println("[diag]       v2[0]: " + (l2.isEmpty() ? "empty" : summarize(l2.get(0))));
                    return;
                }
            } else if (v1 instanceof EObject && v2 instanceof EObject) {
                // 递归比较
            } else if (!java.util.Objects.equals(v1, v2)) {
                System.out.println("[diag]     " + indent(depth) + "feature '" + f.getName() +
                    "' on " + o1.eClass().getName() + ": v1=" + summarize(v1) + " v2=" + summarize(v2));
                return;
            }
        }
        java.util.List<EObject> c1 = o1.eContents();
        java.util.List<EObject> c2 = o2.eContents();
        if (c1.size() != c2.size()) {
            System.out.println("[diag]     " + indent(depth) + "children size: " + c1.size() + " vs " + c2.size() +
                " on " + o1.eClass().getName());
            return;
        }
        for (int i = 0; i < c1.size(); i++) {
            findFirstDiff(c1.get(i), c2.get(i), depth + 1);
        }
    }

    private static String indent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) sb.append("  ");
        return sb.toString();
    }

    private static String summarize(Object v) {
        if (v == null) return "null";
        if (v instanceof EObject) {
            EObject e = (EObject) v;
            return e.eClass().getName() + "@" + Integer.toHexString(System.identityHashCode(e));
        }
        String s = v.toString();
        if (s.length() > 80) s = s.substring(0, 80) + "...";
        return v.getClass().getSimpleName() + ":" + s;
    }
}
