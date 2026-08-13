/**
 * <copyright>
 * 
 * Copyright (c) 2012 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.providers;

import org.eclipse.graphiti.ui.platform.AbstractImageProvider;

/**
 * Provides all the images required in the graphical editor.
 */
public class Hummingbird20PlatformDiagramImageProvider extends AbstractImageProvider {

	/** References to image ComponentType. */
	public static final String IMAGE_COMPONENT_TYPE = Hummingbird20PlatformDiagramTypeProvider.DIAGRAM_TYPE_ID + ".ComponentType"; //$NON-NLS-1$

	/** References to image Interface. */
	public static final String IMAGE_INTERFACE = Hummingbird20PlatformDiagramTypeProvider.DIAGRAM_TYPE_ID + ".Interface"; //$NON-NLS-1$

	/** References to image Port. */
	public static final String IMAGE_PORT = Hummingbird20PlatformDiagramTypeProvider.DIAGRAM_TYPE_ID + ".Port"; //$NON-NLS-1$

	/** References to image Provided Interfaces. */
	public static final String IMAGE_PROVIDED_INTERFACES = Hummingbird20PlatformDiagramTypeProvider.DIAGRAM_TYPE_ID + ".ProvidedInterfaces"; //$NON-NLS-1$

	/** References to image Required Interfaces. */
	public static final String IMAGE_REQUIRED_INTERFACES = Hummingbird20PlatformDiagramTypeProvider.DIAGRAM_TYPE_ID + ".RequiredInterfaces"; //$NON-NLS-1$

	public Hummingbird20PlatformDiagramImageProvider() {
		super();
	}

	@Override
	protected void addAvailableImages() {
		addImageFilePath(IMAGE_COMPONENT_TYPE, "/icons/ComponentType.gif"); //$NON-NLS-1$
		addImageFilePath(IMAGE_INTERFACE, "/icons/Interface.gif"); //$NON-NLS-1$
		addImageFilePath(IMAGE_PORT, "/icons/Port.gif"); //$NON-NLS-1$
		addImageFilePath(IMAGE_PROVIDED_INTERFACES, "/icons/ProvidedInterfaces.gif"); //$NON-NLS-1$
		addImageFilePath(IMAGE_REQUIRED_INTERFACES, "/icons/RequiredInterfaces.gif"); //$NON-NLS-1$
	}
}