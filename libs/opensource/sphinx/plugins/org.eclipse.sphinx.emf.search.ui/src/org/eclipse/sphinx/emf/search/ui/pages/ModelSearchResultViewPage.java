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
package org.eclipse.sphinx.emf.search.ui.pages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.ui.viewer.IViewerProvider;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.RemoveAllEvent;
import org.eclipse.search2.internal.ui.SearchView;
import org.eclipse.sphinx.emf.search.ui.MatchEvent;
import org.eclipse.sphinx.emf.search.ui.ModelSearchMatch;
import org.eclipse.sphinx.emf.search.ui.ModelSearchResult;
import org.eclipse.sphinx.emf.search.ui.actions.CollapseAllAction;
import org.eclipse.sphinx.emf.search.ui.actions.ExpandAllAction;
import org.eclipse.sphinx.emf.search.ui.actions.RemoveAllMatchesAction;
import org.eclipse.sphinx.emf.search.ui.actions.RemoveMatchAction;
import org.eclipse.sphinx.emf.search.ui.actions.RemoveSelectedMatchesAction;
import org.eclipse.sphinx.emf.search.ui.actions.SelectAllAction;
import org.eclipse.sphinx.emf.search.ui.actions.SetLayoutAction;
import org.eclipse.sphinx.emf.search.ui.actions.ShowNextResultAction;
import org.eclipse.sphinx.emf.search.ui.actions.ShowPreviousResultAction;
import org.eclipse.sphinx.emf.search.ui.internal.Activator;
import org.eclipse.sphinx.emf.search.ui.internal.INavigate;
import org.eclipse.sphinx.emf.search.ui.internal.TableViewerNavigator;
import org.eclipse.sphinx.emf.search.ui.internal.TreeViewerNavigator;
import org.eclipse.sphinx.emf.search.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.search.ui.providers.AbstractModelSearchContentProvider;
import org.eclipse.sphinx.emf.search.ui.providers.ModelSearchLabelProvider;
import org.eclipse.sphinx.emf.search.ui.providers.ModelSearchTreeContentProvider;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.OpenAndLinkWithEditorHelper;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

@SuppressWarnings("restriction")
public class ModelSearchResultViewPage extends Page implements ISearchResultPage, ITabbedPropertySheetPageContributor, IAdaptable {

	protected static final ModelSearchMatch[] EMPTY_MATCH_ARRAY = new ModelSearchMatch[0];

	protected Set<IPropertySheetPage> propertySheetPages = new HashSet<IPropertySheetPage>();

	private String pageId;

	private boolean isBusyShown;
	private boolean batchedClearAll;

	private int currentLayout;
	private final int supportedLayouts;
	private int currentMatchIndex = 0;
	private Integer elementLimit;

	private Composite viewerContainer;
	private Control busyLabel;
	private MenuManager menu;
	private StructuredViewer viewer;
	private PageBook pagebook;

	private ModelSearchResult input;
	private ISearchResultViewPart viewPart;
	private IQueryListener searchQueryListener;
	private SelectionProviderAdapter viewerAdapter;
	private ISearchResultListener searchResultListener;

	private AbstractModelSearchContentProvider contentProvider;
	private ModelSearchLabelProvider labelProvider;

	private Set<Object> batchedUpdates;

	/**
	 * Actions
	 */
	private RemoveAllMatchesAction removeAllMatchesAction;
	private RemoveSelectedMatchesAction removeSelectedMatches;
	private RemoveMatchAction removeCurrentMatch;
	private ShowNextResultAction showNextAction;
	private ShowPreviousResultAction showPreviousAction;
	private ExpandAllAction expandAllAction;
	private CollapseAllAction collapseAllAction;
	private SelectAllAction selectAllAction;

	private SetLayoutAction flatAction;
	private SetLayoutAction hierarchicalAction;

	private volatile boolean isUIUpdateScheduled = false;
	private volatile boolean scheduleEnsureSelection = false;
	private static final String KEY_LAYOUT = "org.eclipse.sphinx.emf.search.ui.resultpage.layout"; //$NON-NLS-1$

	/**
	 * Flag (<code>value 1</code>) denoting flat list layout.
	 */
	public static final int FLAG_LAYOUT_FLAT = 1;

	/**
	 * Flag (<code>value 2</code>) denoting tree layout.
	 */
	public static final int FLAG_LAYOUT_TREE = 2;

