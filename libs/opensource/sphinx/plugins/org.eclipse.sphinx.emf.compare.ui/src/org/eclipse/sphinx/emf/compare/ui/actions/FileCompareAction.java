/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [457704] Integrate EMF compare 3.x in Sphinx
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.compare.ui.actions;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.sphinx.emf.compare.ui.editor.ModelCompareEditor;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.team.internal.ui.actions.CompareAction;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action for comparing {@link IFile file}s in the workspace. Overrides Eclipse-defined action behind "Compare With >
 * Each Other" popup menu to make sure that {@link ModelCompareEditor} rather than Eclipse's {@link CompareEditor} is
 * opened when selected {@link IFile file}s are model files.
 *
 * @see ModelCompareEditor
 */
@SuppressWarnings("restriction")
public class FileCompareAction implements IObjectActionDelegate {

	private BasicCompareAction modelCompareActionDelegate;
	private CompareAction eclipseCompareActionDelegate;

	public FileCompareAction() {
		modelCompareActionDelegate = new BasicCompareAction();
		eclipseCompareActionDelegate = new CompareAction();
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		eclipseCompareActionDelegate.setActivePart(action, targetPart);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		eclipseCompareActionDelegate.selectionChanged(action, selection);
		modelCompareActionDelegate.selectionChanged(SelectionUtil.getStructuredSelection(selection));

		// Update enablement state
		if (action != null) {
			action.setEnabled(isEclipseCompareActionEnabled() | modelCompareActionDelegate.isEnabled());
		}
	}

	@Override
	public void run(IAction action) {
		if (modelCompareActionDelegate.isEnabled()) {
			modelCompareActionDelegate.run();
		} else if (isEclipseCompareActionEnabled()) {
			eclipseCompareActionDelegate.run(action);
		}
	}

	private boolean isEclipseCompareActionEnabled() {
		return eclipseCompareActionDelegate.isEnabled();
	}
}