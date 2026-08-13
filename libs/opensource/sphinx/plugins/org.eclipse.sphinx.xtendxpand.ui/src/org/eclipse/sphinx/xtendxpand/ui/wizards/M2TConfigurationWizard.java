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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.eclipse.sphinx.xtendxpand.jobs.M2TJob;
import org.eclipse.sphinx.xtendxpand.jobs.XpandJob;
import org.eclipse.sphinx.xtendxpand.outlet.ExtendedOutlet;
import org.eclipse.sphinx.xtendxpand.preferences.OutletsPreference;
import org.eclipse.sphinx.xtendxpand.preferences.PrDefaultExcludesPreference;
import org.eclipse.sphinx.xtendxpand.preferences.PrExcludesPreference;
import org.eclipse.sphinx.xtendxpand.ui.internal.Activator;
import org.eclipse.sphinx.xtendxpand.ui.internal.messages.Messages;
import org.eclipse.sphinx.xtendxpand.ui.wizards.pages.CheckConfigurationPage;
import org.eclipse.sphinx.xtendxpand.ui.wizards.pages.XpandConfigurationPage;
import org.eclipse.xtend.expression.TypeSystem;
import org.eclipse.xtend.expression.TypeSystemImpl;
import org.eclipse.xtend.typesystem.MetaModel;

public class M2TConfigurationWizard extends AbstractWizard {

	protected EObject modelObject;
	protected TypeSystem typeSystem;

	private String m2tJobName;
	private IWorkspaceResourceLoader workspaceResourceLoader;
	private OutletsPreference outletsPreference;
	private ExtendedOutlet defaultOutlet;
	private IJobChangeListener resultMessageHandler;

	protected XpandConfigurationPage xpandConfigurationPage;
	protected CheckConfigurationPage checkConfigurationPage;

	public M2TConfigurationWizard(EObject modelObject, List<MetaModel> metaModels) {
		Assert.isNotNull(metaModels);

		setDialogSettings(Activator.getDefault().getDialogSettings());
		setWindowTitle(Messages.title_codeGen);

		this.modelObject = modelObject;

		typeSystem = new TypeSystemImpl();
		for (MetaModel metaModel : metaModels) {
			((TypeSystemImpl) typeSystem).registerMetaModel(metaModel);
		}
	}

	public String getM2TJobName() {
		return m2tJobName != null ? m2tJobName : getDefaultM2TJobName();
	}

	protected String getDefaultM2TJobName() {
		return Messages.job_generatingCode;
	}

	public void setM2TJobName(String m2tJobName) {
		this.m2tJobName = m2tJobName;
	}

	public IWorkspaceResourceLoader getWorkspaceResourceLoader() {
		return workspaceResourceLoader;
	}

	public void setWorkspaceResourceLoader(IWorkspaceResourceLoader workspaceResourceLoader) {
		this.workspaceResourceLoader = workspaceResourceLoader;
	}

	public OutletsPreference getOutletsPreference() {
		return outletsPreference;
	}

	public void setOutletsPreference(OutletsPreference outletsPreference) {
		this.outletsPreference = outletsPreference;
	}

	public ExtendedOutlet getDefaultOutlet() {
		return defaultOutlet;
	}

	public void setDefaultOutlet(ExtendedOutlet defaultOutlet) {
		this.defaultOutlet = defaultOutlet;
	}

	public IJobChangeListener getResultMessageHandler() {
		return resultMessageHandler;
	}

	public void setResultMessageHandler(IJobChangeListener resultMessageHandler) {
		this.resultMessageHandler = resultMessageHandler;
	}

	@Override
	public void addPages() {
		xpandConfigurationPage = createXpandConfigurationPage();
		addPage(xpandConfigurationPage);
		checkConfigurationPage = createCheckConfigurationPage();
		addPage(checkConfigurationPage);
	}

	protected XpandConfigurationPage createXpandConfigurationPage() {
		XpandConfigurationPage xpandPage = new XpandConfigurationPage(Messages.label_configPageName);
		xpandPage.init(modelObject, typeSystem, getOutletsPreference(), getDefaultOutlet());
		return xpandPage;
	}

	protected CheckConfigurationPage createCheckConfigurationPage() {
		CheckConfigurationPage checkPage = new CheckConfigurationPage(Messages.label_configPageName);
		checkPage.init(modelObject);
		return checkPage;
	}

	@Override
	protected void doPerformFinish(IProgressMonitor monitor) throws CoreException {
		ExtendedPlatformUI.showSystemConsole();

		CheckJob checkJob = isCheckRequired() ? createCheckJob() : null;
		XpandJob xpandJob = createXpandJob();

		M2TJob job = new M2TJob(getM2TJobName(), xpandJob, checkJob);
		job.setPriority(Job.BUILD);
		IFile file = EcorePlatformUtil.getFile(modelObject);
		if (file != null) {
			job.setRule(file.getProject());
		}
		IJobChangeListener handler = getResultMessageHandler();
		if (handler != null) {
			job.addJobChangeListener(handler);
		}
		job.schedule();
	}

	protected boolean isCheckRequired() {
		return checkConfigurationPage.isCheckEnabled() && !checkConfigurationPage.getCheckEvaluationRequests().isEmpty();
	}

	protected XpandJob createXpandJob() {
		XpandJob job = new XpandJob(getM2TJobName(), typeSystem, xpandConfigurationPage.getXpandEvaluationRequests());
		job.setWorkspaceResourceLoader(getWorkspaceResourceLoader());
		job.getOutlets().addAll(xpandConfigurationPage.getOutlets());
		job.setPriority(Job.BUILD);
		IFile file = EcorePlatformUtil.getFile(modelObject);
		if (file != null) {
			job.configureProtectedRegionResolver(getPrSrcPaths(xpandConfigurationPage.getOutlets()),
					PrDefaultExcludesPreference.INSTANCE.get(file.getProject()), PrExcludesPreference.INSTANCE.get(file.getProject()));
			job.setRule(file.getProject());
		}
		return job;
	}

	protected String getPrSrcPaths(Collection<? extends ExtendedOutlet> outlets) {
		// Use a set to drop outlets pointing at same physical location
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

	protected CheckJob createCheckJob() {
		CheckJob checkJob = new CheckJob(getM2TJobName(), typeSystem, checkConfigurationPage.getCheckEvaluationRequests());
		checkJob.setWorkspaceResourceLoader(getWorkspaceResourceLoader());
		checkJob.setPriority(Job.BUILD);
		IFile file = EcorePlatformUtil.getFile(modelObject);
		if (file != null) {
			checkJob.setRule(file.getProject());
		}
		return checkJob;
	}

	@Override
	protected void doPerformCancel(IProgressMonitor monitor) throws CoreException {
	}
}
