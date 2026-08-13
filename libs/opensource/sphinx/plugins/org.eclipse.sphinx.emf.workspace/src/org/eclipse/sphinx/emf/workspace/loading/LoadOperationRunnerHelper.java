/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.loading;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.emf.workspace.loading.operations.ILoadOperation;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class LoadOperationRunnerHelper {

	public static <T extends ILoadOperation> void run(T operation, boolean async, IProgressMonitor monitor) {
		if (async) {
			LoadJobScheduler loadJobScheduler = new LoadJobScheduler();
			loadJobScheduler.scheduleModelLoadJob(operation);
		} else {
			try {
				operation.run(monitor);
			} catch (OperationCanceledException ex) {
				// Ignore exception
			} catch (CoreException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}
}
