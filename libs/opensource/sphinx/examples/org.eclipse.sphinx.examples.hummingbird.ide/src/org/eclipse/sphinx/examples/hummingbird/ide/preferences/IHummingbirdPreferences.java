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
package org.eclipse.sphinx.examples.hummingbird.ide.preferences;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.examples.hummingbird.ide.internal.preferences.HummingbirdPreferenceInitializer;
import org.eclipse.sphinx.examples.hummingbird.ide.metamodel.HummingbirdMMDescriptor;
import org.eclipse.sphinx.examples.hummingbird.ide.natures.HummingbirdNature;
import org.eclipse.sphinx.platform.preferences.AbstractProjectPreference;
import org.eclipse.sphinx.platform.preferences.AbstractProjectWorkspacePreference;
import org.eclipse.sphinx.platform.preferences.IProjectPreference;
import org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference;

/**
 * A set of preferences through which users can adjust the way how they work with Hummingbird models.
 */
public interface IHummingbirdPreferences {

	/**
	 * The {@link HummingbirdMMDescriptor Hummingbird metamodel version} to be used. Can be defined globally at
	 * workspace level and may be individually redefined, i.e., overridden, by each project.
	 */
	IProjectWorkspacePreference<HummingbirdMMDescriptor> METAMODEL_VERSION = new AbstractProjectWorkspacePreference<HummingbirdMMDescriptor>(
			HummingbirdNature.ID, HummingbirdPreferenceInitializer.QUALIFIER, HummingbirdPreferenceInitializer.PREF_METAMODEL_VERSION,
			HummingbirdPreferenceInitializer.PREF_METAMODEL_VERSION_DEFAULT) {

		/*
		 * @see org.eclipse.sphinx.platform.preferences.AbstractProjectWorkspacePreference#toObject(java.lang.String)
		 */
		@Override
		protected HummingbirdMMDescriptor toObject(String valueAsString) {
			IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(valueAsString);
			return mmDescriptor instanceof HummingbirdMMDescriptor ? (HummingbirdMMDescriptor) mmDescriptor : null;
		}

		/*
		 * @see org.eclipse.sphinx.platform.preferences.AbstractProjectWorkspacePreference#toString(java.lang.Object)
		 */
		@Override
		protected String toString(HummingbirdMMDescriptor valueAsObject) {
			return valueAsObject != null ? valueAsObject.getIdentifier() : null;
		}
	};

	/**
	 * The {@link HummingbirdMMDescriptor Hummingbird resource version} to be used for serializing the resources of a
	 * Hummingbird model. The {@link HummingbirdMMDescriptor Hummingbird resource version} may be the same or older than
	 * the version of the metamodel implementation used for manipulating the Hummingbird model in question. Can be
	 * defined individually by each project.
	 */
	IProjectPreference<HummingbirdMMDescriptor> RESOURCE_VERSION = new AbstractProjectPreference<HummingbirdMMDescriptor>(HummingbirdNature.ID,
			HummingbirdPreferenceInitializer.QUALIFIER, HummingbirdPreferenceInitializer.PREF_RESOURCE_VERSION,
			HummingbirdPreferenceInitializer.PREF_RESOURCE_VERSION_DEFAULT) {

		/*
		 * @see
		 * org.eclipse.sphinx.platform.preferences.AbstractProjectPreference#toObject(org.eclipse.core.resources.IProject
		 * , java.lang.String)
		 */
		@Override
		protected HummingbirdMMDescriptor toObject(IProject project, String valueAsString) {
			if (HummingbirdPreferenceInitializer.PREF_RESOURCE_VERSION_DEFAULT.equals(valueAsString)) {
				return null;
			}

			HummingbirdMMDescriptor mmVersion = METAMODEL_VERSION.get(project);
			if (mmVersion.getIdentifier().equals(valueAsString)) {
				return mmVersion;
			}
			Collection<IMetaModelDescriptor> resourceVersions = mmVersion.getCompatibleResourceVersionDescriptors();
			for (IMetaModelDescriptor resourceVersion : resourceVersions) {
				if (resourceVersion instanceof HummingbirdMMDescriptor && resourceVersion.getIdentifier().equals(valueAsString)) {
					return (HummingbirdMMDescriptor) resourceVersion;
				}
			}

			throw new RuntimeException("Resource version " //$NON-NLS-1$
					+ valueAsString + " is not compatible with Hummingbird metamodel version " + mmVersion.getName() //$NON-NLS-1$
					+ " set in project " //$NON-NLS-1$
					+ project.getName() + "."); //$NON-NLS-1$
		}

		/*
		 * @see
		 * org.eclipse.sphinx.platform.preferences.AbstractProjectPreference#toString(org.eclipse.core.resources.IProject
		 * , java.lang.Object)
		 */
		@Override
		protected String toString(IProject project, HummingbirdMMDescriptor valueAsObject) {
			if (valueAsObject == null) {
				return HummingbirdPreferenceInitializer.PREF_RESOURCE_VERSION_DEFAULT;
			}

			HummingbirdMMDescriptor mmVersion = METAMODEL_VERSION.get(project);
			Collection<IMetaModelDescriptor> resourceVersions = mmVersion.getCompatibleResourceVersionDescriptors();
			if (valueAsObject != mmVersion && !resourceVersions.contains(valueAsObject)) {
				throw new RuntimeException("Resource version " //$NON-NLS-1$
						+ valueAsObject.getIdentifier() + " is not compatible with Hummingbird metamodel version " + mmVersion.getName() //$NON-NLS-1$
						+ " set in project " //$NON-NLS-1$
						+ project.getName() + "."); //$NON-NLS-1$
			}

			return valueAsObject.getIdentifier();
		}
	};
}
