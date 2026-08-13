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
package org.eclipse.sphinx.emf.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLString;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.xml.sax.InputSource;

/**
 * A converter which can be used to serialize and deserialize between in-memory instances created with one (often the
 * latest) version of a metamodel and XML resources corresponding to another (typically older) version of the same
 * metamodel.
 */
public interface IModelConverter {

	/**
	 * @deprecated Use {@link ExtendedResource#OPTION_RESOURCE_VERSION_DESCRIPTOR} instead.
	 */
	@Deprecated
	String OPTION_RESOURCE_VERSION_DESCRIPTOR = "RESOURCE_VERSION_DESCRIPTOR"; //$NON-NLS-1$

	/**
	 * @return The meta-model descriptor corresponding the currently used meta-model version.
	 * @since 0.7.0
	 */
	public IMetaModelDescriptor getMetaModelVersionDescriptor();

	/**
	 * @return The meta-model descriptor owning the namespace that must be used during save operation for the XML output
	 *         writing.
	 * @since 0.7.0
	 */
	public IMetaModelDescriptor getResourceVersionDescriptor();

	/**
	 * During a load operation, returns <code>true</code> if this converter applies to given XMLResource, taking into
	 * account that the in memory meta-model defines given descriptor.
	 * 
	 * @param resource
	 *            The resource that might eventually be converted.
	 * @param options
	 *            The load options.
	 * @return False otherwise.
	 * @since 0.7.0
	 */
	public boolean isLoadConverterFor(XMLResource resource, Map<?, ?> options);

	/**
	 * During a save operation, returns true if this converter applies to given XMLResource.
	 * 
	 * @param resource
	 *            The resource that might eventually be converted.
	 * @param options
	 *            The save options.
	 * @return False otherwise.
	 */
	public boolean isSaveConverterFor(XMLResource resource, Map<?, ?> options);

	/**
	 * Creates an {@link InputSource} from given {@link InputStream} during the load operation for specified resource.
	 * 
	 * @param resource
	 *            The resource being loaded.
	 * @param inputStream
	 *            The {@link InputStream} being read from.
	 * @param options
	 *            The load options.
	 * @return An {@link InputSource} that may be later processed by the parser.
	 * @throws IOException
	 */
	public InputSource convertLoad(XMLResource resource, InputStream inputStream, Map<?, ?> options) throws IOException;

	/**
	 * During a save operation, writes to given {@link OutputStream} with specified encoding the content of given
	 * {@link XMLString}, using the {@link XMLHelper}.
	 * 
	 * @param xml
	 *            The XML content to be converted.
	 * @param flushThreshold
	 *            The number of bytes to write before the outputStream is flushed.
	 * @param uri
	 *            The URI to which the converted XML content will be written.
	 * @param outputStream
	 *            The stream into which the converted XML data will be written.
	 * @param encoding
	 *            The encoding to use in the outputStream.
	 * @param helper
	 *            The XMLHelper.
	 * @param options
	 *            The save options.
	 * @see java.io.PipedOutputStream
	 * @throws IOException
	 */
	public void convertSave(XMLString xml, int flushThreshold, URI uri, OutputStream outputStream, String encoding, XMLHelper helper,
			Map<?, ?> options) throws IOException;

	/**
	 * Provides a hook for adding additional attributes to the model root element during the save operation for
	 * specified resource.
	 * 
	 * @param rootElement
	 *            The {@link XMLString} to write to, using doc.addAttribute.
	 * @param options
	 *            The save options.
	 * @see XMLResource#getDefaultSaveOptions()
	 */
	public void addExtraAttributesToSavedRootElement(XMLString rootElement, Map<?, ?> options);

	public void dispose();
}
