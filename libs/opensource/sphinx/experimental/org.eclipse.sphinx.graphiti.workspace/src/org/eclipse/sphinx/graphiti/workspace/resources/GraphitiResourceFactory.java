/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - 
 * 
 * </copyright>
 */
package org.eclipse.sphinx.graphiti.workspace.resources;

import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

public class GraphitiResourceFactory extends XMIResourceFactoryImpl {

	public final static String XMI_ENCODING = "UTF-8"; //$NON-NLS-1$

	@Override
	public Resource createResource(URI uri) {

		XMIResource resource = new XMIResourceImpl(uri);
		initResource(resource);
		return resource;
	}

	/**
	 * Initializes given {@link XMLResource resource}.
	 * 
	 * @param resource
	 *            The {@link XMLResource resource} to act upon.
	 */
	public void initResource(XMLResource resource) {
		initDefaultOptions(resource);

		resource.setEncoding(XMI_ENCODING);
	}

	/**
	 * Initializes default {@link XMLResource#getDefaultLoadOptions() load} and
	 * {@link XMLResource#getDefaultSaveOptions() save} options of given {@link XMLResource resource}.
	 * 
	 * @param resource
	 *            The {@link XMLResource resource} to act upon.
	 */
	protected void initDefaultOptions(XMLResource resource) {
		Map<Object, Object> defaultSaveOptions = resource.getDefaultSaveOptions();

		defaultSaveOptions.put(XMLResource.OPTION_DECLARE_XML, Boolean.TRUE);
		defaultSaveOptions.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		defaultSaveOptions.put(XMIResource.OPTION_USE_XMI_TYPE, Boolean.TRUE);
		defaultSaveOptions.put(XMLResource.OPTION_SAVE_TYPE_INFORMATION, Boolean.TRUE);
		defaultSaveOptions.put(XMLResource.OPTION_SKIP_ESCAPE_URI, Boolean.FALSE);
		defaultSaveOptions.put(XMLResource.OPTION_ENCODING, XMI_ENCODING);
		defaultSaveOptions.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, XMLResource.OPTION_PROCESS_DANGLING_HREF_DISCARD);
		defaultSaveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
	}
}
