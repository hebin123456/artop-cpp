/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [457704] Integrate EMF compare 3.x in Sphinx
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.compare.ui.actions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.EMFCompare.Builder;
import org.eclipse.emf.compare.domain.ICompareEditingDomain;
import org.eclipse.emf.compare.ide.ui.internal.configuration.EMFCompareConfiguration;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.postprocessor.IPostProcessor;
import org.eclipse.emf.compare.rcp.EMFCompareRCPPlugin;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.compare.scope.ModelComparisonScope;
import org.eclipse.sphinx.emf.compare.ui.editor.ModelCompareEditor;
import org.eclipse.sphinx.emf.compare.ui.editor.ModelComparisonScopeEditorInput;
import org.eclipse.sphinx.emf.compare.ui.internal.Activator;
import org.eclipse.sphinx.emf.compare.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.compare.util.ModelCompareUtil;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * The basic compare action.
 */
@SuppressWarnings("restriction")
public class BasicCompareAction extends BaseSelectionListenerAction implements ISelectionChangedListener {

	/**
	 * The selected objects that must be compared.
	 */
	protected List<WeakReference<EObject>> selectedEObjects = null;

	/**
	 * The selected files that must be compared.
	 */
	protected List<WeakReference<IFile>> selectedFiles = null;

	/**
	 * Constructor.
	 */
	public BasicCompareAction() {
		super(Messages.action_compareWithEachOther);
		setDescription(Messages.action_compareWithEachOther_description);
	}

