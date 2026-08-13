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
package org.eclipse.sphinx.emf.workspace.internal.saving;

import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.platform.stats.AbstractPerformanceStats;
import org.eclipse.sphinx.platform.stats.IEventTypeEnumerator;

/**
 * 
 */
final public class ModelSavingPerformanceStats extends AbstractPerformanceStats<ModelSavingPerformanceStats.ModelEvent> {

	/**
	 * The singleton instance.
	 */
	public static ModelSavingPerformanceStats INSTANCE = new ModelSavingPerformanceStats();

	/*
	 * @see org.eclipse.sphinx.platform.stats.AbstractPerformanceStats#getPluginId()
	 */
	@Override
	protected String getPluginId() {
		return Activator.getPlugin().getSymbolicName();
	}

	/**
	 * 
	 */
	public enum ModelEvent implements IEventTypeEnumerator {

		/**
		 * SaveablesProvider#getSaveable(Object)
		 */
		EVENT_GET_SAVEABLE("getSaveable"); //$NON-NLS-1$

		/**
		 * The name of this event.
		 */
		private String name;

		/**
		 * Private enumerator constructor.
		 * 
		 * @param eventName
		 *            The name of this event.
		 */
		private ModelEvent(String eventName) {
			name = eventName;
		}

		/*
		 * @see org.eclipse.sphinx.platform.stats.IEventTypeEnumerator#getName()
		 */
		@Override
		public String getName() {
			return name;
		}
	}

	/**
	 * 
	 */
	public enum ModelContext implements IEventTypeEnumerator {

		/**
		 * Context for retrieving active saveables.
		 */
		CONTEXT_GET_ACTIVE_SAVEABLES("Get Active Saveables"), //$NON-NLS-1$

		/**
		 * Context for Selection changed.
		 */
		CONTEXT_IS_DIRTY("Is Dirty"), //$NON-NLS-1$

		/**
		 * Context for Selection changed.
		 */
		CONTEXT_SELECTION_CHANGED("Selection Changed"); //$NON-NLS-1$

		/**
		 * The name of this context.
		 */
		private String name;

		/**
		 * Private enumerator constructor.
		 * 
		 * @param contextName
		 *            The name of this context.
		 */
		private ModelContext(String contextName) {
			name = contextName;
		}

		/*
		 * @see org.eclipse.sphinx.platform.stats.IEventTypeEnumerator#getName()
		 */
		@Override
		public String getName() {
			return name;
		}
	}
}
