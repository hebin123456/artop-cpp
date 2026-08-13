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

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;

/**
 * XMLCommentTargetMetaModelDescriptorProvider is based on XML comment implementation for retrieving the target
 * MetaModelDescriptor. The extension model must provide a ResourceImpl which implement the method createSave(), in
 * order to return an instance of TargetMetaModelDescriptorProvidingXMLSaveImpl.
 * TargetMetaModelDescriptorProvidingXMLSaveImpl put in place the comment containing TargetMetaModelDescriptor id while
 * saving the extension resource.
 */
public class XMLCommentTargetMetaModelDescriptorProvider implements ITargetMetaModelDescriptorProvider {

	private final static String TARGET_METAMODEL_DESCRIPTOR_COMMENT_PREFIX = ExtendedResource.OPTION_TARGET_METAMODEL_DESCRIPTOR_ID + "="; //$NON-NLS-1$

	public static class TargetMetaModelDescriptorProvidingXMLSaveImpl extends XMLSaveImpl {

		String targetMetaModelDescriptorId = null;

		public TargetMetaModelDescriptorProvidingXMLSaveImpl(XMLHelper helper) {
			super(helper);
		}

		@Override
		protected void init(XMLResource resource, Map<?, ?> options) {
			if (options != null) {
				targetMetaModelDescriptorId = (String) options.get(ExtendedResource.OPTION_TARGET_METAMODEL_DESCRIPTOR_ID);
			}

			super.init(resource, options);
		}

		@Override
		protected Object writeTopObject(EObject top) {
			// Add comment with target meta-model descriptor from options
			if (targetMetaModelDescriptorId != null) {
				String comment = createTargetMetaModelDescriptorComment(targetMetaModelDescriptorId);
				if (!toDOM) {
					doc.addComment(comment);
				} else {
					currentNode.appendChild(document.createComment(comment));
				}
			}
			return super.writeTopObject(top);
		}
	}

	private static String createTargetMetaModelDescriptorComment(String targetMetaModelDescriptorId) {
		Assert.isNotNull(targetMetaModelDescriptorId);

		return TARGET_METAMODEL_DESCRIPTOR_COMMENT_PREFIX + targetMetaModelDescriptorId;
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.metamodel.ITargetMetaModelDescriptorProvider#getDescriptor(org.eclipse.core.resources.
	 * IFile)
	 */
	@Override
	public IMetaModelDescriptor getDescriptor(IFile file) {
		if (file != null) {
			Collection<String> comments = EcorePlatformUtil.readRootElementComments(file);
			for (String comment : comments) {
				IMetaModelDescriptor descriptor = parseTargetMetaModelDescriptor(comment);
				if (descriptor != null) {
					return descriptor;
				}
			}
		}
		return null;
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.metamodel.ITargetMetaModelDescriptorProvider#getDescriptor(org.eclipse.emf.ecore.resource
	 * .Resource )
	 */
	@Override
	public IMetaModelDescriptor getDescriptor(Resource resource) {
		IFile file = EcorePlatformUtil.getFile(resource);
		return getDescriptor(file);
	}

	private IMetaModelDescriptor parseTargetMetaModelDescriptor(String comment) {
		Assert.isNotNull(comment);

		if (comment.contains(TARGET_METAMODEL_DESCRIPTOR_COMMENT_PREFIX)) {
			comment = comment.trim();
			if (comment.length() > TARGET_METAMODEL_DESCRIPTOR_COMMENT_PREFIX.length()) {
				String targetMetaModelDescriptorId = comment.substring(TARGET_METAMODEL_DESCRIPTOR_COMMENT_PREFIX.length());
				return MetaModelDescriptorRegistry.INSTANCE.getDescriptor(targetMetaModelDescriptorId);
			}
		}
		return null;
	}
}
