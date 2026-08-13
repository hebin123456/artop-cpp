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
package org.eclipse.sphinx.platform.ui.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.sphinx.platform.ui.internal.Activator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

/**
 * An abstract browse dialog enabling the searching and selecting object resources.
 * <p>
 * A collection of item objects is passed as the resource to be filtered. This abstract class shows a list of items with
 * a text entry field for a string pattern used to filter the list of items.
 * <p>
 * The clients must provide their implementations. The methods, {@link #toObject(String)} and {@link #toString(Object)},
 * to convert the item values, should be implemented by the subclasses.
 */
public abstract class AbstractFilteredObjectsSelectionDialog<T> extends FilteredItemsSelectionDialog {

	// TODO Improve names, see WorkflowFileSelectionWizard
	private final String LAST_SELECTED_ITEM_KEY = "last.selected.item"; //$NON-NLS-1$

	protected List<T> items = new ArrayList<T>();

	/**
	 * A main list label provider to provide the text for the label of given element. It maps an element (object) of the
	 * resource to be filtered to an optional text string. These text string labels are used to display the elements in
	 * the viewer's control context field.
	 * <p>
	 * To make the labels more readable, the element names are provided as the text label, instead of simply use the
	 * object.toString().
	 */
	public class BrowseItemListLabelProvider implements ILabelProvider {

		@Override
		public Image getImage(Object element) {
			return null;
		}

		/*
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) {
			// To make the labels more readable, the element names are used as the text label, instead of simply use
			// the object.toString().
			return getElementName(element);
		}

		/*
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		@Override
		public void dispose() {
		}

		/*
		 * @see
		 * org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		/*
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
		 */
		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		/*
		 * @see
		 * org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		@Override
		public void removeListener(ILabelProviderListener listener) {
		}
	}

	/**
	 * Creates a new instance of the abstract browse dialog class. The created dialog does not allow to select more than
	 * one item.
	 *
	 * @param shell
	 *            the {@linkplain Shell shell} to parent the dialog on
	 * @param items
	 *            a {@linkplain Collection collection of items} to be filtered in this browse dialog
	 */
	public AbstractFilteredObjectsSelectionDialog(Shell shell, Collection<T> items) {
		this(shell, false, items);
	}

	/**
	 * Creates a new instance of the abstract browse dialog class. A collection of item objects is passed as the
	 * resource to be filtered. This abstract class shows a list of items with a text entry field for a string pattern
	 * used to filter the list of items.
	 *
	 * @param shell
	 *            the {@linkplain Shell shell} to parent the dialog on
	 * @param multi
	 *            the boolean multi indicates whether the browse dialog allows to select more than one items in its item
	 *            list
	 * @param items
	 *            a {@linkplain Collection collection of items} to be filtered in this browse dialog
	 */
	public AbstractFilteredObjectsSelectionDialog(Shell shell, boolean multi, Collection<T> items) {
		super(shell, multi);
		this.items = new ArrayList<T>(items);
		setListLabelProvider(new BrowseItemListLabelProvider());
		setDetailsLabelProvider(new BrowseItemListLabelProvider());
	}

	/*
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#fillContentProvider(org.eclipse.ui.dialogs.
	 * FilteredItemsSelectionDialog.AbstractContentProvider,
	 * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
			throws CoreException {
		// Adds the items to the contentProvider if the itemsFilter matches the items
		for (Object item : items) {
			contentProvider.add(item, itemsFilter);
		}
	}

	/*
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getElementName(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public String getElementName(Object item) {
		return toString((T) item);
	}

	/*
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createFilter()
	 */
	@Override
	protected ItemsFilter createFilter() {
		// Creates a new instance of filter
		return new ItemsFilter() {
			/*
			 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter#matchItem(java.lang.Object)
			 */
			@Override
			@SuppressWarnings("unchecked")
			public boolean matchItem(Object item) {
				return matches(AbstractFilteredObjectsSelectionDialog.this.toString((T) item));
			}

			/*
			 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter#isConsistentItem(java.lang.Object)
			 */
			@Override
			public boolean isConsistentItem(Object item) {
				return true;
			}
		};
	}

	/*
	 * @see
	 * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createExtendedContentArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createExtendedContentArea(Composite parent) {
		return null;
	}

	/*
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getItemsComparator()
	 */
	@Override
	protected Comparator<?> getItemsComparator() {
		return new Comparator<Object>() {
			@Override
			@SuppressWarnings("unchecked")
			public int compare(Object item1, Object item2) {
				// Compares the String of the two objects, pay attention to the null case
				return safeStringCompareTo(AbstractFilteredObjectsSelectionDialog.this.toString((T) item1),
						AbstractFilteredObjectsSelectionDialog.this.toString((T) item2));
			}
		};
	}

	/**
	 * Compares two String objects, and indicates whether String str1 precedes, follows, or appears in the same position
	 * in the sort order as the specified str2.
	 * <p>
	 * Returns 0 if both strings are null, otherwise returns -1 if str1 is null, returns 1 if str2 is null, otherwise
	 * returns the java.lang.String.compareTo()
	 *
	 * @param str1
	 * @param str2
	 * @return
	 */
	protected int safeStringCompareTo(String str1, String str2) {
		if (str1 == null && str2 == null) {
			return 0;
		}

		if (str1 == null || str2 == null) {
			return str1 == null ? -1 : 1;
		}

		return str1.compareTo(str2);
	}

	/*
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#validateItem(java.lang.Object)
	 */
	@Override
	protected IStatus validateItem(Object item) {
		return Status.OK_STATUS;
	}

	/*
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getDialogSettings()
	 */
	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings().getSection(LAST_SELECTED_ITEM_KEY);
		if (settings == null) {
			settings = Activator.getDefault().getDialogSettings().addNewSection(LAST_SELECTED_ITEM_KEY);
		}
		return settings;
	}

	/**
	 * Converts the itemAsString, that is a possible string value for the dialog, into an object value. This method must
	 * be implemented by the subclasses.
	 *
	 * @param itemAsString
	 *            the String itemAsString to be converted to Object
	 * @return the T Object to be converted into with the given String name itemAsString
	 */
	protected abstract T toObject(String itemAsString);

	/**
	 * Converts the itemAsObject, that is a possible object value for the dialog, into a string value. This method must
	 * be implemented by the subclasses.
	 *
	 * @param itemAsObject
	 *            the Object itemAsObject to be converted to String
	 * @return the String to be converted into with the given Object itemAsObject
	 */
	protected abstract String toString(T itemAsObject);
}