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
package org.eclipse.sphinx.emf.validation.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.emf.validation.internal.util.XmlConstraintDescriptor;
import org.eclipse.emf.validation.service.ConstraintRegistry;
import org.eclipse.emf.validation.service.IConstraintDescriptor;
import org.eclipse.emf.validation.util.XmlConfig;

/**
 * This utility class give some method in order to get data from the extension point definition
 */
@SuppressWarnings("restriction")
public class ConstraintExtensionUtil {

	/**
	 * the constraint registry, useful
	 */
	private static ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

	/**
	 * Return the {@link XmlConstraintDescriptor} for the rule ruleID.
	 * 
	 * @param ruleID
	 *            the rule identifier
	 * @return XmlConstraintDescriptor if the ruleID match a rule, null otherwise
	 */
	public static XmlConstraintDescriptor getXmlConstraintDescriptor(String ruleId) {
		Assert.isNotNull(ruleId);

		IConstraintDescriptor icd = constraintRegistry.getDescriptor(ruleId);

		return icd != null && icd instanceof XmlConstraintDescriptor ? (XmlConstraintDescriptor) icd : null;
	}

	/**
	 * Return for the rule ruleId a association between target and features registered into the extension point
	 * constraint.
	 * 
	 * @param ruleId
	 * @return that: Map<String, Set<String>> where String is the name of the target class and the set is composed of
	 *         the the target features.
	 */
	public static Map<String, Set<String>> getRulesFeatures(String ruleId) {
		Assert.isNotNull(ruleId);

		Map<String, Set<String>> result = new HashMap<String, Set<String>>();

		XmlConstraintDescriptor xcd = getXmlConstraintDescriptor(ruleId);

		if (xcd != null) {
			// All the target node
			IConfigurationElement targets[] = xcd.getConfig().getChildren(XmlConfig.E_TARGET);

			for (IConfigurationElement ice_target : targets) {
				String tgt = ice_target.getAttribute(XmlConfig.A_CLASS);
				Set<String> features = new HashSet<String>();
				for (IConfigurationElement ice_event : ice_target.getChildren(XmlConfig.E_EVENT)) {
					for (IConfigurationElement ice_feature : ice_event.getChildren(XmlConfig.E_FEATURE)) {
						features.add(ice_feature.getAttribute(XmlConfig.A_NAME));
					}
				}
				result.put(tgt, features);
			}

		}

		return result;
	}

	/**
	 * return a {@link Set} of value for the parameter parameterId from the rule ruleID
	 * 
	 * @param ruleID
	 * @param parameterID
	 * @return
	 */
	public static Set<String> getParamOfType(String ruleId, String parameterID) {
		Assert.isNotNull(ruleId);

		Set<String> result = new HashSet<String>();

		XmlConstraintDescriptor xcd = getXmlConstraintDescriptor(ruleId);

		if (xcd != null) {
			// All the param node
			IConfigurationElement targets[] = xcd.getConfig().getChildren(XmlConfig.E_PARAM);
			String value = null;
			for (IConfigurationElement target : targets) {
				if (target.getAttribute(XmlConfig.A_NAME).equals(parameterID)) {
					value = target.getAttribute(XmlConfig.A_VALUE);
					if (value != null && value.length() > 0) {
						result.add(value);
					}
				}
			}
		}

		return result;
	}
}
