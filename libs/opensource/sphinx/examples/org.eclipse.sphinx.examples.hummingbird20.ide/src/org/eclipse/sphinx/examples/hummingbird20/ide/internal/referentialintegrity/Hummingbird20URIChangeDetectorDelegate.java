/**
 * <copyright>
 *
 * Copyright (c) 2016 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.ide.internal.referentialintegrity;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.sphinx.emf.workspace.referentialintegrity.AbstractHierarchicalFragmentURIChangeDetectorDelegate;
import org.eclipse.sphinx.emf.workspace.referentialintegrity.IURIChangeDetectorDelegate;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Package;
import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;

/**
 * An {@link IURIChangeDetectorDelegate} implementation enabling the detection of changes in URIs used to persist
 * Hummingbird 2.0 models.
 *
 * @see IURIChangeDetectorDelegate
 * @see AbstractHierarchicalFragmentURIChangeDetectorDelegate
 */
public class Hummingbird20URIChangeDetectorDelegate extends AbstractHierarchicalFragmentURIChangeDetectorDelegate {

	/*
	 * Overridden to signal name changes of Hummingbird 2.0 Identifiable objects.
	 * @see org.eclipse.sphinx.emf.workspace.referentialintegrity.AbstractURIChangeDetectorDelegate#
	 * affectsURIFragmentSegmentOfChangedObject(org.eclipse.emf.common.notify.Notification)
	 */
	@Override
	protected boolean affectsURIFragmentSegmentOfChangedObject(Notification notification) {
		Object notifier = notification.getNotifier();
		if (notifier instanceof Identifiable) {
			return notification.getFeature() == Common20Package.eINSTANCE.getIdentifiable_Name();
		}
		return false;
	}
}