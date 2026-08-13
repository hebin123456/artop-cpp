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
package org.eclipse.sphinx.emf.ui.properties.filters;

import org.eclipse.sphinx.emf.properties.PropertyFilter;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

public interface IPropertySourceFilter {

	void setPropertyFilter(PropertyFilter propertyFilter);

	/**
	 * Returns whether this IPropertySourceFilter is applicable to the given object. In general, an
	 * IPropertySourceFilter is used for all objects instantiated from a given meta model.
	 * 
	 * @param object
	 *            the object.
	 * @return whether this IPropertySourceFilter is applicable to the given object
	 */
	boolean isFilterForObject(Object object);

	IPropertyDescriptor[] getAcceptedPropertyDescriptors(IPropertySource propertySource);

	boolean accept(Object owner, IPropertyDescriptor propertyDescriptor);
}