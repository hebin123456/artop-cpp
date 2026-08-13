/**
 * <copyright>
 *
 * Copyright (c) 2011-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [406564] BasicWorkspaceResourceLoader#getResource should not delegate to super
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.testutils.integration.referenceworkspace.xtendxpand;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.testutils.integration.AbstractReferenceWorkspace;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.xtendxpand.internal.Activator;

@SuppressWarnings("nls")
public class XtendXpandTestReferenceWorkspace extends AbstractReferenceWorkspace {

	protected static final String DEFAULT_TEST_REFERENCE_WORKSPACE_DIRECTORY = "referenceWorkspace";

	// Xtend project.
	//
	public static final String HB_TRANSFORM_XTEND_PROJECT_NAME = "hummingbird20.transform.xtend";

	public IProject transformXtendProject;

	public static final String HB_TRANSFORM_XTEND_PROJECT_UML_MODEL_PATH = "model/sample.uml";

	public static final String UML2_HB20_EXT_FILE_PATH = "extensions/UML2ToHummingbird20.ext";

	public static final String XTEND_UML2_HB20_EXTENSION_NAME = "extensions::UML2ToHummingbird20::transform";

	public static final String LINKED_UML2_HB20_EXT_FILE_NAME = "UML2ToHummingbird20-linked.ext";

	public static final String LINKED_XTEND_UML2_HB20_EXTENSION_NAME = "UML2ToHummingbird20-linked::transform";

	// Xpand project.
	//
	public static final String HB_CODEGEN_XPAND_PROJECT_NAME = "hummingbird20.codegen.xpand";

	public IProject codegenXpandProject;

	public static final String HB_CODEGEN_XPAND_PROJECT_HB_INSTANCE_MODEL_PATH = "model/sample.instancemodel";

	public static final String HB_CODEGEN_XPAND_PROJECT_HB_TYPE_MODEL_PATH = "model/sample.typemodel";

	public static final String HB_CODEGEN_XPAND_PROJECT_HOUTLET_FOLDER_NAME = "HOUTLET";

	public static final String CONFIGH_FILE_NAME = "Config.h";

	public static final String CONFIGH_XPT_FILE_PATH = "templates/ConfigH.xpt";

	public static final String CONFIGH_TO_HOUTLET_XPT_FILE_PATH = "templates/ConfigHToHoutlet.xpt";

	public static final String CONFIGH_ASPECTS_XPT_FILE_PATH = "templates/ConfigHAspects.xpt";

	public static final String PARAMETER_COMMENT_XPT_FILE_PATH = "templates/ParameterComment.xpt";

	public static final String XPAND_CONFIGH_DEFINITION_NAME = "templates::ConfigH::main";

	public static final String XPAND_CONFIGH_TOHOUTLET_DEFINITION_NAME = "templates::ConfigHToHoutlet::main";

	public static final String HB_CHK_FILE_PATH = "checks/InstanceModel.chk";

	// Editing domains.
	//
	public TransactionalEditingDomain editingDomainUml2;
	public TransactionalEditingDomain editingDomain20;

	public XtendXpandTestReferenceWorkspace(Set<String> referenceProjectSubset) {
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
		transformXtendProject = getReferenceProject(HB_TRANSFORM_XTEND_PROJECT_NAME);
		codegenXpandProject = getReferenceProject(HB_CODEGEN_XPAND_PROJECT_NAME);
	}

	protected void initReferenceEditingDomainAccessors() {
		editingDomainUml2 = WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(), UML2MMDescriptor.INSTANCE);
		editingDomain20 = WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(), Hummingbird20MMDescriptor.INSTANCE);
	}

	@Override
	protected void initReferenceFileDescriptors() {
		addFileDescriptors(HB_TRANSFORM_XTEND_PROJECT_NAME, new String[] { HB_TRANSFORM_XTEND_PROJECT_UML_MODEL_PATH }, UML2MMDescriptor.INSTANCE);
		addFileDescriptors(HB_CODEGEN_XPAND_PROJECT_NAME,
				new String[] { HB_CODEGEN_XPAND_PROJECT_HB_INSTANCE_MODEL_PATH, HB_CODEGEN_XPAND_PROJECT_HB_TYPE_MODEL_PATH },
				Hummingbird20MMDescriptor.INSTANCE);
	}

	@Override
	protected String[] getReferenceProjectsNames() {
		return new String[] { HB_TRANSFORM_XTEND_PROJECT_NAME, HB_CODEGEN_XPAND_PROJECT_NAME };
	}
}