/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.testutils.integration.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.TransactionalEditingDomain;

public class ReferenceEditingDomainDescriptor {

	public String name;
	private Set<URI> resourcesURIs;

	public ReferenceEditingDomainDescriptor(String editingDomainName) {
		name = editingDomainName;
		resourcesURIs = new HashSet<URI>();
	}

	public void addResourceURI(URI fileURI) {
		if (resourcesURIs != null) {
			if (!resourcesURIs.contains(fileURI)) {
				resourcesURIs.add(fileURI);
			}
		}
	}

	public Set<URI> getResourceURIs() {
		return resourcesURIs;
	}

	public boolean containsResource(String ResourceName) {
		for (URI uri : resourcesURIs) {
			String lastSegment = uri.lastSegment();
			if (lastSegment != null) {
				if (lastSegment.equals(ResourceName)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean matchResourceContentWith(TransactionalEditingDomain editingDomain) {
		int count = 0;
		if (editingDomain != null) {
			if (editingDomain.getID().equals(name)) {
				ResourceSet resourceSet = editingDomain.getResourceSet();
				if (resourceSet != null) {
					EList<Resource> resources = resourceSet.getResources();

					if (resources.isEmpty() && resourcesURIs.isEmpty()) {
						return true;
					} else {
						for (Resource resource : resources) {
							if (resourcesURIs.contains(resource.getURI())) {
								count++;
							}
						}
						if (count == resourcesURIs.size()) {
							return true;
						}
					}

				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return name;
	}
}