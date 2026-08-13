/**
 * <copyright>
 *
 * Copyright (c) 2008-2016 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [420520] Model explorer view state not restored completely when affected model objects are added lately
 *     itemis - [458862] Navigation from problem markers in Check Validation view to model editors and Model Explorer view broken
 *     itemis - [460260] Expanded paths are collapsed on resource reload
 *     itemis - [478725] Enable model elements hold by ordered features to be displayed with their native order in Common Navigator-based views
 *     itemis - [480135] Introduce metamodel and view content agnostic problem decorator for model elements
 *     itemis - [481581] Improve refresh behavior of BasicModelContentProvider to avoid performance problems due to needlessly repeated tree state restorations
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.explorer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.ui.viewer.IViewerProvider;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.EMFCommandOperation;
import org.eclipse.emf.workspace.IWorkspaceCommandStack;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.domain.factory.EditingDomainFactoryListenerRegistry;
import org.eclipse.sphinx.emf.domain.factory.ITransactionalEditingDomainFactoryListener;
import org.eclipse.sphinx.emf.explorer.internal.Activator;
import org.eclipse.sphinx.emf.explorer.sorters.NonFinalCommonViewerSorter;
import org.eclipse.sphinx.emf.explorer.sorters.OrderedAwareCommonViewerSorter;
import org.eclipse.sphinx.emf.messages.EMFMessages;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.emf.workspace.domain.WorkspaceEditingDomainManager;
import org.eclipse.sphinx.emf.workspace.internal.saving.ModelSavingPerformanceStats;
import org.eclipse.sphinx.emf.workspace.internal.saving.ModelSavingPerformanceStats.ModelEvent;
import org.eclipse.sphinx.emf.workspace.ui.saving.BasicModelSaveablesProvider;
import org.eclipse.sphinx.emf.workspace.ui.saving.BasicModelSaveablesProvider.SiteNotifyingSaveablesLifecycleListener;
import org.eclipse.sphinx.emf.workspace.ui.viewers.state.ITreeViewerState;
import org.eclipse.sphinx.emf.workspace.ui.viewers.state.TreeViewerStateRecorder;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.SaveablesProvider;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * Extends the behavior of the Eclipse {@linkplain CommonNavigator Common Navigator}.
 */
