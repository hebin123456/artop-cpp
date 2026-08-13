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
 * 
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.navigator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.WorkspaceEditingDomainFactory;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ApplicationEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ComponentEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ConnectionEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20VisualIDRegistry;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Messages;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;

/**
 * @generated
 */
public class Hummingbird20NavigatorContentProvider implements ICommonContentProvider {

	/**
	 * @generated
	 */
	private static final Object[] EMPTY_ARRAY = new Object[0];

	/**
	 * @generated
	 */
	private Viewer myViewer;

	/**
	 * @generated
	 */
	private AdapterFactoryEditingDomain myEditingDomain;

	/**
	 * @generated
	 */
	private WorkspaceSynchronizer myWorkspaceSynchronizer;

	/**
	 * @generated
	 */
	private Runnable myViewerRefreshRunnable;

	/**
	 * @generated
	 */
	@SuppressWarnings({ "unchecked", "serial", "rawtypes" })
	public Hummingbird20NavigatorContentProvider() {
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainFactory.INSTANCE.createEditingDomain();
		myEditingDomain = (AdapterFactoryEditingDomain) editingDomain;
		myEditingDomain.setResourceToReadOnlyMap(new HashMap() {
			@Override
			public Object get(Object key) {
				if (!containsKey(key)) {
					put(key, Boolean.TRUE);
				}
				return super.get(key);
			}
		});
		myViewerRefreshRunnable = new Runnable() {
			@Override
			public void run() {
				if (myViewer != null) {
					myViewer.refresh();
				}
			}
		};
		myWorkspaceSynchronizer = new WorkspaceSynchronizer(editingDomain, new WorkspaceSynchronizer.Delegate() {
			@Override
			public void dispose() {
			}

			@Override
			public boolean handleResourceChanged(final Resource resource) {
				unloadAllResources();
				asyncRefresh();
				return true;
			}

			@Override
			public boolean handleResourceDeleted(Resource resource) {
				unloadAllResources();
				asyncRefresh();
				return true;
			}

			@Override
			public boolean handleResourceMoved(Resource resource, final URI newURI) {
				unloadAllResources();
				asyncRefresh();
				return true;
			}
		});
	}

	/**
	 * @generated
	 */
	@Override
	public void dispose() {
		myWorkspaceSynchronizer.dispose();
		myWorkspaceSynchronizer = null;
		myViewerRefreshRunnable = null;
		myViewer = null;
		unloadAllResources();
		((TransactionalEditingDomain) myEditingDomain).dispose();
		myEditingDomain = null;
	}

	/**
	 * @generated
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		myViewer = viewer;
	}

	/**
	 * @generated
	 */
	void unloadAllResources() {
		for (Resource nextResource : myEditingDomain.getResourceSet().getResources()) {
			nextResource.unload();
		}
	}

	/**
	 * @generated
	 */
	void asyncRefresh() {
		if (myViewer != null && !myViewer.getControl().isDisposed()) {
			myViewer.getControl().getDisplay().asyncExec(myViewerRefreshRunnable);
		}
	}

	/**
	 * @generated
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/**
	 * @generated
	 */
	@Override
	public void restoreState(IMemento aMemento) {
	}

	/**
	 * @generated
	 */
	@Override
	public void saveState(IMemento aMemento) {
	}

	/**
	 * @generated
	 */
	@Override
	public void init(ICommonContentExtensionSite aConfig) {
	}

