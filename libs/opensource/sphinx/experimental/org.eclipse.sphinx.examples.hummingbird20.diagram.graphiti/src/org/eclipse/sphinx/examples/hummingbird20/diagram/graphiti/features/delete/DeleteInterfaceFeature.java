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
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.delete;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.graphiti.workspace.ui.util.DiagramUtil;

public class DeleteInterfaceFeature extends DefaultDeleteFeature {

	public DeleteInterfaceFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public void delete(IDeleteContext context) {
		PictogramElement pictogramElement = context.getPictogramElement();
		EObject eObject = pictogramElement.getLink().getBusinessObjects().get(0);
		if (eObject instanceof Interface) {
			Interface interfaceToRemove = (Interface) eObject;
			// Remove the link from the EMF resource
			DiagramUtil.deleteObjectFromBOResource(interfaceToRemove);
			// Delete object from Diagram
			super.delete(context);
		}
	}
}