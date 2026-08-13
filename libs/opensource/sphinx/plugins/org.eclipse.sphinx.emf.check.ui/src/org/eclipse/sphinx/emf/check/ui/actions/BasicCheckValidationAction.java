/**
 * <copyright>
 *
 * Copyright (c) 2014-2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [456869] Duplicated Check problem markers due to URI comparison
 *     itemis - [458976] Validators are not singleton when they implement checks for different EPackages
 *     itemis - [473260] Progress indication of check framework
 *     itemis - [473261] Check Validation: Cancel button unresponsive
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.sphinx.emf.check.CheckValidatorRegistry;
import org.eclipse.sphinx.emf.check.ICheckValidator;
import org.eclipse.sphinx.emf.check.catalog.Category;
import org.eclipse.sphinx.emf.check.operations.BasicCheckValidationOperation;
import org.eclipse.sphinx.emf.check.ui.IValidationUIConstants;
import org.eclipse.sphinx.emf.check.ui.dialogs.CategorySelectionContentProvider;
import org.eclipse.sphinx.emf.check.ui.dialogs.CategorySelectionLabelProvider;
import org.eclipse.sphinx.emf.check.ui.dialogs.CheckValidationOptionsSelectionDialog;
import org.eclipse.sphinx.emf.check.ui.internal.Activator;
import org.eclipse.sphinx.emf.check.ui.internal.CheckValidationImageProvider;
import org.eclipse.sphinx.emf.check.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.IWrapper;
import org.eclipse.sphinx.platform.jobs.WorkspaceOperationJob;
import org.eclipse.sphinx.platform.operations.ILabeledWorkspaceRunnable;
import org.eclipse.sphinx.platform.ui.operations.RunnableWithProgressAdapter;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * A basic action implementation for performing check-based validation. Given the {@link org.eclipse.emf.ecore.EPackage
 * ePackage} of the input object, the action retrieves the check validator -if any- from the validation registry
 * singleton. Then, whether the validator has an associated check catalog or not, the user is invited to select the set
 * of constraint categories to verify. To launch a validation, the standard
 * {@link org.eclipse.emf.ecore.util.Diagnostician diagnostician} is used. Finally the marker
 * {@link org.eclipse.sphinx.emf.check.services.CheckProblemMarkerService service} is used to display the error markers
 * on the problems view and the check validation view.
 */
public class BasicCheckValidationAction extends BaseSelectionListenerAction {

	private boolean runInBackground;

	public BasicCheckValidationAction() {
		this(IValidationUIConstants.SUBMENU_VALIDATE_LABEL);
		setRunInBackground(false);
	}

	public boolean isRunInBackground() {
		return runInBackground;
	}

	public void setRunInBackground(boolean runInBackground) {
		this.runInBackground = runInBackground;
	}

