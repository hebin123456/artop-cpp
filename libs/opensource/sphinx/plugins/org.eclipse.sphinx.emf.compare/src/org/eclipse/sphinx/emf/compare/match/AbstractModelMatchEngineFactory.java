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
package org.eclipse.sphinx.emf.compare.match;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.compare.scope.IModelComparisonScope;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;

public abstract class AbstractModelMatchEngineFactory extends MatchEngineFactoryImpl {

	protected abstract boolean isMatchEngineFactoryFor(IModelDescriptor modelDescriptor);

	public AbstractModelMatchEngineFactory(IMatchEngine matchEngine) {
		this.matchEngine = matchEngine;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.emf.compare.match.IMatchEngine.Factory.isMatchEngineFactoryFor(org.eclipse.emf.compare
	 *      .scope.IComparisonScope)
	 */
	@Override
	public boolean isMatchEngineFactoryFor(IComparisonScope scope) {
		if (scope instanceof IModelComparisonScope) {
			return isMatchEngineFactoryFor((IModelComparisonScope) scope);
		}

		final Notifier left = scope.getLeft();
		final Notifier right = scope.getRight();
		final Notifier origin = scope.getOrigin();
		if (left instanceof EObject && right instanceof EObject && (origin == null || origin instanceof EObject) || left instanceof Resource
				&& right instanceof Resource && (origin == null || origin instanceof Resource)) {
			IModelDescriptor modelDescriptor = getModelDescriptor(left);
			if (modelDescriptor == null) {
				modelDescriptor = getModelDescriptor(right);
			}
			return isMatchEngineFactoryFor(modelDescriptor);
		}
		return false;
	}

	public boolean isMatchEngineFactoryFor(IModelComparisonScope scope) {
		Object left = scope.isFileBasedComparison() ? scope.getLeftFile() : scope.getLeft();
		Object right = scope.isFileBasedComparison() ? scope.getRightFile() : scope.getRight();

		IModelDescriptor modelDescriptor = getModelDescriptor(left);
		if (modelDescriptor == null) {
			modelDescriptor = getModelDescriptor(right);
		}

		return modelDescriptor != null ? isMatchEngineFactoryFor(modelDescriptor) : true;
	}

	protected IModelDescriptor getModelDescriptor(Object object) {
		IModelDescriptor modelDescriptor = null;

		if (object instanceof IFile) {
			modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel((IFile) object);
		} else if (object instanceof Resource) {
			modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel((Resource) object);
		} else if (object instanceof EObject) {
			modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(((EObject) object).eResource());
		}
		return modelDescriptor;
	}
}
