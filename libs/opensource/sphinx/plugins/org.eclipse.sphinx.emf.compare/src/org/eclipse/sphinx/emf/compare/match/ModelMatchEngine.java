/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [457704] Integrate EMF compare 3.x in Sphinx
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.compare.match;

import java.util.Collections;
import java.util.Iterator;

import org.eclipse.emf.common.util.Monitor;
import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.match.DefaultComparisonFactory;
import org.eclipse.emf.compare.match.DefaultEqualityHelperFactory;
import org.eclipse.emf.compare.match.DefaultMatchEngine;
import org.eclipse.emf.compare.match.IComparisonFactory;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.rcp.EMFCompareRCPPlugin;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EObject;

public class ModelMatchEngine extends DefaultMatchEngine {

	/**
	 * Default Constructor.
	 */
	public ModelMatchEngine() {
		super(getDefaultEObjectMatcher(), getComparisonFactory());
	}

	/**
	 * Constructor with matcher and comparison factory parameters.
	 *
	 * @param matcher
	 *            The matcher that will be in charge of pairing EObjects together for this comparison process.
	 * @param comparisonFactory
	 *            factory that will be use to instantiate Comparison as return by match() methods.
	 */
	public ModelMatchEngine(IEObjectMatcher matcher, IComparisonFactory comparisonFactory) {
		super(matcher, comparisonFactory);
	}

	protected static IEObjectMatcher getDefaultEObjectMatcher() {
		// Never use id in EObjects comparison
		return DefaultMatchEngine.createDefaultEObjectMatcher(UseIdentifiers.NEVER, EMFCompareRCPPlugin.getDefault().getWeightProviderRegistry());
	}

	protected static IComparisonFactory getComparisonFactory() {
		return new DefaultComparisonFactory(new DefaultEqualityHelperFactory());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.emf.compare.match.DefaultMatchEngine.match(org.eclipse.emf.compare.Comparison,
	 *      org.eclipse.emf.compare.scope.IComparisonScope, org.eclipse.emf.ecore.EObject,
	 *      org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.common.util.Monitor)
	 */
	@Override
	protected void match(Comparison comparison, IComparisonScope scope, EObject left, EObject right, EObject origin, Monitor monitor) {
		if (left == null || right == null) {
			throw new IllegalArgumentException();
		}

		Match rootMatch = CompareFactory.eINSTANCE.createMatch();
		rootMatch.setLeft(left);
		rootMatch.setRight(right);
		rootMatch.setOrigin(origin);
		comparison.getMatches().add(rootMatch);

		final Iterator<? extends EObject> leftEObjects = scope.getChildren(left);
		final Iterator<? extends EObject> rightEObjects = scope.getChildren(right);
		final Iterator<? extends EObject> originEObjects;
		if (origin != null) {
			originEObjects = scope.getChildren(origin);
		} else {
			originEObjects = Collections.emptyIterator();
		}

		getEObjectMatcher().createMatches(comparison, leftEObjects, rightEObjects, originEObjects, monitor);
	}
}
