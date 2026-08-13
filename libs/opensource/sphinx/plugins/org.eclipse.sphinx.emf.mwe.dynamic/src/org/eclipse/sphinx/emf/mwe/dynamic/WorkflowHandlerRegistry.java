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
package org.eclipse.sphinx.emf.mwe.dynamic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflow;
import org.eclipse.sphinx.emf.mwe.dynamic.internal.Activator;
import org.eclipse.sphinx.emf.mwe.dynamic.internal.WorkflowHandlerDescriptor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.StatusUtil;

import com.google.common.primitives.Ints;

public class WorkflowHandlerRegistry {

	private static final String EXTP_WORKFLOW_HANDLERS = Activator.INSTANCE.getSymbolicName() + ".workflowHandlers"; //$NON-NLS-1$
	private static final String ELEM_HANDLER = "handler"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	private IExtensionRegistry extensionRegistry;
	private ILog log;

	private Set<WorkflowHandlerDescriptor> workflowHandlerDescriptors;
	private Map<Class<IWorkflow>, List<IWorkflowHandler>> workflowClassToWorkflowHandlerDescriptorsMap;

	/**
	 * The singleton instance of this registry.
	 */
	public static final WorkflowHandlerRegistry INSTANCE = new WorkflowHandlerRegistry(Platform.getExtensionRegistry(),
			PlatformLogUtil.getLog(Activator.getPlugin()));

	private WorkflowHandlerRegistry(IExtensionRegistry extensionRegistry, ILog log) {
		Assert.isNotNull(extensionRegistry);
		Assert.isNotNull(log);

		this.extensionRegistry = extensionRegistry;
		this.log = log;
		initialize();
	}

	/**
	 * Initialize internal data by reading from platform registry
	 */
	private void initialize() {
		if (extensionRegistry == null) {
			return;
		}

		if (workflowHandlerDescriptors == null) {
			synchronized (this) {
				workflowHandlerDescriptors = new HashSet<WorkflowHandlerDescriptor>();
				workflowClassToWorkflowHandlerDescriptorsMap = new HashMap<Class<IWorkflow>, List<IWorkflowHandler>>();

				IConfigurationElement[] elements = extensionRegistry.getConfigurationElementsFor(EXTP_WORKFLOW_HANDLERS);
				for (IConfigurationElement element : elements) {
					if (!element.getName().equals(ELEM_HANDLER)) {
						continue;
					}

					// Ensure that handler class is not missing
					String handlerClass = element.getAttribute(ATTR_CLASS);
					if (handlerClass == null || handlerClass.isEmpty()) {
						String msg = "Missing handler class in " + EXTP_WORKFLOW_HANDLERS + " extension from " //$NON-NLS-1$ //$NON-NLS-2$
								+ element.getContributor().getName();
						IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), new RuntimeException(msg));
						log.log(status);
						continue;
					}

					workflowHandlerDescriptors.add(new WorkflowHandlerDescriptor(element));
				}
			}
		}
	}

	public List<IWorkflowHandler> getSortedHandlers(Class<? extends IWorkflow> workflowClass) {
		List<IWorkflowHandler> result = workflowClassToWorkflowHandlerDescriptorsMap.get(workflowClass);
		if (result == null) {
			result = new ArrayList<IWorkflowHandler>();
			List<WorkflowHandlerDescriptor> handlerDescriptors = getHandlerDescriptors(workflowClass);
			Collections.sort(handlerDescriptors, new Comparator<WorkflowHandlerDescriptor>() {

				@Override
				public int compare(WorkflowHandlerDescriptor desc1, WorkflowHandlerDescriptor desc2) {
					return Ints.compare(desc1.getPriority(), desc2.getPriority());
				}
			});
			for (WorkflowHandlerDescriptor handlerDesc : handlerDescriptors) {
				try {
					result.add(handlerDesc.getInstance());
				} catch (Exception ex) {
					IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
					log.log(status);
				}
			}
		}
		return result;
	}

	private List<WorkflowHandlerDescriptor> getHandlerDescriptors(Class<? extends IWorkflow> workflowClass) {
		List<WorkflowHandlerDescriptor> result = new ArrayList<WorkflowHandlerDescriptor>();
		for (WorkflowHandlerDescriptor handlerDesc : workflowHandlerDescriptors) {
			if (handlerDesc.isApplicableFor(workflowClass)) {
				result.add(handlerDesc);
			}
		}
		return result;
	}
}
