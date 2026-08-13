/**
 * <copyright>
 *
 * Copyright (c) 2012-2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [434230] ParseException when trying to sort BasicXViewerSection for columns displaying Date-typed EAttributes
 *     itemis - [436313] Enable BasicXViewerSection also to be used for EReference-based properties
 *     itemis - [436429] Enable custom layout data to be passed to BasicXViewerSection
 *     itemis - [460260] Expanded paths are collapsed on resource reload
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.editors.forms.nebula.sections;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerFactory;
import org.eclipse.nebula.widgets.xviewer.XViewerSorter;
import org.eclipse.nebula.widgets.xviewer.XViewerTextFilter;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.edit.XViewerEditAdapter;
import org.eclipse.sphinx.emf.editors.forms.BasicTransactionalFormEditor;
import org.eclipse.sphinx.emf.editors.forms.nebula.internal.Activator;
import org.eclipse.sphinx.emf.editors.forms.nebula.providers.BasicModelXViewerLabelProvider;
import org.eclipse.sphinx.emf.editors.forms.pages.AbstractFormPage;
import org.eclipse.sphinx.emf.editors.forms.sections.AbstractViewerFormSection;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

// TODO Provide this as a field rather than a section
public class BasicXViewerSection extends AbstractViewerFormSection {

	protected EObject exampleValue;
	protected XViewerFactory xViewerFactory;
	protected GridData layoutData;

	public BasicXViewerSection(AbstractFormPage formPage, Object sectionInput, EObject exampleValue) {
		this(formPage, sectionInput, exampleValue, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
	}

	public BasicXViewerSection(AbstractFormPage formPage, Object sectionInput, EObject exampleValue, int style) {
		super(formPage, sectionInput, style);

		Assert.isNotNull(exampleValue);

		this.exampleValue = exampleValue;
	}

	public BasicXViewerSection(AbstractFormPage formPage, Object sectionInput, XViewerFactory xViewerFactory) {
		this(formPage, sectionInput, xViewerFactory, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
	}

	public BasicXViewerSection(AbstractFormPage formPage, Object sectionInput, XViewerFactory xViewerFactory, int style) {
		super(formPage, sectionInput, style);

		Assert.isNotNull(xViewerFactory);

		this.xViewerFactory = xViewerFactory;
	}

	public GridData getLayoutData() {
		if (layoutData == null) {
			layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		}
		return layoutData;
	}

	public void setLayoutData(GridData layoutData) {
		this.layoutData = layoutData;
	}

	@Override
	protected int getNumberOfColumns() {
		return 1;
	}

	@Override
	protected void createSectionClientContent(final IManagedForm managedForm, final SectionPart sectionPart, Composite sectionClient) {
		Assert.isNotNull(managedForm);
		Assert.isNotNull(sectionPart);
		Assert.isNotNull(sectionClient);

		// Create xViewer factory and define table columns if necessary
		if (xViewerFactory == null) {
			xViewerFactory = createXViewerFactory();
			registerColumns(xViewerFactory);
		}

		// Create table viewer
		XViewer xViewer = createXViewer(sectionClient, xViewerFactory);
		xViewer.getTree().setLayoutData(getLayoutData());

		// Make table viewer this section's viewer
		/*
		 * !! Important Note !! This must be done before initializing the table viewer's content/label providers because
		 * the latter are likely to require the table viewer and may want to retrieve it by calling #getViewer() (see
		 * #createLabelProvider() for details).
		 */
		setViewer(xViewer);

		// Provide table content
		xViewer.setContentProvider(createContentProvider());
		xViewer.setLabelProvider(createLabelProvider());

		// Set table input
		xViewer.setInput(sectionInput);
	}

	protected XViewerFactory createXViewerFactory() {
		XViewerFactory xViewerFactory = new XViewerFactory(exampleValue.eClass().getName()) {
			@Override
			public boolean isAdmin() {
				return true;
			}

			@Override
			public XViewerSorter createNewXSorter(XViewer xViewer) {
				return new XViewerSorter(xViewer) {
					@Override
					public int getCompareForDate(String date1, Object obj1, String date2, Object obj2) {
						if (date1 != null && obj1 == null && date2 != null && obj2 == null) {
							Date date1Date = null;
							try {
								date1Date = getDateFormat().parse(date1);
							} catch (ParseException ex) {
								PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
								return 0;
							}

							Date date2Date = null;
							try {
								date2Date = getDateFormat().parse(date2);
							} catch (ParseException ex) {
								PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
								return 0;
							}
							return getCompareForDate(date1Date, date2Date);
						} else {
							return super.getCompareForDate(date1, obj1, date2, obj2);
						}
					}
				};
			}
		};

		return xViewerFactory;
	}

	/**
	 * Returns the {@link DateFormat date format} to be used by this viewer's {@link XViewerSorter sorter} to sort
	 * {@link Date}-typed values. Must be the same date format as that being used by this viewer's
	 * {@link #createLabelProvider() label provider} to render Date-typed values.
	 * <p>
	 * This implementation returns an <code>new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US)</code>
	 * instance by default. It matches the format used by EMF Edit to render Date-typed {@link EAttribute attribute}
	 * values (see {@link PropertyValueWrapper#getText(Object), AdapterFactoryItemDelegator#getText(Object) and
	 * Date#toString() for details}. Clients may override and return {@link DateFormat} .getXxxInstance() or new
	 * {@link SimpleDateFormat} ("xxx") as appropriate.
	 * </p>
	 *
	 * @return The date format to be used for sorting Date-typed values displayed by this viewer.
	 * @see DateFormat
	 * @see SimpleDateFormat
	 */
	protected DateFormat getDateFormat() {
		return new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US); //$NON-NLS-1$
	}

	protected XViewer createXViewer(Composite sectionClient, XViewerFactory xViewerFactory) {
		final BasicTransactionalFormEditor formEditor = formPage.getTransactionalFormEditor();
		XViewer xViewer = new XViewer(sectionClient, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER, xViewerFactory) {
			@Override
			public void setSelection(ISelection selection) {
				selection = !SelectionUtil.getStructuredSelection(selection).isEmpty() ? selection : formEditor.getDefaultSelection();
				super.setSelection(selection);
			}

			@Override
			public XViewerTextFilter getXViewerTextFilter() {
				return createXViewerTextFilter(this);
			}
		};

		// Add editing support
		XViewerEditAdapter xViewerEditAdapter = createXViewerEditAdapter();
		if (xViewerEditAdapter != null) {
			xViewer.setXViewerEditAdapter(xViewerEditAdapter);
		}

		return xViewer;
	}

	/**
	 * Override to provide extended filter capabilities.
	 */
	protected XViewerTextFilter createXViewerTextFilter(XViewer xViewer) {
		return new XViewerTextFilter(xViewer);
	}

	/**
	 * Returns null by default. Sub classes can provide a concrete implementation to handle editing support.
	 */
	protected XViewerEditAdapter createXViewerEditAdapter() {
		return null;
	}

	protected void registerColumns(XViewerFactory xViewerFactory) {
		List<IItemPropertyDescriptor> propertyDescriptors = formPage.getItemDelegator().getPropertyDescriptors(exampleValue);
		for (IItemPropertyDescriptor propertyDescriptor : propertyDescriptors) {
			xViewerFactory.registerColumns(new XViewerColumn(propertyDescriptor.getId(exampleValue).toString(),
					propertyDescriptor.getDisplayName(exampleValue), 100, XViewerAlign.Left, true, getSortDataType(propertyDescriptor, exampleValue),
					false, propertyDescriptor.getDescription(exampleValue)));
		}
	}

	protected SortDataType getSortDataType(IItemPropertyDescriptor propertyDescriptor, Object object) {
		Assert.isNotNull(propertyDescriptor);
		Assert.isNotNull(object);

		EClassifier propertyType = ((EStructuralFeature) propertyDescriptor.getFeature(object)).getEType();
		if (propertyType == EcorePackage.eINSTANCE.getEDate()) {
			return SortDataType.Date;
		} else if (propertyType == EcorePackage.eINSTANCE.getEFloat() || propertyType == EcorePackage.eINSTANCE.getEDouble()) {
			return SortDataType.Float;
		} else if (propertyType == EcorePackage.eINSTANCE.getEBoolean()) {
			return SortDataType.Check;
		} else if (propertyType == EcorePackage.eINSTANCE.getEInt()) {
			return SortDataType.Integer;
		} else if (propertyDescriptor.isMultiLine(object)) {
			return SortDataType.String_MultiLine;
		}
		return SortDataType.String;
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new BasicModelXViewerLabelProvider((XViewer) getViewer(), formPage.getItemDelegator());
	}
}
