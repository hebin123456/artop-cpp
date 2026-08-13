/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [393268] - [EMF Workspace] The Workspace Model Save Manager should handle pre save actions before saving models
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.saving;

import org.eclipse.sphinx.emf.model.IModelDescriptor;

/**
 * Listener for model save life cycle, e.g., dirty state change or pre-save events. {@linkplain ModelSaveManager}
 * notifies such listeners when the dirty state of a {@linkplain IModelDescriptor model} changes (after a modification
 * or a save).
 */
public interface IModelSaveLifecycleListener {

	/**
	 * Handles the dirty state changed event that deals with the model whose corresponding descriptor is given in
	 * parameter.
	 * <p>
	 * <table>
	 * <tr valign=top>
	 * <td><b>Note</b>&nbsp;&nbsp;</td>
	 * <td>This method must be called on the UI thread.</td>
	 * </table>
	 * </tr>
	 * 
	 * @param modelDescriptor
	 *            The descriptor of the model whose dirty state changed.
	 * @since 0.7.0
	 */
	void handleDirtyChangedEvent(IModelDescriptor modelDescriptor);

	/**
	 * Handles the model pre-save event that deals with the model whose corresponding descriptor is given in parameter.
	 * 
	 * @param modelDescriptor
	 *            The descriptor of the model to save.
	 * @since 0.7.0
	 */
	void handlePreSaveEvent(IModelDescriptor modelDescriptor);
}
