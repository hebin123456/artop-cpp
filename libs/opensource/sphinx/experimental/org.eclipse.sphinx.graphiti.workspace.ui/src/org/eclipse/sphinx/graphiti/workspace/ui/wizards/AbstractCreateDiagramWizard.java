/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [392424] Migrate Sphinx integration of Graphiti to Graphiti 0.9.x
 * 
 * </copyright>
 */
package org.eclipse.sphinx.graphiti.workspace.ui.wizards;

import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.graphiti.workspace.ui.util.DiagramUtil;
import org.eclipse.sphinx.graphiti.workspace.ui.wizards.pages.AbstractDiagramContainerWizardPage;
import org.eclipse.sphinx.graphiti.workspace.ui.wizards.pages.AbstractDiagramRootWizardPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * The Class BasicCreateDiagramWizard.
 */
// TODO Merge this class into AbstractDiagramNewWizard and create model file or not depending on selection
public abstract class AbstractCreateDiagramWizard extends BasicNewResourceWizard {

	private static final String WIZARD_WINDOW_TITLE = "New Diagram"; //$NON-NLS-1$

	private AbstractDiagramContainerWizardPage diagramContainerPage;
	private AbstractDiagramRootWizardPage diagramRootPage;

	private Diagram diagram;

	@Override
	public void addPages() {
		super.addPages();
		diagramContainerPage = createDiagramContainerWizardPage();
		addPage(diagramContainerPage);

		diagramRootPage = createDiagramRootWizardPage();
		addPage(diagramRootPage);
	}

	protected abstract AbstractDiagramContainerWizardPage createDiagramContainerWizardPage();

	protected abstract AbstractDiagramRootWizardPage createDiagramRootWizardPage();

	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setWindowTitle(WIZARD_WINDOW_TITLE);
	}

	@Override
	public boolean performFinish() {
		String diagramName = diagramContainerPage.getFileName();
		IPath containerPath = diagramContainerPage.getContainerFullPath();
		String diagramType = diagramRootPage.getDiagramType();
		EObject diagramBusinessObject = (EObject) diagramRootPage.getSelectedModelObject();
		diagram = DiagramUtil.createDiagram(containerPath, diagramName, diagramType, diagramBusinessObject);
		if (diagram != null) {
			DiagramUtil.openBasicDiagramEditor(diagram);
			return true;
		}
		return false;
	}

	public Diagram getDiagram() {
		return diagram;
	}
}
