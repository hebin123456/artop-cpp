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
package org.eclipse.sphinx.emf.compare.team.subversive.ui.operations;

import java.lang.reflect.Constructor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.ReflectUtil;

public class DelegatingCompositeOperation implements IActionOperation {

	protected CompositeOperation delegate;

	public DelegatingCompositeOperation(String operationName) {
		this(operationName, SVNMessages.class);
	}

	public DelegatingCompositeOperation(String operationName, Class<? extends NLS> messagesClass) {
		// Ensure backward compatibility with Eclipse 3.5.x and earlier
		try {
			if (ExtendedPlatform.getFeatureVersionOrdinal() >= 36) {
				Constructor<CompositeOperation> constructor = CompositeOperation.class.getConstructor(String.class, Class.class);
				delegate = constructor.newInstance(operationName, messagesClass);
			} else {
				Constructor<CompositeOperation> constructor = CompositeOperation.class.getConstructor(String.class);
				delegate = constructor.newInstance(operationName);
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
	}

	public IActionOperation run(IProgressMonitor monitor) {
		return delegate.run(monitor);
	}

	public IStatus getStatus() {
		return delegate.getStatus();
	}

	public int getExecutionState() {
		return delegate.getExecutionState();
	}

	public String getOperationName() {
		return delegate.getOperationName();
	}

	public int getOperationWeight() {
		return delegate.getOperationWeight();
	}

	public String getId() {
		return delegate.getId();
	}

	public Class<? extends NLS> getMessagesClass() {
		return null;
	}

	public ISchedulingRule getSchedulingRule() {
		return delegate.getSchedulingRule();
	}

	public IConsoleStream getConsoleStream() {
		return delegate.getConsoleStream();
	}

	public void setConsoleStream(IConsoleStream stream) {
		delegate.setConsoleStream(stream);
	}

	public void add(IActionOperation operation) {
		delegate.add(operation);
	}

	public void add(IActionOperation operation, IActionOperation[] dependsOnOperation) {
		delegate.add(operation, dependsOnOperation);
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	public void reportStatus(int severity, String message, Throwable t) {
		// Ensure backward compatibility with Eclipse 3.6.x and earlier
		try {
			if (ExtendedPlatform.getFeatureVersionOrdinal() >= 37) {
				ReflectUtil.invokeMethod(delegate, "reportStatus", severity, message, t); //$NON-NLS-1$
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
	}
}
