/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.platform.resources.AbstractProblemMarkerFinder;
import org.eclipse.sphinx.platform.resources.IProblemMarkerFinder;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * A reusable {@link IProblemMarkerFinder problem marker finder} implementation for model objects supporting
 * {@link EObject}s, {@link TransientItemProvider}s, and {@link IWrapperItemProvider}s.
 * <p>
 * To find the collection of problem markers that is applicable to the given model object, this implementation retrieves
 * the model object's underlying {@link EObject}, then retrieves all {@link IMarker#PROBLEM problem marker}s attached to
 * the {@link IFile file} in which this EObject is stored and retains all those having an
 * {@link EValidator#URI_ATTRIBUTE URI attribute} that matches the URI of the underlying EObject.
 * </p>
 */
public class BasicModelProblemMarkerFinder extends AbstractProblemMarkerFinder {

	/*
	 * @see org.eclipse.sphinx.emf.validation.ui.decorators.AbstractProblemMarkerFinder#canHaveProblemMarkers(java.lang.
	 * Object)
	 */
	@Override
	protected boolean canHaveProblemMarkers(Object object) {
		if (object instanceof EObject) {
			return true;
		} else if (object instanceof TransientItemProvider) {
			// Represents a group of model objects which may never have any problem markers on its own - only its
			// owner (parent) EObject or children EObjects may
			return false;
		} else if (object instanceof IWrapperItemProvider) {
			// Retrieve wrapped EObject if any
			Object unwrapped = AdapterFactoryEditingDomain.unwrap(object);
			if (unwrapped instanceof EObject) {
				return true;
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.platform.resources.IProblemMarkerFinder#getProblemMarkers(java.lang.Object)
	 */
	@Override
	public Collection<IMarker> getProblemMarkers(Object object) throws CoreException {
		Object unwrapped = AdapterFactoryEditingDomain.unwrap(object);
		if (unwrapped instanceof EObject) {
			EObject eObject = (EObject) unwrapped;
			IFile file = getFile(eObject);
			if (file != null && file.exists()) {
				URI eObjectURI = getURI(eObject);
				String eObjectURIFragment = eObjectURI.fragment();
				if (eObjectURIFragment != null) {
					List<IMarker> applicableMarkers = new ArrayList<IMarker>();
					for (IMarker marker : file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO)) {
						URI markerURI = getURI(marker);
						if (markerURI != null) {
							if (eObjectURIFragment.equals(markerURI.fragment())) {
								applicableMarkers.add(marker);
							}
						}
					}
					return applicableMarkers;
				}
			}
		}
		return Collections.emptyList();
	}

	protected IFile getFile(EObject eObject) {
		return EcorePlatformUtil.getFile(eObject);
	}

	protected URI getURI(final EObject eObject) {
		TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(eObject);
		if (editingDomain != null) {
			try {
				return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<URI>() {
					@Override
					public void run() {
						setResult(EcoreResourceUtil.getURI(eObject));
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
				return URI.createURI(""); //$NON-NLS-1$
			}
		} else {
			return EcoreResourceUtil.getURI(eObject);
		}
	}

	protected URI getURI(IMarker marker) throws CoreException {
		if (marker != null && marker.exists()) {
			String uriString = (String) marker.getAttribute(EValidator.URI_ATTRIBUTE);
			if (uriString != null) {
				return URI.createURI(uriString, true);
			}
		}
		return null;
	}
}