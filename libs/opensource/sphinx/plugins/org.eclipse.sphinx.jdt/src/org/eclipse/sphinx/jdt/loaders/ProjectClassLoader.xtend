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
package org.eclipse.sphinx.jdt.loaders

import java.net.URL
import java.net.URLClassLoader
import java.util.List
import org.eclipse.core.runtime.Assert
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.osgi.framework.Bundle

import static extension org.eclipse.sphinx.jdt.util.JavaExtensions.*

class ProjectClassLoader extends URLClassLoader {

	private IJavaProject javaProject;

	protected static def URL[] getJavaOutputURLs(IJavaProject javaProject) {
		Assert.isNotNull(javaProject)
		Assert.isLegal(javaProject.exists && javaProject.isOpen)

		// Retrieve and return URL of absolute file system location behind default output location of given Java project
		val outputURL = javaProject.outputLocation.location?.toFile?.toURI?.toURL
		if(outputURL != null) #[outputURL] else #[]
	}

	protected static def ClassLoader hookupDependencyClassLoaders(IJavaProject javaProject, ClassLoader parent) {
		Assert.isNotNull(javaProject)
		Assert.isLegal(javaProject.exists && javaProject.isOpen)

		var ClassLoader lastClassLoader = parent

		// Create class loader for required other projects in the workspace and add it to parent class loader hierarchy
		val entries = javaProject.getResolvedClasspath(true)
		val Iterable<String> requiredProjectNames = entries.filter[entryKind == IClasspathEntry.CPE_PROJECT].map[
			path.segment(0)]
		for (requiredProjectName : requiredProjectNames) {
			lastClassLoader = new ProjectClassLoader(requiredProjectName.javaProject, lastClassLoader)
		}

		// Create class loaders for required plug-ins and Java libraries and add them to parent class loader hierarchy
		val libraryURLs = entries.filter[entryKind == IClasspathEntry.CPE_LIBRARY].map[file].filter[exists].map[
			toURI.toURL]

		val javaLibraryURLs = newArrayList()
		val List<Bundle> requiredBundles = newArrayList();
		for (URL libraryURL : libraryURLs) {
			// See if current library a required plug-in or an ordinary Java library and keep track of them as such
			val Bundle requiredBundle = libraryURL.bundle
			if (requiredBundle != null) {
				requiredBundles += requiredBundle
			} else {
				javaLibraryURLs += libraryURL
			}
		}

		// Create and use delegating composite bundle class loader for required plug-ins
		if (!requiredBundles.empty) {
			lastClassLoader = new DelegatingCompositeBundleClassLoader(lastClassLoader, requiredBundles)
		}

		// Create and use URL class loader for Java libraries
		if (!javaLibraryURLs.empty) {
			lastClassLoader = new URLClassLoader(javaLibraryURLs, lastClassLoader)
		}

		lastClassLoader
	}

	new(IJavaProject javaProject) {
		this(javaProject, Thread.currentThread().getContextClassLoader())
	}

	new(IJavaProject javaProject, ClassLoader parent) {
		super(getJavaOutputURLs(javaProject), hookupDependencyClassLoaders(javaProject, parent))
		this.javaProject = javaProject
	}

	override protected loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// Class to be loaded located in Java project behind this project class loader?
		val type = javaProject.findType(name)
		if (type != null && !type.binary && javaProject.path.isPrefixOf(type.path)) {
			// Make sure that given class gets loaded by this project class loader, i.e. don't consult the
			// parent class loader hierarchy before
			/*
			 * !! Important Note !! This is necessary to ensure that classes from projects in the runtime workspace
			 * take precedence over the equally named classes in installed plug-ins or "dev mode" plug-ins.
			 */
			return findClass(name);
		}

		// Load all other classes normally
		super.loadClass(name, resolve)
	}

	override toString() {
		class.name + " [project=" + javaProject.project.name + "]";
	}
	
	public def IJavaProject getProject() {
		javaProject
	}
}
