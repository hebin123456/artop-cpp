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
package org.eclipse.sphinx.emf.util;

import java.lang.reflect.Constructor;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.platform.util.StatusUtil;

/**
 * An extensible unwrapping delegating list implementation with a wrapping and unwrapping object support.
 */
public class BasicUnwrappingEList<W extends IWrapper<T>, T> extends AbstractUnwrappingEList<W, T> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates an instance of the BasicUnwrappingEList delegating list.
	 *
	 * @param wrapperType
	 *            the wrapper type
	 * @param targetType
	 *            the target object type
	 */
	public BasicUnwrappingEList(List<W> delegateList, Class<W> wrapperType, Class<T> targetType) {
		super(delegateList, wrapperType, targetType);
	}

	/**
	 * Wraps the given object.
	 *
	 * @param object
	 *            object to be wrapped.
	 * @return the wrapped object.
	 * @throws CoreException
	 */
	@Override
	protected W wrap(T object) throws CoreException {
		try {
			Constructor<W> constructor = wrapperType.getDeclaredConstructor(targetType);
			return constructor.newInstance(object);
		} catch (Exception ex) {
			IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
			throw new CoreException(status);
		}
	}

	/**
	 * Unwraps the given object.
	 *
	 * @param object
	 *            object to be unwrapped.
	 * @return the unwrapped object.
	 */
	@Override
	protected T unwrap(W object) {
		return object.getTarget();
	}
}
