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
package org.eclipse.sphinx.emf.ui.actions.providers;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sphinx.emf.ui.internal.Activator;
import org.eclipse.sphinx.emf.ui.util.EcoreUIUtil;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class OpenWithMenu extends ContributionItem {

	private IWorkbenchPage page;

	private EObject eObject;

	private IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();

	/**
	 * The id of this action.
	 */
	public static final String ID = org.eclipse.sphinx.emf.ui.internal.Activator.getPlugin().getSymbolicName() + ".ObjectOpenWithMenu";//$NON-NLS-1$

	/*
	 * Compares the labels from two IEditorDescriptor objects
	 */
	@SuppressWarnings("rawtypes")
	private static final Comparator comparer = new Comparator() {
		private Collator collator = Collator.getInstance();

		@Override
		public int compare(Object arg0, Object arg1) {
			String s1 = ((IEditorDescriptor) arg0).getLabel();
			String s2 = ((IEditorDescriptor) arg1).getLabel();
			return collator.compare(s1, s2);
		}
	};

	/**
	 * Constructs a new instance of <code>OpenWithMenu</code>.
	 * 
	 * @param page
	 *            the page where the editor is opened if an item within the menu is selected
	 * @param object
	 *            the selected object
	 */
	public OpenWithMenu(IWorkbenchPage page, EObject object) {
		super(ID);
		this.page = page;
		eObject = object;
	}

	/**
	 * Returns an image to show for the corresponding editor descriptor.
	 * 
	 * @param editorDesc
	 *            the editor descriptor, or null for the system editor
	 * @return the image or null
	 */
	private Image getImage(IEditorDescriptor editorDesc) {
		ImageDescriptor imageDesc = getImageDescriptor(editorDesc);
		if (imageDesc == null) {
			return null;
		}
		return Activator.INSTANCE.getResourceManager().createImage(imageDesc);
	}

	/**
	 * Returns the image descriptor for the given editor descriptor, or null if it has no image.
	 */
	private ImageDescriptor getImageDescriptor(IEditorDescriptor editorDesc) {
		ImageDescriptor imageDesc = null;
		if (editorDesc == null) {
			imageDesc = registry.getImageDescriptor(EcoreUIUtil.getDummyFileName(eObject.eClass()));
			// TODO: is this case valid, and if so, what are the implications for content-type editor bindings?
		} else {
			imageDesc = editorDesc.getImageDescriptor();
		}
		if (imageDesc == null) {
			if (editorDesc.getId().equals(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID)) {
				imageDesc = registry.getSystemExternalEditorImageDescriptor(EcoreUIUtil.getDummyFileName(eObject.eClass()));
			}
		}
		return imageDesc;
	}

	/**
	 * Creates the menu item for the editor descriptor.
	 * 
	 * @param menu
	 *            the menu to add the item to
	 * @param descriptor
	 *            the editor descriptor, or null for the system editor
	 * @param preferredEditor
	 *            the descriptor of the preferred editor, or <code>null</code>
	 */
	private void createMenuItem(Menu menu, final IEditorDescriptor descriptor, final IEditorDescriptor preferredEditor) {
		// XXX: Would be better to use bold here, but SWT does not support it.
		final MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		boolean isPreferred = preferredEditor != null && descriptor.getId().equals(preferredEditor.getId());
		menuItem.setSelection(isPreferred);
		menuItem.setText(descriptor.getLabel());
		Image image = getImage(descriptor);
		if (image != null) {
			menuItem.setImage(image);
		}
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Selection:
					if (menuItem.getSelection()) {
						openEditor(descriptor);
					}
					break;
				}
			}
		};
		menuItem.addListener(SWT.Selection, listener);
	}

	/*
	 * (non-Javadoc) Fills the menu with perspective items.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void fill(Menu menu, int index) {
		if (eObject == null) {
			return;
		}

		IEditorDescriptor defaultEditor = EcoreUIUtil.getDefaultEditor(eObject); // may be null
		// TODO aakar we should be able to store the preferred editor some how in the session properties
		// The default editor is the preferred one
		IEditorDescriptor preferredEditor = defaultEditor; // may be null

		Object[] editors = EcoreUIUtil.getEditors(eObject);
		Collections.sort(Arrays.asList(editors), comparer);

		boolean defaultFound = false;

		// Check that we don't add it twice. This is possible
		// if the same editor goes to two mappings.
		List alreadyMapped = new ArrayList();

		for (Object editor2 : editors) {
			IEditorDescriptor editor = (IEditorDescriptor) editor2;
			if (!alreadyMapped.contains(editor)) {
				createMenuItem(menu, editor, preferredEditor);
				if (defaultEditor != null && editor.getId().equals(defaultEditor.getId())) {
					defaultFound = true;
				}
				alreadyMapped.add(editor);
			}
		}

		// Only add a separator if there is something to separate
		if (editors.length > 0) {
			new MenuItem(menu, SWT.SEPARATOR);
		}

		// Add default editor. Check it if it is saved as the preference.
		if (!defaultFound && defaultEditor != null) {
			createMenuItem(menu, defaultEditor, preferredEditor);
		}
	}

	/*
	 * (non-Javadoc) Returns whether this menu is dynamic.
	 */
	@Override
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Opens the given editor on the selected object.
	 * 
	 * @param editorDescriptor
	 *            the editor descriptor, or null for the system editor
	 */
	protected void openEditor(IEditorDescriptor editorDescriptor) {
		if (eObject == null) {
			return;
		}
		if (page != null) {
			try {
				// Create editor input pointing at selected object
				IEditorInput editorInput = EcoreUIUtil.createURIEditorInput(eObject);
				if (editorInput != null) {
					page.openEditor(editorInput, editorDescriptor.getId());
				}
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}
}
