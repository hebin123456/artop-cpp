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
package org.eclipse.sphinx.tests.emf.viatra.query;

import java.lang.management.ManagementFactory;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.services.DefaultMetaModelServiceProvider;
import org.eclipse.sphinx.emf.query.IModelQueryService;
import org.eclipse.sphinx.emf.resource.ScopingResourceSetImpl;
import org.eclipse.sphinx.testutils.AbstractTestCase;

public abstract class AbstractViatraQueryTestCase extends AbstractTestCase {

	private ScopingResourceSetImpl resourceSet;
	protected ResourceFactoryImpl resourceFactory;

	public AbstractViatraQueryTestCase(ResourceFactoryImpl resourceFactory) {
		this.resourceFactory = resourceFactory;
	}

	protected IModelQueryService getModelQueryService(IMetaModelDescriptor mmDescriptor) {
		return new DefaultMetaModelServiceProvider().getService(mmDescriptor, org.eclipse.sphinx.emf.query.IModelQueryService.class);
	}

	@Override
	protected ScopingResourceSetImpl createDefaultResourceSet() {
		if (resourceSet == null) {
			resourceSet = new ScopingResourceSetImpl();
		}
		return resourceSet;
	}

	protected ScopingResourceSetImpl getResourceSet() {
		if (resourceSet == null) {
			return createDefaultResourceSet();
		}
		return resourceSet;
	}

	protected long getCurrentThreadCpuTime() {
		return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
	}

	protected EObject loadInputFile(String fileName) throws Exception {
		return loadInputFile(fileName, resourceFactory, null);
	}

	@Override
	protected Plugin getTestPlugin() {
		return org.eclipse.sphinx.tests.emf.viatra.query.internal.Activator.getPlugin();
	}

	protected void saveWorkingFile(String fileName, EObject modelRoot) throws Exception {
		saveWorkingFile(fileName, modelRoot, resourceFactory, null);
	}

	protected EObject loadWorkingFile(String fileName) throws Exception {
		return loadWorkingFile(fileName, resourceFactory, null);
	}
}
