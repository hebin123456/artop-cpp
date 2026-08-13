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
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.xmi.XMLDefaultHandler;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.XMLParserPool;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An extended {@link XMLLoad} implementation that provides support for on-the-fly migration of older {@link XMLResource
 * resource}s to instances of newer metamodel implementations using {@link IModelConverter model converter}s.
 * <p>
 * The {@link IModelConverter model converter} to be used for on-the-fly resource migration must be contributed to the
 * <code>org.eclipse.sphinx.emf.modelConverters</code> extension point.
 * </p>
 */
public class ExtendedXMLLoadImpl extends XMLLoadImpl {

	protected boolean enableSchemaValidation;

	protected SAXParser parser = null;

	/*
	 * @see XMLLoadImpl#XMLLoadImpl(XMLHelper)
	 */
	public ExtendedXMLLoadImpl(XMLHelper helper) {
		super(helper);
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl#load(org.eclipse.emf.ecore.xmi.XMLResource, java.io.InputStream,
	 * java.util.Map)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void load(XMLResource resource, InputStream inputStream, Map<?, ?> options) throws IOException {
		if (inputStream instanceof URIConverter.Readable) {
			URIConverter.Readable readable = (URIConverter.Readable) inputStream;
			resource.setEncoding(readable.getEncoding());

			InputSource inputSource = new InputSource(readable.asReader());
			if (resource.getURI() != null) {
				String resourceURI = resource.getURI().toString();
				inputSource.setPublicId(resourceURI);
				inputSource.setSystemId(resourceURI);
				inputSource.setEncoding(resource.getEncoding());
			}
			load(resource, inputSource, options);
			return;
		}

		this.resource = resource;
		is = inputStream;
		this.options = options;
		boolean enableSchemaValidation = Boolean.TRUE.equals(options.get(ExtendedResource.OPTION_ENABLE_SCHEMA_VALIDATION));

		// HACK: reading encoding
		String encoding = null;
		if (!Boolean.FALSE.equals(options.get(XMLResource.OPTION_USE_DEPRECATED_METHODS))) {
			encoding = getEncoding();
			resource.setEncoding(encoding);
		}

		// If an applicable model converter is around let it migrate the document prior to parsing it
		InputSource inputSource = null;
		boolean closeIs = false;
		IModelConverter converter = ModelConverterRegistry.INSTANCE.getLoadConverter(resource, options);
		boolean didConvert = false;
		if (converter != null) {
			try {
				inputSource = converter.convertLoad(resource, is, options);
				didConvert = true;
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
				// Renew input stream as current one has been consumed by the model converter
				URIConverter uriConverter = EcoreResourceUtil.getURIConverter(resource.getResourceSet());
				is = uriConverter.createInputStream(resource.getURI(), options);
				closeIs = true;
			} finally {
				converter.dispose();
			}
		}

		if (inputSource == null) {
			inputSource = new InputSource(is);
		}
		if (resource.getURI() != null) {
			String resourceURI = resource.getURI().toString();
			inputSource.setPublicId(resourceURI);
			inputSource.setSystemId(resourceURI);
			inputSource.setEncoding(encoding);
		}

		// Retrieve application-defined XMLReader features (see http://xerces.apache.org/xerces2-j/features.html for
		// available features and their details)
		@SuppressWarnings("unchecked")
		Map<String, Boolean> parserFeatures = (Map<String, Boolean>) options.get(XMLResource.OPTION_PARSER_FEATURES);
		parserFeatures = parserFeatures == null ? new HashMap<String, Boolean>() : parserFeatures;

		// Retrieve application-defined XMLReader properties (see http://xerces.apache.org/xerces2-j/properties.html
		// for available properties and their details)
		@SuppressWarnings("unchecked")
		Map<String, Object> parserProperties = (Map<String, Object>) options.get(XMLResource.OPTION_PARSER_PROPERTIES);
		parserProperties = parserProperties == null ? new HashMap<String, Object>() : parserProperties;

		// Disable doctypes and external entities to prevent XML Entity attacks
		parserFeatures.put(Constants.XERCES_FEATURE_PREFIX + Constants.DISALLOW_DOCTYPE_DECL_FEATURE, true);
		parserFeatures.put(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
		parserFeatures.put(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
		parserFeatures.put(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);

		// Perform namespace processing (prefixes will be stripped off element and attribute names and replaced with the
		// corresponding namespace URIs) but do not report attributes used for namespace declarations, and do not report
		// original prefixed names
		parserFeatures.put(Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE, true);
		parserFeatures.put(Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACE_PREFIXES_FEATURE, false);

		// Optionally enable schema validation unless document has been migrated before; in this case schema validation
		// is subject to the applicable model converter
		if (enableSchemaValidation && !didConvert) {
			parserFeatures.put(Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE, true);
			parserFeatures.put(Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE, true);
			parserProperties.put(Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);
		}

		// Use custom extended error handler wrapper enabling concise distinction between well-formedness, validity and
		// integrity problems
		/*
		 * !! Important Note !! Requires org.apache.xerces parser (but not com.sun.org.apache.xerces parser) to be used.
		 */
		parserProperties.put(Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY, new ExtendedErrorHandlerWrapper());

		DefaultHandler handler = null;
		XMLParserPool pool = (XMLParserPool) options.get(XMLResource.OPTION_USE_PARSER_POOL);
		try {
			if (pool != null) {
				// Use the pool to retrieve the parser
				parser = pool.get(parserFeatures, parserProperties, Boolean.TRUE.equals(options.get(XMLResource.OPTION_USE_LEXICAL_HANDLER)));
				handler = (DefaultHandler) pool.getDefaultHandler(resource, this, helper, options);
			} else {
				parser = makeParser();
				handler = makeDefaultHandler();

				// Set features and properties
				if (parserFeatures != null) {
					for (String feature : parserFeatures.keySet()) {
						parser.getXMLReader().setFeature(feature, parserFeatures.get(feature).booleanValue());
					}
				}
				if (parserProperties != null) {
					for (String property : parserProperties.keySet()) {
						parser.getXMLReader().setProperty(property, parserProperties.get(property));
					}
				}
			}

			// Set lexical handler
			if (Boolean.TRUE.equals(options.get(XMLResource.OPTION_USE_LEXICAL_HANDLER))) {
				if (parserProperties == null || parserProperties.get(SAX_LEXICAL_PROPERTY) == null) {
					parser.setProperty(SAX_LEXICAL_PROPERTY, handler);
				}
			}

			parser.parse(inputSource, handler);
		} catch (SAXException exception) {
			if (exception.getException() != null) {
				throw new Resource.IOWrappedException(exception.getException());
			} else {
				throw new Resource.IOWrappedException(exception);
			}
		} catch (ParserConfigurationException exception) {
			throw new Resource.IOWrappedException(exception);
		} finally {
			// Release parser back to the pool
			if (pool != null) {
				if (parser != null) {
					pool.release(parser, parserFeatures, parserProperties, Boolean.TRUE.equals(options.get(XMLResource.OPTION_USE_LEXICAL_HANDLER)));
				}
				if (handler != null) {
					pool.releaseDefaultHandler((XMLDefaultHandler) handler, options);
				}
			}
			if (closeIs) {
				is.close();
			}

			helper = null;
			parser = null;
			converter = null;
			handleErrors();
		}
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl#makeParser()
	 */
	@Override
	protected SAXParser makeParser() throws ParserConfigurationException, SAXException {
		// Since we're depending on the specific Xerces implementation, rather than relying on OSGi services to
		// create a parser we're instantiating Xerces SAXParser directly
		return new SAXParserFactoryImpl().newSAXParser();
	}

	@Override
	protected DefaultHandler makeDefaultHandler() {
		return new ExtendedSAXXMLHandler(resource, helper, options);
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl#handleErrors()
	 */
	@Override
	protected void handleErrors() throws IOException {
		/*
		 * !! Important Note !! Don't raise exceptions when errors have been encountered during loading. This would
		 * entail that the resource in question doesn't get loaded at all. Instead we want to make sure that it can get
		 * loaded anyway - or at least as far as possible - and errors/warnings encountered during that should be
		 * reported in a less intrusive way, e.g., by attaching problem markers to the underlying file.
		 */
	}
}
