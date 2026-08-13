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
package org.eclipse.sphinx.jdt.util

import java.net.URLClassLoader
import java.util.ArrayList
import java.util.Arrays
import org.eclipse.sphinx.jdt.loaders.ProjectClassLoader

class ClassLoaderExtensions {

	static def void printHierarchy(ClassLoader classLoader) {
		val classLoaderHierarchy = new ArrayList<ClassLoader>();
		classLoaderHierarchy.add(classLoader)

		var parentClassLoader = classLoader.parent
		while (parentClassLoader != null) {
			classLoaderHierarchy.add(parentClassLoader)
			parentClassLoader = parentClassLoader.parent
		}

		classLoaderHierarchy.reverse
		classLoaderHierarchy.forEach[print(it)]
	}

	static def void print(ClassLoader classLoader) {
		var String classLoaderAsString
		if (classLoader instanceof ProjectClassLoader) {
			classLoaderAsString = classLoader.toString
		}
		else if (classLoader instanceof URLClassLoader) {
			classLoaderAsString = classLoader.getClass().name + " [urls=" + Arrays.toString(classLoader.URLs) + "]"
		} else {
			classLoaderAsString = classLoader.toString
		}

		println(classLoaderAsString)
	}
}
