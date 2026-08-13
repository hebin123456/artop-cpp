/**
 * <copyright>
 *
 * Copyright (c) 2010 See4sys and others.
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
package org.eclipse.sphinx.examples.common.ui.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Class generating the initial page layout and visible action set for a Sphinx perspective.
 */
public class SphinxPerspectiveFactory implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for the Sphinx perspective. Add views and shortcuts to compose the perspective.
	 *
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		createLayout(layout);
		addNewWizardShortcuts(layout);
		addShowViewShortcuts(layout);
		addOpenPerspectiveShortcuts(layout);
		addActionSets(layout);
		addShowInParts(layout);
	}

	/**
	 * Creates the layout of the Sphinx perspective.
	 */
	private void createLayout(IPageLayout layout) {

		// Editors are placed for free
		String editorArea = layout.getEditorArea();

		int relativePos = IPageLayout.LEFT;

		IFolderLayout topLeft = layout.createFolder(ISphinxPerspectiveConstants.TOP_LEFT, relativePos, 0.2f, editorArea);
		topLeft.addView(ISphinxPerspectiveConstants.ID_MODEL_EXPLORER);
		topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);

		relativePos = IPageLayout.BOTTOM;

		IFolderLayout bottomRight = layout.createFolder(ISphinxPerspectiveConstants.BOTTOM_RIGHT, relativePos, 0.65f, editorArea);
		bottomRight.addView(IPageLayout.ID_PROP_SHEET);
		bottomRight.addView(ISphinxPerspectiveConstants.ID_VALIDATION_VIEW);
		bottomRight.addView(ISphinxPerspectiveConstants.ID_ERROR_LOG_VIEW);
		bottomRight.addPlaceholder(IPageLayout.ID_PROBLEM_VIEW);
		bottomRight.addPlaceholder(ISphinxPerspectiveConstants.ID_CONSOLE_VIEW);
	}

	/**
	 * Adds new shortcuts to wizards. These wizards appear in the File > New menu when the Sphinx perspective is active.
	 */
	private void addNewWizardShortcuts(IPageLayout layout) {
		layout.addNewWizardShortcut(ISphinxPerspectiveConstants.ID_NEW_LINKED_FILE);
		layout.addNewWizardShortcut(ISphinxPerspectiveConstants.ID_NEW_LINKED_FOLDER);
		layout.addNewWizardShortcut(ISphinxPerspectiveConstants.ID_ECLIPSE_NEW_FOLDER);
		layout.addNewWizardShortcut(ISphinxPerspectiveConstants.ID_ECLIPSE_NEW_FILE);
	}

	/**
	 * Adds new shortcuts to views. These shortcuts appear in the Window > Show View menu when the Sphinx perspective is
	 * active.
	 */
	private void addShowViewShortcuts(IPageLayout layout) {
		layout.addShowViewShortcut(ISphinxPerspectiveConstants.ID_MODEL_EXPLORER);
		layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
		layout.addShowViewShortcut(ISphinxPerspectiveConstants.ID_VALIDATION_VIEW);
		layout.addShowViewShortcut(ISphinxPerspectiveConstants.ID_ERROR_LOG_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(ISphinxPerspectiveConstants.ID_CONSOLE_VIEW);
	}

	/**
	 * Adds new shortcuts to other perspectives. These shortcuts appear in the Window > Open Perspective menu when the
	 * Sphinx perspective is active.
	 */
	private void addOpenPerspectiveShortcuts(IPageLayout layout) {
		layout.addPerspectiveShortcut(ISphinxPerspectiveConstants.ID_RESOURCE_PERSPECTIVE);
	}

	/**
	 * Adds actions sets to the Sphinx perspective. These action sets appear in the tool bar of the Sphinx perspective.
	 */
	private void addActionSets(IPageLayout layout) {

		// Launch action set, with Debug and Run menus
		layout.addActionSet(ISphinxPerspectiveConstants.ID_LAUNCH_ACTIONSET);

		// Team action set, with Synchronize... menu
		layout.addActionSet(ISphinxPerspectiveConstants.ID_TEAM_ACTIONSET);
	}

	/**
	 * Adds views to the Show In prompters. These views appear in the Navigate > Show In menu as well as in the Show In
	 * context submenus of all views that are "Show In..." sources (e.g., the Problems view) when the Sphinx perspective
	 * is active.
	 */
	private void addShowInParts(IPageLayout layout) {

		// Sphinx Model Explorer view
		layout.addShowInPart(ISphinxPerspectiveConstants.ID_MODEL_EXPLORER);

		// Eclipse Project Explorer view
		layout.addShowInPart(IPageLayout.ID_PROJECT_EXPLORER);
	}
}
