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
 * Generic filtering interface.
 */
public interface IFilter {

	/**
	 * Filters the list of elements. Removes the elements that need to filtered out from the list.
	 * 
	 * @param elements
	 */
	public Object[] filter(Object[] elements);

	/**
	 * @param item
	 * @return <ul>
	 *         <li><code>true</code> if the item will make it through the filter.</li>
	 *         <li><code>false</code> if the item will not make it through the filter.</li>
	 *         </ul>
	 */
	public boolean select(Object item);

}
