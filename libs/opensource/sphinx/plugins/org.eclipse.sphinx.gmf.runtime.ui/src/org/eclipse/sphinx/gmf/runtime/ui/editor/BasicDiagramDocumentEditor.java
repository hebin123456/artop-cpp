/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [392464] Finish up Sphinx editor socket for GMF-based graphical editors
 *     itemis - [393479] Enable BasicTabbedPropertySheetTitleProvider to retrieve same AdapterFactory as underlying IWorkbenchPart is using
 *     itemis - [458518] Add org.eclipse.sphinx.emf.editors plug-in
 *
 * </copyright>
 */
package org.eclipse.sphinx.gmf.runtime.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.sphinx.emf.editors.ModelEditorUndoContextManager;
import org.eclipse.sphinx.emf.workspace.ui.saving.BasicModelSaveablesProvider;
import org.eclipse.sphinx.emf.workspace.ui.saving.BasicModelSaveablesProvider.SiteNotifyingSaveablesLifecycleListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.navigator.SaveablesProvider;

public class BasicDiagramDocumentEditor extends DiagramDocumentEditor implements ISaveablesSource {

	protected SaveablesProvider modelSaveablesProvider;
	protected ModelEditorUndoContextManager undoContextManager;

	public BasicDiagramDocumentEditor() {
		super(true);
	}

	public BasicDiagramDocumentEditor(boolean hasFlyoutPalette) {
		super(hasFlyoutPalette);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);

		modelSaveablesProvider = createModelSaveablesProvider();
		modelSaveablesProvider.init(createModelSaveablesLifecycleListener());

		undoContextManager = new ModelEditorUndoContextManager(site, this, getEditingDomain());
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

	/*
	 * @see org.eclipse.ui.part.EditorPart#isSaveOnCloseNeeded()
	 */
	@Override
	public boolean isSaveOnCloseNeeded() {
		// Leave default behavior in place which triggers a user prompt when the the editor is closed while it is still
		// dirty
		/*
		 * !! Important Note !! We don't want to return false like
		 * org.eclipse.sphinx.emf.editors.forms.BasicTransactionalFormEditor does. In contrast to the domain model that
		 * is kept in memory when form editors are closed we consider that there is no value in doing so for the diagram
		 * model. We therefore unload the diagram model when the diagram editor is closed (see
		 * org.eclipse.sphinx.gmf.runtime.ui.editor.document.BasicDocumentProvider.DiagramElementInfo#dispose() for
		 * details) and consequently must prompt the user to save the latter when closing the diagram editor while it is
		 * still dirty.
		 */
		return super.isSaveOnCloseNeeded();
	}

	/*
	 * @see org.eclipse.ui.ISaveablesSource#getActiveSaveables()
	 */
	@Override
	public Saveable[] getActiveSaveables() {
		return getSaveables();
	}

	/*
	 * @see org.eclipse.ui.ISaveablesSource#getSaveables()
	 */
	@Override
	public Saveable[] getSaveables() {
		if (modelSaveablesProvider != null) {
			List<Saveable> saveables = new ArrayList<Saveable>(2);

			// Add saveable of diagram
			Diagram diagram = getDiagram();
			Saveable diagramSaveable = modelSaveablesProvider.getSaveable(diagram);
			if (diagramSaveable != null) {
				saveables.add(diagramSaveable);
			}

			// Add saveable of domain model
			Saveable domainModelSaveable = modelSaveablesProvider.getSaveable(diagram.getElement());
			if (domainModelSaveable != null) {
				saveables.add(domainModelSaveable);
			}

			return saveables.toArray(new Saveable[saveables.size()]);
		}
		return new Saveable[0];
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class key) {
		// FIXME Retrieve IPropertySheetPage created by
		// org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor.getAdapter(Class) and
		// register it with
		// org.eclipse.sphinx.gmf.runtime.ui.internal.editor.ModelEditorUndoContextManager.setTargetPropertySheetPage(IPropertySheetPage)
		if (key.equals(AdapterFactory.class)) {
			return getAdapterFactory();
		} else if (key.equals(IUndoContext.class)) {
			// Used by undo/redo actions to get their undo context
			return undoContextManager.getUndoContext();
		} else {
			return super.getAdapter(key);
		}
	}

