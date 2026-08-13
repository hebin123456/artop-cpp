/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     See4sys - copied from org.eclipse.ui.internal.navigator.resources.actions.ResourceMgmtActionProvider
 *               for overriding {@link CloseResourceAction}
 *******************************************************************************/
package org.eclipse.sphinx.emf.explorer.internal.actions.providers;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.sphinx.emf.explorer.internal.Activator;
import org.eclipse.sphinx.emf.explorer.internal.actions.CloseResourceOverrideAction;
import org.eclipse.sphinx.platform.util.StatusUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.actions.CloseUnrelatedProjectsAction;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.actions.RefreshAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.navigator.resources.actions.ResourceMgmtActionProvider;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * Customized resource management action provider that is intended to override the {@link ResourceMgmtActionProvider
 * original one} from Eclipse. It is used for overriding {@link CloseResourceAction} and discarding
 * {@link CloseUnrelatedProjectsAction}.
 * <p>
 * Unfortunately, there was no other choice than copying the whole code from {@link ResourceMgmtActionProvider} for that
 * purpose because most of the relevant methods and fields of the latter are private or package private.
 * </p>
 */
@SuppressWarnings("restriction")
public class ResourceMgmtOverrideActionProvider extends CommonActionProvider {

	private BuildAction buildAction;

	private OpenResourceAction openProjectAction;

	private CloseResourceAction closeProjectAction;

	// TODO Override CloseUnrelatedProjectsAction in the same way as CloseResourceAction and make it available again
	// private CloseUnrelatedProjectsAction closeUnrelatedProjectsAction;

	private RefreshAction refreshAction;

	/**
	 * The common navigator.
	 */
	private CommonNavigator navigator;

	/*
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	@Override
	public void init(ICommonActionExtensionSite site) {
		super.init(site);

		ICommonViewerSite viewSite = site.getViewSite();
		if (viewSite instanceof ICommonViewerWorkbenchSite) {
			IWorkbenchPart part = ((ICommonViewerWorkbenchSite) viewSite).getPart();
			if (part instanceof CommonNavigator) {
				navigator = (CommonNavigator) part;
				makeActions();
			}
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.BUILD_PROJECT.getId(), buildAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.OPEN_PROJECT.getId(), openProjectAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.CLOSE_PROJECT.getId(), closeProjectAction);
		// TODO Override CloseUnrelatedProjectsAction in the same way as CloseResourceAction and make it available again
		// actionBars.setGlobalActionHandler(IDEActionFactory.CLOSE_UNRELATED_PROJECTS.getId(),
		// closeUnrelatedProjectsAction);
		updateActionBars();
	}

	/**
	 * Adds the build, open project, close project and refresh resource actions to the context menu.
	 * <p>
	 * The following conditions apply: build-only projects selected, auto build disabled, at least one builder present
	 * open project-only projects selected, at least one closed project close project-only projects selected, at least
	 * one open project refresh-no closed project selected
	 * </p>
	 * <p>
	 * Both the open project and close project action may be on the menu at the same time.
	 * </p>
	 * <p>
	 * No disabled action should be on the context menu.
	 * </p>
	 *
	 * @param menu
	 *            context menu to add actions to
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		boolean isProjectSelection = true;
		boolean hasOpenProjects = false;
		boolean hasClosedProjects = false;
		boolean hasBuilder = true; // false if any project is closed or does not have builder
		Iterator<?> resources = selection.iterator();

		while (resources.hasNext() && (!hasOpenProjects || !hasClosedProjects || hasBuilder || isProjectSelection)) {
			Object next = resources.next();
			IProject project = null;

			if (next instanceof IProject) {
				project = (IProject) next;
			} else if (next instanceof IAdaptable) {
				project = (IProject) ((IAdaptable) next).getAdapter(IProject.class);
			}

			if (project == null) {
				isProjectSelection = false;
				continue;
			}
			if (project.isOpen()) {
				hasOpenProjects = true;
				if (hasBuilder && !hasBuilder(project)) {
					hasBuilder = false;
				}
			} else {
				hasClosedProjects = true;
				hasBuilder = false;
			}
		}
		if (!selection.isEmpty() && isProjectSelection && !ResourcesPlugin.getWorkspace().isAutoBuilding() && hasBuilder) {
			// Allow manual incremental build only if auto build is off.
			buildAction.selectionChanged(selection);
			menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, buildAction);
		}
		if (!hasClosedProjects) {
			refreshAction.selectionChanged(selection);
			menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, refreshAction);
		}
		if (isProjectSelection) {
			if (hasClosedProjects) {
				openProjectAction.selectionChanged(selection);
				menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, openProjectAction);
			}
			if (hasOpenProjects) {
				closeProjectAction.selectionChanged(selection);
				menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, closeProjectAction);
				// TODO Override CloseUnrelatedProjectsAction in the same way as CloseResourceAction and make it
				// available again
				// closeUnrelatedProjectsAction.selectionChanged(selection);
				// menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, closeUnrelatedProjectsAction);
			}
		}
	}

	/**
	 * Returns whether there are builders configured on the given project.
	 *
	 * @return <code>true</code> if it has builders, <code>false</code> if not, or if this could not be determined
	 */
	boolean hasBuilder(IProject project) {
		try {
			ICommand[] commands = project.getDescription().getBuildSpec();
			if (commands.length > 0) {
				return true;
			}
		} catch (CoreException e) {
			// Cannot determine if project has builders. Project is closed
			// or does not exist. Fall through to return false.
		}
		return false;
	}

