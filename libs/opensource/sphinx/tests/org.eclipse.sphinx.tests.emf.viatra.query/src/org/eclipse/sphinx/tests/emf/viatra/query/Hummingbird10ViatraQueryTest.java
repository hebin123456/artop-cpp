/**
 * <copyright>
 *
 * Copyright (c) 2017 itemis and others.
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
package org.eclipse.sphinx.tests.emf.viatra.query;

import java.util.List;

import org.eclipse.sphinx.emf.query.IModelQueryService;
import org.eclipse.sphinx.examples.hummingbird10.Application;
import org.eclipse.sphinx.examples.hummingbird10.Component;
import org.eclipse.sphinx.examples.hummingbird10.Connection;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird10.Interface;
import org.eclipse.sphinx.examples.hummingbird10.Parameter;
import org.eclipse.sphinx.examples.hummingbird10.util.Hummingbird10ResourceFactoryImpl;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class Hummingbird10ViatraQueryTest extends AbstractViatraQueryTestCase {

	String HUMMINGBIRD_MODEL_FILE = "hb10.hummingbird";
	IModelQueryService modelQueryService;

	public Hummingbird10ViatraQueryTest() {
		super(new Hummingbird10ResourceFactoryImpl());
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		loadInputFile(HUMMINGBIRD_MODEL_FILE);
		modelQueryService = getModelQueryService(Hummingbird10MMDescriptor.INSTANCE);
		Assert.assertNotNull(modelQueryService);
	}

	@Test
	public void testAllInstancesofApplication() throws Exception {
		List<Application> allApplications = modelQueryService.getAllInstancesOf(getResourceSet().getResources().get(0), Application.class);
		Assert.assertEquals(1, allApplications.size());
	}

	@Test
	public void testAllInstancesofComponent() throws Exception {
		List<Component> allComponents = modelQueryService.getAllInstancesOf(getResourceSet().getResources().get(0), Component.class);
		Assert.assertEquals(7, allComponents.size());
	}

	@Test
	public void testAllInstancesofParameter() throws Exception {
		List<Parameter> allParameters = modelQueryService.getAllInstancesOf(getResourceSet().getResources().get(0), Parameter.class);
		Assert.assertEquals(8, allParameters.size());
	}

	@Test
	public void testAllInstancesofInterface() throws Exception {
		List<Interface> allInterfaces = modelQueryService.getAllInstancesOf(getResourceSet().getResources().get(0), Interface.class);
		Assert.assertEquals(4, allInterfaces.size());
	}

	@Test
	public void testAllInstancesofConnection() throws Exception {
		List<Connection> allConnections = modelQueryService.getAllInstancesOf(getResourceSet().getResources().get(0), Connection.class);
		Assert.assertEquals(7, allConnections.size());
	}
}
