/**
 * <copyright>
 * 
 * Copyright (c) 2008-2013 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [392424] Migrate Sphinx integration of Graphiti to Graphiti 0.9.x
 *     itemis - [418005] Add support for model files with multiple root elements
 * 
 * </copyright>
 */
package org.eclipse.sphinx.graphiti.workspace.ui.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.ui.editor.DefaultPersistencyBehavior;
import org.eclipse.graphiti.ui.editor.DefaultUpdateBehavior;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.graphiti.ui.editor.EditorInputAdapter;
import org.eclipse.graphiti.ui.editor.IDiagramEditorInput;
import org.eclipse.graphiti.ui.internal.config.ConfigurationProvider;
import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
import org.eclipse.graphiti.ui.platform.IConfigurationProvider;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.ui.util.EcoreUIUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.sphinx.emf.workspace.ui.saving.BasicModelSaveablesProvider;
import org.eclipse.sphinx.emf.workspace.ui.saving.BasicModelSaveablesProvider.SiteNotifyingSaveablesLifecycleListener;
import org.eclipse.sphinx.graphiti.workspace.ui.draganddrop.BasicGraphitiObjectTransferDropTargetListener;
import org.eclipse.sphinx.graphiti.workspace.ui.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.navigator.SaveablesProvider;

/**
 * An extended Graphiti diagram that manages Sphinx resources.
 */
// FIXME Suppress default marker behavior as handling of resource load error/warnings is already done centrally by
// Sphinx ResourceProblemHandler
@SuppressWarnings("restriction")
public class BasicGraphitiDiagramEditor extends DiagramEditor implements ISaveablesSource {

	public static final String BASIC_DIAGRAM_EDITOR_ID = "org.eclipse.sphinx.graphiti.workspace.ui.editors.graphitiDiagram"; //$NON-NLS-1$

	private DiagramEditorInput diagramEditorInput;

	private IConfigurationProvider configurationProvider;

	protected SaveablesProvider modelSaveablesProvider;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {

		// Eclipse may call us with other inputs when a file is to be
		// opened. Try to convert this to a valid diagram input.
		if (!(input instanceof IDiagramEditorInput)) {
			IEditorInput newInput = EditorInputAdapter.adaptToDiagramEditorInput(input);
			if (!(newInput instanceof IDiagramEditorInput)) {
				throw new PartInitException("Unknown editor input: " + input); //$NON-NLS-1$
			}
			input = newInput;
		}

		// store editor input here, retrive it from createEditingDomain in order to set the Sphinx editing domain
		// instead of the Graphiti default one
		setDiagramEditorInput((DiagramEditorInput) input);
		((BasicGraphitiDiagramEditorUpdateBehavior) getUpdateBehavior()).createEditingDomain();
		try {
			// In next line GEF calls setSite(), setInput(),
			// getEditingDomain(), ...
			setSite(site);
			setInput(input);
			getCommandStack().addCommandStackListener(this);
			getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
			initializeActionRegistry();
		} catch (RuntimeException e) {
			throw new PartInitException("Can not initialize editor", e); //$NON-NLS-1$
		}
		((BasicGraphitiDiagramEditorUpdateBehavior) getUpdateBehavior()).init();
		migrateDiagramModelIfNecessary();

		modelSaveablesProvider = createModelSaveablesProvider();
		modelSaveablesProvider.init(createModelSaveablesLifecycleListener());
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		// ATTENTION : Duplicate configuration provider : this is because graphiti prohibits to get actual configuration
		// provider
		// See bug 383768
		configurationProvider = new ConfigurationProvider(this, getDiagramTypeProvider());
	}

	@Override
	public Saveable[] getActiveSaveables() {
		return getSaveables();
	}

	@Override
	public Saveable[] getSaveables() {
		if (modelSaveablesProvider != null) {
			List<Saveable> saveables = new ArrayList<Saveable>(2);

			// Add saveables for Graphiti model
			Saveable graphitiSaveable = modelSaveablesProvider.getSaveable(getModelDescriptor());
			if (graphitiSaveable != null) {
				saveables.add(graphitiSaveable);
			}

			// Add saveable for BO model
			Diagram diagram = getDiagramTypeProvider().getDiagram();
			if (!diagram.getLink().getBusinessObjects().isEmpty()) {
				EObject boRoot = diagram.getLink().getBusinessObjects().get(0);
				Saveable boSaveable = modelSaveablesProvider.getSaveable(boRoot);
				if (boSaveable != null) {
					saveables.add(boSaveable);
				}
			}

			return saveables.toArray(new Saveable[saveables.size()]);
		}
		return new Saveable[] {};
	}

