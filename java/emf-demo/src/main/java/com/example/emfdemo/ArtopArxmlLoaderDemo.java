package com.example.emfdemo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;

import autosar40.util.Autosar40ResourceFactoryImpl;
import autosar40.util.Autosar40Package;

/**
 * Java ARTOP Demo —— 加载 /workspace/decompiler/autosar448/model/library 下 53 个 arxml 文件。
 *
 * 核心 ARTOP 加载链：
 *   1) autosar40.util.Autosar40ResourceFactoryImpl  —— 创建 Autosar40ResourceImpl
 *      根据 schemaLocation (`http://autosar.org/schema/r4.0/...`) 选对应 AutosarReleaseDescriptor
 *      (4.0.1 / 4.0.2 / ... / 4.4.8)，调用 initResource 注册 ExtendedMetaData + schemaLocation
 *      catalog + options，完成 arxml 解析所需的命名空间映射和 default values。
 *
 *   2) gautosar.util.GautosarResourceFactoryImpl  —— 创建 GautosarResourceImpl
 *      处理通用 gautosar.ecore 元数据（abstract / mixed / attribute map 等）。
 *
 *   3) ResourceSet 用 org.artop.aal.common.resource.impl.AutosarResourceSetImpl 吗？
 *      这里为了不引入 sphinx 全套（ScopingResourceSetImpl 依赖更多 sphinx 类），
 *      用普通 ResourceSetImpl + 手动注册 Resource.Factory，跑得动就行。
 *      （生产环境应该用 AutosarResourceSetImpl，能跨 arxml 自动解析引用。）
 *
 *   4) 每个 arxml 文件是一个 Resource；根对象是 AUTOSAR（autosar40 的 EClass），
 *      含 AR-PACKAGES 树，子节点 AR-PACKAGE / ELEMENTS / SHOR-NAME-PATTERN / REFERRABLE 等。
 */
public class ArtopArxmlLoaderDemo {

    /** arxml 根元素 namespace 必须是 `http://autosar.org/schema/r4.0` */
    private static final String AUTOSAR_4X_NS = "http://autosar.org/schema/r4.0";

    /** arxml 文件目录（命令行参数可覆盖） */
    private static final String DEFAULT_LIBRARY_DIR = "/workspace/decompiler/autosar448/model/library";

    public static void main(String[] args) throws IOException {
        File libraryDir = new File(args.length > 0 ? args[0] : DEFAULT_LIBRARY_DIR);
        System.out.println("[ARTOP demo] scanning arxml in: " + libraryDir);
        if (!libraryDir.isDirectory()) {
            System.err.println("[ARTOP demo] not a directory: " + libraryDir);
            System.exit(1);
        }

        ResourceSet rs = createResourceSet(libraryDir);
        registerResourceFactories(rs);

        // 收集 arxml
        File[] arxmls = libraryDir.listFiles((d, n) -> n.toLowerCase().endsWith(".arxml"));
        if (arxmls == null || arxmls.length == 0) {
            System.err.println("[ARTOP demo] no arxml in " + libraryDir);
            return;
        }
        Arrays.sort(arxmls);
        System.out.println("[ARTOP demo] found " + arxmls.length + " arxml files");

        // 加载并统计
        Map<String, Integer> classifierCount = new LinkedHashMap<>();
        int totalResources = 0;
        int totalObjects = 0;
        int loadErrors = 0;
        long t0 = System.currentTimeMillis();

        for (File f : arxmls) {
            URI uri = URI.createFileURI(f.getAbsolutePath());
            try {
                // 使用 demandLoad=false 让 arxml 在 getResource 时立即 load
                // 这样 ReferenceBase 在 getPackage() 时的 proxy 解析能正常完成
                // （因为 ar:/ 协议由 URIHandler 处理，且 ar 协议 Resource.Factory 已注册）
                Resource res = rs.getResource(uri, true);
                totalResources++;
                int cnt = 0;
                for (EObject obj : res.getContents()) {
                    cnt += countAndClassify(obj, classifierCount);
                }
                totalObjects += cnt;
                System.out.printf("  [ok] %-70s root=%-30s objects=%d%n",
                    f.getName(), res.getContents().isEmpty() ? "<empty>" : res.getContents().get(0).eClass().getName(), cnt);
            } catch (Throwable t) {
                loadErrors++;
                // 区分真正的错误和 ARTOP 内部 ar:/ 协议引用无法解析的情况
                String msg = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
                if (msg.contains("ar:") || msg.contains("Cannot create a resource")) {
                    // 退而求其次：不用 demandLoad，让 load 推迟，统计已 parse 的部分
                    try {
                        // 清掉之前可能 partial-load 的缓存
                        Resource res = rs.createResource(uri);
                        res.load(rs.getURIConverter().createInputStream(uri), null);
                        totalResources++;
                        int cnt = 0;
                        for (EObject obj : res.getContents()) {
                            cnt += countAndClassify(obj, classifierCount);
                        }
                        totalObjects += cnt;
                        System.out.printf("  [partial] %-67s root=%-30s objects=%d%n",
                            f.getName(), res.getContents().isEmpty() ? "<empty>" : res.getContents().get(0).eClass().getName(), cnt);
                    } catch (Throwable t2) {
                        System.out.printf("  [err]  %-70s err=%s%n", f.getName(), t2.getClass().getSimpleName() + ": " + t2.getMessage());
                        if (System.getenv("ARTOP_DEBUG") != null) {
                            t2.printStackTrace();
                        }
                    }
                } else {
                    System.out.printf("  [err]  %-70s err=%s%n", f.getName(), t.getClass().getSimpleName() + ": " + msg);
                    if (System.getenv("ARTOP_DEBUG") != null) {
                        t.printStackTrace();
                    }
                }
            }
        }
        long dt = System.currentTimeMillis() - t0;

        System.out.println();
        System.out.println("[ARTOP demo] ========== summary ==========");
        System.out.println("  arxml loaded:        " + totalResources + "/" + arxmls.length);
        System.out.println("  load errors:         " + loadErrors);
        System.out.println("  total EObjects:      " + totalObjects);
        System.out.println("  elapsed:             " + dt + " ms");
        System.out.println("  EClass distinct cnt: " + classifierCount.size());
        System.out.println();
        System.out.println("[ARTOP demo] top 30 EClass types in loaded arxmls:");
        classifierCount.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(30)
            .forEach(e -> System.out.printf("    %-40s %6d%n", e.getKey(), e.getValue()));
    }

