/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [468171] Model element splitting service
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;

public class ModelResourceDescriptor {

	private URI uri;
	private String contentTypeId;
	private List<EObject> contents;

	public ModelResourceDescriptor(URI uri, String contentTypeId, EObject content) {
		this(uri, contentTypeId, Collections.singletonList(content));
	}

	public ModelResourceDescriptor(IPath path, String contentTypeId, EObject content) {
		this(EcorePlatformUtil.createURI(path), contentTypeId, Collections.singletonList(content));
	}

	public ModelResourceDescriptor(URI uri, String contentTypeId, List<EObject> contents) {
		Assert.isNotNull(uri);
		Assert.isNotNull(contents);
		this.uri = uri;
		this.contentTypeId = contentTypeId;
		this.contents = contents;
	}

	public ModelResourceDescriptor(IPath path, String contentTypeId, List<EObject> contents) {
		this(EcorePlatformUtil.createURI(path), contentTypeId, contents);
	}

	/**
	 * @deprecated Use {@link #ModelResourceDescriptor(IPath, String, List)} instead.
	 */
	@Deprecated
	public ModelResourceDescriptor(Collection<EObject> contents, IPath path, String contentTypeId) {
		this(EcorePlatformUtil.createURI(path), contentTypeId, new ArrayList<EObject>(contents));
	}

	/**
	 * @deprecated Use {@link #getContents()} instead.
	 */
	@Deprecated
	public Collection<EObject> getModelRoots() {
		return getContents();
	}

	public URI getURI() {
		return uri;
	}

	public IPath getPath() {
		return EcorePlatformUtil.createPath(uri);
	}

	public String getContentTypeId() {
		return contentTypeId;
	}

	public List<EObject> getContents() {
		return contents;
	}
}
