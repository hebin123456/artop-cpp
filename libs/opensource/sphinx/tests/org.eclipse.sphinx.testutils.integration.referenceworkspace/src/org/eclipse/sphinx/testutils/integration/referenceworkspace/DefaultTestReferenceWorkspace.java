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
package org.eclipse.sphinx.testutils.integration.referenceworkspace;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.testutils.integration.AbstractReferenceWorkspace;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.internal.Activator;

/**
 *
 */
@SuppressWarnings("nls")
public class DefaultTestReferenceWorkspace extends AbstractReferenceWorkspace {

	protected static final String DEFAULT_TEST_REFERENCE_WORKSPACE_DIRECTORY = "referenceWorkspace";

	/* ----- HUMMINGBIRD 10 Project A ----- */
	public static final String HB_PROJECT_NAME_10_A = "hbProject10_A";

	public static final String HB_FILE_NAME_10_10A_1 = "hbFile10_10A_1.hummingbird";
	public static final String HB_FILE_NAME_10_10A_2 = "hbFile10_10A_2.hummingbird";
	public static final String HB_FILE_NAME_10_10A_3 = "hbFile10_10A_3.hummingbird";
	public static final String HB_FILE_NAME_10_10A_4 = "hbFile10_10A_4.hummingbird";
	public static final String HB_FILE_NAME_10_10A_5 = "hbFile10_10A_5.hummingbird";

	/* ----- HUMMINGBIRD 10 Project B ----- */
	public static final String HB_PROJECT_NAME_10_B = "hbProject10_B";

	public static final String HB_FILE_NAME_10_10B_1 = "hbFile10_10B_1.hummingbird";
	public static final String HB_FILE_NAME_10_10B_2 = "hbFile10_10B_2.hummingbird";
	public static final String HB_FILE_NAME_10_10B_3 = "hbFile10_10B_3.hummingbird";

	/* ----- HUMMINGBIRD 10 Project C ----- */
	public static final String HB_PROJECT_NAME_10_C = "hbProject10_C";

	public static final String HB_FILE_NAME_10_10C_1 = "hbFile10_10C_1.hummingbird";
	public static final String HB_FILE_NAME_10_10C_2 = "hbFile10_10C_2.hummingbird";
	public static final String HB_FILE_NAME_10_10C_3 = "hbFile10_10C_3.hummingbird";

	public static final String UML2_FILE_NAME_10C_1 = "uml2File_10C_1.uml";
	public static final String UML2_FILE_NAME_10C_2 = "uml2File_10C_2.uml";
	public static final String UML2_FILE_NAME_10C_3 = "uml2File_10C_3.uml";

	/* ----- HUMMINGBIRD 10 Project D ----- */
	public static final String HB_PROJECT_NAME_10_D = "hbProject10_D";

	public static final String HB_FILE_NAME_10_10D_1 = "hbFile10_10D_1.hummingbird";
	public static final String HB_FILE_NAME_10_10D_2 = "hbFile10_10D_2.hummingbird";
	public static final String HB_FILE_NAME_10_10D_3 = "hbFile10_10D_3.hummingbird";

	/* ----- HUMMINGBIRD 10 Project E ----- */
	public static final String HB_PROJECT_NAME_10_E = "hbProject10_E";

	public static final String HB_FILE_NAME_10_10E_1 = "hbFile10_10E_1.hummingbird";
	public static final String HB_FILE_NAME_10_10E_2 = "hbFile10_10E_2.hummingbird";
	public static final String HB_FILE_NAME_10_10E_3 = "hbFile10_10E_3.hummingbird";

	/* ----- HUMMINGBIRD 10 Project F ----- */
	public static final String HB_PROJECT_NAME_10_F = "hbProject10_F";

	public static final String HB_FOLDER_NAME_10_10F_1 = "hbFolder_10_10f_1";

	public static final String HB_FILE_NAME_10_10F_1 = "hbFile10_10F_1.hummingbird";
	public static final String HB_FILE_NAME_10_10F_2 = "hbFile10_10F_2.hummingbird";
	public static final String HB_FILE_NAME_10_10F_3 = "hbFile10_10F_3.hummingbird";

	/* ----- HUMMINGBIRD 20 Project A ----- */
	public static final String HB_PROJECT_NAME_20_A = "hbProject20_A";

