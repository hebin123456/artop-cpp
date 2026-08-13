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
 *     itemis - [392424] Migrate Sphinx integration of Graphiti to Graphiti 0.9.x
 * 
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.providers;

import org.eclipse.graphiti.dt.AbstractDiagramTypeProvider;

/**
 * Diagram Types Provider of Hummingbird20
 */
public class Hummingbird20PlatformDiagramTypeProvider extends AbstractDiagramTypeProvider {

	public static String DIAGRAM_TYPE_ID = "org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.diagramTypes.platform"; //$NON-NLS-1$
	public static String DIAGRAM_TYPE_TYPE = "Hummingbird20PlatformDiagramType"; //$NON-NLS-1$
	public static String DIAGRAM_TYPE_NAME = "Hummingbird 2.0 Platform Diagram Type"; //$NON-NLS-1$

	public Hummingbird20PlatformDiagramTypeProvider() {
		setFeatureProvider(new Hummingbird20PlatformDiagramFeatureProvider(this));
	}

	/**
	 * If isAutoUpdateAtStartup returns true, then the diagram will be updated, when it is initially opened in the
	 * graphical editor. This will make the editor dirty.
	 */
	@Override
	public boolean isAutoUpdateAtStartup() {
		return true;
	}

	/**
	 * If isAutoUpdateAtRuntime returns true, then the diagram will be updated, when it is already open in the graphical
	 * editor, but only if the editor is already dirty.
	 */
	@Override
	public boolean isAutoUpdateAtRuntime() {
		return true;
	}

	/**
	 * If the editor is already dirty and the user chooses to discard his changes (reset the diagram), when a change
	 * from outside the diagram occurs.
	 */
	@Override
	public boolean isAutoUpdateAtReset() {
		return true;
	}
}