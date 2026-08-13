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
package org.eclipse.sphinx.gmf.workspace.metamodel;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.ITargetMetaModelDescriptorProvider;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;

public class GMFTargetMetaModelDescriptorProvider implements ITargetMetaModelDescriptorProvider {

	@Override
	public IMetaModelDescriptor getDescriptor(IFile file) {
		try {
			String targetNamespace = EcorePlatformUtil.readTargetNamespace(file);
			if (targetNamespace != null) {
				return MetaModelDescriptorRegistry.INSTANCE.getDescriptor(new URI(targetNamespace));
			}
		} catch (URISyntaxException ex) {
			// Ignore exception, just return null
		}
		return null;
	}

	@Override
	public IMetaModelDescriptor getDescriptor(Resource resource) {
		try {
			String targetNamespace = EcoreResourceUtil.readTargetNamespace(resource);
			if (targetNamespace != null) {
				return MetaModelDescriptorRegistry.INSTANCE.getDescriptor(new URI(targetNamespace));
			}
		} catch (URISyntaxException ex) {
			// Ignore exception, just return null
		}
		return null;
	}
}
