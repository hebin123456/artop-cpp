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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.internal.messages.Messages;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.providers.Hummingbird20PlatformDiagramTypeProvider;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.graphiti.workspace.ui.wizards.pages.AbstractDiagramRootWizardPage;

public class Hummingbird20PlatformDiagramRootWizardPage extends AbstractDiagramRootWizardPage {

	public Hummingbird20PlatformDiagramRootWizardPage(String pageName) {
		super(pageName, Hummingbird20PlatformDiagramTypeProvider.DIAGRAM_TYPE_TYPE);
	}

	@Override
	protected String doGetTitle() throws MissingResourceException {
		return Hummingbird20PlatformDiagramTypeProvider.DIAGRAM_TYPE_TYPE;
	}

	@Override
	protected String doGetDescription() throws MissingResourceException {
		return NLS.bind(Messages.Hummingbird20DiagramRootWizardPage_PageDescription, Hummingbird20PlatformDiagramTypeProvider.DIAGRAM_TYPE_TYPE);
	}

	@Override
	protected IStatus isValidRootDiagram(ISelection selectedRoot) {
		if (!(selectedRoot instanceof IStructuredSelection) || ((IStructuredSelection) selectedRoot).isEmpty()) {
			return createErrorStatus(Messages.Hummingbird20DiagramRootWizardPage_NoRootSelected);
		}
		IStructuredSelection selection = (IStructuredSelection) selectedRoot;
		if (!(selection.getFirstElement() instanceof Platform)) {
			return createErrorStatus(Messages.Hummingbird20DiagramRootWizardPage_PlatformIsExpected);
		}

		return Status.OK_STATUS;
	}
}