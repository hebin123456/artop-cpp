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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.validation.markers.ValidationMarkerManager;
import org.eclipse.sphinx.emf.validation.ui.util.Messages;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public class BasicCleanProblemMarkersAction extends BaseSelectionListenerAction {

	public BasicCleanProblemMarkersAction() {
		super(Messages._UI_Clean_menu_item);
		setDescription(Messages._UI_Clean_simple_description);
	}

	@Override
	public void run() {

		final List<EObject> selectedModelObjects = getSelectedModelObjects();

		if (!selectedModelObjects.isEmpty()) {
			WorkspaceJob job = new WorkspaceJob(Messages._Job_Clean_Markers) {
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
					for (EObject eObject : selectedModelObjects) {
						ValidationMarkerManager.getInstance().removeMarkers(eObject, EObjectUtil.DEPTH_INFINITE, IMarker.PROBLEM);
					}
					return Status.OK_STATUS;
				}
			};

			ArrayList<ISchedulingRule> myRules = new ArrayList<ISchedulingRule>();
			for (EObject eObject : selectedModelObjects) {
				IFile file = EcorePlatformUtil.getFile(eObject);
				if (file != null) {
					IResourceRuleFactory ruleFactory = file.getWorkspace().getRuleFactory();
					myRules.add(ruleFactory.modifyRule(file));
					myRules.add(ruleFactory.createRule(file));
				}
			}

			job.setRule(new MultiRule(myRules.toArray(new ISchedulingRule[myRules.size()])));
			job.setPriority(Job.BUILD);
			job.schedule();
		}

	}

	/**
	 * Due to performance overhead, its just called before running the action to init the list of selected model objects
	 * 
	 * @param selection
	 *            the current selection
	 */

	protected List<EObject> getSelectedModelObjects() {
		IStructuredSelection selection = getStructuredSelection();
		List<IFile> files = new ArrayList<IFile>();
		List<EObject> result = new ArrayList<EObject>();
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
