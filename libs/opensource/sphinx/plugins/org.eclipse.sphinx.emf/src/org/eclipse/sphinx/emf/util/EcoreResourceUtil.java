/**
 * <copyright>
 *
 * Copyright (c) 2008-2019 BMW Car IT, See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Added/Updated javadoc
 *     itemis - [346715] IMetaModelDescriptor methods of MetaModelDescriptorRegistry taking EObject or Resource arguments should not start new EMF transactions
 *     itemis - [357962] Make sure that problems occurring when saving model elements in a new resource are not recorded as errors/warnings on resource
 *     BMW Car IT - [374883] Improve handling of out-of-sync workspace files during descriptor initialization
 *     itemis - [393021] ClassCastExceptions raised during loading model resources with Sphinx are ignored
 *     itemis - [400897] ExtendedResourceAdapter's approach of reflectively clearing all EObject fields when performing memory-optimized unloads bears the risk of leaving some EObjects leaked
 *     itemis - [409510] Enable resource scope-sensitive proxy resolutions without forcing metamodel implementations to subclass EObjectImpl
 *     itemis - [418005] Add support for model files with multiple root elements
 *	   itemis - [425175] Resources that produce exceptions while being loaded should not get unloaded by Sphinx thereafter
 *     itemis - [442342] Sphinx doen't trim context information from proxy URIs when serializing proxyfied cross-document references
 *     itemis - [458862] Navigation from problem markers in Check Validation view to model editors and Model Explorer view broken
 *     itemis - [458976] Validators are not singleton when they implement checks for different EPackages
 *     itemis - [460534] Make sure that EcoreResourceUtil creates a ResourceSetImpl rather than a ScopingResourceSetImpl when no resource set is provided by the caller
 *     itemis - [460260] Expanded paths are collapsed on resource reload
 *     itemis - [485407] Enable eager post-load proxy resolution to support manifold URI fragments referring to the same object
 *     itemis - [487564] Provide a reusable base implementation of an IURIChangeDetectorDelegate for URIs with hierarchical fragments
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ContentHandler;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.xmi.XMIException;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapterFactory;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.XMLRootElementHandler;
import org.xml.sax.SAXException;

/**
 * Utility class for resource management.
 */
public final class EcoreResourceUtil {

	// Prevent from instantiation
	private EcoreResourceUtil() {
	}

	/**
	 * Returns an instance of {@link ExtensibleURIConverterImpl} where the URI mappings are initialized in such a way
	 * that normalization of non-platform:/resource {@link URI}s which reference resources inside the workspace yields
	 * the corresponding platform:/resource {@link URI}s.
	 *
	 * @return An instance of {@link ExtensibleURIConverterImpl} containing URI mappings for normalizing
	 *         non-platform:/resource {@link URI}s referencing workspace resources to corresponding platform:/resource
	 *         {@link URI}s.
	 */
	public static URIConverter getURIConverter() {
		return getURIConverter(null);
	}

	/**
	 * Returns the {@link URIConverter URI converter} of given {@link ResourceSet resource set}. If no
	 * {@link ResourceSet resource set} is provided an instance of {@link ExtensibleURIConverterImpl} is returned
	 * instead. In both cases, the {@link URIConverter URI converter}'s URI mappings are initialized in such a way that
	 * normalization of non-platform:/resource {@link URI}s which reference resources inside the workspace yields the
	 * corresponding platform:/resource {@link URI}s.
	 *
	 * @param resourceSet
	 *            The {@link ResourceSet resource set} whose {@link URIConverter URI converter} is to be retrieved.
	 * @return The {@link URIConverter URI converter} of given {@link ResourceSet resource set}, or an instance of
	 *         {@link ExtensibleURIConverterImpl} if no such is provided, containing URI mappings for normalizing
	 *         non-platform:/resource {@link URI}s referencing workspace resources to corresponding platform:/resource
	 *         {@link URI}s.
	 */
	public static URIConverter getURIConverter(ResourceSet resourceSet) {
		// Retrieve or create URI converter
		URIConverter uriConverter;
		if (resourceSet != null) {
			uriConverter = resourceSet.getURIConverter();
		} else {
			uriConverter = new ExtensibleURIConverterImpl();
		}
		if (ExtendedPlatform.IS_PLATFORM_RUNNING && ExtendedPlatform.IS_WORKSPACE_AVAILABLE) {
			try {
				// Initialize URI mappings
				IPath workspaceRootPath = ResourcesPlugin.getWorkspace().getRoot().getFullPath().addTrailingSeparator();
				URI workspaceRootURI = URI.createPlatformResourceURI(workspaceRootPath.toString(), true);

				IPath workspaceRootLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().addTrailingSeparator();
				URI workspaceRootLocationURI = URI.createURI(workspaceRootLocation.toString(), true);
				URI workspaceRootLocationFileURI = URI.createFileURI(workspaceRootLocation.toString());

				uriConverter.getURIMap().put(workspaceRootLocationURI, workspaceRootURI);
				uriConverter.getURIMap().put(workspaceRootLocationFileURI, workspaceRootURI);
			} catch (Exception ex) {
				// Ignore exception
			}
		}
		return uriConverter;
	}

	/**
	 * Converts given {@link URI} into an absolute file {@link URI}.
	 *
	 * @param uri
	 *            The {@link URI} to be converted.
	 * @return Absolute file {@link URI} for the given {@link URI} or given {@link URI} if no conversion is possible.
	 */
	public static URI convertToAbsoluteFileURI(URI uri) {
		Assert.isNotNull(uri);

		// Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=423286: manually convert URIs that start with a
		// Windows drive letter
		if (!uri.isRelative() && uri.scheme().matches("[A-Za-z]")) { //$NON-NLS-1$
			uri = URI.createFileURI(uri.toString());
		}

		// Try to convert given URI to absolute file URI right away
		URI convertedURI = CommonPlugin.asLocalURI(uri);

		// Resulting URI still relative?
		if (convertedURI.isRelative()) {
			// Normalize given URI and try to convert it again
			uri = getURIConverter().normalize(uri);
			convertedURI = CommonPlugin.asLocalURI(uri);
		}

		return convertedURI;
	}

	/**
	 * Converts given URI into a platform resource URI.
	 *
	 * @param uri
	 *            The {@link URI} to be converted.
	 * @return platform resource URI for the given URI or given URI if it references a location outside the workspace or
	 *         platform is not available.
	 */
	public static URI convertToPlatformResourceURI(URI uri) {
		Assert.isNotNull(uri);

		// Already a platform resource URI?
		if (uri.isPlatformResource()) {
			return uri;
		}

		// Try to convert absolute file URIs to platform resource URIs
		if (uri.isFile() && !uri.isRelative() && ExtendedPlatform.IS_PLATFORM_RUNNING && ExtendedPlatform.IS_WORKSPACE_AVAILABLE) {
			/*
			 * !! Important Note !! Use IWorkspaceRoot#getFileForLocation(IPath) rather than trying to match the given
			 * URI against the workspace root location. This enables cases to be covered where the given URI references
			 * a resource that is part of the workspace but physically (i.e., at file system level) not located under
			 * the workspace root.
			 */
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(uri.toFileString()));
			if (file != null) {
				return URI.createPlatformResourceURI(file.getFullPath().toString(), true);
			}
		}

