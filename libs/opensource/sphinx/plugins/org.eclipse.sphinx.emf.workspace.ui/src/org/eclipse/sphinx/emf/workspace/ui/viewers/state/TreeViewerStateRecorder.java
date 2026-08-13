/**
 * <copyright>
 *
 * Copyright (c) 2015-2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [480105] Occasional ConcurrentModificationException when re-launching Sphinx on previously used workspace
 *     itemis - [480147] Massive performance problem and SWTError upon startup or viewer refresh when Model Explorer has expanded model content
 *     itemis - [480105] Occasional ConcurrentModificationException when re-launching Sphinx on previously used workspace
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.viewers.state;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.sphinx.emf.workspace.ui.internal.Activator;
import org.eclipse.sphinx.emf.workspace.ui.viewers.state.providers.ITreeElementStateProvider;
import org.eclipse.sphinx.emf.workspace.ui.viewers.state.providers.TreeElementStateProviderFactory;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.IMementoAware;

public class TreeViewerStateRecorder implements IMementoAware {

	protected TreeViewer viewer;
	protected ITreeViewerState deferredState;

	public void setViewer(TreeViewer viewer) {
		this.viewer = viewer;
	}

	public ITreeViewerState getDeferredState() {
		return deferredState;
	}

	protected void setDeferredState(ITreeViewerState deferredState) {
		if (deferredState != null) {
			// Load models behind element(s) that could not be expanded so far
			for (ITreeElementStateProvider provider : deferredState.getExpandedElements()) {
				if (provider.canUnderlyingModelBeLoaded() && !provider.isUnderlyingModelLoaded()) {
					provider.loadUnderlyingModel();
				}
			}

			// No need to load models behind element(s) that could not be selected so far as they are naturally also
			// expanded elements
		}

		// Store as new deferred tree viewer state
		this.deferredState = deferredState != null && !deferredState.isEmpty() ? deferredState : null;
	}

	/*
	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void restoreState(IMemento memento) {
		if (memento != null) {
			ITreeViewerState state = new TreeViewerState();
			TreeElementStateProviderFactory factory = new TreeElementStateProviderFactory(viewer);

			// Retrieve expanded element(s) to be restored from memento
			IMemento expandedMemento = memento.getChild(TreeElementStateProviderFactory.MEMENTO_TYPE_GROUP_EXPANDED);
			if (expandedMemento != null) {
				for (IMemento elementMemento : expandedMemento.getChildren()) {
					ITreeElementStateProvider provider = factory.createFromMemento(elementMemento);
					if (provider != null) {
						state.getExpandedElements().add(provider);
					}
				}
			}

			// Retrieve selected element(s) to be restored from memento
			IMemento selectedMemento = memento.getChild(TreeElementStateProviderFactory.MEMENTO_TYPE_GROUP_SELECTED);
			if (selectedMemento != null) {
				for (IMemento elementMemento : selectedMemento.getChildren()) {
					ITreeElementStateProvider provider = factory.createFromMemento(elementMemento);
					if (provider != null) {
						state.getSelectedElements().add(provider);
					}
				}
			}

			// Apply tree viewer state to be restored
			applyState(state);
		}
	}

	public boolean canApplyState() {
		// Check if viewer and the viewer's input have already been initialized
		/*
		 * !! Important Note !! CommonNavigator#createPartControl() indirectly calls #restoreState() at a moment where
		 * the viewer's input has not yet been set, and, consequently, the viewer has not yet had any chance to create
		 * the mappings between tree elements and tree item widgets (see
		 * org.eclipse.jface.viewers.StructuredViewer#elementMap for details). However, the latter are a prerequisite
		 * and must exist before the restoration of expansion state and selection can be started.
		 */
		return viewer != null && viewer.getInput() != null;
	}

	public void applyState(ITreeViewerState state) {
		if (canApplyState()) {
			// Proceed to applying given tree viewer state if it is meaningful and if it is not already in the process
			// of being applied
			/*
			 * !! Important Note !! It can happen that this function is invoked in a reentrant manner from the same
			 * thread (more precisely, the UI thread) for the same tree viewer state object. This comes from the fact
			 * that applying a tree viewer state involves checking if models are loaded which in turn involves creation
			 * of read transactions. In case that the latter cannot be acquired immediately (e.g., because some
			 * long-running model loading process is ongoing) the control goes back to the UI event loop which starts to
			 * handle the next outstanding asynchronous request. The latter can happen to be just another refresh on the
			 * same viewer and include an attempt to restore the same tree viewer state. Without an appropriate guard
			 * against such reentrant invocations, the subsequent tree viewer state application logic would become
			 * indeterministic and end up in occasional ConcurrentModificationExceptions or other sorts of problems that
			 * are all but easy to spot.
			 */
			if (state != null && !state.isEmpty() && !state.isApplying()) {
				state.setApplying(true);

				// Extract expanded element(s) that can be restored right now
				List<ITreeElementStateProvider> expandableProviders = new ArrayList<ITreeElementStateProvider>();
				Iterator<ITreeElementStateProvider> iter = state.getExpandedElements().iterator();
				while (iter.hasNext()) {
					ITreeElementStateProvider provider = iter.next();
					// FIXME bug 486155: The tree viewer state restoration upon Eclipse startup occasionally failing
					// (https://bugs.eclipse.org/bugs/show_bug.cgi?id=486155)
					if (!provider.hasUnderlyingModel() || provider.isUnderlyingModelLoaded()) {
						expandableProviders.add(provider);
						iter.remove();
					}
				}

				// Progressively restore expanded element(s)
				/*
				 * !! Important Note !! Restrict number of restoration attempts to total number of expandable elements
				 * to avoid endless loops when some expandable elements appear to be permanently unresolvable for some
				 * reason.
				 */
				for (int i = expandableProviders.size(); !expandableProviders.isEmpty() && i > 0; i--) {
					// Remove elements with which we are done
					iter = expandableProviders.iterator();
					while (iter.hasNext()) {
						ITreeElementStateProvider provider = iter.next();
						// Keep all elements whose providers have not yet been fully resolved, i.e. where it's too early
						// to say yet if they can be expanded or not
						if (!provider.isResolved()) {
							continue;
						}

						// Remove stale elements, elements that are no longer expandable, and elements that have been
						// successfully expanded meanwhile
						if (provider.isStale()) {
							iter.remove();
						} else if (!provider.canBeExpanded()) {
							iter.remove();
						} else if (provider.isExpanded()) {
							iter.remove();
						}
					}

					// Try to expand remaining elements
					setExpandedElements(expandableProviders);
				}

				// Extract selected element(s) that can be restored right now
				List<ITreeElementStateProvider> selectableProviders = new ArrayList<ITreeElementStateProvider>();
				iter = state.getSelectedElements().iterator();
				while (iter.hasNext()) {
					ITreeElementStateProvider provider = iter.next();
					if (!provider.hasUnderlyingModel() || provider.isUnderlyingModelLoaded()) {
						selectableProviders.add(provider);
						iter.remove();
					}
				}

				// Restore selected element(s)
				setSelectedElements(selectableProviders);

				state.setApplying(false);
			}
		}

		// Handle elements that could not yet be expanded or selected
		setDeferredState(state);
	}

	protected void setExpandedElements(final List<ITreeElementStateProvider> expandableProviders) {
		for (ITreeElementStateProvider provider : expandableProviders) {
			try {
				Object element = provider.getTreeElement();
				if (element != null) {
					viewer.setExpandedState(element, true);
				}
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}

	protected void setSelectedElements(final List<ITreeElementStateProvider> selectableProviders) {
		List<Object> selectableElements = new ArrayList<Object>();
		for (ITreeElementStateProvider provider : selectableProviders) {
			Object element = provider.getTreeElement();
			if (element != null) {
				selectableElements.add(element);
			}
		}
		if (!selectableElements.isEmpty()) {
			try {
				viewer.setSelection(new StructuredSelection(selectableElements));
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}

	public boolean canRecordState() {
		// Check if viewer has already been initialized
		return viewer != null;
	}

	public ITreeViewerState recordState() {
		if (canRecordState()) {
			ITreeViewerState state = new TreeViewerState();
			TreeElementStateProviderFactory factory = new TreeElementStateProviderFactory(viewer);

			// Record expanded elements
			TreePath expandedTreePaths[] = viewer.getExpandedTreePaths();
			if (expandedTreePaths.length > 0) {
				for (TreePath expandedTreePath : expandedTreePaths) {
					ITreeElementStateProvider provider = factory.create(expandedTreePath);
					if (provider != null) {
						state.getExpandedElements().add(provider);
					}
				}
			}

			// Record selected elements
			Object selectedElements[] = ((IStructuredSelection) viewer.getSelection()).toArray();
			if (selectedElements.length > 0) {
				for (Object selectedElement : selectedElements) {
					ITreeElementStateProvider provider = factory.create(selectedElement);
					if (provider != null) {
						state.getSelectedElements().add(provider);
					}
				}
			}
			return state;
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		if (memento != null) {
			// Record current tree viewer state
			ITreeViewerState state = recordState();
			if (state != null && !state.isEmpty()) {
				// Save expanded elements to given memento
				if (!state.getExpandedElements().isEmpty()) {
					IMemento expandedMemento = memento.createChild(TreeElementStateProviderFactory.MEMENTO_TYPE_GROUP_EXPANDED);
					for (ITreeElementStateProvider provider : state.getExpandedElements()) {
						provider.appendToMemento(expandedMemento);
					}
				}

				// Save selected elements to given memento
				if (!state.getSelectedElements().isEmpty()) {
					IMemento selectedMemento = memento.createChild(TreeElementStateProviderFactory.MEMENTO_TYPE_GROUP_SELECTED);
					for (ITreeElementStateProvider provider : state.getSelectedElements()) {
						provider.appendToMemento(selectedMemento);
					}
				}
			}
		}
	}
}
