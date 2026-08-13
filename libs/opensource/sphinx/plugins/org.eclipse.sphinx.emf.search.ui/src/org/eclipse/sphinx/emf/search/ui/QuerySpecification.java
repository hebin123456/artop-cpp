/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.emf.search.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

public class QuerySpecification {

	private String pattern;
	private boolean isCaseSensitive;
	private Set<IProject> projects;

	public QuerySpecification(String pattern, boolean isCaseSensitive, Set<IProject> projects) {
		this.pattern = pattern;
		this.isCaseSensitive = isCaseSensitive;
		this.projects = projects;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}

	public Set<IProject> getProjects() {
		if (projects.isEmpty()) {
			// TODO (aakar) This is temporary
			projects = new HashSet<IProject>();
			IProject[] projects2 = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (IProject prj : projects2) {
				projects.add(prj);
			}
		}
		return projects;
	}
}
