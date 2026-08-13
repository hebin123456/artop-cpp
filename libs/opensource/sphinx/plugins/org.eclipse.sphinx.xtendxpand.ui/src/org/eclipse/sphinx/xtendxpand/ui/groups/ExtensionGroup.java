/**
 * <copyright>
 *
 * Copyright (c) 2011 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [343844] Enable multiple Xtend MetaModels to be configured on BasicM2xAction, M2xConfigurationWizard, and Xtend/Xpand/CheckJob
 *     itemis - [357813] Risk of NullPointerException when transforming models using M2MConfigurationWizard
 *
 * </copyright>
 */
package org.eclipse.sphinx.xtendxpand.ui.groups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.internal.xtend.xtend.ast.Extension;
import org.eclipse.internal.xtend.xtend.ast.ExtensionFile;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.sphinx.emf.mwe.IXtendXpandConstants;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.platform.ui.fields.ComboField;
import org.eclipse.sphinx.platform.ui.fields.IField;
import org.eclipse.sphinx.platform.ui.fields.IFieldListener;
import org.eclipse.sphinx.platform.ui.fields.StringButtonField;
import org.eclipse.sphinx.platform.ui.fields.StringField;
import org.eclipse.sphinx.platform.ui.fields.adapters.IButtonAdapter;
import org.eclipse.sphinx.platform.ui.groups.AbstractGroup;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.xtendxpand.XtendEvaluationRequest;
import org.eclipse.sphinx.xtendxpand.ui.internal.Activator;
import org.eclipse.sphinx.xtendxpand.ui.internal.messages.Messages;
import org.eclipse.sphinx.xtendxpand.util.XtendXpandUtil;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;
import org.eclipse.xtend.expression.TypeSystem;
import org.eclipse.xtend.shared.ui.core.IXtendXpandProject;
import org.eclipse.xtend.shared.ui.core.IXtendXpandResource;
import org.eclipse.xtend.typesystem.Callable;
import org.eclipse.xtend.typesystem.Type;

public class ExtensionGroup extends AbstractGroup {

	/**
	 * The extension group dialog settings.
	 */
	protected static final String M2M_TRANSFORM_SECTION = Activator.getPlugin().getSymbolicName() + ".M2M_TRANSFORM_SECTION"; //$NON-NLS-1$
	protected static final String STORE_EXTENSION_FILE = "EXTENSION_FILE$"; //$NON-NLS-1$
	protected static final String STORE_SELECTED_FUNCTION = "SELECTED_FUNCTION"; //$NON-NLS-1$

	/**
	 * The Xtend file field.
	 */
	protected StringButtonField extensionFileField;

	/**
	 * The function to be used in the relevant Xtend file.
	 */
	protected ComboField functionField;

	/**
	 * The extension name field.
	 */
	protected StringField extensionNameField;

	/**
	 * The selected model object.
	 */
	protected EObject modelObject;

	/**
	 * The {@link TypeSystem type system} to be used.
	 */
	protected TypeSystem typeSystem;

	/**
	 * Defined extensions in the relevant Xtend file.
	 */
	private List<Extension> extensions;

	public ExtensionGroup(String groupName, EObject modelObject, TypeSystem typeSystem) {
		this(groupName, modelObject, typeSystem, null);
	}

	public ExtensionGroup(String groupName, EObject modelObject, TypeSystem typeSystem, IDialogSettings dialogSettings) {
		super(groupName, dialogSettings);

		Assert.isNotNull(typeSystem);

		this.modelObject = modelObject;
		this.typeSystem = typeSystem;
	}

