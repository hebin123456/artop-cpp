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
package org.eclipse.sphinx.graphiti.workspace.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;

@SuppressWarnings("restriction")
public class BasicDiagramEditorFactory implements IElementFactory {

	@Override
	public IAdaptable createElement(IMemento memento) {
		// Get diagram URI
		final String diagramUriString = memento.getString(DiagramEditorInput.KEY_URI);
		if (diagramUriString == null) {
			return null;
		}
		// Get diagram type provider id
		final String providerID = memento.getString(DiagramEditorInput.KEY_PROVIDER_ID);
		if (providerID == null) {
			return null;
		}
		URI diagramURI = URI.createURI(diagramUriString);
		URI diagramFileURI = diagramURI.trimFragment();
		IFile diagramFile = EcorePlatformUtil.getFile(diagramFileURI);
		if (diagramFile != null) {
			TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(diagramFile);
			if (editingDomain != null) {
				return new DiagramEditorInput(diagramURI, providerID);
			}
		}
		return null;
	}

	public DiagramEditorInput createEditorInput(IEditorInput otherInput) {
		if (otherInput instanceof DiagramEditorInput) {
			return (DiagramEditorInput) otherInput;
		}
		if (otherInput instanceof IFileEditorInput) {
			final IFileEditorInput fileInput = (IFileEditorInput) otherInput;
			final IFile file = fileInput.getFile();
			URI diagramFileUri = GraphitiUiInternal.getEmfService().getFileURI(file);
			if (diagramFileUri != null) {
				// the file has to contain one base node which has to be a diagram
				diagramFileUri = GraphitiUiInternal.getEmfService().mapDiagramFileUriToDiagramUri(diagramFileUri);
				// return new BasicDiagramEditorInput(diagramFileUri, domain, null, true);
				return new DiagramEditorInput(diagramFileUri, null);
			}
		}
		if (otherInput instanceof URIEditorInput) {
			final URIEditorInput uriInput = (URIEditorInput) otherInput;
			URI diagramFileUri = uriInput.getURI();
			if (diagramFileUri != null) {
				// the file has to contain one base node which has to be a DIAGRAM
				diagramFileUri = GraphitiUiInternal.getEmfService().mapDiagramFileUriToDiagramUri(diagramFileUri);
				return new DiagramEditorInput(diagramFileUri, null);
			}
		}
		return null;
	}
}