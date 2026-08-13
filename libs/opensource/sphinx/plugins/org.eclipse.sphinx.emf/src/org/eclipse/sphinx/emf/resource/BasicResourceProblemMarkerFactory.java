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
 *     itemis - [443648] Proxy resolution error markers don't get removed when underlying resource is reloaded
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.emf.internal.resource.ResourceProblemMarkerService;
import org.eclipse.sphinx.platform.resources.MarkerDescriptor;
import org.eclipse.sphinx.platform.resources.MarkerJob;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class BasicResourceProblemMarkerFactory implements IResourceProblemMarkerFactory {

	@Override
	public void createProblemMarker(IResource resource, Diagnostic diagnostic, int severity, Map<Object, Object> problemHandlingOptions) {
		MarkerDescriptor markerDescriptor = createProblemMarkerDescriptor(diagnostic, severity, problemHandlingOptions);
		createProblemMarker(resource, markerDescriptor);
	}

	protected MarkerDescriptor createProblemMarkerDescriptor(Diagnostic diagnostic, int severity, Map<Object, Object> problemHandlingOptions) {
		MarkerDescriptor markerDescriptor = new MarkerDescriptor(IMarker.PROBLEM);

		// Create marker attributes which are common for all problem types
		markerDescriptor.getAttributes().put(IMarker.TRANSIENT, Boolean.TRUE);

		// Make sure that problem marker line is always 1 or greater because otherwise it would not be visible when
		// underlying resource is opened in a text editor
		int line = diagnostic.getLine();
		markerDescriptor.getAttributes().put(IMarker.LINE_NUMBER, line > 0 ? line : 1);
		markerDescriptor.getAttributes().put(IMarker.LOCATION, NLS.bind(Messages.attribute_line, line));
		markerDescriptor.getAttributes().put(IMarker.SEVERITY, severity);

		// Handle ordinary diagnostics
		markerDescriptor.getAttributes().put(IMarker.MESSAGE, diagnostic.getMessage());
		return markerDescriptor;
	}

	@Override
	public void createProblemMarker(IResource resource, Exception exception, int severity) {
		Assert.isNotNull(exception);

		MarkerDescriptor markerDescriptor = createProblemMarkerDescriptor(exception, severity);
		createProblemMarker(resource, markerDescriptor);
	}

	protected MarkerDescriptor createProblemMarkerDescriptor(Exception exception, int severity) {
		MarkerDescriptor markerDescriptor = new MarkerDescriptor(IMarker.PROBLEM);
		markerDescriptor.getAttributes().put(IMarker.TRANSIENT, Boolean.TRUE);
		markerDescriptor.getAttributes().put(IMarker.LINE_NUMBER, 1);
		markerDescriptor.getAttributes().put(IMarker.LOCATION, NLS.bind(Messages.attribute_line, 1));
		markerDescriptor.getAttributes().put(IMarker.SEVERITY, severity);
		markerDescriptor.getAttributes().put(IMarker.MESSAGE, createProblemMarkerMessage(exception));
		return markerDescriptor;
	}

	protected void createProblemMarker(IResource resource, MarkerDescriptor markerDescriptor) {
		// Does resource exist?
		if (resource.isAccessible()) {
			// Create new problem marker with previously calculated markerDescriptor
			MarkerJob.INSTANCE.addCreateMarkerTask(resource, markerDescriptor);
		} else {
			// Use error log for errors of non-existing files or in-memory resources
			Integer effectiveSeverity = (Integer) markerDescriptor.getAttributes().get(IMarker.SEVERITY);
			String effectiveMessage = (String) markerDescriptor.getAttributes().get(IMarker.MESSAGE);
			if (effectiveSeverity == IMarker.SEVERITY_ERROR) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), effectiveMessage);
			} else if (effectiveSeverity == IMarker.SEVERITY_WARNING) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), effectiveMessage);
			} else if (effectiveSeverity == IMarker.SEVERITY_INFO) {
				PlatformLogUtil.logAsInfo(Activator.getPlugin(), effectiveMessage);
			}
		}
	}

	protected String createProblemMarkerMessage(Exception exception) {
		Assert.isNotNull(exception);

		StringBuilder msg = new StringBuilder();
		msg.append(exception.getLocalizedMessage());
		Throwable cause = exception.getCause();
		if (cause != null) {
			String causeMsg = cause.getLocalizedMessage();
			if (causeMsg != null && causeMsg.length() > 0 && !msg.toString().contains(causeMsg)) {
				msg.append(": "); //$NON-NLS-1$
				msg.append(causeMsg);
			}
		}
		return msg.toString();
	}

	@Override
	public void deleteMarkers(IResource resource) {
		for (String markerType : getProblemMarkerTypesToDelete()) {
			MarkerJob.INSTANCE.addDeleteMarkerTask(resource, markerType);
		}
	}

	protected List<String> getProblemMarkerTypesToDelete() {
		List<String> problemMarkerTypes = new ArrayList<String>();

		problemMarkerTypes.add(IMarker.PROBLEM);
		problemMarkerTypes.add(ResourceProblemMarkerService.PROXY_URI_INTEGRITY_PROBLEM);

		return problemMarkerTypes;
	}
}
