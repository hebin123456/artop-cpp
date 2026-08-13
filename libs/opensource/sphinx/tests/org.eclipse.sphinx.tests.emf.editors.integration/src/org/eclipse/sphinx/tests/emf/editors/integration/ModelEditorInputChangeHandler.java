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
package org.eclipse.sphinx.tests.emf.editors.integration;

import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.emf.editors.IModelEditorInputChangeHandler;
import org.eclipse.ui.IEditorInput;

public class ModelEditorInputChangeHandler implements IModelEditorInputChangeHandler {

	private boolean editorInputAdded;
	private boolean editorInputRemoved;
	private boolean editorInputChanged;
	private boolean editorInputMoved;

	public boolean isEditorInputAdded() {
		return editorInputAdded;
	}

	public boolean isEditorInputRemoved() {
		return editorInputRemoved;
	}

	public boolean isEditorInputChanged() {
		return editorInputChanged;
	}

	public boolean isEditorInputMoved() {
		return editorInputMoved;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEditorInputObjectAdded(IEditorInput editorInput, Set<EObject> addedObjects) {
		editorInputAdded = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEditorInputObjectRemoved(IEditorInput editorInput, Set<EObject> removedObjects) {
		editorInputRemoved = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEditorInputObjectChanged(IEditorInput editorInput, Set<EObject> changedObjects) {
		editorInputChanged = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEditorInputObjectMoved(IEditorInput editorInput, Set<EObject> movedObjects) {
		editorInputMoved = true;
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
