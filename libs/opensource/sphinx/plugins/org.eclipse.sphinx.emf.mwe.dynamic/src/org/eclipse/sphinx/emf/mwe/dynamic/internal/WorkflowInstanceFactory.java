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
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.mwe.dynamic.internal;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent;
import org.eclipse.emf.mwe2.runtime.workflow.Workflow;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.sphinx.emf.mwe.dynamic.WorkflowContributorRegistry;
import org.eclipse.sphinx.emf.mwe.dynamic.WorkspaceWorkflow;
import org.eclipse.sphinx.emf.mwe.dynamic.annotations.WorkflowParameter;
import org.eclipse.sphinx.emf.mwe.dynamic.util.XtendUtil;
import org.eclipse.sphinx.jdt.loaders.ProjectClassLoader;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.StatusUtil;

public class WorkflowInstanceFactory {

	public Workflow createWorkflowInstance(Object workflow) throws CoreException {
		try {
			// Retrieve workflow class
			Class<Workflow> workflowClass = getWorkflowClass(workflow);
			if (workflowClass == null) {
				return null;
			}

			// Create workflow instance
			return workflowClass.newInstance();
		} catch (CoreException ex) {
			throw ex;
		} catch (Exception ex) {
			IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
			throw new CoreException(status);
		}
	}

	public Workflow createWorkflowInstance(Object workflow, Map<String, Object> arguments) throws CoreException {
		// Create workflow instance
		Workflow workflowInstance = createWorkflowInstance(workflow);

		// Inject arguments into workflow instance and its components as appropriate
		injectArguments(workflowInstance, arguments);
		return workflowInstance;
	}

	protected Class<Workflow> getWorkflowClass(Object workflow) throws CoreException {
		IType workflowType = getWorkflowType(workflow);
		if (workflowType != null) {
			return loadWorkflowClass(workflowType);
		}
		return doGetWorkflowClass(workflow);
	}

