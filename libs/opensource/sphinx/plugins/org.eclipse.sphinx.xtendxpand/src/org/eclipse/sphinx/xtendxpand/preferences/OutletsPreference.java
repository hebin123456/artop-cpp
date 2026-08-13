/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.xtendxpand.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.sphinx.platform.preferences.AbstractProjectWorkspacePreference;
import org.eclipse.sphinx.xtendxpand.internal.preferences.OutletsPreferenceInitializer;
import org.eclipse.sphinx.xtendxpand.outlet.ExtendedOutlet;
import org.eclipse.sphinx.xtendxpand.util.XtendXpandUtil;

// Pattern name@location?protectedRegion=value;overwrite=value;...
public class OutletsPreference extends AbstractProjectWorkspacePreference<Collection<ExtendedOutlet>> implements IAdaptable {

	/**
	 * Default instance of {@link OutletsPreference}.
	 */
	public static final OutletsPreference INSTANCE = new OutletsPreference(XtendXpandUtil.XTEND_XPAND_NATURE_ID,
			OutletsPreferenceInitializer.QUALIFIER, OutletsPreferenceInitializer.PREF_OUTLETS, OutletsPreferenceInitializer.PREF_OUTLETS_DEFAULT);

	public OutletsPreference(String requiredProjectNatureId, String qualifier, String key, String defaultValueAsString) {
		super(requiredProjectNatureId, qualifier, key, defaultValueAsString);
	}

	@Override
	protected Collection<ExtendedOutlet> toObject(IProject project, String valueAsString) {
		if (valueAsString != null) {
			List<ExtendedOutlet> outlets = new ArrayList<ExtendedOutlet>();
			String[] values = valueAsString.split(File.pathSeparator);
			for (String value : values) {
				String nameAndLocaltion;
				String allAttributes;
				int i = value.indexOf("?"); //$NON-NLS-1$
				nameAndLocaltion = i == -1 ? value : value.substring(0, i);
				allAttributes = i == -1 ? "" : value.substring(i + 1); //$NON-NLS-1$
				String[] args = nameAndLocaltion.split("@"); //$NON-NLS-1$
				String name = args[0];
				String expression = args[1];
				ExtendedOutlet outlet = new ExtendedOutlet(expression, project);
				if (name.length() > 0) {
					outlet.setName(name);
				}
				if (allAttributes.length() > 0) {
					String[] attributes = allAttributes.split(";"); //$NON-NLS-1$
					for (String attribute : attributes) {
						String[] attrNameAndValue = attribute.split("="); //$NON-NLS-1$
						String attrName = attrNameAndValue[0];
						String attrValue = attrNameAndValue[1];
						if ("protectedRegion".equals(attrName)) { //$NON-NLS-1$
							outlet.setProtectedRegion(Boolean.parseBoolean(attrValue));
						}
					}
				}

				outlets.add(outlet);
			}
			return Collections.unmodifiableCollection(outlets);
		}
		return null;
	}

	@Override
	protected String toString(IProject project, java.util.Collection<ExtendedOutlet> valueAsObject) {
		if (valueAsObject != null) {
			StringBuilder builder = new StringBuilder();
			Iterator<ExtendedOutlet> iter = valueAsObject.iterator();
			while (iter.hasNext()) {
				ExtendedOutlet outlet = iter.next();
				builder.append(outlet.getName() != null ? outlet.getName() : ""); //$NON-NLS-1$
				builder.append("@"); //$NON-NLS-1$
				builder.append(outlet.getPathExpression());
				if (outlet.isProtectedRegion()) {
					builder.append("?"); //$NON-NLS-1$
					builder.append("protectedRegion=true"); //$NON-NLS-1$
				}
				if (iter.hasNext()) {
					builder.append(File.pathSeparator);
				}
			}
			return builder.toString();
		}
		return null;
	}

	public ExtendedOutlet getDefaultOutlet(IProject project) {
		for (ExtendedOutlet outlet : get(project)) {
			if (outlet.getName() == null) {
				return outlet;
			}
		}
		return null;
	}

	public Collection<ExtendedOutlet> getNamedOutlets(IProject project) {
		return removeDefaultOutlet(get(project));
	}

	private Collection<ExtendedOutlet> removeDefaultOutlet(Collection<ExtendedOutlet> allOutlets) {
		List<ExtendedOutlet> result = new ArrayList<ExtendedOutlet>(allOutlets);
		for (ExtendedOutlet outlet : allOutlets) {
			if (outlet.getName() == null) {
				result.remove(outlet);
				break;
			}
		}
		return result;
	}

	/**
	 * Returns an object which is an instance of the given class associated with this object. Returns <code>null</code>
	 * if no such object can be found.
	 * <p>
	 * This implementation of the method declared by <code>IAdaptable</code> passes the request along to the platform's
	 * adapter manager; roughly <code>Platform.getAdapterManager().getAdapter(this, adapter)</code>. Subclasses may
	 * override this method (however, if they do so, they should invoke the method on their superclass to ensure that
	 * the Platform's adapter manager is consulted).
	 * </p>
	 * 
	 * @param adapter
	 *            the class to adapt to
	 * @return the adapted object or <code>null</code>
	 * @see IAdaptable#getAdapter(Class)
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}
}
