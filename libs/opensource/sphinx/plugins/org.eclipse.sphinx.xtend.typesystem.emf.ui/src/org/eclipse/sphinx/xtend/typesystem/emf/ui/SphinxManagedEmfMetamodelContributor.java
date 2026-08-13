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
package org.eclipse.sphinx.xtend.typesystem.emf.ui;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.sphinx.xtend.typesystem.emf.SphinxManagedEmfMetaModel;
import org.eclipse.xtend.expression.TypeSystem;
import org.eclipse.xtend.shared.ui.MetamodelContributor;
import org.eclipse.xtend.typesystem.MetaModel;

public class SphinxManagedEmfMetamodelContributor implements MetamodelContributor {

	@Override
	public MetaModel[] getMetamodels(IJavaProject project, TypeSystem ctx) {
		return new MetaModel[] { new SphinxManagedEmfMetaModel(project.getProject()) };
	}
}
