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
package org.eclipse.sphinx.tests.emf.workspace.integration.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.domain.IContainerEditingDomainProvider;
import org.eclipse.sphinx.emf.saving.IResourceSaveIndicator;
import org.eclipse.sphinx.emf.saving.SaveIndicatorUtil;
import org.eclipse.sphinx.emf.workspace.internal.EditingDomainAdapterFactory;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

@SuppressWarnings({ "restriction" })
public class EditingDomainAdapterFactoryTest extends DefaultIntegrationTestCase {

	public EditingDomainAdapterFactoryTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
	}

	static final EditingDomainAdapterFactory factory = new EditingDomainAdapterFactory();

	public void testGetAdapterList() {
		List<Class<?>> ex = new ArrayList<Class<?>>();
		ex.add(IContainerEditingDomainProvider.class);
		ex.add(IEditingDomainProvider.class);
		ex.add(IResourceSaveIndicator.class);

		Class<?>[] res = factory.getAdapterList();

		assertEquals(ex.size(), res.length);
		for (Class<?> re : res) {
			assertTrue(ex.contains(re));
		}

	}

	public void testGetAdapter() {
		// ############### Context Object : IFile ###############################

		// +++++++++++++++++ Hummingbird 10 file ++++++++++++++++++++++++++++++++++++
		IFile file10Project10C_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1);
		assertNotNull(file10Project10C_1);

		// ------------------ Adapter Type is IEditingDomainProvider --------------------
		Object resObject = factory.getAdapter(file10Project10C_1, IEditingDomainProvider.class);
		assertTrue(resObject instanceof IEditingDomainProvider);
		IEditingDomainProvider editingDomainProvider = (IEditingDomainProvider) resObject;
		assertSame(refWks.editingDomain10, editingDomainProvider.getEditingDomain());

		// ------------------ Adapter Type is IContainerEditingDomainProvider------------
		resObject = factory.getAdapter(file10Project10C_1, IContainerEditingDomainProvider.class);
		assertNull(resObject);

		// ------------------ Adapter Type is IResourceSaveIndicator ------------
		resObject = factory.getAdapter(file10Project10C_1, IResourceSaveIndicator.class);
		assertNull(resObject);

		// +++++++++++++++++ Hummingbird 20 file ++++++++++++++++++++++++++++++++++++
		IFile file10Project20B_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertNotNull(file10Project20B_1);

		// ------------------ Adapter Type is IEditingDomainProvider --------------------
		resObject = factory.getAdapter(file10Project20B_1, IEditingDomainProvider.class);
		assertTrue(resObject instanceof IEditingDomainProvider);
		editingDomainProvider = (IEditingDomainProvider) resObject;
		assertSame(refWks.editingDomain20, editingDomainProvider.getEditingDomain());

		// ------------------ Adapter Type is IContainerEditingDomainProvider------------
		resObject = factory.getAdapter(file10Project20B_1, IContainerEditingDomainProvider.class);
		assertNull(resObject);

		// ------------------ Adapter Type is IResourceSaveIndicator ------------
		resObject = factory.getAdapter(file10Project20B_1, IResourceSaveIndicator.class);
		assertNull(resObject);

		// ############### Context Object: IFolder #################################
		IProject arProject10_F = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F);
		assertNotNull(arProject10_F);
		IFolder folderProject10F = arProject10_F.getFolder(DefaultTestReferenceWorkspace.HB_FOLDER_NAME_10_10F_1);
		assertNotNull(folderProject10F);

		// ------------------ Adapter Type is IEditingDomainProvider --------------------
		resObject = factory.getAdapter(folderProject10F, IEditingDomainProvider.class);
		assertNull(resObject);

		// ------------------ Adapter Type is IContainerEditingDomainProvider------------
		resObject = factory.getAdapter(folderProject10F, IContainerEditingDomainProvider.class);
		assertNotNull(resObject);
		assertTrue(resObject instanceof IContainerEditingDomainProvider);
		IContainerEditingDomainProvider iContainerEditingDomainProvider = (IContainerEditingDomainProvider) resObject;
		Collection<TransactionalEditingDomain> editingDomains = iContainerEditingDomainProvider.getEditingDomains();
		assertEquals(1, editingDomains.size());
		assertTrue(editingDomains.contains(refWks.editingDomain10));
		assertFalse(editingDomains.contains(refWks.editingDomain20));

		// ------------------ Adapter Type is IResourceSaveIndicator ------------
		resObject = factory.getAdapter(folderProject10F, IResourceSaveIndicator.class);
		assertNull(resObject);

		// ################# Context Object : IProject #################################

		// +++++++++++++++++ Hummingbird 10 project ++++++++++++++++++++++++++++++++++++++++

		// ------------------ Adapter Type is IEditingDomainProvider --------------------
		resObject = factory.getAdapter(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C), IEditingDomainProvider.class);
		assertNull(resObject);

		// ------------------ Adapter Type is IContainerEditingDomainProvider------------
		resObject = factory.getAdapter(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C),
				IContainerEditingDomainProvider.class);
		assertNotNull(resObject);
		assertTrue(resObject instanceof IContainerEditingDomainProvider);
		iContainerEditingDomainProvider = (IContainerEditingDomainProvider) resObject;
		editingDomains = iContainerEditingDomainProvider.getEditingDomains();
		assertEquals(2, editingDomains.size());
		assertTrue(editingDomains.contains(refWks.editingDomain10));
		assertFalse(editingDomains.contains(refWks.editingDomain20));
		assertTrue(editingDomains.contains(refWks.editingDomainUml2));

		// ------------------ Adapter Type is IResourceSaveIndicator ------------
		resObject = factory.getAdapter(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C), IResourceSaveIndicator.class);
		assertNull(resObject);

		// ++++++++++++++++++ Hummingbird 20 project +++++++++++++++++++++++++++++++++++++++

		// ------------------ Adapter Type is IEditingDomainProvider --------------------
		resObject = factory.getAdapter(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B), IEditingDomainProvider.class);
		assertNull(resObject);

		// ------------------ Adapter Type is IContainerEditingDomainProvider------------
		resObject = factory.getAdapter(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B),
				IContainerEditingDomainProvider.class);
		assertNotNull(resObject);
		assertTrue(resObject instanceof IContainerEditingDomainProvider);
		iContainerEditingDomainProvider = (IContainerEditingDomainProvider) resObject;
		editingDomains = iContainerEditingDomainProvider.getEditingDomains();
		assertEquals(2, editingDomains.size());
		assertFalse(editingDomains.contains(refWks.editingDomain10));
		assertTrue(editingDomains.contains(refWks.editingDomain20));
		assertTrue(editingDomains.contains(refWks.editingDomainUml2));
		// ------------------ Adapter Type is IResourceSaveIndicator ------------
		resObject = factory.getAdapter(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B), IResourceSaveIndicator.class);
		assertNull(resObject);

		// ################# Context Object: WorkspaceRoot #############################

		// ------------------ Adapter Type is IEditingDomainProvider --------------------
		resObject = factory.getAdapter(ResourcesPlugin.getWorkspace().getRoot(), IEditingDomainProvider.class);
		assertNull(resObject);

		// ------------------ Adapter Type is IContainerEditingDomainProvider------------
		resObject = factory.getAdapter(ResourcesPlugin.getWorkspace().getRoot(), IContainerEditingDomainProvider.class);
		assertNotNull(resObject);
		assertTrue(resObject instanceof IContainerEditingDomainProvider);
		iContainerEditingDomainProvider = (IContainerEditingDomainProvider) resObject;
		editingDomains = iContainerEditingDomainProvider.getEditingDomains();
		assertEquals(3, editingDomains.size());
		assertTrue(editingDomains.contains(refWks.editingDomain10));
		assertTrue(editingDomains.contains(refWks.editingDomain20));
		assertTrue(editingDomains.contains(refWks.editingDomainUml2));

		// ------------------- Adapter Type is IResourceSaveIndicator -------------------
		resObject = factory.getAdapter(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C), IResourceSaveIndicator.class);
		assertNull(resObject);

		// ################# Context Object: TransactionalEditingDomain ####################

		// ++++++++++++++++++ editingDomain Hummingbird 10 ++++++++++++++++++++++++++++++++++++

		// ------------------ Adapter Type is IEditingDomainProvider --------------------
		resObject = factory.getAdapter(refWks.editingDomain10, IEditingDomainProvider.class);
		assertNull(resObject);

		// ------------------ Adapter Type is IContainerEditingDomainProvider------------
		resObject = factory.getAdapter(refWks.editingDomain10, IContainerEditingDomainProvider.class);
		assertNull(resObject);

		// ------------------ Adapter Type is IResourceSaveIndicator ------------
		resObject = factory.getAdapter(refWks.editingDomain10, IResourceSaveIndicator.class);
		assertNotNull(resObject);
		assertTrue(resObject instanceof IResourceSaveIndicator);
		IResourceSaveIndicator iResourceSaveIndicator = (IResourceSaveIndicator) resObject;
		assertSame(SaveIndicatorUtil.getResourceSaveIndicator(refWks.editingDomain10), iResourceSaveIndicator);

		// ++++++++++++++++++ editingDomain Hummingbird 20 +++++++++++++++++++++++++++++++++

		// ------------------ Adapter Type is IEditingDomainProvider --------------------
		resObject = factory.getAdapter(refWks.editingDomain20, IEditingDomainProvider.class);
		assertNull(resObject);

		// ------------------ Adapter Type is IContainerEditingDomainProvider------------
		resObject = factory.getAdapter(refWks.editingDomain20, IContainerEditingDomainProvider.class);
		assertNull(resObject);

		// ------------------ Adapter Type is IResourceSaveIndicator ------------
		resObject = factory.getAdapter(refWks.editingDomain20, IResourceSaveIndicator.class);
		assertNotNull(resObject);
		assertTrue(resObject instanceof IResourceSaveIndicator);
		iResourceSaveIndicator = (IResourceSaveIndicator) resObject;
		assertSame(SaveIndicatorUtil.getResourceSaveIndicator(refWks.editingDomain20), iResourceSaveIndicator);

		// ++++++++++++++++++ editingDomain Uml2 +++++++++++++++++++++++++++++++++++++++

		// ------------------ Adapter Type is IEditingDomainProvider --------------------
		resObject = factory.getAdapter(refWks.editingDomainUml2, IEditingDomainProvider.class);
		assertNull(resObject);

		// ------------------ Adapter Type is IContainerEditingDomainProvider------------
		resObject = factory.getAdapter(refWks.editingDomainUml2, IContainerEditingDomainProvider.class);
		assertNull(resObject);

		// ------------------ Adapter Type is IResourceSaveIndicator ------------
		resObject = factory.getAdapter(refWks.editingDomainUml2, IResourceSaveIndicator.class);
		assertNotNull(resObject);
		assertTrue(resObject instanceof IResourceSaveIndicator);
		iResourceSaveIndicator = (IResourceSaveIndicator) resObject;
		assertSame(SaveIndicatorUtil.getResourceSaveIndicator(refWks.editingDomainUml2), iResourceSaveIndicator);

	}
}
