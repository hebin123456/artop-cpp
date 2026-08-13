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
 *     itemis - [418005] Add support for model files with multiple root elements
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.ui.util;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.ui.viewer.IViewerProvider;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.sphinx.emf.ui.actions.providers.OpenWithMenu;
import org.eclipse.sphinx.emf.ui.internal.Activator;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

/**
 *
 */
public class EcoreUIUtil {

	// Prevent from instantiation
	private EcoreUIUtil() {
	}

	/**
	 * Open the editor using the active workbench page for the given object. If {@link OpenStrategy#OPEN_ON_RESOURCE} is
	 * given, the method will retrieve the resource behind the object before opening the editor.
	 *
	 * @param object
	 *            The object for which to open the editor or the resource behind it.
	 * @param openStrategy
	 *            A flag to indicate whether to open the editor on the given object or on the resource behind it.
	 * @see org.eclipse.sphinx.emf.ui.util.OpenStrategy
	 */
	public static void openEditor(Object object, int openStrategy) {
		openEditor(ExtendedPlatformUI.getActivePage(), object, openStrategy);
	}

	public static void openEditor(IWorkbenchPage page, Object object, int openStrategy) {
		Assert.isLegal(openStrategy == org.eclipse.sphinx.emf.ui.util.OpenStrategy.OPEN_ON_OBJECT
				|| openStrategy == org.eclipse.sphinx.emf.ui.util.OpenStrategy.OPEN_ON_RESOURCE);

		try {
			IEditorPart editor = null;
			if (openStrategy == org.eclipse.sphinx.emf.ui.util.OpenStrategy.OPEN_ON_OBJECT) {
				IEditorInput editorInput = EcoreUIUtil.createURIEditorInput(object);
				IEditorDescriptor defaultEditor = getDefaultEditor(object);
				if (defaultEditor != null) {
					editor = page.openEditor(editorInput, defaultEditor.getId());
				}

			} else if (openStrategy == org.eclipse.sphinx.emf.ui.util.OpenStrategy.OPEN_ON_RESOURCE) {
				IFile file = EcorePlatformUtil.getFile(object);
				editor = IDE.openEditor(page, file, OpenStrategy.activateOnOpen());

			}
			if (editor != null && editor instanceof IViewerProvider) {
				Viewer editorViewer = ((IViewerProvider) editor).getViewer();
				editorViewer.setSelection(new StructuredSelection(object), true);
			}

		} catch (PartInitException ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
	}

	public static void openWizardDialog(final IWizard wizard) throws OperationCanceledException, ExecutionException {
		openWizardDialog(wizard, null);
	}

	/**
	 * @param wizard
	 *            The wizard the dialog to open is supposed to work on.
	 * @param editingDomain
	 *            The transactional editing domain to use for the transaction.
	 * @throws OperationCanceledException
	 *             that is mandatory to force underlying operation to abort without commit if user clicks on the cancel
	 *             button.
	 * @throws ExecutionException
	 *             if an execution exception occurs.
	 */
	public static void openWizardDialog(final IWizard wizard, TransactionalEditingDomain editingDomain) throws OperationCanceledException,
			ExecutionException {
		Assert.isNotNull(wizard);

		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// Creates and opens the wizard dialog
				int result = new WizardDialog(ExtendedPlatformUI.getDisplay().getActiveShell(), wizard).open();

				if (result == Window.CANCEL) {
					// OperationCanceledException is mandatory to force underlying operation to abort without commit
					throw new OperationCanceledException(wizard.getWindowTitle());
				}
			}
		};

