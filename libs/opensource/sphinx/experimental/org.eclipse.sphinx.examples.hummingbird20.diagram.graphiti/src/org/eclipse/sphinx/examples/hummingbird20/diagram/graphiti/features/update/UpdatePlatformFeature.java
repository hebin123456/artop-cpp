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
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.update;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.context.impl.RemoveContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.graphiti.workspace.ui.util.DiagramUtil;

public class UpdatePlatformFeature extends AbstractUpdateFeature {

	public UpdatePlatformFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public boolean canUpdate(IUpdateContext context) {
		// return true, if linked business object is a ComponentType
		Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
		return bo instanceof Platform;
	}

	@Override
	public IReason updateNeeded(IUpdateContext context) {
		ContainerShape container = (ContainerShape) context.getPictogramElement();
		EObject boRoot = (EObject) getBusinessObjectForPictogramElement(container);
		for (Shape shape : container.getChildren()) {
			Object bo = getBusinessObjectForPictogramElement(shape);
			String fragment = EcoreUtil.getURI((EObject) bo).fragment();
			if (DiagramUtil.getEObject(boRoot, fragment) == null) {
				return Reason.createTrueReason("Update needed on Platform!"); //$NON-NLS-1$
			}
		}
		return Reason.createFalseReason("Update is not needed on Platform"); //$NON-NLS-1$
	}

	@Override
	public boolean update(IUpdateContext context) {
		List<Shape> shapesToRemove = new ArrayList<Shape>();
		ContainerShape container = (ContainerShape) context.getPictogramElement();
		EObject boRoot = (EObject) getBusinessObjectForPictogramElement(container);
		for (Shape shape : container.getChildren()) {
			Object bo = getBusinessObjectForPictogramElement(shape);
			// Lookup object in BO model, if object deos not exist, add it to list
			String fragment = EcoreUtil.getURI((EObject) bo).fragment();
			if (DiagramUtil.getEObject(boRoot, fragment) == null) {
				shapesToRemove.add(shape);
			}
		}
		// Process list to remove shapes accordingly
		for (Shape shape : shapesToRemove) {
			// Object has been deleted
			RemoveContext removeContext = new RemoveContext(shape);
			IFeatureProvider featureProvider = getFeatureProvider();
			IRemoveFeature removeFeature = featureProvider.getRemoveFeature(removeContext);
			if (removeFeature != null) {
				removeFeature.remove(removeContext);
			} else {
				return false;
			}
		}
		return true;
	}

}