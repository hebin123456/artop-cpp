/**
 * <copyright>
 * 
 * Copyright (c) 2012-2013 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     itemis - Initial API and implementation
 *     itemis - [418005] Add support for model files with multiple root elements
 * 
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.editors.nebula;

import org.eclipse.sphinx.emf.editors.forms.BasicTransactionalFormEditor;
import org.eclipse.sphinx.examples.hummingbird20.editors.nebula.internal.Activator;
import org.eclipse.sphinx.examples.hummingbird20.editors.nebula.pages.EditableParameterValuesOverviewPage;
import org.eclipse.sphinx.examples.hummingbird20.editors.nebula.pages.GenericParameterValuesOverviewPage;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.PartInitException;

public class Hummingbird20ComponentFormEditor extends BasicTransactionalFormEditor {

	@Override
	protected void addPages() {
		try {
			Object input = getEditorInputObject();
			if (input instanceof Component) {
				addPage(new GenericParameterValuesOverviewPage(this));
				addPage(new EditableParameterValuesOverviewPage(this));
			}
		} catch (PartInitException ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
	}
}
