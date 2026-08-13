/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
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
package org.eclipse.sphinx.tests.emf.serialization.monitor;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.sphinx.emf.serialization.XMLPersistenceMappingResource;
import org.eclipse.sphinx.emf.serialization.XMLPersistenceMappingResourceFactoryImpl;
import org.eclipse.sphinx.tests.emf.serialization.model.nodes.NodesPackage;
import org.eclipse.sphinx.tests.emf.serialization.util.LoadSaveUtil;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class ProgressMonitorTests {

	static final String INPUT_PATH = "org.eclipse.sphinx.tests.emf.serialization.progressmonitor/";
	int beginTaskCount = 0;
	int worked = 0;
	int doneCount = 0;
	int myTotalWork;

	@Before
	public void setUp() throws Exception {
		EPackage.Registry.INSTANCE.put(NodesPackage.eNS_URI, NodesPackage.eINSTANCE);
	}

	@Test
	public void testProgressMonitor() {
		String inputFileName = INPUT_PATH + "progressmonitor.xml";
		try {
			beginTaskCount = 0;
			doneCount = 0;
			worked = 0;

			IProgressMonitor monitor = new NullProgressMonitor() {
				@Override
				public void beginTask(String name, int totalWork) {
					super.beginTask(name, totalWork);
					beginTaskCount++;
					myTotalWork = totalWork;
				}

				@Override
				public void worked(int work) {
					super.worked(work);
					worked += work;
				}

				@Override
				public void done() {
					super.done();
					doneCount++;
				}
			};
			Map<String, Object> options = new HashMap<String, Object>();
			options.put(XMLPersistenceMappingResource.OPTION_PROGRESS_MONITOR, monitor);
			LoadSaveUtil.loadResource("resources/input/" + inputFileName, new XMLPersistenceMappingResourceFactoryImpl(), options);

			assertSame(1, beginTaskCount);
			assertSame(4, myTotalWork);
			assertSame(0, worked);
			assertSame(1, doneCount);
		} catch (Exception ex) {
			assertTrue(ex.getMessage(), false);
		}
	}
}
