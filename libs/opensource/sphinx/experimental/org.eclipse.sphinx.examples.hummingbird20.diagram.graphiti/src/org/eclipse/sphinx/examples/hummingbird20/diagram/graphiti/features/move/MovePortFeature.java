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
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.move;

import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IMoveAnchorContext;
import org.eclipse.graphiti.features.impl.DefaultMoveAnchorFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.services.Graphiti;

public class MovePortFeature extends DefaultMoveAnchorFeature {

	public MovePortFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public boolean canMoveAnchor(IMoveAnchorContext context) {
		return super.canMoveAnchor(context);
	}

	@Override
	protected void moveAnchor(final Anchor anchor, final int posX, final int posY) {
		if (anchor instanceof BoxRelativeAnchor) {
			BoxRelativeAnchor boxAnchor = (BoxRelativeAnchor) anchor;
			GraphicsAlgorithm anchorContainerGa = boxAnchor.getParent().getGraphicsAlgorithm();
			anchorContainerGa = anchorContainerGa.getGraphicsAlgorithmChildren().get(0);
			IDimension nodeSize = Graphiti.getGaService().calculateSize(anchorContainerGa, false);

			int nodeWidth = nodeSize.getWidth();
			int nodeHeight = nodeSize.getHeight();

			float widthPercent = (float) (posX - 2) / nodeWidth;
			float heightPercent = (float) (posY - 2) / nodeHeight;
			float deltaY = heightPercent < 1.0f / 2.0f ? heightPercent : 1 - heightPercent;
			float deltaX = widthPercent < 1.0f / 2.0f ? widthPercent : 1 - widthPercent;
			if (deltaY < deltaX) {
				heightPercent = Math.round(heightPercent);
			} else {
				widthPercent = Math.round(widthPercent);
			}

			boxAnchor.setRelativeWidth(widthPercent);
			boxAnchor.setRelativeHeight(heightPercent);

			boxAnchor.setActive(true);
		} else {
			super.moveAnchor(anchor, posX, posY);
		}
	}
}