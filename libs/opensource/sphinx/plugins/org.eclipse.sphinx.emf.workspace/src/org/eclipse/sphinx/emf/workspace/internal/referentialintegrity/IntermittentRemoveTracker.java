/**
 * <copyright>
 *
 * Copyright (c) 2016 itemis and others.
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
package org.eclipse.sphinx.emf.workspace.internal.referentialintegrity;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

/**
 * A {@link Map} implementation that can be used to keep track of objects being removed in a single or in multiple
 * closely spaced (intermittent) remove operations.
 */
public class IntermittentRemoveTracker extends WeakHashMap<EObject, URI> {

	protected static final int MAX_INTERMITTENT_REMOVE_DEFAULT_INTERVAL = 1000;

	// The maximum interval between two discrete remove operations within an intermittent remove operation in
	// milliseconds
	private long maxIntermittentRemoveInterval = MAX_INTERMITTENT_REMOVE_DEFAULT_INTERVAL;

	// Time when last discrete remove operation within an intermittent remove operation has occurred in milliseconds
	private long lastDecreteRemoveTime = System.currentTimeMillis();

	/**
	 * Returns the maximum interval between two discrete remove operations within an intermittent remove operation in
	 * milliseconds.
	 *
	 * @return The maximum intermittent remove interval in milliseconds.
	 */
	public long getMaxIntermittentRemoveInterval() {
		return maxIntermittentRemoveInterval;
	}

	/**
	 * Sets the maximum interval between two discrete remove operations within an intermittent remove operation in
	 * milliseconds.
	 *
	 * @param maxIntermittentRemoveInterval
	 *            The maximum intermittent remove interval in milliseconds to be used.
	 */
	public void setMaxIntermittentRemoveInterval(long maxIntermittentRemoveInterval) {
		this.maxIntermittentRemoveInterval = maxIntermittentRemoveInterval;
	}

	protected void updateLastDiscreteRemoveTime() {
		lastDecreteRemoveTime = System.currentTimeMillis();
	}

	protected boolean isIntermittentRemoveInProgress() {
		return System.currentTimeMillis() - lastDecreteRemoveTime > maxIntermittentRemoveInterval;
	}

	/*
	 * @see java.util.WeakHashMap#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public URI put(EObject eObject, URI oldURI) {
		// Update time of last discrete remove operation
		updateLastDiscreteRemoveTime();

		// Keep track of provided removed object and its old URI
		return super.put(eObject, oldURI);
	}

	/**
	 * Removes all obsolete entries, i.e., all removed object/old URI mappings that are not related to the currently
	 * ongoing intermittent remove operation. Removes all entries in case that no intermittent remove operation is in
	 * progress.
	 */
	public void clearObsoleteEntries() {
		if (isIntermittentRemoveInProgress()) {
			clear();
		}
	}
}
