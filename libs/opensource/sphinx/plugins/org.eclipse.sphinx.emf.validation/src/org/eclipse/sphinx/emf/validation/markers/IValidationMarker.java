/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 See4sys, itemis, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [418902] ValidationMarkerManager does not distinguish objects with identical URI
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.validation.markers;

public interface IValidationMarker {

	/**
	 * Validation problem marker type.
	 */
	public static final String MODEL_VALIDATION_PROBLEM = "sphinx.emf.validation.problem.marker"; //$NON-NLS-1$

	/**
	 * This is the id of the marker attribute in order to hold an understandable eObject name. not used yet
	 */

	public static final String EOBJECT_ATTRIBUTE = "eobject_id"; //$NON-NLS-1$

	/**
	 * This is the id of the marker in order to hold the target features of this markers
	 */

	public static final String FEATURES_ATTRIBUTE = "features_id"; //$NON-NLS-1$

	/**
	 * This is the id of the violated rule
	 */

	public static final String RULE_ID_ATTRIBUTE = "rule_id"; //$NON-NLS-1$

	/**
	 * This is the hash code of the affected object
	 */
	public static final String HASH_ATTRIBUTE = "hash_code"; //$NON-NLS-1$

}
