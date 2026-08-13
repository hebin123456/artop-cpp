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
 *     itemis - [454883] CheckProblemMarkerFactory Exception because of std. EObject validation
 *     itemis - [456869] Duplicated Check problem markers due to URI comparison
 *     itemis - [473262] Check Framework Robustness: Gracefully handle null feature
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.check.ICheckValidationMarker;
import org.eclipse.sphinx.emf.check.internal.messages.Messages;
import org.eclipse.sphinx.emf.check.util.DiagnosticLocation;
import org.eclipse.sphinx.emf.check.util.SourceLocation;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;

/**
 * Default check problem errors factory.
 */
public class CheckProblemMarkerFactory implements ICheckValidationProblemMarkerFactory {

	@Override
	public void createMarker(IResource resource, Diagnostic diagnostic) throws CoreException {
		createMarker(resource, diagnostic, ICheckValidationMarker.CHECK_VALIDATION_PROBLEM);
	}

	@Override
	public void createMarker(IResource resource, Diagnostic diagnostic, String markerType) throws CoreException {
		// Create the marker with the given type
		IMarker marker = resource.createMarker(markerType);
		Map<String, Object> attributes = new HashMap<String, Object>();

		// URI attribute
		EObject affectedObject = getAffectedObject(diagnostic);
		if (affectedObject != null) {
			attributes.put(EValidator.URI_ATTRIBUTE, EcoreResourceUtil.getURI(affectedObject).toString());
		}

		// Location attribute
		DiagnosticLocation affectedLocation = getAffectedLocation(diagnostic);
		if (affectedLocation != null) {
			StringBuilder affectedLocationAsString = new StringBuilder();

			EObject object = affectedLocation.getObject();
			if (object != null) {
				affectedLocationAsString.append(object.eClass().getName());
			} else {
				affectedLocationAsString.append(Messages.markerLocation_unknownObject);
			}

			affectedLocationAsString.append("#"); //$NON-NLS-1$

			EStructuralFeature feature = affectedLocation.getFeature();
			if (feature != null) {
				affectedLocationAsString.append(feature.getName());
			} else {
				affectedLocationAsString.append(Messages.markerLocation_unknownFeature);
			}

			if (affectedLocation.getIndex() != -1) {
				affectedLocationAsString.append("."); //$NON-NLS-1$
				affectedLocationAsString.append(affectedLocation.getIndex());
			}
			attributes.put(IMarker.LOCATION, affectedLocationAsString.toString());
		}

		// Severity attribute
		int markerSeverity;
		int severity = diagnostic.getSeverity();
		if (severity < Diagnostic.WARNING) {
			markerSeverity = IMarker.SEVERITY_INFO;
		} else if (severity < Diagnostic.ERROR) {
			markerSeverity = IMarker.SEVERITY_WARNING;
		} else {
			markerSeverity = IMarker.SEVERITY_ERROR;
		}
		attributes.put(IMarker.SEVERITY, markerSeverity);

		// Message attribute
		String message = diagnostic.getMessage();
		if (message == null) {
			message = Messages.markerMessage_unknownProblem;
		}
		attributes.put(IMarker.MESSAGE, message);

		// Source attribute
		attributes.put(IMarker.SOURCE_ID, getSource(diagnostic));

		// Set the attributes for the created marker
		marker.setAttributes(attributes);
	}

	@SuppressWarnings("unchecked")
	protected EObject getAffectedObject(Diagnostic diagnostic) {
		DiagnosticLocation affectedLocation = getAffectedLocation(diagnostic);
		if (affectedLocation != null) {
			return affectedLocation.getObject();
		}
		// If DiagnosticLocation not found, look for first object in the data
		List<Object> data = (List<Object>) diagnostic.getData();
		Object item = data.get(0);
		if (item instanceof EObject) {
			return (EObject) item;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected DiagnosticLocation getAffectedLocation(Diagnostic diagnostic) {
		List<Object> data = (List<Object>) diagnostic.getData();
		// Try to find a DiagnosticLocation in the data
		for (Object obj : data) {
			if (obj instanceof DiagnosticLocation) {
				return (DiagnosticLocation) obj;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected String getSource(Diagnostic diagnostic) {
		List<Object> data = (List<Object>) diagnostic.getData();
		// Try to find a SourceLocation in the data
		for (Object obj : data) {
			if (obj instanceof SourceLocation) {
				SourceLocation sourceLocation = (SourceLocation) obj;
				if (sourceLocation.getConstraintId() != null && !sourceLocation.getConstraintId().isEmpty()) {
					return sourceLocation.getConstraintId();
				} else {
					StringBuilder sourceBuilder = new StringBuilder();
					sourceBuilder.append(sourceLocation.getCheckValidator().getSimpleName());
					sourceBuilder.append("#"); //$NON-NLS-1$
					sourceBuilder.append(sourceLocation.getCheckMethod().getName());
					return sourceBuilder.toString();
				}
			}
		}
		return diagnostic.getSource();
	}

	@Override
	public void deleteMarkers(Resource resource) throws CoreException {
		deleteMarkers(resource, ICheckValidationMarker.CHECK_VALIDATION_PROBLEM);
	}

	@Override
	public void deleteMarkers(Resource resource, String markerType) throws CoreException {
		IResource file = EcorePlatformUtil.getFile(resource);
		if (file == null || !file.exists()) {
			return;
		}
		IMarker[] markers = file.findMarkers(markerType, true, IResource.DEPTH_INFINITE);
		for (IMarker marker : markers) {
			marker.delete();
		}
	}
}
