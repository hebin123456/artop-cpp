/**
 * <copyright>
 * 
 * Copyright (c) 2012 itemis and others.
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
package org.eclipse.sphinx.graphiti.workspace.ui.util;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;

public class PropertyUtil {

	public static final String SHAPE_KEY = "reference"; //$NON-NLS-1$

	// Store the name of the EReference in the shape representing a connection
	public static final void setReferenceName(PictogramElement pe, EReference referenceId) {
		String referenceName = referenceId.getName();
		Graphiti.getPeService().setPropertyValue(pe, SHAPE_KEY, referenceName);
	}

	public static String getReferenceName(PictogramElement pe) {
		return Graphiti.getPeService().getPropertyValue(pe, SHAPE_KEY);
	}

}
