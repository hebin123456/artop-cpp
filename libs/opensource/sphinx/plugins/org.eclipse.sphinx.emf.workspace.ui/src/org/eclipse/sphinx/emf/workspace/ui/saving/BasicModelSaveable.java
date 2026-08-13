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
 *     Elektrobit - [572592] Prompt for model saving on project close
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.saving;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.ui.Saveable;

/**
 * Basic implementation of saveable for model.
 */
public class BasicModelSaveable extends Saveable implements IBasicModelSaveable {

	protected static final String SAVEABLE_LABEL_PREFIX = "saveableFor"; //$NON-NLS-1$

	/**
	 * The identifier of the model this saveable is associated to.
	 */
	private IModelDescriptor modelDescriptor;

	/**
	 * Constructor.
	 *
	 * @param modelDescriptor
	 *            The descriptor of the model to use in order to correctly associate this saveable to a specific model
	 *            (cannot be null).
	 */
	public BasicModelSaveable(IModelDescriptor modelDescriptor) {
		Assert.isNotNull(modelDescriptor);
		this.modelDescriptor = modelDescriptor;
	}

	/**
	 * @return The descriptor of the model this saveable is associated to.
	 */
	@Override
	public final IModelDescriptor getModelDescriptor() {
		return modelDescriptor;
	}

	/*
	 * @see org.eclipse.ui.Saveable#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return ModelSaveManager.INSTANCE.isDirty(modelDescriptor);
	}

	/*
	 * @see org.eclipse.ui.Saveable#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(final IProgressMonitor monitor) throws CoreException {
		ModelSaveManager.INSTANCE.saveModel(modelDescriptor, false, monitor);
	}

	/*
	 * @see org.eclipse.ui.Saveable#getName()
	 */
	@Override
	public String getName() {
		StringBuilder resourcesNames = new StringBuilder();
		resourcesNames.append(modelDescriptor.getRoot().getName());
		for (IResource resource : modelDescriptor.getReferencedRoots()) {
			resourcesNames.append(" + "); //$NON-NLS-1$
			resourcesNames.append(resource.getName());
		}
		return resourcesNames.toString();
	}

	/*
	 * @see org.eclipse.ui.Saveable#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/*
	 * @see org.eclipse.ui.Saveable#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		return modelDescriptor.getMetaModelDescriptor().getName();
	}

	/*
	 * @see org.eclipse.ui.Saveable#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (object instanceof BasicModelSaveable) {
			return modelDescriptor.equals(((BasicModelSaveable) object).modelDescriptor);
		}
		return false;
	}

	/*
	 * @see org.eclipse.ui.Saveable#hashCode()
	 */
	@Override
	public int hashCode() {
		return modelDescriptor.hashCode();
	}

	@Override
	public String toString() {
		return SAVEABLE_LABEL_PREFIX + "_" + modelDescriptor.toString(); //$NON-NLS-1$
	}
}