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
package org.eclipse.sphinx.emf.explorer.decorators;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.workspace.resources.BasicModelProblemMarkerFinder;
import org.eclipse.sphinx.emf.workspace.ui.decorators.AbstractTreeContentProblemDecorator;
import org.eclipse.sphinx.emf.workspace.ui.decorators.TreeItemDecorationCalculator;
import org.eclipse.sphinx.emf.workspace.ui.viewers.ITreeContentIterator;
import org.eclipse.sphinx.platform.resources.IProblemMarkerFinder;

/**
 * A reusable {@link AbstractTreeContentProblemDecorator tree content problem decorator} implementation which can be
 * used to decorate mixed workspace resource and model element tree content according to the {@link IMarker#PROBLEM
 * problem markers} that relate to the tree item in question or to any of its direct or indirect
 * {@link ITreeContentProvider#getChildren(Object) children}. Supports {@link IResource}, {@link EObject},
 * {@link TransientItemProvider}, {@link IWrapperItemProvider} tree items.
 * <p>
 * Uses an instance of {@link BasicModelProblemMarkerFinder} to detect the problem markers for the given tree item as
 * well as for its direct and indirect children.
 * </p>
 * <p>
 * Assumes that the {@link ITreeContentProvider content provider} to be used to retrieve the direct and indirect
 * children of the given tree item is made available through a {@link IDecorationContext#getProperties() decoration
 * context property} named <code>org.eclipse.jface.viewers.ITreeContentProvider</code> that is stored on the
 * {@link IDecoration decoration definition} passed to the {@link #decorate(Object, IDecoration)} method (see
 * {@link #getContentProvider(IDecoration)} for details).
 * </p>
 * <p>
 * Clients must contribute this implementation or a subclass of it to the <code>org.eclipse.ui.decorators</code>
 * extension point to make it effective.
 * </p>
 *
 * @see AbstractTreeContentProblemDecorator
 * @see BasicModelProblemMarkerFinder
 */
public class BasicExplorerProblemDecorator extends AbstractTreeContentProblemDecorator {

	protected static class ModelExplorerItemFilter implements ITreeContentIterator.IItemFilter {
		@Override
		public boolean accept(Object item) {
			return item instanceof IResource || item instanceof EObject || item instanceof TransientItemProvider
					|| item instanceof IWrapperItemProvider;
		}
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.ui.decorators.AbstractTreeContentProblemDecorator#createProblemMarkerFinder()
	 */
	@Override
	protected IProblemMarkerFinder createProblemMarkerFinder() {
		return new BasicModelProblemMarkerFinder();
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.ui.decorators.AbstractTreeContentProblemDecorator#createDecorationCalculator(org
	 * .eclipse.sphinx.platform.resources.IProblemMarkerFinder)
	 */
	@Override
	protected TreeItemDecorationCalculator createDecorationCalculator(IProblemMarkerFinder problemMarkerFinder) {
		TreeItemDecorationCalculator calculator = super.createDecorationCalculator(problemMarkerFinder);
		calculator.setItemFilter(new ModelExplorerItemFilter());
		return calculator;
	}
}
