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
package org.eclipse.sphinx.examples.tests.perf;

import org.eclipse.sphinx.examples.tests.perf.internal.Activator;
import org.eclipse.sphinx.platform.stats.AbstractPerformanceStats;
import org.eclipse.sphinx.platform.stats.IEventTypeEnumerator;

@SuppressWarnings("nls")
public class ExamplePerformanceStats extends AbstractPerformanceStats<ExamplePerformanceStats.EventsEnum> {

	public static ExamplePerformanceStats INSTANCE = new ExamplePerformanceStats();

	/**
	 * This enum contains all the IDs of the event that can be triggered by this concrete class of
	 * {@link AbstractPerformanceStats}.
	 */
	public enum EventsEnum implements IEventTypeEnumerator {
		EVENT_INSIDE_SAMPLE_METHOD1_PART1("InsideSampleMethod1_part1"), EVENT_INSIDE_SAMPLE_METHOD1_PART2("InsideSampleMethod1_part2"), EVENT_INSIDE_SAMPLE_METHOD1_PART3(
				"InsideSampleMethod1_part3"), EVENT_INSIDE_SAMPLE_METHOD2("InsideSampleMethod2"), EVENT_INSIDE_SAMPLE_METHOD3("InsideSampleMethod3"), EVENT_INSIDE_ALL_METHODS(
				"InsideAllMethod");
		private String name;

		private EventsEnum(String EventName) {
			name = EventName;
		}

		@Override
		public String getName() {

			return name;
		}

	}

	/**
	 * This method is used by {@link AbstractPerformanceStats} to retrieve options set in .option file and relative to
	 * the events defined in the enum.
	 */
	@Override
	protected String getPluginId() {
		return Activator.getPlugin().toString();
	}
}
