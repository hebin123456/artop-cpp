/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.ui.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public class RetrieveNameAttributeHelper {

	private static final String NAME = "name"; //$NON-NLS-1$
	private static final String ID = "id"; //$NON-NLS-1$

	private Map<EClass, NameAttributeInfo> nameAttributeCache = new WeakHashMap<EClass, NameAttributeInfo>();

	/**
	 * Retrieves the name attribute for the given model object. That name attribute may differ from one meta-model to
	 * another; this method contains a default behaviour allowing to retrieve that name attribute event though.
	 *
	 * @param object
	 *            The model element whose name attribute must be returned.
	 * @return The name attribute of the specified model element.
	 */
	public EAttribute getNameAttribute(EObject object) {
		List<EClass> eTypes = new ArrayList<EClass>();
		eTypes.add(object.eClass());
		eTypes.addAll(object.eClass().getEAllSuperTypes());
		for (EClass eType : eTypes) {
			if (nameAttributeCache.containsKey(eType)) {
				NameAttributeInfo info = nameAttributeCache.get(eType);
				if (!info.isFallback()) {
					return info.getEAttribute();
				}
			}
		}

		NameAttributeInfo info = getNameAttribute(object.eClass().getEAllAttributes());
		if (info != null) {
			if (!nameAttributeCache.containsKey(info.getEAttribute().getEContainingClass())) {
				nameAttributeCache.put(info.getEAttribute().getEContainingClass(), info);
			}
			return info.getEAttribute();
		}
		return null;
	}

	protected NameAttributeInfo getNameAttribute(List<EAttribute> nameAttributeCandidates) {
		EAttribute nameAttribute = null;
		boolean isFallback = true;
		for (EAttribute attribute : nameAttributeCandidates) {
			String attributeName = attribute.getName();
			if (attributeName != null) {
				if (attributeName.equalsIgnoreCase(NAME)) {
					nameAttribute = attribute;
					isFallback = false;
				} else if (attributeName.equalsIgnoreCase(ID)) {
					if (nameAttribute == null || !nameAttribute.getName().toLowerCase().endsWith(NAME)) {
						nameAttribute = attribute;
						isFallback = false;
					}
				} else if (attributeName.toLowerCase().endsWith(NAME)) {
					if (nameAttribute == null || !nameAttribute.getName().toLowerCase().endsWith(NAME)
							&& !nameAttribute.getName().equalsIgnoreCase(ID)) {
						nameAttribute = attribute;
						isFallback = false;
					}
				} else if (attributeName.toLowerCase().indexOf(NAME) != -1) {
					if (nameAttribute == null || nameAttribute.getName().toLowerCase().indexOf(NAME) == -1
							&& !nameAttribute.getName().equalsIgnoreCase(ID)) {
						nameAttribute = attribute;
						isFallback = false;
					}
				} else if (nameAttribute == null) {
					nameAttribute = attribute;
				}
			}
		}
		return nameAttribute != null ? new NameAttributeInfo(nameAttribute, isFallback) : null;
	}

	public boolean hasNameAttribute(EObject object) {
		return getNameAttribute(object) != null;
	}

	protected EAttribute cacheAttribute(NameAttributeInfo nameAttribute) {
		if (!nameAttributeCache.containsKey(nameAttribute.getEAttribute().getEContainingClass())) {
			nameAttributeCache.put(nameAttribute.getEAttribute().getEContainingClass(), nameAttribute);
		}
		return nameAttribute.getEAttribute();
	}

	private class NameAttributeInfo {

		private WeakReference<EAttribute> eAttribute;
		private boolean isFallback;

		public NameAttributeInfo(EAttribute attr, boolean isFallback) {
			Assert.isNotNull(attr);
			eAttribute = new WeakReference<EAttribute>(attr);
			this.isFallback = isFallback;
		}

		public EAttribute getEAttribute() {
			return eAttribute.get();
		}

		public boolean isFallback() {
			return isFallback;
		}
	}
}
