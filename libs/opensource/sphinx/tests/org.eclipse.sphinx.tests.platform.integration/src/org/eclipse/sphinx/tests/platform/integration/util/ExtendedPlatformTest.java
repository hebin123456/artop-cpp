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
package org.eclipse.sphinx.tests.platform.integration.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.examples.hummingbird.ide.natures.HummingbirdNature;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

@SuppressWarnings("nls")
public class ExtendedPlatformTest extends DefaultIntegrationTestCase {

	public ExtendedPlatformTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
	}

	/**
	 * Test method for {@link EcorePlatformUtil#createSaveSchedulingRule(IPath modelPath) .
	 */
	public void testGetSchedulingRuleFor_IPATH() throws Exception {

		IPath path = new Path(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		ISchedulingRule Rule = ExtendedPlatform.createSaveNewSchedulingRule(path);
		IFile modelFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertTrue(Rule.contains(modelFile));

		path = new Path(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		Rule = ExtendedPlatform.createSaveNewSchedulingRule(path);
		modelFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		assertTrue(Rule.contains(modelFile));

		path = new Path(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		Rule = ExtendedPlatform.createSaveNewSchedulingRule(path);
		modelFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertTrue(Rule.contains(modelFile));

		path = new Path(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		Rule = ExtendedPlatform.createSaveNewSchedulingRule(path);
		modelFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertTrue(Rule.contains(modelFile));

		path = new Path(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		Rule = ExtendedPlatform.createSaveNewSchedulingRule(path);
		modelFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertTrue(Rule.contains(modelFile));

		path = new Path(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		Rule = ExtendedPlatform.createSaveNewSchedulingRule(path);
		modelFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertTrue(Rule.contains(modelFile));

	}

	/**
	 * Test method for {@link ExtendedPlatform#createSaveSchedulingRule(IResource)}
	 * 
	 * @throws Exception
	 */
	public void testGetSchedulingRuleFor_IFILE() throws Exception {

		IFile modelFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		ISchedulingRule Rule = ExtendedPlatform.createSaveSchedulingRule(modelFile);
		assertTrue(Rule.contains(modelFile));

		modelFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		Rule = ExtendedPlatform.createSaveSchedulingRule(modelFile);
		assertTrue(Rule.contains(modelFile));

		modelFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		Rule = ExtendedPlatform.createSaveSchedulingRule(modelFile);
		assertTrue(Rule.contains(modelFile));

		modelFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		Rule = ExtendedPlatform.createSaveSchedulingRule(modelFile);
		assertTrue(Rule.contains(modelFile));

		modelFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		Rule = ExtendedPlatform.createSaveSchedulingRule(modelFile);
		assertTrue(Rule.contains(modelFile));

		modelFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		Rule = ExtendedPlatform.createSaveSchedulingRule(modelFile);
		assertTrue(Rule.contains(modelFile));

		IFolder folder = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F).getFolder(
				DefaultTestReferenceWorkspace.HB_FOLDER_NAME_10_10F_1);
		Rule = ExtendedPlatform.createCreateSchedulingRule(folder);
		assertTrue(Rule.contains(folder));

		IProject project = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
		Rule = ExtendedPlatform.createCreateSchedulingRule(project);
		assertTrue(Rule.contains(project));

		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		Rule = ExtendedPlatform.createCreateSchedulingRule(workspace);
		assertTrue(Rule.contains(workspace));
	}

	public void testGetRootProjects() throws Exception {

		Collection<IProject> rootProjects = ExtendedPlatform.getRootProjects();
		assertEquals(4, rootProjects.size());
		assertTrue(rootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A)));

		assertTrue(rootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A)));
		assertTrue(rootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F)));
		assertFalse(rootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));
		assertFalse(rootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));

		assertTrue(rootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));
		assertFalse(rootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));

		IProject hbProject20_D = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		IProjectDescription hbProject20DDesc = hbProject20_D.getDescription();
		hbProject20DDesc.setReferencedProjects(new IProject[] { refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E) });
		hbProject20_D.setDescription(hbProject20DDesc, null);

		rootProjects = ExtendedPlatform.getRootProjects();
		assertEquals(6, rootProjects.size());
		assertTrue(rootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A)));

		assertTrue(rootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));
		assertTrue(rootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));

		assertTrue(rootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A)));
		assertTrue(rootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F)));
		assertFalse(rootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));
		assertTrue(rootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));

	}

	/**
	 * Test method for {@link ExtendedPlatform#getAllFiles(org.eclipse.core.resources.IFolder)}
	 */
	public void testGetAllFilesOfFolder() {

		IFolder arproject10_F_Folder = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F).getFolder(
				DefaultTestReferenceWorkspace.HB_FOLDER_NAME_10_10F_1);
		assertEquals(3, ExtendedPlatform.getAllFiles(arproject10_F_Folder).size());

		IFile arproject10_F_File_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F).getFile(
				DefaultTestReferenceWorkspace.HB_FOLDER_NAME_10_10F_1 + IPath.SEPARATOR + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10F_1);
		IFile arproject10_F_File_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F).getFile(
				DefaultTestReferenceWorkspace.HB_FOLDER_NAME_10_10F_1 + IPath.SEPARATOR + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10F_2);
		IFile arproject10_F_File_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F).getFile(
				DefaultTestReferenceWorkspace.HB_FOLDER_NAME_10_10F_1 + IPath.SEPARATOR + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10F_3);
		assertTrue(ExtendedPlatform.getAllFiles(arproject10_F_Folder).contains(arproject10_F_File_1));
		assertTrue(ExtendedPlatform.getAllFiles(arproject10_F_Folder).contains(arproject10_F_File_2));
		assertTrue(ExtendedPlatform.getAllFiles(arproject10_F_Folder).contains(arproject10_F_File_3));

	}

	/**
	 * Test method for {@link ExtendedPlatform#getAllFiles(IProject, boolean)}
	 */
	public void getAllFilesOfProject() {

		assertEquals(5, ExtendedPlatform.getAllFiles(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E), false));
		assertEquals(10, ExtendedPlatform.getAllFiles(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E), true));
		assertEquals(7, ExtendedPlatform.getAllFiles(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D), false));
		assertEquals(14, ExtendedPlatform.getAllFiles(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D), true));
		assertEquals(5, ExtendedPlatform.getAllFiles(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F), true));

	}

	/**
	 * Test method for {@link ExtendedPlatform#getMembersSafely(org.eclipse.core.resources.IContainer)}
	 */
	public void testGetMembersSafelyOfContainer() {
		// Container is Workspace Root
		IWorkspaceRoot workspaceRoot = EcorePlugin.getWorkspaceRoot();
		IResource[] workspaceMembers = ExtendedPlatform.getMembersSafely(workspaceRoot);
		assertEquals(refWks.getReferenceProjectDescriptors().size(), workspaceMembers.length);

		// Container is Project
		IResource[] projectMembers = ExtendedPlatform.getMembersSafely(refWks.hbProject10_A);
		assertEquals(refWks.getReferenceFiles(refWks.hbProject10_A.getName()).size(), projectMembers.length);

		// Container is Folder
		IFolder folder = refWks.hbProject10_F.getFolder(DefaultTestReferenceWorkspace.HB_FOLDER_NAME_10_10F_1);
		IResource[] folderMembers = ExtendedPlatform.getMembersSafely(folder);
		assertEquals(3, folderMembers.length);

	}

	/**
	 * Test method for {@link ExtendedPlatform#isProjectDescriptionFile(org.eclipse.core.resources.IResource)}
	 */
	public void testIsProjectDescriptionFile() {

		IFile project_description = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(".project");
		assertNotNull(project_description);
		assertTrue(ExtendedPlatform.isProjectDescriptionFile(project_description));

		IFile hbProject20_D_hbfile_10_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertNotNull(hbProject20_D_hbfile_10_1);
		assertFalse(ExtendedPlatform.isProjectDescriptionFile(hbProject20_D_hbfile_10_1));
	}

	/**
	 * Test method for {@link ExtendedPlatform#isProjectPropertiesFolder(org.eclipse.core.resources.IResource)}
	 */
	public void testIsProjectPropertiesFolder() {

		IFolder project_description_folder = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F).getFolder(".settings");
		assertNotNull(project_description_folder);
		assertTrue(ExtendedPlatform.isProjectPropertiesFolder(project_description_folder));

		IFolder hbProject10_F_Folder = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F).getFolder(
				DefaultTestReferenceWorkspace.HB_FOLDER_NAME_10_10F_1);
		assertNotNull(hbProject10_F_Folder);
		assertFalse(ExtendedPlatform.isProjectPropertiesFolder(hbProject10_F_Folder));
	}

	/**
	 * Test method for {@link ExtendedPlatform#isProjectPropertiesFile(org.eclipse.core.resources.IResource)}
	 */
	public void testIsProjectPropertiesFile() {

		IFolder project_description_folder = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E).getFolder(".settings");
		assertNotNull(project_description_folder);
		IFile project_properties_file = project_description_folder.getFile("org.eclipse.sphinx.examples.hummingbird.ide.prefs");
		assertTrue(ExtendedPlatform.isProjectPropertiesFile(project_properties_file));

		IFile hbfile_10_F_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertNotNull(hbfile_10_F_1);
		assertFalse(ExtendedPlatform.isProjectPropertiesFile(hbfile_10_F_1));

	}

	private List<IProject> convertToArray(IProject[] projects) {
		List<IProject> result = new ArrayList<IProject>();
		for (IProject project : projects) {
			result.add(project);
		}
		return result;
	}

	/**
	 * Test method for {@link ExtendedPlatform#getReferencedProjectsSafely(IProject)}
	 */
	public void testGetReferencedProjectsSafely() {

		assertEquals(0,
				ExtendedPlatform.getReferencedProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A)).length);
		assertEquals(0,
				ExtendedPlatform.getReferencedProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)).length);
		assertEquals(1,
				ExtendedPlatform.getReferencedProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)).length);
		assertTrue(convertToArray(
				ExtendedPlatform.getReferencedProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)))
				.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));
		assertEquals(0,
				ExtendedPlatform.getReferencedProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F)).length);

		assertEquals(0,
				ExtendedPlatform.getReferencedProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A)).length);

		assertEquals(1,
				ExtendedPlatform.getReferencedProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)).length);
		assertTrue(convertToArray(
				ExtendedPlatform.getReferencedProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)))
				.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));

		assertEquals(1,
				ExtendedPlatform.getReferencedProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)).length);
		assertTrue(convertToArray(
				ExtendedPlatform.getReferencedProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)))
				.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));

	}

	/**
	 * Test method for {@link ExtendedPlatform#getReferencingProjectsSafely(IProject)}
	 */
	public void testGetReferencingProjectsSafely() {

		assertEquals(0,
				ExtendedPlatform.getReferencingProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A)).length);

		assertEquals(1,
				ExtendedPlatform.getReferencingProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)).length);
		assertTrue(convertToArray(
				ExtendedPlatform.getReferencingProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)))
				.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));

		assertEquals(1,
				ExtendedPlatform.getReferencingProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)).length);
		assertTrue(convertToArray(
				ExtendedPlatform.getReferencingProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)))
				.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));

		assertEquals(0,
				ExtendedPlatform.getReferencingProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F)).length);

		assertEquals(0,
				ExtendedPlatform.getReferencingProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A)).length);

		assertEquals(1,
				ExtendedPlatform.getReferencingProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)).length);
		assertTrue(convertToArray(
				ExtendedPlatform.getReferencingProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)))
				.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));

		assertEquals(0,
				ExtendedPlatform.getReferencingProjectsSafely(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)).length);

	}

	/**
	 * Test method for {@link ExtendedPlatform#getContentTypeId(IFile)}
	 */
	public void testGetContentTypeId_IFile() throws Exception {

		final QualifiedName key = ExtendedPlatform.toQualifedName(IExtendedPlatformConstants.RESOURCE_PROPERTY_CONTENT_TYPE_ID);
		final String UNSPECIFIED_CONTENT_TYPE = "org.eclipse.sphinx.platform.unspecifiedContentType"; //$NON-NLS-1$
		final String Mock_CONTENT_TYPE = "org.eclipse.sphinx.platform.mockContentType"; //$NON-NLS-1$
		final String Hummingbird10ContentType = "org.eclipse.sphinx.examples.hummingbird10.hummingbird10XMIFile";
		final String XmlContentype = "org.eclipse.core.runtime.xml";

		// ==========================================================
		// test cases :

		// #########################################
		// (1) File input null or unaccessible #
		// #########################################

		// IFile nullFile = null;
		// assertNull(ExtendedPlatform.getContentTypeId(nullFile));
		refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).close(new NullProgressMonitor());
		waitForModelLoading();

		IFile unaccessableFile = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertFalse(unaccessableFile.isAccessible());
		assertNull(ExtendedPlatform.getContentTypeId(unaccessableFile));
		// ########################################

		// #############################################################
		// (2) File input not null and sessionProperty instanceof String
		// #############################################################
		refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).open(new NullProgressMonitor());
		waitForModelLoading();

		IFile hbFile_10A_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertTrue(hbFile_10A_1.isAccessible());
		hbFile_10A_1.setSessionProperty(key, "sessionproperty");
		Object sessionProperty = hbFile_10A_1.getSessionProperty(key);
		assertNotNull(sessionProperty);
		assertTrue(sessionProperty instanceof String);

		String contentTypeId = ExtendedPlatform.getContentTypeId(hbFile_10A_1);
		assertNotNull(contentTypeId);
		assertTrue(contentTypeId.equals(sessionProperty));
		// #################################################################

		// #################################################################
		// (3) File input not null and !( sessionProperty instanceof String)
		// #################################################################
		IFile hbFile_10A_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertTrue(hbFile_10A_2.isAccessible());
		hbFile_10A_2.setSessionProperty(key, new Object());
		sessionProperty = hbFile_10A_2.getSessionProperty(key);
		assertNotNull(sessionProperty);
		assertFalse(sessionProperty instanceof String);
		contentTypeId = ExtendedPlatform.getContentTypeId(hbFile_10A_2);
		assertNotNull(contentTypeId);

		assertEquals(Hummingbird10ContentType, contentTypeId);
		// #############################################################
		// File input not null and !( sessionProperty instanceof String)
		// (4) | --> persistentProperty != null
		// #############################################################
		IFile hbFile_10A_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertNotNull(hbFile_10A_3);
		hbFile_10A_3.setSessionProperty(key, new Object());
		sessionProperty = hbFile_10A_3.getSessionProperty(key);
		assertNotNull(sessionProperty);
		assertFalse(sessionProperty instanceof String);
		String persistentProperty = hbFile_10A_3.getPersistentProperty(key);
		hbFile_10A_3.setPersistentProperty(key, Mock_CONTENT_TYPE);
		persistentProperty = hbFile_10A_3.getPersistentProperty(key);
		assertNotNull(persistentProperty);
		contentTypeId = ExtendedPlatform.getContentTypeId(hbFile_10A_3);
		assertNotNull(contentTypeId);
		assertTrue(Mock_CONTENT_TYPE.equals(contentTypeId));
		// note : return null when set UNSPECIFIED_CONTENT_TYPE
		hbFile_10A_3.setSessionProperty(key, UNSPECIFIED_CONTENT_TYPE);
		hbFile_10A_3.setPersistentProperty(key, UNSPECIFIED_CONTENT_TYPE);
		contentTypeId = ExtendedPlatform.getContentTypeId(hbFile_10A_3);
		assertNull(contentTypeId);
		// Delete test file to reset reference workspace
		synchronizedDeleteFile(hbFile_10A_3);

		// ########################################

		// ########################################
		// File input not null and !( sessionProperty instanceof String)
		// (5) | --> persistentProperty = null
		// ########################################

		IFile hbFile_3xA_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);
		assertNotNull(hbFile_3xA_3);
		hbFile_3xA_3.setSessionProperty(key, new Object());
		sessionProperty = hbFile_3xA_3.getSessionProperty(key);
		assertNotNull(sessionProperty);
		assertFalse(sessionProperty instanceof String);
		hbFile_3xA_3.setPersistentProperty(key, UNSPECIFIED_CONTENT_TYPE);
		persistentProperty = hbFile_3xA_3.getPersistentProperty(key);
		assertNotNull(persistentProperty);
		// note :return null when set UNSPECIFIED_CONTENT_TYPE
		contentTypeId = ExtendedPlatform.getContentTypeId(hbFile_3xA_3);

		assertNull("" + contentTypeId, contentTypeId);
		// assertTrue(Hummingbird20ContentType.equals(contentTypeId));
		// ########################################

		// ########################################
		// File input not null and !( sessionProperty instanceof String)
		// | --> persistentProperty = null
		// (6) | |-> File input is .project file
		// ########################################

		IFile project3x_A_description = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A).getFile(".project");
		assertNotNull(project3x_A_description);
		assertTrue(project3x_A_description.isAccessible());
		project3x_A_description.setSessionProperty(key, new Object());
		sessionProperty = project3x_A_description.getSessionProperty(key);
		assertNotNull(sessionProperty);
		assertFalse(sessionProperty instanceof String);
		persistentProperty = project3x_A_description.getPersistentProperty(key);
		assertNull(persistentProperty);
		project3x_A_description.setPersistentProperty(key, null);
		persistentProperty = project3x_A_description.getPersistentProperty(key);
		assertNull(persistentProperty);

		contentTypeId = ExtendedPlatform.getContentTypeId(project3x_A_description);

		assertNotNull("" + contentTypeId, contentTypeId);
		assertTrue("" + contentTypeId, XmlContentype.equals(contentTypeId));

		// ########################################

		// ==========================================================

	}

	/**
	 * Test method for {@link ExtendedPlatform#getProjects(String)}
	 */
	public void testGetProjectsFromNatureID() {

		Collection<IProject> hbRootProjects = ExtendedPlatform.getProjects(HummingbirdNature.ID);
		assertEquals(7, hbRootProjects.size());
		assertTrue(hbRootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A)));
		assertTrue(hbRootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));
		assertTrue(hbRootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));
		assertTrue(hbRootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F)));
		assertTrue(hbRootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A)));
		assertTrue(hbRootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));
		assertTrue(hbRootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));

		try {
			HummingbirdNature.removeFrom(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E), new NullProgressMonitor());
			HummingbirdNature.removeFrom(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F), new NullProgressMonitor());
			HummingbirdNature.removeFrom(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), new NullProgressMonitor());
		} catch (CoreException e) {
			return;
		}

		hbRootProjects = ExtendedPlatform.getProjects(HummingbirdNature.ID);
		assertEquals(4, hbRootProjects.size());
		assertTrue(hbRootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A)));
		assertTrue(hbRootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));
		assertTrue(hbRootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A)));
		assertTrue(hbRootProjects.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));
	}

	/**
	 * Test method for {@link ExtendedPlatform#getFirstRootProject(IProject)}
	 */
	public void testGetFirstRootProject() {

		assertSame(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A),
				ExtendedPlatform.getFirstRootProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A)));
		assertSame(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F),
				ExtendedPlatform.getFirstRootProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F)));
		assertSame(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E),
				ExtendedPlatform.getFirstRootProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));
		assertSame(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E),
				ExtendedPlatform.getFirstRootProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));

		assertSame(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A),
				ExtendedPlatform.getFirstRootProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A)));
		assertSame(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E),
				ExtendedPlatform.getFirstRootProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));
		assertSame(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E),
				ExtendedPlatform.getFirstRootProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));

	}

	/**
	 * Test method for {@link ExtendedPlatform#isRootProject(IProject)}
	 */
	public void testIsRootProject() {

		assertTrue(ExtendedPlatform.isRootProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A)));
		assertFalse(ExtendedPlatform.isRootProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));
		assertFalse(ExtendedPlatform.isRootProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));
		assertTrue(ExtendedPlatform.isRootProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F)));
		assertTrue(ExtendedPlatform.isRootProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A)));
		assertFalse(ExtendedPlatform.isRootProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));
		assertTrue(ExtendedPlatform.isRootProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));
	}

	/**
	 * Test method for {@link ExtendedPlatform#getAllReferencingProjects(IProject)}
	 */
	public void testGetAllReferencingProjects() {

		assertEquals(0, ExtendedPlatform.getAllReferencingProjects(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A))
				.size());
		Collection<IProject> ProjectsReferencingArProject10_D = ExtendedPlatform.getAllReferencingProjects(refWks
				.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D));
		assertNotNull(ProjectsReferencingArProject10_D);
		assertEquals(3, ProjectsReferencingArProject10_D.size());
		assertTrue(ProjectsReferencingArProject10_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));
		assertTrue(ProjectsReferencingArProject10_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));
		assertTrue(ProjectsReferencingArProject10_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));

		Collection<IProject> ProjectsReferencingArProject10_E = ExtendedPlatform.getAllReferencingProjects(refWks
				.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E));
		assertNotNull(ProjectsReferencingArProject10_E);
		assertEquals(2, ProjectsReferencingArProject10_E.size());
		assertTrue(ProjectsReferencingArProject10_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));
		assertTrue(ProjectsReferencingArProject10_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));

		assertEquals(0, ExtendedPlatform.getAllReferencingProjects(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F))
				.size());
		assertEquals(0, ExtendedPlatform.getAllReferencingProjects(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A))
				.size());

		Collection<IProject> ProjectsReferencingArProject3x_D = ExtendedPlatform.getAllReferencingProjects(refWks
				.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D));
		assertNotNull(ProjectsReferencingArProject3x_D);
		assertEquals(1, ProjectsReferencingArProject3x_D.size());
		assertTrue(ProjectsReferencingArProject3x_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));

		assertEquals(0, ExtendedPlatform.getAllReferencingProjects(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E))
				.size());

	}

	/**
	 * Test method for {@link ExtendedPlatform#getAllReferencedProjects(IProject)}
	 */
	public void testGetAllReferencedProjects() {

		assertEquals(0, ExtendedPlatform.getAllReferencedProjects(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A))
				.size());
		assertEquals(0, ExtendedPlatform.getAllReferencedProjects(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D))
				.size());

		Collection<IProject> ReferencedProjectsByArProject10_E = ExtendedPlatform.getAllReferencedProjects(refWks
				.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E));
		assertNotNull(ReferencedProjectsByArProject10_E);
		assertEquals(1, ReferencedProjectsByArProject10_E.size());
		assertTrue(ReferencedProjectsByArProject10_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));

		assertEquals(0, ExtendedPlatform.getAllReferencedProjects(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F))
				.size());
		assertEquals(0, ExtendedPlatform.getAllReferencedProjects(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A))
				.size());

		Collection<IProject> ReferencedProjectsByArProject3x_D = ExtendedPlatform.getAllReferencedProjects(refWks
				.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D));
		assertNotNull(ReferencedProjectsByArProject3x_D);
		assertEquals(2, ReferencedProjectsByArProject3x_D.size());
		assertTrue(ReferencedProjectsByArProject3x_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));
		assertTrue(ReferencedProjectsByArProject3x_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));

		Collection<IProject> ReferencedProjectsByArProject3x_E = ExtendedPlatform.getAllReferencedProjects(refWks
				.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		assertNotNull(ReferencedProjectsByArProject3x_E);
		assertEquals(3, ReferencedProjectsByArProject3x_E.size());
		assertTrue(ReferencedProjectsByArProject3x_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));
		assertTrue(ReferencedProjectsByArProject3x_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));
		assertTrue(ReferencedProjectsByArProject3x_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));

	}

	/**
	 * Test method for {@link ExtendedPlatform#getProjectGroup(IProject, boolean)};
	 */
	public void testGetProjectGroup() {

		assertEquals(1, ExtendedPlatform.getProjectGroup(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A), false)
				.size());
		assertTrue(ExtendedPlatform.getProjectGroup(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A), false).contains(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A)));

		assertEquals(1, ExtendedPlatform.getProjectGroup(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A), true).size());
		assertTrue(ExtendedPlatform.getProjectGroup(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A), true).contains(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A)));

		assertEquals(1, ExtendedPlatform.getProjectGroup(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D), false)
				.size());
		assertTrue(ExtendedPlatform.getProjectGroup(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D), false).contains(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));

		Set<IProject> projectGroupForArProject10_D = ExtendedPlatform.getProjectGroup(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D), true);
		assertEquals(4, projectGroupForArProject10_D.size());
		assertTrue(projectGroupForArProject10_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));
		assertTrue(projectGroupForArProject10_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));
		assertTrue(projectGroupForArProject10_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));
		assertTrue(projectGroupForArProject10_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));

		Set<IProject> projectGroupForArProject10_E = ExtendedPlatform.getProjectGroup(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E), true);
		assertEquals(4, projectGroupForArProject10_E.size());
		assertTrue(projectGroupForArProject10_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));
		assertTrue(projectGroupForArProject10_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));
		assertTrue(projectGroupForArProject10_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));
		assertTrue(projectGroupForArProject10_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));

		assertEquals(2, ExtendedPlatform.getProjectGroup(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E), false)
				.size());
		assertTrue(ExtendedPlatform.getProjectGroup(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E), false).contains(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));
		assertTrue(ExtendedPlatform.getProjectGroup(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E), false).contains(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));

		Set<IProject> projectGroupForArProject3x_D = ExtendedPlatform.getProjectGroup(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D), false);
		assertEquals(3, projectGroupForArProject3x_D.size());
		assertTrue(projectGroupForArProject3x_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));
		assertTrue(projectGroupForArProject3x_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));
		assertTrue(projectGroupForArProject3x_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));

		projectGroupForArProject3x_D = ExtendedPlatform.getProjectGroup(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D), true);
		assertEquals(4, projectGroupForArProject3x_D.size());
		assertTrue(projectGroupForArProject3x_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));
		assertTrue(projectGroupForArProject3x_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));
		assertTrue(projectGroupForArProject3x_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));
		assertTrue(projectGroupForArProject3x_D.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));

		Set<IProject> projectGroupForArProject3x_E = ExtendedPlatform.getProjectGroup(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), false);
		assertEquals(4, projectGroupForArProject3x_E.size());
		assertTrue(projectGroupForArProject3x_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));
		assertTrue(projectGroupForArProject3x_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));
		assertTrue(projectGroupForArProject3x_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));
		assertTrue(projectGroupForArProject3x_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));

		projectGroupForArProject3x_E = ExtendedPlatform.getProjectGroup(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), true);
		assertEquals(4, projectGroupForArProject3x_E.size());
		assertTrue(projectGroupForArProject3x_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D)));
		assertTrue(projectGroupForArProject3x_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));
		assertTrue(projectGroupForArProject3x_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D)));
		assertTrue(projectGroupForArProject3x_E.contains(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E)));
	}

	/**
	 * Test method for {@link ExtendedPlatform#isPlatformPrivateResource(IResource)}
	 */
	public void testIsPlatformPrivate() {
		assertNotNull(refWks.hbProject20_D);
		assertTrue(refWks.hbProject20_D.exists());

		// Resource is WorkspaceRoot
		IWorkspaceRoot workspaceRoot = EcorePlugin.getWorkspaceRoot();
		assertFalse(ExtendedPlatform.isPlatformPrivateResource(workspaceRoot));
		// Resource is Project
		assertFalse(ExtendedPlatform.isPlatformPrivateResource(refWks.hbProject20_D));
		// Resource is ProjectPropertied Folder
		IFolder project_description_folder = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFolder(".settings");
		assertNotNull(project_description_folder);
		assertTrue(ExtendedPlatform.isPlatformPrivateResource(project_description_folder));
		// Resource is ProjectDescription File
		IFile project_properties_file = project_description_folder.getFile("org.eclipse.sphinx.examples.hummingbird.prefs");
		assertNotNull(project_properties_file);
		assertTrue(ExtendedPlatform.isPlatformPrivateResource(project_properties_file));
		// Resource is hbFile
		IFile hbFile_20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertNotNull(hbFile_20_D_1);
		assertFalse(ExtendedPlatform.isPlatformPrivateResource(hbFile_20_D_1));
		// Resource is UML2 File
		IFile uml2File_3x_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertNotNull(uml2File_3x_D_1);
		assertFalse(ExtendedPlatform.isPlatformPrivateResource(uml2File_3x_D_1));
		// CVS resource
		IFile cvsFile = refWks.hbProject20_D.getFile("CVS");
		assertNotNull(cvsFile);
		// TODO add cvs file to test
		// assertTrue(ExtendedPlatform.isPlatformPrivateResource(cvsFile));

		// SVN Resource
		IFolder svnFolder = refWks.hbProject20_D.getFolder(".svn");
		assertNotNull(svnFolder);
		assertTrue(ExtendedPlatform.isPlatformPrivateResource(svnFolder));

		IFile svnSubFolder = refWks.hbProject20_D.getFile(".svn/text-base");
		assertNotNull(svnSubFolder);
		assertFalse(ExtendedPlatform.isPlatformPrivateResource(svnSubFolder));

		// SVN- base file
		IFile svnBaseFile = refWks.hbProject20_D.getFile(".svn/text-base/.classpath.svn-base");
		assertNotNull(svnBaseFile);
		assertTrue(ExtendedPlatform.isPlatformPrivateResource(svnBaseFile));

		// File without file extention
		IFile svnFileWithoutExtention = refWks.hbProject20_D.getFile(".svn/entries");
		assertNotNull(svnFileWithoutExtention);
		assertFalse(ExtendedPlatform.isPlatformPrivateResource(svnFileWithoutExtention));

	}

	/**
	 * Test method for {@link ExtendedPlatform#getContentTypeId(java.io.File)}
	 * 
	 * @throws IOException
	 * @throws CoreException
	 * @throws InterruptedException
	 * @throws OperationCanceledException
	 */
	public void testGetContentTypeId_File() throws IOException, CoreException, OperationCanceledException, InterruptedException {

		final String Hummingbird10ContentType = "org.eclipse.sphinx.examples.hummingbird10.hummingbird10XMIFile";
		final String Hummingbird20ContentType = "org.eclipse.sphinx.examples.hummingbird20.hummingbird20XMIFile";
		final String Uml2Contentype = "org.eclipse.uml2.uml_2_1_0";
		String contentTypeId = "";

		IPath hbProject20_D_Path = refWks.hbProject20_D.getLocation();
		// Hummingbird file
		IPath hbFilePath = hbProject20_D_Path.append(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		File hbFile = hbFilePath.toFile();
		assertNotNull(hbFile);
		assertTrue(hbFile.exists());
		assertEquals(Hummingbird20ContentType, ExtendedPlatform.getContentTypeId(hbFile));
		// ==========================================================
		// Uml File
		IPath uml2FilePath = hbProject20_D_Path.append(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		File uml2File = uml2FilePath.toFile();
		assertNotNull(uml2File);
		assertTrue(uml2File.exists());
		assertEquals(Uml2Contentype, ExtendedPlatform.getContentTypeId(uml2File));
		// ==========================================================
		// File input is unloaded
		refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).close(new NullProgressMonitor());
		waitForModelLoading();

		IPath unaccessableFilePath = refWks.hbProject10_A.getLocation().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		File unaccessableFile = unaccessableFilePath.toFile();
		assertEquals(Hummingbird10ContentType, ExtendedPlatform.getContentTypeId(unaccessableFile));
		// ==========================================================
		// File input is .project file -> return XML content type

		IPath projectDescriptionFilePath = hbProject20_D_Path.append(".project");
		File projectDescriptionFile = projectDescriptionFilePath.toFile();
		assertNotNull(projectDescriptionFile);
		assertTrue(projectDescriptionFile.exists());

		// Eclipse cannot get contentypeDescription of this file
		contentTypeId = ExtendedPlatform.getContentTypeId(projectDescriptionFile);
		assertNull(contentTypeId);
		// ==========================================================
		// File is setting file
		IPath projectSettingFilePath = hbProject20_D_Path.append(".settings").append("org.eclipse.sphinx.examples.hummingbird.ide.prefs");
		File projectSettingFile = projectSettingFilePath.toFile();
		assertNotNull(projectSettingFile);
		assertTrue(projectSettingFile.exists());

		contentTypeId = ExtendedPlatform.getContentTypeId(projectSettingFile);
		assertNotNull(contentTypeId);
		assertEquals("org.eclipse.core.resources.preferences", contentTypeId);
		// ==========================================================
		// File is unexisting
		IPath unExistingFileAbsPath = refWks.hbProject20_D.getLocation().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		File unExistingFile1 = unExistingFileAbsPath.toFile();
		assertNotNull(unExistingFile1);
		assertFalse(unExistingFile1.exists());
		assertNull(ExtendedPlatform.getContentTypeId(unExistingFile1));

	}

	/**
	 * Test method for {@link ExtendedPlatform#addNature(IProject, String, org.eclipse.core.runtime.IProgressMonitor)}
	 * 
	 * @throws CoreException
	 */
	public void testAddNature() throws Exception {
		String pluginNature = "org.eclipse.pde.PluginNature";
		String javaNaure = "org.eclipse.jdt.core.javanature";

		// HB20 Project
		IProject project20_A = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
		assertNotNull(project20_A);

		IProjectDescription prj20ADesc = project20_A.getDescription();

		assertEquals(1, prj20ADesc.getNatureIds().length);
		String previousNature20_A = prj20ADesc.getNatureIds()[0];

		ExtendedPlatform.addNature(project20_A, pluginNature, null);

		prj20ADesc = project20_A.getDescription();
		assertEquals(2, prj20ADesc.getNatureIds().length);
		assertEquals(previousNature20_A, prj20ADesc.getNatureIds()[0]);
		assertEquals(pluginNature, prj20ADesc.getNatureIds()[1]);
		// =====================================================
		// HB10 Project
		IProject project10_A = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertNotNull(project10_A);

		IProjectDescription prj10ADesc = project10_A.getDescription();

		assertEquals(1, prj10ADesc.getNatureIds().length);
		String previousNature10_A = prj10ADesc.getNatureIds()[0];
		ExtendedPlatform.addNature(project10_A, javaNaure, null);
		prj10ADesc = project10_A.getDescription();
		assertEquals(2, prj10ADesc.getNatureIds().length);
		assertEquals(previousNature10_A, prj10ADesc.getNatureIds()[0]);
		assertEquals(javaNaure, prj10ADesc.getNatureIds()[1]);
		// =====================================================
		// Add existing nature
		ExtendedPlatform.addNature(project10_A, javaNaure, null);
		prj10ADesc = project10_A.getDescription();
		assertEquals(2, prj10ADesc.getNatureIds().length);
		assertEquals(previousNature10_A, prj10ADesc.getNatureIds()[0]);
		assertEquals(javaNaure, prj10ADesc.getNatureIds()[1]);
		// =====================================================
		// Given Project is null
		IProject nullProject = null;
		try {
			ExtendedPlatform.addNature(nullProject, pluginNature, null);
		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				throw ex;
			}
		}
		// =====================================================
		// Add empty nature
		ExtendedPlatform.addNature(project20_A, "", null);
		prj20ADesc = project20_A.getDescription();
		assertEquals(2, prj20ADesc.getNatureIds().length);
		assertEquals(previousNature20_A, prj20ADesc.getNatureIds()[0]);
		assertEquals(pluginNature, prj20ADesc.getNatureIds()[1]);
		// =====================================================
		// Set null nature
		ExtendedPlatform.addNature(project10_A, null, null);
		prj10ADesc = project10_A.getDescription();
		assertEquals(2, prj10ADesc.getNatureIds().length);
		assertEquals(previousNature10_A, prj10ADesc.getNatureIds()[0]);
		assertEquals(javaNaure, prj10ADesc.getNatureIds()[1]);
	}

	/**
	 * Test method for
	 * {@link ExtendedPlatform#removeNature(IProject, String, org.eclipse.core.runtime.IProgressMonitor)}
	 * 
	 * @throws CoreException
	 */
	public void testRemoveNature() throws Exception {
		String cNature = "org.eclipse.cdt.core.cnature";
		String hummingbirdNature = "org.eclipse.sphinx.examples.hummingbird.ide.HummingbirdNature";
		IProject project10F = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F);
		assertNotNull(project10F);

		IProjectDescription prj10FDesc = project10F.getDescription();
		assertEquals(2, prj10FDesc.getNatureIds().length);
		assertEquals(hummingbirdNature, prj10FDesc.getNatureIds()[0]);
		assertEquals(cNature, prj10FDesc.getNatureIds()[1]);
		// =====================================================
		// Remove pluginNature
		ExtendedPlatform.removeNature(project10F, cNature, null);

		prj10FDesc = project10F.getDescription();
		assertEquals(1, prj10FDesc.getNatureIds().length);
		assertEquals(hummingbirdNature, prj10FDesc.getNatureIds()[0]);
		// =====================================================
		// Remove unexisting nature
		ExtendedPlatform.removeNature(project10F, "anyNature", null);

		prj10FDesc = project10F.getDescription();
		assertEquals(1, prj10FDesc.getNatureIds().length);
		assertEquals(hummingbirdNature, prj10FDesc.getNatureIds()[0]);
		// =====================================================
		// Remove HummingbirdNature
		ExtendedPlatform.removeNature(project10F, hummingbirdNature, null);

		prj10FDesc = project10F.getDescription();
		assertEquals(0, prj10FDesc.getNatureIds().length);

		// =====================================================
		// Given project is Null
		IProject nullProject = null;
		try {
			ExtendedPlatform.removeNature(nullProject, cNature, null);
		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				throw ex;
			}
		}

	}
}
