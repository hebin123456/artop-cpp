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
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.loading;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

public class SchedulingRuleFactory {

	/**
	 * Private constructor for the singleton pattern that prevents from instantiation by clients.
	 */
	public SchedulingRuleFactory() {
		// Nothing to do
	}

	public ISchedulingRule createLoadSchedulingRule(Collection<IProject> projects, boolean includeReferencedProjects) {
		Assert.isNotNull(projects);

		/*
		 * Performance optimization: Create a scheduling rule on a per project basis only if number of projects is
		 * reasonably low.
		 */
		if (projects.size() < ExtendedPlatform.LIMIT_INDIVIDUAL_RESOURCES_SCHEDULING_RULE) {
			Set<ISchedulingRule> rules = new HashSet<ISchedulingRule>();
			for (IProject project : projects) {
				if (!includeReferencedProjects) {
					rules.add(project);
				} else {
					rules.addAll(ExtendedPlatform.getProjectGroup(project, false));
				}
			}
			return MultiRule.combine(rules.toArray(new ISchedulingRule[rules.size()]));
		} else {
			// Return workspace root as scheduling rule otherwise
			return ResourcesPlugin.getWorkspace().getRoot();
		}
	}

	public ISchedulingRule createLoadSchedulingRule(Collection<IFile> files) {
		/*
		 * Performance optimization: Create a scheduling rule on a per file basis only if number of files is reasonably
		 * low.
		 */
		if (files.size() < ExtendedPlatform.LIMIT_INDIVIDUAL_RESOURCES_SCHEDULING_RULE) {
			Assert.isNotNull(files);

			Set<ISchedulingRule> rules1 = new HashSet<ISchedulingRule>();
			for (IFile file : files) {
				ISchedulingRule rule = createLoadSchedulingRule(file);
				if (rule != null) {
					rules1.add(rule);
				}
			}
			Collection<ISchedulingRule> rules = rules1;
			return MultiRule.combine(rules.toArray(new ISchedulingRule[rules.size()]));
		} else {
			// Return workspace root as scheduling rule otherwise
			return ResourcesPlugin.getWorkspace().getRoot();
		}
	}

	public ISchedulingRule createLoadSchedulingRule(IFile file) {
		Assert.isNotNull(file);

		// Use parent resource as rule because URIConverterImpl may refresh file
		return file.getParent();
	}

	public ISchedulingRule createLoadSchedulingRule(Map<TransactionalEditingDomain, Collection<Resource>> resources) {
		Assert.isNotNull(resources);

		Collection<Resource> allResources = new HashSet<Resource>();
		for (Collection<Resource> resourcesInEditingDomain : resources.values()) {
			allResources.addAll(resourcesInEditingDomain);
		}

		/*
		 * Performance optimization: Create a scheduling rule on a per resource basis only if number of resources is
		 * reasonably low.
		 */
		if (allResources.size() < ExtendedPlatform.LIMIT_INDIVIDUAL_RESOURCES_SCHEDULING_RULE) {
			Set<ISchedulingRule> rules = new HashSet<ISchedulingRule>();
			for (Resource resource : allResources) {
				IFile file = EcorePlatformUtil.getFile(resource);
				if (file != null) {
					rules.add(createLoadSchedulingRule(file));
				}
			}
			return MultiRule.combine(rules.toArray(new ISchedulingRule[rules.size()]));
		} else {
			// Return workspace root as scheduling rule otherwise
			return ResourcesPlugin.getWorkspace().getRoot();
		}
	}
}
