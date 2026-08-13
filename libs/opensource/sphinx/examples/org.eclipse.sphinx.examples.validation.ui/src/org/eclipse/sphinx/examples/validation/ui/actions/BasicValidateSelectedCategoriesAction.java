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
package org.eclipse.sphinx.examples.validation.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.validation.model.Category;
import org.eclipse.emf.validation.model.CategoryManager;
import org.eclipse.emf.validation.service.IConstraintFilter;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.sphinx.emf.validation.diagnostic.filters.ConstraintCategoryFilter;
import org.eclipse.sphinx.emf.validation.util.ValidationUtil;
import org.eclipse.sphinx.examples.validation.ui.internal.Activator;
import org.eclipse.sphinx.examples.validation.ui.internal.messages.Messages;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.ListSelectionDialog;

/**
 * @since 0.7.0
 */
public class BasicValidateSelectedCategoriesAction extends BaseSelectionListenerAction {

	/**
	 * Constructor.
	 */
	public BasicValidateSelectedCategoriesAction() {
		super(Messages.action_validateSelectedCategories_label);
	}

	@Override
	public void run() {
		// Retrieves filter for categories of constraints to validate (user's selection)
		final IConstraintFilter selectedConstraintCategoryFilter = selectedConstraintCategories();
		if (selectedConstraintCategoryFilter == null) {
			return;
		}

		try {
			IRunnableWithProgress operation = new WorkspaceModifyDelegatingOperation(new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					List<Object> objects = new ArrayList<Object>();
					for (Iterator<?> it = getStructuredSelection().iterator(); it.hasNext();) {
						objects.add(it.next());
					}
					ValidationUtil.validate(objects, Collections.singleton(selectedConstraintCategoryFilter), monitor);
				}
			});
			// Run the validation operation, and show progress
			new ProgressMonitorDialog(ExtendedPlatformUI.getActiveShell()).run(true, true, operation);
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
	}

	// TODO Enhance dialog so that not only top level categories would be displayed.
	// TODO Externalize dialog in a dedicated class that could be accessible at the Sphinx level.
	private IConstraintFilter selectedConstraintCategories() {
		// The active shell.
		Shell shell = ExtendedPlatformUI.getActiveShell();

		IStructuredContentProvider contentProvider = createContentProvider();
		ILabelProvider labelProvider = createLabelProvider();

		String desc = Messages.dialog_SelectConstraintCategories_description;
		String title = Messages.dialog_SelectConstraintCategories_title;

		// Creates the dialog allowing user to choose categories of constraints to validate

		ListSelectionDialog dialog = new ListSelectionDialog(shell, new Object(), contentProvider, labelProvider, desc);
		dialog.setTitle(title);
		dialog.setBlockOnOpen(true);

		int result = dialog.open();
		if (result == Window.OK) {
			// The selected categories
			// Iterates over list of selected constraint categories
			ConstraintCategoryFilter constraintCategoryFilter = new ConstraintCategoryFilter();
			for (Object obj : dialog.getResult()) {
				if (obj instanceof Category) {
					constraintCategoryFilter.addCategory(((Category) obj).getId());
				}
			}
			return constraintCategoryFilter;
		}

		return null;
	}

	private IStructuredContentProvider createContentProvider() {
		return new IStructuredContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				// Retrieve the top level constraints categories
				return CategoryManager.getInstance().getTopLevelCategories().toArray();
			}

			@Override
			public void dispose() {
				// Do nothing
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// Do nothing
			}
		};
	}

	private ILabelProvider createLabelProvider() {
		return new ILabelProvider() {

			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				if (element instanceof Category) {
					return ((Category) element).getName();
				}
				return element != null ? element.toString() : ""; //$NON-NLS-1$
			}

			@Override
			public void addListener(ILabelProviderListener listener) {
				// Do nothing
			}

			@Override
			public void dispose() {
				// Do nothing
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			@Override
			public void removeListener(ILabelProviderListener listener) {
				// Do nothing
			}
		};
	}
}
