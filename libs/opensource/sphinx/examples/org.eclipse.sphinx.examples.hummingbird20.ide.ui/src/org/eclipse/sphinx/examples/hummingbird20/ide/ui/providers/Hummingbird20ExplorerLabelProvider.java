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
 *     itemis - [450882] Enable navigation to ancestor tree items in Model Explorer kind of model views
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.ide.ui.providers;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.sphinx.emf.explorer.BasicExplorerLabelProvider;

public class Hummingbird20ExplorerLabelProvider extends BasicExplorerLabelProvider {

	@Override
	protected AdapterFactory getCustomAdapterFactory() {
		return Hummingbird20ItemProviderAdapterFactory.INSTANCE;
	}
}
