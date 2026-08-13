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
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.add;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
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
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;
import org.eclipse.sphinx.graphiti.workspace.ui.util.DiagramUtil;

/**
 * Graphiti feature for adding Hummingbird 2.0 {@link ComponentType} elements.
 */
public class AddComponentTypeFeature extends AbstractAddShapeFeature {

	public static final IColorConstant COMPONENT_TYPE_COLOR_TEXT_FOREGROUND = new ColorConstant(0, 0, 0);
	public static final IColorConstant COMPONENT_TYPE_COLOR_FOREGROUND = new ColorConstant(98, 131, 167);
	public static final IColorConstant COMPONENT_TYPE_COLOR_BACKGROUND = new ColorConstant(212, 231, 248);
	public static final int COMPONENT_TYPE_WIDTH_DEFAULT = 200;
	public static final int COMPONENT_TYPE_HEIGHT_DEFAULT = 100;
	public static final int COMPONENT_TYPE_HEIGHT_UPPER_COMPARTMENT_DEFAULT = 20;
	public static final int PORT_SIZE = 10;

	public AddComponentTypeFeature(IFeatureProvider fp) {
		super(fp);
	}

	/*
	 * @see org.eclipse.graphiti.func.IAdd#canAdd(org.eclipse.graphiti.features.context.IAddContext)
	 */
	@Override
	public boolean canAdd(IAddContext context) {
		// Is it an add request for a ComponentType?
		if (context.getNewObject() instanceof ComponentType) {
			// Is it an add request for adding the ComponentType to a DIAGRAM?
			if (context.getTargetContainer() instanceof Diagram) {
				return true;
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.graphiti.func.IAdd#add(org.eclipse.graphiti.features.context.IAddContext)
	 */
	@Override
	public PictogramElement add(IAddContext context) {
		ComponentType componentTypeToAdd = (ComponentType) context.getNewObject();
		Diagram targetContainer = (Diagram) context.getTargetContainer();
		PictogramLink link = targetContainer.getLink();
		// Get the parent Business Object
		EObject parentBO = link.getBusinessObjects().get(0);
		// Update EMF resource
		EReference referenceId = TypeModel20Package.Literals.PLATFORM__COMPONENT_TYPES;

		String fragment = EcoreUtil.getURI(componentTypeToAdd).fragment();
		if (DiagramUtil.getEObject(parentBO, fragment) != null) {
			// the object already exist (Drag & Drop)
		} else {
			DiagramUtil.addObjectToBOResource(parentBO, referenceId, componentTypeToAdd);
		}

		IPeCreateService peCreateService = Graphiti.getPeCreateService();
		IGaService gaService = Graphiti.getGaService();
		// VISIBLE RECTANGLE INSIDE INVISIBLE RECTANGLE
		ContainerShape containerShape = peCreateService.createContainerShape(targetContainer, true);
		{
			// Create shape for invisible rectangle
			Rectangle invisible = gaService.createInvisibleRectangle(containerShape);
			gaService.setLocationAndSize(invisible, context.getX(), context.getY(), COMPONENT_TYPE_WIDTH_DEFAULT, COMPONENT_TYPE_HEIGHT_DEFAULT);
			// INTERNAL RECTANGLE
			// Create shape for rectangle
			Rectangle rectangle = gaService.createRectangle(invisible);
			rectangle.setForeground(manageColor(COMPONENT_TYPE_COLOR_FOREGROUND));
			rectangle.setBackground(manageColor(COMPONENT_TYPE_COLOR_BACKGROUND));
			rectangle.setLineWidth(2);
			gaService.setLocationAndSize(rectangle, PORT_SIZE, PORT_SIZE, COMPONENT_TYPE_WIDTH_DEFAULT - PORT_SIZE / 2, COMPONENT_TYPE_HEIGHT_DEFAULT
					- PORT_SIZE / 2);
			// Create link and wire it up
			link(containerShape, componentTypeToAdd);
		}
		// SHAPE WITH LINE
		{
			// Create shape for line
			Shape lineShape = peCreateService.createShape(containerShape, false);
			Polyline polyline = gaService.createPolyline(lineShape, new int[] { PORT_SIZE / 2,
					COMPONENT_TYPE_HEIGHT_UPPER_COMPARTMENT_DEFAULT + PORT_SIZE, COMPONENT_TYPE_WIDTH_DEFAULT - PORT_SIZE / 2,
					COMPONENT_TYPE_HEIGHT_UPPER_COMPARTMENT_DEFAULT + PORT_SIZE });
			polyline.setForeground(manageColor(COMPONENT_TYPE_COLOR_FOREGROUND));
			polyline.setLineWidth(2);
		}
		// SHAPE WITH TEXT
		{
			// Create shape for text
			Shape textShape = peCreateService.createShape(containerShape, false);
			// Create and set text graphics algorithm
			Text text = gaService.createText(textShape, componentTypeToAdd.getName());
			text.setForeground(manageColor(COMPONENT_TYPE_COLOR_TEXT_FOREGROUND));
			text.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
			text.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
			text.setFont(gaService.manageDefaultFont(getDiagram(), false, true));
			gaService.setLocationAndSize(text, PORT_SIZE / 2, PORT_SIZE / 2, COMPONENT_TYPE_WIDTH_DEFAULT - PORT_SIZE / 2,
					COMPONENT_TYPE_HEIGHT_UPPER_COMPARTMENT_DEFAULT + PORT_SIZE);
			// Create link and wire it up
			link(textShape, componentTypeToAdd);
		}
		// call the layout feature
		layoutPictogramElement(containerShape);
		// Create Chopbox anchor
		peCreateService.createChopboxAnchor(containerShape);
		return containerShape;
	}
}