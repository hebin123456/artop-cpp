/**
 * <copyright>
 *
 * Copyright (c) 2020 IncQuery Labs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     IncQuery Labs - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.editors.viatra.query;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.sphinx.emf.editors.forms.BasicTransactionalFormEditor;
import org.eclipse.viatra.query.runtime.ui.modelconnector.EMFModelConnector;
import org.eclipse.viatra.query.runtime.ui.modelconnector.IModelConnector;

/**
 * This adapter factory makes it possible to use sphinx editors with the VIATRA tooling (e.g. Query Results view)
 */
public class SphinxEditorModelConnectorAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		if (adapterType.equals(IModelConnector.class)) {
			if (adaptableObject instanceof BasicTransactionalFormEditor) {
				final BasicTransactionalFormEditor editor = (BasicTransactionalFormEditor) adaptableObject;
				return new EMFModelConnector(editor);
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { IEditingDomainProvider.class };
	}
}
