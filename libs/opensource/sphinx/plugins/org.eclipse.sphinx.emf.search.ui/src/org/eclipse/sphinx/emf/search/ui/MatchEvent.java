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

import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.SearchResultEvent;

public class MatchEvent extends SearchResultEvent {

	private static final long serialVersionUID = 1L;

	private int kind;
	private ModelSearchMatch[] matches;
	private ModelSearchMatch[] matchContainer = new ModelSearchMatch[1];

	/**
	 * Constant for a matches being added.
	 *
	 * @see MatchEvent#getKind()
	 */
	public static final int ADDED = 1;

	/**
	 * Constant for a matches being removed.
	 *
	 * @see MatchEvent#getKind()
	 */
	public static final int REMOVED = 2;

	private static final ModelSearchMatch[] EmtpyMatches = new ModelSearchMatch[0];

	/**
	 * Constructs a new <code>MatchEvent</code>.
	 *
	 * @param searchResult
	 *            the search result concerned
	 */
	protected MatchEvent(ISearchResult searchResult) {
		super(searchResult);
	}

	/**
	 * Tells whether this is a remove or an add.
	 *
	 * @return one of <code>ADDED</code> or <code>REMOVED</code>
	 */
	public int getKind() {
		return kind;
	}

	/**
	 * Returns the concerned matches.
	 *
	 * @return the matches this event is about
	 */
	public ModelSearchMatch[] getMatches() {
		if (matches != null) {
			return matches;
		} else if (matchContainer[0] != null) {
			return matchContainer;
		} else {
			return EmtpyMatches;
		}
	}

	/**
	 * Sets the kind of event this is.
	 *
	 * @param kind
	 *            the kind to set; either <code>ADDED</code> or <code>REMOVED</code>
	 */
	protected void setKind(int kind) {
		this.kind = kind;
	}

	/**
	 * Sets the match for the change this event reports.
	 *
	 * @param match
	 *            the match to set
	 */
	protected void setMatch(ModelSearchMatch match) {
		matchContainer[0] = match;
		matches = null;
	}

	/**
	 * Sets the matches for the change this event reports.
	 *
	 * @param matches
	 *            the matches to set
	 */
	protected void setMatches(ModelSearchMatch[] matches) {
		matchContainer[0] = null;
		this.matches = matches;
	}
}
