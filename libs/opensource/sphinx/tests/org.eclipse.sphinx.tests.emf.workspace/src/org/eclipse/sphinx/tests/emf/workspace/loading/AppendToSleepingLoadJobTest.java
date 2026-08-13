/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
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
package org.eclipse.sphinx.tests.emf.workspace.loading;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.emf.workspace.internal.loading.ModelLoadJob;
import org.eclipse.sphinx.examples.hummingbird.ide.natures.HummingbirdNature;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

@SuppressWarnings("restriction")
public class AppendToSleepingLoadJobTest extends TestCase {

	private static final String MSG_expectedOneJob = "One and only one job from family MODEL LOADING should be found"; //$NON-NLS-1$

	public void testLoadProjects() throws Exception {

		final String projectAName = "hb20TestProjectA"; //$NON-NLS-1$
		final String projectBName = "hb20TestProjectB"; //$NON-NLS-1$
		final String projectCName = "hb20TestProjectC"; //$NON-NLS-1$
		final String projectDName = "hb20TestProjectD"; //$NON-NLS-1$
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {

				try {
					// Project A
					createProject(projectAName);
					ResourcesPlugin.getWorkspace().checkpoint(false);
					assertOneLoadJobIsSleeping();

					// Project B
					createProject(projectBName);
					ResourcesPlugin.getWorkspace().checkpoint(false);
					assertOneLoadJobIsSleeping();

					// Project C
					createProject(projectCName);
					ResourcesPlugin.getWorkspace().checkpoint(false);
					assertOneLoadJobIsSleeping();

					// Project D
					createProject(projectDName);
					ResourcesPlugin.getWorkspace().checkpoint(false);
					assertOneLoadJobIsSleeping();
				} catch (Exception ex) {
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), 0, new NullProgressMonitor());
	}

	protected void createProject(String projectName) throws Exception {

		IProject newProject = doCreateProject(projectName);

		// Create file A
		String fileAName = getFileName(projectName, "A"); //$NON-NLS-1$
		IFile fileA = newProject.getFile(fileAName);
		saveModel(createHB20Model(), fileA);

		// Create file B
		String fileBName = getFileName(projectName, "B"); //$NON-NLS-1$
		IFile fileB = newProject.getFile(fileBName);
		saveModel(createHB20Model(), fileB);

		// Create file C
		String fileCName = getFileName(projectName, "C"); //$NON-NLS-1$
		IFile fileC = newProject.getFile(fileCName);
		saveModel(createHB20Model(), fileC);

		// Create file D
		String fileDName = getFileName(projectName, "D"); //$NON-NLS-1$
		IFile fileD = newProject.getFile(fileDName);
		saveModel(createHB20Model(), fileD);
	}

	protected void assertOneLoadJobIsSleeping() {
		try {
			// Retrieves from JobManager the list of jobs that belong to the "model loading" family
			Job[] jobs = Job.getJobManager().find(IExtendedPlatformConstants.FAMILY_MODEL_LOADING);

			// Verify that one and only one loading job is queued
			List<Job> jobList = new ArrayList<Job>();
			for (Job job : jobs) {
				if (job instanceof ModelLoadJob) {
					jobList.add(job);
				}
			}
			assertEquals(MSG_expectedOneJob, 1, jobList.size());

		} catch (AssertionFailedError err) {
			throw err;
		}
	}

	protected IProject doCreateProject(String projectName) throws CoreException {
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
		String location = ResourcesPlugin.getWorkspace().getRoot().getLocationURI().getPath() + File.separator + projectName;
		description.setLocation(new Path(location));

		newProject.create(description, null);
		newProject.open(IResource.NONE, null);

		// Add HummingbirdNature
		ExtendedPlatform.addNature(newProject, HummingbirdNature.ID, null);
		return newProject;
	}

	private void saveModel(final EObject rootObject, IFile newFile) throws OperationCanceledException, ExecutionException {
		final TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(newFile.getProject(),
				Hummingbird20MMDescriptor.INSTANCE);
		final URI newResourceURI1 = URI.createPlatformResourceURI(newFile.getFullPath().toString(), true);
		WorkspaceTransactionUtil.executeInWriteTransaction(editingDomain, new Runnable() {

			@Override
			public void run() {
				EcoreResourceUtil.saveNewModelResource(editingDomain.getResourceSet(), newResourceURI1,
						Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), rootObject, EcoreResourceUtil.getDefaultSaveOptions());

			}

		}, "saving"); //$NON-NLS-1$
	}

	private Platform createHB20Model() {
		Platform platform = TypeModel20Factory.eINSTANCE.createPlatform();
		platform.setName("myHBPlatform"); //$NON-NLS-1$
		createInterfaces(platform);
		createComponentTypes(platform);
		return platform;
	}

	private void createInterfaces(Platform platform) {
		for (int i = 0; i < 20; i++) {
			Interface interfaze = TypeModel20Factory.eINSTANCE.createInterface();
			interfaze.setName("interface" + i); //$NON-NLS-1$
			platform.getInterfaces().add(interfaze);
		}
	}

	private void createComponentTypes(Platform platform) {
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
			ComponentType cmpType = TypeModel20Factory.eINSTANCE.createComponentType();
			cmpType.setName("component" + i); //$NON-NLS-1$
			cmpType.getProvidedInterfaces().add(platform.getInterfaces().get(random.nextInt(20)));
			createParameters(cmpType);
			createPorts(cmpType);
			platform.getComponentTypes().add(cmpType);
		}
	}

	private void createPorts(ComponentType cmpType) {
		Random random = new Random();
		for (int i = 0; i < 5; i++) {
			Port port = TypeModel20Factory.eINSTANCE.createPort();
			port.setName(cmpType.getName() + "_port" + i); //$NON-NLS-1$
			port.setMinProviderCount(random.nextInt(10));
			port.setMinProviderCount(random.nextInt(10));
			port.setOwner(cmpType);
		}
	}

	private void createParameters(ComponentType cmpType) {
		for (int i = 0; i < 5; i++) {
			Parameter param = TypeModel20Factory.eINSTANCE.createParameter();
			param.setName(cmpType.getName() + "_parameter" + i); //$NON-NLS-1$
			param.setDataType("data" + i); //$NON-NLS-1$
			cmpType.getParameters().add(param);
		}
	}

	private String getFileName(String projectName, String postFix) {
		String fileAName = projectName + "_hb20File" + postFix + ".typemodel"; //$NON-NLS-1$ //$NON-NLS-2$
		return fileAName;
	}
}
