/**
 * <copyright>
 *
 * Copyright (c) 2011-2014 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [393312] Make sure that transient item providers created by extended item providers can be used before the getChildren() method of the latter has been called
 *     itemis - [447193] Enable transient item providers to be created through adapter factories
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.ide.ui.providers;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.edit.ParameterItemProvider;

public class ExtendedParameterItemProvider extends ParameterItemProvider {

	public ExtendedParameterItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	@Override
	public Object getParent(Object object) {
		Object parent = super.getParent(object);
		return adapterFactory.adapt(parent, ParametersItemProvider.class);
	}
}