	protected void makeActions() {
		// The shell provider for any dialog that could be opened by actions
		final IShellProvider provider = navigator.getSite();

		openProjectAction = createOpenResourceAction(provider);
		closeProjectAction = createCloseResourceAction(provider);

		// TODO Override CloseUnrelatedProjectsAction in the same way as CloseResourceAction and make it available again
		// closeUnrelatedProjectsAction = new CloseUnrelatedProjectsAction(shell);

		refreshAction = createRefreshAction(provider);
		refreshAction.setDisabledImageDescriptor(getImageDescriptor("dlcl16/refresh_nav.gif"));//$NON-NLS-1$
		refreshAction.setImageDescriptor(getImageDescriptor("elcl16/refresh_nav.gif"));//$NON-NLS-1$
		refreshAction.setActionDefinitionId("org.eclipse.ui.file.refresh"); //$NON-NLS-1$

		buildAction = createBuildAction(provider);
		buildAction.setActionDefinitionId("org.eclipse.ui.project.buildProject"); //$NON-NLS-1$
	}

	/**
	 * Creates the {@link OpenResourceAction open resource action} to be used by this action provider.
	 * <p>
	 * Clients may override this method if they need to use a custom implementation of {@link OpenResourceAction}.
	 * </p>
	 *
	 * @param provider
	 *            The {@link IShellProvider shell provider} for any dialogs that could be opened by the action.
	 * @return The open resource action to be used.
	 */
	protected OpenResourceAction createOpenResourceAction(final IShellProvider provider) {
		return new OpenResourceAction(provider);
	}

	/**
	 * Creates the {@link CloseResourceAction close resource action} to be used by this action provider.
	 * <p>
	 * Clients may override this method if they need to use a custom implementation of {@link CloseResourceAction}.
	 * </p>
	 *
	 * @param provider
	 *            The {@link IShellProvider shell provider} for any dialogs that could be opened by the action.
	 * @return The close resource action to be used.
	 */
	protected CloseResourceAction createCloseResourceAction(final IShellProvider provider) {
		return new CloseResourceOverrideAction(provider);
	}

	/**
	 * Creates the {@link RefreshAction refresh action} to be used by this action provider.
	 * <p>
	 * Clients may override this method if they need to use a custom implementation of {@link RefreshAction}.
	 * </p>
	 *
	 * @param provider
	 *            The {@link IShellProvider shell provider} for any dialogs that could be opened by the action.
	 * @return The refresh action to be used.
	 */
	protected RefreshAction createRefreshAction(final IShellProvider provider) {
		return new RefreshAction(provider) {
			@Override
			public void run() {
				final IStatus[] errorStatus = new IStatus[1];
				errorStatus[0] = Status.OK_STATUS;
				final WorkspaceModifyOperation op = (WorkspaceModifyOperation) createOperation(errorStatus);
				WorkspaceJob job = new WorkspaceJob("refresh") { //$NON-NLS-1$

					@Override
					public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
						try {
							op.run(monitor);
							Shell shell = provider.getShell();
							if (shell != null && !shell.isDisposed()) {
								shell.getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										// FIXME Couldn't this be replaced by:
										// 'TreeViewer viewer = navigator.getViewer();'?
										StructuredViewer viewer = getActionSite().getStructuredViewer();
										if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
											viewer.refresh();
										}
									}
								});
							}
						} catch (InvocationTargetException e) {
							throw new CoreException(StatusUtil.createErrorStatus(Activator.getPlugin(), e.getTargetException()));
						} catch (InterruptedException e) {
							return Status.CANCEL_STATUS;
						}
						return errorStatus[0];
					}

				};
				ISchedulingRule rule = op.getRule();
				if (rule != null) {
					job.setRule(rule);
				}
				job.setUser(true);
				job.schedule();
			}
		};
	}

	/**
	 * Creates the {@link BuildAction build action} to be used by this action provider.
	 * <p>
	 * Clients may override this method if they need to use a custom implementation of {@link BuildAction}.
	 * </p>
	 *
	 * @param provider
	 *            The {@link IShellProvider shell provider} for any dialogs that could be opened by the action.
	 * @return The build action to be used.
	 */
	protected BuildAction createBuildAction(final IShellProvider provider) {
		return new BuildAction(provider, IncrementalProjectBuilder.INCREMENTAL_BUILD);
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	protected ImageDescriptor getImageDescriptor(String relativePath) {
		// FIXME Check if images can be retrieved in a warning free way
		return IDEWorkbenchPlugin.getIDEImageDescriptor(relativePath);
	}

	@Override
	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		refreshAction.selectionChanged(selection);
		buildAction.selectionChanged(selection);
		openProjectAction.selectionChanged(selection);
		// TODO Override CloseUnrelatedProjectsAction in the same way as CloseResourceAction and make it available again
		// closeUnrelatedProjectsAction.selectionChanged(selection);
		closeProjectAction.selectionChanged(selection);
	}

}
