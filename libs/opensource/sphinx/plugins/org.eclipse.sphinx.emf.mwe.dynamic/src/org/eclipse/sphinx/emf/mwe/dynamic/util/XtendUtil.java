/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.mwe.dynamic.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.sphinx.emf.mwe.dynamic.internal.Activator;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.xtend.core.xtend.XtendClass;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration;

@SuppressWarnings("restriction")
public class XtendUtil {

	public static boolean isJavaProject(IProject project) {
		Assert.isNotNull(project);

		try {
			return project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
		return false;
	}

	public static IJavaProject getJavaProject(IProject project) {
		Assert.isNotNull(project);

		if (isJavaProject(project)) {
			return JavaCore.create(project);
		}
		return null;
	}

	public static boolean isJavaFile(IFile file) {
		return file != null && IXtendConstants.JAVA_FILE_EXTENSION.equals(file.getFileExtension());
	}

	public static boolean isXtendFile(IFile file) {
		return file != null && IXtendConstants.XTEND_FILE_EXTENSION.equals(file.getFileExtension());
	}

	public static boolean isXtendResource(URI uri) {
		return uri != null && IXtendConstants.XTEND_FILE_EXTENSION.equals(uri.fileExtension());
	}

	public static IPath getJavaPath(IPath xtendOrJavaPath) {
		Assert.isNotNull(xtendOrJavaPath);

		IPath javaPath = new Path(""); //$NON-NLS-1$
		for (String segment : xtendOrJavaPath.segments()) {
			if (segment.equals(IXtendConstants.JAVA_SRC_FOLDER_NAME)) {
				javaPath = javaPath.append(IXtendConstants.XTEND_GEN_FOLDER_NAME);
			} else {
				javaPath = javaPath.append(segment);
			}
		}
		return javaPath.removeFileExtension().addFileExtension(IXtendConstants.JAVA_FILE_EXTENSION);
	}

	public static IFile getJavaFile(IPath xtendOrJavaPath) {
		IPath javaPath = getJavaPath(xtendOrJavaPath);
		return ResourcesPlugin.getWorkspace().getRoot().getFile(javaPath);
	}

	public static IFile getJavaFile(IFile xtendOrJavaFile, String className) {
		// If it is a Java file then just return it
		if (isJavaFile(xtendOrJavaFile)) {
			return xtendOrJavaFile;
		}

		// Xtend file
		if (isXtendFile(xtendOrJavaFile)) {
			// Xtend file contains one class (default) - retrieve corresponding generated Java file
			if (xtendOrJavaFile.getFullPath().removeFileExtension().lastSegment().equals(className)) {
				return getJavaFile(xtendOrJavaFile.getFullPath());
			} else {
				// Xtend file contains multiple classes - retrieve generated Java file for given class name
				IPath virtualXtendPath = xtendOrJavaFile.getFullPath().removeLastSegments(1).append(className)
						.addFileExtension(IXtendConstants.JAVA_FILE_EXTENSION);
				return getJavaFile(virtualXtendPath);
			}
		}

		return null;
	}

	public static IJavaElement getJavaElement(IFile xtendOrJavaFile) {
		IFile javaFile = XtendUtil.getJavaFile(xtendOrJavaFile.getFullPath());
		IJavaProject javaProject = JavaCore.create(javaFile.getProject());
		try {
			return javaProject.findElement(javaFile.getProjectRelativePath().removeFirstSegments(1));
		} catch (JavaModelException ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			return null;
		}
	}

	public static List<String> getAvailableClassNames(IFile xtendFile) {
		List<String> availableClassNames = new ArrayList<String>(1);
		URI uri = EcorePlatformUtil.createURI(xtendFile.getFullPath());
		Resource resource = EcoreResourceUtil.loadResource(null, uri, null);
		if (resource != null) {
			for (EObject object : resource.getContents()) {
				if (object instanceof XtendFile) {
					List<XtendTypeDeclaration> xtendTypes = ((XtendFile) object).getXtendTypes();
					for (XtendTypeDeclaration declaration : xtendTypes) {
						if (declaration instanceof XtendClass) {
							XtendClass clazz = (XtendClass) declaration;
							availableClassNames.add(clazz.getName());
						}
					}
				}
			}
		}
		return availableClassNames;
	}
}
