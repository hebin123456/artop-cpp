/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [343844] Enable multiple Xtend MetaModels to be configured on BasicM2xAction, M2xConfigurationWizard, and Xtend/Xpand/CheckJob
 *     itemis - [343847] Use Xtend MetaModels configured in project settings when running BasicM2xAction or M2xConfigurationWizard
 *     itemis - [358082] Precedence of Xtend MetaModels gets lost in Xtend/Xpand runtime enhancements implemented in Sphinx
 *     itemis - [358591] ResultObjectHandler and ResultMessageHandler used by M2xConfigurationWizards are difficult to customize and should be usable in BasicM2xActions too
 * 
 * </copyright>
 */
package org.eclipse.sphinx.xtendxpand.ui.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.mwe.IXtendXpandConstants;
import org.eclipse.sphinx.emf.mwe.resources.BasicWorkspaceResourceLoader;
import org.eclipse.sphinx.emf.mwe.resources.IWorkspaceResourceLoader;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.sphinx.xtend.typesystem.emf.SphinxManagedEmfMetaModel;
import org.eclipse.sphinx.xtendxpand.XtendEvaluationRequest;
import org.eclipse.sphinx.xtendxpand.jobs.SaveAsNewFileHandler;
import org.eclipse.sphinx.xtendxpand.jobs.XtendJob;
import org.eclipse.sphinx.xtendxpand.ui.internal.messages.Messages;
import org.eclipse.sphinx.xtendxpand.ui.jobs.ResultMessageHandler;
import org.eclipse.sphinx.xtendxpand.ui.wizards.M2MConfigurationWizard;
import org.eclipse.sphinx.xtendxpand.util.XtendXpandUtil;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.xtend.shared.ui.MetamodelContributor;
import org.eclipse.xtend.shared.ui.core.metamodel.MetamodelContributorRegistry;
import org.eclipse.xtend.typesystem.MetaModel;

public class BasicM2MAction extends BaseSelectionListenerAction {

	public static final String DEFAULT_FUNCTION_NAME = "transform"; //$NON-NLS-1$

	private IWorkspaceResourceLoader workspaceResourceLoader;

	public BasicM2MAction(String text) {
		super(text);
	}

	protected EObject getSelectedModelObject() {
		Object selected = getStructuredSelection().getFirstElement();
		if (selected instanceof EObject) {
			return (EObject) selected;
		}
		Resource resource = EcorePlatformUtil.getResource(selected);
		if (resource != null && !resource.getContents().isEmpty()) {
			return resource.getContents().get(0);
		}
		return null;
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return selection.size() == 1 && getSelectedModelObject() != null;
	}

	@Override
	public void run() {
		EObject modelObject = getSelectedModelObject();

		// Extension to be used for selected model object available right away?
		if (getExtensionName(modelObject) != null) {
			// Show console and make sure that all system output produced during execution gets displayed there
			ExtendedPlatformUI.showSystemConsole();

			// Schedule model to model transformation job
			Job job = createXtendJob();
			job.setPriority(Job.BUILD);
			IJobChangeListener resultObjectHandler = createResultObjectHandler();
			if (resultObjectHandler != null) {
				job.addJobChangeListener(resultObjectHandler);
			}
			IJobChangeListener resultMessageHandler = createResultMessageHandler();
			if (resultMessageHandler != null) {
				job.addJobChangeListener(resultMessageHandler);
			}
			job.schedule();
		}

		// Open wizard that lets user select the extension to be used
		else {
			M2MConfigurationWizard wizard = new M2MConfigurationWizard(modelObject, getMetaModels());
			wizard.setM2MJobName(getM2MJobName());
			wizard.setWorkspaceResourceLoader(getWorkspaceResourceLoader());
			wizard.setResultObjectHandler(createResultObjectHandler());
			wizard.setResultMessageHandler(createResultMessageHandler());

			WizardDialog wizardDialog = new WizardDialog(ExtendedPlatformUI.getDisplay().getActiveShell(), wizard);
			wizardDialog.open();
		}
	}

	protected XtendJob createXtendJob() {
		XtendJob job = new XtendJob(getM2MJobName(), getMetaModels(), getXtendEvaluationRequests());
		job.setWorkspaceResourceLoader(getWorkspaceResourceLoader());
		job.setPriority(Job.BUILD);
		job.setRule(EcorePlatformUtil.getFile(getSelectedModelObject()).getProject());
		return job;
	}

	protected String getM2MJobName() {
		return Messages.job_transformingModel;
	}

	protected IJobChangeListener createResultObjectHandler() {
		return new SaveAsNewFileHandler();
	}

	protected IJobChangeListener createResultMessageHandler() {
		return new ResultMessageHandler();
	}

	protected List<MetaModel> getMetaModels() {
		/*
		 * !! Important Note !! We must use a list for storing the Xtend MetaModels and passing them around because the
		 * order of the Xtend MetaModels in the list defines their precedence (Xtend MetaModel with the lowest list
		 * position has the highest precedence). The precedence is vital when multiple Xtend MetaModels are applicable
		 * to - i.e., are able to return an Xtend type for - the same model object under evaluation. The Xtend MetaModel
		 * being used in such cases is that with the highest precedence.
		 */
		List<MetaModel> metaModels = new ArrayList<MetaModel>();

		// Add metamodels defined by Xtend/Xpand preferences
		IFile file = EcorePlatformUtil.getFile(getSelectedModelObject());
		IJavaProject javaProject = JavaCore.create(file.getProject());
		List<? extends MetamodelContributor> contributors = MetamodelContributorRegistry.getActiveMetamodelContributors(javaProject);
		for (MetamodelContributor contributor : contributors) {
			metaModels.addAll(Arrays.asList(contributor.getMetamodels(javaProject, null)));
		}

		// Add metamodel resulting from models in workspace if no such are available in Xtend/Xpand preferences
		if (metaModels.isEmpty()) {
			IModelDescriptor model = ModelDescriptorRegistry.INSTANCE.getModel(file);
			metaModels.add(new SphinxManagedEmfMetaModel(model));
		}

		return Collections.unmodifiableList(metaModels);
	}

	protected Collection<XtendEvaluationRequest> getXtendEvaluationRequests() {
		EObject selected = getSelectedModelObject();
		String extensionName = getExtensionName(selected);
		if (extensionName != null && selected != null) {
			XtendEvaluationRequest request = new XtendEvaluationRequest(extensionName, selected);
			return Collections.singletonList(request);
		}
		return Collections.emptyList();
	}

	protected String getExtensionName(EObject modelObject) {
		IFile extensionFile = getExtensionFile(modelObject);
		if (extensionFile != null) {
			return XtendXpandUtil.getQualifiedName(extensionFile, getFunctionName());
		}
		return null;
	}

	protected IFile getExtensionFile(EObject modelObject) {
		IFile modelFile = EcorePlatformUtil.getFile(modelObject);
		if (modelFile != null) {
			IPath templatePath = modelFile.getFullPath().removeFileExtension().addFileExtension(IXtendXpandConstants.EXTENSION_EXTENSION);
			return ResourcesPlugin.getWorkspace().getRoot().getFile(templatePath);
		}
		return null;
	}

	protected String getFunctionName() {
		return DEFAULT_FUNCTION_NAME;
	}

	protected IWorkspaceResourceLoader getWorkspaceResourceLoader() {
		if (workspaceResourceLoader == null) {
			workspaceResourceLoader = createWorkspaceResourceLoader();
		}
		return workspaceResourceLoader;
	}

	protected IWorkspaceResourceLoader createWorkspaceResourceLoader() {
		return new BasicWorkspaceResourceLoader();
	}
}