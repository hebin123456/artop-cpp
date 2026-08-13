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
 *     Siemens - [574930] Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.loading;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.model.IModelDescriptor;

/**
 * Provides API for loading, unloading, and reloading projects, models, or files. Only one instance should be present in
 * a runtime, which is created by the {@link ModelLoadManager}.
 */
public interface IModelLoadManager {

	/**
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadWorkspace(boolean async, IProgressMonitor monitor);

	/**
	 * @param mmDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadAllProjects(IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor);

	/**
	 * @param project
	 * @param includeReferencedProjects
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadProject(IProject project, boolean includeReferencedProjects, boolean async, IProgressMonitor monitor);

	/**
	 * @param project
	 * @param includeReferencedProjects
	 * @param mmDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadProject(IProject project, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor);

	/**
	 * @param projects
	 * @param includeReferencedProjects
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadProjects(Collection<IProject> projects, boolean includeReferencedProjects, boolean async, IProgressMonitor monitor);

	/**
	 * @param projects
	 * @param includeReferencedProjects
	 * @param mmDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadProjects(Collection<IProject> projects, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor, boolean async,
			IProgressMonitor monitor);

	/**
	 * @param file
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadFile(IFile file, boolean async, IProgressMonitor monitor);

	/**
	 * @param file
	 * @param mmDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadFile(IFile file, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor);

	/**
	 * @param files
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadFiles(Collection<IFile> files, boolean async, IProgressMonitor monitor);

	/**
	 * @param files
	 * @param mmDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadFiles(Collection<IFile> files, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor);

	/**
	 * @param uri
	 *            a model URI
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadURI(URI uri, boolean async, IProgressMonitor monitor);

	/**
	 * @param uri
	 *            a model URI
	 * @param mmDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadURI(URI uri, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor);

	/**
	 * @param uris
	 *            a set of model URIs
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadURIs(Collection<URI> uris, boolean async, IProgressMonitor monitor);

	/**
	 * @param uris
	 *            a set of model URIs
	 * @param mmDescriptor
	 *            the metamodel descriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadURIs(Collection<URI> uris, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor);

	/**
	 * @param project
	 * @param includeReferencedProjects
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadProject(IProject project, boolean includeReferencedProjects, boolean async, IProgressMonitor monitor);

	/**
	 * @param project
	 * @param includeReferencedProjects
	 * @param mmDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadProject(IProject project, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor, boolean async,
			IProgressMonitor monitor);

	/**
	 * @param projects
	 * @param includeReferencedProjects
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadProjects(Collection<IProject> projects, boolean includeReferencedProjects, boolean async, IProgressMonitor monitor);

	void unloadAllProjects(IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor);

	/**
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadWorkspace(boolean async, IProgressMonitor monitor);

	/**
	 * @param projects
	 * @param includeReferencedProjects
	 * @param mmDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadProjects(Collection<IProject> projects, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor, boolean async,
			IProgressMonitor monitor);

	/**
	 * @param file
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadFile(IFile file, boolean async, IProgressMonitor monitor);

	/**
	 * @param file
	 * @param mmDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadFile(IFile file, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor);

	/**
	 * @param files
	 * @param memoryOptimized
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadFiles(Collection<IFile> files, boolean memoryOptimized, boolean async, IProgressMonitor monitor);

	/**
	 * @param files
	 * @param mmDescriptor
	 * @param memoryOptimized
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadFiles(Collection<IFile> files, IMetaModelDescriptor mmDescriptor, boolean memoryOptimized, boolean async, IProgressMonitor monitor);

	/**
	 * @param uri
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadURI(URI uri, boolean async, IProgressMonitor monitor);

	/**
	 * @param uri
	 * @param mmDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadURI(URI uri, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor);

	/**
	 * @param uris
	 * @param memoryOptimized
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadURIs(Collection<URI> uris, boolean memoryOptimized, boolean async, IProgressMonitor monitor);

	/**
	 * @param uris
	 * @param mmDescriptor
	 * @param memoryOptimized
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadURIs(Collection<URI> uris, IMetaModelDescriptor mmDescriptor, boolean memoryOptimized, boolean async, IProgressMonitor monitor);

	/**
	 * @param project
	 * @param includeReferencedProjects
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void reloadProject(IProject project, boolean includeReferencedProjects, boolean async, IProgressMonitor monitor);

	/**
	 * @param project
	 * @param includeReferencedProjects
	 * @param mmDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void reloadProject(IProject project, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor, boolean async,
			IProgressMonitor monitor);

	/**
	 * @param projects
	 * @param includeReferencedProjects
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void reloadProjects(Collection<IProject> projects, boolean includeReferencedProjects, boolean async, IProgressMonitor monitor);

	/**
	 * @param projects
	 * @param includeReferencedProjects
	 * @param mmDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void reloadProjects(Collection<IProject> projects, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor, boolean async,
			IProgressMonitor monitor);

	/**
	 * @param file
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void reloadFile(IFile file, boolean async, IProgressMonitor monitor);

	/**
	 * @param file
	 * @param mmDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void reloadFile(IFile file, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor);

	/**
	 * @param files
	 * @param memoryOptimized
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void reloadFiles(Collection<IFile> files, boolean memoryOptimized, boolean async, IProgressMonitor monitor);

	/**
	 * @param files
	 * @param mmDescriptor
	 * @param memoryOptimized
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void reloadFiles(Collection<IFile> files, IMetaModelDescriptor mmDescriptor, boolean memoryOptimized, boolean async, IProgressMonitor monitor);

	/**
	 * @param uri
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void reloadURI(URI uri, boolean async, IProgressMonitor monitor);

	/**
	 * @param uri
	 * @param mmDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void reloadURI(URI uri, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor);

	/**
	 * @param uris
	 * @param memoryOptimized
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void reloadURIs(Collection<URI> uris, boolean memoryOptimized, boolean async, IProgressMonitor monitor);

	/**
	 * @param uris
	 * @param mmDescriptor
	 * @param memoryOptimized
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void reloadURIs(Collection<URI> uris, IMetaModelDescriptor mmDescriptor, boolean memoryOptimized, boolean async, IProgressMonitor monitor);

	/**
	 * Unresolves (i.e. proxifies) all references of all models inside given set of {@link IProject projects} pointing
	 * to elements of models in {@link IProject project}s which are outside the scope of the underlying
	 * {@link IModelDescriptor model descriptor}s. This method typically needs to be called when references between
	 * {@link IProject project}s are removed in order to make sure that models in the formerly referencing
	 * {@link IProject project}s do no longer reference any elements of models in the formerly referenced
	 * {@link IProject project}s.
	 *
	 * @param projects
	 *            The project of which the unreachable cross project references are to be unresolved.
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 * @see IModelDescriptor#getReferencedRoots()
	 */
	void unresolveUnreachableCrossProjectReferences(Collection<IProject> projects, boolean async, IProgressMonitor monitor);

