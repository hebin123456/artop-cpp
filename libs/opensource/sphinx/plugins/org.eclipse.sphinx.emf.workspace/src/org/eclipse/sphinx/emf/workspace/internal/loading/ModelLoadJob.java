/**
 * <copyright>
 *
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     Siemens - [574930] Model load manager extension
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.internal.loading;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.emf.workspace.loading.operations.ILoadOperation;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.util.StatusUtil;

/**
 * @since 0.7.0
 */
public class ModelLoadJob<T extends ILoadOperation> extends Job {

	protected T operation;

	/**
	 * @param name
	 * @param mmDescriptor
	 */
	public ModelLoadJob(T operation) {
		super(operation.getLabel());
		this.operation = operation;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			operation.run(monitor);
			return Status.OK_STATUS;
		} catch (OperationCanceledException ex) {
			return Status.CANCEL_STATUS;
		} catch (CoreException ex) {
			return ex.getStatus();
		} catch (Exception ex) {
			return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
		}
	}

	public T getOperation() {
		return operation;
	}

	@Override
	public boolean belongsTo(Object family) {
		return IExtendedPlatformConstants.FAMILY_MODEL_LOADING.equals(family) || IExtendedPlatformConstants.FAMILY_LONG_RUNNING.equals(family);
	}

	/**
	 * @param projects
	 *            The {@linkplain IProject project}s that this model load job may cover.
	 * @param includeReferencedProjects
	 *            If <b><code>true</code></b>, consider referenced projects.
	 * @param mmDescriptor
	 *            The {@linkplain IMetaModelDescriptor meta-model descriptor} of the model which has been asked for
	 *            loading.
	 * @return
	 *         <ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> if this job covers the loading of the specified projects with the
	 *         specified meta-model descriptor;</li>
	 *         <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	public boolean covers(Collection<IProject> projects, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor) {
		return operation.covers(projects, includeReferencedProjects, mmDescriptor);
	}

	/**
	 * @param files
	 *            The list of files this loading job is supposed to cover.
	 * @param mmDescriptor
	 *            The {@linkplain IMetaModelDescriptor meta-model descriptor} considered for loading.
	 * @return
	 *         <ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> if this job covers the loading of the specified files with the
	 *         specified meta-model descriptor;</li>
	 *         <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	public boolean covers(Collection<IFile> files, IMetaModelDescriptor mmDescriptor) {
		return operation.covers(files, mmDescriptor);
	}

}
