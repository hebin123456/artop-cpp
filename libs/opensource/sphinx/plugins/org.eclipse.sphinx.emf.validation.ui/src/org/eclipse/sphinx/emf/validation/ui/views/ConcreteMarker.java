/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation, See4sys, itemis and others.
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
 *     itemis - [480135] Introduce metamodel and view content agnostic problem decorator for model elements
 *******************************************************************************/
package org.eclipse.sphinx.emf.validation.ui.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.validation.markers.IValidationMarker;
import org.eclipse.sphinx.emf.validation.util.ValidationUtil;

import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;

/**
 * This is a concrete class that stores the same type of information as the IMarkers used by the IDE. This class exists
 * as an optimization. The various get* methods on IMarker are extremely slow, which makes it very slow to sort markers
 * (for example, in the problems view). This marker class stores the fields in the most efficient form for sorting and
 * display, but necessarily removes some generality from IMarker.
 */
public class ConcreteMarker extends MarkerNode {

	// private String description;

	private String resourceName;

	private String inFolder;

	private String eObjectID;

	private String uriToDisplay;

	private String eObjectType;

	private CollationKey descriptionKey;

	private CollationKey resourceNameKey;

	private int line;

	private String locationString;

	private long creationTime;

	private String type;

	private IMarker marker;

	/**
	 * Cache for the marker ID.
	 */
	private long id = -1L;

	private MarkerNode markerCategory;

	private String shortFolder;

	private Object group;

	public ConcreteMarker(IMarker toCopy) {
		marker = toCopy;
		refresh();
	}

	/**
	 * Clears any cached information. This frees up some memory, but will slow down the next comparison operation. It is
	 * a good idea to call this on a set of markers after sorting them, in order to reduce their memory cost.
	 */
	public void clearCache() {
		resourceNameKey = null;
		descriptionKey = null;
	}

	/**
	 * Refresh the properties of this marker from the underlying IMarker instance
	 */
	public void refresh() {
		clearCache();

		// description = Util.getProperty(IMarker.MESSAGE, marker);
		resourceName = Util.getResourceName(marker);
		inFolder = Util.getContainerName(marker);
		shortFolder = null;
		line = marker.getAttribute(IMarker.LINE_NUMBER, -1);
		locationString = marker.getAttribute(IMarker.LOCATION, Util.EMPTY_STRING);

		boolean validationProblem;
		try {
			validationProblem = marker.isSubtypeOf(IValidationMarker.MODEL_VALIDATION_PROBLEM);
		} catch (CoreException ex1) {
			validationProblem = false;
		}

		if (validationProblem) {
			// Let's get URI
			String uri = marker.getAttribute(EValidator.URI_ATTRIBUTE, MarkerMessages.no_available_data);
			uriToDisplay = ExtendedResource.URI_FRAGMENT_SEPARATOR + URI.createURI(uri, true).fragment();
			try {
				Object object = marker.getAttribute(IValidationMarker.EOBJECT_ATTRIBUTE);
				eObjectID = object == null ? ValidationUtil.getObjectId(uri) : (String) object;
				eObjectType = ValidationUtil.getObjectType(uri);
			} catch (CoreException ex) { // e.g. the EOBJECT_ATTRIBUTE does not exist, well.
				eObjectID = MarkerMessages.no_available_data;
			}
		} else {
			eObjectID = MarkerMessages.no_available_data;
			uriToDisplay = MarkerMessages.no_available_data;
			eObjectType = MarkerMessages.no_available_data;
		}

		try {
			creationTime = marker.getCreationTime();
		} catch (CoreException e) {
			creationTime = 0;
		}

		try {
			type = marker.getType();
		} catch (CoreException e1) {
			type = Util.EMPTY_STRING;
		}

		// store the marker ID locally
		id = marker.getId();
	}

	public IResource getResource() {
		return marker.getResource();
	}

	public String getType() {
		return type;
	}

	public String getEObjectID() {
		return eObjectID;
	}

	public String getEObjectType() {
		return eObjectType;
	}

	public String getURIToDisplay() {
		return uriToDisplay;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getDescription()
	 */
	@Override
	public String getDescription() {
		// if (description == null) {
		// description = Util.getProperty(IMarker.MESSAGE, marker);
		// }
		// return description;

		return Util.getProperty(IMarker.MESSAGE, marker);

	}

	public CollationKey getDescriptionKey() {
		if (descriptionKey == null) {
			// descriptionKey = Collator.getInstance().getCollationKey(description);
			descriptionKey = Collator.getInstance().getCollationKey(getDescription());
		}

		return descriptionKey;
	}

	public String getResourceName() {
		return resourceName;
	}

	public CollationKey getResourceNameKey() {
		if (resourceNameKey == null) {
			resourceNameKey = Collator.getInstance().getCollationKey(resourceName);
		}
		return resourceNameKey;
	}

	public int getLine() {
		return line;
	}

	public String getFolder() {
		return inFolder;
	}

	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * The underlying marker ID value.
	 *
	 * @return the marker's ID.
	 */
	public long getId() {
		return id;
	}

	public IMarker getMarker() {
		return marker;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ConcreteMarker)) {
			return false;
		}

		ConcreteMarker other = (ConcreteMarker) object;

		return other.getMarker().equals(getMarker());
	}

	@Override
	public int hashCode() {
		return getMarker().hashCode();
	}

	/**
	 * Set the category the receiver is in.
	 *
	 * @param category
	 */
	public void setCategory(MarkerNode category) {
		markerCategory = category;

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getChildren()
	 */
	@Override
	public MarkerNode[] getChildren() {
		return Util.EMPTY_MARKER_ARRAY;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getParent()
	 */
	@Override
	public MarkerNode getParent() {
		return markerCategory;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerNode#isConcrete()
	 */
	@Override
	public boolean isConcrete() {
		return true;
	}

	/**
	 * Return the short name for the folder.
	 *
	 * @return String
	 */
	public String getShortFolder() {
		if (shortFolder == null) {
			shortFolder = Util.getShortContainerName(marker);
		}
		return shortFolder;
	}

	/**
	 * Get the location string. If the {@link IMarker#LOCATION } attribute was not set then return an empty String.
	 *
	 * @return String
	 */
	public String getLocationString() {
		return locationString;
	}

	/**
	 * Get the group for the reciever.
	 *
	 * @return Returns the group.
	 */
	public Object getGroup() {
		return group;
	}

	/**
	 * Set the group name.
	 *
	 * @param group
	 *            the group name
	 */
	public void setGroup(Object group) {
		this.group = group;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getConcreteRepresentative()
	 */
	@Override
	public ConcreteMarker getConcreteRepresentative() {
		return this;
	}
}
