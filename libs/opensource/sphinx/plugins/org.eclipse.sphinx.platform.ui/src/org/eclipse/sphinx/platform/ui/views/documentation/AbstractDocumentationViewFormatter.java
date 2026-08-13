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

import org.eclipse.sphinx.platform.ui.internal.messages.Messages;

public abstract class AbstractDocumentationViewFormatter implements IDocumentationViewFormatter {

	protected abstract String formatHeader(Object object);

	protected abstract String format(Object object);

	@Override
	public List<IDocumentationSection> getDocumentationSectionFor(Object object) {
		List<IDocumentationSection> sections = new ArrayList<IDocumentationSection>();
		try {
			sections.add(new DocumentationSection(formatHeader(object), format(object)));
		} catch (Exception ex) {
			sections.add(new DocumentationSection(Messages.title_section_error, "<pre>" + ex.getMessage() + "</pre>")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return sections;
	}
}
