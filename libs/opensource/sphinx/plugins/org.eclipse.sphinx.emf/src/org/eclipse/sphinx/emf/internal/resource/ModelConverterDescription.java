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
package org.eclipse.sphinx.emf.internal.resource;

import org.eclipse.sphinx.emf.resource.IModelConverterDescription;

/**
 * A simple default implementation of the IModelConverterDescription.
 */
public class ModelConverterDescription implements IModelConverterDescription {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String NO_DETAILS_AVAILABLE_TEXT = "No details on the model converter's behavior is provided.";
	private String fBehaviorDetails;
	private String fWarning;

	/**
	 * Creates a new ModelConverterDescription instance.
	 * 
	 * @param behaviorDetails
	 *            Detailed information on the behavior of the IModelConverter this ModelConverterDescription describes.
	 * @param warning
	 *            A warning information which goes along with the IModelConverter this ModelConverterDescription
	 *            describes.
	 */
	public ModelConverterDescription(String behaviorDetails, String warning) {
		fBehaviorDetails = behaviorDetails;
		fWarning = warning;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBehaviorDetails() {
		if (fBehaviorDetails == null) {
			return NO_DETAILS_AVAILABLE_TEXT;
		}
		return fBehaviorDetails;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getWarning() {
		return fWarning;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return !hasWarning() && !hasBehaviorDetails();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasBehaviorDetails() {
		return !isEmpty(getBehaviorDetails());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasWarning() {
		return !isEmpty(getWarning());
	}

	private boolean isEmpty(String string) {
		if (string == null) {
			return true;
		}
		return string.trim().equals(EMPTY_STRING);
	}

}
