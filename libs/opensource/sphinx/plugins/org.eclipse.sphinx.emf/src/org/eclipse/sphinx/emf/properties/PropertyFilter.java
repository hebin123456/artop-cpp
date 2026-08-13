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
package org.eclipse.sphinx.emf.properties;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EStructuralFeature;

public class PropertyFilter {

	protected Set<String> featureNames = new HashSet<String>();
	protected boolean accept;

	public PropertyFilter(boolean accept) {
		this.accept = accept;
	}

	public PropertyFilter(String[] featureNames, boolean accept) {
		this(Arrays.asList(featureNames), accept);
	}

	public PropertyFilter(List<String> featureNames, boolean accept) {
		if (featureNames != null) {
			this.featureNames.addAll(featureNames);
		}
		this.accept = accept;
	}

	public void setFeatureNames(String[] featureNames) {
		setFeatureNames(Arrays.asList(featureNames));
	}

	public void setFeatureNames(List<String> featureNames) {
		if (featureNames != null) {
			this.featureNames.addAll(featureNames);
		}
	}

	// FIXME Add support for filtering owner/feature pairs
	public boolean accept(Object owner, EStructuralFeature feature) {
		for (String featureName : featureNames) {
			if (featureName.equals(feature.getName())) {
				return accept;
			}
		}
		return !accept;
	}

}
