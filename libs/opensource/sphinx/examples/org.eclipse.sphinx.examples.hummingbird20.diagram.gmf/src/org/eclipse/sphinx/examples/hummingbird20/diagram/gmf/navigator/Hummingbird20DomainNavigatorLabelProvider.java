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

import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20DiagramEditorPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

/**
 * @generated
 */
public class Hummingbird20DomainNavigatorLabelProvider implements ICommonLabelProvider {

	/**
	 * @generated
	 */
	private AdapterFactoryLabelProvider myAdapterFactoryLabelProvider = new AdapterFactoryLabelProvider(Hummingbird20DiagramEditorPlugin
			.getInstance().getItemProvidersAdapterFactory());

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
	public Image getImage(Object element) {
		if (element instanceof Hummingbird20DomainNavigatorItem) {
			return myAdapterFactoryLabelProvider.getImage(((Hummingbird20DomainNavigatorItem) element).getEObject());
		}
		return null;
	}

	/**
	 * @generated
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof Hummingbird20DomainNavigatorItem) {
			return myAdapterFactoryLabelProvider.getText(((Hummingbird20DomainNavigatorItem) element).getEObject());
		}
		return null;
	}

	/**
	 * @generated
	 */
	@Override
	public void addListener(ILabelProviderListener listener) {
		myAdapterFactoryLabelProvider.addListener(listener);
	}

	/**
	 * @generated
	 */
	@Override
	public void dispose() {
		myAdapterFactoryLabelProvider.dispose();
	}

	/**
	 * @generated
	 */
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return myAdapterFactoryLabelProvider.isLabelProperty(element, property);
	}

	/**
	 * @generated
	 */
	@Override
	public void removeListener(ILabelProviderListener listener) {
		myAdapterFactoryLabelProvider.removeListener(listener);
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

}
