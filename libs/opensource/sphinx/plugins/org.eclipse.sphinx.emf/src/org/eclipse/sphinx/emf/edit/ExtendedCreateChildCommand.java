/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.edit;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.command.UnexecutableCommand;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;

public class ExtendedCreateChildCommand extends CreateChildCommand {

	public ExtendedCreateChildCommand(EditingDomain domain, EObject owner, EStructuralFeature feature, Object child, int index,
			Collection<?> selection, CreateChildCommand.Helper helper) {
		super(domain, owner, feature, child, index, selection, helper);
	}

	/*
	 * Overridden for returning qualified create child text in case that this is supported by underlying helper
	 * implementation. Qualified create child texts are expected to consist of two segments separated by a vertical bar.
	 * The leading segment typically corresponds to the name of feature on the owner object that holds the child object
	 * and the trailing segment to the type of the child object to be created. They are leveraged by
	 * org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider#extractSubmenuActions(Collection<IAction>,
	 * ISelection) and
	 * org.eclipse.sphinx.emf.editors.forms.BasicTransactionalEditorActionBarContributor#extractSubmenuActions
	 * (Collection<IAction>, ISelection) so as to group the corresponding create child actions in submenus according to
	 * the qualified create child texts' leading segments.
	 * @see org.eclipse.emf.edit.command.CreateChildCommand#getText()
	 */
	@Override
	public String getText() {
		if (helper instanceof ExtendedItemProviderAdapter) {
			return ((ExtendedItemProviderAdapter) helper).getCreateChildText(owner, feature, child, selection, true);
		}
		return super.getText();
	}

	@Override
	protected Command createCommand() {
		if (owner == null || feature == null || child == null) {
			return UnexecutableCommand.INSTANCE;
		}

		CompoundCommand command = new CompoundCommand(0);

		if (feature.isMany()) {
			command.append(AddCommand.create(domain, owner, feature, child, index));
			appendAddToContainerCommand(command);
			return command;
		} else if (owner.eGet(feature) == null) {
			command.append(SetCommand.create(domain, owner, feature, child));
			appendAddToContainerCommand(command);
			return command;
		} else {
			return UnexecutableCommand.INSTANCE;
		}
	}

	@SuppressWarnings("unused")
	private void appendAddToContainerCommand(CompoundCommand command) {
		if (child != null && child instanceof EObject) {
			if (!((EReference) feature).isContainment()) {
				for (EObject eContainer = owner.eContainer(); eContainer != null; eContainer = eContainer.eContainer()) {
					EObject container = null;
					EStructuralFeature containerReference = null;
					for (EReference eContainerReference : eContainer.eClass().getEAllContainments()) {
						if (eContainerReference.getEType().isInstance(child)) {
							if (containerReference == null || isReferenceTypeSubTypeOf(eContainerReference.getEType(), containerReference.getEType())) {
								container = eContainer;
								containerReference = eContainerReference;
							}
						}
					}
					if (container != null) {
						if (containerReference.isMany()) {
							command.append(AddCommand.create(domain, container, containerReference, child));
						} else {
							command.append(SetCommand.create(domain, container, containerReference, child));
						}
					}
					return;
				}
			}
		}
	}

	private boolean isReferenceTypeSubTypeOf(EClassifier referenceType, EClassifier type) {
		Assert.isLegal(referenceType instanceof EClass);
		Assert.isLegal(type instanceof EClass);

		if (referenceType == type) {
			return false;
		}

		if (type == EcorePackage.eINSTANCE.getEObject()) {
			return true;
		}

		for (EClass superType : ((EClass) referenceType).getESuperTypes()) {
			if (superType == type) {
				return true;
			}

			if (isReferenceTypeSubTypeOf(superType, type)) {
				return true;
			}
		}

		return false;
	}
}
