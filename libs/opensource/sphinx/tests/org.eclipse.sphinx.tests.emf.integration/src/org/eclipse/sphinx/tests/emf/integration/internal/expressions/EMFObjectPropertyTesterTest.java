/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *     itemis - [447193] Enable transient item providers to be created through adapter factories
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.integration.internal.expressions;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.emf.edit.provider.WrapperItemProvider;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.internal.expressions.EMFObjectPropertyTester;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

@SuppressWarnings({ "nls", "restriction" })
public class EMFObjectPropertyTesterTest extends DefaultIntegrationTestCase {

	private static final String VALUE_CLASS_NAME_MATCHES = "valueClassNameMatches";
	private static final String PARENT_CLASS_NAME_MATCHES = "parentClassNameMatches";

	public EMFObjectPropertyTesterTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
	}

	public void testParentClassNameMatchesTest() {
		IFile hbFile20_20A_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertNotNull(hbFile20_20A_1);
		assertTrue(hbFile20_20A_1.isAccessible());

		Resource hb20Resource = EcorePlatformUtil.getResource(hbFile20_20A_1);
		assertFalse(hb20Resource.getContents().isEmpty());
		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application hb20Application = (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application) hb20Resource
				.getContents().get(0);
		assertNotNull(hb20Application);

		assertFalse(hb20Application.getComponents().isEmpty());
		Component component = hb20Application.getComponents().get(0);
		assertFalse(component.getParameterValues().isEmpty());

		IWrapperItemProvider wrapperItemProvider = new WrapperItemProvider(component, hb20Application,
				InstanceModel20Package.eINSTANCE.getApplication_Components(), 1,
				((AdapterFactoryEditingDomain) WorkspaceEditingDomainUtil.getEditingDomain(hbFile20_20A_1)).getAdapterFactory());

		TransientItemProvider transientItemProvider = new TransientItemProvider(
				((AdapterFactoryEditingDomain) WorkspaceEditingDomainUtil.getEditingDomain(hbFile20_20A_1)).getAdapterFactory());
		transientItemProvider.setTarget(component);

		EMFObjectPropertyTester emfObjectProTester = new EMFObjectPropertyTester();
		String property = PARENT_CLASS_NAME_MATCHES;
		Object[] args = new Object[] {};

		Object receiver = wrapperItemProvider;
		assertFalse(emfObjectProTester.test(receiver, property, args, hb20Application.getClass().getName()));
		assertFalse(emfObjectProTester.test(receiver, property, args, component.getClass().getName()));

		receiver = transientItemProvider;
		assertFalse(emfObjectProTester.test(receiver, property, args, hb20Application.getClass().getName()));
		assertTrue(emfObjectProTester.test(receiver, property, args, component.getClass().getName()));
	}

	public void testValueClassNameMatchesTest() {
		IFile hbFile20_20A_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertNotNull(hbFile20_20A_1);
		assertTrue(hbFile20_20A_1.isAccessible());

		Resource hb20Resource = EcorePlatformUtil.getResource(hbFile20_20A_1);
		assertFalse(hb20Resource.getContents().isEmpty());
		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application hb20Application = (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application) hb20Resource
				.getContents().get(0);
		assertNotNull(hb20Application);

		assertFalse(hb20Application.getComponents().isEmpty());
		Component component = hb20Application.getComponents().get(0);
		assertFalse(component.getParameterValues().isEmpty());
		ParameterValue param = component.getParameterValues().get(0);

		IWrapperItemProvider wrapperItemProvider1 = new WrapperItemProvider(component, hb20Application,
				InstanceModel20Package.eINSTANCE.getApplication_Components(), 1,
				((AdapterFactoryEditingDomain) WorkspaceEditingDomainUtil.getEditingDomain(hbFile20_20A_1)).getAdapterFactory());

		IWrapperItemProvider wrapperItemProvider2 = new WrapperItemProvider(param, wrapperItemProvider1,
				InstanceModel20Package.eINSTANCE.getComponent_ParameterValues(), 1,
				((AdapterFactoryEditingDomain) WorkspaceEditingDomainUtil.getEditingDomain(hbFile20_20A_1)).getAdapterFactory());

		IWrapperItemProvider wrapperItemProvider22 = new WrapperItemProvider(param, component,
				InstanceModel20Package.eINSTANCE.getComponent_ParameterValues(), 1,
				((AdapterFactoryEditingDomain) WorkspaceEditingDomainUtil.getEditingDomain(hbFile20_20A_1)).getAdapterFactory());

		TransientItemProvider transientItemProvider = new TransientItemProvider(
				((AdapterFactoryEditingDomain) WorkspaceEditingDomainUtil.getEditingDomain(hbFile20_20A_1)).getAdapterFactory());
		transientItemProvider.setTarget(component);

		IWrapperItemProvider wrapperItemProvider3 = new WrapperItemProvider(param, transientItemProvider,
				InstanceModel20Package.eINSTANCE.getComponent_ParameterValues(), 1,
				((AdapterFactoryEditingDomain) WorkspaceEditingDomainUtil.getEditingDomain(hbFile20_20A_1)).getAdapterFactory());

		EMFObjectPropertyTester emfObjectProTester = new EMFObjectPropertyTester();
		String property = VALUE_CLASS_NAME_MATCHES;
		Object[] args = new Object[] {};

		Object receiver = wrapperItemProvider1;
		assertTrue(emfObjectProTester.test(receiver, property, args, component.getClass().getName()));
		assertFalse(emfObjectProTester.test(receiver, property, args, hb20Application.getClass().getName()));

		receiver = wrapperItemProvider2;
		assertTrue(emfObjectProTester.test(receiver, property, args, param.getClass().getName()));
		assertFalse(emfObjectProTester.test(receiver, property, args, component.getClass().getName()));

		receiver = wrapperItemProvider22;
		assertTrue(emfObjectProTester.test(receiver, property, args, param.getClass().getName()));
		assertFalse(emfObjectProTester.test(receiver, property, args, component.getClass().getName()));

		receiver = wrapperItemProvider3;
		assertTrue(emfObjectProTester.test(receiver, property, args, param.getClass().getName()));
		assertFalse(emfObjectProTester.test(receiver, property, args, component.getClass().getName()));

	}
}
