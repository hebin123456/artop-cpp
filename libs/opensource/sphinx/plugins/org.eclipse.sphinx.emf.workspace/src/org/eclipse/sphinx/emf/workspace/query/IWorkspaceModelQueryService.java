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
package org.eclipse.sphinx.emf.workspace.query;

import java.util.List;

import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.query.IModelQueryService;

public interface IWorkspaceModelQueryService extends IModelQueryService {

	<T> List<T> getAllInstancesOf(IModelDescriptor modelDescriptor, Class<T> type);
}
