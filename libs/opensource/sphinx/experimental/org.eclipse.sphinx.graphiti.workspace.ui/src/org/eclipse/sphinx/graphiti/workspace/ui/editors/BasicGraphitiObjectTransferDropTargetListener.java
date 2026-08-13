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
package org.eclipse.sphinx.graphiti.workspace.ui.editors;

import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.internal.dnd.ObjectsTransferDropTargetListener;
import org.eclipse.graphiti.ui.platform.IConfigurationProvider;
import org.eclipse.jface.viewers.ISelection;

@SuppressWarnings("restriction")
public class BasicGraphitiObjectTransferDropTargetListener extends ObjectsTransferDropTargetListener {

	protected static final Dimension UNSPECIFIED_SIZE = new Dimension();
	private static final Dimension PREFERRED_SIZE = new Dimension(-1, -1);
	private DiagramEditor diagramEditor;
	private IConfigurationProvider configurationProvider;

	public BasicGraphitiObjectTransferDropTargetListener(EditPartViewer viewer, DiagramEditor diagramEditor,
			IConfigurationProvider configurationProvider) {
		super(viewer);
		this.diagramEditor = diagramEditor;
		this.configurationProvider = configurationProvider;
	}

	@Override
	protected Command getCommand() {
		BasicGraphitiAddModelObjectCommand addModelObjectCmd = null;
		Request request = getTargetRequest();
		if (request instanceof CreateRequest) {
			CreateRequest createRequest = (CreateRequest) request;
			return getCreateCommand(createRequest);
		}
		return addModelObjectCmd;
	}

	protected Command getCreateCommand(CreateRequest request) {
		Command cmd = UnexecutableCommand.INSTANCE;
		// Retrieve the parent object from the diagram input
		EObject parentObject = (EObject) diagramEditor.getAdapter(Diagram.class);
		if (!(parentObject instanceof ContainerShape) || parentObject == null) {
			return cmd;
		}

		// Calculate parent object
		Object createdObject = request.getNewObject();

		// determine constraint
		org.eclipse.draw2d.geometry.Rectangle rectangle = null;
		if (request.getLocation() != null) {
			rectangle = getConstraintFor(request);
		}

		if (request.getNewObjectType() == ISelection.class) {
			cmd = new BasicGraphitiAddModelObjectCommand(configurationProvider, (ContainerShape) parentObject, (ISelection) createdObject, rectangle);
			// cmd = new AddModelObjectCommand(configurationProvider, (ContainerShape) parentObject, (ISelection)
			// createdObject, rectangle);
		}
		return cmd;
	}

	private org.eclipse.draw2d.geometry.Rectangle getConstraintFor(CreateRequest request) {
		PrecisionRectangle locationAndSize = null;
		if (request.getSize() == null || request.getSize().isEmpty()) {
			locationAndSize = new PrecisionRectangle(request.getLocation(), UNSPECIFIED_SIZE);
		} else {
			locationAndSize = new PrecisionRectangle(request.getLocation(), request.getSize());
		}

		IFigure figure = getLayoutContainer();
		figure.translateToRelative(locationAndSize);
		figure.translateFromParent(locationAndSize);
		Point negatedLayoutOrigin = getLayoutOrigin().getNegated();
		locationAndSize.performTranslate(negatedLayoutOrigin.x, negatedLayoutOrigin.y);
		return getConstraintFor(request, null, locationAndSize);
	}

	private org.eclipse.draw2d.geometry.Rectangle getConstraintFor(Request request, GraphicalEditPart child,
			org.eclipse.draw2d.geometry.Rectangle rectangle) {
		if (UNSPECIFIED_SIZE.equals(rectangle.getSize())) {
			return getConstraintFor(rectangle.getLocation());
		}
		// return getConstraintFor(rectangle);
		return null;
	}

	private org.eclipse.draw2d.geometry.Rectangle getConstraintFor(Point p) {
		return new org.eclipse.draw2d.geometry.Rectangle(p, PREFERRED_SIZE);
	}

	private Point getLayoutOrigin() {
		return getLayoutContainer().getClientArea().getLocation();
	}

	private IFigure getLayoutContainer() {
		Diagram diagram = (Diagram) diagramEditor.getAdapter(Diagram.class);
		Map<?, ?> editPartRegistry = diagramEditor.getGraphicalViewer().getEditPartRegistry();
		if (editPartRegistry != null) {
			Object obj = editPartRegistry.get(diagram);
			if (obj instanceof GraphicalEditPart) {
				GraphicalEditPart ep = (GraphicalEditPart) obj;
				return ep.getContentPane();
			}
		}
		return null;
	}

}
