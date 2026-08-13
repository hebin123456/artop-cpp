/**
 * <copyright>
 * 
 * Copyright (c) 2012 itemis and others.
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
package org.eclipse.sphinx.platform.perfs.util;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.platform.perfs.Measurement;
import org.eclipse.sphinx.platform.perfs.PerformanceStats;
import org.eclipse.sphinx.platform.perfs.internal.Activator;
import org.eclipse.sphinx.platform.perfs.internal.messages.Messages;

public final class LogUtil {

	// Prevent instantiation for singleton pattern
	private LogUtil() {
	}

	/**
	 * Logs the given status. The status is distributed to the log listeners installed on this log and then to the log
	 * listeners installed on the platform.
	 * 
	 * @param status
	 *            the status to log.
	 */
	public static void log(IStatus status) {
		Activator.getPlugin().getLog().log(status);
	}

	/**
	 * Adds the given child status to the provided multi status.
	 * 
	 * @param status
	 *            a multi status.
	 * @param childStatus
	 *            the child status to be add.
	 */
	public static void addStatus(MultiStatus status, IStatus childStatus) {
		if (status != null && childStatus != null) {
			status.add(childStatus);
		}
	}

	/**
	 * Logs the given performance statistics, i.e., all contained measurements. The status is distributed to the log
	 * listeners installed on this log and then to the log listeners installed on the platform.
	 * 
	 * @param status
	 *            the multi status to be used.
	 * @param perfStats
	 *            performance statistics.
	 */
	public static void log(MultiStatus status, PerformanceStats perfStats) {
		if (status != null && perfStats != null) {
			log(status, perfStats.getMeasurements());
		}
	}

	/**
	 * Logs the given performance measurements. The status is distributed to the log listeners installed on this log and
	 * then to the log listeners installed on the platform.
	 * 
	 * @param status
	 *            the multi status to be used.
	 * @param measurements
	 *            a set of performance measurements.
	 */
	public static void log(MultiStatus status, Collection<Measurement> measurements) {
		if (status != null && measurements != null) {
			for (Measurement measurement : measurements) {
				Status measurementStatus = null;
				if (measurement.getChildren().isEmpty()) {
					measurementStatus = new Status(IStatus.INFO, Activator.getPlugin().getSymbolicName(), getLogMesage(measurement));
					addStatus(status, measurementStatus);
				} else {
					measurementStatus = new MultiStatus(Activator.getPlugin().getSymbolicName(), IStatus.INFO, getLogMesage(measurement), null);
					addStatus(status, measurementStatus);
					// Log child measurements as children
					log((MultiStatus) measurementStatus, measurement.getChildren());
				}
			}

			// Log the result multi status
			log(status);
		}
	}

	public static void log(Resource resource) {
		Assert.isNotNull(resource);

		// Retrieve performance stats from given resource
		PerformanceStats performanceStats = null;
		for (EObject rootObject : resource.getContents()) {
			if (rootObject instanceof PerformanceStats) {
				performanceStats = (PerformanceStats) rootObject;
				break;
			}
		}

		if (performanceStats != null) {
			MultiStatus status = new MultiStatus(Activator.getPlugin().getSymbolicName(), IStatus.INFO, "Performance Statistics", null); //$NON-NLS-1$
			EList<Measurement> measurements = performanceStats.getMeasurements();
			if (!measurements.isEmpty()) {
				LogUtil.log(status, measurements);
			}
		}
	}

	private static String getLogMesage(Measurement measurement) {
		Assert.isNotNull(measurement);
		return NLS.bind(Messages.msg_PeformanceStatistics, measurement.getName(), measurement.getTotal() / (long) 1e6);
	}
}
