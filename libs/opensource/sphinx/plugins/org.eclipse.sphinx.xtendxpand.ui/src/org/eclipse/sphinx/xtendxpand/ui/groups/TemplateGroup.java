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
import org.eclipse.internal.xpand2.ast.AbstractDefinition;
import org.eclipse.internal.xpand2.ast.Template;
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
import org.eclipse.sphinx.xtendxpand.XpandEvaluationRequest;
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
import org.eclipse.xtend.typesystem.Type;

public class TemplateGroup extends AbstractGroup {

	/**
	 * The template group dialog settings.
	 */
	protected static final String CODE_GEN_SECTION = Activator.getPlugin().getSymbolicName() + ".CODE_GEN_SECTION"; //$NON-NLS-1$
	protected static final String STORE_TEMPLATE_FILE = "TEMPLATE_FILE$"; //$NON-NLS-1$
	protected static final String STORE_SELECTED_DEFINITION = "SELECTED_DEFINITION"; //$NON-NLS-1$

	/**
	 * The template file field.
	 */
	protected StringButtonField templateFileField;

	/**
	 * The definition field.
	 */
	protected ComboField definitionField;

	/**
	 * The definition name field.
	 */
	protected StringField definitionNameField;

	/**
	 * The selected model object.
	 */
	protected EObject modelObject;

	/**
	 * The {@link TypeSystem type system} to be used.
	 */
	protected TypeSystem typeSystem;

	/**
	 * Defined definitions in the template file.
	 */
	private List<AbstractDefinition> definitions;

	public TemplateGroup(String groupName, EObject modelObject, TypeSystem typeSystem) {
		this(groupName, modelObject, typeSystem, null);
	}

	public TemplateGroup(String groupName, EObject modelObject, TypeSystem typeSystem, IDialogSettings dialogSettings) {
		super(groupName, dialogSettings);

		Assert.isNotNull(typeSystem);

		this.modelObject = modelObject;
		this.typeSystem = typeSystem;
	}

