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
package org.eclipse.sphinx.emf.compare.team.subversive.ui.operations;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.compare.ui.editor.ModelElementCompareEditorInput;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.workspace.domain.WorkspaceEditingDomainManager;
import org.eclipse.sphinx.emf.workspace.domain.factory.IExtendedTransactionalEditingDomainFactory;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

@SuppressWarnings("restriction")
public class ExtendedCompareResourcesInternalOperation extends CompareResourcesInternalOperation {

	protected TransactionalEditingDomain editingDomain;

	public ExtendedCompareResourcesInternalOperation(ILocalResource local, IRepositoryResource remote) {
		super(local, remote, false, false);
	}

	public ExtendedCompareResourcesInternalOperation(ILocalResource local, IRepositoryResource remote, boolean forceReuse) {
		super(local, remote, forceReuse, false);
	}

	public ExtendedCompareResourcesInternalOperation(ILocalResource local, IRepositoryResource remote, boolean forceReuse, boolean showInDialog) {
		super(local, remote, forceReuse, showInDialog);
	}

	@Override
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		EObject leftObject = null;
		EObject rightObject = null;
		EObject ancestorObject = null;

		// Local file
		IResource localResource = local.getResource();
		leftObject = EcorePlatformUtil.loadModelRoot((IFile) localResource);
		editingDomain = getEditingDomain(MetaModelDescriptorRegistry.INSTANCE.getDescriptor(leftObject));

		// Remote file
		if (remote.getSelectedRevision() != SVNRevision.INVALID_REVISION && remote instanceof IRepositoryFile) {
			int revisionKind = remote.getSelectedRevision().getKind();
			AbstractGetFileContentOperation op = revisionKind == SVNRevision.Kind.WORKING || revisionKind == SVNRevision.Kind.BASE ? (AbstractGetFileContentOperation) new GetLocalFileContentOperation(
					local.getResource(), revisionKind) : new GetFileContentOperation(remote);
			op.run(monitor);
			if (op.getExecutionState() == IActionOperation.ERROR) {
				ExtendedCompareResourcesInternalOperation.this.reportStatus(op.getStatus());
				return;
			}
			if (monitor.isCanceled()) {
				return;
			}
			File tempFile = new File(op.getTemporaryPath());
			rightObject = EcoreResourceUtil.loadModelRoot(editingDomain.getResourceSet(), tempFile, null);
			Assert.isNotNull(rightObject);
			// Set right resource as read-only
			((AdapterFactoryEditingDomain) editingDomain).getResourceToReadOnlyMap().put(rightObject.eResource(), true);
		}

		// Ancestor file
		if (ancestor.getSelectedRevision() != SVNRevision.INVALID_REVISION && ancestor instanceof IRepositoryFile) {
			int revisionKind = ancestor.getSelectedRevision().getKind();
			AbstractGetFileContentOperation op = revisionKind == SVNRevision.Kind.WORKING || revisionKind == SVNRevision.Kind.BASE ? (AbstractGetFileContentOperation) new GetLocalFileContentOperation(
					local.getResource(), revisionKind) : new GetFileContentOperation(ancestor);
			op.run(monitor);
			monitor.done();
			if (op.getExecutionState() == IActionOperation.ERROR) {
				ExtendedCompareResourcesInternalOperation.this.reportStatus(op.getStatus());
				return;
			}
			if (monitor.isCanceled()) {
				return;
			}
			File tempFile = new File(op.getTemporaryPath());
			ancestorObject = EcoreResourceUtil.loadModelRoot(editingDomain.getResourceSet(), tempFile, null);
			Assert.isNotNull(ancestorObject);
			// Set ancestor resource as read-only
			((AdapterFactoryEditingDomain) editingDomain).getResourceToReadOnlyMap().put(ancestorObject.eResource(), true);
		}

		MatchModel matchModel = MatchService.doContentMatch(leftObject, rightObject, ancestorObject, null);
		DiffModel diffModel = DiffService.doDiff(matchModel);

