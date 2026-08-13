/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.xtend.typesystem.emf;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;

/**
 * An EMF MetaModel that is based on the {@link EPackage}s behind a given {@link IModelDescriptor context model} or the
 * {@link IModelDescriptor model}s in a given {@link IProject context project}.
 */
// TODO Rename to WorkspaceEmfMetaModel
public class SphinxManagedEmfMetaModel extends ConfigurableEmfMetaModel {

	protected IProject contextProject = null;
	protected IModelDescriptor contextModel = null;

	public SphinxManagedEmfMetaModel(IProject contextProject) {
		this.contextProject = contextProject;
	}

	public SphinxManagedEmfMetaModel(IModelDescriptor contextModel) {
		this.contextModel = contextModel;
	}

	protected Collection<IModelDescriptor> getModelsInScope() {
		Set<IModelDescriptor> modelsInScope = new HashSet<IModelDescriptor>();
		if (contextModel != null) {
			modelsInScope.add(contextModel);
		} else if (contextProject != null) {
			modelsInScope.addAll(ModelDescriptorRegistry.INSTANCE.getModels(contextProject));
		}
		return modelsInScope;
	}

	@Override
	protected Collection<EPackage> doAllPackages() {
		for (IModelDescriptor modelDescriptor : getModelsInScope()) {
			addMetaModelDescriptor(modelDescriptor.getMetaModelDescriptor());
		}
		return super.doAllPackages();
	}
}
