/**
 * <copyright>
 *
 * Copyright (c) 2011-2017 See4sys, itemis and others.
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
 *     itemis - [358131] Make Xtend/Xpand/CheckJobs more robust against template file encoding mismatches
 *     itemis - Eliminated usage of deprecated WorkspaceTransactionUtil#executeInWriteTransaction() method variant
 *
 * </copyright>
 */
package org.eclipse.sphinx.xtendxpand.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitorAdapter;
import org.eclipse.emf.mwe.core.resources.ResourceLoaderFactory;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.mwe.IXtendXpandConstants;
import org.eclipse.sphinx.emf.mwe.resources.IWorkspaceResourceLoader;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.util.StatusUtil;
import org.eclipse.sphinx.xtendxpand.XtendEvaluationRequest;
import org.eclipse.sphinx.xtendxpand.internal.Activator;
import org.eclipse.sphinx.xtendxpand.util.XtendXpandUtil;
import org.eclipse.xpand2.XpandUtil;
import org.eclipse.xtend.XtendFacade;
import org.eclipse.xtend.expression.ExecutionContextImpl;
import org.eclipse.xtend.expression.ResourceManager;
import org.eclipse.xtend.expression.ResourceManagerDefaultImpl;
import org.eclipse.xtend.expression.TypeSystem;
import org.eclipse.xtend.expression.TypeSystemImpl;
import org.eclipse.xtend.expression.Variable;
import org.eclipse.xtend.typesystem.MetaModel;

public class XtendJob extends Job {

	protected static final Log log = LogFactory.getLog(XtendJob.class);

	/**
	 * The {@link MetaModel metamodel}s behind the model(s) to be transformed.
	 *
	 * @see MetaModel
	 */
	protected List<MetaModel> metaModels = null;

	/**
	 * The {@link TypeSystem} including the {@link MetaModel metamodel}s behind the model(s) to be transformed.
	 */
	protected TypeSystem typeSystem = null;

	/**
	 * The collection of {@link XtendEvaluationRequest Xtend evaluation request}s to be processed.
	 *
	 * @see XtendEvaluationRequest
	 */
	protected Collection<XtendEvaluationRequest> xtendEvaluationRequests;

	/**
	 * The resource loader to be used when loading Xtend templates.
	 */
	private IWorkspaceResourceLoader workspaceResourceLoader;

	/**
	 * The label for the {@link IUndoableOperation operation} in which the Xtend transformation is executed.
	 */
	private String operationLabel = null;

	/**
	 * The options to set the Xtend transformation transaction.
	 */
	private Map<String, Object> transactionOptions = null;

	/**
	 * The Xtend transformation result.
	 */
	protected Map<Object, Collection<?>> resultObjects = new HashMap<Object, Collection<?>>();

	/**
	 * Creates an {@link XtendJob} that transforms a model based on given <code>metaModel</code> as specified by
	 * provided <code>xtendEvaluationRequest</code>.
	 *
	 * @param name
	 *            The name of the job.
	 * @param metaModel
	 *            The {@link MetaModel metamodel} to be used.
	 * @param xtendEvaluationRequest
	 *            The {@link XtendEvaluationRequest Xtend evaluation request} to be processed.
	 * @see MetaModel
	 * @see XtendEvaluationRequest
	 */
	public XtendJob(String name, MetaModel metaModel, XtendEvaluationRequest xtendEvaluationRequest) {
		this(name, Collections.singletonList(metaModel), Collections.singleton(xtendEvaluationRequest));
	}

	/**
	 * Creates an {@link XtendJob} that transforms one or several models based on given <code>metaModel</code> as
	 * specified by provided <code>xtendEvaluationRequests</code>.
	 *
	 * @param name
	 *            The name of the job.
	 * @param metaModel
	 *            The {@link MetaModel metamodel} to be used.
	 * @param xtendEvaluationRequests
	 *            The {@link XtendEvaluationRequest Xtend evaluation request}s to be processed.
	 * @see MetaModel
	 * @see XtendEvaluationRequest
	 */
	public XtendJob(String name, MetaModel metaModel, Collection<XtendEvaluationRequest> xtendEvaluationRequests) {
		this(name, Collections.singletonList(metaModel), xtendEvaluationRequests);
	}

