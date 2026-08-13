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
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.providers;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IDeleteFeature;
import org.eclipse.graphiti.features.ILayoutFeature;
import org.eclipse.graphiti.features.IMoveAnchorFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.context.IMoveAnchorContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.features.impl.AbstractFeatureProvider;
import org.eclipse.graphiti.internal.util.T;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.platform.IDiagramEditor;
import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.add.AddComponentTypeFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.add.AddInterfaceFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.add.AddPortFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.add.AddProvidedInterfacesFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.add.AddRequiredInterfacesFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.create.CreateComponentTypeFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.create.CreateInterfaceFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.create.CreatePortFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.create.CreateProvidedInterfacesFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.create.CreateRequiredInterfacesFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.custom.RenameComponentTypeFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.custom.RenameInterfaceFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.delete.DeleteComponentTypeFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.delete.DeleteInterfaceFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.delete.DeletePortFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.delete.DeleteProvidedInterfacesFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.delete.DeleteRequiredInterfacesFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.layout.LayoutComponentTypeFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.layout.LayoutInterfaceFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.layout.LayoutPortFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.move.MovePortFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.update.UpdateComponentTypeFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.update.UpdateInterfaceFeature;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.update.UpdatePlatformFeature;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;

/**
 * Feature Provider of Hummingbird20
 */
@SuppressWarnings("restriction")
public class Hummingbird20PlatformDiagramFeatureProvider extends DefaultFeatureProvider {

	public Hummingbird20PlatformDiagramFeatureProvider(IDiagramTypeProvider dtp) {
		super(dtp);
	}

	@Override
	public IAddFeature getAddFeature(IAddContext context) {
		// Is object for add request a ComponentType?
		if (context.getNewObject() instanceof ComponentType) {
			// Its it a new connection ?
			if (context.getProperty("providedInterfaces") != null) { //$NON-NLS-1$
				return new AddProvidedInterfacesFeature(this);
			}
			// Is it a new ComponentType ?
			return new AddComponentTypeFeature(this);
			// Is it a new Port ?
		} else if (context.getNewObject() instanceof Port) {
			if (context.getProperty("requiredInterface") != null) { //$NON-NLS-1$
				return new AddRequiredInterfacesFeature(this);
			}
			return new AddPortFeature(this);
		}
		// Is object for add request a ComponentType?
		else if (context.getNewObject() instanceof Interface && context.getProperty("sourceAnchor") == null) { //$NON-NLS-1$
			// property sourceAnchor is not null when object creation is requested by drag & drop feature
			return new AddInterfaceFeature(this);
		}
		return super.getAddFeature(context);
	}

	@Override
	public ICreateFeature[] getCreateFeatures() {
		return new ICreateFeature[] { new CreateComponentTypeFeature(this), new CreateInterfaceFeature(this), new CreatePortFeature(this) };
	}

	@Override
	public ICreateConnectionFeature[] getCreateConnectionFeatures() {
		return new ICreateConnectionFeature[] { new CreateProvidedInterfacesFeature(this), new CreateRequiredInterfacesFeature(this) };
	}

	@Override
	public IUpdateFeature getUpdateFeature(IUpdateContext context) {
		PictogramElement pictogramElement = context.getPictogramElement();
		if (pictogramElement instanceof ContainerShape) {
			Object bo = getBusinessObjectForPictogramElement(pictogramElement);
			if (bo instanceof ComponentType) {
				return new UpdateComponentTypeFeature(this);
			}
			if (bo instanceof Interface) {
				return new UpdateInterfaceFeature(this);
			}
			if (bo instanceof Platform) {
				return new UpdatePlatformFeature(this);
			}
		}
		return super.getUpdateFeature(context);
	}

	@Override
	public IDeleteFeature getDeleteFeature(IDeleteContext context) {
		PictogramElement pictogramElement = context.getPictogramElement();
		Object bo = getBusinessObjectForPictogramElement(pictogramElement);
		if (pictogramElement instanceof ContainerShape) {
			if (bo instanceof ComponentType) {
				return new DeleteComponentTypeFeature(this);
			}
			if (bo instanceof Interface) {
				return new DeleteInterfaceFeature(this);
			}
		}
		if (pictogramElement instanceof Connection) {
			// Distinguish between requiredInterfaces and providedInterfaces connections
			if (pictogramElement.getLink().getBusinessObjects().get(0) instanceof Port) {
				return new DeleteRequiredInterfacesFeature(this);
			}
			return new DeleteProvidedInterfacesFeature(this);
		}
		if (pictogramElement instanceof BoxRelativeAnchor) {
			if (bo instanceof Port) {
				return new DeletePortFeature(this);
			}
		}
		return super.getDeleteFeature(context);
	}

	@Override
	public ILayoutFeature getLayoutFeature(ILayoutContext context) {
		PictogramElement pictogramElement = context.getPictogramElement();
		Object bo = getBusinessObjectForPictogramElement(pictogramElement);
		if (bo instanceof ComponentType) {
			return new LayoutComponentTypeFeature(this);
		}
		if (bo instanceof Interface) {
			return new LayoutInterfaceFeature(this);
		} else if (bo instanceof Port) {
			return new LayoutPortFeature(this);
		}
		return super.getLayoutFeature(context);
	}

	@Override
	public IMoveAnchorFeature getMoveAnchorFeature(IMoveAnchorContext context) {
		if (getBusinessObjectForPictogramElement(context.getAnchor()) instanceof Port) {
			return new MovePortFeature(this);
		}
		return super.getMoveAnchorFeature(context);
	}

	@Override
	public ICustomFeature[] getCustomFeatures(ICustomContext context) {
		return new ICustomFeature[] { new RenameComponentTypeFeature(this), new RenameInterfaceFeature(this) };
	}

	@Override
	public PictogramElement addIfPossible(IAddContext context) {
		final String SIGNATURE = "addIfPossible(IAddContext)"; //$NON-NLS-1$
		boolean info = T.racer().info();
		if (info) {
			T.racer().entering(AbstractFeatureProvider.class, SIGNATURE, new Object[] { context });
		}
		PictogramElement returnValue = null;
		if (canAdd(context).toBoolean()) {
			IAddFeature feature = getAddFeature(context);
			IDiagramEditor diagramEditor = getDiagramTypeProvider().getDiagramEditor();
			try {
				Object result = diagramEditor.executeFeature(feature, context);
				if (result instanceof PictogramElement) {
					returnValue = (PictogramElement) result;
				}
			} catch (Exception e) {
				// Wrap in RuintimeException (handled by all callers)
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				} else {
					throw new RuntimeException(e);
				}
			}
		}
		if (info) {
			T.racer().exiting(AbstractFeatureProvider.class, SIGNATURE, returnValue);
		}
		return returnValue;
	}

}