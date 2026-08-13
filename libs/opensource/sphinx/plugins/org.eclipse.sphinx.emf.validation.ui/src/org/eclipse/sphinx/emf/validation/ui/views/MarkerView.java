/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     See4sys - added support for problem markers on model objects (rather than
 *               only on workspace resources). Unfortunately, there was no other
 *               choice than copying the whole code from
 *               org.eclipse.ui.views.markers.internal for that purpose because
 *               many of the relevant classes, methods, and fields are private or
 *               package private.
 *******************************************************************************/
package org.eclipse.sphinx.emf.validation.ui.views;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.validation.ui.SphinxValidationUiActivator;
import org.eclipse.sphinx.emf.validation.ui.util.Messages;
import org.eclipse.sphinx.emf.validation.ui.views.MarkerAdapter.MarkerCategory;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.preferences.ViewPreferencesAction;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.views.tasklist.ITaskListResourceAdapter;

/**
 * MarkerView is the abstract super class of the marker based views.
 */
public abstract class MarkerView extends TableView {

	private static final String TAG_SELECTION = "selection"; //$NON-NLS-1$

	private static final String TAG_MARKER = "marker"; //$NON-NLS-1$

	private static final String TAG_RESOURCE = "resource"; //$NON-NLS-1$

	private static final String TAG_ID = "id"; //$NON-NLS-1$

	private static final String TAG_FILTERS_SECTION = "filters"; //$NON-NLS-1$

	private static final String TAG_FILTER_ENTRY = "filter"; //$NON-NLS-1$

	private static final String MENU_FILTERS_GROUP = "group.filter";//$NON-NLS-1$

	private static final String MENU_SHOW_IN_GROUP = "group.showIn";//$NON-NLS-1$

	// Section from a 3.1 or earlier workbench
	private static final String OLD_FILTER_SECTION = "filter"; //$NON-NLS-1$

	static final Object MARKER_UPDATE_FAMILY = new Object();

	class MarkerProcessJob extends Job {

