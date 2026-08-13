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

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.platform.perfs.Measurement;
import org.eclipse.sphinx.platform.perfs.PerformanceStats;
import org.eclipse.sphinx.platform.perfs.PerfsFactory;
import org.eclipse.sphinx.platform.perfs.internal.Activator;
import org.osgi.framework.Bundle;

public final class PerfModelUtil {

	// Prevent instantiation for singleton pattern
	private PerfModelUtil() {
	}

	/**
	 * Removes obsolete measurements from performance model and adds given measurements instead.
	 */
	public static void updatePerfsModel(Resource resource, Collection<Measurement> measurements) {
		Assert.isNotNull(resource);
		Assert.isNotNull(measurements);

		// Retrieve performance stats from given resource
		PerformanceStats performanceStats = null;
		for (EObject rootObject : resource.getContents()) {
			if (rootObject instanceof PerformanceStats) {
				performanceStats = (PerformanceStats) rootObject;
				break;
			}
		}

		// Create new performance stats if no such was existing so far
		if (performanceStats == null) {
			performanceStats = PerfsFactory.eINSTANCE.createPerformanceStats();
			resource.getContents().add(performanceStats);
		}

		// Remove obsolete measurements
		for (Iterator<Measurement> iter = performanceStats.getMeasurements().iterator(); iter.hasNext();) {
			Measurement measurement = iter.next();
			for (Measurement currentMeasurement : measurements) {
				if (measurement.getName() != null && measurement.getName().equals(currentMeasurement.getName())) {
					iter.remove();
				}
			}
		}

		// Add provided measurements
		performanceStats.getMeasurements().addAll(measurements);
	}

	public static File getPerfStatsModelFile(Bundle bundle, String filePath) throws CoreException {
		Assert.isNotNull(bundle);

		try {
			URL url = FileLocator.find(bundle, new Path(filePath), null);
			// Use file scheme
			url = FileLocator.toFileURL(url);
			String path = url.getPath();
			String os = Platform.getOS();
			if (os.contains("win")) { //$NON-NLS-1$
				// Replace all white spaces in the path by "%20"
				path = path.replaceAll("\\s", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return new File(new URL(url.getProtocol(), null, path).toURI());
		} catch (Exception ex) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getPlugin().getSymbolicName(), ex.getMessage(), ex));
		}
	}
}
