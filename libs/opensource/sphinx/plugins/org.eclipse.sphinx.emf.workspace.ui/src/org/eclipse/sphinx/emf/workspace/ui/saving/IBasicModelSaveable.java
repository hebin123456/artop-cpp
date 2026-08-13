/**
 * <copyright>
 *
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     Elektrobit - [572592] Prompt for model saving on project close
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.saving;

import org.eclipse.sphinx.emf.model.IModelDescriptor;

/**
 * Saveable for a model
 */
public interface IBasicModelSaveable {

	/**
	 * @return The descriptor of the model this saveable is associated to
	 */
	public IModelDescriptor getModelDescriptor();
}
