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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
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
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;
import org.eclipse.sphinx.graphiti.workspace.ui.util.DiagramUtil;

public class AddPortFeature extends AbstractAddShapeFeature {

	public static final int PORT_SIZE = 10;
	public static final IColorConstant PORT_COLOR_TEXT_FOREGROUND = new ColorConstant(0, 0, 0);
	public static final IColorConstant PORT_RECTANGLE_COLOR_FOREGROUND = new ColorConstant(98, 131, 167);
	public static final IColorConstant PORT_RECTANGLE_COLOR_BACKGROUND = new ColorConstant(98, 14, 165);
	public static final int PORT_CONTAINER_WIDTH_DEFAULT = 60;
	public static final int PORT_CONTAINER_HEIGTH_DEFAULT = 20;

	public AddPortFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public boolean canAdd(IAddContext context) {
		// Is it an add request for a Port ?
		if (context.getNewObject() instanceof Port) {
			// Is it an add request for adding the a Port to a diagram?
			ContainerShape container = context.getTargetContainer();
			PictogramLink link = container.getLink();
			EList<EObject> bo = link.getBusinessObjects();
			if (bo.get(0) instanceof ComponentType) {
				return true;
			}

			// Added to handle extended Drag & Drop features
			if (context.getProperty("container") instanceof ComponentType) {
				return true;
			}
		}
		return false;
	}

	@Override
	public PictogramElement add(IAddContext context) {
		Port portToAdd = (Port) context.getNewObject();
		ContainerShape parentContainer = context.getTargetContainer();
		PictogramLink link = parentContainer.getLink();

		EObject parentBO = null;
		PictogramElement newElement = null;
		// Does the context come from Drag & Drop ? see: BasicGraphitiObjectTransferDropTargetListener
		if (context.getProperty("container") != null) { //$NON-NLS-1$
			parentBO = (EObject) context.getProperty("container"); //$NON-NLS-1$
			String fragment = EcoreUtil.getURI(portToAdd).fragment();
			if (DiagramUtil.getEObject(parentBO, fragment) != null) {
				// the object already exist (Drag & Drop)
				EList<Shape> children = context.getTargetContainer().getChildren();
				for (Shape candidateShape : children) {
					if (candidateShape.getLink().getBusinessObjects().get(0) != null) {
						EObject candidateObject = candidateShape.getLink().getBusinessObjects().get(0);
						if (EcoreUtil.equals(candidateObject, parentBO)) {
							// Get eObject2 shape
							newElement = createBoundPort((ContainerShape) candidateShape, 0, 0);
							link(newElement, context.getNewObject());
							break;
						}
					}
				}
			}
		} else {
			// Get the parent Business Object
			parentBO = link.getBusinessObjects().get(0);
			// Get the reference Id
			EReference referenceId = TypeModel20Package.Literals.COMPONENT_TYPE__PORTS;
			// Add object to BO resource
			DiagramUtil.addObjectToBOResource(parentBO, referenceId, portToAdd);
			if (!(context.getTargetContainer() instanceof Diagram)) {
				newElement = createBoundPort(context.getTargetContainer(), context.getX(), context.getY());
				link(newElement, context.getNewObject());
			}
		}

		return newElement;
	}

