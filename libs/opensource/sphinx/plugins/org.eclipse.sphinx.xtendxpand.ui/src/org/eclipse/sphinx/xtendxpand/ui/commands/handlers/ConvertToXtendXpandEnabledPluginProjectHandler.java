/**
 * <copyright>
 * 
 * Copyright (c) 2011 itemis and others.
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
package org.eclipse.sphinx.xtendxpand.ui.commands.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.sphinx.platform.ui.util.UIUtil;
import org.eclipse.sphinx.xtend.typesystem.emf.ui.SphinxManagedEmfMetamodelContributor;
import org.eclipse.sphinx.xtendxpand.jobs.ConvertToXtendXpandEnabledPluginProjectJob;
import org.eclipse.sphinx.xtendxpand.ui.internal.messages.Messages;

/**
 * Command hander that supports conversion of {@link IProject project}s to an Xtend/Xpand-enabled plug-in project.
 */
public class ConvertToXtendXpandEnabledPluginProjectHandler extends AbstractHandler {

	/*
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	// TODO Support simultaneous conversion of multiple projects
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = UIUtil.unwrap(event, IProject.class);
		if (project != null) {
			ConvertToXtendXpandEnabledPluginProjectJob job = new ConvertToXtendXpandEnabledPluginProjectJob(
					Messages.job_convertingToXtendXpandEnabledPluginProject, project);
			job.getEnabledMetamodelContributorTypeNames().add(SphinxManagedEmfMetamodelContributor.class.getName());
			job.schedule();
		}

		return null;
	}
}