		if (editingDomain != null) {
			WorkspaceTransactionUtil.executeInWriteTransaction(editingDomain, runnable, wizard.getWindowTitle());
		} else {
			runnable.run();
		}
	}

	public static URIEditorInput createURIEditorInput(Object object) {
		URI uri = null;

		if (object instanceof URI) {
			uri = (URI) object;
		} else if (object instanceof Resource) {
			uri = ((Resource) object).getURI();
		} else if (object instanceof EObject) {
			if (!((EObject) object).eIsProxy()) {
				uri = EcoreUtil.getURI((EObject) object);
			}
		} else if (object instanceof IWrapperItemProvider) {
			Object unwrapped = AdapterFactoryEditingDomain.unwrap(object);
			return createURIEditorInput(unwrapped);
		} else if (object instanceof FeatureMap.Entry) {
			Object unwrapped = AdapterFactoryEditingDomain.unwrap(object);
			return createURIEditorInput(unwrapped);
		}

		if (uri != null) {
			return new ExtendedURIEditorInput(uri);
		}
		return null;
	}

	public static IEditorDescriptor getDefaultEditor(Object object) {
		if (object instanceof EObject) {
			return getDefaultEditor(((EObject) object).eClass());
		} else if (object instanceof IWrapperItemProvider) {
			Object unwrapped = AdapterFactoryEditingDomain.unwrap(object);
			return getDefaultEditor(unwrapped);
		} else if (object instanceof FeatureMap.Entry) {
			Object unwrapped = AdapterFactoryEditingDomain.unwrap(object);
			return getDefaultEditor(unwrapped);
		} else if (object instanceof Resource) {
			String fileName = ((Resource) object).getURI().lastSegment();
			return PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(fileName);
		}
		return null;
	}

	/**
	 * A convenience method usually used to populate an {@linkplain OpenWithMenu}
	 *
	 * @param object
	 * @return
	 */
	public static IEditorDescriptor[] getEditors(Object object) {
		if (object instanceof EObject) {
			return getEditors(((EObject) object).eClass());
		} else if (object instanceof IWrapperItemProvider) {
			Object unwrapped = AdapterFactoryEditingDomain.unwrap(object);
			return getEditors(unwrapped);
		} else if (object instanceof FeatureMap.Entry) {
			Object unwrapped = AdapterFactoryEditingDomain.unwrap(object);
			return getEditors(unwrapped);
		} else if (object instanceof Resource) {
			String fileName = ((Resource) object).getURI().lastSegment();
			return PlatformUI.getWorkbench().getEditorRegistry().getEditors(fileName);
		}
		return null;
	}

	public static IEditorDescriptor[] getEditors(EClass eClass) {
		return findEditorsForType(eClass);
	}

	/**
	 * @deprecated Use {@link EcoreUIUtil#getDummyFileName(EClass)} instead.
	 */

	@Deprecated
	public static String getDummyFileName(Class<?> objectType) {
		String dummyFileName = "*." + objectType.getName(); //$NON-NLS-1$
		return dummyFileName;
	}

	public static String getDummyFileName(EClass eClass) {
		String dummyFileName = "*." + eClass.getInstanceClassName(); //$NON-NLS-1$
		return dummyFileName;
	}

	/**
	 * @deprecated Use {@link EcoreUIUtil#getDefaultEditor(EClass)} instead.
	 */
	@Deprecated
	public static IEditorDescriptor getDefaultEditor(Class<?> type) {
		IEditorDescriptor descriptor = findDefaultEditorForType(type);
		if (descriptor == null) {
			descriptor = findDefaultEditorForSuperType(type);
		}
		return descriptor;
	}

	public static IEditorDescriptor getDefaultEditor(EClass eClass) {
		IEditorDescriptor descriptor = findDefaultEditorForType(eClass);
		if (descriptor == null) {
			descriptor = findDefaultEditorForSuperType(eClass);
		}
		return descriptor;
	}

	public static URI getURIFromEditorInput(IEditorInput editorInput) {
		if (editorInput instanceof FileEditorInput) {
			FileEditorInput fileEditorInput = (FileEditorInput) editorInput;
			return URI.createPlatformResourceURI(fileEditorInput.getFile().getFullPath().toString(), true);
		}
		if (editorInput instanceof URIEditorInput) {
			return ((URIEditorInput) editorInput).getURI();
		}
		if (editorInput instanceof IURIEditorInput) {
			IURIEditorInput uriEditorInput = (IURIEditorInput) editorInput;
			java.net.URI uri = uriEditorInput.getURI();
			if (uri != null) {
				try {
					return URI.createFileURI(uri.toURL().getFile());
				} catch (MalformedURLException ex) {
					PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
				}
			}
		}
		if (editorInput != null) {
			IFile file = (IFile) editorInput.getAdapter(IFile.class);
			if (file != null) {
				return EcorePlatformUtil.createURI(file.getFullPath());
			}
		}
		return null;
	}

	/**
	 * Returns the file behind the editor input
	 *
	 * @param editorInput
	 * @return as specified above
	 */
	public static IFile getFileFromEditorInput(IEditorInput editorInput) {
		if (editorInput instanceof URIEditorInput) {
			return EcorePlatformUtil.getFile(((URIEditorInput) editorInput).getURI());
		}
		if (editorInput != null) {
			return (IFile) editorInput.getAdapter(IFile.class);
		}
		return null;
	}

	private static IEditorDescriptor[] findEditorsForType(EClass eClass) {
		Set<IEditorDescriptor> result = new HashSet<IEditorDescriptor>();
		if (eClass != null) {
			// Try to find editors registered with qualified or simple EClass name
			for (IEditorDescriptor descriptor : PlatformUI.getWorkbench().getEditorRegistry().getEditors(getDummyFileName(eClass))) {
				if (!isInapplicableTextBasedEditor(descriptor)) {
					result.add(descriptor);
				}
			}

			// Try to find editors registered for all super types
			for (EClass superType : eClass.getEAllSuperTypes()) {
				for (IEditorDescriptor descriptor : PlatformUI.getWorkbench().getEditorRegistry().getEditors(getDummyFileName(superType))) {
					if (!isInapplicableTextBasedEditor(descriptor)) {
						result.add(descriptor);
					}
				}
			}
		}
		return result.toArray(new IEditorDescriptor[result.size()]);
	}

	private static IEditorDescriptor findDefaultEditorForSuperType(Class<?> objectType) {
		if (objectType != null) {
			// Try to find matching super class
			Set<Class<?>> superTypes = new HashSet<Class<?>>();
			if (!objectType.isInterface()) {
				Class<?> superClass = objectType.getSuperclass();
				if (superClass != null) {
					superTypes.add(superClass);
					IEditorDescriptor descriptor = findDefaultEditorForType(superClass);
					if (descriptor != null) {
						return descriptor;
					}
				}
			}

			// Try to find matching interface
			Class<?>[] interfaces = objectType.getInterfaces();
			for (Class<?> interfaze : interfaces) {
				superTypes.add(interfaze);
				IEditorDescriptor descriptor = findDefaultEditorForType(interfaze);
				if (descriptor != null) {
					return descriptor;
				}
			}

			// Try to find matching super type of super class and interfaces
			for (Class<?> superType : superTypes) {
				IEditorDescriptor descriptor = findDefaultEditorForSuperType(superType);
				if (descriptor != null) {
					return descriptor;
				}
			}
		}

		return null;
	}

	private static IEditorDescriptor findDefaultEditorForSuperType(EClass eClass) {
		if (eClass != null) {
			for (EClass superType : eClass.getEAllSuperTypes()) {
				IEditorDescriptor defaultEditor = findDefaultEditorForType(superType);
				if (defaultEditor != null) {
					return defaultEditor;
				}
			}
		}
		return null;
	}

	private static IEditorDescriptor findDefaultEditorForType(Class<?> objectType) {
		if (objectType != null) {
			// Try to find editor registered with qualified or simple object type name
			String dummyFileName = "*." + objectType.getName(); //$NON-NLS-1$
			IEditorDescriptor descriptor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(dummyFileName);
			if (descriptor != null) {
				if (!isInapplicableTextBasedEditor(descriptor)) {
					return descriptor;
				} else {
					// Try to find alternative editor
					for (IEditorDescriptor alternativeDescriptor : PlatformUI.getWorkbench().getEditorRegistry().getEditors(dummyFileName)) {
						if (alternativeDescriptor != descriptor && !isInapplicableTextBasedEditor(alternativeDescriptor)) {
							return alternativeDescriptor;
						}
					}
				}
			}
		}
		return null;
	}

	private static IEditorDescriptor findDefaultEditorForType(EClass eClass) {
		if (eClass != null) {
			// Try to find editor registered with qualified or simple object type name
			IEditorDescriptor descriptor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(getDummyFileName(eClass));
			if (descriptor != null) {
				if (!isInapplicableTextBasedEditor(descriptor)) {
					return descriptor;
				} else {
					// Try to find alternative editor
					for (IEditorDescriptor alternativeDescriptor : PlatformUI.getWorkbench().getEditorRegistry().getEditors(getDummyFileName(eClass))) {
						if (alternativeDescriptor != descriptor && !isInapplicableTextBasedEditor(alternativeDescriptor)) {
							return alternativeDescriptor;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Avoid that model objects are opened in file-based editors. This may happen in case that file-based editors are
	 * registered upon file extensions which are equal to the simple class name of some model object (e.g. model object
	 * type = Library, file extension = *.library)
	 */
	// TODO This hard-coded kind of file editor exclusions could eventually be avoided by providing a separate extension
	// point for model editors or by handing in a list of inapplicable editor id patterns via the API
	private static boolean isInapplicableTextBasedEditor(IEditorDescriptor editorDescriptor) {
		if (editorDescriptor.getId().startsWith("org.eclipse.ui") || editorDescriptor.getId().startsWith("org.eclipse.wst")) { //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}
		return false;
	}
}
