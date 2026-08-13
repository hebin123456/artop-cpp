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
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.providers;

import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20DiagramEditorPlugin;

/**
 * @generated
 */
public class ElementInitializers {

	protected ElementInitializers() {
		// use #getInstance to access cached instance
	}

	/**
	 * @generated
	 */
	public static ElementInitializers getInstance() {
		ElementInitializers cached = Hummingbird20DiagramEditorPlugin.getInstance().getElementInitializers();
		if (cached == null) {
			Hummingbird20DiagramEditorPlugin.getInstance().setElementInitializers(cached = new ElementInitializers());
		}
		return cached;
	}
}