		/**
		 * Create a new instance of the receiver.
		 */
		MarkerProcessJob() {
			super(MarkerMessages.MarkerView_processUpdates);
			setSystem(true);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			updateForContentsRefresh(monitor);
			return Status.OK_STATUS;

		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ui.progress.WorkbenchJob#shouldRun()
		 */
		@Override
		public boolean shouldRun() {
			// Do not run if the change came in before there is a viewer
			return PlatformUI.isWorkbenchRunning();
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
		 */
		@Override
		public boolean belongsTo(Object family) {
			return MARKER_UPDATE_FAMILY == family;
		}

	}

	MarkerProcessJob markerProcessJob = new MarkerProcessJob();

	private class UpdateJob extends WorkbenchJob {

		private class MarkerDescriptor {
			String description;

			String folder;

			String resource;

			int line;

			MarkerDescriptor(ConcreteMarker marker) {
				description = marker.getDescription();
				folder = marker.getFolder();
				resource = marker.getResourceName();
				line = marker.getLine();
			}

			boolean isEquivalentTo(ConcreteMarker marker) {
				return marker.getDescription().equals(description) && marker.getFolder().equals(folder) && marker.getResourceName().equals(resource)
						&& marker.getLine() == line;
			}

		}

		private Collection categoriesToExpand = new HashSet();

		private Collection preservedSelection = new ArrayList();

		UpdateJob() {
			super(MarkerMessages.MarkerView_queueing_updates);
			setSystem(true);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {

			if (getViewer().getControl().isDisposed()) {
				return Status.CANCEL_STATUS;
			}

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			getViewer().refresh(true);

			Tree tree = getTree();

			if (tree != null && !tree.isDisposed()) {
				updateStatusMessage();
				updateTitle();
				// Expand all if the list is small
				if (getCurrentMarkers().getSize() < 20) {
					getViewer().expandAll();
				} else {// Reexpand the old categories
					MarkerCategory[] categories = getMarkerAdapter().getCategories();
					if (categories == null) {
						categoriesToExpand.clear();
					} else {
						if (categories.length == 1) {// Expand if there is
							// only
							// one
							getViewer().expandAll();
							categoriesToExpand.clear();
							if (monitor.isCanceled()) {
								return Status.CANCEL_STATUS;
							}
							categoriesToExpand.add(categories[0].getName());
						} else {
							Collection newCategories = new HashSet();
							for (MarkerCategory category : categories) {
								if (monitor.isCanceled()) {
									return Status.CANCEL_STATUS;
								}
								if (categoriesToExpand.contains(category.getName())) {
									getViewer().expandToLevel(category, AbstractTreeViewer.ALL_LEVELS);
									newCategories.add(category.getName());
								}

							}
							categoriesToExpand = newCategories;
						}
					}

				}
			}

			if (preservedSelection.size() > 0) {

				Collection newSelection = new ArrayList();
				ConcreteMarker[] markers = getCurrentMarkers().toArray();

				for (ConcreteMarker element : markers) {
					Iterator preserved = preservedSelection.iterator();
					while (preserved.hasNext()) {
						MarkerDescriptor next = (MarkerDescriptor) preserved.next();
						if (next.isEquivalentTo(element)) {
							newSelection.add(element);
							continue;
						}
					}
				}

				getViewer().setSelection(new StructuredSelection(newSelection.toArray()), true);
				preservedSelection.clear();
			}
			if (getViewer().getTree().getItemCount() > 0) {
				getViewer().getTree().setTopItem(getViewer().getTree().getItem(0));
			}

			return Status.OK_STATUS;
		}

		/**
		 * Add the category to the list of expanded categories.
		 *
		 * @param category
		 */
		public void addExpandedCategory(MarkerCategory category) {
			categoriesToExpand.add(category.getName());

		}

		/**
		 * Remove the category from the list of expanded ones.
		 *
		 * @param category
		 */
		public void removeExpandedCategory(MarkerCategory category) {
			categoriesToExpand.remove(category.getName());

		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
		 */
		@Override
		public boolean belongsTo(Object family) {
			return family == MARKER_UPDATE_FAMILY;
		}

		/**
		 * Preserve the selection for reselection after the next update.
		 *
		 * @param selection
		 */
		public void saveSelection(ISelection selection) {
			preservedSelection.clear();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structured = (IStructuredSelection) selection;
				Iterator iterator = structured.iterator();
				while (iterator.hasNext()) {
					MarkerNode next = (MarkerNode) iterator.next();
					if (next.isConcrete()) {
						preservedSelection.add(new MarkerDescriptor(next.getConcreteRepresentative()));
					}
				}
			}

		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ui.progress.WorkbenchJob#shouldRun()
		 */
		@Override
		public boolean shouldRun() {
			return !getMarkerAdapter().isBuilding();
		}

	}

	private UpdateJob updateJob = new UpdateJob();

	// A private field for keeping track of the number of markers
	// before the busy testing started
	private int preBusyMarkers = 0;

	protected Object[] focusElements;

	private Clipboard clipboard;

	IResourceChangeListener markerUpdateListener = new IResourceChangeListener() {

		/*
		 * (non-Javadoc)
		 * @seeorg.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.
		 * IResourceChangeEvent)
		 */
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			if (!hasMarkerDelta(event)) {
				return;
			}

			if (event.getType() == IResourceChangeEvent.POST_BUILD) {
				scheduleMarkerUpdate(Util.SHORT_DELAY);
				return;
			}

			// After 30 seconds do updates anyways

			IWorkbenchSiteProgressService progressService = getProgressService();
			if (progressService == null) {
				markerProcessJob.schedule(Util.LONG_DELAY);
			} else {
				getProgressService().schedule(markerProcessJob, Util.LONG_DELAY);
			}

		}

		/**
		 * Returns whether or not the given even contains marker deltas for this view.
		 *
		 * @param event
		 *            the resource change event
		 * @return <code>true</code> if the event contains at least one relevant marker delta
		 * @since 0.7.0
		 */
		private boolean hasMarkerDelta(IResourceChangeEvent event) {
			String[] markerTypes = getMarkerTypes();
			for (String element : markerTypes) {
				if (event.findMarkerDeltas(element, true).length > 0) {
					return true;
				}
			}
			return false;
		}

	};

	private class ContextProvider implements IContextProvider {
		@Override
		public int getContextChangeMask() {
			return SELECTION;
		}

		@Override
		public IContext getContext(Object target) {
			String contextId = null;
			// See if there is a context registered for the current selection
			ConcreteMarker marker = getSelectedConcreteMarker();
			if (marker != null) {
				contextId = IDE.getMarkerHelpRegistry().getHelp(marker.getMarker());
			}

			if (contextId == null) {
				contextId = getStaticContextId();
			}
			return HelpSystem.getContext(contextId);
		}

		/**
		 * Return the currently selected concrete marker or <code>null</code> if there isn't one.
		 *
		 * @return ConcreteMarker
		 */
		private ConcreteMarker getSelectedConcreteMarker() {

			IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
			if (selection.isEmpty()) {
				return null;
			}

			if (selection.getFirstElement() instanceof ConcreteMarker) {
				return (ConcreteMarker) selection.getFirstElement();
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.help.IContextProvider#getSearchExpression(java.lang.Object)
		 */
		@Override
		public String getSearchExpression(Object target) {
			return null;
		}
	}

	private ContextProvider contextProvider = new ContextProvider();

	protected ActionCopyMarker copyAction;

	protected ActionPasteMarker pasteAction;

	protected SelectionProviderAction revealAction;

	protected SelectionProviderAction openAction;

	protected SelectionProviderAction deleteAction;

	protected SelectionProviderAction selectAllAction;

	protected SelectionProviderAction propertiesAction;

	protected UndoActionHandler undoAction;

	protected RedoActionHandler redoAction;

	private ISelectionListener focusListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			MarkerView.this.focusSelectionChanged(part, selection);
		}
	};

	private int totalMarkers = 0;

	private MarkerFilter[] markerFilters = new MarkerFilter[0];

	// A cache of the enabled filters
	private MarkerFilter[] enabledFilters = null;

	private MenuManager filtersMenu;

	private MenuManager showInMenu;

	private IPropertyChangeListener workingSetListener;

	private MarkerAdapter adapter;

	private IPropertyChangeListener preferenceListener;

	/**
	 * Create a new instance of the receiver,
	 */
	public MarkerView() {
		super();
		preferenceListener = new IPropertyChangeListener() {
			/*
			 * (non-Javadoc)
			 * @see
			 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
			 */
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(getFiltersPreferenceName())) {
					loadFiltersPreferences();
					clearEnabledFilters();
					refreshForFocusUpdate();
				}
			}
		};
		SphinxValidationUiActivator.getDefault().getPreferenceStore().addPropertyChangeListener(preferenceListener);
	}

	/**
	 * Get the current markers for the receiver.
	 *
	 * @return MarkerList
	 */
	public MarkerList getCurrentMarkers() {
		return getMarkerAdapter().getCurrentMarkers();
	}

	/**
	 * Get the marker adapter for the receiver.
	 *
	 * @return MarkerAdapter
	 */
	protected MarkerAdapter getMarkerAdapter() {
		return adapter;
	}

