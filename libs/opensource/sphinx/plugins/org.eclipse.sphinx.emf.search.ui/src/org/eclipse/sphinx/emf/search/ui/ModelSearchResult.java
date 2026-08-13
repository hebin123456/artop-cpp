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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.RemoveAllEvent;

public class ModelSearchResult implements ISearchResult {

	private static final ModelSearchMatch[] EMPTY_ARRAY = new ModelSearchMatch[0];

	private ModelSearchQuery query;
	private final ListenerList listeners;
	private final Map<Object, List<ModelSearchMatch>> elementsToMatches;
	private final MatchEvent matchEvent;

	public ModelSearchResult(ModelSearchQuery query) {
		this.query = query;
		listeners = new ListenerList();
		elementsToMatches = new HashMap<Object, List<ModelSearchMatch>>();
		matchEvent = new MatchEvent(this);
	}

	/**
	 * Returns an array with all matches reported against the given element. Note that all matches of the given element
	 * are returned. The filter state of the matches is not relevant.
	 *
	 * @param element
	 *            the element to report matches for
	 * @return all matches reported for this element
	 * @see ModelSearchMatch#getElement()
	 */
	public ModelSearchMatch[] getMatches(Object element) {
		synchronized (elementsToMatches) {
			List<ModelSearchMatch> matches = elementsToMatches.get(element);
			if (matches != null) {
				return matches.toArray(new ModelSearchMatch[matches.size()]);
			}
			return EMPTY_ARRAY;
		}
	}

	/**
	 * Adds a <code>Match</code> to this search result. This method does nothing if the match is already present.
	 * <p>
	 * Subclasses may extend this method.
	 * </p>
	 *
	 * @param match
	 *            the match to add
	 */
	public void addMatch(ModelSearchMatch match) {
		boolean hasAdded = false;
		synchronized (elementsToMatches) {
			hasAdded = doAddMatch(match);
		}
		if (hasAdded) {
			fireChange(getSearchResultEvent(match, MatchEvent.ADDED));
		}
	}

	private boolean doAddMatch(ModelSearchMatch match) {
		// updateFilterState(match);

		List<ModelSearchMatch> matches = elementsToMatches.get(match.getElement());
		if (matches == null) {
			matches = new ArrayList<ModelSearchMatch>();
			elementsToMatches.put(match.getElement(), matches);
			matches.add(match);
			return true;
		}
		if (!matches.contains(match)) {
			matches.add(match);
			return true;
		}
		return false;
	}

	/**
	 * Adds a number of SearchMatches to this search result. This method does nothing for matches that are already
	 * present.
	 * <p>
	 * Subclasses may extend this method.
	 * </p>
	 *
	 * @param matches
	 *            the matches to add
	 */
	public void addMatches(ModelSearchMatch[] matches) {
		Collection<ModelSearchMatch> reallyAdded = new ArrayList<ModelSearchMatch>();
		synchronized (elementsToMatches) {
			for (ModelSearchMatch matche : matches) {
				if (doAddMatch(matche)) {
					reallyAdded.add(matche);
				}
			}
		}
		if (!reallyAdded.isEmpty()) {
			fireChange(getSearchResultEvent(reallyAdded, MatchEvent.ADDED));
		}
	}

	/**
	 * Returns an array containing the set of all elements that matches are reported against in this search result. Note
	 * that all elements that contain matches are returned. The filter state of the matches is not relevant.
	 *
	 * @return the set of elements in this search result
	 */
	public Object[] getElements() {
		synchronized (elementsToMatches) {
			return elementsToMatches.keySet().toArray();
		}
	}

	/**
	 * Removes all matches from this search result.
	 * <p>
	 * Subclasses may extend this method.
	 * </p>
	 */
	public void removeAll() {
		synchronized (elementsToMatches) {
			doRemoveAll();
		}
		fireChange(new RemoveAllEvent(this));
	}

	private void doRemoveAll() {
		elementsToMatches.clear();
	}