	/**
	 * Loads all resources owned by the provided {@link IModelDescriptor model descriptor} (i.e all the persisted
	 * resources owned by the model)
	 *
	 * @param modelDescriptor
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadModel(IModelDescriptor modelDescriptor, boolean async, IProgressMonitor monitor);

	/**
	 * Loads all resources owned by the provided {@link IModelDescriptor model descriptor} (i.e all the persisted
	 * resources owned by the model)
	 *
	 * @param modelDescriptor
	 *            the {@link IModelDescriptor model descriptor} describing the model.
	 * @param includeReferencedScopes
	 *            determine weither or not the referenced scopes must be taken into account while retrieving the
	 *            persisted files to load.
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadModel(IModelDescriptor modelDescriptor, boolean includeReferencedScopes, boolean async, IProgressMonitor monitor);

	/**
	 * Loads in memory all persisted resources owned by the model described by the {@link IModelDescriptor model
	 * Descriptor}s provided in argument.
	 *
	 * @param modelDescriptors
	 *            {@link IModelDescriptor model Descriptor}s describing the models to load.
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadModels(Collection<IModelDescriptor> modelDescriptors, boolean async, IProgressMonitor monitor);

	/**
	 * Loads in memory all persisted resources owned by the model described by the {@link IModelDescriptor model
	 * Descriptor}s provided in argument.
	 *
	 * @param modelDescriptors
	 *            {@link IModelDescriptor model Descriptor}s describing the models to load.
	 * @param includeReferencedScopes
	 *            Boolean that determine if the {@link IModelDescriptor model Descriptor}s referenced by the
	 *            {@link IModelDescriptor model Descriptor}s provided in argument must be loaded too.
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadModels(Collection<IModelDescriptor> modelDescriptors, boolean includeReferencedScopes, boolean async, IProgressMonitor monitor);

	/**
	 * Unloads all resources owned by the {@link IModelDescriptor model Descriptor} provided in argument.
	 *
	 * @param modelDescriptor
	 *            The {@link IModelDescriptor model Descriptor} for the one resources must be unloaded
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadModel(IModelDescriptor modelDescriptor, boolean async, IProgressMonitor monitor);

	/**
	 * Unloads all resources owned by the {@link IModelDescriptor model Descriptor} provided in argument.
	 *
	 * @param modelDescriptor
	 *            The {@link IModelDescriptor model Descriptor} for the one resources must be unloaded.
	 * @param includeReferencedScopes
	 *            Boolean that determine if the {@link IModelDescriptor model Descriptor}s referenced by the
	 *            {@link IModelDescriptor model Descriptor} provided in argument must be unloaded too.
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadModel(IModelDescriptor modelDescriptor, boolean includeReferencedScopes, boolean async, IProgressMonitor monitor);

	/**
	 * Unload all resources owned by the {@link IModelDescriptor model Descriptor}s provided in argument.
	 *
	 * @param modelDescriptors
	 *            The {@link IModelDescriptor model Descriptor}s owning resources that must be unloaded.
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadModels(Collection<IModelDescriptor> modelDescriptors, boolean async, IProgressMonitor monitor);

	/**
	 * Unload all resources owned by the {@link IModelDescriptor model Descriptor}s provided in argument.
	 *
	 * @param modelDescriptors
	 *            The {@link IModelDescriptor model Descriptor}s owning resources that must be unloaded.
	 * @param includeReferencedScopes
	 *            Boolean that determine if the {@link IModelDescriptor model Descriptor}s referenced by the
	 *            {@link IModelDescriptor model Descriptor}s provided in argument must be unloaded too.
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void unloadModels(Collection<IModelDescriptor> modelDescriptors, boolean includeReferencedScopes, boolean async, IProgressMonitor monitor);

	/**
	 * Updates {@link URI}s of {@link Resource resource}s behind the old {@link IFile file}s (keys) in the given map
	 * according to the new {@link IPath path}s (values) they are mapped to. May be run synchronously or asynchronously.
	 *
	 * @param filesToUpdate
	 *            A map specifying relevant old {@link IFile file}s along with their respective new {@link IPath path}s.
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void updateResourceURIs(Map<IFile, IPath> filesToUpdate, boolean async, IProgressMonitor monitor);

}