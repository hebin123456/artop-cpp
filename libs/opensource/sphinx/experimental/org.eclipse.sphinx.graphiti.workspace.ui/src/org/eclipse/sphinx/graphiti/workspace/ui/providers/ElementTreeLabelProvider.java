/**
 * <copyright>
 * 
 * Copyright (c) 2008-2011 See4sys and others.
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
package org.eclipse.sphinx.graphiti.workspace.ui.providers;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.sphinx.emf.explorer.BasicExplorerLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class ElementTreeLabelProvider extends LabelProvider {
	ILabelProvider workbenchLabelProvider;
	ILabelProvider emfLabelProvider;

	public ElementTreeLabelProvider() {
		workbenchLabelProvider = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
		emfLabelProvider = new BasicExplorerLabelProvider();
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IResource) {
			return workbenchLabelProvider.getText(element);
		}
		return emfLabelProvider.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IResource) {
			return workbenchLabelProvider.getImage(element);
		}
		return emfLabelProvider.getImage(element);
	}
}
