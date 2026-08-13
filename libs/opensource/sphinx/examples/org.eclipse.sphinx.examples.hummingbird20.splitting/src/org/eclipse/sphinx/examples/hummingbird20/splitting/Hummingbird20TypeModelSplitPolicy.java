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
package org.eclipse.sphinx.examples.hummingbird20.splitting;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.emf.splitting.AbstractModelSplitPolicy;
import org.eclipse.sphinx.emf.splitting.IModelSplitDirective;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter;

public class Hummingbird20TypeModelSplitPolicy extends AbstractModelSplitPolicy {

	private static final String COMPONENT_TYPES_TARGET_FILE_NAME = "ComponentTypes.typemodel"; //$NON-NLS-1$
	private static final String INTERFACES_TARGET_FILE_NAME = "Interfaces.typemodel"; //$NON-NLS-1$
	private static final String MANDATORY_PARAMETERS_TARGET_FILE_NAME = "MandatoryParameters.typemodel"; //$NON-NLS-1$

	/*
	 * @see org.eclipse.sphinx.emf.splitting.IModelSplitPolicy#getSplitDirective(org.eclipse.emf.ecore.EObject)
	 */
	@Override
	public IModelSplitDirective getSplitDirective(EObject eObject) {
		if (eObject == null || eObject.eResource() == null) {
			return null;
		}

		if (eObject instanceof Interface) {
			// Compute URI of target resource folder
			URI targetResourceBaseURI = getTargetResourceBaseURI(eObject);

			// Compute target resource URI for Hummingbird 2.0 interface objects
			URI targetResourceURI = targetResourceBaseURI.appendSegment(INTERFACES_TARGET_FILE_NAME);

			// Return corresponding model split directive making sure that ancestor objects get replicated into target
			// resource WITH their intrinsic properties
			return createModelSplitDirective(eObject, targetResourceURI, false);
		}
		if (eObject instanceof ComponentType) {
			// Compute URI of target resource folder
			URI targetResourceBaseURI = getTargetResourceBaseURI(eObject);

			// Compute target resource URI for Hummingbird 2.0 component type objects
			URI targetResourceURI = targetResourceBaseURI.appendSegment(COMPONENT_TYPES_TARGET_FILE_NAME);

			// Return corresponding model split directive making sure that ancestor objects get replicated into target
			// resource WITHOUT their intrinsic properties
			return createModelSplitDirective(eObject, targetResourceURI, true);
		}
		if (eObject instanceof Parameter) {
			// Mandatory parameter?
			if (!((Parameter) eObject).isOptional()) {
				// Compute URI of target resource folder
				URI targetResourceBaseURI = getTargetResourceBaseURI(eObject);

				// Compute target resource URI for mandatory Hummingbird 2.0 parameter objects
				URI targetResourceURI = targetResourceBaseURI.appendSegment(MANDATORY_PARAMETERS_TARGET_FILE_NAME);

				// Return corresponding model split directive making sure that ancestor objects get replicated into
				// target resource WITH their intrinsic properties
				return createModelSplitDirective(eObject, targetResourceURI, false);
			}
		}
		return null;
	}

	protected URI getTargetResourceBaseURI(EObject eObject) {
		Assert.isNotNull(eObject);
		Assert.isLegal(eObject.eResource() != null);

		IPath path = EcorePlatformUtil.createPath(eObject.eResource().getURI());
		IPath targetResourceBasePath = path.removeLastSegments(1);
		return EcorePlatformUtil.createURI(targetResourceBasePath);
	}

	protected Hummingbird20ModelSplitDirective createModelSplitDirective(EObject eObject, URI targetResourceURI, boolean stripAncestors) {
		return new Hummingbird20ModelSplitDirective(eObject, targetResourceURI, stripAncestors);
	}
}
