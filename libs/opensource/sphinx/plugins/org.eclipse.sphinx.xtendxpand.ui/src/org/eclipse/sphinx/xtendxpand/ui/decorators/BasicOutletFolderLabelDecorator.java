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
package org.eclipse.sphinx.xtendxpand.ui.decorators;

import java.util.Collection;
import java.util.MissingResourceException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.xtendxpand.outlet.ExtendedOutlet;
import org.eclipse.sphinx.xtendxpand.preferences.OutletsPreference;
import org.eclipse.sphinx.xtendxpand.ui.internal.Activator;

/**
 * Decorates outlet folders for Xpand, i.e., {@link IFolder folders} that have been configured as outlet folders via
 * {@link OutletsPreference outlet preference}, with custom folder image.
 */
public class BasicOutletFolderLabelDecorator implements ILightweightLabelDecorator {

	private static final String OUTLET_OVERLAY_IMAGE_PATH = "full/ovr16/outlet_ovr.gif"; //$NON-NLS-1$

	/**
	 * Returns the {@link OutletsPreference outlets preference} from which to retrieve the information if a given
	 * {@link IFolder folder} is an outlet folder or not.
	 * 
	 * @return The {@link OutletsPreference outlets preference} to be used by this
	 *         {@link BasicOutletFolderLabelDecorator decorator}.
	 */
	protected OutletsPreference getOutletsPreference() {
		return OutletsPreference.INSTANCE;
	}

	/*
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
	 * org.eclipse.jface.viewers.IDecoration)
	 */
	@Override
	public void decorate(Object element, IDecoration decoration) {
		// Handle only folders
		if (element instanceof IFolder) {
			IFolder folder = (IFolder) element;
			IProject project = folder.getProject();

			// Retrieve outlets from outlets preference of project behind given folder
			Collection<ExtendedOutlet> outlets = getOutletsPreference().get(project);
			if (outlets != null) {
				// Check if one to the outlets points at a folder in the workspace that equals given folder
				for (ExtendedOutlet outlet : outlets) {
					if (folder.equals(outlet.getContainer())) {
						// Add outlet overlay image folder icon
						try {
							// Obtain image descriptor of outlet overlay image
							ImageDescriptor imageDescriptor = Activator.getPlugin().getImageDescriptor(OUTLET_OVERLAY_IMAGE_PATH);

							// Add overlay image as top left decoration
							decoration.addOverlay(imageDescriptor, IDecoration.TOP_LEFT);
						} catch (MissingResourceException ex) {
							PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
						}
						break;
					}
				}
			}
		}
	}

	/*
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	@Override
	public void addListener(ILabelProviderListener listener) {
		// Do nothing
	}

	/*
	 * @see
	 * org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	@Override
	public void removeListener(ILabelProviderListener listener) {
		// Do nothing
	}

	/*
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		// Do nothing
	}
}
