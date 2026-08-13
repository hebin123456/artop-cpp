/**
 * <copyright>
 *
 * Copyright (c) 2011-2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementatio
 *     itemis - [463119] References View navigation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedImage;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.ui.provider.TransactionalAdapterFactoryLabelProvider;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.DelegatingDropAdapter;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sphinx.emf.ui.util.EcoreUIUtil;
import org.eclipse.sphinx.emf.ui.util.OpenStrategy;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.workspace.ui.internal.Activator;
import org.eclipse.sphinx.emf.workspace.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.workspace.ui.internal.views.ReferencesHierarchyTransferDropAdapter;
import org.eclipse.sphinx.emf.workspace.ui.internal.views.ToggleReferencesModeAction;
import org.eclipse.sphinx.emf.workspace.ui.util.ICommonModelUIConstants;
import org.eclipse.sphinx.emf.workspace.ui.util.TransactionalDelegatingTreeContentProvider;
import org.eclipse.sphinx.platform.util.DirectedGraph;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 *
 */
public class ReferencesView extends ViewPart implements ITabbedPropertySheetPageContributor {

	private static final String STORE_MODE = "MODE"; //$NON-NLS-1$

	public static final int REFERENCED_OBJECTS_MODE = 0;
	public static final int REFERENCING_OBJECTS_MODE = 1;

	private static final int PAGE_EMPTY = 0;
	private static final int PAGE_VIEWER = 1;

	protected Set<IPropertySheetPage> propertySheetPages = new HashSet<IPropertySheetPage>();

	private Object viewInput;
	private int currentMode;
	private Label noRefsHierarchyShownLabel;
	private TreeViewer viewer;

	private ILabelProvider labelProvider;
	private IContentProvider contentProvider;
	private IDialogSettings dialogSettings;
	private ToggleReferencesModeAction[] toggleReferencesModeActions;

	protected Map<TransactionalEditingDomain, IContentProvider> modelCrossReferenceContentProviders = new WeakHashMap<TransactionalEditingDomain, IContentProvider>();
	protected Map<TransactionalEditingDomain, ILabelProvider> modelLabelProviders = new WeakHashMap<TransactionalEditingDomain, ILabelProvider>();

	private Action doubleClickAction;

	private PageBook pageBook;
	private SashForm refsHierarchyInfosSplitter;

	private DirectedGraph<EObject> graph;

	public ReferencesView() {
		dialogSettings = Activator.getDefault().getDialogSettings();
		graph = new DirectedGraph<EObject>(false, true);
	}

