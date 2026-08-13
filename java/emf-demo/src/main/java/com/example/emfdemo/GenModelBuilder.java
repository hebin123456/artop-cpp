package com.example.emfdemo;

import org.eclipse.emf.codegen.ecore.genmodel.GenJDKLevel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelFactory;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import java.io.File;

/**
 * 为 Library 元模型构造一个对应的 EMF GenModel，并将其序列化为 .genmodel 文件。
 *
 * .genmodel 是 EMF 的代码生成模型：用户在 Eclipse IDE 中打开后，可通过
 *   "Generate -> Model Code" 一键生成 Java 接口/实现代码；
 *   还可以继续生成 Edit、Editor、Test 等工程。
 *
 * 本 Demo 仅生成 .genmodel 文件本身（不直接调用 JET 模板引擎），
 * 因为 EMF 模板引擎在 Eclipse OSGi 容器外运行需要较多配置；
 * 生成的 .genmodel 在 Eclipse 中是可用的。
 */
public final class GenModelBuilder {

    private GenModelBuilder() {}

    public static GenModel buildAndSave(EPackage libraryPkg, String ecoreFilePath, String genModelFilePath) {
        // 0) 确保 GenModel 元模型自身已注册
        ResourceSet rs = newResourceSet();
        EPackage.Registry.INSTANCE.put(GenModelPackage.eNS_URI, GenModelPackage.eINSTANCE);
        // 兼容 EMF 旧实现
        try {
            GenModelPackage genPkg = GenModelPackage.eINSTANCE;
            if (genPkg != null) {
                EPackage.Registry.INSTANCE.put(genPkg.getNsURI(), genPkg);
            }
        } catch (Throwable ignored) {}

        // 1) 创建 GenModel 根
        GenModel genModel = GenModelFactory.eINSTANCE.createGenModel();
        genModel.setModelDirectory("/" + new File(new File(genModelFilePath).getParent(), "src")
                .getAbsolutePath().replace(File.separatorChar, '/') + "/");
        // GenJDKLevel.JDK170_LITERAL = JDK 17
        // 注：setGenJDKLevel 在 2.27 之前的版本不存在；只有 ComplianceLevel
        genModel.setForceOverwrite(true);
        genModel.setCanGenerate(true);
        genModel.setBundleManifest(false);
        genModel.setUpdateClasspath(false);
        genModel.setGenerateSchema(false);
        genModel.setNonNLSMarkers(false);

        // 2) 创建 GenPackage 把 EPackage 包起来
        GenPackage genPackage = GenModelFactory.eINSTANCE.createGenPackage();
        genPackage.setEcorePackage(libraryPkg);
        genPackage.setBasePackage("com.example.emfdemo.model");
        genPackage.setPrefix("Library");
        genPackage.setFileExtensions("ecore");
        genModel.getGenPackages().add(genPackage);

        // 3) 保存 .genmodel（标准 XMI 资源）
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("genmodel", new XMIResourceFactoryImpl());
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("ecore", new EcoreResourceFactoryImpl());

        Resource res = rs.createResource(URI.createFileURI(genModelFilePath));
        res.getContents().add(genModel);
        try {
            res.save(null);
            System.out.println("[GenModelBuilder] Saved GenModel to: " + genModelFilePath);
            System.out.println("[GenModelBuilder] 在 Eclipse 中打开该 .genmodel 后，");
            System.out.println("[GenModelBuilder] 可执行 'Generate Model Code' 生成 Java 模型代码。");
        } catch (Exception e) {
            throw new RuntimeException("Failed to save .genmodel", e);
        }
        return genModel;
    }

    private static ResourceSet newResourceSet() {
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("ecore", new EcoreResourceFactoryImpl());
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("xmi", new XMIResourceFactoryImpl());
        return rs;
    }
}