	/**
	 * @generated
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IFile) {
			IFile file = (IFile) parentElement;
			URI fileURI = URI.createPlatformResourceURI(file.getFullPath().toString(), true);
			Resource resource = myEditingDomain.getResourceSet().getResource(fileURI, true);
			ArrayList<Hummingbird20NavigatorItem> result = new ArrayList<Hummingbird20NavigatorItem>();
			ArrayList<View> topViews = new ArrayList<View>(resource.getContents().size());
			for (EObject o : resource.getContents()) {
				if (o instanceof View) {
					topViews.add((View) o);
				}
			}
			return result.toArray();
		}

		if (parentElement instanceof Hummingbird20NavigatorGroup) {
			Hummingbird20NavigatorGroup group = (Hummingbird20NavigatorGroup) parentElement;
			return group.getChildren();
		}

		if (parentElement instanceof Hummingbird20NavigatorItem) {
			Hummingbird20NavigatorItem navigatorItem = (Hummingbird20NavigatorItem) parentElement;
			if (navigatorItem.isLeaf() || !isOwnView(navigatorItem.getView())) {
				return EMPTY_ARRAY;
			}
			return getViewChildren(navigatorItem.getView(), parentElement);
		}

		return EMPTY_ARRAY;
	}

	/**
	 * @generated
	 */
	private Object[] getViewChildren(View view, Object parentElement) {
		switch (Hummingbird20VisualIDRegistry.getVisualID(view)) {

		case ConnectionEditPart.VISUAL_ID: {
			LinkedList<Hummingbird20AbstractNavigatorItem> result = new LinkedList<Hummingbird20AbstractNavigatorItem>();
			Edge sv = (Edge) view;
			Hummingbird20NavigatorGroup target = new Hummingbird20NavigatorGroup(Messages.NavigatorGroupName_Connection_4001_target,
					"icons/linkTargetNavigatorGroup.gif", parentElement); //$NON-NLS-1$
			Hummingbird20NavigatorGroup source = new Hummingbird20NavigatorGroup(Messages.NavigatorGroupName_Connection_4001_source,
					"icons/linkSourceNavigatorGroup.gif", parentElement); //$NON-NLS-1$
			Collection<View> connectedViews;
			connectedViews = getLinksTargetByType(Collections.singleton(sv), Hummingbird20VisualIDRegistry.getType(ComponentEditPart.VISUAL_ID));
			target.addChildren(createNavigatorItems(connectedViews, target, true));
			connectedViews = getLinksSourceByType(Collections.singleton(sv), Hummingbird20VisualIDRegistry.getType(ComponentEditPart.VISUAL_ID));
			source.addChildren(createNavigatorItems(connectedViews, source, true));
			if (!target.isEmpty()) {
				result.add(target);
			}
			if (!source.isEmpty()) {
				result.add(source);
			}
			return result.toArray();
		}

		case ComponentEditPart.VISUAL_ID: {
			LinkedList<Hummingbird20AbstractNavigatorItem> result = new LinkedList<Hummingbird20AbstractNavigatorItem>();
			Node sv = (Node) view;
			Hummingbird20NavigatorGroup incominglinks = new Hummingbird20NavigatorGroup(Messages.NavigatorGroupName_Component_2001_incominglinks,
					"icons/incomingLinksNavigatorGroup.gif", parentElement); //$NON-NLS-1$
			Hummingbird20NavigatorGroup outgoinglinks = new Hummingbird20NavigatorGroup(Messages.NavigatorGroupName_Component_2001_outgoinglinks,
					"icons/outgoingLinksNavigatorGroup.gif", parentElement); //$NON-NLS-1$
			Collection<View> connectedViews;
			connectedViews = getIncomingLinksByType(Collections.singleton(sv), Hummingbird20VisualIDRegistry.getType(ConnectionEditPart.VISUAL_ID));
			incominglinks.addChildren(createNavigatorItems(connectedViews, incominglinks, true));
			connectedViews = getOutgoingLinksByType(Collections.singleton(sv), Hummingbird20VisualIDRegistry.getType(ConnectionEditPart.VISUAL_ID));
			outgoinglinks.addChildren(createNavigatorItems(connectedViews, outgoinglinks, true));
			if (!incominglinks.isEmpty()) {
				result.add(incominglinks);
			}
			if (!outgoinglinks.isEmpty()) {
				result.add(outgoinglinks);
			}
			return result.toArray();
		}

		case ApplicationEditPart.VISUAL_ID: {
			LinkedList<Hummingbird20AbstractNavigatorItem> result = new LinkedList<Hummingbird20AbstractNavigatorItem>();
			Diagram sv = (Diagram) view;
			Hummingbird20NavigatorGroup links = new Hummingbird20NavigatorGroup(Messages.NavigatorGroupName_Application_1000_links,
					"icons/linksNavigatorGroup.gif", parentElement); //$NON-NLS-1$
			Collection<View> connectedViews;
			connectedViews = getChildrenByType(Collections.singleton(sv), Hummingbird20VisualIDRegistry.getType(ComponentEditPart.VISUAL_ID));
			result.addAll(createNavigatorItems(connectedViews, parentElement, false));
			connectedViews = getDiagramLinksByType(Collections.singleton(sv), Hummingbird20VisualIDRegistry.getType(ConnectionEditPart.VISUAL_ID));
			links.addChildren(createNavigatorItems(connectedViews, links, false));
			if (!links.isEmpty()) {
				result.add(links);
			}
			return result.toArray();
		}
		}
		return EMPTY_ARRAY;
	}

