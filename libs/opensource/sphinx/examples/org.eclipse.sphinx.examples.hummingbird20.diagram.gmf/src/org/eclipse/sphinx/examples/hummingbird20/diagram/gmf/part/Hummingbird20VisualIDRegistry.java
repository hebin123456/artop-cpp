/**
 * <copyright>
 * 
 * Copyright (c) 2013 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part;

import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ApplicationEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ComponentEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ComponentNameEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ConnectionEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ConnectionLabelEditPart;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;

/**
 * This registry is used to determine which type of visual object should be created for the corresponding Diagram, Node,
 * ChildNode or Link represented by a domain model object.
 * 
 * @generated
 */
public class Hummingbird20VisualIDRegistry {

	/**
	 * @generated
	 */
	private static final String DEBUG_KEY = "org.eclipse.sphinx.examples.hummingbird20.diagram/debug/visualID"; //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static int getVisualID(View view) {
		if (view instanceof Diagram) {
			if (ApplicationEditPart.MODEL_ID.equals(view.getType())) {
				return ApplicationEditPart.VISUAL_ID;
			} else {
				return -1;
			}
		}
		return org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20VisualIDRegistry.getVisualID(view.getType());
	}

	/**
	 * @generated
	 */
	public static String getModelID(View view) {
		View diagram = view.getDiagram();
		while (view != diagram) {
			EAnnotation annotation = view.getEAnnotation("Shortcut"); //$NON-NLS-1$
			if (annotation != null) {
				return annotation.getDetails().get("modelID"); //$NON-NLS-1$
			}
			view = (View) view.eContainer();
		}
		return diagram != null ? diagram.getType() : null;
	}

	/**
	 * @generated
	 */
	public static int getVisualID(String type) {
		try {
			return Integer.parseInt(type);
		} catch (NumberFormatException e) {
			if (Boolean.TRUE.toString().equalsIgnoreCase(Platform.getDebugOption(DEBUG_KEY))) {
				Hummingbird20DiagramEditorPlugin.getInstance().logError("Unable to parse view type as a visualID number: " + type);
			}
		}
		return -1;
	}

	/**
	 * @generated
	 */
	public static String getType(int visualID) {
		return Integer.toString(visualID);
	}

	/**
	 * @generated
	 */
	public static int getDiagramVisualID(EObject domainElement) {
		if (domainElement == null) {
			return -1;
		}
		if (InstanceModel20Package.eINSTANCE.getApplication().isSuperTypeOf(domainElement.eClass()) && isDiagram((Application) domainElement)) {
			return ApplicationEditPart.VISUAL_ID;
		}
		return -1;
	}

	/**
	 * @generated
	 */
	public static int getNodeVisualID(View containerView, EObject domainElement) {
		if (domainElement == null) {
			return -1;
		}
		String containerModelID = org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20VisualIDRegistry.getModelID(containerView);
		if (!ApplicationEditPart.MODEL_ID.equals(containerModelID)) {
			return -1;
		}
		int containerVisualID;
		if (ApplicationEditPart.MODEL_ID.equals(containerModelID)) {
			containerVisualID = org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20VisualIDRegistry.getVisualID(containerView);
		} else {
			if (containerView instanceof Diagram) {
				containerVisualID = ApplicationEditPart.VISUAL_ID;
			} else {
				return -1;
			}
		}
		switch (containerVisualID) {
		case ApplicationEditPart.VISUAL_ID:
			if (InstanceModel20Package.eINSTANCE.getComponent().isSuperTypeOf(domainElement.eClass())) {
				return ComponentEditPart.VISUAL_ID;
			}
			break;
		}
		return -1;
	}

	/**
	 * @generated
	 */
	public static boolean canCreateNode(View containerView, int nodeVisualID) {
		String containerModelID = org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20VisualIDRegistry.getModelID(containerView);
		if (!ApplicationEditPart.MODEL_ID.equals(containerModelID)) {
			return false;
		}
		int containerVisualID;
		if (ApplicationEditPart.MODEL_ID.equals(containerModelID)) {
			containerVisualID = org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20VisualIDRegistry.getVisualID(containerView);
		} else {
			if (containerView instanceof Diagram) {
				containerVisualID = ApplicationEditPart.VISUAL_ID;
			} else {
				return false;
			}
		}
		switch (containerVisualID) {
		case ApplicationEditPart.VISUAL_ID:
			if (ComponentEditPart.VISUAL_ID == nodeVisualID) {
				return true;
			}
			break;
		case ComponentEditPart.VISUAL_ID:
			if (ComponentNameEditPart.VISUAL_ID == nodeVisualID) {
				return true;
			}
			break;
		case ConnectionEditPart.VISUAL_ID:
			if (ConnectionLabelEditPart.VISUAL_ID == nodeVisualID) {
				return true;
			}
			break;
		}
		return false;
	}

	/**
	 * @generated
	 */
	public static int getLinkWithClassVisualID(EObject domainElement) {
		if (domainElement == null) {
			return -1;
		}
		if (InstanceModel20Package.eINSTANCE.getConnection().isSuperTypeOf(domainElement.eClass())) {
			return ConnectionEditPart.VISUAL_ID;
		}
		return -1;
	}

	/**
	 * User can change implementation of this method to handle some specific situations not covered by default logic.
	 * 
	 * @generated
	 */
	private static boolean isDiagram(Application element) {
		return true;
	}

}