	/**
	 * Returns the {@link AdapterFactory adapter factory} to be used by this {@link BasicTransactionalFormEditor form
	 * editor} for creating {@link ItemProviderAdapter item provider}s which control the way how {@link EObject model
	 * element}s from given <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns the {@link AdapterFactory adapter factory} which is embedded in the given
	 * <code>editingDomain</code> by default. Clients which want to use an alternative {@link AdapterFactory adapter
	 * factory} (e.g., an {@link AdapterFactory adapter factory} that creates {@link ItemProviderAdapter item provider}s
	 * which are specifically designed for the {@link IEditorPart editor} in which this
	 * {@link BasicTransactionalFormEditor form editor} is used) may override {@link #getCustomAdapterFactory()} and
	 * return any {@link AdapterFactory adapter factory} of their choice. This custom {@link AdapterFactory adapter
	 * factory} will then be returned as result by this method.
	 * </p>
	 *
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} whose embedded {@link AdapterFactory adapter
	 *            factory} is to be returned as default. May be left <code>null</code> if
	 *            {@link #getCustomAdapterFactory()} has been overridden and returns a non-<code>null</code> result.
	 * @return The {@link AdapterFactory adapter factory} that will be used by this {@link BasicTransactionalFormEditor
	 *         form editor}. <code>null</code> if no custom {@link AdapterFactory adapter factory} is provided through
	 *         {@link #getCustomAdapterFactory()} and no <code>editingDomain</code> has been specified.
	 * @see #getCustomAdapterFactory()
	 */
	public AdapterFactory getAdapterFactory() {
		EditingDomain editingDomain = getEditingDomain();
		AdapterFactory customAdapterFactory = getCustomAdapterFactory();
		if (customAdapterFactory != null) {
			return customAdapterFactory;
		} else if (editingDomain != null) {
			return ((AdapterFactoryEditingDomain) editingDomain).getAdapterFactory();
		}
		return null;
	}

	/**
	 * Returns a custom {@link AdapterFactory adapter factory} to be used by this {@link BasicTransactionalFormEditor
	 * form editor} for creating {@link ItemProviderAdapter item provider}s which control the way how {@link EObject
	 * model element}s from given <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns <code>null</code> as default. Clients which want to use their own
	 * {@link AdapterFactory adapter factory} (e.g., an {@link AdapterFactory adapter factory} that creates
	 * {@link ItemProviderAdapter item provider}s which are specifically designed for the {@link IEditorPart editor} in
	 * which this {@link BasicTransactionalFormEditor form editor} is used) may override this method and return any
	 * {@link AdapterFactory adapter factory} of their choice. This custom {@link AdapterFactory adapter factory} will
	 * then be returned as result by {@link #getAdapterFactory(TransactionalEditingDomain)}.
	 * </p>
	 *
	 * @return The custom {@link AdapterFactory adapter factory} that is to be used by this
	 *         {@link BasicTransactionalFormEditor form editor}. <code>null</code> the default {@link AdapterFactory
	 *         adapter factory} returned by {@link #getAdapterFactory(TransactionalEditingDomain)} should be used
	 *         instead.
	 * @see #getAdapterFactory(TransactionalEditingDomain)
	 */
	protected AdapterFactory getCustomAdapterFactory() {
		return null;
	}

	/*
	 * Overridden to deactivate sanity checking and avoid that diagram file can be synchronized/reloaded by this diagram
	 * editor (see #sanityCheckState() and #handleEditorInputChanged() for details). This is actually not necessary as
	 * reloading of diagram and domain model files is already taken in charge by Sphinx model synchronizer.
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor#enableSanityChecking(boolean)
	 */
	@Override
	protected void enableSanityChecking(boolean enable) {
		super.enableSanityChecking(false);
	}

	/*
	 * @see org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor#dispose()
	 */
	@Override
	public void dispose() {
		if (undoContextManager != null) {
			undoContextManager.dispose();
		}
		if (modelSaveablesProvider != null) {
			modelSaveablesProvider.dispose();
		}
		super.dispose();
	}
}
