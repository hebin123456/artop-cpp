/**
 * <copyright>
 * 
 * Copyright (c) 2012 itemis and others.
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
package org.eclipse.sphinx.emf.ui.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.ui.internal.Activator;
import org.eclipse.sphinx.emf.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.ui.wizards.pages.AbstractModelInitialObjectCreationPage;
import org.eclipse.sphinx.emf.ui.wizards.pages.AbstractModelNewFileCreationPage;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;

/**
 * A default implementation of the EMF model creation wizard.
 */
public abstract class AbstractModelNewWizard extends Wizard implements INewWizard {

	/**
	 * The handles the activator of the generated EMF edit plugin of the considered metamodel
	 */
	protected EMFPlugin editPlugin;

	protected static final String MODEL_WIZARD_NAME = Messages._UI_ModelWizardName;

	protected static final List<String> FILE_EXTENSIONS = Collections.singletonList(Messages._UI_DefaultModelEditorFilenameExtensions);
	/**
	 * This caches an instance of the metamodel package.
	 */
	protected EPackage metamodelPackage;

	/**
	 * This caches an instance of the metamodel factory.
	 */
	protected EFactory metamodelFactory;

	/**
	 * This is the model root object
	 */
	protected EObject modelRoot;

	/**
	 * This is the file creation page.
	 */
	protected AbstractModelNewFileCreationPage newFileCreationPage;

	/**
	 * This is the initial object creation page.
	 */
	protected AbstractModelInitialObjectCreationPage initialObjectCreationPage;

	/**
	 * Remember the selection during initialization for populating the default container.
	 */
	protected IStructuredSelection selection;

	/**
	 * Caches the names of the types that can be created as the root object.
	 */
	protected List<String> initialObjectNames;

	private IMetaModelDescriptor mmDescriptor;

	/**
	 * Default constructor initialize : - Activator if Edit plugin - File extensions - metamodel package - metamodel
	 * factory
	 */
	protected AbstractModelNewWizard(IMetaModelDescriptor mmDescriptor) {
		Assert.isNotNull(mmDescriptor);

		this.mmDescriptor = mmDescriptor;

		setEditPluginActivator();
		initFileExtensions();
		initMetamodelPackage();
		initMetamodelFactory();
	}

	/**
	 * This sets the edit plugin activator, clients should implement
	 */
	protected abstract void setEditPluginActivator();

	/**
	 * This initializes the files extensions, clients should implement.
	 */
	protected abstract void initFileExtensions();

	/**
	 * This initializes the metamodel package, clients should implement.
	 */
	protected abstract void initMetamodelPackage();

	/**
	 * This initializes the metamodel factory, clients should implement.
	 */
	protected abstract void initMetamodelFactory();

	/**
	 * The framework calls this to create the contents of the wizard
	 */
	@Override
	public void addPages() {

		// Not defined yet ?
		if (newFileCreationPage == null) {
			// Define page with default values
			createFileCreationPage(MODEL_WIZARD_NAME, FILE_EXTENSIONS);
		}
		addPage(newFileCreationPage);

		// TODO Move this logic to newFileCreationPage
		// Try and get the resource selection to determine a current directory for the file dialog.
		if (selection != null && !selection.isEmpty()) {
			// Get the resource...
			Object selectedElement = selection.iterator().next();
			if (selectedElement instanceof IResource) {
				// Get the resource parent, if its a file.
				IResource selectedResource = (IResource) selectedElement;
				if (selectedResource.getType() == IResource.FILE) {
					selectedResource = selectedResource.getParent();
				}

				// This gives a directory...
				if (selectedResource instanceof IFolder || selectedResource instanceof IProject) {
					// Set this for the container.
					newFileCreationPage.setContainerFullPath(selectedResource.getFullPath());
					// Make up a unique new name here.
					String defaultModelBaseFilename = Messages._UI_ModelEditorFilenameDefaultBase;
					// String defaultModelFilenameExtension = newFileCreationPage.getFileExtensions().get(0);
					String defaultModelFilenameExtension = newFileCreationPage.getFileExtensions().get(0);
					String modelFilename = defaultModelBaseFilename + "." + defaultModelFilenameExtension; //$NON-NLS-1$
					for (int i = 1; ((IContainer) selectedResource).findMember(modelFilename) != null; ++i) {
						modelFilename = defaultModelBaseFilename + i + "." + defaultModelFilenameExtension; //$NON-NLS-1$
					}
					newFileCreationPage.setFileName(modelFilename);
				}
			}
		}

		// Not defined yet ?
		if (initialObjectCreationPage == null) {
			// Define page with default values
			createInitialObjectCreationPage(MODEL_WIZARD_NAME);
		}
		addPage(initialObjectCreationPage);
	}

	// TODO Don't pass wizard name parameter
	protected void createInitialObjectCreationPage(String MODEL_WIZARD_NAME) {
		// Create the initial object selection page
		// TODO Eliminate edit plug-in and use label provider that is created in the same way as in
		// GenericContentsTreePage
		initialObjectCreationPage = new AbstractModelInitialObjectCreationPage("whatever", getInitialObjectNames(), editPlugin); //$NON-NLS-1$
		initialObjectCreationPage.setTitle(NLS.bind(Messages._UI_ModelWizard_label, new Object[] { MODEL_WIZARD_NAME }));
		initialObjectCreationPage.setDescription(Messages._UI_Wizard_initial_object_description);

	}

