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
package org.eclipse.sphinx.tests.emf.workspace.ui.scenarios;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.sphinx.platform.resources.IProblemMarkerFinder;

public abstract class AbstractHummingbird20ScenarioProblemMarkerFinder implements IProblemMarkerFinder {

	protected Hummingbird20ScenarioTreeContentProvider hummingbird20ScenarioTreeContentProvider;

	public AbstractHummingbird20ScenarioProblemMarkerFinder(Hummingbird20ScenarioTreeContentProvider hummingbird20ScenarioTreeContentProvider) {
		Assert.isNotNull(hummingbird20ScenarioTreeContentProvider);
		this.hummingbird20ScenarioTreeContentProvider = hummingbird20ScenarioTreeContentProvider;
	}

	@Override
	public Collection<IMarker> getProblemMarkers(Object object) throws CoreException {
		return Collections.emptyList();
	}

	@Override
	public void reset() {
		// Do nothing
	}

}