	/**
	 * This constructor must be passed a combination of layout flags combined with bitwise or. At least one flag must be
	 * passed in (i.e. 0 is not a permitted value).
	 *
	 * @param supportedLayouts
	 *            flags determining which layout options this page supports. Must not be 0
	 * @see #FLAG_LAYOUT_FLAT
	 * @see #FLAG_LAYOUT_TREE
	 */
	public ModelSearchResultViewPage(int supportedLayouts) {
		this.supportedLayouts = supportedLayouts;
		initLayout();
		removeAllMatchesAction = new RemoveAllMatchesAction(this);
		removeSelectedMatches = new RemoveSelectedMatchesAction(this);
		removeCurrentMatch = new RemoveMatchAction(this);
		showNextAction = new ShowNextResultAction(this);
		showPreviousAction = new ShowPreviousResultAction(this);
		// copyToClipboardAction = new CopyToClipboardAction();
		if ((supportedLayouts & FLAG_LAYOUT_TREE) != 0) {
			expandAllAction = new ExpandAllAction();
			collapseAllAction = new CollapseAllAction();
		}

		selectAllAction = new SelectAllAction();
		createLayoutActions();
		batchedUpdates = new HashSet<Object>();
		batchedClearAll = false;

		searchResultListener = new ISearchResultListener() {
			@Override
			public void searchResultChanged(SearchResultEvent e) {
				handleSearchResultChanged(e);
			}
		};
		elementLimit = null;
	}

	public ModelSearchResultViewPage() {
		this(FLAG_LAYOUT_FLAT | FLAG_LAYOUT_TREE);
	}

	@Override
	public Object getUIState() {
		return viewer.getSelection();
	}

	@Override
	public void setInput(ISearchResult newSearch, Object uiState) {
		if (newSearch != null && !(newSearch instanceof ModelSearchResult)) {
			return; // ignore
		}

		ModelSearchResult oldSearch = input;
		if (oldSearch != null) {
			disconnectViewer();
			oldSearch.removeListener(searchResultListener);
		}
		input = (ModelSearchResult) newSearch;

		if (input != null) {
			input.addListener(searchResultListener);
			connectViewer(input);
			if (uiState instanceof ISelection) {
				viewer.setSelection((ISelection) uiState, true);
			} else {
				navigateNext(true);
			}

			updateBusyLabel();
			turnOffDecoration();
			scheduleUIUpdate();

		} else {
			getViewPart().updateLabel();
		}
	}

	private void updateBusyLabel() {
		ModelSearchResult result = getInput();
		boolean shouldShowBusy = result != null && NewSearchUI.isQueryRunning(result.getQuery()) && result.getMatchCount() == 0;
		if (shouldShowBusy == isBusyShown) {
			return;
		}
		isBusyShown = shouldShowBusy;
		showBusyLabel(isBusyShown);
	}

	private void initLayout() {
		if (supportsTreeLayout()) {
			currentLayout = FLAG_LAYOUT_TREE;
		} else {
			currentLayout = FLAG_LAYOUT_FLAT;
		}
	}

	private void createLayoutActions() {
		if (countBits(supportedLayouts) > 1) {
			flatAction = new SetLayoutAction(this, Messages.ModelSearchResultViewPage_flat_layout_label,
					Messages.ModelSearchResultViewPage_flat_layout_tooltip, FLAG_LAYOUT_FLAT);
			hierarchicalAction = new SetLayoutAction(this, Messages.ModelSearchResultViewPage_hierarchical_layout_label,
					Messages.ModelSearchResultViewPage_hierarchical_layout_tooltip, FLAG_LAYOUT_TREE);
			SearchPluginImages.setImageDescriptors(flatAction, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_FLAT_LAYOUT);
			SearchPluginImages.setImageDescriptors(hierarchicalAction, SearchPluginImages.T_LCL,
					SearchPluginImages.IMG_LCL_SEARCH_HIERARCHICAL_LAYOUT);
		}
	}

	private int countBits(int layoutFlags) {
		int bitCount = 0;
		for (int i = 0; i < 32; i++) {
			if (layoutFlags % 2 == 1) {
				bitCount++;
			}
			layoutFlags >>= 1;
		}
		return bitCount;
	}

	private boolean supportsTreeLayout() {
		return isLayoutSupported(FLAG_LAYOUT_TREE);
	}

	/**
	 * Determines whether a certain layout is supported by this search result page.
	 *
	 * @param layout
	 *            the layout to test for
	 * @return whether the given layout is supported or not
	 * @see ModelSearchResultViewPage#ModelSearchResultViewPage(int)
	 */
	public boolean isLayoutSupported(int layout) {
		return (layout & supportedLayouts) == layout;
	}

	private void showBusyLabel(boolean shouldShowBusy) {
		if (shouldShowBusy) {
			pagebook.showPage(busyLabel);
		} else {
			pagebook.showPage(viewerContainer);
		}
	}

	private void connectViewer(ModelSearchResult search) {
		viewer.setInput(search);
	}