		return getURIConverter().normalize(uri);
	}

	/**
	 * Proves if resource specified by an URI exists.
	 *
	 * @param uri
	 *            The URI to prove Returns <b>true</b> only if the URI represents a file and if this file exists.
	 */
	public static boolean exists(URI uri) {
		if (uri != null) {
			return getURIConverter().exists(uri, null);
		}
		return false;
	}

	/**
	 * Check if provided URI representing a file or object is an EMF model or not.
	 *
	 * @param uri
	 *            the URI representing a model file or model object.
	 * @return <code>true</code> if provided URI correspond to an EMF model. Otherwise <code>false</code>.
	 */
	public static boolean isEMFModelURI(URI uri) {
		String namespace = readModelNamespace(null, uri);
		return EPackage.Registry.INSTANCE.get(namespace) != null;
	}

	/**
	 * Returns the {@link URI} representing given {@link EObject object}.
	 *
	 * @param eObject
	 *            The object to be handled.
	 * @return The URI representing the provided object.
	 */
	public static URI getURI(EObject eObject) {
		return getURI(null, null, eObject, false);
	}

	/**
	 * Returns the {@link URI} representing given {@link EObject object}. The resulting URI can optionally be resolved
	 * against the URI of the resource which contains the object in question.
	 *
	 * @param eObject
	 *            The object to be handled.
	 * @param resolve
	 *            Indicates whether the object's URI should be resolved against the URI of the resource which contains
	 *            the provided model object. This is useful is cases where the native model object URI evaluates in some
	 *            sort of fragment-based URI which does not contain any information about the resource that contains the
	 *            model object (e.g., hb:/#//MyComponent/MyParameterValue). By setting resolve to true, such
	 *            fragment-based URIs will be automatically expanded to a URI that starts with the URI of the model
	 *            object's resource and is followed by the fragment of the model object's native URI (e.g.,
	 *            platform:/resource/MyProject/MyResource/#//MyComponent/MyParameterValue).
	 * @return The URI representing the provided object.
	 */
	public static URI getURI(EObject eObject, boolean resolve) {
		return getURI(null, null, eObject, resolve);
	}

	/**
	 * Returns the {@link URI} representing given {@link EObject object}. If the object is removed (i.e., not contained
	 * in any {@link Resource resource}) its URI is determined by appending the URI fragment segments obtained from the
	 * removed object and its potential direct and indirect removed containers to the base URI obtained from the
	 * provided {@link EObject old owner} and {@link EStructuralFeature old feature}. If the object is still contained
	 * in a resource its URI is computed as usual and the old owner and old feature are ignored.
	 *
	 * @param oldOwner
	 *            The owner object that is assumed to having contained the given object before it was removed.
	 * @param oldFeature
	 *            The feature through which the old owner did contain the given object before it was removed.
	 * @param eObject
	 *            The object to be handled.
	 * @return The URI representing the provided object.
	 */
	public static URI getURI(EObject oldOwner, EStructuralFeature oldFeature, EObject eObject) {
		return getURI(oldOwner, oldFeature, eObject, false);
	}

	/**
	 * Returns the {@link URI} representing given {@link EObject object}. If the object is removed (i.e., not contained
	 * in any {@link Resource resource}) its URI is determined by appending the URI fragment segments obtained from the
	 * removed object and its potential direct and indirect removed containers to the base URI obtained from the
	 * provided {@link EObject old owner} and {@link EStructuralFeature old feature}. If the object is still contained
	 * in a resource its URI is computed as usual and the old owner and old feature are ignored. In both cases, the
	 * resulting URI can optionally be resolved against the URI of the resource which does or did contain the object in
	 * question.
	 *
	 * @param oldOwner
	 *            The owner object that is assumed to having contained the given object before it was removed.
	 * @param oldFeature
	 *            The feature through which the old owner did contain the given object before it was removed.
	 * @param eObject
	 *            The object to be handled.
	 * @param resolve
	 *            Indicates whether the object's URI should be resolved against the URI of the resource which contains
	 *            the provided model object. This is useful is cases where the native model object URI evaluates in some
	 *            sort of fragment-based URI which does not contain any information about the resource that contains the
	 *            model object (e.g., hb:/#//MyComponent/MyParameterValue). By setting resolve to true, such
	 *            fragment-based URIs will be automatically expanded to a URI that starts with the URI of the model
	 *            object's resource and is followed by the fragment of the model object's native URI (e.g.,
	 *            platform:/resource/MyProject/MyResource/#//MyComponent/MyParameterValue).
	 * @return The URI representing the provided object.
	 */
	public static URI getURI(EObject oldOwner, EStructuralFeature oldFeature, EObject eObject, boolean resolve) {
		Assert.isNotNull(eObject);

		ExtendedResource extendedResource = ExtendedResourceAdapterFactory.INSTANCE.getExtendedResource(oldOwner != null ? oldOwner : eObject);
		if (extendedResource != null) {
			return extendedResource.getURI(oldOwner, oldFeature, eObject, resolve);
		} else {
			return EcoreUtil.getURI(eObject);
		}
	}

	/**
	 * Returns the id of the content type of the file behind given {@link URI}.
	 *
	 * @param uri
	 *            The {@link URI} whose content type id is to be established.
	 * @return The id of the content type of the file behind given {@link URI}, or <code>null</code> if given
	 *         {@link URI} references a resource which is no file (e.g., a folder or project), or a file that does not
	 *         exist or has no content type.
	 */
	public static String getContentTypeId(URI uri) {
		if (uri != null) {
			try {
				Map<String, ?> contentDescription = getURIConverter().contentDescription(uri, null);
				return (String) contentDescription.get(ContentHandler.CONTENT_TYPE_PROPERTY);
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
		return null;
	}

	/**
	 * Determines if {@link Resource resource} behind specified {@link URI} is read-only. Returns <code>false</code> if
	 * this resource does not exist.
	 *
	 * @param uri
	 *            The {@link URI} identifying the {@link Resource resource} to be investigated.
	 * @return <code>true</code> if {@link Resource resource} behind specified {@link URI} is read-only, and
	 *         <code>false</code> otherwise.
	 */
	public static boolean isReadOnly(URI uri) {
		if (uri != null) {
			Map<String, ?> attributes = getURIConverter().getAttributes(uri, null);
			Object readOnly = attributes.get(URIConverter.ATTRIBUTE_READ_ONLY);
			return readOnly instanceof Boolean && (Boolean) readOnly;
		}
		return false;
	}

	/**
	 * Creates the normalized form of the given {@link URI} fragment using the provided {@link Resource resource}.
	 * <p>
	 * This may, in theory, do absolutely anything. The general idea behind this feature is to support resources in
	 * which the URI fragments used to identify objects can have manifold forms. The URI fragments that refer to a given
	 * object can then no longer be assumed to be always the same. Instead they may have alternative forms and look
	 * differently from case to case (e.g., have an additional postfix in simple cases, or a completely different format
	 * in more advanced cases). The URI fragment normalization enables such manifold URI fragments to be converted into
	 * a common base form and make them comparable. There is no general assumption of what normalized URI fragments
	 * should look like except for that they always must evaluate to the same string when the original URI fragments
	 * refer to the same object.
	 * </p>
	 * <p>
	 * It is important to emphasize that normalization can result in loss of information. The normalized URI fragment
	 * should generally be used only for the comparison of the identities of the objects being referred to.
	 * </p>
	 *
	 * @param resource
	 *            The resource that contains or may contain cross-document references including the given URI fragment.
	 * @param uriFragment
	 *            The URI fragment to normalize.
	 * @return The URI fragment in its normalized form, or the original URI fragment in case it already was normalized
	 *         or the underlying resource type does not support alternative forms of URI fragments.
	 */
	public static String normalizeURIFragment(Resource resource, String uriFragment) {
		ExtendedResource extendedResource = ExtendedResourceAdapterFactory.INSTANCE.adapt(resource);
		if (extendedResource != null) {
			return extendedResource.nomalizeURIFragment(uriFragment);
		} else {
			return uriFragment;
		}
	}

	private static XMLRootElementHandler readRootElement(URIConverter uriConverter, URI uri, XMLRootElementHandler handler,
			boolean useLexicalHandler) {
		if (handler == null) {
			handler = new XMLRootElementHandler();
		}
		if (exists(uri)) {
			InputStream inputStream = null;
			try {
				uriConverter = uriConverter != null ? uriConverter : new ExtensibleURIConverterImpl();
				inputStream = uriConverter.createInputStream(uri);
				handler.parseContents(inputStream, useLexicalHandler);
			} catch (SAXException ex) {
				// Ignore parse exceptions because we might be face to non-XML files or XML files
				// which are not well-formed - that's o.k. simply return null
			} catch (IOException ex) {
				// Ignore I/O exceptions because we might be face to non-XML files or XML files
				// which are not well-formed - that's o.k. simply return null
			} catch (Exception ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			} finally {
				ExtendedPlatform.safeClose(inputStream);
			}
		}
		return handler;
	}

	/**
	 * Reads the model namespace (i.e. XML namespace) of given {@link Resource resource}. Returns a meaningful result
	 * only if given {@link Resource resource} is an XML document.
	 *
	 * @param resource
	 *            The {@link Resource resource} to investigate.
	 * @return The model namespace denoted in given {@link Resource resource} or <code>null</code> if the
	 *         {@link Resource resource} is either a non-XML file or an XML file which is not well-formed or has no
	 *         model namespace.
	 */
	public static String readModelNamespace(Resource resource) {
		if (resource != null) {
			return readModelNamespace(getURIConverter(resource.getResourceSet()), resource.getURI());
		}
		return null;
	}

	/**
	 * Reads the model namespace (i.e. XML namespace) of resource behind given {@link URI}. Returns a meaningful result
	 * only if the resource in question is an XML document.
	 *
	 * @param uriConverter
	 *            The {@link URIConverter uriConverter} used to create {@link InputStream inputstream} . May be
	 *            <code>null</code>.
	 * @param uri
	 *            The {@link URI uri} of the resource to investigate.
	 * @return The model namespace denoted in resource behind given {@link URI} or <code>null</code> if the resource in
	 *         question is either a non-XML file or an XML file which is not well-formed or has no model namespace.
	 */
	public static String readModelNamespace(URIConverter uriConverter, URI uri) {
		XMLRootElementHandler handler = readRootElement(uriConverter, uri, null, false);
		return handler.getRootElementNamespace();
	}

	/**
	 * Reads the target namespace of given {@link Resource resource}. Returns a meaningful result only if given
	 * {@link Resource resource} is an XML document.
	 *
	 * @param resource
	 *            The {@link Resource resource} to investigate.
	 * @return The target namespace denoted in given {@link Resource resource} or <code>null</code> if the
	 *         {@link Resource resource} is either a not an XML file or an XML file which is not well-formed or has no
	 *         target namespace.
	 */
	public static String readTargetNamespace(Resource resource) {
		return readTargetNamespace(resource, (String[]) null);
	}

	public static String readTargetNamespace(Resource resource, String... targetNamespaceExcludePatterns) {
		if (resource != null) {
			return readTargetNamespace(getURIConverter(resource.getResourceSet()), resource.getURI(), targetNamespaceExcludePatterns);
		}
		return null;
	}

	/**
	 * Reads the target namespace of the resource behind given {@link URI}. Returns a meaningful result only if the
	 * resource in question is an XML document.
	 *
	 * @param uriConverter
	 *            The {@link URIConverter uriConverter} used to create {@link InputStream input stream} . May be
	 *            <code>null</code>.
	 * @param uri
	 *            The {@link URI uri} of the resource to investigate.
	 * @return The target namespace denoted in resource behind given {@link URI} or <code>null</code> if the resource in
	 *         question is either a not an XML file or an XML file which is not well-formed or has no target namespace.
	 */
	public static String readTargetNamespace(URIConverter uriConverter, URI uri) {
		return readTargetNamespace(uriConverter, uri, (String[]) null);
	}

	public static String readTargetNamespace(URIConverter uriConverter, URI uri, String... targetNamespaceExcludePatterns) {
		XMLRootElementHandler handler = new XMLRootElementHandler();
		if (targetNamespaceExcludePatterns != null) {
			handler.setTargetNamespaceExcludePatterns(targetNamespaceExcludePatterns);
		}
		readRootElement(uriConverter, uri, handler, false);
		return handler.getTargetNamespace();
	}

	/**
	 * Retrieves the XML comments located above the root element in given {@link Resource resource}. Returns a
	 * meaningful result only if given {@link Resource resource} is an XML document.
	 *
	 * @param resource
	 *            The {@link Resource resource} to investigate.
	 * @return Collection of strings representing the retrieved XML comments or empty collection if no such could be
	 *         found.
	 */
	public static Collection<String> readRootElementComments(Resource resource) {
		if (resource != null) {
			return readRootElementComments(getURIConverter(resource.getResourceSet()), resource.getURI());
		}
		return Collections.emptyList();
	}

	/**
	 * Retrieves the XML comments located above the root element in resource behind given {@link URI}. Returns a
	 * meaningful result only if the resource in question is an XML document.
	 *
	 * @param uriConverter
	 *            The {@link URIConverter uriConverter} used to create {@link InputStream input stream} . May be
	 *            <code>null</code>.
	 * @param uri
	 *            The {@link URI uri} of the resource to investigate.
	 * @return Collection of strings representing the retrieved XML comments or empty collection if no such could be
	 *         found.
	 */
	public static Collection<String> readRootElementComments(URIConverter uriConverter, URI uri) {
		XMLRootElementHandler handler = readRootElement(uriConverter, uri, null, true);
		return handler.getRootElementComments();
	}

	/**
	 * Reads the XSI schema location of given {@link Resource resource} and extracts pairs of namespace and schema URIs
	 * from it (see http://www.w3.org/TR/xmlschema-0/#schemaLocation for details). Returns a meaningful result only if
	 * given {@link Resource resource} is an XML document.
	 *
	 * @param resource
	 *            The {@link Resource resource} to investigate.
	 * @return Pairs of namespace and schema URIs in given {@link Resource resource} or an empty map if the
	 *         {@link Resource resource} is either a non-XML file or an XML file which is not well-formed or has no XSI
	 *         schema location.
	 */
	public static Map<String, String> readSchemaLocationEntries(Resource resource) {
		if (resource != null) {
			return readSchemaLocationEntries(getURIConverter(resource.getResourceSet()), resource.getURI());
		}
		return Collections.emptyMap();
	}

	/**
	 * Reads the XSI schema location of the resource behind given {@link URI} and extracts pairs of namespace and schema
	 * URIs from it (see http://www.w3.org/TR/xmlschema-0/#schemaLocation for details). Returns a meaningful result only
	 * if the resource in question is an XML document.
	 *
	 * @param uriConverter
	 *            The {@link URIConverter uriConverter} used to create {@link InputStream input stream} . May be
	 *            <code>null</code>.
	 * @param uri
	 *            The {@link URI uri} of the {@link Resource resource} to investigate.
	 * @return Pairs of namespace and schema URIs in resource behind given {@link URI} or an empty map if the resource
	 *         in question is either a non-XML file or an XML file which is not well-formed or has no XSI schema
	 *         location.
	 */
	public static Map<String, String> readSchemaLocationEntries(URIConverter uriConverter, URI uri) {
		XMLRootElementHandler handler = readRootElement(uriConverter, uri, null, false);
		String schemaLocation = handler.getSchemaLocation();
		Map<String, String> schemaLocationEntries = new HashMap<String, String>();
		if (schemaLocation != null) {
			String[] schemaLocationTokens = schemaLocation.split(" "); //$NON-NLS-1$
			for (int i = 0; i + 1 < schemaLocationTokens.length; i = i + 2) {
				schemaLocationEntries.put(schemaLocationTokens[i], schemaLocationTokens[i + 1]);
			}
		}
		return schemaLocationEntries;
	}

	/**
	 * Returns a set of default options which can be used for loading a Resource.
	 *
	 * @return A set of default options for loading a Resource.
	 */
	public static Map<?, ?> getDefaultLoadOptions() {
		HashMap<Object, Object> options = new HashMap<Object, Object>(1);

		// Be fault-tolerant and enable files which are partially broken to be loaded
		options.put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);

		return options;
	}

	/**
	 * Returns a map with the default options for resource saving.
	 *
	 * @return This method will return an empty map.
	 */
	public static Map<?, ?> getDefaultSaveOptions() {
		return Collections.emptyMap();
	}

	/**
	 * Retrieves the root {@link EObject object} contained by given resource.
	 *
	 * @param resource
	 *            Some model resource
	 * @return The root object contained by given resource or <code>null</code> if the resource has not been loaded yet
	 *         or contains no or multiple root objects.
	 * @deprecated Inline this method in client code and adapt it as needed.
	 */
	@Deprecated
	public static EObject getModelRoot(Resource resource) {
		if (resource != null) {
			EList<EObject> contents = resource.getContents();
			if (contents.size() > 0) {
				return resource.getContents().get(0);
			}
		}
		return null;
	}

	/**
	 * Returns the root element of the model owned by the resource specified by the given URI. Does not explicitly ask
	 * the loading of the resource if it has not already been loaded in resource set.
	 *
	 * @param resourceSet
	 *            The resource set into which model resource must be loaded.
	 * @param uri
	 *            The URI to resolve; i.e. the URI of the model resource to load.
	 * @return The root object contained by underlying resource or <code>null</code> if the resource has not been loaded
	 *         yet or contains no or multiple root objects.
	 * @deprecated Use {@link ResourceSet#getResource(URI, boolean)} or {@link #getEObject(ResourceSet, URI)} instead.
	 */
	@Deprecated
	public static EObject getModelRoot(ResourceSet resourceSet, URI uri) {
		return loadModelRoot(resourceSet, uri, null, false);
	}

	/**
	 * Returns the root element of the model owned by the resource specified by the given URI.
	 *
	 * @param resourceSet
	 *            The resource set into which model resource must be loaded.
	 * @param uri
	 *            The URI to resolve; i.e. the URI of the model resource to load.
	 * @param loadOnDemand
	 *            If <code>true</code>, creates and loads the resource if it does not already exist.
	 * @return The root of the loaded model either if loaded or <code>loadOnDemand</code> is <code>true</code>;
	 *         <code>null</code> if underlying resource has not been loaded and <code>loadOnDemand</code> is false.
	 * @deprecated Use {@link #loadModelRoot(ResourceSet, URI, Map)} for loading models or
	 *             {@link #getModelRoot(ResourceSet, URI)} for accessing already loaded models instead.
	 */
	@Deprecated
	public static EObject getModelRoot(ResourceSet resourceSet, URI uri, boolean loadOnDemand) {
		return loadModelRoot(resourceSet, uri, null, loadOnDemand);
	}

	/**
	 * Returns the root element of the model owned by the resource specified by the given URI. Asks the loading of the
	 * resource if it has not already been loaded in resource set.
	 *
	 * @param resourceSet
	 *            The resource set into which model resource must be loaded.
	 * @param uri
	 *            The URI to resolve; i.e. the URI of the model resource to load.
	 * @param options
	 *            The load options. If <code>null</code>, default load options are used.
	 * @return The root object contained by the loaded model resource or <code>null</code> if resource has not been
	 *         loaded yet or contains no or multiple root objects.
	 * @deprecated Use {@link #loadResource(ResourceSet, URI, Map)} or {@link #loadEObject(ResourceSet, URI)} instead.
	 */
	@Deprecated
	public static EObject loadModelRoot(ResourceSet resourceSet, URI uri, Map<?, ?> options) {
		return loadModelRoot(resourceSet, uri, options, true);
	}

	/**
	 * Loads a model from a {@link java.io.File File} in a given {@link ResourceSet}.
	 * <p>
	 * This will return the first root of the loaded model, other roots can be accessed via the resource's content.
	 * </p>
	 *
	 * @param resourceSet
	 *            The {@link ResourceSet} to load the model in.
	 * @param file
	 *            {@link java.io.File File} containing the model to be loaded.
	 * @param options
	 *            Optional custom load options to be used for loading the resource. May be set to <code>null</code> if
	 *            not such are needed.
	 * @return The root object contained by given file or <code>null</code> if the file has not been loaded yet or
	 *         contains no or multiple root objects.
	 * @deprecated Use {@link #loadResource(ResourceSet, File, Map)} or {@link #loadEObject(ResourceSet, URI)} instead.
	 */
	@Deprecated
	public static EObject loadModelRoot(ResourceSet resourceSet, File file, Map<?, ?> options) throws IOException {
		Assert.isNotNull(file);
		return loadModelRoot(resourceSet, URI.createFileURI(file.getPath()), options);
	}

	/**
	 * @deprecated Use {@link #getEObject(ResourceSet, URI)} instead.
	 */
	@Deprecated
	public static EObject getModelFragment(ResourceSet resourceSet, URI uri) {
		return getEObject(resourceSet, uri);
	}

	/**
	 * Returns the element of the model owned by the resource specified by the given URI and pointed by the given
	 * fragment.
	 *
	 * @param resourceSet
	 *            The resource set into which model resource must be loaded.
	 * @param uri
	 *            The URI to resolve; i.e. the URI of the model resource to load.
	 * @param loadOnDemand
	 *            If <code>true</code>, creates and loads the resource if it does not already exist.
	 * @return The element of the loaded model pointed by the fragment of the uri either if resource is loaded or
	 *         loadOnDemand is <code>true</code>; <code>null</code> if underlying resource has not been loaded and
	 *         <code>loadOnDemand</code> is <code>false</code>.
	 * @deprecated Use {@link #loadEObject(ResourceSet, URI)} for loading model fragments or
	 *             {@link #getEObject(ResourceSet, URI)} for accessing already loaded model fragments instead.
	 */
	@Deprecated
	public static EObject getModelFragment(ResourceSet resourceSet, URI uri, boolean loadOnDemand) {
		return loadEObject(resourceSet, uri, loadOnDemand);
	}

	/**
	 * @deprecated Use {@link #loadEObject(ResourceSet, URI)} instead.
	 */
	@Deprecated
	public static EObject loadModelFragment(ResourceSet resourceSet, URI uri) {
		return loadEObject(resourceSet, uri);
	}

	/**
	 * Retrieves the model {@link EObject object} referenced by provided {@link URI} from given {@link ResourceSet
	 * resource set}. Returns <code>null</code> if the {@link Resource resource} containing the model object referenced
	 * by the URI has not yet been loaded into the resource set.
	 *
	 * @param resourceSet
	 *            The resource set from which the model object is to be retrieved.
	 * @param uri
	 *            The URI that identifies the model object to be retrieved.
	 * @return The model object from given resource set referenced by provided URI or <code>null</code> if the
	 *         referenced model object does not exist in underlying resource or the latter has not yet been loaded into
	 *         the resource set.
	 */
	public static EObject getEObject(ResourceSet resourceSet, URI uri) {
		return loadEObject(resourceSet, uri, false);
	}

	/**
	 * Retrieves the model {@link EObject object} referenced by provided {@link URI} from given {@link ResourceSet
	 * resource set}. Loads the {@link Resource resource} containing the model object referenced by the URI into the
	 * resource set if this has not yet been done.
	 *
	 * @param resourceSet
	 *            The resource set from which the model object is to be retrieved.
	 * @param uri
	 *            The URI that identifies the model object to be retrieved.
	 * @return The model object from given resource set referenced by provided URI or <code>null</code> if referenced
	 *         model object does not exist in underlying resource.
	 */
	public static EObject loadEObject(ResourceSet resourceSet, URI uri) {
		return loadEObject(resourceSet, uri, true);
	}

	private static EObject loadEObject(ResourceSet resourceSet, URI uri, boolean loadOnDemand) {
		Assert.isNotNull(uri);

		if (uri.hasFragment()) {
			// Create new ResourceSet if none has been provided
			if (resourceSet == null) {
				resourceSet = new ResourceSetImpl();
			}

			// Try to convert given URI to platform:/resource URI if not yet so
			/*
			 * !! Important Note !! This is necessary in order to avoid that resources which are located inside the
			 * workspace get loaded multiple times just because they are referenced by URIs with different schemes. If
			 * given resource set were an instance of ResourceSetImpl this extra conversion wouldn't be necessary.
			 * org.eclipse.emf.ecore.resource.ResourceSet.getResource(URI, boolean) normalizes and compares given URI
			 * and to normalized copies of URIs of already present resources and thereby avoids multiple loading of same
			 * resources on its own. This is however not true when ExtendedResourceSetImpl or a subclass of it is used.
			 * Herein, URI normalization and comparison has been removed from
			 * org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl.getResource(URI, boolean) in order to increase
			 * runtime performance.
			 */
			if (!uri.isPlatform()) {
				uri = convertToPlatformResourceURI(uri);
			}

			return resourceSet.getEObject(uri, loadOnDemand);
		}
		return null;
	}

	/**
	 * Returns the {@linkplain Resource resource} corresponding to the specified {@linkplain Object object}.
	 * <p>
	 * The supported object types are:
	 * <ul>
	 * <li>{@linkplain org.eclipse.emf.ecore.resource.Resource}</li>
	 * <li>{@linkplain org.eclipse.emf.ecore.EObject}</li>
	 * <li>{@linkplain org.eclipse.emf.ecore.util.FeatureMap.Entry}</li>
	 * <li>{@linkplain org.eclipse.emf.edit.provider.IWrapperItemProvider}</li>
	 * </ul>
	 * <p>
	 * If the type of the specified object does not belongs to that list of supported types, <code>null</code> is
	 * returned.
	 *
	 * @param object
	 *            The object from which a resource must be returned.
	 * @return The underlying resource from the given object.
	 */
	public static Resource getResource(Object object) {
		if (object instanceof Resource) {
			return (Resource) object;
		} else if (object instanceof EObject) {
			return getResource((EObject) object);
		} else if (object instanceof IWrapperItemProvider) {
			return getResource((IWrapperItemProvider) object);
		} else if (object instanceof FeatureMap.Entry) {
			return getResource((FeatureMap.Entry) object);
		} else if (object instanceof TransientItemProvider) {
			return getResource((TransientItemProvider) object);
		}
		return null;
	}

	/**
	 * Retrieves the {@linkplain Resource resource} corresponding to the given {@link EObject object}.
	 *
	 * @param eObject
	 *            The {@linkplain EObject object} whose {@link Resource resource} is to be returned.
	 * @return The resource corresponding to the specified {@link EObject object}.
	 */
	public static Resource getResource(final EObject eObject) {
		if (eObject != null) {
			return eObject.eResource();
		}
		return null;
	}

	/**
	 * Retrieves the {@linkplain Resource resource} owning the given {@link IWrapperItemProvider provider}.
	 * <p>
	 * First retrieves the owner of the {@link IWrapperItemProvider provider}; then, if owner is an {@linkplain EObject}
	 * returns its resource, else delegates to {@linkplain #getResource(Object)}.
	 *
	 * @param provider
	 *            The {@linkplain IWrapperItemProvider} whose resource must be returned.
	 * @return The resource containing the specified {@link IWrapperItemProvider provider}; <code>null</code> if that
	 *         provider is <code>null</code>.
	 */
	public static Resource getResource(final IWrapperItemProvider provider) {
		if (provider != null) {
			Object owner = provider.getOwner();
			if (owner instanceof EObject) {
				return ((EObject) owner).eResource();
			} else {
				Object unwrapped = AdapterFactoryEditingDomain.unwrap(provider);
				return getResource(unwrapped);
			}
		}
		return null;
	}

	/**
	 * Retrieves the {@linkplain Resource resource} matching the given {@link FeatureMap.Entry entry}.
	 * <p>
	 * First unwraps the {@link FeatureMap.Entry entry}; then, delegates to {@linkplain #getResource(Object)}.
	 *
	 * @param entry
	 *            The {@linkplain FeatureMap.Entry} whose underlying resource must be returned.
	 * @return The resource under the specified {@link FeatureMap.Entry entry}.
	 */
	public static Resource getResource(FeatureMap.Entry entry) {
		Object unwrapped = AdapterFactoryEditingDomain.unwrap(entry);
		return getResource(unwrapped);
	}

	/**
	 * Retrieves the {@linkplain Resource resource} owning the given {@link TransientItemProvider provider}.
	 * <p>
	 * First retrieves the owner of the {@link TransientItemProvider provider}; then, if owner is an
	 * {@linkplain EObject} returns its resource, else delegates to {@linkplain #getResource(Object)}.
	 *
	 * @param provider
	 *            The {@linkplain TransientItemProvider} whose resource must be returned.
	 * @return The resource containing the specified {@link IWrapperItemProvider provider}; <code>null</code> if that
	 *         provider is <code>null</code>.
	 */
	public static Resource getResource(TransientItemProvider provider) {
		if (provider != null) {
			Notifier target = provider.getTarget();
			if (target instanceof EObject) {
				return ((EObject) target).eResource();
			}
		}
		return null;
	}

	/**
	 * Returns the contents of given Resource. If the provided Resource is
	 * <tt>null<tt> the method will return an empty list.
	 *
	 * &#64;param resource
	 * &#64;return The content of the given <code>resource</code> or an empty list if no Resource is provided.
	 */
	public static EList<EObject> getResourceContents(Resource resource) {
		if (resource != null) {
			return resource.getContents();
		}
		return new BasicEList<EObject>(0);
	}

	/**
	 * Loads the {@link Resource resource} referred to by given {@link URI}.
	 *
	 * @param resourceSet
	 *            The {@link ResourceSet resource set} that will contain the resource when it has been loaded.
	 * @param uri
	 *            The URI of the resource to be loaded.
	 * @param options
	 *            Optional custom load options to be used for loading the resource. May be set to <code>null</code> if
	 *            not such are needed.
	 * @return The resource referred to by given URI.
	 * @see #loadResource(ResourceSet, File, Map)
	 */
	public static Resource loadResource(ResourceSet resourceSet, URI uri, Map<?, ?> options) {
		Assert.isNotNull(uri);
		return loadResource(resourceSet, uri, options, true);
	}

	/**
	 * Loads the {@link Resource resource} referred to by given {@link java.io.File file}.
	 *
	 * @param resourceSet
	 *            The {@link ResourceSet resource set} that will contain the resource when it has been loaded.
	 * @param file
	 *            The file representing the resource to be loaded.
	 * @param options
	 *            Custom load options. If <code>null</code>, default load options are used.
	 * @return The resource referred to by given file.
	 * @see #loadResource(ResourceSet, URI, Map)
	 */
	public static Resource loadResource(ResourceSet resourceSet, File file, Map<?, ?> options) throws IOException {
		Assert.isNotNull(file);
		return loadResource(resourceSet, URI.createFileURI(file.getPath()), options, true);
	}

	/**
	 * Proves if the model with the specified {@link URI uri} is already loaded into the given {@link ResourceSet
	 * resourceSet}.
	 *
	 * @param resourceSet
	 *            The resource set to search resource in.
	 * @param uri
	 *            The URI of the concerned resource.
	 * @return
	 *         <ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> if resource set contains the model with specified the URI;</li>
	 *         <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	public static boolean isResourceLoaded(ResourceSet resourceSet, URI uri) {
		if (resourceSet != null && uri != null) {
			Resource resource = resourceSet.getResource(uri, false);
			return resource != null && resource.isLoaded();
		}
		return false;
	}

	/**
	 * Returns the name of the model behind provided model object.
	 *
	 * @param notifier
	 *            Can either be an EObject or a Resource.
	 * @return The name of the model specified by the given <code>notifier</code>.
	 */
	public static String getModelName(Notifier notifier) {
		EObject modelContent = null;
		if (notifier instanceof EObject) {
			modelContent = (EObject) notifier;
		}
		if (notifier instanceof Resource) {
			modelContent = ((Resource) notifier).getContents().iterator().next();
		}
		if (modelContent != null) {
			String modelPackageName = modelContent.eClass().getEPackage().getName();
			return modelPackageName.substring(0, 1).toUpperCase() + modelPackageName.substring(1);
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Create the new model given by the {@link EObject content} object parameter. The method will create a new Resource
	 * specified by a given URI and content type id. The new created resource will be added to a given ResourceSet.
	 *
	 * @param resourceSet
	 *            The ResourceSet to which the new Resource is added.
	 * @param uri
	 *            The URI specifying the location to which the Resource is to be saved to.
	 * @param contentTypeId
	 *            The id of the content type of which new created Resource shall be of.
	 * @param content
	 *            Can either be an EObject or a Resource.
	 * @since 0.7.0
	 */
	public static Resource addNewModelResource(ResourceSet resourceSet, URI uri, String contentTypeId, EObject content) {
		return addNewModelResource(resourceSet, uri, contentTypeId, Collections.singletonList(content));
	}

	/**
	 * Create the new model given by the list of {@link EObject content} objects parameter. The method will create a new
	 * Resource specified by a given URI and content type id. The new created resource will be added to a given
	 * ResourceSet.
	 *
	 * @param resourceSet
	 *            The ResourceSet to which the new Resource is added.
	 * @param uri
	 *            The URI specifying the location to which the Resource is to be saved to.
	 * @param contentTypeId
	 *            The id of the content type of which new created Resource shall be of.
	 * @param contents
	 *            The set of EObjects to be use as model root objects.
	 * @since 0.9.0
	 */
	public static Resource addNewModelResource(ResourceSet resourceSet, URI uri, String contentTypeId, List<EObject> contents) {
		if (uri != null && contents != null) {
			try {
				// Create new ResourceSet if none has been provided
				if (resourceSet == null) {
					resourceSet = new ResourceSetImpl();
				}

				// Unload and remove model resource if it is already loaded
				Resource resource = resourceSet.getResource(uri, false);
				if (resource != null) {
					try {
						unloadResource(resource);
					} catch (Exception ex) {
						PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
					}
				}

				// Create and add new model resource to the resourceSet
				resource = resourceSet.createResource(uri, contentTypeId);
				if (resource != null) {
					resource.getContents().addAll(contents);
				}
				return resource;
			} catch (Exception ex) {
				throw new WrappedException(ex);
			}
		}
		return null;
	}

	/**
	 * The {@link Resource} provided as argument will be added to the given ResourceSet if it is not already inside.
	 *
	 * @param resourceSet
	 *            The resourceSet where to add resources
	 * @param resource
	 *            The resource to add
	 * @since 0.7.0
	 */
	public static void addModelResource(ResourceSet resourceSet, Resource resource) {
		if (resource != null) {
			// Create new ResourceSet if none has been provided
			if (resourceSet == null) {
				resourceSet = new ResourceSetImpl();
			}

			// Add resource to resourceSet if not already present
			if (resourceSet.getResource(resource.getURI(), false) == null) {
				resourceSet.getResources().add(resource);
			}
		}
	}

	/**
	 * Saves the new model given by the {@link EObject content} object parameter. The method will create a new Resource
	 * specified by a given URI and content type id. The new created resource will be added to a given ResourceSet and
	 * saved.
	 *
	 * @param resourceSet
	 *            The ResourceSet to which the new Resource is added.
	 * @param uri
	 *            The URI specifying the location to which the Resource is to be saved to.
	 * @param contentTypeId
	 *            The id of the content type of which new created Resource shall be of.
	 * @param content
	 *            Can either be an EObject or a Resource.
	 * @param options
	 *            The save options.
	 * @see #getDefaultSaveOptions()
	 */
	public static void saveNewModelResource(ResourceSet resourceSet, URI uri, String contentTypeId, EObject content, Map<?, ?> options) {
		saveNewModelResource(resourceSet, uri, contentTypeId, Collections.singletonList(content), options);
	}

	/**
	 * Saves the new model given by the list of {@link EObject content} objects parameter. The method will create a new
	 * Resource specified by a given URI and content type id. The new created resource will be added to a given
	 * ResourceSet and saved.
	 *
	 * @param resourceSet
	 *            The ResourceSet to which the new Resource is added.
	 * @param uri
	 *            The URI specifying the location to which the Resource is to be saved to.
	 * @param contentTypeId
	 *            The id of the content type of which new created Resource shall be of.
	 * @param contents
	 *            The set of EObjects to be use as model root objects.
	 * @param options
	 *            The save options.
	 * @see #getDefaultSaveOptions()
	 * @since 0.9.0
	 */
	public static void saveNewModelResource(ResourceSet resourceSet, URI uri, String contentTypeId, List<EObject> contents, Map<?, ?> options) {
		// Create new model resource and add it to the provided ResourceSet
		Resource resource = addNewModelResource(resourceSet, uri, contentTypeId, contents);

		// Save the newly created resource
		saveModelResource(resource, options);
	}

	/**
	 * Saves the specified <code>resource</code>.
	 *
	 * @param resource
	 *            The {@link Resource resource} to be saved.
	 * @param options
	 *            The save options.
	 * @see #getDefaultSaveOptions()
	 */
	public static void saveModelResource(Resource resource, Map<?, ?> options) {
		if (resource != null) {
			try {
				resource.save(options);
			} catch (Exception ex) {
				// Record exception as error on resource
				/*
				 * !! Important Note !! The main intention behind doing so is to enable the exception to be converted to
				 * a problem marker by the resource problem handler later on (see
				 * org.eclipse.sphinx.emf.internal.resource.ResourceProblemHandler#resourceChanged(IResourceChangeEvent)
				 * for details).
				 */
				Throwable cause = ex.getCause();
				Exception exception = cause instanceof Exception ? (Exception) cause : ex;
				URI uri = resource.getURI();
				resource.getErrors().add(new XMIException(NLS.bind(Messages.error_problemOccurredWhenSavingResource, uri.toString()), exception,
						uri.toString(), 1, 1));

				// Re-throw exception
				throw new WrappedException(ex);
			}
		}
	}

	/**
	 * Unloads given {@link Resource resource} and removes it from underlying {@link ResourceSet resourceSet}.
	 *
	 * @param resource
	 *            The resource to be unloaded.
	 */
	public static void unloadResource(Resource resource) {
		unloadResource(resource, false);
	}

	/**
	 * Unloads given {@link Resource resource} and removes it from underlying {@link ResourceSet resourceSet}.
	 *
	 * @param resource
	 *            The resource to be unloaded.
	 * @param memoryOptimized
	 *            Will activate the memory optimization option for unloading the resource. This is only available if the
	 *            resource is an XMLResource.
	 */
	public static void unloadResource(Resource resource, boolean memoryOptimized) {
		if (resource != null) {
			try {
				// Perform resource unload, either memory optimized or normally
				if (memoryOptimized) {
					ExtendedResource extendedResource = ExtendedResourceAdapterFactory.INSTANCE.adapt(resource);
					if (extendedResource != null) {
						Map<Object, Object> defaultLoadOptions = extendedResource.getDefaultLoadOptions();
						defaultLoadOptions.put(ExtendedResource.OPTION_UNLOAD_MEMORY_OPTIMIZED, Boolean.TRUE);
					}
				}
				resource.unload();
			} catch (Exception ex) {
				throw new WrappedException(ex);
			} finally {
				// Remove unloaded resource from ResourceSet
				ResourceSet resourceSet = resource.getResourceSet();
				if (resourceSet != null) {
					resourceSet.getResources().remove(resource);
				}

				// Remove all adapters from unloaded resource
				resource.eAdapters().clear();
			}
		}
	}

	/**
	 * Unloads the resource with the specified URI from the given resource set.
	 *
	 * @param resourceSet
	 *            A resource set from which the model's resource should be unloaded.
	 * @param uri
	 *            The URI of the resource to unload.
	 */
	public static void unloadResource(ResourceSet resourceSet, URI uri) {
		unloadResource(resourceSet, uri, false);
	}

	/**
	 * Unloads the resource with the specified URI from the given resource set.
	 * <p>
	 * It is recommended to call this method inside a write-transaction (see
	 * {@link EcorePlatformUtil#unloadFile(ResourceSet, IPath)}).
	 *
	 * @param resourceSet
	 *            A resource set from which the model's resource should be unloaded.
	 * @param uri
	 *            The URI of the resource to unload.
	 * @param memoryOptimized
	 *            Will activate the memory optimization option for unloading the resource. This is only available if the
	 *            resource is an XMLResource.
	 */
	public static void unloadResource(ResourceSet resourceSet, URI uri, boolean memoryOptimized) {
		if (resourceSet != null && uri != null) {
			// Get resource and unload it
			Resource resource = resourceSet.getResource(uri, false);
			unloadResource(resource, memoryOptimized);
		}
	}

	/**
	 * Parses {@link Resource resource} with given {@link URI uri} and validates it against XSD schema with specified
	 * {@link URL url}. Raises an exception if the {@link Resource resource}'s content is not compliant with respect to
	 * XSD schema.
	 *
	 * @param uri
	 *            The {@link URI uri} of the {@link Resource resource} to be validated.
	 * @param schemaURL
	 *            The {@link URL url} of the XSD schema to be used for validation.
	 */
	public static void validate(URI uri, URL schemaURL) throws SAXException, IOException {
		Assert.isNotNull(uri);

		// 1. Lookup a factory for the W3C XML Schema language
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// 2. Compile the schema
		Schema schema = factory.newSchema(schemaURL);

		// 3. Get a validator from the schema
		Validator validator = schema.newValidator();

		// 4. Load the resource and validate it
		InputStream stream = null;
		try {
			stream = URIConverter.INSTANCE.createInputStream(uri);
			Source source = new StreamSource(stream);
			validator.validate(source);
		} finally {
			ExtendedPlatform.safeClose(stream);
		}
	}

	/**
	 * Loads from resource set the resource specified by the given URI, or try to load the URI if not in resource set.
	 *
	 * @param resourceSet
	 *            The resource set into which model resource must be loaded.
	 * @param uri
	 *            The URI to resolve; <em>i.e.</em> the URI of the model resource to load.
	 * @param options
	 *            The loading options.
	 * @param loadOnDemand
	 *            If <code>true</code>, creates and loads the resource if it does not already exist.
	 * @return The resource referred to by given URI or <code>null</code> if the resource has not been loaded yet and
	 *         demand loading is not required.
	 */
	private static Resource loadResource(ResourceSet resourceSet, URI uri, Map<?, ?> options, boolean loadOnDemand) {
		Assert.isNotNull(uri);

		// Create new ResourceSet if none has been provided
		if (resourceSet == null) {
			resourceSet = new ResourceSetImpl();
		}

		// Try to convert given URI to platform:/resource URI if not yet so
		/*
		 * !! Important Note !! This is necessary in order to avoid that resources which are located inside the
		 * workspace get loaded multiple times just because they are referenced by URIs with different schemes. If given
		 * resource set were an instance of ResourceSetImpl this extra conversion wouldn't be necessary.
		 * org.eclipse.emf.ecore.resource.ResourceSet.getResource(URI, boolean) normalizes and compares given URI and to
		 * normalized copies of URIs of already present resources and thereby avoids multiple loading of same resources
		 * on its own. This is however not true when ExtendedResourceSetImpl or a subclass of it is used. Herein, URI
		 * normalization and comparison has been removed from
		 * org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl.getResource(URI, boolean) in order to increase
		 * runtime performance.
		 */
		if (!uri.isPlatform()) {
			uri = convertToPlatformResourceURI(uri);
		}

		// Just get model resource if it is already loaded
		Resource resource = resourceSet.getResource(uri, false);

		// Load it using specified options if not done so yet and a demand load has been requested
		if ((resource == null || !resource.isLoaded()) && loadOnDemand) {
			if (exists(uri)) {
				if (resource == null) {
					String contentType = getContentTypeId(uri);
					resource = resourceSet.createResource(uri, contentType);
				}
				if (resource != null) {
					try {
						// Capture errors and warnings encountered during resource creation
						/*
						 * !! Important note !! This is necessary because the resource's errors and warnings are
						 * automatically cleared when the loading begins. Therefore, if we don't retrieve them at this
						 * point all previously encountered errors and warnings would be lost (see
						 * org.eclipse.emf.ecore.resource.impl.ResourceImpl.load(InputStream, Map<?, ?>) for details)
						 */
						List<Resource.Diagnostic> creationErrors = new ArrayList<Resource.Diagnostic>(resource.getErrors());
						List<Resource.Diagnostic> creationWarnings = new ArrayList<Resource.Diagnostic>(resource.getWarnings());

						// Load resource
						resource.load(options);

						// Make sure that no empty resources are kept in resource set
						if (resource.getContents().isEmpty()) {
							unloadResource(resource, true);
						}

						// Restore creation time errors and warnings
						resource.getErrors().addAll(creationErrors);
						resource.getWarnings().addAll(creationWarnings);
					} catch (Exception ex) {
						// Make sure that no empty resources are kept in resource set
						if (resource.getContents().isEmpty()) {
							// Capture errors and warnings encountered during resource load attempt
							/*
							 * !! Important note !! This is necessary because the resource's errors and warnings are
							 * automatically cleared when it gets unloaded. Therefore, if we didn't retrieve them at
							 * this point all errors and warnings encountered during loading would be lost (see
							 * org.eclipse.emf.ecore.resource.impl.ResourceImpl.doUnload() for details)
							 */
							List<Resource.Diagnostic> loadErrors = new ArrayList<Resource.Diagnostic>(resource.getErrors());
							List<Resource.Diagnostic> loadWarnings = new ArrayList<Resource.Diagnostic>(resource.getWarnings());

							// Make sure that resource gets unloaded and removed from resource set again
							try {
								unloadResource(resource, true);
							} catch (Exception e) {
								// Log unload problem in Error Log but don't let it go along as runtime exception. It is
								// most likely just a consequence of the load problems encountered before and therefore
								// should not prevent those from being restored as errors and warnings on resource.
								PlatformLogUtil.logAsError(Activator.getPlugin(), e);
							}

							// Restore load time errors and warnings on resource
							/*
							 * !! Important Note !! The main intention behind restoring recorded errors and warnings on
							 * the already unloaded resource is to enable these errors/warnings to be converted to
							 * problem markers by the resource problem handler later on (see
							 * org.eclipse.sphinx.emf.internal.resource.ResourceProblemHandler#resourceSetChanged(
							 * ResourceSetChangeEvent)) for details).
							 */
							resource.getErrors().addAll(loadErrors);
							resource.getWarnings().addAll(loadWarnings);
						}

						// Record exception as error on resource
						Throwable cause = ex.getCause();
						Exception exception = cause instanceof Exception ? (Exception) cause : ex;
						resource.getErrors().add(new XMIException(NLS.bind(Messages.error_problemOccurredWhenLoadingResource, uri.toString()),
								exception, uri.toString(), 1, 1));

						// Re-throw exception
						throw new WrappedException(ex);
					}
				}
			}
		}
		return resource;
	}

	/**
	 * Loads the model from the resource specified by the given URI and returns its root element.
	 *
	 * @param resourceSet
	 *            The resource set into which model resource must be loaded.
	 * @param uri
	 *            The URI to resolve; <em>i.e.</em> the URI of the model resource to load.
	 * @param options
	 *            The load options. If <code>null</code>, default load options are used.
	 * @param loadOnDemand
	 *            If <code>true</code>, creates and loads the resource if it does not already exist.
	 * @return The root object contained in resource referred to by given URI or <code>null</code> if the resource has
	 *         not been loaded yet and demand loading is not required or if resource contains no or multiple root
	 *         objects.
	 * @deprecated Use {@link #loadResource(ResourceSet, URI, Map, boolean)} or
	 *             {@link #loadEObject(ResourceSet, URI, boolean)} instead.
	 */
	@Deprecated
	private static EObject loadModelRoot(ResourceSet resourceSet, URI uri, Map<?, ?> options, boolean loadOnDemand) {
		// Use default load options when no specified
		if (options == null) {
			options = getDefaultLoadOptions();
		}

		// Load resource from resource set
		Resource resource = loadResource(resourceSet, uri, options, loadOnDemand);

		// Obtain and return resource content
		return getModelRoot(resource);
	}
}