	/**
	 * Update for the change in the contents.
	 *
	 * @param monitor
	 */
	public void updateForContentsRefresh(IProgressMonitor monitor) {
		updateJob.cancel();
		getMarkerAdapter().buildAllMarkers(monitor);
		getProgressService().schedule(updateJob);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		IWorkbenchSiteProgressService progressService = getProgressService();
		if (progressService != null) {
			getProgressService().showBusyForFamily(ResourcesPlugin.FAMILY_MANUAL_BUILD);
			getProgressService().showBusyForFamily(ResourcesPlugin.FAMILY_AUTO_BUILD);
			getProgressService().showBusyForFamily(MARKER_UPDATE_FAMILY);
		}
		loadFiltersPreferences();

	}

	/**
	 * Load the filters preference.
	 */
	private void loadFiltersPreferences() {

		String preference = SphinxValidationUiActivator.getDefault().getPreferenceStore().getString(getFiltersPreferenceName());

		if (preference.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
			createDefaultFilter();
			return;
		}

		StringReader reader = new StringReader(preference);
		try {
			restoreFilters(XMLMemento.createReadRoot(reader));
		} catch (WorkbenchException e) {
			PlatformLogUtil.logAsError(SphinxValidationUiActivator.getDefault(), e);
		}

	}

	/**
	 * Update for filter changes. Save the preference and clear the enabled cache.
	 */
	void updateForFilterChanges() {

		XMLMemento memento = XMLMemento.createWriteRoot(TAG_FILTERS_SECTION);

		writeFiltersSettings(memento);

		StringWriter writer = new StringWriter();
		try {
			memento.save(writer);
		} catch (IOException e) {
			SphinxValidationUiActivator.getDefault().getLog().log(Util.errorStatus(e));
		}

		SphinxValidationUiActivator.getDefault().getPreferenceStore().putValue(getFiltersPreferenceName(), writer.toString());
		SphinxValidationUiActivator.getDefault().savePluginPreferences();

		clearEnabledFilters();
		refreshFilterMenu();
		refreshViewer();
	}

	/**
	 * Write the filter settings to the memento.
	 *
	 * @param memento
	 */
	protected void writeFiltersSettings(XMLMemento memento) {
		MarkerFilter[] filters = getUserFilters();
		for (MarkerFilter element : filters) {
			IMemento child = memento.createChild(TAG_FILTER_ENTRY, element.getName());
			element.saveFilterSettings(child);
		}
	}

	/**
	 * Get the name of the filters preference for instances of the receiver.
	 *
	 * @return String
	 */
	abstract String getFiltersPreferenceName();

	/**
	 * Restore the filters from the memento.
	 *
	 * @param memento
	 */
	void restoreFilters(IMemento memento) {

		IMemento[] sections = null;
		if (memento != null) {
			sections = memento.getChildren(TAG_FILTER_ENTRY);
		}

		if (sections == null) {
			// Check if we have an old filter setting around
			IDialogSettings mainSettings = getDialogSettings();
			IDialogSettings filtersSection = mainSettings.getSection(OLD_FILTER_SECTION);
			if (filtersSection != null) {
				MarkerFilter markerFilter = createFilter(MarkerMessages.MarkerFilter_defaultFilterName);
				markerFilter.restoreFilterSettings(filtersSection);
				setFilters(new MarkerFilter[] { markerFilter });
			}

		} else {
			MarkerFilter[] newFilters = new MarkerFilter[sections.length];

			for (int i = 0; i < sections.length; i++) {
				newFilters[i] = createFilter(sections[i].getID());
				newFilters[i].restoreState(sections[i]);
			}
			setFilters(newFilters);
		}

		if (markerFilters.length == 0) {// Make sure there is at least a default
			createDefaultFilter();
		}

	}

	/**
	 * Create a default filter for the receiver.
	 */
	private void createDefaultFilter() {
		MarkerFilter filter = createFilter(MarkerMessages.MarkerFilter_defaultFilterName);
		setFilters(new MarkerFilter[] { filter });
	}

	/**
	 * Create a filter called name.
	 *
	 * @param name
	 * @return MarkerFilter
	 */
	protected abstract MarkerFilter createFilter(String name);

	/**
	 * Return the memento tag for the receiver.
	 *
	 * @return String
	 */
	protected abstract String getSectionTag();

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {

		clipboard = new Clipboard(parent.getDisplay());
		super.createPartControl(parent);

		initDragAndDrop();

		getSite().getPage().addSelectionListener(focusListener);
		focusSelectionChanged(getSite().getPage().getActivePart(), getSite().getPage().getSelection());
		PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(getWorkingSetListener());

		// Set help on the view itself
		getViewer().getControl().addHelpListener(new HelpListener() {
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.HelpListener#helpRequested(org.eclipse.swt.events.HelpEvent)
			 */
			@Override
			public void helpRequested(HelpEvent e) {
				IContext context = contextProvider.getContext(getViewer().getControl());
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(context);
			}
		});

		// Hook up to the resource changes after all widget have been created
		ResourcesPlugin.getWorkspace().addResourceChangeListener(markerUpdateListener,
				IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.POST_BUILD);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adaptable) {
		if (adaptable.equals(IContextProvider.class)) {
			return contextProvider;
		}
		if (adaptable.equals(IShowInSource.class)) {
			return new IShowInSource() {
				@Override
				public ShowInContext getShowInContext() {
					IMarker[] selectedMarkers = getSelectedMarkers();
					return new ShowInContext(new StructuredSelection(retrieveModelObjects(selectedMarkers)), new StructuredSelection(
							retrieveResources(selectedMarkers)));
				}

			};
		}
		return super.getAdapter(adaptable);
	}

