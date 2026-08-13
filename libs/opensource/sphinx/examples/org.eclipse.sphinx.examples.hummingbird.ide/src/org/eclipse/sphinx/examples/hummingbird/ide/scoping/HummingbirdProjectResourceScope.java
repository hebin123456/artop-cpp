/**
 * <copyright>
 * 
 * Copyright (c) 2008-2011 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [346715] IMetaModelDescriptor methods of MetaModelDescriptorRegistry taking EObject or Resource arguments should not start new EMF transactions
 * 
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird.ide.scoping;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.scoping.ProjectResourceScope;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.examples.hummingbird.ide.metamodel.HummingbirdMMDescriptor;
import org.eclipse.sphinx.examples.hummingbird.ide.preferences.IHummingbirdPreferences;

public class HummingbirdProjectResourceScope extends ProjectResourceScope {

	public HummingbirdProjectResourceScope(IResource resource) {
		super(resource);
	}

	public static boolean isResourceVersionCorrespondingToMetaModelVersionOfEnclosingProject(IFile file, IMetaModelDescriptor fileMMDescriptor) {
		if (file != null) {
			if (!(fileMMDescriptor instanceof HummingbirdMMDescriptor)) {
				// Return true for non-Hummingbird files
				return true;
			}
			IProject project = file.getProject();
			HummingbirdMMDescriptor projectRelease = IHummingbirdPreferences.METAMODEL_VERSION.get(project);
			if (projectRelease != null) {
				return fileMMDescriptor == projectRelease;
			} else {
				// Return true for files which are in non-Hummingbird projects
				return true;
			}
		}
		// Return always false for null files.
		return false;

	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.AbstractResourceScope#exists()
	 */
	@Override
	public boolean exists() {
		return super.exists() && IHummingbirdPreferences.METAMODEL_VERSION.get(rootProject) != null;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.ProjectResourceScope#belongsTo(org.eclipse.core.resources.IFile, boolean)
	 */
	@Override
	public boolean belongsTo(IFile file, boolean includeReferencedScopes) {
		IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(file);
		return super.belongsTo(file, includeReferencedScopes)
				&& isResourceVersionCorrespondingToMetaModelVersionOfEnclosingProject(file, mmDescriptor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.ProjectResourceScope#belongsTo(org.eclipse.emf.ecore.resource.Resource,
	 * boolean)
	 */
	@Override
	public boolean belongsTo(Resource resource, boolean includeReferencedScopes) {
		IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(resource);
		return super.belongsTo(resource, includeReferencedScopes)
				&& isResourceVersionCorrespondingToMetaModelVersionOfEnclosingProject(EcorePlatformUtil.getFile(resource), mmDescriptor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.ProjectResourceScope#belongsTo(org.eclipse.emf.common.util.URI, boolean)
	 */
	@Override
	public boolean belongsTo(URI uri, boolean includeReferencedScopes) {
		IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(uri);
		return super.belongsTo(uri, includeReferencedScopes)
				&& isResourceVersionCorrespondingToMetaModelVersionOfEnclosingProject(EcorePlatformUtil.getFile(uri), mmDescriptor);
	}
}
