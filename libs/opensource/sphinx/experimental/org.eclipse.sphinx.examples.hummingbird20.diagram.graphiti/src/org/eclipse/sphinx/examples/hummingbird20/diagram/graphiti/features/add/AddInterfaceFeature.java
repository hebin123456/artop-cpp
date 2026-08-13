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
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.add;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.PictogramLink;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;
import org.eclipse.sphinx.graphiti.workspace.ui.util.DiagramUtil;

public class AddInterfaceFeature extends AbstractAddFeature {

	public static final IColorConstant INTERFACE_COLOR_TEXT_FOREGROUND = new ColorConstant(0, 0, 0);
	public static final IColorConstant INTERFACE_COLOR_FOREGROUND = new ColorConstant(98, 131, 167);
	public static final IColorConstant INTERFACE_COLOR_BACKGROUND = new ColorConstant(212, 231, 248);
	public static final int INTERFACE_WIDTH_DEFAULT = 80;
	public static final int INTERFACE_HEIGHT_DEFAULT = 80;
	public static final int INTERFACE_HEIGHT_UPPER_COMPARTMENT_DEFAULT = 20;

	public AddInterfaceFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public boolean canAdd(IAddContext context) {
		// Is it an add request for an Interface ?
		if (context.getNewObject() instanceof Interface) {
			// Is it an add request for adding the Interface to a DIAGRAM?
			if (context.getTargetContainer() instanceof Diagram) {
				return true;
			}
		}
		return false;
	}

	@Override
	public PictogramElement add(IAddContext context) {
		Interface interfaceToAdd = (Interface) context.getNewObject();
		Diagram targetContainer = (Diagram) context.getTargetContainer();
		PictogramLink link = targetContainer.getLink();
		// Get the parent Business Object
		EObject parentBO = link.getBusinessObjects().get(0);
		// Update EMF resource
		EReference referenceId = TypeModel20Package.Literals.PLATFORM__INTERFACES;
		DiagramUtil.addObjectToBOResource(parentBO, referenceId, interfaceToAdd);

		IPeCreateService peCreateService = Graphiti.getPeCreateService();
		IGaService gaService = Graphiti.getGaService();
		// CONTAINER SHAPE WITH RECTANGLE
		// Create container shape
		ContainerShape containerShape = peCreateService.createContainerShape(targetContainer, true);
		{
			// Create and set graphics algorithm
			RoundedRectangle rectangle = gaService.createRoundedRectangle(containerShape, 20, 20);
			rectangle.setForeground(manageColor(INTERFACE_COLOR_FOREGROUND));
			rectangle.setBackground(manageColor(INTERFACE_COLOR_BACKGROUND));
			rectangle.setLineWidth(2);
			gaService.setLocationAndSize(rectangle, context.getX(), context.getY(), INTERFACE_WIDTH_DEFAULT, INTERFACE_HEIGHT_DEFAULT);
			// Create link and wire it up
			link(containerShape, interfaceToAdd);
		}
		// SHAPE WITH LINE
		{
			// Create shape for line
			Shape lineShape = peCreateService.createShape(containerShape, false);
			// Create and set graphics algorithm
			Polyline polyline = gaService.createPolyline(lineShape, new int[] { 0, INTERFACE_HEIGHT_UPPER_COMPARTMENT_DEFAULT,
					INTERFACE_WIDTH_DEFAULT, INTERFACE_HEIGHT_UPPER_COMPARTMENT_DEFAULT });
			polyline.setForeground(manageColor(INTERFACE_COLOR_FOREGROUND));
			polyline.setLineWidth(2);
		}
		// SHAPE WITH TEXT
		{
			// Create shape for text
			Shape textShape = peCreateService.createShape(containerShape, false);
			// Create and set text graphics algorithm
			Text text = gaService.createText(textShape, interfaceToAdd.getName());
			text.setForeground(manageColor(INTERFACE_COLOR_TEXT_FOREGROUND));
			text.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
			text.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
			text.setFont(gaService.manageDefaultFont(getDiagram(), false, true));
			gaService.setLocationAndSize(text, 0, 0, INTERFACE_WIDTH_DEFAULT, INTERFACE_HEIGHT_UPPER_COMPARTMENT_DEFAULT);
			// Create link and wire it up
			link(textShape, interfaceToAdd);
		}
		// call the layout feature
		layoutPictogramElement(containerShape);
		// Create Chopbox anchor
		peCreateService.createChopboxAnchor(containerShape);
		return containerShape;
	}
}