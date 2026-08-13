/**
 * <copyright>
 *
 * Copyright (c) 2015-2017 itemis, IncQuery Labs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     IncQuery Labs, itemis - [501899] Use base index instead of IncQuery patterns
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.search.ui.viatra.query.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.services.AbstractMetaModelService;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.search.ui.ModelSearchMatch;
import org.eclipse.sphinx.emf.search.ui.QuerySpecification;
import org.eclipse.sphinx.emf.search.ui.services.IModelSearchService;
import org.eclipse.sphinx.emf.search.ui.viatra.query.internal.Activator;
import org.eclipse.sphinx.emf.workspace.viatra.query.IWorkspaceViatraQueryEngineHelper;
import org.eclipse.sphinx.emf.workspace.viatra.query.WorkspaceViatraQueryEngineHelper;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;

public abstract class AbstractViatraQueryModelSearchService extends AbstractMetaModelService implements IModelSearchService {

	private IWorkspaceViatraQueryEngineHelper workspaceViatraQueryEngineHelper;

	public AbstractViatraQueryModelSearchService(Collection<IMetaModelDescriptor> mmDescriptors) {
		super(mmDescriptors);
	}

	protected IWorkspaceViatraQueryEngineHelper getWorkspaceViatraQueryEngineHelper() {
		if (workspaceViatraQueryEngineHelper == null) {
			workspaceViatraQueryEngineHelper = createWorkspaceViatraQueryEngineHelper();
		}
		return workspaceViatraQueryEngineHelper;
	}

	protected IWorkspaceViatraQueryEngineHelper createWorkspaceViatraQueryEngineHelper() {
		return new WorkspaceViatraQueryEngineHelper();
	}

	protected abstract List<ModelSearchMatch> getMatches(QuerySpecification querySpec, ViatraQueryEngine engine);

	@Override
	public List<ModelSearchMatch> getMatches(Collection<Resource> resources, QuerySpecification querySpec) {
		List<ModelSearchMatch> result = new ArrayList<ModelSearchMatch>();
		for (Resource resource : resources) {
			try {
				ViatraQueryEngine engine = getWorkspaceViatraQueryEngineHelper().getEngine(resource, true);
				result.addAll(getMatches(querySpec, engine));
			} catch (ViatraQueryException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
		return result;
	}

	@Override
	public List<ModelSearchMatch> getMatches(IModelDescriptor modelDescriptor, QuerySpecification querySpec) {
		try {
			ViatraQueryEngine engine = getWorkspaceViatraQueryEngineHelper().getEngine(modelDescriptor);
			return getMatches(querySpec, engine);
		} catch (ViatraQueryException ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			return Collections.emptyList();
		}
	}
}
