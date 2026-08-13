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
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.wizards.pages;

import java.util.MissingResourceException;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.internal.messages.Messages;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.providers.Hummingbird20PlatformDiagramTypeProvider;
import org.eclipse.sphinx.graphiti.workspace.ui.wizards.pages.AbstractDiagramContainerWizardPage;

public class Hummingbird20PlatformDiagramContainerWizardPage extends AbstractDiagramContainerWizardPage {

	public Hummingbird20PlatformDiagramContainerWizardPage(String pageName, IStructuredSelection selection, String extension) {
		super(pageName, selection, extension);
	}

	@Override
	protected String doGetTitle() throws MissingResourceException {
		return Hummingbird20PlatformDiagramTypeProvider.DIAGRAM_TYPE_TYPE;
	}

	@Override
	protected String doGetDescription() throws MissingResourceException {
		return NLS.bind(Messages.Hummingbird20DiagramContainerWizardPage_PageDescription, Hummingbird20PlatformDiagramTypeProvider.DIAGRAM_TYPE_TYPE);
	}
}
