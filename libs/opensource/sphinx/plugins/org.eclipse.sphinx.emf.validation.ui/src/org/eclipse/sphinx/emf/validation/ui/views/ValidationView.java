/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     See4sys - copied from org.eclipse.ui.views.markers.internal.ProblemView to
 *               add support for problem markers on model objects (rather than
 *               only on workspace resources)
 *	   Elektrobit - [572591]  Make MarkerResolutionDialog extensible
 *******************************************************************************/
package org.eclipse.sphinx.emf.validation.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.validation.markers.IValidationMarker;
import org.eclipse.sphinx.emf.validation.ui.SphinxValidationUiActivator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import com.ibm.icu.text.MessageFormat;

/**
 * The Validation view is the view that displays problem markers on model objects.
 * <p>
 * Unfortunately, there was no other choice than copying the whole code from ProblemView for that purpose because most
 * of the relevant methods and fields of the latter are private or package private.
 * </p>
 */
public class ValidationView extends MarkerView {

	private final static String[] ROOT_TYPES = { IValidationMarker.MODEL_VALIDATION_PROBLEM, IMarker.PROBLEM };

	private final static String TAG_DIALOG_SECTION = "org.eclipse.sphinx.emf.validation.ui"; //$NON-NLS-1$

	private static final String TAG_SYSTEM_FILTER_ENTRY = "systemFilter";//$NON-NLS-1$

	private ActionResolveMarker resolveMarkerAction;

	private IHandlerService handlerService;

	private IHandlerActivation resolveMarkerHandlerActivation;

	private IActivityManagerListener activityManagerListener;

	private IField severityAndMessage = new FieldSeverityAndMessage();

	private IField eObject = new FieldEObject();

	private IField eObjectURI = new FieldEObjectURI();

	private IField eObjectType = new FieldEObjectType();

	private IField resource = new FieldResource();

	private IField ruleId = new FieldRuleId();

	private IField creationTime = new FieldCreationTime();

	// Add the marker ID so the table sorter won't reduce
	// errors on the same line bug 82502
	private static IField id = new FieldId();

	private class GroupingAction extends Action {

		IField groupingField;

		ValidationView problemView;

		/**
		 * Create a new instance of the receiver.
		 *
		 * @param label
		 * @param field
		 * @param view
		 */
		public GroupingAction(String label, IField field, ValidationView view) {
			super(label, IAction.AS_RADIO_BUTTON);

			groupingField = field;
			problemView = view;
			IField categoryField = view.getMarkerAdapter().getCategorySorter().getCategoryField();
			if (categoryField == null) {
				setChecked(groupingField == null);
			} else {
				boolean state = categoryField.equals(groupingField);
				if (groupingField != null) {
					state |= categoryField.getDescription().equals(groupingField.getDescription());
				}
				setChecked(state);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
		public void run() {

			if (isChecked()) {
				Job categoryJob = new Job(MarkerMessages.ProblemView_UpdateCategoryJob) {
					/*
					 * (non-Javadoc)
					 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
					 */
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							markerProcessJob.join();
						} catch (InterruptedException e) {
							return Status.CANCEL_STATUS;
						}
						problemView.selectCategoryField(groupingField, problemView.getMarkerAdapter().getCategorySorter());

						getMarkerAdapter().getCategorySorter().saveState(getDialogSettings());
						return Status.OK_STATUS;
					}
				};
				categoryJob.setSystem(true);
				problemView.preserveSelection();

				IWorkbenchSiteProgressService progressService = getProgressService();
				if (progressService == null) {
					categoryJob.schedule();
				} else {
					getProgressService().schedule(categoryJob);
				}

			}

		}
	}

