/**
 * <copyright>
 *
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.workspace.domain;

import junit.framework.TestCase;

import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.workspace.domain.WorkspaceEditingDomainManager;
import org.eclipse.sphinx.emf.workspace.domain.factory.IExtendedTransactionalEditingDomainFactory;
import org.eclipse.sphinx.emf.workspace.domain.mapping.IWorkspaceEditingDomainMapping;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;

public class WorkspaceEditingDomainManagerTest extends TestCase {

	private WorkspaceEditingDomainManager editingDomainManager = WorkspaceEditingDomainManager.INSTANCE;

	/**
	 * Test method for {@link WorkspaceEditingDomainManager#getEditingDomainMapping()}
	 */
	public void testGetEditingDomainMapping() {
		IWorkspaceEditingDomainMapping editingDomainMapping = editingDomainManager.getEditingDomainMapping();
		assertNotNull(editingDomainMapping);
	}

	/**
	 * Test method for {@link WorkspaceEditingDomainManager#getEditingDomainFactory(MetaModelDescriptor))}
	 */
	public void testGetEditingDomainFactory() {
		IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
		IExtendedTransactionalEditingDomainFactory editingDomainFactory = editingDomainManager.getEditingDomainFactory(mmDescriptor);
		assertNotNull(editingDomainFactory);
	}
}