	protected BasicCheckValidationAction(String text) {
		super(text);
		setImageDescriptor(Activator.getImageDescriptor(CheckValidationImageProvider.CHECK_ICO));
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection == null || selection.isEmpty()) {
			return false;
		}
		return existsValidator();
	}

	protected boolean existsValidator() {
		List<Object> modelObjects = getModelObjects();
		for (Object modelObject : modelObjects) {
			if (modelObject instanceof EObject) {
				final EPackage ePackage = ((EObject) modelObject).eClass().getEPackage();
				EValidator validator = CheckValidatorRegistry.INSTANCE.getValidator(ePackage);
				if (validator != null) {
					return true;
				}
			}
		}
		return false;
	}

	protected List<Object> getModelObjects() {
		IStructuredSelection structuredSelection = getStructuredSelection();
		if (structuredSelection != null) {
			List<Object> objects = new ArrayList<Object>();
			for (Object selected : structuredSelection.toList()) {
				objects.addAll(getModelObjects(selected));
			}
			return objects;
		}
		return Collections.emptyList();
	}

	protected List<Object> getModelObjects(Object object) {
		// Wrapped model object or model object
		object = AdapterFactoryEditingDomain.unwrap(object);
		if (object instanceof EObject) {
			return Collections.singletonList(object);
		}
		if (object instanceof IWrapper<?>) {
			Object target = ((IWrapper<?>) object).getTarget();
			if (target instanceof EObject) {
				return Collections.singletonList(target);
			}
		}

		// Group of model objects
		if (object instanceof TransientItemProvider) {
			TransientItemProvider provider = (TransientItemProvider) object;
			List<Object> objects = new ArrayList<Object>();
			for (Object child : provider.getChildren(object)) {
				objects.addAll(getModelObjects(child));
			}
			return objects;
		}

		// Model file or model resource
		Resource resource = null;
		if (object instanceof IFile) {
			resource = EcorePlatformUtil.getResource((IFile) object);
		}
		if (object instanceof Resource) {
			resource = (Resource) object;
		}
		if (resource != null) {
			List<Object> objects = new ArrayList<Object>();
			objects.addAll(resource.getContents());
			return objects;
		}

		return Collections.emptyList();
	}

	@Override
	public void run() {
		// Let the user select the options
		Map<Object, Object> options = promptForCheckValidationOptions();
		if (options == null) {
			return;
		}

		final List<Object> modelObjects = getModelObjects();

		// Create the check validation operation
		final ILabeledWorkspaceRunnable operation = createCheckValidationOperation(modelObjects, options);

		if (isRunInBackground()) {
			// Run the check validation operation in a workspace job
			Job job = createWorkspaceOperationJob(operation);
			job.schedule();
		} else {
			// Run the check validation operation in a progress monitor dialog
			try {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(ExtendedPlatformUI.getActiveShell());
				dialog.run(true, true, new RunnableWithProgressAdapter(operation));
			} catch (InterruptedException ex) {
				// Operation has been canceled by user, do nothing
			} catch (InvocationTargetException ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			}
		}
	}

	protected Job createWorkspaceOperationJob(ILabeledWorkspaceRunnable operation) {
		return new WorkspaceOperationJob(operation);
	}

	protected BasicCheckValidationOperation createCheckValidationOperation(List<Object> modelObjects, Map<Object, Object> options) {
		return new BasicCheckValidationOperation(getOperationName(), modelObjects, options);
	}

	public String getOperationName() {
		return Messages.operation_validate_label;
	}

	// @return empty set (=> no check catalog), set with selected category ids, or null (=> check catalog but nothing
	// selected)
	private Map<Object, Object> promptForCheckValidationOptions() {
		Set<String> selectedCategories = new HashSet<String>();

		// Creates the dialog allowing user to choose categories of constraints to validate
		IStructuredContentProvider contentProvider = createContentProvider();
		ILabelProvider labelProvider = createLabelProvider();

		// FIXME Provide dialog strings through Java messages properties
		CheckValidationOptionsSelectionDialog dialog = new CheckValidationOptionsSelectionDialog(ExtendedPlatformUI.getActiveShell(), new Object(),
				contentProvider, labelProvider, IValidationUIConstants.CONSTRAINT_CATEGORIES_SELECTION_MESSAGE);
		dialog.setTitle(IValidationUIConstants.CONSTRAINT_CATEGORIES_SELECTION_TITLE);
		dialog.setBlockOnOpen(true);

		int result = dialog.open();
		if (result == Window.OK) {
			for (Object resultObject : dialog.getResult()) {
				if (resultObject instanceof Category) {
					selectedCategories.add(((Category) resultObject).getId());
				}
			}

			Map<Object, Object> options = new HashMap<Object, Object>();
			options.put(ICheckValidator.OPTION_CATEGORIES, selectedCategories);
			options.put(ICheckValidator.OPTION_ENABLE_INTRINSIC_MODEL_INTEGRITY_CONSTRAINTS, dialog.enableIntrinsicModelIntegrityConstraints());
			return options;
		}

		return null;
	}

	protected ILabelProvider createLabelProvider() {
		return new CategorySelectionLabelProvider();
	}

	protected IStructuredContentProvider createContentProvider() {
		return new CategorySelectionContentProvider();
	}
}
