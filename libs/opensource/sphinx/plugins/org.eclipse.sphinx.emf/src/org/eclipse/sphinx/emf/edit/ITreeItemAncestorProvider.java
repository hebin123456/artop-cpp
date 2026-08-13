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
package org.eclipse.sphinx.emf.edit;

import java.util.List;

public interface ITreeItemAncestorProvider {

	List<Object> getAncestorPath(Object object, boolean unwrap);

	List<Object> getAncestorPath(Object beginObject, Class<?> endType, boolean unwrap);

	Object findAncestor(Object object, Class<?> ancestorType, boolean unwrap);
}