	public static final String HB_FILE_NAME_20_20A_1 = "hbFile20_20A_1.instancemodel";
	public static final String HB_FILE_NAME_20_20A_2 = "hbFile20_20A_2.typemodel";
	public static final String HB_FILE_NAME_20_20A_3 = "hbFile20_20A_3.instancemodel";
	public static final String HB_FILE_NAME_20_20A_4 = "hbFile20_20A_4.typemodel";
	public static final String HB_FILE_NAME_21_20A_4 = "hbFile21_20A_4.instancemodel";
	public static final String HB_FILE_NAME_20_20A_5 = "hbFile20_20A_5.instancemodel";

	/* ----- HUMMINGBIRD 20 Project B ----- */
	public static final String HB_PROJECT_NAME_20_B = "hbProject20_B";

	public static final String HB_FILE_NAME_20_20B_1 = "hbFile20_20B_1.typemodel";
	public static final String HB_FILE_NAME_20_20B_2 = "hbFile20_20B_2.instancemodel";
	public static final String HB_FILE_NAME_20_20B_3 = "hbFile20_20B_3.instancemodel";

	public static final String UML2_FILE_NAME_20B_1 = "uml2File_20B_1.uml";
	public static final String UML2_FILE_NAME_20B_2 = "uml2File_20B_2.uml";
	public static final String UML2_FILE_NAME_20B_3 = "uml2File_20B_3.uml";

	/* ----- HUMMINGBIRD 20 Project C ----- */
	public static final String HB_PROJECT_NAME_20_C = "hbProject20_C";

	public static final String HB_FILE_NAME_10_20C_1 = "hbFile10_20C_1.hummingbird";
	public static final String HB_FILE_NAME_10_20C_2 = "hbFile10_20C_2.hummingbird";
	public static final String HB_FILE_NAME_10_20C_3 = "hbFile10_20C_3.hummingbird";

	public static final String HB_FILE_NAME_20_20C_1 = "hbFile20_20C_1.instancemodel";
	public static final String HB_FILE_NAME_20_20C_2 = "hbFile20_20C_2.instancemodel";
	public static final String HB_FILE_NAME_20_20C_3 = "hbFile20_20C_3.instancemodel";

	/* ----- HUMMINGBIRD 20 Project D ----- */
	public static final String HB_PROJECT_NAME_20_D = "hbProject20_D";

	public static final String HB_FILE_NAME_20_20D_1 = "hbFile20_20D_1.instancemodel";
	public static final String HB_FILE_NAME_20_20D_2 = "hbFile20_20D_2.typemodel";
	public static final String HB_FILE_NAME_20_20D_3 = "hbFile20_20D_3.instancemodel";

	public static final String UML2_FILE_NAME_20D_1 = "uml2File_20D_1.uml";
	public static final String UML2_FILE_NAME_20D_2 = "uml2File_20D_2.uml";
	public static final String UML2_FILE_NAME_20D_3 = "uml2File_20D_3.uml";

	/* ----- HUMMINGBIRD 20 Project E ----- */
	public static final String HB_PROJECT_NAME_20_E = "hbProject20_E";

	public static final String HB_FILE_NAME_20_20E_1 = "hbFile20_20E_1.instancemodel";
	public static final String HB_FILE_NAME_20_20E_2 = "hbFile20_20E_2.instancemodel";
	public static final String HB_FILE_NAME_20_20E_3 = "hbFile20_20E_3.instancemodel";

	public static final String UML2_FILE_NAME_20E_1 = "uml2File_20E_1.uml";
	public static final String UML2_FILE_NAME_20E_2 = "uml2File_20E_2.uml";
	public static final String UML2_FILE_NAME_20E_3 = "uml2File_20E_3.uml";

	/* ----- HUMMINGBIRD 20 Project A ----- */
	public static final String HB_PROJECT_NAME_20_F = "hbProject20_F";

	public static final String HB_FILE_NAME_20_20F_1 = "hbFile20_20F_1.INSTANCEMODEL";
	public static final String HB_FILE_NAME_20_20F_2 = "hbFile20_20F_2.TYPEMODEL";
	public static final String HB_FILE_NAME_20_20F_3 = "hbFile20_20F_3.InStAnCeMoDeL";
	public static final String HB_FILE_NAME_20_20F_4 = "hbFile20_20F_4.Typemodel";
	public static final String HB_FILE_NAME_21_20F_4 = "hbFile21_20F_4.instancemodel";

	/* ----- HUMMINGBIRD 20 Workflows ----- */
	public static final String HB_PROJECT_NAME_20_WORKFLOWS = "hbProject20.workflows";

