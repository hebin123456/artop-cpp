/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.tests.jdt.integration.loaders.mock;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

public class ClassLoaderMockFactory {

	public static final ClassLoaderMockFactory INSTANCE = new ClassLoaderMockFactory();

	private ClassLoaderMockFactory() {
	}

	@SuppressWarnings("nls")
	public List<Bundle> createPluginRequiredBundlesMock(String... pluginIds) throws Exception {
		List<Bundle> requiredBundles = new ArrayList<Bundle>(pluginIds.length);

		for (String dependency : pluginIds) {
			Bundle bundle = createNiceMock(Bundle.class);
			expect(bundle.getSymbolicName()).andReturn(dependency);
			BundleWiring bundleWiring = createNiceMock(BundleWiring.class);
			expect(bundleWiring.getClassLoader()).andReturn(new URLClassLoader(new URL[] { new URL("platform:/plugin/" + dependency) }));
			expect(bundle.adapt(BundleWiring.class)).andReturn(bundleWiring);
			replay(bundleWiring, bundle);

			requiredBundles.add(bundle);
		}

		return requiredBundles;
	}

}
