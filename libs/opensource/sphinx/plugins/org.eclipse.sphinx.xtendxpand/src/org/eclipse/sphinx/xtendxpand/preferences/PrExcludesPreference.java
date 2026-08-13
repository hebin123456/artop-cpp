/**
 * <copyright>
 *
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.xtendxpand.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.sphinx.platform.preferences.AbstractProjectWorkspacePreference;
import org.eclipse.sphinx.xtendxpand.internal.preferences.OutletsPreferenceInitializer;
import org.eclipse.sphinx.xtendxpand.util.XtendXpandUtil;

public class PrExcludesPreference extends AbstractProjectWorkspacePreference<String> {

	/**
	 * Default instance of {@link PrExcludesPreference}.
	 */
	public static final PrExcludesPreference INSTANCE = new PrExcludesPreference(XtendXpandUtil.XTEND_XPAND_NATURE_ID,
			OutletsPreferenceInitializer.QUALIFIER, OutletsPreferenceInitializer.PREF_PR_EXCLUDES,
			OutletsPreferenceInitializer.PREF_PR_EXCLUDES_DEFAULT);

	public PrExcludesPreference(String requiredProjectNatureId, String qualifier, String key, String defaultValueAsString) {
		super(requiredProjectNatureId, qualifier, key, defaultValueAsString);
	}

	@Override
	protected String toObject(IProject project, String valueAsString) {
		return valueAsString == null ? OutletsPreferenceInitializer.PREF_PR_EXCLUDES_DEFAULT : valueAsString;
	}

	@Override
	protected String toString(IProject project, String valueAsObject) {
		return valueAsObject;
	}
}
