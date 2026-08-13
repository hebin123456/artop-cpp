/**
 * <copyright>
 *
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.xtendxpand.ui.groups;

import org.eclipse.core.resources.IProject;
import org.eclipse.sphinx.platform.ui.fields.SelectionButtonField;
import org.eclipse.sphinx.platform.ui.fields.StringField;
import org.eclipse.sphinx.platform.ui.groups.AbstractGroup;
import org.eclipse.sphinx.xtendxpand.preferences.PrDefaultExcludesPreference;
import org.eclipse.sphinx.xtendxpand.preferences.PrExcludesPreference;
import org.eclipse.sphinx.xtendxpand.ui.internal.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ProtectedRegionGroup extends AbstractGroup {

	private IProject project;

	private StringField prExcludesField;
	private SelectionButtonField prDefaultExcludesField;

	public ProtectedRegionGroup(String groupName, IProject project) {
		super(groupName);
		this.project = project;
	}

	@Override
	protected void doCreateContent(Composite parent, int numColumns) {
		parent.setLayout(new GridLayout(numColumns, false));

		prExcludesField = new StringField();
		prExcludesField.setLabelText(Messages.label_prExcludes);
		prExcludesField.fillIntoGrid(parent, 3);
		prExcludesField.setTextWithoutUpdate(PrExcludesPreference.INSTANCE.get(project));
		prExcludesField.setToolTipText(Messages.tooltip_prExcludesField);

		prDefaultExcludesField = new SelectionButtonField(SWT.CHECK);
		prDefaultExcludesField.setLabelText(Messages.label_prDefaultExcludes);
		prDefaultExcludesField.fillIntoGrid(parent, 3);
		prDefaultExcludesField.setSelectionWithoutEvent(PrDefaultExcludesPreference.INSTANCE.get(project));
		prDefaultExcludesField.setToolTipText(Messages.tooltip_prDefaultExcludes);
	}

	public void setToDefault() {
		prExcludesField.setTextWithoutUpdate(PrExcludesPreference.INSTANCE.getDefaultValueAsObject());
		prDefaultExcludesField.setSelectionWithoutEvent(PrDefaultExcludesPreference.INSTANCE.getDefaultValueAsObject());
	}

	public void store() {
		if (project != null) {
			PrExcludesPreference.INSTANCE.setInProject(project, prExcludesField.getText());
			PrDefaultExcludesPreference.INSTANCE.setInProject(project, prDefaultExcludesField.isSelected());
		} else {
			PrExcludesPreference.INSTANCE.setInWorkspace(prExcludesField.getText());
			PrDefaultExcludesPreference.INSTANCE.setInWorkspace(prDefaultExcludesField.isSelected());
		}
	}

	public void dispose() {
		prExcludesField.dispose();
		prDefaultExcludesField.dispose();
	}
}
