/**
 * <copyright>
 *
 * Copyright (c) 2008-2012 See4sys, BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Added/Updated javadoc
 *     itemis - [344145] AbstractModelConverter should build document from XMLInputSource with systemId and publicId set
 *     BMW Car IT - Make sure JDOM uses platform specific line separator instead of always using Windows \r\n.
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;

import org.apache.xerces.impl.Constants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIException;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLHelperImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLString;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A base implementation which provides default implementations of the IModelConverter methods.
 */
public abstract class AbstractModelConverter implements IModelConverter {

	public abstract Object getResourceVersionFromPreferences(IProject project);

	private final Format format = Format.getRawFormat().setLineSeparator(System.getProperty("line.separator")); //$NON-NLS-1$

	@Override
	public boolean isLoadConverterFor(XMLResource resource, Map<?, ?> options) {
		if (resource == null) {
			return false;
		}

		// Check if meta-model versions of this model converter and given resource match
		IMetaModelDescriptor mmVersionDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(resource);
		if (!getMetaModelVersionDescriptor().equals(mmVersionDescriptor)) {
			return false;
		}

		// Check if resource version of this model converter matches resource version indicated by load options
		Object value = options.get(ExtendedResource.OPTION_RESOURCE_VERSION_DESCRIPTOR);
		if (value instanceof IMetaModelDescriptor) {
			return getResourceVersionDescriptor() != null && getResourceVersionDescriptor().equals(value)
					&& !value.equals(getMetaModelVersionDescriptor());
		}

		// Check if resource version matches the resource's namespace
		String resourceNamespace = EcoreResourceUtil.readModelNamespace(resource);
		boolean matching = false;
		if (resourceNamespace != null) {
			matching = resourceNamespace.equals(getResourceVersionDescriptor().getNamespace());
			if (!matching) {
				matching = getResourceVersionDescriptor().matchesEPackageNsURIPattern(resourceNamespace);
			}
		}
		return matching;
	}

	@Override
	public boolean isSaveConverterFor(XMLResource resource, Map<?, ?> options) {
		if (resource == null) {
			return false;
		}

		// Check if meta-model versions of this model converter and given resource match
		IMetaModelDescriptor mmVersionDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(resource);
		if (!getMetaModelVersionDescriptor().equals(mmVersionDescriptor)) {
			return false;
		}

		// Check if resource version of this model converter matches resource version configured in project properties
		// (which is given preference over resource version indicated by save options)
		IFile file = EcorePlatformUtil.getFile(resource);
		if (file != null) {
			Object value = getResourceVersionFromPreferences(file.getProject());
			if (value instanceof IMetaModelDescriptor) {
				return getResourceVersionDescriptor().equals(value);
			}
		}

		// Check if resource version of this model converter matches resource version indicated by save options
		Object value = options.get(ExtendedResource.OPTION_RESOURCE_VERSION_DESCRIPTOR);
		return getResourceVersionDescriptor().equals(value);
	}

	protected XMIException toXMIException(Resource resource, Exception ex) {
		String location = resource.getURI() == null ? null : resource.getURI().toString();
		return new XMIException(ex, location, 1, 1);
	}

	@Override
	public InputSource convertLoad(XMLResource resource, InputStream inputStream, Map<?, ?> options) throws IOException {
		try {
			InputStream converted = doConvertLoad(resource, inputStream, options);

			// This save option will enable us to detect that we have to convert back to some older version
			resource.getDefaultSaveOptions().put(ExtendedResource.OPTION_RESOURCE_VERSION_DESCRIPTOR, getResourceVersionDescriptor());

			return new InputSource(converted);
		} catch (JDOMException ex) {
			throw new Resource.IOWrappedException(ex);
		}
	}

	protected InputStream doConvertLoad(XMLResource resource, InputStream inputStream, Map<?, ?> options) throws IOException, JDOMException {
		// Create SAX builder and XML handler
		SAXBuilder builder = makeBuilder();
		DefaultHandler handler = makeDefaultHandler(resource, options);

		// Create input source from given input stream and initialize public and system id with resource URI so as to
		// enable entity resolution relative to resource location (rather than relative to the running application's
		// working directory)
		InputSource inputSource = new InputSource(inputStream);
		if (resource.getURI() != null) {
			String resourceURI = resource.getURI().toString();
			inputSource.setPublicId(resourceURI);
			inputSource.setSystemId(resourceURI);
		}

		// Retrieve and set application-defined XMLReader features (see http://xerces.apache.org/xerces2-j/features.html
		// for available features and their details)
		@SuppressWarnings("unchecked")
		Map<String, Boolean> parserFeatures = (Map<String, Boolean>) options.get(XMLResource.OPTION_PARSER_FEATURES);
		if (parserFeatures != null) {
			for (String feature : parserFeatures.keySet()) {
				builder.setFeature(feature, parserFeatures.get(feature).booleanValue());
			}
		}

		// Retrieve and set application-defined XMLReader properties (see
		// http://xerces.apache.org/xerces2-j/properties.html available properties and their details)
		@SuppressWarnings("unchecked")
		Map<String, Object> parserProperties = (Map<String, Object>) options.get(XMLResource.OPTION_PARSER_PROPERTIES);
		if (parserProperties != null) {
			for (String property : parserProperties.keySet()) {
				builder.setProperty(property, parserProperties.get(property));
			}
		}

		// Optionally enable schema validation and register XML handler for resolving schema locations
		if (Boolean.TRUE.equals(options.get(ExtendedResource.OPTION_ENABLE_SCHEMA_VALIDATION))) {
			builder.setValidation(true);
			builder.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE, true);
			builder.setProperty(Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);
			builder.setEntityResolver(handler);
		}