	@Override
	protected void doCreateContent(final Composite parent, int numColumns) {
		parent.setLayout(new GridLayout(numColumns, false));

		// Extension file field
		extensionFileField = new StringButtonField(new IButtonAdapter() {

			@Override
			public void changeControlPressed(IField field) {
				ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(parent.getShell(), new WorkbenchLabelProvider(),
						new WorkbenchContentProvider());
				dialog.setTitle(Messages.label_extensionSelection);
				dialog.setMessage(Messages.msg_chooseExtension);
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
				IFile modelFile = EcorePlatformUtil.getFile(modelObject);
				if (modelFile != null) {
					dialog.setInitialSelection(modelFile.getProject());
				}
				dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
				dialog.addFilter(new ViewerFilter() {

					@Override
					public boolean select(Viewer viewer, Object parentElement, Object element) {
						if (element instanceof IFile) {
							return IXtendXpandConstants.EXTENSION_EXTENSION.equals(((IFile) element).getFileExtension());
						}
						if (element instanceof IResource) {
							return !ExtendedPlatform.isPlatformPrivateResource((IResource) element);
						}
						return true;
					}
				});
				dialog.setValidator(new ISelectionStatusValidator() {

					@Override
					public IStatus validate(Object[] selection) {
						int nSelected = selection.length;
						String pluginId = Activator.getPlugin().getSymbolicName();
						if (nSelected == 1 && selection[0] instanceof IFile) {
							IFile selectedFile = (IFile) selection[0];
							if (selectedFile.exists() && IXtendXpandConstants.EXTENSION_EXTENSION.equals(selectedFile.getFileExtension())) {
								return Status.OK_STATUS;
							}
						}
						return new Status(IStatus.ERROR, pluginId, IStatus.ERROR, Messages.msg_chooseExtensionError, null);
					}
				});
				if (dialog.open() == IDialogConstants.OK_ID) {
					IFile file = (IFile) dialog.getFirstResult();
					if (file != null) {
						extensionFileField.setText(file.getFullPath().makeRelative().toString());
						updateFunctionFieldItems(file);
					}
				}
			}
		});
		extensionFileField.setButtonLabel(Messages.label_browse);
		extensionFileField.setLabelText(Messages.label_extensionFile);
		extensionFileField.fillIntoGrid(parent, numColumns);
		extensionFileField.addFieldListener(new IFieldListener() {

			@Override
			public void dialogFieldChanged(IField field) {
				updateFunctionFieldItems(getFile(extensionFileField.getText()));
				updateExtensionNameField();
				notifyGroupChanged(extensionFileField);
			}
		});

		// Function name field
		functionField = new ComboField(true);
		functionField.setLabelText(Messages.label_functionFieldName);
		functionField.fillIntoGrid(parent, numColumns);
		functionField.addFieldListener(new IFieldListener() {

			@Override
			public void dialogFieldChanged(IField field) {
				updateExtensionNameField();
				notifyGroupChanged(extensionFileField);
			}
		});

		// Extension name field
		extensionNameField = new StringField();
		extensionNameField.setLabelText(Messages.label_extensionName);
		extensionNameField.setEditable(false);
		extensionNameField.fillIntoGrid(parent, numColumns);
	}

	/**
	 * Updates items of extensions combo field after loading selected Xtend file.
	 */
	public void updateFunctionFieldItems(IFile templateFile) {
		ExtensionFile extensionFile = loadExtensionFile(templateFile);
		if (extensionFile != null) {
			extensions = extensionFile.getExtensions();
			functionField.setItems(createFunctionFieldItems(extensions));
			return;
		}
		functionField.setItems(new String[0]);
	}

	/**
	 * Creates extension items.
	 */
	protected String[] createFunctionFieldItems(List<Extension> extensions) {
		List<String> result = new ArrayList<String>();
		Type type = typeSystem.getType(modelObject);
		if (type != null) {
			for (Callable extension : XtendXpandUtil.getApplicableFeatures(extensions, Extension.class, null, Arrays.asList(type))) {
				result.add(((Extension) extension).getName());
			}
		}
		// TODO Create an empty combo item if result is empty
		return result.toArray(new String[result.size()]);
	}

	public String getExtensionName() {
		String selectedFunction = getSelectedFunctionFieldItem();
		if (selectedFunction != null) {
			return XtendXpandUtil.getQualifiedName(getFile(getExtensionFileField().getText()), selectedFunction);
		}
		return ""; //$NON-NLS-1$
	}

	protected void updateExtensionNameField() {
		IFile templateFile = getFile(extensionFileField.getText());
		if (templateFile != null) {
			extensionNameField.setText(getExtensionName());
		} else {
			extensionNameField.setText("..."); //$NON-NLS-1$
		}
	}

	/**
	 * Loads an Xtend resource.
	 */
	protected ExtensionFile loadExtensionFile(final IFile extensionFile) {
		if (extensionFile != null && extensionFile.exists() && IXtendXpandConstants.EXTENSION_EXTENSION.equals(extensionFile.getFileExtension())) {
			final IXtendXpandProject project = org.eclipse.xtend.shared.ui.Activator.getExtXptModelManager().findProject(extensionFile);
			if (project != null) {
				final IXtendXpandResource resource = project.findXtendXpandResource(extensionFile);
				if (resource != null) {
					return (ExtensionFile) resource.getExtXptResource();
				}
			}
		}
		return null;
	}

	@Override
	public boolean isGroupComplete() {
		IFile extensionFile = getFile(getExtensionFileField().getText());
		if (extensionFile != null) {
			return extensionFile.exists() && getFunctionField().getSelectionIndex() != -1;
		}
		return false;
	}

