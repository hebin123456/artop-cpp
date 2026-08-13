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
package org.eclipse.sphinx.emf.validation.markers;

import java.util.EventObject;

/**
 * Can be used to get notified about validation problem marker changes.
 */
public interface IValidationProblemMarkersChangeListener {

	/**
	 * Called when validation problem markers have changed.
	 */
	void validationProblemMarkersChanged(EventObject event);
}
