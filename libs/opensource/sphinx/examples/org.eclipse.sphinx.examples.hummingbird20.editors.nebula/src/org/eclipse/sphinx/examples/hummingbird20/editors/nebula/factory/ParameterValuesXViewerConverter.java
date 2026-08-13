/**
 * <copyright>
 * 
 * Copyright (c) 2012 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.editors.nebula.factory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.nebula.widgets.xviewer.edit.CellEditDescriptor;
import org.eclipse.nebula.widgets.xviewer.edit.XViewerConverter;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.examples.hummingbird20.editors.nebula.internal.Activator;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class ParameterValuesXViewerConverter implements XViewerConverter {

	@Override
	public void setInput(Control c, CellEditDescriptor ced, Object selObject) {
		if (c instanceof Text) {
			Text textField = (Text) c;
			if (selObject instanceof ParameterValue) {
				ParameterValue parameterValue = (ParameterValue) selObject;
				if (ced.getInputField().equals(ParameterValuesXViewerFactory.PARAMETER_NAME_COLUMN_NAME)) {
					textField.setText(parameterValue.getName() != null ? parameterValue.getName() : ""); //$NON-NLS-1$
				} else if (ced.getInputField().equals(ParameterValuesXViewerFactory.PARAMETER_VALUE_COLUMN_NAME)) {
					textField.setText(parameterValue.getValue() != null ? parameterValue.getValue() : ""); //$NON-NLS-1$
				}
			}
		}
	}

	@Override
	public Object getInput(Control c, CellEditDescriptor ced, Object selObject) {
		if (c instanceof Text) {
			Text textField = (Text) c;
			if (selObject instanceof ParameterValue) {
				ParameterValue parameterValue = (ParameterValue) selObject;
				if (ced.getInputField().equals(ParameterValuesXViewerFactory.PARAMETER_NAME_COLUMN_NAME)) {
					if (isPropertyValueChanged(textField.getText(), parameterValue.getName())) {
						setParameterValueName(parameterValue, textField.getText());
					}
				} else if (ced.getInputField().equals(ParameterValuesXViewerFactory.PARAMETER_VALUE_COLUMN_NAME)) {
					if (isPropertyValueChanged(textField.getText(), parameterValue.getValue())) {
						setParameterValueValue(parameterValue, textField.getText());
					}
				}
			}
			return selObject;
		}
		return null;
	}

	protected boolean isPropertyValueChanged(Object newValue, Object oldValue) {
		return oldValue == null && newValue != null || oldValue != null && !oldValue.equals(newValue);
	}

	private void setParameterValueName(final ParameterValue parameterValue, final String name) {
		Assert.isNotNull(parameterValue);

		try {
			TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(parameterValue);
			if (editingDomain != null) {
				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						parameterValue.setName(name);
					}
				};
				WorkspaceTransactionUtil.executeInWriteTransaction(editingDomain, runnable, "setName"); //$NON-NLS-1$
			}
		} catch (ExecutionException ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
	}

	private void setParameterValueValue(final ParameterValue parameterValue, final String value) {
		Assert.isNotNull(parameterValue);

		try {
			TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(parameterValue);
			if (editingDomain != null) {
				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						parameterValue.setValue(value);
					}
				};
				WorkspaceTransactionUtil.executeInWriteTransaction(editingDomain, runnable, "setValue"); //$NON-NLS-1$
			}
		} catch (ExecutionException ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
	}

	@Override
	public boolean isValid(CellEditDescriptor ced, Object selObject) {
		return true;
	}
}
