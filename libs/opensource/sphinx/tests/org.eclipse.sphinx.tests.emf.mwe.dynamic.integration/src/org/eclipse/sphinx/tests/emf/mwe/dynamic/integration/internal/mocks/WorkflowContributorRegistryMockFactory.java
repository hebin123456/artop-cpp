/**
 * <copyright>
 *
 * Copyright (c) 2013 itemis and others.
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
package org.eclipse.sphinx.tests.emf.mwe.dynamic.integration.internal.mocks;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.osgi.framework.Bundle;

public class WorkflowContributorRegistryMockFactory {

	@SuppressWarnings("nls")
	public IExtensionRegistry createExtensionRegistryMock(Plugin contributorPlugin, String... pluginIds) {
		List<IConfigurationElement> configurationElements = new ArrayList<IConfigurationElement>(pluginIds.length);

		for (String pluginId : pluginIds) {
			IConfigurationElement configurationElement = createNiceMock(IConfigurationElement.class);
			expect(configurationElement.getName()).andReturn("contributor");
			expect(configurationElement.getAttribute("pluginId")).andReturn(pluginId);
			expect(configurationElement.getContributor()).andReturn(createContributor(contributorPlugin));
			replay(configurationElement);

			configurationElements.add(configurationElement);
		}

		IExtensionRegistry extensionRegistry = createNiceMock(IExtensionRegistry.class);
		expect(extensionRegistry.getConfigurationElementsFor("org.eclipse.sphinx.emf.mwe.dynamic.workflowContributors")).andReturn(
				configurationElements.toArray(new IConfigurationElement[configurationElements.size()])).anyTimes();
		replay(extensionRegistry);

		return extensionRegistry;
	}

	private IContributor createContributor(Plugin contributorPlugin) {
		Bundle contributorBundle = Platform.getBundle(contributorPlugin.getBundle().getSymbolicName());
		String contributorId = Long.toString(contributorBundle.getBundleId());
		String contributorName = contributorBundle.getSymbolicName();
		return new RegistryContributor(contributorId, contributorName, null, null);
	}
}