	protected Class<Workflow> doGetWorkflowClass(Object workflow) throws CoreException {
		if (workflow instanceof Class<?>) {
			Class<?> clazz = (Class<?>) workflow;
			if (!Workflow.class.isAssignableFrom(clazz)) {
				Exception ex = new IllegalStateException("Workflow class '" + clazz.getName() + "' is not a subclass of " + Workflow.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
				IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
				throw new CoreException(status);
			}
			@SuppressWarnings("unchecked")
			Class<Workflow> workflowClass = (Class<Workflow>) clazz;
			return workflowClass;
		} else {

		}
		return null;
	}

	protected IType getWorkflowType(Object workflow) {
		if (workflow instanceof IType) {
			return (IType) workflow;
		}
		if (workflow instanceof ICompilationUnit) {
			return ((ICompilationUnit) workflow).findPrimaryType();
		}
		if (workflow instanceof IFile) {
			IJavaElement workflowJavaElement = XtendUtil.getJavaElement((IFile) workflow);
			if (workflowJavaElement instanceof ICompilationUnit) {
				return ((ICompilationUnit) workflowJavaElement).findPrimaryType();
			}
		}
		return null;
	}

	protected Class<Workflow> loadWorkflowClass(IType workflowType) throws CoreException {
		Assert.isNotNull(workflowType);

		try {
			// Find out where given workflow type originates from
			if (!workflowType.isBinary()) {
				// Workflow type refers to an on-the-fly compiled Java class in the runtime workspace

				// Create project class loader capable of loading Java class behind workflow type from underlying Java
				// or plug-in project in runtime workspace
				ProjectClassLoader projectClassLoader = new ProjectClassLoader(workflowType.getJavaProject());

				// TODO Surround with appropriate tracing option
				// ClassLoaderExtensions.printHierarchy(projectClassLoader);

				// Use project class loader to load Java class behind workflow type
				Class<?> clazz = projectClassLoader.loadClass(workflowType.getFullyQualifiedName());
				if (!Workflow.class.isAssignableFrom(clazz)) {
					throw new IllegalStateException("Workflow class '" + clazz.getName() + "' is not a subclass of " + Workflow.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
				}
				@SuppressWarnings("unchecked")
				Class<Workflow> workflowClass = (Class<Workflow>) clazz;
				return workflowClass;
			} else {
				// Workflow type refers to a binary Java class from the running Eclipse instance

				// Load Java class behind workflow type from underlying contributor plug-in
				return WorkflowContributorRegistry.INSTANCE.loadContributedWorkflowClass(workflowType);
			}
		} catch (CoreException ex) {
			throw ex;
		} catch (Exception ex) {
			IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
			throw new CoreException(status);
		}
	}

	public Map<String, Class<?>> getWorkflowParameters(Workflow workflowInstance) {
		// Retrieve parameters of workflow class itself
		Map<String, Class<?>> workflowParameters = doGetWorkflowParameters(workflowInstance.getClass());

		// Retrive parameters of workflow components if any
		if (workflowInstance instanceof WorkspaceWorkflow) {
			List<IWorkflowComponent> children = ((WorkspaceWorkflow) workflowInstance).getChildren();
			for (IWorkflowComponent component : children) {
				workflowParameters.putAll(doGetWorkflowParameters(component.getClass()));
			}
		}
		return workflowParameters;
	}

	protected Map<String, Class<?>> doGetWorkflowParameters(Class<?> clazz) {
		// Establish ordered map of workflow parameters
		Map<String, Class<?>> workflowParameters = new LinkedHashMap<>();
		for (Field field : clazz.getDeclaredFields()) {
			if (isWorkflowParameter(field)) {
				String name = getWorkflowParameterName(field);
				workflowParameters.put(name, field.getType());
			}
		}
		return workflowParameters;
	}

	protected boolean isWorkflowParameter(Field field) {
		Assert.isNotNull(field);

		return field.getAnnotation(WorkflowParameter.class) != null;
	}

	protected String getWorkflowParameterName(Field parameterField) {
		Assert.isNotNull(parameterField);
		Assert.isLegal(isWorkflowParameter(parameterField));

		// Retrieve workflow parameter name - use field name if no such has been annotated explicitly
		WorkflowParameter annotation = parameterField.getAnnotation(WorkflowParameter.class);
		String value = annotation.value();
		return value != null && !value.isEmpty() ? value : parameterField.getName();
	}

	protected boolean isWorkflowParameterTypeAssignableFrom(Class<?> parameterType, Class<?> valueType) {
		if (parameterType.isPrimitive()) {
			return ClassUtils.isAssignable(parameterType, valueType, true);
		}
		return parameterType.isAssignableFrom(valueType);
	}

	protected void setWorkflowParameterValue(Object object, Field parameterField, Object value) throws CoreException {
		Assert.isNotNull(object);
		Assert.isNotNull(parameterField);
		Assert.isLegal(isWorkflowParameter(parameterField));

		boolean oldAccessible = parameterField.isAccessible();
		parameterField.setAccessible(true);
		try {
			parameterField.set(object, value);
		} catch (Exception ex) {
			IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
			throw new CoreException(status);
		} finally {
			parameterField.setAccessible(oldAccessible);
		}
	}

	public void injectArguments(Workflow workflowInstance, Map<String, Object> arguments) throws CoreException {
		Assert.isNotNull(workflowInstance);

		// Inject arguments into workflow itself
		doInjectArguments(workflowInstance, arguments);

		// Inject arguments into workflow components
		if (workflowInstance instanceof WorkspaceWorkflow) {
			List<IWorkflowComponent> children = ((WorkspaceWorkflow) workflowInstance).getChildren();
			for (IWorkflowComponent component : children) {
				doInjectArguments(component, arguments);
			}
		}
	}

	protected void doInjectArguments(Object object, Map<String, Object> arguments) throws CoreException {
		Class<?> clazz = object.getClass();
		for (Field field : clazz.getDeclaredFields()) {
			if (isWorkflowParameter(field)) {
				String name = getWorkflowParameterName(field);
				Object value = arguments.get(name);
				if (value != null) {
					if (isWorkflowParameterTypeAssignableFrom(field.getType(), value.getClass())) {
						setWorkflowParameterValue(object, field, value);
					} else {
						RuntimeException ex = new RuntimeException("Workflow argument '" + name + " : " + value.getClass().getName() //$NON-NLS-1$ //$NON-NLS-2$
								+ "' cannot be assigned to workflow parameter '" + field.getName() + " : " + field.getType().getName() //$NON-NLS-1$ //$NON-NLS-2$
								+ "'"); //$NON-NLS-1$
						PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
					}
				}
			}
		}
	}
}
