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

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.xtend.typesystem.emf.EmfRegistryMetaModel;

/**
 * An EMF MetaModel that is based on the {@link EPackage}s of a configurable collection of {@link IMetaModelDescriptor
 * metamodel descriptor}s.
 */
public class ConfigurableEmfMetaModel extends EmfRegistryMetaModel {

	private Set<IMetaModelDescriptor> metaModelDescriptors = new HashSet<IMetaModelDescriptor>();

	private Collection<EPackage> allEPackages = null;

	/*
	 * !! Important Note !! This method must be named other to #addMetaModelDescriptor(IMetaModelDescriptor) so as to
	 * make sure that it can be found when ConfigurableEmfMetaModel is instantiated as bean in MWE workflows and
	 * initialized with metamodel descriptor identifiers.
	 */
	public void addMetaModelDescriptorId(String mmDescriptorId) {
		IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(mmDescriptorId);
		if (mmDescriptor != null) {
			metaModelDescriptors.add(mmDescriptor);
		}
	}

	public void addMetaModelDescriptor(IMetaModelDescriptor mmDescriptor) {
		if (mmDescriptor != null) {
			metaModelDescriptors.add(mmDescriptor);
		}
	}

	@Override
	protected EPackage[] allPackages() {
		if (allEPackages == null) {
			allEPackages = doAllPackages();
		}
		return allEPackages.toArray(new EPackage[allEPackages.size()]);
	}

	protected Collection<EPackage> doAllPackages() {
		Collection<EPackage> allEPackages = new HashSet<EPackage>();
		for (IMetaModelDescriptor mmDescriptor : metaModelDescriptors) {
			allEPackages.addAll(mmDescriptor.getEPackages());
		}
		return allEPackages;
	}
}
