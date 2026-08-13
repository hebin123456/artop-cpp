/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.ui.integration.util;

import java.util.Set;

import junit.framework.ComparisonFailure;

import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sphinx.emf.ui.util.EcoreUIUtil;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird10.Interface;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.eclipse.ui.IEditorDescriptor;

@SuppressWarnings("nls")
public class EcoreUIUtilTest extends DefaultIntegrationTestCase {

	public EcoreUIUtilTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
	}

	/**
	 * Test method for {@link EcoreEditorUtil#createURIEditorInput(Object object) } .
	 */
	public void testCreateURIEditorInput() {
		Resource resource20_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		Resource resource20_2 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);
		Resource resource20_3 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);
		assertNotNull(resource20_1);
		assertFalse(resource20_1.getContents().isEmpty());
		assertNotNull(resource20_2);
		assertFalse(resource20_2.getContents().isEmpty());
		assertNotNull(resource20_3);
		assertFalse(resource20_3.getContents().isEmpty());

		Resource resource10_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource resource10_2 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		Resource resource10_3 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, true), false);
		assertNotNull(resource10_1);
		assertFalse(resource10_1.getContents().isEmpty());
		assertNotNull(resource10_2);
		assertFalse(resource10_2.getContents().isEmpty());
		assertNotNull(resource10_3);
		assertFalse(resource10_3.getContents().isEmpty());

		EObject eobject = resource20_1.getContents().get(0);

		URIEditorInput editor = EcoreUIUtil.createURIEditorInput(eobject);
		assertNotNull(editor);
		assertEquals(EcoreUtil.getURI(eobject).toString(), editor.getURI().toString());
		assertEquals(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, editor.getName());

		eobject = resource20_2.getContents().get(0);

		editor = EcoreUIUtil.createURIEditorInput(eobject);
		assertNotNull(editor);
		assertEquals(EcoreUtil.getURI(eobject).toString(), editor.getURI().toString());
		assertEquals(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, editor.getName());

		eobject = resource20_3.getContents().get(0);

		editor = EcoreUIUtil.createURIEditorInput(eobject);
		assertNotNull(editor);
		assertEquals(EcoreUtil.getURI(eobject).toString(), editor.getURI().toString());
		assertEquals(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, editor.getName());

		eobject = resource10_1.getContents().get(0);

		editor = EcoreUIUtil.createURIEditorInput(eobject);
		assertNotNull(editor);
		assertEquals(EcoreUtil.getURI(eobject).toString(), editor.getURI().toString());
		assertEquals(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, editor.getName());

		eobject = resource10_2.getContents().get(0);

		editor = EcoreUIUtil.createURIEditorInput(eobject);
		assertNotNull(editor);
		assertEquals(EcoreUtil.getURI(eobject).toString(), editor.getURI().toString());
		assertEquals(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, editor.getName());

		eobject = resource10_3.getContents().get(0);

		editor = EcoreUIUtil.createURIEditorInput(eobject);
		assertNotNull(editor);
		assertEquals(EcoreUtil.getURI(eobject).toString(), editor.getURI().toString());
		assertEquals(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, editor.getName());
	}

	/**
	 * Test method for {@link EcoreEditorUtil#findMatchingEditorId(Object object) } .
	 */
	public void testGetDefaultEditorClass() {

		IEditorDescriptor editorDescriptor1 = EcoreUIUtil.getDefaultEditor(InstanceModel20Package.eINSTANCE.getComponent());
		assertNotNull(editorDescriptor1);
		assertEquals(editorDescriptor1.getId(), "org.eclipse.sphinx.tests.emf.ui.integration.util.editors.ComponentHb20TestEditor",
				editorDescriptor1.getId());

		IEditorDescriptor editorDescriptor2 = EcoreUIUtil.getDefaultEditor(TypeModel20Package.eINSTANCE.getComponentType());
		assertNotNull(editorDescriptor2);
		// The default editor provided by the tests and the one provide by the
		// Hummingbird example are both accepted.
		String actualId = editorDescriptor2.getId();
		if (actualId == null
				|| !(actualId.equals("org.eclipse.sphinx.tests.emf.ui.integration.util.editors.IdentifiableHb20TestEditor") || actualId
						.equals("org.eclipse.sphinx.examples.hummingbird.ide.ui.editors.hummingbird"))) {
			throw new ComparisonFailure(
					actualId,
					"org.eclipse.sphinx.tests.emf.ui.integration.util.editors.IdentifiableHb20TestEditor or org.eclipse.sphinx.examples.hummingbird.ide.ui.editors.hummingbird",
					actualId);
		}

		IEditorDescriptor editorDescriptor3 = EcoreUIUtil.getDefaultEditor(TypeModel20Package.eINSTANCE.getInterface());
		assertNotNull(editorDescriptor3);
		assertEquals(editorDescriptor3.getId(), "org.eclipse.sphinx.tests.emf.ui.integration.util.editors.InterfaceHb20TestEditor",
				editorDescriptor3.getId());

		IEditorDescriptor editorDescriptor4 = EcoreUIUtil.getDefaultEditor(Hummingbird10Package.eINSTANCE.getComponent());
		assertNotNull(editorDescriptor4);
		assertEquals(editorDescriptor4.getId(), "org.eclipse.sphinx.tests.emf.ui.integration.util.editors.ComponentHb10TestEditor",
				editorDescriptor4.getId());

		IEditorDescriptor editorDescriptor5 = EcoreUIUtil.getDefaultEditor(Hummingbird10Package.eINSTANCE.getInterface());
		assertNotNull(editorDescriptor5);
		assertEquals(editorDescriptor5.getId(), "org.eclipse.sphinx.tests.emf.ui.integration.util.editors.InterfaceHb10TestEditor",
				editorDescriptor5.getId());
	}

	public void testGetDefaultEditorObject() {

		Resource resource20 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		assertNotNull(resource20);
		assertFalse(resource20.getContents().isEmpty());
		Application hb20App = (Application) resource20.getContents().get(0);
		assertNotNull(hb20App);
		assertFalse(hb20App.getComponents().isEmpty());
		Component component = hb20App.getComponents().get(0);

		IEditorDescriptor editorDescriptor1 = EcoreUIUtil.getDefaultEditor(component);
		assertNotNull(editorDescriptor1);
		assertEquals(editorDescriptor1.getId(), "org.eclipse.sphinx.tests.emf.ui.integration.util.editors.ComponentHb20TestEditor",
				editorDescriptor1.getId());
		// ----------------------------
		resource20 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);
		assertNotNull(resource20);
		assertFalse(resource20.getContents().isEmpty());
		Platform hb20Platform = (Platform) resource20.getContents().get(0);
		assertNotNull(hb20Platform);
		assertFalse(hb20Platform.getComponentTypes().isEmpty());
		ComponentType componentType = hb20Platform.getComponentTypes().get(0);
		IEditorDescriptor editorDescriptor2 = EcoreUIUtil.getDefaultEditor(componentType);

		assertNotNull(editorDescriptor2);
		// The default editor provided by the tests and the one provide by the
		// Hummingbird example are both accepted.
		String actualId = editorDescriptor2.getId();
		if (actualId == null
				|| !(actualId.equals("org.eclipse.sphinx.tests.emf.ui.integration.util.editors.IdentifiableHb20TestEditor") || actualId
						.equals("org.eclipse.sphinx.examples.hummingbird.ide.ui.editors.hummingbird"))) {
			throw new ComparisonFailure(
					actualId,
					"org.eclipse.sphinx.tests.emf.ui.integration.util.editors.IdentifiableHb20TestEditor or org.eclipse.sphinx.examples.hummingbird.ide.ui.editors.hummingbird",
					actualId);
		}

		// ===================================
		// HB10 Resource
		Resource resource10 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		assertNotNull(resource10);
		assertFalse(resource10.getContents().isEmpty());
		org.eclipse.sphinx.examples.hummingbird10.Application hb10App = (org.eclipse.sphinx.examples.hummingbird10.Application) resource10
				.getContents().get(0);
		assertFalse(hb10App.getComponents().isEmpty());
		org.eclipse.sphinx.examples.hummingbird10.Component hb10Component = hb10App.getComponents().get(0);

		IEditorDescriptor editorDescriptor4 = EcoreUIUtil.getDefaultEditor(hb10Component);
		assertNotNull(editorDescriptor4);
		assertEquals(editorDescriptor4.getId(), "org.eclipse.sphinx.tests.emf.ui.integration.util.editors.ComponentHb10TestEditor",
				editorDescriptor4.getId());

		assertFalse(hb10App.getInterfaces().isEmpty());
		Interface hb10Interface = hb10App.getInterfaces().get(0);

		IEditorDescriptor editorDescriptor5 = EcoreUIUtil.getDefaultEditor(hb10Interface);
		assertNotNull(editorDescriptor5);
		assertEquals(editorDescriptor5.getId(), "org.eclipse.sphinx.tests.emf.ui.integration.util.editors.InterfaceHb10TestEditor",
				editorDescriptor5.getId());

	}
}