    /** 创建 ResourceSet（含 EPackage Registry、Resource.Factory Registry、URI 映射） */
    private static ResourceSet createResourceSet(File libraryDir) {
        ResourceSet rs = new ResourceSetImpl();

        // 关键：注册 autosar40 + gautosar 两个顶层 EPackage 到全局 Registry。
        // ARTOP 的 XMIHelper 在解析 arxml 时会从 EPackage.Registry 查 nsURI 找 EClass 实例。
        EPackage.Registry registry = rs.getPackageRegistry();
        registry.put(Autosar40Package.eNS_URI, Autosar40Package.eINSTANCE);
        // gautosar 没单独 ResourceFactory，但 EClass 通过 autosar40 的 EPackage 引用，必须注册
        // GAutosarPackage 在 gautosar.util 包下
        try {
            Class<?> cls = Class.forName("gautosar.util.GAutosarPackage");
            Object inst = cls.getField("eINSTANCE").get(null);
            Object nsUri = cls.getField("eNS_URI").get(null);
            registry.put((String) nsUri, (EPackage) inst);
        } catch (Throwable t) {
            System.err.println("[ARTOP demo] WARN: skip GautosarPackage registration: " + t);
        }

        // 装一个调试用的 URIHandler 看实际协议，再装一个 ar 协议处理器
        installArProtocolHandler(rs, libraryDir);
        return rs;
    }

