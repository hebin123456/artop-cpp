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
package org.eclipse.sphinx.tests.emf.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.tests.emf.internal.Activator;
import org.eclipse.sphinx.tests.emf.internal.mocks.MetaModelDescriptorExtensionRegistryMockFactory;
import org.eclipse.sphinx.tests.emf.metamodel.descs.Test1MM;
import org.eclipse.sphinx.tests.emf.metamodel.descs.Test1Release100;
import org.eclipse.sphinx.tests.emf.metamodel.descs.Test2MM;
import org.eclipse.sphinx.tests.emf.metamodel.descs.Test2Release100;
import org.junit.AfterClass;
import org.junit.Test;

public class WorkspaceEditingDomainUtilTest {

	private MetaModelDescriptorExtensionRegistryMockFactory mockFactory = new MetaModelDescriptorExtensionRegistryMockFactory();

	@AfterClass
	public static void tearDown() throws Exception {
		MetaModelDescriptorRegistry.INSTANCE.setExtensionRegistry(null);
	}

	private void initMetaModelDescriptorRegistryWith(IMetaModelDescriptor... mmDescriptors) {
		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(), mmDescriptors);
		MetaModelDescriptorRegistry.INSTANCE.setExtensionRegistry(extensionRegistry);
	}

	/**
	 * Test method for {@link EcoreResourceUtil#readSchemaLocationEntries(Resource)}
	 */
	@Test
	public void testGetEditingDomainsFromScheme() {

		// Register one meta model
		initMetaModelDescriptorRegistryWith(Test1MM.INSTANCE);
		List<TransactionalEditingDomain> expectedEditingDomains = new ArrayList<TransactionalEditingDomain>();
		// Get editing domains that use URIs with given "tr1" scheme in cross-document references and as proxy URIs.
		org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI.createURI("tr1:/#test1Release.sphinx.org/scheme", true); //$NON-NLS-1$
		String uriScheme = uri.scheme();
		assertEquals(uriScheme, "tr1"); //$NON-NLS-1$
		Collection<TransactionalEditingDomain> actualEditingDomains = WorkspaceEditingDomainUtil.getEditingDomains(uriScheme);
		expectedEditingDomains.add(WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(), Test1MM.INSTANCE));
		assertEquals(actualEditingDomains.size(), 1);
		assertEquals(expectedEditingDomains, actualEditingDomains);

		// Register four meta models
		initMetaModelDescriptorRegistryWith(Test1MM.INSTANCE, Test1Release100.INSTANCE, Test2MM.INSTANCE, Test2Release100.INSTANCE);
		// Get editing domains that use URIs with given "tr2" scheme in cross-document references and as proxy URIs.
		uri = org.eclipse.emf.common.util.URI.createURI("tr2:/#test2Release100.sphinx.org/scheme", true); //$NON-NLS-1$
		uriScheme = uri.scheme();
		assertEquals(uriScheme, "tr2"); //$NON-NLS-1$
		actualEditingDomains = WorkspaceEditingDomainUtil.getEditingDomains(uriScheme);
		assertEquals(actualEditingDomains.size(), 2);
		expectedEditingDomains.clear();
		expectedEditingDomains.add(WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(), Test2MM.INSTANCE));
		expectedEditingDomains.add(WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(), Test2Release100.INSTANCE));
		assertEquals(expectedEditingDomains, actualEditingDomains);
	}
}
