/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.ui.views.documentation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class DocumentationViewFormatterDescriptorRegistry {

	/**
	 * The singleton instance of this registry.
	 */
	public static final DocumentationViewFormatterDescriptorRegistry INSTANCE = new DocumentationViewFormatterDescriptorRegistry();

	private static final String EXTENSION_POINT_DOCUMENTATION_VIEW_FORMATTERS = "org.eclipse.sphinx.platform.ui.documentationViewFormatters"; //$NON-NLS-1$

	private IExtensionRegistry extensionRegistry;
	private List<DocumentationViewFormatterDescriptor> docViewFormatterDescriptors = new ArrayList<DocumentationViewFormatterDescriptor>();

	protected Ordering<DocumentationViewFormatterDescriptor> docViewFormatterOrdering = new Ordering<DocumentationViewFormatterDescriptor>() {

		@Override
		public int compare(DocumentationViewFormatterDescriptor left, DocumentationViewFormatterDescriptor right) {
			return Ints.compare(left.getPriority(), right.getPriority());
		}
	};

	/**
	 * Private constructor for the singleton pattern.
	 */
	private DocumentationViewFormatterDescriptorRegistry() {
		initialize();
	}

	private void initialize() {
		for (IConfigurationElement configurationElement : readContributedDocViewFormatters()) {
			docViewFormatterDescriptors.add(new DocumentationViewFormatterDescriptor(configurationElement));
		}
	}

	private IExtensionRegistry getExtensionRegistry() {
		if (extensionRegistry == null) {
			extensionRegistry = Platform.getExtensionRegistry();
		}
		return extensionRegistry;
	}

	protected IConfigurationElement[] readContributedDocViewFormatters() {
		IExtensionRegistry extensionRegistry = getExtensionRegistry();
		if (extensionRegistry != null) {
			return extensionRegistry.getConfigurationElementsFor(EXTENSION_POINT_DOCUMENTATION_VIEW_FORMATTERS);
		}
		return new IConfigurationElement[0];
	}

	protected Iterable<DocumentationViewFormatterDescriptor> getApplicableDocViewFormatterFor(final Object selectedObject) {
		Iterable<DocumentationViewFormatterDescriptor> applicableDocViewFormatters = Iterables.filter(
				Lists.newArrayList(docViewFormatterDescriptors), new Predicate<DocumentationViewFormatterDescriptor>() {

					@Override
					public boolean apply(DocumentationViewFormatterDescriptor docViewFormatterDescriptor) {
						return docViewFormatterDescriptor.isApplicableFor(selectedObject);
					}
				});
		return applicableDocViewFormatters;
	}
}
