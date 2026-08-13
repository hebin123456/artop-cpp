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

import java.security.SecureClassLoader
import java.util.Arrays
import java.util.List
import org.eclipse.core.runtime.Assert
import org.osgi.framework.Bundle
import org.osgi.framework.wiring.BundleWiring

class DelegatingCompositeBundleClassLoader extends SecureClassLoader {

	private List<Bundle> bundles
	private List<ClassLoader> bundleClassLoaders = null

	new(ClassLoader parent, List<Bundle> bundles) {
		super(parent);

		Assert.isNotNull(bundles);
		this.bundles = bundles;
	}

	public def List<ClassLoader> getBundleClassLoaders() {
		if (bundleClassLoaders == null) {
			// Retrieve original bundle class loaders for give set of bundles
			bundleClassLoaders = bundles.map[adapt(BundleWiring)].filterNull.map[classLoader].toList
		}
		bundleClassLoaders
	}

	override protected findClass(String name) throws ClassNotFoundException {
		for (ClassLoader bundleClassLoader : getBundleClassLoaders()) {
			try {
				val clazz = bundleClassLoader.loadClass(name)
				return clazz;
			} catch (Throwable ex) {
				// Do nothing, try next bundle class loader
			}
		}
		throw new ClassNotFoundException(name);
	}

	override toString() {
		class.name + " [bundles=" + Arrays.toString(bundles.toArray) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
