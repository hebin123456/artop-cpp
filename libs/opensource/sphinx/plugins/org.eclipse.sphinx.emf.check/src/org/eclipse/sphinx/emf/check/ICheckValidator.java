/**
 * <copyright>
 *
 * Copyright (c) 2014-2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [473260] Progress indication of check framework
 *     itemis - [473261] Check Validation: Cancel button unresponsive
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;

public interface ICheckValidator extends EValidator {

	/**
	 * Specifies a {@link Set set} with the ids of the constraint categories to be used for validation. If not present,
	 * all categories are used.
	 */
	String OPTION_CATEGORIES = "CATEGORIES"; //$NON-NLS-1$

	/**
	 * Id of the special category "Other" that can be used to enable/disable all validators that are not associated with
	 * any category.
	 */
	String OPTION_CATEGORIES_OTHER_ID = "CATEGORIES_OTHER_ID"; //$NON-NLS-1$

	// TODO Think of introducing a special category representing all constraints
	// /**
	// * Id of the special category "All" that can be used to enable/disable all validators regardless of their
	// category.
	// */
	// String OPTION_CATEGORIES_ALL_ID = "CATEGORIES_ALL_ID"; //$NON-NLS-1$

	/**
	 * Specifies whether to validate the intrinsic EMF default constraints or not. The default value of this option is
	 * <code>Boolean.FALSE</code>.
	 */
	String OPTION_ENABLE_INTRINSIC_MODEL_INTEGRITY_CONSTRAINTS = "ENABLE_INTRINSIC_MODEL_INTEGRITY_CONSTRAINTS"; //$NON-NLS-1$

	/**
	 * Specifies the {@link IProgressMonitor progress monitor} to be used for monitoring the progress and allow for
	 * cancellation while an {@link EObject object} is being validated.
	 */
	String OPTION_PROGRESS_MONITOR = "PROGRESS_MONITOR"; //$NON-NLS-1$

	ThreadLocal<CheckValidatorState> getState();
}