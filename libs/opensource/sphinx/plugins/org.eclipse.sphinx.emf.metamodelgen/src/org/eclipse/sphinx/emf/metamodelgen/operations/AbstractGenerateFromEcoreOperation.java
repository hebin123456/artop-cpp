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
package org.eclipse.sphinx.emf.metamodelgen.operations;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sphinx.emf.metamodelgen.internal.Activator;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.platform.operations.AbstractWorkspaceOperation;
import org.eclipse.sphinx.platform.util.StatusUtil;

public abstract class AbstractGenerateFromEcoreOperation extends AbstractWorkspaceOperation implements IGenerateFromEcoreOperation {

	protected IFile ecoreFile;

	public AbstractGenerateFromEcoreOperation(String label) {
		super(label);
	}

	public AbstractGenerateFromEcoreOperation(String label, IFile ecoreFile) {
		super(label);
		this.ecoreFile = ecoreFile;
	}

	/*
	 * @see org.eclipse.sphinx.platform.operations.IWorkspaceOperation#getRule()
	 */
	@Override
	public ISchedulingRule getRule() {
		return ecoreFile != null ? ecoreFile.getProject() : null;
	}

	/*
	 * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		EPackage ecoreModel = loadEcoreModel();
		progress.worked(30);

		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		generate(ecoreModel, progress.newChild(70));
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.metamodelgen.operations.IGenerateFromEcoreOperation#generate(org.eclipse.emf.ecore.EPackage
	 * , org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public abstract void generate(EPackage ecoreModel, IProgressMonitor monitor) throws CoreException, OperationCanceledException;

	protected EPackage loadEcoreModel() throws CoreException {
		Assert.isNotNull(ecoreFile);

		try {
			URI ecoreFileURI = EcorePlatformUtil.createURI(ecoreFile.getFullPath());
			URI ecoreModelURI = ecoreFileURI.appendFragment("/"); //$NON-NLS-1$

			ResourceSet resourceSet = new ResourceSetImpl();
			return (EPackage) resourceSet.getEObject(ecoreModelURI, true);
		} catch (RuntimeException ex) {
			IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
			throw new CoreException(status);
		}
	}

	protected void saveEcoreModel(URI ecoreFileURI, EPackage ecoreModel) throws CoreException {
		try {
			ResourceSet resourceSet = new ResourceSetImpl();
			Resource resource = resourceSet.createResource(ecoreFileURI);
			if (resource == null) {
				throw new IOException("Unable to create resource for '" + ecoreFileURI + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			resource.getContents().add(ecoreModel);
			resource.save(null);
		} catch (IOException ex) {
			IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
			throw new CoreException(status);
		}
	}
}
