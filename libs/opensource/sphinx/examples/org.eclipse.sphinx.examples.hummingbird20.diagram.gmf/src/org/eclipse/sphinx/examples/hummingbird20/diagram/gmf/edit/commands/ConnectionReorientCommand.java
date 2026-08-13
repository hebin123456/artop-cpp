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
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.commands;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.emf.type.core.commands.EditElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientRequest;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.policies.Hummingbird20BaseItemSemanticEditPolicy;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection;

/**
 * @generated
 */
public class ConnectionReorientCommand extends EditElementCommand {

	/**
	 * @generated
	 */
	private final int reorientDirection;

	/**
	 * @generated
	 */
	private final EObject oldEnd;

	/**
	 * @generated
	 */
	private final EObject newEnd;

	/**
	 * @generated
	 */
	public ConnectionReorientCommand(ReorientRelationshipRequest request) {
		super(request.getLabel(), request.getRelationship(), request);
		reorientDirection = request.getDirection();
		oldEnd = request.getOldRelationshipEnd();
		newEnd = request.getNewRelationshipEnd();
	}

	/**
	 * @generated
	 */
	@Override
	public boolean canExecute() {
		if (false == getElementToEdit() instanceof Connection) {
			return false;
		}
		if (reorientDirection == ReorientRequest.REORIENT_SOURCE) {
			return canReorientSource();
		}
		if (reorientDirection == ReorientRequest.REORIENT_TARGET) {
			return canReorientTarget();
		}
		return false;
	}

	/**
	 * @generated
	 */
	protected boolean canReorientSource() {
		if (!(oldEnd instanceof Component && newEnd instanceof Component)) {
			return false;
		}
		Component target = getLink().getTargetComponent();
		if (!(getLink().eContainer() instanceof Component)) {
			return false;
		}
		Component container = (Component) getLink().eContainer();
		return Hummingbird20BaseItemSemanticEditPolicy.getLinkConstraints().canExistConnection_4001(container, getLink(), getNewSource(), target);
	}

	/**
	 * @generated
	 */
	protected boolean canReorientTarget() {
		if (!(oldEnd instanceof Component && newEnd instanceof Component)) {
			return false;
		}
		Component source = getLink().getSourceComponent();
		if (!(getLink().eContainer() instanceof Component)) {
			return false;
		}
		Component container = (Component) getLink().eContainer();
		return Hummingbird20BaseItemSemanticEditPolicy.getLinkConstraints().canExistConnection_4001(container, getLink(), source, getNewTarget());
	}

	/**
	 * @generated
	 */
	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		if (!canExecute()) {
			throw new ExecutionException("Invalid arguments in reorient link command"); //$NON-NLS-1$
		}
		if (reorientDirection == ReorientRequest.REORIENT_SOURCE) {
			return reorientSource();
		}
		if (reorientDirection == ReorientRequest.REORIENT_TARGET) {
			return reorientTarget();
		}
		throw new IllegalStateException();
	}

	/**
	 * @generated
	 */
	protected CommandResult reorientSource() throws ExecutionException {
		getLink().setSourceComponent(getNewSource());
		return CommandResult.newOKCommandResult(getLink());
	}

	/**
	 * @generated
	 */
	protected CommandResult reorientTarget() throws ExecutionException {
		getLink().setTargetComponent(getNewTarget());
		return CommandResult.newOKCommandResult(getLink());
	}

	/**
	 * @generated
	 */
	protected Connection getLink() {
		return (Connection) getElementToEdit();
	}

	/**
	 * @generated
	 */
	protected Component getOldSource() {
		return (Component) oldEnd;
	}

	/**
	 * @generated
	 */
	protected Component getNewSource() {
		return (Component) newEnd;
	}

	/**
	 * @generated
	 */
	protected Component getOldTarget() {
		return (Component) oldEnd;
	}

	/**
	 * @generated
	 */
	protected Component getNewTarget() {
		return (Component) newEnd;
	}
}
