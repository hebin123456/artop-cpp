/**
 * <copyright>
 *
 * Copyright (c) 2017 itemis and others.
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
package org.eclipse.sphinx.emf.metamodel.services;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;

public class AbstractMetaModelService implements IMetaModelService {

	private Collection<IMetaModelDescriptor> mmDescriptors;

	public AbstractMetaModelService(Collection<IMetaModelDescriptor> mmDescriptors) {
		Assert.isNotNull(mmDescriptors);
		this.mmDescriptors = mmDescriptors;
	}

	/*
	 * @see org.eclipse.sphinx.emf.metamodel.services.IMetaModelService#getMetaModelDescriptors()
	 */
	@Override
	public Collection<IMetaModelDescriptor> getMetaModelDescriptors() {
		return mmDescriptors;
	}
}
