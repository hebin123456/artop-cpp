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
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.metamodel;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * TargetMetaModelDescriptorProvider is in charge of providing a {@link IMetaModelDescriptor target meta model
 * descriptor }. To contribute a TargetMetaModelDescriptorProvider you need to add config element
 * "targetDescriptorProvider" to "/org.eclipse.sphinx.emf/schema/metaModelDescriptors.exsd" extension point. This config
 * element define the instance class of the provider , its id and the contentype and file extension for the one it is a
 * provider.
 */
public interface ITargetMetaModelDescriptorProvider {

	/**
	 * Return the target meta model descriptor for the {@link IFile file} provided as argument.
	 * 
	 * @param The
	 *            {@link IFile file} to dertermine wich target metamodel descriptor must be retrieve a .
	 * @return The {@link IMetaModelDescriptor target meta model descriptor } corresponding to the {@link IFile file}
	 *         given in argument.
	 */
	IMetaModelDescriptor getDescriptor(IFile file);

	/**
	 * Return the target meta model descriptor for the {@link Resource resource} provided as argument.
	 * 
	 * @param The
	 *            {@link Resource resource} to dertermine wich target metamodel descriptor must be retrieve a .
	 * @return The {@link IMetaModelDescriptor target meta model descriptor } corresponding to the {@link Resource
	 *         resource} given in argument.
	 */
	IMetaModelDescriptor getDescriptor(Resource resource);
}
