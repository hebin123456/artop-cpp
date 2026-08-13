/**
 * <copyright>
 * 
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [418005] Add support for model files with multiple root elements
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.validation.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.emf.edit.ui.EMFEditUIPlugin;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.validation.diagnostic.ExtendedDiagnostician;
import org.eclipse.sphinx.emf.validation.markers.ValidationMarkerManager;
import org.eclipse.sphinx.emf.validation.stats.ValidationPerformanceStats;
import org.eclipse.sphinx.emf.validation.ui.SphinxValidationUiActivator;
import org.eclipse.sphinx.emf.validation.ui.util.DiagnosticUI;
import org.eclipse.sphinx.emf.validation.ui.util.Messages;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

/**
 * Basic implementation of validate action. A <em>Validate</em> action is supposed to ask for the validation of a
 * selected model, <em>i.e.</em> for verifying constraints that are applicable to the selected model's objects.
 * <p>
 * <table>
 * <tr valign=top>
 * <td><b>Note</b>&nbsp;&nbsp;</td>
 * <td>Action's enablement is not computed by action itself. This is due to an optimization that has been made at the
 * parent action provider level; indeed
 * {@linkplain org.eclipse.sphinx.emf.validation.ui.actions.providers.BasicValidationActionProvider
 * BasicValidationActionProvider} computes enablement (for all actions it owns) only once.</td>
 * </tr>
 * </table>
 */
public class BasicValidateAction extends BaseSelectionListenerAction {

	private boolean displayBriefReport = false;

	public BasicValidateAction() {
		super(Messages._UI_Validate_menu_item);
		setDescription(Messages._UI_Validate_simple_description);
	}

	@Override
	public void run() {

		final List<EObject> selectedModelObjects = getSelectedModelObjects();

		final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
			@Override
			public void run(final IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
				try {
					// FIXME Shouldn't this profiling be started and stopped inside 'asyncExec'?
					ValidationPerformanceStats.INSTANCE.openContext("Validation of " + selectedModelObjects.get(0));

					final List<Diagnostic> diagnostics = validateMulti(selectedModelObjects, progressMonitor);

					shell.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (progressMonitor.isCanceled()) {
								handleDiagnostic(selectedModelObjects, Diagnostic.CANCEL_INSTANCE);
							} else if (diagnostics != null) {
								handleDiagnosticMulti(selectedModelObjects, diagnostics, displayBriefReport);
							}

							try {
								PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
										.showView("org.eclipse.sphinx.examples.validation.ui.views.validation"); //$NON-NLS-1$
							} catch (PartInitException ex) {
								// Fail silent
							}
						}
					});

