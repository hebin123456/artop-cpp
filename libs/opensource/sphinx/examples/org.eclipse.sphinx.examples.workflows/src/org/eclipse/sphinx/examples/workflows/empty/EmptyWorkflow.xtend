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
 *     itemis - [506671] Add support for specifying and injecting user-defined arguments for workflows through workflow launch configurations
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.workflows.empty

import org.eclipse.sphinx.emf.mwe.dynamic.WorkspaceWorkflow

class EmptyWorkflow extends WorkspaceWorkflow {

	new() {
	}
	
	override preInvoke() {
		System.out.println("Running empty workflow")
		super.preInvoke()
	}
}