/**
 * <copyright>
 * 
 * Copyright (c) 2008-2011 See4sys and others.
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
package org.eclipse.sphinx.emf.editors.forms.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sphinx.emf.editors.forms.BasicTransactionalFormEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.Saveable;

/**
 * A default {@link Saveable} implementation that wrap a regular workbench part. It's used by the
 * {@link BasicTransactionalFormEditor} when the resource resides outside the workspace and can be used when the
 * workbench part does not itself adapt to {@link Saveable}.
 * 
 * @since 3.2
 */
public class DefaultSaveable extends Saveable {

	private IWorkbenchPart part;

	/**
	 * Creates a new DefaultSaveable.
	 * 
	 * @param part
	 *            the part represented by this model
	 */
	public DefaultSaveable(IWorkbenchPart part) {
		this.part = part;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.Saveable#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		if (part instanceof ISaveablePart) {
			ISaveablePart saveable = (ISaveablePart) part;
			saveable.doSave(monitor);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.Saveable#getName()
	 */
	@Override
	public String getName() {
		if (part instanceof IWorkbenchPart2) {
			return ((IWorkbenchPart2) part).getPartName();
		}
		return part.getTitle();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.Saveable#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		Image image = part.getTitleImage();
		if (image == null) {
			return null;
		}
		return ImageDescriptor.createFromImage(image);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.Saveable#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		return part.getTitleToolTip();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.Saveable#isDirty()
	 */
	@Override
	public boolean isDirty() {
		if (part instanceof ISaveablePart) {
			return ((ISaveablePart) part).isDirty();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return part.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DefaultSaveable other = (DefaultSaveable) obj;
		if (part == null) {
			if (other.part != null) {
				return false;
			}
		} else if (!part.equals(other.part)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.Saveable#show(org.eclipse.ui.IWorkbenchPage)
	 */
	@Override
	public boolean show(IWorkbenchPage page) {
		IWorkbenchPartReference reference = page.getReference(part);
		if (reference != null) {
			page.activate(part);
			return true;
		}
		if (part instanceof IViewPart) {
			IViewPart viewPart = (IViewPart) part;
			try {
				page.showView(viewPart.getViewSite().getId(), viewPart.getViewSite().getSecondaryId(), IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException e) {
				return false;
			}
			return true;
		}
		return false;
	}
}
