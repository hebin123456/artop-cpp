/**
 * <copyright>
 *
 * Copyright (c) 2008-2016 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [348822] Enable BasicExplorerContentProvider to be used for displaying model content under folders and projects
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [420505] Editor shows no content when editor input object is added lately
 *     itemis - [460260] Expanded paths are collapsed on resource reload
 *     itemis - [480135] Introduce metamodel and view content agnostic problem decorator for model elements
 *     itemis - [481581] Improve refresh behavior of BasicModelContentProvider to avoid performance problems due to needlessly repeated tree state restorations
 *     itemis - [501109] The tree viewer state restoration upon Eclipse startup and viewer refreshed still running in cases where it is not needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.explorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.transaction.NotificationFilter;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListener;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.ui.internal.Tracing;
import org.eclipse.emf.transaction.ui.provider.TransactionalAdapterFactoryContentProvider;
import org.eclipse.emf.transaction.ui.provider.TransactionalAdapterFactoryLabelProvider;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.explorer.refresh.FullRefreshStrategy;
import org.eclipse.sphinx.emf.explorer.refresh.ModelObjectRefreshStrategy;
import org.eclipse.sphinx.emf.explorer.refresh.ModelResourceRefreshStrategy;
import org.eclipse.sphinx.emf.explorer.refresh.WorkspaceResourceRefreshStrategy;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.emf.workspace.ui.viewers.state.ITreeViewerState;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;

/**
 * EMF model content provider for use with the Common Navigator framework. It deals with the workspace integration of
 * the EMF model and internally delegates to EMF-based model content provider to get the children and parent of model
 * objects.
 */
@SuppressWarnings("restriction")
public class BasicExplorerContentProvider implements IModelCommonContentProvider {

	protected static final int LIMIT_INDIVIDUAL_RESOURCES_REFRESH = 20;

	protected static final int LIMIT_INDIVIDUAL_OBJECTS_REFRESH = 100;

	protected Viewer viewer;

	protected INavigatorContentDescriptor contentDescriptor;

	protected Map<TransactionalEditingDomain, AdapterFactoryContentProvider> modelContentProviders = new WeakHashMap<TransactionalEditingDomain, AdapterFactoryContentProvider>();

	protected ResourceSetListener resourceChangedListener;

	protected ResourceSetListener resourceMovedListener;

	protected ResourceSetListener crossReferenceChangedListener;

	protected ResourceSetListener modelContentRootChangedListener;

	private IResourceChangeListener resourceMarkerChangeListener;

	/**
	 * Returns the viewer whose content is provided by this content provider.
	 *
	 * @return the viewer for this content provider
	 */
	@Override
	public Viewer getViewer() {
		return viewer;
	}

