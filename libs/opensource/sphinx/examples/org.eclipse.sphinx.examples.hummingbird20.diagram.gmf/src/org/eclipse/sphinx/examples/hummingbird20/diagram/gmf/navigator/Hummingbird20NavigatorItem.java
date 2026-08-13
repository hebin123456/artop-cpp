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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.notation.View;

/**
 * @generated
 */
public class Hummingbird20NavigatorItem extends Hummingbird20AbstractNavigatorItem {

	/**
	 * @generated
	 */
	static {
		final Class[] supportedTypes = new Class[] { View.class, EObject.class };
		Platform.getAdapterManager().registerAdapters(new IAdapterFactory() {

			@Override
			public Object getAdapter(Object adaptableObject, Class adapterType) {
				if (adaptableObject instanceof org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.navigator.Hummingbird20NavigatorItem
						&& (adapterType == View.class || adapterType == EObject.class)) {
					return ((org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.navigator.Hummingbird20NavigatorItem) adaptableObject).getView();
				}
				return null;
			}

			@Override
			public Class[] getAdapterList() {
				return supportedTypes;
			}
		}, org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.navigator.Hummingbird20NavigatorItem.class);
	}

	/**
	 * @generated
	 */
	private View myView;

	/**
	 * @generated
	 */
	private boolean myLeaf = false;

	/**
	 * @generated
	 */
	public Hummingbird20NavigatorItem(View view, Object parent, boolean isLeaf) {
		super(parent);
		myView = view;
		myLeaf = isLeaf;
	}

	/**
	 * @generated
	 */
	public View getView() {
		return myView;
	}

	/**
	 * @generated
	 */
	public boolean isLeaf() {
		return myLeaf;
	}

	/**
	 * @generated
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.navigator.Hummingbird20NavigatorItem) {
			return EcoreUtil.getURI(getView()).equals(
					EcoreUtil.getURI(((org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.navigator.Hummingbird20NavigatorItem) obj).getView()));
		}
		return super.equals(obj);
	}

	/**
	 * @generated
	 */
	@Override
	public int hashCode() {
		return EcoreUtil.getURI(getView()).hashCode();
	}

}
