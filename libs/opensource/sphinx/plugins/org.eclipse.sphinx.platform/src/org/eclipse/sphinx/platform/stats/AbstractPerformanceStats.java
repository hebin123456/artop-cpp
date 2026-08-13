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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.platform.internal.Activator;
import org.eclipse.sphinx.platform.internal.messages.Messages;
import org.eclipse.sphinx.platform.messages.PlatformMessages;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

// TODO Add JavaDoc comments
public abstract class AbstractPerformanceStats<T extends IEventTypeEnumerator> implements IPerformanceStats<T> {

	private Map<Integer, String> contextNames;
	private Map<Integer, long[]> contextUserTimes;
	private Map<Integer, String> eventNames;
	private Map<Integer, ArrayList<Integer>> contextArborescence;
	private Map<Integer, Map<Integer, Map<Integer, StatsEvent>>> eventStatsMap;
	private LinkedList<String> contextStackCall;
	private LinkedList<String> contextPrinted;

	private PlatformLogWriter outLog;
	private static final String PERF = "/perf"; //$NON-NLS-1$
	private static final String JUNIT_OPTIONS = "/jUnitOptions";//$NON-NLS-1$
	private static final String LOG_OPTIONS = "/logOptions";//$NON-NLS-1$
	private static final String EVENT_OPTIONS = "/eventOptions";//$NON-NLS-1$
	private static final String TIMEOUT_VALUE = "/timeOutValue";//$NON-NLS-1$
	private static final String PERF_ID = Activator.PLUGIN_ID + PERF;
	private static final String RESET_EVENT_BASE_ID = Activator.PLUGIN_ID + PERF + JUNIT_OPTIONS + "/keepEventInfoAfterLogging"; //$NON-NLS-1$
	private static final String LOG_IN_SEPARATE_LOG_FILE_ID = Activator.PLUGIN_ID + PERF + LOG_OPTIONS + "/logInSeparateLogFile"; //$NON-NLS-1$
	private static final String LOG_ERROR_ON_TIMEOUT_ID = Activator.PLUGIN_ID + PERF + LOG_OPTIONS + "/logErrorOnTimeout"; //$NON-NLS-1$
	private static final String LOG_WARNING_ON_NO_TIME_ID = Activator.PLUGIN_ID + PERF + LOG_OPTIONS + "/logWarningOnNoTime"; //$NON-NLS-1$
	private static final String CLEAR_LOG_FILES_ON_STARTUP_ID = Activator.PLUGIN_ID + PERF + LOG_OPTIONS + "/clearLogFilesOnStartup";//$NON-NLS-1$
	private static final String LOG_EVENTS_DETAILS = Activator.PLUGIN_ID + PERF + LOG_OPTIONS + "/logEventsDetails"; //$NON-NLS-1$
	private static boolean logEventDetails;
	private static boolean enabled;
	private static boolean resetEventBase;
	private static boolean logInSeparateLogFile;
	private static boolean logErrorOnTimeOut;
	private static boolean logWarningOnNoTime;
	private static boolean clearLogFilesOnStartup;
	public static String DEFAULT_CONTEXT = "Global"; //$NON-NLS-1$

	public AbstractPerformanceStats() {
		contextNames = new HashMap<Integer, String>();
		eventNames = new HashMap<Integer, String>();
		contextArborescence = new HashMap<Integer, ArrayList<Integer>>();
		eventStatsMap = new HashMap<Integer, Map<Integer, Map<Integer, StatsEvent>>>();
		contextStackCall = new LinkedList<String>();
		contextPrinted = new LinkedList<String>();
		contextUserTimes = new HashMap<Integer, long[]>();
		outLog = createLog(getClass().getSimpleName() + ".log"); //$NON-NLS-1$
	}

