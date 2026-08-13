/**
 * <copyright>
 * 
 * Copyright (c) 2013 itemis and others.
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
package org.eclipse.sphinx.examples.uml2.ide.ui.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.ui.provider.PropertyDescriptor;
import org.eclipse.emf.edit.ui.provider.PropertySource;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.ui.provider.TransactionalAdapterFactoryContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.sphinx.emf.ui.properties.BasicTransactionalAdvancedPropertySection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.uml2.common.edit.provider.IItemQualifiedTextProvider;
import org.eclipse.uml2.uml.edit.providers.ElementItemProvider;

/**
 * A specialized {@link BasicTransactionalAdvancedPropertySection advance property section} for UML2 that provides
 * enhanced property sources supporting insertion and edition of additional property fields for stereotype applications,
 * creation of label texts using qualified element names, and insertion of custom cell editors.
 */
public class UML2AdvancedPropertySection extends BasicTransactionalAdvancedPropertySection {

	@Override
	protected IPropertySourceProvider createModelPropertySourceProvider(TransactionalEditingDomain editingDomain) {
		Assert.isNotNull(editingDomain);

		AdapterFactory adapterFactory = getAdapterFactory(editingDomain);
		return new TransactionalAdapterFactoryContentProvider(editingDomain, adapterFactory) {
			@Override
			protected IPropertySource createPropertySource(final Object object, final IItemPropertySource itemPropertySource) {
				return wrap(run(new RunnableWithResult.Impl<IPropertySource>() {
					@Override
					public void run() {
						setResult(new UML2PropertySource(object, itemPropertySource));
					}
				}));
			}
		};
	}

	/**
	 * A specialized {@link PropertySource property source} for UML2 that inserts additional property descriptors for
	 * stereotype applications and redirects corresponding read and write accesses to them.
	 */
	protected class UML2PropertySource extends PropertySource {

		protected UML2PropertySource(Object object, IItemPropertySource itemPropertySource) {
			super(object, itemPropertySource);
		}

		/*
		 * @see org.eclipse.emf.edit.ui.provider.PropertySource#getPropertyDescriptors()
		 */
		@Override
		public IPropertyDescriptor[] getPropertyDescriptors() {
			List<IPropertyDescriptor> propertyDescriptors = new ArrayList<IPropertyDescriptor>();
			propertyDescriptors.addAll(Arrays.asList(super.getPropertyDescriptors()));

			if (itemPropertySource instanceof ElementItemProvider) {
				ElementItemProvider elementItemProvider = (ElementItemProvider) itemPropertySource;
				List<IItemPropertyDescriptor> stereotypeApplicationItemPropertyDescriptors = elementItemProvider
						.getStereotypeApplicationPropertyDescriptors(object);
				if (stereotypeApplicationItemPropertyDescriptors != null) {
					for (IItemPropertyDescriptor itemPropertyDescriptor : stereotypeApplicationItemPropertyDescriptors) {
						propertyDescriptors.add(createPropertyDescriptor(itemPropertyDescriptor));
					}
				}
			}

			return propertyDescriptors.toArray(new IPropertyDescriptor[propertyDescriptors.size()]);
		}

		/*
		 * @see org.eclipse.emf.edit.ui.provider.PropertySource#createPropertyDescriptor(org.eclipse.emf.edit.provider.
		 * IItemPropertyDescriptor)
		 */
		@Override
		protected IPropertyDescriptor createPropertyDescriptor(IItemPropertyDescriptor itemPropertyDescriptor) {
			return new UML2PropertyDescriptor(object, itemPropertyDescriptor);
		}

		/*
		 * @see org.eclipse.emf.edit.ui.provider.PropertySource#getPropertyValue(java.lang.Object)
		 */
		@Override
		public Object getPropertyValue(Object propertyId) {
			return getItemPropertyDescriptor(propertyId).getPropertyValue(object);
		}

		/*
		 * @see org.eclipse.emf.edit.ui.provider.PropertySource#isPropertySet(java.lang.Object)
		 */
		@Override
		public boolean isPropertySet(Object propertyId) {
			return getItemPropertyDescriptor(propertyId).isPropertySet(object);
		}

		/*
		 * @see org.eclipse.emf.edit.ui.provider.PropertySource#resetPropertyValue(java.lang.Object)
		 */
		@Override
		public void resetPropertyValue(Object propertyId) {
			getItemPropertyDescriptor(propertyId).resetPropertyValue(object);
		}

		/*
		 * @see org.eclipse.emf.edit.ui.provider.PropertySource#setPropertyValue(java.lang.Object, java.lang.Object)
		 */
		@Override
		public void setPropertyValue(Object propertyId, Object value) {
			getItemPropertyDescriptor(propertyId).setPropertyValue(object, value);
		}

		protected IItemPropertyDescriptor getItemPropertyDescriptor(Object propertyId) {
			IItemPropertyDescriptor itemPropertyDescriptor = itemPropertySource.getPropertyDescriptor(object, propertyId);

			return itemPropertyDescriptor == null && itemPropertySource instanceof ElementItemProvider ? ((ElementItemProvider) itemPropertySource)
					.getStereotypeApplicationPropertyDescriptor(object, propertyId) : itemPropertyDescriptor;
		}
	}

	/**
	 * A specialized {@link PropertyDescriptor property descriptor} for UML2 that creates label texts using qualified
	 * element names, and enables custom cell editors to be inserted.
	 */
	protected class UML2PropertyDescriptor extends PropertyDescriptor {

		protected UML2PropertyDescriptor(Object object, IItemPropertyDescriptor itemPropertyDescriptor) {
			super(object, itemPropertyDescriptor);
		}

		/*
		 * @see org.eclipse.emf.edit.ui.provider.PropertyDescriptor#getEditLabelProvider()
		 */
		@Override
		protected ILabelProvider getEditLabelProvider() {
			final ILabelProvider editLabelProvider = super.getEditLabelProvider();

			return new LabelProvider() {

				@Override
				public String getText(Object object) {
					return itemPropertyDescriptor instanceof IItemQualifiedTextProvider ? ((IItemQualifiedTextProvider) itemPropertyDescriptor)
							.getQualifiedText(object) : editLabelProvider.getText(object);
				}

				@Override
				public Image getImage(Object object) {
					return editLabelProvider.getImage(object);
				}
			};
		}

		/*
		 * @see
		 * org.eclipse.emf.edit.ui.provider.PropertyDescriptor#createPropertyEditor(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		public CellEditor createPropertyEditor(final Composite composite) {
			CellEditor editor = UML2AdvancedPropertySection.this.createPropertyEditor(composite, object, itemPropertyDescriptor, this);
			if (editor != null) {
				return editor;
			}
			return super.createPropertyEditor(composite);
		}
	}
}
