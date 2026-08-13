/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.workspace.ui.decorators;

import static org.eclipse.sphinx.emf.workspace.ui.decorators.TreeItemDecorationCalculator.DecorationOverlayKind.ERROR;
import static org.eclipse.sphinx.emf.workspace.ui.decorators.TreeItemDecorationCalculator.DecorationOverlayKind.NONE;
import static org.eclipse.sphinx.emf.workspace.ui.decorators.TreeItemDecorationCalculator.DecorationOverlayKind.WARNING;
import static org.junit.Assert.assertSame;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.sphinx.emf.workspace.ui.decorators.TreeItemDecorationCalculator;
import org.eclipse.sphinx.tests.emf.workspace.ui.scenarios.AbstractHummingbird20ScenarioProblemMarkerFinder;
import org.eclipse.sphinx.tests.emf.workspace.ui.scenarios.Hummingbird20ScenarioTreeContentProvider;
import org.junit.Test;

public class TreeItemDecorationCalculatorTest {

	private static class Component12ErrorAndComponent21WarningProblemMarkerFinder extends AbstractHummingbird20ScenarioProblemMarkerFinder {

		public Component12ErrorAndComponent21WarningProblemMarkerFinder(Hummingbird20ScenarioTreeContentProvider provider) {
			super(provider);
		}

		@Override
		public int getSeverity(Object object) throws CoreException {
			Object unwrapped = AdapterFactoryEditingDomain.unwrap(object);
			if (unwrapped == hummingbird20ScenarioTreeContentProvider.component12) {
				return IMarker.SEVERITY_ERROR;
			}
			if (unwrapped == hummingbird20ScenarioTreeContentProvider.component21) {
				return IMarker.SEVERITY_WARNING;
			}
			return -1;
		}
	}

	@Test
	public void testTreeItemDecorationCalculatorWithComponent12ErrorAndComponent21Warning() {
		Hummingbird20ScenarioTreeContentProvider provider = new Hummingbird20ScenarioTreeContentProvider();
		Component12ErrorAndComponent21WarningProblemMarkerFinder finder = new Component12ErrorAndComponent21WarningProblemMarkerFinder(provider);

		TreeItemDecorationCalculator calculator = new TreeItemDecorationCalculator(finder);

		assertSame(ERROR, calculator.getDecorationOverlayKind(provider, provider.project1));

		assertSame(ERROR, calculator.getDecorationOverlayKind(provider, provider.file1));
		assertSame(ERROR, calculator.getDecorationOverlayKind(provider, provider.application1));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.description1));
		assertSame(ERROR, calculator.getDecorationOverlayKind(provider, provider.components1));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component11));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.outgoingConnections11));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component11ToComponent22Connection));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component22Ref));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.outgoingConnections22Ref));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component22ToComponent11ConnectionRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component11RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.outgoingConnections11RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component11ToComponent22ConnectionRefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component22RefRefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValues11RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue111RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue112RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValues22Ref));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue221Ref));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValues11));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue111));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue112));
		assertSame(ERROR, calculator.getDecorationOverlayKind(provider, provider.component12));

		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.file2));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.application2));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.description2));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.components2));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.component21));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component22));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.outgoingConnections22));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component22ToComponent11Connection));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component11Ref));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.outgoingConnections11Ref));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component11ToComponent22ConnectionRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component22RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.outgoingConnections22RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component22ToComponent11ConnectionRefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component11RefRefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValues22RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue221RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValues11Ref));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue111Ref));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue112Ref));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValues22));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue221));
	}

	private static class Application1ErrorAndComponent22WarningProblemMarkerFinder extends AbstractHummingbird20ScenarioProblemMarkerFinder {

		public Application1ErrorAndComponent22WarningProblemMarkerFinder(Hummingbird20ScenarioTreeContentProvider provider) {
			super(provider);
		}

		@Override
		public int getSeverity(Object object) throws CoreException {
			Object unwrapped = AdapterFactoryEditingDomain.unwrap(object);
			if (unwrapped == hummingbird20ScenarioTreeContentProvider.application2) {
				return IMarker.SEVERITY_ERROR;
			}
			if (unwrapped == hummingbird20ScenarioTreeContentProvider.component22) {
				return IMarker.SEVERITY_WARNING;
			}
			return -1;
		}
	}

	@Test
	public void testTreeItemDecorationCalculatorWithApplication1ErrorAndComponent22Warning() {
		Hummingbird20ScenarioTreeContentProvider provider = new Hummingbird20ScenarioTreeContentProvider();
		Application1ErrorAndComponent22WarningProblemMarkerFinder finder = new Application1ErrorAndComponent22WarningProblemMarkerFinder(provider);

		TreeItemDecorationCalculator calculator = new TreeItemDecorationCalculator(finder);

		assertSame(ERROR, calculator.getDecorationOverlayKind(provider, provider.project1));

		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.file1));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.application1));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.description1));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.components1));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.component11));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.outgoingConnections11));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.component11ToComponent22Connection));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.component22Ref));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.outgoingConnections22Ref));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.component22ToComponent11ConnectionRef));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.component11RefRef));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.outgoingConnections11RefRef));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.component11ToComponent22ConnectionRefRef));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.component22RefRefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValues11RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue111RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue112RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValues22Ref));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue221Ref));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValues11));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue111));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue112));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component12));

		assertSame(ERROR, calculator.getDecorationOverlayKind(provider, provider.file2));
		assertSame(ERROR, calculator.getDecorationOverlayKind(provider, provider.application2));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.description2));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.components2));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component21));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.component22));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.outgoingConnections22));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.component22ToComponent11Connection));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.component11Ref));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.outgoingConnections11Ref));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.component11ToComponent22ConnectionRef));
		assertSame(WARNING, calculator.getDecorationOverlayKind(provider, provider.component22RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.outgoingConnections22RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component22ToComponent11ConnectionRefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.component11RefRefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValues22RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue221RefRef));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValues11Ref));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue111Ref));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue112Ref));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValues22));
		assertSame(NONE, calculator.getDecorationOverlayKind(provider, provider.parameterValue221));
	}
}
