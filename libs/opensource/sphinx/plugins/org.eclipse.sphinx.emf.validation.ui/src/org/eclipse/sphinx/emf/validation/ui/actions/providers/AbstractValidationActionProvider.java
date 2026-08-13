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
package org.eclipse.sphinx.emf.validation.ui.actions.providers;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.ui.navigator.CommonActionProvider;

/**
 * An abstract base class for {@linkplain BasicActionProvider action provider}s that are dedicated to validation related
 * {@link IAction}s.
 */
public abstract class AbstractValidationActionProvider extends BasicActionProvider {

	/*
	 * @see
	 * org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider#fillSubMenu(org.eclipse.jface.action.IMenuManager
	 * )
	 */
	@Override
	public void fillSubMenu(IMenuManager subMenuManager) {
		// Retrieve selection
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

		// Calculate enablement
		boolean enabled = isEnabled(selection);

		// Performance optimization: Rather than overriding updateSelection() and individually calculating the
		// enablement status on each validation action we calculate it once here and then set it on all validation
		// actions
		populateActions(subMenuManager, selection, enabled);
	}

	/**
	 * Populates specified {@linkplain IMenuManager menu} with the {@linkplain org.eclipse.jface.action.IAction action}s
	 * provided by this {@link AbstractValidationActionProvider action provider} implementation. Initializes
	 * {@linkplain org.eclipse.jface.action.IAction action}s with given {@linkplain IStructuredSelection selection} and
	 * <code>enabled</code> status.
	 * 
	 * @param menu
	 *            The {@linkplain IMenuManager menu} actions should be added to.
	 * @param selection
	 *            The {@linkplain IStructuredSelection selection} actions must act upon.
	 * @param enabled
	 *            Enablement status indicating if actions should be enabled or not.
	 */
	protected abstract void populateActions(IMenuManager menu, IStructuredSelection selection, boolean enabled);

	/**
	 * Computes the enable state of this {@linkplain CommonActionProvider action provider} according to the given
	 * {@link IStructuredSelection selection}.
	 * <p>
	 * <table>
	 * <tr valign=top>
	 * <td><b>Note</b>&nbsp;&nbsp;</td>
	 * <td>This method is not supposed to be overridden by client applications for providing a custom enablement
	 * computation; in that purpose overriding {@linkplain #isEnabled(Object)} is preferable.</td>
	 * </tr>
	 * </table>
	 * 
	 * @param selection
	 *            The {@linkplain IStructuredSelection selection} for which enablement must be computed.
	 * @return <ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> if this {@linkplain action provider} is enabled for the specified
	 *         {@link IStructuredSelection selection};</li> <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	protected final boolean isEnabled(IStructuredSelection selection) {
		for (Object selectedObject : selection.toList()) {
			boolean enabled = isEnabled(selectedObject);
			if (enabled) {
				// When true, return and stop iteration on multi-selection
				return enabled;
			} else {
				// May be enabled for other objects in the multi-selection
				continue;
			}
		}
		return false;
	}

	/**
	 * Computes the enable state of this {@linkplain CommonActionProvider action provider} according to the given
	 * {@link Object selectedObject}.
	 * 
	 * @param selectedObject
	 *            The object currently selected; object from which enable state for this
	 *            {@linkplain CommonActionProvider action provider} must be computed.
	 * @return <code>true</code>if this {@linkplain action provider} is enabled for the given {@link Object object},
	 *         <code>false</code> otherwise.
	 */
	protected boolean isEnabled(Object selectedObject) {
		if (selectedObject instanceof IContainer) {
			IContainer container = (IContainer) selectedObject;
			return ModelDescriptorRegistry.INSTANCE.getModels(container).size() > 0;
		} else if (selectedObject instanceof IModelDescriptor) {
			return true;
		} else {
			IFile file = EcorePlatformUtil.getFile(selectedObject);
			if (file != null) {
				return ModelDescriptorRegistry.INSTANCE.getModel(file) != null;
			}
		}
		return false;
	}
}
