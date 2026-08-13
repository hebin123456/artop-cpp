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
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.navigator;

import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserOptions;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ApplicationEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ComponentEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ComponentNameEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ConnectionEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ConnectionLabelEditPart;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20DiagramEditorPlugin;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20VisualIDRegistry;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.providers.Hummingbird20ElementTypes;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.providers.Hummingbird20ParserProvider;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

/**
 * @generated
 */
public class Hummingbird20NavigatorLabelProvider extends LabelProvider implements ICommonLabelProvider, ITreePathLabelProvider {

	/**
	 * @generated
	 */
	static {
		Hummingbird20DiagramEditorPlugin.getInstance().getImageRegistry()
				.put("Navigator?UnknownElement", ImageDescriptor.getMissingImageDescriptor()); //$NON-NLS-1$
		Hummingbird20DiagramEditorPlugin.getInstance().getImageRegistry().put("Navigator?ImageNotFound", ImageDescriptor.getMissingImageDescriptor()); //$NON-NLS-1$
	}

	/**
	 * @generated
	 */
	@Override
	public void updateLabel(ViewerLabel label, TreePath elementPath) {
		Object element = elementPath.getLastSegment();
		if (element instanceof Hummingbird20NavigatorItem && !isOwnView(((Hummingbird20NavigatorItem) element).getView())) {
			return;
		}
		label.setText(getText(element));
		label.setImage(getImage(element));
	}

	/**
	 * @generated
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof Hummingbird20NavigatorGroup) {
			Hummingbird20NavigatorGroup group = (Hummingbird20NavigatorGroup) element;
			return Hummingbird20DiagramEditorPlugin.getInstance().getBundledImage(group.getIcon());
		}

		if (element instanceof Hummingbird20NavigatorItem) {
			Hummingbird20NavigatorItem navigatorItem = (Hummingbird20NavigatorItem) element;
			if (!isOwnView(navigatorItem.getView())) {
				return super.getImage(element);
			}
			return getImage(navigatorItem.getView());
		}

		return super.getImage(element);
	}

	/**
	 * @generated
	 */
	public Image getImage(View view) {
		switch (Hummingbird20VisualIDRegistry.getVisualID(view)) {
		case ConnectionEditPart.VISUAL_ID:
			return getImage(
					"Navigator?Link?http://www.eclipse.org/sphinx/examples/hummingbird/2.0.1/instancemodel?Connection", Hummingbird20ElementTypes.Connection_4001); //$NON-NLS-1$
		case ComponentEditPart.VISUAL_ID:
			return getImage(
					"Navigator?TopLevelNode?http://www.eclipse.org/sphinx/examples/hummingbird/2.0.1/instancemodel?Component", Hummingbird20ElementTypes.Component_2001); //$NON-NLS-1$
		case ApplicationEditPart.VISUAL_ID:
			return getImage(
					"Navigator?Diagram?http://www.eclipse.org/sphinx/examples/hummingbird/2.0.1/instancemodel?Application", Hummingbird20ElementTypes.Application_1000); //$NON-NLS-1$
		}
		return getImage("Navigator?UnknownElement", null); //$NON-NLS-1$
	}

	/**
	 * @generated
	 */
	private Image getImage(String key, IElementType elementType) {
		ImageRegistry imageRegistry = Hummingbird20DiagramEditorPlugin.getInstance().getImageRegistry();
		Image image = imageRegistry.get(key);
		if (image == null && elementType != null && Hummingbird20ElementTypes.isKnownElementType(elementType)) {
			image = Hummingbird20ElementTypes.getImage(elementType);
			imageRegistry.put(key, image);
		}

		if (image == null) {
			image = imageRegistry.get("Navigator?ImageNotFound"); //$NON-NLS-1$
			imageRegistry.put(key, image);
		}
		return image;
	}

	/**
	 * @generated
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof Hummingbird20NavigatorGroup) {
			Hummingbird20NavigatorGroup group = (Hummingbird20NavigatorGroup) element;
			return group.getGroupName();
		}

		if (element instanceof Hummingbird20NavigatorItem) {
			Hummingbird20NavigatorItem navigatorItem = (Hummingbird20NavigatorItem) element;
			if (!isOwnView(navigatorItem.getView())) {
				return null;
			}
			return getText(navigatorItem.getView());
		}

		return super.getText(element);
	}

	/**
	 * @generated
	 */
	public String getText(View view) {
		if (view.getElement() != null && view.getElement().eIsProxy()) {
			return getUnresolvedDomainElementProxyText(view);
		}
		switch (Hummingbird20VisualIDRegistry.getVisualID(view)) {
		case ConnectionEditPart.VISUAL_ID:
			return getConnection_4001Text(view);
		case ComponentEditPart.VISUAL_ID:
			return getComponent_2001Text(view);
		case ApplicationEditPart.VISUAL_ID:
			return getApplication_1000Text(view);
		}
		return getUnknownElementText(view);
	}

	/**
	 * @generated
	 */
	private String getApplication_1000Text(View view) {
		Application domainModelElement = (Application) view.getElement();
		if (domainModelElement != null) {
			return domainModelElement.getName();
		} else {
			Hummingbird20DiagramEditorPlugin.getInstance().logError("No domain element for view with visualID = " + 1000); //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * @generated
	 */
	private String getConnection_4001Text(View view) {
		IParser parser = Hummingbird20ParserProvider.getParser(Hummingbird20ElementTypes.Connection_4001,
				view.getElement() != null ? view.getElement() : view, Hummingbird20VisualIDRegistry.getType(ConnectionLabelEditPart.VISUAL_ID));
		if (parser != null) {
			return parser.getPrintString(new EObjectAdapter(view.getElement() != null ? view.getElement() : view), ParserOptions.NONE.intValue());
		} else {
			Hummingbird20DiagramEditorPlugin.getInstance().logError("Parser was not found for label " + 6001); //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * @generated
	 */
	private String getComponent_2001Text(View view) {
		IParser parser = Hummingbird20ParserProvider.getParser(Hummingbird20ElementTypes.Component_2001,
				view.getElement() != null ? view.getElement() : view, Hummingbird20VisualIDRegistry.getType(ComponentNameEditPart.VISUAL_ID));
		if (parser != null) {
			return parser.getPrintString(new EObjectAdapter(view.getElement() != null ? view.getElement() : view), ParserOptions.NONE.intValue());
		} else {
			Hummingbird20DiagramEditorPlugin.getInstance().logError("Parser was not found for label " + 5001); //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * @generated
	 */
	private String getUnknownElementText(View view) {
		return "<UnknownElement Visual_ID = " + view.getType() + ">"; //$NON-NLS-1$  //$NON-NLS-2$
	}

	/**
	 * @generated
	 */
	private String getUnresolvedDomainElementProxyText(View view) {
		return "<Unresolved domain element Visual_ID = " + view.getType() + ">"; //$NON-NLS-1$  //$NON-NLS-2$
	}

	/**
	 * @generated
	 */
	@Override
	public void init(ICommonContentExtensionSite aConfig) {
	}

	/**
	 * @generated
	 */
	@Override
	public void restoreState(IMemento aMemento) {
	}

	/**
	 * @generated
	 */
	@Override
	public void saveState(IMemento aMemento) {
	}

	/**
	 * @generated
	 */
	@Override
	public String getDescription(Object anElement) {
		return null;
	}

	/**
	 * @generated
	 */
	private boolean isOwnView(View view) {
		return ApplicationEditPart.MODEL_ID.equals(Hummingbird20VisualIDRegistry.getModelID(view));
	}

}
