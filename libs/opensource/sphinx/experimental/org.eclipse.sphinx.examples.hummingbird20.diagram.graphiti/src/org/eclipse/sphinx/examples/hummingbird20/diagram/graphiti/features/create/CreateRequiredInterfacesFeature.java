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

import org.eclipse.emf.ecore.EReference;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.providers.Hummingbird20PlatformDiagramImageProvider;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

public class CreateRequiredInterfacesFeature extends AbstractCreateConnectionFeature {

	public static String CREATE_CONNECTION_NAME = "Required Interfaces"; //$NON-NLS-1$
	public static String CREATE_CONNECTION_DESCRIPTION = "Create Required Interfaces"; //$NON-NLS-1$

	public CreateRequiredInterfacesFeature(IFeatureProvider fp) {
		super(fp, CREATE_CONNECTION_NAME, CREATE_CONNECTION_DESCRIPTION);
	}

	@Override
	public String getCreateImageId() {
		return Hummingbird20PlatformDiagramImageProvider.IMAGE_REQUIRED_INTERFACES;
	}

	@Override
	public boolean canCreate(ICreateConnectionContext context) {
		// return true if source anchor belongs to an ComponentType and target anchor to Interface
		Port source = getPort(context.getSourceAnchor());
		Interface target = getInterface(context.getTargetAnchor());
		if (source != null && target != null && source != target) {
			return true;
		}
		return false;

	}

	@Override
	public Connection create(ICreateConnectionContext context) {
		Connection newConnection = null;
		// get Port which should be connected
		Port source = getPort(context.getSourceAnchor());
		// get Interface which should be connected
		Interface target = getInterface(context.getTargetAnchor());
		if (source != null && target != null) {
			// get new business object
			Port eReference = createReference(source, target);
			// add connection for business object
			AddConnectionContext addContext = new AddConnectionContext(context.getSourceAnchor(), context.getTargetAnchor());
			addContext.setNewObject(eReference);
			// Add property to the context so that the Feature provider knows that it is a reference
			EReference referenceId = TypeModel20Package.Literals.PORT__REQUIRED_INTERFACE;
			addContext.putProperty(referenceId.getName(), referenceId);
			newConnection = (Connection) getFeatureProvider().addIfPossible(addContext);
		}
		return newConnection;
	}

	@Override
	public boolean canStartConnection(ICreateConnectionContext context) {
		// return true if start anchor belongs to a ComponentType
		if (getPort(context.getSourceAnchor()) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the ComponentType belonging to the anchor, or null if not available.
	 */
	private Port getPort(Anchor anchor) {
		if (anchor != null && anchor.getLink() != null) {
			// Object object = getBusinessObjectForPictogramElement(anchor.getParent());
			Object object = anchor.getLink().getBusinessObjects().get(0);
			if (object != null) {
				if (object instanceof Port) {
					return (Port) object;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the Interface belonging to the anchor, or null if not available.
	 */
	private Interface getInterface(Anchor anchor) {
		if (anchor != null) {
			Object object = getBusinessObjectForPictogramElement(anchor.getParent());
			if (object instanceof Interface) {
				return (Interface) object;
			}
		}
		return null;
	}

	/**
	 * Creates a EReference between a ComponentType and an Interface.
	 */
	private Port createReference(Port source, Interface target) {
		source.setRequiredInterface(target);
		return source;
	}
}