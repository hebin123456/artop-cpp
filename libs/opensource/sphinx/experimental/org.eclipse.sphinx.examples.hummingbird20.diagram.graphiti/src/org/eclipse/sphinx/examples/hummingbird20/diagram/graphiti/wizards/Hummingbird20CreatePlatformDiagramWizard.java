/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [392424] Migrate Sphinx integration of Graphiti to Graphiti 0.9.x
 * 
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.wizards;

import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.wizards.pages.Hummingbird20PlatformDiagramContainerWizardPage;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.wizards.pages.Hummingbird20PlatformDiagramRootWizardPage;
import org.eclipse.sphinx.graphiti.workspace.metamodel.GraphitiMMDescriptor;
import org.eclipse.sphinx.graphiti.workspace.ui.wizards.AbstractCreateDiagramWizard;
import org.eclipse.sphinx.graphiti.workspace.ui.wizards.pages.AbstractDiagramContainerWizardPage;
import org.eclipse.sphinx.graphiti.workspace.ui.wizards.pages.AbstractDiagramRootWizardPage;

public class Hummingbird20CreatePlatformDiagramWizard extends AbstractCreateDiagramWizard {

	@Override
	protected AbstractDiagramContainerWizardPage createDiagramContainerWizardPage() {
		return new Hummingbird20PlatformDiagramContainerWizardPage(Hummingbird20PlatformDiagramContainerWizardPage.class.getSimpleName(), selection,
				GraphitiMMDescriptor.GRAPHITI_DIAGRAM_DEFAULT_FILE_EXTENSION);
	}

	@Override
	protected AbstractDiagramRootWizardPage createDiagramRootWizardPage() {
		return new Hummingbird20PlatformDiagramRootWizardPage(Hummingbird20PlatformDiagramRootWizardPage.class.getSimpleName());
	}
}