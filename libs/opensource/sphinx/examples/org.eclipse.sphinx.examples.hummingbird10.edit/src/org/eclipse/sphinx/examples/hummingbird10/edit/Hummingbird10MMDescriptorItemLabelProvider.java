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
package org.eclipse.sphinx.examples.hummingbird10.edit;

import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;

public class Hummingbird10MMDescriptorItemLabelProvider implements IItemLabelProvider {

	private static String IMG_OVR_HUMMINGBIRD10 = "full/ovr16/hummingbird10_ovr"; //$NON-NLS-1$

	@Override
	public Object getImage(Object object) {
		if (object instanceof Hummingbird10MMDescriptor) {
			return Activator.INSTANCE.getImage(IMG_OVR_HUMMINGBIRD10);
		}
		return null;
	}

	@Override
	public String getText(Object object) {
		if (object instanceof Hummingbird10MMDescriptor) {
			return ((Hummingbird10MMDescriptor) object).getName();
		}
		return null;
	}
}
