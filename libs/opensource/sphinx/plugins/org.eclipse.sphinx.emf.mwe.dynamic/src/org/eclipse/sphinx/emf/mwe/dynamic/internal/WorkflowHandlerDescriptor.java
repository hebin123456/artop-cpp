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
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.mwe.dynamic.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflow;
import org.eclipse.sphinx.emf.mwe.dynamic.IWorkflowHandler;
import org.eclipse.sphinx.platform.util.ExtensionClassDescriptor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class WorkflowHandlerDescriptor extends ExtensionClassDescriptor<IWorkflowHandler> {

	private final static String NODE_APPLICABLE_FOR = "applicableFor"; //$NON-NLS-1$
	private final static String NODE_INSTANCEOF = "instanceof"; //$NON-NLS-1$
	private final static String ATTR_PRIORITY = "priority"; //$NON-NLS-1$
	private final static String ATTR_VALUE = "value"; //$NON-NLS-1$

	private int priority = -1;
	private Set<String> applicableWorkflowClassNames;

	public WorkflowHandlerDescriptor(IConfigurationElement configurationElement) {
		super(configurationElement);

		String prioAttr = configurationElement.getAttribute(ATTR_PRIORITY);
		if (prioAttr != null && !prioAttr.isEmpty()) {
			try {
				priority = Integer.valueOf(prioAttr);
			} catch (NumberFormatException ex) {
				// Ignore Exception
			}
		}

		initApplicableWorkflows(configurationElement);
	}

	private void initApplicableWorkflows(IConfigurationElement configurationElement) {
		applicableWorkflowClassNames = new HashSet<String>();

		IConfigurationElement[] applicableForElements = configurationElement.getChildren(NODE_APPLICABLE_FOR);
		for (IConfigurationElement applicableFor : applicableForElements) {
			for (IConfigurationElement child : applicableFor.getChildren(NODE_INSTANCEOF)) {
				String workflowClass = child.getAttribute(ATTR_VALUE);
				// Missing workflowClass, continue
				if (workflowClass == null) {
					continue;
				}
				applicableWorkflowClassNames.add(workflowClass);
			}
		}
	}

	public int getPriority() {
		return priority;
	}

	public boolean isApplicableFor(Class<? extends IWorkflow> workflowClass) {
		String workflowClassName = workflowClass.getName();
		for (String applicableWorkflowClassName : applicableWorkflowClassNames) {
			try {
				/*
				 * Performance optimization: Check is the class names are equals before calling Class#forName
				 */
				if (workflowClassName.equals(applicableWorkflowClassName)) {
					return true;
				} else {
					Class<?> applicableWorkflowClass = Class.forName(applicableWorkflowClassName);
					if (applicableWorkflowClass.isAssignableFrom(workflowClass)) {
						return true;
					}
				}
			} catch (ClassNotFoundException ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			}
		}
		return false;
	}
}
