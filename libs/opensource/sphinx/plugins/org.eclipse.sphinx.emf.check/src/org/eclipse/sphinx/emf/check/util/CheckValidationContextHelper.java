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
package org.eclipse.sphinx.emf.check.util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.sphinx.emf.check.ICheckValidator;

public class CheckValidationContextHelper {

	protected Map<Object, Object> context;

	public CheckValidationContextHelper(Map<Object, Object> context) {
		this.context = context;
	}

	public Set<String> getConstraintCategories() {
		if (context != null) {
			Object categories = context.get(ICheckValidator.OPTION_CATEGORIES);
			if (categories instanceof Set<?>) {
				@SuppressWarnings("unchecked")
				Set<String> castedCategories = (Set<String>) categories;
				return castedCategories;
			}
		}
		return Collections.emptySet();
	}

	public boolean areIntrinsicModelIntegrityConstraintsEnabled() {
		if (context != null) {
			Object enabled = context.get(ICheckValidator.OPTION_ENABLE_INTRINSIC_MODEL_INTEGRITY_CONSTRAINTS);
			if (enabled instanceof Boolean) {
				return (Boolean) enabled;
			}
		}
		return false;
	}

	public void addEnableIntrinsicModelIntegrityConstraintsOption(Boolean enabled) {
		if (context != null && enabled != null) {
			context.put(ICheckValidator.OPTION_ENABLE_INTRINSIC_MODEL_INTEGRITY_CONSTRAINTS, enabled);
		}
	}

	public Boolean removeEnableIntrinsicModelIntegrityConstraintsOption() {
		if (context != null) {
			Object enabled = context.remove(ICheckValidator.OPTION_ENABLE_INTRINSIC_MODEL_INTEGRITY_CONSTRAINTS);
			if (enabled instanceof Boolean) {
				return (Boolean) enabled;
			}
		}
		return null;
	}

	public IProgressMonitor getProgressMonitor() {
		if (context != null) {
			Object monitor = context.get(ICheckValidator.OPTION_PROGRESS_MONITOR);
			if (monitor instanceof IProgressMonitor) {
				return (IProgressMonitor) monitor;
			}
		}
		return new NullProgressMonitor();
	}

	public void addProgressMonitorOption(IProgressMonitor monitor) {
		if (context != null && monitor != null) {
			context.put(ICheckValidator.OPTION_PROGRESS_MONITOR, monitor);
		}
	}

	public IProgressMonitor removeProgressMonitorOption() {
		if (context != null) {
			Object monitor = context.remove(ICheckValidator.OPTION_PROGRESS_MONITOR);
			if (monitor instanceof IProgressMonitor) {
				return (IProgressMonitor) monitor;
			}
		}
		return null;
	}
}
