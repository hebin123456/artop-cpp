/**
 * <copyright>
 * 
 * Copyright (c) 2011-2013 See4sys, itemis and others.
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
package org.eclipse.sphinx.emf.mwe.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;

// TODO Create ModelSaver as counterpart which takes a collection of model objects and saves them using
// org.eclipse.sphinx.emf.util.EcorePlatformUtil.saveNewModelResources(TransactionalEditingDomain, Collection<ModelResourceDescriptor>, boolean, IProgressMonitor)
public class ModelLoader extends WorkflowComponentWithModelSlot {

	private String projectName;
	private String metaModelDescriptorId;

	@Override
	public void checkConfiguration(Issues issues) {
		super.checkConfiguration(issues);
		checkRequiredConfigProperty("project", projectName, issues); //$NON-NLS-1$
		checkRequiredConfigProperty("metaModelDescriptorId", metaModelDescriptorId, issues); //$NON-NLS-1$
	}

	@Override
	protected void invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		// Wait until initialization jobs have completed (required for safely accessing models in
		// ModelDescriptorRegistry)
		// TODO Create FAMILY_INITIALIZATION and add it to ModelDescriptorRegistryInitializer so as to enable this
		// kind of jobs being focused more specifically
		try {
			Job.getJobManager().join(IExtendedPlatformConstants.FAMILY_LONG_RUNNING, null);
		} catch (Exception ex) {
			// Ignore exception
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);

		if (!project.exists()) {
			issues.addError("Project '" + projectName + "' does not exist in the workspace " + workspace.getRoot().getLocation().toFile()); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if (!project.isAccessible()) {
			issues.addError("Project '" + projectName + "' is not accessible in the workspace " + workspace.getRoot().getLocation().toFile()); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		List<EObject> modelRoots = new ArrayList<EObject>();
		IMetaModelDescriptor mmd = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(metaModelDescriptorId);
		if (mmd == null) {
			issues.addError("Metamodel Descriptor '" + metaModelDescriptorId + "' unknown"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		Collection<IModelDescriptor> models = ModelDescriptorRegistry.INSTANCE.getModels(project, mmd);
		if (models.isEmpty()) {
			issues.addError("Project '" + projectName + "' does not contain any " + mmd.getName() + " models"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return;
		}

		// TODO Rework to support the case where multiple models for same metamodel descriptor exist in given project
		IModelDescriptor modelDescriptor = models.iterator().next();

		ModelLoadManager.INSTANCE.loadModel(modelDescriptor, false, null);

		for (Resource resource : modelDescriptor.getLoadedResources(true)) {
			modelRoots.addAll(resource.getContents());
			issues.addInfo("Loaded resource " + resource.getURI()); //$NON-NLS-1$
		}
		if (modelRoots.isEmpty()) {
			issues.addError("Project '" + projectName + "' does not contain" + mmd.getName() + "resources"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return;
		}
		ctx.set(getModelSlot(), modelRoots);
	}

	public void setProject(String projectName) {
		this.projectName = projectName;
	}

	public String getProject() {
		return projectName;
	}

	public void setMetaModelDescriptor(String metaModelDescriptorId) {
		this.metaModelDescriptorId = metaModelDescriptorId;
	}

	public String getMetaModelDesriptor() {
		return metaModelDescriptorId;
	}
}
