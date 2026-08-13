/**
 * <copyright>
 *
 * Copyright (c) 2014-2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [458862] Navigation from problem markers in Check Validation view to model editors and Model Explorer view broken
 *     itemis - [480122] Unable to navigate from Check Validation Problem markers to corresponding model element in Model Explorer
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class CheckValidationView extends MarkerSupportView implements ITabbedPropertySheetPageContributor {

	public static final String ID = "org.eclipse.sphinx.emf.check.ui.markersView"; //$NON-NLS-1$

	protected Set<IPropertySheetPage> propertySheetPages = new HashSet<IPropertySheetPage>();

	public CheckValidationView() {
		super(IValidationUIConstants.VALIDATION_CHECK_MARKER_GENERATOR);
	}

	/*
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter.equals(IShowInSource.class)) {
			return new IShowInSource() {
				@SuppressWarnings("restriction")
				@Override
				public ShowInContext getShowInContext() {
					IMarker[] markers = getSelectedMarkers();
					return new ShowInContext(new StructuredSelection(retrieveModelObjects(markers)),
							new StructuredSelection(retrieveResources(markers)));
				}
			};
		}
		if (IPropertySheetPage.class == adapter) {
			return getPropertySheetPage();
		}
		return super.getAdapter(adapter);
	}

	/**
	 * This creates a new property sheet page instance and manages it in the cache.
	 */
	protected IPropertySheetPage getPropertySheetPage() {
		IPropertySheetPage propertySheetPage = new TabbedPropertySheetPage(this);
		propertySheetPages.add(propertySheetPage);
		return propertySheetPage;
	}

	private IResource[] retrieveResources(IMarker[] markers) {
		Set<IResource> resources = new HashSet<IResource>();
		for (IMarker marker : markers) {
			resources.add(marker.getResource());
		}
		return resources.toArray(new IResource[resources.size()]);
	}

	private Object[] retrieveModelObjects(IMarker[] markers) {
		// Retrieve model objects behind problem markers
		Set<Object> objects = new HashSet<Object>();
		for (IMarker marker : markers) {
			try {
				if (marker.isSubtypeOf(EValidator.MARKER)) {
					IResource resource = marker.getResource();
					TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(resource);
					if (editingDomain != null) {
						String uriAttribute = marker.getAttribute(EValidator.URI_ATTRIBUTE, null);
						if (uriAttribute != null) {
							URI uri = URI.createURI(uriAttribute, true);
							URI resolvedURI = EcorePlatformUtil.resolveURI(uri, resource.getFullPath());
							EObject object = EcorePlatformUtil.getEObject(editingDomain, resolvedURI);
							if (object != null) {
								objects.add(object);
							}
						}
					}
				}
			} catch (Exception ex) {
				// Ignore exception, just continue with next marker
			}
		}
		return objects.toArray();
	}

	@Override
	public String getContributorId() {
		return getSite().getId();
	}
}
