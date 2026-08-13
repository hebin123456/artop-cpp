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
package org.eclipse.sphinx.emf.validation.diagnostic.filters.util;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.sphinx.emf.validation.util.ConstraintExtensionUtil;

/**
 * This utility class allow
 */
public class ConstraintFilteringUtil {

	/**
	 * the parameter name (check constraint extension point definition)
	 */
	static public final String FILTER_NOT_VALIDATE_ON_VALUE = "DISABLE_CONSTRAINT_ON"; //$NON-NLS-1$

	/**
	 * return a {@link Set} of filtering value from the extension point definition of the constraint ruleId
	 * 
	 * @param ruleID
	 * @return
	 */
	static public Set<String> getFilteringParameter(String ruleId) {
		Assert.isNotNull(ruleId);

		return ConstraintExtensionUtil.getParamOfType(ruleId, FILTER_NOT_VALIDATE_ON_VALUE);

	}

	/**
	 * return a {@link Set} of filtering value from the extension point definition of the constraint ruleId
	 * 
	 * @param ruleID
	 * @return
	 */
	static public Set<ConstraintFilterValue> getFilteringParameterEnum(String ruleId) {
		Assert.isNotNull(ruleId);

		Set<ConstraintFilterValue> filters = new HashSet<ConstraintFilterValue>();

		Set<String> params = getFilteringParameter(ruleId);

		for (String current : params) {
			ConstraintFilterValue cfv = ConstraintFilterValue.convert(current);
			if (cfv != null) {
				filters.add(cfv);
			}
		}

		return filters;

	}
}