	protected ExtendedCommonNavigator getCommonNavigator() {
		if (viewer instanceof CommonViewer) {
			CommonViewer commonViewer = (CommonViewer) viewer;
			CommonNavigator commonNavigator = commonViewer.getCommonNavigator();
			if (commonNavigator instanceof ExtendedCommonNavigator) {
				return (ExtendedCommonNavigator) commonNavigator;
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.sphinx.emf.explorer.IExtendedCommonContentProvider#recordViewerState()
	 */
	@Override
	public ITreeViewerState recordViewerState() {
		ExtendedCommonNavigator navigator = getCommonNavigator();
		if (navigator != null) {
			return navigator.getViewerStateRecorder().recordState();
		}
		return null;
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.explorer.IExtendedCommonContentProvider#applyViewerState(org.eclipse.sphinx.emf.workspace.
	 * ui.viewers.state.ITreeViewerState)
	 */
	@Override
	public void applyViewerState(ITreeViewerState state) {
		ExtendedCommonNavigator navigator = getCommonNavigator();
		if (navigator != null) {
			navigator.getViewerStateRecorder().applyState(state);
		}
	}

	/*
	 * @see org.eclipse.ui.navigator.ICommonContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
	 */
	@Override
	public void init(ICommonContentExtensionSite config) {
		contentDescriptor = config.getExtension().getDescriptor();

		if (resourceMarkerChangeListener == null) {
			resourceMarkerChangeListener = createResourceMarkerChangeListener();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceMarkerChangeListener, IResourceChangeEvent.POST_CHANGE);
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.explorer.IExtendedCommonContentProvider#isTriggerPoint(java.lang.Object)
	 */
	@Override
	public boolean isTriggerPoint(Object element) {
		return contentDescriptor.isTriggerPoint(element);
	}

	/*
	 * @see org.eclipse.sphinx.emf.explorer.IExtendedCommonContentProvider#isPossibleChild(java.lang.Object)
	 */
	@Override
	public boolean isPossibleChild(Object element) {
		return contentDescriptor.isPossibleChild(element);
	}

	/*
	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		// Do nothing by default
	}

	/*
	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void restoreState(IMemento memento) {
		// Do nothing by default
	}

	/*
	 * @see org.eclipse.sphinx.emf.explorer.ICommonModelContentProvider#getModelResource(org.eclipse.core.resources.
	 * IResource)
	 */
	@Override
	public Resource getModelResource(IResource workspaceResource) {
		// Is given workspace resource a file?
		if (workspaceResource instanceof IFile) {
			// Get model behind given workspace file
			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel((IFile) workspaceResource);
			if (modelDescriptor != null) {
				// Try to retrieve model resource behind given workspace file but don't force it to be loaded in case
				// that this has not been done yet
				Resource modelResource = EcorePlatformUtil.getResource((IFile) workspaceResource);

				// Given model resource already loaded?
				if (modelResource != null) {
					return modelResource;
				} else {
					// Make sure that resource set listeners for refreshing the viewer are installed on editing domain
					// and get notified when model resources get loaded subsequently
					addTransactionalEditingDomainListeners(modelDescriptor.getEditingDomain());

					// Request asynchronous loading of model behind given workspace file
					ModelLoadManager.INSTANCE.loadModel(modelDescriptor, true, null);
				}
			}
		}
		return null;
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.explorer.ICommonModelContentProvider#getModelContentRoots(org.eclipse.emf.ecore.resource.
	 * Resource)
	 */
	@Override
	public List<Object> getModelContentRoots(Resource modelResource) {
		if (modelResource != null) {
			ArrayList<Object> modelContentRoots = new ArrayList<Object>(3);

			// Ensure backward compatibility
			if (!modelResource.getContents().isEmpty()) {
				Object deprecatedModelContentRoot = getModelContentRoot(modelResource.getContents().get(0));
				if (deprecatedModelContentRoot != null) {
					modelContentRoots.add(deprecatedModelContentRoot);
				}
			}

			// Return model resource as only model content root by default
			if (modelContentRoots.isEmpty()) {
				modelContentRoots.add(modelResource);
			}

			return modelContentRoots;
		}
		return Collections.emptyList();
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.explorer.ICommonModelContentProvider#getWorkspaceResource(org.eclipse.emf.ecore.resource.
	 * Resource)
	 */
	@Override
	public IResource getWorkspaceResource(Resource modelResource) {
		return EcorePlatformUtil.getFile(modelResource);
	}

	/**
	 * Retrieves the model root behind specified {@link IResource resource}. Returns <code>null</code> if no such is
	 * available or the {@link IModelDescriptor model} behind specified resource has not been loaded yet.
	 * <p>
	 * Default implementation supports the handling of {@link IFile file} resources including lazy loading of the
	 * underlying {@link IModelDescriptor model}s: if the given file belongs to some model that has not been loaded yet
	 * then the loading of that model, i.e., the given file and all other files belonging to the same model, will be
	 * triggered. The model loading will be done asynchronously and therefore won't block the UI. When the model loading
	 * has been completed, the {@link #resourceChangedListener} automatically refreshes the underlying {@link #viewer
	 * viewer} so that the model elements contained by the given file become visible.
	 * <p>
	 * Clients may override this method so as to add support for other resource types (e.g., {@link IProject project}s
	 * or {@link IFolder folder}s) or implement different lazy or eager loading strategies.
	 *
	 * @param resource
	 *            The {@link IResource resource} whose model root is to be retrieved.
	 * @return The model root behind specified resource or <code>null</code> if no such is available or the model behind
	 *         specified resource has not been loaded yet.
	 */
	@Deprecated
	protected Object getModelRoot(IResource resource) {
		// Is given workspace resource a file?
		if (resource instanceof IFile) {
			// Get model behind given file
			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel((IFile) resource);
			if (modelDescriptor != null) {
				// Get model root of given file but don't force it to be loaded in case that this has not
				// been done yet
				Object modelRoot = null;
				Resource modelResource = EcorePlatformUtil.getResource((IFile) resource);
				if (modelResource != null && !modelResource.getContents().isEmpty()) {
					modelRoot = modelResource.getContents().get(0);
				}

				// Given file not loaded yet?
				if (modelRoot == null) {
					// Make sure that resource set listeners for refreshing the viewer are installed on editing domain
					// and get notified when model resources have been loaded
					addTransactionalEditingDomainListeners(modelDescriptor.getEditingDomain());

					// Request asynchronous loading of model behind given file
					ModelLoadManager.INSTANCE.loadModel(modelDescriptor, true, null);
				}
				return modelRoot;
			}
		}

		return null;
	}

	/**
	 * Retrieves the <em>root element</em> of the model contained in the specified {@link IFile file}. Returns
	 * <code>null</code> if that file has not yet been loaded into the {@linkplain ResourceSet} of the specified
	 * {@link TransactionalEditingDomain editingDomain} or if it is empty.
	 * <p>
	 * Default implementation provides a lazy loading mechanism. When the user expands an model file of some
	 * {@link IModelDescriptor model} which has not been loaded yet, this and all other model files which belong to that
	 * {@link IModelDescriptor model} will be loaded by that time. This loading will be done asynchronously using
	 * {@linkplain ModelLoadManager} API, and therefore won't block the UI while the loading process is ongoing. Once
	 * the loading of the {@link IModelDescriptor model} will be finished, the refresh mechanism (i.e., the
	 * {@linkplain ResourceSetListener resource changed listener}) will make sure that the view gets refreshed and the
	 * model elements inside the expanded model file become visible.
	 * <p>
	 * Clients may override this method in order to provide a custom lazy or eager loading strategy.
	 *
	 * @param editingDomain
	 *            The {@linkplain TransactionalEditingDomain editing domain} owning the {@linkplain ResourceSet resource
	 *            set} inside which the {@linkplain Resource resource} corresponding to the specified {@link IFile file}
	 *            should be loaded.
	 * @param file
	 *            The {@linkplain IFile file} which <em>model root</em> must be returned.
	 * @return The root element of the model owned by the specified {@link IFile file}; or <code>null</code> if file has
	 *         not been loaded.
	 * @deprecated Use {@link #getModelRoot(IResource)} instead. Rationale: Navigation into models should not be
	 *             supported only from files but also from projects and folders. The TransactionalEditingDomain can be
	 *             easily retrieved inside the method and therefore does not need to be provided by the caller.
	 */
	@Deprecated
	protected EObject getModelRoot(TransactionalEditingDomain editingDomain, IFile file) {
		Object modelRoot = getModelRoot(file);
		return modelRoot instanceof EObject ? (EObject) modelRoot : null;
	}

	/**
	 * Returns the {@link EObject object} or {@link Resource resource} which is used as root of the model content
	 * provided by this {@link BasicExplorerContentProvider content provider} for given model object. The model content
	 * root is the model object that is mapped to the {@link IResource workspace resource} under which the model behind
	 * given model object is made visible. It may or may not be the actual root object of the model. The model content
	 * root itself will not become visible in the underlying viewer, only the workspace resource it is mapped to and its
	 * children will.
	 *
	 * @param object
	 *            An arbitrary model object to be investigated.
	 * @return The {@link EObject object} or {@link Resource resource} which is used as root of the model content
	 *         provided by this content provider for given model object or <code>null</code> if given object is no model
	 *         object or has no parent that corresponds to the expected model content root.
	 * @deprecated Use {@link #getModelContentRoots(Resource)} instead.
	 */
	@Deprecated
	protected Object getModelContentRoot(Object object) {
		if (object instanceof EObject) {
			return getModelContentRoot((EObject) object);
		} else if (object instanceof IWrapperItemProvider) {
			return getModelContentRoot((IWrapperItemProvider) object);
		} else if (object instanceof FeatureMap.Entry) {
			return getModelContentRoot((FeatureMap.Entry) object);
		} else if (object instanceof TransientItemProvider) {
			return getModelContentRoot((TransientItemProvider) object);
		}
		return null;
	}

	/**
	 * Returns the {@link EObject object} or {@link Resource resource} which is used as root of the model content
	 * provided by this {@link BasicExplorerContentProvider content provider} for given {@link EObject model object}.
	 * The model content root is the model object that is mapped to the {@link IResource workspace resource} under which
	 * the model behind given model object is made visible. It may or may not be the actual root object of the model.
	 * The model content root itself will not become visible in the underlying viewer, only the workspace resource it is
	 * mapped to and its children will.
	 * <p>
	 * This implementation returns the {@link Resource resource} behind given {@link EObject model object} as model
	 * content root. Clients should override this method if they require some other model object to be used instead.
	 * </p>
	 * <p>
	 * The most typical use cases and implementations of this method are as follows:
	 * <ul>
	 * <li>The {@link Resource resource} behind given model object is to be used as model content root:<br>
	 * <blockquote><code>return object.eResource();</code></blockquote></li>
	 * <li>The {@link EObject model root object} behind given model object is to be used as model content root:<br>
	 * <blockquote><code>return EcoreUtil.getRootContainer(object);</code></blockquote></li>
	 * </ul>
	 * </p>
	 *
	 * @param object
	 *            An arbitrary {@link EObject object} to be investigated.
	 * @return The {@link EObject object} or {@link Resource resource} which is used as root of the model content
	 *         provided by this content provider for given model object or <code>null</code> if given object is no model
	 *         object or has no parent that corresponds to the expected model content root.
	 * @deprecated Use {@link #getModelContentRoots(Resource)} instead.
	 */
	@Deprecated
	protected Object getModelContentRoot(EObject object) {
		Assert.isNotNull(object);

		// Ensure backward compatibility
		Object mappedModelRoot = getMappedModelRoot(object);
		if (mappedModelRoot != null) {
			return mappedModelRoot;
		}

		return object.eResource();
	}

	/**
	 * @deprecated Use {@link #getModelContentRoot(EObject)} instead.
	 */
	@Deprecated
	protected Object getMappedModelRoot(EObject object) {
		return null;
	}

	/**
	 * @deprecated Use {@link #getModelContentRoots(Resource)} instead.
	 */
	@Deprecated
	protected Object getModelContentRoot(IWrapperItemProvider wrapperItemProvider) {
		Object unwrapped = AdapterFactoryEditingDomain.unwrap(wrapperItemProvider);
		return getModelContentRoot(unwrapped);
	}

	/**
	 * @deprecated Use {@link #getModelContentRoots(Resource)} instead.
	 */
	@Deprecated
	protected Object getModelContentRoot(FeatureMap.Entry entry) {
		Object unwrapped = AdapterFactoryEditingDomain.unwrap(entry);
		return getModelContentRoot(unwrapped);
	}

	/**
	 * @deprecated Use {@link #getModelContentRoots(Resource)} instead.
	 */
	@Deprecated
	protected Object getModelContentRoot(TransientItemProvider transientItemProvider) {
		Object target = transientItemProvider.getTarget();
		return getModelContentRoot(target);
	}

	/**
	 * Returns the {@link IResource resource} corresponding to given {@link #getModelContentRoot(Object) model content
	 * root}.
	 *
	 * @param modelContentRoot
	 *            The {@link #getModelContentRoot(Object) model content root} object in question.
	 * @return The {@link IResource resource} corresponding to given model content root.
	 * @see #getModelContentRoot(Object)
	 * @deprecated Use {@link #getWorkspaceResource(Resource)} instead.
	 */
	@Deprecated
	protected IResource getUnderlyingWorkspaceResource(Object modelContentRoot) {
		return EcorePlatformUtil.getFile(modelContentRoot);
	}

	protected AdapterFactoryContentProvider getModelContentProvider(Object object) {
		// Retrieve editing domain behind specified object
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(object);
		if (editingDomain != null) {
			// Retrieve model content provider for given editing domain; create new one if not existing yet
			AdapterFactoryContentProvider modelContentProvider = modelContentProviders.get(editingDomain);
			if (modelContentProvider == null) {
				modelContentProvider = createModelContentProvider(editingDomain);
				modelContentProviders.put(editingDomain, modelContentProvider);
				addTransactionalEditingDomainListeners(editingDomain);
			}
			return modelContentProvider;
		}
		return null;
	}

	protected AdapterFactoryContentProvider createModelContentProvider(final TransactionalEditingDomain editingDomain) {
		Assert.isNotNull(editingDomain);
		AdapterFactory adapterFactory = getAdapterFactory(editingDomain);
		return new TransactionalAdapterFactoryContentProvider(editingDomain, adapterFactory) {
			@Override
			// Overridden to avoid somewhat annoying logging of Eclipse exceptions resulting from event queue
			// dispatching that is done before transaction is acquired and actually starts to run
			protected <T> T run(RunnableWithResult<? extends T> run) {
				try {
					return TransactionUtil.runExclusive(editingDomain, run);
				} catch (Exception e) {
					Tracing.catching(TransactionalAdapterFactoryLabelProvider.class, "run", e); //$NON-NLS-1$

					// propagate interrupt status because we are not throwing
					Thread.currentThread().interrupt();

					return null;
				}
			}
		};
	}

	/**
	 * Returns the {@link AdapterFactory adapter factory} to be used by this {@link BasicExplorerContentProvider content
	 * provider} for creating {@link ItemProviderAdapter item provider}s which control the way how {@link EObject model
	 * element}s from given <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns the {@link AdapterFactory adapter factory} which is embedded in the given
	 * <code>editingDomain</code> by default. Clients which want to use an alternative {@link AdapterFactory adapter
	 * factory} (e.g., an {@link AdapterFactory adapter factory} that creates {@link ItemProviderAdapter item provider}s
	 * which are specifically designed for the {@link IEditorPart editor} in which this
	 * {@link BasicExplorerContentProvider content provider} is used) may override {@link #getCustomAdapterFactory()}
	 * and return any {@link AdapterFactory adapter factory} of their choice. This custom {@link AdapterFactory adapter
	 * factory} will then be returned as result by this method.
	 * </p>
	 *
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} whose embedded {@link AdapterFactory adapter
	 *            factory} is to be returned as default. May be left <code>null</code> if
	 *            {@link #getCustomAdapterFactory()} has been overridden and returns a non-<code>null</code> result.
	 * @return The {@link AdapterFactory adapter factory} that will be used by this {@link BasicExplorerContentProvider
	 *         content provider}. <code>null</code> if no custom {@link AdapterFactory adapter factory} is provided
	 *         through {@link #getCustomAdapterFactory()} and no <code>editingDomain</code> has been specified.
	 * @see #getCustomAdapterFactory()
	 */
	protected AdapterFactory getAdapterFactory(TransactionalEditingDomain editingDomain) {
		AdapterFactory customAdapterFactory = getCustomAdapterFactory();
		if (customAdapterFactory != null) {
			return customAdapterFactory;
		} else if (editingDomain != null) {
			return ((AdapterFactoryEditingDomain) editingDomain).getAdapterFactory();
		}
		return null;
	}

	/**
	 * Returns a custom {@link AdapterFactory adapter factory} to be used by this {@link BasicExplorerContentProvider
	 * content provider} for creating {@link ItemProviderAdapter item provider}s which control the way how
	 * {@link EObject model element}s from given <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns <code>null</code> as default. Clients which want to use their own
	 * {@link AdapterFactory adapter factory} (e.g., an {@link AdapterFactory adapter factory} that creates
	 * {@link ItemProviderAdapter item provider}s which are specifically designed for the {@link IEditorPart editor} in
	 * which this {@link BasicExplorerContentProvider content provider} is used) may override this method and return any
	 * {@link AdapterFactory adapter factory} of their choice. This custom {@link AdapterFactory adapter factory} will
	 * then be returned as result by {@link #getAdapterFactory(TransactionalEditingDomain)}.
	 * </p>
	 *
	 * @return The custom {@link AdapterFactory adapter factory} that is to be used by this
	 *         {@link BasicExplorerContentProvider content provider}. <code>null</code> the default
	 *         {@link AdapterFactory adapter factory} returned by {@link #getAdapterFactory(TransactionalEditingDomain)}
	 *         should be used instead.
	 * @see #getAdapterFactory(TransactionalEditingDomain)
	 */
	protected AdapterFactory getCustomAdapterFactory() {
		return null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		ArrayList<Object> children = new ArrayList<Object>();
		try {
			// Is parent element a workspace resource?
			if (parentElement instanceof IResource) {
				// Retrieve model resource behind the workspace resource handed in as parent element
				Resource modelResource = getModelResource((IResource) parentElement);

				// Get corresponding model content provider
				AdapterFactoryContentProvider contentProvider = getModelContentProvider(modelResource);
				if (contentProvider != null) {
					// Set model resource as model content provider input
					contentProvider.inputChanged(viewer, null, modelResource);

					// Determine the model content roots, i.e., the actual parent objects of the model objects to be
					// displayed as virtual children of the workspace resource
					for (Object modelContentRoot : getModelContentRoots(modelResource)) {
						// Retrieve children of current parent object
						children.addAll(Arrays.asList(contentProvider.getChildren(modelContentRoot)));
					}
				}
			}

			// Assume that parent element is a model object
			else {
				// Try to obtain corresponding model content provider
				AdapterFactoryContentProvider contentProvider = getModelContentProvider(parentElement);
				if (contentProvider != null) {
					// Retrieve children of specified parent element
					children.addAll(Arrays.asList(contentProvider.getChildren(parentElement)));
				}
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
		return children.toArray(new Object[children.size()]);
	}

	@Override
	public Object getParent(Object element) {
		Object parent = null;
		AdapterFactoryContentProvider contentProvider = getModelContentProvider(element);
		if (contentProvider != null) {
			parent = contentProvider.getParent(element);
		}

		// Is parent element a model content root?
		Resource modelResource = EcoreResourceUtil.getResource(parent);
		List<Object> modelContentRoots = getModelContentRoots(modelResource);
		if (modelContentRoots.contains(parent)) {
			// Return corresponding workspace resource
			return getWorkspaceResource(modelResource);
		}
		return parent;
	}

	@Override
	public void dispose() {
		for (TransactionalEditingDomain editingDomain : modelContentProviders.keySet()) {
			removeTransactionalEditingDomainListeners(editingDomain);
			AdapterFactoryContentProvider modelContentProvider = modelContentProviders.get(editingDomain);
			modelContentProvider.dispose();
		}
		modelContentProviders.clear();

		if (resourceMarkerChangeListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceMarkerChangeListener);
		}
		resourceMarkerChangeListener = null;
	}

	protected void addTransactionalEditingDomainListeners(TransactionalEditingDomain editingDomain) {
		Assert.isNotNull(editingDomain);

		if (resourceChangedListener == null) {
			resourceChangedListener = createResourceChangedListener();
			Assert.isNotNull(resourceChangedListener);
		}
		editingDomain.addResourceSetListener(resourceChangedListener);

		if (resourceMovedListener == null) {
			resourceMovedListener = createResourceMovedListener();
			Assert.isNotNull(resourceMovedListener);
		}
		editingDomain.addResourceSetListener(resourceMovedListener);

		if (crossReferenceChangedListener == null) {
			crossReferenceChangedListener = createCrossReferenceChangedListener();
			Assert.isNotNull(crossReferenceChangedListener);
		}
		editingDomain.addResourceSetListener(crossReferenceChangedListener);

		if (modelContentRootChangedListener == null) {
			modelContentRootChangedListener = createModelContentRootChangedListener();
			Assert.isNotNull(modelContentRootChangedListener);
		}
		editingDomain.addResourceSetListener(modelContentRootChangedListener);
	}

	protected void removeTransactionalEditingDomainListeners(TransactionalEditingDomain editingDomain) {
		Assert.isNotNull(editingDomain);

		if (resourceChangedListener != null) {
			editingDomain.removeResourceSetListener(resourceChangedListener);
		}
		if (resourceMovedListener != null) {
			editingDomain.removeResourceSetListener(resourceMovedListener);
		}
		if (crossReferenceChangedListener != null) {
			editingDomain.removeResourceSetListener(crossReferenceChangedListener);
		}
		if (modelContentRootChangedListener != null) {
			editingDomain.removeResourceSetListener(modelContentRootChangedListener);
		}
	}

	/**
	 * Creates a ResourceSetChangedListener that detects (re-)loaded resources resources and refreshes their parent(s).
	 */
	// TODO To further reduce number of refresh requests: Combine this and subsequent ResourceSetListeners into a single
	// ResourceSetListener, delegate to sub methods that evaluate notifications for changed resources, moved resources,
	// changed cross references, and changed model content root elements, and makes sure that only a single refresh
	// request that is the most appropriate is issued (e.g. when an entire resource has changed and needs to be
	// refreshed then there is no need to request refreshes of changed cross references or model content root elements
	// inside that resource).
	protected ResourceSetListener createResourceChangedListener() {
		return new ResourceSetListenerImpl(NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResource(), Resource.RESOURCE__IS_LOADED)
				.or(NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResourceSet(), ResourceSet.RESOURCE_SET__RESOURCES))) {
			@Override
			public void resourceSetChanged(ResourceSetChangeEvent event) {
				Set<Resource> loadedResources = new HashSet<Resource>();
				Set<Resource> unloadedResources = new HashSet<Resource>();
				Set<Resource> addedResources = new HashSet<Resource>();
				Set<Resource> removedResources = new HashSet<Resource>();

				// Analyze notifications for changed resources; record only loaded and unloaded or added and removed
				// resources which have not got unloaded/loaded or removed/added again later on
				for (Notification notification : event.getNotifications()) {
					Object notifier = notification.getNotifier();
					if (notifier instanceof Resource) {
						Resource resource = (Resource) notifier;
						Boolean newValue = (Boolean) notification.getNewValue();
						if (newValue) {
							if (unloadedResources.contains(resource)) {
								unloadedResources.remove(resource);
							} else {
								loadedResources.add(resource);
							}
						} else {
							if (loadedResources.contains(resource)) {
								loadedResources.remove(resource);
							} else {
								unloadedResources.add(resource);
							}
						}
					} else if (notifier instanceof ResourceSet) {
						if (notification.getEventType() == Notification.ADD || notification.getEventType() == Notification.ADD_MANY) {
							List<Resource> newResources = new ArrayList<Resource>();
							Object newValue = notification.getNewValue();
							if (newValue instanceof List<?>) {
								@SuppressWarnings("unchecked")
								List<Resource> newResourcesValue = (List<Resource>) newValue;
								newResources.addAll(newResourcesValue);
							} else if (newValue instanceof Resource) {
								newResources.add((Resource) newValue);
							}

							for (Resource newResource : newResources) {
								if (removedResources.contains(newResource)) {
									removedResources.remove(newResource);
								} else {
									addedResources.add(newResource);
								}
							}
						} else if (notification.getEventType() == Notification.REMOVE || notification.getEventType() == Notification.REMOVE_MANY) {
							List<Resource> oldResources = new ArrayList<Resource>();
							Object oldValue = notification.getOldValue();
							if (oldValue instanceof List<?>) {
								@SuppressWarnings("unchecked")
								List<Resource> oldResourcesValue = (List<Resource>) oldValue;
								oldResources.addAll(oldResourcesValue);
							} else if (oldValue instanceof Resource) {
								oldResources.add((Resource) oldValue);
							}

							for (Resource oldResource : oldResources) {
								if (addedResources.contains(oldResource)) {
									addedResources.remove(oldResource);
								} else {
									removedResources.add(oldResource);
								}
							}
						}
					}
				}

				// Handle changed resources
				ModelResourceRefreshStrategy refreshStrategy = new ModelResourceRefreshStrategy(BasicExplorerContentProvider.this, true);
				Set<Resource> resourcesToRefresh = refreshStrategy.getTreeElementsToRefresh();
				resourcesToRefresh.addAll(loadedResources);
				resourcesToRefresh.addAll(addedResources);
				resourcesToRefresh.addAll(unloadedResources);
				resourcesToRefresh.addAll(removedResources);
				refreshStrategy.run();
			}

			@Override
			public boolean isPostcommitOnly() {
				return true;
			}
		};
	}

	protected ResourceSetListener createResourceMovedListener() {
		return new ResourceSetListenerImpl(NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResource(), Resource.RESOURCE__URI)) {
			@Override
			public void resourceSetChanged(ResourceSetChangeEvent event) {
				Set<Resource> movedResources = new HashSet<Resource>();
				for (Notification notification : event.getNotifications()) {
					movedResources.add((Resource) notification.getNotifier());
				}
				// TODO Add support for restoring tree viewer state for moved resources by adding a old/new resource URI
				// mappings to the refresh strategy and applying it to recorded tree viewer state before reapplying it
				// to viewer (see AbstractRefreshStrategy#run() for details)
				ModelResourceRefreshStrategy refreshStrategy = new ModelResourceRefreshStrategy(BasicExplorerContentProvider.this, false);
				refreshStrategy.getTreeElementsToRefresh().addAll(movedResources);
				refreshStrategy.run();
			}

			@Override
			public boolean isPostcommitOnly() {
				return true;
			}
		};
	}

	protected ResourceSetListener createCrossReferenceChangedListener() {
		return new ResourceSetListenerImpl(NotificationFilter.createNotifierTypeFilter(EObject.class)) {
			@Override
			public void resourceSetChanged(ResourceSetChangeEvent event) {
				ModelObjectRefreshStrategy refreshStrategy = new ModelObjectRefreshStrategy(BasicExplorerContentProvider.this);
				for (Notification notification : event.getNotifications()) {
					EObject object = (EObject) notification.getNotifier();
					if (notification.getFeature() instanceof EReference) {
						EReference reference = (EReference) notification.getFeature();
						if (!reference.isContainment() && !reference.isContainer() && reference.getEType() instanceof EClass) {
							refreshStrategy.getTreeElementsToRefresh().add(object);
						}
					}
				}
				refreshStrategy.run();
			}

			@Override
			public boolean isPostcommitOnly() {
				return true;
			}
		};
	}

	/**
	 * Explicitly refreshes the corresponding workspace resources in case of changes among the direct children of the
	 * model root objects.
	 * <p>
	 * !! Important note !! This is necessary because viewer refreshes triggered by the model content provider only
	 * affect the model content root objects but not the corresponding workspace resources. As the former are not
	 * represented by any tree item in the viewer none of these refreshes performed will have any visible effect.
	 * </p>
	 *
	 * @return
	 */
	protected ResourceSetListener createModelContentRootChangedListener() {
		return new ResourceSetListenerImpl(NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResource(), Resource.RESOURCE__CONTENTS)
				.or(NotificationFilter.createNotifierTypeFilter(EObject.class))) {
			@Override
			public void resourceSetChanged(ResourceSetChangeEvent event) {
				Set<EObject> addedObjects = new HashSet<EObject>();
				Set<EObject> removedObjects = new HashSet<EObject>();
				Set<EObject> changedObjects = new HashSet<EObject>();

				// Analyze notifications for changed objects; record only set/added and unset/removed objects which have
				// not got unset/removed or set/added again later on
				for (Notification notification : event.getNotifications()) {
					Object notifier = notification.getNotifier();
					if (notifier instanceof Resource || notification.getFeature() instanceof EReference) {
						if (notification.getEventType() == Notification.SET || notification.getEventType() == Notification.ADD
								|| notification.getEventType() == Notification.ADD_MANY) {
							List<EObject> newValues = new ArrayList<EObject>();
							Object newValue = notification.getNewValue();
							if (newValue instanceof List<?>) {
								@SuppressWarnings("unchecked")
								List<EObject> newValueList = (List<EObject>) newValue;
								newValues.addAll(newValueList);
							} else if (newValue instanceof EObject) {
								newValues.add((EObject) newValue);
							}

							for (EObject value : newValues) {
								if (removedObjects.contains(value)) {
									removedObjects.remove(value);
								} else {
									addedObjects.add(value);
								}
							}
						} else if (notification.getEventType() == Notification.UNSET || notification.getEventType() == Notification.REMOVE
								|| notification.getEventType() == Notification.REMOVE_MANY) {
							List<EObject> oldValues = new ArrayList<EObject>();
							Object oldValue = notification.getOldValue();
							if (oldValue instanceof List<?>) {
								@SuppressWarnings("unchecked")
								List<EObject> oldValueList = (List<EObject>) oldValue;
								oldValues.addAll(oldValueList);
							} else if (oldValue instanceof EObject) {
								oldValues.add((EObject) oldValue);
							}

							for (EObject value : oldValues) {
								if (addedObjects.contains(value)) {
									addedObjects.remove(value);
								} else {
									removedObjects.add(value);
								}
							}
						}
					}

				}
				changedObjects.addAll(addedObjects);
				changedObjects.addAll(removedObjects);

				// Check if changed objects are children of the model content roots and refresh corresponding workspace
				// resource if so
				WorkspaceResourceRefreshStrategy refreshStrategy = new WorkspaceResourceRefreshStrategy(BasicExplorerContentProvider.this, true);
				for (EObject changedObject : changedObjects) {
					Object changedObjectParent = getParent(changedObject);
					if (changedObjectParent instanceof IResource) {
						refreshStrategy.getTreeElementsToRefresh().add((IResource) changedObjectParent);
					}
				}
				refreshStrategy.run();
			}

			@Override
			public boolean isPostcommitOnly() {
				return true;
			}
		};
	}

	/**
	 * Refreshes the {@link CommonViewer viewer} in case of problem marker changes to trigger update of problem
	 * decoration.
	 */
	protected IResourceChangeListener createResourceMarkerChangeListener() {
		return new IResourceChangeListener() {
			@Override
			public void resourceChanged(IResourceChangeEvent event) {
				IMarkerDelta[] markerDelta = event.findMarkerDeltas(IMarker.PROBLEM, true);
				if (markerDelta != null && markerDelta.length > 0) {
					FullRefreshStrategy refreshStrategy = new FullRefreshStrategy(BasicExplorerContentProvider.this, false);
					refreshStrategy.run();
				}
			}
		};
	}
}
