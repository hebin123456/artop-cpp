/**
 * <copyright>
 *
 * Copyright (c) 2008-2019 See4sys, BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - [374883] Improve handling of out-of-sync workspace files during descriptor initialization
 *     itemis - [434954] Hook for overwriting conversion of EMF Diagnostics to IMarkers
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.internal.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.xmi.XMIException;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.resource.BasicResourceProblemMarkerFactory;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapterFactory;
import org.eclipse.sphinx.emf.resource.IResourceProblemMarkerFactory;
import org.eclipse.sphinx.emf.resource.IXMLMarker;
import org.eclipse.sphinx.emf.resource.ProxyURIIntegrityException;
import org.eclipse.sphinx.emf.resource.XMLIntegrityException;
import org.eclipse.sphinx.emf.resource.XMLResourceProblemMarkerFactory;
import org.eclipse.sphinx.emf.resource.XMLValidityException;
import org.eclipse.sphinx.emf.resource.XMLWellformednessException;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.platform.resources.MarkerJob;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * Provides methods for analyzing {@link Resource#getErrors() errors} and {@link Resource#getWarnings() warnings} of
 * {@link Resource} resources and creating corresponding problem markers on underlying {@link IFile file}s.
 *
 * @see IXMLMarker#XML_WELLFORMEDNESS_PROBLEM
 * @see IXMLMarker#XML_VALIDITY_PROBLEM
 * @see IXMLMarker#XML_INTEGRITY_PROBLEM
 * @see #PROXY_URI_INTEGRITY_PROBLEM
 * @see IMarker#PROBLEM
 */
public class ResourceProblemMarkerService {

	/**
	 * Proxy URI integrity problem marker type.
	 * <p>
	 * !! Important Note !! Don't use Activator.getPlugin().getSymbolicName() instead of hard-coded plug-in name because
	 * this would prevent this class from being loaded in Java standalone applications.
	 * </p>
	 *
	 * @see IMarker#getType()
	 */
	public static final String PROXY_URI_INTEGRITY_PROBLEM = "org.eclipse.sphinx.emf.proxyuriintegrityproblemmarker"; //$NON-NLS-1$

	/**
	 * Singleton instance.
	 */
	public static ResourceProblemMarkerService INSTANCE = new ResourceProblemMarkerService();

	// Private default constructor for singleton pattern
	private ResourceProblemMarkerService() {
	}

	/**
	 * Analyzes {@link Resource#getErrors() errors} and {@link Resource#getWarnings() warnings} of given {@link Resource
	 * resource} and creates corresponding problem markers on underlying {@link IFile file}.
	 * <p>
	 * The type of the problem marker being created depends on the type of {@link Resource#getErrors() error} or
	 * {@link Resource#getWarnings() warning} of given {@link Resource resource} and can be one of the following:
	 * </p>
	 * <table>
	 * <tr align="left">
	 * <th>{@link Diagnostic Diagnostic type}</th>
	 * <th>{@link Exception#getCause() Exception cause} (in case that {@link Diagnostic diagnostic} is an
	 * {@link Exception exception})</th>
	 * <th>Problem marker type</th>
	 * </tr>
	 * <tr>
	 * <td>{@link XMLIntegrityException}</td>
	 * <td>any</td>
	 * <td>{@link IXMLMarker#XML_INTEGRITY_PROBLEM}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link ProxyURIIntegrityException}</td>
	 * <td>any</td>
	 * <td>{@link #PROXY_URI_INTEGRITY_PROBLEM}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link XMIException}</td>
	 * <td>{@link XMLWellformednessException}</td>
	 * <td>{@link IXMLMarker#XML_WELLFORMEDNESS_PROBLEM}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link XMIException}</td>
	 * <td>{@link XMLValidityException}</td>
	 * <td>{@link IXMLMarker#XML_VALIDITY_PROBLEM}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link XMIException}</td>
	 * <td>any other</td>
	 * <td>{@link IMarker#PROBLEM}</td>
	 * </tr>
	 * <tr>
	 * <td>any other</td>
	 * <td>any</td>
	 * <td>{@link IMarker#PROBLEM}</td>
	 * </tr>
	 * </table>
	 *
	 * @param resource
	 *            {@link Resource} whose {@link Resource#getErrors() errors} and {@link Resource#getWarnings() warnings}
	 *            are to be analyzed and converted into corresponding problem markers on underlying {@link IFile file}.
	 * @see IXMLMarker#XML_WELLFORMEDNESS_PROBLEM
	 * @see IXMLMarker#XML_VALIDITY_PROBLEM
	 * @see IXMLMarker#XML_INTEGRITY_PROBLEM
	 * @see #PROXY_URI_INTEGRITY_PROBLEM
	 * @see IMarker#PROBLEM
	 */
	public void addProblemMarkers(Resource resource, final IProgressMonitor monitor) {
		if (resource != null) {
			addProblemMarkers(Collections.singleton(resource), monitor);
		}
	}

