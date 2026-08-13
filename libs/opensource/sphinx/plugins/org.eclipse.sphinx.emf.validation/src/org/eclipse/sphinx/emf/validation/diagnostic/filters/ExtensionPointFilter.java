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
package org.eclipse.sphinx.emf.validation.diagnostic.filters;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.service.IConstraintDescriptor;
import org.eclipse.emf.validation.service.IConstraintFilter;
import org.eclipse.sphinx.emf.validation.diagnostic.filters.util.ConstraintFilterValue;
import org.eclipse.sphinx.emf.validation.diagnostic.filters.util.ConstraintFilteringUtil;

public class ExtensionPointFilter implements IConstraintFilter {

	private Set<ConstraintFilterValue> cfvs = null;

	public ExtensionPointFilter(Set<ConstraintFilterValue> cfvs) {
		this.cfvs = cfvs;
	}

	public ExtensionPointFilter(ConstraintFilterValue cfv) {
		cfvs = new HashSet<ConstraintFilterValue>();
		cfvs.add(cfv);
	}

	@Override
	public boolean accept(IConstraintDescriptor constraint, EObject target) {

		if (cfvs == null) {
			return true;
		}

		for (ConstraintFilterValue current : ConstraintFilteringUtil.getFilteringParameterEnum(constraint.getId())) {
			if (cfvs.contains(current)) {
				return false;
			}
		}

		return true;
	}
}
