/**
 * <copyright>
 *
 * Copyright (c) 2008-2016 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [408533] Inner commands of Copy/Cut/Delete/Paste commands not created correctly when using ExtendedXxxAction with custom adapter factory
 *     itemis - [481581] Improve refresh behavior of BasicModelContentProvider to avoid performance problems due to needlessly repeated tree state restorations
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.ui.actions;

import java.util.Collection;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.ui.action.DeleteAction;
import org.eclipse.emf.transaction.ResourceSetListener;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.edit.LocalProxyChangeListener;
import org.eclipse.sphinx.platform.util.ReflectUtil;

public class ExtendedDeleteAction extends DeleteAction {

	protected AdapterFactory customAdapterFactory;

	public ExtendedDeleteAction(EditingDomain domain, boolean removeAllReferences, AdapterFactory customAdapterFactory) {
		super(domain, removeAllReferences);
		this.customAdapterFactory = customAdapterFactory;
	}

	public ExtendedDeleteAction(boolean removeAllReferences, AdapterFactory customAdapterFactory) {
		this(null, removeAllReferences, customAdapterFactory);
	}

	/*
	 * @see org.eclipse.emf.edit.ui.action.CommandActionHandler#updateSelection(org.eclipse.jface.viewers.
	 * IStructuredSelection )
	 */
	@Override
	public boolean updateSelection(IStructuredSelection selection) {
		if (domain != null) {
			AdapterFactory oldAdapterFactory = null;
			if (customAdapterFactory != null) {
				oldAdapterFactory = ((AdapterFactoryEditingDomain) domain).getAdapterFactory();
				((AdapterFactoryEditingDomain) domain).setAdapterFactory(customAdapterFactory);
			}
			boolean result = super.updateSelection(selection);
			if (oldAdapterFactory != null) {
				((AdapterFactoryEditingDomain) domain).setAdapterFactory(oldAdapterFactory);
			}
			return result;
		}
		return false;
	}

	@Override
	public Command createCommand(Collection<?> selection) {
		// Don't clean up all references to deleted model object if editing domain supports converting it into a
		// proxy
		if (supportsProxyficationOfRemovedElements(domain)) {
			removeAllReferences = false;
		}
		return super.createCommand(selection);
	}

	/**
	 * Tests if given {@link EditingDomain editing domain} supports automatic conversion of removed model objects into
	 * proxies.
	 *
	 * @param domain
	 *            The {@link EditingDomain editing domain} to be investigated.
	 * @return <code>true</code> if given {@link EditingDomain editing domain} supports automatic conversion of removed
	 *         model objects into proxies, or <code>false</code> otherwise.
	 * @see LocalProxyChangeListener
	 */
	protected boolean supportsProxyficationOfRemovedElements(EditingDomain domain) {
		if (domain instanceof TransactionalEditingDomain) {
			try {
				ResourceSetListener[] listeners = (ResourceSetListener[]) ReflectUtil.invokeInvisibleMethod(domain, "getPostcommitListeners");//$NON-NLS-1$
				for (ResourceSetListener listner : listeners) {
					if (listner instanceof LocalProxyChangeListener) {
						return true;
					}
				}
			} catch (Exception ex) {
				// Ignore exception
			}
		}
		return false;
	}
}
