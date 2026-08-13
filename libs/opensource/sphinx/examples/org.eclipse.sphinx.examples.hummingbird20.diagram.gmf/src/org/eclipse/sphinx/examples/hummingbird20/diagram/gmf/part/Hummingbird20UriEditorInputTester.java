/**
 * <copyright>
 * 
 * Copyright (c) 2013 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.emf.common.ui.URIEditorInput;

/**
 * @generated
 */
public class Hummingbird20UriEditorInputTester extends PropertyTester {

	/**
	 * @generated
	 */
	@Override
	public boolean test(Object receiver, String method, Object[] args, Object expectedValue) {
		if (false == receiver instanceof URIEditorInput) {
			return false;
		}
		URIEditorInput editorInput = (URIEditorInput) receiver;
		return "instancemodel_diagram".equals(editorInput.getURI().fileExtension()); //$NON-NLS-1$
	}

}
