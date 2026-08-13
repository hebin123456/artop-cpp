/**
 * <copyright>
 *
 * Copyright (c) 2010-2015 BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     BMW Car IT - Initial API and implementation
 *     itemis - Moved from Artop to Sphinx, adapted method and variable naming to Sphinx conventions
 *     itemis - [458921] Newly introduced registries for metamodel serives, check validators and workflow contributors are not standalone-safe
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.metamodel.services;

import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.providers.IMetaModelDescriptorProvider;

/**
 * A plug-in extension registry based {@link IMetaModelServiceProvider}.
 */
public class DefaultMetaModelServiceProvider implements IMetaModelServiceProvider {

	@Override
	public <T extends IMetaModelService> T getService(IMetaModelDescriptor mmDescriptor, Class<T> mmServiceType) {
		return MetaModelServiceRegistry.INSTANCE.getService(mmDescriptor, mmServiceType);
	}

	@Override
	public <T extends IMetaModelService> T getService(IMetaModelDescriptorProvider mmDesciptorProvider, Class<T> mmServiceType) {
		return getService(mmDesciptorProvider.getMetaModelDescriptor(), mmServiceType);
	}
}
