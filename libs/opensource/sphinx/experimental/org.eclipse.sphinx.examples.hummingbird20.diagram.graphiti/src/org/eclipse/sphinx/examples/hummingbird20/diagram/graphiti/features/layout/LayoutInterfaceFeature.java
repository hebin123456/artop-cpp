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
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.layout;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.impl.AbstractLayoutFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;

public class LayoutInterfaceFeature extends AbstractLayoutFeature {

	public static final int MIN_HEIGHT = 80;
	public static final int MIN_WIDTH = 80;

	public LayoutInterfaceFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public boolean canLayout(ILayoutContext context) {
		// return true, if pictogram element is linked to a ComponentType
		PictogramElement pe = context.getPictogramElement();
		if (!(pe instanceof ContainerShape)) {
			return false;
		}
		EList<EObject> businessObjects = pe.getLink().getBusinessObjects();
		return businessObjects.get(0) instanceof Interface;
	}

	@Override
	public boolean layout(ILayoutContext context) {
		boolean anythingChanged = false;
		ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
		GraphicsAlgorithm containerGa = containerShape.getGraphicsAlgorithm();
		// height
		if (containerGa.getHeight() < MIN_HEIGHT) {
			containerGa.setHeight(MIN_HEIGHT);
			anythingChanged = true;
		}
		// width
		if (containerGa.getWidth() < MIN_WIDTH) {
			containerGa.setWidth(MIN_WIDTH);
			anythingChanged = true;
		}

		int containerWidth = containerGa.getWidth();

		for (Shape shape : containerShape.getChildren()) {
			GraphicsAlgorithm graphicsAlgorithm = shape.getGraphicsAlgorithm();
			IGaService gaService = Graphiti.getGaService();
			IDimension size = gaService.calculateSize(graphicsAlgorithm);
			if (containerWidth != size.getWidth()) {
				if (graphicsAlgorithm instanceof Polyline) {
					Polyline polyline = (Polyline) graphicsAlgorithm;
					Point secondPoint = polyline.getPoints().get(1);
					Point newSecondPoint = gaService.createPoint(containerWidth, secondPoint.getY());
					polyline.getPoints().set(1, newSecondPoint);
					anythingChanged = true;
				} else {
					gaService.setWidth(graphicsAlgorithm, containerWidth);
					anythingChanged = true;
				}
			}
		}
		return anythingChanged;
	}
}