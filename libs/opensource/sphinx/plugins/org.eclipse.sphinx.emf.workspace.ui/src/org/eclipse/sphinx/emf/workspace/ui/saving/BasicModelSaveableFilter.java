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

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.ui.ISaveableFilter;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.Saveable;

/**
 * A basic implementation of model saveable filter.
 * <p>
 * This filter only selects saveables:
 * <ul>
 * <li>that are instances of {@link IBasicModelSaveable};</li>
 * <li>whose model identifier has the specified project as model project.</li>
 * </ul>
 */
public class BasicModelSaveableFilter implements ISaveableFilter {

	/**
	 * The project used as a filtering criteria.
	 */
	protected Collection<IModelDescriptor> modelDescriptors = new HashSet<IModelDescriptor>();

	/**
	 * Constructor.
	 *
	 * @param project
	 *            The project to use as a filtering criterion.
	 */
	public BasicModelSaveableFilter(IProject project) {
		Assert.isNotNull(project);
		modelDescriptors.addAll(ModelDescriptorRegistry.INSTANCE.getModels(project));
	}

	public BasicModelSaveableFilter(IProject project, IMetaModelDescriptor mmFilter) {
		Assert.isNotNull(project);
		modelDescriptors.addAll(ModelDescriptorRegistry.INSTANCE.getModels(project, mmFilter));
	}

	/**
	 * Constructor.
	 *
	 * @param projects
	 *            The projects to use as a filtering criterion.
	 */
	public BasicModelSaveableFilter(Collection<IProject> projects) {
		Assert.isNotNull(projects);
		for (IProject project : projects) {
			modelDescriptors.addAll(ModelDescriptorRegistry.INSTANCE.getModels(project));
		}

	}

	public BasicModelSaveableFilter(Collection<IProject> projects, IMetaModelDescriptor mmFilter) {
		Assert.isNotNull(projects);
		for (IProject project : projects) {
			modelDescriptors.addAll(ModelDescriptorRegistry.INSTANCE.getModels(project, mmFilter));
		}
	}

	/*
	 * @see org.eclipse.ui.ISaveableFilter#select(org.eclipse.ui.Saveable, org.eclipse.ui.IWorkbenchPart[])
	 */
	@Override
	public boolean select(Saveable saveable, IWorkbenchPart[] containingParts) {
		if (saveable instanceof IBasicModelSaveable) {
			IModelDescriptor modelDescriptor = ((IBasicModelSaveable) saveable).getModelDescriptor();
			if (modelDescriptors.contains(modelDescriptor)) {
				return true;
			}
		}
		return false;
	}
}
