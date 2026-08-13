/**
 * <copyright>
 *
 * Copyright (c) 2013 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [406062] Removal of the required project nature parameter in NewModelFileCreationPage constructor and CreateNewModelProjectJob constructor
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.jobs;

import java.net.URI;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;
import org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.StatusUtil;

/**
 * A {@link CreateNewModelProjectJob} capable of creating a new model project with given nature. A new project is
 * created, and the required nature is added to this project.
 * <p>
 * This job is set by default the priority to Job.BUILD and the rule to the workspace root.
 */

public class CreateNewModelProjectJob<T extends IMetaModelDescriptor> extends WorkspaceJob {

	protected IProject newProject;
	protected URI location;
	protected String natureId;
	protected T metaModelVersionDescriptor;
	protected IProjectWorkspacePreference<T> metaModelVersionPreference;

	/**
	 * @deprecated Use {@link #newProject} instead.
	 */
	@Deprecated
	protected IProject project;

	private IAdaptable uiInfo;
	private IProject[] referencedProjects;

	/**
	 * Creates a new instance of model project job
	 *
	 * @param jobName
	 *            the name of the job, must not be null
	 * @param newProject
	 *            the new project to be created, must not be null
	 */
	public CreateNewModelProjectJob(String jobName, IProject newProject) {
		this(jobName, newProject, null, null);
	}

	/**
	 * Creates a new instance of model project job with a required project nature id.
	 *
	 * @param jobName
	 *            the name of the job, must not be null
	 * @param natureId
	 *            the (principal) nature of the project to be created; when set to <code>null</code> no nature will be
	 *            added
	 * @deprecated Use {@link #CreateNewModelProjectJob(String, IProject, URI, String)} instead.
	 */
	@Deprecated
	public CreateNewModelProjectJob(String jobName, String natureId) {
		this(jobName, ResourcesPlugin.getWorkspace().getRoot().getProject("NewModelProject"), null, natureId); //$NON-NLS-1$
	}

	/**
	 * Creates a new instance of model project job with a required project nature id.
	 *
	 * @param jobNname
	 *            the name of the job, must not be null
	 * @param newProject
	 *            the new project to be created, must not be null
	 * @param location
	 *            the location where the project will be created; when set to <code>null</code> the default location
	 *            will be used
	 * @param natureId
	 *            the (principal) nature of the project to be created; when set to <code>null</code> no nature will be
	 *            added
	 */
	public CreateNewModelProjectJob(String jobName, IProject newProject, URI location, String natureId) {
		super(jobName);
		Assert.isNotNull(newProject);

		this.newProject = project = newProject;
		this.location = location;
		this.natureId = natureId;

		// Set priority and rule
		setPriority(Job.BUILD);
		setRule(ResourcesPlugin.getWorkspace().getRoot());
	}

	/**
	 * Creates a new instance of model project job.
	 *
	 * @param jobName
	 *            the name of the job, must not be null
	 * @param newProject
	 *            the new project to be created, must not be null
	 * @param location
	 *            the location where the project will be created; when set to <code>null</code> the default location
	 *            will be used
	 * @param metaModelVersionDescriptor
	 *            the meta-model version that the project will be used for; when set to <code>null</code> no metamodel
	 *            version will be configured
	 * @param metaModelVersionPreference
	 *            the metamodel version preference of the project; when set to <code>null</code> no metamodel version
	 *            will be configured
	 */
	public CreateNewModelProjectJob(String jobName, IProject newProject, URI location, T metaModelVersionDescriptor,
			IProjectWorkspacePreference<T> metaModelVersionPreference) {
		this(jobName, newProject, location, metaModelVersionPreference != null ? metaModelVersionPreference.getRequiredProjectNatureId() : null);
		this.metaModelVersionDescriptor = metaModelVersionDescriptor;
		this.metaModelVersionPreference = metaModelVersionPreference;
	}