	@Override
	public boolean isDirty() {
		Object diagramRoot = getDiagramRoot();
		if (diagramRoot instanceof EObject) {
			// Return true if the model, this editor or both are dirty
			return ModelSaveManager.INSTANCE.isDirty(((EObject) diagramRoot).eResource());
		}
		return super.isDirty();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// Do nothing. This method is not called because BasicDiagramEditor implements ISaveablesSource. All
		// saves will therefore go through the ISaveablesSource/Saveable protocol instead (see
		// org.eclipse.ui.internal.SaveableHelper#savePart(ISaveablePart, IWorkbenchPart, IWorkbenchWindow, boolean) for
		// details).
	}

	private Object getDiagramRoot() {
		URI editorInputURI = EcoreUIUtil.getURIFromEditorInput(getEditorInput());
		return loadDiagram(editorInputURI);
	}

	// TODO Return actual type
	// FIXME Check if "getDiagram" wouldn't be sufficient given that this method is only called from isDirty()
	// FIXME Check if this shouldn't be replaced by a delegation to BasicGraphitiDiagramEditorPersistencyBehavior
	protected EObject loadDiagram(final URI uri) {
		final TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(uri);
		if (editingDomain != null) {
			try {
				return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<EObject>() {
					@Override
					public void run() {
						Map<?, ?> options = EcoreResourceUtil.getDefaultLoadOptions();

						EObject rootObject = null;
						Resource resource = EcoreResourceUtil.loadResource(editingDomain.getResourceSet(), uri, options);
						if (!resource.getContents().isEmpty()) {
							rootObject = resource.getContents().get(0);
						}
						setResult(rootObject);
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			}
		}
		return null;
	}

	protected SaveablesProvider createModelSaveablesProvider() {
		return new BasicModelSaveablesProvider();
	}

	/**
	 * Creates an {@linkplain ISaveablesLifecycleListener}
	 * 
	 * @return
	 */
	protected ISaveablesLifecycleListener createModelSaveablesLifecycleListener() {
		return new SiteNotifyingSaveablesLifecycleListener(this) {
			@Override
			public void handleLifecycleEvent(SaveablesLifecycleEvent event) {
				super.handleLifecycleEvent(event);

				if (event.getEventType() == SaveablesLifecycleEvent.DIRTY_CHANGED) {
					firePropertyChange(IEditorPart.PROP_DIRTY);
				}
			}
		};
	}

	protected Object getModelDescriptor() {
		IEditorInput editorInput = getEditorInput();
		if (editorInput instanceof DiagramEditorInput) {
			URI uri = EcoreUIUtil.getURIFromEditorInput(editorInput);
			IFile file = GraphitiUiInternal.getEmfService().getFile(uri);
			return ModelDescriptorRegistry.INSTANCE.getModel(file);
		} else if (editorInput instanceof IFileEditorInput) {
			return ModelDescriptorRegistry.INSTANCE.getModel(((IFileEditorInput) editorInput).getFile());
		} else if (editorInput instanceof URIEditorInput) {
			URI uri = EcoreUIUtil.getURIFromEditorInput(editorInput);
			EObject modelFragment = EcoreResourceUtil.getEObject(getResourceSet(), uri);
			if (modelFragment != null) {
				return ModelDescriptorRegistry.INSTANCE.getModel(modelFragment.eResource());
			}
		}
		return null;
	}

	@Override
	protected DefaultUpdateBehavior createUpdateBehavior() {
		return new BasicGraphitiDiagramEditorUpdateBehavior(this);
	}

	@Override
	protected DefaultPersistencyBehavior createPersistencyBehavior() {
		return new BasicGraphitiDiagramEditorPersistencyBehavior(this);
	}

	@Override
	public void dispose() {
		if (modelSaveablesProvider != null) {
			modelSaveablesProvider.dispose();
		}
		super.dispose();
	}

	/**
	 * @return the diagramEditorInput
	 */
	@Override
	public DiagramEditorInput getDiagramEditorInput() {
		return diagramEditorInput;
	}

	/**
	 * @param diagramEditorInput
	 *            the diagramEditorInput to set
	 */
	public void setDiagramEditorInput(DiagramEditorInput diagramEditorInput) {
		this.diagramEditorInput = diagramEditorInput;
	}

	/**
	 * Called to initialize the editor with its content. Here everything is done, which is dependent of the
	 * IConfigurationProvider.
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#initializeGraphicalViewer()
	 */
	@Override
	protected void initializeGraphicalViewer() {
		// Important! Add custom drop target listener before calling super method because the drop handler will choose
		// the first listener
		// which is compatible with the drop object transfer
		// TODO: see Bug 383765
		getGraphicalViewer().addDropTargetListener(
				new BasicGraphitiObjectTransferDropTargetListener(getGraphicalViewer(), this, configurationProvider));
		super.initializeGraphicalViewer();
	}

}