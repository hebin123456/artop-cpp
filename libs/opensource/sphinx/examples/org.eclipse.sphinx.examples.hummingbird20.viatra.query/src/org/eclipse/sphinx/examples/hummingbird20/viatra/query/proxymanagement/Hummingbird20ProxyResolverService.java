/**
 * <copyright>
 *
 * Copyright (c) 2014-2017 itemis, IncQuery Labs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [475954] Proxies with fragment-based proxy URIs may get resolved across model boundaries
 *     IncQuery Labs, itemis - [501899] Use base index instead of IncQuery patterns
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.viatra.query.proxymanagement;

import java.util.Collection;

import org.eclipse.sphinx.emf.ecore.proxymanagement.AbstractProxyResolverService;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;

public class Hummingbird20ProxyResolverService extends AbstractProxyResolverService {

	public Hummingbird20ProxyResolverService(Collection<IMetaModelDescriptor> mmDescriptors) {
		super(mmDescriptors);
	}

	@Override
	protected void initProxyResolvers() {
		getProxyResolvers().add(new Hummingbird20ProxyResolver());
	}
}
