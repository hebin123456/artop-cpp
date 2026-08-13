/**
 * <copyright>
 *
 * Copyright (c) 2014-2017 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [501899] Use base index instead of IncQuery patterns
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.viatra.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.search.ui.ModelSearchMatch;
import org.eclipse.sphinx.emf.search.ui.QuerySpecification;
import org.eclipse.sphinx.emf.search.ui.viatra.query.services.AbstractViatraQueryModelSearchService;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Package;
import org.eclipse.sphinx.examples.hummingbird20.viatra.query.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.base.api.IndexingLevel;
import org.eclipse.viatra.query.runtime.base.api.NavigationHelper;
import org.eclipse.viatra.query.runtime.emf.EMFScope;

public class Hummingbird20ModelSearchService extends AbstractViatraQueryModelSearchService {

	public Hummingbird20ModelSearchService(Collection<IMetaModelDescriptor> mmDescriptors) {
		super(mmDescriptors);
	}

	// TODO Move generic part of this method to AbstractViatraQueryModelSearchService
	@Override
	protected List<ModelSearchMatch> getMatches(QuerySpecification querySpec, ViatraQueryEngine engine) {
		try {
			final NavigationHelper baseIndex = EMFScope.extractUnderlyingEMFIndex(engine);

			final EAttribute nameAttribute = Common20Package.Literals.IDENTIFIABLE__NAME;
			baseIndex.coalesceTraversals(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					baseIndex.registerEStructuralFeatures(Collections.singleton(nameAttribute), IndexingLevel.FULL);
					return null;
				}
			});

			// TODO Check with Viatra/Query guys if simple patterns and/or RegEx can be supported
			List<ModelSearchMatch> matches = new ArrayList<ModelSearchMatch>();
			Set<EObject> matchingIdentifiables = baseIndex.findByAttributeValue(querySpec.getPattern(), nameAttribute);
			for (EObject identifiable : matchingIdentifiables) {
				matches.add(new ModelSearchMatch(identifiable));
			}
			return matches;
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			return Collections.emptyList();
		}
	}
}
