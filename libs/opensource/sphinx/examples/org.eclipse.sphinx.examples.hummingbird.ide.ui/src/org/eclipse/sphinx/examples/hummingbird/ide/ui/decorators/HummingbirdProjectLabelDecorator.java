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
package org.eclipse.sphinx.examples.hummingbird.ide.ui.decorators;

import java.net.URL;
import java.util.MissingResourceException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.sphinx.examples.hummingbird.ide.metamodel.HummingbirdMMDescriptor;
import org.eclipse.sphinx.examples.hummingbird.ide.natures.HummingbirdNature;
import org.eclipse.sphinx.examples.hummingbird.ide.preferences.IHummingbirdPreferences;
import org.eclipse.sphinx.examples.hummingbird.ide.ui.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * Decorates Hummingbird projects, i.e., {@link IProject projects} with a {@link HummingbirdNature Hummingbird nature},
 * with an overlay image indicating the {@link IHummingbirdPreferences#METAMODEL_VERSION Hummingbird metamodel version}
 * they are supporting.
 */
public class HummingbirdProjectLabelDecorator implements ILightweightLabelDecorator {

	/*
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
	 * org.eclipse.jface.viewers.IDecoration)
	 */
	@Override
	public void decorate(Object element, IDecoration decoration) {
		// Handle only projects
		if (element instanceof IProject) {
			// Try to retrieve Hummingbird metamodel descriptor from given project
			HummingbirdMMDescriptor mmDescriptor = IHummingbirdPreferences.METAMODEL_VERSION.get((IProject) element);
			if (mmDescriptor != null) {
				// Adapt Hummingbird metamodel descriptor in order to get its item label provider
				IItemLabelProvider itemLabelProvider = (IItemLabelProvider) Platform.getAdapterManager().loadAdapter(mmDescriptor,
						IItemLabelProvider.class.getName());
				if (itemLabelProvider != null) {
					try {
						// URL of the image given by item label provider
						Object imageURL = itemLabelProvider.getImage(mmDescriptor);
						if (imageURL instanceof URL) {
							// Obtain corresponding image descriptor from image URL
							ImageDescriptor imageDescriptor = Activator.getPlugin().getImageDescriptor((URL) imageURL);

							// Add image as top left decoration
							decoration.addOverlay(imageDescriptor, IDecoration.TOP_LEFT);
						}
					} catch (MissingResourceException ex) {
						PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
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
