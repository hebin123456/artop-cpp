/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     BMW Car IT - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.metamodel.providers;

import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;

/**
 * A provider for {@link IMetaModelDescriptor}.
 */
public interface IMetaModelDescriptorProvider {

	/**
	 * Returns a metamodel descriptor.
	 * 
	 * @return the metamodel descriptor
	 */
	IMetaModelDescriptor getMetaModelDescriptor();

}
