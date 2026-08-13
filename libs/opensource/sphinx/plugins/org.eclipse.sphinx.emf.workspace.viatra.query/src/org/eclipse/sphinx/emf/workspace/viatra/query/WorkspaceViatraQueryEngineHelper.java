/**
 * <copyright>
 *
 * Copyright (c) 2014-2020 itemis, IncQuery Labs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [475954] Proxies with fragment-based proxy URIs may get resolved across model boundaries
 *     IncQuery Labs, itemis - [501899] Use base index instead of IncQuery patterns
 *     Elektrobit Automotive GmbH, IncQuery Labs [566004] Use resource filter on Viatra engine
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.viatra.query;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.ScopingResourceSet;
import org.eclipse.sphinx.emf.viatra.query.ViatraQueryEngineHelper;
import org.eclipse.sphinx.emf.workspace.viatra.query.internal.ScopeResourceFilter;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.base.api.BaseIndexOptions;
import org.eclipse.viatra.query.runtime.base.api.filters.IBaseIndexResourceFilter;
import org.eclipse.viatra.query.runtime.emf.EMFScope;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;

public class WorkspaceViatraQueryEngineHelper extends ViatraQueryEngineHelper implements IWorkspaceViatraQueryEngineHelper {

	@Override
	public ViatraQueryEngine getEngine(Resource contextResource) throws ViatraQueryException {
		IModelDescriptor contextModelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextResource);
		if (contextModelDescriptor != null) {
			return getEngine(contextModelDescriptor);
		}
		return getEngine(contextResource, false);
	}

	@Override
	public ViatraQueryEngine getEngine(IModelDescriptor contextModelDescriptor) throws ViatraQueryException {
		if (contextModelDescriptor != null) {
			ResourceSet resourceSet = contextModelDescriptor.getEditingDomain().getResourceSet();
			if (resourceSet instanceof ScopingResourceSet) {
				BaseIndexOptions baseIndexOptions = createBaseIndexOptions(contextModelDescriptor);
				return ViatraQueryEngine.on(new EMFScope(resourceSet, baseIndexOptions));
			}
			return ViatraQueryEngine.on(new EMFScope(resourceSet));
		}
		return null;
	}

	/**
	 * @param contextModelDescriptor
	 *            the model descriptor to be used by the filter
	 * @return a base index options equipped with a {@link IBaseIndexResourceFilter} that accepts only the resources that
	 *         are part of the given <code>contextModelDescriptor</code>
	 */
	private BaseIndexOptions createBaseIndexOptions(final IModelDescriptor contextModelDescriptor) {
		return new BaseIndexOptions().withResourceFilterConfiguration(new ScopeResourceFilter(contextModelDescriptor));
	}
}
