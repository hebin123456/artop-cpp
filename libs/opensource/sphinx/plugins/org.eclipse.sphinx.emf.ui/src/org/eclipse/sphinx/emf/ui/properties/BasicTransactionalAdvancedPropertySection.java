/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [393441] SWTException occasionally occurring when BasicTransactionalAdvancedPropertySection is updated
 *     itemis - [393477] Provider hook for unwrapping elements before letting BasicTabbedPropertySheetTitleProvider retrieve text or image for them
 *     itemis - [408537] Enable property descriptions of model object features to be displayed in status line of Properties view
 *     itemis - [408540] Provide hook for unwrapping selected model object before letting BasicTransactionalAdvancedPropertySection process it
 *     itemis - [422130] The original element should be returned when unwrapped if the element is instance of FeatureMapEntryWrapperItemProvider
 *     itemis - [425252] UML property section hangs when accessing reference property of a stereotype application
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.ui.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.ui.celleditor.ExtendedDialogCellEditor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.FeatureMapEntryWrapperItemProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.WrapperItemProvider;
import org.eclipse.emf.edit.ui.provider.PropertyDescriptor;
import org.eclipse.emf.edit.ui.provider.PropertySource;
import org.eclipse.emf.transaction.NotificationFilter;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.ui.provider.TransactionalAdapterFactoryContentProvider;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.tabbed.AdvancedPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class BasicTransactionalAdvancedPropertySection extends AdvancedPropertySection implements IPropertySourceProvider {

	protected TabbedPropertySheetPage tabbedPropertySheetPage;

	protected IPropertySourceProvider lastPropertySourceProviderDelegate = null;

	protected ResourceSetListenerImpl selectedObjectChangedListener = null;

	protected Object lastSelectedObject = null;

	/**
	 * Required to make sure that Properties view gets refreshed when attributes of currently selected model element are
	 * changed (because the {@link TabbedPropertySheetPage}'s
	 * {@link org.eclipse.jface.viewers.ISelectionChangedListener} does nothing in this case)
	 */
	protected ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (tabbedPropertySheetPage != null) {
				IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);
				if (!structuredSelection.isEmpty()) {
					Object selectedObject = unwrap(structuredSelection.getFirstElement());
					if (selectedObject != lastSelectedObject) {
						// Remove existing selected object changed listener for previously selected object if any
						/*
						 * !! Important Note !! Don't use WorkspaceEditingDomainUtil for retrieving editing domain here
						 * because we only want to handle objects which are eligible to EMF.Edit rather than just any
						 * object from which an editing domain can be retrieved.
						 */
						TransactionalEditingDomain oldEditingDomain = TransactionUtil.getEditingDomain(lastSelectedObject);
						if (oldEditingDomain != null) {
							oldEditingDomain.removeResourceSetListener(selectedObjectChangedListener);
						}

						// Remember currently selected object as for future removals of selected object changed listener
						lastSelectedObject = selectedObject;

						// Install new selected object changed listener for currently selected object
						/*
						 * !! Important Note !! Don't use WorkspaceEditingDomainUtil for retrieving editing domain here
						 * because we only want to handle objects which are eligible to EMF.Edit rather than just any
						 * object from which an editing domain can be retrieved.
						 */
						TransactionalEditingDomain newEditingDomain = TransactionUtil.getEditingDomain(selectedObject);
						if (newEditingDomain != null) {
							selectedObjectChangedListener = createSelectedObjectChangedListener(selectedObject);
							newEditingDomain.addResourceSetListener(selectedObjectChangedListener);
						}
					}
				}
			}
		}
	};

	protected ResourceSetListenerImpl createSelectedObjectChangedListener(Object selectedObject) {
		Assert.isNotNull(selectedObject);

		return new ResourceSetListenerImpl(NotificationFilter.createNotifierFilter(selectedObject)) {
			/*
			 * @see org.eclipse.emf.transaction.ResourceSetListenerImpl#resourceSetChanged(ResourceSetChangeEvent)
			 */
			@Override
			public void resourceSetChanged(ResourceSetChangeEvent event) {
				if (tabbedPropertySheetPage != null) {
					IPageSite site = tabbedPropertySheetPage.getSite();
					if (site != null) {
						site.getShell().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								// Refresh property section content
								if (page != null) {
									refresh();

									// Refresh property sheet title through this indirect call to private
									// TabbedPropertySheetPage#refreshTitleBar() method
									if (tabbedPropertySheetPage != null) {
										tabbedPropertySheetPage.labelProviderChanged(new LabelProviderChangedEvent(new BaseLabelProvider()));
									}
								}
							}
						});
					}
				}
			}
		};
	}

	/**
	 * Extracts the actual element to rendered from given {@link Object element}.
	 * <p>
	 * This implementation calls {@link AdapterFactoryEditingDomain#unwrap()} for that purpose. Subclasses may override
	 * and extend as appropriate.
	 * </p>
	 *
	 * @param element
	 *            The element to be unwrapped.
	 * @return The extracted {@link Object element} if the original element could be successfully unwrapped or the
	 *         original element otherwise.
	 */
	protected Object unwrap(Object element) {
		if (element instanceof FeatureMapEntryWrapperItemProvider) {
			return element;
		}
		return AdapterFactoryEditingDomain.unwrap(element);
	}

	/*
	 * @see
	 * org.eclipse.ui.views.properties.tabbed.AdvancedPropertySection#createControls(org.eclipse.swt.widgets.Composite,
	 * org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
	public void createControls(Composite parent, final TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);
		this.tabbedPropertySheetPage = tabbedPropertySheetPage;

		// Create embedded standard property sheet page
		createEmbeddedStandardPropertySheetPage(parent);

		// Install selection listener and invoke it once in order to make sure that we are in phase with current
		// selection
		ISelectionService selectionService = tabbedPropertySheetPage.getSite().getWorkbenchWindow().getSelectionService();
		selectionService.addSelectionListener(selectionListener);
		selectionListener.selectionChanged(getPart(), selectionService.getSelection());
	}

	/**
	 * Creates and initializes the embedded standard property sheet page which presents a table of property names and
	 * values obtained from the current selection in the active workbench part.
	 */
	protected void createEmbeddedStandardPropertySheetPage(Composite parent) {
		// Register this class as property source provider
		page.setPropertySourceProvider(this);

		// Connect the tabbed property sheet's action bars to the embedded standard property sheet page
		/*
		 * !! Important Note !! This is necessary to get the descriptions of the property sources displayed in the
		 * status line.
		 */
		IActionBars actionBars = tabbedPropertySheetPage.getSite().getActionBars();
		page.makeContributions(actionBars.getMenuManager(), actionBars.getToolBarManager(), actionBars.getStatusLineManager());
	}

	/*
	 * @see org.eclipse.ui.views.properties.tabbed.AdvancedPropertySection#setInput(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		if (!selection.isEmpty() && selection instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			ArrayList<Object> translatedSelection = new ArrayList<Object>(structuredSelection.size());
			for (Iterator<?> it = structuredSelection.iterator(); it.hasNext();) {
				Object unwrapped = unwrap(it.next());
				if (unwrapped != null) {
					translatedSelection.add(unwrapped);
				}
			}
			selection = new StructuredSelection(translatedSelection);
		}
		super.setInput(part, selection);
	}

	/*
	 * @see org.eclipse.ui.views.properties.IPropertySourceProvider#getPropertySource(java.lang.Object)
	 */
	@Override
	public IPropertySource getPropertySource(Object object) {
		// Let EMF.Edit try to find a property source adapter
		/*
		 * !! Important Note !! Don't use WorkspaceEditingDomainUtil for retrieving editing domain here because we only
		 * want to handle objects which are eligible to EMF.Edit rather than just any object from which an editing
		 * domain can be retrieved.
		 */
		if (object != null) {
			// Try to retrieve model property source provider for given object and remember it so as to have it at hand
			// for subsequent objects for which no property source provider can be retrieved (e.g., FeatureMap.Entry
			// objects with primitive values)
			TransactionalEditingDomain editingDomain = getEditingDomain(object);
			if (editingDomain != null) {
				lastPropertySourceProviderDelegate = createModelPropertySourceProvider(editingDomain);
			}

			// Try retrieve property source adapter for given object using model property source provider
			if (lastPropertySourceProviderDelegate != null) {
				IPropertySource propertySource = lastPropertySourceProviderDelegate.getPropertySource(object);
				if (propertySource != null) {
					return new FilteringPropertySource(propertySource);
				}
			}
		}

		// Let Eclipse Platform try to find a property source adapter for objects that are not supported by EMF.Edit
		if (object instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) object;
			return adaptable.getAdapter(IPropertySource.class);
		}

		return null;
	}

	private TransactionalEditingDomain getEditingDomain(Object object) {

		TransactionalEditingDomain editingDomain = null;
		if (object instanceof WrapperItemProvider) {
			Object wrappedValue = ((WrapperItemProvider) object).getValue();
			if (wrappedValue instanceof FeatureMap.Entry) {
				Object value = ((FeatureMap.Entry) wrappedValue).getValue();
				editingDomain = TransactionUtil.getEditingDomain(value);
			}
		} else {
			editingDomain = TransactionUtil.getEditingDomain(object);
		}
		return editingDomain;

	}

	protected IPropertySourceProvider createModelPropertySourceProvider(TransactionalEditingDomain editingDomain) {
		Assert.isNotNull(editingDomain);

		AdapterFactory adapterFactory = getAdapterFactory(editingDomain);
		return new TransactionalAdapterFactoryContentProvider(editingDomain, adapterFactory) {
			/**
			 * Overridden to enable insertion of custom cell editor that will be used to edit the value of the given
			 * property.
			 */
			@Override
			protected IPropertySource createPropertySource(final Object object, final IItemPropertySource itemPropertySource) {
				return wrap(run(new RunnableWithResult.Impl<IPropertySource>() {
					@Override
					public void run() {
						setResult(new PropertySource(object, itemPropertySource) {
							@Override
							protected IPropertyDescriptor createPropertyDescriptor(IItemPropertyDescriptor itemPropertyDescriptor) {
								return new PropertyDescriptor(object, itemPropertyDescriptor) {
									@Override
									public CellEditor createPropertyEditor(final Composite composite) {
										CellEditor editor = BasicTransactionalAdvancedPropertySection.this.createPropertyEditor(composite, object,
												itemPropertyDescriptor, this);
										if (editor != null) {
											return editor;
										}
										return super.createPropertyEditor(composite);
									}
								};
							}
						});
					}
				}));
			}
		};
	}

	/**
	 * Returns the {@link AdapterFactory adapter factory} to be used by this {@link BasicModelEditActionProvider action
	 * provider} for creating {@link ItemProviderAdapter item provider}s which control the way how {@link EObject model
	 * element}s from given <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns the {@link AdapterFactory adapter factory} which is embedded in the given
	 * <code>editingDomain</code> by default. Clients which want to use an alternative {@link AdapterFactory adapter
	 * factory} (e.g., an {@link AdapterFactory adapter factory} that creates {@link ItemProviderAdapter item provider}s
	 * which are specifically designed for the {@link IEditorPart editor} in which this
	 * {@link BasicModelEditActionProvider action provider} is used) may override {@link #getCustomAdapterFactory()} and
	 * return any {@link AdapterFactory adapter factory} of their choice. This custom {@link AdapterFactory adapter
	 * factory} will then be returned as result by this method.
	 * </p>
	 *
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} whose embedded {@link AdapterFactory adapter
	 *            factory} is to be returned as default. May be left <code>null</code> if
	 *            {@link #getCustomAdapterFactory()} has been overridden and returns a non-<code>null</code> result.
	 * @return The {@link AdapterFactory adapter factory} that will be used by this {@link BasicModelEditActionProvider
	 *         action provider}. <code>null</code> if no custom {@link AdapterFactory adapter factory} is provided
	 *         through {@link #getCustomAdapterFactory()} and no <code>editingDomain</code> has been specified.
	 * @see #getCustomAdapterFactory()
	 */
	protected AdapterFactory getAdapterFactory(TransactionalEditingDomain editingDomain) {
		AdapterFactory customAdapterFactory = getCustomAdapterFactory();
		if (customAdapterFactory != null) {
			return customAdapterFactory;
		} else if (editingDomain != null) {
			return ((AdapterFactoryEditingDomain) editingDomain).getAdapterFactory();
		}
		return null;
	}

	/**
	 * Returns a custom {@link AdapterFactory adapter factory} to be used by this
	 * {@link BasicTransactionalAdvancedPropertySection advanced property section} for creating
	 * {@link ItemProviderAdapter item provider}s which control the way how {@link EObject model element}s from given
	 * <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns <code>null</code> as default. Clients which want to use their own
	 * {@link AdapterFactory adapter factory} (e.g., an {@link AdapterFactory adapter factory} that creates
	 * {@link ItemProviderAdapter item provider}s which are specifically designed for the {@link IEditorPart editor} in
	 * which this {@link BasicModelEditActionProvider action provider} is used) may override this method and return any
	 * {@link AdapterFactory adapter factory} of their choice. This custom {@link AdapterFactory adapter factory} will
	 * then be returned as result by {@link #getAdapterFactory(TransactionalEditingDomain)}.
	 * </p>
	 *
	 * @return The custom {@link AdapterFactory adapter factory} that is to be used by this
	 *         {@link BasicModelEditActionProvider action provider}. <code>null</code> the default {@link AdapterFactory
	 *         adapter factory} returned by {@link #getAdapterFactory(TransactionalEditingDomain)} should be used
	 *         instead.
	 * @see #getAdapterFactory(TransactionalEditingDomain)
	 */
	protected AdapterFactory getCustomAdapterFactory() {
		return null;
	}

	/**
	 * Return a custom {@link CellEditor cell editor} to be used for editing the value of given property.
	 *
	 * @param composite
	 *            The parent control of the {@link CellEditor cell editor} to be created.
	 * @param object
	 *            The owner of the {@link IItemPropertyDescriptor property} to be edited.
	 * @param itemPropertyDescriptor
	 *            The {@link IItemPropertyDescriptor item descriptor} of the property to be edited.
	 * @param propertyDescriptor
	 *            The {@link PropertyDescriptor descriptor} of the property to be edited.
	 * @return A newly created custom {@link CellEditor cell editor} to be used or <code>null</code> to indicate that
	 *         default {@link CellEditor cell editor} created by EMF.Edit should be used.
	 */
	protected CellEditor createPropertyEditor(Composite composite, Object object, final IItemPropertyDescriptor itemPropertyDescriptor,
			final PropertyDescriptor propertyDescriptor) {
		if (object instanceof EObject) {
			final EObject eObject = (EObject) object;
			Object feature = itemPropertyDescriptor.getFeature(eObject);
			if (feature instanceof EReference) {
				final EReference reference = (EReference) feature;
				if (!reference.isMany()) {
					Collection<?> choiceOfValues = itemPropertyDescriptor.getChoiceOfValues(eObject);
					if (choiceOfValues != null) {
						EObject value = (EObject) choiceOfValues.iterator().next();
						if (value != null && value.eIsProxy()) {
							return new ProxyURICellEditor(composite, eObject, reference, value);
						}
					}
				} else {
					final Collection<?> choiceOfValues = itemPropertyDescriptor.getChoiceOfValues(eObject);
					if (choiceOfValues != null) {
						for (Object element : choiceOfValues) {
							EObject value = (EObject) element;
							if (value.eIsProxy()) {
								final ILabelProvider editLabelProvider = propertyDescriptor.getLabelProvider();
								return new ExtendedDialogCellEditor(composite, editLabelProvider) {
									@Override
									protected Object openDialogBox(Control cellEditorWindow) {
										ProxyURIFeatureEditorDialog dialog = new ProxyURIFeatureEditorDialog(cellEditorWindow.getShell(),
												editLabelProvider, eObject, reference, propertyDescriptor.getDisplayName(),
												new ArrayList<Object>(choiceOfValues), false, itemPropertyDescriptor.isSortChoices(eObject));
										dialog.open();
										return dialog.getResult();
									}
								};
							}
						}
					}
				}
			}
		}

		return null;
	}

	@Override
	public void dispose() {
		// Unregister remaining selected object changed listener from all editing domains
		if (selectedObjectChangedListener != null) {
			for (TransactionalEditingDomain editingDomain : WorkspaceEditingDomainUtil.getAllEditingDomains()) {
				editingDomain.removeResourceSetListener(selectedObjectChangedListener);
			}
		}
		// Uninstall selection listener
		if (selectionListener != null) {
			ISelectionService selectionService = null;
			if (tabbedPropertySheetPage != null) {
				IPageSite site = tabbedPropertySheetPage.getSite();
				if (site != null) {
					selectionService = site.getWorkbenchWindow().getSelectionService();
				}
			}
			if (selectionService == null) {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null) {
					IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
					if (windows.length > 0) {
						window = windows[0];
					}
				}
				if (window != null) {
					selectionService = window.getSelectionService();
				}
			}
			if (selectionService != null) {
				selectionService.removeSelectionListener(selectionListener);
			}
		}
		super.dispose();
	}
}
