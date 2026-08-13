/**
 * <copyright>
 *
 * Copyright (c) 2014-2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [503063] Provide launching support for Sphinx Workflows
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.mwe.dynamic.operations;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.sphinx.platform.operations.IWorkspaceOperation;

public interface IWorkflowRunnerOperation extends IWorkspaceOperation {

	Object getWorkflow();

	Object getModel();

	List<URI> getModelURIs();

	boolean isAutoSave();

	void setAutoSave(boolean autoSave);

	Map<String, Object> getArguments();
}