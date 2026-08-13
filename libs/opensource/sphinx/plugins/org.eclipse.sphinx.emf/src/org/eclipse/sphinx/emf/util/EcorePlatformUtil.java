/**
 * <copyright>
 *
 * Copyright (c) 2008-2019 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [346715] IMetaModelDescriptor methods of MetaModelDescriptorRegistry taking EObject or Resource arguments should not start new EMF transactions
 *     itemis - [393021] ClassCastExceptions raised during loading model resources with Sphinx are ignored
 *     itemis - [409459] Enable asynchronous loading of affected models when creating or resolving references to elements in other models
 *     itemis - [409458] Enhance ScopingResourceSetImpl#getEObjectInScope() to enable cross-document references between model files with different metamodels
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [423687] Synchronize ExtendedPlatformContentHandlerImpl wrt latest changes in EMF's PlatformContentHandlerImpl
 *     itemis - [427461] Add progress monitor to resource load options (useful for loading large models)
 *     itemis - [442342] Sphinx doen't trim context information from proxy URIs when serializing proxyfied cross-document references
 *     itemis - [468171] Model element splitting service
 *     itemis - [480122] Unable to navigate from Check Validation Problem markers to corresponding model element in Model Explorer
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.emf.workspace.AbstractEMFOperation;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.emf.loading.IModelLoadService;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ModelResourceDescriptor;
import org.eclipse.sphinx.emf.saving.SaveIndicatorUtil;
import org.eclipse.sphinx.emf.scoping.IResourceScope;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.StatusUtil;
import org.xml.sax.SAXException;

/**
 * Eclipse platform utility class.
 * <p>
 * Provides a set of methods allowing to handle resources, to load or save models, to retrieve a file from a resource
 * (and vice-versa), etc.
 */
public final class EcorePlatformUtil {

	// Prevent from instantiation
	private EcorePlatformUtil() {
	}

	/**
	 * Converts given {@link IPath path} into a workspace-relative platform resource {@link URI} if possible or an
	 * absolute file {@link URI} otherwise. Returns given {@link IPath} as indeterminate (scheme-less) {@link URI} if
	 * none of both is possible.
	 *
	 * @param path
	 *            The {@link IPath path} for which to create the {@link URI}; must not be <code>null</code>.
	 * @return Workspace-relative platform resource {@link URI} or absolute file {@link URI} for given {@link IPath
	 *         path} or given {@link IPath} as indeterminate (scheme-less) {@link URI} otherwise.
	 */
	public static URI createURI(IPath path) {
		Assert.isNotNull(path);

		URI uri = URI.createURI(path.toString(), true);
		URI convertedURI = EcoreResourceUtil.convertToPlatformResourceURI(uri);
		if (!convertedURI.isPlatformResource()) {
			convertedURI = EcoreResourceUtil.convertToAbsoluteFileURI(convertedURI);
		}
		return convertedURI;
	}

	public static IPath createPath(URI uri) {
		Assert.isNotNull(uri);

		if (uri.isPlatform()) {
			return new Path(uri.toPlatformString(true)).removeTrailingSeparator();
		} else if (uri.isFile()) {
			return new Path(uri.toFileString()).removeTrailingSeparator();
		} else {
			return new Path(uri.toString()).removeTrailingSeparator();
		}
	}

	/**
	 * Converts the given {@link IPath path} into an absolute file {@linkplain URI}.
	 *
	 * @param path
	 *            The {@link Path path} for which an {@linkplain URI} is to be created.
	 * @return The absolute file {@linkplain URI} for the given {@link IPath path} or an {@link URI} corresponding to
	 *         given {@link IPath path} as is if no conversion is possible.
	 */
	public static URI createAbsoluteFileURI(IPath path) {
		URI uri = createURI(path);
		return EcoreResourceUtil.convertToAbsoluteFileURI(uri);
	}

	/**
	 * Converts the given {@link URI uri} into an absolute file location, <em>i.e.</em> into a {@linkplain IPath path}.
	 *
	 * @param uri
	 *            The file {@linkplain URI} for which an absolute file {@linkplain IPath location} must be created.
	 * @return The absolute file {@linkplain IPath location} for the given file {@link URI uri}.
	 */
	public static IPath createAbsoluteFileLocation(URI uri) {
		uri = EcoreResourceUtil.convertToAbsoluteFileURI(uri);
		String uriString;
		if (uri.isFile()) {
			uriString = uri.toFileString();
		} else {
			uriString = uri.toString();
		}
		return new Path(uriString).removeTrailingSeparator();
	}

	/**
	 * Converts the given {@link IPath path} into an absolute file {@linkplain IPath location}.
	 *
	 * @param path
	 *            The file path for which an absolute file {@linkplain IPath location} must be created.
	 * @return The absolute file {@linkplain IPath location} for the given file {@link IPath path}.
	 */
	public static IPath convertToAbsoluteFileLocation(IPath path) {
		URI uri = createAbsoluteFileURI(path);
		String uriString;
		if (uri.isFile()) {
			uriString = uri.toFileString();
		} else {
			uriString = uri.toString();
		}
		return new Path(uriString).removeTrailingSeparator();
	}

	/**
	 * Resolves given fragment-based {@link URI} against the specified {@link IPath path}. Returns the given URI as is
	 * if it is not fragment-based.
	 * <p>
	 * A fragment-based URI is a URI that doesn't contain any information about the resource that contains the model
	 * object it refers to (e.g., hb:/#//MyComponent/MyParameterValue). By calling this method, such fragment-based URIs
	 * will be expanded to a URI that starts with the specified path and is followed by the fragment of the given URI
	 * (e.g., platform:/resource/MyProject/MyResource/#//MyComponent/MyParameterValue).
	 * </p>
	 *
	 * @param uri
	 *            The fragment-based URI to be resolved.
	 * @param path
	 *            The {@link IPath path} against which the fragment-based URI is to be resolved.
	 * @return The resolved {@link URI} if given URI is a fragment-based URI, or the given URI as is otherwise.
	 */
	public static URI resolveURI(URI uri, IPath path) {
		Assert.isNotNull(uri);

		// Is given URI a fragment-based URI not knowing the resource that contains the eObject it refers to?
		if (uri.segmentCount() == 0) {
			// Form resolved URI by using the URI corresponding to specified path as prefix and the fragment of given
			// URI as postfix
			URI resourceURI = createURI(path);
			String eObjectURIFragment = uri.fragment();
			return resourceURI.appendFragment(eObjectURIFragment);
		}
		return uri;
	}

	/**
	 * Reads the model namespace (i.e. XML namespace) of given {@link IFile file}. Returns a meaningful result only if
	 * the {@link IFile file} is an XML document.
	 *
	 * @param file
	 *            The {@link IFile file} to investigate.
	 * @return The model namespace denoted in the specified {@link IFile file} or <code>null</code> if the file is
	 *         either a non-XML file or an XML file which is not well-formed or has no model namespace.
	 */
	public static String readModelNamespace(IFile file) {
		if (file != null && file.isAccessible()) {
			URI uri = createURI(file.getFullPath());
			return EcoreResourceUtil.readModelNamespace(null, uri);
		}
		return null;
	}

	/**
	 * Reads the target namespace of given {@link IFile file}. Returns a meaningful result only if given {@link IFile
	 * file} is an XML document.
	 *
	 * @param file
	 *            The {@link IFile file} to investigate.
	 * @return The target namespace denoted in given {@link IFile file} or <code>null</code> if the {@link IFile file}
	 *         is either a not an XML file or an XML file which is not well-formed or has no target namespace.
	 */
	public static String readTargetNamespace(IFile file) {
		return readTargetNamespace(file, (String[]) null);
	}

	/**
	 * @param file
	 * @param targetNamespaceExcludePatterns
	 * @return
	 */
	public static String readTargetNamespace(IFile file, String... targetNamespaceExcludePatterns) {
		if (file != null && file.isAccessible()) {
			URI uri = createURI(file.getFullPath());
			return EcoreResourceUtil.readTargetNamespace(null, uri, targetNamespaceExcludePatterns);
		}
		return null;
	}

	/**
	 * Retrieves the XML comments located above the root element in given {@link IFile file}. Returns a meaningful
	 * result only if given {@link IFile file} is an XML document.
	 *
	 * @param file
	 *            The {@link IFile file} to investigate.
	 * @return Collection of strings representing the retrieved XML comments or empty collection if no such could be
	 *         found.
	 */
	public static Collection<String> readRootElementComments(IFile file) {
		if (file != null && file.isAccessible()) {
			URI uri = createURI(file.getFullPath());
			return EcoreResourceUtil.readRootElementComments(null, uri);
		}
		return Collections.emptyList();
	}

	/**
	 * Retrieves the {@link EObject root object} of the model contained in given {@link IFile file}. Returns
	 * <code>null</code> if the {@link IFile file} has not been loaded yet or contains no or multiple root objects.
	 *
	 * @param file
	 *            The {@link IFile file} containing the model.
	 * @return The {@link EObject root object} of the model in given {@link IFile file} or <code>null</code> if the
	 *         {@link IFile file} has not been loaded yet or contains no or multiple root objects.
	 * @see #getModelRoot(TransactionalEditingDomain, IFile)
	 * @deprecated Use {@link #getResource(IFile)} or {@link #getEObject(URI)} instead.
	 */
	@Deprecated
	public static EObject getModelRoot(IFile file) {
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(file);
		if (editingDomain != null) {
			return getModelRoot(editingDomain, file);
		}
		return null;
	}

