/**
 * <copyright>
 *
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.explorer.internal.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.sphinx.emf.explorer.internal.Activator;
import org.eclipse.sphinx.emf.explorer.internal.messages.Messages;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.saving.SaveIndicatorUtil;
import org.eclipse.sphinx.emf.workspace.ui.saving.BasicModelSaveableFilter;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * Customized close resource action that is intended to override the {@link CloseResourceAction original one} from
 * Eclipse. It is used to save dirty models before closing.
 */
@SuppressWarnings("restriction")
public class CloseResourceOverrideAction extends CloseResourceAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = Activator.getPlugin().getSymbolicName() + ".CloseResourceAction"; //$NON-NLS-1$

	/**
	 * Creates a new action.
	 *
	 * @param provider
	 *            the shell provider for any dialogs
	 */
	public CloseResourceOverrideAction(final IShellProvider provider) {
		this(provider, IDEWorkbenchMessages.CloseResourceAction_text);
	}

	/**
	 * Creates a new action.
	 *
	 * @param provider
	 *            the shell provider for any dialogs
	 * @param text
	 *            the action's label
	 */
	public CloseResourceOverrideAction(IShellProvider provider, String text) {
		super(provider, text);
		Assert.isNotNull(provider);
		initAction();
	}

	/**
	 * Initialize action
	 */
	private void initAction() {
		setId(ID);
		setToolTipText(IDEWorkbenchMessages.CloseResourceAction_toolTip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.CLOSE_RESOURCE_ACTION);
	}

	@Override
	public void run() {
		if (!saveDirtyModels()) {
			return;
		}
		super.run();
	}

	/**
	 * Causes all dirty models associated to the resource(s) to be saved, if so specified by the user, and closed.
	 */
	protected boolean saveDirtyModels() {
		// Get the items to close
		final List<? extends IResource> resources = getSelectedResources();
		if (resources == null || resources.isEmpty()) {
			// No action needs to be taken since no resources are selected
			return false;
		}

		final List<IProject> projects = new ArrayList<IProject>();
		for (IResource resource : resources) {
			if (resource instanceof IProject) {
				projects.add((IProject) resource);
			}
		}

		if (projects.isEmpty()) {
			// No action needs to be taken since no projects are selected
			return false;
		}

		final boolean canceled[] = new boolean[1];
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				SafeRunner.run(new SafeRunnable(Messages.error_failedToSaveModelsInWorkbench) {
					@Override
					public void run() {
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						if (window == null) {
							IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
							if (windows.length > 0) {
								window = windows[0];
							}
						}
						if (window != null) {
							canceled[0] = !PlatformUI.getWorkbench().saveAll(window, window, new BasicModelSaveableFilter(projects), true);
						}
					}
				});
			}
		});

		// Abort operation if saving has been canceled by user
		if (canceled[0]) {
			return false;
		}

		// Force reset of dirty information on all models in given projects for clearing dirty information of those
		// models that have not been taken into account by the save operation (happens e.g. when user deselects some
		// or all of them before proceeding with the save operation)
		Set<IModelDescriptor> modelDescriptors = new HashSet<IModelDescriptor>();
		for (IProject project : projects) {
			modelDescriptors.addAll(ModelDescriptorRegistry.INSTANCE.getModels(project));
		}
		for (IModelDescriptor modelDescriptor : modelDescriptors) {
			SaveIndicatorUtil.setSaved(modelDescriptor);
		}

		return true;
	}
}
