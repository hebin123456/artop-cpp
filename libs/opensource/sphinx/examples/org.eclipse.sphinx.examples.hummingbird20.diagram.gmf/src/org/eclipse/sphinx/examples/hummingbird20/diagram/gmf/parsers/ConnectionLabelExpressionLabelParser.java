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
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.parsers;

import java.util.Collections;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.command.UnexecutableCommand;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParserEditStatus;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserEditStatus;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection;

/**
 * @generated
 */
public class ConnectionLabelExpressionLabelParser implements IParser {
	/**
	 * @generated
	 */
	public ConnectionLabelExpressionLabelParser() {
	}

	/**
	 * @generated
	 */
	@Override
	public String getEditString(IAdaptable element, int flags) {
		return getPrintString(element, flags);
	}

	/**
	 * @generated
	 */
	@Override
	public IParserEditStatus isValidEditString(IAdaptable element, String editString) {
		return ParserEditStatus.EDITABLE_STATUS;
	}

	/**
	 * @generated
	 */
	@Override
	public ICommand getParseCommand(IAdaptable element, final String newString, int flags) {
		final EObject target = (EObject) element.getAdapter(EObject.class);
		if (!validateValues(target, newString)) {
			return UnexecutableCommand.INSTANCE;
		}
		TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(target);
		if (editingDomain == null) {
			return UnexecutableCommand.INSTANCE;
		}
		IFile affectedFile = WorkspaceSynchronizer.getFile(target.eResource());
		return new AbstractTransactionalCommand(editingDomain, "Set Values", affectedFile == null ? null : Collections.singletonList(affectedFile)) { //$NON-NLS-1$ 
			@Override
			protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				return new CommandResult(updateValues(target, newString));
			}
		};
	}

	/**
	 * @generated
	 */
	@Override
	public String getPrintString(IAdaptable element, int flags) {
		return evaluatePrintExpression((EObject) element.getAdapter(EObject.class));
	}

	/**
	 * @generated
	 */
	@Override
	public boolean isAffectingEvent(Object event, int flags) {
		// XXX Any event is recognized as important, unless there's a way to extract this information from expression
		// itself.
		// TODO analyze expressions (e.g. using OCL parser) to find out structural features in use
		return true;
	}

	/**
	 * @generated
	 */
	@Override
	public IContentAssistProcessor getCompletionProcessor(IAdaptable element) {
		return null;
	}

	/**
	 * @generated
	 */
	private boolean validateValues(EObject target, String newString) {
		// TODO implement as needed
		return true;
	}

	/**
	 * @generated
	 */
	private IStatus updateValues(EObject target, String newString) throws ExecutionException {
		// TODO implement this method
		// DO NOT FORGET to remove @generated tag or mark method @generated NOT
		throw new ExecutionException("Please implement parsing and value modification");
	}

	/**
	 * @generated NOT
	 */
	private String evaluatePrintExpression(EObject self) {
		if (self instanceof Connection) {
			Connection connection = (Connection) self;
			if (connection.getName() != null && connection.getName().length() > 0) {
				return connection.getName();
			} else if (connection.getSourcePort() != null) {
				if (connection.getSourcePort().getName() != null && connection.getSourcePort().getName().length() > 0) {
					return connection.getSourcePort().getName();
				} else if (connection.getSourcePort().getRequiredInterface() != null
						&& connection.getSourcePort().getRequiredInterface().getName() != null
						&& connection.getSourcePort().getRequiredInterface().getName().length() > 0) {
					return connection.getSourcePort().getRequiredInterface().getName();
				}
			}
		}
		return "Port";
	}

}
