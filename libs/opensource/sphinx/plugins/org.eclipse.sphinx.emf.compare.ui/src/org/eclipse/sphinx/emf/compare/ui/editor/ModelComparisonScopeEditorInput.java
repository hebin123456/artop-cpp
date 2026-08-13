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
package org.eclipse.sphinx.emf.compare.ui.editor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.domain.ICompareEditingDomain;
import org.eclipse.emf.compare.ide.ui.internal.configuration.EMFCompareConfiguration;
import org.eclipse.emf.compare.ide.ui.internal.editor.ComparisonScopeEditorInput;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.edit.provider.IDisposable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.compare.domain.DelegatingEMFCompareEditingDomain;
import org.eclipse.sphinx.emf.compare.scope.IModelComparisonScope;
import org.eclipse.sphinx.emf.compare.ui.internal.Activator;
import org.eclipse.sphinx.emf.compare.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.compare.util.ModelCompareUtil;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.sphinx.emf.workspace.ui.saving.BasicModelSaveablesProvider;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.navigator.SaveablesProvider;

public class ModelComparisonScopeEditorInput extends ComparisonScopeEditorInput implements ISaveablesSource {

	protected SaveablesProvider modelSaveablesProvider;

	private final IComparisonScope scope;

	// Selected EObject or IFile objects being compared
	private Object leftObject;
	private Object rightObject;

	// EMF resources if selected objects are IFile
	private Resource leftResource;
	private Resource rightResource;

	public ModelComparisonScopeEditorInput(EMFCompareConfiguration configuration, ICompareEditingDomain editingDomain, AdapterFactory adapterFactory,
			EMFCompare comparator, IComparisonScope scope) {
		super(configuration, editingDomain, adapterFactory, comparator, scope);
		this.scope = scope;
	}

	public IComparisonScope getScope() {
		return scope;
	}

	public Resource getLeftResource() {
		return leftResource;
	}

	public Resource getRightResource() {
		return rightResource;
	}

	@Override
	public boolean isDirty() {
		boolean isDirty = false;
		List<Object> selectedObjects = new ArrayList<Object>();
		selectedObjects.add(getLeftObject());
		selectedObjects.add(getRightObject());

		for (int i = 0; i < selectedObjects.size(); i++) {
			Object object = selectedObjects.get(i);
			Resource resource = null;
			if (object instanceof EObject) {
				resource = ((EObject) object).eResource();
			} else if (object instanceof Resource) {
				resource = (Resource) object;
			} else if (object instanceof IFile) {
				resource = i == 0 ? leftResource : rightResource;
			}

			if (resource != null) {
				// Return true if the model, this editor or both are dirty
				isDirty = isDirty || ModelSaveManager.INSTANCE.isDirty(resource);
			}
		}
		return isDirty;
	}

	protected void init() {
		if (modelSaveablesProvider == null) {
			modelSaveablesProvider = createModelSaveablesProvider();
			if (getWorkbenchPart() instanceof ModelCompareEditor) {
				ISaveablesLifecycleListener modelSaveablesLifecycleListener = ((ModelCompareEditor) getWorkbenchPart())
						.createModelSaveablesLifecycleListener();
				modelSaveablesProvider.init(modelSaveablesLifecycleListener);
			} else {
				if (getWorkbenchPart() == null) {
					PlatformLogUtil.logAsWarning(Activator.getPlugin(), new NullPointerException(Messages.warning_workbenchPartNull));
				} else {
					PlatformLogUtil.logAsWarning(Activator.getPlugin(), new RuntimeException(
							Messages.warning_workbenchPartInstanceofModelCompareEditor));
				}
			}
		}
	}

	/**
	 * @return The left object that is currently being edited in this editor or <code>null</code> if no such is
	 *         available.
	 */
	public Object getLeftObject() {
		if (scope instanceof IModelComparisonScope) {
			// File-based comparison
			if (((IModelComparisonScope) scope).isFileBasedComparison()) {
				if (leftObject == null) {
					leftObject = ((IModelComparisonScope) scope).getLeftFile();
				}
			} else {
				if (leftObject == null
						|| leftObject instanceof EObject
						&& (((EObject) leftObject).eIsProxy() || ((EObject) leftObject).eResource() == null || !((EObject) leftObject).eResource()
								.isLoaded())) {
					leftObject = scope.getLeft();
				}
			}
		}

		return leftObject;
	}

	/**
	 * @return The right object that is currently being edited in this editor or <code>null</code> if no such is
	 *         available.
	 */
	public Object getRightObject() {
		if (scope instanceof IModelComparisonScope) {
			// File-based comparison
			if (((IModelComparisonScope) scope).isFileBasedComparison()) {
				if (rightObject == null) {
					rightObject = ((IModelComparisonScope) scope).getRightFile();
				}
			} else {
				if (rightObject == null
						|| rightObject instanceof EObject
						&& (((EObject) rightObject).eIsProxy() || ((EObject) rightObject).eResource() == null || !((EObject) rightObject).eResource()
								.isLoaded())) {
					rightObject = scope.getRight();
				}
			}
		}

		return rightObject;
	}

