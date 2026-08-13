package artop.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

import org.artop.aal.serialization.SerializationFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.XMLSave;

import autosar40.util.Autosar40Package;
import autosar40.util.Autosar40ResourceFactoryImpl;

/**
 * 诊断 NPE 的精确位置
 *
 * 通过自定义 SerializationFactory.factoryImpl，拦截 getQName(EStructuralFeature) 调用，
 * 在 NPE 发生前打印导致问题的 feature 信息。
 */
public class DiagnoseNpe6 {

    // 自定义 helper，继承 AutosarXMLHelperImpl，重写 getQName 拦截 NPE
    public static class DebugHelper extends org.artop.aal.serialization.internal.AutosarXMLHelperImpl {
        public DebugHelper(XMLResource resource) {
            super(resource);
        }

        @Override
        public String getQName(EPackage ePackage, String name) {
            if (ePackage == null) {
                System.out.println("[diag] !!! NPE WARNING: getQName(ePackage=null, name=" + name + ")");
                System.out.println("[diag]   调用栈:");
                for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
                    System.out.println("[diag]     at " + ste);
                }
                // 尝试找出是哪个 feature 导致的
                System.out.println("[diag]   name 参数: " + name);
            }
            return super.getQName(ePackage, name);
        }

        @Override
        public String getQName(EStructuralFeature feature) {
            try {
                // 在调用 super 之前，检查可能导致 NPE 的情况
                String name = extendedMetaData.getName(feature);
                String namespace = extendedMetaData.getNamespace(feature);
                EPackage ePackage = null;
                if (namespace != null) {
                    ePackage = extendedMetaData.getPackage(namespace);
                    if (ePackage == null) {
                        System.out.println("[diag] !!! WARNING: namespace='" + namespace + "' but getPackage returned null");
                        System.out.println("[diag]   feature: " + feature.getName() + " (" + feature.getClass().getName() + ")");
                        EClass ec = feature.getEContainingClass();
                        System.out.println("[diag]   containingClass: " + (ec != null ? ec.getName() : "null"));
                        if (ec != null) {
                            System.out.println("[diag]   containingClass.pkg: " + (ec.getEPackage() != null ? ec.getEPackage().getName() + " (" + ec.getEPackage().getNsURI() + ")" : "null"));
                        }
                    }
                } else {
                    EClass ec = feature.getEContainingClass();
                    if (ec == null) {
                        System.out.println("[diag] !!! WARNING: namespace=null and containingClass=null");
                        System.out.println("[diag]   feature: " + feature.getName() + " (" + feature.getClass().getName() + ")");
                    } else if (ec.getEPackage() == null) {
                        System.out.println("[diag] !!! WARNING: namespace=null and containingClass.pkg=null");
                        System.out.println("[diag]   feature: " + feature.getName() + " (" + feature.getClass().getName() + ")");
                        System.out.println("[diag]   containingClass: " + ec.getName());
                    }
                }
                return super.getQName(feature);
            } catch (NullPointerException npe) {
                System.out.println("[diag] !!! NPE in getQName(feature)");
                System.out.println("[diag]   feature: " + feature);
                System.out.println("[diag]   feature.name: " + feature.getName());
                System.out.println("[diag]   feature.class: " + feature.getClass().getName());
                EClass ec = feature.getEContainingClass();
                System.out.println("[diag]   feature.containingClass: " + (ec != null ? ec.getName() : "null"));
                if (ec != null) {
                    EPackage ep = ec.getEPackage();
                    System.out.println("[diag]   containingClass.pkg: " + (ep != null ? ep.getName() + " (" + ep.getNsURI() + ")" : "null"));
                }
                try {
                    String ns = extendedMetaData.getNamespace(feature);
                    System.out.println("[diag]   emd.namespace(feature): " + ns);
                    if (ns != null) {
                        EPackage pkg = extendedMetaData.getPackage(ns);
                        System.out.println("[diag]   emd.getPackage(ns): " + pkg);
                    }
                    System.out.println("[diag]   emd.name(feature): " + extendedMetaData.getName(feature));
                    System.out.println("[diag]   emd.featureKind(feature): " + extendedMetaData.getFeatureKind(feature));
                } catch (Exception ex) {
                    System.out.println("[diag]   Cannot get emd info: " + ex);
                }
                throw npe;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String inputDir = "/workspace/decompiler/autosar448/model/library";
        String outputDir = "/workspace/artop_demo/output_diag";

        // 初始化
        System.out.println("[diag] Initializing Autosar40Package...");
        Autosar40Package.eINSTANCE.getClass();

        // 替换 SerializationFactory.factoryImpl，用自定义的 helper 拦截 getQName
        System.out.println("[diag] Installing custom SerializationFactory...");
        SerializationFactory.factoryImpl = new SerializationFactory.ISerializationFactoryImpl() {
            @Override
            public XMLHelper createXMLHelper(XMLResource resource) {
                return new DebugHelper(resource);
            }

            @Override
            public XMLLoad createXMLLoad(EPackage basePackage, XMLResource resource) {
                // 用原始 factory 创建 load，但替换 helper
                XMLHelper helper = new DebugHelper(resource);
                try {
                    Class<?> loadClass = Class.forName("org.artop.aal.serialization.internal.AutosarXMLLoadImpl");
                    java.lang.reflect.Constructor<?> ctor = loadClass.getConstructor(XMLHelper.class, EPackage.class);
                    return (XMLLoad) ctor.newInstance(helper, basePackage);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public XMLSave createXMLSave(XMLResource resource) {
                // 用原始 factory 创建 save，但替换 helper
                XMLHelper helper = new DebugHelper(resource);
                try {
                    Class<?> saveClass = Class.forName("org.artop.aal.serialization.internal.AutosarXMLSaveImpl");
                    java.lang.reflect.Constructor<?> ctor = saveClass.getConstructor(XMLHelper.class);
                    return (XMLSave) ctor.newInstance(helper);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        // 创建 ResourceFactory
        Autosar40ResourceFactoryImpl factory = new Autosar40ResourceFactoryImpl();

        // 列出所有 arxml 文件
        File[] arxmlFiles = new File(inputDir).listFiles((dir, name) -> name.endsWith(".arxml"));
        if (arxmlFiles == null || arxmlFiles.length == 0) {
            System.err.println("[diag] No arxml files found in: " + inputDir);
            System.exit(1);
        }
        System.out.println("[diag] Found " + arxmlFiles.length + " arxml files");

        new File(outputDir).mkdirs();

        // 只处理第一个文件
        File arxmlFile = arxmlFiles[0];
        String fileName = arxmlFile.getName();
        System.out.println("[diag] Processing: " + fileName);

        try {
            // 反序列化
            URI uri = URI.createFileURI(arxmlFile.getAbsolutePath());
            Resource resource = factory.createResource(uri);

            try (FileInputStream fis = new FileInputStream(arxmlFile)) {
                resource.load(fis, null);  // 使用 null，让 resource 用默认 options
            }

            System.out.println("[diag]   Loaded: " + resource.getContents().size() + " root objects, " +
                resource.getErrors().size() + " errors, " + resource.getWarnings().size() + " warnings");

            // 序列化回去
            File outputFile = new File(outputDir, fileName);
            System.out.println("[diag]   Saving to: " + outputFile.getAbsolutePath());

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                resource.save(fos, null);  // 使用 null，让 resource 用默认 options
            }

            System.out.println("[diag]   Saved: " + outputFile.length() + " bytes");
            System.out.println("[diag] SUCCESS!");
        } catch (Exception e) {
            System.out.println("[diag] FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