	private void disconnectViewer() {
		viewer.setInput(null);
	}

	/**
	 * Selects the element corresponding to the next match and shows the match in an editor. Note that this will cycle
	 * back to the first match after the last match.
	 */
	public void gotoNextMatch() {
		gotoNextMatch(false);
	}

	private void gotoNextMatch(boolean activateEditor) {
		currentMatchIndex++;
		ModelSearchMatch nextMatch = getCurrentMatch();
		if (nextMatch == null) {
			navigateNext(true);
			currentMatchIndex = 0;
		}
		showCurrentMatch(activateEditor);
	}

	/**
	 * Selects the element corresponding to the previous match and shows the match in an editor. Note that this will
	 * cycle back to the last match after the first match.
	 */
	public void gotoPreviousMatch() {
		gotoPreviousMatch(false);
	}

	private void gotoPreviousMatch(boolean activateEditor) {
		currentMatchIndex--;
		ModelSearchMatch nextMatch = getCurrentMatch();
		if (nextMatch == null) {
			navigateNext(false);
			currentMatchIndex = getDisplayedMatchCount(getFirstSelectedElement()) - 1;
		}
		showCurrentMatch(activateEditor);
	}

	private void navigateNext(boolean forward) {
		INavigate navigator = null;
		if (viewer instanceof TableViewer) {
			navigator = new TableViewerNavigator((TableViewer) viewer);
		} else {
			navigator = new TreeViewerNavigator(this, (TreeViewer) viewer);
		}
		navigator.navigateNext(forward);
	}

	/**
	 * Returns the currently shown result.
	 *
	 * @return the previously set result or <code>null</code>
	 * @see ModelSearchResultViewPage#setInput(ISearchResult, Object)
	 */
	public ModelSearchResult getInput() {
		return input;
	}

	public int getDisplayedMatchCount(Object element) {
		ModelSearchResult result = getInput();
		if (result == null) {
			return 0;
		}
		// TODO aakar we have to remove filtered matches from the count when we add filters
		ModelSearchMatch[] matches = result.getMatches(element);
		return matches.length;
	}

	/**
	 * Returns the view part set with <code>setViewPart(ISearchResultViewPart)</code>.
	 *
	 * @return The view part or <code>null</code> if the view part hasn't been set yet (or set to null).
	 */
	protected ISearchResultViewPart getViewPart() {
		return viewPart;
	}

	@Override
	public void setViewPart(ISearchResultViewPart part) {
		viewPart = part;
	}

	@Override
	public void restoreState(IMemento memento) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveState(IMemento memento) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setID(String id) {
		pageId = id;
	}

	@Override
	public String getID() {
		return pageId;
	}

	@Override
	public String getLabel() {
		ModelSearchResult result = getInput();
		if (result == null) {
			return ""; //$NON-NLS-1$
		}
		return result.getLabel();
	}

	/**
	 * Returns a dialog settings object for this search result page. There will be one dialog settings object per search
	 * result page id.
	 *
	 * @return the dialog settings for this search result page
	 * @see ModelSearchResultViewPage#getID()
	 */
	protected IDialogSettings getSettings() {
		IDialogSettings parent = Activator.getDefault().getDialogSettings();
		IDialogSettings settings = parent.getSection(getID());
		if (settings == null) {
			settings = parent.addNewSection(getID());
		}
		return settings;
	}

	/**
	 * Returns the viewer currently used in this page.
	 *
	 * @return the currently used viewer or <code>null</code> if none has been created yet.
	 */
	public StructuredViewer getViewer() {
		return viewer;
	}

