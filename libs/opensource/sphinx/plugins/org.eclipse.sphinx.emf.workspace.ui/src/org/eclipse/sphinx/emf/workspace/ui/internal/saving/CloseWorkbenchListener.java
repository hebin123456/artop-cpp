/**
 * <copyright>
 *
 * Copyright (c) 2008-2012 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [387211] CloseWorkbenchListener prevents Eclipse from showing "Save Resource"-Dialog on Exit
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.internal.saving;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.saving.SaveIndicatorUtil;
import org.eclipse.sphinx.emf.workspace.ui.internal.messages.Messages;
import org.eclipse.ui.ISaveableFilter;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;

/**
 * Implementation of {@linkplain IWorkbenchListener workbench listener} that is responsible for opening a dialog to
 * prompt the user if saving dirty resources before closing workbench is required or not (<em>i.e.</em>, ask for saving
 * confirmation).
 */
public class CloseWorkbenchListener implements IWorkbenchListener {

	/*
	 * @see org.eclipse.ui.IWorkbenchListener#postShutdown(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void postShutdown(IWorkbench workbench) {
		// Nothing to do.
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchListener#preShutdown(org.eclipse.ui.IWorkbench, boolean)
	 */
	@Override
	public boolean preShutdown(IWorkbench workbench, boolean forced) {

		final boolean canceled[] = new boolean[1];
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				SafeRunner.run(new SafeRunnable(Messages.error_failedToSaveModelsDuringWorkbenchClosing) {
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
							/*
							 * !!! Important Note !!! In Eclipse version 3.8.0, the
							 * Workbench.saveAll(IShellProvider,IRunnableContext,ISaveableFilter,boolean) method's
							 * implementation was robust enough to handle a call where the ISaveableFilter is null. For
							 * that reason, we pass a new instance of AllSaveablesFilter that indicates we should save
							 * all dirty saveables instead of null.
							 */
							canceled[0] = !PlatformUI.getWorkbench().saveAll(window, window, new AllSaveablesFilter(), true);
						}
					}
				});
			}
		});

		// Abort operation if saving has been canceled by user
		if (canceled[0]) {
			return false;
		}

		// Force reset of dirty information on all models in the workspace for clearing dirty information of those
		// models that have not been taken into account by the save operation (happens e.g. when user deselects some
		// or all of them before proceeding with the save operation)
		for (IModelDescriptor modelDescriptor : ModelDescriptorRegistry.INSTANCE.getAllModels()) {
			SaveIndicatorUtil.setSaved(modelDescriptor);
		}

		return true;
	}

	/**
	 * This class provides a filter for saving all dirty saveables.
	 */
	private class AllSaveablesFilter implements ISaveableFilter {

		@Override
		public boolean select(Saveable saveable, IWorkbenchPart[] containingParts) {
			return saveable.isDirty();
		}
	}
}
