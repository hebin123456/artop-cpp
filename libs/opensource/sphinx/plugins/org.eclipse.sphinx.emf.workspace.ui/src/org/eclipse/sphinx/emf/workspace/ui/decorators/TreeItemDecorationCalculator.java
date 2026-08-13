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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.sphinx.emf.workspace.ui.internal.Activator;
import org.eclipse.sphinx.emf.workspace.ui.viewers.ITreeContentIterator;
import org.eclipse.sphinx.emf.workspace.ui.viewers.TreeContentProviderIterator;
import org.eclipse.sphinx.platform.resources.IProblemMarkerFinder;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * Calculates the decoration state for {@link TreeViewer tree viewer} items according to {@link IMarker#PROBLEM problem
 * markers} that relate to the tree item in question or to any of its direct or indirect
 * {@link ITreeContentProvider#getChildren(Object) children}. Caches the decoration state of every tree item once it has
 * been calculated to increase the decoration performance when the same tree item is requested to be decorated multiple
 * times.
 * <p>
 * Requires an instance of {@link IProblemMarkerFinder} to detect the problem markers for the given tree item as well as
 * for its direct and indirect children.
 * </p>
 *
 * @see IProblemMarkerFinder
 */
public class TreeItemDecorationCalculator {

	public enum DecorationOverlayKind {
		NONE, WARNING, ERROR;

		public DecorationOverlayKind update(DecorationOverlayKind overlayKind) {
			return isLessThan(overlayKind) ? overlayKind : this;
		}

		public boolean isLessThan(DecorationOverlayKind otherOverlayKind) {
			Assert.isNotNull(otherOverlayKind);
			return ordinal() < otherOverlayKind.ordinal();
		}

		public boolean isGreaterThan(DecorationOverlayKind otherOverlayKind) {
			Assert.isNotNull(otherOverlayKind);
			return ordinal() > otherOverlayKind.ordinal();
		}
	}

	protected IProblemMarkerFinder problemMarkerFinder;

	private ITreeContentIterator.IItemFilter itemFilter;

	protected Map<Object, TreeItemDecorationCalculator.DecorationOverlayKind> decorationOverlayCache = new HashMap<Object, TreeItemDecorationCalculator.DecorationOverlayKind>();

	public TreeItemDecorationCalculator(IProblemMarkerFinder problemMarkerFinder) {
		Assert.isNotNull(problemMarkerFinder);
		this.problemMarkerFinder = problemMarkerFinder;
	}

	public ITreeContentIterator.IItemFilter getItemFilter() {
		return itemFilter;
	}

	public void setItemFilter(ITreeContentIterator.IItemFilter itemFilter) {
		this.itemFilter = itemFilter;
	}

	public TreeItemDecorationCalculator.DecorationOverlayKind getDecorationOverlayKind(ITreeContentProvider contentProvider, Object item) {
		Assert.isNotNull(contentProvider);
		try {
			DecorationOverlayKind itemOverlayKind = decorationOverlayCache.get(item);
			if (itemOverlayKind != null) {
				return itemOverlayKind;
			}

			ITreeContentIterator iter = new TreeContentProviderIterator(contentProvider, item, itemFilter);
			while (iter.hasNext()) {
				Object childItem = iter.next();
				DecorationOverlayKind childItemOverlayKind = decorationOverlayCache.get(childItem);
				if (childItemOverlayKind != null) {
					iter.prune();
				} else {
					int severity = problemMarkerFinder.getSeverity(childItem);
					childItemOverlayKind = getDecorationOverlayKind(severity);

					if (iter.isRecurrent()) {
						resetCachedAncestorDecorationKinds(contentProvider, childItem, item);
					} else {
						decorationOverlayCache.put(childItem, childItemOverlayKind);
						updateCachedAncestorDecorationOverlayKinds(contentProvider, childItem, childItemOverlayKind);
					}
				}

				if (itemOverlayKind == null || childItemOverlayKind.isGreaterThan(itemOverlayKind)) {
					itemOverlayKind = childItemOverlayKind;
				}
				if (itemOverlayKind == DecorationOverlayKind.ERROR) {
					break;
				}
			}

			decorationOverlayCache.put(item, itemOverlayKind);
			updateCachedAncestorDecorationOverlayKinds(contentProvider, item, itemOverlayKind);
			return itemOverlayKind;
		} catch (CoreException ex) {
			PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
		}
		return DecorationOverlayKind.NONE;
	}

	protected DecorationOverlayKind getDecorationOverlayKind(int severity) {
		switch (severity) {
		case IMarker.SEVERITY_WARNING:
			return DecorationOverlayKind.WARNING;
		case IMarker.SEVERITY_ERROR:
			return DecorationOverlayKind.ERROR;
		default:
			return DecorationOverlayKind.NONE;
		}
	}

	protected void updateCachedAncestorDecorationOverlayKinds(ITreeContentProvider contentProvider, Object object,
			DecorationOverlayKind newOverlayKind) {
		Assert.isNotNull(contentProvider);
		Assert.isNotNull(newOverlayKind);

		Object parent = contentProvider.getParent(object);
		while (parent != null) {
			DecorationOverlayKind cachedOverlayKind = decorationOverlayCache.get(parent);
			if (cachedOverlayKind != null && newOverlayKind.isGreaterThan(cachedOverlayKind)) {
				decorationOverlayCache.put(parent, newOverlayKind);
			} else {
				break;
			}
			parent = contentProvider.getParent(parent);
		}
	}

	protected void resetCachedAncestorDecorationKinds(ITreeContentProvider contentProvider, Object object, Object limit) {
		Assert.isNotNull(contentProvider);

		Object parent = contentProvider.getParent(object);
		while (parent != limit && parent != null) {
			decorationOverlayCache.remove(parent);
			parent = contentProvider.getParent(parent);
		}
	}

	public void reset() {
		decorationOverlayCache.clear();
		problemMarkerFinder.reset();
	}
}