	static {
		/**  */
		enabled = getBooleanOption(PERF_ID, false);

		/**  */
		resetEventBase = getBooleanOption(RESET_EVENT_BASE_ID, false);

		/**  */
		logInSeparateLogFile = !getBooleanOption(LOG_IN_SEPARATE_LOG_FILE_ID, true);

		/**  */
		logErrorOnTimeOut = getBooleanOption(LOG_ERROR_ON_TIMEOUT_ID, true);

		/**  */
		logWarningOnNoTime = getBooleanOption(LOG_WARNING_ON_NO_TIME_ID, true);

		clearLogFilesOnStartup = getBooleanOption(CLEAR_LOG_FILES_ON_STARTUP_ID, true);

		logEventDetails = getBooleanOption(LOG_EVENTS_DETAILS, false);
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	private PlatformLogWriter createLog(String logOutFileName) {
		FrameworkLog perfLog = new PerformanceStatsLog(new File(logOutFileName));
		IPath logLocation = Platform.getLogFileLocation();
		logLocation = logLocation.removeLastSegments(1).append(logOutFileName);
		try {
			perfLog.setFile(logLocation.toFile(), false);
			if (perfLog.getFile() != null) {
				if (perfLog.getFile().exists() && clearLogFilesOnStartup) {
					perfLog.getFile().delete();
				}
			}
			perfLog.setFile(logLocation.toFile(), false);
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
		return new PlatformLogWriter(perfLog);
	}

	private void addEventName(String eventName) {
		if (!eventNames.containsKey(eventName.hashCode())) {
			eventNames.put(eventName.hashCode(), eventName);
		}
	}

	private String getEventName(Integer eventId) {
		if (eventNames.containsKey(eventId)) {
			return eventNames.get(eventId);
		} else {
			return "Not referenced Event"; //$NON-NLS-1$
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#isEventTriggered()
	 */
	@Override
	public boolean isEventTriggered() {
		return true;
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#openContextIfFirstOne(java.lang.String)
	 */
	@Override
	public boolean openContextIfFirstOne(String context) {
		if (contextStackCall.isEmpty()) {
			openContext(context);
			return true;
		}
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#openContext(java.lang.String, java.lang.Object)
	 */
	@Override
	public void openContext(String context, Object blame) {
		if (blame != null) {
			openContext(context + blame.toString());
		} else {
			openContext(context);
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#openContext(java.lang.String)
	 */
	@Override
	public void openContext(String context) {
		if (context != null) {
			if (isEnabled()) {
				if (contextStackCall != null) {
					if (contextStackCall.size() == 0) {
						addContextName(context);
						contextStackCall.addLast(context);
					} else {
						addChildContext(contextStackCall.getLast(), context);
						contextStackCall.addLast(context);
					}
					long[] userTimeForContext = contextUserTimes.get(context.hashCode());
					if (userTimeForContext != null) {
						if (userTimeForContext[0] <= 0) {
							userTimeForContext[0] = System.currentTimeMillis();
						}
					} else {
						if (userTimeForContext[0] <= 0) {
							userTimeForContext[0] = System.currentTimeMillis();
						}
					}
				}
			}
		} else {
			String arg = "context";//$NON-NLS-1$
			String msg = NLS.bind(PlatformMessages.error_mustNotBeNull, arg);
			PlatformLogUtil.logAsWarning(Activator.getDefault(), new IllegalArgumentException(msg));
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#closeAndLogContext(java.lang.String, java.lang.Object)
	 */
	@Override
	public void closeAndLogContext(String context, Object blame) {
		closeAndLogContext(context + blame.toString());
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#closeAndLogContext(java.lang.String)
	 */
	@Override
	public void closeAndLogContext(String context) {
		if (isEnabled()) {
			if (contextStackCall != null && !contextStackCall.isEmpty()) {
				if (context.equals(contextStackCall.getLast())) {
					if (context.equals(contextStackCall.getFirst())) {
						long[] userTimeForContext = contextUserTimes.get(context.hashCode());
						if (userTimeForContext != null) {
							userTimeForContext[0] = System.currentTimeMillis() - userTimeForContext[0];
						}
						if (getLongContextRunningTime(context.hashCode()) != 0) {
							logContext(context);
						}
						if (!resetEventBase) {
							removeContext(context);
						}
						contextStackCall.remove(context);
					} else {
						long[] userTimeForContext = contextUserTimes.get(context.hashCode());
						if (userTimeForContext != null) {
							userTimeForContext[0] = System.currentTimeMillis() - userTimeForContext[0];
						}
						contextStackCall.remove(context);
					}
				}
			}
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#closeAndLogCurrentContext()
	 */
	@Override
	public void closeAndLogCurrentContext() {
		if (isEnabled()) {
			if (contextStackCall != null && !contextStackCall.isEmpty()) {
				String context = contextStackCall.getLast();
				if (context != null) {
					closeAndLogContext(context);
				} else {
					closeAndLogContext(DEFAULT_CONTEXT);
				}
			}
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#closeAllParentContext()
	 */
	@Override
	public void closeAllParentContext() {
		if (isEnabled()) {
			while (!contextStackCall.isEmpty()) {
				closeAndLogCurrentContext();
			}
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#closeContext(java.lang.String)
	 */
	@Override
	public void closeContext(String context) {
		if (contextStackCall != null && !contextStackCall.isEmpty()) {
			if (context.equals(contextStackCall.getLast())) {
				contextStackCall.remove(context);
			}
		}
	}

	private boolean isParentContext(String contextTested, String SupposedParentContext) {
		if (contextTested != null && SupposedParentContext != null) {
			ArrayList<Integer> childs = getContextChilds(SupposedParentContext);
			if (childs != null) {
				if (childs.contains(contextTested.hashCode())) {
					return true;
				}
				for (Integer childId : childs) {
					if (isParentContext(contextTested, getContextName(childId))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private synchronized ArrayList<Integer> getContextChilds(String context) {
		return context != null ? getContextChilds(context.hashCode()) : null;
	}

	private synchronized ArrayList<Integer> getContextChilds(Integer context) {
		return contextArborescence != null ? contextArborescence.get(context) : null;
	}

	private boolean addContextName(String context) {
		if (context != null) {
			if (!contextNames.containsKey(context.hashCode())) {
				long[] temp = { 0 };
				contextUserTimes.put(context.hashCode(), temp);
				contextNames.put(context.hashCode(), context);
				return true;
			}
		}
		return false;
	}

	private String getContextName(Integer contextId) {
		return contextNames.get(contextId);
	}

	private boolean addChildContext(String parentContextName, String childContext) {
		if (parentContextName != null && childContext != null) {
			addContextName(parentContextName);
			addContextName(childContext);
			if (!parentContextName.equals(childContext) && contextArborescence != null) {
				ArrayList<Integer> contextChilds = contextArborescence.get(parentContextName.hashCode());
				if (contextChilds == null) {
					contextChilds = new ArrayList<Integer>();
					contextArborescence.put(parentContextName.hashCode(), contextChilds);
				}
				if (!isParentContext(parentContextName, childContext)) {
					if (!contextChilds.contains(childContext.hashCode())) {
						contextChilds.add(childContext.hashCode());
						return true;
					}
				}
			}
		}
		return false;
	}

	protected static boolean getBooleanOption(String optionName, boolean defaultValue) {
		String optionValue = Platform.getDebugOption(optionName);
		if (optionValue != null) {
			Boolean value = Boolean.parseBoolean(optionValue);
			return value.booleanValue();
		} else {
			return defaultValue;
		}
	}

	protected int getIntOption(String optionName, int defaultValue) {
		String optionValue = Platform.getDebugOption(optionName);
		if (optionValue != null) {
			int value = Integer.parseInt(optionValue);
			return value;
		} else {
			return defaultValue;
		}
	}

	private int getTimeOutForEvent(StatsEvent event) {
		return event == null ? -1 : getIntOption(getPerformanceStatsId() + "/" + event.getEvent() + TIMEOUT_VALUE, -1); //$NON-NLS-1$
	}

	protected abstract String getPluginId();

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#removeStat(T, java.lang.Object, java.lang.String)
	 */
	@Override
	public void removeStat(T enumerator, Object blameObject, String context) {
		if (enumerator != null && blameObject != null && context != null) {
			Map<Integer, StatsEvent> eventMap = getEventStats(enumerator, context);
			if (eventMap != null) {
				eventMap.remove(blameObject.hashCode());
			}
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#removeContext(java.lang.String)
	 */
	@Override
	public void removeContext(String context) {
		if (context != null && contextNames != null) {
			contextNames.remove(context.hashCode());
			removeStatsForContext(context);
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#removeStatsForContext(java.lang.String)
	 */
	@Override
	public void removeStatsForContext(String context) {
		if (context != null && eventStatsMap != null) {
			Map<Integer, Map<Integer, StatsEvent>> mapEventStats = eventStatsMap.get(context.hashCode());
			if (mapEventStats != null) {
				mapEventStats.clear();
				if (!contextArborescence.isEmpty() && contextArborescence.containsKey(context.hashCode())) {
					ArrayList<Integer> childList = contextArborescence.get(context.hashCode());
					if (childList != null) {
						for (Integer contextChildsToEraseID : childList) {
							removeStatsForContext(contextNames.get(contextChildsToEraseID));
							contextNames.remove(contextChildsToEraseID);
						}
						childList.clear();
					}
				}
			}
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#removeStatsForEvent(T, java.lang.String)
	 */
	@Override
	public void removeStatsForEvent(T enumerator, String context) {
		if (enumerator != null && eventStatsMap != null) {
			Map<Integer, Map<Integer, StatsEvent>> mapEventStats = eventStatsMap.get(context.hashCode());
			if (mapEventStats != null) {
				mapEventStats.remove(enumerator.getName().hashCode());
			}
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#removeAllStats()
	 */
	@Override
	public void removeAllStats() {
		if (eventStatsMap != null && eventStatsMap != null && contextArborescence != null) {
			eventStatsMap.clear();
			contextNames.clear();
			contextArborescence.clear();
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#isEventTimeFail(T, java.lang.Object)
	 */
	@Override
	public boolean isEventTimeFail(T enumerator, Object blameObject) {
		return isEventTimeFail(enumerator, blameObject, DEFAULT_CONTEXT);
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#isEventTimeFail(T, java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean isEventTimeFail(T enumerator, Object blameObject, String context) {
		if (blameObject != null && enumerator != null) {
			return isEventTimeFailure(getEventStat(enumerator, blameObject, context));
		} else {
			String arg = "";//$NON-NLS-1$
			if (blameObject == null) {
				arg = "blameObject"; //$NON-NLS-1$
			} else if (enumerator == null) {
				arg = "eventId"; //$NON-NLS-1$
			} else if (context == null) {
				arg = "context"; //$NON-NLS-1$
			}
			String msg = NLS.bind(PlatformMessages.error_mustNotBeNull, arg);
			PlatformLogUtil.logAsWarning(Activator.getDefault(), new IllegalArgumentException(msg));
			return false;
		}
	}

	private boolean isEventTimeFailure(StatsEvent event) {
		if (event != null) {
			int timeOut = getTimeOutForEvent(event);
			if (timeOut != -1) {
				return event.getRunningTime() >= timeOut;
			}
		}
		return false;
	}

	private String getPerformanceStatsId() {
		return getPluginId() + PERF + EVENT_OPTIONS + "/" + getClass().getSimpleName(); //$NON-NLS-1$
	}

	private boolean isEventTracingActive(String eventName) {
		return eventName != null ? getBooleanOption(getPerformanceStatsId() + "/" + eventName, false) : false; //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#startNewEvent(T, java.lang.Object)
	 */
	@Override
	public void startNewEvent(T enumerator, Object blameObject) {
		if (blameObject != null && enumerator != null) {
			if (contextStackCall != null) {
				if (contextStackCall.isEmpty()) {
					addContextName(DEFAULT_CONTEXT);
					startEvent(enumerator, blameObject, DEFAULT_CONTEXT, true);
				} else {
					startEvent(enumerator, blameObject, contextStackCall.getLast(), true);
				}
			} else {
				addContextName(DEFAULT_CONTEXT);
				startEvent(enumerator, blameObject, DEFAULT_CONTEXT, true);
			}
		} else {
			String arg = "";//$NON-NLS-1$
			if (blameObject == null) {
				arg = "blameObject"; //$NON-NLS-1$
			} else if (enumerator == null) {
				arg = "eventId"; //$NON-NLS-1$
			}
			String msg = NLS.bind(PlatformMessages.error_mustNotBeNull, arg);
			PlatformLogUtil.logAsWarning(Activator.getDefault(), new IllegalArgumentException(msg));
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#startEvent(T, java.lang.Object)
	 */
	@Override
	public void startEvent(T enumerator, Object blameObject) {
		if (blameObject != null && enumerator != null) {
			if (contextStackCall != null) {
				if (contextStackCall.isEmpty()) {
					addContextName(DEFAULT_CONTEXT);
					startEvent(enumerator, blameObject, DEFAULT_CONTEXT, false);
				} else {
					startEvent(enumerator, blameObject, contextStackCall.getLast(), false);
				}
			} else {
				addContextName(DEFAULT_CONTEXT);
				startEvent(enumerator, blameObject, DEFAULT_CONTEXT, false);
			}
		} else {
			String arg = "";//$NON-NLS-1$
			if (blameObject == null) {
				arg = "blameObject"; //$NON-NLS-1$
			} else if (enumerator == null) {
				arg = "eventId"; //$NON-NLS-1$
			}
			String msg = NLS.bind(PlatformMessages.error_mustNotBeNull, arg);
			PlatformLogUtil.logAsWarning(Activator.getDefault(), new IllegalArgumentException(msg));
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#startNewEvent(T, java.lang.Object, java.lang.String)
	 */
	@Override
	public void startNewEvent(T enumerator, Object blameObject, String context) {
		startEvent(enumerator, blameObject, context, true);
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#startEvent(T, java.lang.Object, java.lang.String)
	 */
	@Override
	public void startEvent(T enumerator, Object blameObject, String context) {
		startEvent(enumerator, blameObject, context, false);
	}

	private void startEvent(T enumerator, Object blameObject, String context, boolean reset) {
		long time = 0;
		if (isEnabled() && isEventTracingActive(enumerator.getName())) {
			StatsEvent eventStat = null;
			if (context != null) {
				eventStat = getEventStat(enumerator, blameObject, context);
			} else {
				eventStat = getEventStat(enumerator, blameObject, DEFAULT_CONTEXT);
			}
			if (eventStat == null) {
				eventStat = addEventStat(enumerator, blameObject, context);
			}
			time = getTime();
			eventStat.startRun(time, reset, context);
		}
	}

	/**
	 * Return the name of the current opened context.
	 * 
	 * @return
	 */
	private String getLastContext() {
		if (contextStackCall != null && !contextStackCall.isEmpty()) {
			return contextStackCall.getLast();
		}
		return DEFAULT_CONTEXT;
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#endEvent(T, java.lang.Object)
	 */
	@Override
	public void endEvent(T enumerator, Object blameObject) {
		endEvent(enumerator, blameObject, getLastContext());
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#endEvent(T, java.lang.Object, java.lang.String)
	 */
	@Override
	public void endEvent(T enumerator, Object blameObject, String context) {
		long time = getTime();
		if (blameObject != null && enumerator != null && context != null) {
			if (isEnabled() && isEventTracingActive(enumerator.getName())) {
				// String context = returnCurrentContext(contextPath);
				StatsEvent eventStat = getEventStat(enumerator, blameObject, context);
				if (eventStat != null) {
					eventStat.endRun(time);
					logEvent(eventStat);
				}
			}
		} else {
			String arg = "";//$NON-NLS-1$
			if (blameObject == null) {
				arg = "blameObject"; //$NON-NLS-1$

			} else if (enumerator == null) {
				arg = "eventId"; //$NON-NLS-1$
			} else if (context == null) {
				arg = "contextId"; //$NON-NLS-1$
			}
			String msg = NLS.bind(PlatformMessages.error_mustNotBeNull, arg);
			PlatformLogUtil.logAsWarning(Activator.getDefault(), new IllegalArgumentException(msg));
		}
	}

	private StatsEvent addEventStat(T enumerator, Object blameObject, String context) {
		Map<Integer, Map<Integer, StatsEvent>> statsForContext = eventStatsMap.get(context.hashCode());
		if (statsForContext == null) {
			eventStatsMap.put(context.hashCode(), new HashMap<Integer, Map<Integer, StatsEvent>>());
			statsForContext = eventStatsMap.get(context.hashCode());
		}
		Map<Integer, StatsEvent> statsForEvent = statsForContext.get(enumerator.getName().hashCode());
		if (statsForEvent == null) {
			statsForContext.put(enumerator.getName().hashCode(), new HashMap<Integer, StatsEvent>());
			statsForEvent = statsForContext.get(enumerator.getName().hashCode());
			addEventName(enumerator.getName());
		}
		StatsEvent eventStat = statsForEvent.get(blameObject.hashCode());
		if (eventStat == null) {
			eventStat = new StatsEvent(enumerator.getName(), blameObject);
			statsForEvent.put(blameObject.hashCode(), eventStat);
		}
		return eventStat;
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#getEventStat(T, java.lang.Object, java.lang.String)
	 */
	@Override
	public StatsEvent getEventStat(T enumerator, Object blameObject, String context) {
		if (blameObject != null && enumerator != null && context != null) {
			Map<Integer, StatsEvent> statsForEvent = getEventStats(enumerator, context);
			if (statsForEvent != null) {
				StatsEvent stats = statsForEvent.get(blameObject.hashCode());
				return stats;
			} else {
				return null;
			}
		} else {
			String arg = "";//$NON-NLS-1$
			if (blameObject == null) {
				arg = "blameObject"; //$NON-NLS-1$

			} else if (enumerator == null) {
				arg = "eventId"; //$NON-NLS-1$
			}
			String msg = NLS.bind(PlatformMessages.error_mustNotBeNull, arg);
			PlatformLogUtil.logAsWarning(Activator.getDefault(), new IllegalArgumentException(msg));
			return null;
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#getEventStats(T, java.lang.String)
	 */
	@Override
	public Map<Integer, StatsEvent> getEventStats(T enumerator, String context) {
		if (enumerator != null && context != null) {
			return getEventStats(enumerator.getName().hashCode(), context.hashCode());
		} else {
			String arg = "";//$NON-NLS-1$
			if (enumerator == null) {
				arg = "eventId"; //$NON-NLS-1$
			} else if (context == null) {
				arg = "context"; //$NON-NLS-1$
			}
			String msg = NLS.bind(PlatformMessages.error_mustNotBeNull, arg);
			PlatformLogUtil.logAsWarning(Activator.getDefault(), new IllegalArgumentException(msg));
			return null;
		}
	}

	private Map<Integer, StatsEvent> getEventStats(Integer EventTypeId, Integer context) {
		Map<Integer, Map<Integer, StatsEvent>> statsForContext = getContextStats(context);
		if (statsForContext != null) {
			return statsForContext.get(EventTypeId);
		} else {
			return null;
		}
	}

	private Map<Integer, Map<Integer, StatsEvent>> getContextStats(Integer context) {
		return eventStatsMap.get(context);
	}

	// TODO Create and use NLS messages.
	private String getStringContextRunningTime(Integer contextId) {
		long[] userTimeForContext = contextUserTimes.get(contextId);
		String message = ""; //$NON-NLS-1$
		if (userTimeForContext != null) {
			message = NLS.bind(Messages.perfLog_$0$1runningTimeAndUserRunningTime, getConvertedRunningTime(getLongContextRunningTime(contextId)),
					getConvertedRunningTime(userTimeForContext[0]));
			userTimeForContext[0] = 0;
		} else {
			message = NLS.bind(Messages.perfLog_$0runningTime, getConvertedRunningTime(getLongContextRunningTime(contextId)));
		}
		return message;
	}

	private long getLongContextEventsRunningTime(Integer context, Integer eventId) {
		long result = 0;
		Map<Integer, Map<Integer, StatsEvent>> statsForContext = getContextStats(context);
		if (statsForContext != null) {
			Map<Integer, StatsEvent> statsforEvent = statsForContext.get(eventId);
			if (statsforEvent != null) {
				for (Integer statId : statsforEvent.keySet()) {
					result += statsforEvent.get(statId).getRunningTime();
				}
			}
		}
		return result;
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#getContextNames()
	 */
	@Override
	public Collection<String> getContextNames() {
		if (contextNames != null) {
			return contextNames.values();
		} else {
			return null;
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#contextRunningTimeExceedTimeOut(java.lang.String, long)
	 */
	@Override
	public boolean contextRunningTimeExceedTimeOut(String context, long timeOut) {
		if (context != null) {
			return getContextRunningTime(context) >= timeOut;
		} else {
			return false;
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#isRegisteredContext(java.lang.String)
	 */
	@Override
	public boolean isRegisteredContext(String context) {
		if (contextNames != null) {
			return contextNames.containsKey(context.hashCode());
		} else {
			return false;
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#getContextRunningTime(java.lang.String)
	 */
	@Override
	public long getContextRunningTime(String context) {
		long result = 0;
		if (contextNames.containsKey(context.hashCode())) {
			result = getLongContextRunningTime(context.hashCode());
		} else {
			result = -1;
		}
		return result;
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#getTimeFailureEventsInContext(java.lang.String)
	 */
	@Override
	public List<StatsEvent> getTimeFailureEventsInContext(String context) {
		if (isRegisteredContext(context) && context != null) {
			return getTimeFailureEventsInContext(context.hashCode());
		} else {
			return null;
		}
	}

	private LinkedList<StatsEvent> getTimeFailureEventsInContext(Integer context) {
		LinkedList<StatsEvent> result = new LinkedList<StatsEvent>();
		Map<Integer, Map<Integer, StatsEvent>> statsForContext = getContextStats(context);
		if (statsForContext != null) {
			for (Integer eventId : statsForContext.keySet()) {
				Map<Integer, StatsEvent> statsforEvent = statsForContext.get(eventId);
				if (statsforEvent != null) {
					for (Integer statId : statsforEvent.keySet()) {
						if (isEventTimeFailure(statsforEvent.get(statId))) {
							result.addLast(statsforEvent.get(statId));
						}
					}
				}
			}
		}
		ArrayList<Integer> contextChilds = getContextChilds(getContextName(context));
		if (contextChilds != null) {
			for (Integer childiD : contextChilds) {
				result.addAll(getTimeFailureEventsInContext(childiD));
			}
		}
		return result;
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#isEventsTimeFailure()
	 */
	@Override
	public boolean isEventsTimeFailure() {
		boolean result = false;
		for (Integer contextId : contextNames.keySet()) {
			result = result || isContextEventsTimeFailure(contextId) > 0;
		}
		return result;
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#numberOfEventsTimeFailure(java.lang.String)
	 */
	@Override
	public int numberOfEventsTimeFailure(String context) {
		if (context != null) {
			return getTimeFailureEventsInContext(context).size();
		} else {
			return 0;
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#getEventsTimeFailure()
	 */
	@Override
	public List<StatsEvent> getEventsTimeFailure() {
		LinkedList<StatsEvent> result = null;
		if (isEnabled() && eventStatsMap != null) {
			result = new LinkedList<StatsEvent>();
			for (Integer contextid : eventStatsMap.keySet()) {
				if (eventStatsMap.get(contextid) != null) {
					for (Integer eventTypeid : eventStatsMap.get(contextid).keySet()) {
						if (eventStatsMap.get(contextid).get(eventTypeid) != null) {
							for (Integer blameId : eventStatsMap.get(contextid).get(eventTypeid).keySet()) {
								if (isEventTimeFailure(eventStatsMap.get(contextid).get(eventTypeid).get(blameId))) {
									result.addLast(eventStatsMap.get(contextid).get(eventTypeid).get(blameId));
								}
							}
						}
					}
				}
			}
		}
		return result;
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#isContextEventsTimeFailure(java.lang.String)
	 */
	@Override
	public boolean isContextEventsTimeFailure(String context) {
		if (isRegisteredContext(context)) {
			return isContextEventsTimeFailure(context.hashCode()) > 0;
		} else {
			return false;
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#numberOfContextEventsTimeFailure(java.lang.String)
	 */
	@Override
	public int numberOfContextEventsTimeFailure(String context) {
		if (isRegisteredContext(context)) {
			return isContextEventsTimeFailure(context.hashCode());
		} else {
			return 0;
		}
	}

	private int isContextEventsTimeFailure(Integer context) {
		int result = 0;
		Map<Integer, Map<Integer, StatsEvent>> statsForContext = getContextStats(context);
		if (statsForContext != null) {
			for (Integer eventId : statsForContext.keySet()) {
				Map<Integer, StatsEvent> statsforEvent = statsForContext.get(eventId);
				if (statsforEvent != null) {
					for (Integer statId : statsforEvent.keySet()) {
						if (isEventTimeFailure(statsforEvent.get(statId))) {
							result++;
						}
					}
				}
			}
		}
		ArrayList<Integer> contextChilds = getContextChilds(getContextName(context));
		if (contextChilds != null) {
			for (Integer childiD : contextChilds) {
				result += isContextEventsTimeFailure(childiD);
			}
		}
		return result;
	}

	private long getLongContextRunningTime(Integer context) {
		long result = 0;
		Map<Integer, Map<Integer, StatsEvent>> statsForContext = getContextStats(context);
		if (statsForContext != null) {
			for (Integer eventId : statsForContext.keySet()) {
				Map<Integer, StatsEvent> statsforEvent = statsForContext.get(eventId);
				if (statsforEvent != null) {
					for (Integer statId : statsforEvent.keySet()) {
						result += statsforEvent.get(statId).getRunningTime();
					}
				}
			}
		}
		ArrayList<Integer> contextChilds = getContextChilds(getContextName(context));
		if (contextChilds != null) {
			for (Integer childiD : contextChilds) {
				result += getLongContextRunningTime(childiD);
			}
		}
		return result;
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#printContextTree(java.lang.String)
	 */
	@Override
	public String printContextTree(String context) {
		return printContextTree(context.hashCode());
	}

	private String printContextTree(Integer contextId) {
		if (isEnabled()) {
			StringBuffer result = new StringBuffer();
			ArrayList<Integer> Childlist = contextArborescence.get(contextId);
			if (Childlist != null) {
				for (Integer childId : Childlist) {
					result.append("\n Child: " + getContextName(childId));//$NON-NLS-1$
					result.append("\n -> " + printContextTree(childId)); //$NON-NLS-1$
				}
			}
			return result.toString();
		}
		return ""; //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#printContextsTree()
	 */
	@Override
	public String printContextsTree() {
		if (isEnabled() && contextArborescence != null) {
			StringBuffer result = new StringBuffer();
			for (Integer contextId : contextArborescence.keySet()) {
				result.append("\n\nContext: " + getContextName(contextId) + " Running time: " //$NON-NLS-1$ //$NON-NLS-2$
						+ getConvertedRunningTime(getLongContextRunningTime(contextId)));
				result.append("\n -> " + printContextTree(contextId)); //$NON-NLS-1$
			}
			return result.toString();
		}
		return ""; //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#printAllStats()
	 */
	@Override
	public String printAllStats() {
		contextPrinted.clear();
		if (isEnabled() && contextNames != null) {
			StringBuffer result = new StringBuffer();
			String offset = "|";//$NON-NLS-1$
			for (Integer contextId : contextNames.keySet()) {
				if (!contextPrinted.contains(getContextName(contextId))) {
					result.append("\n-------------------------------------------------------" + printContextStats(getContextName(contextId), offset)//$NON-NLS-1$
							+ "\n-------------------------------------------------------"); //$NON-NLS-1$
				}
			}
			return result.toString();
		} else {
			return Messages.perfLog_performanceStatsNotActivated;
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#printContextStats(java.lang.String)
	 */
	@Override
	public String printContextStats(String context) {
		return printContextStats(context, ""); //$NON-NLS-1$
	}

	private String printContextStats(String context, String offset) {
		if (isEnabled() && context != null && eventStatsMap != null) {
			StringBuffer result = new StringBuffer("");//$NON-NLS-1$
			if (eventStatsMap.containsKey(context.hashCode())) {
				Map<Integer, Map<Integer, StatsEvent>> Statsmap = eventStatsMap.get(context.hashCode());
				if (Statsmap != null) {
					result.append("\n\n" + offset + " + " + "Context: " + getContextName(context.hashCode()) + " Running time: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							+ getConvertedRunningTime(getLongContextRunningTime(context.hashCode())));
					for (Integer eventId : Statsmap.keySet()) {
						result.append(printEventStats(eventId, context.hashCode(), offset));
					}
				}
				ArrayList<Integer> contextChilds = getContextChilds(context);
				if (contextChilds != null) {
					for (int i = 0; i < contextChilds.size(); i++) {
						result.append(printContextStats(getContextName(contextChilds.get(i)), offset + "  " + "|")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				contextPrinted.add(context);
			}
			return result.toString();
		} else {
			return Messages.perfLog_performanceStatsNotActivated;
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#printEventStat(T, java.lang.Object, java.lang.String)
	 */
	@Override
	public String printEventStat(T enumerator, Object blameObject, String context) {
		if (isEnabled() && enumerator != null && blameObject != null && context != null) {
			StatsEvent event = getEventStat(enumerator, blameObject, context);
			if (event != null) {
				return printEventStat(event);
			}
			return ""; //$NON-NLS-1$
		} else {
			if (isEnabled()) {
				return PlatformMessages.arg_mustNotBeNull;
			} else {
				return Messages.perfLog_performanceStatsNotActivated;
			}
		}
	}

	private String printEventStats(Integer EventId, Integer contextId, String offset) {
		Map<Integer, StatsEvent> eventMap = getEventStats(EventId, contextId);
		if (eventMap != null) {
			StringBuffer result = new StringBuffer();
			for (Integer eventBlameId : eventMap.keySet()) {
				result.append("\n" + printEventStat(eventMap.get(eventBlameId), offset)); //$NON-NLS-1$
			}
			return result.toString();
		}
		return ""; //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.sphinx.platform.stats.IPerformanceStats#printEventStats(T, java.lang.String)
	 */
	@Override
	public String printEventStats(T enumerator, String context) {
		if (isEnabled() && enumerator != null && context != null) {
			Map<Integer, StatsEvent> eventMap = getEventStats(enumerator, context);
			if (eventMap != null) {
				StringBuffer result = new StringBuffer("\n-Event Type: " + enumerator.getName());//$NON-NLS-1$ 
				for (Integer eventBlameId : eventMap.keySet()) {
					result.append("\n" + eventMap.get(eventBlameId).printEventStatProperties()); //$NON-NLS-1$
				}
				return result.toString();
			}
			return ""; //$NON-NLS-1$
		} else {
			return Messages.perfLog_performanceStatsNotActivated;
		}
	}

	private String printEventStat(StatsEvent event) {
		return event.printEventStat();
	}

	private String printEventStat(StatsEvent event, String offset) {
		return event.printEventStat(offset);
	}

	private long getTime() {
		long time = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		time = time / 1000000;
		return time;
	}

	private void logEvent(StatsEvent event) {
		if (event != null) {
			if (event.getContextName().equals(DEFAULT_CONTEXT)) {
				Status eventStatus = getEventStatus(event);
				if (eventStatus != null) {
					if (!logInSeparateLogFile) {
						if (outLog != null) {
							outLog.logging(eventStatus, Activator.PLUGIN_ID);
						}
					} else {
						Activator.getDefault().getLog().log(eventStatus);
					}
				}
			}
		}
	}

	private void logContext(String context) {
		if (!logInSeparateLogFile) {
			if (outLog != null) {
				outLog.logging(getContextInfoStatus(context.hashCode()), Activator.PLUGIN_ID);
			}
		} else {
			Activator.getDefault().getLog().log(getContextInfoStatus(context.hashCode()));
		}
	}

	private MultiStatus getContextEventsInfoStatus(Integer contextId, Integer eventId) {
		int count = 0;
		Map<Integer, Map<Integer, StatsEvent>> contextStatstemp = getContextStats(contextId);
		if (contextStatstemp != null) {
			Map<Integer, StatsEvent> eventsMap = contextStatstemp.get(eventId);
			if (eventsMap != null) {
				count = eventsMap.size();
			}
		}
		int status = IStatus.INFO;
		MultiStatus result = new MultiStatus(Activator.PLUGIN_ID, status, NLS.bind(Messages.perfLog_$0$1$2contextInfos, new String[] { "" + count, //$NON-NLS-1$
				getEventName(eventId), getConvertedRunningTime(getLongContextEventsRunningTime(contextId, eventId)) }), new RuntimeException());
		if (contextId != null && logEventDetails) {
			Map<Integer, Map<Integer, StatsEvent>> contextStats = getContextStats(contextId);
			if (contextStats != null) {
				Map<Integer, StatsEvent> eventsMap = contextStats.get(eventId);
				if (eventsMap != null) {
					for (Integer statId : eventsMap.keySet()) {
						StatsEvent statsEvent = eventsMap.get(statId);
						if (statsEvent != null) {
							Status eventStatus = getEventStatus(statsEvent);
							if (eventStatus != null) {
								encloseStatus(result, eventStatus);
							}
						}
					}
				}
			}
		}
		return result;
	}

	private Status getEventStatus(StatsEvent event) {
		if (isEnabled()) {
			if (event != null) {
				if (logErrorOnTimeOut && isEventTimeFailure(event)) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind(Messages.perfLog_$0runningTimeExceedTimeout, event.toString()),
							new RuntimeException());
				} else if (logWarningOnNoTime && event.getRunningTime() == 0) {
					return new Status(IStatus.WARNING, Activator.PLUGIN_ID, NLS.bind(Messages.perfLog_$0runningTimeZero, event.toString()),
							new RuntimeException());
				} else {
					return null;
				}
			}
		}
		return null;
	}

	private MultiStatus getContextInfoStatus(Integer contextId) {
		String contextName = getContextName(contextId);
		if (contextId == DEFAULT_CONTEXT.hashCode()) {
			contextName = DEFAULT_CONTEXT;
		} else if (contextName == null) {
			contextName = "_" + contextId + "_"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		String msg = contextName + " - " + getStringContextRunningTime(contextId); //$NON-NLS-1$
		MultiStatus result = new MultiStatus(Activator.PLUGIN_ID, IStatus.INFO, msg, new RuntimeException());
		if (contextId != null) {
			Map<Integer, Map<Integer, StatsEvent>> contextStats = getContextStats(contextId);
			if (contextStats != null) {
				for (Integer eventId : contextStats.keySet()) {
					encloseStatus(result, getContextEventsInfoStatus(contextId, eventId));
				}
			}
			ArrayList<Integer> contextChilds = this.getContextChilds(contextId);
			if (contextChilds != null) {
				for (Integer childId : contextChilds) {
					encloseStatus(result, getContextInfoStatus(childId));
				}
			}
		}
		return result;
	}

	private static void encloseStatus(MultiStatus parentIStatus, Status childIStatus) {
		parentIStatus.add(childIStatus);
	}

	private static void encloseStatus(MultiStatus parentIStatus, MultiStatus childIStatus) {
		parentIStatus.add(childIStatus);
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

	private String getConvertedRunningTime(long runningTime) {
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
			result = runningTime + " ms"; //$NON-NLS-1$
		}
		return "[ " + result + " ]"; //$NON-NLS-1$//$NON-NLS-2$
	}
}
