/**
 * <copyright>
 *
 * Copyright (c) 2015-2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [480105] Occasional ConcurrentModificationException when re-launching Sphinx on previously used workspace
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.viewers.state.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.ui.IMemento;

public class TransientElementStateProvider extends AbstractTreeElementStateProvider {

	protected static final String TRANSIENT_CHILDREN_SEPARATOR = "/"; //$NON-NLS-1$

	private URI parentURI = null;
	private EObject parentEObject = null;
	private List<Object> transientChildren;
	private boolean stale = false;

	public TransientElementStateProvider(TreeViewer viewer, IMemento memento) {
		super(viewer);
		Assert.isNotNull(memento);

		String parentURIAsString = memento.getString(TreeElementStateProviderFactory.MEMENTO_KEY_PARENT_URI);
		if (parentURIAsString != null) {
			parentURI = URI.createURI(parentURIAsString, true);
		}

		String transientChildrenAsString = memento.getString(TreeElementStateProviderFactory.MEMENTO_KEY_TRANSIENT_CHILDREN);
		transientChildren = readTransientChildrenFromString(transientChildrenAsString);
	}

	public TransientElementStateProvider(TreeViewer viewer, URI parentURI, List<Object> transientChildren) {
		super(viewer);
		Assert.isNotNull(transientChildren);

		this.parentURI = parentURI;
		this.transientChildren = deresolveTransientChildren(transientChildren);
	}

	protected EObject getParentEObject() {
		if (parentEObject == null) {
			parentEObject = EcorePlatformUtil.getEObject(parentURI);

			if (parentEObject == null && isUnderlyingModelLoaded()) {
				// At this point we know that the transient children's parent EObject can definitely not be found and
				// that transient element targeted by this tree element state provider does not exist
				stale = true;
			}
		}
		return parentEObject;
	}

	protected List<Object> readTransientChildrenFromString(String transientChildrenAsString) {
		if (transientChildrenAsString != null) {
			return new ArrayList<Object>(Arrays.asList(transientChildrenAsString.split(TRANSIENT_CHILDREN_SEPARATOR)));
		}
		return Collections.emptyList();
	}

	protected String writeTransientChildrenToString(List<Object> transientChildren) {
		Assert.isNotNull(transientChildren);

		List<Object> deresolvedTransientChildren = deresolveTransientChildren(transientChildren);
		StringBuilder transientChildrenAsString = new StringBuilder();
		Iterator<?> iter = deresolvedTransientChildren.iterator();
		while (iter.hasNext()) {
			Object transientChild = iter.next();
			transientChildrenAsString.append(transientChild);

			if (iter.hasNext()) {
				transientChildrenAsString.append(TRANSIENT_CHILDREN_SEPARATOR);
			}
		}
		return transientChildrenAsString.toString();
	}

	protected List<Object> resolveTransientChildren(List<Object> transientChildren) {
		Assert.isNotNull(transientChildren);

		// Transient children already fully resolved?
		if (transientChildren.isEmpty() || !(transientChildren.get(transientChildren.size() - 1) instanceof String)) {
			return transientChildren;
		}
		if (stale) {
			return transientChildren;
		}

		// Get the transient children's parent EObject
		Object parent = getParentEObject();
		if (parent == null) {
			return transientChildren;
		}

		// Resolve transient children
		List<Object> resolvedTransientChildren = new ArrayList<Object>();
		for (Object transientChild : transientChildren) {
			// Current transient child not yet resolved?
			if (transientChild instanceof String) {
				// Resolve ...
				Object resolvedTransientChild = resolveTransientChild(parent, (String) transientChild);

				// ... and store current transient child
				resolvedTransientChildren.add(resolvedTransientChild);
			} else {
				// Store current transient child as is
				resolvedTransientChildren.add(transientChild);
			}

			// Let current transient child be next parent
			parent = transientChild;
		}
		return resolvedTransientChildren;
	}

	protected Object resolveTransientChild(Object parent, String transientChildAsString) {
		IContentProvider contentProvider = viewer.getContentProvider();
		if (contentProvider instanceof ITreeContentProvider) {
			// Content provider ready to return children of given parent?
			if (isTreeContentProviderAvailable(parent)) {
				// Try to find child among the given parent's children that matches specified transient child
				for (Object child : ((ITreeContentProvider) contentProvider).getChildren(parent)) {
					if (child instanceof TransientItemProvider) {
						if (child.getClass().getName().equals(transientChildAsString)) {
							return child;
						}
					}
					if (child instanceof IWrapperItemProvider) {
						IWrapperItemProvider wrapperItemProvider = (IWrapperItemProvider) child;
						Object value = wrapperItemProvider.getValue();
						if (value instanceof EObject) {
							EStructuralFeature feature = wrapperItemProvider.getFeature();
							if (feature != null && feature.getName().equals(transientChildAsString)) {
								return child;
							}
						} else if (value instanceof TransientItemProvider) {
							if (value.getClass().getName().equals(transientChildAsString)) {
								return child;
							}
						}
					}
				}

				// At this point we know that specified transient child can definitely not be found and that transient
				// element targeted by this tree element state provider does not exist
				stale = true;
			}
		}
		return transientChildAsString;
	}

	protected List<Object> deresolveTransientChildren(List<Object> transientChildren) {
		Assert.isNotNull(transientChildren);

		// Deresolve transient children
		List<Object> deresolvedTransientChildren = new ArrayList<Object>();
		for (Object transientChild : transientChildren) {
			// Current transient child not yet deresolved?
			if (!(transientChild instanceof String)) {
				// Deresolve ...
				Object deresolvedTransientChild = deresolveTransientChild(transientChild);

				// ... and store current transient child
				deresolvedTransientChildren.add(deresolvedTransientChild);
			} else {
				// Store current transient child as is
				deresolvedTransientChildren.add(transientChild);
			}
		}
		return deresolvedTransientChildren;
	}

	protected Object deresolveTransientChild(Object transientChild) {
		if (transientChild instanceof TransientItemProvider) {
			return transientChild.getClass().getName();
		} else if (transientChild instanceof IWrapperItemProvider) {
			IWrapperItemProvider wrapperItemProvider = (IWrapperItemProvider) transientChild;
			Object value = wrapperItemProvider.getValue();
			if (value instanceof EObject) {
				EStructuralFeature feature = wrapperItemProvider.getFeature();
				if (feature != null) {
					return feature.getName();
				}
			} else if (value instanceof TransientItemProvider) {
				return value.getClass().getName();
			}
		}
		return transientChild;
	}

	protected Object getLastTransientChild() {
		if (!transientChildren.isEmpty()) {
			Object lastTransientChild = transientChildren.get(transientChildren.size() - 1);
			if (!(lastTransientChild instanceof String)) {
				return lastTransientChild;
			}
		}
		return null;
	}

	@Override
	public boolean hasUnderlyingModel() {
		return true;
	}

	@Override
	public boolean canUnderlyingModelBeLoaded() {
		return EcoreResourceUtil.exists(parentURI);
	}

	@Override
	public boolean isUnderlyingModelLoaded() {
		return EcorePlatformUtil.getResource(parentURI) != null;
	}

	@Override
	public void loadUnderlyingModel() {
		IFile file = EcorePlatformUtil.getFile(parentURI);
		IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(file);
		if (modelDescriptor != null) {
			// Request asynchronous loading of model behind given workspace file
			ModelLoadManager.INSTANCE.loadModel(modelDescriptor, true, null);
		}
	}

	@Override
	public boolean isStale() {
		return stale;
	}

	@Override
	public Object getTreeElement() {
		transientChildren = resolveTransientChildren(transientChildren);
		return getLastTransientChild();
	}

	@Override
	public void appendToMemento(IMemento parentMemento) {
		if (parentURI != null) {
			IMemento memento = parentMemento.createChild(TreeElementStateProviderFactory.MEMENTO_TYPE_ELEMENT_TRANSIENT);
			memento.putString(TreeElementStateProviderFactory.MEMENTO_KEY_PARENT_URI, parentURI.toString());
			String transientChildrenAsString = writeTransientChildrenToString(transientChildren);
			memento.putString(TreeElementStateProviderFactory.MEMENTO_KEY_TRANSIENT_CHILDREN, transientChildrenAsString);
		}
	}

	@Override
	public String toString() {
		return "TransientElementProvider [parentURI=" + parentURI + ", transientChildren=" + writeTransientChildrenToString(transientChildren) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