		// Use custom extended error handler wrapper enabling concise distinction between well-formedness, validity
		// and integrity problems
		/*
		 * !! Important Note !! Requires org.apache.xerces parser (but not com.sun.org.apache.xerces parser) to be used.
		 */
		builder.setProperty(Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY, new ExtendedErrorHandlerWrapper());

		// Register XML handler for capturing anomalies encountered during parsing as errors and warnings on resource
		builder.setErrorHandler(handler);

		// Parse input source into DOM structure
		final Document document = builder.build(inputSource);

		// Iterate over all DOM elements and let them be converted
		List<Element> elementsToConvert = new ArrayList<Element>();
		for (Iterator<?> iterator = document.getRootElement().getDescendants(); iterator.hasNext();) {
			Object next = iterator.next();
			if (next instanceof Element) {
				Element element = (Element) next;
				elementsToConvert.add(element);
			}
		}
		for (Element element : elementsToConvert) {
			try {
				convertLoadElement(element, options);
			} catch (Exception ex) {
				resource.getErrors().add(toXMIException(resource, ex));
			}
		}

		// Write converted DOM structure into an output stream which is connected to a new input stream
		PipedInputStream pipedInputStream = new PipedInputStream();
		final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
		final XMLOutputter out = new XMLOutputter(format);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					out.output(document, pipedOutputStream);
				} catch (IOException ex) {
					PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
				} finally {
					try {
						pipedOutputStream.close();
					} catch (IOException ex) {
						PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
					}
				}
			}
		}, "Converting " + resource.getURI().toString()); //$NON-NLS-1$
		thread.start();

		// Return new input stream yielding converted XML document
		return pipedInputStream;
	}

	protected SAXBuilder makeBuilder() {
		return new SAXBuilder(org.apache.xerces.parsers.SAXParser.class.getName());
	}

	protected DefaultHandler makeDefaultHandler(XMLResource resource, Map<?, ?> options) {
		return new ExtendedSAXXMLHandler(resource, new XMLHelperImpl(), options);
	}

	// TODO Pass resource as additional parameter and add enable exceptions to added to the resource's error/warning
	// lists rather than logging them in error log
	protected abstract void convertLoadElement(Element element, Map<?, ?> options);

	@Override
	public void convertSave(final XMLString xml, final int flushThreshold, URI uri, OutputStream outputStream, final String encoding,
			final XMLHelper helper, Map<?, ?> options) throws IOException {
		try {
			// Write given XML string into an output stream which is connected to a new input stream
			PipedInputStream pipedInputStream = new PipedInputStream();
			final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						if ("US-ASCII".equals(encoding) || "ASCII".equals(encoding)) { //$NON-NLS-1$ //$NON-NLS-2$
							xml.writeAscii(pipedOutputStream, flushThreshold);
							pipedOutputStream.flush();
						} else {
							OutputStreamWriter outputStreamWriter = new OutputStreamWriter(pipedOutputStream, helper.getJavaEncoding(encoding));
							xml.write((Writer) outputStreamWriter, flushThreshold);
							outputStreamWriter.flush();
						}
					} catch (IOException ex) {
						PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
					} finally {
						try {
							pipedOutputStream.close();
						} catch (IOException ex) {
							PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
						}
					}
				}
			}, "Converting " + uri.toString()); //$NON-NLS-1$
			thread.start();

			// Parser new XML input stream into DOM structure
			final Document document = new SAXBuilder().build(pipedInputStream);

			// Iterate over all DOM elements and let them be converted
			List<Element> elementsToConvert = new ArrayList<Element>();
			for (Iterator<?> iterator = document.getRootElement().getDescendants(); iterator.hasNext();) {
				Object next = iterator.next();
				if (next instanceof Element) {
					Element element = (Element) next;
					elementsToConvert.add(element);
				}
			}
			for (Element element : elementsToConvert) {
				try {
					convertSaveElement(element, options);
				} catch (Exception ex) {
					// TODO Pass resource rather than just URI to convertSave and add exception to the resource's error
					// list rather than logging it in error log
					// resource.getErrors().add(toXMIException(resource, ex));
					PlatformLogUtil.logAsError(Activator.getDefault(), ex);
				}
			}

			// Write converted DOM structure into given XML output stream
			new XMLOutputter(format).output(document, outputStream);
		} catch (Exception ex) {
			throw new Resource.IOWrappedException(ex);
		}
	}

	// TODO Pass resource as additional parameter and add enable exceptions to added to the resource's error/warning
	// lists rather than logging them in error log
	protected abstract void convertSaveElement(Element element, Map<?, ?> options);

	@Override
	public void addExtraAttributesToSavedRootElement(XMLString rootElement, Map<?, ?> options) {
		// Do nothing by default
	}

	@Override
	public void dispose() {
		// Do nothing by default
	}
}
