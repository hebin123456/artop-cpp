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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptorProvider;

public class BasicTabDescriptorProvider implements ITabDescriptorProvider {

	/**
	 * Cache the last computed TabDescriptors.
	 */
	protected List<ITabDescriptor> currentTabDescriptors = new ArrayList<ITabDescriptor>();

	@Override
	public ITabDescriptor[] getTabDescriptors(IWorkbenchPart part, ISelection selection) {
		currentTabDescriptors.clear();
		createBasicTabDescriptors(selection);
		return currentTabDescriptors.toArray(new ITabDescriptor[currentTabDescriptors.size()]);
	}

	protected void createBasicTabDescriptors(Object selectedElement) {
		currentTabDescriptors.add(new BasicAdvancedTabDescriptor("basic.tabDesc", "Advanced", "Default")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	}
}
