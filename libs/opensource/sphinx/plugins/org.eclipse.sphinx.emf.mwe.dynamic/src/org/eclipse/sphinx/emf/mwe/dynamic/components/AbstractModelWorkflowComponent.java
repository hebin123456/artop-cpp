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
package org.eclipse.sphinx.emf.mwe.dynamic.components;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public abstract class AbstractModelWorkflowComponent extends AbstractWorkspaceWorkflowComponent implements IModelWorkflowComponent {

	private final boolean modifiesModel;

	public AbstractModelWorkflowComponent() {
		this(null, false);
	}

	public AbstractModelWorkflowComponent(ISchedulingRule rule) {
		this(rule, false);
	}

	public AbstractModelWorkflowComponent(boolean modifiesModel) {
		this(null, modifiesModel);
	}

	public AbstractModelWorkflowComponent(ISchedulingRule rule, boolean modifiesModel) {
		super(rule);
		this.modifiesModel = modifiesModel;
	}

	@Override
	public boolean isModifyingModel() {
		return modifiesModel;
	}
}
