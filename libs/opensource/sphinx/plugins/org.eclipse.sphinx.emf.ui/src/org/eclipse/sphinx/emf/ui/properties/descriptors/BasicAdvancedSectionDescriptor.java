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
package org.eclipse.sphinx.emf.ui.properties.descriptors;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.ui.properties.BasicTransactionalAdvancedPropertySection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.ISectionDescriptor;

public class BasicAdvancedSectionDescriptor extends AbstractSectionDescriptor {

	private String id;
	private String targetTab;
	private AdapterFactory customAdapterFactory;
	private int ENABLES_FOR_ONE = 1;

	public BasicAdvancedSectionDescriptor(String id, String targetTab) {
		this(id, targetTab, null);
	}

	public BasicAdvancedSectionDescriptor(String id, String targetTab, AdapterFactory customAdapterFactory) {
		this.id = id;
		this.targetTab = targetTab;
		this.customAdapterFactory = customAdapterFactory;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getTargetTab() {
		return targetTab;
	}

	public AdapterFactory getCustomAdapterFactory() {
		return customAdapterFactory;
	}

	@Override
	public int getEnablesFor() {
		return ENABLES_FOR_ONE;
	}

	@Override
	public ISection getSectionClass() {
		return new BasicTransactionalAdvancedPropertySection() {
			@Override
			protected AdapterFactory getCustomAdapterFactory() {
				return customAdapterFactory;
			}
		};
	}

	@Override
	public boolean appliesTo(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection && selection.isEmpty() == false) {
			if (getEnablesFor() != ISectionDescriptor.ENABLES_FOR_ANY && ((IStructuredSelection) selection).size() != getEnablesFor()) {
				// #getEnablesFor() does not match the size of the selection, do not display section
				return false;
			}
		}
		return true;
	}
}
