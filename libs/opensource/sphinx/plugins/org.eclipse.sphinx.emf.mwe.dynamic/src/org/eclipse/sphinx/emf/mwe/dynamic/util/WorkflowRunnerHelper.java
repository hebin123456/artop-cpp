/**
 * <copyright>
 *
 * Copyright (c) 2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [506671] Add support for specifying and injecting user-defined arguments for workflows through workflow launch configurations
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.mwe.dynamic.util;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.mwe.dynamic.internal.Activator;
import org.eclipse.sphinx.emf.mwe.dynamic.internal.messages.Messages;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.URIExtensions;
import org.eclipse.sphinx.jdt.util.JavaExtensions;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

@SuppressWarnings("restriction")
public class WorkflowRunnerHelper {

	public String toWorkflowString(IType workflowType) {
		Assert.isNotNull(workflowType);

		if (workflowType.isBinary()) {
			return workflowType.getFullyQualifiedName();
		} else {
			return ((SourceType) workflowType).getPath().toString();
		}
	}

	public Object toWorkflowObject(String workflowString) throws ClassNotFoundException, FileNotFoundException {
		if (workflowString == null) {
			return null;
		}
		workflowString = workflowString.trim();
		if (workflowString.isEmpty()) {
			return null;
		}

		Matcher matcher = JavaExtensions.CLASS_NAME_PATTERN.matcher(workflowString);
		if (matcher.find()) {
			// Workflow is a fully qualified Java class name
			try {
				return Class.forName(workflowString);
			} catch (ClassNotFoundException ex) {
				throw new ClassNotFoundException(NLS.bind(Messages.error_workflowClassNotFound, workflowString));
			}
		} else {
			// Workflow is assumed to be a workspace-relative path
			IPath workflowPath = new Path(workflowString);
			// TODO Perform more sophisticated error handling like below
			// TODO Move resulting logic to separate method and use it in toModelURIObject() as well
			// IWorkspace workspace = ResourcesPlugin.getWorkspace();
			// IStatus status = workspace.validateName(workflow, IResource.PROJECT);
			// if (status.isOK()) {
			// IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(workflow);
			// if (!project.exists()) {
			// setErrorMessage(NLS.bind(LauncherMessages.JavaMainTab_20, new String[] { workflow }));
			// return false;
			// }
			// if (!project.isOpen()) {
			// setErrorMessage(NLS.bind(LauncherMessages.JavaMainTab_21, new String[] { workflow }));
			// return false;
			// }
			// } else {
			// setErrorMessage(NLS.bind(Messages.error_illegalWorkflow, new String[] { status.getMessage() }));
			// return false;
			// }
			IFile workflowFile = ResourcesPlugin.getWorkspace().getRoot().getFile(workflowPath);
			if (!workflowFile.exists()) {
				IPath workflowLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(workflowPath);
				throw new FileNotFoundException(NLS.bind(Messages.error_workflowFileDoesNotExist, workflowLocation.toOSString()));
			}
			return workflowFile;
		}
	}

	public String toModelURIString(URI modelURI) {
		Assert.isNotNull(modelURI);

		return modelURI.toString();
	}

	public URI toModelURIObject(String modelURIString) throws FileNotFoundException {
		if (modelURIString == null) {
			return null;
		}
		modelURIString = modelURIString.trim();
		if (modelURIString.isEmpty()) {
			return null;
		}

		URI modelURI = URI.createURI(modelURIString);
		Path modelFilePath = new Path(modelURI.trimFragment().toString());
		URI absoluteModelFileURI = EcorePlatformUtil.createURI(modelFilePath.makeAbsolute());
		if (!EcoreResourceUtil.exists(absoluteModelFileURI)) {
			throw new FileNotFoundException(NLS.bind(Messages.error_modelResourceDoesNotExist, absoluteModelFileURI.toPlatformString(true)));
		}
		return absoluteModelFileURI.appendFragment(modelURI.fragment());
	}

	public Map<String, Object> toArgumentObjects(Map<String, Object> arguments, Map<String, Class<?>> parameters) {
		if (arguments == null) {
			return Collections.emptyMap();
		}

		Map<String, Object> convertedArguments = new HashMap<String, Object>();
		for (String name : arguments.keySet()) {
			Object value = arguments.get(name);

			// Null argument?
			if (value == null) {
				convertedArguments.put(name, null);
				continue;
			}

			// Formal argument?
			if (parameters.containsKey(name)) {
				Class<?> type = parameters.get(name);

				// Argument type different from parameter type?
				if (value.getClass() != type) {
					try {
						// Convert argument to parameter type
						if (value instanceof String) {
							Object convertedValue = EObjectUtil.createFromString(type, (String) value);
							convertedArguments.put(name, convertedValue);
						} else {
							throw new RuntimeException("Unable to convert workflow argument '" + name + "' to '" + type.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					} catch (Exception ex) {
						PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
					}
				} else {
					// Take over argument as is
					convertedArguments.put(name, value);
				}
				continue;
			}

			// Informal string argument?
			if (value instanceof String) {
				Object convertedValue = toArgumentValueObject(name, (String) value);
				convertedArguments.put(name, convertedValue);
				continue;
			}

			// Take over informal object arguments as is
			convertedArguments.put(name, value);
		}
		return convertedArguments;
	}

	public Object toArgumentValueObject(String name, String valueString) {
		Assert.isNotNull(valueString);
		valueString = valueString.trim();

		// Detect and convert boolean values
		if (Boolean.TRUE.toString().equalsIgnoreCase(valueString)) {
			return Boolean.TRUE;
		} else if (Boolean.FALSE.toString().equalsIgnoreCase(valueString)) {
			return Boolean.FALSE;
		}

		// Detect and convert URI values
		URI valueURI = URI.createURI(valueString);
		if (name != null && name.contains(URI.class.getSimpleName())) {
			return valueURI;
		}
		if (URIExtensions.isActual(valueURI)) {
			return valueURI;
		}

		// Take over all other string values as is
		return valueString;
	}
}
