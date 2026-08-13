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
package org.eclipse.sphinx.emf.compare.ui.viewer.structuremerge;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.compare.ide.ui.internal.configuration.EMFCompareConfiguration;
import org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.EMFCompareStructureMergeViewer;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sphinx.emf.compare.scope.IModelComparisonScope;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.swt.widgets.Composite;

public class ModelCompareStructureMergeViewer extends EMFCompareStructureMergeViewer {

	// Input EMF resources if selected input objects are IFile
	private Resource leftResource;
	private Resource rightResource;

	public ModelCompareStructureMergeViewer(Composite parent, EMFCompareConfiguration config) {
		super(parent, config);
	}

	// FIXME we need to override this method but not visible.
	// Therefore, we performed required actions, e.g., model load when preparing the editor input.
	// @Override
	// protected void compareInputChanged(final ICompareInput input, IProgressMonitor monitor) {
	// if (input instanceof ModelElementComparisonScopeEditorInput) {
	// IComparisonScope scope = ((ModelElementComparisonScopeEditorInput) input).getScope();
	// if (scope instanceof IModelComparisonScope) {
	// loadModel((IModelComparisonScope) scope);
	//
	// ((IModelComparisonScope) scope).setDelegate(new DefaultComparisonScope(leftResource, rightResource, null));
	//
	// EMFCompareConfiguration compareConfiguration = getCompareConfiguration();
	// ICompareEditingDomain editingDomain = compareConfiguration.getEditingDomain();
	// if (editingDomain instanceof DelegatingEMFCompareEditingDomain) {
	// ICompareEditingDomain delegatingEditingDomain = BasicCompareUIUtil.createEMFCompareEditingDomain(leftResource,
	// rightResource,
	// null);
	// ((DelegatingEMFCompareEditingDomain) editingDomain).setDelegate(delegatingEditingDomain);
	// }
	// }
	// }
	// }

	protected void loadModel(IModelComparisonScope comparisonScope, IProgressMonitor monitor) {
		if (comparisonScope != null && comparisonScope.isFileBasedComparison()) {
			final Set<IFile> sphinxModelFiles = new HashSet<IFile>();
			ResourceSet nonSphinxModelResouceSet = new ResourceSetImpl();

			IFile leftFile = comparisonScope.getLeftFile();
			if (leftFile != null) {
				if (ModelDescriptorRegistry.INSTANCE.isModelFile(leftFile)) {
					sphinxModelFiles.add(leftFile);
				} else {
					leftResource = EcoreResourceUtil.loadResource(nonSphinxModelResouceSet, EcorePlatformUtil.createURI(leftFile.getFullPath()),
							getLoadOptions());
				}
			}
			IFile rightFile = comparisonScope.getRightFile();
			if (rightFile != null) {
				if (ModelDescriptorRegistry.INSTANCE.isModelFile(rightFile)) {
					sphinxModelFiles.add(rightFile);
				} else {
					rightResource = EcoreResourceUtil.loadResource(nonSphinxModelResouceSet, EcorePlatformUtil.createURI(rightFile.getFullPath()),
							getLoadOptions());
				}
			}

			ModelLoadManager.INSTANCE.loadFiles(sphinxModelFiles, false, monitor);
			if (leftResource == null) {
				leftResource = EcorePlatformUtil.getResource(leftFile);
			}
			if (rightResource == null) {
				rightResource = EcorePlatformUtil.getResource(rightFile);
			}
		}
	}

	/**
	 * Returns the load options to consider while loading the underlying model being edited. Default implementation
	 * returns the default load options provided by the Sphinx EMF platform utility {@linkplain EcoreResourceUtil}.
	 * Clients may override this method in order to specify custom options.
	 *
	 * @return The load options to consider while loading the underlying model being edited.
	 */
	protected Map<?, ?> getLoadOptions() {
		return EcoreResourceUtil.getDefaultLoadOptions();
	}
}