	@Override
	protected void doCreateContent(final Composite parent, int numColumns) {
		parent.setLayout(new GridLayout(numColumns, false));

		// Template file field
		templateFileField = new StringButtonField(new IButtonAdapter() {

			@Override
			public void changeControlPressed(IField field) {
				ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(parent.getShell(), new WorkbenchLabelProvider(),
						new WorkbenchContentProvider());
				dialog.setTitle(Messages.label_templateSelection);
				dialog.setMessage(Messages.msg_chooseTemplate);
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
							return IXtendXpandConstants.TEMPLATE_EXTENSION.equals(((IFile) element).getFileExtension());
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
							if (selectedFile.exists() && IXtendXpandConstants.TEMPLATE_EXTENSION.equals(selectedFile.getFileExtension())) {
								return Status.OK_STATUS;
							}
						}
						return new Status(IStatus.ERROR, pluginId, IStatus.ERROR, Messages.msg_chooseTemplateError, null);
					}
				});
				if (dialog.open() == IDialogConstants.OK_ID) {
					IFile file = (IFile) dialog.getFirstResult();
					if (file != null) {
						templateFileField.setText(file.getFullPath().makeRelative().toString());
						updateDefinitionFieldItems(file);
					}
				}
			}
		});
		templateFileField.setButtonLabel(Messages.label_browse);
		templateFileField.setLabelText(Messages.label_templateFile);
		templateFileField.fillIntoGrid(parent, numColumns);
		templateFileField.addFieldListener(new IFieldListener() {

			@Override
			public void dialogFieldChanged(IField field) {
				updateDefinitionFieldItems(getFile(templateFileField.getText()));
				updateDefinitionNameField();
				notifyGroupChanged(templateFileField);
			}
		});

		// Define block field
		definitionField = new ComboField(true);
		definitionField.setLabelText(Messages.label_definition);
		definitionField.fillIntoGrid(parent, numColumns);
		definitionField.addFieldListener(new IFieldListener() {

			@Override
			public void dialogFieldChanged(IField field) {
				updateDefinitionNameField();
				notifyGroupChanged(templateFileField);
			}
		});

		// Definition name field
		definitionNameField = new StringField();
		definitionNameField.setLabelText(Messages.label_definitionName);
		definitionNameField.setEditable(false);
		definitionNameField.fillIntoGrid(parent, numColumns);
	}

	/**
	 * Updates items of define block field after loading selected template file.
	 */
	public void updateDefinitionFieldItems(IFile templateFile) {
		Template template = loadTemplate(templateFile);
		if (template != null) {
			definitions = Arrays.asList(template.getAllDefinitions());
			definitionField.setItems(createDefinitionFieldItems(definitions));
			return;
		}
		definitionField.setItems(new String[0]);
	}

	/**
	 * Creates define block items.
	 */
	protected String[] createDefinitionFieldItems(List<AbstractDefinition> definitions) {
		List<String> result = new ArrayList<String>();
		Type type = typeSystem.getType(modelObject);
		if (type != null) {
			for (AbstractDefinition definition : definitions) {
				// TODO Replace filtering based on target type names by one that takes inheritance hierarchy into
				// account, just as org.eclipse.xpand2.XpandExecutionContextImpl.findDefinition(XpandDefinition[],
				// String, Type, Type[], XpandExecutionContext) does; provide a new helper method similar to
				// org.eclipse.sphinx.xtendxpand.util.XtendXpandUtil.getApplicableFeatures(List<? extends Callable>,
				// Class<?>, String, List<? extends Type>) to XtendXpandUtil for that purpose
				if (type.getName().equals(definition.getTargetType()) || getSimpleTypeName(type).equals(definition.getTargetType())) {
					result.add(definition.getName());
				}
			}
		}
		// TODO Create an empty combo item if result is empty
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Gets the simple name of the given <code>type</type>.
	 */
	protected String getSimpleTypeName(Type type) {
		String typeName = type.getName();
		int idx = typeName.lastIndexOf(IXtendXpandConstants.NS_DELIMITER);
		return idx != -1 && typeName.length() >= idx + IXtendXpandConstants.NS_DELIMITER.length() ? typeName.substring(idx
				+ IXtendXpandConstants.NS_DELIMITER.length()) : typeName;
	}

	public String getDefinitionName() {
		String selectedDefinitionName = getSelectedDefinitionFieldItem();
		if (selectedDefinitionName != null) {
			return XtendXpandUtil.getQualifiedName(getFile(getTemplateFileField().getText()), selectedDefinitionName);
		}
		return ""; //$NON-NLS-1$
	}

	protected void updateDefinitionNameField() {
		IFile templateFile = getFile(templateFileField.getText());
		if (templateFile != null) {
			definitionNameField.setText(getDefinitionName());
		} else {
			definitionNameField.setText("..."); //$NON-NLS-1$
		}
	}

	/**
	 * Loads an Xpand resource.
	 */
	protected Template loadTemplate(final IFile templateFile) {
		if (templateFile != null && templateFile.exists() && IXtendXpandConstants.TEMPLATE_EXTENSION.equals(templateFile.getFileExtension())) {
			final IXtendXpandProject project = org.eclipse.xtend.shared.ui.Activator.getExtXptModelManager().findProject(templateFile);
			if (project != null) {
				final IXtendXpandResource resource = project.findXtendXpandResource(templateFile);
				if (resource != null) {
					return (Template) resource.getExtXptResource();
				}
			}
		}
		return null;
	}

	@Override
	public boolean isGroupComplete() {
		IFile templateFile = getFile(getTemplateFileField().getText());
		if (templateFile != null) {
			return templateFile.exists() && getDefinitionField().getSelectionIndex() != -1;
		}
		return false;
	}

	/**
	 * Gets the file located at the given full path or returns null.
	 */
	public IFile getFile(String fullPath) {
		if (fullPath != null && fullPath.length() > 0) {
			Path path = new Path(fullPath);
			if (path.segmentCount() > 1) {
				return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			}
		}
		return null;
	}

	public StringButtonField getTemplateFileField() {
		return templateFileField;
	}

	public ComboField getDefinitionField() {
		return definitionField;
	}

	public String getSelectedDefinitionFieldItem() {
		if (definitionField != null && !definitionField.getComboControl().isDisposed()) {
			String[] items = definitionField.getItems();
			int selectionIndex = definitionField.getSelectionIndex();
			if (items.length > 0 && selectionIndex != -1) {
				return items[selectionIndex];
			}
		}
		return null;
	}

	public StringField getDefinitionNameField() {
		return definitionNameField;
	}

	public List<AbstractDefinition> getDefinitions() {
		return definitions;
	}

	public Collection<XpandEvaluationRequest> getXpandEvaluationRequests() {
		List<XpandEvaluationRequest> requests = new ArrayList<XpandEvaluationRequest>();
		if (modelObject != null) {
			String definitionName = getDefinitionName();
			if (definitionName != null && definitionName.length() > 0) {
				requests.add(new XpandEvaluationRequest(definitionName, modelObject));
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
		String templateFile = getTemplateFileFromDialogSettings();
		if (templateFile != null) {
			templateFileField.setText(templateFile);
			updateDefinitionFieldItems(getFile(templateFile));
			String definition = getDefinitionNameFromDialogSettings();
			if (definition != null) {
				definitionField.selectItem(definition);
			}
		}
	}

	public String getTemplateFileFromDialogSettings() {
		String result = null;
		String templateFileDialogSettingsKey = getTemplateFileDialogSettingsKey(modelObject);
		IDialogSettings templateFileSection = getTemplateFileSection();
		if (templateFileSection != null) {
			String templateFilePath = templateFileSection.get(templateFileDialogSettingsKey);
			if (templateFilePath != null) {
				IFile templateFile = getFile(templateFilePath);
				if (templateFile != null && templateFile.exists()) {
					result = templateFilePath;
				}
			}
		}
		return result;
	}

	public String getDefinitionNameFromDialogSettings() {
		String result = null;
		IDialogSettings templateFileSection = getTemplateFileSection();
		if (templateFileSection != null) {
			result = templateFileSection.get(STORE_SELECTED_DEFINITION);
		}
		return result;
	}

	protected IDialogSettings getTemplateFileSection() {
		IDialogSettings result = null;
		String templateFileDialogSettingsKey = getTemplateFileDialogSettingsKey(modelObject);
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings != null) {
			IDialogSettings section = dialogSettings.getSection(CODE_GEN_SECTION);
			if (section != null) {
				result = section.getSection(templateFileDialogSettingsKey);
			}
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
		if (settings != null) {
			String key = getTemplateFileDialogSettingsKey(modelObject);
			IDialogSettings topLevelSection = settings.getSection(CODE_GEN_SECTION);
			if (topLevelSection == null) {
				topLevelSection = settings.addNewSection(CODE_GEN_SECTION);
			}
			if (templateFileField.getText().trim().length() != 0) {
				IDialogSettings templateFileSection = topLevelSection.getSection(key);
				if (templateFileSection == null) {
					templateFileSection = topLevelSection.addNewSection(key);
				}
				templateFileSection.put(key, templateFileField.getText());
				String[] items = definitionField.getItems();
				int selectionIndex = definitionField.getSelectionIndex();
				if (items.length > 0 && selectionIndex != -1) {
					templateFileSection.put(STORE_SELECTED_DEFINITION, items[selectionIndex]);
				}
			}
		}
	}

	protected String getTemplateFileDialogSettingsKey(EObject object) {
		Assert.isNotNull(object);
		return TemplateGroup.STORE_TEMPLATE_FILE + EcoreResourceUtil.getURI(object).toString();
	}
}
