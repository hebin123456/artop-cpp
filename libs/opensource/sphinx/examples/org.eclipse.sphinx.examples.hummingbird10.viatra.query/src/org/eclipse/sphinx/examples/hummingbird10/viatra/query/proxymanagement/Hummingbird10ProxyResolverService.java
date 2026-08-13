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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.sphinx.emf.ecore.proxymanagement.AbstractProxyResolverService;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;

public class Hummingbird10ProxyResolverService extends AbstractProxyResolverService {

	protected static final String URI_FEATURE_SEGMENT_PREFIX = "@"; //$NON-NLS-1$
	protected static final String URI_FEATURE_SEGMENT_INDEX_SEPARATOR = "."; //$NON-NLS-1$

	private Map<String, EClass> featureNameToEClassMap;

	public Hummingbird10ProxyResolverService(Collection<IMetaModelDescriptor> mmDescriptors) {
		super(mmDescriptors);
	}

	@Override
	protected void initProxyResolvers() {
		getProxyResolvers().add(new Hummingbird10ProxyResolver());
	}

	@Override
	protected EClass getTargetEClass(URI uri) {
		String fragment = uri.fragment();
		if (fragment != null) {
			int featurePrefixIdx = fragment.lastIndexOf(URI_FEATURE_SEGMENT_PREFIX);
			if (featurePrefixIdx != -1 && fragment.length() > featurePrefixIdx + 1) {
				String featureName = fragment.substring(featurePrefixIdx + 1);
				int featureIndexSeparatorIdx = featureName.lastIndexOf(URI_FEATURE_SEGMENT_INDEX_SEPARATOR);
				if (featureIndexSeparatorIdx != -1) {
					featureName = featureName.substring(0, featureIndexSeparatorIdx);
				}

				return getTargetEClass(featureName);
			}
		}
		return null;
	}

	protected EClass getTargetEClass(String featureName) {
		if (featureNameToEClassMap == null) {
			featureNameToEClassMap = new HashMap<String, EClass>();
			featureNameToEClassMap.put(Hummingbird10Package.eINSTANCE.getApplication_Interfaces().getName(),
					Hummingbird10Package.eINSTANCE.getInterface());
			featureNameToEClassMap.put(Hummingbird10Package.eINSTANCE.getApplication_Components().getName(),
					Hummingbird10Package.eINSTANCE.getComponent());
			featureNameToEClassMap.put(Hummingbird10Package.eINSTANCE.getComponent_OutgoingConnections().getName(),
					Hummingbird10Package.eINSTANCE.getConnection());
		}
		return featureNameToEClassMap.get(featureName);
	}
}
