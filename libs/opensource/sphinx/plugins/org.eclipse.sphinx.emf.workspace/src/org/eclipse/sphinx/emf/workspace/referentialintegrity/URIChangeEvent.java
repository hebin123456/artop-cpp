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
package org.eclipse.sphinx.emf.workspace.referentialintegrity;

import java.util.EventObject;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

/**
 * Stores all informations relative to an URI Change event.
 */
public class URIChangeEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private final List<URIChangeNotification> notifications;

	public URIChangeEvent(Resource source, List<URIChangeNotification> notifications) {
		super(source);
		this.notifications = notifications;
	}

	/**
	 * @return The {@link URIChangeNotification notification} associated to this event.
	 */
	public List<URIChangeNotification> getNotifications() {
		return notifications;
	}
}