	/* ----- Projects ----- */
	public IProject hbProject10_A;
	public IProject hbProject10_B;
	public IProject hbProject10_C;
	public IProject hbProject10_D;
	public IProject hbProject10_E;
	public IProject hbProject10_F;
	public IProject hbProject20_A;
	public IProject hbProject20_B;
	public IProject hbProject20_C;
	public IProject hbProject20_D;
	public IProject hbProject20_E;
	public IProject hbProject20_F;
	public IProject hbProject20_Workflows;

	/* ----- EditingDomains ----- */
	public TransactionalEditingDomain editingDomain10;
	public TransactionalEditingDomain editingDomain20;
	public TransactionalEditingDomain editingDomainUml2;

	public DefaultTestReferenceWorkspace(Set<String> referenceProjectSubset) {
		super(referenceProjectSubset);
	}

	@Override
	public Plugin getReferenceWorkspacePlugin() {
		return Activator.getPlugin();
	}

	@Override
	public String getReferenceWorkspaceArchiveFileName() {
		return DEFAULT_TEST_REFERENCE_WORKSPACE_DIRECTORY;
	}

	@Override
	public void initContentAccessors() {
		initReferenceProjectAccessors();
		initReferenceEditingDomainAccessors();
	}

	protected void initReferenceProjectAccessors() {
		hbProject10_A = getReferenceProject(HB_PROJECT_NAME_10_A);
		hbProject10_B = getReferenceProject(HB_PROJECT_NAME_10_B);
		hbProject10_C = getReferenceProject(HB_PROJECT_NAME_10_C);
		hbProject10_D = getReferenceProject(HB_PROJECT_NAME_10_D);
		hbProject10_E = getReferenceProject(HB_PROJECT_NAME_10_E);
		hbProject10_F = getReferenceProject(HB_PROJECT_NAME_10_F);
		hbProject20_A = getReferenceProject(HB_PROJECT_NAME_20_A);
		hbProject20_B = getReferenceProject(HB_PROJECT_NAME_20_B);
		hbProject20_C = getReferenceProject(HB_PROJECT_NAME_20_C);
		hbProject20_D = getReferenceProject(HB_PROJECT_NAME_20_D);
		hbProject20_E = getReferenceProject(HB_PROJECT_NAME_20_E);
		hbProject20_F = getReferenceProject(HB_PROJECT_NAME_20_F);
		hbProject20_Workflows = getReferenceProject(HB_PROJECT_NAME_20_WORKFLOWS);
	}

