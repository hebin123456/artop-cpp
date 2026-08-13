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
package org.eclipse.sphinx.tests.emf.workspace.integration.domain.mapping;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.workspace.domain.WorkspaceEditingDomainManager;
import org.eclipse.sphinx.emf.workspace.domain.mapping.DefaultWorkspaceEditingDomainMapping;
import org.eclipse.sphinx.emf.workspace.domain.mapping.IWorkspaceEditingDomainMapping;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

@SuppressWarnings({ "nls" })
public class DefaultWorkspaceEditingDomainMappingTest extends DefaultIntegrationTestCase {

	private IWorkspaceEditingDomainMapping editingDomainMapping;
	private String idEditingDomain20 = "editingDomainFor_org.eclipse.sphinx.examples.hummingbird20";
	private String idEditingDomain10 = "editingDomainFor_org.eclipse.sphinx.examples.hummingbird10";
	private String idEditingDomainUml2 = "editingDomainFor_org.eclipse.sphinx.examples.uml2";

	public DefaultWorkspaceEditingDomainMappingTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
	}

	@Override
	protected void setUp() throws Exception {
		// Create and register mapping to test
		super.setUp();
		editingDomainMapping = WorkspaceEditingDomainManager.INSTANCE.getEditingDomainMapping();
	}

	/**
	 * Test method for
	 * {@link DefaultWorkspaceEditingDomainMapping#getEditingDomain(org.eclipse.core.resources.IContainer, org.eclipse.sphinx.emf.metamodel.MetaModelDescriptor)}
	 * 
	 * @throws Exception
	 */
	public void testGetEditingDomainFromContainerAndMetaModelDescriptor() throws Exception {
		IFile file20 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile file10 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1);
		IFile fileUml2 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);

		List<TransactionalEditingDomain> editingDomains = editingDomainMapping.getEditingDomains();
		assertEquals(3, editingDomains.size());

		// Expected Editing Domain
		TransactionalEditingDomain exEditingDomain10 = editingDomainMapping.getEditingDomain(file10);
		TransactionalEditingDomain exEditingDomain20 = editingDomainMapping.getEditingDomain(file20);
		TransactionalEditingDomain exEditingDomainUml2 = editingDomainMapping.getEditingDomain(fileUml2);
		assertNotNull(exEditingDomain10);
		assertNotNull(exEditingDomain20);
		assertNotNull(exEditingDomainUml2);
		// Given Container is Project
		TransactionalEditingDomain acEditingDomain10 = editingDomainMapping.getEditingDomain(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E), Hummingbird10MMDescriptor.INSTANCE);
		TransactionalEditingDomain acEditingDomain20 = editingDomainMapping.getEditingDomain(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D), Hummingbird20MMDescriptor.INSTANCE);
		TransactionalEditingDomain acEditingDomainUml2 = editingDomainMapping.getEditingDomain(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D), UML2MMDescriptor.INSTANCE);

		assertEquals(exEditingDomain10, acEditingDomain10);
		assertEquals(exEditingDomain20, acEditingDomain20);
		assertEquals(exEditingDomainUml2, acEditingDomainUml2);
		// Given container is Folder
		acEditingDomain20 = editingDomainMapping.getEditingDomain(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E),
				Hummingbird20MMDescriptor.INSTANCE);
		assertNotSame(exEditingDomain10, acEditingDomain20);
		IProject hbProject10_F = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F);
		assertNotNull(hbProject10_F);
		IFolder testFolder = hbProject10_F.getFolder(DefaultTestReferenceWorkspace.HB_FOLDER_NAME_10_10F_1);
		assertNotNull(testFolder);

		TransactionalEditingDomain acEditingDomainForFolder10 = editingDomainMapping.getEditingDomain(testFolder, Hummingbird10MMDescriptor.INSTANCE);
		assertSame(exEditingDomain10, acEditingDomainForFolder10);

		// Given Container is Workspace Root.
		IWorkspaceRoot WorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		TransactionalEditingDomain acEditingDomainForWkspace10 = editingDomainMapping.getEditingDomain(WorkspaceRoot,
				Hummingbird10MMDescriptor.INSTANCE);
		assertSame(exEditingDomain10, acEditingDomainForWkspace10);
		TransactionalEditingDomain acEditingDomainForWkspace20 = editingDomainMapping.getEditingDomain(WorkspaceRoot,
				Hummingbird20MMDescriptor.INSTANCE);
		assertSame(exEditingDomain20, acEditingDomainForWkspace20);
		TransactionalEditingDomain acEditingDomainForWkspaceUml2 = editingDomainMapping.getEditingDomain(WorkspaceRoot, UML2MMDescriptor.INSTANCE);
		assertSame(exEditingDomainUml2, acEditingDomainForWkspaceUml2);

		// Given Container is null
		IFolder nullFolder = hbProject10_F.getFolder("test");
		assertFalse(nullFolder.isAccessible());
		TransactionalEditingDomain acEditingDomainForNullContainer10 = editingDomainMapping.getEditingDomain(nullFolder,
				Hummingbird10MMDescriptor.INSTANCE);
		assertSame(exEditingDomain10, acEditingDomainForNullContainer10);
	}

	/**
	 * Test method for {@link DefaultWorkspaceEditingDomainMapping#getEditingDomain(IFile)}
	 */
	public void testGetEditingDomainFromFile() throws Exception {
		IFile file20 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile file10 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1);
		IFile fileUml2 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		IFile nullFile = null;

		List<TransactionalEditingDomain> editingDomains = editingDomainMapping.getEditingDomains();
		assertEquals(3, editingDomains.size());

		TransactionalEditingDomain editingDomainFile20 = editingDomainMapping.getEditingDomain(file20);
		assertNotNull(editingDomainFile20);
		assertEquals(idEditingDomain20, editingDomainFile20.getID());

		TransactionalEditingDomain editingDomainFile10 = editingDomainMapping.getEditingDomain(file10);
		assertNotNull(editingDomainFile10);
		assertEquals(idEditingDomain10, editingDomainFile10.getID());
		assertNotSame(editingDomainFile20, editingDomainFile10);

		TransactionalEditingDomain editingDomainfileUml2 = editingDomainMapping.getEditingDomain(fileUml2);
		assertNotNull(editingDomainfileUml2);
		assertEquals(idEditingDomainUml2, editingDomainfileUml2.getID());
		assertNotSame(editingDomainFile10, editingDomainfileUml2);
		assertNotSame(editingDomainFile20, editingDomainfileUml2);

		assertNull(editingDomainMapping.getEditingDomain(nullFile));
	}

	/**
	 * Test method for {@link DefaultWorkspaceEditingDomainMapping#getEditingDomains(IProject, boolean)}
	 * 
	 * @throws Exception
	 */
	public void testGetEditingDomainFromProject() throws Exception {
		int resource10InHBProject10E = refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E,
				Hummingbird10MMDescriptor.INSTANCE).size();

		int resource10InHBProject10F = refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F,
				Hummingbird10MMDescriptor.INSTANCE).size();

		int resource20InHBProject20D = refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				Hummingbird20MMDescriptor.INSTANCE).size();
		int resourceUml2InHBProject20D = refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE)
				.size();

		int resource20InHBProject20C = refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C,
				Hummingbird20MMDescriptor.INSTANCE).size();

		List<TransactionalEditingDomain> editingDomains = editingDomainMapping.getEditingDomains();
		assertEquals(3, editingDomains.size());
		// ===========================
		// HB20
		// HbProject20D
		editingDomains = editingDomainMapping.getEditingDomains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D));
		// There is one HB20 Model in project
		assertEquals(3, editingDomains.size());
		TransactionalEditingDomain retrievedEditingDomain20 = null;
		TransactionalEditingDomain retrievedEditingDomain10 = null;
		TransactionalEditingDomain retrievedEditingDomainUml2 = null;
		for (TransactionalEditingDomain editingDomain : editingDomains) {
			if (idEditingDomain20.equals(editingDomain.getID())) {
				retrievedEditingDomain20 = editingDomain;
			} else if (idEditingDomain10.equals(editingDomain.getID())) {
				retrievedEditingDomain10 = editingDomain;
			} else if (idEditingDomainUml2.equals(editingDomain.getID())) {
				retrievedEditingDomainUml2 = editingDomain;
			}

		}
		assertNotNull(retrievedEditingDomain20);
		assertNotNull(retrievedEditingDomain10);
		assertNotNull(retrievedEditingDomainUml2);
		assertEditingDomainResourcesSizeEquals(retrievedEditingDomain20, resource20InHBProject20D + resource20InHBProject20C);
		assertEditingDomainResourcesSizeEquals(retrievedEditingDomain10, resource10InHBProject10E + resource10InHBProject10F);
		assertEditingDomainResourcesSizeEquals(retrievedEditingDomainUml2, resourceUml2InHBProject20D);
		// ----------------
		// HbProject20C
		editingDomains = editingDomainMapping.getEditingDomains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C));
		assertEquals(1, editingDomains.size());
		assertTrue(idEditingDomain20.equals(editingDomains.get(0).getID()));
		assertEditingDomainResourcesSizeEquals(editingDomains.get(0), resource20InHBProject20D + resource20InHBProject20C);
		// ==================================
		// HB10
		// HbProject10E
		editingDomains = editingDomainMapping.getEditingDomains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E));
		assertEquals(1, editingDomains.size());
		assertTrue(idEditingDomain10.equals(editingDomains.get(0).getID()));
		assertEditingDomainResourcesSizeEquals(editingDomains.get(0), resource10InHBProject10E + resource10InHBProject10F);

	}

}