	/**
	 * Removes the given match from this search result. This method has no effect if the match is not found.
	 * <p>
	 * Subclasses may extend this method.
	 * </p>
	 *
	 * @param match
	 *            the match to remove
	 */
	public void removeMatch(ModelSearchMatch match) {
		boolean existed = false;
		synchronized (elementsToMatches) {
			existed = doRemoveMatch(match);
		}
		if (existed) {
			fireChange(getSearchResultEvent(match, MatchEvent.REMOVED));
		}
	}

	private boolean doRemoveMatch(ModelSearchMatch match) {
		boolean existed = false;
		List<ModelSearchMatch> matches = elementsToMatches.get(match.getElement());
		if (matches != null) {
			existed = matches.remove(match);
			if (matches.isEmpty()) {
				elementsToMatches.remove(match.getElement());
			}
		}
		return existed;
	}

	/**
	 * Removes the given matches from this search result. This method has no effect for matches that are not found
	 * <p>
	 * Subclasses may extend this method.
	 * </p>
	 *
	 * @param matches
	 *            the matches to remove
	 */
	public void removeMatches(ModelSearchMatch[] matches) {
		List<ModelSearchMatch> existing = new ArrayList<ModelSearchMatch>();
		synchronized (elementsToMatches) {
			for (ModelSearchMatch matche : matches) {
				if (doRemoveMatch(matche)) {
					existing.add(matche); // no duplicate matches at this point
				}
			}
		}
		if (!existing.isEmpty()) {
			fireChange(getSearchResultEvent(existing, MatchEvent.REMOVED));
		}
	}

	/**
	 * Send the given <code>SearchResultEvent</code> to all registered search result listeners.
	 *
	 * @param e
	 *            the event to be sent
	 * @see ISearchResultListener
	 */
	protected void fireChange(SearchResultEvent e) {
		Object[] listeners2 = listeners.getListeners();
		for (Object l : listeners2) {
			((ISearchResultListener) l).searchResultChanged(e);
		}
	}

	private MatchEvent getSearchResultEvent(ModelSearchMatch match, int eventKind) {
		matchEvent.setKind(eventKind);
		matchEvent.setMatch(match);
		return matchEvent;
	}

	private MatchEvent getSearchResultEvent(Collection<ModelSearchMatch> matches, int eventKind) {
		matchEvent.setKind(eventKind);
		ModelSearchMatch[] matchArray = matches.toArray(new ModelSearchMatch[matches.size()]);
		matchEvent.setMatches(matchArray);
		return matchEvent;
	}

	@Override
	public void addListener(ISearchResultListener searchResultListener) {
		listeners.add(searchResultListener);
	}

	@Override
	public void removeListener(ISearchResultListener searchResultListener) {
		listeners.remove(searchResultListener);
	}

	@Override
	public String getLabel() {
		return query.getResultLabel(getMatchCount());
	}

	@Override
	public String getTooltip() {
		return getLabel();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISearchQuery getQuery() {
		return query;
	}

	/**
	 * Returns the total number of matches contained in this search result. The filter state of the matches is not
	 * relevant when counting matches. All matches are counted.
	 *
	 * @return total number of matches
	 */
	public int getMatchCount() {
		int count = 0;
		synchronized (elementsToMatches) {
			for (List<ModelSearchMatch> matches : elementsToMatches.values()) {
				if (matches != null) {
					count += matches.size();
				}
			}
		}
		return count;
	}

	/**
	 * Returns the number of matches reported against a given element. This is equivalent to calling
	 * <code>getMatches(element).length</code> The filter state of the matches is not relevant when counting matches.
	 * All matches are counted.
	 *
	 * @param element
	 *            the element to get the match count for
	 * @return the number of matches reported against the element
	 */
	public int getMatchCount(Object element) {
		List<ModelSearchMatch> matches = elementsToMatches.get(element);
		if (matches != null) {
			return matches.size();
		}
		return 0;
	}
}
