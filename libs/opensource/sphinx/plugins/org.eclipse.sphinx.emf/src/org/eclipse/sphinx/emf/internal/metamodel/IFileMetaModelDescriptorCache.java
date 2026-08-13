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
package org.eclipse.sphinx.emf.internal.metamodel;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;

public interface IFileMetaModelDescriptorCache {

	/**
	 * Adds cached {@link IMetaModelDescriptor meta-model descriptor} to the {@link MetaModelDescriptorRegistry} for the
	 * given {@link IFile file}.
	 * 
	 * @param file
	 *            The {@link IFile file} for the whom {@link IMetaModelDescriptor meta-model descriptor} need to be
	 *            registered.
	 * @param mmDescriptor
	 *            The {@link IMetaModelDescriptor meta-model descriptor} to register for the given {@link IFile file}.
	 * @see #getDescriptor(IFile)
	 */
	void addDescriptor(IFile file, IMetaModelDescriptor mmDescriptor);

	/**
	 * Moves cached {@link IMetaModelDescriptor meta-model descriptor} from specified {@link IFile old file}, if any, to
	 * specified {@link IFile new file}. The previous {@link IMetaModelDescriptor meta-model descriptor} of specified
	 * {@link IFile old file} is put on a separate cache and can still be queried by calling
	 * {@link #getOldDescriptor(IFile)}.
	 * <p>
	 * Clients should call this method in the following two situations:
	 * <ul>
	 * <li>When some {@link IFile file} has been moved from {@link IFile oldFile} to {@link IFile newFile}. The cached
	 * {@link IMetaModelDescriptor meta-model descriptor} must then be updated in order to be accurate.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param oldFile
	 *            The {@link IFile old file} whose {@link IMetaModelDescriptor meta-model descriptor} is to be moved.
	 * @param newFile
	 *            The {@link IFile new file} to which the {@link IFile old file}'s {@link IMetaModelDescriptor
	 *            meta-model descriptor} is to be moved.
	 * @see #getDescriptor(IFile)
	 * @see #getOldDescriptor(IFile)
	 */
	void moveDescriptor(IFile oldFile, IFile newFile);

	/**
	 * Removes cached {@link IMetaModelDescriptor meta-model descriptor} for given {@link IFile file}. This has the
	 * effect that upon the next call to {@link #getDescriptor(IFile)} for the same {@link IFile file} the meta-model
	 * descriptor will be re-detected rather just taken from the cache. The previous {@link IMetaModelDescriptor
	 * meta-model descriptor} is put on a separate cache and can still be queried by calling
	 * {@link #getOldDescriptor(IFile)}.
	 * <p>
	 * Clients should call this method in the following two situations:
	 * <ul>
	 * <li>When the {@link IFile file} has been deleted. The cached {@link IMetaModelDescriptor meta-model descriptor}
	 * must then be removed in order to avoid obsolete cache entries and unnecessary memory consumption.</li>
	 * <li>When there is a reason to believe that the {@link IFile file} 's {@link IMetaModelDescriptor meta-model
	 * descriptor} has changed (e.g. because the {@link IFile file} 's content has changed). The cached
	 * {@link IMetaModelDescriptor meta-model descriptor} must then be removed because it is potentially invalid.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param file
	 *            The {@link IFile file} whose cached {@link IMetaModelDescriptor meta-model descriptor} is to be
	 *            discarded.
	 * @see #getDescriptor(IFile)
	 * @see #getOldDescriptor(IFile)
	 */
	void removeDescriptor(IFile file);

	/**
	 * Clears cached old {@link IMetaModelDescriptor meta-model descriptor}s.
	 * <p>
	 * Clients should call this method in the following two situations:
	 * <ul>
	 * <li>When one or multiple {@link IFile file}s have been deleted and hadn't been loaded in any model before. The
	 * cached old {@link IMetaModelDescriptor meta-model descriptor} must then be removed in order to avoid obsolete
	 * cache entries and unnecessary memory consumption.</li>
	 * <li>When one or multiple {@link Resource resource}s have been unloaded from a model and don't exist anymore. The
	 * cached old {@link IMetaModelDescriptor meta-model descriptor} must then be removed in order to avoid obsolete
	 * cache entries and unnecessary memory consumption.</li>
	 * </ul>
	 * </p>
	 * 
	 * @see #getDescriptor(IFile)
	 * @see #getOldDescriptor(IFile)
	 */
	void clearOldDescriptors();
}