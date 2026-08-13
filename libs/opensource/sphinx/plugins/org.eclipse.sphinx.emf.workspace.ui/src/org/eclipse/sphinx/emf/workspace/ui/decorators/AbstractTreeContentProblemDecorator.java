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
package org.eclipse.sphinx.emf.workspace.ui.decorators;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.sphinx.emf.workspace.ui.decorators.TreeItemDecorationCalculator.DecorationOverlayKind;
import org.eclipse.sphinx.emf.workspace.ui.internal.Activator;
import org.eclipse.sphinx.platform.resources.IProblemMarkerFinder;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.decorators.DecorationBuilder;

/**
 * An extensible {@link ILightweightLabelDecorator lightweight label decorator} implementation which can be used to
 * decorate {@link TreeViewer tree viewer} items according to the {@link IMarker#PROBLEM problem markers} that relate to
 * the tree item in question or to any of its direct or indirect {@link ITreeContentProvider#getChildren(Object)
 * children}.
 * <p>
 * Requires an instance of {@link TreeItemDecorationCalculator} to calculate the decoration state of the given tree item
 * and an instance of {@link IProblemMarkerFinder} to detect the problem markers for the given tree item as well as for
 * its direct and indirect children.
 * </p>
 * <p>
 * Assumes that the {@link ITreeContentProvider content provider} to be used to retrieve the direct and indirect
 * children of the given tree item is made available through a {@link IDecorationContext#getProperties() decoration
 * context property} named <code>org.eclipse.jface.viewers.ITreeContentProvider</code> that is stored on the
 * {@link IDecoration decoration definition} passed to the {@link #decorate(Object, IDecoration)} method (see
 * {@link #getContentProvider(IDecoration)} for details).
 * </p>
 *
 * @see TreeItemDecorationCalculator
 * @see IProblemMarkerFinder
 */
@SuppressWarnings("restriction")
public abstract class AbstractTreeContentProblemDecorator implements ILightweightLabelDecorator {

	protected TreeItemDecorationCalculator decorationCalculator;
	protected IResourceChangeListener problemMarkerChangeListener;

	public AbstractTreeContentProblemDecorator() {
		IProblemMarkerFinder problemMarkerFinder = createProblemMarkerFinder();
		decorationCalculator = createDecorationCalculator(problemMarkerFinder);

		problemMarkerChangeListener = createProblemMarkerChangeListener();
		ServiceCaller.callOnce(getClass(), IWorkspace.class, ws -> {
			ws.addResourceChangeListener(problemMarkerChangeListener, IResourceChangeEvent.POST_CHANGE);
		});
	}

	protected abstract IProblemMarkerFinder createProblemMarkerFinder();

	protected TreeItemDecorationCalculator createDecorationCalculator(IProblemMarkerFinder problemMarkerFinder) {
		return new TreeItemDecorationCalculator(problemMarkerFinder);
	}

	/**
	 * Creates an {@link IResourceChangeListener} in order to wake up decoration of IContainer and IResource.
	 */
	protected IResourceChangeListener createProblemMarkerChangeListener() {
		return new IResourceChangeListener() {
			@Override
			public void resourceChanged(IResourceChangeEvent event) {
				Assert.isNotNull(event);

				IMarkerDelta[] markerDelta = event.findMarkerDeltas(IMarker.PROBLEM, true);
				if (markerDelta != null && markerDelta.length > 0) {
					decorationCalculator.reset();
				}
			}
		};
	}

	/*
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
	 * org.eclipse.jface.viewers.IDecoration)
	 */
	@Override
	public void decorate(Object element, IDecoration decoration) {
		ITreeContentProvider contentProvider = getContentProvider(decoration);
		if (contentProvider != null) {
			String overlayImageName = null;
			DecorationOverlayKind overlayKind = decorationCalculator.getDecorationOverlayKind(contentProvider, element);
			switch (overlayKind) {
			case NONE:
				break;
			case WARNING:
				overlayImageName = ISharedImages.IMG_DEC_FIELD_WARNING;
				break;
			case ERROR:
				overlayImageName = ISharedImages.IMG_DEC_FIELD_ERROR;
				break;
			default:
				PlatformLogUtil.logAsError(Activator.getPlugin(), new RuntimeException("Invalid decoration overlay kind: " + overlayKind)); //$NON-NLS-1$
				break;
			}

			if (overlayImageName != null) {
				ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
				ImageDescriptor overlayImageDescriptor = sharedImages.getImageDescriptor(overlayImageName);
				decoration.addOverlay(overlayImageDescriptor, IDecoration.BOTTOM_LEFT);
			}
		}
	}

	protected ITreeContentProvider getContentProvider(IDecoration decoration) {
		if (decoration instanceof DecorationBuilder) {
			DecorationBuilder builder = (DecorationBuilder) decoration;
			IDecorationContext context = builder.getDecorationContext();
			return (ITreeContentProvider) context.getProperty(ITreeContentProvider.class.getName());
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	/*
	 * @see
	 * org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	@Override
	public void removeListener(ILabelProviderListener listener) {

	}

	/*
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		ServiceCaller.callOnce(getClass(), IWorkspace.class, ws -> {
			ws.removeResourceChangeListener(problemMarkerChangeListener);
		});
	}
}