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

/**
 * MarkerGroupingEntry is the configuration object for the markerGroupingEntry extension.
 * 
 * @since 0.7.0
 */
public class MarkerGroupingEntry {

	private FieldMarkerGroup markerGroup;
	private String label;
	private String id;
	private int sortPriority;

	/**
	 * Create a new instance of the receiver with name name and an id of identifier.
	 * 
	 * @param name
	 * @param identifer
	 * @param priority
	 */
	public MarkerGroupingEntry(String name, String identifer, int priority) {
		label = name;
		id = identifer;
		sortPriority = priority;
	}

	/**
	 * Set the receiver as the default grouping entry for type markerType.
	 * 
	 * @param markerType
	 *            String
	 */
	public void setAsDefault(String markerType) {
		markerGroup.setAsDefault(markerType, this);

	}

	/**
	 * Map the attribute for the markerType to map to the receiver when it is equal to attributeValue.
	 * 
	 * @param markerType
	 * @param attribute
	 * @param attributeValue
	 */
	public void mapAttribute(String markerType, String attribute, String attributeValue) {
		markerGroup.mapAttribute(markerType, attribute, attributeValue, this);

	}

	/**
	 * Return the id for the receiver.
	 * 
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the group for the receiver.
	 * 
	 * @param group
	 */
	public void setGroupingEntry(FieldMarkerGroup group) {
		markerGroup = group;

	}

	/**
	 * Get the label of the receiver.
	 * 
	 * @return String
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Return the priority of the receiver.
	 * 
	 * @return int
	 */
	public int getPriority() {
		return sortPriority;
	}

	/**
	 * Return the marker group for the receiver.
	 * 
	 * @return FieldMarkerGroup
	 */
	public FieldMarkerGroup getMarkerGroup() {
		return markerGroup;
	}

}
