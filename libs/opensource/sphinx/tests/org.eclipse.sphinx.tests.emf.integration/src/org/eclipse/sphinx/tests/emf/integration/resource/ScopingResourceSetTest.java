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
 *     itemis - [409458] Enhance ScopingResourceSetImpl#getEObjectInScope() to enable cross-document references between model files with different metamodels
 *     itemis - [410825] Make sure that EcorePlatformUtil#getResourcesInModel(contextResource, includeReferencedModels) method return resources of the context resource in the same resource set
 *     itemis - [409510] Enable resource scope-sensitive proxy resolutions without forcing metamodel implementations to subclass EObjectImpl
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.integration.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSet;
import org.eclipse.sphinx.emf.resource.ScopingResourceSet;
import org.eclipse.sphinx.emf.resource.ScopingResourceSetImpl;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.examples.hummingbird10.Application;
import org.eclipse.sphinx.examples.hummingbird10.Component;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Factory;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird10.Parameter;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;

@SuppressWarnings("nls")
public class ScopingResourceSetTest extends DefaultIntegrationTestCase {
	List<String> hbProject10AResources10;
	int resources10FromHbProject10_A;

	List<String> hbProject10BResources10;
	int resources10FromHbProject10_B;

	List<String> hbProject10CResources10;
	int resources10FromHbProject10_C;
	List<String> hbProject10CResourcesUml2;
	int resourcesUml2FromHbProject10_C;

	List<String> hbProject10DResources10;
	int resources10FromHbProject10_D;

	List<String> hbProject10EResources10;
	int resources10FromHbProject10_E;

	List<String> hbProject20HBesources20;
	int resources20FromHbProject20_A;

	List<String> hbProject20BResources20;
	int resources20FromHbProject20_B;
	List<String> hbProject20BResourcesUml2;
	int resourcesUml2FromHbProject20_B;

	List<String> hbProject20CResources20;
	int resources20FromHbProject20_C;

	List<String> hbProject20DResources20;
	int resources20FromHbProject20_D;
	List<String> hbProject20DResourcesUml2;
	int resourcesUml2FromHbProject20_D;

