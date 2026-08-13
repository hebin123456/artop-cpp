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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.metamodel.services.DefaultMetaModelServiceProvider;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.search.ui.internal.Activator;
import org.eclipse.sphinx.emf.search.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.search.ui.services.IModelSearchService;

public class ModelSearchQuery implements ISearchQuery {

	private ISearchResult searchResult;
	private QuerySpecification querySpecification;

	public ModelSearchQuery(QuerySpecification querySpec) {
		querySpecification = querySpec;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		ModelSearchResult result = (ModelSearchResult) getSearchResult();

		List<ModelSearchMatch> matches = performSearch();

		if (!matches.isEmpty()) {
			if (matches.size() == 1) {
				result.addMatch(matches.get(0));
			} else {
				result.addMatches(matches.toArray(new ModelSearchMatch[matches.size()]));
			}
		}

		String message = NLS.bind(Messages.ModelSearchQuery_status_ok_message, String.valueOf(result.getMatchCount()));
		return new Status(IStatus.OK, Activator.getDefault().getSymbolicName(), 0, message, null);
	}

	public String getResultLabel(int nMatches) {
		String searchString = getSearchString();
		// TODO (aakar) Adjust scope description
		if (nMatches == 1) {
			Object[] args = { searchString, "workspace" };
			return NLS.bind(Messages.ModelSearchQuery_singularLabel, args);
		}
		Object[] args = { searchString, new Integer(nMatches), "workspace" };
		return NLS.bind(Messages.ModelSearchQuery_pluralPattern, args);
	}

	private String getSearchString() {
		return querySpecification.getPattern();
	}

	private List<ModelSearchMatch> performSearch() {
		List<ModelSearchMatch> result = new ArrayList<ModelSearchMatch>();
		Set<IProject> projects = querySpecification.getProjects();
		for (IProject prj : projects) {
			Collection<IModelDescriptor> models = ModelDescriptorRegistry.INSTANCE.getModels(prj);
			for (IModelDescriptor modelDescriptor : models) {
				IMetaModelDescriptor metaModelDescriptor = modelDescriptor.getMetaModelDescriptor();
				IModelSearchService modelSearchService = getModelSearchService(metaModelDescriptor);
				if (modelSearchService != null) {
					result.addAll(modelSearchService.getMatches(modelDescriptor, querySpecification));
				}
			}
		}
		return result;
	}

	protected IModelSearchService getModelSearchService(IMetaModelDescriptor descriptor) {
		IModelSearchService modelSearchService = null;
		if (descriptor != null) {
			modelSearchService = new DefaultMetaModelServiceProvider().getService(descriptor, IModelSearchService.class);
		}
		// TODO Move this to MetaModelServiceRegistry
		if (modelSearchService == null) {
			modelSearchService = new DefaultMetaModelServiceProvider().getService(MetaModelDescriptorRegistry.ANY_MM, IModelSearchService.class);
		}
		return modelSearchService;
	}

	@Override
	public String getLabel() {
		return Messages.ModelSearchQuery_label;
	}

	@Override
	public boolean canRerun() {
		return true;
	}

	@Override
	public boolean canRunInBackground() {
		return true;
	}

	@Override
	public ISearchResult getSearchResult() {
		if (searchResult == null) {
			ModelSearchResult result = new ModelSearchResult(this);
			// new SearchResultUpdater(result);
			searchResult = result;
		}
		return searchResult;
	}
}
