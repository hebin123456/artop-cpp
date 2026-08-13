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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;

public abstract class AbstractFileLoadOperation extends AbstractLoadOperation implements IFileLoadCommonOperation {

	private Set<IFile> files = Collections.synchronizedSet(new HashSet<IFile>());

	public AbstractFileLoadOperation(String label, Collection<IFile> files, IMetaModelDescriptor mmDescriptor) {
		super(label, mmDescriptor);
		addFiles(files);
	}

	@Override
	public ISchedulingRule getRule() {
		return getSchedulingRuleFactory().createLoadSchedulingRule(getFiles());
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.operations.IFileLoadOperation#getFiles()
	 */
	@Override
	public Collection<IFile> getFiles() {
		synchronized (files) {
			return Collections.unmodifiableSet(new HashSet<IFile>(files));
		}
	}

	@Override
	public boolean covers(Collection<IProject> projects, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor) {
		return false;
	}

	@Override
	public boolean covers(Collection<IFile> files, IMetaModelDescriptor mmDescriptor) {
		int filesComparison = compare(getFiles(), files);
		int mmDescriptorsComparison = compare(getMetaModelDescriptor(), mmDescriptor);
		if (filesComparison == EQUAL) {
			if (mmDescriptorsComparison == EQUAL || mmDescriptorsComparison == GREATER_THAN) {
				return true;
			}
		} else if (filesComparison == GREATER_THAN) {
			if (mmDescriptorsComparison == EQUAL || mmDescriptorsComparison == GREATER_THAN) {
				return true;
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.operations.IFileLoadOperation#addFiles(java.util.Collection)
	 */
	@Override
	public void addFiles(Collection<IFile> files) {
		this.files.addAll(files);
	}
}
