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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.platform.ui.internal.Activator;
import org.eclipse.sphinx.platform.ui.internal.messages.Messages;
import org.eclipse.sphinx.platform.ui.views.documentation.bootstrap.BootstrapDescriptionWrapper;
import org.eclipse.sphinx.platform.ui.views.documentation.bootstrap.BootstrapFormatterHTML;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * The view to show any additional documentation. It collects the data to actually display through various extension
 * points.
 */
public class DocumentationView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.eclipse.sphinx.platform.ui.views.documentation.DocumentationView"; //$NON-NLS-1$

	private Browser browser;
	protected IDescriptionWrapper wrapper = new BootstrapDescriptionWrapper();

	/*
	 * The content provider class is responsible for providing objects to the view. It can wrap existing objects in
	 * adapters or simply return objects as-is. These objects may be sensitive to the current input of the view, or
	 * ignore it and always show the same content (like Task List, for example).
	 */
	private ISelectionListener listener = new ISelectionListener() {

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (!(selection instanceof IStructuredSelection)) {
				return;
			}

			StringBuilder builder = new StringBuilder();
			Iterator<?> iterator = ((IStructuredSelection) selection).iterator();
			String topTitle = Messages.title_documentation;
			List<IDocumentationSection> allSections = Lists.<IDocumentationSection> newArrayList();

			while (iterator.hasNext()) {
				final Object selectedObject = iterator.next();
				Iterable<DocumentationViewFormatterDescriptor> applicableDocViewFormatters = DocumentationViewFormatterDescriptorRegistry.INSTANCE
						.getApplicableDocViewFormatterFor(selectedObject);
				List<DocumentationViewFormatterDescriptor> sortedApplicableDocViewFormatters = DocumentationViewFormatterDescriptorRegistry.INSTANCE.docViewFormatterOrdering
						.sortedCopy(applicableDocViewFormatters);
				Iterable<IDocumentationViewFormatter> formatters = Iterables.transform(sortedApplicableDocViewFormatters,
						new Function<DocumentationViewFormatterDescriptor, IDocumentationViewFormatter>() {

							@Override
							public IDocumentationViewFormatter apply(DocumentationViewFormatterDescriptor descriptor) {
								try {
									return descriptor.getInstance();
								} catch (Exception ex) {
									PlatformLogUtil.logAsError(Activator.getDefault(), ex);
									return null;
								}
							}
						});

				for (IDocumentationViewFormatter formatter : formatters) {
					if (formatter != null) {
						List<IDocumentationSection> formatterSections = formatter.getDocumentationSectionFor(selectedObject);
						allSections.addAll(formatterSections);
						for (IDocumentationSection section : formatterSections) {
							if (section.getSectionTitle() != null && section.getSectionTitle().length() > 0 && section.getSectionBody() != null
									&& section.getSectionBody().trim().length() > 0) {
								builder.append(wrapper.textPre());
								builder.append("<h3>" + section.getSectionTitle() + "</h3>"); //$NON-NLS-1$ //$NON-NLS-2$
								builder.append(section.getSectionBody() + wrapper.textPost());
							}
						}
					}
					if (formatter instanceof ITitleProvider) {
						// FIXME
						topTitle = ((ITitleProvider) formatter).getTitle(selectedObject);
					}
				}
			}

			browser.setText(BootstrapFormatterHTML.format(topTitle, builder.toString(), allSections));
		}
	};

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		try {
			browser = new Browser(parent, SWT.NONE);

			String html = "<HTML><BODY>";
			html += "<P>" + Messages.desc_model_object_selection + "</P>";
			html += "</BODY></HTML>";

			browser.setText(html);
			getSite().getPage().addSelectionListener(listener);
		} catch (SWTError ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			return;
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		browser.setFocus();
	}

	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(listener);
		super.dispose();
	}
}