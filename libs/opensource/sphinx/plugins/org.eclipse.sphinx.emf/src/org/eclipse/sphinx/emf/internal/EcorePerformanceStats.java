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
package org.eclipse.sphinx.emf.internal;

import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.platform.stats.AbstractPerformanceStats;
import org.eclipse.sphinx.platform.stats.IEventTypeEnumerator;

/**
 * 
 */
final public class EcorePerformanceStats extends AbstractPerformanceStats<EcorePerformanceStats.EcoreEvent> {

	/**
	 * The singleton instance.
	 */
	public static EcorePerformanceStats INSTANCE = new EcorePerformanceStats();

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
	public enum EcoreEvent implements IEventTypeEnumerator {

		/**
		 * Event for collecting choices of values.
		 */
		EVENT_CHOICES_OF_VALUES("choicesOfValues"), //$NON-NLS-1$

		/**
		 * Event for factorizing objects of a resource.
		 */
		EVENT_FACTORIZED_OBJECTS("factorizedObjects"), //$NON-NLS-1$

		/**
		 * Event for reading contribution to custom commands extension point.
		 */
		EVENT_READ_CONTRIBUTIONS_CUSTOM_COMMANDS("readContributions_customCommands"); //$NON-NLS-1$

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
		private EcoreEvent(String eventName) {
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
	public enum EcoreContext implements IEventTypeEnumerator {

		CONTEXT(""); //$NON-NLS-1$

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
		private EcoreContext(String contextName) {
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
