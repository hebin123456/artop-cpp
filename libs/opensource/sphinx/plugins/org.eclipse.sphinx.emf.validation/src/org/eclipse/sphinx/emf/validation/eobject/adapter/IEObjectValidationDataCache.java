/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.validation.eobject.adapter;

public interface IEObjectValidationDataCache {

	/**
	 * Checks if the severity value stored into the adapter is up-to-date.
	 * 
	 * @return <tt>true</tt> if the stored serverity value is up-to-dat, otherwise <tt>false</tt>.
	 */
	public boolean isSeverityOk();

	/**
	 * Returns the severity value stored for the adapted object.
	 * 
	 * @return The stored severity value.
	 * @see org.eclipse.sphinx.emf.validation.markers.ValidationStatusCode
	 */
	public int getSeverity();

}
