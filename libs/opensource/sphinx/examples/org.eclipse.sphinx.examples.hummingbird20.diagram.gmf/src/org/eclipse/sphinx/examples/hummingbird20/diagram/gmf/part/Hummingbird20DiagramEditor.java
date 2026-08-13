/**
 * <copyright>
 * 
 * Copyright (c) 2013 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gmf.runtime.common.ui.services.marker.MarkerNavigationService;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.ui.actions.ActionIds;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocument;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocument;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocumentProvider;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.navigator.Hummingbird20NavigatorItem;
import org.eclipse.sphinx.examples.hummingbird20.ide.ui.providers.Hummingbird20ItemProviderAdapterFactory;
import org.eclipse.sphinx.gmf.runtime.ui.editor.BasicDiagramDocumentEditor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;

/**
 * @generated NOT
 */
public class Hummingbird20DiagramEditor extends BasicDiagramDocumentEditor implements IGotoMarker {

	/**
	 * @generated
	 */
	public static final String ID = "org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20DiagramEditorID"; //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final String CONTEXT_ID = "org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.ui.diagramContext"; //$NON-NLS-1$

	/**
	 * @generated
	 */
	public Hummingbird20DiagramEditor() {
		super(true);
	}

	/**
	 * @generated
	 */
	@Override
	protected String getContextID() {
		return CONTEXT_ID;
	}

	/**
	 * @generated
	 */
	@Override
	protected PaletteRoot createPaletteRoot(PaletteRoot existingPaletteRoot) {
		PaletteRoot root = super.createPaletteRoot(existingPaletteRoot);
		new Hummingbird20PaletteFactory().fillPalette(root);
		return root;
	}

	/**
	 * @generated
	 */
	@Override
	protected PreferencesHint getPreferencesHint() {
		return Hummingbird20DiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT;
	}

	/**
	 * @generated
	 */
	@Override
	public String getContributorId() {
		return Hummingbird20DiagramEditorPlugin.ID;
	}

	/**
	 * @generated
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class type) {
		if (type == IShowInTargetList.class) {
			return new IShowInTargetList() {
				@Override
				public String[] getShowInTargetIds() {
					return new String[] { ProjectExplorer.VIEW_ID };
				}
			};
		}
		return super.getAdapter(type);
	}

	@Override
	protected AdapterFactory getCustomAdapterFactory() {
		return Hummingbird20ItemProviderAdapterFactory.INSTANCE;
	}

	/**
	 * @generated
	 */
	@Override
	protected IDocumentProvider getDocumentProvider(IEditorInput input) {
		if (input instanceof IFileEditorInput || input instanceof URIEditorInput) {
			return Hummingbird20DiagramEditorPlugin.getInstance().getDocumentProvider();
		}
		return super.getDocumentProvider(input);
	}

	/**
	 * @generated
	 */
	@Override
	public TransactionalEditingDomain getEditingDomain() {
		IDocument document = getEditorInput() != null ? getDocumentProvider().getDocument(getEditorInput()) : null;
		if (document instanceof IDiagramDocument) {
			return ((IDiagramDocument) document).getEditingDomain();
		}
		return super.getEditingDomain();
	}

	/**
	 * @generated
	 */
	@Override
	protected void setDocumentProvider(IEditorInput input) {
		if (input instanceof IFileEditorInput || input instanceof URIEditorInput) {
			setDocumentProvider(Hummingbird20DiagramEditorPlugin.getInstance().getDocumentProvider());
		} else {
			super.setDocumentProvider(input);
		}
	}

	/**
	 * @generated
	 */
	@Override
	public void gotoMarker(IMarker marker) {
		MarkerNavigationService.getInstance().gotoMarker(this, marker);
	}

	/**
	 * @generated
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * @generated
	 */
	@Override
	public void doSaveAs() {
		performSaveAs(new NullProgressMonitor());
	}

	/**
	 * @generated
	 */
	@Override
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		Shell shell = getSite().getShell();
		IEditorInput input = getEditorInput();
		SaveAsDialog dialog = new SaveAsDialog(shell);
		IFile original = input instanceof IFileEditorInput ? ((IFileEditorInput) input).getFile() : null;
		if (original != null) {
			dialog.setOriginalFile(original);
		}
		dialog.create();
		IDocumentProvider provider = getDocumentProvider();
		if (provider == null) {
			// editor has been programmatically closed while the dialog was open
			return;
		}
		if (provider.isDeleted(input) && original != null) {
			String message = NLS.bind(Messages.Hummingbird20DiagramEditor_SavingDeletedFile, original.getName());
			dialog.setErrorMessage(null);
			dialog.setMessage(message, IMessageProvider.WARNING);
		}
		if (dialog.open() == Window.CANCEL) {
			if (progressMonitor != null) {
				progressMonitor.setCanceled(true);
			}
			return;
		}
		IPath filePath = dialog.getResult();
		if (filePath == null) {
			if (progressMonitor != null) {
				progressMonitor.setCanceled(true);
			}
			return;
		}
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = workspaceRoot.getFile(filePath);
		final IEditorInput newInput = new FileEditorInput(file);
		// Check if the editor is already open
		IEditorMatchingStrategy matchingStrategy = getEditorDescriptor().getEditorMatchingStrategy();
		IEditorReference[] editorRefs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		for (IEditorReference editorRef : editorRefs) {
			if (matchingStrategy.matches(editorRef, newInput)) {
				MessageDialog.openWarning(shell, Messages.Hummingbird20DiagramEditor_SaveAsErrorTitle,
						Messages.Hummingbird20DiagramEditor_SaveAsErrorMessage);
				return;
			}
		}
		boolean success = false;
		try {
			provider.aboutToChange(newInput);
			getDocumentProvider(newInput).saveDocument(progressMonitor, newInput, getDocumentProvider().getDocument(getEditorInput()), true);
			success = true;
		} catch (CoreException x) {
			IStatus status = x.getStatus();
			if (status == null || status.getSeverity() != IStatus.CANCEL) {
				ErrorDialog.openError(shell, Messages.Hummingbird20DiagramEditor_SaveErrorTitle,
						Messages.Hummingbird20DiagramEditor_SaveErrorMessage, x.getStatus());
			}
		} finally {
			provider.changed(newInput);
			if (success) {
				setInput(newInput);
			}
		}
		if (progressMonitor != null) {
			progressMonitor.setCanceled(!success);
		}
	}

	/**
	 * @generated
	 */
	@Override
	public ShowInContext getShowInContext() {
		return new ShowInContext(getEditorInput(), getNavigatorSelection());
	}

	/**
	 * @generated
	 */
	private ISelection getNavigatorSelection() {
		IDiagramDocument document = getDiagramDocument();
		if (document == null) {
			return StructuredSelection.EMPTY;
		}
		Diagram diagram = document.getDiagram();
		if (diagram == null || diagram.eResource() == null) {
			return StructuredSelection.EMPTY;
		}
		IFile file = WorkspaceSynchronizer.getFile(diagram.eResource());
		if (file != null) {
			Hummingbird20NavigatorItem item = new Hummingbird20NavigatorItem(diagram, file, false);
			return new StructuredSelection(item);
		}
		return StructuredSelection.EMPTY;
	}

	/**
	 * @generated
	 */
	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		DiagramEditorContextMenuProvider provider = new DiagramEditorContextMenuProvider(this, getDiagramGraphicalViewer());
		getDiagramGraphicalViewer().setContextMenu(provider);
		getSite().registerContextMenu(ActionIds.DIAGRAM_EDITOR_CONTEXT_MENU, provider, getDiagramGraphicalViewer());
	}

}