	// TODO Don't pass wizard name and file extensions as parameters
	protected void createFileCreationPage(String MODEL_WIZARD_NAME, List<String> FILE_EXTENSIONS) {
		// Create the creation page
		newFileCreationPage = new AbstractModelNewFileCreationPage("whatever2", selection);
		newFileCreationPage.setTitle(NLS.bind(Messages._UI_ModelWizard_label, new Object[] { MODEL_WIZARD_NAME }));
		newFileCreationPage.setDescription(NLS.bind(Messages._UI_ModelWizard_description, new Object[] { MODEL_WIZARD_NAME }));
		newFileCreationPage.setFileName(Messages._UI_ModelEditorFilenameDefaultBase + "." + FILE_EXTENSIONS.get(0));
		// TODO Replace file extensions with metamodel descriptor
		newFileCreationPage.setFileExtensions(FILE_EXTENSIONS);
	}

	/**
	 * Do the work after everything is specified.
	 */
	@Override
	public boolean performFinish() {
		createAndSaveNewModel();
		selectAndRevealModelInCurrentView();
		openNewModelInEditor();
		return true;
	}

	/**
	 * Creates new model and saves it to a file.
	 */
	protected void createAndSaveNewModel() {
		// Create new model
		modelRoot = createInitialModel();

		// Save new model to file
		IFile modelFile = getModelFile();
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(modelFile.getParent(), getMetaModelDescriptor());
		EcorePlatformUtil.saveNewModelResource(editingDomain, modelFile.getFullPath(), getModelFileContentTypeId(), modelRoot, false, null);
	}

	/**
	 * Selects the newly created model in the current view.
	 */
	protected void selectAndRevealModelInCurrentView() {
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();
		final IWorkbenchPart activePart = page.getActivePart();
		final IFile modelFile = getModelFile();
		if (activePart instanceof ISetSelectionTarget) {
			final ISelection targetSelection = new StructuredSelection(modelFile);
			getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					((ISetSelectionTarget) activePart).selectReveal(targetSelection);
				}
			});
		}
	}

	/**
	 * Opens newly created model in an editor.
	 */
	protected void openNewModelInEditor() {
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();
		IFile modelFile = getModelFile();
		try {
			page.openEditor(new FileEditorInput(modelFile),
					PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(modelFile.getFullPath().toString()).getId());
		} catch (PartInitException exception) {
			MessageDialog.openError(workbenchWindow.getShell(), Messages._UI_OpenEditorError_label, exception.getMessage());
		}
	}

	/**
	 * Create a new model. Subclasses may reimplement.
	 */
	protected EObject createInitialModel() {
		// TODO Eliminate metamodel package and factory fields and retrieve both from metamodel descriptor instead
		EClass eClass = (EClass) metamodelPackage.getEClassifier(initialObjectCreationPage.getInitialObjectName());
		return metamodelFactory.create(eClass);
	}

	/**
	 * Returns the names of the types that can be created as the root object. Subclasses may reimplement.
	 */
	protected Collection<String> getInitialObjectNames() {
		// TODO Eliminate metamodel package field and retrieve it from metamodel descriptor instead
		if (initialObjectNames == null) {
			initialObjectNames = new ArrayList<String>();
			for (EClassifier eClassifier : metamodelPackage.getEClassifiers()) {
				if (eClassifier instanceof EClass) {
					EClass eClass = (EClass) eClassifier;
					if (!eClass.isAbstract()) {
						initialObjectNames.add(eClass.getName());
					}
				}
			}
			Collections.sort(initialObjectNames, CommonPlugin.INSTANCE.getComparator());
		}
		return initialObjectNames;
	}

	/**
	 * Initializes the default page image descriptor to an appropriate banner. By calling
	 * <code>setDefaultPageImageDescriptor</code>. The default implementation of this method uses a generic new wizard
	 * image.
	 * <p>
	 * Subclasses may reimplement.
	 * </p>
	 */
	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor imageDescriptor = ExtendedImageRegistry.INSTANCE.getImageDescriptor(Activator.INSTANCE.getImage("wizban16/NewModel")); //$NON-NLS-1$
		setDefaultPageImageDescriptor(imageDescriptor);
	}

	/**
	 * Get the file from the page.
	 */
	public IFile getModelFile() {
		return newFileCreationPage.getModelFile();
	}

	/**
	 * Returns the model file's content type id.
	 * 
	 * @return The model file's content type id.
	 */
	protected String getModelFileContentTypeId() {
		return mmDescriptor.getDefaultContentTypeId();
	}

	/**
	 * Returns the model file's meta model descriptor.
	 * 
	 * @return The model file's meta model descriptor.
	 */
	protected IMetaModelDescriptor getMetaModelDescriptor() {
		return mmDescriptor;
	}

	@Override
	public void dispose() {
		// dispose pages
		try {
			newFileCreationPage.dispose();
			initialObjectCreationPage.dispose();
		} catch (Exception e) {
			Status status = new Status(IStatus.ERROR, Policy.JFACE, IStatus.ERROR, e.getMessage(), e);
			Policy.getLog().log(status);
		}
		super.dispose();
	}
}