	/**
	 * Analyzes {@link Resource#getErrors() errors} and {@link Resource#getWarnings() warnings} of given collection of
	 * {@link Resource resource}s and creates corresponding problem markers on underlying {@link IFile file}s.
	 * <p>
	 * The type of the problem marker being created depends on the type of {@link Resource#getErrors() error} or
	 * {@link Resource#getWarnings() warning} of given {@link Resource resource} and can be one of the following:
	 * </p>
	 * <table>
	 * <tr align="left">
	 * <th>{@link Diagnostic Diagnostic type}</th>
	 * <th>{@link Exception#getCause() Exception cause} (in case that {@link Diagnostic diagnostic} is an
	 * {@link Exception exception})</th>
	 * <th>Problem marker type</th>
	 * </tr>
	 * <tr>
	 * <td>{@link XMLIntegrityException}</td>
	 * <td>any</td>
	 * <td>{@link IXMLMarker#XML_INTEGRITY_PROBLEM}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link ProxyURIIntegrityException}</td>
	 * <td>any</td>
	 * <td>{@link #PROXY_URI_INTEGRITY_PROBLEM}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link XMIException}</td>
	 * <td>{@link XMLWellformednessException}</td>
	 * <td>{@link IXMLMarker#XML_WELLFORMEDNESS_PROBLEM}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link XMIException}</td>
	 * <td>{@link XMLValidityException}</td>
	 * <td>{@link IXMLMarker#XML_VALIDITY_PROBLEM}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link XMIException}</td>
	 * <td>any other</td>
	 * <td>{@link IMarker#PROBLEM}</td>
	 * </tr>
	 * <tr>
	 * <td>any other</td>
	 * <td>any</td>
	 * <td>{@link IMarker#PROBLEM}</td>
	 * </tr>
	 * </table>
	 *
	 * @param resources
	 *            Collection of {@link Resource}s whose {@link Resource#getErrors() errors} and
	 *            {@link Resource#getWarnings() warnings} are to be analyzed and converted into corresponding problem
	 *            markers on underlying {@link IFile file}s.
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 * @see IXMLMarker#XML_WELLFORMEDNESS_PROBLEM
	 * @see IXMLMarker#XML_VALIDITY_PROBLEM
	 * @see IXMLMarker#XML_INTEGRITY_PROBLEM
	 * @see #PROXY_URI_INTEGRITY_PROBLEM
	 * @see IMarker#PROBLEM
	 */
	public void addProblemMarkers(final Collection<Resource> resources, final IProgressMonitor monitor) {
		Assert.isNotNull(resources);

		// Retrieve resources with problems (errors + warnings)
		Set<Resource> resourcesWithProblems = new HashSet<Resource>();
		for (Resource resource : resources) {
			if (!resource.getErrors().isEmpty() || !resource.getWarnings().isEmpty()) {
				resourcesWithProblems.add(resource);
			}
		}

		if (!resourcesWithProblems.isEmpty()) {
			// Collect resources to update problem markers for in sets of resources per editing domain; tolerate
			// resources that aren't in any editing domain
			final Map<TransactionalEditingDomain, Collection<Resource>> resourcesToUpdate = new HashMap<TransactionalEditingDomain, Collection<Resource>>();
			for (Resource resource : resourcesWithProblems) {
				TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(resource);
				if (editingDomain != null) {
					Collection<Resource> resourcesToUpdateInEditingDomain = resourcesToUpdate.get(editingDomain);
					if (resourcesToUpdateInEditingDomain == null) {
						resourcesToUpdateInEditingDomain = new HashSet<Resource>();
						resourcesToUpdate.put(editingDomain, resourcesToUpdateInEditingDomain);
					}
					resourcesToUpdateInEditingDomain.add(resource);
				}
			}

			// Update problem markers of resources in each editing domain
			SubMonitor progress = SubMonitor.convert(monitor, resourcesToUpdate.keySet().size());
			if (progress.isCanceled()) {
				return;
			}

			for (final TransactionalEditingDomain editingDomain : resourcesToUpdate.keySet()) {
				addProblemMarkersInEditingDomain(editingDomain, resourcesToUpdate, progress.newChild(1));
			}

			MarkerJob.INSTANCE.schedule();
		}
	}

