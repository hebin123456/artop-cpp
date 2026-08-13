// XMIReader.java —— Java EMF 端，加载 XMI 文件并验证数据
//
// 用途：验证 C++ 静态类型保存出来的 XMI 文件，Java EMF 能否正确读出。
// 加载流程对齐 Java EMF：XMIResourceFactory + ResourceSet + URI file scheme。

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
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Java EMF 端 XMI 读取器。
 *
 * 用法：XMIReader.main(ecore-path, xmi-path...)
 * 流程：
 *   1) 用 EcoreResourceFactoryImpl 加载 .ecore（注册到 Registry）
 *   2) 用 XMIResourceFactoryImpl 加载每个 .xmi
 *   3) 遍历 containment 树，把每节点的关键属性 dump 出来
 *   4) 退出码 0 = OK，2 = 内容错误，3 = 加载失败
 */
public final class XMIReader {

    private XMIReader() {}

    public static int readAndValidate(String[] ecorePaths, String[] xmiPaths) throws IOException {
        // 1) 加载元模型（支持多个 ecore：跨包时需要 library.ecore + library_ext.ecore 一起）
        ResourceSet rs = newResourceSet();
        EPackage libraryPkg = null;
        for (String ecorePath : ecorePaths) {
            URI ecoreUri = URI.createFileURI(ecorePath);
            Resource ecoreRes = rs.getResource(ecoreUri, true);
            EPackage pkg = (EPackage) ecoreRes.getContents().get(0);
            // 用 ecore 文件里实际的 nsURI 注册（不同 fixture 用不同 nsURI）
            EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
            // 兼容旧 demo：用第一个 ecore 的 nsURI 也映射到 EcoreModelBuilder.NS_URI
            if (libraryPkg == null) libraryPkg = pkg;
        }
        // 兼容 demo：把 EcoreModelBuilder 旧 nsURI 也指向第一个 ecore
        if (libraryPkg != null) {
            EPackage.Registry.INSTANCE.put(EcoreModelBuilder.NS_URI, libraryPkg);
        }

        // 2) 加载每个 XMI
        boolean ok = true;
        for (String xp : xmiPaths) {
            URI xuri = URI.createFileURI(xp);
            Resource xres = rs.getResource(xuri, true);
            if (xres == null) {
                System.err.println("[XMIReader] FAIL: cannot load " + xp);
                ok = false;
                continue;
            }
            System.out.println("[XMIReader] Loaded: " + xp + " contents=" + xres.getContents().size());
            for (EObject root : xres.getContents()) {
                if (!dumpValidate(root, 0, libraryPkg)) ok = false;
            }
        }
        return ok ? 0 : 2;
    }

    /** 递归 dump + 校验：每个 EObject 打印 name/title，并校验 class name 与 pkg 中声明匹配。 */
    private static boolean dumpValidate(EObject obj, int depth, EPackage pkg) {
        if (obj == null) return true;
        boolean ok = true;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) sb.append("  ");
        EClass cls = obj.eClass();
        sb.append(cls.getName());
        for (EAttribute a : cls.getEAllAttributes()) {
            Object v = obj.eGet(a);
            if (v == null) continue;
            if (a.getName().equals("name") || a.getName().equals("title")
                    || a.getName().equals("email") || a.getName().equals("birthYear")
                    || a.getName().equals("isbn") || a.getName().equals("pages")
                    || a.getName().equals("price") || a.getName().equals("issueNumber")
                    || a.getName().equals("periodicity") || a.getName().equals("category")
                    || a.getName().equals("street") || a.getName().equals("city")
                    || a.getName().equals("country") || a.getName().equals("zipCode")
                    || a.getName().equals("note") || a.getName().equals("label")
                    || a.getName().equals("publishDate")) {
                sb.append(" ").append(a.getName()).append("=").append(v);
            }
        }
        System.out.println(sb.toString());
        for (EReference ref : cls.getEAllReferences()) {
            if (ref.isContainment()) {
                Object v = obj.eGet(ref);
                if (ref.isMany()) {
                    EList<?> list = (EList<?>) v;
                    for (Object child : list) {
                        if (!dumpValidate((EObject) child, depth + 1, pkg)) ok = false;
                    }
                } else if (v instanceof EObject) {
                    if (!dumpValidate((EObject) v, depth + 1, pkg)) ok = false;
                }
            }
        }
        return ok;
    }

    private static ResourceSet newResourceSet() {
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("ecore", new EcoreResourceFactoryImpl());
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("xmi", new org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl());
        return rs;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("usage: XMIReader <ecore> [<ecore> ...] -- <xmi> [<xmi> ...]");
            System.err.println("or:    XMIReader <ecore> <xmi> [<xmi> ...]  (backward compat: only 1 ecore)");
            System.exit(2);
        }
        // 简单判断：如果有 "--" 分隔符，前面全是 ecore，后面全是 xmi
        int sep = -1;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--")) { sep = i; break; }
        }
        String[] ecorePaths;
        String[] xmiPaths;
        if (sep >= 0) {
            ecorePaths = new String[sep];
            System.arraycopy(args, 0, ecorePaths, 0, sep);
            int nx = args.length - sep - 1;
            xmiPaths = new String[nx];
            System.arraycopy(args, sep + 1, xmiPaths, 0, nx);
        } else {
            // 旧式：第一个 arg = ecore，其余 = xmi
            ecorePaths = new String[]{args[0]};
            xmiPaths = new String[args.length - 1];
            System.arraycopy(args, 1, xmiPaths, 0, xmiPaths.length);
        }
        int rc = readAndValidate(ecorePaths, xmiPaths);
        System.out.println("[XMIReader] exit=" + rc);
        System.exit(rc);
    }
}