@SuppressWarnings("restriction")
public class ExtendedCommonNavigator extends CommonNavigator
		implements ITabbedPropertySheetPageContributor, IViewerProvider, ITransactionalEditingDomainFactoryListener {

	private IOperationHistoryListener affectedObjectsListener;

	protected Set<IPropertySheetPage> propertySheetPages = new HashSet<IPropertySheetPage>();
	protected IUndoContext undoContext;

	protected SaveablesProvider modelSaveablesProvider;

	protected IPartListener partListener = new IPartListener() {

		@Override
		public void partActivated(IWorkbenchPart part) {
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
			if (part instanceof PropertySheet) {
				((PropertySheet) part).getCurrentPage().dispose();
				propertySheetPages.remove(((PropertySheet) part).getCurrentPage());
			}
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
		}
	};

	protected TreeViewerStateRecorder treeViewerStateRecorder = new TreeViewerStateRecorder();

	protected ITreeViewerState deferredViewerState = null;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);

		if (modelSaveablesProvider == null) {
			modelSaveablesProvider = createModelSaveablesProvider();

			ISaveablesLifecycleListener modelSaveablesLifecycleListener = createModelSaveablesLifecycleListener();
			modelSaveablesProvider.init(modelSaveablesLifecycleListener);
		}

		site.getPage().addPartListener(partListener);

		// Create one ResourceListener that detects objects changed by EMF commands
		affectedObjectsListener = createAffectedObjectsListener();
		Assert.isNotNull(affectedObjectsListener);

		// Register this as an Editing Domain Factory Listener (dynamically)
		EditingDomainFactoryListenerRegistry.INSTANCE.addListener(MetaModelDescriptorRegistry.ANY_MM, null, this, null);

		// Register listener on already created editing domains
		for (TransactionalEditingDomain editingDomain : WorkspaceEditingDomainManager.INSTANCE.getEditingDomainMapping().getEditingDomains()) {
			// Register the ResourceListener that detects objects changed by EMF commands
			WorkspaceTransactionUtil.getOperationHistory(editingDomain).addOperationHistoryListener(affectedObjectsListener);
		}
	}

	@Override
	protected CommonViewer createCommonViewerObject(Composite aParent) {
		CommonViewer viewer = new CommonViewer(getViewSite().getId(), aParent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL) {

			@Override
			protected void init() {
				super.init();

				// Enable label decorators to access the underlying viewer's content provider
				DecoratingStyledCellLabelProvider labelProvider = (DecoratingStyledCellLabelProvider) getLabelProvider();
				DecorationContext decorationContext = new DecorationContext();
				decorationContext.putProperty(ITreeContentProvider.class.getName(), getContentProvider());
				labelProvider.setDecorationContext(decorationContext);
			}

			/*
			 * Overridden to apply remaining deferred viewer state
			 * @see org.eclipse.ui.navigator.CommonViewer#refresh(java.lang.Object, boolean)
			 */
			@Override
			public void refresh(Object element, boolean updateLabels) {
				super.refresh(element, updateLabels);

				/*
				 * !! Important Note !! Don't override #refresh() to apply the deferred viewer state there as well. This
				 * is actually needless and counterproductive because #refresh() delegates to #refresh(Object, boolean)
				 * and therefore would result in 2 consecutive tentatives to apply the same deferred viewer state.
				 */
				treeViewerStateRecorder.applyState(deferredViewerState);
				deferredViewerState = treeViewerStateRecorder.getDeferredState();
			}

			/*
			 * Overridden to make sure that NonFinalCommonViewerSorter gets initialized in the same way as original
			 * CommonViewerSorter
			 * @see org.eclipse.ui.navigator.CommonViewer#setSorter(org.eclipse.jface.viewers.ViewerSorter)
			 */
			@Override
			public void setSorter(ViewerSorter sorter) {
				if (sorter != null && sorter instanceof NonFinalCommonViewerSorter) {
					((NonFinalCommonViewerSorter) sorter).setContentService(getNavigatorContentService());
				}
				super.setSorter(sorter);
			}
		};

		treeViewerStateRecorder.setViewer(viewer);

		return viewer;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		// Replace default sorter by one that honors the "ordered" information of the Ecore feature behind the
		// collection holding the elements to be sorted
		getCommonViewer().setSorter(new OrderedAwareCommonViewerSorter());

		// Give action providers a chance to initialize retargetable actions
		/*
		 * !! Important Note !! It is crucial to do this as early as possible because the connected
		 * TabbedPropertySheetPage will try to retrieve the retargetable undo and redo actions from this view part's
		 * action bars and set the same on its own action bars (see TabbedPropertySheetPage#setActionBars() for
		 * details). A failure to so will entail that the undo/redo actions in the Edit menu remain grayed out.
		 */
		getNavigatorActionService().fillActionBars(getViewSite().getActionBars());

		// Perform a view part level restore state
		/*
		 * !! Important Note !! CommonNavigator#createPartControl() also invokes restoreState() but only via the
		 * navigator content service. This entails that restoreState() is eventually only invoked on the subset of
		 * common content providers which are currently active according to the current selection and the triggerPoints
		 * condition on the org.eclipse.ui.navigator.navigatorContent/navigatorContent contribution. In contrast to
		 * that, the additional view part level restoreState() invocation implemented here takes always place and does
		 * not depend on the activation state of contributed common content providers.
		 */
		restoreState(memento);
	}

	@Override
	public boolean isDirty() {
		Saveable[] saveables = getActiveSaveables();
		for (Saveable element : saveables) {
			if (element.isDirty()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Saveable[] getSaveables() {
		Set<Saveable> saveables = new HashSet<Saveable>(Arrays.asList(super.getSaveables()));
		if (modelSaveablesProvider != null) {
			saveables.addAll(Arrays.asList(modelSaveablesProvider.getSaveables()));
		}
		return saveables.toArray(new Saveable[saveables.size()]);
	}

	@Override
	public Saveable[] getActiveSaveables() {
		String contextGetActiveSaveables = ModelSavingPerformanceStats.ModelContext.CONTEXT_GET_ACTIVE_SAVEABLES.getName()
				+ this.getClass().getSimpleName();
		ModelEvent modelEvent = ModelSavingPerformanceStats.ModelEvent.EVENT_GET_SAVEABLE;

		// Open profiling context
		ModelSavingPerformanceStats.INSTANCE.openContext(contextGetActiveSaveables);
		ModelSavingPerformanceStats.INSTANCE.startNewEvent(modelEvent, this.getClass().getSimpleName());

		Set<Saveable> saveables = new HashSet<Saveable>(Arrays.asList(super.getActiveSaveables()));
		if (modelSaveablesProvider != null) {
			CommonViewer viewer = getCommonViewer();
			if (viewer != null) {
				if (viewer.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					for (Object selected : selection.toList()) {
						if (selected instanceof IContainer) {
							IContainer container = (IContainer) selected;
							Collection<IModelDescriptor> modelDescriptors = ModelDescriptorRegistry.INSTANCE.getModels(container);
							for (IModelDescriptor modelDescriptor : modelDescriptors) {
								Saveable saveable = modelSaveablesProvider.getSaveable(modelDescriptor);
								if (saveable != null) {
									saveables.add(saveable);
								}
							}
						} else {
							Saveable saveable = modelSaveablesProvider.getSaveable(selected);
							if (saveable != null) {
								saveables.add(saveable);
							}
						}
					}
				}
			} else {
				saveables.addAll(Arrays.asList(modelSaveablesProvider.getSaveables()));
			}
		}

		// Close and log profiling context;
		ModelSavingPerformanceStats.INSTANCE.endEvent(modelEvent, this.getClass().getSimpleName());
		ModelSavingPerformanceStats.INSTANCE.closeAndLogContext(contextGetActiveSaveables);
		return saveables.toArray(new Saveable[saveables.size()]);
	}

	@Override
	public String getContributorId() {
		return getViewSite().getId();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (IPropertySheetPage.class == adapter) {
			return getPropertySheetPage();
		} else if (adapter == IUndoContext.class) {
			return getUndoContext();
		}
		return super.getAdapter(adapter);
	}

	/*
	 * @see org.eclipse.emf.common.ui.viewer.IViewerProvider#getViewer()
	 */
	@Override
	public Viewer getViewer() {
		return getCommonViewer();
	}

	public TreeViewerStateRecorder getViewerStateRecorder() {
		return treeViewerStateRecorder;
	}

	@Override
	public void dispose() {
		if (modelSaveablesProvider != null) {
			modelSaveablesProvider.dispose();
		}
		modelSaveablesProvider = null;

		getSite().getPage().removePartListener(partListener);
		for (IPropertySheetPage propertySheetPage : propertySheetPages) {
			propertySheetPage.dispose();
		}
		propertySheetPages.clear();

		if (affectedObjectsListener != null) {
			for (TransactionalEditingDomain editingDomain : WorkspaceEditingDomainManager.INSTANCE.getEditingDomainMapping().getEditingDomains()) {
				WorkspaceTransactionUtil.getOperationHistory(editingDomain).removeOperationHistoryListener(affectedObjectsListener);
			}
			// Unregister this as an Editing Domain Factory Listener (dynamically)
			EditingDomainFactoryListenerRegistry.INSTANCE.removeListener(this);
		}
		affectedObjectsListener = null;

		super.dispose();
	}

	public void restoreState(IMemento memento) {
		treeViewerStateRecorder.restoreState(memento);
		deferredViewerState = treeViewerStateRecorder.getDeferredState();
	}

	@Override
	public void saveState(IMemento memento) {
		treeViewerStateRecorder.saveState(memento);
		super.saveState(memento);
	}

	/*
	 * @see org.eclipse.ui.navigator.CommonNavigator#show(org.eclipse.ui.part.ShowInContext)
	 */
	@Override
	public boolean show(ShowInContext context) {
		// Check if the show in context's input transports a selection
		Object input = context.getInput();
		if (input instanceof IStructuredSelection) {
			// Reveal and select model objects
			try {
				StructuredSelection selection = (StructuredSelection) input;
				selectReveal(selection);
				return true;
			} catch (Exception ex) {
				// Ignore exception, just return false
				return false;
			}
		}

		try {
			return super.show(context);
		} catch (Exception ex) {
			// Ignore exception, just return false
			return false;
		}
	}

	protected Collection<TransactionalEditingDomain> getEditingDomainsFromSelection() {
		// Editing domains to return
		Set<TransactionalEditingDomain> editingDomains = new HashSet<TransactionalEditingDomain>();

		CommonViewer viewer = getCommonViewer();
		if (viewer != null) {
			ISelection selection = viewer.getSelection();
			if (selection instanceof IStructuredSelection) {
				for (Object selected : ((IStructuredSelection) selection).toList()) {
					if (selected instanceof EObject && ((EObject) selected).eIsProxy()) {
						PlatformLogUtil.logAsWarning(Activator.getPlugin(),
								new RuntimeException(NLS.bind(EMFMessages.warning_selectionContainsUnresolvedModelElement, selected)));
					} else if (selected instanceof IProject || selected instanceof IFolder) {
						editingDomains.addAll(WorkspaceEditingDomainUtil.getEditingDomains((IContainer) selected));
					} else {
						TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(selected);
						if (editingDomain != null) {
							editingDomains.add(editingDomain);
						}
					}
				}
			}
		}
		return editingDomains;
	}

	/**
	 * This creates a new property sheet page instance and manages it in the cache.
	 */
	public IPropertySheetPage getPropertySheetPage() {
		IPropertySheetPage propertySheetPage = new TabbedPropertySheetPage(this);
		propertySheetPages.add(propertySheetPage);
		return propertySheetPage;
	}

	protected IUndoContext getUndoContext() {
		if (undoContext == null) {
			undoContext = new ObjectUndoContext(this, getContributorId() + ".context"); //$NON-NLS-1$
		}
		return undoContext;
	}

	protected boolean isActivePart() {
		IWorkbenchPartSite site = getSite();
		return site != null ? this == site.getWorkbenchWindow().getPartService().getActivePart() : false;
	}

	protected boolean isMyActivePropertySheetPage() {
		IWorkbenchPartSite site = getSite();
		if (site != null) {
			IWorkbenchPart activePart = site.getWorkbenchWindow().getPartService().getActivePart();
			if (activePart instanceof PropertySheet) {
				return propertySheetPages.contains(((PropertySheet) activePart).getCurrentPage());
			}
		}
		return false;
	}

	protected SaveablesProvider createModelSaveablesProvider() {
		return new BasicModelSaveablesProvider();
	}

	protected IOperationHistoryListener createAffectedObjectsListener() {
		return new IOperationHistoryListener() {
			@Override
			public void historyNotification(final OperationHistoryEvent event) {
				if (event.getEventType() == OperationHistoryEvent.ABOUT_TO_EXECUTE) {
					handleOperationAboutToExecute(event.getOperation());
				} else if (event.getEventType() == OperationHistoryEvent.DONE || event.getEventType() == OperationHistoryEvent.UNDONE
						|| event.getEventType() == OperationHistoryEvent.REDONE) {
					handleOperationFinished(event.getOperation());
				}
			}

			private void handleOperationAboutToExecute(final IUndoableOperation operation) {
				if (operation.canUndo()) {
					IWorkbenchPartSite site = getSite();
					if (site != null) {
						site.getShell().getDisplay().syncExec(new Runnable() {
							@Override
							public void run() {
								if (isActivePart() || isMyActivePropertySheetPage()) {
									for (TransactionalEditingDomain editingDomain : getEditingDomainsFromSelection()) {
										// Remove default undo context and add the global undo context
										CommandStack commandStack = editingDomain.getCommandStack();
										if (commandStack instanceof IWorkspaceCommandStack) {
											IUndoContext undoContext = ((IWorkspaceCommandStack) commandStack).getDefaultUndoContext();
											if (undoContext != null) {
												operation.removeContext(undoContext);
											}
										}
									}
									operation.addContext(getUndoContext());
								}
							}
						});
					}
				}
			}

			private void handleOperationFinished(final IUndoableOperation operation) {
				IWorkbenchPartSite site = getSite();
				if (site != null) {
					site.getShell().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							// Try to show the affected objects in viewer
							if (operation instanceof EMFCommandOperation) {
								Command command = ((EMFCommandOperation) operation).getCommand();
								if (command != null) {
									Collection<?> affectedObjects = command.getAffectedObjects();
									if (affectedObjects.size() > 0) {
										try {
											ISelection selection = new StructuredSelection(affectedObjects.toArray());
											selectReveal(selection);
										} catch (RuntimeException ex) {
											// Ignore exception
										}
									}
								}
							}
						}
					});
				}
			}
		};
	}

	/**
	 * @return
	 */
	protected ISaveablesLifecycleListener createModelSaveablesLifecycleListener() {
		return new SiteNotifyingSaveablesLifecycleListener(this) {
			@Override
			public void handleLifecycleEvent(SaveablesLifecycleEvent event) {
				super.handleLifecycleEvent(event);

				if (event.getEventType() == SaveablesLifecycleEvent.DIRTY_CHANGED) {
					firePropertyChange(PROP_DIRTY);
				}
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void postCreateEditingDomain(TransactionalEditingDomain editingDomain) {
		// Register the ResourceListener that detects objects changed by EMF commands
		WorkspaceTransactionUtil.getOperationHistory(editingDomain).addOperationHistoryListener(affectedObjectsListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preDisposeEditingDomain(TransactionalEditingDomain editingDomain) {
		if (affectedObjectsListener != null) {
			WorkspaceTransactionUtil.getOperationHistory(editingDomain).removeOperationHistoryListener(affectedObjectsListener);
		}
	}
}
