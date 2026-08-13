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
package org.eclipse.sphinx.graphiti.workspace.ui.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.sphinx.graphiti.workspace.ui.internal.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;

/**
 * A generic implementation of the EMF model creation wizard.
 */

// TODO : Move this class and properties in plugin.properties to a separate plugin

public abstract class AbstractModelNewWizard extends Wizard implements INewWizard {

	protected static final String MODEL_WIZARD_NAME = "Default"; //$NON-NLS-1$
	protected static final List<String> FILE_EXTENSIONS = Collections.singletonList("default"); //$NON-NLS-1$

	/**
	 * The handels the activator of the edit plugin of the considered metamodel
	 */
	protected EMFPlugin editPlugin;

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
	 * Remember the workbench during initialization.
	 */
	protected IWorkbench workbench;

	/**
	 * Caches the names of the types that can be created as the root object.
	 */
	protected List<String> initialObjectNames;

	/**
	 * Default constructor initialize metamodel package and metamodel factory
	 */
	protected AbstractModelNewWizard() {
		setEditPluginActivator();
		initFileExtensions();
		initMetamodelPackage();
		initMetamodelFactory();
	}

	/**
	 * This just records the information.
	 */
	@Override
	public abstract void init(IWorkbench workbench, IStructuredSelection selection);

	/**
	 * This sets the edit plugin activator
	 */
	protected abstract void setEditPluginActivator();

	/**
	 * This initializes the files extensions
	 */
	protected abstract void initFileExtensions();

	/**
	 * This initializes the metamodel package
	 */
	protected abstract void initMetamodelPackage();

	/**
	 * This initializes the metamodel factory
	 */
	protected abstract void initMetamodelFactory();

	/**
	 * This creates the EMF resource and later called from performFinish()
	 */
	protected boolean createEMFResource() {
		try {
			// Get the file.
			final IFile modelFile = getModelFile();
			// Do the work within an operation.
			WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor progressMonitor) {
					try {
						// Create a resource set
						ResourceSet resourceSet = new ResourceSetImpl();
						URI fileURI = URI.createPlatformResourceURI(modelFile.getFullPath().toString(), true);
						Resource resource = resourceSet.createResource(fileURI);

						// Add the initial model object to the contents.
						createInitialModel();

						if (modelRoot != null) {
							resource.getContents().add(modelRoot);
						}
						// Save the contents of the resource to the file system.
						Map<Object, Object> options = new HashMap<Object, Object>();
						options.put(XMLResource.OPTION_ENCODING, initialObjectCreationPage.getEncoding());
						resource.save(options);
					} catch (Exception exception) {
						Activator.INSTANCE.log(exception);
					} finally {
						progressMonitor.done();
					}
				}
			};

