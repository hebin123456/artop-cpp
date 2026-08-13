/**
 * <copyright>
 *
 * Copyright (c) 2021 Siemens and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     Siemens - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.resource;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.XMLResource.ResourceHandler;

public class Hummingbird20ResourceHandler implements ResourceHandler {

	private boolean isPreLoadCalled;
	private boolean isPostLoadCalled;
	private boolean isPreSaveCalled;
	private boolean isPostSaveCalled;

	public boolean isPreLoadCalled() {
		return isPreLoadCalled;
	}

	public boolean isPostLoadCalled() {
		return isPostLoadCalled;
	}

	public boolean isPreSaveCalled() {
		return isPreSaveCalled;
	}

	public boolean isPostSaveCalled() {
		return isPostSaveCalled;
	}

	@Override
	public void preLoad(XMLResource resource, InputStream inputStream, Map<?, ?> options) {
		isPreLoadCalled = true;
	}

	@Override
	public void postLoad(XMLResource resource, InputStream inputStream, Map<?, ?> options) {
		isPostLoadCalled = true;
	}

	@Override
	public void preSave(XMLResource resource, OutputStream outputStream, Map<?, ?> options) {
		isPreSaveCalled = true;
	}

	@Override
	public void postSave(XMLResource resource, OutputStream outputStream, Map<?, ?> options) {
		isPostSaveCalled = true;
	}

}