	/**
	 * @param selection
	 *            The selection in the viewer onto which this action should perform an operation.
	 * @return <ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> if compare action is available (i.e. if {@link IStructuredSelection
	 *         selection} matches enablement criteria);</li>
	 *         <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	@Override
	public boolean updateSelection(IStructuredSelection selection) {
		Assert.isNotNull(selection);

		if (selection.size() != 2) {
			return false;
		}

		// Reset attributes
		selectedFiles = null;
		selectedEObjects = null;

		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			Object object = it.next();

			if (object instanceof EObject) {
				if (selectedEObjects == null) {
					selectedEObjects = new ArrayList<WeakReference<EObject>>();
				}
				selectedEObjects.add(new WeakReference<EObject>((EObject) object));
			} else if (object instanceof IFile) {
				if (selectedFiles == null) {
					selectedFiles = new ArrayList<WeakReference<IFile>>();
				}
				selectedFiles.add(new WeakReference<IFile>((IFile) object));
			}
		}
		return selectedFiles != null ? selectedFiles.size() == 2 : false ^ selectedEObjects != null ? selectedEObjects.size() == 2 : false;
	}

	@Override
	public boolean isEnabled() {
		if (selectedFiles != null && selectedFiles.size() == 2) {
			IFile leftFile = selectedFiles.get(0).get();
			IFile rightFile = selectedFiles.get(1).get();
			return ModelDescriptorRegistry.INSTANCE.isModelFile(leftFile) && ModelDescriptorRegistry.INSTANCE.isModelFile(rightFile);
		}
		return super.isEnabled();
	}

	@Override
	public void run() {
		Object leftObject = null;
		Object rightObject = null;
		if (selectedEObjects != null && selectedEObjects.size() == 2) {
			leftObject = selectedEObjects.get(0).get();
			rightObject = selectedEObjects.get(1).get();
		} else if (selectedFiles != null && selectedFiles.size() == 2) {
			leftObject = selectedFiles.get(0).get();
			rightObject = selectedFiles.get(1).get();
		}

		if (leftObject == null || rightObject == null) {
			return;
		}

		CompareEditorInput input = getCompareEditorInput(leftObject, rightObject);
		IWorkbenchPage page = ExtendedPlatformUI.getActivePage();
		IReusableEditor editor = getReusableEditor();

		openCompareEditor(input, page, editor);
	}

	protected AdapterFactory getAdapterFactory() {
		return new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
	}

	protected IMatchEngine.Factory.Registry getMatchEngineFactoryRegistry() {
		return EMFCompareRCPPlugin.getDefault().getMatchEngineFactoryRegistry();
	}

	protected IPostProcessor.Descriptor.Registry<?> getPostProcessorRegistry() {
		return EMFCompareRCPPlugin.getDefault().getPostProcessorRegistry();
	}

	protected CompareEditorInput getCompareEditorInput(Object leftObject, Object rightObject) {
		Assert.isTrue(leftObject instanceof Notifier || leftObject instanceof IFile);
		Assert.isTrue(rightObject instanceof Notifier || rightObject instanceof IFile);

		CompareEditorInput input = createCompareEditorInput(getAdapterFactory(), leftObject, rightObject, null);
		CompareConfiguration configuration = input.getCompareConfiguration();
		if (configuration != null) {
			IPreferenceStore prefStore = configuration.getPreferenceStore();
			if (prefStore != null) {
				configuration.setProperty(CompareConfiguration.USE_OUTLINE_VIEW,
						Boolean.valueOf(prefStore.getBoolean(ComparePreferencePage.USE_OUTLINE_VIEW)));
			}
		}
		return input;
	}

	protected CompareEditorInput createCompareEditorInput(AdapterFactory adapterFactory, Object left, Object right, Notifier origin) {
		return createCompareEditorInput(adapterFactory, left, right, origin, null);
	}

	protected CompareEditorInput createCompareEditorInput(AdapterFactory adapterFactory, Object left, Object right, Notifier origin,
			IEclipsePreferences enginePreferences) {
		Builder builder = EMFCompare.builder().setPostProcessorRegistry(getPostProcessorRegistry());
		// FIXME commented due to API changes in EMF compare available in Eclipse Mars platform.
		// We need to check later if the same API is provided for Eclipse Luna SR2
		// if (enginePreferences != null) {
		// EMFCompareBuilderConfigurator engineProvider = new EMFCompareBuilderConfigurator(enginePreferences,
		// matchEngineFactoryRegistry);
		// engineProvider.configure(builder);
		// }
		builder.setMatchEngineFactoryRegistry(getMatchEngineFactoryRegistry());

		EMFCompare comparator = builder.build();
		final ICompareEditingDomain editingDomain = ModelCompareUtil.createEMFCompareEditingDomain(left, right, origin);
		final EMFCompareConfiguration configuration = getEMFCompareConfiguration();
		IComparisonScope scope = getComparisonScope(left, right, origin);

		CompareEditorInput input = new ModelComparisonScopeEditorInput(configuration, editingDomain, adapterFactory, comparator, scope);
		configuration.setContainer(input);
		return input;
	}

	protected EMFCompareConfiguration getEMFCompareConfiguration() {
		return new EMFCompareConfiguration(new CompareConfiguration());
	}

	protected IComparisonScope getComparisonScope(Object left, Object right, Object origin) {
		if (left instanceof Notifier && right instanceof Notifier) {
			return new ModelComparisonScope((Notifier) left, (Notifier) right, origin instanceof Notifier ? (Notifier) origin : null);
		} else if (left instanceof IFile && right instanceof IFile) {
			return new ModelComparisonScope((IFile) left, (IFile) right);
		}
		return null;
	}

	protected IReusableEditor getReusableEditor() {
		return null;
	}

	/**
	 * Performs the comparison described by the given input and opens a compare editor on the result.
	 *
	 * @param input
	 *            the input on which to open the compare editor
	 * @param page
	 *            the workbench page on which to create a new compare editor
	 * @param editor
	 *            if not null the input is opened in this editor
	 * @see CompareEditorInput
	 */
	protected void openCompareEditor(final CompareEditorInput input, final IWorkbenchPage page, final IReusableEditor editor) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (editor != null && !editor.getSite().getShell().isDisposed()) {
					// Reuse the given editor
					editor.setInput(input);
					return;
				}
				if (page != null) {
					// Open new CompareEditor on page
					try {
						page.openEditor(input, getCompareEditorId(input));
					} catch (PartInitException e) {
						PlatformLogUtil.logAsError(Activator.getPlugin(), e);
						MessageDialog.openError(ExtendedPlatformUI.getActiveShell(), Messages.error_openEditorError, e.getMessage());
					}
				} else {
					String msg = Messages.error_noActiveWorkbenchPage;
					PlatformLogUtil.logAsError(Activator.getPlugin(), new NullPointerException(msg));
					MessageDialog.openError(ExtendedPlatformUI.getActiveShell(), Messages.error_openEditorError, msg);
				}
			}
		};

		Display display = ExtendedPlatformUI.getDisplay();
		if (display != null) {
			display.syncExec(runnable);
		} else {
			runnable.run();
		}
	}

	/**
	 * Returns the identifier of the compare editor to open.
	 * <p>
	 * Inheriting clients may override this method in order to specify the identifier of another compare editor (e.g.
	 * according to the type of the specified input).
	 *
	 * @param input
	 *            The {@linkplain CompareEditorInput editor input} for which a compare editor is supposed to be opened.
	 * @return The identifier of the compare editor to open.
	 */
	protected String getCompareEditorId(CompareEditorInput input) {
		// Use our own ModelCompareEditor rather than Eclipse's org.eclipse.compare.CompareEditor
		return ModelCompareEditor.ID;
	}
}
