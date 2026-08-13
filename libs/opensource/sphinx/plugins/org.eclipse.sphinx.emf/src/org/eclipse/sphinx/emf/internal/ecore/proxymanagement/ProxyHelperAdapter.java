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
package org.eclipse.sphinx.emf.internal.ecore.proxymanagement;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sphinx.emf.internal.ecore.proxymanagement.blacklist.ModelIndex;
import org.eclipse.sphinx.emf.internal.ecore.proxymanagement.lookupresolver.EcoreIndex;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;

/**
 * {@link Adapter}-based implementation of {@link ProxyHelper}. May be used with {@link ResourceSet}s.
 * 
 * @deprecated Will be removed as soon as a full-fledged model indexing service is in place and can be used to overcome
 *             performance bottlenecks due to proxy resolution.
 */
@Deprecated
public class ProxyHelperAdapter extends AdapterImpl implements ProxyHelper {

	private boolean fIgnoreFragmentBasedProxies = false;
	private IgnoreFragmentBasedProxiesFlagResetter fIgnoreFragmentBasedProxiesFlagResetter = new IgnoreFragmentBasedProxiesFlagResetter();

	private EcoreIndex fLookupResolver = new EcoreIndex();
	private ModelIndex fBlackList = new ModelIndex();

	@Override
	public boolean isIgnoreFragmentBasedProxies() {
		return fIgnoreFragmentBasedProxies;
	}

	@Override
	public void setIgnoreFragmentBasedProxies(boolean ignore) {
		fIgnoreFragmentBasedProxies = ignore;

		if (ignore) {
			Job.getJobManager().addJobChangeListener(fIgnoreFragmentBasedProxiesFlagResetter);
		} else {
			Job.getJobManager().removeJobChangeListener(fIgnoreFragmentBasedProxiesFlagResetter);
		}
	}

	@Override
	public EcoreIndex getLookupResolver() {
		return fLookupResolver;
	}

	@Override
	public ModelIndex getBlackList() {
		return fBlackList;
	}

	public void dispose() {
		fBlackList.dispose();
	}

	/*
	 * @see org.eclipse.emf.common.notify.impl.AdapterImpl#isAdapterForType(java.lang.Object)
	 */
	@Override
	public boolean isAdapterForType(Object type) {
		return type == ProxyHelper.class;
	}

	/*
	 * @see org.eclipse.emf.common.notify.impl.AdapterImpl#setTarget(org.eclipse.emf.common.notify.Notifier)
	 */
	@Override
	public void setTarget(Notifier newTarget) {
		Assert.isLegal(newTarget == null || newTarget instanceof ResourceSet);
		super.setTarget(newTarget);
	}

	private class IgnoreFragmentBasedProxiesFlagResetter extends JobChangeAdapter {

		private boolean isModelLoadingJob(IJobChangeEvent event) {
			if (event != null) {
				Job job = event.getJob();
				if (job != null) {
					return job.belongsTo(IExtendedPlatformConstants.FAMILY_MODEL_LOADING);
				}
			}
			return false;
		}

		@Override
		public void done(IJobChangeEvent event) {
			if (isModelLoadingJob(event)) {
				// Make sure that resolution of fragment-based proxies gets re-enabled in case that this is the last
				// running model loading job
				if (Job.getJobManager().find(IExtendedPlatformConstants.FAMILY_MODEL_LOADING).length <= 1) {
					setIgnoreFragmentBasedProxies(false);
				}
			}
		}
	}
}
