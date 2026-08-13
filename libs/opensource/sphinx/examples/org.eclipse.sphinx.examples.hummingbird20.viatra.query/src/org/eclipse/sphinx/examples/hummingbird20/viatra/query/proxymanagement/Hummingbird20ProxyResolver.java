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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sphinx.emf.workspace.viatra.query.proxymanagement.AbstractScopingViatraQueryProxyResolver;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Package;

public class Hummingbird20ProxyResolver extends AbstractScopingViatraQueryProxyResolver {

	@Override
	protected String getTargetEObjectName(URI uri) {
		// Hummingbird 2.0 proxy URI fragments are /-separated sequences of object names reflecting the target
		// object and its containers. So, the target object's name is simply the last segment of the proxy URI
		// fragment.
		String fragment = uri.fragment();
		if (fragment != null) {
			URI fragmentURI = URI.createURI(fragment);
			return fragmentURI.lastSegment();
		}
		return null;
	}

	@Override
	protected EStructuralFeature getTargetEObjectNameFeature(EClass eclass) {
		return Common20Package.Literals.IDENTIFIABLE__NAME;
	}

	@Override
	protected boolean isTypeSupported(EClass eType) {
		return Common20Package.eINSTANCE.getIdentifiable().isSuperTypeOf(eType);
	}

}
