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
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.custom;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.util.ExampleUtil;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;

public class RenameComponentTypeFeature extends AbstractCustomFeature {

	private boolean hasDoneChanges = false;
	private static final String CUSTOM_FEATURE_NAME = "Rename ComponentType"; //$NON-NLS-1$
	private static final String CUSTOM_FEATURE_DESCRIPTION = "Change the name of the ComponentType"; //$NON-NLS-1$

	public RenameComponentTypeFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public String getName() {
		return CUSTOM_FEATURE_NAME;
	}

	@Override
	public String getDescription() {
		return CUSTOM_FEATURE_DESCRIPTION;
	}

	@Override
	public boolean canExecute(ICustomContext context) {
		// allow rename if exactly one pictogram element representing a ComponentType is selected
		boolean ret = false;
		PictogramElement[] pes = context.getPictogramElements();
		if (pes != null && pes.length == 1) {
			Object bo = getBusinessObjectForPictogramElement(pes[0]);
			if (bo instanceof ComponentType) {
				ret = true;
			}
		}
		return ret;
	}

	@Override
	public boolean hasDoneChanges() {
		return hasDoneChanges;
	}

	@Override
	public void execute(ICustomContext context) {
		PictogramElement[] pes = context.getPictogramElements();
		if (pes != null && pes.length == 1) {
			Object bo = getBusinessObjectForPictogramElement(pes[0]);
			if (bo instanceof ComponentType) {
				ComponentType eComponentType = (ComponentType) bo;
				String currentName = eComponentType.getName();
				// ask user for a new class name
				String newName = ExampleUtil.askString(getName(), getDescription(), currentName);
				if (newName != null && !newName.equals(currentName)) {
					hasDoneChanges = true;
					eComponentType.setName(newName);
					updatePictogramElement(pes[0]);
				}
			}
		}
	}
}