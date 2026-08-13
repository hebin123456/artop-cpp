/**
 * <copyright>
 * 
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [393268] - [EMF Workspace] The Workspace Model Save Manager should handle pre save actions before saving models
 *     itemis - [419818] Avoid that model dirty change listeners and model pre-save listeners need to be registered separately
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.saving;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.IModelDescriptorChangeListener;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.workspace.saving.IModelSaveLifecycleListener;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.internal.SaveablesList;
import org.eclipse.ui.navigator.SaveablesProvider;

/**
 * Basic implementation of a {@link SaveablesProvider saveables provider} for Sphinx-managed shared model instances.
 */
@SuppressWarnings("restriction")
public class BasicModelSaveablesProvider extends SaveablesProvider implements IModelDescriptorChangeListener {

	/**
	 * {@link ISaveablesLifecycleListener} implementation to be used in {@link IWorkbenchPart workbench parts}s which
	 * want the site-level {@link Saveable saveable} management to be automatically notified about all changes in
	 * {@link Saveable saveables} signaled to this {@link ISaveablesLifecycleListener listener}.
	 * 
	 * @see Saveable
	 * @see ISaveablesLifecycleListener
	 */
	public static class SiteNotifyingSaveablesLifecycleListener implements ISaveablesLifecycleListener {

		protected IWorkbenchPart workbenchPart;

		protected ISaveablesLifecycleListener siteSaveablesLifecycleListener;

		/**
		 * Constructor.
		 */
		public SiteNotifyingSaveablesLifecycleListener(IWorkbenchPart workbenchPart) {
			Assert.isNotNull(workbenchPart);

			this.workbenchPart = workbenchPart;
			siteSaveablesLifecycleListener = (ISaveablesLifecycleListener) workbenchPart.getSite().getService(ISaveablesLifecycleListener.class);
		}

		@Override
		public void handleLifecycleEvent(SaveablesLifecycleEvent event) {
			/*
			 * !! Important Note !! Try to use internal API for filtering out irrelevant saveables. Otherwise warnings
			 * are raised at org.eclipse.ui.internal.SaveablesList#addModel(Object, Saveable) and
			 * org.eclipse.ui.internal.SaveablesList#removeModel(Object, Saveable) in case that the same saveable is
			 * signaled multiple times.
			 */
			List<Saveable> saveables = new ArrayList<Saveable>(Arrays.asList(event.getSaveables()));
			if (siteSaveablesLifecycleListener instanceof SaveablesList) {
				SaveablesList siteSaveablesList = (SaveablesList) siteSaveablesLifecycleListener;
				for (Iterator<Saveable> iter = saveables.iterator(); iter.hasNext();) {
					List<IWorkbenchPart> workbenchParts = Arrays.asList(siteSaveablesList.getPartsForSaveable(iter.next()));
					if (event.getEventType() == SaveablesLifecycleEvent.POST_OPEN && workbenchParts.contains(workbenchPart)) {
						iter.remove();
					}
					if (event.getEventType() == SaveablesLifecycleEvent.POST_CLOSE && !workbenchParts.contains(workbenchPart)) {
						iter.remove();
					}
				}
			}

			// Notify site-level saveable management about changed saveables, if any
			if (!saveables.isEmpty()) {
				Saveable[] saveablesArray = saveables.toArray(new Saveable[saveables.size()]);
				event = new SaveablesLifecycleEvent(workbenchPart, event.getEventType(), saveablesArray, event.isForce());
				siteSaveablesLifecycleListener.handleLifecycleEvent(event);
			}
		}
	}

	/**
	 * The saveables hold by this provider. Each saveable is associated to a model identifier.
	 */
	protected static Map<IModelDescriptor, BasicModelSaveable> saveables = Collections
			.synchronizedMap(new HashMap<IModelDescriptor, BasicModelSaveable>());

	/**
	 * Flag indicating if this provider has been disposed yet or not.
	 */
	protected boolean disposed = false;

	/**
	 * The listener that gets notified when model save lifecycle events occur.
	 */
	private IModelSaveLifecycleListener modelSaveLifecycleListener;

	/**
	 * Returns whether at least one saveable is dirty (among saveables managed by this provider).
	 * 
	 * @return <ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> at least one saveable is dirty;</li>
	 *         <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	public boolean isDirty() {
		synchronized (saveables) {
			for (Saveable saveable : saveables.values()) {
				if (saveable.isDirty()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Creates a new {@link IModelSaveLifecycleListener}.
	 * 
	 * @return The model life cycle listener that handle the dirty state change listener.
	 */
	protected IModelSaveLifecycleListener createModelSaveLifecycleListener() {
		return new IModelSaveLifecycleListener() {
			@Override
			public void handleDirtyChangedEvent(IModelDescriptor modelDescriptor) {
				Saveable saveable = getSaveable(modelDescriptor);
				if (saveable != null) {
					fireSaveablesLifecycleEventUIThread(saveable, SaveablesLifecycleEvent.DIRTY_CHANGED, true);
				}
			}

			@Override
			public void handlePreSaveEvent(IModelDescriptor modelDescriptor) {
				// Do nothing by default.
			}
		};
	}

	@Override
	protected void doInit() {
		modelSaveLifecycleListener = createModelSaveLifecycleListener();
		ModelSaveManager.INSTANCE.addModelSaveLifecycleListener(MetaModelDescriptorRegistry.ANY_MM, modelSaveLifecycleListener);
		ModelDescriptorRegistry.INSTANCE.addModelDescriptorChangeListener(this);
	}

