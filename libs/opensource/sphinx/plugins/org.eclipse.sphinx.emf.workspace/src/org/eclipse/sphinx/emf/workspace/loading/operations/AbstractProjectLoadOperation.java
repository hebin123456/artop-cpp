/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     Siemens - [574930] Model load manager extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.loading.operations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

public abstract class AbstractProjectLoadOperation extends AbstractLoadOperation implements IProjectLoadCommonOperation {

	private Collection<IProject> projects;
	private boolean includeReferencedProjects;

	public AbstractProjectLoadOperation(String label, Collection<IProject> projects, boolean includeReferencedProjects,
			IMetaModelDescriptor mmDescriptor) {
		super(label, mmDescriptor);
		this.includeReferencedProjects = includeReferencedProjects;
		addProjects(projects);
	}

	@Override
	public ISchedulingRule getRule() {
		return getSchedulingRuleFactory().createLoadSchedulingRule(getProjects(), isIncludeReferencedProjects());
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.operations.IProjectLoadOperation#getProjects()
	 */
	@Override
	public Collection<IProject> getProjects() {
		return projects;
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.operations.IProjectLoadOperation#isIncludeReferencedProjects()
	 */
	@Override
	public boolean isIncludeReferencedProjects() {
		return includeReferencedProjects;
	}

	@Override
	public boolean covers(Collection<IProject> projects, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor) {
		Set<IProject> projectGroup = new HashSet<IProject>(projects);
		if (includeReferencedProjects) {
			for (IProject p : projects) {
				projectGroup.addAll(ExtendedPlatform.getProjectGroup(p, false));
			}
		}
		int projectsComparison = compare(getProjects(), projectGroup);
		if (projectsComparison == EQUAL || projectsComparison == GREATER_THAN) {
			int mmDescriptorsComparison = compare(getMetaModelDescriptor(), mmDescriptor);
			if (mmDescriptorsComparison == EQUAL || mmDescriptorsComparison == GREATER_THAN) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean covers(Collection<IFile> files, IMetaModelDescriptor mmDescriptor) {
		Set<IProject> projects = new HashSet<IProject>();
		for (IFile file : files) {
			projects.add(file.getProject());
		}
		return covers(projects, false, null);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.operations.IProjectLoadOperation#addProjects(java.util.Collection)
	 */
	@Override
	public void addProjects(Collection<IProject> projects) {
		if (getProjects() == null) {
			this.projects = new HashSet<IProject>();
		}
		getProjects().addAll(projects);
		// Compute project group if referenced projects must be considered
		if (includeReferencedProjects) {
			for (IProject p : projects) {
				getProjects().addAll(getProjectGroup(p, false));
			}
		}
	}
}