	List<String> hbProject20EResources20;
	int resources20FromHbProject20_E;
	List<String> hbProject20EResourcesUml2;
	int resourcesUml2FromHbProject20_E;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		hbProject10AResources10 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, Hummingbird10MMDescriptor.INSTANCE);
		resources10FromHbProject10_A = hbProject10AResources10.size();

		hbProject10BResources10 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_B, Hummingbird10MMDescriptor.INSTANCE);
		resources10FromHbProject10_B = hbProject10BResources10.size();

		hbProject10CResources10 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C, Hummingbird10MMDescriptor.INSTANCE);
		resources10FromHbProject10_C = hbProject10CResources10.size();
		hbProject10CResourcesUml2 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C, UML2MMDescriptor.INSTANCE);
		resourcesUml2FromHbProject10_C = hbProject10CResourcesUml2.size();

		hbProject10DResources10 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D, Hummingbird10MMDescriptor.INSTANCE);
		resources10FromHbProject10_D = hbProject10DResources10.size();

		hbProject10EResources10 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E, Hummingbird10MMDescriptor.INSTANCE);
		resources10FromHbProject10_E = hbProject10EResources10.size();

		hbProject20HBesources20 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, Hummingbird20MMDescriptor.INSTANCE);
		resources20FromHbProject20_A = hbProject20HBesources20.size();

		hbProject20BResources20 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, Hummingbird20MMDescriptor.INSTANCE);
		resources20FromHbProject20_B = hbProject20BResources20.size();
		hbProject20BResourcesUml2 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, UML2MMDescriptor.INSTANCE);
		resourcesUml2FromHbProject20_B = hbProject20BResourcesUml2.size();

		hbProject20CResources20 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C, Hummingbird20MMDescriptor.INSTANCE);
		resources20FromHbProject20_C = hbProject20CResources20.size();

		hbProject20DResources20 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE);
		resources20FromHbProject20_D = hbProject20DResources20.size();
		hbProject20DResourcesUml2 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE);
		resourcesUml2FromHbProject20_D = hbProject20DResourcesUml2.size();

		hbProject20EResources20 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, Hummingbird20MMDescriptor.INSTANCE);
		resources20FromHbProject20_E = hbProject20EResources20.size();
		hbProject20EResourcesUml2 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, UML2MMDescriptor.INSTANCE);
		resourcesUml2FromHbProject20_E = hbProject20EResourcesUml2.size();

	}

	protected String[] getProjectToLoad() {
		return null;
	}

	protected ScopingResourceSet getScopingResourceSet(TransactionalEditingDomain editingDomain) {
		ResourceSet resourceSet = editingDomain.getResourceSet();
		assertTrue(resourceSet instanceof ScopingResourceSet);
		return (ScopingResourceSet) resourceSet;
	}

	//
	// This test class covers :
	// ignoreMetaModel is FALSE
	// 1. getResourceInScope(contextObject, true, false)
	// -----getResourceInModel(contextObject)
	// 2. getResourceInScope(contextObject, false, false)
	// -----getResourceInModel(contextObject, uncludeReferenceScope)
	// ignoreMetaModel is TRUE
	// 3. getResourceInScope(contextObject, true, true)
	// -----getResourceInScope(contextObject)
	// 4. getResourceInScope(contextObject, false, true)
	// -----getResourceInScope(contextObject, includeReferenceScope)
	// 5. getEObject(URI uri, loadOnDemand)
	// -----getEObjectInScope(uri, loadOnDemand, null)
	// 6. getEObjectInScope(URI uri, boolean loadOnDemand, EObject contextObject)
	// 7. getResourceInScope/InModel from object without underlying resource
	// 8. getResourceInScope/InModel from object without resourceSet
	// 9. getResourceInScope/InModel from object in resourceSet without EditingDomain

	// #############################################################################
	// ignoreMetaModel is FALSE
	// ========== 1 =====================
	/**
	 * Test method for {@link ScopingResourceSet#getResourcesInModel(Object, boolean))} with second input is TRUE
	 */
	/**
	 * Test method for:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link ScopingResourceSet#getResourcesInModel(Object)}<br>
	 * with a contextual object of type:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link IProject} (AUTOSHB project).
	 */
	public void testGetResourcesInModelFromProject() throws Exception {
		// Verify that loading has really been performed

		String message = "Context project: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10project without reference to other projects
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
			assertNotNull(contextProject);

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources10FromHbProject10_A, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(hbProject10AResources10.contains(resource.getURI().lastSegment()));
			}
		}
		// ----------------------------------------------------------------------
		// Context: HB10 project reference to another HB10 project and a HB20 Project
		{

			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources10FromHbProject10_E + resources10FromHbProject10_D,
					resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// ----------------------------------------------------------------------
		// Context: HB20 project contain HB20 and Uml Models
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
			assertNotNull(contextProject);
			assertTrue("Project :" + contextProject.getName() + " does not exist", refWks.hbProject20_B.exists()); //$NON-NLS-1$ //$NON-NLS-2$

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources20FromHbProject20_B, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resourcesUml2FromHbProject20_B, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(hbProject20BResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInModelUml2) {
				assertTrue(hbProject20BResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		// ----------------------------------------------------------------------------
		// Context: HB20 project contain HB20 and Uml Models
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C);
			assertNotNull(contextProject);

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources20FromHbProject20_C, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(hbProject20CResources20.contains(resource.getURI().lastSegment()));
			}
		}
		// Context: HB20 Project contains HB20 and Uml MOdels references to HB10 Project
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
			assertNotNull(contextProject);

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources20FromHbProject20_D, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resourcesUml2FromHbProject20_D, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInModelUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInModel10) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment())
						|| hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
	}

	/**
	 * Test method for:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link ScopingResourceSet#getResourcesInModel(Object)}<br>
	 * with a contextual object of type:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link IModelDescriptor} (HBHB20model descriptor).
	 */
	public void testGetResourcesInModelFromHummingbirdModelDescriptor() throws Exception {
		String message = "Context model descriptor: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: HB10 model descriptor doesn't have referenced scopes
		{
			IFile contextFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D).getFile(
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1);

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(modelDescriptor);
			assertEquals(NLS.bind(message, binding), resources10FromHbProject10_D, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 0, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 0, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB10 model descriptor has referenced scopes
		{
			IFile contextFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E).getFile(
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1);
			assertNotNull(contextFile);

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(modelDescriptor);
			assertEquals(NLS.bind(message, binding), resources10FromHbProject10_E + resources10FromHbProject10_D, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 0, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 0, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 model descriptor doesn't have referenced scopes and its project containers contains UML
		// models

		{
			IFile contextFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);

			IModelDescriptor contextHBModelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(contextHBModelDescriptor);

			String bindingHB = contextHBModelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextHBModelDescriptor);
			assertEquals(NLS.bind(message, bindingHB), resources20FromHbProject20_D, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextHBModelDescriptor);
			assertEquals(NLS.bind(message, bindingHB), 0, resourcesInModelUml2.size());

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextHBModelDescriptor);
			assertEquals(NLS.bind(message, bindingHB), 0, resourcesInModel10.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 model descriptor has referenced scopes and its project containers contains UML models
		{
			IFile contextFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);

			IModelDescriptor contextHBModelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(contextHBModelDescriptor);

			String bindingHB = contextHBModelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextHBModelDescriptor);
			assertEquals(NLS.bind(message, bindingHB), resources20FromHbProject20_E + resources20FromHbProject20_D, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextHBModelDescriptor);
			assertEquals(NLS.bind(message, bindingHB), 0, resourcesInModelUml2.size());

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextHBModelDescriptor);
			assertEquals(NLS.bind(message, bindingHB), 0, resourcesInModel10.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment())
						|| hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
		}

	}

	/**
	 * Test method for:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link ScopingResourceSet#getResourcesInModel(Object)}<br>
	 * with a contextual object of type:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link IFile} (HBHB20file).
	 */
	public void testGetResourcesInModelFromHummingbirdFile() throws Exception {
		String message = "Context file: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 file
		{
			// Get the context file to use for filtering
			IFile contextFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
			assertNotNull(contextFile);

			// Ask filtering
			List<Resource> resourceInModel10A = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_A, resourceInModel10A.size());

			// Verify that expected files have been filtered
			for (Resource resource : resourceInModel10A) {
				assertTrue(hbProject10AResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB10 file (case of referenced project)
		{
			// Get the context file to use for filtering
			IFile contextFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1);
			assertNotNull(contextFile);

			List<Resource> resourceInModel10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_D, resourceInModel10D.size());

			// Verify that expected files have been filtered
			for (Resource resource : resourceInModel10D) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		{
			// Get the context file to use for filtering
			IFile contextFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1);
			assertNotNull(contextFile);

			List<Resource> resourcesInModel10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_E + resources10FromHbProject10_D,
					resourcesInModel10E.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInModel20.size());

			for (Resource resource : resourcesInModel10E) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 file
		{
			IFile contextFile = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
			assertNotNull(contextFile);

			List<Resource> resourcesInModel20A = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources20FromHbProject20_A, resourcesInModel20A.size());

			for (Resource resource : resourcesInModel20A) {
				assertTrue(hbProject20HBesources20.contains(resource.getURI().lastSegment()));
			}
		}

		List<Data> dataList20C = new ArrayList<Data>();
		List<String> hbProject20_CFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C);
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20_CFileNames,
				resources20FromHbProject20_C));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20_CFileNames,
				resources20FromHbProject20_C));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20_CFileNames,
				resources20FromHbProject20_C));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_20C_1, hbProject20_CFileNames, 0));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_20C_2, hbProject20_CFileNames, 0));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_20C_3, hbProject20_CFileNames, 0));

		for (Data data : dataList20C) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInModel10.size());

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), expected, resourceInModel20.size());

			for (Resource resource : resourceInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 file (case of referenced project)
		List<Data> dataList20D = new ArrayList<Data>();
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, hbProject20DResources20,
				resources20FromHbProject20_D));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, hbProject20DResources20,
				resources20FromHbProject20_D));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, hbProject20DResources20,
				resources20FromHbProject20_D));
		for (Data data : dataList20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInModelUml2.size());

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), expected, resourcesInModel20.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}

		}
		List<Data> dataList20E = new ArrayList<Data>();
		List<String> resourcesInHbProject20_DE = new ArrayList<String>(hbProject20DResources20);
		resourcesInHbProject20_DE.addAll(hbProject20EResources20);

		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, resourcesInHbProject20_DE));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2, resourcesInHbProject20_DE));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3, resourcesInHbProject20_DE));
		for (Data data : dataList20E) {
			IProject project = data.project;
			assertNotNull(project);
			List<String> contents = data.contents;

			String fileName = data.fileName;
			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources20FromHbProject20_D + resources20FromHbProject20_E,
					resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInModelUml2.size());

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInModel10.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

	}

	/**
	 * Test method for:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link ScopingResourceSet#getResourcesInModel(Object)}<br>
	 * with a contextual object of type:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link Resource} (HBHB20resource).
	 */
	public void testGetResourcesInModelFromHummingbirdResource() throws Exception {
		String message = "Context resource: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 resource

		List<Data> dataList10ABC = new ArrayList<Data>();
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_2, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_3, hbProject10CResources10,
				resources10FromHbProject10_C));

		for (Data data : dataList10ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			// The resource used as context for filtering
			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInModel10.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB10 resource (case of referenced project)
		List<Data> dataList10D = new ArrayList<Data>();
		dataList10D.add(new Data(refWks.hbProject10_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1, hbProject10DResources10,
				resources10FromHbProject10_D));
		dataList10D.add(new Data(refWks.hbProject10_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2, hbProject10DResources10,
				resources10FromHbProject10_D));
		dataList10D.add(new Data(refWks.hbProject10_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3, hbProject10DResources10,
				resources10FromHbProject10_D));

		for (Data data : dataList10D) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			// The resource used as context for filtering
			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInModel10D.size());

			for (Resource resource : resourcesInModel10D) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		List<String> resourcesInHbProject10_DE = new ArrayList<String>(hbProject10DResources10);
		resourcesInHbProject10_DE.addAll(hbProject10EResources10);

		List<Data> dataList10E = new ArrayList<Data>();
		dataList10E.add(new Data(refWks.hbProject10_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, resourcesInHbProject10_DE));
		dataList10E.add(new Data(refWks.hbProject10_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2, resourcesInHbProject10_DE));
		dataList10E.add(new Data(refWks.hbProject10_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3, resourcesInHbProject10_DE));

		for (Data data : dataList10E) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			// The resource used as context for filtering
			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourcesInModel10E.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInModel20.size());

			for (Resource resource : resourcesInModel10E) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 resource

		List<Data> dataList20ABC = new ArrayList<Data>();
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20CResources20,
				resources20FromHbProject20_C));

		for (Data data : dataList20ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInModel20.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// Context: references to other projects

		List<Data> dataList20D = new ArrayList<Data>();
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, hbProject20DResources20));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, hbProject20DResources20));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, hbProject20DResources20));
		for (Data data : dataList20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel20D = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D, resourcesInModel20D.size());

			for (Resource resource : resourcesInModel20D) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		List<Data> dataList20E = new ArrayList<Data>();
		List<String> resourcesInHbProject20_DE = new ArrayList<String>(hbProject20DResources20);
		resourcesInHbProject20_DE.addAll(hbProject20EResources20);
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, resourcesInHbProject20_DE));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2, resourcesInHbProject20_DE));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3, resourcesInHbProject20_DE));

		for (Data data : dataList20E) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel20E = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D + resources20FromHbProject20_E,
					resourcesInModel20E.size());

			for (Resource resource : resourcesInModel20E) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
	}

	/**
	 * Test method for:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link ScopingResourceSet#getResourcesInModel(Object)}<br>
	 * with a contextual object of type:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link URI} (
	 */
	public void testGetResourcesInModelFromHummingbirdUri() throws Exception {
		String message = "Context resource: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 resource

		List<Data> dataList10ABC = new ArrayList<Data>();
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_2, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_3, hbProject10CResources10,
				resources10FromHbProject10_C));

		for (Data data : dataList10ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			// The resource used as context for filtering
			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInModel10.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB10 resource (case of referenced project)
		List<Data> dataList10D = new ArrayList<Data>();
		dataList10D.add(new Data(refWks.hbProject10_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1, hbProject10DResources10,
				resources10FromHbProject10_D));
		dataList10D.add(new Data(refWks.hbProject10_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2, hbProject10DResources10,
				resources10FromHbProject10_D));
		dataList10D.add(new Data(refWks.hbProject10_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3, hbProject10DResources10,
				resources10FromHbProject10_D));

		for (Data data : dataList10D) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			// The resource used as context for filtering
			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInModel10D.size());

			for (Resource resource : resourcesInModel10D) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		List<String> resourcesInHbProject10_DE = new ArrayList<String>(hbProject10DResources10);
		resourcesInHbProject10_DE.addAll(hbProject10EResources10);

		List<Data> dataList10E = new ArrayList<Data>();
		dataList10E.add(new Data(refWks.hbProject10_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, resourcesInHbProject10_DE));
		dataList10E.add(new Data(refWks.hbProject10_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2, resourcesInHbProject10_DE));
		dataList10E.add(new Data(refWks.hbProject10_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3, resourcesInHbProject10_DE));

		for (Data data : dataList10E) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			// The resource used as context for filtering
			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourcesInModel10E.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInModel20.size());

			for (Resource resource : resourcesInModel10E) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 resource

		List<Data> dataList20ABC = new ArrayList<Data>();
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20CResources20,
				resources20FromHbProject20_C));

		for (Data data : dataList20ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInModel20.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// Context: references to other projects

		List<Data> dataList20D = new ArrayList<Data>();
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, hbProject20DResources20));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, hbProject20DResources20));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, hbProject20DResources20));
		for (Data data : dataList20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel20D = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D, resourcesInModel20D.size());

			for (Resource resource : resourcesInModel20D) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		List<Data> dataList20E = new ArrayList<Data>();
		List<String> resourcesInHbProject20_DE = new ArrayList<String>(hbProject20DResources20);
		resourcesInHbProject20_DE.addAll(hbProject20EResources20);
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, resourcesInHbProject20_DE));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2, resourcesInHbProject20_DE));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3, resourcesInHbProject20_DE));

		for (Data data : dataList20E) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel20E = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D + resources20FromHbProject20_E,
					resourcesInModel20E.size());

			for (Resource resource : resourcesInModel20E) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
	}

	/**
	 * Test method for:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link ScopingResourceSet#getResourcesInModel(Object)}<br>
	 * with a contextual object of type:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link EObject} (HBHB20object).
	 */
	public void testGetResourcesInModelFromHummingbirdObject() throws Exception {
		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: HB10 resource

		List<Data> dataList10ABC = new ArrayList<Data>();
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_2, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_3, hbProject10CResources10,
				resources10FromHbProject10_C));

		for (Data data : dataList10ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			// The context object use for filtering
			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), expected, resourcesInModel10.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB10 resource (case of referenced project)
		List<Data> dataList10D = new ArrayList<Data>();
		dataList10D.add(new Data(refWks.hbProject10_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1, hbProject10DResources10,
				resources10FromHbProject10_D));
		dataList10D.add(new Data(refWks.hbProject10_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2, hbProject10DResources10,
				resources10FromHbProject10_D));
		dataList10D.add(new Data(refWks.hbProject10_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3, hbProject10DResources10,
				resources10FromHbProject10_D));
		for (Data data : dataList10ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), expected, resourcesInModel10D.size());

			for (Resource resource : resourcesInModel10D) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		List<Data> dataList10E = new ArrayList<Data>();

		List<String> resourcesInHbProject10_DE = new ArrayList<String>(hbProject10DResources10);
		resourcesInHbProject10_DE.addAll(hbProject10EResources10);

		dataList10E.add(new Data(refWks.hbProject10_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, resourcesInHbProject10_DE));
		dataList10E.add(new Data(refWks.hbProject10_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2, resourcesInHbProject10_DE));
		dataList10E.add(new Data(refWks.hbProject10_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3, resourcesInHbProject10_DE));

		for (Data data : dataList10E) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourcesInModel10E.size());

			for (Resource resource : resourcesInModel10E) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 resource

		List<Data> dataList20ABC = new ArrayList<Data>();
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20CResources20,
				resources20FromHbProject20_C));

		for (Data data : dataList20ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), expected, resourcesInModel20.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// Context Object is an EObject in HBProject20_D
		List<Data> dataList20D = new ArrayList<Data>();
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, hbProject20DResources20,
				resources20FromHbProject20_D));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, hbProject20DResources20,
				resources20FromHbProject20_D));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, hbProject20DResources20,
				resources20FromHbProject20_D));

		for (Data data : dataList20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			int expected = data.expectedNumber;
			List<String> contents = data.contents;
			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel20D = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), expected, resourcesInModel20D.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel20D) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// Context Object is an EObject in HBProject20_E
		List<Data> dataList20E = new ArrayList<Data>();
		List<String> resourcesInHbProject20_DE = new ArrayList<String>(hbProject20DResources20);
		resourcesInHbProject20_DE.addAll(hbProject20EResources20);
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, resourcesInHbProject20_DE));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2, resourcesInHbProject20_DE));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3, resourcesInHbProject20_DE));

		for (Data data : dataList20E) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources20FromHbProject20_D + resources20FromHbProject20_E,
					resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourcesInModelUml2.size());

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourcesInModel10.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

	}

	/**
	 * Test method for:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link ScopingResourceSet#getResourcesInModel(Object)}<br>
	 * with a contextual object of type:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link IModelDescriptor} (Uml2 model descriptor).
	 */
	public void testGetResourcesInModelFromUml2ModelDescriptor() throws Exception {
		String message = "Context model descriptor: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 model descriptor
		{
			IFile contextFile = refWks.hbProject10_C.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1);
			assertNotNull(contextFile);

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 0, resourceInModel10.size());

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 0, resourceInModel20.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 3, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C).contains(resource.getURI().lastSegment()));
			}
		}

		{
			IFile contextFile = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
			assertTrue(contextFile.isAccessible());

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			// assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 0, resourceInModel10.size());

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 0, resourceInModel20.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 6, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).contains(resource.getURI().lastSegment())
						|| refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E).contains(resource.getURI().lastSegment()));
			}
		}
	}

	/**
	 * Test method for:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link ScopingResourceSet#getResourcesInModel(Object)}<br>
	 * with a contextual object of type:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link IFile} (Uml2 file).
	 */
	public void testGetResourcesInModelFromUml2File() throws Exception {
		String message = "Context file: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 file

		List<Data> dataListSxBC = new ArrayList<Data>();
		List<String> hbProject10_CFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C);
		dataListSxBC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1, hbProject10_CFileNames));
		dataListSxBC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_2, hbProject10_CFileNames));
		dataListSxBC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_3, hbProject10_CFileNames));
		List<String> hbProject20_BFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		dataListSxBC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1, hbProject20_BFileNames));
		dataListSxBC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2, hbProject20_BFileNames));
		dataListSxBC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3, hbProject20_BFileNames));

		for (Data data : dataListSxBC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 3, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		List<Data> dataListSxDE = new ArrayList<Data>();
		List<String> hbProject20_DFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		dataListSxDE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, hbProject20_DFileNames, 3));
		dataListSxDE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, hbProject20_DFileNames, 3));
		dataListSxDE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, hbProject20_DFileNames, 3));
		List<String> hbProject20_EFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		dataListSxDE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, hbProject20_EFileNames, 6));
		dataListSxDE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2, hbProject20_EFileNames, 6));
		dataListSxDE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3, hbProject20_EFileNames, 6));

		for (Data data : dataListSxDE) {
			IProject project = data.project;
			String fileName = data.fileName;
			// List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInModel20.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), expected, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(hbProject20_DFileNames.contains(resource.getURI().lastSegment())
						|| hbProject20_EFileNames.contains(resource.getURI().lastSegment()));
			}
		}
	}

	/**
	 * Test method for {@link ScopingResourceSet#getResourcesInModel(Object)} with contextual object of type
	 * {@link Resource} (Uml2 Resource)
	 */
	public void testGetResourcesInModelFromUml2Resource() {
		List<String> hbProject20_DFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		List<String> hbProject20_EFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);

		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 resource
		List<Data> dataListUml2_20D = new ArrayList<Data>();
		dataListUml2_20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, hbProject20_DFileNames,
				resourcesUml2FromHbProject20_D));
		dataListUml2_20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, hbProject20_DFileNames,
				resourcesUml2FromHbProject20_D));
		dataListUml2_20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, hbProject20_DFileNames,
				resourcesUml2FromHbProject20_D));
		for (Data data : dataListUml2_20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			int expected = data.expectedNumber;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel20.size());

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel10.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(hbProject20_DFileNames.contains(resource.getURI().lastSegment()));
			}
		}
		List<Data> dataListUml2_20E = new ArrayList<Data>();
		dataListUml2_20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, hbProject20_DFileNames));
		dataListUml2_20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2, hbProject20_DFileNames));
		dataListUml2_20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3, hbProject20_DFileNames));
		for (Data data : dataListUml2_20E) {
			IProject project = data.project;
			String fileName = data.fileName;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel20.size());

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel10.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E,
					resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(hbProject20_DFileNames.contains(resource.getURI().lastSegment())
						|| hbProject20_EFileNames.contains(resource.getURI().lastSegment()));
			}
		}
	}

	/**
	 * Test method for {@link ScopingResourceSet#getResourcesInModel(Object)} with contextual object of type {@link URI}
	 */
	public void testGetResourcesInModelFromUml2Uri() {
		List<String> hbProject20_DFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		List<String> hbProject20_EFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);

		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 resource
		List<Data> dataListUml2_20D = new ArrayList<Data>();
		dataListUml2_20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, hbProject20_DFileNames,
				resourcesUml2FromHbProject20_D));
		dataListUml2_20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, hbProject20_DFileNames,
				resourcesUml2FromHbProject20_D));
		dataListUml2_20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, hbProject20_DFileNames,
				resourcesUml2FromHbProject20_D));
		for (Data data : dataListUml2_20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			int expected = data.expectedNumber;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel20.size());

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel10.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(hbProject20_DFileNames.contains(resource.getURI().lastSegment()));
			}
		}
		List<Data> dataListUml2_20E = new ArrayList<Data>();
		dataListUml2_20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, hbProject20_DFileNames));
		dataListUml2_20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2, hbProject20_DFileNames));
		dataListUml2_20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3, hbProject20_DFileNames));
		for (Data data : dataListUml2_20E) {
			IProject project = data.project;
			String fileName = data.fileName;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel20.size());

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel10.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E,
					resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(hbProject20_DFileNames.contains(resource.getURI().lastSegment())
						|| hbProject20_EFileNames.contains(resource.getURI().lastSegment()));
			}
		}
	}

	/**
	 * Test method for:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link ScopingResourceSet#getResourcesInModel(Object)}<br>
	 * with a contextual object of type:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link EObject} (Uml2 Object).
	 */
	public void testGetResourcesInModelFromUml2Object() {

		List<String> hbProject20_DFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		List<String> hbProject20_EFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);

		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 resource

		// Context Object is an EObject in Uml2 Model in HBProject20_D
		List<Data> dataListUml2_20D = new ArrayList<Data>();
		dataListUml2_20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, hbProject20_DFileNames,
				resourcesUml2FromHbProject20_D));
		dataListUml2_20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, hbProject20_DFileNames,
				resourcesUml2FromHbProject20_D));
		dataListUml2_20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, hbProject20_DFileNames,
				resourcesUml2FromHbProject20_D));

		for (Data data : dataListUml2_20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourceInModel20.size());

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourceInModel10.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), expected, resourceInModelUml2.size());

			for (Resource resource : resourceInModel20) {
				assertTrue(hbProject20_DFileNames.contains(resource.getURI().lastSegment()));
			}
		}
		// Context Object is an EObject in Uml2 Model in HBProject20_E
		List<Data> dataListUml2_20E = new ArrayList<Data>();
		dataListUml2_20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, hbProject20_DFileNames));
		dataListUml2_20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2, hbProject20_DFileNames));
		dataListUml2_20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3, hbProject20_DFileNames));

		for (Data data : dataListUml2_20E) {
			IProject project = data.project;
			String fileName = data.fileName;
			int expected = resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourceInModel20.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), expected, resourceInModelUml2.size());

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourceInModel10.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(hbProject20_EFileNames.contains(resource.getURI().lastSegment())
						|| hbProject20_DFileNames.contains(resource.getURI().lastSegment()));
			}
		}
	}

	// ========== 2 =====================
	/**
	 * Test method for {@link ScopingResourceSet#getResourcesInModel(Object, boolean))} with second input is FALSE
	 * 
	 * @see Tests for {@link ScopingResourceSet#getResourcesInModel(Object)}
	 */

	/**
	 * Test method for:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link ScopingResourceSet#getResourcesInModel(Object, boolean)}<br>
	 * with a contextual object of type:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@link IProject} (HBHB20project).
	 */
	public void testGetResourcesInModelFromProject_withoutReferenedScopes() {

		String message = "Context project: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 project without reference to other projects
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
			assertNotNull(contextProject);

			TransactionalEditingDomain editingDomain10 = WorkspaceEditingDomainUtil.getEditingDomain(refWks.hbProject10_A,
					Hummingbird10MMDescriptor.INSTANCE);
			assertNotNull(editingDomain10);

			TransactionalEditingDomain editingDomain20 = WorkspaceEditingDomainUtil.getEditingDomain(refWks.hbProject10_A,
					Hummingbird20MMDescriptor.INSTANCE);
			assertNotNull(editingDomain20);

			TransactionalEditingDomain editingDomainUml2 = WorkspaceEditingDomainUtil.getEditingDomain(refWks.hbProject10_A,
					UML2MMDescriptor.INSTANCE);
			assertNotNull(editingDomainUml2);

			List<Resource> resourcesInModel10 = getScopingResourceSet(editingDomain10).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources10FromHbProject10_A, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(editingDomain20).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(editingDomainUml2).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(hbProject10AResources10.contains(resource.getURI().lastSegment()));
			}
		}
		// ----------------------------------------------------------------------
		// Context: HB10 project reference to another HB10 project and a HB20 Project
		{

			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);

			TransactionalEditingDomain editingDomain10 = WorkspaceEditingDomainUtil.getEditingDomain(refWks.hbProject10_E,
					Hummingbird10MMDescriptor.INSTANCE);
			assertNotNull(editingDomain10);

			TransactionalEditingDomain editingDomain20 = WorkspaceEditingDomainUtil.getEditingDomain(refWks.hbProject10_E,
					Hummingbird20MMDescriptor.INSTANCE);
			assertNotNull(editingDomain20);

			TransactionalEditingDomain editingDomainUml2 = WorkspaceEditingDomainUtil.getEditingDomain(refWks.hbProject10_E,
					UML2MMDescriptor.INSTANCE);
			assertNotNull(editingDomainUml2);

			List<Resource> resourcesInModel10 = getScopingResourceSet(editingDomain10).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources10FromHbProject10_E, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(editingDomain20).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(editingDomainUml2).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// ----------------------------------------------------------------------
		// Context: HB20 project contain HB20 and Uml Models
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
			assertNotNull(contextProject);

			assertTrue("Project :" + contextProject.getName() + " does not exist", refWks.hbProject20_B.exists()); //$NON-NLS-1$ //$NON-NLS-2$

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources20FromHbProject20_B, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resourcesUml2FromHbProject20_B, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(hbProject20BResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInModelUml2) {
				assertTrue(hbProject20BResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C);
			assertNotNull(contextProject);

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources20FromHbProject20_C, resourcesInModel20.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(hbProject20CResources20.contains(resource.getURI().lastSegment()));
			}
		}
		// Context: HB20 Project contains HB20 and Uml MOdels references to HB10 Project
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
			assertNotNull(contextProject);

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources20FromHbProject20_D, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resourcesUml2FromHbProject20_D, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInModelUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}

		}
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
			assertNotNull(contextProject);

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources20FromHbProject20_E, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resourcesUml2FromHbProject20_E, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInModelUml2) {
				assertTrue(hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}

		}
	}

	public void testGetResourcesInModelFromHummingbirdModelDescriptor_withoutReferenedScopes() {
		String message = "Context model descriptor: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: HB20 model descriptor doesn't have referenced scopes
		{
			IFile contextFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D).getFile(
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1);

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), resources10FromHbProject10_D, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 model descriptor has referenced scopes
		{

			IFile contextFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E).getFile(
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1);
			assertNotNull(contextFile);

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), resources10FromHbProject10_E, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourcesInModelUml2.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 model descriptor doesn't have referenced scopes and contains UML models
		{
			IFile contextFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);

			IModelDescriptor contextHBModelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(contextHBModelDescriptor);

			String bindingHB = contextHBModelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextHBModelDescriptor, false);
			assertEquals(NLS.bind(message, bindingHB), resources20FromHbProject20_D, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2)
					.getResourcesInModel(contextHBModelDescriptor, false);
			assertEquals(NLS.bind(message, bindingHB), 0, resourcesInModelUml2.size());

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextHBModelDescriptor, false);
			assertEquals(NLS.bind(message, bindingHB), 0, resourcesInModel10.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 model descriptor has referenced scopes and contains UML models
		{
			IFile contextFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);

			IModelDescriptor contextHBModelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(contextHBModelDescriptor);

			String bindingHB = contextHBModelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextHBModelDescriptor, false);
			assertEquals(NLS.bind(message, bindingHB), resources20FromHbProject20_E, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2)
					.getResourcesInModel(contextHBModelDescriptor, false);
			assertEquals(NLS.bind(message, bindingHB), 0, resourcesInModelUml2.size());

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextHBModelDescriptor, false);
			assertEquals(NLS.bind(message, bindingHB), 0, resourcesInModel10.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
		}

	}

	public void testGetResourcesInModelFromHummingbirdFile_withoutReferenedScopes() {
		String message = "Context file: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 file
		{
			// Get the context file to use for filtering
			IFile contextFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
			assertNotNull(contextFile);

			List<Resource> resourceInModel10A = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_A, resourceInModel10A.size());

			for (Resource resource : resourceInModel10A) {
				assertTrue(hbProject10AResources10.contains(resource.getURI().lastSegment()));
			}
		}

		for (String fileName : hbProject10BResources10) {
			IProject project = refWks.hbProject10_B;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourcesInModel10B = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_B, resourcesInModel10B.size());

			for (Resource resource : resourcesInModel10B) {
				assertTrue(hbProject10BResources10.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB10 file (case of referenced project)

		for (String fileName : hbProject10DResources10) {
			IProject project = refWks.hbProject10_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(project, Hummingbird10MMDescriptor.INSTANCE);
			assertNotNull(editingDomain);

			List<Resource> resourcesInModel10D = getScopingResourceSet(editingDomain).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_D, resourcesInModel10D.size());

			for (Resource resource : resourcesInModel10D) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject10EResources10) {
			IProject project = refWks.hbProject10_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull("File : " + contextFile.getName(), contextFile); //$NON-NLS-1$

			TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(project, Hummingbird10MMDescriptor.INSTANCE);
			assertNotNull(editingDomain);

			List<Resource> resourcesInModel10E = getScopingResourceSet(editingDomain).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_E, resourcesInModel10E.size());

			for (Resource resource : resourcesInModel10E) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 file
		{
			IFile contextFile = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
			assertNotNull(contextFile);

			List<Resource> resourcesInModel20A = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources20FromHbProject20_A, resourcesInModel20A.size());

			for (Resource resource : resourcesInModel20A) {
				assertTrue(hbProject20HBesources20.contains(resource.getURI().lastSegment()));
			}
		}

		List<Data> dataList20C = new ArrayList<Data>();
		List<String> hbProject20_CFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C);
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20_CFileNames, 3));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20_CFileNames, 3));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20_CFileNames, 3));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_20C_1, hbProject20_CFileNames, 0));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_20C_2, hbProject20_CFileNames, 0));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_20C_3, hbProject20_CFileNames, 0));

		for (Data data : dataList20C) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInModel10.size());

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), expected, resourceInModel20.size());

			for (Resource resource : resourceInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 file (case of referenced project)
		List<Data> dataList20D = new ArrayList<Data>();
		List<String> hbProject20_DFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, hbProject20_DFileNames, 3));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, hbProject20_DFileNames, 3));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, hbProject20_DFileNames, 3));
		for (Data data : dataList20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInModelUml2.size());

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), expected, resourcesInModel20.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}

		}
		List<Data> dataList20E = new ArrayList<Data>();
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, hbProject20EResources20,
				resources20FromHbProject20_E));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2, hbProject20EResources20,
				resources20FromHbProject20_E));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3, hbProject20EResources20,
				resources20FromHbProject20_E));
		for (Data data : dataList20E) {
			IProject project = data.project;
			assertNotNull(project);
			int expected = data.expectedNumber;
			List<String> contents = data.contents;

			String fileName = data.fileName;
			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> esourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), expected, esourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInModelUml2.size());

			for (Resource resource : esourcesInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
	}

	public void testGetResourcesInModelFromHummingbirdResource_withoutReferenedScopes() {
		String message = "Context resource: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 resource

		List<Data> dataList10ABC = new ArrayList<Data>();
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_2, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_3, hbProject10CResources10,
				resources10FromHbProject10_C));

		for (Data data : dataList10ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), contents.size(), resourcesInModel10.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB10 resource (case of referenced project)

		for (String fileName : hbProject10DResources10) {
			IProject project = refWks.hbProject10_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_D, resourcesInModel10D.size());

			for (Resource resource : resourcesInModel10D) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject10EResources10) {
			IProject project = refWks.hbProject10_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_E, resourcesInModel10E.size());

			for (Resource resource : resourcesInModel10E) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 resource

		List<Data> dataList20ABC = new ArrayList<Data>();
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20CResources20,
				resources20FromHbProject20_C));

		for (Data data : dataList20ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInModel20.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 resource( case of references)
		List<Data> dataList20D = new ArrayList<Data>();
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, hbProject20DResources20));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, hbProject20DResources20));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, hbProject20DResources20));
		for (Data data : dataList20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel20D = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D, resourcesInModel20D.size());

			for (Resource resource : resourcesInModel20D) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		List<Data> dataList20E = new ArrayList<Data>();
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, hbProject20EResources20));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2, hbProject20EResources20));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3, hbProject20EResources20));
		for (Data data : dataList20E) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel20E = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_E, resourcesInModel20E.size());

			for (Resource resource : resourcesInModel20E) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

	}

	public void testGetResourcesInModelFromHummingbirdUri_withoutReferenedScopes() {
		String message = "Context resource: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 resource

		List<Data> dataList10ABC = new ArrayList<Data>();
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_2, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_3, hbProject10CResources10,
				resources10FromHbProject10_C));

		for (Data data : dataList10ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), contents.size(), resourcesInModel10.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB10 resource (case of referenced project)

		for (String fileName : hbProject10DResources10) {
			IProject project = refWks.hbProject10_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_D, resourcesInModel10D.size());

			for (Resource resource : resourcesInModel10D) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject10EResources10) {
			IProject project = refWks.hbProject10_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_E, resourcesInModel10E.size());

			for (Resource resource : resourcesInModel10E) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 resource

		List<Data> dataList20ABC = new ArrayList<Data>();
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20CResources20,
				resources20FromHbProject20_C));

		for (Data data : dataList20ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInModel20.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 resource( case of references)
		List<Data> dataList20D = new ArrayList<Data>();
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, hbProject20DResources20));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, hbProject20DResources20));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, hbProject20DResources20));
		for (Data data : dataList20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel20D = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D, resourcesInModel20D.size());

			for (Resource resource : resourcesInModel20D) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		List<Data> dataList20E = new ArrayList<Data>();
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, hbProject20EResources20));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2, hbProject20EResources20));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3, hbProject20EResources20));
		for (Data data : dataList20E) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInModel20E = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_E, resourcesInModel20E.size());

			for (Resource resource : resourcesInModel20E) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

	}

	public void testGetResourcesInModelFromHummingbirdObject_withoutReferenedScopes() {
		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: HB10 resource

		List<Data> dataList10ABC = new ArrayList<Data>();
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_2, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_3, hbProject10CResources10,
				resources10FromHbProject10_C));

		for (Data data : dataList10ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			// The context object use for filtering
			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), contents.size(), resourcesInModel10.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB10 resource (case of referenced project)

		for (String fileName : hbProject10DResources10) {
			IProject project = refWks.hbProject10_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources10FromHbProject10_D, resourcesInModel10D.size());

			for (Resource resource : resourcesInModel10D) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject10EResources10) {
			IProject project = refWks.hbProject10_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources10FromHbProject10_E, resourcesInModel10E.size());

			for (Resource resource : resourcesInModel10E) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 resource

		List<Data> dataList20ABC = new ArrayList<Data>();
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3, hbProject20BResources20,
				resources20FromHbProject20_B));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20CResources20,
				resources20FromHbProject20_C));

		for (Data data : dataList20ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);
			assertNotNull("Cannot get model root of resource: " + contextResource.getURI().toString(), contextObject);
			int resources20InContextHbProject = refWks.getReferenceFiles(contextFile.getParent().getName(), Hummingbird20MMDescriptor.INSTANCE)
					.size();
			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources20InContextHbProject, resourcesInModel20.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// Context Object is an EObject in HBProject20_D
		List<Data> dataList20D = new ArrayList<Data>();
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, hbProject20DResources20,
				resources20FromHbProject20_D));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, hbProject20DResources20,
				resources20FromHbProject20_D));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, hbProject20DResources20,
				resources20FromHbProject20_D));

		for (Data data : dataList20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			int expected = data.expectedNumber;
			List<String> contents = data.contents;
			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel20D = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), expected, resourcesInModel20D.size());

			List<Resource> filteredResourcesUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, filteredResourcesUml2.size());

			for (Resource resource : resourcesInModel20D) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// Context Object is an EObject in HBProject20_E
		List<Data> dataList20E = new ArrayList<Data>();
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, hbProject20EResources20,
				resources20FromHbProject20_E));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2, hbProject20EResources20,
				resources20FromHbProject20_E));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3, hbProject20EResources20,
				resources20FromHbProject20_E));

		for (Data data : dataList20E) {
			IProject project = data.project;
			String fileName = data.fileName;
			int expected = data.expectedNumber;
			List<String> contents = data.contents;
			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), expected, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourcesInModelUml2.size());

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourcesInModel10.size());

			for (Resource resource : resourcesInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
	}

	public void testGetResourcesInModelFromUml2ModelDescriptor_withoutReferenedScopes() {
		String message = "Context model descriptor: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 model descriptor that has no references
		{
			IFile contextFile = refWks.hbProject10_C.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1);
			assertNotNull(contextFile);

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourceInModel10.size());

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourceInModel20.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), resourcesUml2FromHbProject10_C, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(hbProject10CResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: Uml2 model descriptor that has referencing projects
		{
			IFile contextFile = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
			assertTrue(contextFile.isAccessible());

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourceInModel10.size());

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourceInModel20.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), resourcesUml2FromHbProject20_D, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: Uml2 model descriptor that has referenced projects
		{
			IFile contextFile = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
			assertTrue(contextFile.isAccessible());

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourceInModel10.size());

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourceInModel20.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), resourcesUml2FromHbProject20_E, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
	}

	public void testGetResourcesInModelFromUml2File_withoutReferenedScopes() {
		String message = "Context file: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 file

		List<Data> dataListUml2BC = new ArrayList<Data>();
		dataListUml2BC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1, hbProject10CResourcesUml2,
				resourcesUml2FromHbProject10_C));
		dataListUml2BC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_2, hbProject10CResourcesUml2,
				resourcesUml2FromHbProject10_C));
		dataListUml2BC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_3, hbProject10CResourcesUml2,
				resourcesUml2FromHbProject10_C));
		dataListUml2BC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1, hbProject20BResourcesUml2,
				resourcesUml2FromHbProject20_B));
		dataListUml2BC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2, hbProject20BResourcesUml2,
				resourcesUml2FromHbProject20_B));
		dataListUml2BC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3, hbProject20BResourcesUml2,
				resourcesUml2FromHbProject20_B));

		for (Data data : dataListUml2BC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInModel20.size());

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInModel10.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), expected, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		List<Data> dataListUml2DE = new ArrayList<Data>();
		dataListUml2DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, hbProject20DResourcesUml2,
				resourcesUml2FromHbProject20_D));
		dataListUml2DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, hbProject20DResourcesUml2,
				resourcesUml2FromHbProject20_D));
		dataListUml2DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, hbProject20DResourcesUml2,
				resourcesUml2FromHbProject20_D));
		dataListUml2DE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, hbProject20EResourcesUml2,
				resourcesUml2FromHbProject20_E));
		dataListUml2DE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2, hbProject20EResourcesUml2,
				resourcesUml2FromHbProject20_E));
		dataListUml2DE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3, hbProject20EResourcesUml2,
				resourcesUml2FromHbProject20_E));

		for (Data data : dataListUml2DE) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInModel20.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), expected, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
	}

	public void testGetResourcesInModelFromUml2Resource_withoutReferenedScopes() {

		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: Uml2 resource
		List<Data> dataListUml2BC = new ArrayList<Data>();
		dataListUml2BC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1, hbProject10CResourcesUml2,
				resourcesUml2FromHbProject10_C));
		dataListUml2BC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_2, hbProject10CResourcesUml2,
				resourcesUml2FromHbProject10_C));
		dataListUml2BC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_3, hbProject10CResourcesUml2,
				resourcesUml2FromHbProject10_C));
		dataListUml2BC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1, hbProject20BResourcesUml2,
				resourcesUml2FromHbProject20_B));
		dataListUml2BC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2, hbProject20BResourcesUml2,
				resourcesUml2FromHbProject20_B));
		dataListUml2BC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3, hbProject20BResourcesUml2,
				resourcesUml2FromHbProject20_B));

		for (Data data : dataListUml2BC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel20.size());

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel10.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: Uml2 resource ( case of references)
		List<Data> dataListUml2_20DE = new ArrayList<Data>();
		dataListUml2_20DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, hbProject20DResourcesUml2,
				resourcesUml2FromHbProject20_D));
		dataListUml2_20DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, hbProject20DResourcesUml2,
				resourcesUml2FromHbProject20_D));
		dataListUml2_20DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, hbProject20DResourcesUml2,
				resourcesUml2FromHbProject20_D));

		dataListUml2_20DE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, hbProject20EResourcesUml2,
				resourcesUml2FromHbProject20_E));
		dataListUml2_20DE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2, hbProject20EResourcesUml2,
				resourcesUml2FromHbProject20_E));
		dataListUml2_20DE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3, hbProject20EResourcesUml2,
				resourcesUml2FromHbProject20_E));

		for (Data data : dataListUml2_20DE) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel20.size());

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel10.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
	}

	public void testGetResourcesInModelFromUml2Uri_withoutReferenedScopes() {

		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: Uml2 resource
		List<Data> dataListUml2BC = new ArrayList<Data>();
		dataListUml2BC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1, hbProject10CResourcesUml2,
				resourcesUml2FromHbProject10_C));
		dataListUml2BC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_2, hbProject10CResourcesUml2,
				resourcesUml2FromHbProject10_C));
		dataListUml2BC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_3, hbProject10CResourcesUml2,
				resourcesUml2FromHbProject10_C));
		dataListUml2BC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1, hbProject20BResourcesUml2,
				resourcesUml2FromHbProject20_B));
		dataListUml2BC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2, hbProject20BResourcesUml2,
				resourcesUml2FromHbProject20_B));
		dataListUml2BC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3, hbProject20BResourcesUml2,
				resourcesUml2FromHbProject20_B));

		for (Data data : dataListUml2BC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel20.size());

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel10.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: Uml2 resource ( case of references)
		List<Data> dataListUml2_20DE = new ArrayList<Data>();
		dataListUml2_20DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, hbProject20DResourcesUml2,
				resourcesUml2FromHbProject20_D));
		dataListUml2_20DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, hbProject20DResourcesUml2,
				resourcesUml2FromHbProject20_D));
		dataListUml2_20DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, hbProject20DResourcesUml2,
				resourcesUml2FromHbProject20_D));

		dataListUml2_20DE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, hbProject20EResourcesUml2,
				resourcesUml2FromHbProject20_E));
		dataListUml2_20DE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2, hbProject20EResourcesUml2,
				resourcesUml2FromHbProject20_E));
		dataListUml2_20DE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3, hbProject20EResourcesUml2,
				resourcesUml2FromHbProject20_E));

		for (Data data : dataListUml2_20DE) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel20.size());

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInModel10.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourceInModelUml2.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
	}

	public void testGetResourcesInModelFromUml2Object_withoutReferenedScopes() {

		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 resource
		List<Data> dataListUml2BC = new ArrayList<Data>();
		dataListUml2BC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1, hbProject10CResourcesUml2,
				resourcesUml2FromHbProject10_C));
		dataListUml2BC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_2, hbProject10CResourcesUml2,
				resourcesUml2FromHbProject10_C));
		dataListUml2BC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_3, hbProject10CResourcesUml2,
				resourcesUml2FromHbProject10_C));
		dataListUml2BC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1, hbProject20BResourcesUml2,
				resourcesUml2FromHbProject20_B));
		dataListUml2BC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2, hbProject20BResourcesUml2,
				resourcesUml2FromHbProject20_B));
		dataListUml2BC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3, hbProject20BResourcesUml2,
				resourcesUml2FromHbProject20_B));

		for (Data data : dataListUml2BC) {
			IProject project = data.project;
			String fileName = data.fileName;
			int expected = data.expectedNumber;
			List<String> contents = data.contents;
			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourceInModel20.size());

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourceInModel10.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), expected, resourceInModelUml2.size());

			for (Resource resource : resourceInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: Uml2 resource

		// Context Object is an EObject in Uml2 Model in HBProject20_D
		List<Data> dataListUml2_20D = new ArrayList<Data>();
		dataListUml2_20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, hbProject20DResourcesUml2,
				resourcesUml2FromHbProject20_D));
		dataListUml2_20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, hbProject20DResourcesUml2,
				resourcesUml2FromHbProject20_D));
		dataListUml2_20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, hbProject20DResourcesUml2,
				resourcesUml2FromHbProject20_D));

		for (Data data : dataListUml2_20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			int expected = data.expectedNumber;
			List<String> contents = data.contents;
			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourceInModel20.size());

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourceInModel10.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), expected, resourceInModelUml2.size());

			for (Resource resource : resourceInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// Context Object is an EObject in Uml2 Model in HBProject20_E
		List<Data> dataListUml2_20E = new ArrayList<Data>();
		dataListUml2_20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, hbProject20EResourcesUml2,
				resourcesUml2FromHbProject20_E));
		dataListUml2_20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2, hbProject20EResourcesUml2,
				resourcesUml2FromHbProject20_E));
		dataListUml2_20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3, hbProject20EResourcesUml2,
				resourcesUml2FromHbProject20_E));

		for (Data data : dataListUml2_20E) {
			IProject project = data.project;
			String fileName = data.fileName;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourceInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourceInModel20.size());

			List<Resource> resourceInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), expected, resourceInModelUml2.size());

			List<Resource> resourceInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourceInModel10.size());

			for (Resource resource : resourceInModelUml2) {
				assertTrue(hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
	}

	// #############################################################################
	// ignoreMetaModel is TRUE
	// ========== 3 =====================
	/**
	 * Test method for {@link ScopingResourceSet#getResourcesInScope(Object, boolean))} with second input is TRUE
	 */
	public void testGetResourcesInScopeFromProject() {
		// Verify that loading has really been performed
		waitForModelLoading();
		String message = "Context project: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 project without reference to other projects
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
			assertNotNull(contextProject);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources10FromHbProject10_A, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10AResources10.contains(resource.getURI().lastSegment()));
			}
		}
		// ----------------------------------------------------------------------
		// Context: HB10 project reference to another HB10 project and a HB20 Project
		{

			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources10FromHbProject10_E + resources10FromHbProject10_D,
					resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// ----------------------------------------------------------------------
		// Context: HB20 project contain HB20 and Uml Models
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
			assertNotNull(contextProject);

			assertTrue("Project :" + contextProject.getName() + " does not exist", refWks.hbProject20_B.exists()); //$NON-NLS-1$ //$NON-NLS-2$

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources20FromHbProject20_B, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resourcesUml2FromHbProject20_B, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20BResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20BResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		// ----------------------------------------------------------------------------
		// Context: HB20 project contain HB20 and Uml Models
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C);
			assertNotNull(contextProject);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources20FromHbProject20_C, resourcesInScope20.size());
			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20CResources20.contains(resource.getURI().lastSegment()));
			}
		}

		// Context: HB20 Project contains HB20 and Uml MOdels references to HB10 Project
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
			assertNotNull(contextProject);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources20FromHbProject20_D, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextProject);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resourcesUml2FromHbProject20_D, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment())
						|| hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}

	}

	public void testGetResourcesInScopeFromHummingbirdModelDescriptor() {
		String message = "Context model descriptor: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: HB20 model descriptor
		{
			IFile contextFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D).getFile(
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1);

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), resources10FromHbProject10_D, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 model descriptor ( case of reference)
		{

			IFile contextFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E).getFile(
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1);
			assertNotNull(contextFile);

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), resources10FromHbProject10_E + resources10FromHbProject10_D, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}

		}
		// --------------------------------------------------------------------
		// Context: HB20 model descriptor - Project container contains UML Models

		{
			IFile contextFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);

			IModelDescriptor contextHBModelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(contextHBModelDescriptor);

			// Context Object is HBHB20MODEL
			String bindingHB = contextHBModelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextHBModelDescriptor);
			assertEquals(NLS.bind(message, bindingHB), resources20FromHbProject20_D, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextHBModelDescriptor);
			assertEquals(NLS.bind(message, bindingHB), resourcesUml2FromHbProject20_D, resourcesInScopeUml2.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextHBModelDescriptor);
			assertEquals(NLS.bind(message, bindingHB), resources10FromHbProject10_E + resources10FromHbProject10_D, resourcesInScope10.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 model descriptor - Project container contains UML Models( case of references)
		{
			IFile contextFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);

			IModelDescriptor contextHBModelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(contextHBModelDescriptor);

			// Context Object is HBHB20MODEL
			String bindingHB = contextHBModelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextHBModelDescriptor);
			assertEquals(NLS.bind(message, bindingHB), resources20FromHbProject20_E + resources20FromHbProject20_D, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextHBModelDescriptor);
			assertEquals(NLS.bind(message, bindingHB), resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E, resourcesInScopeUml2.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextHBModelDescriptor);
			assertEquals(NLS.bind(message, bindingHB), resources10FromHbProject10_D + resources10FromHbProject10_E, resourcesInScope10.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment())
						|| hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}

			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment())
						|| hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}

	}

	// TODO if contecxtObject is a File, resources which has different MMDesciptor were not returned
	public void testGetResourcesInScopeFromHummingbirdFile() {
		String message = "Context file: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 file
		{
			// Get the context file to use for filtering
			IFile contextFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
			assertNotNull(contextFile);

			List<Resource> resourceInScope10A = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile);
			// Verify the expected number of files
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_A, resourceInScope10A.size());

			for (Resource resource : resourceInScope10A) {
				assertTrue(hbProject10AResources10.contains(resource.getURI().lastSegment()));
			}
		}

		for (String fileName : hbProject10BResources10) {
			IProject project = refWks.hbProject10_B;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourcesInScope10B = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_B, resourcesInScope10B.size());

			for (Resource resource : resourcesInScope10B) {
				assertTrue(hbProject10BResources10.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject10CResources10) {
			IProject project = refWks.hbProject10_C;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourcesInScope10C = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_C, resourcesInScope10C.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10C) {
				assertTrue(hbProject10CResources10.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB10 file (case of referenced project)

		for (String fileName : hbProject10DResources10) {
			IProject project = refWks.hbProject10_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourcesInScope10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_D, resourcesInScope10D.size());

			for (Resource resource : resourcesInScope10D) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject10EResources10) {
			IProject project = refWks.hbProject10_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull("File : " + contextFile.getName(), contextFile); //$NON-NLS-1$

			List<Resource> resourcesInScope10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourcesInScope10E.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10E) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 file
		{
			IFile contextFile = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
			assertNotNull(contextFile);

			List<Resource> resourcesInScope20A = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources20FromHbProject20_A, resourcesInScope20A.size());

			for (Resource resource : resourcesInScope20A) {
				assertTrue(hbProject20HBesources20.contains(resource.getURI().lastSegment()));
			}
		}
		List<String> emptyContent = new ArrayList<String>();
		List<Data> dataList20C = new ArrayList<Data>();
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_20C_1, emptyContent, 0));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_20C_2, emptyContent, 0));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_20C_3, emptyContent, 0));

		for (Data data : dataList20C) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), expected, resourceInScope20.size());

			for (Resource resource : resourceInScope20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 file (case of referenced project)
		List<Data> dataList20D = new ArrayList<Data>();
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, hbProject20DResources20,
				resources20FromHbProject20_D));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, hbProject20DResources20,
				resources20FromHbProject20_D));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, hbProject20DResources20,
				resources20FromHbProject20_D));
		for (Data data : dataList20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;
			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);
			// TODO the return should contains UMl2 resources of hbProject20_D
			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScopeUml2.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), expected, resourcesInScope20.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}

		}
		List<Data> dataList20E = new ArrayList<Data>();
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, hbProject20EResources20));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2, hbProject20EResources20));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3, hbProject20EResources20));
		for (Data data : dataList20E) {
			IProject project = data.project;
			assertNotNull(project);

			String fileName = data.fileName;
			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources20FromHbProject20_D + resources20FromHbProject20_E,
					resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment())
						|| hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
		}
	}

	public void testGetResourcesInScopeFromHummingbirdResource() {
		String message = "Context resource: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 resource

		List<Data> dataList10ABC = new ArrayList<Data>();
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_2, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_3, hbProject10CResources10,
				resources10FromHbProject10_C));

		for (Data data : dataList10ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInScope10.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB10 resource (case of referenced project)

		for (String fileName : hbProject10DResources10) {
			IProject project = refWks.hbProject10_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_D, resourcesInScope10D.size());

			for (Resource resource : resourcesInScope10D) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject10EResources10) {
			IProject project = refWks.hbProject10_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourcesInScope10E.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10E) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 resource

		List<Data> dataList20ABC = new ArrayList<Data>();
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20CResources20,
				resources20FromHbProject20_C));

		for (Data data : dataList20ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInScope20.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		List<Data> dataList20B = new ArrayList<Data>();
		dataList20B.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1, hbProject20BResources20));
		dataList20B.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2, hbProject20BResources20));
		dataList20B.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3, hbProject20BResources20));
		for (Data data : dataList20B) {
			IProject project = data.project;
			String fileName = data.fileName;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_B, resourcesInScope20.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_B, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20BResources20.contains(resource.getURI().lastSegment()));
			}

			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20BResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		List<Data> dataList20D = new ArrayList<Data>();
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, hbProject20DResources20));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, hbProject20DResources20));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, hbProject20DResources20));
		for (Data data : dataList20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D, resourcesInScope20.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourcesInScope10.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_D, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment())
						|| hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		List<Data> dataList20E = new ArrayList<Data>();
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, hbProject20EResources20));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2, hbProject20EResources20));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3, hbProject20EResources20));
		for (Data data : dataList20E) {
			IProject project = data.project;
			String fileName = data.fileName;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D + resources20FromHbProject20_E,
					resourcesInScope20.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourcesInScope10.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E,
					resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment())
						|| hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment())
						|| hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment())
						|| hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}

	}

	public void testGetResourcesInScopeFromHummingbirdUri() {
		String message = "Context resource: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 resource

		List<Data> dataList10ABC = new ArrayList<Data>();
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_2, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_3, hbProject10CResources10,
				resources10FromHbProject10_C));

		for (Data data : dataList10ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInScope10.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB10 resource (case of referenced project)

		for (String fileName : hbProject10DResources10) {
			IProject project = refWks.hbProject10_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_D, resourcesInScope10D.size());

			for (Resource resource : resourcesInScope10D) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject10EResources10) {
			IProject project = refWks.hbProject10_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourcesInScope10E.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10E) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 resource

		List<Data> dataList20ABC = new ArrayList<Data>();
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20CResources20,
				resources20FromHbProject20_C));

		for (Data data : dataList20ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInScope20.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		List<Data> dataList20B = new ArrayList<Data>();
		dataList20B.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1, hbProject20BResources20));
		dataList20B.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2, hbProject20BResources20));
		dataList20B.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3, hbProject20BResources20));
		for (Data data : dataList20B) {
			IProject project = data.project;
			String fileName = data.fileName;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);
			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_B, resourcesInScope20.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20BResources20.contains(resource.getURI().lastSegment()));
			}

		}
		List<Data> dataList20D = new ArrayList<Data>();
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, hbProject20DResources20));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, hbProject20DResources20));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, hbProject20DResources20));
		for (Data data : dataList20D) {
			IProject project = data.project;
			String fileName = data.fileName;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D, resourcesInScope20.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}

		}
		List<Data> dataList20E = new ArrayList<Data>();
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, hbProject20EResources20));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2, hbProject20EResources20));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3, hbProject20EResources20));
		for (Data data : dataList20E) {
			IProject project = data.project;
			String fileName = data.fileName;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D + resources20FromHbProject20_E,
					resourcesInScope20.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment())
						|| hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}

		}

	}

	public void testGetResourcesInScopeFromHummingbirdObject() {
		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: HB10 resource

		List<Data> dataList10ABC = new ArrayList<Data>();
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_2, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_3, hbProject10CResources10,
				resources10FromHbProject10_C));

		for (Data data : dataList10ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), expected, resourcesInScope10.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB10 resource (case of referenced project)

		for (String fileName : hbProject10DResources10) {
			IProject project = refWks.hbProject10_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInScope10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources10FromHbProject10_D, resourcesInScope10D.size());

			for (Resource resource : resourcesInScope10D) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject10EResources10) {
			IProject project = refWks.hbProject10_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInScope10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourcesInScope10E.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10E) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 resource

		List<Data> dataList20AC = new ArrayList<Data>();
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4, hbProject20HBesources20,
				resources20FromHbProject20_A));

		dataList20AC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20AC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20AC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20CResources20,
				resources20FromHbProject20_C));

		for (Data data : dataList20AC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), expected, resourcesInScope20.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		for (String fileName : hbProject20BResources20) {
			IProject project = refWks.hbProject20_B;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources20FromHbProject20_B, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resourcesUml2FromHbProject20_B, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20BResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20BResourcesUml2.contains(resource.getURI().lastSegment()));
			}

		}
		// Context Object is an EObject in HBProject20_D
		List<Data> dataList20D = new ArrayList<Data>();
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		dataList20D.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		for (Data data : dataList20D) {
			IProject project = data.project;
			String fileName = data.fileName;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources20FromHbProject20_D, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resourcesUml2FromHbProject20_D, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment())
						|| hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment())
						|| hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment())
						|| hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		// Context Object is an EObject in HBProject20_E
		List<Data> dataList20E = new ArrayList<Data>();
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		dataList20E.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		for (Data data : dataList20E) {
			IProject project = data.project;
			String fileName = data.fileName;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources20FromHbProject20_D + resources20FromHbProject20_E,
					resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E,
					resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment())
						|| hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScope10) {
				assertTrue("Resources: " + resource.getURI().toString(), hbProject10EResources10.contains(resource.getURI().lastSegment())
						|| hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment())
						|| hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}

	}

	public void testGetResourcesInScopeFromUml2ModelDescriptor() {
		String message = "Context model descriptor: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 model descriptor
		{
			IFile contextFile = refWks.hbProject10_C.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1);
			assertNotNull(contextFile);

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), resources10FromHbProject10_C, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), 0, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), resourcesUml2FromHbProject10_C, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject10CResourcesUml2.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope10) {
				assertTrue(hbProject10CResources10.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: Uml2 model descriptor( case of references
		{
			IFile contextFile = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
			assertTrue(contextFile.isAccessible());

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), resources10FromHbProject10_D + resources10FromHbProject10_E, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), resourcesUml2FromHbProject20_D, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), resourcesUml2FromHbProject20_D, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
		}

		{
			IFile contextFile = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
			assertTrue(contextFile.isAccessible());

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), resources10FromHbProject10_D + resources10FromHbProject10_E, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), resources20FromHbProject20_D + resources20FromHbProject20_E, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(modelDescriptor);
			assertEquals(NLS.bind(message, binding), resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment())
						|| hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment())
						|| hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
		}
	}

	public void testGetResourcesInScopeFromUml2File() {
		String message = "Context file: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 file

		for (String fileName : hbProject10CResourcesUml2) {
			IProject project = refWks.hbProject10_C;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);
			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resourcesUml2FromHbProject10_C, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject10CResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject20BResourcesUml2) {
			IProject project = refWks.hbProject20_B;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);
			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resourcesUml2FromHbProject20_B, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20BResourcesUml2.contains(resource.getURI().lastSegment()));
			}

		}

		for (String fileName : hbProject20DResourcesUml2) {
			IProject project = refWks.hbProject20_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resourcesUml2FromHbProject20_D, resourceInScopeUml2.size());
			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}

		}
		for (String fileName : hbProject20EResourcesUml2) {
			IProject project = refWks.hbProject20_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E,
					resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment())
						|| hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}

		}
	}

	public void testGetResourcesInScopeFromUml2Resource() {

		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 resource
		for (String fileName : hbProject20DResourcesUml2) {
			IProject project = refWks.hbProject20_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D, resourceInScope20.size());

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourceInScope10.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_D, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		for (String fileName : hbProject20EResourcesUml2) {
			IProject project = refWks.hbProject20_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D + resources20FromHbProject20_E,
					resourceInScope20.size());

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourceInScope10.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource);
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E,
					resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment())
						|| hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment())
						|| hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}
	}

	public void testGetResourcesInScopeFromUml2Uri() {

		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 resource
		for (String fileName : hbProject20DResourcesUml2) {
			IProject project = refWks.hbProject20_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInScope20.size());

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInScope10.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_D, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}

		}

		for (String fileName : hbProject20EResourcesUml2) {
			IProject project = refWks.hbProject20_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInScope20.size());

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInScope10.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource.getURI());
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E,
					resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment())
						|| hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}

		}
	}

	public void testGetResourcesInScopeFromUml2Object() {

		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 resource

		// Context Object is an EObject in Uml2 Model in HBProject20_D
		for (String fileName : hbProject20DResourcesUml2) {
			IProject project = refWks.hbProject20_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);
			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources20FromHbProject20_D, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resourcesUml2FromHbProject20_D, resourceInScopeUml2.size());

			for (Resource resource : resourceInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		// Context Object is an EObject in Uml2 Model in HBProject20_E

		for (String fileName : hbProject20EResourcesUml2) {
			IProject project = refWks.hbProject20_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources20FromHbProject20_D + resources20FromHbProject20_E,
					resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E,
					resourceInScopeUml2.size());

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourceInScope10.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment())
						|| hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment())
						|| hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment())
						|| hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
		}
	}

	// ========== 4 =====================
	public void testGetResourcesInScopeFromProject_withoutReferencedScopes() {
		// Verify that loading has really been performed
		waitForModelLoading();
		String message = "Context project: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 project without reference to other projects
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
			assertNotNull(contextProject);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources10FromHbProject10_A, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10AResources10.contains(resource.getURI().lastSegment()));
			}
		}
		// ----------------------------------------------------------------------
		// Context: HB10 project reference to another HB10 project and a HB20 Project
		{

			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources10FromHbProject10_E, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// ----------------------------------------------------------------------
		// Context: HB20 project contain HB20 and Uml Models
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
			assertNotNull(contextProject);

			assertTrue("Project :" + contextProject.getName() + " does not exist", refWks.hbProject20_B.exists()); //$NON-NLS-1$ //$NON-NLS-2$

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources20FromHbProject20_B, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resourcesUml2FromHbProject20_B, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20BResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20BResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		// ----------------------------------------------------------------------------
		// Context: HB20 project contain HB20 and Uml Models
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C);
			assertNotNull(contextProject);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources20FromHbProject20_C, resourcesInScope20.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20CResources20.contains(resource.getURI().lastSegment()));
			}
		}
		// Context: HB20 Project contains HB20 and Uml MOdels references to HB10 Project
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
			assertNotNull(contextProject);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources20FromHbProject20_D, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resourcesUml2FromHbProject20_D, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}

		}
		{
			IProject contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
			assertNotNull(contextProject);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resources20FromHbProject20_E, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextProject, false);
			assertEquals(NLS.bind(message, contextProject.getFullPath()), resourcesUml2FromHbProject20_E, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}

		}

	}

	public void testGetResourcesInScopeFromHummingbirdModelDescriptor_withoutReferencedScopes() {
		String message = "Context model descriptor: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB20 model descriptor
		{
			IFile contextFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D).getFile(
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1);

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), resources10FromHbProject10_D, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 model descriptor ( case of references)
		{

			IFile contextFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E).getFile(
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1);
			assertNotNull(contextFile);

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), resources10FromHbProject10_E, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}

		}
		// --------------------------------------------------------------------
		// Context: HB20 model descriptor- project container contains UML Model

		{
			IFile contextFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);

			IModelDescriptor contextHBModelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(contextHBModelDescriptor);

			// Context Object is HBHB20MODEL
			String bindingHB = contextHBModelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextHBModelDescriptor, false);
			assertEquals(NLS.bind(message, bindingHB), resources20FromHbProject20_D, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2)
					.getResourcesInScope(contextHBModelDescriptor, false);
			assertEquals(NLS.bind(message, bindingHB), resourcesUml2FromHbProject20_D, resourcesInScopeUml2.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextHBModelDescriptor, false);
			assertEquals(NLS.bind(message, bindingHB), 0, resourcesInScope10.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}

			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 model descriptor- project container contains UML models( case of references)
		{
			IFile contextFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);

			IModelDescriptor contextHBModelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(contextHBModelDescriptor);

			// Context Object is HBHB20MODEL
			String bindingHB = contextHBModelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextHBModelDescriptor, false);
			assertEquals(NLS.bind(message, bindingHB), resources20FromHbProject20_E, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2)
					.getResourcesInScope(contextHBModelDescriptor, false);
			assertEquals(NLS.bind(message, bindingHB), resourcesUml2FromHbProject20_E, resourcesInScopeUml2.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextHBModelDescriptor, false);
			assertEquals(NLS.bind(message, bindingHB), 0, resourcesInScope10.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}

			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}

	}

	public void testGetResourcesInScopeFromHummingbirdFile_withoutReferencedScopes() {
		String message = "Context file: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 file
		{
			// Get the context file to use for filtering
			IFile contextFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
			assertNotNull(contextFile);

			List<Resource> resourceInScope10A = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_A, resourceInScope10A.size());

			for (Resource resource : resourceInScope10A) {
				assertTrue(hbProject10AResources10.contains(resource.getURI().lastSegment()));
			}
		}

		for (String fileName : hbProject10BResources10) {
			IProject project = refWks.hbProject10_B;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourcesInScope10B = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_B, resourcesInScope10B.size());

			for (Resource resource : resourcesInScope10B) {
				assertTrue(hbProject10BResources10.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject10CResources10) {
			IProject project = refWks.hbProject10_C;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourcesInScope10C = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_C, resourcesInScope10C.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10C) {
				assertTrue(hbProject10CResources10.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB10 file (case of referenced project)

		for (String fileName : hbProject10DResources10) {
			IProject project = refWks.hbProject10_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourcesInScope10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_D, resourcesInScope10D.size());

			for (Resource resource : resourcesInScope10D) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}

		for (String fileName : hbProject10EResources10) {
			IProject project = refWks.hbProject10_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull("File : " + contextFile.getName(), contextFile); //$NON-NLS-1$

			List<Resource> resourcesInScope10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources10FromHbProject10_E, resourcesInScope10E.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10E) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 file
		{

			IFile contextFile = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
			assertNotNull(contextFile);

			List<Resource> resourcesInScope20A = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources20FromHbProject20_A, resourcesInScope20A.size());

			for (Resource resource : resourcesInScope20A) {
				assertTrue(hbProject20HBesources20.contains(resource.getURI().lastSegment()));
			}
		}

		List<Data> dataList20C = new ArrayList<Data>();
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_20C_1, hbProject20CResources20, 0));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_20C_2, hbProject20CResources20, 0));
		dataList20C.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_20C_3, hbProject20CResources20, 0));

		for (Data data : dataList20C) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);
			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), expected, resourceInScope20.size());

			for (Resource resource : resourceInScope20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 file (case of referenced project)
		for (String fileName : hbProject20DResources20) {
			IProject project = refWks.hbProject20_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScopeUml2.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources20FromHbProject20_D, resourcesInScope20.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}

		}
		for (String fileName : hbProject20EResources20) {
			IProject project = refWks.hbProject20_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resources20FromHbProject20_E, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
		}
	}

	public void testGetResourcesInScopeFromHummingbirdResource_withoutReferencedScopes() {
		String message = "Context resource: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 resource

		List<Data> dataList10ABC = new ArrayList<Data>();
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_2, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_3, hbProject10CResources10,
				resources10FromHbProject10_C));

		for (Data data : dataList10ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInScope10.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB10 resource (case of referenced project)

		for (String fileName : hbProject10DResources10) {
			IProject project = refWks.hbProject10_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_D, resourcesInScope10D.size());

			for (Resource resource : resourcesInScope10D) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject10EResources10) {
			IProject project = refWks.hbProject10_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_E, resourcesInScope10E.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10E) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 resource

		List<Data> dataList20AC = new ArrayList<Data>();
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20AC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20AC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20CResources20,
				resources20FromHbProject20_C));

		for (Data data : dataList20AC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			int resources20InContextHbProject = refWks.getReferenceFiles(contextFile.getParent().getName(), Hummingbird20MMDescriptor.INSTANCE)
					.size();
			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20InContextHbProject, resourcesInScope20.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject20BResources20) {
			IProject project = refWks.hbProject20_B;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_B, resourcesInScope20.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_B, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20BResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20BResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}

		for (String fileName : hbProject20EResources20) {
			IProject project = refWks.hbProject20_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_E, resourcesInScope20.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_E, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject20DResources20) {
			IProject project = refWks.hbProject20_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D, resourcesInScope20.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_D, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}

			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}

	}

	public void testGetResourcesInScopeFromHummingbirdUri_withoutReferencedScopes() {
		String message = "Context resource: \"{0}\" - Number of resources"; //$NON-NLS-1$

		// --------------------------------------------------------------------
		// Context: HB10 resource

		List<Data> dataList10ABC = new ArrayList<Data>();
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_2, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_3, hbProject10CResources10,
				resources10FromHbProject10_C));

		for (Data data : dataList10ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInScope10.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB10 resource (case of referenced project)

		for (String fileName : hbProject10DResources10) {
			IProject project = refWks.hbProject10_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_D, resourcesInScope10D.size());

			for (Resource resource : resourcesInScope10D) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject10EResources10) {
			IProject project = refWks.hbProject10_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources10FromHbProject10_E, resourcesInScope10E.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2)
					.getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10E) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 resource

		List<Data> dataList20AC = new ArrayList<Data>();
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20AC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20AC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20CResources20,
				resources20FromHbProject20_C));

		for (Data data : dataList20AC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), expected, resourcesInScope20.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject20BResources20) {
			IProject project = refWks.hbProject20_B;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_B, resourcesInScope20.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2)
					.getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20BResources20.contains(resource.getURI().lastSegment()));
			}

		}

		for (String fileName : hbProject20EResources20) {
			IProject project = refWks.hbProject20_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_E, resourcesInScope20.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2)
					.getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}

		}
		for (String fileName : hbProject20DResources20) {
			IProject project = refWks.hbProject20_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D, resourcesInScope20.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2)
					.getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}

		}

	}

	public void testGetResourcesInScopeFromHummingbirdObject_withoutReferencedScopes() {
		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: HB10 resource
		List<Data> dataList10ABC = new ArrayList<Data>();
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5, hbProject10AResources10,
				resources10FromHbProject10_A));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3, hbProject10BResources10,
				resources10FromHbProject10_B));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_2, hbProject10CResources10,
				resources10FromHbProject10_C));
		dataList10ABC.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_3, hbProject10CResources10,
				resources10FromHbProject10_C));
		for (Data data : dataList10ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;
			int expected = data.expectedNumber;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			// The context object use for filtering
			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), expected, resourcesInScope10.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB10 resource (case of referenced project)

		for (String fileName : hbProject10DResources10) {
			IProject project = refWks.hbProject10_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInScope10D = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources10FromHbProject10_D, resourcesInScope10D.size());

			for (Resource resource : resourcesInScope10D) {
				assertTrue(hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject10EResources10) {
			IProject project = refWks.hbProject10_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources10FromHbProject10_E, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment()));
			}
		}

		// --------------------------------------------------------------------
		// Context: HB20 resource

		List<Data> dataList20AC = new ArrayList<Data>();
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4, hbProject20HBesources20,
				resources20FromHbProject20_A));
		dataList20AC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20AC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20CResources20,
				resources20FromHbProject20_C));
		dataList20AC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20CResources20,
				resources20FromHbProject20_C));

		for (Data data : dataList20AC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);
			int resources20InContextHbProject = refWks.getReferenceFiles(contextFile.getParent().getName(), Hummingbird20MMDescriptor.INSTANCE)
					.size();
			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources20InContextHbProject, resourcesInScope20.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

		for (String fileName : hbProject20BResourcesUml2) {
			IProject project = refWks.hbProject20_B;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);
			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_B, resourcesInScope20.size());

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourcesInScope10.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_B, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20BResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20BResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		// Context Object is an EObject in HBProject20_D
		for (String fileName : hbProject20DResourcesUml2) {
			IProject project = refWks.hbProject20_D;
			assertNotNull(project);

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources20FromHbProject20_D, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resourcesUml2FromHbProject20_D, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}

			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		// Context Object is an EObject in HBProject20_E
		for (String fileName : hbProject20EResourcesUml2) {
			IProject project = refWks.hbProject20_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourcesInScope10.size());

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources20FromHbProject20_E, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resourcesUml2FromHbProject20_E, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}

			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}

	}

	public void testGetResourcesInScopeFromUml2ModelDescriptor_withoutReferencedScopes() {
		String message = "Context model descriptor: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 model descriptor
		{
			IFile contextFile = refWks.hbProject10_C.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1);
			assertNotNull(contextFile);

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), resources10FromHbProject10_C, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), resourcesUml2FromHbProject10_C, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject10CResourcesUml2.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope10) {
				assertTrue(hbProject10CResources10.contains(resource.getURI().lastSegment()));
			}
		}
		{
			IFile contextFile = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
			assertTrue(contextFile.isAccessible());

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			// assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), resourcesUml2FromHbProject20_D, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), resourcesUml2FromHbProject20_D, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}

		{
			IFile contextFile = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
			assertTrue(contextFile.isAccessible());

			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextFile);
			assertNotNull(modelDescriptor);

			String binding = modelDescriptor.getMetaModelDescriptor().getIdentifier() + " " + contextFile.getFullPath(); //$NON-NLS-1$

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), 0, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), resources20FromHbProject20_E, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(modelDescriptor, false);
			assertEquals(NLS.bind(message, binding), resourcesUml2FromHbProject20_E, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
	}

	public void testGetResourcesInScopeFromUml2File_withoutReferencedScopes() {
		String message = "Context file: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 file
		{
			IFile contextFile = refWks.hbProject10_C.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1);
			assertNotNull(contextFile);

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resourcesUml2FromHbProject10_C, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject10CResourcesUml2.contains(resource.getURI().lastSegment()));
			}

		}

		for (String fileName : hbProject20BResourcesUml2) {
			IProject project = refWks.hbProject20_B;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);
			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resourcesUml2FromHbProject20_B, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20BResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}

		for (String fileName : hbProject20DResourcesUml2) {
			IProject project = refWks.hbProject20_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);
			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resourcesUml2FromHbProject20_D, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		for (String fileName : hbProject20EResourcesUml2) {
			IProject project = refWks.hbProject20_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);
			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope10.size());

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), 0, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextFile, false);
			assertEquals(NLS.bind(message, contextFile.getFullPath()), resourcesUml2FromHbProject20_E, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}

		}
	}

	public void testGetResourcesInScopeFromUml2Resource_withoutReferencedScopes() {

		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 resource
		for (String fileName : hbProject20DResourcesUml2) {
			IProject project = refWks.hbProject20_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_D, resourceInScope20.size());

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInScope10.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_D, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
		}

		for (String fileName : hbProject20EResourcesUml2) {
			IProject project = refWks.hbProject20_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resources20FromHbProject20_E, resourceInScope20.size());

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInScope10.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource, false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_E, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope20) {
				assertTrue(hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
		}
	}

	public void testGetResourcesInScopeFromUml2UriwithoutReferencedScopes() {

		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 resource
		for (String fileName : hbProject20DResourcesUml2) {
			IProject project = refWks.hbProject20_D;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInScope20.size());

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInScope10.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_D, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}

		}

		for (String fileName : hbProject20EResourcesUml2) {
			IProject project = refWks.hbProject20_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInScope20.size());

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), 0, resourceInScope10.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextResource.getURI(), false);
			assertEquals(NLS.bind(message, contextResource.getURI()), resourcesUml2FromHbProject20_E, resourceInScopeUml2.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}

		}
	}

	public void testGetResourcesInScopeFromUml2Object_withoutReferencedScopes() {

		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: Uml2 resource

		// Context Object is an EObject in Uml2 Model in HBProject20_D

		for (String fileName : hbProject20DResourcesUml2) {
			IProject project = refWks.hbProject20_D;
			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources20FromHbProject20_D, resourceInScope20.size());

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourceInScope10.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resourcesUml2FromHbProject20_D, resourceInScopeUml2.size());

			for (Resource resource : resourceInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
		// Context Object is an EObject in Uml2 Model in HBProject20_E
		for (String fileName : hbProject20EResourcesUml2) {
			IProject project = refWks.hbProject20_E;
			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourceInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resources20FromHbProject20_E, resourceInScope20.size());

			List<Resource> resourceInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), resourcesUml2FromHbProject20_E, resourceInScopeUml2.size());

			List<Resource> resourceInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(contextObject, false);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 0, resourceInScope10.size());

			for (Resource resource : resourceInScopeUml2) {
				assertTrue(hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourceInScope20) {
				assertTrue(hbProject20EResources20.contains(resource.getURI().lastSegment()));
			}
		}
	}

	// ===============================
	/**
	 * Test method for {@link ScopingResourceSet#getResourcesInModel(Object, boolean)} and
	 * {@link ScopingResourceSet#getResourcesInScope(Object, boolean)}
	 */
	public void testGetResourceFromWorkspaceRoot() throws IOException {
		String message = "Context object is WorkspaceRoot - Number of resources"; //$NON-NLS-1$" +
		IWorkspaceRoot workspaceRoot = EcorePlugin.getWorkspaceRoot();
		assertNotNull(workspaceRoot);
		int resourceInEditingDomain10 = refWks.editingDomain10.getResourceSet().getResources().size();
		int resourceInEditingDomain20 = refWks.editingDomain20.getResourceSet().getResources().size();
		int resourceInEditingDomainUml2 = refWks.editingDomainUml2.getResourceSet().getResources().size();

		// Context object's inside workspace
		List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(workspaceRoot);
		assertEquals(message, resourceInEditingDomain20, resourcesInScope20.size());
		resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(workspaceRoot, false);
		assertEquals(message, resourceInEditingDomain20, resourcesInScope20.size());
		resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(workspaceRoot);
		assertEquals(message, resourceInEditingDomain20, resourcesInScope20.size());
		resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(workspaceRoot, false);
		assertEquals(message, resourceInEditingDomain20, resourcesInScope20.size());

		List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(workspaceRoot);
		assertEquals(message, resourceInEditingDomain10, resourcesInScope10.size());
		resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(workspaceRoot, false);
		assertEquals(message, resourceInEditingDomain10, resourcesInScope10.size());
		resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(workspaceRoot);
		assertEquals(message, resourceInEditingDomain10, resourcesInScope10.size());
		resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(workspaceRoot, false);
		assertEquals(message, resourceInEditingDomain10, resourcesInScope10.size());

		List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(workspaceRoot);
		assertEquals(message, resourceInEditingDomainUml2, resourcesInScopeUml2.size());
		resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(workspaceRoot, false);
		assertEquals(message, resourceInEditingDomainUml2, resourcesInScopeUml2.size());
		resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(workspaceRoot);
		assertEquals(message, resourceInEditingDomainUml2, resourcesInScopeUml2.size());
		resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(workspaceRoot, false);
		assertEquals(message, resourceInEditingDomainUml2, resourcesInScopeUml2.size());

	}

	/**
	 * Test method for {@link ScopingResourceSet#getResourcesInModel(Object, boolean)} to verify that unloading/loading
	 * resources impact to list resources to return
	 * 
	 * @throws Exception
	 */
	public void testGetResourceInScopeAfterUnloadingReloadingResources() {
		// Unload resource
		String message = "Context project: \"{0}\" - Number of resources"; //$NON-NLS-1$
		{
			// Unload resource in arProjec10E
			IProject projectToUnload = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);
			assertNotNull(projectToUnload);
			synchronizedUnloadProject(projectToUnload, false);

			List<Resource> resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(refWks.hbProject20_D);
			assertEquals(NLS.bind(message, refWks.hbProject20_D.getFullPath()), resources10FromHbProject10_D, resourcesInScope10.size());

			// Unload resource in arProjec20E
			projectToUnload = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
			assertNotNull(projectToUnload);
			synchronizedUnloadProject(projectToUnload, false);

			List<Resource> resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(refWks.hbProject20_E);
			assertEquals(NLS.bind(message, refWks.hbProject20_E.getFullPath()), resources20FromHbProject20_D, resourcesInScope20.size());

			List<Resource> resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(refWks.hbProject20_E);
			assertEquals(NLS.bind(message, refWks.hbProject20_E.getFullPath()), resourcesUml2FromHbProject20_D, resourcesInScopeUml2.size());

			for (Resource resource : resourcesInScope20) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScopeUml2) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
			for (Resource resource : resourcesInScope10) {
				assertTrue(hbProject10EResources10.contains(resource.getURI().lastSegment())
						|| hbProject10DResources10.contains(resource.getURI().lastSegment()));
			}
			// Re-load hbProject10E
			IProject projectToload = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);
			assertNotNull(projectToUnload);
			synchronizedLoadProject(projectToload, false);

			resourcesInScope10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInScope(refWks.hbProject20_E);
			assertEquals(NLS.bind(message, refWks.hbProject20_E.getFullPath()), resources10FromHbProject10_D + resources10FromHbProject10_E,
					resourcesInScope10.size());

			resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(refWks.hbProject20_E);
			assertEquals(NLS.bind(message, refWks.hbProject20_E.getFullPath()), resources20FromHbProject20_D, resourcesInScope20.size());

			resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(refWks.hbProject20_E);
			assertEquals(NLS.bind(message, refWks.hbProject20_E.getFullPath()), resourcesUml2FromHbProject20_D, resourcesInScopeUml2.size());

			// Re-load hbProject20_E
			projectToload = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
			assertNotNull(projectToUnload);
			synchronizedLoadProject(projectToload, false);

			resourcesInScope20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInScope(refWks.hbProject20_E);
			assertEquals(NLS.bind(message, refWks.hbProject20_E.getFullPath()), resources20FromHbProject20_D + resources20FromHbProject20_E,
					resourcesInScope20.size());

			resourcesInScopeUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInScope(refWks.hbProject20_E);
			assertEquals(NLS.bind(message, refWks.hbProject20_E.getFullPath()), resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E,
					resourcesInScopeUml2.size());
		}
	}

	/**
	 * Test method for {@link ScopingResourceSet#getResourcesInModel(Object, boolean)} to verify that adding/removing
	 * dependencies btw projects impact to list resources to return
	 * 
	 * @throws Exception
	 */
	public void testGetResourcesInAfterAddingBreakingDependency() throws Exception {
		String message = "Context object: \"{0}\" - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: HB10 resource
		// Set dependency from hbProject10B to hbProject10A
		IProjectDescription hbProject10BDesc = refWks.hbProject10_B.getDescription();
		hbProject10BDesc.setReferencedProjects(new IProject[] { refWks.hbProject10_A });
		refWks.hbProject10_B.setDescription(hbProject10BDesc, new NullProgressMonitor());
		waitForModelLoading();

		List<Data> dataList10ABC_ = new ArrayList<Data>();
		List<String> hbProject10ABFileNames = new ArrayList<String>();
		List<String> hbProject10AFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
				Hummingbird10MMDescriptor.INSTANCE);
		List<String> hbProject10BFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_B,
				Hummingbird10MMDescriptor.INSTANCE);
		hbProject10ABFileNames.addAll(hbProject10AFileNames);
		hbProject10ABFileNames.addAll(hbProject10BFileNames);
		assertEquals(hbProject10AFileNames.size() + hbProject10BFileNames.size(), hbProject10ABFileNames.size());

		dataList10ABC_.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, hbProject10AFileNames));
		dataList10ABC_.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, hbProject10AFileNames));
		dataList10ABC_.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, hbProject10AFileNames));
		dataList10ABC_.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4, hbProject10AFileNames));
		dataList10ABC_.add(new Data(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5, hbProject10AFileNames));
		dataList10ABC_.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1, hbProject10ABFileNames));
		dataList10ABC_.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2, hbProject10ABFileNames));
		dataList10ABC_.add(new Data(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3, hbProject10ABFileNames));

		dataList10ABC_.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C, Hummingbird10MMDescriptor.INSTANCE)));
		dataList10ABC_.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_2, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C, Hummingbird10MMDescriptor.INSTANCE)));
		dataList10ABC_.add(new Data(refWks.hbProject10_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_3, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C, Hummingbird10MMDescriptor.INSTANCE)));

		for (Data data : dataList10ABC_) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull(contextResource);

			// The context object used for filtering
			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel10 = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, contextFile.getName()), contents.size(), resourcesInModel10.size());

			for (Resource resource : resourcesInModel10) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB10 resource (case of referenced project)

		List<String> hbProject10_DFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D);
		List<String> hbProject10_EFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);
		for (String fileName : hbProject10_EFileNames) {
			IProject project = refWks.hbProject10_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), 6, resourcesInModel10E.size());

			for (Resource resource : resourcesInModel10E) {
				assertTrue(hbProject10_DFileNames.contains(resource.getURI().lastSegment())
						|| hbProject10_EFileNames.contains(resource.getURI().lastSegment()));
			}
		}
		// Remove reference of from hbProject10E to hbProject10D

		IProjectDescription hbProject10EDesc = refWks.hbProject10_E.getDescription();
		hbProject10EDesc.setReferencedProjects(new IProject[] {});
		refWks.hbProject10_E.setDescription(hbProject10EDesc, new NullProgressMonitor());
		waitForModelLoading();
		for (String fileName : hbProject10_EFileNames) {
			IProject project = refWks.hbProject10_E;
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel10E = getScopingResourceSet(refWks.editingDomain10).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, EcoreUtil.getURI(contextObject)), hbProject10_EFileNames.size(), resourcesInModel10E.size());

			for (Resource resource : resourcesInModel10E) {
				assertTrue(hbProject10_EFileNames.contains(resource.getURI().lastSegment()));
			}
		}
		// --------------------------------------------------------------------
		// Context: HB20 resource

		List<String> hbProject20_AFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A,
				Hummingbird20MMDescriptor.INSTANCE);
		List<String> hbProject20_BFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B,
				Hummingbird20MMDescriptor.INSTANCE);
		List<String> hbProject20_CFileNames = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C,
				Hummingbird20MMDescriptor.INSTANCE);

		List<String> hbProject20ABFileNames = new ArrayList<String>();
		hbProject20ABFileNames.addAll(hbProject20_AFileNames);
		hbProject20ABFileNames.addAll(hbProject20_BFileNames);
		assertEquals(resources20FromHbProject20_A + resources20FromHbProject20_B, hbProject20ABFileNames.size());

		// Set reference from hbProject20B to hbProject20A
		IProjectDescription hbProject20BDesc = refWks.hbProject20_B.getDescription();
		hbProject20BDesc.setReferencedProjects(new IProject[] { refWks.hbProject20_A });
		refWks.hbProject20_B.setDescription(hbProject20BDesc, new NullProgressMonitor());
		waitForModelLoading();

		IProjectDescription hbProject20CDesc = refWks.hbProject20_C.getDescription();
		hbProject20CDesc.setReferencedProjects(new IProject[] { refWks.hbProject20_B });
		refWks.hbProject20_C.setDescription(hbProject20CDesc, new NullProgressMonitor());
		waitForModelLoading();

		List<Data> dataList20ABC = new ArrayList<Data>();
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, hbProject20_AFileNames));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, hbProject20_AFileNames));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, hbProject20_AFileNames));
		dataList20ABC.add(new Data(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4, hbProject20_AFileNames));

		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1, hbProject20ABFileNames));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2, hbProject20ABFileNames));
		dataList20ABC.add(new Data(refWks.hbProject20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3, hbProject20ABFileNames));

		List<String> hbProject20ABCFileNames = new ArrayList<String>();
		hbProject20ABCFileNames.addAll(hbProject20ABFileNames);
		hbProject20ABCFileNames.addAll(hbProject20_CFileNames);

		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, hbProject20ABCFileNames));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_2, hbProject20ABCFileNames));
		dataList20ABC.add(new Data(refWks.hbProject20_C, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_3, hbProject20ABCFileNames));

		for (Data data : dataList20ABC) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource); //$NON-NLS-1$

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, contextFile.getName()), contents.size(), resourcesInModel20.size());
			for (Resource resource : resourcesInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// Remove dependency from hbProject20E to hbProject20D
		IProjectDescription hbProject20EDesc = refWks.hbProject20_E.getDescription();
		hbProject20EDesc.setReferencedProjects(new IProject[] {});
		refWks.hbProject20_E.setDescription(hbProject20EDesc, new NullProgressMonitor());
		waitForModelLoading();

		List<Data> dataList20DE = new ArrayList<Data>();
		dataList20DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE)));
		dataList20DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE)));
		dataList20DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE)));

		dataList20ABC.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, Hummingbird20MMDescriptor.INSTANCE)));
		dataList20ABC.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, Hummingbird20MMDescriptor.INSTANCE)));
		dataList20ABC.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, Hummingbird20MMDescriptor.INSTANCE)));

		for (Data data : dataList20DE) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModel20 = getScopingResourceSet(refWks.editingDomain20).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, contextFile.getName()), contents.size(), resourcesInModel20.size());
			for (Resource resource : resourcesInModel20) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}
		// Context: Non Hummingbird Context Object
		List<Data> dataListUml2DE = new ArrayList<Data>();
		dataListUml2DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE)));
		dataListUml2DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE)));
		dataListUml2DE.add(new Data(refWks.hbProject20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE)));

		dataListUml2DE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, UML2MMDescriptor.INSTANCE)));
		dataListUml2DE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, UML2MMDescriptor.INSTANCE)));
		dataListUml2DE.add(new Data(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3, refWks.getReferenceFileNames(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, UML2MMDescriptor.INSTANCE)));

		for (Data data : dataListUml2DE) {
			IProject project = data.project;
			String fileName = data.fileName;
			List<String> contents = data.contents;

			assertNotNull(fileName);
			assertNotNull(project);

			IFile contextFile = project.getFile(fileName);
			assertNotNull(contextFile);

			Resource contextResource = EcorePlatformUtil.getResource(contextFile);
			assertNotNull("File : " + contextFile.getName(), contextResource);

			assertFalse(contextResource.getContents().isEmpty());
			EObject contextObject = contextResource.getContents().get(0);
			assertNotNull(contextObject);

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(refWks.editingDomainUml2).getResourcesInModel(contextObject);
			assertEquals(NLS.bind(message, contextFile.getName()), contents.size(), resourcesInModelUml2.size());
			for (Resource resource : resourcesInModelUml2) {
				assertTrue(contents.contains(resource.getURI().lastSegment()));
			}
		}

	}

	/**
	 * Test method for {@link ScopingResourceSet#getResourcesInScope(Object, boolean)} with context Object is NULL
	 * 
	 * @throws Exception
	 */
	public void testGetResourcesFromNullObject() throws Exception {

		String message = "Context object: is NULL - Number of resources"; //$NON-NLS-1$
		// --------------------------------------------------------------------
		// Context: ContextObject is NULL
		{
			IProject contextObject = null;

			TransactionalEditingDomain editingDomain10 = WorkspaceEditingDomainUtil.getEditingDomain(refWks.hbProject10_A,
					Hummingbird10MMDescriptor.INSTANCE);
			assertNotNull(editingDomain10);

			TransactionalEditingDomain editingDomain20 = WorkspaceEditingDomainUtil.getEditingDomain(refWks.hbProject10_A,
					Hummingbird20MMDescriptor.INSTANCE);
			assertNotNull(editingDomain20);

			TransactionalEditingDomain editingDomainUml2 = WorkspaceEditingDomainUtil.getEditingDomain(refWks.hbProject10_A,
					UML2MMDescriptor.INSTANCE);
			assertNotNull(editingDomainUml2);

			List<Resource> resourcesInModel10 = getScopingResourceSet(editingDomain10).getResourcesInModel(contextObject);
			assertEquals(message, 0, resourcesInModel10.size());

			List<Resource> resourcesInModel20 = getScopingResourceSet(editingDomain20).getResourcesInModel(contextObject);
			assertEquals(message, 0, resourcesInModel20.size());

			List<Resource> resourcesInModelUml2 = getScopingResourceSet(editingDomainUml2).getResourcesInModel(contextObject);
			assertEquals(message, 0, resourcesInModelUml2.size());

		}

	}

	/**
	 * Test method for {@link ExtendedResourceSet#getEObject(URI, boolean)} with context aware URI
	 */
	public void testGetEObjectWithContextAwareURI() {

		// =====================================================
		// HB20, targetMM and contextURI are not NULL
		URI testUri20 = URI
				.createURI("hb:/?tgtMMD=org.eclipse.sphinx.examples.hummingbird20&ctxURI=platform:/resource/hbProject20_A#//@componentTypes.0");

		assertNotNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri20, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri20, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri20, true));

		// the component type does not exist in the context project hbProject20_C
		testUri20 = URI
				.createURI("hb:/?tgtMMD=org.eclipse.sphinx.examples.hummingbird20&ctxURI=platform:/resource/hbProject20_C#//@componentTypes.0");

		assertNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri20, true));
		assertNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri20, true));
		assertNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri20, true));

		// =====================================================
		// HB20, contextURI is NULL
		URI testUri20B = URI.createURI("hb:/?tgtMMD=org.eclipse.sphinx.examples.hummingbird20#//@componentTypes.0");

		assertNotNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri20B, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri20B, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri20B, true));

		// target metamodel is not HB20
		testUri20B = URI.createURI("hb:/?tgtMMD=org.eclipse.sphinx.examples.hummingbird10#//@componentTypes.0");

		assertNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri20B, true));
		assertNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri20B, true));
		assertNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri20B, true));

		// =====================================================
		// HB20, targetMM is NULL
		URI testUri20C = URI.createURI("hb:/?ctxURI=platform:/resource/hbProject20_A#//@componentTypes.0");

		assertNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri20C, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri20C, true));
		assertNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri20C, true));

		// the component type does not exist in the context project hbProject20_C
		testUri20C = URI.createURI("hb:/?ctxURI=platform:/resource/hbProject20_C#//@componentTypes.0");

		assertNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri20C, true));
		assertNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri20C, true));
		assertNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri20C, true));

		// =====================================================
		// HB20, targetMM and contextURI are NULL
		URI testUri20D = URI.createURI("hb:/#//@componentTypes.0");

		assertNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri20D, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri20D, true));
		assertNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri20D, true));

		// =====================================================
		// HB10
		URI testUri10A = URI.createURI("hb:/#//@interfaces.0");

		assertNotNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri10A, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri10A, true));
		assertNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri10A, true));

		// =====================================================
		// UML, targetMM and contextURI are not NULL
		URI testUml2Uri = URI
				.createURI("/?tgtMMD=org.eclipse.sphinx.examples.uml2&ctxURI=platform:/resource/hbProject20_D#//package2/FunctionBehavior20D1_1");

		assertNotNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUml2Uri, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUml2Uri, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUml2Uri, true));

		// the uml element does not exist in the context project hbProject20_A
		testUml2Uri = URI
				.createURI("/?tgtMMD=org.eclipse.sphinx.examples.uml2&ctxURI=platform:/resource/hbProject20_A#//package2/FunctionBehavior20D1_1");

		assertNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUml2Uri, true));
		assertNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUml2Uri, true));
		assertNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUml2Uri, true));

		// =====================================================
		// UML, contextURI is null
		URI testUml2UriA = URI.createURI("/?tgtMMD=org.eclipse.sphinx.examples.uml2#//package2/FunctionBehavior20D1_1");

		assertNotNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUml2UriA, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUml2UriA, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUml2UriA, true));

		// target metamodel is not UML
		testUml2UriA = URI.createURI("/?tgtMMD=org.eclipse.sphinx.examples.hummingbird20#//package2/FunctionBehavior20D1_1");

		assertNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri20B, true));
		assertNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri20B, true));
		assertNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri20B, true));

		// =====================================================
		// UML, targetMM is NULL
		URI testUml2UriB = URI.createURI("/?ctxURI=platform:/resource/hbProject20_D#//package2/FunctionBehavior20D1_1");

		assertNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUml2UriB, true));
		assertNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUml2UriB, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUml2UriB, true));

		// the element does not exist in the context project hbProject20_A
		testUml2UriB = URI.createURI("hb:/?ctxURI=platform:/resource/hbProject20_A#//package2/FunctionBehavior20D1_1");

		assertNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUml2UriB, true));
		assertNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUml2UriB, true));
		assertNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUml2UriB, true));

		// =====================================================
		// UML, targetMM and contextURI are NULL
		URI testUml2UriC = URI.createURI("/#//package2/FunctionBehavior20D1_1");

		// the expected UML element exists in project hbProject20_D
		assertNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUml2UriC, true));
		assertNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUml2UriC, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUml2UriC, true));

		// =======================================================
		// platform URI
		// HB20, targetMM and contextURI are not NULL
		URI testUri20A = URI
				.createURI("platform:/resource/hbProject20_A/hbFile20_20A_2.typemodel?tgtMMD=org.eclipse.sphinx.examples.hummingbird20&ctxURI=platform:/resource/hbProject20_A#//@componentTypes.0");

		assertNotNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri20A, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri20A, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri20A, true));

		// platform URI, the component type does not exist in the context project hbProject20_C
		testUri20A = URI
				.createURI("platform:/resource/hbProject20_A/hbFile20_20A_2.typemodel?tgtMMD=org.eclipse.sphinx.examples.hummingbird20&ctxURI=platform:/resource/hbProject20_C#//@componentTypes.0");

		assertNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri20A, true));
		assertNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri20A, true));
		assertNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri20A, true));

		// =====================================================
		// HB20, contextURI is NULL
		testUri20B = URI
				.createURI("platform:/resource/hbProject20_D/hbFile20_20D_2.typemodel?tgtMMD=org.eclipse.sphinx.examples.hummingbird20#//@componentTypes.0");

		assertNotNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri20B, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri20B, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri20B, true));

		// platform URI, target metamodel is not HB20
		testUri20B = URI
				.createURI("platform:/resource/hbProject20_C/hbFile20_20C_1.instancemodel?tgtMMD=org.eclipse.sphinx.examples.hummingbird10#//@componentTypes.0");

		assertNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri20B, true));
		assertNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri20B, true));
		assertNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri20B, true));

		// =====================================================
		// HB20, targetMM is NULL
		testUri20C = URI
				.createURI("platform:/resource/hbProject20_A/hbFile20_20A_2.typemodel?ctxURI=platform:/resource/hbProject20_A#//@componentTypes.0");

		assertNotNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri20C, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri20C, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri20C, true));

		// the component type does not exist in the context project hbProject20_C
		testUri20C = URI
				.createURI("platform:/resource/hbProject20_A/hbFile20_20A_2.typemodel?ctxURI=platform:/resource/hbProject20_C#//@componentTypes.0");

		assertNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri20C, true));
		assertNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri20C, true));
		assertNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri20C, true));

		// =====================================================
		// HB10, platform URI
		testUri10A = URI.createURI("platform:/resource/hbProject10_A/hbFile10_10A_4.hummingbird#//@interfaces.0");

		assertNotNull(getScopingResourceSet(refWks.editingDomain10).getEObject(testUri10A, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomain20).getEObject(testUri10A, true));
		assertNotNull(getScopingResourceSet(refWks.editingDomainUml2).getEObject(testUri10A, true));
	}

	/**
	 * Test method for {@link ScopingResourceSet#getEObject(URI, boolean)}. <br>
	 * This test covers {@link ScopingResourceSet#getEObjectInScope(URI, boolean, EObject)} with contextObject is NULL<br>
	 */
	public void testGetEObjectInScopeWithNullContextObject() throws Exception {

		ScopingResourceSet scopingResourceSet = new ScopingResourceSetImpl();

		// create an XMI resource inside filteringResourceSet
		Model model = UMLFactory.eINSTANCE.createModel();
		model.setName("model"); //$NON-NLS-1$
		org.eclipse.uml2.uml.Package pack1 = UMLFactory.eINSTANCE.createPackage();
		pack1.setName("Package1");
		model.getPackagedElements().add(pack1);

		String newUml2ResName = "uml2.xml"; //$NON-NLS-1$
		URI newUml2ResourceURI = URI.createPlatformResourceURI("/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/" + newUml2ResName, //$NON-NLS-1$ //$NON-NLS-2$
				true);
		EcoreResourceUtil.saveNewModelResource(scopingResourceSet, newUml2ResourceURI, UMLPackage.eCONTENT_TYPE, model,
				EcoreResourceUtil.getDefaultSaveOptions());
		assertEquals(1, scopingResourceSet.getResources().size());
		URI uri1 = EcoreUtil.getURI(pack1);
		EObject eObject = scopingResourceSet.getEObject(uri1, true);
		assertNotNull("Cannot get Eobject with uri and without contextObject" + uri1.toString(), eObject);

		// Create a HB10 resource inside filteringResourceSet
		Application hb10Application = Hummingbird10Factory.eINSTANCE.createApplication();
		hb10Application.setName("Application");
		Component component = Hummingbird10Factory.eINSTANCE.createComponent();
		component.setName("component");
		hb10Application.getComponents().add(component);
		Parameter param = Hummingbird10Factory.eINSTANCE.createParameter();
		param.setName("param");
		component.getParameters().add(param);

		String newHummingbirdResName = "newResource.hummingbird"; //$NON-NLS-1$
		URI newResourceURI = URI.createPlatformResourceURI("/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/" + newHummingbirdResName, //$NON-NLS-1$ //$NON-NLS-2$
				true);
		EcoreResourceUtil.saveNewModelResource(scopingResourceSet, newResourceURI, Hummingbird10Package.eCONTENT_TYPE, hb10Application,
				EcoreResourceUtil.getDefaultSaveOptions());

		assertEquals(2, scopingResourceSet.getResources().size());

		URI uri2 = EcoreUtil.getURI(param);

		eObject = scopingResourceSet.getEObject(uri2, true);
		assertNotNull("Cannot get EObject with uri and without contextObject" + uri2.toString(), eObject);
	}

	public void testGetEobjectInScopeWithNullUri() {

		IFile contextFile = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertNotNull(contextFile);

		Resource contextResource = EcorePlatformUtil.getResource(contextFile);
		assertNotNull(contextResource);

		// The context object use for filtering
		assertFalse(contextResource.getContents().isEmpty());
		EObject contextObject = contextResource.getContents().get(0);
		assertNotNull(contextObject);

		String message = "Given URI is NULL, context object is not NULL. Object return is \"{0}\" "; //$NON-NLS-1$
		assertNull(NLS.bind(message, EcoreUtil.getURI(contextObject)), getScopingResourceSet(refWks.editingDomain10).getEObject(null, true));
		assertNull(NLS.bind(message, EcoreUtil.getURI(contextObject)), getScopingResourceSet(refWks.editingDomain20).getEObject(null, true));
	}

	// ==================================
	private class Data {

		private List<String> contents = new ArrayList<String>();
		private String fileName;
		private IProject project;
		private int expectedNumber;

		private Data(IProject project, String fileName) {
			this.project = project;
			this.fileName = fileName;
		}

		private Data(IProject project, String fileName, List<String> contents) {
			this.project = project;
			this.fileName = fileName;
			this.contents = contents;
		}

		private Data(IProject project, String fileName, List<String> contents, int expectedNumber) {
			this(project, fileName, contents);
			this.expectedNumber = expectedNumber;
		}
	}
}
