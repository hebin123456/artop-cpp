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
package org.eclipse.sphinx.testutils.integration.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.sphinx.platform.util.StatusUtil;
import org.eclipse.sphinx.testutils.TestFileAccessor;

public class ReferenceWorkspaceExtractor {

	private File extractedWorkspaceRootDirectory = null;

	public File getExtractedWorkspaceRootDirectory() {
		return extractedWorkspaceRootDirectory;
	}

	/**
	 * Extracts specified input zip archive to given target directory
	 * 
	 * @throws CoreException
	 */
	public void extract(final TestFileAccessor inputFileAccessor, final String referenceWorkspaceArchiveFileName, final File targetDirectory)
			throws CoreException {
		Assert.isNotNull(inputFileAccessor);

		try {
			URI archiveURI = inputFileAccessor.getInputFileURI(referenceWorkspaceArchiveFileName, true);
			ZipFile zipFile = new ZipFile(archiveURI.getPath());
			extractedWorkspaceRootDirectory = extract(zipFile, targetDirectory);
		} catch (Exception ex) {
			throw new CoreException(StatusUtil.createErrorStatus(Activator.getPlugin(), ex));
		}
	}

	private File extract(ZipFile zipFile, File targetDirectory) throws IOException {
		Assert.isNotNull(zipFile);
		Assert.isNotNull(targetDirectory);

		File workspaceRootDirectory = null;
		Enumeration<?> en = zipFile.entries();
		while (en.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) en.nextElement();
			if (entry.isDirectory()) {
				continue;
			}

			// Create directory if needed
			File targetResource = new File(targetDirectory, entry.toString());
			File targetResourceDirectory = targetResource.getParentFile();
			if (!targetResourceDirectory.exists()) {
				targetResourceDirectory.mkdirs();
			}

			// Get the reference workspace root directory path
			if (workspaceRootDirectory == null && IProjectDescription.DESCRIPTION_FILE_NAME.equals(targetResource.getName())) {
				// Remove 2 last segments of .project file to get reference workspace source root directory's path
				workspaceRootDirectory = targetResourceDirectory.getParentFile();
			}

			InputStream in = null;
			try {
				in = zipFile.getInputStream(entry);
				saveInputStreamToFile(in, targetResource);
			} finally {
				if (in != null) {
					in.close();
				}
			}
		}
		return workspaceRootDirectory;
	}

	private void saveInputStreamToFile(InputStream in, File targetFile) throws IOException {
		FileOutputStream out = new FileOutputStream(targetFile);
		try {
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			out.close();
		}
	}
}
