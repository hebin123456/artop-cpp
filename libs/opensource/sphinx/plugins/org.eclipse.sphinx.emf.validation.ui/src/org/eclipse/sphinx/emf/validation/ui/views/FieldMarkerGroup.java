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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

/**
 * @since 0.7.0
 */
public class FieldMarkerGroup implements IField {

	class EntryMapping {
		MarkerGroupingEntry groupingEntry;

		/**
		 * Create an entry mapping for the receiver.
		 * 
		 * @param entry
		 */
		EntryMapping(MarkerGroupingEntry entry) {
			groupingEntry = entry;
		}

		/**
		 * Return whether or not the receiver tests attributes.
		 * 
		 * @return boolean
		 */
		public boolean hasAttributes() {
			return false;
		}

		/**
		 * Test the attribute of the marker to find a grouping.
		 * 
		 * @param marker
		 * @return MarkerGroupingEntry or <code>null</code> if there is not entry.
		 */
		public MarkerGroupingEntry testAttribute(ConcreteMarker marker) {
			return null;
		}
	}

	private static MarkerGroupingEntry undefinedEntry = new MarkerGroupingEntry(MarkerMessages.FieldCategory_Uncategorized, null, 0);

	class AttributeMapping extends EntryMapping {

		String attribute;

		String attributeValue;

		/**
		 * Create a mapping for an attribute with name attributeName and value value to the supplied entry.
		 * 
		 * @param entry
		 * @param attributeName
		 * @param value
		 */
		AttributeMapping(MarkerGroupingEntry entry, String attributeName, String value) {
			super(entry);
			attribute = attributeName;
			attributeValue = value;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ui.views.markers.internal.FieldMarkerGroup.EntryMapping#hasAttributes()
		 */
		@Override
		public boolean hasAttributes() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.ui.views.markers.internal.FieldMarkerGroup.EntryMapping#testAttribute(org.eclipse.ui.views.markers
		 * .internal.ConcreteMarker)
		 */
		@Override
		public MarkerGroupingEntry testAttribute(ConcreteMarker marker) {
			Object value;

			if (!marker.getMarker().exists()) {
				return null;// If the marker was deleted during the update drop it
			}

			try {
				value = marker.getMarker().getAttribute(attribute);
			} catch (CoreException e) {
				Util.log(e);
				return null;
			}

			if (value != null && attributeValue.equals(value.toString())) {
				return groupingEntry;
			}
			return null;
		}
	}

	private String title;

	private String id;

	private Map typesToMappings = new HashMap();

	private boolean showing;

	/**
	 * Create a new instance of the receiver called name with id identifier.
	 * 
	 * @param name
	 * @param identifier
	 */
	public FieldMarkerGroup(String name, String identifier) {
		title = name;
		id = identifier;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescription()
	 */
	@Override
	public String getDescription() {
		return title;
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
		return title;
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
		MarkerNode node = (MarkerNode) obj;

		if (node.isConcrete()) {
			MarkerGroupingEntry groupingEntry = getMapping((ConcreteMarker) node);
			return groupingEntry.getLabel();
		}
		return node.getDescription();
	}

	/**
	 * Get the attribute mapping for the marker
	 * 
	 * @param marker
	 * @return MarkerGroupingEntry
	 */
	private MarkerGroupingEntry getMapping(ConcreteMarker marker) {

		if (marker.getGroup() == null) {
			marker.setGroup(findGroupValue(marker));
		}
		return (MarkerGroupingEntry) marker.getGroup();
	}

	/**
	 * Find the group value. If it cannot be found in an attribute mapping then return null;
	 * 
	 * @param marker
	 * @return String or <code>null</code>
	 */
	private MarkerGroupingEntry findGroupValue(ConcreteMarker marker) {

		if (typesToMappings.containsKey(marker.getType())) {
			EntryMapping defaultMapping = null;
			Iterator mappings = ((Collection) typesToMappings.get(marker.getType())).iterator();
			while (mappings.hasNext()) {
				EntryMapping mapping = (EntryMapping) mappings.next();
				if (mapping.hasAttributes()) {
					MarkerGroupingEntry entry = mapping.testAttribute(marker);
					if (entry != null) {
						return entry;
					}
				} else {
					// If it has no attributes it is our default
					defaultMapping = mapping;
				}
			}
			if (defaultMapping != null) {
				return defaultMapping.groupingEntry;
			}

		}

		return undefinedEntry;

	} /*
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

		MarkerGroupingEntry entry1 = getMapping(((MarkerNode) obj1).getConcreteRepresentative());
		MarkerGroupingEntry entry2 = getMapping(((MarkerNode) obj2).getConcreteRepresentative());
		return entry2.getPriority() - entry1.getPriority();

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
		return 75;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#isShowing()
	 */
	@Override
	public boolean isShowing() {
		return showing;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#setShowing(boolean)
	 */
	@Override
	public void setShowing(boolean showing) {
		this.showing = showing;

	}

	/**
	 * Set entry and the default entry for the supplied markerType.
	 * 
	 * @param markerType
	 * @param entry
	 */

	public void setAsDefault(String markerType, MarkerGroupingEntry entry) {
		addEntry(markerType, new EntryMapping(entry));

	}

	/**
	 * Add the entry for the markerType.
	 * 
	 * @param markerType
	 * @param entry
	 */
	private void addEntry(String markerType, EntryMapping entry) {

		MarkerType[] allDerived = getMarkerTypes(markerType);

		for (MarkerType type : allDerived) {
			Collection entries = new HashSet();
			if (typesToMappings.containsKey(type.getId())) {
				entries = (Collection) typesToMappings.get(markerType);
			} else {
				entries = new HashSet();
			}

			entries.add(entry);
			typesToMappings.put(type.getId(), entries);
		}

	}

	/**
	 * Return the marker types that match and are subtypes of markerType.
	 * 
	 * @param markerType
	 * @return MarkerType[]
	 */
	private MarkerType[] getMarkerTypes(String markerType) {
		MarkerTypesModel model = MarkerTypesModel.getInstance();
		Collection types = new HashSet();

		MarkerType type = model.getType(markerType);
		if (type != null) {
			types.add(type);
			MarkerType[] subs = type.getAllSubTypes();
			for (MarkerType sub : subs) {
				types.add(sub);
			}
		}

		if (types.isEmpty()) {
			return new MarkerType[0];
		}

		MarkerType[] typesArray = new MarkerType[types.size()];
		types.toArray(typesArray);
		return typesArray;
	}

	/**
	 * Add an attributeMapping for the markerType.
	 * 
	 * @param markerType
	 * @param attribute
	 * @param attributeValue
	 * @param entry
	 */
	public void mapAttribute(String markerType, String attribute, String attributeValue, MarkerGroupingEntry entry) {
		addEntry(markerType, new AttributeMapping(entry, attribute, attributeValue));

	}

	/**
	 * Return the id of the receiver.
	 * 
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Remove the entry from all of the entries in the receiver.
	 * 
	 * @param entry
	 */
	public void remove(MarkerGroupingEntry entry) {
		Iterator entries = typesToMappings.values().iterator();
		Collection removeCollection = new ArrayList();
		while (entries.hasNext()) {
			Collection mappings = (Collection) entries.next();
			Iterator mappingsIterator = mappings.iterator();
			while (mappingsIterator.hasNext()) {
				EntryMapping next = (EntryMapping) mappingsIterator.next();
				if (next.groupingEntry.equals(entry)) {
					removeCollection.add(next);
				}

			}
			mappings.removeAll(removeCollection);
			removeCollection.clear();
		}

	}
}