	private IResource[] retrieveResources(IMarker[] markers) {
		Set<IResource> resources = new HashSet<IResource>();
		for (IMarker marker : markers) {
			resources.add(marker.getResource());
		}
		return resources.toArray(new IResource[resources.size()]);
	}

	private Object[] retrieveModelObjects(IMarker[] markers) {
		// Retrieve model objects behind problem markers
		Set<Object> objects = new HashSet<Object>();
		for (IMarker marker : markers) {
			try {
				if (marker.isSubtypeOf(EValidator.MARKER)) {
					TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(marker.getResource());
					if (editingDomain != null) {
						String uriAttribute = marker.getAttribute(EValidator.URI_ATTRIBUTE, null);
						if (uriAttribute != null) {
							EObject object = EcorePlatformUtil.getEObject(editingDomain, URI.createURI(uriAttribute, true));
							if (object != null) {
								objects.add(object);
							}
						}
					}
				}
			} catch (Exception ex) {
				// Ignore exception, just continue with next marker
			}
		}
		return objects.toArray();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.views.markers.internal.TableView#viewerSelectionChanged(org.eclipse.jface.viewers.IStructuredSelection
	 * )
	 */
	@Override
	protected void viewerSelectionChanged(IStructuredSelection selection) {

		Object[] rawSelection = selection.toArray();

		List markers = new ArrayList();

		for (Object element : rawSelection) {

			if (element instanceof ConcreteMarker) {
				markers.add(((ConcreteMarker) element).getMarker());
			}
		}

		setSelection(new StructuredSelection(markers));

		updateStatusMessage(selection);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		cancelJobs();

		ResourcesPlugin.getWorkspace().removeResourceChangeListener(markerUpdateListener);
		PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(workingSetListener);
		SphinxValidationUiActivator.getDefault().getPreferenceStore().removePropertyChangeListener(preferenceListener);
		getSite().getPage().removeSelectionListener(focusListener);

		// dispose of selection provider actions (may not have been created yet
		// if createPartControls was never called)
		if (openAction != null) {
			openAction.dispose();
			copyAction.dispose();
			selectAllAction.dispose();
			deleteAction.dispose();
			revealAction.dispose();
			propertiesAction.dispose();
			undoAction.dispose();
			redoAction.dispose();
			clipboard.dispose();
		}
		if (showInMenu != null) {
			showInMenu.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#createActions()
	 */
	@Override
	protected void createActions() {
		revealAction = new ActionRevealMarker(this, getViewer());
		openAction = new ActionOpenMarker(this, getViewer());
		copyAction = new ActionCopyMarker(this, getViewer());
		copyAction.setClipboard(clipboard);
		copyAction.setProperties(getSortingFields());
		pasteAction = new ActionPasteMarker(this, getViewer(), getMarkerName());
		pasteAction.setClipboard(clipboard);
		pasteAction.setPastableTypes(getMarkerTypes());
		deleteAction = new ActionRemoveMarker(this, getViewer(), getMarkerName());
		selectAllAction = new ActionSelectAll(this);
		propertiesAction = new ActionMarkerProperties(this, getViewer(), getMarkerName());

		IUndoContext undoContext = getUndoContext();
		undoAction = new UndoActionHandler(getSite(), undoContext);
		redoAction = new RedoActionHandler(getSite(), undoContext);

		super.createActions();

		setFilterAction(new FiltersAction(this));

		setPreferencesAction(new ViewPreferencesAction() {
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.ui.preferences.ViewPreferencesAction#openViewPreferencesDialog()
			 */
			@Override
			public void openViewPreferencesDialog() {
				openPreferencesDialog(getMarkerEnablementPreferenceName(), getMarkerLimitPreferenceName());

			}

		});
	}

	/**
	 * Open a dialog to set the preferences.
	 *
	 * @param markerEnablementPreferenceName
	 * @param markerLimitPreferenceName
	 */
	private void openPreferencesDialog(String markerEnablementPreferenceName, String markerLimitPreferenceName) {

		Dialog dialog = new MarkerViewPreferenceDialog(getSite().getWorkbenchWindow().getShell(), markerEnablementPreferenceName,
				markerLimitPreferenceName, MarkerMessages.MarkerPreferences_DialogTitle);
		if (dialog.open() == Window.OK) {
			refreshViewer();
		}

	}

	/**
	 * Get the name of the marker enablement preference.
	 *
	 * @return String
	 */
	abstract String getMarkerLimitPreferenceName();

	abstract String[] getMarkerTypes();

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#initToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	@Override
	protected void initToolBar(IToolBarManager tbm) {
		tbm.add(deleteAction);
		tbm.add(getFilterAction());
		tbm.update(false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#registerGlobalActions(org.eclipse.ui.IActionBars)
	 */
	@Override
	protected void registerGlobalActions(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), pasteAction);
		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
		actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), selectAllAction);
		actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), propertiesAction);
		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);

		copyAction.setActionDefinitionId("org.eclipse.ui.edit.copy"); //$NON-NLS-1$
		pasteAction.setActionDefinitionId("org.eclipse.ui.edit.paste"); //$NON-NLS-1$
		deleteAction.setActionDefinitionId("org.eclipse.ui.edit.delete"); //$NON-NLS-1$
		selectAllAction.setActionDefinitionId("org.eclipse.ui.edit.selectAll"); //$NON-NLS-1$
		propertiesAction.setActionDefinitionId("org.eclipse.ui.file.properties"); //$NON-NLS-1$
		undoAction.setActionDefinitionId("org.eclipse.ui.edit.undo"); //$NON-NLS-1$
		redoAction.setActionDefinitionId("org.eclipse.ui.edit.redo"); //$NON-NLS-1$
	}

	protected void initDragAndDrop() {
		int operations = DND.DROP_COPY;
		Transfer[] transferTypes = new Transfer[] { MarkerTransfer.getInstance(), TextTransfer.getInstance() };
		DragSourceListener listener = new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				performDragSetData(event);
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
			}
		};

		getViewer().addDragSupport(operations, transferTypes, listener);
	}

	/**
	 * The user is attempting to drag marker data. Add the appropriate data to the event depending on the transfer type.
	 */
	private void performDragSetData(DragSourceEvent event) {
		if (MarkerTransfer.getInstance().isSupportedType(event.dataType)) {

			event.data = getSelectedMarkers();
			return;
		}
		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			List selection = ((IStructuredSelection) getViewer().getSelection()).toList();
			try {
				IMarker[] markers = new IMarker[selection.size()];
				selection.toArray(markers);
				if (markers != null) {
					event.data = copyAction.createMarkerReport(markers);
				}
			} catch (ArrayStoreException e) {
			}
		}
	}

	/**
	 * Get the array of selected markers.
	 *
	 * @return IMarker[]
	 */
	private IMarker[] getSelectedMarkers() {
		Object[] selection = ((IStructuredSelection) getViewer().getSelection()).toArray();
		List<IMarker> markers = new ArrayList<IMarker>();
		for (Object element : selection) {
			if (element instanceof ConcreteMarker) {
				markers.add(((ConcreteMarker) element).getMarker());
			}
		}
		return markers.toArray(new IMarker[markers.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillContextMenu(IMenuManager manager) {
		if (manager == null) {
			return;
		}
		manager.add(openAction);
		createShowInMenu(manager);
		manager.add(new Separator());
		manager.add(copyAction);
		pasteAction.updateEnablement();
		manager.add(pasteAction);

		if (canBeEditable()) {
			manager.add(deleteAction);
		}
		manager.add(selectAllAction);
		fillContextMenuAdditions(manager);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator());
		manager.add(propertiesAction);
	}

	/**
	 * Return whether or not any of the types in the receiver can be editable.
	 *
	 * @return <code>true</code> if it is possible to have an editable marker in this view.
	 */
	boolean canBeEditable() {
		return true;
	}

	/**
	 * Fill the context menu for the receiver.
	 *
	 * @param manager
	 */
	abstract void fillContextMenuAdditions(IMenuManager manager);

	/**
	 * Get the filters for the receiver.
	 *
	 * @return MarkerFilter[]
	 */
	protected final MarkerFilter[] getUserFilters() {
		return markerFilters;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#handleKeyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	@Override
	protected void handleKeyPressed(KeyEvent event) {
		// Default is do nothing.
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#handleOpenEvent(org.eclipse.jface.viewers.OpenEvent)
	 */
	@Override
	protected void handleOpenEvent(OpenEvent event) {
		if (openAction.isEnabled()) {
			openAction.run();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#saveSelection(org.eclipse.ui.IMemento)
	 */
	protected void saveSelection(IMemento memento) {
		IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
		IMemento selectionMem = memento.createChild(TAG_SELECTION);
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			Object next = iterator.next();
			if (!(next instanceof ConcreteMarker)) {
				continue;
			}
			ConcreteMarker marker = (ConcreteMarker) next;
			IMemento elementMem = selectionMem.createChild(TAG_MARKER);
			elementMem.putString(TAG_RESOURCE, marker.getMarker().getResource().getFullPath().toString());
			elementMem.putString(TAG_ID, String.valueOf(marker.getMarker().getId()));
		}
	}

	protected abstract String[] getRootTypes();

	/**
	 * @param part
	 * @param selection
	 */
	protected void focusSelectionChanged(IWorkbenchPart part, ISelection selection) {

		if (!(part instanceof CommonNavigator)) {
			return;
		}

		List selectedElements = new ArrayList();
		if (part instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart) part;
			IFile file = ResourceUtil.getFile(editor.getEditorInput());
			if (file == null) {
				IEditorInput editorInput = editor.getEditorInput();
				if (editorInput != null) {
					Object mapping = editorInput.getAdapter(ResourceMapping.class);
					if (mapping != null) {
						selectedElements.add(mapping);
					}
				}
			} else {
				selectedElements.add(file);
			}
		} else {
			if (selection instanceof IStructuredSelection) {
				for (Iterator iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext();) {
					Object object = iterator.next();
					if (object instanceof IAdaptable) {
						ITaskListResourceAdapter taskListResourceAdapter;
						Object adapter = ((IAdaptable) object).getAdapter(ITaskListResourceAdapter.class);
						if (adapter != null && adapter instanceof ITaskListResourceAdapter) {
							taskListResourceAdapter = (ITaskListResourceAdapter) adapter;
						} else {
							taskListResourceAdapter = DefaultMarkerResourceAdapter.getDefault();
						}

						IResource resource = taskListResourceAdapter.getAffectedResource((IAdaptable) object);
						if (resource == null) {
							Object mapping = ((IAdaptable) object).getAdapter(ResourceMapping.class);
							if (mapping != null) {
								selectedElements.add(mapping);
							}
						} else {
							selectedElements.add(resource);
						}
					} else if (object instanceof EObject) {
						selectedElements.add(object);
					} else if (object instanceof IWrapperItemProvider) {
						Object obj = ((IWrapperItemProvider) object).getValue();
						if (obj instanceof EObject) {
							selectedElements.add(obj);
						}
					}
				}
			}
		}

		updateFocusMarkers(selectedElements.toArray());
	}

	/**
	 * Update the focus resources of the filters.
	 *
	 * @param elements
	 */
	protected final void updateFilterSelection(Object[] elements) {

		Collection selectionCollection = new ArrayList();

		for (Object element : elements) {
			if (element instanceof IResource) {
				selectionCollection.add(element);
			} else if (element instanceof ResourceMapping) {
				addResources(selectionCollection, (ResourceMapping) element);
			} else if (element instanceof EObject) {
				selectionCollection.add(element);
			} else if (element instanceof IWrapperItemProvider) {
				selectionCollection.add(element);
			}
		}

		Object[] selections = new Object[selectionCollection.size()];
		selectionCollection.toArray(selections);

		for (MarkerFilter element : markerFilters) {
			element.setFocusSelection(selections);
		}

		Iterator systemFilters = MarkerSupportRegistry.getInstance().getRegisteredFilters().iterator();

		while (systemFilters.hasNext()) {
			MarkerFilter filter = (MarkerFilter) systemFilters.next();
			filter.setFocusSelection(selections);

		}
	}

	/**
	 * Add the resources for the mapping to resources.
	 *
	 * @param resources
	 * @param mapping
	 */
	private void addResources(Collection resources, ResourceMapping mapping) {
		try {
			ResourceTraversal[] traversals = mapping.getTraversals(ResourceMappingContext.LOCAL_CONTEXT, new NullProgressMonitor());
			for (ResourceTraversal traversal : traversals) {
				IResource[] result = traversal.getResources();
				for (IResource element : result) {
					resources.add(element);
				}
			}
		} catch (CoreException e) {
			Util.log(e);
			return;
		}

	}

	protected abstract String getStaticContextId();

	/**
	 * Update the focus markers for the supplied elements.
	 *
	 * @param elements
	 */
	protected void updateFocusMarkers(Object[] elements) {

		boolean updateNeeded = updateNeeded(focusElements, elements);
		if (updateNeeded) {
			focusElements = elements;
			refreshForFocusUpdate();
		}

	}

	private boolean updateNeeded(Object[] oldElements, Object[] newElements) {
		// determine if an update if refiltering is required
		MarkerFilter[] filters = getEnabledFilters();
		boolean updateNeeded = false;

		for (MarkerFilter filter : filters) {
			if (!filter.isEnabled()) {
				continue;
			}

			int onResource = filter.getOnResource();
			if (onResource == MarkerFilter.ON_ANY || onResource == MarkerFilter.ON_WORKING_SET) {
				continue;
			}
			if (newElements == null || newElements.length < 1) {
				continue;
			}
			if (oldElements == null || oldElements.length < 1) {
				return true;
			}
			if (Arrays.equals(oldElements, newElements)) {
				continue;
			}
			if (onResource == MarkerFilter.ON_ANY_IN_SAME_CONTAINER) {
				Collection oldProjects = MarkerFilter.getProjectsAsCollection(oldElements);
				Collection newProjects = MarkerFilter.getProjectsAsCollection(newElements);

				if (oldProjects.size() == newProjects.size()) {
					if (newProjects.containsAll(oldProjects)) {
						continue;
					}
				}

				return true;
			}
			updateNeeded = true;// We are updating as there is nothing to stop
			// us
		}

		return updateNeeded;
	}

	void updateTitle() {
		String status = Util.EMPTY_STRING;
		int filteredCount = getCurrentMarkers().getItemCount();
		int totalCount = getTotalMarkers();
		if (filteredCount == totalCount) {
			status = NLS.bind(MarkerMessages.filter_itemsMessage, new Integer(totalCount));
		} else {
			status = NLS.bind(MarkerMessages.filter_matchedMessage, new Integer(filteredCount), new Integer(totalCount));
		}
		setContentDescription(status);
	}

	/**
	 * Updates the message displayed in the status line. This method is invoked in the following cases:
	 * <ul>
	 * <li>when this view is first created</li>
	 * <li>when new elements are added</li>
	 * <li>when something is deleted</li>
	 * <li>when the filters change</li>
	 * </ul>
	 * <p>
	 * By default, this method calls <code>updateStatusMessage(IStructuredSelection)</code> with the current selection
	 * or <code>null</code>. Classes wishing to override this functionality, should just override the method
	 * <code>updateStatusMessage(IStructuredSelection)</code>.
	 * </p>
	 */
	protected void updateStatusMessage() {
		ISelection selection = getViewer().getSelection();

		if (selection instanceof IStructuredSelection) {
			updateStatusMessage((IStructuredSelection) selection);
		} else {
			updateStatusMessage(null);
		}
	}

	/**
	 * Updates that message displayed in the status line. If the selection parameter is <code>null</code> or its size is
	 * 0, the status area is blanked out. If only 1 marker is selected, the status area is updated with the contents of
	 * the message attribute of this marker. In other cases (more than one marker is selected) the status area indicates
	 * how many items have been selected.
	 * <p>
	 * This method may be overwritten.
	 * </p>
	 * <p>
	 * This method is called whenever a selection changes in this view.
	 * </p>
	 *
	 * @param selection
	 *            a valid selection or <code>null</code>
	 */
	protected void updateStatusMessage(IStructuredSelection selection) {
		String message = ""; //$NON-NLS-1$

		if (selection == null || selection.size() == 0) {
			// Show stats on all items in the view
			message = updateSummaryVisible();
		} else if (selection.size() == 1) {
			// Use the Message attribute of the marker
			Object first = selection.getFirstElement();
			if (first instanceof ConcreteMarker) {
				message = ((ConcreteMarker) first).getDescription();
			}
		} else if (selection.size() > 1) {
			// Show stats on only those items in the selection
			message = updateSummarySelected(selection);
		}
		getViewSite().getActionBars().getStatusLineManager().setMessage(message);
	}

	/**
	 * @param selection
	 * @return the summary status message
	 */
	protected String updateSummarySelected(IStructuredSelection selection) {
		// Show how many items selected
		return MessageFormat.format(MarkerMessages.marker_statusSummarySelected, new Object[] { new Integer(selection.size()) });
	}

	/**
	 * @return the update summary
	 */
	protected String updateSummaryVisible() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Open a dialog on the filters
	 */
	public final void openFiltersDialog() {

		DialogMarkerFilter dialog = createFiltersDialog();

		if (dialog.open() == Window.OK) {

			MarkerFilter[] result = dialog.getFilters();
			if (result == null) {
				return;
			}
			if (result.length == 0) {
				setFilters(new MarkerFilter[] { createFilter(MarkerMessages.MarkerFilter_defaultFilterName) });
			} else {
				setFilters(result);
			}

			updateForFilterChanges();
		}
	}

	/**
	 * Refresh the contents of the viewer.
	 */
	public void refreshViewer() {
		scheduleMarkerUpdate(Util.SHORT_DELAY);
	}

	/**
	 * Set the filters to newFilters.
	 *
	 * @param newFilters
	 */
	void setFilters(MarkerFilter[] newFilters) {
		markerFilters = newFilters;
	}

	/**
	 * Clear the cache of enabled filters.
	 */
	void clearEnabledFilters() {
		enabledFilters = null;
	}

	/**
	 * Refresh the contents of the filter sub menu.
	 */
	private void refreshFilterMenu() {
		if (filtersMenu == null) {
			return;
		}
		filtersMenu.removeAll();
		MarkerFilter[] filters = getAllFilters();
		for (MarkerFilter element : filters) {
			filtersMenu.add(new FilterEnablementAction(element, this));
		}

	}

	/**
	 * Open a filter dialog on the receiver.
	 */
	protected abstract DialogMarkerFilter createFiltersDialog();

	/**
	 * Given a selection of IMarker, reveals the corresponding elements in the viewer
	 *
	 * @param structuredSelection
	 * @param reveal
	 */
	public void setSelection(IStructuredSelection structuredSelection, boolean reveal) {
		TreeViewer viewer = getViewer();

		List newSelection = new ArrayList(structuredSelection.size());

		for (Iterator i = structuredSelection.iterator(); i.hasNext();) {
			Object next = i.next();
			if (next instanceof IMarker) {
				ConcreteMarker marker = getCurrentMarkers().getMarker((IMarker) next);
				if (marker != null) {
					newSelection.add(marker);
				}
			}
		}

		if (viewer != null) {
			viewer.setSelection(new StructuredSelection(newSelection), reveal);
		}
	}

	protected MarkerList getVisibleMarkers() {
		return getCurrentMarkers();
	}

	/**
	 * Returns the total number of markers. Should not be called while the marker list is still updating.
	 *
	 * @return the total number of markers in the workspace (including everything that doesn't pass the filters)
	 */
	int getTotalMarkers() {
		// The number of visible markers should never exceed the total number of
		// markers in
		// the workspace. If this assertation fails, it probably indicates some
		// sort of concurrency problem
		// (most likely, getTotalMarkers was called while we were still
		// computing the marker lists)
		// Assert.isTrue(totalMarkers >= currentMarkers.getItemCount());

		return totalMarkers;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#showBusy(boolean)
	 */
	@Override
	public void showBusy(boolean busy) {
		super.showBusy(busy);

		if (busy) {
			preBusyMarkers = totalMarkers;
		} else {// Only bold if there has been a change in count
			if (totalMarkers != preBusyMarkers) {
				getProgressService().warnOfContentChange();
			}
		}

	}

	/**
	 * Get the filters that are currently enabled.
	 *
	 * @return MarkerFilter[]
	 */
	protected MarkerFilter[] getEnabledFilters() {

		if (enabledFilters == null) {
			Collection filters = findEnabledFilters();

			enabledFilters = new MarkerFilter[filters.size()];
			filters.toArray(enabledFilters);
		}
		return enabledFilters;

	}

	/**
	 * Find the filters enabled in the view.
	 *
	 * @return Collection of MarkerFilter
	 */
	protected Collection findEnabledFilters() {
		MarkerFilter[] allFilters = getAllFilters();
		ArrayList filters = new ArrayList(0);
		for (MarkerFilter element : allFilters) {
			if (element.isEnabled()) {
				filters.add(element);
			}
		}
		return filters;
	}

	/**
	 * Get all of the filters applied to the receiver.
	 *
	 * @return MarkerFilter[]
	 */
	MarkerFilter[] getAllFilters() {
		return getUserFilters();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.views.markers.internal.TableView#addDropDownContributions(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	void addDropDownContributions(IMenuManager menu) {
		super.addDropDownContributions(menu);

		menu.add(new Separator(MENU_FILTERS_GROUP));
		// Don't add in the filters until they are set
		filtersMenu = new MenuManager(MarkerMessages.filtersSubMenu_title);
		refreshFilterMenu();
		menu.appendToGroup(MENU_FILTERS_GROUP, filtersMenu);
	}

	private class ExtendedContributionItemFactory extends ContributionItemFactory {

		private String targetObjectURI = null;

		public ExtendedContributionItemFactory(String contributionItemId) {
			super(contributionItemId);
		}

		public void setTargetObject(String uri) {
			targetObjectURI = uri;
		}

		/* (non-javadoc) method declared on ContributionItemFactory */
		@Override
		public IContributionItem create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			return new ShowInMenu(window, getId(), targetObjectURI);
		}
	}

	/**
	 * Create the show in menu if there is a single selection.
	 *
	 * @param menu
	 */
	void createShowInMenu(IMenuManager menu) {
		ISelection selection = getViewer().getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		IStructuredSelection structured = (IStructuredSelection) selection;
		if (!Util.isSingleConcreteSelection(structured)) {
			return;
		}

		menu.add(new Separator(MENU_SHOW_IN_GROUP));
		// Don't add in the filters until they are set

		String showInLabel = Messages._UI_Workbench_showIn;
		IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getAdapter(IBindingService.class);
		if (bindingService != null) {
			String keyBinding = bindingService.getBestActiveBindingFormattedFor("org.eclipse.ui.navigate.showInQuickMenu"); //$NON-NLS-1$
			if (keyBinding != null) {
				showInLabel += '\t' + keyBinding;
			}
		}

		ProblemMarker marker = (ProblemMarker) structured.getFirstElement();
		String targetObjectURI = null;
		try {
			targetObjectURI = (String) marker.getMarker().getAttribute(EValidator.URI_ATTRIBUTE);
		} catch (CoreException ex) {

		}

		showInMenu = new MenuManager(showInLabel);
		if (targetObjectURI == null || targetObjectURI.equals("")) {
			// don't filter views if targetObject is not defined
			showInMenu.add(ContributionItemFactory.VIEWS_SHOW_IN.create(getViewSite().getWorkbenchWindow()));
		} else {
			ExtendedContributionItemFactory contributionItemFactory = new ExtendedContributionItemFactory("extendedViewsShowIn"); //$NON-NLS-1$
			contributionItemFactory.setTargetObject(targetObjectURI);
			showInMenu.add(contributionItemFactory.create(getViewSite().getWorkbenchWindow()));
		}

		menu.appendToGroup(MENU_SHOW_IN_GROUP, showInMenu);

	}

	/**
	 * Refresh the marker counts
	 *
	 * @param monitor
	 */
	void refreshMarkerCounts(IProgressMonitor monitor) {
		monitor.subTask(MarkerMessages.MarkerView_refreshing_counts);
		try {
			totalMarkers = MarkerList.compute(getMarkerTypes()).length;
		} catch (CoreException e) {
			Util.log(e);
			return;
		}

	}

	/**
	 * Returns the marker limit or -1 if unlimited
	 *
	 * @return int
	 */
	int getMarkerLimit() {

		// If limits are enabled return it. Otherwise return -1
		if (IDEWorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(getMarkerEnablementPreferenceName())) {
			return IDEWorkbenchPlugin.getDefault().getPreferenceStore().getInt(getMarkerLimitPreferenceName());

		}
		return -1;

	}

	/**
	 * Get the name of the marker limit preference.
	 *
	 * @return String
	 */
	abstract String getMarkerEnablementPreferenceName();

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.TableView#createViewerInput()
	 */
	@Override
	Object createViewerInput() {
		adapter = new MarkerAdapter(this);
		return adapter;
	}

	/**
	 * Add a listener for the end of the update.
	 *
	 * @param listener
	 */
	public void addUpdateFinishListener(IJobChangeListener listener) {
		updateJob.addJobChangeListener(listener);

	}

	/**
	 * Remove a listener for the end of the update.
	 *
	 * @param listener
	 */
	public void removeUpdateFinishListener(IJobChangeListener listener) {
		updateJob.removeJobChangeListener(listener);

	}

	/**
	 * Create a listener for working set changes.
	 *
	 * @return IPropertyChangeListener
	 */
	private IPropertyChangeListener getWorkingSetListener() {
		workingSetListener = new IPropertyChangeListener() {
			/*
			 * (non-Javadoc)
			 * @see
			 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
			 */
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				clearEnabledFilters();
				refreshViewer();

			}
		};
		return workingSetListener;
	}

	/**
	 * Schedule an update of the markers with a delay of time
	 *
	 * @param time
	 */
	void scheduleMarkerUpdate(int time) {
		cancelJobs();
		getProgressService().schedule(markerProcessJob, time);
	}

	/**
	 * Cancel the pending jobs in the receiver.
	 */
	private void cancelJobs() {
		markerProcessJob.cancel();
		updateJob.cancel();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.TableView#createTree(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Tree createTree(Composite parent) {
		Tree tree = super.createTree(parent);
		tree.addTreeListener(new TreeAdapter() {
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.TreeAdapter#treeCollapsed(org.eclipse.swt.events.TreeEvent)
			 */
			@Override
			public void treeCollapsed(TreeEvent e) {
				updateJob.removeExpandedCategory((MarkerCategory) e.item.getData());
			}

			/*
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.TreeAdapter#treeExpanded(org.eclipse.swt.events.TreeEvent)
			 */
			@Override
			public void treeExpanded(TreeEvent e) {
				updateJob.addExpandedCategory((MarkerCategory) e.item.getData());
			}
		});

		return tree;
	}

	/**
	 * The focus elements have changed. Update accordingly.
	 */
	private void refreshForFocusUpdate() {
		if (focusElements != null) {
			updateFilterSelection(focusElements);
			refreshViewer();
		}
	}

	/**
	 * Save the current selection in the update for reselection after update.
	 */
	protected void preserveSelection() {
		updateJob.saveSelection(getViewer().getSelection());

	}

	/**
	 * Return the string name of the specific type of marker shown in this view.
	 */
	protected abstract String getMarkerName();

	/**
	 * Return the undo context associated with operations performed in this view. By default, return the workspace undo
	 * context. Subclasses should override if a more specific undo context should be used.
	 */
	protected IUndoContext getUndoContext() {
		return (IUndoContext) ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
	}

}
