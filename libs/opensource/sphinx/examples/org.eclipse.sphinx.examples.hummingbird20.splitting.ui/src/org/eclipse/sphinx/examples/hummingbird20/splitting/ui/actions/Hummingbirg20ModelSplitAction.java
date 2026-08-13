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
package org.eclipse.sphinx.examples.hummingbird20.splitting.ui.actions;

import org.eclipse.sphinx.emf.workspace.ui.actions.BasicModelSplitAction;
import org.eclipse.sphinx.examples.hummingbird20.splitting.Hummingbird20TypeModelSplitPolicy;
import org.eclipse.sphinx.examples.hummingbird20.splitting.ui.internal.messages.Messages;

public class Hummingbirg20ModelSplitAction extends BasicModelSplitAction {

	public Hummingbirg20ModelSplitAction() {
		this(Messages.menu_splitTypeModel_label);
	}

	public Hummingbirg20ModelSplitAction(String text) {
		super(text, new Hummingbird20TypeModelSplitPolicy());
	}
}
