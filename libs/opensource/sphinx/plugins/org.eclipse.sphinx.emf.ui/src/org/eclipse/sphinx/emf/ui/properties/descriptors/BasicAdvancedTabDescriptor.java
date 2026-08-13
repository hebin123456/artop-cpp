/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [393477] Provider hook for unwrapping elements before letting BasicTabbedPropertySheetTitleProvider retrieve text or image for them
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.ui.properties.descriptors;

import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.ui.views.properties.tabbed.AbstractTabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISectionDescriptor;

public class BasicAdvancedTabDescriptor extends AbstractTabDescriptor {

	private String id;
	private String label;
	private String category;
	private AdapterFactory customAdapterFactory;

	public BasicAdvancedTabDescriptor(String id, String label, String category) {
		this(id, label, category, null);
	}

	public BasicAdvancedTabDescriptor(String id, String label, String category, AdapterFactory customAdapterFactory) {
		this.id = id;
		this.label = label;
		this.category = category;
		this.customAdapterFactory = customAdapterFactory;

		@SuppressWarnings("unchecked")
		List<ISectionDescriptor> sectionDescriptors = getSectionDescriptors();
		sectionDescriptors.add(new BasicAdvancedSectionDescriptor(id + ".sectionDesc", id, customAdapterFactory)); //$NON-NLS-1$
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getCategory() {
		return category;
	}

	public AdapterFactory getCustomAdapterFactory() {
		return customAdapterFactory;
	}
}