	/**
	 * Removes problem markers related to resource loading and saving.
	 *
	 * @param files
	 *            Collection of {@link IFile file}s for which we ask to remove problem markers.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	public void removeProblemMarkers(final Collection<IFile> files, final IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, files.size());
		for (IFile file : files) {
			if (file.isAccessible()) {
				getResourceProblemMarkerFactory(file).deleteMarkers(file);
			}

			progress.worked(1);
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
	}

	private void addProblemMarkersInEditingDomain(final TransactionalEditingDomain editingDomain,
			final Map<TransactionalEditingDomain, Collection<Resource>> resourcesToUpdate, final IProgressMonitor monitor) {
		Assert.isNotNull(resourcesToUpdate);

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Collection<Resource> resourcesToUpdateInEditingDomain = resourcesToUpdate.get(editingDomain);
				SubMonitor progress = SubMonitor.convert(monitor, resourcesToUpdateInEditingDomain.size());
				if (progress.isCanceled()) {
					throw new OperationCanceledException();
				}

				for (Resource resource : resourcesToUpdateInEditingDomain) {
					IFile file = EcorePlatformUtil.getFile(resource);
					if (file != null) {
						Map<Object, Object> options = getProblemHandlingOptions(resource);
						IResourceProblemMarkerFactory factory = getResourceProblemMarkerFactory(resource, options);
						int maxCount = getMaxProblemMarkerCount(options);
						int count = 0;

						// Handle errors
						ArrayList<Diagnostic> safeErrors = new ArrayList<Diagnostic>(resource.getErrors());
						for (Iterator<Diagnostic> iter = safeErrors.iterator(); iter.hasNext() && count != maxCount; count++) {
							try {
								factory.createProblemMarker(file, iter.next(), IMarker.SEVERITY_ERROR, options);
							} catch (Exception ex) {
								PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
							}
						}

						// Handle warnings
						ArrayList<Diagnostic> safeWarnings = new ArrayList<Diagnostic>(resource.getWarnings());
						for (Iterator<Diagnostic> iter = safeWarnings.iterator(); iter.hasNext() && count != maxCount; count++) {
							try {
								factory.createProblemMarker(file, iter.next(), IMarker.SEVERITY_WARNING, options);
							} catch (Exception ex) {
								PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
							}
						}
					}

					progress.worked(1);
					if (progress.isCanceled()) {
						throw new OperationCanceledException();
					}
				}
			}
		};

		if (editingDomain != null) {
			try {
				editingDomain.runExclusive(runnable);
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			}
		} else {
			runnable.run();
		}
	}

	private Map<Object, Object> getProblemHandlingOptions(Resource resource) {
		if (resource != null) {
			ExtendedResource extendedResource = ExtendedResourceAdapterFactory.INSTANCE.adapt(resource);
			if (extendedResource != null) {
				return extendedResource.getProblemHandlingOptions();
			}
		}
		return null;
	}

	private int getMaxProblemMarkerCount(Map<Object, Object> problemHandlingOptions) {
		if (problemHandlingOptions != null) {
			Object count = problemHandlingOptions.get(ExtendedResource.OPTION_MAX_PROBLEM_MARKER_COUNT);
			if (count instanceof Integer) {
				return (Integer) count;
			}
		}
		return ExtendedResource.OPTION_MAX_PROBLEM_MARKER_COUNT_UNLIMITED;
	}

	private IResourceProblemMarkerFactory getResourceProblemMarkerFactory(IFile file) {
		Resource resource = EcorePlatformUtil.getResource(file);
		if (resource != null) {
			Map<Object, Object> problemHandlingOptions = getProblemHandlingOptions(resource);
			return getResourceProblemMarkerFactory(resource, problemHandlingOptions);
		}
		return createDefaultResourceProblemMarkerFactory();
	}

	private IResourceProblemMarkerFactory getResourceProblemMarkerFactory(Resource resource, Map<Object, Object> problemHandlingOptions) {
		if (problemHandlingOptions != null) {
			Object factory = problemHandlingOptions.get(ExtendedResource.OPTION_PROBLEM_MARKER_FACTORY);
			if (factory instanceof IResourceProblemMarkerFactory) {
				return (IResourceProblemMarkerFactory) factory;
			}
		}
		return createResourceProblemMarkerFactory(resource);
	}

	private IResourceProblemMarkerFactory createResourceProblemMarkerFactory(Resource resource) {
		if (resource instanceof XMLResource) {
			return new XMLResourceProblemMarkerFactory();
		}
		return createDefaultResourceProblemMarkerFactory();
	}

	private BasicResourceProblemMarkerFactory createDefaultResourceProblemMarkerFactory() {
		return new BasicResourceProblemMarkerFactory();
	}
}