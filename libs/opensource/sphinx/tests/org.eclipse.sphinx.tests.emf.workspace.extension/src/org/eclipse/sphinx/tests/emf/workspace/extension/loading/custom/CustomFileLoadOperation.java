/**
 * <copyright>
 *
 * Copyright (c) 2021 Siemens and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     Siemens - [574930] Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.workspace.extension.loading.custom;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultFileLoadOperation;

public class CustomFileLoadOperation extends DefaultFileLoadOperation {

	public CustomFileLoadOperation(Collection<IFile> files, IMetaModelDescriptor mmDescriptor) {
		super(files, mmDescriptor);
	}
}
