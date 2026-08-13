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

import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.platform.internal.messages.Messages;

public class StatsEvent {

	private static final long NOT_STARTED = -1;

	private Object blameObject;

	private String eventName;

	private String contextName;

	private int runCount = 0;

	private long runningTime = 0;
	private double nanoRunningTime = 0;

	private long currentStartTime = NOT_STARTED;
	private double nanoCurrentStartTime = NOT_STARTED;

	public StatsEvent(String name, Object blame) {
		eventName = name;
		blameObject = blame;
	}

	/**
	 * Start the time counter for an Event.
	 * 
	 * @param time
	 *            The current time in millisecond.Used to set initial time for this event.
	 * @param reset
	 *            If <code><b>true</b></code> and if the Event has already been started ,running time and run count will
	 *            be reset to 0.
	 * @param context
	 *            The name of the context containing the event.
	 * @return LinkedList<StatsEvent> .
	 */
	public void startRun(long time, boolean reset, String context) {
		currentStartTime = time;
		contextName = context;

		if (reset) {
			runCount = 0;
			runningTime = 0;
		}
	}

	public void startNanoRun(double time, boolean reset, String context) {
		nanoCurrentStartTime = time;
		// contextName = context;

		if (reset) {
			runCount = 0;
			nanoRunningTime = 0;
		}
	}

	/**
	 * Stop the time counter for an Event.
	 * 
	 * @param time
	 *            The current time in millisecond.Used to calculate running time for this event.
	 * @return LinkedList<StatsEvent> .
	 */
	public void endRun(long time) {
		addRun(time - currentStartTime);
		currentStartTime = NOT_STARTED;
	}

	public void endNanoRun(double time) {
		addNanoRun(time - nanoCurrentStartTime);
		nanoCurrentStartTime = NOT_STARTED;
	}

	private void addRun(double elapsed) {
		runCount++;
		runningTime += elapsed;
	}

	private void addNanoRun(double elapsed) {
		// runCount++;
		nanoRunningTime += elapsed;
	}

	/**
	 * Compare current event to obj.
	 * 
	 * @param obj
	 *            the object to compare with current event.
	 * @return true if obj is an instance of {@link StatsEvent} and has same name and same blame object.
	 */
	@Override
	public boolean equals(Object obj) {
		// count and time are not considered part of equality
		if (!(obj instanceof StatsEvent)) {
			return false;
		}
		StatsEvent that = (StatsEvent) obj;
		if (!eventName.equals(that.eventName)) {
			return false;
		}
		if (!getBlameString().equals(that.getBlameString())) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a string describing the blame for this event.
	 * 
	 * @return A string describing the blame.
	 */
	public String getBlameString() {
		return blameObject.toString();
	}

	/**
	 * Return the event name.
	 * 
	 * @return A string representing the event ID.
	 */
	public String getEvent() {
		return eventName;
	}

	/**
	 * Returns the total number of times this event has occurred.
	 * 
	 * @return The number of occurrences of this event.
	 */
	public int getRunCount() {
		return runCount;
	}

	/**
	 * Returns the total execution time in milliseconds for all occurrences of this event.
	 * 
	 * @return The total running time in milliseconds.
	 */
	public long getRunningTime() {
		return runningTime;
	}

	public double getNanoRunningTime() {
		return nanoRunningTime;
	}

	@Override
	public int hashCode() {
		// count and time are not considered part of equality
		return eventName.hashCode() * 37 + getBlameString().hashCode();
	}

	/**
	 * For debugging purposes only.
	 */
	@Override
	public String toString() {
		if (runCount > 1) {
			return NLS.bind(Messages.perfLog_$0$1$2$3statEventToStringWithRunCount, new String[] { eventName, blameObject.toString(),
					getConvertedRunningTime(), "" + runCount }); //$NON-NLS-1$

		} else {

			return NLS.bind(Messages.perfLog_$0$1$2$3statEventToStringSimple, new String[] { eventName, blameObject.toString(),
					getConvertedRunningTime() });
		}

	}

	/**
	 * Provide a String representing the event.
	 * 
	 * @return String.
	 */
	public String printEventStat() {
		return printEventStat(""); //$NON-NLS-1$
	}

	/**
	 * Provide a String representing the event.Each line will start with offset.
	 * 
	 * @param offset
	 *            A string placed at the beginning of each line.
	 * @return String.
	 */
	public String printEventStat(String offset) {
		StringBuffer result = new StringBuffer("\n" + offset + " - Event: " + eventName); //$NON-NLS-1$//$NON-NLS-2$
		result.append(printEventStatProperties(offset));
		return result.toString();
	}

	/**
	 * Return the context containing the event.
	 * 
	 * @return string
	 */
	public String getContextName() {
		return contextName;
	}

	private String getConvertedSecondes(long runningTime) {
		String result = ""; //$NON-NLS-1$
		double time = ((double) runningTime - (double) (runningTime % 1000)) / 1000;
		double reste = (double) runningTime % 1000;
		result = (long) time + " s"; //$NON-NLS-1$
		if (reste > 0) {
			result += ":" + (long) reste + " ms"; //$NON-NLS-1$//$NON-NLS-2$
		}
		return result;
	}

	private String getConvertedRunningTime() {
		String result = "";//$NON-NLS-1$
		if (runningTime >= 60000) {
			double time = ((double) runningTime - (double) (runningTime % 60000)) / 60000;
			double reste = runningTime % 60000;
			result = (long) time + " min"; //$NON-NLS-1$
			if (reste > 0) {
				result += ":" + getConvertedSecondes((long) reste); //$NON-NLS-1$

			}
		} else if (runningTime >= 1000) {

			result = getConvertedSecondes(runningTime);

		} else {
			if (runningTime > 0) {
				result = runningTime + " ms"; //$NON-NLS-1$
			} else {

				result = nanoRunningTime + " ns"; //$NON-NLS-1$
			}
		}
		return "[ " + result + " ]"; //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Provide a String representing event's properties.
	 * 
	 * @param offset
	 *            A string placed at the beginning of each line.
	 * @return String.
	 */
	public String printEventStatProperties() {
		return printEventStatProperties(""); //$NON-NLS-1$
	}

	/**
	 * Provide a String representing event's properties.Each line will start with offset.
	 * 
	 * @param offset
	 *            A string placed at the beginning of each line.
	 * @return String.
	 */
	public String printEventStatProperties(String offset) {
		StringBuffer result = new StringBuffer("\n" + offset + " - Blame object: " + blameObject); //$NON-NLS-1$ //$NON-NLS-2$
		result.append("\n" + offset + " - CPU running time: " + getConvertedRunningTime()); //$NON-NLS-1$//$NON-NLS-2$
		if (runCount > 1) {
			result.append("\n" + offset + " - Run count: " + runCount); //$NON-NLS-1$//$NON-NLS-2$
		}
		return result.toString();
	}
}
