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
 *     itemis - [406194] Enable title and descriptions of model project and file creation wizards to be calculated automatically
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird.ide.ui.wizards;

import org.eclipse.sphinx.emf.workspace.ui.wizards.AbstractNewModelProjectWizard;
import org.eclipse.sphinx.examples.hummingbird.ide.metamodel.HummingbirdMMDescriptor;
import org.eclipse.sphinx.examples.hummingbird.ide.preferences.IHummingbirdPreferences;
import org.eclipse.sphinx.examples.hummingbird.ide.ui.internal.preferences.IHummingbirdPreferencesUI;

/**
 * Basic wizard that creates a new Hummingbird project resource in the workspace.
 */
public class NewHummingbirdProjectWizard extends AbstractNewModelProjectWizard<HummingbirdMMDescriptor> {

	public NewHummingbirdProjectWizard() {
		super(false, HummingbirdMMDescriptor.INSTANCE, IHummingbirdPreferences.METAMODEL_VERSION,
				IHummingbirdPreferencesUI.HUMMINGBIRD_METAMODEL_VERSION_PREFERENCE_PAGE_ID);
	}
}
