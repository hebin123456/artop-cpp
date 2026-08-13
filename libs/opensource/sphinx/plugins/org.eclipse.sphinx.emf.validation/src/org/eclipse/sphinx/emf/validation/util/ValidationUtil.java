/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, Continental Engineering Services, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     Continental Engineering Services - Wait for the markers to be assigned to the resources
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [480135] Introduce metamodel and view content agnostic problem decorator for model elements
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.validation.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.emf.validation.service.IConstraintFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.validation.Activator;
import org.eclipse.sphinx.emf.validation.diagnostic.ExtendedDiagnostician;
import org.eclipse.sphinx.emf.validation.internal.messages.Messages;
import org.eclipse.sphinx.emf.validation.markers.ValidationMarkerManager;
import org.eclipse.sphinx.emf.validation.stats.ValidationPerformanceStats;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * useful utility methods.
 */
public class ValidationUtil {

	public static String getObjectId(String uri) {
		int lowerBound = uri.lastIndexOf("/"); //$NON-NLS-1$
		int upperBound = uri.lastIndexOf("?"); //$NON-NLS-1$
		if (lowerBound != -1 && upperBound != -1) {
			return uri.substring(lowerBound + 1, upperBound);
		}
		return ""; //$NON-NLS-1$
	}

	public static String[] splitURI(EObject eObject) {
		URI uri = getURI(eObject);
		return splitURI(uri);
	}

