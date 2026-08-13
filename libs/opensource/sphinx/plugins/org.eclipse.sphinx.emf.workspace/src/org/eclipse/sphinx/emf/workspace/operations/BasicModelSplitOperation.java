/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.emf.workspace.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.splitting.IModelSplitOperation;
import org.eclipse.sphinx.emf.splitting.IModelSplitPolicy;
import org.eclipse.sphinx.emf.splitting.ModelSplitProcessor;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.platform.operations.AbstractLabeledWorkspaceRunnable;
import org.eclipse.sphinx.platform.operations.AbstractWorkspaceOperation;
import org.eclipse.sphinx.platform.operations.ILabeledWorkspaceRunnable;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.StatusUtil;

public class BasicModelSplitOperation extends AbstractWorkspaceOperation implements IModelSplitOperation {

	private Collection<Resource> resources;
	private Collection<URI> resourceURIs;
	private IModelSplitPolicy modelSplitPolicy;
	private boolean deleteOriginalResources = false;

	private TransactionalEditingDomain editingDomain = null;

	public BasicModelSplitOperation(IModelSplitPolicy modelSplitPolicy) {
		super(Messages.operation_splitModel_label);
		Assert.isNotNull(modelSplitPolicy);

		this.modelSplitPolicy = modelSplitPolicy;
	}

	public BasicModelSplitOperation(Resource resource, IModelSplitPolicy modelSplitPolicy) {
		super(Messages.operation_splitModel_label);
		Assert.isNotNull(resource);
		Assert.isNotNull(modelSplitPolicy);

		getResources().add(resource);
		this.modelSplitPolicy = modelSplitPolicy;
	}

	public BasicModelSplitOperation(URI resourceURI, IModelSplitPolicy modelSplitPolicy) {
		super(Messages.operation_splitModel_label);
		Assert.isNotNull(resourceURI);
		Assert.isNotNull(modelSplitPolicy);

		getResourceURIs().add(resourceURI);
		this.modelSplitPolicy = modelSplitPolicy;
	}

	public Collection<Resource> getResources() {
		if (resources == null) {
			resources = new ArrayList<Resource>();
		}
		return resources;
	}

	public Collection<URI> getResourceURIs() {
		if (resourceURIs == null) {
			resourceURIs = new ArrayList<URI>();
		}
		return resourceURIs;
	}

	@Override
	public IModelSplitPolicy getModelSplitPolicy() {
		return modelSplitPolicy;
	}

	@Override
	public boolean isDeleteOriginalResources() {
		return deleteOriginalResources;
	}

	@Override
	public void setDeleteOriginalResources(boolean deleteOriginalResources) {
		this.deleteOriginalResources = deleteOriginalResources;
	}

	@Override
	public ISchedulingRule getRule() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	protected TransactionalEditingDomain getEditingDomain() {
		if (editingDomain == null) {
			Collection<Resource> resources = getResources();
			if (!resources.isEmpty()) {
				editingDomain = TransactionUtil.getEditingDomain(resources.iterator().next());
			}
			Collection<URI> resourceURIs = getResourceURIs();
			if (!resourceURIs.isEmpty()) {
				editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(resourceURIs.iterator().next());
			}
		}
		return editingDomain;
	}

	protected Map<?, ?> getSaveOptions() {
		return EcoreResourceUtil.getDefaultSaveOptions();
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		try {
			// TODO Split runnable into separate transactions for loading, splitting & saving and deleting and make sure
			// that only split & save operation can be undone under the condition that the original resource are not
			// getting deleted
			ILabeledWorkspaceRunnable runnable = new AbstractLabeledWorkspaceRunnable(getLabel()) {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					SubMonitor progress = SubMonitor.convert(monitor, 100);
					if (progress.isCanceled()) {
						throw new OperationCanceledException();
					}

					ModelSplitProcessor processor = new ModelSplitProcessor(modelSplitPolicy);
					try {
						// Load model resources to be split
						loadResourcesToSplit(processor, progress.newChild(25));

						if (progress.isCanceled()) {
							throw new OperationCanceledException();
						}

						// Split model resources
						processor.run(progress.newChild(25));

						if (progress.isCanceled()) {
							throw new OperationCanceledException();
						}

						// Save resulting model resources
						saveSplitResources(processor, progress.newChild(25));

						if (progress.isCanceled()) {
							throw new OperationCanceledException();
						}

						// Delete original model resources, if required
						if (deleteOriginalResources) {
							deleteOriginalResources(progress.newChild(25));
						} else {
							progress.worked(25);
						}
					} finally {
						processor.dispose();
					}
				}
			};

			TransactionalEditingDomain editingDomain = getEditingDomain();
			if (editingDomain != null) {
				WorkspaceTransactionUtil.executeInWriteTransaction(editingDomain, runnable, monitor);
			} else {
				runnable.run(monitor);
			}
		} catch (OperationCanceledException ex) {
			throw ex;
		} catch (ExecutionException ex) {
			IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
			throw new CoreException(status);
		}
	}

	protected void loadResourcesToSplit(ModelSplitProcessor processor, IProgressMonitor monitor) {
		Assert.isNotNull(processor);

		// Make sure that resources behind specified resource URIs are loaded
		Collection<URI> resourceURIs = getResourceURIs();
		ModelLoadManager.INSTANCE.loadURIs(resourceURIs, false, monitor);

		// Add resources corresponding to specified resource URIs to resources to be split
		for (URI resourceURI : resourceURIs) {
			Resource resource = EcorePlatformUtil.getResource(resourceURI);
			if (resource != null) {
				resources.add(resource);
			}
		}

		// Add resources to be split to model split processor
		processor.getResourcesToSplit().addAll(resources);
	}

	protected void saveSplitResources(ModelSplitProcessor processor, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(processor);

		// Grab split resources from model split processor and save them
		EcorePlatformUtil.saveNewModelResources(getEditingDomain(), processor.getSplitResourceDescriptors(), false, monitor);
	}

	protected void deleteOriginalResources(IProgressMonitor monitor) throws CoreException {
		// TODO Wrap in write transaction and move to new EcorePlatformUtil method
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				Collection<Resource> resources = getResources();
				SubMonitor progress = SubMonitor.convert(monitor, resources.size());
				if (progress.isCanceled()) {
					throw new OperationCanceledException();
				}

				for (Resource resource : resources) {
					// Delete resource if it exists physically, just unload it otherwise
					if (EcoreResourceUtil.exists(resource.getURI())) {
						try {
							resource.delete(Collections.emptyMap());
						} catch (IOException ex) {
							PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
						}
					} else {
						EcoreResourceUtil.unloadResource(resource);
					}

					progress.worked(1);
					if (progress.isCanceled()) {
						throw new OperationCanceledException();
					}
				}
			}
		};

		// Execute save operation as IWorkspaceRunnable on workspace in order to avoid resource change
		// notifications during transaction execution
		/*
		 * !! Important Note !! Setting the IWorkspace.AVOID_UPDATE flag on the outer workspace job or workspace
		 * runnable from which this method is called doesn't help because the matter of executing a transaction inside
		 * suppresses its effect.
		 */
		/*
		 * !! Important Note !! Only set IWorkspace.AVOID_UPDATE flag but don't define any scheduling restrictions for
		 * the save operation right here (this must only be done on the outer workspace job or workspace runnable from
		 * which this method is called). Otherwise it would be likely to end up in deadlocks with operations which
		 * already have acquired exclusive access to the workspace but are waiting for exclusive access to the model
		 * (i.e. for the transaction).
		 */
		ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, monitor);
	}
}
