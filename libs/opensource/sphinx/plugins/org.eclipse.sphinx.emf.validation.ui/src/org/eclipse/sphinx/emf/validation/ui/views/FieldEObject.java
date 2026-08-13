/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     See4sys - added support for problem markers on model objects (rather than 
 *               only on workspace resources). Unfortunately, there was no other 
 *               choice than copying the whole code from 
 *               org.eclipse.ui.views.markers.internal for that purpose because 
 *               many of the relevant classes, methods, and fields are private or
 *               package private.
 *******************************************************************************/
package org.eclipse.sphinx.emf.validation.ui.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * Folder field. Designed to display and compare the names of the eObject that contain IMarker objects.
 */
public class FieldEObject extends AbstractField {

	/**
	 * Create a new instance of the receiver.
	 */
	public FieldEObject() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescription()
	 */
	@Override
	public String getDescription() {
		return MarkerMessages.description_eObject;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescriptionImage()
	 */
	@Override
	public Image getDescriptionImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderText()
	 */
	@Override
	public String getColumnHeaderText() {
		return MarkerMessages.description_eObject;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderImage()
	 */
	@Override
	public Image getColumnHeaderImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getValue(java.lang.Object)
	 */
	@Override
	public String getValue(Object obj) {
		if (obj == null || !(obj instanceof ConcreteMarker)) {
			return Util.EMPTY_STRING;
		}
		ConcreteMarker marker = (ConcreteMarker) obj;
		return marker.getEObjectID();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object obj) {

		if (obj == null || !(obj instanceof ConcreteMarker)) {
			return null;
		}
		ConcreteMarker marker = (ConcreteMarker) obj;

		EObject eObject = null;
		IResource resource = marker.getResource();
		if (resource instanceof IFile) {
			IMetaModelDescriptor descriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor((IFile) resource);
			if (descriptor != null) {
				EPackage ePackage = descriptor.getRootEPackage();
				if (ePackage != null) {
					EClassifier eClassifier = ePackage.getEClassifier(marker.getEObjectType());
					if (eClassifier != null && eClassifier instanceof EClass) {
						EClass eClass = (EClass) eClassifier;
						eObject = ePackage.getEFactoryInstance().create(eClass);
						if (eObject != null) {
							ComposeableAdapterFactory adaptarFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
							IItemLabelProvider provider = (IItemLabelProvider) adaptarFactory.adapt(eObject, IItemLabelProvider.class);
							return ExtendedImageRegistry.getInstance().getImage(provider.getImage(eObject));
						}
					}
				}
			}
		}
		return ExtendedImageRegistry.getInstance().getImage(obj);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Object obj1, Object obj2) {
		if (obj1 == null || obj2 == null || !(obj1 instanceof ConcreteMarker) || !(obj2 instanceof ConcreteMarker)) {
			return 0;
		}
		ConcreteMarker marker1 = (ConcreteMarker) obj1;
		ConcreteMarker marker2 = (ConcreteMarker) obj2;

		// construct full address of targetObject
		String targetObj1 = marker1.getFolder() + "/" + marker1.getResourceName() + "/" + marker1.getEObjectID(); //$NON-NLS-1$ //$NON-NLS-2$
		String targetObj2 = marker2.getFolder() + "/" + marker2.getResourceName() + "/" + marker2.getEObjectID(); //$NON-NLS-1$//$NON-NLS-2$
		return targetObj1.compareTo(targetObj2);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDefaultDirection()
	 */
	@Override
	public int getDefaultDirection() {
		return TableComparator.ASCENDING;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getPreferredWidth()
	 */
	@Override
	public int getPreferredWidth() {
		return 150;
	}

}
