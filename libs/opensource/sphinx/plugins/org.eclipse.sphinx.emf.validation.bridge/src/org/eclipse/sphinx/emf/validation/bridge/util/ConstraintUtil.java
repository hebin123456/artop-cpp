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
package org.eclipse.sphinx.emf.validation.bridge.util;

import java.util.HashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.emf.validation.bridge.extensions.RulesExtCache;
import org.eclipse.sphinx.emf.validation.bridge.extensions.RulesExtInternal;

public class ConstraintUtil {

	/**
	 * Return the rules filter used for the meta-model of the EObject class names name
	 * 
	 * @param eObject
	 *            the target {@link EObject}
	 * @return the filter, "" whether the connected meta-model has not been connected
	 */
	public static String getModelFilter(EObject eObject) {
		Assert.isNotNull(eObject);

		return getModelFilter(eObject.eClass().getInstanceClassName());
	}

	/**
	 * return the rules filter used for the meta-model of the class names name
	 * 
	 * @param name
	 *            the full InstanceClassName
	 * @return the filter, "" whether the connected meta-model has not been connected
	 */
	public static String getModelFilter(String name) {
		Assert.isNotNull(name);

		HashMap<String, RulesExtInternal> registry = RulesExtCache.getSingleton().getRulesExtInternals();

		String suffix = name.substring(0, name.indexOf('.'));
		final RulesExtInternal rei = registry.get(suffix);

		if (rei != null) {
			return rei.getFilter();
		} else {
			return ""; //$NON-NLS-1$
		}
	}

}
