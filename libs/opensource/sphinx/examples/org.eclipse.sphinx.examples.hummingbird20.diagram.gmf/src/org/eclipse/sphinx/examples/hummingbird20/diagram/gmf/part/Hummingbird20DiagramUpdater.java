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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ApplicationEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ComponentEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ConnectionEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.providers.Hummingbird20ElementTypes;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;

/**
 * @generated
 */
public class Hummingbird20DiagramUpdater {

	/**
	 * @generated
	 */
	public static List<Hummingbird20NodeDescriptor> getSemanticChildren(View view) {
		switch (Hummingbird20VisualIDRegistry.getVisualID(view)) {
		case ApplicationEditPart.VISUAL_ID:
			return getApplication_1000SemanticChildren(view);
		}
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public static List<Hummingbird20NodeDescriptor> getApplication_1000SemanticChildren(View view) {
		if (!view.isSetElement()) {
			return Collections.emptyList();
		}
		Application modelElement = (Application) view.getElement();
		LinkedList<Hummingbird20NodeDescriptor> result = new LinkedList<Hummingbird20NodeDescriptor>();
		for (Object name : modelElement.getComponents()) {
			Component childElement = (Component) name;
			int visualID = Hummingbird20VisualIDRegistry.getNodeVisualID(view, childElement);
			if (visualID == ComponentEditPart.VISUAL_ID) {
				result.add(new Hummingbird20NodeDescriptor(childElement, visualID));
				continue;
			}
		}
		return result;
	}

	/**
	 * @generated
	 */
	public static List<Hummingbird20LinkDescriptor> getContainedLinks(View view) {
		switch (Hummingbird20VisualIDRegistry.getVisualID(view)) {
		case ApplicationEditPart.VISUAL_ID:
			return getApplication_1000ContainedLinks(view);
		case ComponentEditPart.VISUAL_ID:
			return getComponent_2001ContainedLinks(view);
		case ConnectionEditPart.VISUAL_ID:
			return getConnection_4001ContainedLinks(view);
		}
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public static List<Hummingbird20LinkDescriptor> getIncomingLinks(View view) {
		switch (Hummingbird20VisualIDRegistry.getVisualID(view)) {
		case ComponentEditPart.VISUAL_ID:
			return getComponent_2001IncomingLinks(view);
		case ConnectionEditPart.VISUAL_ID:
			return getConnection_4001IncomingLinks(view);
		}
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public static List<Hummingbird20LinkDescriptor> getOutgoingLinks(View view) {
		switch (Hummingbird20VisualIDRegistry.getVisualID(view)) {
		case ComponentEditPart.VISUAL_ID:
			return getComponent_2001OutgoingLinks(view);
		case ConnectionEditPart.VISUAL_ID:
			return getConnection_4001OutgoingLinks(view);
		}
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public static List<Hummingbird20LinkDescriptor> getApplication_1000ContainedLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public static List<Hummingbird20LinkDescriptor> getComponent_2001ContainedLinks(View view) {
		Component modelElement = (Component) view.getElement();
		LinkedList<Hummingbird20LinkDescriptor> result = new LinkedList<Hummingbird20LinkDescriptor>();
		result.addAll(getContainedTypeModelFacetLinks_Connection_4001(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public static List<Hummingbird20LinkDescriptor> getConnection_4001ContainedLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public static List<Hummingbird20LinkDescriptor> getComponent_2001IncomingLinks(View view) {
		Component modelElement = (Component) view.getElement();
		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet()
				.getResources());
		LinkedList<Hummingbird20LinkDescriptor> result = new LinkedList<Hummingbird20LinkDescriptor>();
		result.addAll(getIncomingTypeModelFacetLinks_Connection_4001(modelElement, crossReferences));
		return result;
	}

	/**
	 * @generated
	 */
	public static List<Hummingbird20LinkDescriptor> getConnection_4001IncomingLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	public static List<Hummingbird20LinkDescriptor> getComponent_2001OutgoingLinks(View view) {
		Component modelElement = (Component) view.getElement();
		LinkedList<Hummingbird20LinkDescriptor> result = new LinkedList<Hummingbird20LinkDescriptor>();
		result.addAll(getOutgoingTypeModelFacetLinks_Connection_4001(modelElement));
		return result;
	}

	/**
	 * @generated
	 */
	public static List<Hummingbird20LinkDescriptor> getConnection_4001OutgoingLinks(View view) {
		return Collections.emptyList();
	}

	/**
	 * @generated
	 */
	private static Collection<Hummingbird20LinkDescriptor> getContainedTypeModelFacetLinks_Connection_4001(Component container) {
		LinkedList<Hummingbird20LinkDescriptor> result = new LinkedList<Hummingbird20LinkDescriptor>();
		for (Object name : container.getOutgoingConnections()) {
			EObject linkObject = (EObject) name;
			if (false == linkObject instanceof Connection) {
				continue;
			}
			Connection link = (Connection) linkObject;
			if (ConnectionEditPart.VISUAL_ID != Hummingbird20VisualIDRegistry.getLinkWithClassVisualID(link)) {
				continue;
			}
			Component dst = link.getTargetComponent();
			Component src = link.getSourceComponent();
			result.add(new Hummingbird20LinkDescriptor(src, dst, link, Hummingbird20ElementTypes.Connection_4001, ConnectionEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	private static Collection<Hummingbird20LinkDescriptor> getIncomingTypeModelFacetLinks_Connection_4001(Component target,
			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
		LinkedList<Hummingbird20LinkDescriptor> result = new LinkedList<Hummingbird20LinkDescriptor>();
		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
		for (EStructuralFeature.Setting setting : settings) {
			if (setting.getEStructuralFeature() != InstanceModel20Package.eINSTANCE.getConnection_TargetComponent()
					|| false == setting.getEObject() instanceof Connection) {
				continue;
			}
			Connection link = (Connection) setting.getEObject();
			if (ConnectionEditPart.VISUAL_ID != Hummingbird20VisualIDRegistry.getLinkWithClassVisualID(link)) {
				continue;
			}
			Component src = link.getSourceComponent();
			result.add(new Hummingbird20LinkDescriptor(src, target, link, Hummingbird20ElementTypes.Connection_4001, ConnectionEditPart.VISUAL_ID));
		}
		return result;
	}

	/**
	 * @generated
	 */
	private static Collection<Hummingbird20LinkDescriptor> getOutgoingTypeModelFacetLinks_Connection_4001(Component source) {
		Component container = null;
		// Find container element for the link.
		// Climb up by containment hierarchy starting from the source
		// and return the first element that is instance of the container class.
		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
			if (element instanceof Component) {
				container = (Component) element;
			}
		}
		if (container == null) {
			return Collections.emptyList();
		}
		LinkedList<Hummingbird20LinkDescriptor> result = new LinkedList<Hummingbird20LinkDescriptor>();
		for (Object name : container.getOutgoingConnections()) {
			EObject linkObject = (EObject) name;
			if (false == linkObject instanceof Connection) {
				continue;
			}
			Connection link = (Connection) linkObject;
			if (ConnectionEditPart.VISUAL_ID != Hummingbird20VisualIDRegistry.getLinkWithClassVisualID(link)) {
				continue;
			}
			Component dst = link.getTargetComponent();
			Component src = link.getSourceComponent();
			if (src != source) {
				continue;
			}
			result.add(new Hummingbird20LinkDescriptor(src, dst, link, Hummingbird20ElementTypes.Connection_4001, ConnectionEditPart.VISUAL_ID));
		}
		return result;
	}

}
