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
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.create;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.providers.Hummingbird20PlatformDiagramImageProvider;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.util.ExampleUtil;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

/**
 * Graphiti feature for adding Hummingbird 2.0 {@link ComponentType} elements.
 */
public class CreateComponentTypeFeature extends AbstractCreateFeature {

	public static String CREATE_CONNECTION_NAME = "Component Type"; //$NON-NLS-1$
	public static String CREATE_CONNECTION_DESCRIPTION = "Create Component Type"; //$NON-NLS-1$
	public static final String TITLE = "Create " + TypeModel20Package.eINSTANCE.getComponentType().getName(); //$NON-NLS-1$
	public static final String USER_QUESTION = "Enter new " + TypeModel20Package.eINSTANCE.getComponentType().getName() + " name"; //$NON-NLS-1$ //$NON-NLS-2$

	public CreateComponentTypeFeature(IFeatureProvider fp) {
		super(fp, CREATE_CONNECTION_NAME, CREATE_CONNECTION_DESCRIPTION);
	}

	@Override
	public String getCreateImageId() {
		return Hummingbird20PlatformDiagramImageProvider.IMAGE_COMPONENT_TYPE;
	}

	@Override
	public boolean canCreate(ICreateContext context) {
		return context.getTargetContainer() instanceof Diagram;
	}

	@Override
	public Object[] create(ICreateContext context) {
		// Ask user for ComponentType name
		String newComponentTypeName = ExampleUtil.askString(TITLE, USER_QUESTION, ""); //$NON-NLS-1$
		if (newComponentTypeName == null || newComponentTypeName.trim().length() == 0) {
			return EMPTY;
		}
		// Create ComponentType
		ComponentType newComponentType = TypeModel20Factory.eINSTANCE.createComponentType();
		newComponentType.setName(newComponentTypeName);
		// Do the add
		addGraphicalRepresentation(context, newComponentType);
		// Return newly created component type
		return new Object[] { newComponentType };
	}
}