		ComparisonSnapshot comparisonSnapshot = createComparisonSnapshot(matchModel, diffModel);

		CompareEditorInput input = getCompareEditorInput(comparisonSnapshot);
		openCompareEditor(input, ExtendedPlatformUI.getActivePage(), null);
	}

	protected CompareEditorInput getCompareEditorInput(ComparisonSnapshot comparisonSnapshot) {
		ModelElementCompareEditorInput input = new ModelElementCompareEditorInput(comparisonSnapshot);

		CompareConfiguration configuration = input.getCompareConfiguration();
		if (configuration != null) {
			IPreferenceStore ps = configuration.getPreferenceStore();
			if (ps != null) {
				configuration.setProperty(CompareConfiguration.USE_OUTLINE_VIEW,
						Boolean.valueOf(ps.getBoolean(ComparePreferencePage.USE_OUTLINE_VIEW)));
				// To prevent modification of distant file
				configuration.setRightEditable(false);
			}
		}
		// Set the temporary editing domain on the input. When disposing the input, the editing domain will be disposed
		// also.
		input.setEditingDomain(editingDomain);
		return input;
	}

	protected ComparisonSnapshot createComparisonSnapshot(MatchModel matchModel, DiffModel diffModel) {
		ComparisonResourceSnapshot snapshot = DiffFactory.eINSTANCE.createComparisonResourceSnapshot();
		snapshot.setDate(Calendar.getInstance().getTime());
		snapshot.setDiff(diffModel);
		snapshot.setMatch(matchModel);
		return snapshot;
	}

	/**
	 * Performs the comparison described by the given input and opens a compare editor on the result.
	 * 
	 * @param input
	 *            the input on which to open the compare editor
	 * @param page
	 *            the workbench page on which to create a new compare editor
	 * @param editor
	 *            if not null the input is opened in this editor
	 * @see CompareEditorInput
	 */
	protected void openCompareEditor(final CompareEditorInput input, final IWorkbenchPage page, final IReusableEditor editor) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (editor != null && !editor.getSite().getShell().isDisposed()) {
					// Reuse the given editor
					editor.setInput(input);
					return;
				}
				if (page != null) {
					// Open new CompareEditor on page
					try {
						page.openEditor(input, getCompareEditorId(input));
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				} else {
					MessageDialog.openError(ExtendedPlatformUI.getActiveShell(), "Open Editor Error", "Active page is null"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		};

		Display display = ExtendedPlatformUI.getDisplay();
		if (display != null) {
			display.syncExec(runnable);
		} else {
			runnable.run();
		}
	}

	/**
	 * Returns the identifier of the compare editor to open.
	 * <p>
	 * Inheriting clients may override this method in order to specify the identifier of another compare editor (e.g.
	 * according to the type of the specified input).
	 * 
	 * @param input
	 *            The {@linkplain CompareEditorInput editor input} for which a compare editor is supposed to be opened.
	 * @return The identifier of the compare editor to open.
	 */
	protected String getCompareEditorId(CompareEditorInput input) {
		// Use our own ModelCompareEditor rather than Eclipse's org.eclipse.compare.CompareEditor
		return "org.eclipse.sphinx.emf.compare.ui.editors.modelCompareEditor"; //$NON-NLS-1$
	}

	protected TransactionalEditingDomain getEditingDomain(IMetaModelDescriptor mmDescriptor) {
		Assert.isNotNull(mmDescriptor);
		if (editingDomain != null) {
			return editingDomain;
		}
		IExtendedTransactionalEditingDomainFactory factory = WorkspaceEditingDomainManager.INSTANCE.getEditingDomainFactory(mmDescriptor);
		if (factory == null) {
			// Create new EditingDomain using appropriate EditingDomainFactory
			throw new NullPointerException(NLS.bind(org.eclipse.sphinx.emf.workspace.internal.messages.Messages.error_notFound_editingDomainFactory,
					mmDescriptor.getName()));
		}
		editingDomain = factory.createEditingDomain(Collections.singletonList(mmDescriptor));
		return editingDomain;
	}
}
