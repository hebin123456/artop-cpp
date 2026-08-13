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
package org.eclipse.sphinx.xtendxpand.ui.outlet.providers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.sphinx.xtendxpand.outlet.ExtendedOutlet;
import org.eclipse.sphinx.xtendxpand.preferences.OutletsPreference;
import org.eclipse.xpand2.output.Outlet;

public class OutletProvider implements IPreferenceChangeListener {

	private IProject project;
	private OutletsPreference outletsPreference;
	private Set<ExtendedOutlet> allOutlets;
	private Set<ExtendedOutlet> unappliedOutlets;

	public OutletProvider(OutletsPreference outletPreference) {
		this(null, outletPreference);
	}

	public OutletProvider(IProject project, OutletsPreference outletsPreference) {
		Assert.isNotNull(outletsPreference);

		this.project = project;
		this.outletsPreference = outletsPreference;

		allOutlets = new HashSet<ExtendedOutlet>();
		unappliedOutlets = new HashSet<ExtendedOutlet>();
		allOutlets.addAll(outletsPreference.get(project));

		if (project != null) {
			outletsPreference.addPreferenceChangeListenerToProject(project, this);
		}
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public Set<ExtendedOutlet> getOutlets() {
		return Collections.unmodifiableSet(allOutlets);
	}

	public ExtendedOutlet getDefaultOutlet() {
		for (ExtendedOutlet outlet : getOutlets()) {
			if (outlet.getName() == null) {
				return outlet;
			}
		}
		return null;
	}

	public Set<ExtendedOutlet> getNamedOutlets() {
		Set<ExtendedOutlet> result = getOutlets();
		Outlet defaultOutlet = getDefaultOutlet();
		if (defaultOutlet != null) {
			result.remove(defaultOutlet);
		}
		return Collections.unmodifiableSet(result);
	}

	public void addOutlet(ExtendedOutlet outlet) {
		unappliedOutlets.add(outlet);
		allOutlets.add(outlet);
	}

	public void removeOutlet(ExtendedOutlet outlet) {
		unappliedOutlets.remove(outlet);
		allOutlets.remove(outlet);
	}

	public void setToDefault() {
		unappliedOutlets.clear();
		allOutlets.clear();
		allOutlets.addAll(outletsPreference.getDefaultValueAsObject());
	}

	public synchronized void store() {
		// Unapplied outlets must be cleared before setting the preference into the project or the workspace, otherwise
		// newly added outlets will be duplicated.
		unappliedOutlets.clear();
		if (project != null) {
			outletsPreference.setInProject(project, getOutlets());
		} else {
			outletsPreference.setInWorkspace(getOutlets());
		}
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (outletsPreference.getKey().equals(event.getKey())) {
			updateOutlets();
		}
	}

	protected void updateOutlets() {
		allOutlets.clear();
		allOutlets.addAll(outletsPreference.get(project));
		allOutlets.addAll(unappliedOutlets);
	}

	public void dispose() {
		if (project != null) {
			outletsPreference.removePreferenceChangeListenerFromProject(project, this);
		}
		allOutlets.clear();
		unappliedOutlets.clear();
	}
}