	/**
	 * Creates a new instance of model project job.
	 *
	 * @param jobName
	 *            the name of the job, must not be null
	 * @param newProject
	 *            the new project to be created, must not be null
	 * @param location
	 *            the location where the project will be created; when set to <code>null</code> the default location
	 *            will be used
	 * @param metaModelVersionDescriptor
	 *            the meta-model version that this project will be used for; when set to <code>null</code> no metamodel
	 *            version will be configured
	 * @param natureId
	 *            the (principal) nature of the project to be created; when set to <code>null</code> no nature will be
	 *            added
	 * @param metaModelVersionPreference
	 *            the metamodel version preference of this project; when set to <code>null</code> no metamodel version
	 *            will be configured
	 * @deprecated Use
	 *             {@link #CreateNewModelProjectJob(String, IProject, URI, IMetaModelDescriptor, IProjectWorkspacePreference)}
	 *             instead.
	 */
	@Deprecated
	public CreateNewModelProjectJob(String jobName, IProject newProject, URI location, T metaModelVersionDescriptor, String natureId,
			IProjectWorkspacePreference<T> metaModelVersionPreference) {
		this(jobName, newProject, location, natureId);
		this.metaModelVersionDescriptor = metaModelVersionDescriptor;
		this.metaModelVersionPreference = metaModelVersionPreference;
	}

	/**
	 * Sets an adaptable to be used by
	 * {@link IOperationHistory#execute(org.eclipse.core.commands.operations.IUndoableOperation, IProgressMonitor, IAdaptable)}
	 * <br/>
	 * At a minimum the adaptable should be able to adapt to org.eclipse.swt.widgets.Shell.<br/>
	 * Having a shell, such an adaptable can be obtained by <code>WorkspaceUndoUtil.getUIInfoAdapter(shell)</code><br/>
	 * If null, a default shell will be created to ask the user for confirmation.
	 *
	 * @param uiInfo
	 *            the uiInfo adaptable to set
	 */
	public void setUIInfoAdaptable(IAdaptable uiInfo) {
		this.uiInfo = uiInfo;
	}

	/**
	 * @return the uiInfoAdaptable
	 */
	public IAdaptable getUIInfoAdaptable() {
		return uiInfo;
	}

	/**
	 * @return the referencedProjects
	 */
	public IProject[] getReferencedProjects() {
		return referencedProjects;
	}

	/**
	 * Sets the referenced project. Can be null or an empty array.
	 *
	 * @param referencedProjects
	 *            the referencedProjects to set
	 */
	public void setReferencedProjects(IProject[] referencedProjects) {
		this.referencedProjects = referencedProjects;
	}

	/*
	 * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		try {
			SubMonitor progress = SubMonitor.convert(monitor, getName(), 100);
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}

			createNewProject(progress.newChild(70));
			addNatures(progress.newChild(15));

			if (metaModelVersionDescriptor != null && metaModelVersionPreference != null) {
				metaModelVersionPreference.setInProject(newProject, metaModelVersionDescriptor);
			}
			progress.worked(15);

			return Status.OK_STATUS;
		} catch (OperationCanceledException exception) {
			return Status.CANCEL_STATUS;
		} catch (Exception ex) {
			return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
		}
	}

	/**
	 * Creates the project on disk.
	 *
	 * @param monitor
	 */
	protected void createNewProject(IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}
		progress.subTask(Messages.subTask_creatingNewModelProject);

		IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(newProject.getName());
		description.setLocationURI(location);

		// Update the referenced project if provided
		if (referencedProjects != null && referencedProjects.length > 0) {
			description.setReferencedProjects(referencedProjects);
		}

		newProject.create(description, progress.newChild(50));
		newProject.open(IResource.NONE, progress.newChild(50));

		progress.subTask(""); //$NON-NLS-1$
	}

	/**
	 * Adds the required natures to the project created by this {@link CreateNewModelFileJob}.
	 *
	 * @param monitor
	 */
	protected void addNatures(IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		if (natureId != null) {
			progress.subTask(Messages.subTask_addingProjectNatures);
			ExtendedPlatform.addNature(newProject, natureId, progress.newChild(100));
			progress.subTask(""); //$NON-NLS-1$
		}
	}
}
