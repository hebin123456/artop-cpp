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
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [458518] Add org.eclipse.sphinx.emf.editors plug-in
 *
 * </copyright>
 */
package org.eclipse.sphinx.gmf.runtime.ui.editor.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.DiagramDocument;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocument;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocumentProvider;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocument;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.editors.IModelEditorInputChangeAnalyzer;
import org.eclipse.sphinx.emf.editors.IModelEditorInputChangeHandler;
import org.eclipse.sphinx.emf.editors.ModelEditorInputSynchronizer;
import org.eclipse.sphinx.emf.ui.util.EcoreUIUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.sphinx.gmf.runtime.ui.internal.Activator;
import org.eclipse.sphinx.gmf.runtime.ui.internal.messages.Messages;
import org.eclipse.sphinx.gmf.workspace.metamodel.GMFNotationDescriptor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

public class BasicDocumentProvider extends AbstractDocumentProvider implements IDiagramDocumentProvider, IModelEditorInputChangeHandler {

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#createElementInfo(java.
	 * lang.Object)
	 */
	@Override
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		if (false == element instanceof FileEditorInput && false == element instanceof URIEditorInput) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getPlugin().getSymbolicName(), 0, NLS.bind(Messages.error_IncorrectInput,
					new Object[] { element, "org.eclipse.ui.part.FileEditorInput", "org.eclipse.emf.common.ui.URIEditorInput" }), //$NON-NLS-1$ //$NON-NLS-2$
					null));
		}
		IEditorInput editorInput = (IEditorInput) element;
		IDiagramDocument document = (IDiagramDocument) createDocument(editorInput);

		DiagramElementInfo info = new DiagramElementInfo(document, editorInput);
		info.fStatus = null;
		return info;
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#createDocument(java.lang
	 * .Object)
	 */
	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		if (false == element instanceof FileEditorInput && false == element instanceof URIEditorInput) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getPlugin().getSymbolicName(), 0, NLS.bind(Messages.error_IncorrectInput,
					new Object[] { element, "org.eclipse.ui.part.FileEditorInput", "org.eclipse.emf.common.ui.URIEditorInput" }), //$NON-NLS-1$ //$NON-NLS-2$
					null));
		}
		IEditorInput editorInput = (IEditorInput) element;
		IDiagramDocument document = (IDiagramDocument) createEmptyDocument();
		document.setEditingDomain(getEditingDomain(editorInput));

		setDocumentContent(document, editorInput);
		setupDocument(element, document);
		return document;
	}

	/**
	 * Sets up the given document as it would be provided for the given element. The content of the document is not
	 * changed. This default implementation is empty. Subclasses may re-implement.
	 *
	 * @param element
	 *            the blue-print element
	 * @param document
	 *            the document to set up
	 */
	protected void setupDocument(Object element, IDocument document) {
		// for subclasses
	}

	/*
	 * @see org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#createEmptyDocument()
	 */
	@Override
	protected IDocument createEmptyDocument() {
		return new DiagramDocument();
	}

	protected TransactionalEditingDomain getEditingDomain(IEditorInput editorInput) throws CoreException {
		IFile file = EcoreUIUtil.getFileFromEditorInput(editorInput);
		return WorkspaceEditingDomainUtil.getEditingDomain(file);
	}

	protected void setDocumentContent(IDocument document, IEditorInput element) throws CoreException {
		if (false == element instanceof FileEditorInput && false == element instanceof URIEditorInput) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getPlugin().getSymbolicName(), 0, NLS.bind(Messages.error_IncorrectInput,
					new Object[] { element, "org.eclipse.ui.part.FileEditorInput", "org.eclipse.emf.common.ui.URIEditorInput" }), //$NON-NLS-1$ //$NON-NLS-2$
					null));
		}
		IDiagramDocument diagramDocument = (IDiagramDocument) document;
		URI editorInputURI = EcoreUIUtil.getURIFromEditorInput(element);
		Diagram diagram = loadDiagram(diagramDocument.getEditingDomain(), editorInputURI);
		if (diagram == null) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getPlugin().getSymbolicName(), 0, Messages.error_NoDiagramInResource, null));
		}
		document.setContent(diagram);
	}

	protected Diagram loadDiagram(final TransactionalEditingDomain editingDomain, final URI uri) {
		if (editingDomain != null) {
			try {
				return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<Diagram>() {
					@Override
					public void run() {
						Map<?, ?> options = getLoadOptions();
						if (options == null) {
							options = EcoreResourceUtil.getDefaultLoadOptions();
						}

						Diagram diagram = null;
						Resource resource = EcoreResourceUtil.loadResource(editingDomain.getResourceSet(), uri, options);
						if (!resource.getContents().isEmpty()) {
							EObject rootObject = resource.getContents().get(0);
							if (rootObject instanceof Diagram) {
								diagram = (Diagram) rootObject;
							}
						}
						setResult(diagram);
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			}
		}
		return null;
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#isDeleted(java.lang.Object)
	 */
	@Override
	public boolean isDeleted(Object element) {
		IDiagramDocument document = getDiagramDocument(element);
		if (document != null) {
			Resource diagramResource = document.getDiagram().eResource();
			if (diagramResource != null) {
				IFile file = WorkspaceSynchronizer.getFile(diagramResource);
				return file == null || file.getLocation() == null || !file.getLocation().toFile().exists();
			}
		}
		return super.isDeleted(element);
	}

	public DiagramElementInfo getDiagramElementInfo(Object element) {
		return (DiagramElementInfo) super.getElementInfo(element);
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#disposeElementInfo(java
	 * .lang.Object, org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider.ElementInfo)
	 */
	@Override
	protected void disposeElementInfo(Object element, ElementInfo info) {
		if (info instanceof DiagramElementInfo) {
			((DiagramElementInfo) info).dispose();
		}
		super.disposeElementInfo(element, info);
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#doValidateState(java.lang
	 * .Object, java.lang.Object)
	 */
	@Override
	protected void doValidateState(Object element, Object computationContext) throws CoreException {
		/*
		 * Performance optimization: Just validate the diagram file behind editor input but do not iterate over all
		 * resources in the resource set.
		 */
		IFile file = EcoreUIUtil.getFileFromEditorInput((IEditorInput) element);
		if (file != null && file.exists() && file.isReadOnly()) {
			Collection<org.eclipse.core.resources.IFile> files2Validate = Collections.singletonList(file);
			ResourcesPlugin.getWorkspace().validateEdit(files2Validate.toArray(new IFile[files2Validate.size()]), computationContext);
		}
		super.doValidateState(element, computationContext);
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#isReadOnly(java.lang.Object
	 * )
	 */
	@Override
	public boolean isReadOnly(Object element) {
		DiagramElementInfo info = getDiagramElementInfo(element);
		if (info != null) {
			if (info.isUpdateCache()) {
				try {
					updateCache(element);
				} catch (CoreException ex) {
					PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
				}
			}
			return info.isReadOnly();
		}
		return super.isReadOnly(element);
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#isModifiable(java.lang.
	 * Object)
	 */
	@Override
	public boolean isModifiable(Object element) {
		if (!isStateValidated(element)) {
			if (element instanceof FileEditorInput || element instanceof URIEditorInput) {
				return true;
			}
		}
		DiagramElementInfo info = getDiagramElementInfo(element);
		if (info != null) {
			if (info.isUpdateCache()) {
				try {
					updateCache(element);
				} catch (CoreException ex) {
					PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
				}
			}
			return info.isModifiable();
		}
		return super.isModifiable(element);
	}

	protected void updateCache(Object element) throws CoreException {
		DiagramElementInfo info = getDiagramElementInfo(element);
		if (info != null) {
			Resource resource = info.getDiagramResource();
			IFile file = WorkspaceSynchronizer.getFile(resource);
			if (file != null && file.isReadOnly()) {
				info.setReadOnly(true);
				info.setModifiable(false);
				return;
			}
			info.setReadOnly(false);
			info.setModifiable(true);
			return;
		}
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#doUpdateStateCache(java
	 * .lang.Object)
	 */
	@Override
	protected void doUpdateStateCache(Object element) throws CoreException {
		DiagramElementInfo info = getDiagramElementInfo(element);
		if (info != null) {
			info.setUpdateCache(true);
		}
		super.doUpdateStateCache(element);
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#getResetRule(java.lang.
	 * Object)
	 */
	@Override
	protected ISchedulingRule getResetRule(Object element) {
		DiagramElementInfo info = getDiagramElementInfo(element);
		if (info != null) {
			Resource resource = info.getDiagramResource();
			IFile file = WorkspaceSynchronizer.getFile(resource);
			if (file != null) {
				return ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(file);
			}
		}
		return null;
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#getSaveRule(java.lang.Object
	 * )
	 */
	@Override
	protected ISchedulingRule getSaveRule(Object element) {
		DiagramElementInfo info = getDiagramElementInfo(element);
		if (info != null) {
			Resource resource = info.getDiagramResource();
			IFile file = WorkspaceSynchronizer.getFile(resource);
			if (file != null) {
				return computeSchedulingRule(file);
			}
		}
		return null;
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#getValidateStateRule(java
	 * .lang.Object)
	 */
	@Override
	protected ISchedulingRule getValidateStateRule(Object element) {
		DiagramElementInfo info = getDiagramElementInfo(element);
		if (info != null) {
			Collection<org.eclipse.core.runtime.jobs.ISchedulingRule> files = new ArrayList<org.eclipse.core.runtime.jobs.ISchedulingRule>();
			Resource resource = info.getDiagramResource();
			IFile file = WorkspaceSynchronizer.getFile(resource);
			if (file != null) {
				files.add(file);
			}
			return ResourcesPlugin.getWorkspace().getRuleFactory().validateEditRule(files.toArray(new IFile[files.size()]));
		}
		return null;
	}

	private ISchedulingRule computeSchedulingRule(IResource toCreateOrModify) {
		if (toCreateOrModify.exists()) {
			return ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(toCreateOrModify);
		}

		IResource parent = toCreateOrModify;
		do {
			/*
			 * XXX This is a workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=67601
			 * IResourceRuleFactory.createRule should iterate the hierarchy itself.
			 */
			toCreateOrModify = parent;
			parent = toCreateOrModify.getParent();
		} while (parent != null && !parent.exists());

		return ResourcesPlugin.getWorkspace().getRuleFactory().createRule(toCreateOrModify);
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#doSaveDocument(org.eclipse
	 * .core.runtime.IProgressMonitor, java.lang.Object,
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocument, boolean)
	 */
	@Override
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
		DiagramElementInfo info = getDiagramElementInfo(element);
		if (info != null) {
			// Perform regular save operation
			fireElementStateChanging(element);
			try {
				Resource diagramResource = info.getDiagramResource();
				ModelSaveManager.INSTANCE.saveModel(diagramResource, getSaveOptions(), false, monitor);
				Resource domainModelResource = info.getDomainModelResource();
				ModelSaveManager.INSTANCE.saveModel(domainModelResource, false, monitor);
			} catch (RuntimeException ex) {
				fireElementStateChangeFailed(element);
				throw ex;
			}
		} else {
			// Perform save as operation
			if (false == element instanceof FileEditorInput && false == element instanceof URIEditorInput) {
				fireElementStateChangeFailed(element);
				throw new CoreException(new Status(IStatus.ERROR, Activator.getPlugin().getSymbolicName(), 0, NLS.bind(Messages.error_IncorrectInput,
						new Object[] { element, "org.eclipse.ui.part.FileEditorInput", "org.eclipse.emf.common.ui.URIEditorInput" }), //$NON-NLS-1$ //$NON-NLS-2$
						null));
			}
			URI newResoruceURI = EcoreUIUtil.getURIFromEditorInput((IEditorInput) element);

			if (false == document instanceof IDiagramDocument) {
				fireElementStateChangeFailed(element);
				throw new CoreException(
						new Status(
								IStatus.ERROR,
								Activator.getPlugin().getSymbolicName(),
								0,
								"Incorrect document used: " + document + " instead of org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocument", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			IDiagramDocument diagramDocument = (IDiagramDocument) document;

			final Diagram diagramCopy = EcoreUtil.copy(diagramDocument.getDiagram());
			EcorePlatformUtil.saveNewModelResource(diagramDocument.getEditingDomain(), EcorePlatformUtil.getFile(newResoruceURI).getFullPath(),
					GMFNotationDescriptor.GMF_DIAGRAM_CONTENT_TYPE_ID, diagramCopy, false, monitor);
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.editors.IModelEditorInputChangeHandler#handleEditorInputObjectAdded(org.eclipse.ui.
	 * IEditorInput, java.util.Set)
	 */
	@Override
	public void handleEditorInputObjectAdded(IEditorInput editorInput, Set<EObject> addedObjects) {
		// Do nothing
	}

	/*
	 * @see org.eclipse.sphinx.emf.editors.IModelEditorInputChangeHandler#handleEditorInputObjectRemoved(org.eclipse.ui.
	 * IEditorInput, java.util.Set)
	 */
	@Override
	public void handleEditorInputObjectRemoved(final IEditorInput editorInput, Set<EObject> removedObjects) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				fireElementDeleted(editorInput);
			}
		});
	}

	/*
	 * @see org.eclipse.sphinx.emf.editors.IModelEditorInputChangeHandler#handleEditorInputObjectMoved(org.eclipse.ui.
	 * IEditorInput, java.util.Set)
	 */
	@Override
	public void handleEditorInputObjectMoved(IEditorInput editorInput, Set<EObject> movedObjects) {
		// Do nothing
	}

	/*
	 * @see org.eclipse.sphinx.emf.editors.IModelEditorInputChangeHandler#handleEditorInputObjectChanged(org.eclipse.ui.
	 * IEditorInput, java.util.Set)
	 */
	@Override
	public void handleEditorInputObjectChanged(IEditorInput editorInput, Set<EObject> changedObjects) {
		// Do nothing
	}

	/*
	 * @see
	 * org.eclipse.sphinx.gmf.runtime.ui.internal.editor.IModelEditorInputChangeHandler#handleEditorInputResourceLoaded
	 * (org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void handleEditorInputResourceLoaded(final IEditorInput editorInput) {
		// Do nothing
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.editors.IModelEditorInputChangeHandler#handleEditorInputResourceUnloaded(org.eclipse.ui
	 * .IEditorInput)
	 */
	@Override
	public void handleEditorInputResourceUnloaded(IEditorInput editorInput) {
		// Do nothing
	}

	/*
	 * @see
	 * org.eclipse.sphinx.gmf.runtime.ui.internal.editor.IModelEditorInputChangeHandler#handleEditorInputResourceMoved
	 * (org.eclipse.ui.IEditorInput, org.eclipse.emf.common.util.URI, org.eclipse.emf.common.util.URI)
	 */
	@Override
	public void handleEditorInputResourceMoved(final IEditorInput editorInput, final URI oldURI, final URI newURI) {
		DiagramElementInfo info = getDiagramElementInfo(editorInput);
		if (info != null) {
			if (oldURI.equals(info.getDiagramResourceURI())) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (editorInput instanceof FileEditorInput) {
							IFile newFile = ResourcesPlugin.getWorkspace().getRoot()
									.getFile(new Path(URI.decode(newURI.path())).removeFirstSegments(1));
							fireElementMoved(editorInput, newFile == null ? null : new FileEditorInput(newFile));
							return;
						}
						// TODO: append suffix to the URI! (use diagram as a parameter)
						fireElementMoved(editorInput, new URIEditorInput(newURI));
					}
				});
			} else {
				handleEditorInputResourceRemoved(editorInput);
			}
		}
	}

	/*
	 * @see
	 * org.eclipse.sphinx.gmf.runtime.ui.internal.editor.IModelEditorInputChangeHandler#handleEditorInputResourceRemoved
	 * (org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void handleEditorInputResourceRemoved(final IEditorInput editorInput) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				fireElementDeleted(editorInput);
			}
		});
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocumentProvider#createInputWithEditingDomain
	 * (org.eclipse.ui.IEditorInput, org.eclipse.emf.transaction.TransactionalEditingDomain)
	 */
	@Override
	public IEditorInput createInputWithEditingDomain(IEditorInput editorInput, TransactionalEditingDomain domain) {
		return editorInput;
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocumentProvider#getDiagramDocument(java
	 * .lang.Object)
	 */
	@Override
	public IDiagramDocument getDiagramDocument(Object element) {
		IDocument doc = getDocument(element);
		if (doc instanceof IDiagramDocument) {
			return (IDiagramDocument) doc;
		}
		return null;
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#getOperationRunner(org.
	 * eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
		return null;
	}

	protected Map<?, ?> getLoadOptions() {
		return Collections.emptyMap();
	}

	protected Map<?, ?> getSaveOptions() {
		return Collections.emptyMap();
	}

	/*
	 * @see
	 * org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider#canSaveDocument(java.lang
	 * .Object)
	 */
	@Override
	public boolean canSaveDocument(Object element) {
		DiagramElementInfo info = getDiagramElementInfo(element);
		if (info != null) {
			Resource diagramResource = info.getDiagramResource();
			if (diagramResource != null) {
				return ModelSaveManager.INSTANCE.isDirty(diagramResource);
			}
		}
		return super.canSaveDocument(element);
	}

	/**
	 * Bundle of all required information to allow diagram document to be retrieved from Sphinx-managed shared workspace
	 * editing domain.
	 */
	protected class DiagramElementInfo extends ElementInfo implements IModelEditorInputChangeAnalyzer {

		private final IDiagramDocument diagramDocument;
		private final IEditorInput editorInput;
		private boolean updateCache = true;
		private boolean modifiable = false;
		private boolean readOnly = true;

		private ModelEditorInputSynchronizer editorInputSynchronizer;

		private URI diagramResourceURI;
		private URI domainModelResourceURI;

		public DiagramElementInfo(IDiagramDocument diagramDocument, IEditorInput editorInput) {
			super(diagramDocument);

			Assert.isNotNull(diagramDocument);
			Assert.isNotNull(editorInput);
			this.diagramDocument = diagramDocument;
			this.editorInput = editorInput;

			editorInputSynchronizer = new ModelEditorInputSynchronizer(editorInput, diagramDocument.getEditingDomain(), this,
					BasicDocumentProvider.this);

			diagramResourceURI = EcoreUIUtil.getURIFromEditorInput(editorInput);
			Resource domainModelResource = getDomainModelResource();
			domainModelResourceURI = domainModelResource != null ? domainModelResource.getURI() : null;
		}

		public IEditorInput getEditorInput() {
			return editorInput;
		}

		public Resource getDiagramResource() {
			return diagramDocument.getDiagram().eResource();
		}

		public URI getDiagramResourceURI() {
			return diagramResourceURI;
		}

		public Resource getDomainModelResource() {
			EObject element = diagramDocument.getDiagram().getElement();
			if (element != null) {
				return element.eResource();
			}
			return null;
		}

		public URI getDomainModelResourceURI() {
			return domainModelResourceURI;
		}

		public void dispose() {
			editorInputSynchronizer.dispose();

			unloadResource(diagramDocument.getEditingDomain(), getDiagramResource(), false);
		}

		public boolean isUpdateCache() {
			return updateCache;
		}

		public void setUpdateCache(boolean update) {
			updateCache = update;
		}

		public boolean isModifiable() {
			return modifiable;
		}

		public void setModifiable(boolean modifiable) {
			this.modifiable = modifiable;
		}

		public boolean isReadOnly() {
			return readOnly;
		}

		public void setReadOnly(boolean readOnly) {
			this.readOnly = readOnly;
		}

		/*
		 * @see
		 * org.eclipse.sphinx.gmf.runtime.ui.internal.editor.IModelEditorInputChangeAnalyzer#containEditorInputObject
		 * (org.eclipse.ui.IEditorInput, java.util.Set)
		 */
		@Override
		public boolean containsEditorInputObject(IEditorInput editorInput, Set<EObject> removedObjects) {
			return removedObjects.contains(diagramDocument.getDiagram());
		}

		/*
		 * @see
		 * org.eclipse.sphinx.gmf.runtime.ui.internal.editor.IModelEditorInputChangeAnalyzer#containEditorInputResourceURI
		 * (org.eclipse.ui.IEditorInput, java.util.Set)
		 */
		@Override
		public boolean containsEditorInputResourceURI(IEditorInput editorInput, Set<URI> resourceURIs) {
			for (URI resourceURI : resourceURIs) {
				if (resourceURI.equals(diagramResourceURI) || resourceURI.equals(domainModelResourceURI)) {
					return true;
				}
			}
			return false;
		}
	}

	// TODO Move to EcorePlatformUtil
	public static void unloadResource(final TransactionalEditingDomain editingDomain, final Resource resource, final boolean memoryOptimized) {
		if (editingDomain != null && resource != null) {
			try {
				editingDomain.runExclusive(new Runnable() {
					@Override
					public void run() {
						try {
							if (editingDomain.getResourceSet().getResources().contains(resource)) {
								EcoreResourceUtil.unloadResource(resource, memoryOptimized);
							}
						} catch (RuntimeException ex) {
							PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
						}
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}
}