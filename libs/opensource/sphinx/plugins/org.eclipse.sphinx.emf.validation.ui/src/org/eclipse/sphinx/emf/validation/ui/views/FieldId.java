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

import org.eclipse.swt.graphics.Image;

/**
 * FieldId is the field for marker id.
 */
public class FieldId implements IField {

	private String description;

	private Image image;

	/**
	 * Create a new instance of the receiver.
	 */
	public FieldId() {
		description = MarkerMessages.description_markerId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescriptionImage()
	 */
	@Override
	public Image getDescriptionImage() {
		return image;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderText()
	 */
	@Override
	public String getColumnHeaderText() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderImage()
	 */
	@Override
	public Image getColumnHeaderImage() {
		return image;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getValue(java.lang.Object)
	 */
	@Override
	public String getValue(Object obj) {
		if (obj == null || !(obj instanceof ConcreteMarker)) {
			return ""; //$NON-NLS-1$
		}
		ConcreteMarker marker = (ConcreteMarker) obj;
		return "" + marker.getId(); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object obj) {
		return null;
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
		return (int) (marker1.getId() - marker2.getId());
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
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#isShowing()
	 */
	@Override
	public boolean isShowing() {
		// Never show id
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#setShowing(boolean)
	 */
	@Override
	public void setShowing(boolean showing) {
		// Ignore

	}
}
