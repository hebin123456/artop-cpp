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

import java.lang.management.ManagementFactory;

@SuppressWarnings("nls")
public class ExampleApplication {

	public ExampleApplication() {
	}

	/**
	 * A sample method performing a time loop . Effective waiting time may defer from the value provide in argument.
	 * 
	 * @param timeOut
	 *            value of the timeLoop in millisecond.
	 */
	private void timeLoop(long timeOut) {
		long currentCpuTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

		while (ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() <= currentCpuTime + timeOut * (long) 1e6) {

		}

	}

	public void method1(String param) {

		// Start a new event of type EVENT_INSIDE_SAMPLE_METHOD1_PART1
		// this event will be added to default context if no context has been previously opened.
		ExamplePerformanceStats.INSTANCE.startNewEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD1_PART1, param);
		// ----Portion of code measured by previous event.----
		timeLoop(234);
		// ---------------------------------------------------
		// This call stop the running time for the event previously opened.

		ExamplePerformanceStats.INSTANCE.endEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD1_PART1, param);

		// Start a new event of type EVENT_INSIDE_SAMPLE_METHOD1_PART2
		// this event will be added to default context if no context has been previously opened.

		ExamplePerformanceStats.INSTANCE.startNewEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD1_PART2, param);

		// ----Portion of code measured by previous event.----
		timeLoop(678);
		// ---------------------------------------------------

		// This call stop the running time for the event previously opened.

		ExamplePerformanceStats.INSTANCE.endEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD1_PART2, param);

		// Start a new event of type EVENT_INSIDE_SAMPLE_METHOD1_PART2
		// this event will be added to default context if no context has been previously opened.

		ExamplePerformanceStats.INSTANCE.startNewEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD1_PART3, param);

		// ----Portion of code measured by previous event.----
		timeLoop(101);
		// --------------------------------------------------

		// This call stop the running time for the event previously opened.

		ExamplePerformanceStats.INSTANCE.endEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD1_PART3, param);

	}

	public void method2(String param) {

		// Open a new context named "Method2". This context is now the last opened context and is the upper context in
		// context stack call.
		// All events that will be triggered will refer to that context until another context would be opened or this
		// context would be closed.
		// Each context that will be opened and closed before "Method2" context would be closed ,will be child context
		// of "Method2" context.

		ExamplePerformanceStats.INSTANCE.openContext("Method2");

		// Start a new event of type EVENT_INSIDE_SAMPLE_METHOD2
		// this event will be added to the context "Method2" since this context has been opened and still not closed.

		ExamplePerformanceStats.INSTANCE.startNewEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD2, param);

		// ----Portion of code measured by previous event.----
		timeLoop(1000);
		// --------------------------------------------------

		// This call stop the running time for the event previously opened.

		ExamplePerformanceStats.INSTANCE.endEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD2, param);

		// Open a new context named "Call Method3 from Method2". This context is now the last opened context and is the
		// upper context in context stack call.
		// All events that will be triggered will refer to that context until another context would be opened or this
		// context would be closed.
		// All triggered events and opened contexts into method3 will refer to that context since it is closed after
		// returning from method3.

		ExamplePerformanceStats.INSTANCE.openContext("Call Method3 from Method2");

		method3(param);

		// This method closed the last opened context registered in context stack call.The context is removed from
		// context stack call
		// and if it is the first context opened (first element of context stack call list) , the context is logged and
		// then removed from the base (only if remove base option set to false).
		// In this example case "Call Method3 from Method2" is closed but not logged since it is not the last opened
		// context.
		// The running time for the context is the addition of all the running time of the events and the contexts it
		// contains.

		ExamplePerformanceStats.INSTANCE.closeAndLogCurrentContext();// end of context "Call Method3 from Method2"

		// This method closed the last opened context registered in context stack call.The context is removed from
		// context stack call
		// and if it is the first context opened (first element of context stack call list) , the context is logged and
		// then removed from the base (only if remove base option set to false).
		// The running time for the context is the addition of all the running time of the events and the contexts it
		// contains.

		ExamplePerformanceStats.INSTANCE.closeAndLogCurrentContext();// end of context "Method2"
	}

	public void method3(String param) {
		// Start a new event of type EVENT_INSIDE_SAMPLE_METHOD3
		// this event will be added to default context if no context has been previously opened.

		ExamplePerformanceStats.INSTANCE.startNewEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD3, param);

		// ----Portion of code measured by previous event.----
		timeLoop(2000);
		// --------------------------------------------------

		// This call stop the running time for the event previously opened.

		ExamplePerformanceStats.INSTANCE.endEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD3, param);

		// Start an event of type EVENT_INSIDE_SAMPLE_METHOD3
		// this event will be added to default context if no context has been previously opened.
		// Since we use startEvent method and since EVENT_INSIDE_SAMPLE_METHOD3 event with blame object "param" has
		// already been triggered, this call
		// will add a run count to the event and its running time will be added to previous running time.

		ExamplePerformanceStats.INSTANCE.startEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD3, param);

		// ----Portion of code measured by previous event.----
		timeLoop(300);
		// --------------------------------------------------

		// This call stop the running time for the event previously opened.

		ExamplePerformanceStats.INSTANCE.endEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD3, param);

		// Start an event of type EVENT_INSIDE_SAMPLE_METHOD3
		// this event will be added to default context if no context has been previously opened.
		// Note that this event has the same type as previous triggered events but it used a different blame object.
		// This will be registered as another instance of statsEvent of type EVENT_INSIDE_SAMPLE_METHOD3.

		ExamplePerformanceStats.INSTANCE.startNewEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD3, param + " other blame object");

		// ----Portion of code measured by previous event.----
		timeLoop(3000);
		// --------------------------------------------------

		// This call stop the running time for the event previously opened.

		ExamplePerformanceStats.INSTANCE.endEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_SAMPLE_METHOD3, param + " other blame object");
	}

	public void allMethods(String param) {

		// Open a new context named "All Methods". This context is now the last opened context and is the upper context
		// in context stack call.
		// All events that will be triggered will refer to that context until another context would be opened or this
		// context would be closed.
		// All triggered events and opened contexts into method3 will refer to that context since it is closed after
		// returning from method3.

		ExamplePerformanceStats.INSTANCE.openContext("All Methods");

		// Start a new event of type EVENT_INSIDE_ALL_METHODS
		// this event will be added to default context if no context has been previously opened.

		ExamplePerformanceStats.INSTANCE.startNewEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_ALL_METHODS, param + " Before call");

		// ----Portion of code measured by previous event.----
		timeLoop(500);
		// --------------------------------------------------

		// This call stop the running time for the event previously opened.

		ExamplePerformanceStats.INSTANCE.endEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_ALL_METHODS, param + " Before call");

		// Open a new context named "All Methods call". This context is now the last opened context and is the upper
		// context in context stack call.
		// All events that will be triggered will refer to that context until another context would be opened or this
		// context would be closed.
		// All triggered events and opened contexts into method3 will refer to that context since it is closed after
		// returning from method3.

		ExamplePerformanceStats.INSTANCE.openContext("All Methods call");

		method1(param);
		method2(param);

		// This method closed the last opened context registered in context stack call.The context is removed from
		// context stack call
		// and if it is the first context opened (first element of context stack call list) , the context is logged and
		// then removed from the base (only if remove base option set to false).
		// The running time for the context is the addition of all the running time of the events and the contexts it
		// contains.

		ExamplePerformanceStats.INSTANCE.closeAndLogContext("All Methods call");// end of context "All Methods call"

		// Start a new event of type EVENT_INSIDE_ALL_METHODS
		// this event will be added to default context if no context has been previously opened.

		ExamplePerformanceStats.INSTANCE.startNewEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_ALL_METHODS, param + " After call");

		// ----Portion of code measured by previous event.----
		timeLoop(300);
		// --------------------------------------------------

		// This call stop the running time for the event previously opened.

		ExamplePerformanceStats.INSTANCE.endEvent(ExamplePerformanceStats.EventsEnum.EVENT_INSIDE_ALL_METHODS, param + " After call");

		// This method closed the last opened context registered in context stack call.The context is removed from
		// context stack call
		// and if it is the first context opened (first element of context stack call list) , the context is logged and
		// then removed from the base (only if remove base option set to false).
		// The running time for the context is the addition of all the running time of the events and the contexts it
		// contains.

		ExamplePerformanceStats.INSTANCE.closeAndLogCurrentContext();// end of context "All Methods"
	}

}
