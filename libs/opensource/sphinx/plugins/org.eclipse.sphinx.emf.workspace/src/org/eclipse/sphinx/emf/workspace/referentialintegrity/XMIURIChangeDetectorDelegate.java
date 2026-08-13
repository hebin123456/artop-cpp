/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 See4sys, itemis, Siemens and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [418005] Add support for model files with multiple root elements
 *     Siemens - [577073] URI change detector delegate extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.referentialintegrity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 *
 */
public class XMIURIChangeDetectorDelegate implements IURIChangeDetectorDelegate {

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.referentialintegrity.IURIChangeDetectorDelegate#detectChangedURIs(java.util.
	 * List)
	 */
	@Override
	public Map<Resource, List<URIChangeNotification>> detectChangedURIs(List<Notification> notifications) {

		Map<Resource, List<URIChangeNotification>> resourceToUriChangeNotification = new HashMap<>();

		for (Notification notification : notifications) {
			Object notifier = notification.getNotifier();
			if (notifier instanceof EObject) {
				EObject eObject = (EObject) notifier;
				Resource resource = ((EObject) notifier).eResource();

				resourceToUriChangeNotification.computeIfAbsent(resource, k -> new ArrayList<>());
				resourceToUriChangeNotification.get(resource).add(new URIChangeNotification(eObject, EcoreResourceUtil.getURI(eObject)));
			}
		}

		return resourceToUriChangeNotification;
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.referencialintegrity.IURIChangeDetectorDelegate#detectChangedURIs(org.eclipse
	 * .emf .common.notify.Notification)
	 */
	@Override
	public List<URIChangeNotification> detectChangedURIs(Notification notification) {
		return Collections.emptyList(); // NOOP implementation for the deprecated API
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.referencialintegrity.IURIChangeDetectorDelegate#detectChangedURIs(org.eclipse
	 * .core .resources.IFile, org.eclipse.core.resources.IFile)
	 */
	@Override
	public List<URIChangeNotification> detectChangedURIs(IFile oldFile, IFile newFile) {
		if (!oldFile.getFullPath().equals(newFile.getFullPath())) {
			final Resource resource = EcorePlatformUtil.getResource(oldFile);
			if (resource != null) {
				final TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(resource);
				if (editingDomain != null) {
					try {
						return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<List<URIChangeNotification>>() {
							@Override
							public void run() {
								List<URIChangeNotification> uriChangeNotifications = new ArrayList<URIChangeNotification>();

								TreeIterator<EObject> allContents = resource.getAllContents();
								while (allContents.hasNext()) {
									EObject eObject = allContents.next();
									uriChangeNotifications.add(new URIChangeNotification(eObject, EcoreResourceUtil.getURI(eObject)));
								}

								setResult(uriChangeNotifications);
							}
						});
					} catch (InterruptedException ex) {
						PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
					}
				}
			}
		}
		return Collections.emptyList();
	}
}