	/**
	 * Return a new instance of the receiver.
	 */
	public ValidationView() {
		super();
		creationTime.setShowing(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#dispose()
	 */
	@Override
	public void dispose() {
		if (resolveMarkerAction != null) {
			resolveMarkerAction.dispose();
		}
		if (resolveMarkerHandlerActivation != null && handlerService != null) {
			handlerService.deactivateHandler(resolveMarkerHandlerActivation);
		}

		PlatformUI.getWorkbench().getActivitySupport().getActivityManager().removeActivityManagerListener(activityManagerListener);
		super.dispose();
	}

	// /*
	// * @see org.eclipse.sphinx.emf.validation.ui.views.MarkerView#getAdapter(java.lang.Class)
	// */
	// @Override
	// public Object getAdapter(Class adaptable) {
	// if (adaptable.equals(IShowInSource.class)) {
	// return new IShowInSource() {
	// @Override
	// public ShowInContext getShowInContext() {
	// ISelection selection = getViewer().getSelection();
	// if (!(selection instanceof IStructuredSelection)) {
	// return null;
	// }
	// IStructuredSelection structured = (IStructuredSelection) selection;
	// Iterator markerIterator = structured.iterator();
	// List newSelection = new ArrayList();
	// List selectedMarkers = new ArrayList();
	// while (markerIterator.hasNext()) {
	// Object obj = markerIterator.next();
	// if (obj instanceof ConcreteMarker) {
	// ConcreteMarker element = (ConcreteMarker) obj;
	// newSelection.add(element.getResource());
	// selectedMarkers.add(element.getMarker());
	// }
	// }
	// return new ShowInContext(new StructuredSelection(selectedMarkers), new StructuredSelection(newSelection));
	// }
	//
	// };
	// }
	// return super.getAdapter(adaptable);
	// }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.TableView#getSortingFields()
	 */
	@Override
	protected IField[] getSortingFields() {
		return new IField[] { severityAndMessage, eObject, eObjectURI, resource, eObjectType, ruleId, creationTime,
				// Add the marker ID so the table sorter won't reduce
				// errors on the same line bug 82502
				id };
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.TableView#getDialogSettings()
	 */
	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings workbenchSettings = SphinxValidationUiActivator.getDefault().getDialogSettings();
		IDialogSettings settings = workbenchSettings.getSection(TAG_DIALOG_SECTION);

		if (settings == null) {
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);
		}

		return settings;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.TableView#createActions()
	 */
	@Override
	protected void createActions() {
		super.createActions();
		propertiesAction = new ActionProblemProperties(this, getViewer());
		resolveMarkerAction = createActionResolveMarker(this, getViewer());
	}

	/**
	 * @param markerView
	 * @param provider
	 * @return the resolve marker action
	 */
	protected ActionResolveMarker createActionResolveMarker(MarkerView markerView, ISelectionProvider provider) {
		return new ActionResolveMarker(markerView, provider);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#registerGlobalActions(org.eclipse.ui.IActionBars)
	 */
	@Override
	protected void registerGlobalActions(IActionBars actionBars) {
		super.registerGlobalActions(actionBars);

		String quickFixId = "org.eclipse.jdt.ui.edit.text.java.correction.assist.proposals"; //$NON-NLS-1$
		resolveMarkerAction.setActionDefinitionId(quickFixId);

		handlerService = (IHandlerService) getViewSite().getService(IHandlerService.class);
		if (handlerService != null) {
			resolveMarkerHandlerActivation = handlerService.activateHandler(quickFixId, new ActionHandler(resolveMarkerAction));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.views.markers.internal.MarkerView#fillContextMenuAdditions(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillContextMenuAdditions(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(resolveMarkerAction);
	}

	@Override
	protected String[] getRootTypes() {
		return ROOT_TYPES;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.TableView#getAllFields()
	 */
	@Override
	protected IField[] getAllFields() {

		// Add the marker ID so the table sorter won't reduce
		// errors on the same line bug 82502
		return new IField[] { severityAndMessage, eObject, eObjectURI, resource, eObjectType, ruleId, creationTime };
	}

	@Override
	void updateTitle() {
		MarkerList visibleMarkers = getVisibleMarkers();
		String breakdown = formatSummaryBreakDown(visibleMarkers);
		int filteredCount = visibleMarkers.getItemCount();
		int totalCount = getTotalMarkers();
		if (filteredCount != totalCount) {
			breakdown = NLS.bind(MarkerMessages.problem_filter_matchedMessage, new Object[] { breakdown, new Integer(filteredCount),
					new Integer(totalCount) });
		}
		setContentDescription(breakdown);
	}

	private String formatSummaryBreakDown(MarkerList visibleMarkers) {
		return MessageFormat.format(MarkerMessages.problem_statusSummaryBreakdown, new Object[] { new Integer(visibleMarkers.getErrors()),
				new Integer(visibleMarkers.getWarnings()), new Integer(visibleMarkers.getInfos()) });
	}

	private String getSummary(MarkerList markers) {
		String message = MessageFormat.format(MarkerMessages.marker_statusSummarySelected, new Object[] { new Integer(markers.getItemCount()),
				formatSummaryBreakDown(markers) });
		return message;
	}

	/**
	 * Retrieves statistical information (the total number of markers with each severity type) for the markers contained
	 * in the selection passed in. This information is then massaged into a string which may be displayed by the caller.
	 *
	 * @param selection
	 *            a valid selection or <code>null</code>
	 * @return a message ready for display
	 */
	@Override
	protected String updateSummarySelected(IStructuredSelection selection) {
		Collection selectionList;

		selectionList = new ArrayList();
		Iterator selectionIterator = selection.iterator();
		while (selectionIterator.hasNext()) {
			MarkerNode next = (MarkerNode) selectionIterator.next();
			if (next.isConcrete()) {
				selectionList.add(next);
			}
		}

		return getSummary(new MarkerList(selectionList));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerTypes()
	 */
	@Override
	protected String[] getMarkerTypes() {
		return new String[] { IMarker.PROBLEM };
	}

	@Override
	protected String getStaticContextId() {
		return PlatformUI.PLUGIN_ID + ".problem_view_context";//$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#createFiltersDialog()
	 */
	@Override
	protected DialogMarkerFilter createFiltersDialog() {

		MarkerFilter[] filters = getUserFilters();
		ProblemFilter[] problemFilters = new ProblemFilter[filters.length];
		System.arraycopy(filters, 0, problemFilters, 0, filters.length);
		return new DialogProblemFilter(getSite().getShell(), problemFilters);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#createFilter(java.lang.String)
	 */
	@Override
	protected MarkerFilter createFilter(String name) {
		return new ProblemFilter(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getSectionTag()
	 */
	@Override
	protected String getSectionTag() {
		return TAG_DIALOG_SECTION;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerEnablementPreferenceName()
	 */
	@Override
	String getMarkerEnablementPreferenceName() {
		return IDEInternalPreferences.LIMIT_PROBLEMS;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerLimitPreferenceName()
	 */
	@Override
	String getMarkerLimitPreferenceName() {
		return IDEInternalPreferences.PROBLEMS_LIMIT;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getFiltersPreferenceName()
	 */
	@Override
	String getFiltersPreferenceName() {
		return IDEInternalPreferences.PROBLEMS_FILTERS;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getAllFilters()
	 */
	@Override
	MarkerFilter[] getAllFilters() {
		MarkerFilter[] userFilters = super.getAllFilters();
		Collection declaredFilters = MarkerSupportRegistry.getInstance().getRegisteredFilters();
		Iterator iterator = declaredFilters.iterator();

		MarkerFilter[] allFilters = new MarkerFilter[userFilters.length + declaredFilters.size()];
		System.arraycopy(userFilters, 0, allFilters, 0, userFilters.length);
		int index = userFilters.length;

		while (iterator.hasNext()) {
			allFilters[index] = (MarkerFilter) iterator.next();
			index++;
		}
		return allFilters;

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.views.markers.internal.MarkerView#addDropDownContributions(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	void addDropDownContributions(IMenuManager menu) {

		MenuManager groupByMenu = new MenuManager(MarkerMessages.ProblemView_GroupByMenu);
		groupByMenu.add(new GroupingAction(MarkerMessages.ProblemView_EObject, new FieldEObject(), this));
		groupByMenu.add(new GroupingAction(MarkerMessages.ProblemView_EObjectType, new FieldEObjectType(), this));
		groupByMenu.add(new GroupingAction(MarkerMessages.ProblemView_Type, new FieldCategory(), this));
		groupByMenu.add(new GroupingAction(MarkerMessages.ProblemView_RuleId, new FieldRuleId(), this));

		Iterator definedGroups = MarkerSupportRegistry.getInstance().getMarkerGroups().iterator();
		while (definedGroups.hasNext()) {
			Object o = definedGroups.next();
			if (o instanceof FieldMarkerGroup) {
				FieldMarkerGroup group = (FieldMarkerGroup) o;
				if (group.getId().equals("org.eclipse.ui.ide.severity")) {
					groupByMenu.add(new GroupingAction(group.getDescription(), group, this));
				}
			}
		}

		groupByMenu.add(new GroupingAction(MarkerMessages.ProblemView_None, null, this));
		menu.add(groupByMenu);

		super.addDropDownContributions(menu);
	}

	/**
	 * Resize the category column in the table.
	 */
	protected void regenerateLayout() {
		TableLayout layout = new TableLayout();
		getViewer().getTree().setLayout(layout);

		ColumnLayoutData[] columnWidths = getDefaultColumnLayouts();
		for (ColumnLayoutData element : columnWidths) {
			layout.addColumnData(element);

		}
		getViewer().getTree().layout(true);

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.TableView#setSorter(org.eclipse.ui.views.markers.internal.TableSorter)
	 */
	@Override
	void setComparator(TableComparator sorter2) {
		getMarkerAdapter().getCategorySorter().setTableSorter(sorter2);
		getMarkerAdapter().getCategorySorter().saveState(getDialogSettings());
		updateForNewComparator(sorter2);

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getTableSorter()
	 */
	@Override
	public TableComparator getTableSorter() {
		return ((CategoryComparator) getViewer().getComparator()).innerSorter;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		createActivityManagerListener();
		PlatformUI.getWorkbench().getActivitySupport().getActivityManager().addActivityManagerListener(activityManagerListener);
	}

	/**
	 * Create a new listener for activity changes.
	 */
	private void createActivityManagerListener() {
		activityManagerListener = new IActivityManagerListener() {
			/*
			 * (non-Javadoc)
			 * @seeorg.eclipse.ui.activities.IActivityManagerListener#activityManagerChanged(org.eclipse.ui.activities.
			 * ActivityManagerEvent)
			 */
			@Override
			public void activityManagerChanged(ActivityManagerEvent activityManagerEvent) {
				clearEnabledFilters();
				refreshViewer();
			}
		};

	}

	/**
	 * Return the field whose description matches description.
	 *
	 * @param description
	 * @return IField
	 */
	public IField findField(String description) {
		IField[] fields = getSortingFields();
		for (IField element : fields) {
			if (element.getDescription().equals(description)) {
				return element;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.TableView#buildComparator()
	 */
	@Override
	protected ViewerComparator buildComparator() {

		TableComparator sorter = createTableComparator();
		CategoryComparator category = new CategoryComparator(sorter);
		category.restoreState(getDialogSettings(), this);
		return category;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#canBeEditable()
	 */
	@Override
	boolean canBeEditable() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#initToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	@Override
	protected void initToolBar(IToolBarManager tbm) {
		tbm.add(getFilterAction());
		tbm.update(false);
	}

	/**
	 * Select the category for the receiver.
	 *
	 * @param description
	 * @param sorter
	 *            - the sorter to select for
	 */
	public void selectCategory(String description, CategoryComparator sorter) {

		if (description == null) {
			selectCategoryField(null, sorter);
		}

		if (description.equals(MarkerMessages.description_eObject)) {
			selectCategoryField(new FieldEObject(), sorter);
			return;
		} else if (description.equals(MarkerMessages.description_eObjectType)) {
			selectCategoryField(new FieldEObjectType(), sorter);
			return;
		} else if (description.equals(MarkerMessages.description_type)) {
			selectCategoryField(new FieldCategory(), sorter);
			return;
		} else if (description.equals(MarkerMessages.description_ruleId)) {
			selectCategoryField(new FieldRuleId(), sorter);
			return;
		}

		Iterator definedGroups = MarkerSupportRegistry.getInstance().getMarkerGroups().iterator();
		while (definedGroups.hasNext()) {
			// FieldMarkerGroup group = (FieldMarkerGroup) definedGroups.next();
			IField group = (IField) definedGroups.next();
			if (group.getDescription().equals(description)) {
				selectCategoryField(group, sorter);
				return;
			}
		}
		selectCategoryField(null, sorter);

	}

	/**
	 * Select the field groupingField.
	 *
	 * @param groupingField
	 * @param sorter
	 */
	void selectCategoryField(IField groupingField, CategoryComparator sorter) {
		sorter.setCategoryField(groupingField);

		// Do not refresh if the input has not been set yet
		if (getMarkerAdapter() != null) {
			getMarkerAdapter().getCurrentMarkers().clearGroups();
			refreshViewer();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#writeFiltersSettings(org.eclipse.ui.XMLMemento)
	 */
	@Override
	protected void writeFiltersSettings(XMLMemento memento) {
		super.writeFiltersSettings(memento);

		// Add the system filters
		Iterator filters = MarkerSupportRegistry.getInstance().getRegisteredFilters().iterator();

		while (filters.hasNext()) {
			MarkerFilter filter = (MarkerFilter) filters.next();
			IMemento child = memento.createChild(TAG_SYSTEM_FILTER_ENTRY, filter.getName());
			child.putString(MarkerFilter.TAG_ENABLED, String.valueOf(filter.isEnabled()));
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#restoreFilters(org.eclipse.ui.IMemento)
	 */
	@Override
	void restoreFilters(IMemento memento) {

		super.restoreFilters(memento);

		if (memento == null) {
			return;
		}

		IMemento[] sections = memento.getChildren(TAG_SYSTEM_FILTER_ENTRY);

		Collection registered = MarkerSupportRegistry.getInstance().getRegisteredFilters();
		MarkerFilter[] filters = new MarkerFilter[registered.size()];
		registered.toArray(filters);

		if (sections != null) {

			for (IMemento element : sections) {
				String filterName = element.getID();
				boolean enabled = Boolean.valueOf(element.getString(MarkerFilter.TAG_ENABLED)).booleanValue();
				setEnablement(filterName, enabled, filters);

			}
		}

	}

	/**
	 * Set the enablement state of the filter called filterName to enabled.
	 *
	 * @param filterName
	 * @param enabled
	 * @param filters
	 */
	private void setEnablement(String filterName, boolean enabled, MarkerFilter[] filters) {
		for (MarkerFilter element : filters) {
			if (element.getName().equals(filterName)) {
				element.setEnabled(enabled);
				return;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerName()
	 */
	@Override
	protected String getMarkerName() {
		return MarkerMessages.problem_title;
	}
}
