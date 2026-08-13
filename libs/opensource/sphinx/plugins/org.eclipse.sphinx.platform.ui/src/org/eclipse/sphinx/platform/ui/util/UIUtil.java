/**
 * <copyright>
 * 
 * Copyright (c) 2011 itemis and others.
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
package org.eclipse.sphinx.platform.ui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Provides utility methods for common user interface tasks.
 */
public class UIUtil {

	/**
	 * Unwraps given <code>object</code> and extracts an object with given {@link Class type} from it.
	 * 
	 * @param object
	 *            The object to be unwrapped.
	 * @param type
	 *            The {@link Class type} of the object to be extracted.
	 * @return An object with specified {@link Class type} that is wrapped by given <code>object</code>, or
	 *         <code>null</code> if given <code>object</code> does not wrap an object with given type or no object at
	 *         all.
	 */
	public static <T> T unwrap(Object object, Class<T> type) {
		if (object instanceof ExecutionEvent) {
			object = HandlerUtil.getCurrentSelection((ExecutionEvent) object);
		}

		if (object instanceof IStructuredSelection) {
			object = ((IStructuredSelection) object).getFirstElement();
		}

		if (object instanceof IAdaptable) {
			object = ((IAdaptable) object).getAdapter(type);
		}

		if (type.isInstance(object)) {
			return type.cast(object);
		}

		return null;
	}

	/**
	 * Unwraps given <code>object</code> and extracts a list of objects with given {@link Class type} from it.
	 * 
	 * @param object
	 *            The object to be unwrapped.
	 * @param type
	 *            The {@link Class type} of the objects to be extracted.
	 * @return A list of objects with specified {@link Class type} that is wrapped by given <code>object</code>, or
	 *         <code>null</code> if given <code>object</code> does not wrap any objects with given type or no objects at
	 *         all.
	 */
	public static <T> List<T> unwrapAll(Object object, Class<T> clazz) {
		if (object instanceof ExecutionEvent) {
			object = HandlerUtil.getCurrentSelection((ExecutionEvent) object);
		}

		if (object instanceof IStructuredSelection) {
			List<T> list = new ArrayList<T>();
			IStructuredSelection selection = (IStructuredSelection) object;
			for (Iterator<?> it = selection.iterator(); it.hasNext();) {
				T next = unwrap(it.next(), clazz);
				if (next != null) {
					list.add(next);
				}
			}
			return list;
		}

		T element = unwrap(object, clazz);
		if (element != null) {
			return Collections.singletonList(element);
		}

		return Collections.emptyList();
	}
}
