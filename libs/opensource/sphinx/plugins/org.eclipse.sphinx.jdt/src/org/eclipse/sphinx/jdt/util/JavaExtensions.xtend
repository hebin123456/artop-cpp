/**
 * <copyright>
 *
 * Copyright (c) 2014-2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [503063] Provide launching support for Sphinx Workflows
 *
 * </copyright>
 */
package org.eclipse.sphinx.jdt.util

import java.io.File
import java.io.IOException
import java.net.URL
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Assert
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Platform
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.IVMInstall
import org.eclipse.jdt.launching.IVMInstall2
import org.eclipse.jdt.launching.JavaRuntime
import org.eclipse.osgi.util.NLS
import org.eclipse.sphinx.jdt.internal.Activator
import org.eclipse.sphinx.jdt.internal.messages.Messages
import org.eclipse.sphinx.platform.util.ExtendedPlatform
import org.eclipse.sphinx.platform.util.PlatformLogUtil
import org.eclipse.sphinx.platform.util.StatusUtil
import org.osgi.framework.Bundle
import java.util.regex.Pattern

class JavaExtensions {

	// Ensure backward compatibility with Eclipse 4.3.x (Kepler) and earlier
	public static final String VERSION_1_8 = "1.8"; //$NON-NLS-1$

	public static final String DEFAULT_OUTPUT_FOLDER_NAME = "bin"

	// See http://stackoverflow.com/a/18667639 for details
	public static final Pattern CLASS_NAME_PATTERN = Pattern.compile("^([a-z][a-z_0-9]*\\.)*[A-Z_]($[A-Z_]|[\\w_])*$");

	private static final String PLUGIN_ID_VERSION_SEPARATOR = "_"; //$NON-NLS-1$

	static def File getFile(IClasspathEntry entry) {
		if (entry.path.toFile.exists) {
			entry.path.toFile
		} else {
			ResourcesPlugin.getWorkspace.root.location.append(entry.path).toFile
		}
	}

	static def IJavaProject getJavaProject(String projectName) {
		JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName))
	}

	/**
	 * Returns the absolute path in the local file system corresponding to given workspace-relative path.
	 *
	 * @param workspacePath the workspace-relative path to some resource in the workspace
	 *
	 * @return the absolute path in the local file system corresponding to given <code>workspacePath</code>, or null if no path can be determined
	 */
	static def IPath getLocation(IPath workspacePath) {
		ResourcesPlugin.getWorkspace().getRoot().findMember(workspacePath)?.getLocation()
	}

	/**
	 * Returns the plug-in installation location. It is composed of the Platform's installation location
	 * followed by the name of the "plugins" folder inside it.
	 *
	 * @return The plug-in installation location.
	 */
	static def IPath getPluginInstallLocation() {
		new Path(Platform.getInstallLocation().getURL().getPath()).append("plugins")
	}

	/**
	 * Returns the common postfix of the classpath root locations of "dev mode" plug-ins. It is equal to the name
	 * of the default output folder ("bin") in Java projects.
	 *
	 * @return The common classpath root location postfix of "dev" mode plug-ins.
	 */
	static def String getDevModePluginClasspathRootLocationPostfix() {
		JavaExtensions.DEFAULT_OUTPUT_FOLDER_NAME
	}

	static def boolean isInstalledPluginClasspathRootLocationOf(String pluginId, IPath classpathLocation) {
		Assert.isNotNull(classpathLocation)

		pluginInstallLocation.isPrefixOf(classpathLocation) && classpathLocation.lastSegment.split(PLUGIN_ID_VERSION_SEPARATOR).get(0).equals(pluginId)
	}

	static def boolean isDevModePluginClasspathLocationOf(String pluginId, IPath classpathLocation) {
		Assert.isNotNull(classpathLocation)

		classpathLocation.toString().contains(new Path(pluginId).append(devModePluginClasspathRootLocationPostfix).toString())
	}

	static def Bundle getBundle(URL pluginClasspathRootLocationURL) {
		Assert.isNotNull(pluginClasspathRootLocationURL)

		var IPath pluginClasspathRootLocation = new Path(pluginClasspathRootLocationURL.path)

		// Installed plug-in classpath?
		if (pluginInstallLocation.isPrefixOf(pluginClasspathRootLocation)) {
			// Retrieve corresponding bundle
			return Activator.context.getBundle("reference:" + pluginClasspathRootLocationURL)
		}

		// "Dev mode" plug-in classpath?
		if (pluginClasspathRootLocation.lastSegment.equals(devModePluginClasspathRootLocationPostfix)) {
			// Remove "bin" postfix from classpath root location and retrieve corresponding bundle
			return Activator.context.getBundle("reference:" + pluginClasspathRootLocation.removeLastSegments(1).toFile.toURI.toURL)
		}

		null;
	}

	static def IPath getPluginClasspathRootLocation(String pluginId) {
		// Retrieve bundle behind given plug-in id
		val Bundle bundle = ExtendedPlatform.loadBundle(pluginId);
		if (bundle != null) {
			try {
				// Retrieve and return the bundle's classpath root location
				val URL classpathRootURL = bundle.getResource("/"); //$NON-NLS-1$
				val URL resolvedClasspathRootURL = FileLocator.resolve(classpathRootURL);
				return new Path(resolvedClasspathRootURL.getPath()).removeTrailingSeparator();
			} catch (IOException ex) {
				val IStatus status = StatusUtil.createErrorStatus(Activator.getDefault, ex);
				Activator.getDefault.log.log(status);
			}
		}
		return null;
	}

	static def void validateCompilerCompliance(String compliance) {
		val IVMInstall install = JavaRuntime.getDefaultVMInstall()
		if (install instanceof IVMInstall2) {
			val String compilerCompliance = getCompilerCompliance(install, compliance)

			// Compliance to set must be equal or less than compliance level from VMInstall.
			if (!compilerCompliance.equals(compliance)) {
				try {
					val float complianceToSet = Float.parseFloat(compliance)
					val float complianceFromVM = Float.parseFloat(compilerCompliance)
					Assert.isLegal(complianceToSet <= complianceFromVM,
						NLS.bind(Messages.error_JRECompliance_NotCompatible, compliance, compilerCompliance))
				} catch (NumberFormatException ex) {
					PlatformLogUtil.logAsWarning(Activator.getDefault(), ex)
				}
			}
		}
	}

	private static def String getCompilerCompliance(IVMInstall2 vmInstall, String defaultCompliance) {
		Assert.isNotNull(vmInstall)

		val String version = vmInstall.getJavaVersion();
		if (version == null) {
			return defaultCompliance;
		} else if (version.startsWith(VERSION_1_8)) {
			return VERSION_1_8;
		} else if (version.startsWith(JavaCore.VERSION_1_7)) {
			return JavaCore.VERSION_1_7;
		} else if (version.startsWith(JavaCore.VERSION_1_6)) {
			return JavaCore.VERSION_1_6;
		} else if (version.startsWith(JavaCore.VERSION_1_5)) {
			return JavaCore.VERSION_1_5;
		} else if (version.startsWith(JavaCore.VERSION_1_4)) {
			return JavaCore.VERSION_1_4;
		} else if (version.startsWith(JavaCore.VERSION_1_3)) {
			return JavaCore.VERSION_1_3;
		} else if (version.startsWith(JavaCore.VERSION_1_2)) {
			return JavaCore.VERSION_1_3;
		} else if (version.startsWith(JavaCore.VERSION_1_1)) {
			return JavaCore.VERSION_1_3;
		}
		return defaultCompliance;
	}
}
