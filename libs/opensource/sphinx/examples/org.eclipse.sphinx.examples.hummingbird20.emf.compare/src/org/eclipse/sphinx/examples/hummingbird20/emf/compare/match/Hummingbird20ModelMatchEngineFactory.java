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
package org.eclipse.sphinx.examples.hummingbird20.emf.compare.match;

import org.eclipse.sphinx.emf.compare.match.DefaultModelMatchEngineFactory;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;

public class Hummingbird20ModelMatchEngineFactory extends DefaultModelMatchEngineFactory {

	@Override
	protected boolean isMatchEngineFactoryFor(IModelDescriptor modelDescriptor) {
		if (modelDescriptor != null) {
			return Hummingbird20MMDescriptor.INSTANCE == modelDescriptor.getMetaModelDescriptor();
		}
		return false;
	}
}
