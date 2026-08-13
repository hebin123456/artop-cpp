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
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.create;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.providers.Hummingbird20PlatformDiagramImageProvider;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.util.ExampleUtil;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

public class CreateInterfaceFeature extends AbstractCreateFeature {

	public static final String TITLE = "Create " + TypeModel20Package.eINSTANCE.getInterface().getName(); //$NON-NLS-1$
	public static final String USER_QUESTION = "Enter new " + TypeModel20Package.eINSTANCE.getInterface().getName() + " name"; //$NON-NLS-1$ //$NON-NLS-2$

	public CreateInterfaceFeature(IFeatureProvider fp) {
		super(fp, TypeModel20Package.eINSTANCE.getInterface().getName(), "Create " + TypeModel20Package.eINSTANCE.getInterface().getName()); //$NON-NLS-1$
	}

	@Override
	public String getCreateImageId() {
		return Hummingbird20PlatformDiagramImageProvider.IMAGE_INTERFACE;
	}

	@Override
	public boolean canCreate(ICreateContext context) {
		return context.getTargetContainer() instanceof Diagram;
	}

	@Override
	public Object[] create(ICreateContext context) {
		// Ask user for Interface name
		String newInterfaceName = ExampleUtil.askString(TITLE, USER_QUESTION, ""); //$NON-NLS-1$
		if (newInterfaceName == null || newInterfaceName.trim().length() == 0) {
			return EMPTY;
		}
		// Create Interface
		Interface newInterface = TypeModel20Factory.eINSTANCE.createInterface();
		newInterface.setName(newInterfaceName);
		// Do the add
		addGraphicalRepresentation(context, newInterface);
		// Return newly created Interface
		return new Object[] { newInterface };
	}
}