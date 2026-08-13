/**
 * <copyright>
 *
 * Copyright (c) 2008-2018 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [531560] Provide better control over schema locations being added to serialized model files
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.XMLSave;
import org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.ReflectUtil;
import org.w3c.dom.Element;

/**
 * An extended {@link XMLSave} implementation that provides support for on-the-fly migration of instances of newer
 * metamodel implementations to older {@link XMLResource resource}s using {@link IModelConverter model converter}s.
 * <p>
 * The {@link IModelConverter model converter} to be used for on-the-fly resource migration must be contributed to the
 * <code>org.eclipse.sphinx.emf.modelConverters</code> extension point.
 * </p>
 */
public class ExtendedXMLSaveImpl extends XMLSaveImpl {

	/**
	 * The save options.
	 */
	protected Map<?, ?> options;

	protected ExtendedResource extendedResource;

	protected IModelConverter converter;

	/*
	 * @see XMLSaveImpl#XMLSaveImpl(XMLHelper)
	 */
	public ExtendedXMLSaveImpl(XMLHelper helper) {
		super(helper);
	}

	/*
	 * @see XMLSaveImpl#XMLSaveImpl(Map, XMLHelper, String)
	 */
	public ExtendedXMLSaveImpl(Map<?, ?> options, XMLHelper helper, String encoding) {
		super(options, helper, encoding);
	}

	/*
	 * @see XMLSaveImpl#XMLSaveImpl(Map, XMLHelper, String, String)
	 */
	public ExtendedXMLSaveImpl(Map<?, ?> options, XMLHelper helper, String encoding, String xmlVersion) {
		super(options, helper, encoding, xmlVersion);
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl#save(org.eclipse.emf.ecore.xmi.XMLResource, java.io.Writer,
	 * java.util.Map)
	 */
	@Override
	public void save(XMLResource resource, Writer writer, Map<?, ?> options) throws IOException {
		this.options = options;
		super.save(resource, writer, options);
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl#save(org.eclipse.emf.ecore.xmi.XMLResource, java.io.OutputStream,
	 * java.util.Map)
	 */
	@Override
	public void save(XMLResource resource, OutputStream outputStream, Map<?, ?> options) throws IOException {
		this.options = options;

		if (outputStream instanceof URIConverter.Writeable) {
			URIConverter.Writeable writeable = (URIConverter.Writeable) outputStream;
			resource.setEncoding(writeable.getEncoding());
			save(resource, writeable.asWriter(), options);
			return;
		}
		xmlResource = resource;
		extendedResource = ExtendedResourceAdapterFactory.INSTANCE.adapt(xmlResource);
		init(resource, options);
		converter = ModelConverterRegistry.INSTANCE.getSaveConverter(xmlResource, options);

		@SuppressWarnings("unchecked")
		List<? extends EObject> contents = roots = (List<? extends EObject>) options.get(XMLResource.OPTION_ROOT_OBJECTS);
		if (contents == null) {
			contents = resource.getContents();
		}
		// TODO File bug to EMF: NPE is raised when contents is empty
		if (contents.size() > 0) {
			traverse(contents);
		}

		// If an applicable model converter is around let it migrate and save the document
		boolean didConvert = false;
		if (converter != null) {
			try {
				converter.convertSave(doc, flushThreshold, xmlResource.getURI(), outputStream, encoding, helper, options);
				didConvert = true;
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			} finally {
				converter.dispose();
			}
		}

		if (!didConvert) {
			if ("US-ASCII".equals(encoding) || "ASCII".equals(encoding)) { //$NON-NLS-1$//$NON-NLS-2$
				writeAscii(outputStream);
				outputStream.flush();
			} else {
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, helper.getJavaEncoding(encoding));
				write((Writer) outputStreamWriter);
				outputStreamWriter.flush();
			}
		}

