/**
 * <copyright>
 *
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - Renamed ProjectStatisticsAction to GenerateModelStatisticsReportAction
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.actions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.examples.actions.internal.Activator;
import org.eclipse.sphinx.examples.actions.internal.messages.Messages;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.ide.IDE;

/**
 * This example action shows how to navigate over a {@link IModelDescriptor model} starting from a {@link IProject
 * project} selected by the user. It creates a statistic report for the {@link IModelDescriptor model}s in the selected
 * {@link IProject project} by counting the number of model objects per model object type.
 */
public class GenerateModelStatisticsReportAction extends BaseSelectionListenerAction {

	/**
	 * Constructor.
	 */
	public GenerateModelStatisticsReportAction() {
		super(Messages.act_GenerateModelStatisticsReport_label);
	}

	@Override
	public boolean updateSelection(IStructuredSelection selection) {
		// Action is only available on single project selection
		if (selection.size() == 1) {
			if (selection.getFirstElement() instanceof IProject) {
				// The selected project
				IProject project = (IProject) selection.getFirstElement();
				// Returns true if project has at least one model
				return ModelDescriptorRegistry.INSTANCE.getModels(project).size() > 0;
			}
		}
		return false;
	}

	@Override
	public void run() {
		IProject project = null;
		// We map objects base on their type
		final Map<Class<?>, Set<Object>> objectsPerType = new HashMap<Class<?>, Set<Object>>();
		final int totalObjects[] = { 0 };
		final int totalResources[] = { 0 };
		if (getStructuredSelection().getFirstElement() instanceof IProject) {
			project = (IProject) getStructuredSelection().getFirstElement();

			for (final IModelDescriptor modelDescriptor : ModelDescriptorRegistry.INSTANCE.getModels(project)) {
				try {
					modelDescriptor.getEditingDomain().runExclusive(new Runnable() {
						@Override
						public void run() {
							for (Resource resource : modelDescriptor.getLoadedResources(true)) {
								totalResources[0]++;
								// Go through all contents within the resource and map them base on their type
								for (TreeIterator<EObject> allContent = resource.getAllContents(); allContent.hasNext();) {
									EObject eObject = allContent.next();
									Class<?> type = eObject.getClass();
									Set<Object> objects = objectsPerType.get(type);
									if (objects == null) {
										objects = new HashSet<Object>();
										objectsPerType.put(type, objects);
									}
									if (objects.add(eObject)) {
										totalObjects[0]++;
									}
								}
							}
						}
					});
				} catch (Exception ex) {
					// Ignore exception, just continue with next model
				}
			}

			// Prepare the statistic report
			String fileName = "Statistic.txt"; //$NON-NLS-1$
			// Check if the report file existed
			if (project.getFile(fileName).exists()) {
				// Get a new file name
				InputDialog dialog = new InputDialog(ExtendedPlatformUI.getActiveShell(),
						Messages.dlg_GenerateModelStatisticsReport_fileAlreadyExists_title,
						Messages.dlg_GenerateModelStatisticsReport_fileAlreadyExists_desc, fileName, null);
				dialog.setBlockOnOpen(true);
				int result = dialog.open();
				if (result == Window.OK) {
					fileName = dialog.getValue();
				} else {
					fileName = null;
				}
			}
			if (fileName != null) {
				try {
					writeStatisticReport(project, objectsPerType, totalObjects[0], fileName, totalResources[0]);
					// Refresh the viewer and open report file in the editor
					project.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
					IFile file = project.getFile(fileName);
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					if (page != null) {
						IDE.openEditor(page, file, true);
					}
				} catch (Exception ex) {
					PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
				}
			}
		}
	}

	/**
	 * Write a statistic report file within the project folder.
	 *
	 * @param project
	 *            The project which will be made a statistic report.
	 * @param objectsPerType
	 *            The map of objects base on their type.
	 * @param totalObjects
	 *            Number of objects within the project.
	 * @param fileName
	 *            Report file name.
	 * @throws IOException
	 */
	private void writeStatisticReport(IProject project, Map<Class<?>, Set<Object>> objectsPerType, int totalObjects, String fileName, int totalFile)
			throws IOException {
		FileWriter fw = new FileWriter(project.getLocation().toOSString() + File.separator + fileName);
		PrintWriter out = new PrintWriter(fw);
		String message = NLS.bind(Messages.act_GenerateModelStatisticsReport_result_ProjectName, project.getName());
		out.println(message);
		message = NLS
				.bind(Messages.act_GenerateModelStatisticsReport_result_Summary, new Object[] { totalObjects, objectsPerType.size(), totalFile });
		out.println(message);
		// Format the output
		String format = "| %1$-10s| %2$-100s|\n"; //$NON-NLS-1$

		out.printf(format, Messages.act_GenerateModelStatisticsReport_result_columLabel_Quantity,
				Messages.act_GenerateModelStatisticsReport_result_columLabel_Type);
		for (Class<?> type : objectsPerType.keySet()) {
			out.printf(format, objectsPerType.get(type).size(), type.getCanonicalName());
		}
		out.print(Messages.act_GenerateModelStatisticsReport_result_eof);
		out.close();
	}
}
