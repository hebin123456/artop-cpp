/**
 * <copyright>
 *
 * Copyright (c) 2015-2017 itemis, IncQuery Labs and others.
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
package org.eclipse.sphinx.examples.hummingbird10.viatra.query.proxymanagement;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sphinx.emf.workspace.viatra.query.proxymanagement.AbstractScopingViatraQueryProxyResolver;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;

public class Hummingbird10ProxyResolver extends AbstractScopingViatraQueryProxyResolver {

	@Override
	protected String getTargetEObjectName(URI uri) {
		// Hummingbird 1.0 proxy URI fragments are ordinary XMI URI fragments (consisting of /-separated
		// @featureName.idx segments). Therefore, it's not possible to retrieve the target object's name from such
		// proxy URIs. So just return null here which is fine because it won't abort the proxy resolution process
		// but makes it operate in a bit less optimized mode.
		return null;
	}

	@Override
	protected EStructuralFeature getTargetEObjectNameFeature(EClass eclass) {
		if (Hummingbird10Package.Literals.APPLICATION.equals(eclass)) {
			return Hummingbird10Package.Literals.APPLICATION__NAME;
		}
		if (Hummingbird10Package.Literals.COMPONENT.equals(eclass)) {
			return Hummingbird10Package.Literals.COMPONENT__NAME;
		}
		if (Hummingbird10Package.Literals.CONNECTION.equals(eclass)) {
			return Hummingbird10Package.Literals.CONNECTION__NAME;
		}
		if (Hummingbird10Package.Literals.INTERFACE.equals(eclass)) {
			return Hummingbird10Package.Literals.INTERFACE__NAME;
		}
		if (Hummingbird10Package.Literals.PARAMETER.equals(eclass)) {
			return Hummingbird10Package.Literals.PARAMETER__NAME;
		}
		return null;
	}

	@Override
	protected boolean isTypeSupported(EClass eType) {
		return eType == Hummingbird10Package.Literals.APPLICATION || eType == Hummingbird10Package.Literals.COMPONENT
				|| eType == Hummingbird10Package.Literals.CONNECTION || eType == Hummingbird10Package.Literals.INTERFACE
				|| eType == Hummingbird10Package.Literals.PARAMETER;
	}
}