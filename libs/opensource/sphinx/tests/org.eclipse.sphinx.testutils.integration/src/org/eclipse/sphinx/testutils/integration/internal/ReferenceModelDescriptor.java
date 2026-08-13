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

import org.eclipse.core.resources.IProject;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;

public class ReferenceModelDescriptor {

	private IMetaModelDescriptor fMetaModelDescriptor;
	private String fEditingDomainName;
	private IProject fRootProject;

	public ReferenceModelDescriptor(IMetaModelDescriptor metaModelDescriptor, String editingDomainName, IProject originalRootProject) {
		fEditingDomainName = editingDomainName;
		fMetaModelDescriptor = metaModelDescriptor;
		fRootProject = originalRootProject;
	}

	public IMetaModelDescriptor getMetaModelDescriptor() {
		return fMetaModelDescriptor;
	}

	public String getEditingDomainName() {
		return fEditingDomainName;
	}

	public IProject getRootProject() {
		return fRootProject;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof ReferenceModelDescriptor) {
			ReferenceModelDescriptor otherReferenceModelDescriptor = (ReferenceModelDescriptor) object;
			return fMetaModelDescriptor.equals(otherReferenceModelDescriptor.fMetaModelDescriptor)
					&& fEditingDomainName.equals(otherReferenceModelDescriptor.fEditingDomainName)
					&& fRootProject.equals(otherReferenceModelDescriptor.fRootProject);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return fMetaModelDescriptor.hashCode() + fEditingDomainName.hashCode() + fRootProject.hashCode();
	}

	@Override
	@SuppressWarnings("nls")
	public String toString() {
		return fMetaModelDescriptor + "@" + fRootProject.getName();
	}
}
