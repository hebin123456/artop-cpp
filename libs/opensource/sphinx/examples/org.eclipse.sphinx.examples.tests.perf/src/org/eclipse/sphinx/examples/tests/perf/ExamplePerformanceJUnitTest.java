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

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class ExamplePerformanceJUnitTest extends TestCase {

	public void testAllMethods() {
		// The data base containing performance stats events is reset manualy.
		// Option org.eclipse.sphinx.platform/perf/jUnitOptions/ resetEventBase=true
		// in ".option" file.
		ExamplePerformanceStats.INSTANCE.removeAllStats();

		// A call to an instrumented method.
		ExampleApplication application = new ExampleApplication();
		application.allMethods("Param1");

		// We check if performance stats is activated . Option
		// org.eclipse.sphinx.platform/perf=true in ".option" file.

		// We check if the context "All Methods" was created.
		assertTrue(ExamplePerformanceStats.INSTANCE.isRegisteredContext("All Methods"));

		// We check if the context "All Methods" running time is under a given
		// timeout (for this example 10000 ms).

		assertTrue(!ExamplePerformanceStats.INSTANCE.contextRunningTimeExceedTimeOut("All Methods", 10000));

		// printAllStats() is used to display all the performance events stored
		// in the base .

		System.out.println("################### Performance report testAllMethods:" + ExamplePerformanceStats.INSTANCE.printAllStats());

	}

	public void testMethod1() {
		// The data base containing performance stats events is reset manualy.
		// Option org.eclipse.sphinx. platform/perf/jUnitOptions/resetEventBase =true
		// in ".option" file.
		ExamplePerformanceStats.INSTANCE.removeAllStats();

		// A call to an instrumented method.
		ExampleApplication application = new ExampleApplication();
		application.method1("Param2");

		// We check if performance stats is activated . Option
		// org.eclipse.sphinx.platform/perf=true in ".option" file.

		System.out.println("################### Performance report testMethod1:" + ExamplePerformanceStats.INSTANCE.printAllStats());

		// isEventsTimeFailure() return true if any of the triggered event have
		// a running time upper than its timeout value. getEventsTimeFailure()
		// return a list of all triggered event that have a running time upper
		// than their timeout value.

		System.out.println("################### Events in time failure state for testMethod1:"
				+ "\n Check if there is events with time failure state. : " + ExamplePerformanceStats.INSTANCE.isEventsTimeFailure()
				+ "\n The list of all the events with time failure state. :" + ExamplePerformanceStats.INSTANCE.getEventsTimeFailure());

	}

	public void testMethod2() {
		// The data base containing performance stats events is reset manualy.
		// Option org.eclipse.sphinx. platform/perf/jUnitOptions/resetEventBase =true
		// in ".option" file.
		ExamplePerformanceStats.INSTANCE.removeAllStats();
		ExampleApplication application = new ExampleApplication();
		application.method2("Param3");
		/* We check if the context "Method2" was created */
		assertTrue(ExamplePerformanceStats.INSTANCE.isRegisteredContext("Method2"));

		// printContextStats("Method2") is used to display all the performance
		// events for the context "Method2" stored in the base .

		System.out.println("################### Performance report testMethod1:" + ExamplePerformanceStats.INSTANCE.printContextStats("Method2"));
		// isContextEventsTimeFailure("Method2") return true if any of the
		// triggered event contained in context "Method2" have a running time
		// upper than its timeout value.
		// getTimeFailureEventsInContext("Method2") return a list of all
		// triggered event contained in context "Method2" that have a running
		// time upper than their timeout value.

		System.out.println("################### Performance report testMethod2 for context 'Call Method3 from Method2':"
				+ ExamplePerformanceStats.INSTANCE.printContextStats("Call Method3 from Method2"));
		System.out.println("################### Events with time failure state for testMethod2:"
				+ "\n Check if there is events with time failure state. : " + ExamplePerformanceStats.INSTANCE.isContextEventsTimeFailure("Method2")
				+ "\n The list of all the events with time failure state. :"
				+ ExamplePerformanceStats.INSTANCE.getTimeFailureEventsInContext("Method2"));

	}

	public void testMethod3() {
		// The data base containing performance stats events is reset manualy.
		// Option org.eclipse.sphinx. platform/perf/jUnitOptions/resetEventBase =true
		// in ".option" file.
		ExamplePerformanceStats.INSTANCE.removeAllStats();
		ExampleApplication application = new ExampleApplication();
		application.method3("Param4");
		// isEventTimeFail(SamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD3, "Param4") check if the event
		// exist and has a running time upper
		// than its timeout value.

		assertTrue(ExamplePerformanceStats.INSTANCE.isEventTimeFail(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD3, "Param4"));
		System.out.println("################### Performance report testMethod3:" + ExamplePerformanceStats.INSTANCE.printAllStats());

	}

}
