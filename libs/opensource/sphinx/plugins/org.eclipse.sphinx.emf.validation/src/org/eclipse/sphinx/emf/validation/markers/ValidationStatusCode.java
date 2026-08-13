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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IStatus;

/**
 * Status code for validation
 */
public final class ValidationStatusCode {

	public static final int SEVERITY_OK = 0;

	public static final int SEVERITY_WARNING = 1;
	public static final int SEVERITY_LOCAL_WARNING = 2;
	public static final int SEVERITY_WARNING_ON_CHILDREN = 3;

	public static final int SEVERITY_ERROR = 4;
	public static final int SEVERITY_LOCAL_ERROR = 5;
	public static final int SEVERITY_ERROR_ON_CHILDREN = 6;

	/**
	 * convert Marker problem status to sphinx validation framework status
	 * 
	 * @param markerProblemCode
	 *            Severity code as stored into Marker
	 * @return severity code for the sphinx validation framework
	 * @see IMarker
	 */
	public static int convertMarkerToValidationStatusCode(int markerProblemCode) {

		int result = ValidationStatusCode.SEVERITY_OK;

		switch (markerProblemCode) {
		case IMarker.SEVERITY_ERROR:
			result = ValidationStatusCode.SEVERITY_ERROR;
			break;
		case IMarker.SEVERITY_WARNING:
			result = ValidationStatusCode.SEVERITY_WARNING;
			break;
		default: // we does not treat the INFO level
			result = ValidationStatusCode.SEVERITY_OK;
			break;
		}

		return result;
	}

	/**
	 * Convert a validation error code to an comprehensible string.
	 * 
	 * @param errorCode
	 * @return
	 */
	public static String convertErrorInt(int errorCode) {

		String result = ""; //$NON-NLS-1$

		switch (errorCode) {
		case IStatus.ERROR:
			result = "ERROR"; //$NON-NLS-1$
			break;
		case IStatus.WARNING:
			result = "WARNING"; //$NON-NLS-1$
			break;
		case IStatus.INFO:
			result = "INFO"; //$NON-NLS-1$
			break;
		case IStatus.OK:
			result = "OK"; //$NON-NLS-1$
			break;
		default: // we does not treat the INFO level
			result = "UNKNOWN"; //$NON-NLS-1$
			break;
		}

		return result;
	}
}
