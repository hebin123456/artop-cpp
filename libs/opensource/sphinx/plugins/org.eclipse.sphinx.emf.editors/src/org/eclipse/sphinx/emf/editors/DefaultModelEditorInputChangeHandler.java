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
package org.eclipse.sphinx.emf.editors;

import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.IEditorInput;

public class DefaultModelEditorInputChangeHandler implements IModelEditorInputChangeHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEditorInputObjectAdded(IEditorInput editorInput, Set<EObject> addedObjects) {
		// Do nothing by default
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEditorInputObjectRemoved(IEditorInput editorInput, Set<EObject> removedObjects) {
		// Do nothing by default
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEditorInputObjectChanged(IEditorInput editorInput, Set<EObject> changedObjects) {
		// Do nothing by default
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEditorInputObjectMoved(IEditorInput editorInput, Set<EObject> movedObjects) {
		// Do nothing by default
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEditorInputResourceLoaded(IEditorInput editorInput) {
		// Do nothing by default
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEditorInputResourceUnloaded(IEditorInput editorInput) {
		// Do nothing by default
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEditorInputResourceMoved(IEditorInput editorInput, URI oldURI, URI newURI) {
		// Do nothing by default
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEditorInputResourceRemoved(IEditorInput editorInput) {
		// Do nothing by default
	}
}
