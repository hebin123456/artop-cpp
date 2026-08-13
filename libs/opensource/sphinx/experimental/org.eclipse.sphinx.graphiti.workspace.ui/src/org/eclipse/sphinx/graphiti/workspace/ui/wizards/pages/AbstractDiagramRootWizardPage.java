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
package org.eclipse.sphinx.graphiti.workspace.ui.wizards.pages;

import java.util.Vector;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.graphiti.dt.IDiagramType;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.sphinx.graphiti.workspace.ui.ElementTreeSelectionGroup;
import org.eclipse.sphinx.graphiti.workspace.ui.internal.messages.Messages;
import org.eclipse.sphinx.graphiti.workspace.ui.providers.ElementTreeContentProvider;
import org.eclipse.sphinx.graphiti.workspace.ui.providers.ElementTreeLabelProvider;
import org.eclipse.sphinx.platform.ui.fields.StringField;
import org.eclipse.sphinx.platform.ui.wizards.pages.AbstractWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class AbstractDiagramRootWizardPage extends AbstractWizardPage {

	private String diagramType;
	private StringField diagramTypeField;
	private ElementTreeSelectionGroup modelObjectSelectionGroup;

	public AbstractDiagramRootWizardPage(String pageName, String diagramType) {
		super(pageName);

		Assert.isLegal(diagramType != null && diagramType.trim().length() != 0);
		Assert.isLegal(isValidDiagramType(diagramType));

		this.diagramType = diagramType;
	}

	public AbstractDiagramRootWizardPage(String pageName, String title, ImageDescriptor titleImage, String diagramType) {
		this(pageName, diagramType);
		setTitle(title);
		setImageDescriptor(titleImage);
	}

	@Override
	protected Control doCreateControl(Composite parent) {

		Composite topLevel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		topLevel.setLayout(layout);
		topLevel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		diagramTypeField = new StringField(SWT.READ_ONLY | SWT.BORDER);
		diagramTypeField.setEditable(false);
		diagramTypeField.setLabelText(Messages.DiagramRootWizardPage_DiagramTypeField);
		diagramTypeField.fillIntoGrid(topLevel, 3);
		diagramTypeField.setText(getDiagramType());

		modelObjectSelectionGroup = new ElementTreeSelectionGroup(topLevel, new ElementTreeContentProvider(), new ElementTreeLabelProvider());
		GridData modelObjectLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		modelObjectLayoutData.horizontalSpan = 2;
		modelObjectSelectionGroup.setLayoutData(modelObjectLayoutData);
		modelObjectSelectionGroup.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStatus status = validateRules();
				setPageComplete(status == null || status.isOK());
			}
		});
		validateRules();
		setControl(topLevel);

		return topLevel;
	}

	@Override
	protected boolean doIsPageComplete() {
		IStatus status = isValidRootDiagram(modelObjectSelectionGroup.getViewer().getSelection());
		return status == null || status.isOK();

	}

	@Override
	protected IStatus doValidateRules() {
		IStatus status = isValidRootDiagram(modelObjectSelectionGroup.getViewer().getSelection());
		updateStatus(status);
		return status;
	}

	protected abstract IStatus isValidRootDiagram(ISelection selectedRoot);

	protected boolean isValidDiagramType(String diagramType) {
		String[] allAvailableDiagramTypes = getAllAvailableDiagramTypes();
		for (String type : allAvailableDiagramTypes) {
			if (diagramType.equals(type)) {
				return true;
			}
		}
		return false;
	}

	protected String[] getAllAvailableDiagramTypes() {
		Vector<String> diagramIds = new Vector<String>();
		for (IDiagramType diagramType : GraphitiUi.getExtensionManager().getDiagramTypes()) {
			diagramIds.add(diagramType.getId());
		}

		return diagramIds.toArray(new String[] {});
	}

	protected IStatus createErrorStatus(String message) {
		return new Status(IStatus.ERROR, "pluginId", message); //$NON-NLS-1$
	}

	public String getDiagramType() {
		return diagramType;
	}

	public Object getSelectedModelObject() {
		ISelection selection = modelObjectSelectionGroup.getSelection();
		if (selection instanceof IStructuredSelection) {
			return ((IStructuredSelection) selection).getFirstElement();
		}
		return null;
	}
}