	/**
	 * Gets the file located at the given full path or returns null.
	 */
	protected IFile getFile(String fullPath) {
		if (fullPath != null && fullPath.length() > 0) {
			Path path = new Path(fullPath);
			if (path.segmentCount() > 1) {
				return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			}
		}
		return null;
	}

	public String getSelectedFunctionFieldItem() {
		if (functionField != null && !functionField.getComboControl().isDisposed()) {
			String[] items = functionField.getItems();
			int selectionIndex = functionField.getSelectionIndex();
			if (items.length > 0 && selectionIndex != -1) {
				return items[selectionIndex];
			}
		}
		return null;
	}

	public StringButtonField getExtensionFileField() {
		return extensionFileField;
	}

	public ComboField getFunctionField() {
		return functionField;
	}

	public List<Extension> getExtensions() {
		return extensions;
	}

	public Collection<XtendEvaluationRequest> getXtendEvaluationRequests() {
		List<XtendEvaluationRequest> requests = new ArrayList<XtendEvaluationRequest>();
		if (modelObject != null) {
			String extensionName = getExtensionName();
			if (extensionName != null && extensionName.length() > 0) {
				requests.add(new XtendEvaluationRequest(extensionName, modelObject));
			}
		}
		return requests;
	}

	/**
	 * Loads the template path and the define block from the dialog settings. Must call
	 * {@link #setDialogSettings(IDialogSettings)} before calling this method.
	 */
	@Override
	protected void loadGroupSettings() {
		String extensionFile = getExtensionFileFromDialogSettings();
		if (extensionFile != null) {
			extensionFileField.setText(extensionFile);
			updateFunctionFieldItems(getFile(extensionFile));
			String functionName = getFunctionFromDialogSettings();
			if (functionName != null) {
				functionField.selectItem(functionName);
			}
		}
	}

	public String getExtensionFileFromDialogSettings() {
		String result = null;
		String key = getExtensionFileDialogSettingsKey(modelObject);
		IDialogSettings extensionFileSection = getExtensionFileSection();
		if (extensionFileSection != null) {
			String extensionFilePath = extensionFileSection.get(key);
			if (extensionFilePath != null) {
				IFile extensionFile = getFile(extensionFilePath);
				if (extensionFile != null && extensionFile.exists()) {
					result = extensionFilePath;
				}
			}
		}
		return result;
	}

	public String getFunctionFromDialogSettings() {
		String result = null;
		IDialogSettings extensionFileSection = getExtensionFileSection();
		if (extensionFileSection != null) {
			result = extensionFileSection.get(STORE_SELECTED_FUNCTION);
		}
		return result;
	}

	protected IDialogSettings getExtensionFileSection() {
		IDialogSettings result = null;
		String key = getExtensionFileDialogSettingsKey(modelObject);
		IDialogSettings section = getDialogSettings().getSection(M2M_TRANSFORM_SECTION);
		if (section != null) {
			result = section.getSection(key);
		}
		return result;
	}

	/**
	 * Saves, using the {@link DialogSettings} dialogSettings, the state of the different fields of this group.
	 *
	 * @param templatePathDialogSettingsKey
	 * @see #setDialogSettings(IDialogSettings)
	 */
	@Override
	public void saveGroupSettings() {
		IDialogSettings settings = getDialogSettings();
		String extensionFileDialogSettingsKey = getExtensionFileDialogSettingsKey(modelObject);
		if (settings != null) {
			IDialogSettings topLevelSection = settings.getSection(M2M_TRANSFORM_SECTION);
			if (topLevelSection == null) {
				topLevelSection = settings.addNewSection(M2M_TRANSFORM_SECTION);
			}
			if (extensionFileField.getText().trim().length() != 0) {
				IDialogSettings extensionFileSection = topLevelSection.getSection(extensionFileDialogSettingsKey);
				if (extensionFileSection == null) {
					extensionFileSection = topLevelSection.addNewSection(extensionFileDialogSettingsKey);
				}
				extensionFileSection.put(extensionFileDialogSettingsKey, extensionFileField.getText());
				String[] items = functionField.getItems();
				int selectionIndex = functionField.getSelectionIndex();
				if (items.length > 0 && selectionIndex != -1) {
					extensionFileSection.put(STORE_SELECTED_FUNCTION, items[selectionIndex]);
				}
			}
		}
	}

	protected String getExtensionFileDialogSettingsKey(EObject object) {
		Assert.isNotNull(object);
		return ExtensionGroup.STORE_EXTENSION_FILE + EcoreResourceUtil.getURI(object).toString();
	}
}
