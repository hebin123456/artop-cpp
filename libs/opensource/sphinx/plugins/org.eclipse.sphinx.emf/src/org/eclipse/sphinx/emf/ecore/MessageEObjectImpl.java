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
 *   itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.ecore;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.sphinx.emf.model.IModelDescriptor;

/**
 * Dummy {@link EObject} used as temporary replacement for real objects in a {@link IModelDescriptor model} that has not
 * yet been fully loaded. Holds a message that can be used to inform the user that the real objects are not yet
 * available.
 */
public class MessageEObjectImpl extends EObjectImpl {

	private String message;

	public MessageEObjectImpl(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return getMessage();
	}
}
