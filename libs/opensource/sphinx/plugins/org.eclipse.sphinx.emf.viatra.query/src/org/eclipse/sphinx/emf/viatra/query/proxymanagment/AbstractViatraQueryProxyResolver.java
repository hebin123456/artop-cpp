/**
 * <copyright>
 *
 * Copyright (c) 2014-2017 itemis, IncQuery Labs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [475954] Proxies with fragment-based proxy URIs may get resolved across model boundaries
 *     IncQuery Labs, itemis - [501899] Use base index instead of IncQuery patterns
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.viatra.query.proxymanagment;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.sphinx.emf.ecore.proxymanagement.IProxyResolver;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapterFactory;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSet;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.viatra.query.IViatraQueryEngineHelper;
import org.eclipse.sphinx.emf.viatra.query.ViatraQueryEngineHelper;
import org.eclipse.sphinx.emf.viatra.query.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.base.api.IndexingLevel;
import org.eclipse.viatra.query.runtime.base.api.NavigationHelper;
import org.eclipse.viatra.query.runtime.emf.EMFScope;

public abstract class AbstractViatraQueryProxyResolver implements IProxyResolver {

	private IViatraQueryEngineHelper viatraQueryEngineHelper;

	protected boolean isBlank(String text) {
		return text == null || text.isEmpty();
	}

	protected IViatraQueryEngineHelper getViatraQueryEngineHelper() {
		if (viatraQueryEngineHelper == null) {
			viatraQueryEngineHelper = createViatraQueryEngineHelper();
		}
		return viatraQueryEngineHelper;
	}

	protected IViatraQueryEngineHelper createViatraQueryEngineHelper() {
		return new ViatraQueryEngineHelper();
	}

	protected String getTargetEObjectName(EObject proxy) {
		InternalEObject internalEObject = (InternalEObject) proxy;
		if (internalEObject.eIsProxy()) {
			return getTargetEObjectName(internalEObject.eProxyURI());
		}
		return null;
	}

	protected abstract String getTargetEObjectName(URI uri);

	protected abstract EStructuralFeature getTargetEObjectNameFeature(EClass eclass);

	/**
	 * @param proxy
	 * @param contextObject
	 * @param engine
	 * @return
	 */
	// URI uri, IMetaModelDescriptor targetMetaModelDescriptor, Object contextObject, boolean loadOnDemand
	protected Stream<EObject> getEObjectCandidates(EObject proxy, ViatraQueryEngine engine) {
		Assert.isNotNull(proxy);

		// Extract target EObject name from proxy EObject
		String targetEObjectName = getTargetEObjectName(proxy);

		// Query target EObject candidates
		return doGetEObjectCandidates(targetEObjectName, proxy.eClass(), engine);
	}

	protected Stream<EObject> getEObjectCandidates(URI uri, EClass targetEClass, ViatraQueryEngine engine) {
		// Extract target EObject name from proxy EObject
		String targetEObjectName = getTargetEObjectName(uri);

		// Query target EObject candidates
		return doGetEObjectCandidates(targetEObjectName, targetEClass, engine);
	}

	protected Stream<EObject> doGetEObjectCandidates(String targetEObjectName, final EClass targetEClass, ViatraQueryEngine engine) {
		Assert.isNotNull(targetEClass);
		try {
			final NavigationHelper baseIndex = EMFScope.extractUnderlyingEMFIndex(engine);

			// Target EObject name available?
			if (!isBlank(targetEObjectName)) {
				// Lookup target EObject with given name
				final EStructuralFeature targetEObjectNameFeature = getTargetEObjectNameFeature(targetEClass);
				baseIndex.coalesceTraversals(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						baseIndex.registerEStructuralFeatures(Collections.singleton(targetEObjectNameFeature), IndexingLevel.FULL);
						return null;
					}
				});

				return baseIndex.findByFeatureValue(targetEObjectName, targetEObjectNameFeature).stream()
						.filter(eobject -> targetEClass.isInstance(eobject));
			} else {
				// Return all EObjects that match the proxy EObject's type
				final EClass finalTargetEClass = targetEClass;
				baseIndex.coalesceTraversals(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						baseIndex.registerEClasses(Collections.singleton(finalTargetEClass), IndexingLevel.FULL);
						return null;
					}
				});

				return baseIndex.getAllInstances(targetEClass).stream();
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			return Stream.empty();
		}
	}

	protected EObject getMatchingEObject(final URI uri, final Object contextObject, Stream<EObject> candidateObjects) {
		if (uri != null && candidateObjects != null) {
			Optional<EObject> value = candidateObjects.filter(candidateObject -> matchesEObjectCandidate(uri, contextObject, candidateObject))
					.findFirst();
			return value.isPresent() ? value.get() : null;
		}
		return null;
	}

	protected boolean matchesEObjectCandidate(URI uri, Object contextObject, EObject candidateObject) {
		return matchesEObjectCandidate(uri, candidateObject);
	}

	protected boolean matchesEObjectCandidate(URI uri, EObject candidateObject) {
		URI candidateURI = EcoreResourceUtil.getURI(candidateObject);
		return uri.equals(candidateURI);
	}

	@Override
	public boolean canResolve(EObject eObject) {
		if (eObject != null) {
			return canResolve(eObject.eClass());
		}
		return false;
	}

	protected abstract boolean isTypeSupported(EClass eType);

	@Override
	public boolean canResolve(EClass eType) {
		if (eType != null) {
			return isTypeSupported(eType);
		}
		return false;
	}

	protected URI trimContextInfo(URI proxyURI, EObject contextObject) {
		if (contextObject != null) {
			ExtendedResource extendedResource = ExtendedResourceAdapterFactory.INSTANCE.getExtendedResource(contextObject);
			if (extendedResource != null) {
				return extendedResource.trimProxyContextInfo(proxyURI);
			}
		}
		return proxyURI;
	}

	@Override
	public EObject getEObject(EObject proxy, EObject contextObject, boolean loadOnDemand) {
		try {
			if (proxy != null) {
				URI uri = trimContextInfo(((InternalEObject) proxy).eProxyURI(), contextObject);
				ViatraQueryEngine engine = getViatraQueryEngineHelper().getEngine(contextObject);
				Stream<EObject> candidateObjects = getEObjectCandidates(proxy, engine);
				return getMatchingEObject(uri, contextObject, candidateObjects);
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
		return null;
	}

	@Override
	public EObject getEObject(URI uri, EClass targetEClass, ExtendedResourceSet contextResourceSet, Object contextObject, boolean loadOnDemand) {
		try {
			if (contextResourceSet != null) {
				uri = contextResourceSet.trimProxyContextInfo(uri);
				ViatraQueryEngine engine = getViatraQueryEngineHelper().getEngine(contextResourceSet);
				Stream<EObject> candidateObjects = getEObjectCandidates(uri, targetEClass, engine);
				return getMatchingEObject(uri, contextObject, candidateObjects);
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
		return null;
	}
}