			getContainer().run(false, false, operation);

		} catch (Exception exception) {
			Activator.INSTANCE.log(exception);
			return false;
		}
		return true;
	}

	/**
	 * This creates the EMF resource given an URI and a content type
	 */
	protected boolean createEMFResource(final URI uri, final String contentType) {
		try {
			// Do the work within an operation.
			WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor progressMonitor) {
					try {
						// Create the resource given the URI
						ResourceSet resourceSet = new ResourceSetImpl();
						Resource resource = resourceSet.createResource(uri, contentType);

						// Add the initial model object to the contents.
						createInitialModel();

						if (modelRoot != null) {
							resource.getContents().add(modelRoot);
						}
						// Save the contents of the resource to the file system.
						Map<Object, Object> options = new HashMap<Object, Object>();
						options.put(XMLResource.OPTION_ENCODING, initialObjectCreationPage.getEncoding());
						resource.save(options);
					} catch (Exception exception) {
						Activator.INSTANCE.log(exception);
					} finally {
						progressMonitor.done();
					}
				}
			};

			getContainer().run(false, false, operation);

		} catch (Exception exception) {
			Activator.INSTANCE.log(exception);
			return false;
		}
		return true;
	}

	/**
	 * Do the work after everything is specified.
	 */
	@Override
	public boolean performFinish() {

		// Create the EMF resource
		if (!createEMFResource()) {
			return false;
		}

		// Select the new file resource in the current view.
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();
		final IWorkbenchPart activePart = page.getActivePart();
		if (activePart instanceof ISetSelectionTarget) {
			final ISelection targetSelection = new StructuredSelection(getModelFile());
			getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					((ISetSelectionTarget) activePart).selectReveal(targetSelection);
				}
			});
		}
		// Open an editor on the new file.
		try {
			page.openEditor(new FileEditorInput(getModelFile()),
					workbench.getEditorRegistry().getDefaultEditor(getModelFile().getFullPath().toString()).getId());
		} catch (PartInitException exception) {
			MessageDialog.openError(workbenchWindow.getShell(), Activator.INSTANCE.getString("_UI_OpenEditorError_label"), exception.getMessage()); //$NON-NLS-1$
			return false;
		}

		return true;
	}

	/**
	 * Create a new model. Subclasses may reimplement.
	 */
	protected void createInitialModel() {
		EClass eClass = (EClass) metamodelPackage.getEClassifier(initialObjectCreationPage.getInitialObjectName());
		modelRoot = metamodelFactory.create(eClass);
	}

	/**
	 * Returns the names of the types that can be created as the root object. Subclasses may reimplement.
	 */
	protected Collection<String> getInitialObjectNames() {
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
	 * This is the one page of the wizard.
	 */
	public class AbstractModelNewFileCreationPage extends WizardNewFileCreationPage {

		/**
		 * The file extensions
		 */
		private List<String> FILE_EXTENSIONS;

		/**
		 * Pass in the selection
		 */
		public AbstractModelNewFileCreationPage(String pageId, IStructuredSelection selection) {
			super(pageId, selection);
		}

		public void setFileExtensions(List<String> fileExtension) {
			FILE_EXTENSIONS = fileExtension;
		}

		public List<String> getFileExtensions() {
			return FILE_EXTENSIONS;
		}

		/**
		 * The framework calls this to see if the file is correct.
		 */
		@Override
		protected boolean validatePage() {
			if (super.validatePage()) {
				String extension = new Path(getFileName()).getFileExtension();
				if (extension == null || !FILE_EXTENSIONS.contains(extension)) {
					String key = FILE_EXTENSIONS.size() > 1 ? "_WARN_FilenameExtensions" : "_WARN_FilenameExtension"; //$NON-NLS-1$ //$NON-NLS-2$
					setErrorMessage(Activator.INSTANCE.getString(key, new Object[] { FILE_EXTENSIONS }));
					return false;
				}
				return true;
			}
			return false;
		}

		public IFile getModelFile() {
			return ResourcesPlugin.getWorkspace().getRoot().getFile(getContainerFullPath().append(getFileName()));
		}

	}

	/**
	 * This is the page where the type of object to create is selected.
	 */
	public class AbstractModelInitialObjectCreationPage extends WizardPage {

		protected Combo initialObjectField;
		protected List<String> encodings;
		protected Combo encodingField;

		public AbstractModelInitialObjectCreationPage(String pageId) {
			super(pageId);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			{
				GridLayout layout = new GridLayout();
				layout.numColumns = 1;
				layout.verticalSpacing = 12;
				composite.setLayout(layout);

				GridData data = new GridData();
				data.verticalAlignment = GridData.FILL;
				data.grabExcessVerticalSpace = true;
				data.horizontalAlignment = GridData.FILL;
				composite.setLayoutData(data);
			}

			Label containerLabel = new Label(composite, SWT.LEFT);
			{
				containerLabel.setText(Activator.INSTANCE.getString("_UI_ModelObject")); //$NON-NLS-1$

				GridData data = new GridData();
				data.horizontalAlignment = GridData.FILL;
				containerLabel.setLayoutData(data);
			}

			initialObjectField = new Combo(composite, SWT.BORDER);
			{
				GridData data = new GridData();
				data.horizontalAlignment = GridData.FILL;
				data.grabExcessHorizontalSpace = true;
				initialObjectField.setLayoutData(data);
			}

			for (String objectName : getInitialObjectNames()) {
				initialObjectField.add(getLabel(objectName));
			}

			if (initialObjectField.getItemCount() == 1) {
				initialObjectField.select(0);
			}
			initialObjectField.addModifyListener(validator);

			Label encodingLabel = new Label(composite, SWT.LEFT);
			{
				encodingLabel.setText(Activator.INSTANCE.getString("_UI_XMLEncoding")); //$NON-NLS-1$

				GridData data = new GridData();
				data.horizontalAlignment = GridData.FILL;
				encodingLabel.setLayoutData(data);
			}
			encodingField = new Combo(composite, SWT.BORDER);
			{
				GridData data = new GridData();
				data.horizontalAlignment = GridData.FILL;
				data.grabExcessHorizontalSpace = true;
				encodingField.setLayoutData(data);
			}

			for (String encoding : getEncodings()) {
				encodingField.add(encoding);
			}

			encodingField.select(0);
			encodingField.addModifyListener(validator);

			setPageComplete(validatePage());
			setControl(composite);
		}

		protected ModifyListener validator = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		};

		protected boolean validatePage() {
			return getInitialObjectName() != null && getEncodings().contains(encodingField.getText());
		}

		@Override
		public void setVisible(boolean visible) {
			super.setVisible(visible);
			if (visible) {
				if (initialObjectField.getItemCount() == 1) {
					initialObjectField.clearSelection();
					encodingField.setFocus();
				} else {
					encodingField.clearSelection();
					initialObjectField.setFocus();
				}
			}
		}

		public String getInitialObjectName() {
			String label = initialObjectField.getText();

			for (String name : getInitialObjectNames()) {
				if (getLabel(name).equals(label)) {
					return name;
				}
			}
			return null;
		}

		public String getEncoding() {
			return encodingField.getText();
		}

		protected String getLabel(String typeName) {
			try {
				return editPlugin.getString("_UI_" + typeName + "_type"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (MissingResourceException mre) {
				Activator.INSTANCE.log(mre);
			}
			return typeName;
		}

		protected Collection<String> getEncodings() {
			if (encodings == null) {
				encodings = new ArrayList<String>();
				for (StringTokenizer stringTokenizer = new StringTokenizer(Activator.INSTANCE.getString("_UI_XMLEncodingChoices")); stringTokenizer.hasMoreTokens();) //$NON-NLS-1$
				{
					encodings.add(stringTokenizer.nextToken());
				}
			}
			return encodings;
		}
	}

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

				// This gives us a directory...
				if (selectedResource instanceof IFolder || selectedResource instanceof IProject) {
					// Set this for the container.
					newFileCreationPage.setContainerFullPath(selectedResource.getFullPath());

					// Make up a unique new name here.
					String defaultModelBaseFilename = Activator.INSTANCE.getString("_UI_ModelEditorFilenameDefaultBase"); //$NON-NLS-1$
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

	protected void createInitialObjectCreationPage(String MODEL_WIZARD_NAME) {
		// Create the initial object selection page
		initialObjectCreationPage = new AbstractModelInitialObjectCreationPage("Whatever2"); //$NON-NLS-1$
		initialObjectCreationPage.setTitle(Activator.INSTANCE.getString("_UI_ModelWizard_label", new Object[] { MODEL_WIZARD_NAME })); //$NON-NLS-1$
		initialObjectCreationPage.setDescription(Activator.INSTANCE.getString("_UI_Wizard_initial_object_description")); //$NON-NLS-1$

	}

	protected void createFileCreationPage(String MODEL_WIZARD_NAME, List<String> FILE_EXTENSIONS) {
		// Create the creation page
		newFileCreationPage = new AbstractModelNewFileCreationPage("Whatever", selection); //$NON-NLS-1$
		newFileCreationPage.setTitle(Activator.INSTANCE.getString("_UI_ModelWizard_label", new Object[] { MODEL_WIZARD_NAME })); //$NON-NLS-1$
		newFileCreationPage.setDescription(Activator.INSTANCE.getString("_UI_ModelWizard_description", new Object[] { MODEL_WIZARD_NAME })); //$NON-NLS-1$
		newFileCreationPage.setFileName(Activator.INSTANCE.getString("_UI_ModelEditorFilenameDefaultBase") + "." + FILE_EXTENSIONS.get(0)); //$NON-NLS-1$ //$NON-NLS-2$
		newFileCreationPage.setFileExtensions(FILE_EXTENSIONS);
	}

	/**
	 * Get the file from the page.
	 */
	public IFile getModelFile() {
		return newFileCreationPage.getModelFile();
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
