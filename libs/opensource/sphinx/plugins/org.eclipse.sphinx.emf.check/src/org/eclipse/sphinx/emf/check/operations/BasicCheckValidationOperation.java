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
 *     itemis - [463895] org.eclipse.sphinx.emf.check.AbstractCheckValidator.validate(EClass, EObject, DiagnosticChain, Map<Object, Object>) throws NPE
 *     itemis - [473260] Progress indication of check framework
 *     itemis - [473261] Check Validation: Cancel button unresponsive
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.operations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.sphinx.emf.check.internal.Activator;
import org.eclipse.sphinx.emf.check.internal.messages.Messages;
import org.eclipse.sphinx.emf.check.services.CheckProblemMarkerService;
import org.eclipse.sphinx.emf.check.util.ExtendedDiagnostician;
import org.eclipse.sphinx.platform.operations.AbstractLabeledWorkspaceRunnable;
import org.eclipse.sphinx.platform.util.StatusUtil;

public class BasicCheckValidationOperation extends AbstractLabeledWorkspaceRunnable {

	protected static final Object ALL_MODEL_OBJECT_COUNT = "ALL_MODEL_OBJECT_COUNT"; //$NON-NLS-1$
	protected static final float PROBLEM_MARKER_TO_VALIDATION_WORK_RATIO = 0.1f;

	private List<?> modelObjects;
	private Map<Object, Object> options;
	private ExtendedDiagnostician diagnostician;

	public BasicCheckValidationOperation(List<?> modelObjects, Map<Object, Object> options) {
		this(Messages.operation_validate_label, modelObjects, options);
	}

	public BasicCheckValidationOperation(String label, List<? extends Object> modelObjects, Map<Object, Object> options) {
		super(label);
		Assert.isNotNull(modelObjects);

		this.modelObjects = modelObjects;
		this.options = options;
	}

	protected List<?> getModelObjects() {
		return modelObjects;
	}

	protected Map<Object, Integer> getModelObjectCounts() {
		List<?> objects = getModelObjects();
		Map<Object, Integer> counts = new HashMap<Object, Integer>(objects.size());
		int allCount = 0;
		for (Object object : objects) {
			int count = getModelObjectCount(object);
			counts.put(object, count);
			allCount += count;
		}
		counts.put(ALL_MODEL_OBJECT_COUNT, allCount);
		return counts;
	}

	protected int getModelObjectCount(Object object) {
		int count = 1;
		if (object instanceof EObject) {
			for (Iterator<?> iter = ((EObject) object).eAllContents(); iter.hasNext(); iter.next()) {
				count++;
			}
		}
		return count;
	}

	protected Map<Object, Object> getOptions() {
		if (options == null) {
			options = new HashMap<Object, Object>();
		}
		return options;
	}

	protected ExtendedDiagnostician getDiagnostician() {
		if (diagnostician == null) {
			diagnostician = createDiagnostician();
		}
		return diagnostician;
	}

	protected ExtendedDiagnostician createDiagnostician() {
		return new ExtendedDiagnostician();
	}

	protected float getProblemMarkerToValidationWorkRatio() {
		return PROBLEM_MARKER_TO_VALIDATION_WORK_RATIO;
	}

	protected TransactionalEditingDomain getEditingDomain() {
		List<?> objects = getModelObjects();
		if (!objects.isEmpty()) {
			return TransactionUtil.getEditingDomain(objects.get(0));
		}
		return null;
	}

	@Override
	public void run(final IProgressMonitor monitor) throws CoreException {
		try {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					Map<Object, Integer> modelObjectCounts = getModelObjectCounts();
					float problemMarkerToValidationWorkRatio = getProblemMarkerToValidationWorkRatio();
					int totalWork = Math.round(modelObjectCounts.get(ALL_MODEL_OBJECT_COUNT) * (1 + problemMarkerToValidationWorkRatio));
					SubMonitor progress = SubMonitor.convert(monitor, Messages.task_validating, totalWork);
					if (progress.isCanceled()) {
						throw new OperationCanceledException();
					}

					for (Object modelObject : getModelObjects()) {
						int validationWork = modelObjectCounts.get(modelObject);
						int problemMarkerWork = Math.round(validationWork * problemMarkerToValidationWorkRatio);
						validate(modelObject, progress.newChild(validationWork + problemMarkerWork), validationWork, problemMarkerWork);

						if (progress.isCanceled()) {
							throw new OperationCanceledException();
						}
					}
				}
			};

			TransactionalEditingDomain editingDomain = getEditingDomain();
			if (editingDomain != null) {
				editingDomain.runExclusive(runnable);
			} else {
				runnable.run();
			}
		} catch (OperationCanceledException ex) {
			throw ex;
		} catch (Exception ex) {
			IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
			throw new CoreException(status);
		}
	}

	protected void validate(Object modelObject, SubMonitor progress, int validationWork, int problemMarkerWork) throws OperationCanceledException {
		Assert.isNotNull(progress);

		if (modelObject instanceof EObject) {
			Diagnostic diagnostic = getDiagnostician().validate((EObject) modelObject, getOptions(), progress.newChild(validationWork));

			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}

			updateProblemMarkers((EObject) modelObject, diagnostic, progress.newChild(problemMarkerWork));
		} else {
			progress.done();
		}
	}

	protected void updateProblemMarkers(EObject eObject, Diagnostic diagnostic, IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 1);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// TODO Add progress monitor support to CheckProblemMarkerService
		CheckProblemMarkerService.INSTANCE.updateProblemMarkers(eObject, diagnostic);
		progress.worked(1);
	}
}