		endSave(contents);
		xmlResource = null;
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl#traverse(java.util.List)
	 */
	@Override
	public void traverse(List<? extends EObject> contents) {
		super.traverse(contents);

		// If an applicable model converter is around let it complement the root element attributes
		if (converter != null) {
			try {
				converter.addExtraAttributesToSavedRootElement(doc, options);
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			}
		}
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl#addNamespaceDeclarations()
	 */
	@Override
	protected void addNamespaceDeclarations() {
		EPackage noNamespacePackage = helper.getNoNamespacePackage();
		EPackage[] packages = helper.packages();
		StringBuffer buffer = getBuffer();
		if (buffer == null) {
			buffer = new StringBuffer();
		}
		buffer.setLength(0);
		StringBuffer xsiSchemaLocation = buffer;
		String xsiNoNamespaceSchemaLocation = null;
		if (declareSchemaLocation) {
			// Schema location entries defined by resource?
			Map<String, String> schemaLocationEntries = extendedResource != null ? extendedResource.getSchemaLocationEntries(options)
					: Collections.<String, String> emptyMap();
			if (schemaLocationEntries != null && !schemaLocationEntries.isEmpty()) {
				declareXSI = true;

				// Write schema location value string from resource-defined schema location entries
				for (Map.Entry<String, String> entry : schemaLocationEntries.entrySet()) {
					String namespace = entry.getKey();
					String location = entry.getValue();
					URI locationURI = URI.createURI(location);
					if (namespace == null) {
						xsiNoNamespaceSchemaLocation = helper.deresolve(locationURI).toString();
					} else {
						if (xsiSchemaLocation.length() > 0) {
							xsiSchemaLocation.append(' ');
						}
						xsiSchemaLocation.append(namespace);
						xsiSchemaLocation.append(' ');
						xsiSchemaLocation.append(helper.deresolve(locationURI).toString());
					}
				}
			} else {
				Map<String, String> handledBySchemaLocationMap = new HashMap<String, String>();
				@SuppressWarnings("unchecked")
				Map<String, String> schemaLocationCatalog = (Map<String, String>) options.get(ExtendedResource.OPTION_SCHEMA_LOCATION_CATALOG);

				// Write schema location value string from recorded schema location entries
				if (extendedMetaData != null) {
					Resource resource = helper.getResource();
					if (resource != null && resource.getContents().size() >= 1) {
						EObject schemaLocationRoot = getSchemaLocationRoot(resource.getContents().get(0));

						EReference xsiSchemaLocationMapFeature = extendedMetaData.getXSISchemaLocationMapFeature(schemaLocationRoot.eClass());
						if (xsiSchemaLocationMapFeature != null) {
							@SuppressWarnings("unchecked")
							EMap<String, String> xsiSchemaLocationMap = (EMap<String, String>) schemaLocationRoot.eGet(xsiSchemaLocationMapFeature);
							if (!xsiSchemaLocationMap.isEmpty()) {
								handledBySchemaLocationMap = new HashMap<String, String>(xsiSchemaLocationMap.map());
								for (Map.Entry<String, String> entry : xsiSchemaLocationMap.entrySet()) {
									String namespace = entry.getKey();
									String location = entry.getValue();
									URI locationURI = URI.createURI(location);
									if (namespace == null) {
										declareXSI = true;

										xsiNoNamespaceSchemaLocation = helper.deresolve(locationURI).toString();
									} else {
										// Need to adjust current schema namespace according to applicable model converter?
										if (converter != null) {
											String mmBaseNsURI = converter.getMetaModelVersionDescriptor().getNamespace();
											String resourceBaseNsURI = converter.getResourceVersionDescriptor().getNamespace();
											if (mmBaseNsURI != null && resourceBaseNsURI != null && namespace.startsWith(mmBaseNsURI)) {
												// Substitute metamodel version in current schema namespace with
												// resource version of applicable model converter
												namespace = namespace.replace(mmBaseNsURI, resourceBaseNsURI);

												// Retrieve schema location corresponding to adjusted schema namespace
												// from schema location catalog
												locationURI = null;
												if (schemaLocationCatalog != null) {
													location = schemaLocationCatalog.get(namespace);
													if (location != null) {
														locationURI = URI.createURI(location);
													} else {
														PlatformLogUtil.logAsWarning(Activator.getPlugin(), new RuntimeException(
																"Schema location catalog entry for namespace '" + namespace + "' is missing")); //$NON-NLS-1$ //$NON-NLS-2$
													}
												} else {
													PlatformLogUtil.logAsWarning(Activator.getPlugin(),
															new RuntimeException("Schema location catalog is missing")); //$NON-NLS-1$
												}
											}
										} else {
											// Current schema namespace corresponding to a metamodel version that is an
											// older version of the metamodel behind schema location root?
											IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE
													.getDescriptor(schemaLocationRoot);
											if (mmDescriptor != null) {
												boolean compatible = false;
												for (java.net.URI compatibleNsURI : mmDescriptor.getCompatibleNamespaceURIs()) {
													if (namespace.startsWith(compatibleNsURI.toString())) {
														compatible = true;
														break;
													}
												}
												if (compatible) {
													// Ignore corresponding recorded schema location entry; as no model
													// converter is available the model will be serialized using the most
													// recent metamodel namespace; we therefore must not write a schema
													// location entry for the original old namespace but the most recent
													// one; this is done below anyway, so we can simply skip the rest right
													// here
													continue;
												}
											}
										}

										if (locationURI != null) {
											declareXSI = true;

											if (xsiSchemaLocation.length() > 0) {
												xsiSchemaLocation.append(' ');
											}
											xsiSchemaLocation.append(namespace);
											xsiSchemaLocation.append(' ');
											xsiSchemaLocation.append(helper.deresolve(locationURI).toString());
										}
									}
								}
							}
						}
					}
				}

				// Complete schema location value string with calculated schema location entries
				for (EPackage ePackage : packages) {
					String javaImplementationLocation = null;
					if (declareSchemaLocationImplementation) {
						// First try to see if this package's implementation class has an eInstance.
						//
						try {
							Field field = ePackage.getClass().getField("eINSTANCE"); //$NON-NLS-1$
							javaImplementationLocation = "java://" + field.getDeclaringClass().getName(); //$NON-NLS-1$
						} catch (Exception exception) {
							// If there is no field, then we can't do this.
						}
					}

					if (noNamespacePackage == ePackage) {
						if (ePackage.eResource() != null && !handledBySchemaLocationMap.containsKey(null)) {
							if (javaImplementationLocation != null) {
								xsiNoNamespaceSchemaLocation = javaImplementationLocation;
							} else if (schemaLocationCatalog != null) {
								// Retrieve schema location corresponding to no namespace from schema location catalog
								xsiNoNamespaceSchemaLocation = schemaLocationCatalog.get(null);
								if (xsiNoNamespaceSchemaLocation == null) {
									PlatformLogUtil.logAsWarning(Activator.getPlugin(),
											new RuntimeException("Schema location catalog entry for no namespace (null) is missing")); //$NON-NLS-1$
								}
							} else {
								xsiNoNamespaceSchemaLocation = helper.getHREF(ePackage);
								if (xsiNoNamespaceSchemaLocation != null && xsiNoNamespaceSchemaLocation.endsWith("#/")) { //$NON-NLS-1$
									xsiNoNamespaceSchemaLocation = xsiNoNamespaceSchemaLocation.substring(0,
											xsiNoNamespaceSchemaLocation.length() - 2);
								}
							}

							declareXSI = xsiNoNamespaceSchemaLocation != null;
						}
					} else {
						String namespace = extendedMetaData == null ? ePackage.getNsURI() : extendedMetaData.getNamespace(ePackage);

						// Need to adjust schema namespace according to applicable model converter?
						if (converter != null) {
							String mmBaseNsURI = converter.getMetaModelVersionDescriptor().getNamespace();
							String resourceBaseNsURI = converter.getResourceVersionDescriptor().getNamespace();
							if (mmBaseNsURI != null && resourceBaseNsURI != null && namespace.startsWith(mmBaseNsURI)) {
								// Substitute metamodel version in schema namespace with resource
								// version of applicable model converter
								namespace = namespace.replace(mmBaseNsURI, resourceBaseNsURI);
							}
						}

						if (!handledBySchemaLocationMap.containsKey(namespace)) {
							if (javaImplementationLocation != null || namespace != null) {
								String location = null;
								if (javaImplementationLocation != null) {
									location = javaImplementationLocation;
								} else if (schemaLocationCatalog != null) {
									// Retrieve schema location corresponding to schema namespace from schema location
									// catalog
									location = schemaLocationCatalog.get(namespace);
									if (location == null) {
										PlatformLogUtil.logAsWarning(Activator.getPlugin(),
												new RuntimeException("Schema location catalog entry for namespace '" + namespace + "' is missing")); //$NON-NLS-1$ //$NON-NLS-2$
									}
								} else {
									location = helper.getHREF(ePackage);
									location = convertURI(location);
									if (location.endsWith("#/")) { //$NON-NLS-1$
										location = location.substring(0, location.length() - 2);
										URI uri = ePackage.eResource().getURI();
										if (uri != null && uri.hasFragment()) {
											location += "#" + uri.fragment(); //$NON-NLS-1$
										}
									}
								}

								if (location != null) {
									declareXSI = true;

									if (xsiSchemaLocation.length() > 0) {
										xsiSchemaLocation.append(' ');
									}
									xsiSchemaLocation.append(namespace);
									xsiSchemaLocation.append(' ');
									xsiSchemaLocation.append(location);
								}

								// Avoid duplicate schema location entries
								handledBySchemaLocationMap.put(namespace, location);
							}
						}
					}
				}
			}
		}

		for (EPackage ePackage : packages) {
			if (ePackage != noNamespacePackage && ePackage != XMLNamespacePackage.eINSTANCE
					&& !ExtendedMetaData.XMLNS_URI.equals(ePackage.getNsURI())) {
				String nsURI = extendedMetaData == null ? ePackage.getNsURI() : extendedMetaData.getNamespace(ePackage);
				if (ePackage == xmlSchemaTypePackage) {
					nsURI = XMLResource.XML_SCHEMA_URI;
				}
				if (nsURI != null && !isDuplicateURI(nsURI)) {
					// Need to adjust EPackage namespace according to applicable model converter?
					if (converter != null) {
						String mmBaseNsURI = converter.getMetaModelVersionDescriptor().getNamespace();
						String resourceBaseNsURI = converter.getResourceVersionDescriptor().getNamespace();
						if (mmBaseNsURI != null && resourceBaseNsURI != null && nsURI.startsWith(mmBaseNsURI)) {
							// Substitute metamodel version in EPackage namespace with resource version of applicable
							// model converter
							nsURI = nsURI.replace(mmBaseNsURI, resourceBaseNsURI);
						}
					}

					List<String> nsPrefixes = helper.getPrefixes(ePackage);
					for (String nsPrefix : nsPrefixes) {
						if (!toDOM) {
							if (nsPrefix != null && nsPrefix.length() > 0) {
								if (!declareXSI || !"xsi".equals(nsPrefix)) { //$NON-NLS-1$
									doc.addAttributeNS(XMLResource.XML_NS, nsPrefix, nsURI);
								}
							} else {
								doc.addAttribute(XMLResource.XML_NS, nsURI);
							}
						} else {
							if (nsPrefix != null && nsPrefix.length() > 0) {
								if (!declareXSI || !"xsi".equals(nsPrefix)) { //$NON-NLS-1$
									((Element) currentNode).setAttributeNS(ExtendedMetaData.XMLNS_URI, XMLResource.XML_NS + ":" + nsPrefix, nsURI); //$NON-NLS-1$
								}
							} else {
								((Element) currentNode).setAttributeNS(ExtendedMetaData.XMLNS_URI, XMLResource.XML_NS, nsURI);
							}
						}
					}
				}
			}
		}

		if (declareXSI) {
			if (!toDOM) {
				doc.addAttribute(XSI_XMLNS, XMLResource.XSI_URI);
			} else {
				((Element) currentNode).setAttributeNS(ExtendedMetaData.XMLNS_URI, XSI_XMLNS, XMLResource.XSI_URI);
			}
		}

		if (xsiSchemaLocation.length() > 0) {
			if (!toDOM) {
				doc.addAttribute(XSI_SCHEMA_LOCATION, xsiSchemaLocation.toString());
			} else {
				((Element) currentNode).setAttributeNS(XMLResource.XSI_URI, XSI_SCHEMA_LOCATION, xsiSchemaLocation.toString());
			}
		}

		if (xsiNoNamespaceSchemaLocation != null) {
			if (!toDOM) {
				doc.addAttribute(XSI_NO_NAMESPACE_SCHEMA_LOCATION, xsiNoNamespaceSchemaLocation);
			} else {
				((Element) currentNode).setAttributeNS(XMLResource.XSI_URI, XSI_NO_NAMESPACE_SCHEMA_LOCATION, xsiNoNamespaceSchemaLocation);
			}
		}
	}

	protected StringBuffer getBuffer() {
		try {
			return (StringBuffer) ReflectUtil.getInvisibleFieldValue(this, "buffer"); //$NON-NLS-1$
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
		return null;
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl#writeTopObject(org.eclipse.emf.ecore.EObject)
	 */
	@Override
	protected Object writeTopObject(EObject top) {
		// Make sure that comments, text, CDATA and processing instructions get added BEFORE the
		// root element
		saveOuterContent(top);

		return super.writeTopObject(top);
	}

	/**
	 * Writes out the contents of the special feature ExtendedResourceConstants.OUTER_CONTENT_ATTRIBUTE_NAME of the passed
	 * in object. This method is used to write out such comment before the starting tag of the actual root element.
	 *
	 * @param top
	 *            The {@link EObject root object} whose mixed outer content (text, comments, CDATA and processing
	 *            instructions) is to be saved.
	 */
	protected void saveOuterContent(EObject top) {
		EList<EAttribute> allAttributes = top.eClass().getEAllAttributes();
		for (Object element : allAttributes) {
			EAttribute attribute = (EAttribute) element;
			if (ExtendedResourceConstants.OUTER_CONTENT_ATTRIBUTE_NAME.equals(attribute.getName())) {
				saveElementFeatureMap(top, attribute);
			}
		}
	}
}