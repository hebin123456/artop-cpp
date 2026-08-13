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

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * AttributeMarkerGrouping is the configuration element for the markerAttributeGrouping extension.
 * 
 * @since 0.7.0
 */
public class AttributeMarkerGrouping {

	private String attribute;

	private String markerType;

	private String defaultGroupingEntry;

	private IConfigurationElement element;

	/**
	 * Create a new instance of the receiver for the given attribute on the markerType with an optional default
	 * grouping.
	 * 
	 * @param attributeId
	 * @param markerId
	 * @param defaultEntry
	 * @param configElement
	 */
	public AttributeMarkerGrouping(String attributeId, String markerId, String defaultEntry, IConfigurationElement configElement) {
		attribute = attributeId;
		markerType = markerId;
		defaultGroupingEntry = defaultEntry;
		element = configElement;

	}

	/**
	 * Return the id of the default grouping.
	 * 
	 * @return String or <code>null</code> if it is not defined.
	 */
	public String getDefaultGroupingEntry() {
		return defaultGroupingEntry;
	}

	/**
	 * Return the id of the marker type for this type.
	 * 
	 * @return String
	 */
	public String getMarkerType() {
		return markerType;
	}

	/**
	 * Return the name of the attribute for the receiver.
	 * 
	 * @return String
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * Return the IConfigurationElement for the receiver.
	 * 
	 * @return IConfigurationElement
	 */
	public IConfigurationElement getElement() {
		return element;
	}

}
