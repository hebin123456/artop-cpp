/**
 * <copyright>
 *
 * Copyright (c) 2016 itemis and others.
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
package org.eclipse.sphinx.tests.emf.workspace.referentialintegrity.scenarios;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Factory;
import org.eclipse.sphinx.examples.hummingbird20.common.Description;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue;

public class Hummingbird20TestModel {

	public ResourceSet resourceSet = new ResourceSetImpl();

	public Resource resource1 = new ResourceImpl();
	public Application application1 = InstanceModel20Factory.eINSTANCE.createApplication();
	public Description description1 = Common20Factory.eINSTANCE.createDescription();
	public Component component11 = InstanceModel20Factory.eINSTANCE.createComponent();
	public Connection component11ToComponent22Connection = InstanceModel20Factory.eINSTANCE.createConnection();
	public ParameterValue parameterValue111 = InstanceModel20Factory.eINSTANCE.createParameterValue();
	public ParameterValue parameterValue112 = InstanceModel20Factory.eINSTANCE.createParameterValue();
	public Component component12 = InstanceModel20Factory.eINSTANCE.createComponent();

	public Resource resource2 = new ResourceImpl();
	public Application application2 = InstanceModel20Factory.eINSTANCE.createApplication();
	public Description description2 = Common20Factory.eINSTANCE.createDescription();
	public Component component21 = InstanceModel20Factory.eINSTANCE.createComponent();
	public Component component22 = InstanceModel20Factory.eINSTANCE.createComponent();
	public Connection component22ToComponent11Connection = InstanceModel20Factory.eINSTANCE.createConnection();
	public ParameterValue parameterValue221 = InstanceModel20Factory.eINSTANCE.createParameterValue();

	@SuppressWarnings("nls")
	public Hummingbird20TestModel() {
		resourceSet.getResources().add(resource1);
		resourceSet.getResources().add(resource2);

		resource1.setURI(URI.createURI("resource1", true));
		resource1.getContents().add(application1);

		application1.setName("application1");
		application1.setDescription(description1);
		application1.getComponents().add(component11);
		application1.getComponents().add(component12);

		component11.setName("component11");
		component11.getOutgoingConnections().add(component11ToComponent22Connection);
		component11.getParameterValues().add(parameterValue111);
		component11.getParameterValues().add(parameterValue112);

		component12.setName("component12");
		component11ToComponent22Connection.setName("component11ToComponent22Connection");
		parameterValue111.setName("parameterValue111");
		parameterValue112.setName("parameterValue112");

		resource2.setURI(URI.createURI("resource2", true));
		resource2.getContents().add(application2);

		application2.setName("application2");
		application2.setDescription(description2);
		application2.getComponents().add(component21);
		application2.getComponents().add(component22);

		component21.setName("component21");

		component22.setName("component22");
		component22.getOutgoingConnections().add(component22ToComponent11Connection);
		component22.getParameterValues().add(parameterValue221);

		component22ToComponent11Connection.setName("component22ToComponent11Connection");
		parameterValue221.setName("parameterValue221");
	}
}