	protected void initReferenceEditingDomainAccessors() {
		editingDomain10 = WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(), Hummingbird10MMDescriptor.INSTANCE);
		editingDomain20 = WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(), Hummingbird20MMDescriptor.INSTANCE);
		editingDomainUml2 = WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(), UML2MMDescriptor.INSTANCE);
	}

	@Override
	protected void initReferenceFileDescriptors() {

		addFileDescriptors(HB_PROJECT_NAME_10_A,
				new String[] { HB_FILE_NAME_10_10A_1, HB_FILE_NAME_10_10A_2, HB_FILE_NAME_10_10A_3, HB_FILE_NAME_10_10A_4, HB_FILE_NAME_10_10A_5 },
				Hummingbird10MMDescriptor.INSTANCE);
		addFileDescriptors(HB_PROJECT_NAME_10_B, new String[] { HB_FILE_NAME_10_10B_1, HB_FILE_NAME_10_10B_2, HB_FILE_NAME_10_10B_3 },
				Hummingbird10MMDescriptor.INSTANCE);
		addFileDescriptors(HB_PROJECT_NAME_10_C, new String[] { HB_FILE_NAME_10_10C_1, HB_FILE_NAME_10_10C_2, HB_FILE_NAME_10_10C_3 },
				Hummingbird10MMDescriptor.INSTANCE);
		addFileDescriptors(HB_PROJECT_NAME_10_C, new String[] { UML2_FILE_NAME_10C_1, UML2_FILE_NAME_10C_2, UML2_FILE_NAME_10C_3 },
				UML2MMDescriptor.INSTANCE);
		addFileDescriptors(HB_PROJECT_NAME_10_D, new String[] { HB_FILE_NAME_10_10D_1, HB_FILE_NAME_10_10D_2, HB_FILE_NAME_10_10D_3 },
				Hummingbird10MMDescriptor.INSTANCE);
		addFileDescriptors(HB_PROJECT_NAME_10_E, new String[] { HB_FILE_NAME_10_10E_1, HB_FILE_NAME_10_10E_2, HB_FILE_NAME_10_10E_3 },
				Hummingbird10MMDescriptor.INSTANCE);
		addFileDescriptors(
				HB_PROJECT_NAME_10_F, new String[] { HB_FOLDER_NAME_10_10F_1 + "/" + HB_FILE_NAME_10_10F_1,
						HB_FOLDER_NAME_10_10F_1 + "/" + HB_FILE_NAME_10_10F_2, HB_FOLDER_NAME_10_10F_1 + "/" + HB_FILE_NAME_10_10F_3 },
				Hummingbird10MMDescriptor.INSTANCE);
		addFileDescriptors(HB_PROJECT_NAME_20_A, new String[] { HB_FILE_NAME_20_20A_1, HB_FILE_NAME_20_20A_2, HB_FILE_NAME_20_20A_3,
				HB_FILE_NAME_20_20A_4, HB_FILE_NAME_21_20A_4, HB_FILE_NAME_20_20A_5 }, Hummingbird20MMDescriptor.INSTANCE);
		addFileDescriptors(HB_PROJECT_NAME_20_B, new String[] { HB_FILE_NAME_20_20B_1, HB_FILE_NAME_20_20B_2, HB_FILE_NAME_20_20B_3 },
				Hummingbird20MMDescriptor.INSTANCE);
		addFileDescriptors(HB_PROJECT_NAME_20_B, new String[] { UML2_FILE_NAME_20B_1, UML2_FILE_NAME_20B_2, UML2_FILE_NAME_20B_3 },
				UML2MMDescriptor.INSTANCE);
		addFileDescriptors(HB_PROJECT_NAME_20_C, new String[] { HB_FILE_NAME_20_20C_1, HB_FILE_NAME_20_20C_2, HB_FILE_NAME_20_20C_3 },
				Hummingbird20MMDescriptor.INSTANCE);
		addFileDescriptors(HB_PROJECT_NAME_20_C, new String[] { HB_FILE_NAME_10_20C_1, HB_FILE_NAME_10_20C_2, HB_FILE_NAME_10_20C_3 });

		addFileDescriptors(HB_PROJECT_NAME_20_D, new String[] { HB_FILE_NAME_20_20D_1, HB_FILE_NAME_20_20D_2, HB_FILE_NAME_20_20D_3 },
				Hummingbird20MMDescriptor.INSTANCE);
		addFileDescriptors(HB_PROJECT_NAME_20_D, new String[] { UML2_FILE_NAME_20D_1, UML2_FILE_NAME_20D_2, UML2_FILE_NAME_20D_3 },
				UML2MMDescriptor.INSTANCE);
		addFileDescriptors(HB_PROJECT_NAME_20_E, new String[] { HB_FILE_NAME_20_20E_1, HB_FILE_NAME_20_20E_2, HB_FILE_NAME_20_20E_3 },
				Hummingbird20MMDescriptor.INSTANCE);
		addFileDescriptors(HB_PROJECT_NAME_20_E, new String[] { UML2_FILE_NAME_20E_1, UML2_FILE_NAME_20E_2, UML2_FILE_NAME_20E_3 },
				UML2MMDescriptor.INSTANCE);
		addFileDescriptors(HB_PROJECT_NAME_20_F,
				new String[] { HB_FILE_NAME_20_20F_1, HB_FILE_NAME_20_20F_2, HB_FILE_NAME_20_20F_3, HB_FILE_NAME_20_20F_4, HB_FILE_NAME_21_20F_4 },
				Hummingbird20MMDescriptor.INSTANCE);
	}

	@Override
	protected String[] getReferenceProjectsNames() {
		return new String[] { HB_PROJECT_NAME_10_A, HB_PROJECT_NAME_10_B, HB_PROJECT_NAME_10_C, HB_PROJECT_NAME_10_D, HB_PROJECT_NAME_10_E,
				HB_PROJECT_NAME_10_F, HB_PROJECT_NAME_20_A, HB_PROJECT_NAME_20_B, HB_PROJECT_NAME_20_C, HB_PROJECT_NAME_20_D, HB_PROJECT_NAME_20_E,
				HB_PROJECT_NAME_20_F, HB_PROJECT_NAME_20_WORKFLOWS };
	}
}