/**
 * <copyright>
 *
 * Copyright (c) 2014-2017 itemis, IncQuery Labs and others.
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
package org.eclipse.sphinx.emf.workspace.viatra.query;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.viatra.query.BasicViatraModelQueryService;
import org.eclipse.sphinx.emf.viatra.query.IViatraQueryEngineHelper;
import org.eclipse.sphinx.emf.workspace.query.IWorkspaceModelQueryService;
import org.eclipse.sphinx.emf.workspace.viatra.query.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;

public class BasicWorkspaceViatraModelQueryService extends BasicViatraModelQueryService implements IWorkspaceModelQueryService {

	public BasicWorkspaceViatraModelQueryService(Collection<IMetaModelDescriptor> mmDescriptors) {
		super(mmDescriptors);
	}

	@Override
	protected IViatraQueryEngineHelper createViatraQueryEngineHelper() {
		return new WorkspaceViatraQueryEngineHelper();
	}

	@Override
	public <T> List<T> getAllInstancesOf(IModelDescriptor modelDescriptor, Class<T> type) {
		try {
			ViatraQueryEngine engine = ((IWorkspaceViatraQueryEngineHelper) getViatraQueryEngineHelper()).getEngine(modelDescriptor);
			return doGetAllInstancesOf(type, engine);
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			return Collections.emptyList();
		}
	}
}