					ValidationPerformanceStats.INSTANCE.closeAndLogCurrentContext();
				} finally {
					progressMonitor.done();
				}
			}
		};

		try {
			// This runs the operation, and shows progress.
			// (It appears to be a bad thing to fork this onto another thread.)
			new ProgressMonitorDialog(shell).run(true, true, new WorkspaceModifyDelegatingOperation(runnableWithProgress));
		} catch (Exception exception) {
			EMFEditUIPlugin.INSTANCE.log(exception);
		}
	}

	protected void handleDiagnostic(List<EObject> selectedModelObjects, Diagnostic diagnostic) {
		handleDiagnosticMulti(selectedModelObjects, Collections.singletonList(diagnostic), displayBriefReport);
	}

	protected List<Diagnostic> validateMulti(List<EObject> selectedModelObjects, IProgressMonitor progressMonitor) {

		ArrayList<Diagnostic> result = new ArrayList<Diagnostic>();
		ExtendedDiagnostician diagnostician = new ExtendedDiagnostician();
		if (selectedModelObjects.size() == 1) {
			EObject eObject = selectedModelObjects.get(0);
			if (eObject != null) {

				int count = getNumberOfObject(eObject);
				// ValidationPerformanceStats.INSTANCE.startEvent(enumerator, blameObject);

				progressMonitor.beginTask("", count); //$NON-NLS-1$
				progressMonitor.setTaskName(EMFEditUIPlugin.INSTANCE.getString("_UI_Validating_message", //$NON-NLS-1$
						new Object[] { diagnostician.getObjectLabel(eObject) }));
				ValidationPerformanceStats.INSTANCE.startNewEvent(ValidationPerformanceStats.ValidationEvent.EVENT_APPLY_CONSTRAINTS,
						eObject.toString());
				diagnostician.setProgressMonitor(progressMonitor);
				result.add(diagnostician.validate(eObject));
				ValidationPerformanceStats.INSTANCE.endEvent(ValidationPerformanceStats.ValidationEvent.EVENT_APPLY_CONSTRAINTS, eObject.toString());
				diagnostician.setProgressMonitor(null);
				progressMonitor.done();
			}

			return result;
		}

		else if (selectedModelObjects.size() > 1) {

			int count = 0;
			int[] subCount = new int[selectedModelObjects.size()];
			int cptObject = 0;
			for (EObject object : selectedModelObjects) {
				subCount[cptObject] = getNumberOfObject(object);
				count += subCount[cptObject++];
			}

			progressMonitor.beginTask("", count); //$NON-NLS-1$
			progressMonitor.setTaskName(Messages._UI_progressBar_InitialMsg);

			boolean isProgressMonitor = false;

			diagnostician.setProgressMonitor(progressMonitor);
			isProgressMonitor = true;

			Diagnostic diag = null;
			cptObject = 0;

			int[] nbE = { 0, 0, 0 }; // Error, Warning, Info
			final int ERRIdx = 0;
			final int WARNIdx = 1;
			final int INFOIdx = 2;

			for (Object current : selectedModelObjects) {
				if (progressMonitor.isCanceled()) {
					break;
				}

				IProgressMonitor subMonitor = null;
				if (isProgressMonitor) {
					subMonitor = new SubProgressMonitor(progressMonitor, subCount[cptObject++]);
					subMonitor.subTask(NLS.bind(Messages._UI_subValidationMonitorIntro, EcorePlatformUtil.getFile((EObject) current).getName()));
				}
				ValidationPerformanceStats.INSTANCE.startNewEvent(ValidationPerformanceStats.ValidationEvent.EVENT_APPLY_CONSTRAINTS,
						current.toString());
				diag = diagnostician.validate((EObject) current);
				if (diag != null) {
					result.add(diag);

					for (Diagnostic c : diag.getChildren()) {
						switch (c.getSeverity()) {
						case Diagnostic.ERROR:
							nbE[ERRIdx]++;
							break;
						case Diagnostic.WARNING:
							nbE[WARNIdx]++;
							break;
						case Diagnostic.INFO:
							nbE[INFOIdx]++;
							break;
						default: // do nothing
						}
					}
				}

				if (subMonitor != null) {
					subMonitor.done();
				}
				ValidationPerformanceStats.INSTANCE.endEvent(ValidationPerformanceStats.ValidationEvent.EVENT_APPLY_CONSTRAINTS, current.toString());
				progressMonitor.setTaskName(NLS.bind(Messages._UI_progressBarMulti_ErrWarnInfo, new Object[] { nbE[ERRIdx], nbE[WARNIdx],
						nbE[INFOIdx] }));
			}

			diagnostician.setProgressMonitor(null);
			progressMonitor.done();

			return result;
		}

		PlatformLogUtil.logAsWarning(SphinxValidationUiActivator.getDefault(), new RuntimeException("Cannot perform validation on empty element selection.")); //$NON-NLS-1$
		return null;
	}

	protected void handleDiagnosticMulti(final List<EObject> selectedModelObjects, final List<Diagnostic> diagnostics, boolean showBriefReport) {
		Assert.isNotNull(diagnostics);
		ValidationPerformanceStats.INSTANCE.startNewEvent(ValidationPerformanceStats.ValidationEvent.EVENT_UPDATE_PROBLEM_MARKERS, "UpdateMarkers");
		// Optionally show validation results in a Pop-up window
		if (showBriefReport) {
			DiagnosticUI.showDiagnostic(diagnostics);
		}

		// On a second and, let's create markers
		WorkspaceJob job = new WorkspaceJob(Messages._Job_HandleDiagnostic) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				for (Diagnostic diag : diagnostics) {
					ValidationMarkerManager.getInstance().handleDiagnostic(diag);
				}
				return Status.OK_STATUS;
			}

		};

		ArrayList<ISchedulingRule> myRules = new ArrayList<ISchedulingRule>();
		for (EObject eObject : selectedModelObjects) {
			IResource r = EcorePlatformUtil.getFile(eObject);
			if (r != null) {
				IResourceRuleFactory ruleFactory = r.getWorkspace().getRuleFactory();
				myRules.add(ruleFactory.modifyRule(r));
				myRules.add(ruleFactory.createRule(r));
			}
		}

		job.setRule(new MultiRule(myRules.toArray(new ISchedulingRule[myRules.size()])));
		job.setPriority(Job.BUILD);
		job.schedule();
		ValidationPerformanceStats.INSTANCE.endEvent(ValidationPerformanceStats.ValidationEvent.EVENT_UPDATE_PROBLEM_MARKERS, "UpdateMarkers");
	}

	/**
	 * For progress bar, useful method which return number of Object to validate into model
	 * 
	 * @param eObject
	 * @return number of Object which will be validate
	 */
	protected int getNumberOfObject(EObject eObject) {
		int count = 0;
		for (Iterator<?> i = eObject.eAllContents(); i.hasNext(); i.next()) {
			++count;
		}
		return count;
	}

	/**
	 * Due to performance overhead, its just called before running the action to initialize the list of selected model
	 * objects.
	 * 
	 * @param selection
	 *            the current selection
	 */
	private List<EObject> getSelectedModelObjects() {
		// Just retrieve the selection that has been given to this action by the parent action provider
		IStructuredSelection selection = getStructuredSelection();

		List<EObject> result = new ArrayList<EObject>();
		List<IFile> files = new ArrayList<IFile>();
		for (Object selectedObject : selection.toList()) {
			if (selectedObject instanceof IProject) {
				IProject project = (IProject) selectedObject;
				if (project.isAccessible()) {
					files.addAll(ExtendedPlatform.getAllFiles((IProject) selectedObject, true));
				}
			} else if (selectedObject instanceof IFolder) {
				IFolder folder = (IFolder) selectedObject;
				if (folder.isAccessible()) {
					files.addAll(ExtendedPlatform.getAllFiles((IFolder) selectedObject));
				}
			} else if (selectedObject instanceof IFile) {
				IFile file = (IFile) selectedObject;
				if (file.isAccessible()) {
					files.add((IFile) selectedObject);
				}
			} else if (selectedObject instanceof EObject) {
				result.add((EObject) selectedObject);
			} else if (selectedObject instanceof IWrapperItemProvider) {
				Object object = AdapterFactoryEditingDomain.unwrap(selectedObject);
				if (object instanceof EObject) {
					result.add((EObject) object);
				}
			}
		}
		if (!files.isEmpty()) {
			// If selected object is a file, get the mapped model root
			for (IFile file : files) {
				// Get model objects from workspace file
				Resource resource = EcorePlatformUtil.getResource(file);
				if (resource != null) {
					result.addAll(resource.getContents());
				}
			}
		}
		return result;
	}
}