	/**
	 * Creates an {@link XtendJob} that transforms a model based on given <code>metaModels</code> as specified by
	 * provided <code>xtendEvaluationRequest</code>.
	 *
	 * @param name
	 *            The name of the job.
	 * @param metaModels
	 *            The {@link MetaModel metamodel}s to be used.
	 * @param xtendEvaluationRequest
	 *            The {@link XtendEvaluationRequest Xtend evaluation request} to be processed.
	 * @see MetaModel
	 * @see XtendEvaluationRequest
	 */
	public XtendJob(String name, List<MetaModel> metaModels, XtendEvaluationRequest xtendEvaluationRequest) {
		this(name, metaModels, Collections.singleton(xtendEvaluationRequest));
	}

	/**
	 * Creates an {@link XtendJob} that transforms one or several models based on given <code>metaModels</code> as
	 * specified by provided <code>xtendEvaluationRequests</code>.
	 *
	 * @param name
	 *            The name of the job.
	 * @param metaModels
	 *            The {@link MetaModel metamodel}s to be used.
	 * @param xtendEvaluationRequests
	 *            The {@link XtendEvaluationRequest Xtend evaluation request}s to be processed.
	 * @see MetaModel
	 * @see XtendEvaluationRequest
	 */
	public XtendJob(String name, List<MetaModel> metaModels, Collection<XtendEvaluationRequest> xtendEvaluationRequests) {
		super(name);

		Assert.isNotNull(metaModels);
		Assert.isNotNull(xtendEvaluationRequests);

		this.metaModels = metaModels;
		this.xtendEvaluationRequests = xtendEvaluationRequests;
	}

	/**
	 * Creates an {@link XtendJob} that transforms a model based on given <code>typeSystem</code> as specified by
	 * provided <code>xtendEvaluationRequest</code>.
	 *
	 * @param name
	 *            The name of the job.
	 * @param typeSystem
	 *            The {@link TypeSystem type system} that includes the {@link MetaModel metamodel}s to be used.
	 * @param xtendEvaluationRequest
	 *            The {@link XtendEvaluationRequest Xtend evaluation request} to be processed.
	 * @see TypeSystem
	 * @see XtendEvaluationRequest
	 */
	public XtendJob(String name, TypeSystem typeSystem, XtendEvaluationRequest xtendEvaluationRequest) {
		this(name, typeSystem, Collections.singleton(xtendEvaluationRequest));
	}

	/**
	 * Creates an {@link XtendJob} that transforms one or several models based on given <code>typeSystem</code> as
	 * specified by provided <code>xtendEvaluationRequests</code>.
	 *
	 * @param name
	 *            The name of the job.
	 * @param typeSystem
	 *            The {@link TypeSystem type system} that includes the {@link MetaModel metamodel}s to be used.
	 * @param xtendEvaluationRequests
	 *            The {@link XtendEvaluationRequest Xtend evaluation request}s to be processed.
	 * @see TypeSystem
	 * @see XtendEvaluationRequest
	 */
	public XtendJob(String name, TypeSystem typeSystem, Collection<XtendEvaluationRequest> xtendEvaluationRequests) {
		super(name);

		Assert.isNotNull(typeSystem);
		Assert.isNotNull(xtendEvaluationRequests);

		this.typeSystem = typeSystem;
		this.xtendEvaluationRequests = xtendEvaluationRequests;
	}

	protected Map<TransactionalEditingDomain, Collection<XtendEvaluationRequest>> getXtendEvaluationRequests() {
		Map<TransactionalEditingDomain, Collection<XtendEvaluationRequest>> requests = new HashMap<TransactionalEditingDomain, Collection<XtendEvaluationRequest>>();
		for (XtendEvaluationRequest request : xtendEvaluationRequests) {
			TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(request.getTargetObject());
			Collection<XtendEvaluationRequest> requestsInEditingDomain = requests.get(editingDomain);
			if (requestsInEditingDomain == null) {
				requestsInEditingDomain = new HashSet<XtendEvaluationRequest>();
				requests.put(editingDomain, requestsInEditingDomain);
			}
			requestsInEditingDomain.add(request);
		}
		return requests;
	}

