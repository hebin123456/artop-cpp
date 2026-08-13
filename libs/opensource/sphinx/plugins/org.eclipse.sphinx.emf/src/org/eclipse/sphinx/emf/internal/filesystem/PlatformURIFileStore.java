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
package org.eclipse.sphinx.emf.internal.filesystem;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.platform.messages.PlatformMessages;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.StatusUtil;
import org.osgi.framework.Bundle;

/**
 * Represents a file that has the scheme <code>platform:/</code>.
 * <p>
 * Note: This store does not support folder URI.
 */
public class PlatformURIFileStore extends FileStore {

	public static org.eclipse.emf.common.util.URI toEMFURI(URI uri) {
		Assert.isNotNull(uri);
		return org.eclipse.emf.common.util.URI.createURI(uri.toString(), true);
	}

	protected URI uri;

	public PlatformURIFileStore(URI uri) {
		this.uri = uri;
	}

	protected boolean existsPlatformPluginURI() {
		org.eclipse.emf.common.util.URI eURI = toEMFURI(uri);
		if (eURI.isPlatformPlugin()) {
			String pluginId = eURI.segment(1);
			Bundle bundle = ExtendedPlatform.loadBundle(pluginId);
			if (bundle != null) {
				String pluginBasedPath = eURI.toPlatformString(true);
				String pluginPath = ExtendedResource.URI_SEGMENT_SEPARATOR + pluginId + ExtendedResource.URI_SEGMENT_SEPARATOR;
				String pluginRelativePath = pluginBasedPath.replaceFirst(pluginPath, ""); //$NON-NLS-1$
				return FileLocator.find(bundle, new Path(pluginRelativePath), null) != null;
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.core.filesystem.provider.FileStore#fetchInfo(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		FileInfo info = new FileInfo(getName());
		org.eclipse.emf.common.util.URI eURI = toEMFURI(uri);
		if (eURI.isPlatformPlugin()) {
			info.setExists(existsPlatformPluginURI());
			info.setDirectory(false);
			info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
			info.setAttribute(EFS.ATTRIBUTE_HIDDEN, false);
		} else {
			org.eclipse.emf.common.util.URI convertedURI = EcoreResourceUtil.convertToAbsoluteFileURI(eURI);
			if (convertedURI.isFile()) {
				File file = new File(convertedURI.toFileString());
				info.setExists(file.exists());
				info.setDirectory(file.isDirectory());
				info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, !file.canWrite());
				info.setAttribute(EFS.ATTRIBUTE_HIDDEN, file.isHidden());
			} else {
				info.setExists(false);
				info.setDirectory(false);
				info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
				info.setAttribute(EFS.ATTRIBUTE_HIDDEN, false);
			}
		}
		return info;
	}

	/*
	 * @see org.eclipse.core.filesystem.provider.FileStore#childNames(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		return new String[0];
	}

	/*
	 * @see org.eclipse.core.filesystem.provider.FileStore#getChild(java.lang.String)
	 */
	@Override
	public IFileStore getChild(String name) {
		return null;
	}

	/*
	 * @see org.eclipse.core.filesystem.provider.FileStore#getName()
	 */
	@Override
	public String getName() {
		String path = uri.getPath();
		int lastIndexOf = path.lastIndexOf(ExtendedResource.URI_SEGMENT_SEPARATOR);
		if (lastIndexOf >= 0 && lastIndexOf < path.length() - 1) {
			return path.substring(lastIndexOf + 1, path.length());
		}
		return path;
	}

	/*
	 * @see org.eclipse.core.filesystem.provider.FileStore#getParent()
	 */
	@Override
	public IFileStore getParent() {
		return null;
	}

	/*
	 * @see org.eclipse.core.filesystem.provider.FileStore#openInputStream(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		try {
			return uri.toURL().openStream();
		} catch (IOException ex) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
					PlatformMessages.error_openingStream, ex));
		}
	}

	/*
	 * @see org.eclipse.core.filesystem.provider.FileStore#openOutputStream(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		if (toEMFURI(uri).isPlatformPlugin()) {
			return super.openOutputStream(options, monitor);
		} else {
			URL url;
			try {
				url = new URL(uri.toString());
				final URLConnection urlConnection = url.openConnection();
				urlConnection.setDoOutput(true);
				OutputStream stream = urlConnection.getOutputStream();
				if (stream == null) {
					stream = new FilterOutputStream(stream) {
						@Override
						public void close() throws IOException {
							try {
								super.close();
							} catch (Exception ex) {
							}

						}
					};
				}
				return stream;
			} catch (Exception ex) {
				IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
				throw new CoreException(status);
			}
		}
	}

	/*
	 * @see org.eclipse.core.filesystem.provider.FileStore#toURI()
	 */
	@Override
	public URI toURI() {
		return uri;
	}

}
