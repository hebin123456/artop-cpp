package org.eclipse.sphinx.tests.emf.integration.scoping;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.sphinx.emf.scoping.ResourceScopeProviderRegistry;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

public class ResourceScopeProviderRegistryTest extends DefaultIntegrationTestCase {

	public ResourceScopeProviderRegistryTest() {
		setRecycleReferenceWorkspaceOfPreviousTestRun(false);

		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_F);
	}

	public void testResourceScopeProviderRegistry_UpperCaseFileExtension() {
		IFile file20F_1 = refWks.hbProject20_F.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20F_1);
		assertNotNull(file20F_1);
		assertEquals("INSTANCEMODEL", file20F_1.getFileExtension()); //$NON-NLS-1$
		assertTrue(ResourceScopeProviderRegistry.INSTANCE.hasApplicableFileExtension(file20F_1));

		IFile file20F_2 = refWks.hbProject20_F.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20F_2);
		assertNotNull(file20F_2);
		assertEquals("TYPEMODEL", file20F_2.getFileExtension()); //$NON-NLS-1$
		assertTrue(ResourceScopeProviderRegistry.INSTANCE.hasApplicableFileExtension(file20F_2));
	}

	public void testResourceScopeProviderRegistry_MixedCaseFileExtension() {
		IFile file20F_3 = refWks.hbProject20_F.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20F_3);
		assertNotNull(file20F_3);
		assertEquals("InStAnCeMoDeL", file20F_3.getFileExtension()); //$NON-NLS-1$
		assertTrue(ResourceScopeProviderRegistry.INSTANCE.hasApplicableFileExtension(file20F_3));

		IFile file20F_4 = refWks.hbProject20_F.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20F_4);
		assertNotNull(file20F_4);
		assertEquals("Typemodel", file20F_4.getFileExtension()); //$NON-NLS-1$
		assertTrue(ResourceScopeProviderRegistry.INSTANCE.hasApplicableFileExtension(file20F_4));
	}

	public void testResourceScopeProviderRegistry_LowerCaseFileExtension() {
		IFile file21F_4 = refWks.hbProject20_F.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_21_20F_4);
		assertNotNull(file21F_4);
		assertEquals("instancemodel", file21F_4.getFileExtension()); //$NON-NLS-1$
		assertTrue(ResourceScopeProviderRegistry.INSTANCE.hasApplicableFileExtension(file21F_4));
	}
}
