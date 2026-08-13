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
package org.eclipse.sphinx.emf.compare.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.ReferenceChange;
import org.eclipse.emf.compare.ResourceAttachmentChange;
import org.eclipse.emf.compare.domain.ICompareEditingDomain;
import org.eclipse.emf.compare.domain.impl.EMFCompareEditingDomain;
import org.eclipse.emf.compare.utils.MatchUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.compare.domain.DelegatingEMFCompareEditingDomain;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;

public final class ModelCompareUtil {

	private ModelCompareUtil() {
	}

	public static ICompareEditingDomain createEMFCompareEditingDomain(final Object left, final Object right, Notifier origin) {
		if (left instanceof IFile && right instanceof IFile) {
			return new DelegatingEMFCompareEditingDomain();
		}

		if (!(left instanceof Notifier) || !(right instanceof Notifier)) {
			return null;
		}

		EditingDomain delegatingEditingDomain = getEditingDomain(left, right);
		CommandStack delegatingCommandStack = null;
		if (delegatingEditingDomain != null) {
			delegatingCommandStack = delegatingEditingDomain.getCommandStack();
		}

		if (delegatingCommandStack == null) {
			return EMFCompareEditingDomain.create((Notifier) left, (Notifier) right, origin);
		} else {
			return EMFCompareEditingDomain.create((Notifier) left, (Notifier) right, origin, delegatingCommandStack);
		}
	}

	public static EditingDomain getEditingDomain(Object left, Object right) {
		EditingDomain editingDomain = null;
		TransactionalEditingDomain leftEditingDomain = WorkspaceEditingDomainUtil.getEditingDomain(left);
		EditingDomain rightEditingDomain = WorkspaceEditingDomainUtil.getEditingDomain(right);
		if (leftEditingDomain != null) {
			editingDomain = leftEditingDomain;
		} else if (rightEditingDomain != null) {
			editingDomain = rightEditingDomain;
		}
		return editingDomain;
	}

	public static EObject getValue(Comparison comparison, Diff diff) {
		EObject value = null;
		if (diff instanceof ReferenceChange) {
			value = ((ReferenceChange) diff).getValue();
		} else if (diff instanceof ResourceAttachmentChange) {
			value = MatchUtil.getContainer(comparison, diff);
		}
		return value;
	}
}