	/**
	 * Create a port that is bound to an entity's boundary.
	 * 
	 * @param container
	 *            the container shape of the parent entity
	 * @param xpos
	 *            the x position
	 * @param ypos
	 *            the y position
	 * @return a new pictogram element for the port
	 */
	private PictogramElement createBoundPort(final ContainerShape container, final int xpos, final int ypos) {
		int nodeWidth = container.getGraphicsAlgorithm().getWidth();
		int nodeHeight = container.getGraphicsAlgorithm().getHeight();
		float widthPercent = (float) (xpos - 2) / nodeWidth;
		float heightPercent = (float) (ypos - 2) / nodeHeight;
		float deltaY = heightPercent < 1.0f / 2.0f ? heightPercent : 1 - heightPercent;
		float deltaX = widthPercent < 1.0f / 2.0f ? widthPercent : 1 - widthPercent;
		if (deltaY < deltaX) {
			heightPercent = Math.round(heightPercent);
		} else {
			widthPercent = Math.round(widthPercent);
		}
		IPeCreateService peCreateService = Graphiti.getPeCreateService();
		BoxRelativeAnchor boxAnchor = peCreateService.createBoxRelativeAnchor(container);
		boxAnchor.setRelativeWidth(widthPercent);
		boxAnchor.setRelativeHeight(heightPercent);
		boxAnchor.setActive(true);

		IGaService gaService = Graphiti.getGaService();
		// look for the actual rectangle that represents the parent entity
		for (GraphicsAlgorithm ga : container.getGraphicsAlgorithm().getGraphicsAlgorithmChildren()) {
			if (ga instanceof Rectangle) {
				boxAnchor.setReferencedGraphicsAlgorithm(ga);
				break;
			}
		}
		Rectangle rectangleShape = gaService.createRectangle(boxAnchor);
		gaService.setLocationAndSize(rectangleShape, -PORT_SIZE / 2, -PORT_SIZE / 2, PORT_SIZE, PORT_SIZE);
		return boxAnchor;
	}

	/**
	 * Create a port that is bound to an entity's boundary.
	 * 
	 * @param container
	 *            the container shape of the parent entity
	 * @param xpos
	 *            the x position
	 * @param ypos
	 *            the y position
	 * @return a new pictogram element for the port
	 */
	private PictogramElement createBoundPortWithLabel(final ContainerShape container, final int xpos, final int ypos) {
		// TODO : validate this method
		int nodeWidth = container.getGraphicsAlgorithm().getWidth();
		int nodeHeight = container.getGraphicsAlgorithm().getHeight();

		float widthPercent = (float) (xpos - 2) / nodeWidth;
		float heightPercent = (float) (ypos - 2) / nodeHeight;
		float deltaY = heightPercent < 1.0f / 2.0f ? heightPercent : 1 - heightPercent;
		float deltaX = widthPercent < 1.0f / 2.0f ? widthPercent : 1 - widthPercent;
		if (deltaY < deltaX) {
			heightPercent = Math.round(heightPercent);
		} else {
			widthPercent = Math.round(widthPercent);
		}
		IPeCreateService peCreateService = Graphiti.getPeCreateService();
		BoxRelativeAnchor boxAnchor = peCreateService.createBoxRelativeAnchor(container);
		boxAnchor.setRelativeWidth(widthPercent);
		boxAnchor.setRelativeHeight(heightPercent);
		boxAnchor.setActive(true);

		IGaService gaService = Graphiti.getGaService();
		// look for the actual rectangle that represents the parent entity
		for (GraphicsAlgorithm ga : container.getGraphicsAlgorithm().getGraphicsAlgorithmChildren()) {
			if (ga instanceof Rectangle) {
				boxAnchor.setReferencedGraphicsAlgorithm(ga);
				break;
			}
		}
		// INVISIBLE RECTANGLE
		Rectangle invisibleRectangle = gaService.createInvisibleRectangle(boxAnchor);
		gaService.setLocationAndSize(invisibleRectangle, -PORT_SIZE / 2, -PORT_SIZE / 2, 6 * PORT_SIZE, PORT_SIZE);
		// INTERNAL RECTANGLE
		Rectangle internalRectangle = gaService.createRectangle(invisibleRectangle);
		internalRectangle.setLineWidth(2);
		gaService.setLocationAndSize(internalRectangle, 0, 0, PORT_SIZE, PORT_SIZE);
		// TEXT SHAPE
		Text text = gaService.createText(invisibleRectangle, "IN#"); //$NON-NLS-1$
		gaService.setLocationAndSize(text, 2 * PORT_SIZE, 0, 5 * PORT_SIZE, PORT_SIZE);
		text.setHorizontalAlignment(Orientation.ALIGNMENT_LEFT);
		text.setFont(gaService.manageDefaultFont(getDiagram(), false, true));
		return boxAnchor;
	}

}