	/**
	 * Retrieves the {@link EObject root object} of the model contained in given {@link IFile file} using given
	 * {@link TransactionalEditingDomain editing domain}. Returns <code>null</code> if the {@link IFile file} has not
	 * been loaded yet or contains no or multiple root objects.
	 *
	 * @param editingDomain
	 *            The {@linkplain TransactionalEditingDomain editing domain} the specified {@link IFile file} belongs
	 *            to; must not be <code>null</code>.
	 * @param file
	 *            The {@linkplain IFile file} containing the model; must not be <code>null</code>.
	 * @return The {@linkplain EObject root object} of the model in given {@link IFile file} or <code>null</code> if the
	 *         {@linkplain IFile file} has not been loaded yet or contains no or multiple root objects.
	 * @deprecated Use {@link #getResource(IFile)} or {@link #getEObject(TransactionalEditingDomain, URI)} instead.
	 */
	@Deprecated
	public static EObject getModelRoot(final TransactionalEditingDomain editingDomain, final IFile file) {
		if (editingDomain != null && file != null) {
			try {
				return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<EObject>() {
					@Override
					public void run() {
						URI uri = createURI(file.getFullPath());
						setResult(EcoreResourceUtil.getModelRoot(editingDomain.getResourceSet(), uri));
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
		return null;
	}

	/**
	 * Loads the model contained in given {@link IFile file} using {@link EcoreResourceUtil#getDefaultLoadOptions()
	 * default load options} and returns its {@link EObject root object}. Returns <code>null</code> if the {@link IFile
	 * file} is empty.
	 * <p>
	 * Note: Calling this method involves retrieving the {@link TransactionalEditingDomain editing domain} behind the
	 * given file. In case that the {@link IFile file} has not been loaded yet this is done by analyzing the its content
	 * type. However, if it happens that the {@link IFile file}'s content type is retrieved for the very first time this
	 * operation is somewhat costly in terms of runtime performance. Therefore, when the
	 * {@link TransactionalEditingDomain editing domain} is already available prior to calling this method it is
	 * recommended to use {@link #loadModelRoot(TransactionalEditingDomain, IFile)} instead.
	 *
	 * @param file
	 *            The {@link IFile file} containing the model.
	 * @return The {@link EObject root object} of the model in given {@link IFile file} or <code>null</code> if the
	 *         {@link IFile file} is empty.
	 * @see EcoreResourceUtil#getDefaultLoadOptions()
	 * @see #loadModelRoot(TransactionalEditingDomain, IFile)
	 * @deprecated Use {@link #loadModelRoot(IFile, Map)} instead.
	 */
	@Deprecated
	public static EObject loadModelRoot(IFile file) {
		return loadModelRoot(file, EcoreResourceUtil.getDefaultLoadOptions());
	}

	/**
	 * Loads the model contained in given {@link IFile file} using given load options and returns its {@link EObject
	 * root object}. Returns <code>null</code> if the {@link IFile file} contains no or multiple root objects..
	 * <p>
	 * Note: Calling this method involves retrieving the {@link TransactionalEditingDomain editing domain} behind the
	 * given file. In case that the {@link IFile file} has not been loaded yet this is done by analyzing the its content
	 * type. However, if it happens that the {@link IFile file}'s content type is retrieved for the very first time this
	 * operation is somewhat costly in terms of runtime performance. Therefore, when the
	 * {@link TransactionalEditingDomain editing domain} is already available prior to calling this method it is
	 * recommended to use {@link #loadModelRoot(TransactionalEditingDomain, IFile, Map)} instead.
	 *
	 * @param file
	 *            The {@link IFile file} containing the model.
	 * @param options
	 *            The options to be used for loading the model.
	 * @return The {@link EObject root object} of the model in given {@link IFile file} or <code>null</code> if the
	 *         {@link IFile file} contains no or multiple root objects.
	 * @see #loadModelRoot(TransactionalEditingDomain, IFile, Map)
	 * @deprecated Use {@link #loadResource(IFile, Map)} or {@link #loadEObject(URI)} instead.
	 */
	@Deprecated
	public static EObject loadModelRoot(IFile file, Map<?, ?> options) {
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(file);
		if (editingDomain != null) {
			return loadModelRoot(editingDomain, file, options);
		}
		return null;
	}

	/**
	 * Loads the model contained in given {@link IFile file} into given {@link TransactionalEditingDomain editing
	 * domain} using {@link EcoreResourceUtil#getDefaultLoadOptions() default load options} and returns its
	 * {@link EObject root object}. Returns <code>null</code> if the {@link IFile file} contains no or multiple root
	 * objects.
	 *
	 * @param file
	 *            The {@link IFile file} containing the model.
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} the {@link IFile file} belongs to.
	 * @param options
	 *            The options to be used for loading the model.
	 * @return The {@link EObject root object} of the model in given {@link IFile file} or <code>null</code> if the
	 *         {@link IFile file} contains no or multiple root objects.
	 * @see EcoreResourceUtil#getDefaultLoadOptions()
	 * @deprecated Use {@link #loadModelRoot(TransactionalEditingDomain, IFile, Map)} instead.
	 */
	@Deprecated
	public static EObject loadModelRoot(TransactionalEditingDomain editingDomain, IFile file) {
		return loadModelRoot(editingDomain, file, EcoreResourceUtil.getDefaultLoadOptions());
	}

	/**
	 * Loads the model contained in given {@link IFile file} into given {@link TransactionalEditingDomain editing
	 * domain} using given load options and returns its {@link EObject root object}. Returns <code>null</code> if the
	 * {@link IFile file} contains no or multiple root objects.
	 *
	 * @param file
	 *            The {@link IFile file} containing the model; must not be <code>null</code>.
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} the {@link IFile file} belongs to; must not be
	 *            <code>null</code>.
	 * @param options
	 *            The options to be used for loading the model.
	 * @return The {@link EObject root object} of the model in given {@link IFile file} or <code>null</code> if the
	 *         {@link IFile file} contains no or multiple root objects.
	 * @deprecated Use {@link #loadResource(TransactionalEditingDomain, IFile, Map)} or
	 *             {@link #loadEObject(TransactionalEditingDomain, URI)} instead.
	 */
	@Deprecated
	public static EObject loadModelRoot(final TransactionalEditingDomain editingDomain, final IFile file, final Map<?, ?> options) {
		if (editingDomain != null && file != null) {
			try {
				return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<EObject>() {
					@Override
					public void run() {
						URI uri = createURI(file.getFullPath());
						setResult(EcoreResourceUtil.loadModelRoot(editingDomain.getResourceSet(), uri, options));
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
		return null;
	}

	/**
	 * Retrieves the model {@link EObject object} referenced by provided {@link URI} from the {@link ResourceSet
	 * resource set} of the {@link TransactionalEditingDomain editing domain} the URI is mapped to. Returns
	 * <code>null</code> if the {@link Resource resource} containing the model object referenced by the URI has not yet
	 * been loaded into the editing domain's resource set.
	 *
	 * @param uri
	 *            The URI that identifies the model object to be retrieved.
	 * @return The model object referenced by provided URI or <code>null</code> if referenced model object does not
	 *         exist in underlying resource or the latter has not yet been loaded into the editing domain's resource
	 *         set.
	 */
	public static EObject getEObject(URI uri) {
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(uri);
		if (editingDomain != null) {
			return getEObject(editingDomain, uri);
		}
		return null;
	}

	/**
	 * Retrieves the model {@link EObject object} referenced by provided {@link URI} from the {@link ResourceSet
	 * resource set} of given {@link TransactionalEditingDomain editing domain}. Returns <code>null</code> if the
	 * {@link Resource resource} containing the model object referenced by the URI has not yet been loaded into the
	 * editing domain's resource set.
	 *
	 * @param editingDomain
	 *            The editing domain from the resource set of which the model object is to be retrieved.
	 * @param uri
	 *            The URI that identifies the model object to be retrieved.
	 * @return The model object from given editing domain's resource set referenced by provided URI or <code>null</code>
	 *         if the referenced model object does not exist in underlying resource or the latter has not yet been
	 *         loaded into the editing domain's resource set.
	 */
	public static EObject getEObject(final TransactionalEditingDomain editingDomain, final URI uri) {
		if (editingDomain != null && uri != null) {
			try {
				return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<EObject>() {
					@Override
					public void run() {
						setResult(EcoreResourceUtil.getEObject(editingDomain.getResourceSet(), uri));
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
		return null;
	}

	/**
	 * Retrieves the model {@link EObject object} referenced by provided {@link URI} from the {@link ResourceSet
	 * resource set} of the {@link TransactionalEditingDomain editing domain} the URI is mapped to. Loads the
	 * {@link Resource resource} containing the model object referenced by the URI into the editing domain's resource
	 * set if this has not yet been done.
	 *
	 * @param uri
	 *            The URI that identifies the model object to be retrieved.
	 * @return The model object referenced by provided URI or <code>null</code> if referenced model object does not
	 *         exist in underlying resource.
	 */
	public static EObject loadEObject(URI uri) {
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(uri);
		if (editingDomain != null) {
			return loadEObject(editingDomain, uri);
		}
		return null;
	}

	/**
	 * Retrieves the model {@link EObject object} referenced by provided {@link URI} from the {@link ResourceSet
	 * resource set} of given {@link TransactionalEditingDomain editing domain}. Loads the {@link Resource resource}
	 * containing the model object referenced by the URI into the editing domain's resource set if this has not yet been
	 * done.
	 *
	 * @param editingDomain
	 *            The editing domain from the resource set of which the model object is to be retrieved.
	 * @param uri
	 *            The URI that identifies the model object to be retrieved.
	 * @return The model object from given editing domain's resource set referenced by provided URI or <code>null</code>
	 *         if referenced model object does not exist in underlying resource.
	 */
	public static EObject loadEObject(final TransactionalEditingDomain editingDomain, final URI uri) {
		if (editingDomain != null && uri != null) {
			try {
				return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<EObject>() {
					@Override
					public void run() {
						setResult(EcoreResourceUtil.loadEObject(editingDomain.getResourceSet(), uri));
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
		return null;
	}

	/**
	 * Returns the {@linkplain Resource resource} corresponding to the specified {@linkplain Object object}.
	 * <p>
	 * The supported object types are:
	 * <ul>
	 * <li>{@linkplain org.eclipse.core.resources.IFile}</li>
	 * <li>{@linkplain org.eclipse.emf.common.util.URI}</li>
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
		if (object instanceof IFile) {
			return getResource((IFile) object);
		} else if (object instanceof URI) {
			return getResource((URI) object);
		} else if (object instanceof Resource) {
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
	 * Retrieves the {@linkplain Resource resource} corresponding to the given {@link IFile file}.
	 *
	 * @param file
	 *            The {@linkplain IFile file} whose {@link Resource resource} is to be returned.
	 * @return The resource corresponding to the specified {@link IFile file}.
	 */
	public static Resource getResource(final IFile file) {
		final TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getCurrentEditingDomain(file);
		if (editingDomain != null) {
			try {
				return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<Resource>() {
					@Override
					public void run() {
						URI uri = createURI(file.getFullPath());
						setResult(editingDomain.getResourceSet().getResource(uri, false));
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
		return null;
	}

	/**
	 * Returns the {@linkplain Resource resource} corresponding to the specified {@link URI uri}.
	 *
	 * @param uri
	 *            The {@linkplain URI} of the resource to return.
	 * @return The resource corresponding to the specified {@link URI uri}.
	 */
	public static Resource getResource(final URI uri) {
		IFile file = getFile(uri);
		final TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getCurrentEditingDomain(file);
		if (editingDomain != null) {
			try {
				return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<Resource>() {
					@Override
					public void run() {
						ResourceSet resourceSet = editingDomain.getResourceSet();
						setResult(resourceSet.getResource(uri, false));
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
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
		return EcoreResourceUtil.getResource(eObject);
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
		return EcoreResourceUtil.getResource(provider);
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
		return EcoreResourceUtil.getResource(entry);
	}

	/**
	 * Retrieves the {@linkplain Resource resource} owning the given {@link TransientItemProvider provider}.
	 * <p>
	 * First retrieves the owner of the {@link TransientItemProvider provider}; then, if owner is an
	 * {@linkplain EObject} returns its resource.
	 *
	 * @param provider
	 *            The {@linkplain TransientItemProvider} whose resource must be returned.
	 * @return The resource containing the specified {@link TransientItemProvider provider}; <code>null</code> if that
	 *         provider is <code>null</code>.
	 */
	public static Resource getResource(final TransientItemProvider provider) {
		return EcoreResourceUtil.getResource(provider);
	}

	/**
	 * Loads the {@link Resource resource} referred to by given {@link IFile file}.
	 *
	 * @param file
	 *            The file from which the resource is to be loaded.
	 * @param options
	 *            Optional custom load options to be used for loading the resource. May be set to <code>null</code> if
	 *            not such are needed.
	 * @return The resource referred to by given file.
	 * @see #loadResource(TransactionalEditingDomain, IFile, Map)
	 */
	public static Resource loadResource(IFile file, Map<?, ?> options) {
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(file);
		if (editingDomain != null) {
			return loadResource(editingDomain, file, options);
		}
		return null;
	}

	/**
	 * Loads the {@link Resource resource} referred to by given {@link IFile file} into the {@link ResourceSet resource
	 * set} of given {@link TransactionalEditingDomain editing domain}.
	 *
	 * @param file
	 *            The file from which the resource is to be loaded.
	 * @param editingDomain
	 *            The editing domain into the resource set of which the file is to be loaded.
	 * @param options
	 *            Optional custom load options to be used for loading the resource. May be set to <code>null</code> if
	 *            not such are needed.
	 * @return The resource referred to by given file.
	 * @see #loadResource(IFile, Map)
	 */
	public static Resource loadResource(final TransactionalEditingDomain editingDomain, final IFile file, final Map<?, ?> options) {
		if (editingDomain != null && file != null) {
			try {
				return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<Resource>() {
					@Override
					public void run() {
						URI uri = createURI(file.getFullPath());
						setResult(EcoreResourceUtil.loadResource(editingDomain.getResourceSet(), uri, options));
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
		return null;
	}

	/**
	 * Tests if the given {@link Resource resource} is loaded in the {@link ResourceSet resource set} of given
	 * {@link TransactionalEditingDomain editingDomain}.
	 *
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} with the {@link ResourceSet resource set} to be
	 *            investigated.
	 * @param resource
	 *            The {@link Resource resource} that may or not be loaded.
	 * @return <code>true</code> if specified {@link Resource resource} is loaded in {@link ResourceSet resource set} of
	 *         given {@link TransactionalEditingDomain editingDomain}; <code>false</code> otherwise.
	 */
	public static boolean isResourceLoaded(final TransactionalEditingDomain editingDomain, final Resource resource) {
		if (editingDomain != null && resource != null) {
			try {
				return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<Boolean>() {
					@Override
					public void run() {
						setResult(EcoreResourceUtil.isResourceLoaded(editingDomain.getResourceSet(), resource.getURI()));
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
		return false;
	}

	/**
	 * Loads all persisted resources that belong to the model specified by given <code>modelDescriptor</code>.
	 *
	 * @param modelDescriptors
	 *            The {@link IModelDescriptor model} whose resources are to be loaded.
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	public static void loadModel(IModelDescriptor modelDescriptor, boolean async, IProgressMonitor monitor) {
		loadModels(Collections.singleton(modelDescriptor), async, monitor);
	}

	/**
	 * Loads all persisted resources that belong to the models specified by given <code>modelDescriptors</code>.
	 *
	 * @param modelDescriptors
	 *            The collection of {@link IModelDescriptor model}s whose resources are to be loaded.
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	public static void loadModels(Collection<IModelDescriptor> modelDescriptors, boolean async, IProgressMonitor monitor) {
		if (modelDescriptors != null && modelDescriptors.size() != 0) {

			Map<IMetaModelDescriptor, Collection<IModelDescriptor>> map = new HashMap<IMetaModelDescriptor, Collection<IModelDescriptor>>();
			for (IModelDescriptor modelDescriptor : modelDescriptors) {
				IMetaModelDescriptor mmDescriptor = modelDescriptor.getMetaModelDescriptor();
				Collection<IModelDescriptor> models = map.get(mmDescriptor);
				if (models == null) {
					models = new HashSet<IModelDescriptor>();
					map.put(mmDescriptor, models);
				}
				models.add(modelDescriptor);
			}

			for (IMetaModelDescriptor metaModelDescriptor : map.keySet()) {
				IModelLoadService service = (IModelLoadService) Platform.getAdapterManager().loadAdapter(metaModelDescriptor,
						IModelLoadService.class.getName());
				if (service != null) {
					service.loadModels(map.get(metaModelDescriptor), async, monitor);
				}
			}
		}
	}

	/**
	 * Tests if the persisted files of the given {@link IModelDescriptor model Descriptor} are loaded completely.
	 *
	 * @param modelDescriptor
	 *            The {@link IModelDescriptor model Descriptor} that may or not be loaded.
	 * @return <code>true</code> if the persisted files of the specified {@link IModelDescriptor model Descriptor} are
	 *         all loaded; <code>false</code> otherwise.
	 */
	public static boolean isModelLoaded(IModelDescriptor modelDescriptor) {
		for (IFile persistedFile : modelDescriptor.getPersistedFiles(true)) {
			if (!isFileLoaded(persistedFile)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Tests if the persisted files of the given collection of {@link IModelDescriptor model Descriptor}s are loaded
	 * completely.
	 *
	 * @param modelDescriptors
	 *            The collection of {@link IModelDescriptor model Descriptor}s that may or not be loaded.
	 * @return <code>true</code> if the persisted files of the specified collection of {@link IModelDescriptor model
	 *         Descriptor}s are all loaded; <code>false</code> otherwise.
	 */
	public static boolean areModelsLoaded(Collection<IModelDescriptor> modelDescriptors) {
		for (IModelDescriptor targetModelDescriptor : modelDescriptors) {
			if (!isModelLoaded(targetModelDescriptor)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the {@linkplain IFile file} corresponding to the specified {@linkplain Object object}.
	 * <p>
	 * The supported object types are:
	 * <ul>
	 * <li>{@linkplain org.eclipse.core.resources.IFile}</li>
	 * <li>{@linkplain org.eclipse.emf.common.util.URI}</li>
	 * <li>{@linkplain org.eclipse.emf.ecore.resource.Resource}</li>
	 * <li>{@linkplain org.eclipse.emf.ecore.EObject}</li>
	 * <li>{@linkplain org.eclipse.emf.edit.provider.IWrapperItemProvider}</li>
	 * <li>{@linkplain org.eclipse.emf.ecore.util.FeatureMap.Entry}</li>
	 * </ul>
	 *
	 * @param object
	 *            The object from which a file must be returned.
	 * @return The underlying file from the given object.
	 */
	public static IFile getFile(Object object) {
		if (object instanceof IFile) {
			return (IFile) object;
		} else if (object instanceof URI) {
			return getFile((URI) object);
		} else if (object instanceof Resource) {
			return getFile((Resource) object);
		} else if (object instanceof EObject) {
			return getFile((EObject) object);
		} else if (object instanceof IWrapperItemProvider) {
			return getFile((IWrapperItemProvider) object);
		} else if (object instanceof FeatureMap.Entry) {
			return getFile((FeatureMap.Entry) object);
		} else if (object instanceof TransientItemProvider) {
			return getFile((TransientItemProvider) object);
		}
		return null;
	}

	/**
	 * Retrieves the {@linkplain IFile file} corresponding to the given {@linkplain org.eclipse.emf.common.util.URI}.
	 *
	 * @param uri
	 *            The {@linkplain URI} of the file to return.
	 * @return The file corresponding to the specified {@link URI uri}.
	 */
	public static IFile getFile(URI uri) {
		if (uri != null && ExtendedPlatform.IS_PLATFORM_RUNNING) {
			// Create dummy resource transporting given URI
			Resource resource = new ResourceImpl(uri);

			// Create dummy resource set transporting appropriate URI converter
			ResourceSet resourceSet = new ResourceSetImpl();
			resourceSet.getResources().add(resource);
			resourceSet.setURIConverter(EcoreResourceUtil.getURIConverter());

			// Delegate to getFile(Resource) method
			return getFile(resource);
		}
		return null;
	}

	/**
	 * Retrieves the {@link IFile file} corresponding to the given <code>resource</code>.
	 *
	 * @param resource
	 *            The {@link Resource resource} for which the file is to be returned.
	 * @return The file corresponding to the specified <code>resource</code>.
	 */
	public static IFile getFile(final Resource resource) {
		if (resource != null && ExtendedPlatform.IS_PLATFORM_RUNNING) {
			return WorkspaceSynchronizer.getFile(resource);
		}
		return null;
	}

	/**
	 * Retrieves the {@linkplain IFile file} owning to the given {@linkplain org.eclipse.emf.ecore.EObject}.
	 *
	 * @param eObject
	 *            The {@linkplain EObject} whose file must be returned.
	 * @return The file containing the specified {@link EObject eObject}.
	 */
	public static IFile getFile(final EObject eObject) {
		if (eObject != null) {
			return getFile(eObject.eResource());
		}
		return null;
	}

	/**
	 * Retrieves the {@linkplain IFile file} owning the given
	 * {@linkplain org.eclipse.emf.edit.provider.IWrapperItemProvider}.
	 * <p>
	 * First retrieves the owner of the {@link IWrapperItemProvider provider}; then, if owner is an {@linkplain EObject}
	 * delegates to {@linkplain #getFile(EObject)} else delegates to {@linkplain #getFile(Object)}.
	 *
	 * @param provider
	 *            The {@linkplain IWrapperItemProvider} whose file must be returned.
	 * @return The file containing the specified {@link IWrapperItemProvider provider}; <code>null</code> if that
	 *         provider is <code>null</code>.
	 */
	public static IFile getFile(final IWrapperItemProvider provider) {
		if (provider != null) {
			Object owner = provider.getOwner();
			if (owner instanceof EObject) {
				return getFile((EObject) owner);
			} else {
				Object unwrapped = AdapterFactoryEditingDomain.unwrap(provider);
				return getFile(unwrapped);
			}
		}
		return null;
	}

	/**
	 * Retrieves the {@linkplain IFile file} matching the given {@link FeatureMap.Entry entry}.
	 * <p>
	 * First unwraps the {@link FeatureMap.Entry entry}; then, delegates to {@linkplain #getFile(Object)}.
	 *
	 * @param entry
	 *            The {@linkplain FeatureMap.Entry} whose underlying file must be returned.
	 * @return The file under the specified {@link FeatureMap.Entry entry}.
	 */
	public static IFile getFile(FeatureMap.Entry entry) {
		Object unwrapped = AdapterFactoryEditingDomain.unwrap(entry);
		return getFile(unwrapped);
	}

	/**
	 * Retrieves the {@linkplain IFile file} owning the given {@linkplain TransientItemProvider}.
	 * <p>
	 * First retrieves the owner of the {@link TransientItemProvider provider}; then, if owner is an
	 * {@linkplain EObject} delegates to {@linkplain #getFile(EObject)} else delegates to {@linkplain #getFile(Object)}.
	 *
	 * @param provider
	 *            The {@linkplain TransientItemProvider} whose file must be returned.
	 * @return The file containing the specified {@link TransientItemProvider provider}; <code>null</code> if that
	 *         provider is <code>null</code>.
	 */
	public static IFile getFile(final TransientItemProvider provider) {
		if (provider != null) {
			Object target = provider.getTarget();
			if (target instanceof EObject) {
				return getFile((EObject) target);
			}
		}
		return null;
	}

	/**
	 * Tests if the given {@link IFile file} is loaded in the {@link ResourceSet resource set} of some
	 * {@link TransactionalEditingDomain editingDomain}.
	 *
	 * @param file
	 *            The {@link IFile file} that may or not be loaded.
	 * @return <code>true</code> if specified {@link IFile file} is loaded in {@link ResourceSet resource set} of some
	 *         {@link TransactionalEditingDomain editingDomain}; <code>false</code> otherwise.
	 */
	public static boolean isFileLoaded(IFile file) {
		return WorkspaceEditingDomainUtil.getCurrentEditingDomain(file) != null;
	}

	/**
	 * Tests if the given {@link IFile file} is loaded in the {@link ResourceSet resource set} of the given
	 * {@link TransactionalEditingDomain editingDomain}.
	 *
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} with the {@link ResourceSet resource set} to be
	 *            investigated.
	 * @param file
	 *            The {@link IFile file} that may or not be loaded.
	 * @return <code>true</code> if specified {@link IFile file} is loaded in {@link ResourceSet resource set} of given
	 *         {@link TransactionalEditingDomain editingDomain}; <code>false</code> otherwise.
	 */
	public static boolean isFileLoaded(final TransactionalEditingDomain editingDomain, final IFile file) {
		if (editingDomain != null && file != null) {
			try {
				return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<Boolean>() {
					@Override
					public void run() {
						URI uri = createURI(file.getFullPath());
						setResult(EcoreResourceUtil.isResourceLoaded(editingDomain.getResourceSet(), uri));
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
		return false;
	}

	/**
	 * Returns all resources owned by the {@link IModelDescriptor model descriptor} of the contextObject.
	 *
	 * @param contextObject
	 *            The Object used to research resources in the model.
	 * @param includeReferencedModels
	 *            Determines if the {@link IModelDescriptor model descriptors} referenced by the context object's
	 *            {@link IModelDescriptor model descriptor} must be considered for the research.
	 * @return The resources in the context object's model.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInModel(Object contextObject, boolean includeReferencedModels) {
		if (contextObject instanceof IModelDescriptor) {
			return getResourcesInModel((IModelDescriptor) contextObject, includeReferencedModels);
		} else if (contextObject instanceof IFile) {
			return getResourcesInModel((IFile) contextObject, includeReferencedModels);
		} else if (contextObject instanceof URI) {
			return getResourcesInModel((URI) contextObject, includeReferencedModels);
		} else if (contextObject instanceof Resource) {
			return getResourcesInModel((Resource) contextObject, includeReferencedModels);
		} else if (contextObject instanceof EObject) {
			return getResourcesInModel((EObject) contextObject, includeReferencedModels);
		} else if (contextObject instanceof IWrapperItemProvider) {
			return getResourcesInModel((IWrapperItemProvider) contextObject, includeReferencedModels);
		} else if (contextObject instanceof FeatureMap.Entry) {
			return getResourcesInModel((FeatureMap.Entry) contextObject, includeReferencedModels);
		} else if (contextObject instanceof TransientItemProvider) {
			return getResourcesInModel((TransientItemProvider) contextObject, includeReferencedModels);
		}
		return Collections.emptyList();
	}

	/**
	 * Returns all resources owned by the {@link IModelDescriptor model descriptor} provided in argument .
	 *
	 * @param modelDescriptor
	 *            The {@link IModelDescriptor model descriptor} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IModelDescriptor model descriptors} referenced by the context
	 *            {@link IModelDescriptor model descriptor} must be considered for the research.
	 * @return The {@link Resource resource}s owned by the {@link IModelDescriptor model descriptor}.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInModel(IModelDescriptor modelDescriptor, boolean includeReferencedModels) {
		if (modelDescriptor != null) {
			return modelDescriptor.getLoadedResources(includeReferencedModels);
		}
		return Collections.emptySet();
	}

	/**
	 * Returns all resources owned by the {@link IModelDescriptor model descriptor} of the context {@link IFile file}.
	 * If the given {@link IFile file} doesn't belong to any {@link IModelDescriptor model descriptor}, resources in
	 * context{@link ResourceSet} will be returned
	 *
	 * @param contextFile
	 *            The {@link IFile file} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IModelDescriptor model descriptors} referenced by the context {@link IFile
	 *            file}'s {@link IModelDescriptor model descriptor} must be considered for the research.
	 * @return The resources in the context {@link IFile file}'s model.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInModel(IFile contextFile, boolean includeReferencedModels) {
		IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
		if (modelDescriptor != null) {
			return modelDescriptor.getLoadedResources(includeReferencedModels);
		}
		return getResourcesInContext(contextFile);
	}

	/**
	 * Returns all resources owned by the {@link IModelDescriptor model descriptor} of the context {@link URI uri}.
	 *
	 * @param contextURI
	 *            The {@link URI uri} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IModelDescriptor model descriptors} referenced by the context {@link URI uri}
	 *            's {@link IModelDescriptor model descriptor} must be considered for the research.
	 * @return The resources in the context {@link URI uri}'s model.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInModel(URI contextURI, boolean includeReferencedModels) {
		Resource contextResource = getResource(contextURI);
		return getResourcesInModel(contextResource, includeReferencedModels);
	}

	/**
	 * Returns all resources owned by the {@link IModelDescriptor model descriptor} of the context {@link Resource
	 * resource}.If the given {@link Resource contextResource} doesn't belong to any {@link IModelDescriptor model
	 * descriptor}, resources in context{@link ResourceSet} will be returned
	 *
	 * @param contextResource
	 *            The {@link Resource resource} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IModelDescriptor model descriptors} referenced by the context {@link Resource
	 *            resource} 's {@link IModelDescriptor model descriptor} must be considered for the research.
	 * @return The resources in the context {@link Resource resource}'s model.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInModel(Resource contextResource, boolean includeReferencedModels) {
		IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextResource);
		if (modelDescriptor != null) {
			return modelDescriptor.getLoadedResources(includeReferencedModels);
		}
		return getResourcesInContext(contextResource);
	}

	/**
	 * Returns all resources owned by the {@link IModelDescriptor model descriptor} of the context {@link EObject
	 * eobject}.
	 *
	 * @param contextEObject
	 *            The {@link EObject eobject} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IModelDescriptor model descriptors} referenced by the context {@link EObject
	 *            eobject}'s {@link IModelDescriptor model descriptor} must be considered for the research.
	 * @return The resources in the context {@link EObject eobject}'s model.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInModel(EObject contextEObject, boolean includeReferencedModels) {
		Resource contextResource = contextEObject.eResource();
		return getResourcesInModel(contextResource, includeReferencedModels);
	}

	/**
	 * Returns all resources owned by the {@link IModelDescriptor model descriptor} of the context
	 * {@link IWrapperItemProvider provider}.
	 *
	 * @param contextProvider
	 *            The {@link IWrapperItemProvider provider} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IModelDescriptor model descriptors} referenced by the context
	 *            {@link IWrapperItemProvider provider}'s {@link IModelDescriptor model descriptor} must be considered
	 *            for the research.
	 * @return The resources in the context {@link IWrapperItemProvider provider}'s model.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInModel(IWrapperItemProvider contextProvider, boolean includeReferencedModels) {
		Resource contextResource = getResource(contextProvider);
		return getResourcesInModel(contextResource, includeReferencedModels);
	}

	/**
	 * Returns all resources owned by the {@link IModelDescriptor model descriptor} of the context
	 * {@link FeatureMap.Entry feature map entry}.
	 *
	 * @param contextEntry
	 *            The {@link FeatureMap.Entry feature map entry} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IModelDescriptor model descriptors} referenced by the context
	 *            {@link FeatureMap.Entry feature map entry}'s {@link IModelDescriptor model descriptor} must be
	 *            considered for the research.
	 * @return The resources in the context {@link FeatureMap.Entry feature map entry}'s model.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInModel(FeatureMap.Entry contextEntry, boolean includeReferencedModels) {
		Resource contextResource = getResource(contextEntry);
		return getResourcesInModel(contextResource, includeReferencedModels);
	}

	/**
	 * Returns all resources owned by the {@link IModelDescriptor model descriptor} of the context
	 * {@link TransientItemProvider provider}.
	 *
	 * @param contextProvider
	 *            The {@link TransientItemProvider provider} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IModelDescriptor model descriptors} referenced by the context
	 *            {@link TransientItemProvider provider}'s {@link IModelDescriptor model descriptor} must be considered
	 *            for the research.
	 * @return The resources in the context {@link TransientItemProvider provider}'s model.
	 * @since 2.1
	 */
	public static Collection<Resource> getResourcesInModel(TransientItemProvider contextProvider, boolean includeReferencedModels) {
		Resource contextResource = getResource(contextProvider);
		return getResourcesInModel(contextResource, includeReferencedModels);
	}

	/**
	 * Returns all resources owned by the {@link IModelDescriptor model}s of given {@link IMetaModelDescriptor
	 * metamodel} in the context {@link IContainer container}.
	 *
	 * @param contextContainer
	 *            The {@link IContainer container} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IModelDescriptor model descriptors} referenced by the context
	 *            {@link IContainer container}'s {@link IModelDescriptor model descriptor} must be considered for the
	 *            research.
	 * @return The resources in the context {@link IContainer container}'s models.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInModels(IContainer contextContainer, IMetaModelDescriptor mmDescriptor,
			boolean includeReferencedModels) {
		Collection<Resource> resourcesInModels = new HashSet<Resource>();
		Collection<IModelDescriptor> modelDescriptors = ModelDescriptorRegistry.INSTANCE.getModels(contextContainer, mmDescriptor);
		for (IModelDescriptor modelDescriptor : modelDescriptors) {
			resourcesInModels.addAll(modelDescriptor.getLoadedResources(includeReferencedModels));
		}
		return resourcesInModels;
	}

	/**
	 * Returns all resources owned by the {@link IModelDescriptor model descriptor}s which are filtered by the
	 * {@link IMetaModelDescriptor other meta model descriptor}. These resources reside in the models other than the
	 * model of the context resource resides.
	 *
	 * @param contextResource
	 *            The {@link Resource resource} used as context object for investigation.
	 * @param otherMMDescriptor
	 *            The target {@link IMetaModelDescriptor meta model descriptor}
	 * @param includeReferencedModels
	 *            Determines if the {@link IResourceScope model resource scopes} referenced by the context
	 *            {@link Resource resource}'s {@link IResourceScope model resource scope} must be considered for the
	 *            research.
	 */
	public static Collection<Resource> getResourcesInOtherModels(Resource contextResource, IMetaModelDescriptor otherMMDescriptor,
			boolean includeReferencedModels) {
		IFile contextFile = getFile(contextResource);
		if (contextFile != null) {
			Set<Resource> otherResources = new HashSet<Resource>();
			Collection<IModelDescriptor> otherModelDescriptors = ModelDescriptorRegistry.INSTANCE.getModels(contextFile.getParent(),
					otherMMDescriptor);
			for (IModelDescriptor otherModelDescriptor : otherModelDescriptors) {
				otherResources.addAll(getResourcesInModel(otherModelDescriptor, includeReferencedModels));
			}
			return otherResources;
		}
		return Collections.emptySet();
	}

	/**
	 * Returns all resources owned by the {@link IResourceScope model resource scope} of the contextObject.
	 *
	 * @param contextObject
	 *            The Object used to research resources in the model.
	 * @param includeReferencedScopes
	 *            Determines if the {@link IResourceScope model resource scope} referenced by the context object's
	 *            {@link IResourceScope model resource scope} must be considered for the research.
	 * @return The resources in the context object's {@link IResourceScope model resource scope}.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInScope(Object contextObject, boolean includeReferencedScopes) {
		if (contextObject instanceof IModelDescriptor) {
			return getResourcesInScope((IModelDescriptor) contextObject, includeReferencedScopes);
		} else if (contextObject instanceof IFile) {
			return getResourcesInScope((IFile) contextObject, includeReferencedScopes);
		} else if (contextObject instanceof URI) {
			return getResourcesInScope((URI) contextObject, includeReferencedScopes);
		} else if (contextObject instanceof Resource) {
			return getResourcesInScope((Resource) contextObject, includeReferencedScopes);
		} else if (contextObject instanceof EObject) {
			return getResourcesInScope((EObject) contextObject, includeReferencedScopes);
		} else if (contextObject instanceof IWrapperItemProvider) {
			return getResourcesInScope((IWrapperItemProvider) contextObject, includeReferencedScopes);
		} else if (contextObject instanceof FeatureMap.Entry) {
			return getResourcesInScope((FeatureMap.Entry) contextObject, includeReferencedScopes);
		} else if (contextObject instanceof TransientItemProvider) {
			return getResourcesInScope((TransientItemProvider) contextObject, includeReferencedScopes);
		}
		return Collections.emptyList();
	}

	/**
	 * Returns all resources owned by the {@link IResourceScope model resource scope} of the {@link IModelDescriptor
	 * model descriptor} provided in argument .
	 *
	 * @param modelDescriptor
	 *            The {@link IModelDescriptor model descriptor} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IResourceScope model resource scopes} referenced by the context
	 *            {@link IResourceScope model resource scope} must be considered for the research.
	 * @return The {@link Resource resource}s owned by the {@link IResourceScope model resource scope}.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInScope(IModelDescriptor modelDescriptor, boolean includeReferencedScopes) {
		if (modelDescriptor != null) {
			TransactionalEditingDomain editingDomain = modelDescriptor.getEditingDomain();
			return modelDescriptor.getScope().getLoadedResources(editingDomain, includeReferencedScopes);
		}
		return Collections.emptySet();
	}

	/**
	 * Returns all resources owned by the {@link IResourceScope model resource scope} of the {@link IModelDescriptor
	 * model descriptor} of the context {@link IFile file}. If the given {@link IFile file} does not belong to any
	 * {@link IModelDescriptor model descriptor}, resources in context {@link ResourceSet} will be returned.
	 *
	 * @param contextFile
	 *            The {@link IFile file} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IResourceScope model resource scopes} referenced by the context {@link IFile
	 *            file}'s {@link IResourceScope model resource scope} must be considered for the research.
	 * @return The resources in the context {@link IFile file}'s {@link IResourceScope model resource scope}.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInScope(IFile contextFile, boolean includeReferencedScopes) {
		IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
		if (modelDescriptor != null) {
			return getResourcesInScope(modelDescriptor, includeReferencedScopes);
		}
		return getResourcesInContext(contextFile);
	}

	/**
	 * Returns all resources owned by the {@link IResourceScope model resource scope} of the {@link IModelDescriptor
	 * model descriptor} of the context {@link URI uri}.
	 *
	 * @param contextURI
	 *            The {@link URI uri} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IResourceScope model resource scopes} referenced by the context {@link URI
	 *            uri}'s {@link IResourceScope model resource scope} must be considered for the research.
	 * @return The resources in the context {@link URI uri}'s {@link IResourceScope model resource scope}.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInScope(URI contextURI, boolean includeReferencedScopes) {
		Resource contextResource = getResource(contextURI);
		return getResourcesInScope(contextResource, includeReferencedScopes);
	}

	/**
	 * Returns all resources owned by the {@link IResourceScope model resource scope} of the {@link IModelDescriptor
	 * model descriptor} of the context {@link Resource resource}.If the given {@link Resource contextResource} does not
	 * belong to any {@link IModelDescriptor model descriptor}, resources in context {@link ResourceSet} will be
	 * returned.
	 *
	 * @param contextResource
	 *            The {@link Resource resource} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IResourceScope model resource scopes} referenced by the context
	 *            {@link Resource resource}'s {@link IResourceScope model resource scope} must be considered for the
	 *            research.
	 * @return The resources in the context {@link Resource resource}'s {@link IResourceScope model resource scope}.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInScope(Resource contextResource, boolean includeReferencedScopes) {
		IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextResource);
		if (modelDescriptor != null) {
			return getResourcesInScope(modelDescriptor, includeReferencedScopes);
		}
		return getResourcesInContext(contextResource);
	}

	/**
	 * Returns all resources owned by the {@link IResourceScope model resource scope} of the {@link IModelDescriptor
	 * model descriptor} of the context {@link EObject eObject}.
	 *
	 * @param contextEObject
	 *            The {@link EObject eObject} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IResourceScope model resource scopes} referenced by the context
	 *            {@link EObject eObject}'s {@link IResourceScope model resource scope} must be considered for the
	 *            research.
	 * @return The resources in the context {@link EObject eObject}'s {@link IResourceScope model resource scope}.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInScope(EObject contextEObject, boolean includeReferencedScopes) {
		Resource contextResource = contextEObject.eResource();
		return getResourcesInScope(contextResource, includeReferencedScopes);
	}

	/**
	 * Returns all resources owned by the {@link IResourceScope model resource scope} of the {@link IModelDescriptor
	 * model descriptor} of the context {@link IWrapperItemProvider provider}.
	 *
	 * @param contextProvider
	 *            The {@link IWrapperItemProvider provider} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IResourceScope model resource scopes} referenced by the context
	 *            {@link IWrapperItemProvider provider}'s {@link IResourceScope model resource scope} must be considered
	 *            for the research.
	 * @return The resources in the context {@link IWrapperItemProvider provider}'s {@link IResourceScope model resource
	 *         scope}.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInScope(IWrapperItemProvider contextProvider, boolean includeReferencedScopes) {
		Resource contextResource = getResource(contextProvider);
		return getResourcesInScope(contextResource, includeReferencedScopes);
	}

	/**
	 * Returns all resources owned by the {@link IResourceScope model resource scope} of the {@link IModelDescriptor
	 * model descriptor} of the context {@link FeatureMap.Entry feature map entry}.
	 *
	 * @param contextEntry
	 *            The {@link FeatureMap.Entry feature map entry} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IResourceScope model resource scopes} referenced by the context
	 *            {@link FeatureMap.Entry feature map entry}'s {@link IResourceScope model resource scope} must be
	 *            considered for the research.
	 * @return The resources in the context {@link FeatureMap.Entry feature map entry}'s {@link IResourceScope model
	 *         resource scope}.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInScope(FeatureMap.Entry contextEntry, boolean includeReferencedScopes) {
		Resource contextResource = getResource(contextEntry);
		return getResourcesInScope(contextResource, includeReferencedScopes);
	}

	/**
	 * Returns all resources owned by the {@link IResourceScope model resource scope} of the {@link IModelDescriptor
	 * model descriptor} of the context {@link TransientItemProvider provider}.
	 *
	 * @param contextProvider
	 *            The {@link TransientItemProvider provider} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IResourceScope model resource scopes} referenced by the context
	 *            {@link TransientItemProvider provider}'s {@link IResourceScope model resource scope} must be
	 *            considered for the research.
	 * @return The resources in the context {@link TransientItemProvider provider}'s {@link IResourceScope model
	 *         resource scope}.
	 * @since 2.1
	 */
	public static Collection<Resource> getResourcesInScope(TransientItemProvider contextProvider, boolean includeReferencedScopes) {
		Resource contextResource = getResource(contextProvider);
		return getResourcesInScope(contextResource, includeReferencedScopes);
	}

	/**
	 * Returns all resources owned by the {@link IResourceScope model resource scope}s of the {@link IModelDescriptor
	 * model descriptor}s of the context {@link IContainer container}.
	 *
	 * @param contextContainer
	 *            The {@link IContainer container} used as context object for investigation.
	 * @param includeReferencedModels
	 *            Determines if the {@link IResourceScope model resource scopes} referenced by the context
	 *            {@link IContainer container}'s {@link IResourceScope model resource scope} must be considered for the
	 *            research.
	 * @return The resources in the context {@link IContainer container}'s {@link IResourceScope model resource scope}.
	 * @since 0.7.0
	 */
	public static Collection<Resource> getResourcesInScopes(IContainer contextContainer, boolean includeReferencedScopes) {
		Collection<Resource> resourcesInScopes = new HashSet<Resource>();
		Collection<IModelDescriptor> modelDescriptors = ModelDescriptorRegistry.INSTANCE.getModels(contextContainer);
		for (IModelDescriptor modelDescriptor : modelDescriptors) {
			resourcesInScopes.addAll(getResourcesInScope(modelDescriptor, includeReferencedScopes));
		}
		return resourcesInScopes;
	}

	private static Collection<Resource> getResourcesInContext(Object contextObject) {
		Resource resource = EcoreResourceUtil.getResource(contextObject);
		if (resource != null) {
			ResourceSet resourceSet = resource.getResourceSet();
			if (resourceSet != null) {
				return resourceSet.getResources();
			}
			return Collections.singletonList(resource);
		}
		return Collections.emptySet();
	}

	public static ISchedulingRule createSaveNewSchedulingRule(Collection<ModelResourceDescriptor> modelResourceDescriptors) {
		if (modelResourceDescriptors != null) {
			Set<IPath> paths = new HashSet<IPath>();
			for (ModelResourceDescriptor descriptor : modelResourceDescriptors) {
				paths.add(descriptor.getPath());
			}
			return ExtendedPlatform.createSaveNewSchedulingRule(paths);
		}
		return null;
	}

	/**
	 * Creates the {@linkplain ISchedulingRule scheduling rule} that is required for saving the specified
	 * {@link Resource resource}.
	 *
	 * @param resource
	 *            The {@linkplain Resource resource} to be saved.
	 * @return The {@linkplain ISchedulingRule scheduling rule} required for saving the given {@link Resource resource}.
	 */
	public static ISchedulingRule createSaveSchedulingRule(Resource resource) {
		if (resource != null) {
			IFile modelFile = getFile(resource);
			if (EcoreResourceUtil.exists(resource.getURI())) {
				return ExtendedPlatform.createSaveSchedulingRule(modelFile);
			}
			return ExtendedPlatform.createSaveNewSchedulingRule(modelFile);
		}
		return null;
	}

	/**
	 * Creates the {@linkplain ISchedulingRule scheduling rule} that are required for saving the specified
	 * {@link Resource resource}s.
	 *
	 * @param resources
	 *            The {@linkplain Resource resource}s to be saved (for which scheduling rules must be created.
	 * @return The scheduling {@linkplain MultiRule rule}s required for saving the given {@link Resource resources}.
	 */
	public static ISchedulingRule createSaveSchedulingRule(Collection<Resource> resources) {
		if (resources != null) {
			/*
			 * Performance optimization: Create a scheduling rule on a per resource basis only if number of resources is
			 * reasonably low.
			 */
			if (resources.size() < ExtendedPlatform.LIMIT_INDIVIDUAL_RESOURCES_SCHEDULING_RULE) {
				Set<ISchedulingRule> rules = new HashSet<ISchedulingRule>();
				for (Resource resource : resources) {
					ISchedulingRule schedulingRule = createSaveSchedulingRule(resource);
					if (schedulingRule != null) {
						rules.add(schedulingRule);
					}
				}
				return MultiRule.combine(rules.toArray(new ISchedulingRule[rules.size()]));
			} else {
				// Return workspace root as scheduling rule otherwise
				return ResourcesPlugin.getWorkspace().getRoot();
			}
		}
		return null;
	}

	private static ISchedulingRule createSaveSchedulingRule(Map<TransactionalEditingDomain, Collection<Resource>> resources) {
		if (resources != null) {
			Collection<Resource> allResources = new HashSet<Resource>();
			for (Collection<Resource> resourcesInEditingDomain : resources.values()) {
				allResources.addAll(resourcesInEditingDomain);
			}
			return createSaveSchedulingRule(allResources);
		}
		return null;
	}

	/**
	 * Add a new model {@link Resource} to the provided {@link TransactionalEditingDomain} , created by using
	 * {@link EObject content} as basis for the containing model.
	 *
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain} where to add new resource.
	 * @param path
	 *            The relative {@link IPath} of the new {@link Resource}.
	 * @param contentTypeId
	 *            The contenType ID of the contained model.
	 * @param content
	 *            The root element of the model to include in the new {@link Resource}.
	 * @param async
	 *            Boolean parameter to determine if the execution must be synchronous or asynchronous.
	 * @param monitor
	 *            The {@link IProgressMonitor}.
	 */
	public static void addNewModelResource(TransactionalEditingDomain editingDomain, IPath path, final String contentTypeId, EObject content,
			boolean async, IProgressMonitor monitor) {
		addNewModelResources(editingDomain, Collections.singletonList(new ModelResourceDescriptor(path, contentTypeId, content)), async, monitor);
	}

	/**
	 * * Add a new {@link Resource resource}s described by modelResourceDescriptors to the provided
	 * {@link TransactionalEditingDomain editingDomain} .
	 *
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain} where to add new resource.
	 * @param modelResourceDescriptors
	 *            The list of {@link ModelResourceDescriptor} describing new resources to add.
	 * @param async
	 *            Boolean parameter to determine if the execution must be synchronous or asynchronous.
	 * @param monitor
	 *            The {@link IProgressMonitor}.
	 */
	public static void addNewModelResources(final TransactionalEditingDomain editingDomain,
			final Collection<ModelResourceDescriptor> modelResourceDescriptors, boolean async, final IProgressMonitor monitor) {
		if (modelResourceDescriptors != null && editingDomain != null && modelResourceDescriptors.size() > 0) {
			ISchedulingRule rule = createSaveNewSchedulingRule(modelResourceDescriptors);
			if (async) {
				Job job = new Job(modelResourceDescriptors.size() == 1 ? Messages.job_addingNewModelResource : Messages.job_addingNewModelResources) {
					@Override
					public IStatus run(IProgressMonitor monitor) {
						try {
							runAddNewModelResources(editingDomain, modelResourceDescriptors, monitor);
							return Status.OK_STATUS;
						} catch (OperationCanceledException ex) {
							return Status.CANCEL_STATUS;
						} catch (Exception ex) {
							return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
						}
					}

					@Override
					public boolean belongsTo(Object family) {
						return IExtendedPlatformConstants.FAMILY_MODEL_LOADING.equals(family);
					}
				};
				job.setRule(rule);
				job.setPriority(Job.BUILD);
				job.schedule();
			} else {
				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
					@Override
					public void run(IProgressMonitor monitor) throws CoreException {
						runAddNewModelResources(editingDomain, modelResourceDescriptors, monitor);
					}
				};

				ServiceCaller.callOnce(EcorePlatformUtil.class, IWorkspace.class, ws -> {
					try {
						ws.run(runnable, rule, IResource.NONE, monitor);
					} catch (CoreException ex) {
						PlatformLogUtil.logAsError(Activator.getDefault(), ex);
					}
				});

			}
		}

	}

	private static void runAddNewModelResources(final TransactionalEditingDomain editingDomain,
			final Collection<ModelResourceDescriptor> modelResourceDescriptors, IProgressMonitor monitor)
			throws CoreException, OperationCanceledException {
		Assert.isNotNull(editingDomain);
		Assert.isNotNull(modelResourceDescriptors);
		SubMonitor progress = SubMonitor.convert(monitor,
				modelResourceDescriptors.size() == 1 ? Messages.task_addingNewModelResource : Messages.task_addingNewModelResources, 1);

		Map<String, Object> transactionOptions = WorkspaceTransactionUtil.getDefaultSaveNewTransactionOptions();
		String label = modelResourceDescriptors.size() == 1 ? Messages.operation_addingNewModelResource : Messages.operation_addingNewModelResources;
		final IUndoableOperation operation = new AbstractEMFOperation(editingDomain, label, transactionOptions) {
			@Override
			protected IStatus doExecute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				try {
					SubMonitor progress = SubMonitor.convert(monitor, modelResourceDescriptors.size());
					for (ModelResourceDescriptor descriptor : modelResourceDescriptors) {
						progress.subTask(NLS.bind(Messages.subtask_addingResource, descriptor.getPath().toString()));

						List<EObject> contents = descriptor.getContents();
						if (!contents.isEmpty()) {
							// Add new resource
							Resource resource = EcoreResourceUtil.addNewModelResource(editingDomain.getResourceSet(), descriptor.getURI(),
									descriptor.getContentTypeId(), contents);

							// Mark new resource as dirty
							SaveIndicatorUtil.setDirty(editingDomain, resource);
						}
						progress.worked(1);
						progress.subTask(""); //$NON-NLS-1$
						if (progress.isCanceled()) {
							throw new OperationCanceledException();
						}
					}
					return Status.OK_STATUS;
				} catch (Exception ex) {
					return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
				}

			}

			@Override
			public boolean canUndo() {
				// Avoid the save operation to appear in the undo menu
				return false;
			}
		};
		IOperationHistory history = WorkspaceTransactionUtil.getOperationHistory(editingDomain);
		try {
			history.execute(operation, progress.newChild(1), null);
		} catch (ExecutionException ex) {
			IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
			throw new CoreException(status);
		}
	}

	public static void saveNewModelResource(TransactionalEditingDomain editingDomain, IPath path, final String contentTypeId, EObject content,
			boolean async, IProgressMonitor monitor) {
		saveNewModelResources(editingDomain, Collections.singletonList(new ModelResourceDescriptor(path, contentTypeId, content)),
				EcoreResourceUtil.getDefaultSaveOptions(), async, monitor);
	}

	public static void saveNewModelResources(TransactionalEditingDomain editingDomain, Collection<ModelResourceDescriptor> modelResourceDescriptors,
			boolean async, IProgressMonitor monitor) {
		saveNewModelResources(editingDomain, modelResourceDescriptors, EcoreResourceUtil.getDefaultSaveOptions(), async, monitor);
	}

	public static void saveNewModelResource(TransactionalEditingDomain editingDomain, IPath path, final String contentTypeId, EObject content,
			Map<?, ?> options, boolean async, IProgressMonitor monitor) {
		saveNewModelResources(editingDomain, Collections.singletonList(new ModelResourceDescriptor(path, contentTypeId, content)), options, async,
				monitor);
	}

	public static void saveNewModelResources(final TransactionalEditingDomain editingDomain,
			final Collection<ModelResourceDescriptor> modelResourceDescriptors, final Map<?, ?> options, boolean async, IProgressMonitor monitor) {

		if (modelResourceDescriptors != null && editingDomain != null && modelResourceDescriptors.size() > 0) {
			ISchedulingRule rule = createSaveNewSchedulingRule(modelResourceDescriptors);
			if (async) {
				Job job = new Job(modelResourceDescriptors.size() == 1 ? Messages.job_savingNewModelResource : Messages.job_savingNewModelResources) {
					@Override
					public IStatus run(IProgressMonitor monitor) {
						try {
							runSaveNewModelResources(editingDomain, modelResourceDescriptors, options, monitor);
							return Status.OK_STATUS;
						} catch (OperationCanceledException ex) {
							return Status.CANCEL_STATUS;
						} catch (Exception ex) {
							return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
						}
					}

					@Override
					public boolean belongsTo(Object family) {
						return IExtendedPlatformConstants.FAMILY_MODEL_SAVING.equals(family);
					}
				};
				job.setRule(rule);
				job.setPriority(Job.BUILD);
				job.schedule();
			} else {
				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
					@Override
					public void run(IProgressMonitor monitor) throws CoreException {
						runSaveNewModelResources(editingDomain, modelResourceDescriptors, options, monitor);
					}
				};
				try {
					/*
					 * !! Important Note !! No need to set the IWorkspace.AVOID_UPDATE flag here because the transaction
					 * created inside the runnable to be executed suppresses its effect.
					 */
					ResourcesPlugin.getWorkspace().run(runnable, rule, IResource.NONE, monitor);
				} catch (CoreException ex) {
					PlatformLogUtil.logAsError(Activator.getDefault(), ex);
				}
			}
		}
	}

	/**
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} onto which a write-transaction must be created
	 *            in order to safely perform the saving; must not be <code>null</code>.
	 * @param modelResourceDescriptors
	 * @param options
	 * @param monitor
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	private static void runSaveNewModelResources(final TransactionalEditingDomain editingDomain,
			final Collection<ModelResourceDescriptor> modelResourceDescriptors, final Map<?, ?> options, IProgressMonitor monitor)
			throws CoreException, OperationCanceledException {
		Assert.isNotNull(editingDomain);
		Assert.isNotNull(modelResourceDescriptors);
		SubMonitor progress = SubMonitor.convert(monitor,
				modelResourceDescriptors.size() == 1 ? Messages.task_savingNewModelResource : Messages.task_savingNewModelResources, 1);

		Map<String, Object> transactionOptions = WorkspaceTransactionUtil.getDefaultSaveNewTransactionOptions();
		String label = modelResourceDescriptors.size() == 1 ? Messages.operation_savingNewModelResource : Messages.operation_savingNewModelResources;
		final IUndoableOperation operation = new AbstractEMFOperation(editingDomain, label, transactionOptions) {
			@Override
			protected IStatus doExecute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
					@Override
					public void run(IProgressMonitor monitor) throws CoreException {
						SubMonitor progress = SubMonitor.convert(monitor, modelResourceDescriptors.size());

						// Create and add new resources with specified path, content type, and contents to the given
						// editing domain's resource set
						/*
						 * !! Important Note !! Don't use EcoreResourceUtil.saveNewModelResource() to create each
						 * resource, add it to the resource set and save it in a single step. In case that the objects
						 * of the different resource contents reference each other this way of saving them would end up
						 * in DanglingHREFExceptions because the objects of the resources being saved at first would
						 * reference other objects that are not yet contained in any resource.
						 */
						Set<Resource> resourcesToSave = new HashSet<Resource>();
						for (ModelResourceDescriptor descriptor : modelResourceDescriptors) {
							List<EObject> contents = descriptor.getContents();
							if (!contents.isEmpty()) {
								Resource resource = EcoreResourceUtil.addNewModelResource(editingDomain.getResourceSet(), descriptor.getURI(),
										descriptor.getContentTypeId(), contents);
								resourcesToSave.add(resource);
							}
						}

						// Save new resources
						for (Resource resource : resourcesToSave) {
							progress.subTask(NLS.bind(Messages.subtask_savingResource, resource.getURI().toString()));

							// Add progress monitor to save options
							Map<?, ?> optionsWithProgressMonitor = addProgressMonitorToOptions(options, progress.newChild(1));

							try {
								// Save new resource
								/*
								 * !! Important Note !! Resource must be saved before marking it as freshly saved
								 * because otherwise the resource would loose its dirty state and consequently would not
								 * be saved at all.
								 */
								EcoreResourceUtil.saveModelResource(resource, optionsWithProgressMonitor);

								// Mark resource as freshly saved in order to avoid that it gets automatically reloaded
								SaveIndicatorUtil.setSaved(editingDomain, resource);
							} catch (Exception ex) {
								// Log exception in error log
								/*
								 * !! Important Note !! The exception has already been recorded as error on the resource
								 * and is principally subject to being converted to a problem marker by the resource
								 * problem handler later on (see
								 * org.eclipse.sphinx.emf.util.EcoreResourceUtil#saveModelResource(Resource, Map<?,?>)
								 * and org.eclipse.sphinx.emf.internal.resource.ResourceProblemHandler#resourceChanged(
								 * IResourceChangeEvent) for details). However, this is a new resource which has never
								 * been saved before and does not yet exist in the file system. As a consequence, there
								 * is no target to which the problem marker could be attached and the problem behind
								 * this exception would remain unperceivable if we wouldn't log anything at this point.
								 */
								PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
							}

							progress.subTask(""); //$NON-NLS-1$
							if (progress.isCanceled()) {
								throw new OperationCanceledException();
							}
						}
					}
				};

				try {
					// Execute save operation as IWorkspaceRunnable on workspace in order to avoid resource change
					// notifications during transaction execution
					/*
					 * !! Important Note !! Setting the IWorkspace.AVOID_UPDATE flag on the outer workspace job or
					 * workspace runnable from which this method is called doesn't help because the matter of executing
					 * a transaction inside suppresses its effect.
					 */
					/*
					 * !! Important Note !! Only set IWorkspace.AVOID_UPDATE flag but don't define any scheduling
					 * restrictions for the save operation right here (this must only be done on the outer workspace job
					 * or workspace runnable from which this method is called). Otherwise it would be likely to end up
					 * in deadlocks with operations which already have acquired exclusive access to the workspace but
					 * are waiting for exclusive access to the model (i.e. for the transaction).
					 */
					ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, monitor);
					return Status.OK_STATUS;
				} catch (CoreException ex) {
					return ex.getStatus();
				}
			}

			@Override
			public boolean canUndo() {
				// Avoid the save operation to appear in the undo menu
				return false;
			}
		};
		IOperationHistory history = WorkspaceTransactionUtil.getOperationHistory(editingDomain);
		try {
			history.execute(operation, progress.newChild(1), null);
		} catch (ExecutionException ex) {
			IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
			throw new CoreException(status);
		}
	}

	/**
	 * Saves all modified resources of the model behind the specified object (<em>i.e.</em> all resources in the context
	 * of the given object).
	 *
	 * @param contextResource
	 *            The object resource identifying the model to save.
	 * @param async
	 *            If <code>true</code>, model will be saved within a workspace job.
	 * @param monitor
	 *            The progress monitor to use for showing save process progress.
	 */
	public static void saveModel(Object contextObject, boolean async, IProgressMonitor monitor) {
		saveModel(contextObject, EcoreResourceUtil.getDefaultSaveOptions(), async, monitor);
	}

	/**
	 * Saves all modified, writable models from a given context object.
	 *
	 * @param contextObject
	 *            The object context identifying the models to save.
	 * @param options
	 *            The save options.
	 * @param async
	 *            When passing <code>true</code> the model will be saved within a workspace job.
	 * @param monitor
	 *            The progress monitor to use for showing save process progress.
	 */
	// TODO Make sure that this method can also be used for contextObjects in resources which are located outside the
	// Eclipse workspace (only ISchedulingRule creation and IWorkspaceRunnable usages need to be adapted)
	public static void saveModel(Object contextObject, final Map<?, ?> options, boolean async, IProgressMonitor monitor) {
		if (contextObject instanceof IProject) {
			saveProject((IProject) contextObject, options, async, monitor);
			return;
		}

		SubMonitor progress = SubMonitor.convert(monitor, 100);
		Collection<Resource> resourcesInModel = new ArrayList<Resource>(getResourcesInModel(contextObject, true));
		final Map<TransactionalEditingDomain, Collection<Resource>> resourcesToSave = detectResourcesToSave(resourcesInModel,
				progress.newChild(async ? 100 : 5));
		if (resourcesToSave.size() > 0) {
			ISchedulingRule rule = createSaveSchedulingRule(resourcesToSave);
			if (async) {
				Job job = new Job(Messages.job_savingModel) {
					@Override
					public IStatus run(IProgressMonitor monitor) {
						try {
							runSaveModelResources(resourcesToSave, options, monitor);
							return Status.OK_STATUS;
						} catch (OperationCanceledException ex) {
							return Status.CANCEL_STATUS;
						} catch (Exception ex) {
							return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
						}
					}

					@Override
					public boolean belongsTo(Object family) {
						return IExtendedPlatformConstants.FAMILY_MODEL_SAVING.equals(family);
					}
				};

				job.setRule(rule);
				job.setPriority(Job.BUILD);
				job.schedule();
			} else {
				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
					@Override
					public void run(IProgressMonitor monitor) throws CoreException {
						runSaveModelResources(resourcesToSave, options, monitor);
					}
				};
				try {
					/*
					 * !! Important Note !! No need to set the IWorkspace.AVOID_UPDATE flag here because the transaction
					 * created inside the runnable to be executed suppresses its effect.
					 */
					ResourcesPlugin.getWorkspace().run(runnable, rule, IResource.NONE, progress.newChild(95));
				} catch (CoreException ex) {
					PlatformLogUtil.logAsError(Activator.getDefault(), ex);
				}
			}
		}
	}

	/**
	 * Saves all modified {@link Resource resource}s of all models behind the specified {@link IProject project}
	 * including all referenced projects ( <em>i.e.</em> all resources in the context of the given project).
	 *
	 * @param project
	 *            The {@link IProject project} identifying the models to save.
	 * @param async
	 *            If <code>true</code>, models will be saved within a workspace job.
	 * @param monitor
	 *            The progress monitor to use for showing save process progress.
	 */
	public static void saveProject(IProject project, boolean async, IProgressMonitor monitor) {
		saveProject(project, EcoreResourceUtil.getDefaultSaveOptions(), async, monitor);
	}

	/**
	 * Saves all modified {@link Resource}s of all models behind the specified project including all referenced
	 * {@link IProject}s ( <em>i.e.</em> all resources in the context of the given project).
	 *
	 * @param project
	 *            The {@link IProject project} identifying the models to save.
	 * @param options
	 *            The save options.
	 * @param async
	 *            If <code>true</code>, models will be saved within a workspace job.
	 * @param monitor
	 *            The progress monitor to use for showing save process progress.
	 */
	public static void saveProject(IProject project, final Map<?, ?> options, boolean async, IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		Collection<Resource> resourcesInProject = new ArrayList<Resource>(getResourcesInScopes(project, true));
		final Map<TransactionalEditingDomain, Collection<Resource>> resourcesToSave = detectResourcesToSave(resourcesInProject,
				progress.newChild(async ? 100 : 5));
		if (resourcesToSave.size() > 0) {
			ISchedulingRule rule = createSaveSchedulingRule(resourcesToSave);
			if (async) {
				Job job = new Job(resourcesToSave.size() == 1 ? Messages.job_savingModel : Messages.job_savingModels) {
					@Override
					public IStatus run(IProgressMonitor monitor) {
						try {
							runSaveModelResources(resourcesToSave, options, monitor);
							return Status.OK_STATUS;
						} catch (OperationCanceledException ex) {
							return Status.CANCEL_STATUS;
						} catch (Exception ex) {
							return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
						}
					}

					@Override
					public boolean belongsTo(Object family) {
						return IExtendedPlatformConstants.FAMILY_MODEL_SAVING.equals(family);
					}
				};
				job.setRule(rule);
				job.setPriority(Job.BUILD);
				job.schedule();
			} else {
				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
					@Override
					public void run(IProgressMonitor monitor) throws CoreException {
						runSaveModelResources(resourcesToSave, options, monitor);
					}
				};
				try {
					/*
					 * !! Important Note !! No need to set the IWorkspace.AVOID_UPDATE flag here because the transaction
					 * created inside the runnable to be executed suppresses its effect.
					 */
					ResourcesPlugin.getWorkspace().run(runnable, rule, IResource.NONE, progress.newChild(95));
				} catch (CoreException ex) {
					PlatformLogUtil.logAsError(Activator.getDefault(), ex);
				}
			}
		}
	}

	private static Map<TransactionalEditingDomain, Collection<Resource>> detectResourcesToSave(Collection<Resource> resources,
			IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, resources.size());

		Map<TransactionalEditingDomain, Collection<Resource>> resourcesToSave = new HashMap<TransactionalEditingDomain, Collection<Resource>>();
		for (Resource resource : resources) {
			TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(resource);
			if (editingDomain != null) {
				if (isResourceToSave(editingDomain, resource)) {
					Collection<Resource> resourcesToSaveInEditingDomain = resourcesToSave.get(editingDomain);
					if (resourcesToSaveInEditingDomain == null) {
						resourcesToSaveInEditingDomain = new HashSet<Resource>();
						resourcesToSave.put(editingDomain, resourcesToSaveInEditingDomain);
					}
					resourcesToSaveInEditingDomain.add(resource);
				}
			}
			progress.worked(1);
		}
		return resourcesToSave;
	}

	private static boolean isResourceToSave(TransactionalEditingDomain editingDomain, Resource contextResource) {
		Assert.isNotNull(editingDomain);
		return !editingDomain.isReadOnly(contextResource) && SaveIndicatorUtil.isDirty(editingDomain, contextResource);
	}

	/**
	 * Saves model {@link Resource resource}s.
	 *
	 * @param resourcesToSave
	 *            The {@link TransactionalEditingDomain editing domain}s with the corresponding resources to save; must
	 *            not be <code>null</code>.
	 * @param options
	 *            The save options.
	 * @param monitor
	 *            The progress monitor to use (can be <code>null</code>).
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	private static void runSaveModelResources(final Map<TransactionalEditingDomain, Collection<Resource>> resourcesToSave, final Map<?, ?> options,
			IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		Assert.isNotNull(resourcesToSave);
		SubMonitor progress = SubMonitor.convert(monitor, resourcesToSave.size());

		for (final TransactionalEditingDomain editingDomain : resourcesToSave.keySet()) {
			progress.setTaskName(
					resourcesToSave.get(editingDomain).size() == 1 ? Messages.task_savingModelResource : Messages.task_savingModelResources);

			/*
			 * !! Important Note !! The saving of model resources as such does not imply any modifications of the
			 * resource set and therefore could be done in a read transaction. However, it is possible that the
			 * resources to be saved have a org.eclipse.emf.ecore.xmi.ResoureHandler registered upon them and that the
			 * ResourceHandler#preSave() method performs some resource set modifying operations. In order to avoid
			 * IllegalStateExceptions telling that the resource set cannot be modified without a write transaction in
			 * such cases the whole save operation must be executed in a write transaction.
			 */
			Map<String, Object> transactionOptions = WorkspaceTransactionUtil.getDefaultSaveTransactionOptions();
			String label = resourcesToSave.get(editingDomain).size() == 1 ? Messages.operation_savingModelResource
					: Messages.operation_savingModelResources;
			final IUndoableOperation operation = new AbstractEMFOperation(editingDomain, label, transactionOptions) {
				@Override
				protected IStatus doExecute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
					IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
						@Override
						public void run(IProgressMonitor monitor) throws CoreException {
							SubMonitor progress = SubMonitor.convert(monitor, resourcesToSave.get(editingDomain).size());
							for (Resource resource : resourcesToSave.get(editingDomain)) {
								progress.subTask(NLS.bind(Messages.subtask_savingResource, resource.getURI().toString()));

								// Add progress monitor to save options
								Map<?, ?> optionsWithProgressMonitor = addProgressMonitorToOptions(options, progress.newChild(1));

								try {
									// Save resource
									/*
									 * !! Important Note !! Resource must be saved before marking it as freshly saved
									 * because otherwise the resource would loose its dirty state and consequently would
									 * not be saved at all.
									 */
									EcoreResourceUtil.saveModelResource(resource, optionsWithProgressMonitor);

									// Mark resource as freshly saved in order to avoid that it gets automatically
									// reloaded
									SaveIndicatorUtil.setSaved(editingDomain, resource);
								} catch (Exception ex) {
									// Ignore exception
									/*
									 * !! Important Note !! The exception has already been recorded as error on resource
									 * and will be converted to a problem marker by the resource problem handler later
									 * on (see org.eclipse.sphinx.emf.util.EcoreResourceUtil#saveModelResource(Resource,
									 * Map<?,?>) and
									 * org.eclipse.sphinx.emf.internal.resource.ResourceProblemHandler#resourceChanged(
									 * IResourceChangeEvent) for details).
									 */
								}

								progress.subTask(""); //$NON-NLS-1$
								if (progress.isCanceled()) {
									throw new OperationCanceledException();
								}
							}

							// Refresh command stack state of associated editing domain
							((BasicCommandStack) editingDomain.getCommandStack()).saveIsDone();
						}
					};

					try {
						// Execute save operation as IWorkspaceRunnable on workspace in order to avoid resource change
						// notifications during transaction execution
						/*
						 * !! Important Note !! Setting the IWorkspace.AVOID_UPDATE flag on the outer workspace job or
						 * workspace runnable from which this method is called doesn't help because the matter of
						 * executing a transaction inside suppresses its effect.
						 */
						/*
						 * !! Important Note !! Only set IWorkspace.AVOID_UPDATE flag but don't define any scheduling
						 * restrictions for the save operation right here (this must only be done on the outer workspace
						 * job or workspace runnable from which this method is called). Otherwise it would be likely to
						 * end up in deadlocks with operations which already have acquired exclusive access to the
						 * workspace but are waiting for exclusive access to the model (i.e. for the transaction).
						 */
						ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, monitor);
						return Status.OK_STATUS;
					} catch (CoreException ex) {
						return ex.getStatus();
					}
				}

				@Override
				public boolean canUndo() {
					// Avoid that save operation appears in the undo menu
					return false;
				}
			};

			IOperationHistory history = WorkspaceTransactionUtil.getOperationHistory(editingDomain);
			try {
				history.execute(operation, progress.newChild(1), null);
			} catch (ExecutionException ex) {
				IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
				throw new CoreException(status);
			}
		}
	}

	/**
	 * Wraps given {@link IProgressMonitor progress monitor} into an {@link ExtendedResource#OPTION_PROGRESS_MONITOR}
	 * option and adds it to provided <code>options</code> map.
	 *
	 * @param options
	 *            A resource options map; may be <code>null</code>.
	 * @param monitor
	 *            A progress monitor; may be <code>null</code>.
	 * @return An options map containing provided <code>options</code> plus an extra
	 *         {@link ExtendedResource#OPTION_PROGRESS_MONITOR} option that holds given <code>monitor</code>. If
	 *         <code>monitor</code> is <code>null</code>, the <code>options</code> are returned as is. If
	 *         <code>options</code> is null, a new options map containing only the
	 *         {@link ExtendedResource#OPTION_PROGRESS_MONITOR} option that holds given <code>monitor</code> is
	 *         returned.
	 */
	private static Map<?, ?> addProgressMonitorToOptions(Map<?, ?> options, IProgressMonitor monitor) {
		if (monitor == null) {
			return options;
		}
		if (options == null) {
			return Collections.singletonMap(ExtendedResource.OPTION_PROGRESS_MONITOR, monitor);
		}

		// Create new options map because it could be that provided options map is a singleton or an unmodifiable map
		Map<Object, Object> newOptions = new HashMap<Object, Object>(options);
		newOptions.put(ExtendedResource.OPTION_PROGRESS_MONITOR, monitor);
		return newOptions;
	}

	/**
	 * Unloads the model contained in given {@link IFile file}.
	 *
	 * @param file
	 *            The {@link IFile file} containing the model.
	 * @see #unloadFile(TransactionalEditingDomain, IFile)
	 */
	public static void unloadFile(IFile file) {
		/*
		 * !! Important Note !! For the sake of robustness, it is necessary to consider all editing domains but not only
		 * the one which would be returned by WorkspaceEditingDomainUtil#getCurrentEditingDomain(IFile). Although not
		 * really intended by Sphinx workspace management it might anyway happen that the same file gets loaded into
		 * multiple editing domains. Typical reasons for this are e.g. lazy loading of one file from multiple other
		 * files which are in different editing domains or programatic action by some application. We then have to make
		 * sure that the given file gets unloaded from all editing domains it is in.
		 */
		Collection<TransactionalEditingDomain> editingDomains = WorkspaceEditingDomainUtil.getAllEditingDomains();
		for (TransactionalEditingDomain editingDomain : editingDomains) {
			if (EcorePlatformUtil.isFileLoaded(editingDomain, file)) {
				unloadFile(editingDomain, file);
			}
		}
	}

	/**
	 * Unloads the model contained in given {@linkplain IFile file} from given {@linkplain TransactionalEditingDomain
	 * editing domain}.
	 *
	 * @param file
	 *            The {@linkplain IFile file} containing the model.
	 * @param editingDomain
	 *            The {@linkplain TransactionalEditingDomain editing domain} the <code>file</code> belongs to.
	 */
	public static void unloadFile(final TransactionalEditingDomain editingDomain, final IFile file) {
		if (editingDomain != null && file != null) {
			try {
				editingDomain.runExclusive(new Runnable() {
					@Override
					public void run() {
						try {
							URI uri = createURI(file.getFullPath());
							EcoreResourceUtil.unloadResource(editingDomain.getResourceSet(), uri);
						} catch (RuntimeException ex) {
							PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
						}
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}

	/**
	 * Unloads the models contained in given {@linkplain IFile file}s from given {@linkplain TransactionalEditingDomain
	 * editing domain} inside a read-only transaction.
	 *
	 * @param files
	 *            The {@link IFile file}s containing the models.
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} the {@link IFile file}s belong to.
	 * @throws OperationCanceledException
	 * @since 0.7.0
	 */
	public static void unloadFiles(final TransactionalEditingDomain editingDomain, final Collection<IFile> files, final boolean memoryOptimized,
			IProgressMonitor monitor) throws OperationCanceledException {
		if (editingDomain != null && files != null && files.size() > 0) {
			final SubMonitor progress = SubMonitor.convert(monitor, Messages.task_unloadingModelFiles, files.size());
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}

			try {
				editingDomain.runExclusive(new Runnable() {
					@Override
					public void run() {
						for (IFile file : files) {
							progress.subTask(NLS.bind(Messages.subtask_unloadingModelFile, file.getFullPath().toString()));

							try {
								URI uri = createURI(file.getFullPath());
								EcoreResourceUtil.unloadResource(editingDomain.getResourceSet(), uri, memoryOptimized);
							} catch (RuntimeException ex) {
								PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
							}

							progress.worked(1);
							progress.subTask(""); //$NON-NLS-1$
							if (progress.isCanceled()) {
								throw new OperationCanceledException();
							}
							editingDomain.yield();
						}
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}

	/**
	 * Unloads the specified {@linkplain Resource resource}s from given {@linkplain TransactionalEditingDomain editing
	 * domain} inside a read-only transaction.
	 *
	 * @param memoryOptimized
	 * @param resources
	 *            The resources to unload.
	 * @param editingDomain
	 *            The editing domain owning {@link Resource resource}s.
	 * @throws OperationCanceledException
	 * @since 0.7.0
	 */
	public static void unloadResources(final TransactionalEditingDomain editingDomain, final Collection<Resource> resources,
			final boolean memoryOptimized, IProgressMonitor monitor) throws OperationCanceledException {
		if (editingDomain != null && resources != null && resources.size() > 0) {
			final SubMonitor progress = SubMonitor.convert(monitor, Messages.task_unloadingModelResources, resources.size());
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}

			try {
				editingDomain.runExclusive(new Runnable() {
					@Override
					public void run() {
						List<Resource> safeResources = new ArrayList<Resource>(resources);
						for (Resource resource : safeResources) {
							progress.subTask(NLS.bind(Messages.subtask_unloadingModelResource, resource.getURI().toString()));

							try {
								if (editingDomain.getResourceSet().getResources().contains(resource)) {
									EcoreResourceUtil.unloadResource(resource, memoryOptimized);
								}
							} catch (RuntimeException ex) {
								PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
							}

							progress.worked(1);
							progress.subTask(""); //$NON-NLS-1$
							if (progress.isCanceled()) {
								throw new OperationCanceledException();
							}
							editingDomain.yield();
						}
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}

	/**
	 * Unloads all {@linkplain Resource resource}s from given {@linkplain TransactionalEditingDomain editing domain}
	 * inside a read-only transaction.
	 *
	 * @param editingDomain
	 *            The editing domain owning {@link Resource resource}s.
	 * @throws OperationCanceledException
	 */
	public static void unloadAllResources(final TransactionalEditingDomain editingDomain, IProgressMonitor monitor)
			throws OperationCanceledException {
		if (editingDomain != null) {
			final SubMonitor progress = SubMonitor.convert(monitor, Messages.task_unloadingModelResources,
					editingDomain.getResourceSet().getResources().size());
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}

			try {
				editingDomain.runExclusive(new Runnable() {
					@Override
					public void run() {
						List<Resource> safeResources = new ArrayList<Resource>(editingDomain.getResourceSet().getResources());
						for (Resource resource : safeResources) {
							progress.subTask(NLS.bind(Messages.subtask_unloadingModelResource, resource.getURI().toString()));

							try {
								EcoreResourceUtil.unloadResource(resource, true);
							} catch (RuntimeException ex) {
								PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
							}

							progress.worked(1);
							progress.subTask(""); //$NON-NLS-1$
							if (progress.isCanceled()) {
								throw new OperationCanceledException();
							}
							editingDomain.yield();
						}
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}

	/**
	 * Parses given {@link IFile file} and validates it against XSD schema with specified {@link URL url}. Raises an
	 * exception if the {@link IFile file}'s content is not compliant with respect to XSD schema.
	 *
	 * @param file
	 *            The {@link IFile file} to be validated.
	 * @param schemaURL
	 *            The {@link URL url} of the XSD schema to be used for validation.
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void validate(IFile file, URL schemaURL) throws SAXException, IOException {
		Assert.isNotNull(file);

		URI uri = createURI(file.getFullPath());
		EcoreResourceUtil.validate(uri, schemaURL);
	}

}
