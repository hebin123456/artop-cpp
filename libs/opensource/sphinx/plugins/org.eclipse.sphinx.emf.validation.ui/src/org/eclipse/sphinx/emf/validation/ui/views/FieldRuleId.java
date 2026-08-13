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

import org.eclipse.emf.validation.service.ConstraintRegistry;
import org.eclipse.emf.validation.service.IConstraintDescriptor;
import org.eclipse.sphinx.emf.validation.markers.IValidationMarker;
import org.eclipse.swt.graphics.Image;

/**
 * Folder field. Designed to display and compare the names of the folders that contain IMarker objects.
 */
public class FieldRuleId extends AbstractField {

	/**
	 * Create a new instance of the receiver.
	 */
	public FieldRuleId() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescription()
	 */
	@Override
	public String getDescription() {
		return MarkerMessages.description_ruleId;
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
		return MarkerMessages.description_ruleId;
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
		IConstraintDescriptor icd = ConstraintRegistry.getInstance().getDescriptor(
				marker.getMarker().getAttribute(IValidationMarker.RULE_ID_ATTRIBUTE, MarkerMessages.no_available_data));
		if (icd == null) {
			return marker.getMarker().getAttribute("message", MarkerMessages.no_available_data); //$NON-NLS-1$
		}
		return icd.getName();
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

		return marker1.getMarker().getAttribute(IValidationMarker.RULE_ID_ATTRIBUTE, MarkerMessages.no_available_data)
				.compareTo(marker2.getMarker().getAttribute(IValidationMarker.RULE_ID_ATTRIBUTE, MarkerMessages.no_available_data));
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

}
