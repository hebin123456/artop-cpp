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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.workspace.internal.loading.ModelLoadJob;
import org.eclipse.sphinx.emf.workspace.loading.LoadJobScheduler;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultFileLoadOperation;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

/**
 *
 */
@SuppressWarnings({ "restriction" })
public class FileLoadJobTest extends AbstractLoadJobTest {
	private IFile hbFile10_10A_1;
	private IFile hbFile10_10A_2;
	private IFile hbFile10_10A_3;

	private IFile hbFile10_10B_1;
	private IFile hbFile10_10B_2;
	private IFile hbFile10_10B_3;

	private IFile hbFile20_20B_1;
	private IFile hbFile20_20B_2;
	private IFile hbFile20_20B_3;

	private IFile uml2File_20B_1;
	private IFile uml2File_20B_2;
	private IFile uml2File_20B_3;

	public FileLoadJobTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_B);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		hbFile10_10A_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		hbFile10_10A_2 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		hbFile10_10A_3 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		hbFile10_10B_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_B,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1);
		hbFile10_10B_2 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_B,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2);
		hbFile10_10B_3 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_B,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3);

		hbFile20_20B_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		hbFile20_20B_2 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		hbFile20_20B_3 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);

		uml2File_20B_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B,
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		uml2File_20B_2 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B,
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		uml2File_20B_3 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B,
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of file {@linkplain DefaultTestReferenceWorkspace#HB_FILE_NAME_10_10A_1} specifying
	 * {@linkplain MetaModelDescriptorRegistry#ANY_MM} as meta-model descriptor.
	 */
	public void testShouldCreateJob_10A_oneFile() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = Collections.singletonList(hbFile10_10A_1);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, MetaModelDescriptorRegistry.ANY_MM, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[6];
		// The messages to display in case of violated assertions
		String[] messages = new String[6];

		IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;

		int index = -1;
		{ // 0
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_1);

			messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 1
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_2);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 2
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_3);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10B_1);
			files.add(hbFile10_10B_2);
			files.add(hbFile10_10B_3);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 5
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();

		// Check assertions
		assertFalse(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of files from project {@linkplain DefaultTestReferenceWorkspace#arProject10_A} specifying
	 * {@linkplain MetaModelDescriptorRegistry#ANY_MM} as meta-model descriptor.
	 */
	public void testCoveredByExistingLoadJob_10A_ANYMM() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(hbFile10_10A_1);
		filesToLoad.add(hbFile10_10A_2);
		filesToLoad.add(hbFile10_10A_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, MetaModelDescriptorRegistry.ANY_MM, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[12];
		// The messages to display in case of violated assertions
		String[] messages = new String[12];

		int index = -1;

		IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;

		{ // 0
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_1);

			messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 1
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_2);

			messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 2
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_3);

			messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10B_1);
			files.add(hbFile10_10B_2);
			files.add(hbFile10_10B_3);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 5
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}

		mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;

		{ // 6
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_1);

			messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 7
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_2);

			messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 8
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_3);

			messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 9
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 10
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10B_1);
			files.add(hbFile10_10B_2);
			files.add(hbFile10_10B_3);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 11
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();

		// Check assertions
		assertFalse(messages[0], shouldCreateJob[0]);
		assertFalse(messages[1], shouldCreateJob[1]);
		assertFalse(messages[2], shouldCreateJob[2]);
		assertFalse(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);
		assertFalse(messages[6], shouldCreateJob[6]);
		assertFalse(messages[7], shouldCreateJob[7]);
		assertFalse(messages[8], shouldCreateJob[8]);
		assertFalse(messages[9], shouldCreateJob[9]);
		assertTrue(messages[10], shouldCreateJob[10]);
		assertTrue(messages[11], shouldCreateJob[11]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of files from project {@linkplain DefaultTestReferenceWorkspace#arProject10_A} specifying
	 * {@linkplain Hummingbird10MMDescriptor} as meta-model descriptor.
	 */
	public void testShouldCreateJob_10A_Hb10RD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(hbFile10_10A_1);
		filesToLoad.add(hbFile10_10A_2);
		filesToLoad.add(hbFile10_10A_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, Hummingbird10MMDescriptor.INSTANCE, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[12];
		// The messages to display in case of violated assertions
		String[] messages = new String[12];

		int index = -1;

		IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;

		{ // 0
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_1);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 1
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_2);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 2
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_3);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10B_1);
			files.add(hbFile10_10B_2);
			files.add(hbFile10_10B_3);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 5
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}

		mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;

		{ // 6
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_1);

			messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 7
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_2);

			messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 8
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_3);

			messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 9
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 10
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10B_1);
			files.add(hbFile10_10B_2);
			files.add(hbFile10_10B_3);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 11
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);

			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();

		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);
		assertFalse(messages[6], shouldCreateJob[6]);
		assertFalse(messages[7], shouldCreateJob[7]);
		assertFalse(messages[8], shouldCreateJob[8]);
		assertFalse(messages[9], shouldCreateJob[9]);
		assertTrue(messages[10], shouldCreateJob[10]);
		assertTrue(messages[11], shouldCreateJob[11]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of files from project {@linkplain DefaultTestReferenceWorkspace#arProject10_A} specifying
	 * {@linkplain UML2MMDescriptor} as meta-model descriptor.
	 */
	public void testShouldCreateJob_10A_UML2MMD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(hbFile10_10A_1);
		filesToLoad.add(hbFile10_10A_2);
		filesToLoad.add(hbFile10_10A_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, UML2MMDescriptor.INSTANCE, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[6];
		// The messages to display in case of violated assertions
		String[] messages = new String[6];

		IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;

		int index = -1;
		{ // 0
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_1);
			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 1
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_2);
			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 2
			Collection<IFile> files = Collections.singletonList(hbFile10_10A_3);
			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);
			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10B_1);
			files.add(hbFile10_10B_2);
			files.add(hbFile10_10B_3);
			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}
		{ // 5
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
			DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
			shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();

		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of files from project {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying
	 * {@linkplain MetaModelDescriptorRegistry#ANY_MM} as meta-model descriptor.
	 */
	public void testShouldCreateJob_20B_AllFiles_ANYMM() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(hbFile20_20B_1);
		filesToLoad.add(hbFile20_20B_2);
		filesToLoad.add(hbFile20_20B_3);
		filesToLoad.add(uml2File_20B_1);
		filesToLoad.add(uml2File_20B_2);
		filesToLoad.add(uml2File_20B_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, MetaModelDescriptorRegistry.ANY_MM, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();

		// Check assertions
		assertFalse(messages[0], shouldCreateJob[0]);
		assertFalse(messages[1], shouldCreateJob[1]);
		assertFalse(messages[2], shouldCreateJob[2]);
		assertFalse(messages[3], shouldCreateJob[3]);
		assertFalse(messages[4], shouldCreateJob[4]);
		assertFalse(messages[5], shouldCreateJob[5]);
		assertFalse(messages[6], shouldCreateJob[6]);
		assertFalse(messages[7], shouldCreateJob[7]);
		assertFalse(messages[8], shouldCreateJob[8]);
		assertFalse(messages[9], shouldCreateJob[9]);
		assertFalse(messages[10], shouldCreateJob[10]);
		assertFalse(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of files from project {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying
	 * {@linkplain Hummingbird10MMDescriptor} as meta-model descriptor.
	 */
	public void testShouldCreateJob_20B_AllFiles_Hb10RD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(hbFile20_20B_1);
		filesToLoad.add(hbFile20_20B_2);
		filesToLoad.add(hbFile20_20B_3);
		filesToLoad.add(uml2File_20B_1);
		filesToLoad.add(uml2File_20B_2);
		filesToLoad.add(uml2File_20B_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, Hummingbird10MMDescriptor.INSTANCE, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();

		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertFalse(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertFalse(messages[5], shouldCreateJob[5]);
		assertTrue(messages[6], shouldCreateJob[6]);
		assertTrue(messages[7], shouldCreateJob[7]);
		assertTrue(messages[8], shouldCreateJob[8]);
		assertFalse(messages[9], shouldCreateJob[9]);
		assertTrue(messages[10], shouldCreateJob[10]);
		assertTrue(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of files from project {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying
	 * {@linkplain Hummingbird20MMDescriptor} as meta-model descriptor.
	 */
	public void testShouldCreateJob_20B_AllFiles_Hb20RD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(hbFile20_20B_1);
		filesToLoad.add(hbFile20_20B_2);
		filesToLoad.add(hbFile20_20B_3);
		filesToLoad.add(uml2File_20B_1);
		filesToLoad.add(uml2File_20B_2);
		filesToLoad.add(uml2File_20B_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, Hummingbird20MMDescriptor.INSTANCE, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();

		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertFalse(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);
		assertFalse(messages[6], shouldCreateJob[6]);
		assertTrue(messages[7], shouldCreateJob[7]);
		assertTrue(messages[8], shouldCreateJob[8]);
		assertTrue(messages[9], shouldCreateJob[9]);
		assertFalse(messages[10], shouldCreateJob[10]);
		assertTrue(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of files from project {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying
	 * {@linkplain UML2MMDescriptor} as meta-model descriptor.
	 */
	public void testShouldCreateJob_20B_AllFiles_UML2MMD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(hbFile20_20B_1);
		filesToLoad.add(hbFile20_20B_2);
		filesToLoad.add(hbFile20_20B_3);
		filesToLoad.add(uml2File_20B_1);
		filesToLoad.add(uml2File_20B_2);
		filesToLoad.add(uml2File_20B_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, UML2MMDescriptor.INSTANCE, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();

		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertFalse(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);
		assertTrue(messages[6], shouldCreateJob[6]);
		assertFalse(messages[7], shouldCreateJob[7]);
		assertTrue(messages[8], shouldCreateJob[8]);
		assertTrue(messages[9], shouldCreateJob[9]);
		assertTrue(messages[10], shouldCreateJob[10]);
		assertFalse(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of Hummingbird 10 files from project
	 * {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying
	 * {@linkplain MetaModelDescriptorRegistry#ANY_MM} as meta-model descriptor.
	 */
	public void testShouldCreateJob_20B_Hb10Files_ANYMM() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		ModelLoadManager.INSTANCE.loadFiles(Collections.<IFile> emptyList(), MetaModelDescriptorRegistry.ANY_MM, true, null);

		// Verify prerequisites assertions
		assertNoLoadJobIsSleeping();

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);
			// 1011 1010 1001 1111 1100
			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();
		// 1111 1110 1001 1111 1100
		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);
		assertTrue(messages[6], shouldCreateJob[6]);
		assertTrue(messages[7], shouldCreateJob[7]);
		assertTrue(messages[8], shouldCreateJob[8]);
		assertTrue(messages[9], shouldCreateJob[9]);
		assertTrue(messages[10], shouldCreateJob[10]);
		assertTrue(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of Hummingbird 10 files from project
	 * {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying {@linkplain Hummingbird10MMDescriptor} as
	 * meta-model descriptor.
	 */
	public void testShouldCreateJob_20B_Hb10Files_Hb10RD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		ModelLoadManager.INSTANCE.loadFiles(Collections.<IFile> emptyList(), Hummingbird10MMDescriptor.INSTANCE, true, null);

		// Verify prerequisites assertions
		assertNoLoadJobIsSleeping();

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();
		// 1011 1010 1001 1111 1100
		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);
		assertTrue(messages[6], shouldCreateJob[6]);
		assertTrue(messages[7], shouldCreateJob[7]);
		assertTrue(messages[8], shouldCreateJob[8]);
		assertTrue(messages[9], shouldCreateJob[9]);
		assertTrue(messages[10], shouldCreateJob[10]);
		assertTrue(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of Hummingbird 10 files from project
	 * {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying {@linkplain Hummingbird20MMDescriptor} as
	 * meta-model descriptor.
	 */
	public void testShouldCreateJob_20B_Hb10Files_Hb20RD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		ModelLoadManager.INSTANCE.loadFiles(Collections.<IFile> emptyList(), Hummingbird20MMDescriptor.INSTANCE, true, null);

		// Verify prerequisites assertions
		assertNoLoadJobIsSleeping();

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();
		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);
		assertTrue(messages[6], shouldCreateJob[6]);
		assertTrue(messages[7], shouldCreateJob[7]);
		assertTrue(messages[8], shouldCreateJob[8]);
		assertTrue(messages[9], shouldCreateJob[9]);
		assertTrue(messages[10], shouldCreateJob[10]);
		assertTrue(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of Hummingbird 10 files from project
	 * {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying {@linkplain UML2MMDescriptor} as meta-model
	 * descriptor.
	 */
	public void testShouldCreateJob_20B_Hb10Files_UML2MMD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		ModelLoadManager.INSTANCE.loadFiles(Collections.<IFile> emptyList(), UML2MMDescriptor.INSTANCE, true, null);

		// Verify prerequisites assertions
		assertNoLoadJobIsSleeping();

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();
		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);
		assertTrue(messages[6], shouldCreateJob[6]);
		assertTrue(messages[7], shouldCreateJob[7]);
		assertTrue(messages[8], shouldCreateJob[8]);
		assertTrue(messages[9], shouldCreateJob[9]);
		assertTrue(messages[10], shouldCreateJob[10]);
		assertTrue(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of Hummingbird 20 files from project
	 * {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying
	 * {@linkplain MetaModelDescriptorRegistry#ANY_MM} as meta-model descriptor.
	 */
	public void testShouldCreateJob_20B_Hb20Files_ANYMM() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(hbFile20_20B_1);
		filesToLoad.add(hbFile20_20B_2);
		filesToLoad.add(hbFile20_20B_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, MetaModelDescriptorRegistry.ANY_MM, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();
		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertFalse(messages[4], shouldCreateJob[4]);
		assertFalse(messages[5], shouldCreateJob[5]);
		assertFalse(messages[6], shouldCreateJob[6]);
		assertFalse(messages[7], shouldCreateJob[7]);
		assertTrue(messages[8], shouldCreateJob[8]);
		assertTrue(messages[9], shouldCreateJob[9]);
		assertTrue(messages[10], shouldCreateJob[10]);
		assertTrue(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of Hummingbird 20 files from project
	 * {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying {@linkplain Hummingbird10MMDescriptor} as
	 * meta-model descriptor.
	 */
	public void testShouldCreateJob_20B_Hb20Files_Hb10RD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(hbFile20_20B_1);
		filesToLoad.add(hbFile20_20B_2);
		filesToLoad.add(hbFile20_20B_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, Hummingbird10MMDescriptor.INSTANCE, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();
		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertFalse(messages[5], shouldCreateJob[5]);
		assertTrue(messages[6], shouldCreateJob[6]);
		assertTrue(messages[7], shouldCreateJob[7]);
		assertTrue(messages[8], shouldCreateJob[8]);
		assertTrue(messages[9], shouldCreateJob[9]);
		assertTrue(messages[10], shouldCreateJob[10]);
		assertTrue(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of Hummingbird 20 files from project
	 * {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying {@linkplain Hummingbird20MMDescriptor} as
	 * meta-model descriptor.
	 */
	public void testShouldCreateJob_20B_Hb20Files_Hb20RD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(hbFile20_20B_1);
		filesToLoad.add(hbFile20_20B_2);
		filesToLoad.add(hbFile20_20B_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, Hummingbird20MMDescriptor.INSTANCE, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();
		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);
		assertFalse(messages[6], shouldCreateJob[6]);
		assertTrue(messages[7], shouldCreateJob[7]);
		assertTrue(messages[8], shouldCreateJob[8]);
		assertTrue(messages[9], shouldCreateJob[9]);
		assertTrue(messages[10], shouldCreateJob[10]);
		assertTrue(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of Hummingbird 20 files from project
	 * {@linkplain DefaultTestReferenceWorkspace#arProject20_B} specifying {@linkplain UML2MMDescriptor} as meta-model
	 * descriptor.
	 */
	public void testShouldCreateJob_20B_Hb20Files_UML2MMD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(hbFile20_20B_1);
		filesToLoad.add(hbFile20_20B_2);
		filesToLoad.add(hbFile20_20B_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, UML2MMDescriptor.INSTANCE, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();
		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);
		assertTrue(messages[6], shouldCreateJob[6]);
		assertFalse(messages[7], shouldCreateJob[7]);
		assertTrue(messages[8], shouldCreateJob[8]);
		assertTrue(messages[9], shouldCreateJob[9]);
		assertTrue(messages[10], shouldCreateJob[10]);
		assertTrue(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of Uml2 files from project {@linkplain DefaultTestReferenceWorkspace#arProject20_B}
	 * specifying {@linkplain MetaModelDescriptorRegistry#ANY_MM} as meta-model descriptor.
	 */
	public void testShouldCreateJob_20B_UML2Files_ANYMM() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(uml2File_20B_1);
		filesToLoad.add(uml2File_20B_2);
		filesToLoad.add(uml2File_20B_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, MetaModelDescriptorRegistry.ANY_MM, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();
		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);
		assertTrue(messages[6], shouldCreateJob[6]);
		assertTrue(messages[7], shouldCreateJob[7]);
		assertFalse(messages[8], shouldCreateJob[8]);
		assertFalse(messages[9], shouldCreateJob[9]);
		assertFalse(messages[10], shouldCreateJob[10]);
		assertFalse(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	//
	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of Uml2 files from project {@linkplain DefaultTestReferenceWorkspace#arProject20_B}
	 * specifying {@linkplain Hummingbird10MMDescriptor} as meta-model descriptor.
	 */
	public void testShouldCreateJob_20B_Uml2Files_Hb10RD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(uml2File_20B_1);
		filesToLoad.add(uml2File_20B_2);
		filesToLoad.add(uml2File_20B_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, Hummingbird10MMDescriptor.INSTANCE, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();
		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);
		assertTrue(messages[6], shouldCreateJob[6]);
		assertTrue(messages[7], shouldCreateJob[7]);
		assertTrue(messages[8], shouldCreateJob[8]);
		assertFalse(messages[9], shouldCreateJob[9]);
		assertTrue(messages[10], shouldCreateJob[10]);
		assertTrue(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of Uml2 files from project {@linkplain DefaultTestReferenceWorkspace#arProject20_B}
	 * specifying {@linkplain Hummingbird20MMDescriptor} as meta-model descriptor.
	 */
	public void testShouldCreateJob_20B_Uml2Files_Hb20RD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(uml2File_20B_1);
		filesToLoad.add(uml2File_20B_2);
		filesToLoad.add(uml2File_20B_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, Hummingbird20MMDescriptor.INSTANCE, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();
		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);
		assertTrue(messages[6], shouldCreateJob[6]);
		assertTrue(messages[7], shouldCreateJob[7]);
		assertTrue(messages[8], shouldCreateJob[8]);
		assertTrue(messages[9], shouldCreateJob[9]);
		assertFalse(messages[10], shouldCreateJob[10]);
		assertTrue(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}

	/**
	 * Test method for
	 * {@linkplain ModelLoadJob#shouldCreateJob(java.util.Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)}
	 * <p>
	 * Test made on loading of Uml2 files from project {@linkplain DefaultTestReferenceWorkspace#arProject20_B}
	 * specifying {@linkplain UML2MMDescriptor} as meta-model descriptor.
	 */
	public void testShouldCreateJob_20B_Uml2Files_UML2MMD() {

		// Local initialization of this test
		LoadJobScheduler loadJobScheduler = new LoadJobScheduler();

		Collection<IFile> filesToLoad = new ArrayList<IFile>();
		filesToLoad.add(uml2File_20B_1);
		filesToLoad.add(uml2File_20B_2);
		filesToLoad.add(uml2File_20B_3);

		ModelLoadManager.INSTANCE.loadFiles(filesToLoad, UML2MMDescriptor.INSTANCE, true, null);

		// Verify prerequisites assertions
		assertOnlyOneLoadJobIsSleeping(DefaultFileLoadOperation.class);

		// The results of the method under test
		boolean[] shouldCreateJob = new boolean[20];
		// The messages to display in case of violated assertions
		String[] messages = new String[20];

		int index = -1;

		{ // 0
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 0.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 0.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 1
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);

			{ // 1.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 1.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 2
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);

			{ // 2.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 2.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_NOT_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 3
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile20_20B_1);
			files.add(hbFile20_20B_2);
			files.add(hbFile20_20B_3);
			files.add(uml2File_20B_1);
			files.add(uml2File_20B_2);
			files.add(uml2File_20B_3);
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 3.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 3.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}
		{ // 4
			Collection<IFile> files = new ArrayList<IFile>();
			files.add(hbFile10_10A_1);
			files.add(hbFile10_10A_2);
			files.add(hbFile10_10A_3);

			{ // 4.0
				IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.ANY_MM;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.1
				IMetaModelDescriptor mmDescriptor = Hummingbird10MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.2
				IMetaModelDescriptor mmDescriptor = Hummingbird20MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
			{ // 4.3
				IMetaModelDescriptor mmDescriptor = UML2MMDescriptor.INSTANCE;
				messages[++index] = getMessage(SHOULD_CREATE, files, mmDescriptor);
				DefaultFileLoadOperation defaultFileLoadOperation = new DefaultFileLoadOperation(files, mmDescriptor);
				shouldCreateJob[index] = !loadJobScheduler.coveredByExistingLoadJob(defaultFileLoadOperation);
			}
		}

		// Wake up the model load job that we put sleeping before
		wakeUp();
		// Check assertions
		assertTrue(messages[0], shouldCreateJob[0]);
		assertTrue(messages[1], shouldCreateJob[1]);
		assertTrue(messages[2], shouldCreateJob[2]);
		assertTrue(messages[3], shouldCreateJob[3]);
		assertTrue(messages[4], shouldCreateJob[4]);
		assertTrue(messages[5], shouldCreateJob[5]);
		assertTrue(messages[6], shouldCreateJob[6]);
		assertTrue(messages[7], shouldCreateJob[7]);
		assertTrue(messages[8], shouldCreateJob[8]);
		assertTrue(messages[9], shouldCreateJob[9]);
		assertTrue(messages[10], shouldCreateJob[10]);
		assertFalse(messages[11], shouldCreateJob[11]);
		assertTrue(messages[12], shouldCreateJob[12]);
		assertTrue(messages[13], shouldCreateJob[13]);
		assertTrue(messages[14], shouldCreateJob[14]);
		assertTrue(messages[15], shouldCreateJob[15]);
		assertTrue(messages[16], shouldCreateJob[16]);
		assertTrue(messages[17], shouldCreateJob[17]);
		assertTrue(messages[18], shouldCreateJob[18]);
		assertTrue(messages[19], shouldCreateJob[19]);

		// Ends the test by verifying that everything is fine
		finish();
	}
}
