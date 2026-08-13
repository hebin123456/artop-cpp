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
package org.eclipse.sphinx.platform.stats;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IPerformanceStats<T extends IEventTypeEnumerator> {

	/**
	 * Return activation status for performanceStats framework.
	 * 
	 * @return true if option "org.eclipse.sphinx.platform/perf/jUnitOptions/resetEventBase" = true in ".option" file
	 *         and if -debug flag is set in configuration launch arguments.
	 */
	public abstract boolean isEnabled();

	public abstract boolean isEventTriggered();

	public abstract boolean openContextIfFirstOne(String context);

	/**
	 * Open a context using name provided in argument, and add it to the top of <b><code>contextStackCall</code></b>
	 * list. If context is open for the first time a new one is created and added. All the events triggered after the
	 * call to <b><code>openContext(String context)</code></b> and before a call to <b>
	 * <code>closeContext(String context)</code></b> or <b><code>closeCurrentContext()</code></b> will be included in
	 * this context.
	 * 
	 * @param context
	 *            the name of the context to open. This must not be null.
	 */
	public abstract void openContext(String context, Object blame);

	/**
	 * Open a context using name provided in argument, and add it to the top of <b><code>contextStackCall</code></b>
	 * list. If context is open for the first time a new one is created and added. All the events triggered after the
	 * call to <b><code>openContext(String context)</code></b> and before a call to <b>
	 * <code>closeContext(String context)</code></b> or <b><code>closeCurrentContext()</code></b> will be included in
	 * this context.
	 * 
	 * @param context
	 *            the name of the context to open. This must not be null.
	 */
	public abstract void openContext(String context);

	/**
	 * Close a context using name provided in argument and remove it from <b><code>contextStackCall</code></b> list. In
	 * runTime mode context description is logged after context is closed. If the option
	 * <i><b>org.eclipse.sphinx.platform/perf/jUnitOptions/resetEventBase</b></i> in .option file is set
	 * <code>true</code> all the events included in this context are removed from the base after logging.
	 * 
	 * @param context
	 *            the name of the context to close. This must not be null.
	 */
	public abstract void closeAndLogContext(String context, Object blame);

	/**
	 * Close a context using name provided in argument and remove it from <b><code>contextStackCall</code></b> list. In
	 * runTime mode context description is logged after context is closed. If the option
	 * <i><b>org.eclipse.sphinx.platform/perf/jUnitOptions/resetEventBase</b></i> in .option file is set
	 * <code>true</code> all the events included in this context are removed from the base after logging.
	 * 
	 * @param context
	 *            the name of the context to close. This must not be null.
	 */
	public abstract void closeAndLogContext(String context);

	/**
	 * Close the last context in <b><code>contextStackCall</code></b> list and remove it from the list. In runTime mode
	 * context description is logged after context is closed. If the option
	 * <i><b>org.eclipse.sphinx.platform/perf/jUnitOptions/resetEventBase</b></i> in .option file is set
	 * <code>true</code> all the events included in this context are removed from the base after logging.
	 */
	public abstract void closeAndLogCurrentContext();

	/**
	 * Close all the context in <b><code>contextStackCall</code></b> list and remove them from the list. In runTime mode
	 * context description of the first context in the list (the parent context)is logged. If the option
	 * <i><b>org.eclipse.sphinx.platform/perf/jUnitOptions/resetEventBase</b></i> in .option file is set
	 * <code>true</code> all the events included in this context are removed from the base after logging.
	 */
	public abstract void closeAllParentContext();

	/**
	 * Close a context using name provided in argument and remove it from <b><code>context stack call</code></b> list.
	 * All the events triggered after this call to the close method will be included in the previous opened context. If
	 * there is no more opened context events are added to default context <i>Global</i>.
	 * 
	 * @param context
	 *            the name of the context to close. This must not be null.
	 */
	public abstract void closeContext(String context);

	/**
	 * Remove a {@link StatsEvent} object from the data base.
	 * 
	 * @param enumerator
	 *            The ID of the event.Must be an enumerator provided by the enum declared in the concrete class of
	 *            {@link AbstractPerformanceStats}.
	 * @param blameObject
	 *            An object responsible for the event.Can be a simple string. Must not be null.
	 * @param context
	 *            the context where the event has been triggered.
	 */
	public abstract void removeStat(T enumerator, Object blameObject, String context);

	/**
	 * Remove a context from the data base and all the events stored under that context.
	 * 
	 * @param context
	 *            the name of the context to remove.
	 */
	public abstract void removeContext(String context);

	/**
	 * Remove all the events stored under that context from the data base.
	 * 
	 * @param context
	 *            the name of the context.
	 */
	public abstract void removeStatsForContext(String context);

	/**
	 * Remove all the events of one given ID stored under that context from the data base.
	 * 
	 * @param enumerator
	 *            The ID of the event.Must be an enumerator provided by the enum declared in the concrete class of
	 *            {@link AbstractPerformanceStats}.
	 * @param context
	 *            the name of the context.
	 */
	public abstract void removeStatsForEvent(T enumerator, String context);

	/**
	 * Remove all contexts and events stored in the data base.
	 */
	public abstract void removeAllStats();

	/**
	 * Test if an event triggered under default context has a running time exceeding timeout value defined in .option
	 * file.
	 * 
	 * @param enumerator
	 *            The ID of the event.Must be an enumerator provided by the enum declared in the concrete class of
	 *            {@link AbstractPerformanceStats}.
	 * @param blameObject
	 *            An object responsible for the event.Can be a simple string. Must not be null.
	 * @return true if running time for the event exceed the timeout value defined in .option file. return false if the
	 *         event does not exist ,if timeout value has not been set or if running time is under timeout value.
	 */
	public abstract boolean isEventTimeFail(T enumerator, Object blameObject);

	/**
	 * Test if an event triggered under context has a running time exceeding timeout value defined in .option file.
	 * 
	 * @param enumerator
	 *            The ID of the event.Must be an enumerator provided by the enum declared in the concrete class of
	 *            {@link AbstractPerformanceStats}.
	 * @param blameObject
	 *            An object responsible for the event.Can be a simple string. Must not be null.
	 * @param context
	 *            the name of the context.
	 * @return true if running time for the event exceed the timeout value defined in .option file. return false if the
	 *         event does not exist ,if timeout value has not been set or if running time is under timeout value.
	 */
	public abstract boolean isEventTimeFail(T enumerator, Object blameObject, String context);

	/**
	 * Start a new event under the current opened context (default context if no context is opened).If an event with the
	 * same ID and the same blame object has been previously triggered it will be replaced.
	 * 
	 * @param enumerator
	 *            The ID of the event.Must be an enumerator provided by the enum declared in the concrete class of
	 *            {@link AbstractPerformanceStats}.
	 * @param blameObject
	 *            An object responsible for the event.Can be a simple string. Must not be null.
	 */
	public abstract void startNewEvent(T enumerator, Object blameObject);

	/**
	 * Start an event under the current opened context (default context if no context is opened).If an event with the
	 * same ID and the same blame object has been previously triggered, the <b>running time</b> for this declaration
	 * will be added to the current <b>running time</b> value and <b>runCount</b> parameter will be incremented.
	 * 
	 * @param enumerator
	 *            The ID of the event.Must be an enumerator provided by the enum declared in the concrete class of
	 *            {@link AbstractPerformanceStats}.
	 * @param blameObject
	 *            An object responsible for the event.Can be a simple string. Must not be null.
	 */
	public abstract void startEvent(T enumerator, Object blameObject);

	/**
	 * Start a new event under the provided context (default context if the context is not opened).If an event with the
	 * same ID and the same blame object has been previously triggered it will be replaced.
	 * 
	 * @param enumerator
	 *            The ID of the event.Must be an enumerator provided by the enum declared in the concrete class of
	 *            {@link AbstractPerformanceStats}.
	 * @param blameObject
	 *            An object responsible for the event.Can be a simple string. Must not be null.
	 */
	public abstract void startNewEvent(T enumerator, Object blameObject, String context);

	/**
	 * Start an event under the provided context (default context if the context is not opened).If an event with the
	 * same ID and the same blame object has been previously triggered, the <b>running time</b> for this declaration
	 * will be added to the current <b>running time</b> value and <b>runCount</b> parameter will be incremented.
	 * 
	 * @param enumerator
	 *            The ID of the event.Must be an enumerator provided by the enum declared in the concrete class of
	 *            {@link AbstractPerformanceStats}.
	 * @param blameObject
	 *            An object responsible for the event.Can be a simple string. Must not be null.
	 */
	public abstract void startEvent(T enumerator, Object blameObject, String context);

	/**
	 * Stop the running time for the event referenced by the ID and the blame object under the current opened context .
	 * 
	 * @param enumerator
	 *            The ID of the event.Must be an enumerator provided by the enum declared in the concrete class of
	 *            {@link AbstractPerformanceStats}.
	 * @param blameObject
	 *            An object responsible for the event.Can be a simple string. Must not be null.
	 */
	public abstract void endEvent(T enumerator, Object blameObject);

	/**
	 * Stop the running time for the event referenced by the ID and the blame object under the provided context.
	 * 
	 * @param enumerator
	 *            The ID of the event.Must be an enumerator provided by the enum declared in the concrete class of
	 *            {@link AbstractPerformanceStats}.
	 * @param blameObject
	 *            An object responsible for the event.Can be a simple string. Must not be null.
	 * @param context
	 *            the name of the context containing the event.
	 */
	public abstract void endEvent(T enumerator, Object blameObject, String context);

	/**
	 * Return the {@link StatsEvent} referenced by the ID and the blame object under the provided context.
	 * 
	 * @param enumerator
	 *            The ID of the event.Must be an enumerator provided by the enum declared in the concrete class of
	 *            {@link AbstractPerformanceStats}.
	 * @param blameObject
	 *            An object responsible for the event.Can be a simple string. Must not be null.
	 * @param context
	 *            the name of the context containing the event.
	 * @return {@link StatsEvent}
	 */
	public abstract StatsEvent getEventStat(T enumerator, Object blameObject, String context);

	/**
	 * Return all the {@link StatsEvent} of this ID under the provided context.
	 * 
	 * @param enumerator
	 *            The ID of the event.Must be an enumerator provided by the enum declared in the concrete class of
	 *            {@link AbstractPerformanceStats}.
	 * @param context
	 *            the name of the context containing the event.
	 * @return Map<Integer, StatsEvent>
	 */
	public abstract Map<Integer, StatsEvent> getEventStats(T enumerator, String context);

	/**
	 * Return a list of all the context declared in the base.
	 * 
	 * @return Collection<String>
	 */
	public abstract Collection<String> getContextNames();

	/**
	 * Check if the running time for the context provided in argument exceed the timeout value provide in argument.
	 * 
	 * @param context
	 *            the name of the context.
	 * @param timeOut
	 *            timeout value in millisecond.
	 * @return <code>true</code> if context has been declared <code>false</code> otherwise.
	 */
	public abstract boolean contextRunningTimeExceedTimeOut(String context, long timeOut);

	/**
	 * Check if the context provided in argument has been declared and used.
	 * 
	 * @param context
	 *            the name of the context.
	 * @return <code>true</code> if context has been declared <code>false</code> otherwise.
	 */
	public abstract boolean isRegisteredContext(String context);

	/**
	 * Return the context running time in millisecond.
	 * 
	 * @param context
	 *            the name of the context.
	 * @return long the running time in millisecond.return -1 if the context do not exist.
	 */
	public abstract long getContextRunningTime(String context);

	/**
	 * Return a list of all the events under that context that are in time failure state.
	 * 
	 * @param context
	 *            the name of the context.
	 * @return LinkedList<StatsEvent> .
	 */
	public abstract List<StatsEvent> getTimeFailureEventsInContext(String context);

	/**
	 * Check if there are events triggered under any of the contexts,that are in time failure state.
	 * 
	 * @return <code>true</code> if at least one of all the events triggered is in time failure state.
	 */
	public abstract boolean isEventsTimeFailure();

	/**
	 * Return the number of events triggered under any of the contexts,that are in time failure state.
	 * 
	 * @return int
	 */
	public abstract int numberOfEventsTimeFailure(String context);

	/**
	 * Return a list of all the events triggered under any of the contexts,that are in time failure state.
	 * 
	 * @return LinkedList<StatsEvent>
	 */
	public abstract List<StatsEvent> getEventsTimeFailure();

	/**
	 * Check if there are events triggered under the context,that are in time failure state.
	 * 
	 * @param context
	 *            the name of the context.
	 * @return <code>true</code> if at least one of all the events triggered under this context is in time failure
	 *         state.
	 */
	public abstract boolean isContextEventsTimeFailure(String context);

	/**
	 * Return the number of events triggered under the context,that are in time failure state.
	 * 
	 * @param context
	 *            the name of the context.
	 * @return int
	 */
	public abstract int numberOfContextEventsTimeFailure(String context);

	/**
	 * Provided a string representing the tree architecture for one context. All its child contexts are listed.
	 * 
	 * @param context
	 *            the name of the context.
	 * @return String
	 */
	public abstract String printContextTree(String context);

	/**
	 * Provided a string representing the tree architecture for all the contexts. All contexts stored in the base and
	 * there child contexts are listed.
	 * 
	 * @return String
	 */
	public abstract String printContextsTree();

	/**
	 * Provided a string representing all data stored in the base. All contexts stored in the base and their events are
	 * listed.
	 * 
	 * @return String
	 */
	public abstract String printAllStats();

	/**
	 * Provided a string representing all data stored in the base for one context. All events and child contexts stored
	 * in the base for this context are listed.
	 * 
	 * @param context
	 *            the name of the context.
	 * @return String
	 */
	public abstract String printContextStats(String context);

	/**
	 * Provided a string representing data stored in the base for one event.
	 * 
	 * @param enumerator
	 *            The ID of the event.Must be an enumerator provided by the enum declared in the concrete class of
	 *            {@link AbstractPerformanceStats}.
	 * @param blameObject
	 *            An object responsible for the event.Can be a simple string. Must not be null.
	 * @param context
	 *            the name of the context containing the event.
	 * @return String
	 */
	public abstract String printEventStat(T enumerator, Object blameObject, String context);

	/**
	 * Provided a string representing data stored in the base for one ID type of event under a given context.
	 * 
	 * @param enumerator
	 *            The ID of the event.Must be an enumerator provided by the enum declared in the concrete class of
	 *            {@link AbstractPerformanceStats}.
	 * @param context
	 *            the name of the context containing the event.
	 * @return String
	 */
	public abstract String printEventStats(T enumerator, String context);

}