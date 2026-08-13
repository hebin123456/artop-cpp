/**
 * <copyright>
 *
 * Copyright (c) 2014-2020 Elektrobit Automotive GmbH, IncQuery Labs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     Elektrobit Automotive GmbH, IncQuery Labs [566004] Use resource filter on Viatra engine
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.viatra.query.internal;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.viatra.query.runtime.base.api.filters.IBaseIndexResourceFilter;

public class ScopeResourceFilter implements IBaseIndexResourceFilter {

	private IModelDescriptor modelDescriptor;
	private static final int PRIME = 31;

	/**
	 * @param modelDescriptor
	 *            the model descriptor whose resources should be accepted by this filter
	 */
	public ScopeResourceFilter(IModelDescriptor modelDescriptor) {
		this.modelDescriptor = modelDescriptor;
	}

	@Override
	public boolean isResourceFiltered(Resource resource) {
		return !modelDescriptor.belongsTo(resource, false);
	}

	@Override
	public int hashCode() {
		/*
		 * !!! Rationale why hashCode() and equals(Object) were overridden The ViatraQueryEngineManager keeps a cache of {@link
		 * org.eclipse.viatra.query.runtime.api.ViatraQueryEngine}s in the form of a WeakHasMap (K=QueryScope and
		 * V=ViatraQueryEngine). Due to the lookup performed in {@link
		 * org.eclipse.viatra.query.runtime.api.ViatraQueryEngineManager.getEngineInternal(QueryScope)}, we need to override
		 * hashCode() and equals(Object), as org.eclipse.viatra.query.runtime.emf.EMFScope.hashCode() relies on
		 * org.eclipse.viatra.query.runtime.base.api.BaseIndexOptions.hashCode() that, in turn, uses
		 * IBaseIndexResourceFilter.hashCode()
		 */
		int result = 1;
		result = PRIME * result + modelDescriptor.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ScopeResourceFilter)) {
			return false;
		}

		ScopeResourceFilter other = (ScopeResourceFilter) obj;

		return modelDescriptor.equals(other.modelDescriptor);
	}

}
