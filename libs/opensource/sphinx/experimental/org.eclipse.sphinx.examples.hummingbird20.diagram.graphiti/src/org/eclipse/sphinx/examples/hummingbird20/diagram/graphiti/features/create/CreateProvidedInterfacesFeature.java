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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.providers.Hummingbird20PlatformDiagramImageProvider;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

public class CreateProvidedInterfacesFeature extends AbstractCreateConnectionFeature {

	public static String CREATE_CONNECTION_NAME = "Provided Interfaces"; //$NON-NLS-1$
	public static String CREATE_CONNECTION_DESCRIPTION = "Create Provided Interfaces"; //$NON-NLS-1$

	public CreateProvidedInterfacesFeature(IFeatureProvider fp) {
		super(fp, CREATE_CONNECTION_NAME, CREATE_CONNECTION_DESCRIPTION);
	}

	@Override
	public String getCreateImageId() {
		return Hummingbird20PlatformDiagramImageProvider.IMAGE_PROVIDED_INTERFACES;
	}

	@Override
	public boolean canCreate(ICreateConnectionContext context) {
		Object source = null, target = null;
		Anchor sourceAnchor = context.getSourceAnchor();
		if (sourceAnchor != null) {
			source = getBusinessObjectForPictogramElement(sourceAnchor.getParent());
		}
		Anchor targetAnchor = context.getTargetAnchor();
		if (targetAnchor != null) {
			target = getBusinessObjectForPictogramElement(targetAnchor.getParent());
		}
		if (source != null && target != null && source != target) {
			if (source instanceof ComponentType && target instanceof Interface) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Connection create(ICreateConnectionContext context) {
		Connection newConnection = null;
		// get ComponentType which should be connected
		Anchor sourceAnchor = context.getSourceAnchor();
		ComponentType source = (ComponentType) getBusinessObjectForPictogramElement(sourceAnchor.getParent());
		// get Interface which should be connected
		Anchor targetAnchor = context.getTargetAnchor();
		Interface target = (Interface) getBusinessObjectForPictogramElement(targetAnchor.getParent());

		if (source != null && target != null) {
			// get new business object
			ComponentType eReference = createReference(source, target);
			// add connection for business object
			AddConnectionContext addContext = new AddConnectionContext(context.getSourceAnchor(), context.getTargetAnchor());
			addContext.setNewObject(eReference);
			// Add property to the context so that the Feature provider knows that it is a reference
			EReference referenceId = TypeModel20Package.Literals.COMPONENT_TYPE__PROVIDED_INTERFACES;
			addContext.putProperty(referenceId.getName(), referenceId);
			newConnection = (Connection) getFeatureProvider().addIfPossible(addContext);
		}
		return newConnection;
	}

	@Override
	public boolean canStartConnection(ICreateConnectionContext context) {
		// return true if start anchor belongs to a ComponentType
		if (context.getSourceAnchor() != null && context.getSourceAnchor().getLink() == null && context.getSourceAnchor().eContainer() != null) {
			ContainerShape parent = (ContainerShape) context.getSourceAnchor().eContainer();
			EObject eObject = parent.getLink().getBusinessObjects().get(0);
			if (eObject instanceof ComponentType) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates a EReference between a ComponentType and an Interface.
	 */
	private ComponentType createReference(ComponentType source, Interface target) {
		source.getProvidedInterfaces().add(target);
		return source;
	}
}