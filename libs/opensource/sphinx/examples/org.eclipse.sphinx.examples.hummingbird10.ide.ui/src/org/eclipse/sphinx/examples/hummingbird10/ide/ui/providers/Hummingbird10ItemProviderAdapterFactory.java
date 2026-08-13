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
 *     itemis - [392426] Avoid to have multiple instances of same custom adapter factory
 *     itemis - [447193] Enable transient item providers to be created through adapter factories
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird10.ide.ui.providers;

import org.eclipse.emf.edit.provider.ComposedAdapterFactory;

public class Hummingbird10ItemProviderAdapterFactory extends ComposedAdapterFactory {

	/**
	 * Singleton instance.
	 */
	public static final Hummingbird10ItemProviderAdapterFactory INSTANCE = new Hummingbird10ItemProviderAdapterFactory();

	public Hummingbird10ItemProviderAdapterFactory() {
		super(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

		addAdapterFactory(new ExtendedHummingbird10ItemProviderAdapterFactory());
	}
}
