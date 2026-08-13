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
 *     itemis - [393310] Viewer input for GenericContentsTreeSection should be calculated using content provider
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [484821] Newly created model elements are no longer selected in current viewer of BasicTransactionalFormEditor
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.editors.forms.pages;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.ui.provider.TransactionalAdapterFactoryContentProvider;
import org.eclipse.emf.transaction.ui.provider.TransactionalAdapterFactoryLabelProvider;
import org.eclipse.emf.workspace.EMFCommandOperation;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.sphinx.emf.editors.forms.BasicTransactionalFormEditor;
import org.eclipse.sphinx.emf.editors.forms.internal.Activator;
import org.eclipse.sphinx.emf.editors.forms.sections.IFormSection;
import org.eclipse.sphinx.emf.ui.forms.messages.IFormMessage;
import org.eclipse.sphinx.emf.ui.forms.messages.IFormMessageProvider;
import org.eclipse.sphinx.emf.validation.IValidationProblemMarkersChangeListener;
import org.eclipse.sphinx.emf.validation.ValidationProblemMarkersChangeNotifier;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

public abstract class AbstractFormPage extends FormPage {

	private static final String EXTP_FORM_MESSAGE_PROVIDERS = "org.eclipse.sphinx.emf.editors.forms.formMessageProvider"; //$NON-NLS-1$
	private static final String NODE_PROVIDER = "formMessageProvider"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	protected Object pageInput = null;

	// TODO Refactor to enum-based creation status
	private boolean creating = false;
	private boolean created = false;

	private List<IFormSection> sections = new ArrayList<IFormSection>();
	private IFormSection activeSection;

	private IContentProvider contentProvider;
	private IBaseLabelProvider labelProvider;

	private List<IFormMessageProvider> messageProviders = new ArrayList<IFormMessageProvider>();

