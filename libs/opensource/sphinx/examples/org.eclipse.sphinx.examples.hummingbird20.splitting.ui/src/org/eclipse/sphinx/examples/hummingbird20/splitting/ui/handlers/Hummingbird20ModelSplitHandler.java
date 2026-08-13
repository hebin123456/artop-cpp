/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.splitting.ui.handlers;

import org.eclipse.sphinx.emf.workspace.ui.handlers.BasicModelSplitHandler;
import org.eclipse.sphinx.examples.hummingbird20.splitting.Hummingbird20TypeModelSplitPolicy;

public class Hummingbird20ModelSplitHandler extends BasicModelSplitHandler {

	public Hummingbird20ModelSplitHandler() {
		super(new Hummingbird20TypeModelSplitPolicy());
	}
}
