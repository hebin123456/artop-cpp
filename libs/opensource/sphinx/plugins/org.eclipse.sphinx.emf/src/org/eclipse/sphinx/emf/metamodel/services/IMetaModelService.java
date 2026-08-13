/**
 * <copyright>
 *
 * Copyright (c) 2010-2017 Continental Engineering Services, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     Continental Engineering Services - Initial API and implementation
 *     itemis - Moved from Artop to Sphinx, adapted method and variable naming to Sphinx conventions
 *     itemis - [458921] Newly introduced registries for metamodel serives, check validators and workflow contributors are not standalone-safe
 *     itemis - [501899] Use base index instead of IncQuery patterns
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.metamodel.services;

import java.util.Collection;

import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;

/**
 * A service that allows for generic operations on models but may be specifically implemented for one or several
 * metamodel(s).
 */
public interface IMetaModelService {

	/**
	 * Returns the {@link IMetaModelDescriptor descriptor}(s) of the metamodel(s) to which this
	 * {@link IMetaModelService} applies.
	 *
	 * @return The descriptor(s) of the applicable metamodel(s).
	 */
	Collection<IMetaModelDescriptor> getMetaModelDescriptors();
}