	/**
	 * Note: this is internal API and should not be called from clients outside of the search plug-in.
	 * <p>
	 * Removes the currently selected match. Does nothing if no match is selected.
	 * </p>
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void internalRemoveSelected() {
		ModelSearchResult result = getInput();
		if (result == null) {
			return;
		}
		StructuredViewer viewer = getViewer();
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

		Set<ModelSearchMatch> set = new HashSet<ModelSearchMatch>();
		if (viewer instanceof TreeViewer) {
			ITreeContentProvider cp = (ITreeContentProvider) viewer.getContentProvider();
			collectAllMatchesBelow(result, set, cp, selection.toArray());
		} else {
			collectAllMatches(set, selection.toArray());
		}
		navigateNext(true);

		ModelSearchMatch[] matches = new ModelSearchMatch[set.size()];
		set.toArray(matches);
		result.removeMatches(matches);
	}

	private void collectAllMatches(Set<ModelSearchMatch> set, Object[] elements) {
		for (Object element : elements) {
			ModelSearchMatch[] matches = getDisplayedMatches(element);
			for (ModelSearchMatch matche : matches) {
				set.add(matche);
			}
		}
	}

	private void collectAllMatchesBelow(ModelSearchResult result, Set<ModelSearchMatch> set, ITreeContentProvider cp, Object[] elements) {
		for (Object element : elements) {
			ModelSearchMatch[] matches = getDisplayedMatches(element);
			for (ModelSearchMatch matche : matches) {
				set.add(matche);
			}
			Object[] children = cp.getChildren(element);
			collectAllMatchesBelow(result, set, cp, children);
		}
	}

	@Override
	public void createControl(Composite parent) {
		searchQueryListener = createQueryListener();
		menu = new MenuManager("#PopUp"); //$NON-NLS-1$
		menu.setRemoveAllWhenShown(true);
		menu.setParent(getSite().getActionBars().getMenuManager());
		menu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager mgr) {
				SearchView.createContextMenuGroups(mgr);
				fillContextMenu(mgr);
				viewPart.fillContextMenu(mgr);
			}
		});
		pagebook = new PageBook(parent, SWT.NULL);
		pagebook.setLayoutData(new GridData(GridData.FILL_BOTH));
		busyLabel = createBusyControl();
		viewerContainer = new Composite(pagebook, SWT.NULL);
		viewerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		viewerContainer.setSize(100, 100);
		viewerContainer.setLayout(new FillLayout());

		viewerAdapter = new SelectionProviderAdapter();
		getSite().setSelectionProvider(viewerAdapter);
		// Register menu
		getSite().registerContextMenu(viewPart.getViewSite().getId(), menu, viewerAdapter);

		createViewer(viewerContainer, currentLayout);
		showBusyLabel(isBusyShown);
		NewSearchUI.addQueryListener(searchQueryListener);

	}

	private void createViewer(Composite parent, int layout) {

		if ((layout & FLAG_LAYOUT_FLAT) != 0) {
			TableViewer viewer = createTableViewer(parent);
			this.viewer = viewer;
			configureTableViewer(viewer);
		} else if ((layout & FLAG_LAYOUT_TREE) != 0) {
			TreeViewer viewer = createTreeViewer(parent);
			this.viewer = viewer;
			configureTreeViewer(viewer);
			collapseAllAction.setViewer(viewer);
			expandAllAction.setViewer(viewer);
		}

		// copyToClipboardAction.setViewer(viewer);
		selectAllAction.setViewer(viewer);

		IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
		tbm.removeAll();
		SearchView.createToolBarGroups(tbm);
		fillToolbar(tbm);
		tbm.update(false);

		new OpenAndLinkWithEditorHelper(viewer) {

			@Override
			protected void activate(ISelection selection) {
				final int currentMode = OpenStrategy.getOpenMethod();
				try {
					OpenStrategy.setOpenMethod(OpenStrategy.DOUBLE_CLICK);
					handleOpen(new OpenEvent(viewer, selection));
				} finally {
					OpenStrategy.setOpenMethod(currentMode);
				}
			}

			@Override
			protected void linkToEditor(ISelection selection) {
				// not supported by this part
			}

			@Override
			protected void open(ISelection selection, boolean activate) {
				handleOpen(new OpenEvent(viewer, selection));
			}

		};

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				currentMatchIndex = -1;
				removeSelectedMatches.setEnabled(canRemoveMatchesWith(event.getSelection()));
			}
		});

		viewer.addSelectionChangedListener(viewerAdapter);

		Menu menu = this.menu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		updateLayoutActions();
		getViewPart().updateLabel();
	}

	protected void configureTreeViewer(TreeViewer treeViewer) {
		contentProvider = new ModelSearchTreeContentProvider(this);
		treeViewer.setContentProvider(contentProvider);

		labelProvider = new ModelSearchLabelProvider();
		treeViewer.setLabelProvider(labelProvider);
	}

	protected void configureTableViewer(TableViewer tableViewer) {
		contentProvider = new ModelSearchTreeContentProvider(this);
		tableViewer.setContentProvider(contentProvider);

		labelProvider = new ModelSearchLabelProvider();
		tableViewer.setLabelProvider(labelProvider);
	}

	/**
	 * <p>
	 * This method is called when the search page gets an 'open' event from its underlying viewer (for example on double
	 * click). The default implementation will open the first match on any element that has matches. If the element to
	 * be opened is an inner node in the tree layout, the node will be expanded if it's collapsed and vice versa.
	 * Subclasses are allowed to override this method.
	 * </p>
	 *
	 * @param event
	 *            the event sent for the currently shown viewer
	 * @see IOpenListener
	 */
	protected void handleOpen(OpenEvent event) {
		Viewer viewer = event.getViewer();
		boolean hasCurrentMatch = showCurrentMatch(OpenStrategy.activateOnOpen());
		ISelection sel = event.getSelection();
		if (viewer instanceof TreeViewer && sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			TreeViewer tv = (TreeViewer) getViewer();
			Object element = selection.getFirstElement();
			if (element != null) {
				if (!hasCurrentMatch && getDisplayedMatchCount(element) > 0) {
					gotoNextMatch(OpenStrategy.activateOnOpen());
				} else {
					tv.setExpandedState(element, !tv.getExpandedState(element));
				}
			}
			return;
		} else if (!hasCurrentMatch) {
			gotoNextMatch(OpenStrategy.activateOnOpen());
		}
	}