	protected IPropertyListener inputChangeListener = new IPropertyListener() {
		@Override
		public void propertyChanged(Object source, int propId) {
			if (source.equals(getEditor()) && AbstractFormPage.this.equals(((FormEditor) source).getActivePageInstance())
					&& propId == IWorkbenchPartConstants.PROP_INPUT) {
				getSite().getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (!created) {
							createFormContent(getManagedForm());
						} else {
							setPageInput(getPageInputFromEditor());
						}
					}
				});
			}
		}
	};

	protected IPageChangedListener pageChangedListener = new IPageChangedListener() {
		@Override
		public void pageChanged(PageChangedEvent event) {
			if (event.getSelectedPage().equals(AbstractFormPage.this)) {
				getSite().getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						refreshPage();
					}
				});
			}
		}
	};

	protected IOperationHistoryListener operationHistoryListener = new IOperationHistoryListener() {
		@Override
		public void historyNotification(OperationHistoryEvent event) {
			switch (event.getEventType()) {
			case OperationHistoryEvent.DONE:
			case OperationHistoryEvent.UNDONE:
			case OperationHistoryEvent.REDONE:
				if (AbstractFormPage.this.equals(getEditor().getActivePageInstance())) {
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							refreshPage();
						}
					});
				}
				break;
			}
		}
	};

	protected IValidationProblemMarkersChangeListener validationProblemMarkersChangeListener = new IValidationProblemMarkersChangeListener() {
		@Override
		public void validationProblemMarkersChanged(final EventObject event) {
			if (AbstractFormPage.this.equals(getEditor().getActivePageInstance())) {
				if ((EObject) event.getSource() == pageInput) {
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							refreshMessages();
						}
					});
				}
			}
		}
	};

	public AbstractFormPage(FormEditor editor, String title) {
		this(editor, title.replaceAll("[^A-Z]", ""), title); //$NON-NLS-1$//$NON-NLS-2$
	}

	public AbstractFormPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
		getEditor().addPropertyListener(inputChangeListener);
		getEditor().addPageChangedListener(pageChangedListener);
		IOperationHistory operationHistory = getTransactionalFormEditor().getOperationHistory();
		if (operationHistory != null) {
			operationHistory.addOperationHistoryListener(operationHistoryListener);
		}
		ValidationProblemMarkersChangeNotifier.INSTANCE.addListener(validationProblemMarkersChangeListener);

		// TODO Move to separate registry class
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(EXTP_FORM_MESSAGE_PROVIDERS);
		messageProviders = new ArrayList<IFormMessageProvider>();
		for (IConfigurationElement cfgElem : extension.getConfigurationElements()) {
			if (NODE_PROVIDER.equals(cfgElem.getName())) {
				try {
					IFormMessageProvider msgProvider = (IFormMessageProvider) cfgElem.createExecutableExtension(ATTR_CLASS);
					messageProviders.add(msgProvider);
				} catch (CoreException ex) {
					PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
				}
			}
		}
	}

	/**
	 * @return The parent transactional form editor of this form page.
	 */
	public BasicTransactionalFormEditor getTransactionalFormEditor() {
		return (BasicTransactionalFormEditor) getEditor();
	}

	public AdapterFactoryItemDelegator getItemDelegator() {
		return getTransactionalFormEditor().getItemDelegator();
	}

	public IContentProvider getContentProvider() {
		if (contentProvider == null) {
			contentProvider = createContentProvider();
		}
		return contentProvider;
	}

	protected IContentProvider createContentProvider() {
		AdapterFactory adapterFactory = getTransactionalFormEditor().getAdapterFactory();
		if (adapterFactory != null) {
			EditingDomain editingDomain = getTransactionalFormEditor().getEditingDomain();
			if (editingDomain instanceof TransactionalEditingDomain) {
				return new TransactionalAdapterFactoryContentProvider((TransactionalEditingDomain) editingDomain, adapterFactory);
			}
		}
		return null;
	}

	public IBaseLabelProvider getLabelProvider() {
		if (labelProvider == null) {
			labelProvider = createLabelProvider();
		}
		return labelProvider;
	}

	protected IBaseLabelProvider createLabelProvider() {
		AdapterFactory adapterFactory = getTransactionalFormEditor().getAdapterFactory();
		if (adapterFactory != null) {
			EditingDomain editingDomain = getTransactionalFormEditor().getEditingDomain();
			if (editingDomain instanceof TransactionalEditingDomain) {
				return new TransactionalAdapterFactoryLabelProvider((TransactionalEditingDomain) editingDomain, adapterFactory);
			}
		}
		return null;
	}

	/**
	 * Returns the list of sections added to this page by calling {@link AbstractFormPage#addSection(IFormSection)}
	 *
	 * @return
	 */
	public List<IFormSection> getSections() {
		return sections;
	}

	/**
	 * @deprecated Use {@link #getSections()} instead.
	 */
	@Deprecated
	public List<IFormSection> getFormSections() {
		return getSections();
	}

	protected void addSection(IFormSection section) {
		if (section != null) {
			sections.add(section);
		}
	}

	/**
	 * Sets the active section on this page.
	 *
	 * @param section
	 */
	public void setActiveSection(IFormSection section) {
		activeSection = section;
	}

	/**
	 * @return the active section in this page (e.g. the section which have the focus).
	 */
	public IFormSection getActiveSection() {
		return activeSection;
	}

	protected Object getPageInputFromEditor() {
		return getTransactionalFormEditor().getEditorInputObject();
	}

	protected void setPageInput(Object pageInput) {
		// Initialize page input
		this.pageInput = pageInput;

		// Propagate new page input to sections
		for (IFormSection section : sections) {
			section.setSectionInput(pageInput);
		}
	}

	protected boolean canCreateFormContent() {
		return pageInput != null;
	}

	@Override
	protected final synchronized void createFormContent(final IManagedForm managedForm) {
		if (!created && !creating) {
			// Set creating flag immediately to true in order to avoid that creation is invoked multiple times by the
			// same thread (different threads get blocked due to the synchronized keyword but the same thread isn't
			// because synchronization is reentrant; see
			// http://java.sun.com/docs/books/tutorial/essential/concurrency/locksync.html for
			// details)
			creating = true;

			// Initialize page input
			setPageInput(getPageInputFromEditor());
			if (managedForm != null && canCreateFormContent()) {
				// Set form title
				managedForm.getForm().setText(getTitle());

				// Create form content
				doCreateFormContent(managedForm);

				// Lay out form content to make sure that it gets visible after deferred creation
				managedForm.getForm().getBody().layout(true);

				created = true;

				// Display validation problem messages
				refreshMessages();
			}

			creating = false;
		}
	}

	protected abstract void doCreateFormContent(IManagedForm managedForm);

	@Override
	public void dispose() {
		sections.clear();
		ValidationProblemMarkersChangeNotifier.INSTANCE.removeListener(validationProblemMarkersChangeListener);
		IOperationHistory operationHistory = getTransactionalFormEditor().getOperationHistory();
		if (operationHistory != null) {
			operationHistory.removeOperationHistoryListener(operationHistoryListener);
		}
		getEditor().removePageChangedListener(pageChangedListener);
		getEditor().removePropertyListener(inputChangeListener);
		super.dispose();
	}

	protected final void refreshPage() {
		if (!created) {
			createFormContent(getManagedForm());
		} else {
			doRefreshPage();
		}
	}

	protected void doRefreshPage() {
		for (IFormSection section : sections) {
			section.refreshSection();
		}

		// Try to show objects affected by most recently executed, undone, or redone command in viewer
		IUndoContext undoContext = (IUndoContext) getTransactionalFormEditor().getAdapter(IUndoContext.class);
		if (undoContext != null) {
			IOperationHistory operationHistory = getTransactionalFormEditor().getOperationHistory();
			if (operationHistory != null) {
				IUndoableOperation operation = operationHistory.getUndoOperation(undoContext);
				if (operation instanceof EMFCommandOperation) {
					Command command = ((EMFCommandOperation) operation).getCommand();
					if (command != null) {
						getTransactionalFormEditor().setSelectionToViewer(command.getAffectedObjects());
					}
				}
			}
		}
	}

	protected final void refreshMessages() {
		getManagedForm().getMessageManager().removeAllMessages();

		for (IFormMessageProvider provider : messageProviders) {
			Map<EStructuralFeature, Set<IFormMessage>> messages = provider.getMessages(pageInput);
			if (messages.size() > 0) {
				doRefreshMessages(getManagedForm().getMessageManager(), messages);
			}
		}
	}

	protected void doRefreshMessages(IMessageManager messageManager, Map<EStructuralFeature, Set<IFormMessage>> messages) {
		for (IFormSection section : sections) {
			section.refreshMessages(messageManager, messages);
		}
	}

	public boolean isEmpty() {
		return !created;
	}
}
