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
package org.eclipse.sphinx.tests.platform.perfs;

import java.io.File;
import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.platform.perfs.Measurement;
import org.eclipse.sphinx.platform.perfs.util.LogUtil;
import org.eclipse.sphinx.platform.perfs.util.PerfModelUtil;
import org.junit.Test;

public class PerfStatsTestCase extends TestCase {

	public static final String PERF_STATS_FILE_PATH = "model/PerformanceStats.xmi"; //$NON-NLS-1$

	@Test
	public void testPerfStats() throws CoreException {
		// Load performance stats model resource
		File perfsModel = PerfModelUtil.getPerfStatsModelFile(Activator.getPlugin().getBundle(), PERF_STATS_FILE_PATH);
		assertNotNull(perfsModel);
		assertTrue(perfsModel.exists());

		// Create application data
		PerfStatsExample application = new PerfStatsExample(perfsModel);
		Collection<Measurement> measurements = application.getPerfMeasurements();

		Resource resource = application.getPerfsModelResource();
		assertNotNull(resource);

		PerfModelUtil.updatePerfsModel(resource, measurements);

		// Save resulting statistics model
		try {
			resource.save(null);
		} catch (Exception ex) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getPlugin().getSymbolicName(), ex.getMessage(), ex));
		}

		// Log performance statistics
		LogUtil.log(resource);
	}
}
