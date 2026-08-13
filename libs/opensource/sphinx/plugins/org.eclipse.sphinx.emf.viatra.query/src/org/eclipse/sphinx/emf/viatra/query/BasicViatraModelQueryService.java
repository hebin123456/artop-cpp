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
 *     IncQuery Labs, itemis - [501899] Use base index instead of IncQuery patterns
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.viatra.query;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.services.AbstractMetaModelService;
import org.eclipse.sphinx.emf.query.IModelQueryService;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.viatra.query.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.base.api.IndexingLevel;
import org.eclipse.viatra.query.runtime.base.api.NavigationHelper;
import org.eclipse.viatra.query.runtime.emf.EMFScope;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;

public class BasicViatraModelQueryService extends AbstractMetaModelService implements IModelQueryService {

	private IViatraQueryEngineHelper viatraQueryEngineHelper;

	public BasicViatraModelQueryService(Collection<IMetaModelDescriptor> mmDescriptors) {
		super(mmDescriptors);
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

	protected EClass getEClassForType(Class<?> type) {
		for (IMetaModelDescriptor mmDescriptor : getMetaModelDescriptors()) {
			for (EPackage ePackage : mmDescriptor.getEPackages()) {
				EClassifier eClassifier = EObjectUtil.findEClassifier(ePackage, type);
				if (eClassifier instanceof EClass) {
					return (EClass) eClassifier;
				}
			}
		}
		return null;
	}

	@Override
	public <T> List<T> getAllInstancesOf(EObject contextObject, Class<T> type) {
		return getAllInstancesOf(contextObject.eResource(), type);
	}

	@Override
	public <T> List<T> getAllInstancesOf(Resource contextResource, Class<T> type) {
		try {
			ViatraQueryEngine engine = getViatraQueryEngineHelper().getEngine(contextResource);
			return doGetAllInstancesOf(type, engine);
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			return Collections.emptyList();
		}
	}

	protected <T> List<T> doGetAllInstancesOf(Class<T> type, ViatraQueryEngine engine) throws ViatraQueryException, InvocationTargetException {
		Assert.isNotNull(type);
		final NavigationHelper baseIndex = EMFScope.extractUnderlyingEMFIndex(engine);

		final EClass eClass = getEClassForType(type);
		baseIndex.coalesceTraversals(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				baseIndex.registerEClasses(Collections.singleton(eClass), IndexingLevel.FULL);
				return null;
			}
		});

		List<T> instances = new ArrayList<T>();
		for (EObject instance : baseIndex.getAllInstances(eClass)) {
			instances.add(type.cast(instance));
		}
		return instances;
	}
}
