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
package org.eclipse.sphinx.emf.validation.markers.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.service.ConstraintRegistry;
import org.eclipse.emf.validation.service.IConstraintDescriptor;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.validation.constraint.IExtendedConstraintDescriptor;
import org.eclipse.sphinx.emf.validation.markers.IValidationMarker;
import org.eclipse.sphinx.emf.validation.util.ConstraintExtensionUtil;

/**
 * Utility class in order to manage the {@link IValidationMarker#FEATURES_ATTRIBUTE} attribute for validation marker.
 */
public class FeatureAttUtil {

	/**
	 * internal separator for the
	 */
	private static String separator = "::"; //$NON-NLS-1$

	/**
	 * Tools in order to pack target features into marker attribute {@link IValidationMarker#EOBJECT_ATTRIBUTE}
	 * 
	 * @param marker
	 *            the target marker
	 * @param features
	 *            set of String
	 */
	public static void packFeatures(IMarker marker, Set<String> features) throws CoreException {
		Assert.isNotNull(marker);
		Assert.isNotNull(features);

		if (features.size() < 1) {
			return;
		}

		String res = ""; //$NON-NLS-1$
		for (String feature : features) {
			res += feature + separator;
		}

		marker.setAttribute(IValidationMarker.FEATURES_ATTRIBUTE, res.substring(0, res.length() - 2));

	}

	/**
	 * Tools in order to pack target features into String as required for the marker attribute
	 * {@link IValidationMarker#EOBJECT_ATTRIBUTE}
	 * 
	 * @param marker
	 *            the target marker
	 * @param features
	 *            set of String
	 */
	public static String packFeaturesAsString(IMarker marker, Set<String> features) throws CoreException {
		Assert.isNotNull(marker);
		Assert.isNotNull(features);

		if (features.size() < 1) {
			return null;
		}

		String res = ""; //$NON-NLS-1$
		for (String feature : features) {
			res += feature + separator;
		}

		return res.substring(0, res.length() - 2);

	}

	/**
	 * Tools in order to unpack target features from the marker attribute {@link IValidationMarker#EOBJECT_ATTRIBUTE}
	 * 
	 * @param marker
	 *            the target marker
	 * @return a Set of features name if the attribute was found/populated, an empty Set otherwise
	 * @throws CoreException
	 */
	public static Set<String> unpackFeatures(IMarker marker) throws CoreException {
		Assert.isNotNull(marker);

		Object obj = marker.getAttribute(IValidationMarker.FEATURES_ATTRIBUTE);

		Set<String> result = new HashSet<String>();

		if (obj != null && obj instanceof String) {
			for (String feature : ((String) obj).split(separator)) {
				result.add(feature);
			}
		}

		return result;
	}

	/**
	 * get the set of features whose match the target EObject
	 * 
	 * @param ruleId
	 *            the ruleID
	 * @param eObject
	 *            the target eObject
	 * @return a Set of features name whose match the target EObject, an empty Set otherwise
	 */
	public static Set<String> getRulesFeaturesForEObj(String ruleId, EObject eObject) {

		Set<String> result = new HashSet<String>();

		IConstraintDescriptor icd = ConstraintRegistry.getInstance().getDescriptor(ruleId);
		if (icd != null && icd instanceof IExtendedConstraintDescriptor) {
			for (String current : ((IExtendedConstraintDescriptor) icd).getFeatures()) {
				result.add(current);
			}
			return result;
		}

		// Let's obtain all the (key/value) for this rule
		Map<String, Set<String>> kf = ConstraintExtensionUtil.getRulesFeatures(ruleId);

		for (String key : kf.keySet()) {
			if (EObjectUtil.isAssignableFrom(eObject.eClass(), key)) {
				result.addAll(kf.get(key));
			}
		}

		return result;
	}
}
