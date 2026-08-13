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
 *     Siemens - [574930] Model load manager extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.workspace.integration.internal.loading;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.workspace.internal.loading.ModelLoadJob;
import org.eclipse.sphinx.emf.workspace.loading.LoadJobScheduler;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultModelLoadOperation;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

/**
 *
 */
@SuppressWarnings({ "restriction" })
public class ModelLoadJobTest extends AbstractLoadJobTest {

	public ModelLoadJobTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		setProjectsClosedOnStartup(false);
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of project {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying
	 * {@linkplain Hummingbird10MMDescriptor} as meta-model descriptor (consider or not its referenced projects is not
	 * relevant in the context of this test and corresponding boolean will be kept to <code>false</code>).
	 */
	public void testShouldCreateJob_20B_hb20RD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IProject> projects = Collections.singletonList(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		Collection<IModelDescriptor> modelDescriptors = ModelDescriptorRegistry.INSTANCE
				.getModels(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		IModelDescriptor modelDescriptor = null;
		for (IModelDescriptor descriptor : modelDescriptors) {
			if (descriptor.getMetaModelDescriptor() == Hummingbird20MMDescriptor.INSTANCE) {
				modelDescriptor = descriptor;
				break;
			}
		}
		assertNotNull(modelDescriptor);

		ModelLoadManager.INSTANCE.loadModel(modelDescriptor, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultModelLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[5];
		// The messages to display in case of violated assertions
		String[] messages = new String[5];

		int index = -1;
		{ // 0
			IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_NOT_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 1
			IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 2
			IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 3
			IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;

			messages[++index] = getMessage(SHOULD_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 4
			IMetaModelDescriptor mmDescriptor = null;

			messages[++index] = getMessage(SHOULD_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();

		// Check assertions
		assertFalse(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of project {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying
	 * {@linkplain Hummingbird20MMDescriptor} as meta-model descriptor (consider or not its referenced projects is not
	 * relevant in the context of this test and corresponding boolean will be kept to <code>false</code>).
	 */
	public void testShouldCreateJob_20B_Hb20RD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IProject> projects = Collections.singletonList(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		Collection<IModelDescriptor> modelDescriptors = ModelDescriptorRegistry.INSTANCE
				.getModels(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		IModelDescriptor modelDescriptor = null;
		for (IModelDescriptor descriptor : modelDescriptors) {
			if (descriptor.getMetaModelDescriptor() == Hummingbird20MMDescriptor.INSTANCE) {
				modelDescriptor = descriptor;
				break;
			}
		}
		assertNotNull(modelDescriptor);

		ModelLoadManager.INSTANCE.loadModel(modelDescriptor, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultModelLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[5];
		// The messages to display in case of violated assertions
		String[] messages = new String[5];

		int index = -1;
		{ // 0
			IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 1
			IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_NOT_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 2
			IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 3
			IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;

			messages[++index] = getMessage(SHOULD_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 4
			IMetaModelDescriptor mmDescriptor = null;

			messages[++index] = getMessage(SHOULD_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();

		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertFalse(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of project {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying
	 * {@linkplain UML2MMDescriptor} as meta-model descriptor (consider or not its referenced projects is not relevant
	 * in the context of this test and corresponding boolean will be kept to <code>false</code>).
	 */
	public void testShouldCreateJob_20B_UML2MMD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IProject> projects = Collections.singletonList(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		Collection<IModelDescriptor> modelDescriptors = ModelDescriptorRegistry.INSTANCE
				.getModels(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		IModelDescriptor modelDescriptor = null;
		for (IModelDescriptor descriptor : modelDescriptors) {
			if (descriptor.getMetaModelDescriptor() == UML2MMDescriptor.INSTANCE) {
				modelDescriptor = descriptor;
				break;
			}
		}
		assertNotNull(modelDescriptor);

		ModelLoadManager.INSTANCE.loadModel(modelDescriptor, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultModelLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[5];
		// The messages to display in case of violated assertions
		String[] messages = new String[5];

		int index = -1;
		{ // 0
			IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 1
			IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 2
			IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_NOT_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 3
			IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;

			messages[++index] = getMessage(SHOULD_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 4
			IMetaModelDescriptor mmDescriptor = null;

			messages[++index] = getMessage(SHOULD_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();

		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertFalse(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of project {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying
	 * {@linkplain MetaModelDescriptorRegistry#ANY_MM} as meta-model descriptor (consider or not its referenced projects
	 * is not relevant in the context of this test and corresponding boolean will be kept to <code>false</code>).
	 *
	 * @throws Exception
	 */
	public void testShouldCreateJob_20B_ANYMM() throws Exception {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IProject> projects = Collections.singletonList(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		Collection<IModelDescriptor> modelDescriptors = ModelDescriptorRegistry.INSTANCE
				.getModels(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		IModelDescriptor modelDescriptor = null;
		for (IModelDescriptor descriptor : modelDescriptors) {
			if (descriptor.getMetaModelDescriptor() == Hummingbird20MMDescriptor.INSTANCE) {
				modelDescriptor = descriptor;
				break;
			}
		}
		assertNotNull(modelDescriptor);

		ModelLoadManager.INSTANCE.loadModel(modelDescriptor, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultModelLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[5];
		// The messages to display in case of violated assertions
		String[] messages = new String[5];

		int index = -1;
		{ // 0
			IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_NOT_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 1
			IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_NOT_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 2
			IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_NOT_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 3
			IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;

			messages[++index] = getMessage(SHOULD_NOT_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 4
			IMetaModelDescriptor mmDescriptor = null;

			messages[++index] = getMessage(SHOULD_NOT_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();

		// Check assertions
		assertFalse(messages[0], shouldCreateJob[0]);
		assertFalse(messages[1], shouldCreateJob[1]);
		assertFalse(messages[2], shouldCreateJob[2]);
		assertFalse(messages[3], shouldCreateJob[3]);
		assertFalse(messages[4], shouldCreateJob[4]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of project {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying
	 * <code>null</code> as meta-model descriptor (consider or not its referenced projects is not relevant in the
	 * context of this test and corresponding boolean will be kept to <code>false</code>).
	 */
	public void testShouldCreateJob_20B_null() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IProject> projects = Collections.singletonList(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		Collection<IModelDescriptor> modelDescriptors = ModelDescriptorRegistry.INSTANCE
				.getModels(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		IModelDescriptor modelDescriptor = null;
		for (IModelDescriptor descriptor : modelDescriptors) {
			if (descriptor.getMetaModelDescriptor() == Hummingbird20MMDescriptor.INSTANCE) {
				modelDescriptor = descriptor;
				break;
			}
		}
		assertNotNull(modelDescriptor);

		ModelLoadManager.INSTANCE.loadModel(modelDescriptor, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultModelLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[5];
		// The messages to display in case of violated assertions
		String[] messages = new String[5];

		int index = -1;
		{ // 0
			IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_NOT_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 1
			IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_NOT_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 2
			IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;

			messages[++index] = getMessage(SHOULD_NOT_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 3
			IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;

			messages[++index] = getMessage(SHOULD_NOT_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}
		{ // 4
			IMetaModelDescriptor mmDescriptor = null;

			messages[++index] = getMessage(SHOULD_NOT_CREATE, projects, mmDescriptor);

			DefaultModelLoadOperation defaultModelLoadOperation = new DefaultModelLoadOperation(modelDescriptor, false);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultModelLoadOperation);
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();

		// Check assertions
		assertFalse(messages[0], shouldCreateJob[0]);
		assertFalse(messages[1], shouldCreateJob[1]);
		assertFalse(messages[2], shouldCreateJob[2]);
		assertFalse(messages[3], shouldCreateJob[3]);
		assertFalse(messages[4], shouldCreateJob[4]);

		// Ends the test by verifying that everything is fine
		finish();
	}
}
