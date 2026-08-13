/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
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
package org.eclipse.sphinx.emf.validation;

import java.util.EventObject;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.emf.ecore.EObject;

/**
 * A singleton notifier that is responsible for firing validation problem markers changes to encapsulated listeners.
 * <p>
 * These listeners are must implement {@linkplain IValidationProblemMarkersChangeListener}.
 */
public final class ValidationProblemMarkersChangeNotifier {

	/**
	 * The singleton instance.
	 */
	public static ValidationProblemMarkersChangeNotifier INSTANCE = new ValidationProblemMarkersChangeNotifier();

	// Prevent from instantiation
	private ValidationProblemMarkersChangeNotifier() {
	}

	/**
	 * The list of validation problem markers change listeners.
	 */
	protected ListenerList listeners = new ListenerList();

	/**
	 * Adds the specified {@linkplain IValidationProblemMarkersChangeListener} to the list of listeners managed by this
	 * notifier.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addListener(IValidationProblemMarkersChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the specified {@linkplain IValidationProblemMarkersChangeListener} from the list of listeners managed by
	 * this notifier.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public void removeListener(IValidationProblemMarkersChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notifies encapsulated listeners that problem markers changed for the given object.
	 * 
	 * @param eObject
	 *            The object for which attached problem markers changed.
	 */
	public void fireValidationProblemMarkersChanged(EObject eObject) {
		for (Object listener : listeners.getListeners()) {
			((IValidationProblemMarkersChangeListener) listener).validationProblemMarkersChanged(new EventObject(eObject));
		}
	}
}
