/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
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
package org.eclipse.sphinx.emf.mwe.dynamic.ui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.mwe.dynamic.ui.dialogs.WorkflowTypeSelectionDialog;
import org.eclipse.sphinx.emf.mwe.dynamic.util.XtendUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

public class WorkflowRunnerUIHelper {

	public boolean isWorkflow(IStructuredSelection structuredSelection) {
		return getWorkflow(structuredSelection) != null;
	}

	public Object getWorkflow(IStructuredSelection structuredSelection) {
		if (structuredSelection == null || structuredSelection.isEmpty() && structuredSelection.size() > 1) {
			return null;
		}
		Object selected = structuredSelection.getFirstElement();

		// Java class
		if (selected instanceof IType) {
			return selected;
		}

		// Java file
		if (selected instanceof ICompilationUnit) {
			return selected;
		}

		// Xtend file
		if (selected instanceof IFile && XtendUtil.isXtendFile((IFile) selected)) {
			return selected;
		}

		return null;
	}

	public IType promptForWorkflowType() {
		SelectionDialog dialog = new WorkflowTypeSelectionDialog(ExtendedPlatformUI.getActiveShell());

		int returnCode = dialog.open();
		if (returnCode == IDialogConstants.OK_ID) {
			Object[] workflowTypes = dialog.getResult();
			if (workflowTypes.length > 0) {
				return (IType) workflowTypes[0];
			}
		}

		return null;
	}

	public boolean isModel(IStructuredSelection structuredSelection) {
		for (URI uri : getModelURIs(structuredSelection)) {
			if (EcoreResourceUtil.isEMFModelURI(uri)) {
				return true;
			}
		}

		return !getModelObjects(structuredSelection).isEmpty();
	}

	public List<URI> getModelURIs(IStructuredSelection structuredSelection) {
		if (structuredSelection == null) {
			return Collections.emptyList();
		}

		List<URI> uris = new ArrayList<URI>();
		for (Object selected : structuredSelection.toList()) {
			uris.addAll(getModelURIs(selected));
		}

		return uris;
	}

	protected List<URI> getModelURIs(Object object) {
		// Wrapped model object or model object
		object = AdapterFactoryEditingDomain.unwrap(object);
		if (object instanceof EObject) {
			return Collections.singletonList(EcoreResourceUtil.getURI((EObject) object, true));
		}

		// Group of model objects
		if (object instanceof TransientItemProvider) {
			TransientItemProvider provider = (TransientItemProvider) object;
			List<URI> uris = new ArrayList<URI>();
			for (Object child : provider.getChildren(object)) {
				uris.addAll(getModelURIs(child));
			}
			return uris;
		}

		// Model file or model resource
		URI uri = null;
		if (object instanceof IFile) {
			uri = EcorePlatformUtil.createURI(((IFile) object).getFullPath());
		}
		if (object instanceof Resource) {
			uri = ((Resource) object).getURI();
		}
		if (uri != null) {
			return Collections.singletonList(uri);
		}

		return Collections.emptyList();
	}

	protected List<EObject> getModelObjects(IStructuredSelection structuredSelection) {
		if (structuredSelection == null) {
			return Collections.emptyList();
		}

		List<EObject> modelObjects = new ArrayList<EObject>();
		for (Object selected : structuredSelection.toList()) {
			modelObjects.addAll(getModelObjects(selected));
		}

		return modelObjects;
	}

	protected List<EObject> getModelObjects(Object object) {
		// Wrapped model object or model object
		object = AdapterFactoryEditingDomain.unwrap(object);
		if (object instanceof EObject) {
			return Collections.singletonList((EObject) object);
		}

		// Group of model objects
		if (object instanceof TransientItemProvider) {
			TransientItemProvider provider = (TransientItemProvider) object;
			List<EObject> modelObjects = new ArrayList<EObject>();
			for (Object child : provider.getChildren(object)) {
				modelObjects.addAll(getModelObjects(child));
			}
			return modelObjects;
		}

		return Collections.emptyList();
	}
}
