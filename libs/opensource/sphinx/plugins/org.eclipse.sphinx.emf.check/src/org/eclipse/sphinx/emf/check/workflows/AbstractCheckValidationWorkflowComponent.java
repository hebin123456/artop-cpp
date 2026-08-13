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
 *     itemis - [458976] Validators are not singleton when they implement checks for different EPackages
 *     itemis - [463895] org.eclipse.sphinx.emf.check.AbstractCheckValidator.validate(EClass, EObject, DiagnosticChain, Map<Object, Object>) throws NPE
 *     itemis - [473260] Progress indication of check framework
 *     itemis - [473261] Check Validation: Cancel button unresponsive
 *     itemis - [503063] Provide launching support for Sphinx Workflows
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.workflows;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.sphinx.emf.check.ICheckValidator;
import org.eclipse.sphinx.emf.check.operations.BasicCheckValidationOperation;
import org.eclipse.sphinx.emf.check.util.EclipseProgressMonitorAdapter;
import org.eclipse.sphinx.emf.mwe.dynamic.IModelWorkflowSlots;
import org.eclipse.sphinx.emf.mwe.dynamic.components.AbstractWorkspaceWorkflowComponent;
import org.eclipse.sphinx.emf.mwe.dynamic.components.IWorkspaceWorkflowComponent;

/**
 * An abstract {@link IWorkspaceWorkflowComponent workflow component} that can be used to perform check validation of
 * the models provided in the {@link IModelWorkflowSlots#MODEL_SLOT_NAME model slot}.
 */
public abstract class AbstractCheckValidationWorkflowComponent extends AbstractWorkspaceWorkflowComponent {

	private Set<String> categories;
	private boolean intrinsicModelIntegrityConstraintsEnabled;

	public Set<String> getCategories() {
		if (categories == null) {
			categories = new HashSet<String>();
		}
		return categories;
	}

	public boolean isIntrinsicModelIntegrityConstraintsEnabled() {
		return intrinsicModelIntegrityConstraintsEnabled;
	}

	public void setIntrinsicModelIntegrityConstraintsEnabled(boolean intrinsicModelIntegrityConstraintsEnabled) {
		this.intrinsicModelIntegrityConstraintsEnabled = intrinsicModelIntegrityConstraintsEnabled;
	}

	protected Map<Object, Object> createOptions() {
		Map<Object, Object> options = new HashMap<Object, Object>();
		options.put(ICheckValidator.OPTION_CATEGORIES, getCategories());
		options.put(ICheckValidator.OPTION_ENABLE_INTRINSIC_MODEL_INTEGRITY_CONSTRAINTS, intrinsicModelIntegrityConstraintsEnabled);
		return options;
	}

	protected BasicCheckValidationOperation createCheckValidationOperation(List<EObject> modelObjects, Map<Object, Object> options) {
		return new BasicCheckValidationOperation(modelObjects, options);
	}

	@Override
	protected void invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		@SuppressWarnings("unchecked")
		List<EObject> models = (List<EObject>) ctx.get(IModelWorkflowSlots.MODEL_SLOT_NAME);
		if (models == null || models.isEmpty()) {
			return;
		}

		try {
			BasicCheckValidationOperation operation = createCheckValidationOperation(models, createOptions());
			operation.run(new EclipseProgressMonitorAdapter(monitor));
		} catch (OperationCanceledException ex) {
			return;
		} catch (Exception ex) {
			issues.addError(this, ex.getMessage(), models, ex, null);
		}
	}
}
