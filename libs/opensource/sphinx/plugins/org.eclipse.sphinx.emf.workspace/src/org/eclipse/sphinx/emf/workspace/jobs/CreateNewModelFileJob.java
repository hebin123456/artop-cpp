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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.StatusUtil;

/**
 * A {@linkplain CreateNewModelFileJob create new model job} capable of creating and saving an initial model to be added
 * to the new model compliant {@link IFile file} in the workspace. This model file to be created must not be null. The
 * initial model is based on the given {@link IMetaModelDescriptor metamodel descriptor}. The initial model will create
 * a root object using a {@linkplain EClassifier root object classifier}, and a {@linkplain EPackage root object
 * package} for the model to be contained.
 * <p>
 * This job is set by default the priority to Job.BUILD and the rule to the workspace root to be run with the job
 * manager.
 */
public class CreateNewModelFileJob extends WorkspaceJob {

	protected IFile newFile;
	protected IMetaModelDescriptor metaModelDescriptor;
	protected EPackage rootObjectEPackage;
	protected EClassifier rootObjectEClassifier;

	/**
	 * Creates a new instance of this {@linkplain CreateNewModelFileJob}. It sets by default the priority to Job.BUILD
	 * and the rule to the workspace root. The model file that will be created and the metamodel descriptor that this
	 * model file is based on must not be null.
	 * 
	 * @param jobName
	 *            the name of the job
	 * @param newFile
	 *            the model file that will be created, must not be <code>null</code>
	 * @param metaModelDescriptor
	 *            the {@linkplain IMetaModelDescriptor descriptor} of metamodel the model file should be based on, must
	 *            not be <code>null</code>
	 * @param rootObjectEPackage
	 *            the {@linkplain EPackage root object package} to be used for creating the initial model to be
	 *            contained by the model file, must not be <code>null</code>
	 * @param rootObjectEClassifier
	 *            the {@linkplain EClassifier root object classifier} of the initial model's root object, must not be
	 *            <code>null</code>
	 */
	public CreateNewModelFileJob(String jobName, IFile newFile, IMetaModelDescriptor metaModelDescriptor, EPackage rootObjectEPackage,
			EClassifier rootObjectEClassifier) {
		super(jobName);
		Assert.isNotNull(newFile);
		Assert.isNotNull(metaModelDescriptor);
		Assert.isLegal(metaModelDescriptor != MetaModelDescriptorRegistry.ANY_MM);
		Assert.isLegal(metaModelDescriptor != MetaModelDescriptorRegistry.NO_MM);
		Assert.isNotNull(rootObjectEPackage);
		Assert.isNotNull(rootObjectEClassifier);

		this.newFile = newFile;
		this.metaModelDescriptor = metaModelDescriptor;
		this.rootObjectEPackage = rootObjectEPackage;
		this.rootObjectEClassifier = rootObjectEClassifier;

		// Set priority and rule
		setPriority(Job.BUILD);
		setRule(ExtendedPlatform.createSaveNewSchedulingRule(newFile));
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

			// Create initial model
			EObject rootObject = createInitialModel();
			progress.worked(20);

			// Save initial model to new file in the workspace
			saveInitialModel(rootObject, progress.newChild(80));

			return Status.OK_STATUS;
		} catch (OperationCanceledException exception) {
			return Status.CANCEL_STATUS;
		} catch (Exception ex) {
			return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
		}
	}

	/**
	 * Creates the initial model to be added to the new model file.
	 * <p>
	 * This implementation creates a new {@linkplain EObject root object} using the {@linkplain EPackage root object
	 * package} and {@linkplain EClassifier root object classifier} provided to this {@link CreateNewModelFileJob}.
	 * </p>
	 */
	protected EObject createInitialModel() {
		return rootObjectEPackage.getEFactoryInstance().create((EClass) rootObjectEClassifier);
	}

	/**
	 * Saves the initial model rooted by given {@linkplain EObject root object} to a new file in the workspace.
	 * 
	 * @param rootObject
	 *            the {@linkplain EObject root object} to be saved in a file
	 * @param monitor
	 *            a progress {@linkplain IProgressMonitor monitor} monitor, or <code>null</code> if progress reporting
	 *            and cancelation are not desired
	 */
	protected void saveInitialModel(EObject rootObject, IProgressMonitor monitor) {
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(newFile.getProject(), metaModelDescriptor);
		EcorePlatformUtil.saveNewModelResource(editingDomain, newFile.getFullPath(), metaModelDescriptor.getDefaultContentTypeId(), rootObject,
				false, monitor);
	}
}