	@Override
	public void dispose() {
		disposed = true;
		ModelSaveManager.INSTANCE.removeModelSaveLifecycleListener(modelSaveLifecycleListener);
		ModelDescriptorRegistry.INSTANCE.removeModelDescriptorChangeListener(this);
		super.dispose();
	}

	@Override
	public Object[] getElements(Saveable saveable) {
		if (saveable instanceof BasicModelSaveable) {
			// FIXME Can't we just return the ModelDescriptor here? This would be much better for the sake of
			// performance...
			IModelDescriptor modelDescriptor = ((BasicModelSaveable) saveable).getModelDescriptor();
			Collection<Resource> filteredResources = EcorePlatformUtil.getResourcesInModel(modelDescriptor, true);
			return filteredResources.toArray(new Object[filteredResources.size()]);
		}
		return new Object[0];
	}

	/**
	 * @param modelDescriptor
	 *            The identifier of the model for which a saveable must be created.
	 * @return The model saveable associated to the model identified by the specified identifier.
	 */
	protected BasicModelSaveable createModelSaveable(IModelDescriptor modelDescriptor) {
		return new BasicModelSaveable(modelDescriptor);
	}

	/*
	 * @see org.eclipse.ui.navigator.SaveablesProvider#getSaveable(java.lang.Object)
	 */
	@Override
	public Saveable getSaveable(Object element) {
		if (element instanceof IModelDescriptor) {
			return getSaveable((IModelDescriptor) element);
		} else {
			Resource resource = EcorePlatformUtil.getResource(element);
			if (resource != null) {
				return getSaveable(ModelDescriptorRegistry.INSTANCE.getModel(resource));
			}
		}
		return null;
	}

	protected Saveable getSaveable(IModelDescriptor modelDescriptor) {
		if (modelDescriptor != null) {
			synchronized (saveables) {
				BasicModelSaveable saveable = saveables.get(modelDescriptor);
				if (saveable == null) {
					saveable = createModelSaveable(modelDescriptor);
					saveables.put(modelDescriptor, saveable);
					fireSaveablesLifecycleEventUIThread(saveable, SaveablesLifecycleEvent.POST_OPEN, true);
				}
				return saveable;
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.navigator.SaveablesProvider#getSaveables()
	 */
	@Override
	public Saveable[] getSaveables() {
		return saveables.values().toArray(new Saveable[saveables.values().size()]);
	}

	/**
	 * Fires the suitable notification corresponding to the SaveablesLifecycleEventType.
	 * <p>
	 * Notification is performed inside UI thread.
	 * 
	 * @param saveable
	 *            The saveable created inside this provider.
	 */
	protected void fireSaveablesLifecycleEventUIThread(final Saveable saveable, final int SaveablesLifecycleEventType, boolean async) {
		if (disposed) {
			return;
		}
		Display display = ExtendedPlatformUI.getDisplay();
		if (display != null) {
			if (async) {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						doRun(saveable, SaveablesLifecycleEventType);
					}
				});
			} else {
				display.syncExec(new Runnable() {
					@Override
					public void run() {
						doRun(saveable, SaveablesLifecycleEventType);
					}
				});
			}
		}
	}

	protected void doRun(final Saveable saveable, final int SaveablesLifecycleEventType) {
		if (disposed) {
			return;
		}
		switch (SaveablesLifecycleEventType) {
		case SaveablesLifecycleEvent.DIRTY_CHANGED:
			fireSaveablesDirtyChanged(new Saveable[] { saveable });
			break;
		case SaveablesLifecycleEvent.POST_OPEN:
			fireSaveablesOpened(new Saveable[] { saveable });
			break;
		case SaveablesLifecycleEvent.POST_CLOSE:
			fireSaveablesClosed(new Saveable[] { saveable });
			break;
		case SaveablesLifecycleEvent.PRE_CLOSE:
			fireSaveablesClosing(new Saveable[] { saveable }, true);
			break;
		default:
			break;
		}
	}

	/**
	 * Nothing to do since {@linkplain Saveable} are created lazily.
	 */
	@Override
	public void handleModelAdded(IModelDescriptor modelDescriptor) {

	}

	/**
	 * Removes {@linkplain Saveable} from saveables map and fires {@linkplain SaveablesLifecycleEvent.PRE_CLOSE} and
	 * {@linkplain SaveablesLifecycleEvent.POST_CLOSE} notification.
	 */
	@Override
	public void handleModelRemoved(IModelDescriptor modelDescriptor) {
		if (disposed) {
			return;
		}
		if (modelDescriptor != null) {
			synchronized (saveables) {
				BasicModelSaveable saveable = saveables.get(modelDescriptor);
				if (saveable != null) {
					/*
					 * !! Important Note !! As we are in a synchronized block here and have no control about which
					 * saveables lifecycle event listeners are around and what they do we must not fire PRE_CLOSE event
					 * synchronously. Otherwise we'd introduce a potential risk of deadlocks.
					 */
					fireSaveablesLifecycleEventUIThread(saveable, SaveablesLifecycleEvent.PRE_CLOSE, true);
					saveables.remove(modelDescriptor);
					fireSaveablesLifecycleEventUIThread(saveable, SaveablesLifecycleEvent.POST_CLOSE, true);
				}
			}
		}
	}
}