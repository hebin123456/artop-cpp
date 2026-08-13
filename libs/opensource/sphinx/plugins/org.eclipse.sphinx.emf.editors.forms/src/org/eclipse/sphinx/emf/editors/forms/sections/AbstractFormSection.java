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
package org.eclipse.sphinx.emf.editors.forms.sections;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.sphinx.emf.editors.forms.layouts.LayoutFactory;
import org.eclipse.sphinx.emf.editors.forms.pages.AbstractFormPage;
import org.eclipse.sphinx.emf.ui.forms.messages.IFormMessage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public abstract class AbstractFormSection implements IFormSection {

	protected AbstractFormPage formPage;
	protected Object sectionInput;
	protected int style;
	// FIXME Make title and description private in order to force clients to use corresponding setters
	protected String title;
	protected String description;
	protected Section section;
	protected FocusListener focusListener;

	public AbstractFormSection(AbstractFormPage formPage, Object sectionInput) {
		this(formPage, sectionInput, SWT.NONE);
	}

	public AbstractFormSection(AbstractFormPage formPage, Object sectionInput, int style) {
		Assert.isNotNull(formPage);
		this.formPage = formPage;
		this.sectionInput = sectionInput;
		this.style = style;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
		if (section != null) {
			section.setText(title);
		}
	}

	public String getTitle() {
		return title;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
		if (section != null) {
			section.setDescription(description);
		}
	}

	protected int getDefaultSectionStyle() {
		return Section.DESCRIPTION | ExpandableComposite.TITLE_BAR;
	}

	@Override
	public void setSectionInput(Object sectionInput) {
		this.sectionInput = sectionInput;
	}

	public Object getSectionInput() {
		return sectionInput;
	}

	protected String getSectionInputName() {
		if (sectionInput instanceof Resource) {
			URI sectionInputURI = ((Resource) sectionInput).getURI();
			return sectionInputURI.segmentCount() > 0 ? sectionInputURI.segment(sectionInputURI.segmentCount() - 1) : sectionInputURI.toString();
		}
		AdapterFactoryItemDelegator itemDelegator = formPage.getItemDelegator();
		if (itemDelegator != null) {
			return itemDelegator.getText(sectionInput);
		}
		return ""; //$NON-NLS-1$
	}

	protected String getSectionInputTypeName() {
		if (sectionInput instanceof EObject) {
			return ((EObject) sectionInput).eClass().getName();
		}
		return sectionInput != null ? sectionInput.getClass().getSimpleName() : ""; //$NON-NLS-1$
	}

	@Override
	public void createContent(IManagedForm managedForm, Composite parent) {
		Assert.isNotNull(managedForm);
		Assert.isNotNull(parent);

		// Create section part
		FormToolkit toolkit = managedForm.getToolkit();
		SectionPart sectionPart = createSectionPart(parent, toolkit);

		// Initialize section
		section = sectionPart.getSection();
		if (title != null) {
			section.setText(title);
		}
		if (description != null) {
			section.setDescription(description);
		}
		if (parent.getLayout() instanceof GridLayout) {
			section.setLayoutData(new GridData(GridData.FILL_BOTH));
		} else if (parent.getLayout() instanceof TableWrapLayout) {
			section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));
		}
		section.marginWidth = 10;
		section.marginHeight = 5;

		// Create and set section client
		Composite client = createSectionClient(managedForm, sectionPart);
		addFocusListener(client);
		section.setClient(client);

		// Add section part to form
		managedForm.addPart(sectionPart);
	}

	protected SectionPart createSectionPart(Composite parent, FormToolkit toolkit) {
		style = style != SWT.NONE ? style : getDefaultSectionStyle();
		return new SectionPart(parent, toolkit, style);
	}

	protected Composite createSectionClient(IManagedForm managedForm, SectionPart sectionPart) {
		return doCreateSectionClient(managedForm, sectionPart);
	}

	protected Composite doCreateSectionClient(IManagedForm managedForm, SectionPart sectionPart) {
		Assert.isNotNull(managedForm);
		Assert.isNotNull(sectionPart);

		// Create section client composite
		FormToolkit toolkit = managedForm.getToolkit();
		Composite sectionClient = toolkit.createComposite(section);
		toolkit.paintBordersFor(sectionClient);
		if (section.getLayoutData() instanceof GridData) {
			sectionClient.setLayoutData(new GridData(GridData.FILL_BOTH));
		} else if (section.getLayoutData() instanceof TableWrapData) {
			sectionClient.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));
		}
		sectionClient.setLayout(createSectionClientLayout());

		// Create section client content
		createSectionClientContent(managedForm, sectionPart, sectionClient);

		// Create a section tool bar manager
		ToolBarManager toolBarManager = createSectionToolbar(section, toolkit);

		fillSectionToolBarActions(toolBarManager);

		toolBarManager.update(true);

		return sectionClient;
	}

	protected ToolBarManager createSectionToolbar(Section section, FormToolkit toolkit) {

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		final Cursor handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
		toolbar.setCursor(handCursor);
		// Cursor needs to be explicitly disposed
		toolbar.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (handCursor != null && handCursor.isDisposed() == false) {
					handCursor.dispose();
				}
			}
		});
		section.setTextClient(toolbar);
		return toolBarManager;
	}

	/**
	 * Clients can override this methods to contribute actions to the tool bar of the section
	 * 
	 * @param toolBarManager
	 */
	protected void fillSectionToolBarActions(ToolBarManager toolBarManager) {
		// Default implementation nothing to add

	}

	protected Layout createSectionClientLayout() {
		if (section.getLayoutData() instanceof GridData) {
			return LayoutFactory.createSectionClientGridLayout(false, getNumberOfColumns());
		} else if (section.getLayoutData() instanceof TableWrapData) {
			return LayoutFactory.createSectionClientTableWrapLayout(false, getNumberOfColumns());
		}
		return null;
	}

	protected int getNumberOfColumns() {
		return 2;
	}

	protected abstract void createSectionClientContent(IManagedForm managedForm, SectionPart sectionPart, Composite sectionClient);

	protected boolean isControlAccessible(Control control) {
		return control != null && !control.isDisposed() && control.getDisplay() != null;
	}

	protected FocusListener getFocusListener() {
		if (focusListener == null) {
			focusListener = createFocusListner();
		}
		return focusListener;
	}

	protected FocusListener createFocusListner() {
		return new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				AbstractFormSection.this.focusLost(e);
			}

			@Override
			public void focusGained(FocusEvent e) {
				AbstractFormSection.this.focusGained(e);
			}

		};
	}

	protected void addFocusListener(Control control) {
		control.addFocusListener(getFocusListener());
		// Add the listener recursively to all children control
		if (control instanceof Composite) {
			for (Control childControl : ((Composite) control).getChildren()) {
				addFocusListener(childControl);
			}
		}
	}

	protected void focusLost(FocusEvent e) {
		// Do nothing by default
	}

	protected void focusGained(FocusEvent e) {
		formPage.setActiveSection(this);
	}

	public AbstractFormPage getFormPage() {
		return formPage;
	}

	@Override
	public void refreshSection() {
		// Do nothing by default
	}

	@Override
	public void refreshMessages(IMessageManager messageManager, Map<EStructuralFeature, Set<IFormMessage>> messages) {
		// Do nothing by default
	}

	public boolean isEmpty() {
		return sectionInput != null;
	}

	public void dispose() {
		// FIXME Implement dispose method in all subclasses
	}
}