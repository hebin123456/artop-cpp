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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * @generated
 */
public class Hummingbird20CreationWizard extends Wizard implements INewWizard {

	/**
	 * @generated
	 */
	private IWorkbench workbench;

	/**
	 * @generated
	 */
	protected IStructuredSelection selection;

	/**
	 * @generated
	 */
	protected Hummingbird20CreationWizardPage diagramModelFilePage;

	/**
	 * @generated
	 */
	protected Hummingbird20CreationWizardPage domainModelFilePage;

	/**
	 * @generated
	 */
	protected Resource diagram;

	/**
	 * @generated
	 */
	private boolean openNewlyCreatedDiagramEditor = true;

	/**
	 * @generated
	 */
	public IWorkbench getWorkbench() {
		return workbench;
	}

	/**
	 * @generated
	 */
	public IStructuredSelection getSelection() {
		return selection;
	}

	/**
	 * @generated
	 */
	public final Resource getDiagram() {
		return diagram;
	}

	/**
	 * @generated
	 */
	public final boolean isOpenNewlyCreatedDiagramEditor() {
		return openNewlyCreatedDiagramEditor;
	}

	/**
	 * @generated
	 */
	public void setOpenNewlyCreatedDiagramEditor(boolean openNewlyCreatedDiagramEditor) {
		this.openNewlyCreatedDiagramEditor = openNewlyCreatedDiagramEditor;
	}

	/**
	 * @generated
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		setWindowTitle(Messages.Hummingbird20CreationWizardTitle);
		setDefaultPageImageDescriptor(Hummingbird20DiagramEditorPlugin.getBundledImageDescriptor("icons/wizban/NewInstanceModel20Wizard.gif")); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}

	/**
	 * @generated
	 */
	@Override
	public void addPages() {
		diagramModelFilePage = new Hummingbird20CreationWizardPage("DiagramModelFile", getSelection(), "instancemodel_diagram"); //$NON-NLS-1$ //$NON-NLS-2$
		diagramModelFilePage.setTitle(Messages.Hummingbird20CreationWizard_DiagramModelFilePageTitle);
		diagramModelFilePage.setDescription(Messages.Hummingbird20CreationWizard_DiagramModelFilePageDescription);
		addPage(diagramModelFilePage);

		domainModelFilePage = new Hummingbird20CreationWizardPage("DomainModelFile", getSelection(), "instancemodel") { //$NON-NLS-1$ //$NON-NLS-2$

			@Override
			public void setVisible(boolean visible) {
				if (visible) {
					String fileName = diagramModelFilePage.getFileName();
					fileName = fileName.substring(0, fileName.length() - ".instancemodel_diagram".length()); //$NON-NLS-1$
					setFileName(Hummingbird20DiagramEditorUtil.getUniqueFileName(getContainerFullPath(), fileName, "instancemodel")); //$NON-NLS-1$
				}
				super.setVisible(visible);
			}
		};
		domainModelFilePage.setTitle(Messages.Hummingbird20CreationWizard_DomainModelFilePageTitle);
		domainModelFilePage.setDescription(Messages.Hummingbird20CreationWizard_DomainModelFilePageDescription);
		addPage(domainModelFilePage);
	}

	/**
	 * @generated
	 */
	@Override
	public boolean performFinish() {
		IRunnableWithProgress op = new WorkspaceModifyOperation(null) {

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InterruptedException {
				diagram = Hummingbird20DiagramEditorUtil.createDiagram(diagramModelFilePage.getURI(), domainModelFilePage.getURI(), monitor);
				if (isOpenNewlyCreatedDiagramEditor() && diagram != null) {
					try {
						Hummingbird20DiagramEditorUtil.openDiagram(diagram);
					} catch (PartInitException e) {
						ErrorDialog.openError(getContainer().getShell(), Messages.Hummingbird20CreationWizardOpenEditorError, null, e.getStatus());
					}
				}
			}
		};
		try {
			getContainer().run(false, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof CoreException) {
				ErrorDialog.openError(getContainer().getShell(), Messages.Hummingbird20CreationWizardCreationError, null,
						((CoreException) e.getTargetException()).getStatus());
			} else {
				Hummingbird20DiagramEditorPlugin.getInstance().logError("Error creating diagram", e.getTargetException()); //$NON-NLS-1$
			}
			return false;
		}
		return diagram != null;
	}
}
