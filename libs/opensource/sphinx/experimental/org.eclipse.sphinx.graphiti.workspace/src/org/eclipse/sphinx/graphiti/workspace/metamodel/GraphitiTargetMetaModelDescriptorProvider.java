/**
 * <copyright>
 * 
 * Copyright (c) 2008-2011 See4sys and others.
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
package org.eclipse.sphinx.graphiti.workspace.metamodel;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.ITargetMetaModelDescriptorProvider;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;

public class GraphitiTargetMetaModelDescriptorProvider implements ITargetMetaModelDescriptorProvider {

	String targetNamespaceExcludePatterns = "http://eclipse.org/graphiti/.*"; //$NON-NLS-1$

	@Override
	public IMetaModelDescriptor getDescriptor(IFile file) {
		try {
			String targetNamespace = EcorePlatformUtil.readTargetNamespace(file, targetNamespaceExcludePatterns);
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
		if (resource != null && !resource.getContents().isEmpty()) {
			EObject rootObject = resource.getContents().get(0);
			if (rootObject instanceof PictogramElement) {
				EObject bo = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement((PictogramElement) rootObject);
				if (bo != null) {
					return MetaModelDescriptorRegistry.INSTANCE.getDescriptor(bo);
				}
			}
			try {
				String targetNamespace = EcoreResourceUtil.readTargetNamespace(resource, targetNamespaceExcludePatterns);
				return MetaModelDescriptorRegistry.INSTANCE.getDescriptor(new URI(targetNamespace));
			} catch (URISyntaxException ex) {
				// Ignore exception, just return null
			}
		}
		return null;
	}
}
