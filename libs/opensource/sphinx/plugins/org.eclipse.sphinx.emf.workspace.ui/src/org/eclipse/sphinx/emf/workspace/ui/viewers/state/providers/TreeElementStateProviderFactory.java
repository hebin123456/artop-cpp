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
package org.eclipse.sphinx.emf.workspace.ui.viewers.state.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.workspace.ui.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IMemento;

public class TreeElementStateProviderFactory {

	public static final String MEMENTO_TYPE_GROUP_SELECTED = Activator.getPlugin().getSymbolicName() + ".selected"; //$NON-NLS-1$
	public static final String MEMENTO_TYPE_GROUP_EXPANDED = Activator.getPlugin().getSymbolicName() + ".expanded"; //$NON-NLS-1$

	public static final String MEMENTO_TYPE_ELEMENT_PROJECT = Activator.getPlugin().getSymbolicName() + ".project"; //$NON-NLS-1$
	public static final String MEMENTO_TYPE_ELEMENT_FOLDER = Activator.getPlugin().getSymbolicName() + ".folder"; //$NON-NLS-1$
	public static final String MEMENTO_TYPE_ELEMENT_FILE = Activator.getPlugin().getSymbolicName() + ".file"; //$NON-NLS-1$
	public static final String MEMENTO_TYPE_ELEMENT_EOBJECT = Activator.getPlugin().getSymbolicName() + ".eObject"; //$NON-NLS-1$
	public static final String MEMENTO_TYPE_ELEMENT_TRANSIENT = Activator.getPlugin().getSymbolicName() + ".transient"; //$NON-NLS-1$

	public static final String MEMENTO_KEY_NAME = "name"; //$NON-NLS-1$
	public static final String MEMENTO_KEY_PATH = "path"; //$NON-NLS-1$
	public static final String MEMENTO_KEY_URI = "uri"; //$NON-NLS-1$
	public static final String MEMENTO_KEY_PARENT_URI = "parentURI"; //$NON-NLS-1$
	public static final String MEMENTO_KEY_TRANSIENT_CHILDREN = "transientChildren"; //$NON-NLS-1$

	private TreeViewer viewer;

	public TreeElementStateProviderFactory(TreeViewer viewer) {
		Assert.isNotNull(viewer);
		this.viewer = viewer;
	}

	public ITreeElementStateProvider create(Object element) {
		return create(getParentPath(element), element);
	}

	public ITreeElementStateProvider create(TreePath treePath) {
		Assert.isNotNull(treePath);
		return create(treePath.getParentPath(), treePath.getLastSegment());
	}

	protected ITreeElementStateProvider create(TreePath parentPath, Object element) {
		Assert.isNotNull(parentPath != null);

		if (element instanceof IProject) {
			return new ProjectElementStateProvider(viewer, (IProject) element);
		} else if (element instanceof IFolder) {
			return new FolderElementStateProvider(viewer, (IFolder) element);
		} else if (element instanceof IFile) {
			return new FileElementStateProvider(viewer, (IFile) element);
		} else if (element instanceof EObject) {
			URI uri = getURI((EObject) element);
			return new EObjectElementStateProvider(viewer, uri);
		} else if (isTransientElement(element)) {
			Object eObjectParent = null;
			List<Object> transientChildren = new ArrayList<Object>();
			transientChildren.add(element);
			for (int i = parentPath.getSegmentCount() - 1; i >= 0; i--) {
				Object parent = parentPath.getSegment(i);
				if (!isTransientElement(parent)) {
					eObjectParent = parent;
					break;
				}
				transientChildren.add(parent);
			}
			Collections.reverse(transientChildren);

			if (eObjectParent instanceof EObject) {
				URI eObjectParentURI = getURI((EObject) eObjectParent);
				return createTransientElementProvider(eObjectParentURI, transientChildren);
			}
		}
		return null;
	}

	public ITreeElementStateProvider createFromMemento(IMemento elementMemento) {
		if (MEMENTO_TYPE_ELEMENT_PROJECT.equals(elementMemento.getType())) {
			return new ProjectElementStateProvider(viewer, elementMemento);
		} else if (MEMENTO_TYPE_ELEMENT_FOLDER.equals(elementMemento.getType())) {
			return new FolderElementStateProvider(viewer, elementMemento);
		} else if (MEMENTO_TYPE_ELEMENT_FILE.equals(elementMemento.getType())) {
			return new FileElementStateProvider(viewer, elementMemento);
		} else if (MEMENTO_TYPE_ELEMENT_EOBJECT.equals(elementMemento.getType())) {
			return new EObjectElementStateProvider(viewer, elementMemento);
		} else if (MEMENTO_TYPE_ELEMENT_TRANSIENT.equals(elementMemento.getType())) {
			return createTransientElementStateProvider(elementMemento);
		}
		return null;
	}

	protected boolean isTransientElement(Object element) {
		return element instanceof TransientItemProvider || element instanceof IWrapperItemProvider;
	}

	protected TransientElementStateProvider createTransientElementProvider(URI eObjectParentURI, List<Object> transientChildren) {
		return new TransientElementStateProvider(viewer, eObjectParentURI, transientChildren);
	}

	protected TransientElementStateProvider createTransientElementStateProvider(IMemento elementMemento) {
		return new TransientElementStateProvider(viewer, elementMemento);
	}

	protected TreePath getParentPath(Object element) {
		IContentProvider contentProvider = viewer.getContentProvider();
		if (contentProvider instanceof ITreePathContentProvider) {
			TreePath[] parents = ((ITreePathContentProvider) contentProvider).getParents(element);
			if (parents.length > 0) {
				return parents[0];
			}
		}
		return TreePath.EMPTY;
	}

	protected URI getURI(final EObject eObject) {
		if (eObject != null) {
			final TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(eObject);
			if (editingDomain != null) {
				try {
					return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<URI>() {
						@Override
						public void run() {
							setResult(EcoreResourceUtil.getURI(eObject, true));
						}
					});
				} catch (InterruptedException ex) {
					PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
				}
			} else {
				return EcoreResourceUtil.getURI(eObject, true);
			}
		}
		return null;
	}
}
