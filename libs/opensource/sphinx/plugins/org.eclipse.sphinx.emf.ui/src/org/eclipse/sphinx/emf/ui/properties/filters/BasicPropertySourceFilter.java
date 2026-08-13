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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.ui.provider.PropertyDescriptor;
import org.eclipse.sphinx.emf.properties.PropertyFilter;
import org.eclipse.sphinx.emf.ui.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.ReflectUtil;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

public class BasicPropertySourceFilter implements IPropertySourceFilter {

	protected PropertyFilter propertyFilter;

	public BasicPropertySourceFilter(PropertyFilter propertyFilter) {
		this.propertyFilter = propertyFilter;
	}

	@Override
	public void setPropertyFilter(PropertyFilter propertyFilter) {
		this.propertyFilter = propertyFilter;
	}

	/*
	 * @see org.eclipse.sphinx.emf.explorer.properties.filters.IPropertySourceFilter#isFilterForObject(java.lang.Object)
	 */
	@Override
	public boolean isFilterForObject(Object object) {
		return true;
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.explorer.properties.IPropertySourceFilter#getAcceptedPropertyDescriptors(org.eclipse.ui
	 * .views .properties.IPropertySource)
	 */
	@Override
	public IPropertyDescriptor[] getAcceptedPropertyDescriptors(IPropertySource propertySource) {
		List<IPropertyDescriptor> acceptedDescriptors = new ArrayList<IPropertyDescriptor>();
		if (propertySource != null) {
			IPropertyDescriptor[] allDescriptors = propertySource.getPropertyDescriptors();
			Object owner = propertySource.getEditableValue();
			if (allDescriptors != null) {
				for (IPropertyDescriptor descriptor : allDescriptors) {
					if (accept(owner, descriptor)) {
						acceptedDescriptors.add(descriptor);
					}
				}
			}
		}
		return acceptedDescriptors.toArray(new IPropertyDescriptor[acceptedDescriptors.size()]);
	}

	@Override
	public boolean accept(Object owner, IPropertyDescriptor propertyDescriptor) {
		Assert.isTrue(propertyFilter != null);

		if (propertyDescriptor instanceof PropertyDescriptor) {
			try {
				IItemPropertyDescriptor itemPropertyDescriptor = (IItemPropertyDescriptor) ReflectUtil.getInvisibleFieldValue(propertyDescriptor,
						"itemPropertyDescriptor"); //$NON-NLS-1$
				Object feature = itemPropertyDescriptor.getFeature(owner);
				return feature instanceof EStructuralFeature && propertyFilter.accept(owner, (EStructuralFeature) feature);
			} catch (Exception ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			}
		}
		return true;
	}
}