	/**
	 * Sets the {@link IWorkspaceResourceLoader resource loader} for resolving resources referenced by Xtend extensions.
	 *
	 * @param resourceLoader
	 *            The resource loader to be used.
	 */
	public void setWorkspaceResourceLoader(IWorkspaceResourceLoader resourceLoader) {
		workspaceResourceLoader = resourceLoader;
	}

	/**
	 * Returns the label for the {@link IUndoableOperation operation} in which the Xtend transformation is executed.
	 *
	 * @return The operation label for the Xtend transformation.
	 * @see #setOperationLabel(String)
	 */
	protected String getOperationLabel() {
		if (operationLabel == null) {
			// Retrieve operation label from job name
			operationLabel = getName();
		}
		return operationLabel;
	}

	/**
	 * Sets the label for the {@link IUndoableOperation operation} in which the Xtend transformation is executed.
	 *
	 * @param operationLabel
	 *            The operation label for the Xtend transformation.
	 */
	public void setOperationLabel(String operationLabel) {
		this.operationLabel = operationLabel;
	}

	/**
	 * Returns the IOperationHistory for the given <code>editingDomain</code>.
	 *
	 * @param editingDomain
	 *            The EditingDomain for which the {@link IOperationHistory operation history} is to be retrieved.
	 * @return The {@link IOperationHistory operation history} of the given <code>editingDomain</code>.
	 */
	protected IOperationHistory getOperationHistory(TransactionalEditingDomain editingDomain) {
		return WorkspaceTransactionUtil.getOperationHistory(editingDomain);
	}

	/**
	 * Returns the options to use in the Xtend transformation transaction. If no transaction options are specified
	 * {@link WorkspaceTransactionUtil#getDefaultTransactionOptions()} are used as default.
	 *
	 * @return The options to use in the Xtend transformation transaction.
	 * @see #setTransactionOptions(Map)
	 */
	protected Map<String, Object> getTransactionOptions() {
		if (transactionOptions == null) {
			transactionOptions = WorkspaceTransactionUtil.getDefaultTransactionOptions();
		}
		return transactionOptions;
	}