	@Override
	public void createPartControl(Composite parent) {

		pageBook = new PageBook(parent, SWT.NONE);

		// First page: viewers
		refsHierarchyInfosSplitter = new SashForm(pageBook, SWT.NONE);
		viewer = new TreeViewer(refsHierarchyInfosSplitter, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		if (contentProvider != null) {
			viewer.setContentProvider(contentProvider);
		}
		if (labelProvider != null) {
			viewer.setLabelProvider(labelProvider);
		}

		// Second page: when nothing selected
		noRefsHierarchyShownLabel = new Label(pageBook, SWT.TOP + SWT.LEFT + SWT.WRAP);
		noRefsHierarchyShownLabel.setText(Messages.label_ReferencesHierarchyEmpty); //

		showPage(PAGE_EMPTY);

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		// initDragAndDrop();

		initMode();
	}

	private void showPage(int page) {
		boolean isEmpty = page == PAGE_EMPTY;
		Control control = isEmpty ? (Control) noRefsHierarchyShownLabel : refsHierarchyInfosSplitter;
		if (isEmpty) {
			setContentDescription(""); //$NON-NLS-1$
			setTitleToolTip(getPartName());
			getViewSite().getActionBars().getStatusLineManager().setMessage(""); //$NON-NLS-1$
		}
		pageBook.showPage(control);
	}

	protected TreeViewer getViewer() {
		return viewer;
	}

	public void setViewInput(Object viewInput) {
		this.viewInput = viewInput;
		contentProvider = getModelContentProvider(viewInput);
		if (contentProvider != null) {
			viewer.setContentProvider(contentProvider);
		}
		labelProvider = getModelLabelProvider(viewInput);
		if (labelProvider != null) {
			viewer.setLabelProvider(labelProvider);
		}
		viewer.setInput(getViewerInput());

		updateView();
	}

	protected Object getViewerInput() {
		return new WrappedViewInput(viewInput);
	}

	protected boolean isRecursive(Object element) {
		Set<DirectedGraph<EObject>.Edge> outgoingEdgesOfElement = graph.outgoingEdgesOf((EObject) element);
		Set<DirectedGraph<EObject>.Edge> incomingEdgesOfElement = graph.incomingEdgesOf((EObject) element);
		return !outgoingEdgesOfElement.isEmpty() && incomingEdgesOfElement.size() >= 1;
	}

	protected IContentProvider getModelContentProvider(Object element) {
		// Retrieve editing domain behind specified object
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(element);
		if (editingDomain != null) {
			// Retrieve model content provider for given editing domain; create new one if not existing yet
			IContentProvider modelContentProvider = modelCrossReferenceContentProviders.get(editingDomain);
			if (modelContentProvider == null) {
				modelContentProvider = createModelCrossReferenceContentProvider(editingDomain);
				modelCrossReferenceContentProviders.put(editingDomain, modelContentProvider);
			}
			return modelContentProvider;
		}
		return null;
	}

	protected ITreeContentProvider createModelCrossReferenceContentProvider(TransactionalEditingDomain editingDomain) {
		return new TransactionalDelegatingTreeContentProvider(new ITreeContentProvider() {

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// Do nothing
			}

			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof WrappedViewInput) {
					EObject input = (EObject) ((WrappedViewInput) inputElement).getInput();
					// As it's the first element to be displayed in the view, add it to the graph
					graph.addVertex(input);
					return new Object[] { input };
				} else {
					return getChildren(inputElement);
				}
			}

			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof EObject) {
					return !isRecursive(element) && getChildren(element).length > 0;
				}
				return false;
			}

			// TODO Add defer and abort capability
			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof EObject) {
					final EObject parentEObject = (EObject) parentElement;

					// Add the parent to the graph
					graph.addVertex(parentEObject);

					TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(parentEObject);
					if (editingDomain != null) {
						try {
							return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<Object[]>() {
								@Override
								public void run() {
									Collection<EObject> eCrossReferences = getECrossReferences(parentEObject);
									addEdges(parentEObject, eCrossReferences);
									setResult(eCrossReferences.toArray(new EObject[eCrossReferences.size()]));
								}
							});
						} catch (InterruptedException ex) {
							PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
						}
					} else {
						Collection<EObject> eCrossReferences = getECrossReferences(parentEObject);
						addEdges(parentEObject, eCrossReferences);
						return eCrossReferences.toArray(new EObject[eCrossReferences.size()]);
					}
				}
				return new Object[0];
			}

			protected void addEdges(EObject parentEObject, Collection<EObject> eCrossReferences) {
				for (EObject crossRef : eCrossReferences) {
					graph.addVertex(crossRef);
					graph.addEdge(parentEObject, crossRef);
				}
			}

			protected Collection<EObject> getECrossReferences(EObject eObject) {
				if (currentMode == REFERENCING_OBJECTS_MODE) {
					List<EObject> eInverseCrossReferences = new ArrayList<EObject>();
					Collection<Setting> inverseReferences = EObjectUtil.getInverseReferences(eObject, true);
					for (Setting inverseReference : inverseReferences) {
						EStructuralFeature eStructuralFeature = inverseReference.getEStructuralFeature();
						if (eStructuralFeature instanceof EReference) {
							if (!((EReference) eStructuralFeature).isContainment()) {
								eInverseCrossReferences.add(inverseReference.getEObject());
							}
						} else {
							eInverseCrossReferences.add(inverseReference.getEObject());
						}
					}
					return eInverseCrossReferences;
				} else {
					return eObject.eCrossReferences();
				}
			}

			@Override
			public Object getParent(Object element) {
				if (element instanceof EObject) {
					return ((EObject) element).eContainer();
				}
				return null;
			}

			@Override
			public void dispose() {
				// Do nothing
			}
		}, editingDomain);
	}

	protected ILabelProvider getModelLabelProvider(Object element) {
		// Retrieve editing domain behind specified object
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(element);
		if (editingDomain != null) {
			// Retrieve model label provider for given editing domain; create new one if not existing yet
			ILabelProvider modelLabelProvider = modelLabelProviders.get(editingDomain);
			if (modelLabelProvider == null) {
				modelLabelProvider = createModelLabelProvider(editingDomain);
				modelLabelProviders.put(editingDomain, modelLabelProvider);
			}
			return modelLabelProvider;
		} else if (element instanceof EObject && ((EObject) element).eIsProxy()) {
			// Use non-transactional adapter factory label provider to avoid that proxified EObjects end up being
			// represented as empty tree nodes
			return new AdapterFactoryLabelProvider(getAdapterFactory(editingDomain));
		}
		return null;
	}

	protected ILabelProvider createModelLabelProvider(final TransactionalEditingDomain editingDomain) {
		Assert.isNotNull(editingDomain);
		AdapterFactory adapterFactory = getAdapterFactory(editingDomain);
		return new DecorationAwareTransactionalAdapterFactoryLabelProvider(editingDomain, adapterFactory) {
			@Override
			// Overridden to avoid the somewhat annoying logging of Eclipse exceptions resulting from event queue
			// dispatching that is done before transaction is acquired and actually starts to run
			protected <T> T run(RunnableWithResult<? extends T> run) {
				try {
					return TransactionUtil.runExclusive(editingDomain, run);
				} catch (Exception e) {
					// propagate interrupt status because we are not throwing
					Thread.currentThread().interrupt();

					return null;
				}
			}
		};
	}

	/**
	 * Returns the {@link AdapterFactory adapter factory} to be used by this {@link BasicExplorerLabelProvider label
	 * provider} for creating {@link ItemProviderAdapter item provider}s which control the way how {@link EObject model
	 * element}s from given <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns the {@link AdapterFactory adapter factory} which is embedded in the given
	 * <code>editingDomain</code> by default. Clients which want to use an alternative {@link AdapterFactory adapter
	 * factory} (e.g., an {@link AdapterFactory adapter factory} that creates {@link ItemProviderAdapter item provider}s
	 * which are specifically designed for the {@link IEditorPart editor} in which this
	 * {@link BasicExplorerLabelProvider label provider} is used) may override {@link #getCustomAdapterFactory()} and
	 * return any {@link AdapterFactory adapter factory} of their choice. This custom {@link AdapterFactory adapter
	 * factory} will then be returned as result by this method.
	 * </p>
	 *
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} whose embedded {@link AdapterFactory adapter
	 *            factory} is to be returned as default. May be left <code>null</code> if
	 *            {@link #getCustomAdapterFactory()} has been overridden and returns a non-<code>null</code> result.
	 * @return The {@link AdapterFactory adapter factory} that will be used by this {@link BasicExplorerLabelProvider
	 *         label provider}. <code>null</code> if no custom {@link AdapterFactory adapter factory} is provided
	 *         through {@link #getCustomAdapterFactory()} and no <code>editingDomain</code> has been specified.
	 * @see #getCustomAdapterFactory()
	 */
	protected AdapterFactory getAdapterFactory(TransactionalEditingDomain editingDomain) {
		if (editingDomain != null) {
			return ((AdapterFactoryEditingDomain) editingDomain).getAdapterFactory();
		}
		return null;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				ReferencesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		for (ToggleReferencesModeAction toggleReferencesModeAction : toggleReferencesModeActions) {
			manager.add(toggleReferencesModeAction);
			if (toggleReferencesModeAction != toggleReferencesModeActions[toggleReferencesModeActions.length - 1]) {
				manager.add(new Separator());
			}
		}
	}

	private void fillContextMenu(IMenuManager manager) {
		for (ToggleReferencesModeAction toggleAction : toggleReferencesModeActions) {
			manager.add(toggleAction);
		}
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		for (ToggleReferencesModeAction toggleAction : toggleReferencesModeActions) {
			manager.add(toggleAction);
		}
		manager.add(new Separator());
	}

	private void makeActions() {

		toggleReferencesModeActions = new ToggleReferencesModeAction[] { new ToggleReferencesModeAction(this, REFERENCED_OBJECTS_MODE),
				new ToggleReferencesModeAction(this, REFERENCING_OBJECTS_MODE) };

		doubleClickAction = new Action() {
			@Override
			public void run() {
				ISelection selection = viewer.getSelection();
				if (selection instanceof IStructuredSelection) {
					Object obj = ((IStructuredSelection) selection).getFirstElement();
					EcoreUIUtil.openEditor(getSite().getPage(), obj, OpenStrategy.OPEN_ON_RESOURCE);
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void initDragAndDrop() {
		// TODO (aakar) Add Drag support if needed
		// addDragAdapters(viewer);
		addDropAdapters(viewer);

		// DND on empty view
		DropTarget dropTarget = new DropTarget(pageBook, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK | DND.DROP_DEFAULT);
		dropTarget.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });
		dropTarget.addDropListener(new ReferencesHierarchyTransferDropAdapter(this, viewer));
	}

	private void addDropAdapters(StructuredViewer viewer) {
		Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };
		int ops = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK | DND.DROP_DEFAULT;

		DelegatingDropAdapter delegatingDropAdapter = new DelegatingDropAdapter();
		delegatingDropAdapter.addDropTargetListener(new ReferencesHierarchyTransferDropAdapter(this, viewer));

		viewer.addDropSupport(ops, transfers, delegatingDropAdapter);
	}

	private void initMode() {
		int mode;

		try {
			mode = dialogSettings.getInt(STORE_MODE);

			if (mode < 0 || mode > 1) {
				mode = REFERENCED_OBJECTS_MODE;
			}
		} catch (NumberFormatException e) {
			mode = REFERENCED_OBJECTS_MODE;
		}

		// force update in setMode(int)
		currentMode = -1;

		setMode(mode);
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter.equals(IShowInSource.class)) {
			return new IShowInSource() {
				@Override
				public ShowInContext getShowInContext() {
					// Set the selection as input for the context so that the ExtendedCommonNavigator will show it and
					// set the IFile behind the selection as the selection of the context so that the ProjectExplorer
					// will be able to show the file.
					return new ShowInContext(getViewer().getSelection(), getSelectedFiles(getViewer().getSelection()));
				}

				private ISelection getSelectedFiles(ISelection selection) {
					if (selection instanceof IStructuredSelection) {
						Set<IFile> selectedFiles = new HashSet<IFile>();
						for (Object obj : ((IStructuredSelection) selection).toList()) {
							IFile file = EcorePlatformUtil.getFile(obj);
							if (file != null) {
								selectedFiles.add(file);
							}
						}
						return new StructuredSelection(selectedFiles.toArray());
					}
					return selection;
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

	/**
	 * Passing the focus request to the pageBook.
	 */
	@Override
	public void setFocus() {
		pageBook.setFocus();
	}

	/**
	 * Called from {@link ToggleReferencesModeAction}.
	 *
	 * @param mode
	 *            {@link REFERENCED_OBJECTS_MODE } or {@link REFERENCING_OBJECTS_MODE}
	 */
	public void setMode(int mode) {
		if (currentMode != mode) {

			for (ToggleReferencesModeAction toggleReferencesModeAction : toggleReferencesModeActions) {
				toggleReferencesModeAction.setChecked(mode == toggleReferencesModeAction.getMode());
			}

			currentMode = mode;
			dialogSettings.put(STORE_MODE, mode);

			updateView();
		}
	}

	private void updateView() {
		if (viewInput != null) {
			showPage(PAGE_VIEWER);
			// Clear the Graph so that when we switch between view modes, we will start from an empty graph.
			graph.clear();
			viewer.setInput(getViewerInput());
			viewer.refresh();
		} else {
			showPage(PAGE_EMPTY);
		}
	}

	@Override
	public String getContributorId() {
		return ICommonModelUIConstants.VIEW_REFERENCES_ID;
	}

	protected class WrappedViewInput {

		private Object input;

		protected WrappedViewInput(Object input) {
			this.input = input;
		}

		public Object getInput() {
			return input;
		}
	}

	protected class DecorationAwareTransactionalAdapterFactoryLabelProvider extends TransactionalAdapterFactoryLabelProvider
			implements ILabelDecorator {

		private final ImageDescriptor RECURSIVE = Activator.getPlugin().getImageDescriptor("full/over16/recursive_ref.gif"); //$NON-NLS-1$

		public DecorationAwareTransactionalAdapterFactoryLabelProvider(TransactionalEditingDomain domain, AdapterFactory adapterFactory) {
			super(domain, adapterFactory);
		}

		@Override
		public Image getImage(Object object) {
			Image image = super.getImage(object);
			return decorateImage(image, object);
		}

		@Override
		public Image decorateImage(Image image, Object element) {
			if (image != null && element instanceof EObject && isRecursive(element)) {
				List<Image> images = new ArrayList<Image>();
				images.add(image);
				images.add(RECURSIVE.createImage());
				ComposedImage composedImage = new DecoratedComposedImage(images);
				return ExtendedImageRegistry.INSTANCE.getImage(composedImage);
			}
			return image;
		}

		@Override
		public String decorateText(String text, Object element) {
			return null;
		}
	}

	private static final class DecoratedComposedImage extends ComposedImage {
		private DecoratedComposedImage(Collection<?> images) {
			super(images);
		}

		@Override
		public List<Point> getDrawPoints(Size size) {
			List<Point> result = new ArrayList<Point>();
			result.add(new Point());
			Point overlay = new Point();
			overlay.y = 7;
			result.add(overlay);
			return result;
		}
	}
}