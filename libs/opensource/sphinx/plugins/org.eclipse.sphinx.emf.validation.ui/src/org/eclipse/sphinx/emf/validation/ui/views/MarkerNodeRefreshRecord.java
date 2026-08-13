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

class MarkerNodeRefreshRecord {
	Collection removedMarkers;
	Collection addedMarkers;
	Collection changedMarkers;

	/**
	 * Create a new instance of the receiver with the supplied markers.
	 * 
	 * @param removed
	 * @param added
	 * @param changed
	 */
	MarkerNodeRefreshRecord(Collection removed, Collection added, Collection changed) {
		removedMarkers = new ArrayList(removed);
		addedMarkers = new ArrayList(added);
		changedMarkers = new ArrayList(changed);
	}

	/**
	 * Add the node to the list of removals.
	 * 
	 * @param node
	 */
	public void remove(MarkerNode node) {
		removedMarkers.add(node);

	}

	/**
	 * Add the node to the list of adds.
	 * 
	 * @param node
	 */
	public void add(MarkerNode node) {
		addedMarkers.add(node);

	}
}