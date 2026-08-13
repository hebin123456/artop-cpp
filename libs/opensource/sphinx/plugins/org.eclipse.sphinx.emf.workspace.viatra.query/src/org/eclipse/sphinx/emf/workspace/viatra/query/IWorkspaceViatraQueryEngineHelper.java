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

import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.viatra.query.IViatraQueryEngineHelper;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;

public interface IWorkspaceViatraQueryEngineHelper extends IViatraQueryEngineHelper {

	ViatraQueryEngine getEngine(IModelDescriptor contextModelDescriptor) throws ViatraQueryException;
}