	/**
	 * Fills the toolbar contribution for this page. Subclasses may override this method.
	 *
	 * @param tbm
	 *            the tool bar manager representing the view's toolbar
	 */
	protected void fillToolbar(IToolBarManager tbm) {
		tbm.appendToGroup(IContextMenuConstants.GROUP_SHOW, showNextAction);
		tbm.appendToGroup(IContextMenuConstants.GROUP_SHOW, showPreviousAction);
		tbm.appendToGroup(IContextMenuConstants.GROUP_REMOVE_MATCHES, removeSelectedMatches);
		tbm.appendToGroup(IContextMenuConstants.GROUP_REMOVE_MATCHES, removeAllMatchesAction);
		IActionBars actionBars = getSite().getActionBars();
		if (actionBars != null) {
			actionBars.setGlobalActionHandler(ActionFactory.NEXT.getId(), showNextAction);
			actionBars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), showPreviousAction);
			actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), removeSelectedMatches);
			// actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyToClipboardAction);
			actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), selectAllAction);
		}
		if (getLayout() == FLAG_LAYOUT_TREE) {
			tbm.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, expandAllAction);
			tbm.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, collapseAllAction);
		}
	}

	/**
	 * Sets the layout of this search result page. The layout must be on of <code>FLAG_LAYOUT_FLAT</code> or
	 * <code>FLAG_LAYOUT_TREE</code> and it must be one of the values passed during construction of this search result
	 * page.
	 *
	 * @param layout
	 *            the new layout
	 * @see ModelSearchResultViewPage#isLayoutSupported(int)
	 */
	public void setLayout(int layout) {
		Assert.isTrue(countBits(layout) == 1);
		Assert.isTrue(isLayoutSupported(layout));
		if (countBits(supportedLayouts) < 2) {
			return;
		}
		if (currentLayout == layout) {
			return;
		}
		currentLayout = layout;
		ISelection selection = viewer.getSelection();
		disconnectViewer();
		disposeViewer();
		createViewer(viewerContainer, layout);
		viewerContainer.layout(true);
		connectViewer(input);
		viewer.setSelection(selection, true);
		getSettings().put(KEY_LAYOUT, layout);
		getViewPart().updateLabel();
	}

	private void disposeViewer() {
		viewer.removeSelectionChangedListener(viewerAdapter);
		viewer.getControl().dispose();
		viewer = null;
	}

	private void updateLayoutActions() {
		if (flatAction != null) {
			flatAction.setChecked(currentLayout == flatAction.getLayout());
		}
		if (hierarchicalAction != null) {
			hierarchicalAction.setChecked(currentLayout == hierarchicalAction.getLayout());
		}
	}

	/**
	 * Creates the tree viewer to be shown on this page. Clients may override this method.
	 *
	 * @param parent
	 *            the parent widget
	 * @return returns a newly created <code>TreeViewer</code>.
	 */
	protected TreeViewer createTreeViewer(Composite parent) {
		return new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	}

	/**
	 * Creates the table viewer to be shown on this page. Clients may override this method.
	 *
	 * @param parent
	 *            the parent widget
	 * @return returns a newly created <code>TableViewer</code>
	 */
	protected TableViewer createTableViewer(Composite parent) {
		return new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	}

	private Control createBusyControl() {
		Table busyLabel = new Table(pagebook, SWT.NONE);
		TableItem item = new TableItem(busyLabel, SWT.NONE);
		item.setText(Messages.ModelSearchResultViewPage_searching_label);
		busyLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return busyLabel;
	}

	private IQueryListener createQueryListener() {
		return new IQueryListener() {
			@Override
			public void queryAdded(ISearchQuery query) {
				// ignore
			}

			@Override
			public void queryRemoved(ISearchQuery query) {
				// ignore
			}

			@Override
			public void queryStarting(final ISearchQuery query) {
				final Runnable runnable1 = new Runnable() {
					@Override
					public void run() {
						updateBusyLabel();
						ModelSearchResult result = getInput();

						if (result == null || !result.getQuery().equals(query)) {
							return;
						}
						turnOffDecoration();
						scheduleUIUpdate();
					}

				};
				asyncExec(runnable1);
			}

			@Override
			public void queryFinished(final ISearchQuery query) {
				// handle the end of the query in the UIUpdateJob, as ui updates
				// may not be finished here.
				postEnsureSelection();
			}
		};
	}

	/**
	 * Posts a UI update to make sure an element is selected.
	 */
	protected void postEnsureSelection() {
		scheduleEnsureSelection = true;
		scheduleUIUpdate();
	}

	private void runBatchedClear() {
		synchronized (this) {
			if (!batchedClearAll) {
				return;
			}
			batchedClearAll = false;
			updateBusyLabel();
		}
		getViewPart().updateLabel();
		clear();
	}

	private void asyncExec(final Runnable runnable) {
		final Control control = getControl();
		if (control != null && !control.isDisposed()) {
			Display currentDisplay = Display.getCurrent();
			if (currentDisplay == null || !currentDisplay.equals(control.getDisplay())) {
				// meaning we're not executing on the display thread of the
				// control
				control.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (!control.isDisposed()) {
							runnable.run();
						}
					}
				});
			} else {
				runnable.run();
			}
		}
	}

	/**
	 * Fills the context menu for this page. Subclasses may override this method.
	 *
	 * @param mgr
	 *            the menu manager representing the context menu
	 */
	protected void fillContextMenu(IMenuManager mgr) {
		mgr.appendToGroup(IContextMenuConstants.GROUP_SHOW, showNextAction);
		mgr.appendToGroup(IContextMenuConstants.GROUP_SHOW, showPreviousAction);
		// mgr.appendToGroup(IContextMenuConstants.GROUP_EDIT, copyToClipboardAction);
		if (getCurrentMatch() != null) {
			mgr.appendToGroup(IContextMenuConstants.GROUP_REMOVE_MATCHES, removeCurrentMatch);
		}
		if (canRemoveMatchesWith(getViewer().getSelection())) {
			mgr.appendToGroup(IContextMenuConstants.GROUP_REMOVE_MATCHES, removeSelectedMatches);
		}
		mgr.appendToGroup(IContextMenuConstants.GROUP_REMOVE_MATCHES, removeAllMatchesAction);

		if (getLayout() == FLAG_LAYOUT_TREE) {
			mgr.appendToGroup(IContextMenuConstants.GROUP_SHOW, expandAllAction);
		}
	}

	/**
	 * Determines whether the provided selection can be used to remove matches from the result.
	 *
	 * @param selection
	 *            the selection to test
	 * @return returns <code>true</code> if the elements in the current selection can be removed.
	 */
	protected boolean canRemoveMatchesWith(ISelection selection) {
		return !selection.isEmpty();
	}

	private void showMatch(final ModelSearchMatch match, final boolean activateEditor) {
		ISafeRunnable runnable = new ISafeRunnable() {
			@Override
			public void handleException(Throwable exception) {
				if (exception instanceof PartInitException) {
					PartInitException pie = (PartInitException) exception;
					ErrorDialog.openError(getSite().getShell(), Messages.ModelSearchResultViewPage_show_match,
							Messages.ModelSearchResultViewPage_error_no_editor, pie.getStatus());
				}
			}

			@Override
			public void run() throws Exception {
				// TODO aakar Implement this to show match in view or editor
				//MessageDialog.openInformation(getSite().getShell(), Messages.ModelSearchResultViewPage_show_match, "Not Yet Supported"); //$NON-NLS-1$
				// ExtendedPlatformUI.showObjectsInView(objects, viewId);
				// getViewer().setSelection(new StructuredSelection(match.getElement()), true);
				IFile matchFile = getFile(match);
				if (activateEditor && match != null) {
					IEditorPart editor = getSite().getPage().getActiveEditor();
					if (editor != null) {
						IEditorInput input = editor.getEditorInput();
						IFile file = ResourceUtil.getFile(input);
						if (file != null) {
							if (file.equals(matchFile) && OpenStrategy.activateOnOpen()) {
								getSite().getPage().activate(editor);
							}
						}
					}

					try {
						editor = IDE.openEditor(getSite().getPage(), matchFile, OpenStrategy.activateOnOpen());
					} catch (PartInitException ex) {
						PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
					}
					if (editor != null && editor instanceof IViewerProvider) {
						Viewer editorViewer = ((IViewerProvider) editor).getViewer();
						editorViewer.setSelection(new StructuredSelection(match.getElement()), true);
					}
				}
			}
		};
		SafeRunner.run(runnable);
	}

	private IFile getFile(ModelSearchMatch modelSearchMatch) {
		return EcorePlatformUtil.getFile(modelSearchMatch.getElement());
	}

	/**
	 * Return the layout this page is currently using.
	 *
	 * @return the layout this page is currently using
	 * @see #FLAG_LAYOUT_FLAT
	 * @see #FLAG_LAYOUT_TREE
	 */
	public int getLayout() {
		return currentLayout;
	}

	private boolean showCurrentMatch(boolean activateEditor) {
		ModelSearchMatch currentMatch = getCurrentMatch();
		if (currentMatch != null) {
			showMatch(currentMatch, activateEditor);
			return true;
		}
		return false;
	}

	/**
	 * Returns the currently selected match.
	 *
	 * @return the selected match or <code>null</code> if none are selected
	 */
	public ModelSearchMatch getCurrentMatch() {
		Object element = getFirstSelectedElement();
		if (element != null) {
			ModelSearchMatch[] matches = getDisplayedMatches(element);
			if (currentMatchIndex >= 0 && currentMatchIndex < matches.length) {
				return matches[currentMatchIndex];
			}
		}
		return null;
	}

	/**
	 * Returns the matches that are currently displayed for the given element. If
	 * {@link ModelSearchResult#getActiveMatchFilters()} is not null, only matches are returned that are not filtered by
	 * the match filters. If {@link ModelSearchResult#getActiveMatchFilters()} is null all matches of the given element
	 * are returned. Any action operating on the visible matches in the search result page should use this method to get
	 * the matches for a search result (instead of asking the search result directly).
	 *
	 * @param element
	 *            The element to get the matches for
	 * @return The matches displayed for the given element. If the current input of this page is <code>null</code>, an
	 *         empty array is returned
	 * @see ModelSearchResult#getMatches(Object)
	 */
	public ModelSearchMatch[] getDisplayedMatches(Object element) {
		ModelSearchResult result = getInput();
		if (result == null) {
			return EMPTY_MATCH_ARRAY;
		}

		// TODO (aakar) Support filtering
		ModelSearchMatch[] matches = result.getMatches(element);
		// if (result.getActiveMatchFilters() == null) {
		return matches;
		// }

		// int count = 0;
		// for (int i = 0; i < matches.length; i++) {
		// if (matches[i].isFiltered()) {
		// matches[i] = null;
		// } else {
		// count++;
		// }
		// }
		// if (count == matches.length) {
		// return matches;
		// }
		//
		// SearchMatch[] filteredMatches = new SearchMatch[count];
		// for (int i = 0, k = 0; i < matches.length; i++) {
		// if (matches[i] != null) {
		// filteredMatches[k++] = matches[i];
		// }
		// }
		// return filteredMatches;
	}

	private Object getFirstSelectedElement() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection.size() > 0) {
			return selection.getFirstElement();
		}
		return null;
	}

	@Override
	public Control getControl() {
		return pagebook;
	}

	@Override
	public void setFocus() {
		Control control = viewer.getControl();
		if (control != null && !control.isDisposed()) {
			control.setFocus();
		}
	}

	private synchronized void scheduleUIUpdate() {
		if (!isUIUpdateScheduled) {
			isUIUpdateScheduled = true;
			new UpdateUIJob().schedule();
		}
	}

	private void turnOffDecoration() {
		IBaseLabelProvider lp = viewer.getLabelProvider();
		if (lp instanceof DecoratingLabelProvider) {
			((DecoratingLabelProvider) lp).setLabelDecorator(null);
		}
	}

	private void turnOnDecoration() {
		IBaseLabelProvider lp = viewer.getLabelProvider();
		if (lp instanceof DecoratingLabelProvider) {
			((DecoratingLabelProvider) lp).setLabelDecorator(PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator());

		}
	}

	/**
	 * Sets the maximal number of top level elements to be shown in a viewer. If <code>null</code> is set, the view page
	 * does not support to limit the elements and will not provide UI to configure it. If a non-null value is set,
	 * configuration UI will be provided. The limit value must be a positive number or <code>-1</code> to not limit top
	 * level element. If enabled, the element limit has to be enforced by the content provider that is implemented by
	 * the client. The view page just manages the value and configuration.
	 *
	 * @param limit
	 *            the element limit. Valid values are:
	 *            <dl>
	 *            <li><code>null</code> to not limit and not provide configuration UI</li>
	 *            <li><code>-1</code> to not limit and provide configuration UI</li>
	 *            <li><code>positive integer</code> to limit by the given value and provide configuration UI</li>
	 *            </dl>
	 */
	public void setElementLimit(Integer limit) {
		elementLimit = limit;

		if (viewer != null) {
			viewer.refresh();
		}
		if (viewPart != null) {
			viewPart.updateLabel();
		}
	}

	/**
	 * Gets the maximal number of top level elements to be shown in a viewer. <code>null</code> means the view page does
	 * not limit the elements and will not provide UI to configure it. If a non-null value is set, configuration UI will
	 * be provided. The limit value must be a positive number or <code>-1</code> to not limit top level element.
	 *
	 * @return returns the element limit. Valid values are:
	 *         <dl>
	 *         <li><code>null</code> to not limit and not provide configuration UI (default value)</li>
	 *         <li><code>-1</code> to not limit and provide configuration UI</li>
	 *         <li><code>positive integer</code> to limit by the given value and provide configuration UI</li>
	 *         </dl>
	 */
	public Integer getElementLimit() {
		return elementLimit;
	}

	private class UpdateUIJob extends UIJob {

		public UpdateUIJob() {
			super(Messages.ModelSearchResultViewPage_update_job_name);
			setSystem(true);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			Control control = getControl();
			if (control == null || control.isDisposed()) {
				// disposed the control while the UI was posted.
				return Status.OK_STATUS;
			}
			runBatchedClear();
			runBatchedUpdates();
			if (hasMoreUpdates() || isQueryRunning()) {
				schedule(500);
			} else {
				isUIUpdateScheduled = false;
				turnOnDecoration();
				updateBusyLabel();
				if (scheduleEnsureSelection) {
					scheduleEnsureSelection = false;
					ModelSearchResult result = getInput();
					if (result != null && viewer.getSelection().isEmpty()) {
						navigateNext(true);
					}
				}
			}
			viewPart.updateLabel();
			viewer.refresh();
			return Status.OK_STATUS;
		}
	}

	/**
	 * Handles a search result event for the current search result.
	 *
	 * @param e
	 *            the event to handle
	 */
	protected void handleSearchResultChanged(final SearchResultEvent e) {
		if (e instanceof MatchEvent) {
			postUpdate(((MatchEvent) e).getMatches());
		} else if (e instanceof RemoveAllEvent) {
			postClear();
		}
	}

	/**
	 * Evaluates the elements to that are later passed to {@link #elementsChanged(Object[])}. By default the element to
	 * change are the elements received by ({@link ModelSearchMatch#getElement()}). Client implementations can modify
	 * this behavior.
	 *
	 * @param matches
	 *            the matches that were added or removed
	 * @param changedElements
	 *            the set that collects the elements to change. Clients should only add elements to the set.
	 * @since 3.4
	 */
	protected void evaluateChangedElements(ModelSearchMatch[] matches, Set<Object> changedElements) {
		for (ModelSearchMatch matche : matches) {
			changedElements.add(matche.getElement());
		}
	}

	private synchronized void postUpdate(ModelSearchMatch[] matches) {
		evaluateChangedElements(matches, batchedUpdates);
		scheduleUIUpdate();
	}

	private synchronized void runBatchedUpdates() {
		elementsChanged(batchedUpdates.toArray());
		batchedUpdates.clear();
		updateBusyLabel();
	}

	protected void elementsChanged(Object[] updatedElements) {
		if (contentProvider != null) {
			contentProvider.elementsChanged(updatedElements);
		}
	}

	protected void clear() {
		if (contentProvider != null) {
			contentProvider.clear();
		}
	}

	private synchronized void postClear() {
		batchedClearAll = true;
		batchedUpdates.clear();
		scheduleUIUpdate();
	}

	private synchronized boolean hasMoreUpdates() {
		return batchedClearAll || batchedUpdates.size() > 0;
	}

	private boolean isQueryRunning() {
		ModelSearchResult result = getInput();
		if (result != null) {
			return NewSearchUI.isQueryRunning(result.getQuery());
		}
		return false;
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (IPropertySheetPage.class == adapter) {
			return getPropertySheetPage();
		}
		return null;
	}

	/**
	 * This creates a new property sheet page instance and manages it in the cache.
	 */
	public IPropertySheetPage getPropertySheetPage() {
		IPropertySheetPage propertySheetPage = new TabbedPropertySheetPage(this);
		propertySheetPages.add(propertySheetPage);
		return propertySheetPage;
	}

	@Override
	public String getContributorId() {
		return viewPart.getViewSite().getId();
	}

	private class SelectionProviderAdapter implements ISelectionProvider, ISelectionChangedListener {
		private List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>(5);

		@Override
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.add(listener);
		}

		@Override
		public ISelection getSelection() {
			return viewer.getSelection();
		}

		@Override
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.remove(listener);
		}

		@Override
		public void setSelection(ISelection selection) {
			viewer.setSelection(selection);
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			// forward to my listeners
			SelectionChangedEvent wrappedEvent = new SelectionChangedEvent(this, event.getSelection());
			for (ISelectionChangedListener listener : listeners) {
				listener.selectionChanged(wrappedEvent);
			}
		}
	}
}
