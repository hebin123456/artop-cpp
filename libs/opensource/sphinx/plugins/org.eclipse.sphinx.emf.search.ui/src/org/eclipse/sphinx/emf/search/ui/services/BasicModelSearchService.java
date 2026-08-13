/**
 * <copyright>
 *
 * Copyright (c) 2015-2021 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [501899] Use base index instead of IncQuery patterns
 *     Elektrobit - [575391] BasicModelSearchService now supports the use of wildcards and regular expressions
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.search.ui.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.services.AbstractMetaModelService;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.search.ui.ModelSearchMatch;
import org.eclipse.sphinx.emf.search.ui.QuerySpecification;
import org.eclipse.sphinx.emf.ui.util.RetrieveNameAttributeHelper;

public class BasicModelSearchService extends AbstractMetaModelService implements IModelSearchService {

	protected RetrieveNameAttributeHelper helper = new RetrieveNameAttributeHelper();
	private Map<Character, String> replacementMap = null;

	public BasicModelSearchService(Collection<IMetaModelDescriptor> mmDescriptors) {
		super(mmDescriptors);
	}

	@Override
	public List<ModelSearchMatch> getMatches(IModelDescriptor modelDescriptor, QuerySpecification spec) {
		return getMatches(modelDescriptor.getLoadedResources(true), spec);
	}

	@Override
	public List<ModelSearchMatch> getMatches(Collection<Resource> resources, QuerySpecification spec) {
		List<ModelSearchMatch> result = new ArrayList<ModelSearchMatch>();
		initReplacementMap();
		for (Resource resource : resources) {
			TreeIterator<EObject> allContents = resource.getAllContents();
			while (allContents.hasNext()) {
				EObject eObject = allContents.next();
				EAttribute nameAttribute = helper.getNameAttribute(eObject);
				if (nameAttribute != null) {
					Object nameObj = eObject.eGet(nameAttribute);
					if (nameObj != null) {
						if (isMatchingPattern(spec, nameObj.toString())) {
							result.add(createModelSearchMatch(eObject));
						}
					}
				}
			}
		}
		return result;
	}

	private boolean isMatchingPattern(QuerySpecification spec, String content) {
		if (spec == null) {
			return false;
		}
		String specificationPattern = spec.getPattern();
		if (specificationPattern == null) {
			return false;
		}
		String regularExpression = getRegularExpression(specificationPattern);
		int flag = spec.isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE;
		return Pattern.compile(regularExpression, flag).matcher(content).find();
	}

	private String getRegularExpression(String globInput) {
		StringBuilder sb = new StringBuilder();
		for (char character : globInput.toCharArray()) {
			String replacement = replacementMap.get(character);
			if (replacement == null) {
				sb.append(character);
			} else {
				sb.append(replacement);
			}
		}
		return sb.toString();
	}

	private void initReplacementMap() {
		if (replacementMap != null) {
			return;
		}
		replacementMap = new HashMap<>();
		replacementMap.put('*', ".*"); //$NON-NLS-1$
		replacementMap.put('?', "."); //$NON-NLS-1$
		replacementMap.put('(', "\\("); //$NON-NLS-1$
		replacementMap.put(')', "\\)"); //$NON-NLS-1$
		replacementMap.put('[', "\\["); //$NON-NLS-1$
		replacementMap.put(']', "\\]"); //$NON-NLS-1$
		replacementMap.put('{', "\\}"); //$NON-NLS-1$
		replacementMap.put('.', "\\."); //$NON-NLS-1$
		replacementMap.put('$', "\\$"); //$NON-NLS-1$
		replacementMap.put('^', "\\^"); //$NON-NLS-1$
	}

	private ModelSearchMatch createModelSearchMatch(EObject eObject) {
		ModelSearchMatch match = new ModelSearchMatch(eObject);
		return match;
	}
}