    /**
     * 给 ResourceSet 的 URIConverter 注册一个 URIHandler，把 ar:/ 协议改写到本地 file:
     * 协议，定位到 libraryDir 下的 .arxml 文件。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void installArProtocolHandler(ResourceSet rs, File libraryDir) {
        final File libDir = libraryDir;

        // ar 协议 handler：把 ar:/filename/<path>#fragment
        //              -> file:/abs/path/to/library/<filename>.arxml#fragment
        // ARTOP 的 Sphinx ScopingResourceSetImpl 默认就有这层映射。
        // 我们用普通 ResourceSetImpl 时只能自己实现：segment(0) 是 arxml 文件名（不带 .arxml），
        // 后续 segment 是 AR-PACKAGE 路径。空路径（ar:/）直接返回原 URI。
        URIHandler arHandler = new URIHandler() {
            // ATTRIBUTE_* 标准 key 名
            static final String ATTRIBUTE_TIME_STAMP = "timeStamp";
            static final String ATTRIBUTE_LENGTH = "length";
            static final String ATTRIBUTE_EXISTS = "exists";
            static final String ATTRIBUTE_READ_ONLY = "readOnly";
            static final String ATTRIBUTE_DIRECTORY = "directory";

            @Override
            public boolean canHandle(URI uri) {
                return "ar".equals(uri.scheme());
            }

            @Override
            public java.io.InputStream createInputStream(URI uri, Map options) throws java.io.IOException {
                URI fileUri = resolve(uri);
                if (fileUri == uri) {
                    // 没解析出来 (空路径 ar:/)，返回空流让 normalize 失败
                    return new java.io.ByteArrayInputStream(new byte[0]);
                }
                return new java.io.FileInputStream(new java.io.File(fileUri.toFileString()));
            }

            @Override
            public java.io.OutputStream createOutputStream(URI uri, Map options) throws java.io.IOException {
                URI fileUri = resolve(uri);
                if (fileUri == uri) throw new java.io.IOException("cannot write to " + uri);
                return new java.io.FileOutputStream(new java.io.File(fileUri.toFileString()));
            }

            @Override
            public void delete(URI uri, Map options) throws java.io.IOException {
                if (uri.segmentCount() == 0) return;
                new java.io.File(resolve(uri).toFileString()).delete();
            }

            @Override
            public boolean exists(URI uri, Map options) {
                if (uri.segmentCount() == 0) return false;
                java.io.File f = new java.io.File(resolve(uri).toFileString());
                return f.exists();
            }

            @Override
            public Map getAttributes(URI uri, Map options) {
                java.io.File f = uri.segmentCount() == 0 ? null : new java.io.File(resolve(uri).toFileString());
                Map attrs = new java.util.LinkedHashMap();
                if (f != null) {
                    attrs.put(ATTRIBUTE_TIME_STAMP, f.lastModified());
                    attrs.put(ATTRIBUTE_LENGTH, f.length());
                    attrs.put(ATTRIBUTE_EXISTS, f.exists());
                    attrs.put(ATTRIBUTE_READ_ONLY, !f.canWrite());
                    attrs.put(ATTRIBUTE_DIRECTORY, f.isDirectory());
                } else {
                    attrs.put(ATTRIBUTE_EXISTS, false);
                }
                return attrs;
            }

            @Override
            public void setAttributes(URI uri, Map attributes, Map options) throws java.io.IOException {
                if (uri.segmentCount() == 0) return;
                java.io.File f = new java.io.File(resolve(uri).toFileString());
                if (attributes.containsKey(ATTRIBUTE_TIME_STAMP)) {
                    f.setLastModified((Long) attributes.get(ATTRIBUTE_TIME_STAMP));
                }
            }

            @Override
            public Map contentDescription(URI uri, Map options) throws java.io.IOException {
                return java.util.Collections.emptyMap();
            }

            /**
             * ar:/<filename>/<package-path>#<fragment>
             * -> file:/abs/path/to/library/<filename>.arxml#<fragment>
             */
            private URI resolve(URI uri) {
                if (uri.segmentCount() == 0) {
                    // 没有 segment 的 ar:/ URI (例如 ar:/) 通常是 fragment-only 引用
                    return uri;
                }
                String firstSeg = uri.segment(0);
                java.io.File arxmlFile = new java.io.File(libDir, firstSeg + ".arxml");
                URI fileUri = URI.createFileURI(arxmlFile.getAbsolutePath());
                if (uri.fragment() != null) {
                    fileUri = fileUri.appendFragment(uri.fragment());
                }
                return fileUri;
            }
        };
        rs.getURIConverter().getURIHandlers().add(0, arHandler);
        System.out.println("[ARTOP demo] ar:/ protocol handler installed -> " + libraryDir.getAbsolutePath() + "/*.arxml");
    }

    /** 注册 arxml Resource.Factory —— 用 ARTOP 的 Autosar40ResourceFactoryImpl。 */
    private static void registerResourceFactories(ResourceSet rs) {
        // ARTOP factory 用 .arxml extension 识别，并按 schemaLocation 选 release
        Resource.Factory.Registry reg = rs.getResourceFactoryRegistry();
        Resource.Factory autosar40Factory = new Autosar40ResourceFactoryImpl();
        reg.getExtensionToFactoryMap().put("arxml", autosar40Factory);
        reg.getProtocolToFactoryMap().put("arxml", autosar40Factory);

        // 注册 ar 协议 Resource.Factory
        // 当 Sphinx ProxyResolutionBehavior 解析 ar:/ 协议时，会经过 URIConverter.normalize
        // (由 ar URIHandler 改写为 file:/...) → getResource(URI, true)
        // 如果改写失败 (例如 ar:/ 完全是空路径)，getResource 还是会去找 ar 协议的 factory。
        // 这里注册一个 dummy factory：返回一个"已加载"的空 XMIResource。
        // - load(Map) 被重写为 no-op（EMF 的 ResourceSetImpl.getResource 检查的是
        //   getContents().isEmpty() 而非 isLoaded()，所以仍会触发 demandLoadHelper）
        // - getEObject(fragment) 被重写为直接返回 null，避免 NumberFormatException
        //   （ResourceSetImpl.getEObject(URI, false) 会遍历所有资源并调用
        //    resource.getEObject(fragment)，如果 fragment 不是数字会抛 NFE）
        reg.getProtocolToFactoryMap().put("ar", new Resource.Factory() {
            @Override
            public Resource createResource(URI uri) {
                return new org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl(uri) {
                    @Override
                    public boolean isLoaded() { return true; }
                    @Override
                    public void load(Map<?, ?> options) throws java.io.IOException { /* no-op */ }
                    @Override
                    public EObject getEObject(String uriFragment) { return null; }
                };
            }
        });
    }

    /** 用 TreeIterator 迭代统计 EClass 出现次数（避免深递归导致 StackOverflow）。 */
    private static int countAndClassify(EObject root, Map<String, Integer> counter) {
        if (root == null) return 0;
        int n = 0;
        TreeIterator<EObject> it = EcoreUtil.getAllContents(Collections.singleton(root));
        while (it.hasNext()) {
            EObject obj = it.next();
            EClass cls = obj.eClass();
            String key = cls.getEPackage().getNsURI() + "#//" + cls.getName();
            counter.merge(key, 1, Integer::sum);
            n++;
        }
        return n;
    }
}
