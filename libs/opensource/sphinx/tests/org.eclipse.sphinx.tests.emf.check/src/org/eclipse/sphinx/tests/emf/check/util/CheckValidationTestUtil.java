/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.tests.emf.check.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;

public class CheckValidationTestUtil {

	public static List<Diagnostic> findDiagnositcsWithMsg(List<Diagnostic> diagnostics, String issueMsg) {
		List<Diagnostic> result = new ArrayList<Diagnostic>();
		for (Diagnostic diag : diagnostics) {
			if (diag.getMessage().contains(issueMsg)) {
				result.add(diag);
			}
		}
		return result;
	}

	public static Application createApplication(String appName) {
		Application app = InstanceModel20Factory.eINSTANCE.createApplication();
		app.setName(appName);
		return app;
	}

	public static Interface createInterface(String interfaceName) {
		Interface interfacce = TypeModel20Factory.eINSTANCE.createInterface();
		interfacce.setName(interfaceName);
		return interfacce;
	}

	public static Component createComponent(String componentName) {
		Component component = InstanceModel20Factory.eINSTANCE.createComponent();
		component.setName(componentName);
		return component;
	}

	public static Connection createConnection(String connectionName) {
		Connection connection = InstanceModel20Factory.eINSTANCE.createConnection();
		connection.setName(connectionName);
		return connection;
	}
}