	/**
	 * @generated
	 */
	private Collection<View> getLinksSourceByType(Collection<Edge> edges, String type) {
		LinkedList<View> result = new LinkedList<View>();
		for (Edge nextEdge : edges) {
			View nextEdgeSource = nextEdge.getSource();
			if (type.equals(nextEdgeSource.getType()) && isOwnView(nextEdgeSource)) {
				result.add(nextEdgeSource);
			}
		}
		return result;
	}

	/**
	 * @generated
	 */
	private Collection<View> getLinksTargetByType(Collection<Edge> edges, String type) {
		LinkedList<View> result = new LinkedList<View>();
		for (Edge nextEdge : edges) {
			View nextEdgeTarget = nextEdge.getTarget();
			if (type.equals(nextEdgeTarget.getType()) && isOwnView(nextEdgeTarget)) {
				result.add(nextEdgeTarget);
			}
		}
		return result;
	}

	/**
	 * @generated
	 */
	private Collection<View> getOutgoingLinksByType(Collection<? extends View> nodes, String type) {
		LinkedList<View> result = new LinkedList<View>();
		for (View nextNode : nodes) {
			result.addAll(selectViewsByType(nextNode.getSourceEdges(), type));
		}
		return result;
	}

	/**
	 * @generated
	 */
	private Collection<View> getIncomingLinksByType(Collection<? extends View> nodes, String type) {
		LinkedList<View> result = new LinkedList<View>();
		for (View nextNode : nodes) {
			result.addAll(selectViewsByType(nextNode.getTargetEdges(), type));
		}
		return result;
	}

	/**
	 * @generated
	 */
	private Collection<View> getChildrenByType(Collection<? extends View> nodes, String type) {
		LinkedList<View> result = new LinkedList<View>();
		for (View nextNode : nodes) {
			result.addAll(selectViewsByType(nextNode.getChildren(), type));
		}
		return result;
	}

	/**
	 * @generated
	 */
	private Collection<View> getDiagramLinksByType(Collection<Diagram> diagrams, String type) {
		ArrayList<View> result = new ArrayList<View>();
		for (Diagram nextDiagram : diagrams) {
			result.addAll(selectViewsByType(nextDiagram.getEdges(), type));
		}
		return result;
	}

	// TODO refactor as static method
	/**
	 * @generated
	 */
	private Collection<View> selectViewsByType(Collection<View> views, String type) {
		ArrayList<View> result = new ArrayList<View>();
		for (View nextView : views) {
			if (type.equals(nextView.getType()) && isOwnView(nextView)) {
				result.add(nextView);
			}
		}
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isOwnView(View view) {
		return ApplicationEditPart.MODEL_ID.equals(Hummingbird20VisualIDRegistry.getModelID(view));
	}

	/**
	 * @generated
	 */
	private Collection<Hummingbird20NavigatorItem> createNavigatorItems(Collection<View> views, Object parent, boolean isLeafs) {
		ArrayList<Hummingbird20NavigatorItem> result = new ArrayList<Hummingbird20NavigatorItem>(views.size());
		for (View nextView : views) {
			result.add(new Hummingbird20NavigatorItem(nextView, parent, isLeafs));
		}
		return result;
	}

	/**
	 * @generated
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof Hummingbird20AbstractNavigatorItem) {
			Hummingbird20AbstractNavigatorItem abstractNavigatorItem = (Hummingbird20AbstractNavigatorItem) element;
			return abstractNavigatorItem.getParent();
		}
		return null;
	}

	/**
	 * @generated
	 */
	@Override
	public boolean hasChildren(Object element) {
		return element instanceof IFile || getChildren(element).length > 0;
	}

}
