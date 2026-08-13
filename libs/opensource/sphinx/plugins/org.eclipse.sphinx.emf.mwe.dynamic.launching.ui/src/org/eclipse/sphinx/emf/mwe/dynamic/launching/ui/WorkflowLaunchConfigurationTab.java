/**
 * <copyright>
 *
 * Copyright (c) 2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [506671] Add support for specifying and injecting user-defined arguments for workflows through workflow launch configurations
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.mwe.dynamic.launching.ui;

import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_ARGUMENTS;
import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_ARGUMENTS_DEFAULT;
import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_AUTO_SAVE;
import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_AUTO_SAVE_DEFAULT;
import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_MODEL;
import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_MODEL_DEFAULT;
import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_WORKFLOW;
import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_WORKFLOW_DEFAULT;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflow;
import org.eclipse.emf.mwe2.runtime.workflow.Workflow;
import org.eclipse.jdt.core.IType;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.sphinx.emf.mwe.dynamic.annotations.WorkflowParameter;
import org.eclipse.sphinx.emf.mwe.dynamic.internal.WorkflowInstanceFactory;
import org.eclipse.sphinx.emf.mwe.dynamic.launching.ui.internal.Activator;
import org.eclipse.sphinx.emf.mwe.dynamic.launching.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.mwe.dynamic.ui.util.WorkflowRunnerUIHelper;
import org.eclipse.sphinx.emf.mwe.dynamic.util.WorkflowRunnerHelper;
import org.eclipse.sphinx.platform.ui.fields.IField;
import org.eclipse.sphinx.platform.ui.groups.IGroupListener;
import org.eclipse.sphinx.platform.ui.groups.NameValueTableGroup;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

@SuppressWarnings("restriction")
public class WorkflowLaunchConfigurationTab extends AbstractLaunchConfigurationTab {

	protected static abstract class AbstractFocusListener implements FocusListener {
		@Override
		public void focusGained(FocusEvent e) {
			// Do nothing by default
		}
	}

	protected static abstract class AbstractSelectionListener implements SelectionListener {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// Do nothing by default
		}
	}

	private Image image;

	protected Text workflowText;
	protected Button browseWorkflowPushButton;

	protected Text modelText;
	protected Button browseModelPushButton;
	protected Button autoSaveCheckButton;

	protected NameValueTableGroup argumentsGroup;
	protected Button envAddButton;
	protected Button envEditButton;
	protected Button envRemoveButton;

	protected WorkflowRunnerHelper helper = new WorkflowRunnerHelper();

	protected WorkflowRunnerUIHelper uiHelper = new WorkflowRunnerUIHelper();

	public WorkflowLaunchConfigurationTab() {
		image = PDEPluginImages.DESC_MAIN_TAB.createImage();
	}

	/*
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return Messages.tab_workflow_name;
	}

	/*
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return image;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_BOTH);

		createWorkflowGroup(composite);
		createModelGroup(composite);
		createArgumentsGroup(composite);

		setControl(composite);
	}

	protected void createWorkflowGroup(Composite parent) {
		Group group = SWTFactory.createGroup(parent, Messages.group_workflow_label, 2, 1, GridData.FILL_HORIZONTAL);

		workflowText = SWTFactory.createSingleText(group, 1);
		workflowText.addFocusListener(new AbstractFocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				updateLaunchConfigurationDialog();
				updateArguments();
			}
		});

		browseWorkflowPushButton = createPushButton(group, Messages.pushButton_browse_label, null);
		browseWorkflowPushButton.addSelectionListener(new AbstractSelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IType workflowType = uiHelper.promptForWorkflowType();
				if (workflowType != null) {
					workflowText.setText(helper.toWorkflowString(workflowType));
				}
				updateLaunchConfigurationDialog();
				updateArguments();
			}
		});
	}

	protected void createModelGroup(Composite parent) {
		Group group = SWTFactory.createGroup(parent, Messages.group_model_label, 2, 1, GridData.FILL_HORIZONTAL);

		modelText = SWTFactory.createSingleText(group, 1);
		modelText.addFocusListener(new AbstractFocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				autoSaveCheckButton.setEnabled(!modelText.getText().isEmpty());
				updateLaunchConfigurationDialog();
			}
		});

		browseModelPushButton = createPushButton(group, Messages.pushButton_browse_label, null);
		// TODO Add support for browsing model elements in an appropriate manner and update
		// autoSaveCheckButton.setEnabled()
		// browseModelPushButton.addSelectionListener(new AbstractSelectionListener() {});
		browseModelPushButton.setEnabled(false);

		autoSaveCheckButton = createCheckButton(group, Messages.checkButton_autoSave_label);
		autoSaveCheckButton.setEnabled(false);
	}

	protected void createArgumentsGroup(Composite composite) {
		argumentsGroup = new NameValueTableGroup(Messages.group_arguments_label) {
			@Override
			public void setInput(Map<String, String> entries) {
				/*
				 * !! Important Note !! Pass copy of original entries as input to group to make sure that changes made
				 * to the entries can be detected by the launch configuration framework and the enablement of the Apply
				 * and Revert buttons gets updated accordingly.
				 */
				super.setInput(new HashMap<String, String>(entries));
			}
		};
		argumentsGroup.createContent(composite, 1, true);
		argumentsGroup.addGroupListener(new IGroupListener() {
			@Override
			public void groupChanged(IField field) {
				updateLaunchConfigurationDialog();
			}
		});
	}

	/*
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String workflow = configuration.getAttribute(ATTR_WORKFLOW, ATTR_WORKFLOW_DEFAULT);
			workflowText.setText(workflow);

			String model = configuration.getAttribute(ATTR_MODEL, ATTR_MODEL_DEFAULT);
			modelText.setText(model);
			boolean autoSave = configuration.getAttribute(ATTR_AUTO_SAVE, ATTR_AUTO_SAVE_DEFAULT);
			autoSaveCheckButton.setSelection(autoSave);

			Map<String, String> arguments = configuration.getAttribute(ATTR_ARGUMENTS, ATTR_ARGUMENTS_DEFAULT);
			argumentsGroup.setInput(arguments);

			updateArguments();
		} catch (CoreException ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
	}

	/**
	 * Loads the {@link WorkflowParameter}s of a {@link IWorkflow} and updates the argument table accordingly to avoid
	 * that users have to know and enter all of them manually.
	 */
	protected void updateArguments() {
		try {
			// Retrieve workflow class or file
			String workflowString = workflowText.getText();
			Object workflow = helper.toWorkflowObject(workflowString);

			// Create workflow instance and retrieve workflow parameters
			WorkflowInstanceFactory factory = new WorkflowInstanceFactory();
			Workflow instance = factory.createWorkflowInstance(workflow);
			Map<String, Class<?>> parameters = factory.getWorkflowParameters(instance);

			// Determine and add missing arguments to the arguments table
			Map<String, String> arguments = argumentsGroup.getInput();
			parameters.keySet().removeAll(arguments.keySet());
			for (String missingArgument : parameters.keySet()) {
				argumentsGroup.addEntry(missingArgument, ""); //$NON-NLS-1$
			}
		} catch (Exception ex) {
			// Do nothing
		}
	}

	/*
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_WORKFLOW, ATTR_WORKFLOW_DEFAULT);
		configuration.setAttribute(ATTR_MODEL, ATTR_MODEL_DEFAULT);
		configuration.setAttribute(ATTR_AUTO_SAVE, ATTR_AUTO_SAVE_DEFAULT);
		configuration.setAttribute(ATTR_ARGUMENTS, ATTR_ARGUMENTS_DEFAULT);
	}

	/*
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_WORKFLOW, workflowText.getText());
		configuration.setAttribute(ATTR_MODEL, modelText.getText());
		configuration.setAttribute(ATTR_AUTO_SAVE, autoSaveCheckButton.getSelection());
		configuration.setAttribute(ATTR_ARGUMENTS, argumentsGroup.getInput());
	}

	/*
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);

		String workflowString = workflowText.getText();
		if (workflowString.isEmpty()) {
			setErrorMessage(Messages.error_workflowNotSpecified);
			return false;
		}
		try {
			helper.toWorkflowObject(workflowString);
		} catch (Exception ex) {
			setErrorMessage(ex.getLocalizedMessage());
			return false;
		}

		String modelURIString = modelText.getText();
		try {
			helper.toModelURIObject(modelURIString);
		} catch (Exception ex) {
			setErrorMessage(ex.getLocalizedMessage());
			return false;
		}

		return true;
	}

	/*
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#dispose()
	 */
	@Override
	public void dispose() {
		image.dispose();
		super.dispose();
	}
}