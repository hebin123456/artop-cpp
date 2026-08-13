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
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.helpers;

import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.edithelper.AbstractEditHelper;
import org.eclipse.gmf.runtime.emf.type.core.edithelper.IEditHelperAdvice;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyReferenceRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;

/**
 * @generated
 */
public class Hummingbird20BaseEditHelper extends AbstractEditHelper {

	/**
	 * @generated
	 */
	public static final String EDIT_POLICY_COMMAND = "edit policy command"; //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final String CONTEXT_ELEMENT_TYPE = "context element type"; //$NON-NLS-1$

	/**
	 * @generated
	 */
	@Override
	protected IEditHelperAdvice[] getEditHelperAdvice(IEditCommandRequest req) {
		if (req.getParameter(CONTEXT_ELEMENT_TYPE) instanceof IElementType) {
			return ElementTypeRegistry.getInstance().getEditHelperAdvice((IElementType) req.getParameter(CONTEXT_ELEMENT_TYPE));
		}
		return super.getEditHelperAdvice(req);
	}

	/**
	 * @generated
	 */
	@Override
	protected ICommand getInsteadCommand(IEditCommandRequest req) {
		ICommand epCommand = (ICommand) req.getParameter(EDIT_POLICY_COMMAND);
		req.setParameter(EDIT_POLICY_COMMAND, null);
		ICommand ehCommand = super.getInsteadCommand(req);
		if (epCommand == null) {
			return ehCommand;
		}
		if (ehCommand == null) {
			return epCommand;
		}
		CompositeCommand command = new CompositeCommand(null);
		command.add(epCommand);
		command.add(ehCommand);
		return command;
	}

	/**
	 * @generated
	 */
	@Override
	protected ICommand getCreateCommand(CreateElementRequest req) {
		return null;
	}

	/**
	 * @generated
	 */
	@Override
	protected ICommand getCreateRelationshipCommand(CreateRelationshipRequest req) {
		return null;
	}

	/**
	 * @generated
	 */
	@Override
	protected ICommand getDestroyElementCommand(DestroyElementRequest req) {
		return null;
	}

	/**
	 * @generated
	 */
	@Override
	protected ICommand getDestroyReferenceCommand(DestroyReferenceRequest req) {
		return null;
	}
}
