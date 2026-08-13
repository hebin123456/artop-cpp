/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [358082] Precedence of Xtend MetaModels gets lost in Xtend/Xpand runtime enhancements implemented in Sphinx
 *     itemis - Revised implementation (redesigned overriding points and getters/setters, improved of naming, fixed progress monitor issues)
 * 
 * </copyright>
 */
package org.eclipse.sphinx.xtendxpand.ui.jobs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;

/**
 * A {@link WorkspaceJob workspace job} that supports conversion of {@link IProject project}s to an Xtend/Xpand-enabled
 * plug-in project.
 * 
 * @deprecated Use {@link org.eclipse.sphinx.xtendxpand.jobs.ConvertToXtendXpandEnabledPluginProjectJob} instead.
 */
@Deprecated
public class ConvertToXtendXpandEnabledProjectJob extends org.eclipse.sphinx.xtendxpand.jobs.ConvertToXtendXpandEnabledPluginProjectJob {

	public ConvertToXtendXpandEnabledProjectJob(String name, IProject project) {
		super(name, project);
	}
}
