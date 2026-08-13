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
package org.eclipse.sphinx.tests.emf.integration.resource;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.resource.AbstractModelConverter;
import org.eclipse.sphinx.examples.hummingbird.ide.preferences.IHummingbirdPreferences;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMCompatibility;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.tests.emf.integration.internal.Activator;
import org.jdom.Element;

@SuppressWarnings("nls")
public class MockModelConverter extends AbstractModelConverter {
	public static final String IM_COMPONENT = "components";
	public static final String CONVERTED_IN_LOADING = "convertedInLoading";
	public static final String CONVERTED_IN_SAVING = "convertedInSaving";

	@Override
	public IMetaModelDescriptor getResourceVersionFromPreferences(IProject project) {
		return IHummingbirdPreferences.RESOURCE_VERSION.get(project);
	}

	@Override
	public IMetaModelDescriptor getResourceVersionDescriptor() {
		return Hummingbird20MMCompatibility.HUMMINGBIRD_2_0_0_RESOURCE_DESCRIPTOR;
	}

	@Override
	public IMetaModelDescriptor getMetaModelVersionDescriptor() {

		return Hummingbird20MMDescriptor.INSTANCE;
	}

	@Override
	protected void convertLoadElement(Element element, Map<?, ?> options) {
		try {
			if (IM_COMPONENT.equals(element.getName())) {
				String oldname = element.getAttributeValue("name");
				element.setAttribute("name", CONVERTED_IN_LOADING + "_" + oldname);
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.resource.AbstractModelConverter#convertSaveElement(org.jdom.Element)
	 */
	@Override
	protected void convertSaveElement(Element element, Map<?, ?> options) {
		try {
			if (IM_COMPONENT.equals(element.getName())) {
				String oldname = element.getAttributeValue("name");
				element.setAttribute("name", CONVERTED_IN_SAVING + "_" + oldname);
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
	}

}
