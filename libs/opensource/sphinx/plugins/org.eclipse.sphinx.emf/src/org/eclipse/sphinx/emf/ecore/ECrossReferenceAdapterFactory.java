/**
 * <copyright>
 *
 * Copyright (c) 2008-2010 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [400895] Provide workarounds for memory leaks caused by EMF's ECrossReferenceAdapter
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.ecore;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapterFactory;
import org.eclipse.sphinx.platform.util.ReflectUtil;

/**
 *
 */
public class ECrossReferenceAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The singleton instance of this {@link AdapterFactory adapter factory}.
	 */
	public static ECrossReferenceAdapterFactory INSTANCE = new ECrossReferenceAdapterFactory();

	// Protected default constructor for singleton pattern
	protected ECrossReferenceAdapterFactory() {
		// Nothing to do
	}

	/*
	 * @see org.eclipse.emf.common.notify.impl.AdapterFactoryImpl#isFactoryForType(java.lang.Object)
	 */
	@Override
	public boolean isFactoryForType(Object type) {
		return type == ECrossReferenceAdapter.class;
	}

	/**
	 * Returns either a previously associated {@link ECrossReferenceAdapter} adapter or a newly associated one, as
	 * appropriate. {@link Adapter#isAdapterForType(Object) Checks} if the an adapter for {@link ECrossReferenceAdapter}
	 * is already associated with the target and returns it in that case;
	 * {@link AdapterFactory#adaptNew(Notifier, Object) creates} a new {@link ECrossReferenceAdapter} adapter if
	 * possible otherwise.
	 * 
	 * @param target
	 *            The notifier to adapt.
	 * @return A previously existing associated {@link ECrossReferenceAdapter} adapter, a new associated
	 *         {@link ECrossReferenceAdapter} adapter if possible, or <code>null</code> otherwise.
	 * @see AdapterFactory#adapt(Notifier, Object)
	 * @see Adapter#isAdapterForType(Object)
	 * @see AdapterFactory#adaptNew(Notifier, Object)
	 */
	public ECrossReferenceAdapter adapt(Notifier target) {
		if (target != null) {
			ECrossReferenceAdapter adapter = ECrossReferenceAdapter.getCrossReferenceAdapter(target);
			if (adapter == null) {
				adapter = (ECrossReferenceAdapter) adaptNew(target, ECrossReferenceAdapter.class);
			}
			return adapter;
		}
		return null;
	}

	/*
	 * @see org.eclipse.emf.common.notify.impl.AdapterFactoryImpl#createAdapter(org.eclipse.emf.common.notify.Notifier)
	 */
	@Override
	protected Adapter createAdapter(Notifier target) {
		if (target instanceof ResourceSet || target instanceof Resource || target instanceof EObject) {
			return new ECrossReferenceAdapter() {
				// Overridden to provide workaround for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=400887
				@Override
				protected void unsetTarget(Resource target) {
					List<EObject> contents = target.getContents();
					for (int i = 0, size = contents.size(); i < size; ++i) {
						Notifier notifier = contents.get(i);
						removeAdapter(notifier);
					}
					unloadedResources.remove(target);
				}

				// Overridden to provide workaround for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=400891
				@Override
				public void selfAdapt(Notification notification) {
					Object notifier = notification.getNotifier();
					if (notifier instanceof Resource) {
						switch (notification.getFeatureID(Resource.class)) {
						case Resource.RESOURCE__IS_LOADED: {
							if (!notification.getNewBooleanValue()) {
								unloadedResources.add((Resource) notifier);
								for (Iterator<Map.Entry<EObject, Resource>> i = unloadedEObjects.entrySet().iterator(); i.hasNext();) {
									Map.Entry<EObject, Resource> entry = i.next();
									if (entry.getValue() == notifier) {
										i.remove();

										// Don't keep track of proxies if memory-optimized unload is to be performed -
										// they would never ever be cleared off again and result in a memory-leak
										ExtendedResource extendedResource = ExtendedResourceAdapterFactory.INSTANCE.adapt((Resource) notifier);
										if (extendedResource == null
												|| extendedResource.getDefaultLoadOptions().get(ExtendedResource.OPTION_UNLOAD_MEMORY_OPTIMIZED) != Boolean.TRUE) {
											EObject eObject = entry.getKey();
											Collection<EStructuralFeature.Setting> settings = inverseCrossReferencer.get(eObject);
											if (settings != null) {
												for (EStructuralFeature.Setting setting : settings) {
													try {
														ReflectUtil.invokeInvisibleMethod(inverseCrossReferencer, "addProxy", eObject, //$NON-NLS-1$
																setting.getEObject());
													} catch (Exception ex) {
														// Ignore exception
													}
												}
											}
										}
									}
								}
								return;
							}
						}
						}
					}

					super.selfAdapt(notification);
				}
			};
		}
		return null;
	}
}
