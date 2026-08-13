/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
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
package org.eclipse.sphinx.emf.workspace.viatra.query.internal;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.ScopingResourceSet;

/**
 * @deprecated this class is no longer used. Viatra scope is filtered using ResourceFilter instead.
 */
@Deprecated
public class DelegatingScopingResourceSetImpl implements ResourceSet {

	private IModelDescriptor contextModelDescriptor;
	private ScopingResourceSet scopingResourceSet;

	public DelegatingScopingResourceSetImpl(ScopingResourceSet scopingResourceSet, EObject contextObject) {
		this(scopingResourceSet, contextObject != null ? ModelDescriptorRegistry.INSTANCE.getModel(contextObject.eResource()) : null);
	}

	public DelegatingScopingResourceSetImpl(ScopingResourceSet scopingResourceSet, Resource contextResource) {
		this(scopingResourceSet, ModelDescriptorRegistry.INSTANCE.getModel(contextResource));
	}

	public DelegatingScopingResourceSetImpl(ScopingResourceSet scopingResourceSet, IModelDescriptor contextModelDescriptor) {
		this.scopingResourceSet = scopingResourceSet;
		this.contextModelDescriptor = contextModelDescriptor;
	}

	@Override
	public EList<Adapter> eAdapters() {
		return scopingResourceSet.eAdapters();
	}

	@Override
	public boolean eDeliver() {
		return scopingResourceSet.eDeliver();
	}

	@Override
	public void eSetDeliver(boolean deliver) {
		scopingResourceSet.eSetDeliver(deliver);
	}

	@Override
	public void eNotify(Notification notification) {
		scopingResourceSet.eNotify(notification);
	}

	@Override
	public EList<Resource> getResources() {
		List<Resource> resourcesInScope = scopingResourceSet.getResourcesInScope(contextModelDescriptor);
		return new BasicEList<Resource>(resourcesInScope);
	}

	@Override
	public TreeIterator<Notifier> getAllContents() {
		return scopingResourceSet.getAllContents();
	}

	@Override
	public EList<AdapterFactory> getAdapterFactories() {
		return scopingResourceSet.getAdapterFactories();
	}

	@Override
	public Map<Object, Object> getLoadOptions() {
		return scopingResourceSet.getLoadOptions();
	}

	@Override
	public EObject getEObject(URI uri, boolean loadOnDemand) {
		return scopingResourceSet.getEObject(uri, loadOnDemand);
	}

	@Override
	public Resource getResource(URI uri, boolean loadOnDemand) {
		return scopingResourceSet.getResource(uri, loadOnDemand);
	}

	@Override
	public Resource createResource(URI uri) {
		return scopingResourceSet.createResource(uri);
	}

	@Override
	public Resource createResource(URI uri, String contentType) {
		return scopingResourceSet.createResource(uri, contentType);
	}

	@Override
	public Registry getResourceFactoryRegistry() {
		return scopingResourceSet.getResourceFactoryRegistry();
	}

	@Override
	public void setResourceFactoryRegistry(Registry resourceFactoryRegistry) {
		scopingResourceSet.setResourceFactoryRegistry(resourceFactoryRegistry);
	}

	@Override
	public URIConverter getURIConverter() {
		return scopingResourceSet.getURIConverter();
	}

	@Override
	public void setURIConverter(URIConverter converter) {
		scopingResourceSet.setURIConverter(converter);
	}

	@Override
	public org.eclipse.emf.ecore.EPackage.Registry getPackageRegistry() {
		return scopingResourceSet.getPackageRegistry();
	}

	@Override
	public void setPackageRegistry(org.eclipse.emf.ecore.EPackage.Registry packageRegistry) {
		scopingResourceSet.setPackageRegistry(packageRegistry);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (contextModelDescriptor == null ? 0 : contextModelDescriptor.hashCode());
		result = prime * result + (scopingResourceSet == null ? 0 : scopingResourceSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DelegatingScopingResourceSetImpl other = (DelegatingScopingResourceSetImpl) obj;
		if (contextModelDescriptor == null) {
			if (other.contextModelDescriptor != null) {
				return false;
			}
		} else if (!contextModelDescriptor.equals(other.contextModelDescriptor)) {
			return false;
		}
		if (scopingResourceSet == null) {
			if (other.scopingResourceSet != null) {
				return false;
			}
		} else if (!scopingResourceSet.equals(other.scopingResourceSet)) {
			return false;
		}
		return true;
	}
}
