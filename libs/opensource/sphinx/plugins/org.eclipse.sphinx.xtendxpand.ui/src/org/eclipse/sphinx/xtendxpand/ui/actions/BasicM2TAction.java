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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.sphinx.xtendxpand.XpandEvaluationRequest;
import org.eclipse.sphinx.xtendxpand.jobs.XpandJob;
import org.eclipse.sphinx.xtendxpand.outlet.ExtendedOutlet;
import org.eclipse.sphinx.xtendxpand.preferences.OutletsPreference;
import org.eclipse.sphinx.xtendxpand.preferences.PrDefaultExcludesPreference;
import org.eclipse.sphinx.xtendxpand.preferences.PrExcludesPreference;
import org.eclipse.sphinx.xtendxpand.ui.internal.messages.Messages;
import org.eclipse.sphinx.xtendxpand.ui.jobs.ResultMessageHandler;
import org.eclipse.sphinx.xtendxpand.ui.wizards.M2TConfigurationWizard;
import org.eclipse.sphinx.xtendxpand.util.XtendXpandUtil;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.xtend.shared.ui.MetamodelContributor;
import org.eclipse.xtend.shared.ui.core.metamodel.MetamodelContributorRegistry;
import org.eclipse.xtend.typesystem.MetaModel;

public class BasicM2TAction extends BaseSelectionListenerAction {

	/**
	 * The default output folder for code generation.
	 */
	public static final String PROJECT_RELATIVE_DEFAULT_OUTLET_PATH = "gen"; //$NON-NLS-1$

	/**
	 * The default template name.
	 */
	public static final String DEFAULT_TEMPLATE_NAME = "main"; //$NON-NLS-1$

	private IWorkspaceResourceLoader workspaceResourceLoader;

	public BasicM2TAction(String text) {
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

		// Definition to be used for selected model object available right away?
		if (getDefinitionName(modelObject) != null) {
			// Show console and make sure that all system output produced during execution gets displayed there
			ExtendedPlatformUI.showSystemConsole();

			// Schedule model to text transformation job
			Job job = createXpandJob();
			job.setPriority(Job.BUILD);
			IFile file = EcorePlatformUtil.getFile(modelObject);
			if (file != null) {
				job.setRule(file.getProject());
			}
			IJobChangeListener handler = createResultMessageHandler();
			if (handler != null) {
				job.addJobChangeListener(handler);
			}
			job.schedule();
			return;
		}

		// Open wizard that lets user select the definition to be used
		else {
			M2TConfigurationWizard wizard = new M2TConfigurationWizard(modelObject, getMetaModels());
			wizard.setM2TJobName(getM2TJobName());
			wizard.setWorkspaceResourceLoader(getWorkspaceResourceLoader());
			wizard.setOutletsPreference(getOutletsPreference());
			wizard.setDefaultOutlet(getDefaultOutlet());
			wizard.setResultMessageHandler(createResultMessageHandler());

			WizardDialog wizardDialog = new WizardDialog(ExtendedPlatformUI.getDisplay().getActiveShell(), wizard);
			wizardDialog.open();
		}
	}

	protected XpandJob createXpandJob() {
		XpandJob job = new XpandJob(getM2TJobName(), getMetaModels(), getXpandEvaluationRequests());
		job.setWorkspaceResourceLoader(getWorkspaceResourceLoader());
		job.getOutlets().addAll(getOutlets());
		IProject currentProject = EcorePlatformUtil.getFile(getSelectedModelObject()).getProject();
		job.configureProtectedRegionResolver(getPrSrcPaths(getOutlets()), PrDefaultExcludesPreference.INSTANCE.get(currentProject),
				PrExcludesPreference.INSTANCE.get(currentProject));
		job.setPriority(Job.BUILD);
		job.setRule(currentProject);
		return job;
	}

	protected String getPrSrcPaths(Collection<ExtendedOutlet> outlets) {
		// Use a set drop outlet pointing to same physical path
		Set<String> paths = new HashSet<String>();
		StringBuilder builder = new StringBuilder();
		List<ExtendedOutlet> allOutlets = new ArrayList<ExtendedOutlet>(outlets);
		for (ExtendedOutlet outlet : allOutlets) {
			if (outlet.isProtectedRegion()) {
				paths.add(outlet.getPath());
			}
		}
		for (Iterator<String> iter = paths.iterator(); iter.hasNext();) {
			builder.append(iter.next());
			if (iter.hasNext()) {
				builder.append(","); //$NON-NLS-1$
			}
		}
		return builder.toString();
	}

	protected String getM2TJobName() {
		return Messages.job_generatingCode;
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

	protected Collection<XpandEvaluationRequest> getXpandEvaluationRequests() {
		EObject selected = getSelectedModelObject();
		String definitionName = getDefinitionName(selected);
		if (definitionName != null && selected != null) {
			XpandEvaluationRequest request = new XpandEvaluationRequest(definitionName, selected);
			return Collections.singletonList(request);
		}
		return Collections.emptyList();
	}

	protected String getDefinitionName(EObject modelObject) {
		IFile templateFile = getTemplateFile(modelObject);
		if (templateFile != null) {
			return XtendXpandUtil.getQualifiedName(templateFile, getTemplateName());
		}
		return null;
	}

	protected IFile getTemplateFile(EObject modelObject) {
		IFile modelFile = EcorePlatformUtil.getFile(modelObject);
		if (modelFile != null) {
			IPath templatePath = modelFile.getFullPath().removeFileExtension().addFileExtension(IXtendXpandConstants.TEMPLATE_EXTENSION);
			return ResourcesPlugin.getWorkspace().getRoot().getFile(templatePath);
		}
		return null;
	}

	protected String getTemplateName() {
		return DEFAULT_TEMPLATE_NAME;
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

	protected OutletsPreference getOutletsPreference() {
		return null;
	}

	protected Collection<ExtendedOutlet> getOutlets() {
		// Return outlets resulting outlets preference if available
		OutletsPreference outletsPreference = getOutletsPreference();
		if (outletsPreference != null) {
			IFile file = EcorePlatformUtil.getFile(getSelectedModelObject());
			if (file != null && file.getProject() != null) {
				return outletsPreference.get(file.getProject());
			}
		}

		// Return default outlet otherwise
		ExtendedOutlet defaultOutlet = getDefaultOutlet();
		if (defaultOutlet != null) {
			return Collections.singletonList(defaultOutlet);
		}

		return Collections.<ExtendedOutlet> emptyList();
	}

	protected ExtendedOutlet getDefaultOutlet() {
		IFile file = EcorePlatformUtil.getFile(getStructuredSelection().getFirstElement());
		if (file != null) {
			IProject project = file.getProject();
			IContainer defaultOutletContainer;
			String projectRelativeDefaultOutletPath = getProjectRelativeDefaultOutletPath();
			if (projectRelativeDefaultOutletPath != null && projectRelativeDefaultOutletPath.length() > 0) {
				defaultOutletContainer = project.getFolder(projectRelativeDefaultOutletPath);
			} else {
				defaultOutletContainer = project;
			}
			return new ExtendedOutlet(defaultOutletContainer);
		}
		return null;
	}

	protected String getProjectRelativeDefaultOutletPath() {
		return PROJECT_RELATIVE_DEFAULT_OUTLET_PATH;
	}
}