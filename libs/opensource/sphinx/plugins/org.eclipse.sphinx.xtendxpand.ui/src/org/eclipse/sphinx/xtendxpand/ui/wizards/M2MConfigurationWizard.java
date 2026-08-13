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
 *     itemis - [357813] Risk of NullPointerException when transforming models using M2MConfigurationWizard
 *     itemis - [358082] Precedence of Xtend MetaModels gets lost in Xtend/Xpand runtime enhancements implemented in Sphinx
 *     itemis - [358591] ResultObjectHandler and ResultMessageHandler used by M2xConfigurationWizards are difficult to customize and should be usable in BasicM2xActions too
 * 
 * </copyright>
 */
package org.eclipse.sphinx.xtendxpand.ui.wizards;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.emf.mwe.resources.IWorkspaceResourceLoader;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.sphinx.platform.ui.wizards.AbstractWizard;
import org.eclipse.sphinx.xtendxpand.jobs.CheckJob;
import org.eclipse.sphinx.xtendxpand.jobs.M2MJob;
import org.eclipse.sphinx.xtendxpand.jobs.XtendJob;
import org.eclipse.sphinx.xtendxpand.ui.internal.Activator;
import org.eclipse.sphinx.xtendxpand.ui.internal.messages.Messages;
import org.eclipse.sphinx.xtendxpand.ui.wizards.pages.CheckConfigurationPage;
import org.eclipse.sphinx.xtendxpand.ui.wizards.pages.XtendConfigurationPage;
import org.eclipse.xtend.expression.TypeSystem;
import org.eclipse.xtend.expression.TypeSystemImpl;
import org.eclipse.xtend.typesystem.MetaModel;

public class M2MConfigurationWizard extends AbstractWizard {

	protected EObject modelObject;
	protected TypeSystem typeSystem;

	private String m2mJobName;
	private IWorkspaceResourceLoader workspaceResourceLoader;
	private IJobChangeListener resultObjectHandler;
	private IJobChangeListener resultMessageHandler;

	protected XtendConfigurationPage xtendConfigurationPage;
	protected CheckConfigurationPage checkConfigurationPage;

	public M2MConfigurationWizard(EObject modelObject, List<MetaModel> metaModels) {
		Assert.isNotNull(metaModels);

		setDialogSettings(Activator.getDefault().getDialogSettings());
		setWindowTitle(Messages.title_modelTransformation);

		this.modelObject = modelObject;

		typeSystem = new TypeSystemImpl();
		for (MetaModel metaModel : metaModels) {
			((TypeSystemImpl) typeSystem).registerMetaModel(metaModel);
		}
	}

	public String getM2MJobName() {
		return m2mJobName != null ? m2mJobName : getDefaultM2MJobName();
	}

	protected String getDefaultM2MJobName() {
		return Messages.job_transformingModel;
	}

	public void setM2MJobName(String m2mJobName) {
		this.m2mJobName = m2mJobName;
	}

	public IWorkspaceResourceLoader getWorkspaceResourceLoader() {
		return workspaceResourceLoader;
	}

	public void setWorkspaceResourceLoader(IWorkspaceResourceLoader resourceLoader) {
		workspaceResourceLoader = resourceLoader;
	}

	public IJobChangeListener getResultObjectHandler() {
		return resultObjectHandler;
	}

	public void setResultObjectHandler(IJobChangeListener resultObjectHandler) {
		this.resultObjectHandler = resultObjectHandler;
	}

	public IJobChangeListener getResultMessageHandler() {
		return resultMessageHandler;
	}

	public void setResultMessageHandler(IJobChangeListener resultMessageHandler) {
		this.resultMessageHandler = resultMessageHandler;
	}

	@Override
	public void addPages() {
		xtendConfigurationPage = createXtendConfigurationPage();
		addPage(xtendConfigurationPage);
		checkConfigurationPage = createCheckConfigurationPage();
		addPage(checkConfigurationPage);
	}

	protected XtendConfigurationPage createXtendConfigurationPage() {
		XtendConfigurationPage xtendPage = new XtendConfigurationPage(Messages.label_xtendPageName);
		xtendPage.init(modelObject, typeSystem);
		return xtendPage;
	}

	protected CheckConfigurationPage createCheckConfigurationPage() {
		CheckConfigurationPage checkPage = new CheckConfigurationPage(Messages.label_xtendPageName);
		checkPage.init(modelObject);
		return checkPage;
	}

	@Override
	protected void doPerformFinish(IProgressMonitor monitor) throws CoreException {
		ExtendedPlatformUI.showSystemConsole();

		CheckJob checkJob = isCheckRequired() ? createCheckJob() : null;
		XtendJob xtendJob = createXtendJob();

		M2MJob job = new M2MJob(getM2MJobName(), xtendJob, checkJob);
		job.setPriority(Job.BUILD);
		IJobChangeListener resultObjectHandler = getResultObjectHandler();
		if (resultObjectHandler != null) {
			job.addJobChangeListener(resultObjectHandler);
		}
		IJobChangeListener resultMessageHandler = getResultMessageHandler();
		if (resultMessageHandler != null) {
			job.addJobChangeListener(resultMessageHandler);
		}
		job.schedule();
	}

	protected boolean isCheckRequired() {
		return checkConfigurationPage.isCheckEnabled() && !checkConfigurationPage.getCheckEvaluationRequests().isEmpty();
	}

	protected CheckJob createCheckJob() {
		CheckJob checkJob = new CheckJob(getM2MJobName(), typeSystem, checkConfigurationPage.getCheckEvaluationRequests());
		checkJob.setWorkspaceResourceLoader(getWorkspaceResourceLoader());
		checkJob.setPriority(Job.BUILD);
		IFile file = EcorePlatformUtil.getFile(modelObject);
		if (file != null) {
			checkJob.setRule(file.getProject());
		}
		return checkJob;
	}

	protected XtendJob createXtendJob() {
		XtendJob job = new XtendJob(getM2MJobName(), typeSystem, xtendConfigurationPage.getXtendEvaluationRequests());
		job.setWorkspaceResourceLoader(getWorkspaceResourceLoader());
		job.setPriority(Job.BUILD);
		IFile file = EcorePlatformUtil.getFile(modelObject);
		if (file != null) {
			job.setRule(file.getProject());
		}
		return job;
	}

	@Override
	protected void doPerformCancel(IProgressMonitor monitor) throws CoreException {
	}
}
