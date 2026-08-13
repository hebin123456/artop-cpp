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
package org.eclipse.sphinx.examples.hummingbird.ide.scoping;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.scoping.ProjectResourceScope;
import org.eclipse.sphinx.emf.scoping.ProjectResourceScopeProvider;
import org.eclipse.sphinx.emf.scoping.ResourceScopeMarkerSynchronizer;
import org.eclipse.sphinx.examples.hummingbird.ide.internal.messages.Messages;
import org.eclipse.sphinx.examples.hummingbird.ide.internal.scoping.HummingbirdResourceScopeMarkerSynchronizerDelegate;
import org.eclipse.sphinx.examples.hummingbird.ide.metamodel.HummingbirdMMDescriptor;
import org.eclipse.sphinx.examples.hummingbird.ide.preferences.IHummingbirdPreferences;

public class HummingbirdProjectResourceScopeProvider extends ProjectResourceScopeProvider {

	public HummingbirdProjectResourceScopeProvider() {
		ResourceScopeMarkerSynchronizer.INSTANCE.addDelegate(HummingbirdResourceScopeMarkerSynchronizerDelegate.INSTANCE);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.scoping.ProjectResourceScopeProvider#createScope(org.eclipse.core.resources.IResource)
	 */
	@Override
	protected ProjectResourceScope createScope(IResource resource) {
		return new HummingbirdProjectResourceScope(resource);
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScopeProvider#validate(org.eclipse.core.resources.IFile)
	 */
	@Override
	public Diagnostic validate(IFile file) {
		IMetaModelDescriptor effectiveMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getEffectiveDescriptor(file);
		if (!HummingbirdProjectResourceScope.isResourceVersionCorrespondingToMetaModelVersionOfEnclosingProject(file, effectiveMMDescriptor)) {
			HummingbirdMMDescriptor projectRelease = IHummingbirdPreferences.METAMODEL_VERSION.get(file.getProject());
			String msg = NLS.bind(Messages.warning_resourceVersionNotCompatibleWithMetaModelVersionOfEnclosingProject, new String[] { file.getName(),
					projectRelease.getName(), effectiveMMDescriptor.getName() });
			return new BasicDiagnostic(Diagnostic.WARNING, Activator.getPlugin().getSymbolicName(), 0, msg, new Object[] { file });
		}
		return Diagnostic.OK_INSTANCE;
	}
}
