/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [409510] Enable resource scope-sensitive proxy resolutions without forcing metamodel implementations to subclass EObjectImpl
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.impl.Constants;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.sphinx.emf.resource.BasicMigrationExtendedMetaData;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.SchemaLocationURIHandler;
import org.eclipse.sphinx.examples.hummingbird.ide.metamodel.HummingbirdMMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.Activator;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

/**
 * The <b>Resource Factory</b> associated with the package.
 *
 * @see org.eclipse.sphinx.examples.hummingbird20.util.Hummingbird20ResourceImpl
 */
public class Hummingbird20ResourceFactoryImpl extends ResourceFactoryImpl {

	protected ExtendedMetaData extendedMetaData;

	protected Map<String, String> schemaLocationCatalog;

	protected SchemaLocationURIHandler schemaLocationURIHandler;

	/**
	 * Creates an instance of the resource factory.
	 */
	public Hummingbird20ResourceFactoryImpl() {
		// Create and initialize schema location catalog, i.e., a map providing namespace and system identifier pairs
		// that are allowed to be written in the resource's xsi:schemaLocation/xsi:noNamespaceSchemaLocation during
		// saving
		schemaLocationCatalog = new HashMap<String, String>();

		// Register schema files for current metamodel version
		schemaLocationCatalog.put(Common20Package.eNS_URI, "Common20XMI.xsd"); //$NON-NLS-1$
		schemaLocationCatalog.put(TypeModel20Package.eNS_URI, "TypeModel20XMI.xsd"); //$NON-NLS-1$
		schemaLocationCatalog.put(InstanceModel20Package.eNS_URI, "InstanceModel20XMI.xsd"); //$NON-NLS-1$
		schemaLocationCatalog.put(XMIResource.XMI_URI, "XMI.xsd"); //$NON-NLS-1$

		// Register schema files for compatible metamodel versions
		schemaLocationCatalog.put(HummingbirdMMDescriptor.BASE_NAMESPACE + "/2.0.0/common", "Common200XMI.xsd"); //$NON-NLS-1$ //$NON-NLS-2$
		schemaLocationCatalog.put(HummingbirdMMDescriptor.BASE_NAMESPACE + "/2.0.0/typemodel", "TypeModel200XMI.xsd"); //$NON-NLS-1$ //$NON-NLS-2$
		schemaLocationCatalog.put(HummingbirdMMDescriptor.BASE_NAMESPACE + "/2.0.0/instancemodel", "InstanceModel200XMI.xsd"); //$NON-NLS-1$ //$NON-NLS-2$

		// Create and initialize schema location URI handler enabling schema locations to be resolved to schema files
		// during loading and schema files to be deresolved to schema locations during saving
		schemaLocationURIHandler = new SchemaLocationURIHandler(schemaLocationCatalog);
		schemaLocationURIHandler.addSchemaLocationBaseURI(Activator.getPlugin(), "model"); //$NON-NLS-1$
		schemaLocationURIHandler.addSchemaLocationBaseURI(Activator.getPlugin(), "model/archive"); //$NON-NLS-1$

		// Create and initialize migration-enabled extended meta data enabling Hummingbird resources whose version is
		// not the same but compatible with that of current Hummingbird metamodel implementation to be loaded
		extendedMetaData = new BasicMigrationExtendedMetaData(new EPackageRegistryImpl(EPackage.Registry.INSTANCE));

		// Map relevant EPackages to their namespaces in current metamodel version
		extendedMetaData.putPackage(TypeModel20Package.eNS_URI, TypeModel20Package.eINSTANCE);
		extendedMetaData.putPackage(InstanceModel20Package.eNS_URI, InstanceModel20Package.eINSTANCE);

		// Map relevant EPackages to their namespaces in compatible metamodel versions
		extendedMetaData.putPackage(HummingbirdMMDescriptor.BASE_NAMESPACE + "/2.0.0/typemodel", TypeModel20Package.eINSTANCE); //$NON-NLS-1$
		extendedMetaData.putPackage(HummingbirdMMDescriptor.BASE_NAMESPACE + "/2.0.0/instancemodel", InstanceModel20Package.eINSTANCE); //$NON-NLS-1$
	}

	/**
	 * Creates an instance of the resource.
	 */
	@Override
	public Resource createResource(URI uri) {
		XMIResource result = new Hummingbird20ResourceImpl(uri);

		result.getDefaultLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetaData);
		result.getDefaultSaveOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetaData);

		result.getDefaultLoadOptions().put(XMLResource.OPTION_USE_LEXICAL_HANDLER, Boolean.TRUE);

		// Configure SAX parser to continue even in case of fatal errors (typically XML well-formedness problems or I/O
		// errors) so as to always load XML documents as far as possible rather than not loading the entire document
		// just because a potentially small part of it is not good
		Map<String, Boolean> parserFeatures = new HashMap<String, Boolean>();
		parserFeatures.put(Constants.XERCES_FEATURE_PREFIX + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE, true);
		result.getDefaultLoadOptions().put(XMLResource.OPTION_PARSER_FEATURES, parserFeatures);

		result.getDefaultSaveOptions().put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		result.getDefaultSaveOptions().put(ExtendedResource.OPTION_SCHEMA_LOCATION_CATALOG, schemaLocationCatalog);
		result.getDefaultLoadOptions().put(XMLResource.OPTION_URI_HANDLER, schemaLocationURIHandler);
		result.getDefaultSaveOptions().put(XMLResource.OPTION_URI_HANDLER, schemaLocationURIHandler);

		result.getDefaultLoadOptions().put(ExtendedResource.OPTION_ENABLE_SCHEMA_VALIDATION, Boolean.TRUE);

		// Necessary to make sure that whitespace before closing tag in XML document gets interpreted as mixed content
		// but not as proxy URI (see org.eclipse.emf.ecore.xmi.impl.XMLHandler.endElement(String, String, String) for
		// details)
		result.getDefaultLoadOptions().put(XMLResource.OPTION_SUPPRESS_DOCUMENT_ROOT, Boolean.TRUE);

		// Don't use context-aware proxy URIs when creating proxy objects since Hummingbird 2.0 model classes
		// extend Sphinx-defined ExtendedMinimalEObjectImpl which means that context information required for resource
		// scoping-aware proxy resolution is passed along via API and doesn't need to be encoded into the proxy URIs
		result.getDefaultLoadOptions().put(ExtendedResource.OPTION_USE_CONTEXT_AWARE_PROXY_URIS, Boolean.FALSE);

		return result;
	}
} // Hummingbird20ResourceFactoryImpl
