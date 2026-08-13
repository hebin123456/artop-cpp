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
 *     itemis - [456869] Duplicated Check problem markers due to URI comparison
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.check.ICheckValidationMarker;
import org.eclipse.sphinx.emf.check.internal.Activator;
import org.eclipse.sphinx.emf.check.internal.messages.Messages;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.platform.util.StatusUtil;

// TODO Rename to CheckValidationProblemMarkerService
public class CheckProblemMarkerService {

	public static final String UPDATE_CHECK_PROBLEM_MARKER = "UPDATE_CHECK_PROBLEM_MARKER"; //$NON-NLS-1$

	public static CheckProblemMarkerService INSTANCE = new CheckProblemMarkerService();

	private ICheckValidationProblemMarkerFactory markerFactory = null;

	protected CheckProblemMarkerService() {
	}

	private ICheckValidationProblemMarkerFactory getProblemMarkerFactory() {
		if (markerFactory == null) {
			markerFactory = createProblemMarkerFactory();
		}
		return markerFactory;
	}

	private ICheckValidationProblemMarkerFactory createProblemMarkerFactory() {
		return new CheckProblemMarkerFactory();
	}

	// FIXME Use org.eclipse.sphinx.platform.resources.MarkerJob instead of local WorkspaceJob instance to avoid massive
	// creation of WorkspaceJobs when markers for many root objects need to be created; see ResourceProblemMarkerService
	// for details
	public void updateProblemMarkers(EObject validationInput, final Diagnostic diagnostic) {
		WorkspaceJob job = new WorkspaceJob(Messages.job_handlingDiagnostics_label) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				try {
					handleDiagnostic(diagnostic);
				} catch (CoreException ex) {
					return ex.getStatus();
				} catch (Exception ex) {
					return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
				}
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				return UPDATE_CHECK_PROBLEM_MARKER.equals(family);
			}
		};
		ArrayList<ISchedulingRule> myRules = new ArrayList<ISchedulingRule>();
		IResource resource = EcorePlatformUtil.getFile(validationInput);
		if (resource != null) {
			IResourceRuleFactory ruleFactory = resource.getWorkspace().getRuleFactory();
			myRules.add(ruleFactory.modifyRule(resource));
			myRules.add(ruleFactory.createRule(resource));
		}
		job.setRule(new MultiRule(myRules.toArray(new ISchedulingRule[myRules.size()])));
		job.setPriority(Job.BUILD);
		job.schedule();
	}

	public void handleDiagnostic(Diagnostic diagnostic) throws CoreException {
		handleDiagnostic(diagnostic, EObjectUtil.DEPTH_INFINITE);
	}

	public void handleDiagnostic(Diagnostic diagnostic, int depth) throws CoreException {
		Assert.isNotNull(diagnostic);
		if (diagnostic.getData() == null) {
			return;
		}
		List<?> diagnosticData = diagnostic.getData();
		if (diagnosticData.isEmpty() || !(diagnosticData.get(0) instanceof EObject)) {
			return;
		}
		EObject eObject = (EObject) diagnosticData.get(0);
		if (diagnostic.getSeverity() == Diagnostic.OK) {
			deleteMarkers(eObject, depth);
		} else {
			Resource resource = eObject.eResource();
			if (resource != null) {
				deleteMarkers(eObject, depth);
				addMarkers(resource, diagnostic);
			}
		}
	}

	private void addMarkers(Resource resource, Diagnostic diagnostic) throws CoreException {
		IResource file = EcorePlatformUtil.getFile(resource);
		if (file == null || !file.exists()) {
			return;
		}
		List<?> diagnosticData = diagnostic.getData();
		if (diagnosticData == null || diagnosticData.size() == 0) {
			return;
		}
		for (Diagnostic childDiagnostic : diagnostic.getChildren()) {
			if (!childDiagnostic.getData().isEmpty()) {
				getProblemMarkerFactory().createMarker(file, childDiagnostic);
			}
		}
	}

	private void deleteMarkers(EObject eObject, int depth) throws CoreException {
		deleteMarkers(eObject, depth, ICheckValidationMarker.CHECK_VALIDATION_PROBLEM);
	}

	private void deleteMarkers(EObject eObject, int depth, String markerType) throws CoreException {
		Resource resource = eObject.eResource();
		if (resource != null && resource.getContents() != null) {
			EObject rootObject = resource.getContents().get(0);
			if (rootObject == eObject && depth == EObjectUtil.DEPTH_INFINITE) {
				getProblemMarkerFactory().deleteMarkers(resource);
				return;
			}
		}
		IMarker[] markers = getValidationMarkers(eObject, depth, markerType);
		if (markers == null || markers.length == 0) {
			return;
		}
		for (IMarker marker : markers) {
			marker.delete();
		}
	}

	public IMarker[] getValidationMarkers(final EObject eObject) throws CoreException, InterruptedException {
		return getValidationMarkers(eObject, IResource.DEPTH_INFINITE, ICheckValidationMarker.CHECK_VALIDATION_PROBLEM);
	}

	public IMarker[] getValidationMarkers(EObject eObject, int depth) throws CoreException {
		Job[] jobs = Job.getJobManager().find(UPDATE_CHECK_PROBLEM_MARKER);
		if (jobs.length > 0) {
			try {
				jobs[0].join();
			} catch (InterruptedException ex) {
				// Nothing to do
			}
		}
		return getValidationMarkers(eObject, depth, ICheckValidationMarker.CHECK_VALIDATION_PROBLEM);
	}

	private IMarker[] getValidationMarkers(EObject eObject, int depth, String markerType) throws CoreException {
		IResource resource = EcorePlatformUtil.getFile(eObject);
		if (resource == null || !resource.exists()) {
			return new IMarker[0];
		}
		// All the Markers connected with this resource
		IMarker[] allMarkers = resource.findMarkers(markerType, true, IResource.DEPTH_INFINITE);

		// filter the markers according to the current object and the depth
		// 1. if depth = zero, return the marker with the uri equal to object if any
		// 2. if depth = one, return the marker with the uri equal to object and markers on direct children if any
		// 3. if depth = infinite, return the marker with the uri equal to object and markers on direct and indirect
		// children if any

		List<IMarker> result = new ArrayList<IMarker>();
		URI referenceURI = EcoreResourceUtil.getURI(eObject);
		for (IMarker current : allMarkers) {
			String currentStringURI = (String) current.getAttribute(EValidator.URI_ATTRIBUTE);
			if (currentStringURI != null) {
				URI currentURI = URI.createURI(currentStringURI);
				// String fragment = currentURI.fragment();
				switch (depth) {
				case EObjectUtil.DEPTH_ZERO:
					if (currentURI.equals(referenceURI)) {
						result.add(current);
					}
					break;
				case EObjectUtil.DEPTH_ONE:
					if (currentURI.equals(referenceURI)) {
						result.add(current);
					}
					break;
				case EObjectUtil.DEPTH_INFINITE:
					if (currentURI.equals(referenceURI)) {
						result.add(current);
					} else {
						if (contains(currentURI, referenceURI)) {
							result.add(current);
						}
					}
					break;
				default:
					break;
				}
			}
		}
		return result.toArray(new IMarker[result.size()]);
	}

	protected boolean contains(URI uri, URI anotherURI) {
		if (uri != null && anotherURI != null) {
			return uri.toString().contains(anotherURI.toString());
		}
		return false;
	}
}