/**
 * <copyright>
 * 
 * Copyright (c) 2012 itemis and others.
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
package org.eclipse.sphinx.graphiti.workspace.ui.editors;

import java.util.Collection;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.IWorkspaceCommandStack;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.ui.editor.DefaultUpdateBehavior;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.graphiti.ui.internal.Messages;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.sphinx.emf.ui.util.EcoreUIUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

/**
 * An extension of the default behaviour of Graphiti editors
 */
// FIXME Remove traditional workspace synchronization copied from DefaultUpdateBehavior (updateAdapter,
// resourceSetUpdateAdapter, handleActivate(), handleChangedResource(), handleDirtyConflict(), etc.) and use
// ModelEditorInputSynchronizer instead
// FIXME Override init() and suppress installation of the default implementation's updateAdapter
@SuppressWarnings("restriction")
public class BasicGraphitiDiagramEditorUpdateBehavior extends DefaultUpdateBehavior implements IAdaptable, IEditingDomainProvider,
		IOperationHistoryListener {

	/**
	 * The part this model editor works on.
	 */
	private IEditorPart editorPart = null;

	/**
	 * Keeps track of the editing domain that is used to track all changes to the model.
	 */
	private TransactionalEditingDomain editingDomain;

	/**
	 * Closes editor if model object is deleted.
	 */
	private ElementDeleteListener elementDeleteListener = null;

	/**
	 * The update adapter is added to every {@link Resource} adapters in the {@link ResourceSet} of the
	 * {@link TransactionalEditingDomain}. When notified, it adds an
	 * {@link BasicGraphitiDiagramEditorUpdateBehavior#updateAdapter} to the adapters of the ResourceSet.
	 * 
	 * @see BasicGraphitiDiagramEditorUpdateBehavior#initializeEditingDomain(TransactionalEditingDomain)
	 */
	private ResourceSetUpdateAdapter resourceSetUpdateAdapter;

	/**
	 * Is toggled by {@link BasicGraphitiDiagramEditorUpdateBehavior#updateAdapter}.
	 */
	protected boolean resourceDeleted = false;

	/**
	 * @return the resourceDeleted
	 */
	@Override
	public boolean isResourceDeleted() {
		return resourceDeleted;
	}

	/**
	 * @param resourceDeleted
	 *            the resourceDeleted to set
	 */
	@Override
	public void setResourceDeleted(boolean resourceDeleted) {
		this.resourceDeleted = resourceDeleted;
	}

	/**
	 * Is toggled by {@link BasicGraphitiDiagramEditorUpdateBehavior#updateAdapter}.
	 */
	private boolean resourceChanged = false;

	/**
	 * @return the resourceChanged
	 */
	@Override
	public boolean isResourceChanged() {
		return resourceChanged;
	}

	/**
	 * @param resourceChanged
	 *            the resourceChanged to set
	 */
	@Override
	public void setResourceChanged(boolean resourceChanged) {
		this.resourceChanged = resourceChanged;
	}

	/**
	 * Creates a model editor responsible for the given {@link IEditorPart}.
	 * 
	 * @param editorPart
	 *            the part this model editor works on
	 */
	public BasicGraphitiDiagramEditorUpdateBehavior(IEditorPart editorPart) {
		super((DiagramEditor) editorPart);
		this.editorPart = editorPart;
	}

	/**
	 * Created the {@link TransactionalEditingDomain} that shall be used within the diagram editor and initializes it by
	 * delegating to {@link #initializeEditingDomain(TransactionalEditingDomain)}.
	 */
	@Override
	protected void createEditingDomain() {
		IEditorInput editorInput = ((BasicGraphitiDiagramEditor) diagramEditor).getDiagramEditorInput();
		if (editorInput instanceof DiagramEditorInput) {
			URI uri = EcoreUIUtil.getURIFromEditorInput(editorInput);
			TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(uri);
			initializeEditingDomain(editingDomain);
		}
	}

	/**
	 * This sets up the editing domain for this model editor.
	 * 
	 * @param domain
	 *            The {@link TransactionalEditingDomain} that is used within this model editor
	 */
	@Override
	protected void initializeEditingDomain(TransactionalEditingDomain domain) {
		editingDomain = domain;
		final ResourceSet resourceSet = domain.getResourceSet();

		resourceSetUpdateAdapter = new ResourceSetUpdateAdapter();
		resourceSet.eAdapters().add(resourceSetUpdateAdapter);

		for (final Resource r : resourceSet.getResources()) {
			if (r != null) {
				r.eAdapters().add(updateAdapter);
			}
		}
	}

	private Shell getShell() {
		return editorPart.getSite().getShell();
	}

	private final Adapter updateAdapter = new AdapterImpl() {
		@Override
		public void notifyChanged(Notification msg) {
			if (msg.getFeatureID(Resource.class) == Resource.RESOURCE__IS_LOADED) {
				if (msg.getNewBooleanValue() == Boolean.FALSE) {
					final Resource resource = (Resource) msg.getNotifier();
					final URI uri = resource.getURI();
					if (editingDomain.getResourceSet().getURIConverter().exists(uri, null)) {
						// file content has changes
						setResourceChanged(true);
						final IEditorPart activeEditor = editorPart.getSite().getPage().getActiveEditor();
						if (activeEditor == editorPart) {
							getShell().getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									handleActivate();
								}
							});
						}
					} else {
						// file has been deleted
						if (!isDirty()) {
							final IEditorInput editorInput = editorPart.getEditorInput();
							if (editorInput instanceof DiagramEditorInput) {
								EObject eObject = null;
								try {
									// Retrieve the object from the editor input
									eObject = (EObject) diagramEditor.getAdapter(Diagram.class);
								} catch (final Exception e) {
									// Exception getting object --> object probably deleted
									startCloseEditorJob();
								}
								if (eObject != null) {
									final Resource eResource = eObject.eResource();
									if (eResource == null || eResource.equals(resource)) {
										startCloseEditorJob();
									}
								}
							}
						} else {
							setResourceDeleted(true);
							final IEditorPart activeEditor = editorPart.getSite().getPage().getActiveEditor();
							if (activeEditor == editorPart) {
								if (getShell() != null && getShell().getDisplay() != null) {
									getShell().getDisplay().asyncExec(new Runnable() {
										@Override
										public void run() {
											handleActivate();
										}
									});
								}
							}
						}
					}
				}
			}
			super.notifyChanged(msg);
		}

		private void startCloseEditorJob() {
			Display display = ExtendedPlatformUI.getDisplay();
			if (display != null) {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						closeEditor();
					}
				});
			}
		}
	};

	private void closeEditor() {
		IWorkbenchPartSite site = editorPart.getSite();
		// Since we run async we have to check if our ui is still there.
		if (site == null) {
			return;
		}
		IWorkbenchPage page = site.getPage();
		if (page == null) {
			return;
		}
		page.closeEditor(editorPart, false);
	}

	@Override
	protected boolean isAdapterActive() {
		return false;
	}

	/**
	 * Handles activation of the editor.
	 */
	@Override
	public void handleActivate() {
		if (isResourceDeleted()) {
			if (handleDirtyConflict()) {
				closeEditor();
			} else {
				setResourceDeleted(false);
				setResourceChanged(false);
			}
		} else if (isResourceChanged()) {
			handleChangedResources();
			setResourceChanged(false);
		}
	}

	/**
	 * Handles what to do with changed resources on activation.
	 */
	@Override
	public void handleChangedResources() {
		if (!isDirty() || handleDirtyConflict()) {
			getOperationHistory().dispose(getUndoContext(), true, true, true);

			// Disable adapter temporarily.
			setAdapterActive(false);
			try {
				// We unload our resources such that refreshEditorContent does a complete diagram refresh.
				EList<Resource> resources = getEditingDomain().getResourceSet().getResources();
				for (Resource resource : resources) {
					resource.unload();
				}
				refreshEditorContent();
			} finally {
				setAdapterActive(true);
			}
		}
	}

	/**
	 * Shows a dialog that asks if conflicting changes should be discarded.
	 */
	@Override
	public boolean handleDirtyConflict() {
		return MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.DiscardChangesDialog_0_xmsg,
				Messages.DiscardChangesDialog_1_xmsg);
	}

	/**
	 * This returns the editing domain as required by the {@link IEditingDomainProvider} interface.
	 * 
	 * @return The {@link TransactionalEditingDomain} that is used within this editor
	 */
	@Override
	public TransactionalEditingDomain getEditingDomain() {
		return editingDomain;
	}

	/**
	 * @return the editor's dirty state
	 * @see ISaveablePart#isDirty()
	 */
	public boolean isDirty() {
		Resource diagramResource = getDiagramResource();
		if (diagramResource != null) {
			// Return true if the model, this editor or both are dirty
			return ModelSaveManager.INSTANCE.isDirty(diagramResource);
		}
		return false;
	}

	private IOperationHistory getOperationHistory() {
		IOperationHistory history = null;
		final TransactionalEditingDomain domain = getEditingDomain();
		if (domain != null) {
			final IWorkspaceCommandStack commandStack = (IWorkspaceCommandStack) getEditingDomain().getCommandStack();
			if (commandStack != null) {
				history = commandStack.getOperationHistory();
			}
		}
		return history;
	}

	@Override
	public void dispose() {
		// Remove all the registered listeners
		editingDomain.getResourceSet().eAdapters().remove(resourceSetUpdateAdapter);
		getOperationHistory().removeOperationHistoryListener(this);

		for (Resource r : editingDomain.getResourceSet().getResources()) {
			r.eAdapters().remove(updateAdapter);
		}

		EObject object = (EObject) editorPart.getEditorInput().getAdapter(EObject.class);
		if (object != null) {
			object.eAdapters().remove(elementDeleteListener);
			// FIXME Check if this should really be done here or not better in
			// BasicGraphitiDiagramEditorPersistencyBehavior
			EcorePlatformUtil.unloadFile(editingDomain, EcorePlatformUtil.getFile(object));
		}
	}

	/**
	 * Adding update adapters to the respective resources.
	 */
	private final class ResourceSetUpdateAdapter extends AdapterImpl {
		@SuppressWarnings("unchecked")
		@Override
		public void notifyChanged(Notification msg) {
			if (msg.getFeatureID(ResourceSet.class) == ResourceSet.RESOURCE_SET__RESOURCES) {
				switch (msg.getEventType()) {
				case Notification.ADD:
					((Resource) msg.getNewValue()).eAdapters().add(updateAdapter);
					break;
				case Notification.ADD_MANY:
					for (final Resource res : (Collection<Resource>) msg.getNewValue()) {
						res.eAdapters().add(updateAdapter);
					}
					break;
				case Notification.REMOVE:
					((Resource) msg.getOldValue()).eAdapters().remove(updateAdapter);
					break;
				case Notification.REMOVE_MANY:
					for (final Resource res : (Collection<Resource>) msg.getOldValue()) {
						res.eAdapters().remove(updateAdapter);
					}
					break;

				default:
					break;
				}
			}
		}
	}

	/**
	 * Closes editor if model element was deleted.
	 */
	private final class ElementDeleteListener extends AdapterImpl {

		@Override
		public boolean isAdapterForType(Object type) {
			return type instanceof EObject;
		}

		@Override
		public void notifyChanged(Notification msg) {
			final IEditorPart part = editorPart;
			final IEditorInput in = part.getEditorInput();
			if (in != null) {
				final IEditorSite site = part.getEditorSite();
				if (site == null) {
					return;
				}
				final Shell shell = site.getShell();
				// Do the real work, e.g. object retrieval from input and
				// closing, asynchronous to not block this listener longer than necessary,
				// which may provoke deadlocks.
				shell.getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						if (editorPart == null) {
							return; // disposed
						}
						if (shell.isDisposed()) {
							return; // disposed
						}
						EObject obj = null;
						try {
							obj = (EObject) in.getAdapter(EObject.class);
						} catch (final Exception e) {
							// Ignore, exception indicates that the object has been deleted
						}
						if (obj == null || EcoreUtil.getRootContainer(obj) == null) {
							// object is gone so try to close
							final IWorkbenchPage page = site.getPage();
							page.closeEditor(part, false);
						}
					}
				});
			}
		}
	}

	/**
	 * Called by editor parts when focus is set by Eclipse. It is necessary to update the action bars (undo menu)
	 * accordingly if editor receives focus
	 */
	public void setFocus() {
		handleActivate();
	}

	protected Resource getDiagramResource() {
		EObject diagram = (EObject) editorPart.getEditorInput().getAdapter(EObject.class);
		if (diagram != null) {
			return diagram.eResource();
		}
		return null;
	}

	public IUndoContext getUndoContext() {
		return ((IWorkspaceCommandStack) getEditingDomain().getCommandStack()).getDefaultUndoContext();
	}

	private void refreshEditorContent() {
		if (editorPart instanceof BasicGraphitiDiagramEditor) {
			((BasicGraphitiDiagramEditor) editorPart).refreshContent();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}
}
