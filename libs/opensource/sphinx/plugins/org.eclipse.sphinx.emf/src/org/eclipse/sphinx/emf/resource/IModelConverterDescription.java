/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     BMW Car IT - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

/**
 * Represents the user relevant information about an IModelConverter. User relevant means information which the user
 * should know and which is to be displayed to her/him within the UI.
 * 
 * @since 0.7.0
 */
public interface IModelConverterDescription {

	/**
	 * Returns details on this IModelConverter's behavior. The details are intended to be displayed to the user in order
	 * to inform her/him about the characteristics and the behavior of this IModelConverter.
	 * 
	 * @return A description of this IModelConverter's behavior.
	 */
	String getBehaviorDetails();

	/**
	 * Returns a message containing information about side effects of this IModelConverter. This message is intended to
	 * be displayed to the user to warn her/him about the side effects which might result from the usage of this
	 * IModleConverter.
	 * 
	 * @return A message intended to be displayed to the user as a warning.
	 */
	String getWarning();

	/**
	 * Determines if any user relevant information is provided by this IModelConverterDescription. User relevant means
	 * any data which is to be displayed to the user within the UI.
	 * 
	 * @return <code>true<code> if no user relevant information is provided by this IModelConverterDescription, <code>false</code>
	 *         otherwise.
	 */
	boolean isEmpty();

	/**
	 * Determines if this IModelConverterDescription provides any warning.
	 * 
	 * @return <code>true</code> if this IModelConverterDescription provides a warning.
	 */
	boolean hasWarning();

	/**
	 * Determines if this IModelConverterDescription provides any details on the IModelConverter's behavior.
	 * 
	 * @return <code>true</code> if this IModelConverterDescription provides behavior details, <code>false</code>
	 *         otherwise.
	 */
	boolean hasBehaviorDetails();

}