	/**
	 * Sets the options to be used in the Xtend transformation transaction.
	 *
	 * @param transactionOptions
	 *            The transaction options to be used.
	 */
	public void setTransactionOptions(Map<String, Object> transactionOptions) {
		this.transactionOptions = transactionOptions;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		try {
			if (xtendEvaluationRequests.isEmpty()) {
				return Status.CANCEL_STATUS;
			}

			// Log start of transformation
			log.info("Xtend started..."); //$NON-NLS-1$

			// Install resource loader
			installResourceLoader();

			// Create execution context
			final ResourceManager resourceManager = new ResourceManagerDefaultImpl();
			Map<String, Variable> variables = new HashMap<String, Variable>();
			Map<String, Variable> globalVarsMap = new HashMap<String, Variable>();
			final ExecutionContextImpl execCtx = new ExecutionContextImpl(resourceManager, null,
					typeSystem instanceof TypeSystemImpl ? (TypeSystemImpl) typeSystem : new TypeSystemImpl(), variables, globalVarsMap,
					new ProgressMonitorAdapter(monitor), null, null, null, null, null, null, null);
			if (metaModels != null) {
				for (MetaModel metaModel : metaModels) {
					execCtx.registerMetaModel(metaModel);
				}
			}

			// Execute transformation
			long startTime = System.currentTimeMillis();
			final Map<TransactionalEditingDomain, Collection<XtendEvaluationRequest>> requests = getXtendEvaluationRequests();
			for (final TransactionalEditingDomain editingDomain : requests.keySet()) {

				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						for (XtendEvaluationRequest request : requests.get(editingDomain)) {
							log.info("Xtend transformation for " + request.getTargetObject() + " with '" + request.getExtensionName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

							// Update resource loader context
							updateResourceLoaderContext(request.getTargetObject());

							// Update resource manager with file encoding information for next Xtend file to be
							// evaluated
							IFile extensionFile = XtendXpandUtil.getUnderlyingFile(request.getExtensionName(),
									IXtendXpandConstants.EXTENSION_EXTENSION, workspaceResourceLoader);
							if (extensionFile != null) {
								try {
									resourceManager.setFileEncoding(extensionFile.getCharset());
								} catch (CoreException ex) {
									// Ignore exception
								}
							}

							// Evaluate current request
							XtendFacade facade = XtendFacade.create(execCtx, XpandUtil.withoutLastSegment(request.getExtensionName()));
							List<Object> parameterList = new ArrayList<Object>();
							parameterList.add(request.getTargetObject());
							parameterList.addAll(request.getParameterList());
							Object result = facade.call(XpandUtil.getLastSegment(request.getExtensionName()), parameterList);
							if (result != null) {
								if (result instanceof Collection) {
									resultObjects.put(request.getTargetObject(), (Collection<?>) result);
								} else if (result instanceof Object[]) {
									resultObjects.put(request.getTargetObject(), Arrays.asList((Object[]) result));
								} else {
									resultObjects.put(request.getTargetObject(), Collections.singleton(result));
								}
							}
						}
					}
				};

				if (editingDomain != null) {
					WorkspaceTransactionUtil.executeInWriteTransaction(editingDomain, runnable, getOperationLabel(),
							getOperationHistory(editingDomain), getTransactionOptions());
				} else {
					runnable.run();
				}
			}
			long duration = System.currentTimeMillis() - startTime;

			// Log end of transformation
			log.info("Xtend completed in " + duration + "ms!"); //$NON-NLS-1$ //$NON-NLS-2$
			return Status.OK_STATUS;
		} catch (OperationCanceledException exception) {
			return Status.CANCEL_STATUS;
		} catch (Exception ex) {
			return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
		} finally {
			// Always uninstall resource loader again
			uninstallResourceLoader();
		}
	}

	/**
	 * Installs a {@link IWorkspaceResourceLoader resource loader}.
	 */
	protected void installResourceLoader() {
		if (workspaceResourceLoader == null) {
			if (ResourceLoaderFactory.getCurrentThreadResourceLoader() instanceof IWorkspaceResourceLoader) {
				workspaceResourceLoader = (IWorkspaceResourceLoader) ResourceLoaderFactory.getCurrentThreadResourceLoader();
			}
		} else {
			ResourceLoaderFactory.setCurrentThreadResourceLoader(workspaceResourceLoader);
		}
	}

	/**
	 * Updates context of current {@link IWorkspaceResourceLoader resource loader} according to given
	 * <code>contextObject</code>.
	 */
	protected void updateResourceLoaderContext(Object contextObject) {
		if (workspaceResourceLoader != null) {
			IFile contextFile = EcorePlatformUtil.getFile(contextObject);
			IModelDescriptor contextModel = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			if (contextModel != null) {
				workspaceResourceLoader.setContextModel(contextModel);
			}
		}
	}

	/**
	 * Uninstalls current {@link IWorkspaceResourceLoader resource loader}.
	 */
	protected void uninstallResourceLoader() {
		ResourceLoaderFactory.setCurrentThreadResourceLoader(null);
	}

	/**
	 * Returns a map with the collections of objects resulting from the Xtend model transformation keyed by the target
	 * objects from the {@link XtendEvaluationRequest evaluation requests} that have been provided as input for the
	 * Xtend model transformation.
	 */
	public Map<Object, Collection<?>> getResultObjects() {
		return resultObjects;
	}

	@Override
	public boolean belongsTo(Object family) {
		return IExtendedPlatformConstants.FAMILY_LONG_RUNNING.equals(family);
	}
}