	protected SaveablesProvider createModelSaveablesProvider() {
		return new BasicModelSaveablesProvider();
	}

	/**
	 * @see Bug 892 - Indicate files being compared in compare editor tab title
	 * @see #setTitle(String)
	 */
	@Override
	protected Object doPrepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		init();

		SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// FIXME The models should be loaded on demand in the
		// org.eclipse.sphinx.emf.compare.ui.viewer.structuremerge.ModelElementStructureMergeViewer. We perform model
		// loading, if needed, due to an API restriction from EMF compare side.
		if (scope instanceof IModelComparisonScope && ((IModelComparisonScope) scope).isFileBasedComparison()) {
			loadModel((IModelComparisonScope) scope, progress.newChild(50));

			((IModelComparisonScope) scope).setDelegate(new DefaultComparisonScope(leftResource, rightResource, null));

			EMFCompareConfiguration compareConfiguration = getCompareConfiguration();
			ICompareEditingDomain editingDomain = compareConfiguration.getEditingDomain();
			if (editingDomain instanceof DelegatingEMFCompareEditingDomain) {
				ICompareEditingDomain delegatingEditingDomain = ModelCompareUtil.createEMFCompareEditingDomain(leftResource, rightResource, null);
				((DelegatingEMFCompareEditingDomain) editingDomain).setDelegate(delegatingEditingDomain);
			}
		} else {
			progress.worked(50);
		}

		Object input = super.doPrepareInput(progress.newChild(50));

		String title;
		String leftLabel = getLabel(getLeftObject());
		String rightLabel = getLabel(getRightObject());
		String ancestorLabel = getAncestorLabel();
		if (ancestorLabel == null) {
			title = NLS.bind(Messages.twoWay_title, leftLabel, rightLabel);
		} else {
			title = NLS.bind(Messages.threeWay_title, new String[] { ancestorLabel, leftLabel, rightLabel });
		}
		setTitle(title);
		return input;
	}

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
	 * Returns the label of the provided object to use it in the title and the tool tip of the compare editor.
	 *
	 * @param object
	 *            an EMF object or a IFile.
	 * @return the label of the provided object to use it in the title and the tool tip of the compare editor.
	 */
	protected String getLabel(Object object) {
		String label = ""; //$NON-NLS-1$;

		if (object instanceof EObject) {
			URI uri = EcoreResourceUtil.getURI((EObject) object, true);
			label = uri.trimQuery().toString();
		} else if (object instanceof IFile) {
			label = ((IFile) object).getName();
		}

		return label;
	}

	/**
	 * Returns the label of the ancestor object in case of three way comparison.
	 *
	 * @return As specified above.
	 */
	protected String getAncestorLabel() {
		// Overrides this in case three way comparison supported
		return null;
	}

	@Override
	public void saveChanges(IProgressMonitor monitor) {
		List<Object> selectedObjects = new ArrayList<Object>();
		selectedObjects.add(getLeftObject());
		selectedObjects.add(getRightObject());

		for (int i = 0; i < selectedObjects.size(); i++) {
			Resource resource = null;
			Object object = selectedObjects.get(i);
			if (object instanceof EObject) {
				resource = ((EObject) object).eResource();
			} else if (object instanceof IFile) {
				resource = i == 0 ? leftResource : rightResource;
			}
			// Save the all dirty resources of underlying model
			if (resource != null) {
				ModelSaveManager.INSTANCE.saveModel(resource, getSaveOptions(), false, monitor);
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

	/**
	 * Returns the save options to consider while saving the underlying model being edited. Default implementation
	 * returns the default save options provided by the Sphinx EMF platform utility {@linkplain EcoreResourceUtil}.
	 * Clients may override this method in order to specify custom options.
	 *
	 * @return The save options to consider while saving the underlying model being edited.
	 */
	protected Map<?, ?> getSaveOptions() {
		return EcoreResourceUtil.getDefaultSaveOptions();
	}

	@Override
	public Saveable[] getActiveSaveables() {
		return getSaveables();
	}

	@Override
	public Saveable[] getSaveables() {
		Set<Saveable> saveables = new HashSet<Saveable>();
		if (modelSaveablesProvider != null) {
			if (getLeftObject() != null) {
				Saveable leftSaveable = modelSaveablesProvider.getSaveable(getLeftObject());
				if (leftSaveable != null) {
					saveables.add(leftSaveable);
				}
			}

			if (getRightObject() != null) {
				Saveable rightSaveable = modelSaveablesProvider.getSaveable(getRightObject());
				if (rightSaveable != null) {
					saveables.add(rightSaveable);
				}
			}
		}
		return saveables.toArray(new Saveable[saveables.size()]);
	}

	@Override
	protected void finalize() throws Throwable {
		if (modelSaveablesProvider != null) {
			modelSaveablesProvider.dispose();
			modelSaveablesProvider = null;
		}
		super.finalize();
	}

	@Override
	protected void handleDispose() {
		super.handleDispose();

		ICompareEditingDomain editingDomain = getEditingDomain();
		if (editingDomain instanceof IDisposable) {
			((IDisposable) editingDomain).dispose();
		}
	}
}
