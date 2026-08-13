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
package org.eclipse.sphinx.tests.emf.internal.mocks;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.osgi.framework.Bundle;

public class MetaModelDescriptorExtensionRegistryMockFactory {

	@SuppressWarnings("nls")
	public IExtensionRegistry createExtensionRegistryMock(Plugin contributorPlugin, IMetaModelDescriptor... mmDescriptors) {
		List<IConfigurationElement> configurationElements = new ArrayList<IConfigurationElement>(mmDescriptors.length);

		for (IMetaModelDescriptor mmDescriptor : mmDescriptors) {
			IConfigurationElement configurationElement = createNiceMock(IConfigurationElement.class);
			expect(configurationElement.getName()).andReturn("descriptor");
			expect(configurationElement.getAttribute("id")).andReturn(mmDescriptor.getIdentifier());
			expect(configurationElement.getAttribute("class")).andReturn(mmDescriptor.getClass().getName());
			expect(configurationElement.getContributor()).andReturn(createContributor(contributorPlugin));
			replay(configurationElement);

			configurationElements.add(configurationElement);
		}

		IExtension extension = createNiceMock(IExtension.class);
		expect(extension.getConfigurationElements())
				.andReturn(configurationElements.toArray(new IConfigurationElement[configurationElements.size()])).anyTimes();
		replay(extension);

		IExtensionPoint extensionPoint = createNiceMock(IExtensionPoint.class);
		expect(extensionPoint.getExtensions()).andReturn(new IExtension[] { extension }).anyTimes();
		replay(extensionPoint);

		IExtensionRegistry extensionRegistry = createNiceMock(IExtensionRegistry.class);
		expect(extensionRegistry.getExtensionPoint("org.eclipse.sphinx.emf.metaModelDescriptors")).andReturn(extensionPoint).anyTimes();
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
