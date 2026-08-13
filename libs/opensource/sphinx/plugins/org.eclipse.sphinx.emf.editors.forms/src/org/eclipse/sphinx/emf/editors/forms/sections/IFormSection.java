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
package org.eclipse.sphinx.emf.editors.forms.sections;

import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sphinx.emf.ui.forms.messages.IFormMessage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;

/**
 * 
 */
public interface IFormSection {

	void setTitle(String title);

	void setDescription(String description);

	void setSectionInput(Object sectionInput);

	void createContent(IManagedForm managedForm, Composite parent);

	void refreshSection();

	void refreshMessages(IMessageManager messageManager, Map<EStructuralFeature, Set<IFormMessage>> messages);
}