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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramLink;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.providers.Hummingbird20PlatformDiagramImageProvider;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.util.ExampleUtil;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

public class CreatePortFeature extends AbstractCreateFeature {

	public static final String TITLE = "Create " + TypeModel20Package.eINSTANCE.getPort().getName(); //$NON-NLS-1$
	public static final String USER_QUESTION = "Enter new " + TypeModel20Package.eINSTANCE.getPort().getName() + " name"; //$NON-NLS-1$ //$NON-NLS-2$

	public CreatePortFeature(IFeatureProvider fp) {
		super(fp, TypeModel20Package.eINSTANCE.getPort().getName(), "Create " + TypeModel20Package.eINSTANCE.getPort().getName()); //$NON-NLS-1$
	}

	@Override
	public String getCreateImageId() {
		return Hummingbird20PlatformDiagramImageProvider.IMAGE_PORT;
	}

	@Override
	public boolean canCreate(ICreateContext context) {
		// Create a port only for a ComponentType
		ContainerShape container = context.getTargetContainer();
		PictogramLink link = container.getLink();
		EList<EObject> bo = link.getBusinessObjects();
		if (bo.get(0) instanceof ComponentType) {
			return true;
		}
		return false;
	}

	@Override
	public Object[] create(ICreateContext context) {
		// Ask user for Port name
		String newPortName = ExampleUtil.askString(TITLE, USER_QUESTION, ""); //$NON-NLS-1$
		if (newPortName == null || newPortName.trim().length() == 0) {
			return EMPTY;
		}
		// Create Port
		Port newPort = TypeModel20Factory.eINSTANCE.createPort();
		newPort.setName(newPortName);
		// Do the add
		addGraphicalRepresentation(context, newPort);
		// Return newly created port
		return new Object[] { newPort };
	}

}
