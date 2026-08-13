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
package org.eclipse.sphinx.emf.search.ui.services;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.services.IMetaModelService;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.search.ui.ModelSearchMatch;
import org.eclipse.sphinx.emf.search.ui.QuerySpecification;

public interface IModelSearchService extends IMetaModelService {

	List<ModelSearchMatch> getMatches(IModelDescriptor modelDescriptor, QuerySpecification spec);

	List<ModelSearchMatch> getMatches(Collection<Resource> resources, QuerySpecification spec);
}
