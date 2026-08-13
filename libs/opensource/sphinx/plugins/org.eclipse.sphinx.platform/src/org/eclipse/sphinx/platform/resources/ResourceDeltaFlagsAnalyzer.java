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
package org.eclipse.sphinx.platform.resources;

import org.eclipse.core.resources.IResourceDelta;

/**
 * Helper class for analyzing {@linkplain IResourceDelta resource delta}s.
 */
public class ResourceDeltaFlagsAnalyzer {

	public final boolean ADDED;
	public final boolean ADDED_PHANTOM;
	public final boolean ALL_WITH_PHANTOMS;
	public final boolean CHANGED;
	public final boolean CONTENT;
	public final boolean COPIED_FROM;
	public final boolean DESCRIPTION;
	public final boolean ENCODING;
	public final boolean LOCAL_CHANGED;
	public final boolean MARKERS;
	public final boolean MOVED_FROM;
	public final boolean MOVED_TO;
	public final boolean NO_CHANGE;
	public final boolean OPEN;
	public final boolean REMOVED;
	public final boolean REMOVED_PHANTOM;
	public final boolean REPLACED;
	public final boolean SYNC;
	public final boolean TYPE;
	public final boolean ZERO;

	public ResourceDeltaFlagsAnalyzer(IResourceDelta delta) {
		int flags = delta.getFlags();
		ADDED = (flags & IResourceDelta.ADDED) != 0;
		ADDED_PHANTOM = (flags & IResourceDelta.ADDED_PHANTOM) != 0;
		ALL_WITH_PHANTOMS = (flags & IResourceDelta.ALL_WITH_PHANTOMS) != 0;
		CHANGED = (flags & IResourceDelta.CHANGED) != 0;
		CONTENT = (flags & IResourceDelta.CONTENT) != 0;
		COPIED_FROM = (flags & IResourceDelta.COPIED_FROM) != 0;
		DESCRIPTION = (flags & IResourceDelta.DESCRIPTION) != 0;
		ENCODING = (flags & IResourceDelta.ENCODING) != 0;
		LOCAL_CHANGED = (flags & IResourceDelta.LOCAL_CHANGED) != 0;
		MARKERS = (flags & IResourceDelta.MARKERS) != 0;
		MOVED_FROM = (flags & IResourceDelta.MOVED_FROM) != 0;
		MOVED_TO = (flags & IResourceDelta.MOVED_TO) != 0;
		NO_CHANGE = (flags & IResourceDelta.NO_CHANGE) != 0;
		OPEN = (flags & IResourceDelta.OPEN) != 0;
		REMOVED = (flags & IResourceDelta.REMOVED) != 0;
		REMOVED_PHANTOM = (flags & IResourceDelta.REMOVED_PHANTOM) != 0;
		REPLACED = (flags & IResourceDelta.REPLACED) != 0;
		SYNC = (flags & IResourceDelta.SYNC) != 0;
		TYPE = (flags & IResourceDelta.TYPE) != 0;
		ZERO = flags == 0;
	}

	@Override
	public String toString() {
		return ((ADDED ? " ADDED" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (ADDED_PHANTOM ? " ADDED_PHANTOM" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (CHANGED ? " CHANGED" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (CONTENT ? " CONTENT" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (COPIED_FROM ? " COPIED_FROM" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (DESCRIPTION ? " DESCRIPTION" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (ENCODING ? " ENCODING" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (LOCAL_CHANGED ? " LOCAL_CHANGED" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (MARKERS ? " MARKERS" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (MOVED_FROM ? " MOVED_FROM" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (MOVED_TO ? " MOVED_TO" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (NO_CHANGE ? " NO_CHANGE" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (OPEN ? " OPEN" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (REMOVED ? " REMOVED" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (REMOVED_PHANTOM ? " REMOVED_PHANTOM" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (REPLACED ? " REPLACED" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (SYNC ? " SYNC" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (TYPE ? " TYPE" : "") //$NON-NLS-1$ //$NON-NLS-2$
		+ (ZERO ? " ZERO" : "")).substring(1); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
