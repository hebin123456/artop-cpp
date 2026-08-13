/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [358131] Make Xtend/Xpand/CheckJobs more robust against template file encoding mismatches
 *      
 * </copyright>
 */
package org.eclipse.sphinx.xtendxpand.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.mwe.core.resources.ResourceLoader;
import org.eclipse.internal.xtend.type.baseimpl.TypesComparator;
import org.eclipse.sphinx.emf.mwe.IXtendXpandConstants;
import org.eclipse.sphinx.emf.mwe.resources.BasicWorkspaceResourceLoader;
import org.eclipse.xtend.typesystem.Callable;
import org.eclipse.xtend.typesystem.Feature;
import org.eclipse.xtend.typesystem.ParameterizedCallable;
import org.eclipse.xtend.typesystem.Type;

public final class XtendXpandUtil {

	public static final String XTEND_SHARED_UI_PLUGIN_ID = "org.eclipse.xtend.shared.ui"; //$NON-NLS-1$
	public static final String XTEND_XPAND_NATURE_ID = XTEND_SHARED_UI_PLUGIN_ID + ".xtendXPandNature"; //$NON-NLS-1$

	public static final String PREFERENCE_KEY_PROJECT_SPECIFIC_METAMODEL = "project.specific.metamodel"; //$NON-NLS-1$
	public static final String PREFERENCE_KEY_METAMODEL_CONTRIBUTOR = "metamodelContributor"; //$NON-NLS-1$

	// Prevent from instantiation
	private XtendXpandUtil() {
	}

	public static String getQualifiedName(IFile underlyingFile, String definitionOrFeatureName) {
		Assert.isNotNull(underlyingFile);

		if (underlyingFile.exists()) {
			StringBuilder qualifiedName = new StringBuilder();
			IPath path = underlyingFile.getProjectRelativePath().removeFileExtension();
			for (Iterator<String> iter = Arrays.asList(path.segments()).iterator(); iter.hasNext();) {
				String segment = iter.next();
				qualifiedName.append(segment);
				if (iter.hasNext()) {
					qualifiedName.append(IXtendXpandConstants.NS_DELIMITER);
				}
			}
			if (definitionOrFeatureName != null && definitionOrFeatureName.length() > 0) {
				qualifiedName.append(IXtendXpandConstants.NS_DELIMITER);
				qualifiedName.append(definitionOrFeatureName);
			}
			return qualifiedName.toString();
		}
		return null;
	}

	public static IFile getUnderlyingFile(String qualifiedName, String extension) {
		return getUnderlyingFile(qualifiedName, extension, new BasicWorkspaceResourceLoader());
	}

	public static IFile getUnderlyingFile(String qualifiedName, String extension, ResourceLoader resourceLoader) {
		Assert.isNotNull(resourceLoader);

		if (qualifiedName != null) {
			// Assume that given qualified name represents an Xpand or Xtend template and try resolve it as is
			IPath path = new Path(qualifiedName.replace(IXtendXpandConstants.NS_DELIMITER, Character.toString(IPath.SEPARATOR)))
					.addFileExtension(extension);
			URL resourceURL = resourceLoader.getResource(path.toString());
			if (resourceURL == null && path.segmentCount() > 1) {
				// Assume that given qualified name represents a definition of feature inside an Xpand or Xtend
				// template; so ignore the last segment and try to resolve only the file part of it
				resourceURL = resourceLoader.getResource(path.removeLastSegments(1).addFileExtension(extension).toString());
			}
			if (resourceURL != null) {
				// Avoid CoreException in
				// org.eclipse.core.internal.filesystem.InternalFileSystemCore.getFileSystem(String)
				if (!"bundleresource".equals(resourceURL.getProtocol())) { //$NON-NLS-1$
					try {
						IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
						// Find all files that are mapped to the given URI
						/*
						 * !! Important Note !! The search result includes files located in the workspace as well as
						 * linked files or files contained in a linked folder that were located outside of the
						 * workspace.
						 */
						IFile[] files = workspaceRoot.findFilesForLocationURI(resourceURL.toURI());
						if (files != null && files.length > 0) {
							// Returns the first workspace file that match
							return files[0];
						}
					} catch (Exception ex) {
						// Ignore exception
					}
				}
			}
		}
		return null;
	}

	public static List<Callable> getApplicableFeatures(final List<? extends Callable> features, Class<?> featureType, String featureName,
			List<? extends Type> paramTypes) {
		final List<Callable> applicableFeatures = new ArrayList<Callable>();
		Comparator<List<? extends Type>> typesComparator = new TypesComparator();
		for (Callable feature : features) {
			if (featureType.isInstance(feature) && (featureName == null || feature.getName().equals(featureName))) {
				final List<? extends Type> featureParamTypes = getParamTypes(feature);
				if (featureParamTypes.size() == paramTypes.size() && typesComparator.compare(featureParamTypes, paramTypes) >= 0) {
					applicableFeatures.add(feature);
				}
			}
		}
		return applicableFeatures;
	}

	private static List<? extends Type> getParamTypes(Callable feature) {
		final List<Type> result = new ArrayList<Type>();
		if (feature instanceof Feature) {
			result.add(((Feature) feature).getOwner());
		}
		if (feature instanceof ParameterizedCallable) {
			if (((ParameterizedCallable) feature).getParameterTypes() != null) {
				result.addAll(((ParameterizedCallable) feature).getParameterTypes());
			}
		}
		return result;
	}
}
