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
package org.eclipse.sphinx.emf.validation.constraint;

import org.eclipse.emf.validation.service.IConstraintDescriptor;

/**
 * This interface provide an extension of the {@link IConstraintDescriptor} one in order to access simply to features
 * whether the constraint is not described through an xml extension point contribution.
 */
public interface IExtendedConstraintDescriptor extends IConstraintDescriptor {

	/**
	 * @return target features
	 */
	public String[] getFeatures();

}
