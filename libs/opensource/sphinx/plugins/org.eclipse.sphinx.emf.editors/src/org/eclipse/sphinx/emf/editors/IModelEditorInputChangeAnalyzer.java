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
package org.eclipse.sphinx.emf.editors;

import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.IEditorInput;

public interface IModelEditorInputChangeAnalyzer {

	boolean containsEditorInputObject(IEditorInput editorInput, Set<EObject> objects);

	boolean containsEditorInputResourceURI(IEditorInput editorInput, Set<URI> resourceURIs);
}
