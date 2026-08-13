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

public class RuleIdFilter implements IConstraintFilter {

	private Set<String> ruleIds = null;

	public RuleIdFilter(String ruleId) {
		ruleIds = new HashSet<String>();
		ruleIds.add(ruleId);
	}

	public RuleIdFilter(Set<String> ruleIds) {
		this.ruleIds = ruleIds;
	}

	@Override
	public boolean accept(IConstraintDescriptor constraint, EObject target) {
		if (ruleIds == null) {
			return true;
		}
		return ruleIds.contains(constraint.getId()) ? true : false;
	}
}