	protected static URI getURI(final EObject eObject) {
		if (eObject != null) {
			final TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(eObject);
			if (editingDomain != null) {
				try {
					return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<URI>() {
						@Override
						public void run() {
							setResult(EcoreResourceUtil.getURI(eObject, true));
						}
					});
				} catch (InterruptedException ex) {
					PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
				}
			}
		}
		return null;
	}

	public static String[] splitURI(URI uri) {
		return uri == null ? null : splitURI(uri.toString());
	}

	public static String[] splitURI(String uri) {
		return uri == null ? null : uri.split("\\?"); //$NON-NLS-1$
	}

	// FIXME It is not admissible to assume any fix URI format right here. Old URI calculation must take resource or
	// even metamodel specific URI formats into account.
	public static String computeOldURI(EObject eObject, String oldName) {
		if (oldName == null) {
			return null;
		}

		URI uri_ = getURI(eObject);
		if (uri_ == null || uri_.segmentCount() < 1) {
			return null;
		}

		String uri = uri_.toString();
		int lowerBound = uri.lastIndexOf("/"); //$NON-NLS-1$
		int upperBound = uri.lastIndexOf("?"); //$NON-NLS-1$
		String newName = uri.substring(lowerBound, upperBound);

		return uri.replace(newName + "?", "/" + oldName + "?"); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
	}

	public static String getObjectType(String uri) {
		return uri.lastIndexOf("=") != -1 ? uri.substring(uri.lastIndexOf("=") + 1) : ""; //$NON-NLS-1$ //$NON-NLS-3$
	}

	private static List<EObject> getModelObjects(Collection<?> objects) {
		List<EObject> result = new ArrayList<EObject>();
		List<IFile> files = new ArrayList<IFile>();
		for (Object obj : objects) {
			if (obj instanceof IProject) {
				IProject project = (IProject) obj;
				if (project.isAccessible()) {
					files.addAll(ExtendedPlatform.getAllFiles((IProject) obj, true));
				}
			} else if (obj instanceof IFolder) {
				IFolder folder = (IFolder) obj;
				if (folder.isAccessible()) {
					files.addAll(ExtendedPlatform.getAllFiles((IFolder) obj));
				}
			} else if (obj instanceof IFile) {
				IFile file = (IFile) obj;
				if (file.isAccessible()) {
					files.add((IFile) obj);
				}
			} else if (obj instanceof EObject) {
				result.add((EObject) obj);
			} else if (obj instanceof IWrapperItemProvider) {
				Object object = AdapterFactoryEditingDomain.unwrap(obj);
				if (object instanceof EObject) {
					result.add((EObject) object);
				}
			}
		}
		if (!files.isEmpty()) {
			// If selected object is a file, get the mapped model root
			for (IFile file : files) {
				// Get model from workspace file
				Resource resource = EcorePlatformUtil.getResource(file);
				if (resource != null) {
					result.addAll(resource.getContents());
				}
			}
		}
		return result;
	}

	/**
	 * For progress bar, useful method which return number of Object to validate into model
	 *
	 * @param eObject
	 * @return number of Object which will be validate
	 */
	private static int getNumberOfObject(EObject eObject) {
		int count = 0;
		for (Iterator<?> i = eObject.eAllContents(); i.hasNext(); i.next()) {
			++count;
		}
		return count;
	}

	public static void validate(Collection<?> objects, Collection<IConstraintFilter> filters, IProgressMonitor monitor) {
		// Retrieve underlying model objects
		List<EObject> modelObjects = getModelObjects(objects);
		if (!modelObjects.isEmpty()) {
			ValidationPerformanceStats.INSTANCE.openContext("Validation of " + modelObjects.get(0)); //$NON-NLS-1$

			List<Diagnostic> diagnostics = validate(modelObjects, filters, monitor);

			handleDiagnostics(modelObjects, diagnostics);
		}
		ValidationPerformanceStats.INSTANCE.closeAndLogCurrentContext();
		monitor.done();
	}

	private static List<Diagnostic> validate(List<EObject> modelObjects, Collection<IConstraintFilter> filters, IProgressMonitor monitor) {
		ArrayList<Diagnostic> result = new ArrayList<Diagnostic>();
		ExtendedDiagnostician diagnostician = new ExtendedDiagnostician();
		if (modelObjects.size() == 1) {
			EObject eObject = modelObjects.get(0);
			if (eObject != null) {

				int count = getNumberOfObject(eObject);
				// ValidationPerformanceStats.INSTANCE.startEvent(enumerator, blameObject);

				monitor.beginTask(new String(), count);
				monitor.setTaskName(NLS.bind(Messages.task_subtask_validatingObject, diagnostician.getObjectLabel(eObject)));
				ValidationPerformanceStats.INSTANCE.startNewEvent(ValidationPerformanceStats.ValidationEvent.EVENT_APPLY_CONSTRAINTS,
						eObject.toString());
				diagnostician.setProgressMonitor(monitor);
				result.add(diagnostician.validate(eObject, new HashSet<IConstraintFilter>(filters)));
				ValidationPerformanceStats.INSTANCE.endEvent(ValidationPerformanceStats.ValidationEvent.EVENT_APPLY_CONSTRAINTS, eObject.toString());
				diagnostician.setProgressMonitor(null);
				monitor.done();
			}

			return result;
		} else if (modelObjects.size() > 1) {

			int count = 0;
			int[] subCount = new int[modelObjects.size()];
			int cptObject = 0;
			for (EObject object : modelObjects) {
				subCount[cptObject] = getNumberOfObject(object);
				count += subCount[cptObject++];
			}

			monitor.beginTask(new String(), count);
			monitor.setTaskName(Messages.task_progressBar_InitialMsg);

			boolean isProgressMonitor = false;

			diagnostician.setProgressMonitor(monitor);
			isProgressMonitor = true;

			Diagnostic diag = null;
			cptObject = 0;

			int[] nbE = { 0, 0, 0 }; // Error, Warning, Info
			final int ERRIdx = 0;
			final int WARNIdx = 1;
			final int INFOIdx = 2;

			for (Object current : modelObjects) {
				if (monitor.isCanceled()) {
					break;
				}

				IProgressMonitor subMonitor = null;
				if (isProgressMonitor) {
					subMonitor = new SubProgressMonitor(monitor, subCount[cptObject++]);
					subMonitor.subTask(NLS.bind(Messages.task_subtask_validatingFile, EcorePlatformUtil.getFile((EObject) current).getName()));
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
				monitor.setTaskName(NLS.bind(Messages.task_progressBar_ErrWarnInfo, new Object[] { nbE[ERRIdx], nbE[WARNIdx], nbE[INFOIdx] }));
			}

			diagnostician.setProgressMonitor(null);
			monitor.done();

			return result;
		}

		PlatformLogUtil.logAsWarning(Activator.getDefault(), new RuntimeException("Cannot perform validation on empty element selection.")); //$NON-NLS-1$
		return null;
	}

	private static void handleDiagnostics(final List<EObject> selectedModelObjects, final List<Diagnostic> diagnostics) {
		if (diagnostics == null) {
			return;
		}

		WorkspaceJob job = new WorkspaceJob(Messages.job_HandlingDiagnostics) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				String blameObject = "UpdateMarkers"; //$NON-NLS-1$
				ValidationPerformanceStats.INSTANCE.startNewEvent(ValidationPerformanceStats.ValidationEvent.EVENT_UPDATE_PROBLEM_MARKERS,
						blameObject);

				ValidationMarkerManager markerManager = ValidationMarkerManager.getInstance();
				for (Diagnostic diag : diagnostics) {
					markerManager.handleDiagnostic(diag);
				}

				ValidationPerformanceStats.INSTANCE.endEvent(ValidationPerformanceStats.ValidationEvent.EVENT_UPDATE_PROBLEM_MARKERS, blameObject);

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
	}
}
