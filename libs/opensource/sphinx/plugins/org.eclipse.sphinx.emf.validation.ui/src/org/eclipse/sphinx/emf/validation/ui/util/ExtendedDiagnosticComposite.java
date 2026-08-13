/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.validation.ui.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.ui.CommonUIPlugin;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Extension of the DiagnosticComposite class in order to treat multi diagnostic
 */
public class ExtendedDiagnosticComposite extends Composite {
	public static class TextProvider {
		/**
		 * Returns the text associated to be displayed in the detail text when a a diagnostic is selected in the tree.
		 * 
		 * @param diagnostic
		 * @return a not null String
		 */
		public String getDetail(Diagnostic diagnostic) {
			Throwable throwable = diagnostic.getException();
			if (throwable != null) {
				StringWriter in = new StringWriter();
				PrintWriter ps = new PrintWriter(in);
				throwable.printStackTrace(ps);
				return in.getBuffer().toString();
			}

			for (Object datum : diagnostic.getData()) {
				if (datum instanceof StringBuilder) {
					return datum.toString();
				}
			}

			return ""; //$NON-NLS-1$
		}
	}

	public static int ERROR_WARNING_MASK = Diagnostic.ERROR | Diagnostic.WARNING;

	public static boolean severityMatches(Diagnostic diagnostic, int mask) {
		return (diagnostic.getSeverity() & mask) != 0;
	}

	protected List<Diagnostic> diagnostics;
	protected TextProvider textProvider;

	protected boolean showRootDiagnostic = false;
	protected TreeViewer diagnosticTreeViewer;
	protected Text detailText;

	protected int severityMask = 0;

	/**
	 * @param parent
	 * @param style
	 */
	public ExtendedDiagnosticComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout layout = new GridLayout();
		int spacing = 3;
		layout.marginTop = -5;
		layout.marginBottom = -5;
		layout.marginLeft = -5;
		layout.marginRight = -5;
		layout.horizontalSpacing = spacing;
		layout.verticalSpacing = spacing;
		setLayout(layout);
	}

	@Override
	public void dispose() {
		diagnostics = null;
		diagnosticTreeViewer = null;
		detailText = null;

		super.dispose();
	}

	public void initialize(List<Diagnostic> diagnostics) {
		if (!isInitialized()) {
			setDiagnostics(diagnostics);
			createControls(this);
		}
	}

	public boolean isInitialized() {
		return diagnosticTreeViewer != null && detailText != null;
	}

	public void setDiagnostics(List<Diagnostic> diagnostics) {
		this.diagnostics = diagnostics;
		if (isInitialized()) {
			detailText.setText(""); //$NON-NLS-1$
			if (getDiagnostics() != null) {
				diagnosticTreeViewer.setInput(getDiagnostics());
			} else {
				diagnosticTreeViewer.getTree().removeAll();
			}
		}
	}

	public List<Diagnostic> getDiagnostics() {
		return diagnostics;
	}

	public void setTextProvider(TextProvider textProvider) {
		this.textProvider = textProvider;
		if (detailText != null) {
			String detail = getTextProvider().getDetail(getSelection());
			setDetailText(detail);
		}
	}

	public TextProvider getTextProvider() {
		if (textProvider == null) {
			textProvider = new TextProvider();
		}
		return textProvider;
	}

	public void setShowRootDiagnostic(boolean showRootDiagnostic) {
		this.showRootDiagnostic = showRootDiagnostic;
	}

	public boolean isShowRootDiagnostic() {
		return showRootDiagnostic;
	}

	public void setSeverityMask(int severityMask) {
		this.severityMask = severityMask;
	}

	public int getSeverityMask() {
		return severityMask;
	}

	protected void createControls(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL));

		diagnosticTreeViewer = new TreeViewer(sashForm, SWT.BORDER);
		diagnosticTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_BEGINNING));

		detailText = new Text(sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 20;
		gridData.grabExcessVerticalSpace = true;
		detailText.setLayoutData(gridData);
		detailText.setBackground(detailText.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		sashForm.setWeights(new int[] { 70, 30 });
		sashForm.setMaximizedControl(diagnosticTreeViewer.getTree());

		diagnosticTreeViewer.setContentProvider(createContentProvider());
		diagnosticTreeViewer.setLabelProvider(createLabelProvider());

		diagnosticTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty()) {
					Diagnostic diagnostic = (Diagnostic) ((IStructuredSelection) event.getSelection()).getFirstElement();
					diagnosticSelected(diagnostic);
				} else {
					detailText.setText(""); //$NON-NLS-1$
				}
			}
		});

		if (getDiagnostics() != null) {
			diagnosticTreeViewer.setInput(getDiagnostics());
		}

		// FIXME DOES NOT SEEM TO WORK..
		diagnosticTreeViewer.expandToLevel(AbstractTreeViewer.ALL_LEVELS);
	}

	public void setDetailText(String text) {
		if (text == null) {
			text = ""; //$NON-NLS-1$
		}

		if (detailText != null && !text.equals(detailText.getText())) {
			detailText.setText(text);
		}
	}

	public String getDetailText() {
		return detailText == null ? "" : detailText.getText(); //$NON-NLS-1$
	}

	public Diagnostic getSelection() {
		return diagnosticTreeViewer == null ? null : (Diagnostic) ((IStructuredSelection) diagnosticTreeViewer.getSelection()).getFirstElement();
	}

	protected void diagnosticSelected(Diagnostic selection) {
		String detail = getTextProvider().getDetail(selection).trim();

		SashForm sashForm = (SashForm) detailText.getParent();
		setDetailText(detail);
		if (detail.length() == 0) {
			sashForm.setMaximizedControl(diagnosticTreeViewer.getTree());
		} else {
			sashForm.setMaximizedControl(null);
			if (diagnosticTreeViewer != null) {
				diagnosticTreeViewer.getTree().showSelection();
			}
		}
	}

	protected ITreeContentProvider createContentProvider() {
		return new ITreeContentProvider() {
			private boolean isRootElement = isShowRootDiagnostic();
			private Map<String, Diagnostic[]> rootToChildrenMap = new HashMap<String, Diagnostic[]>();
			private Map<Diagnostic, Diagnostic[]> parentToChildrenMap = new HashMap<Diagnostic, Diagnostic[]>();

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				parentToChildrenMap.clear();
			}

			@Override
			public void dispose() {
				parentToChildrenMap.clear();
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				if (isRootElement) {
					isRootElement = false;
					Diagnostic diagnostic = (Diagnostic) inputElement;
					if (severityMatches(diagnostic, severityMask)) {
						if (diagnostic.getMessage() != null || diagnostic.getException() != null) {
							return new Object[] { diagnostic };
						}
					} else {
						return new Object[0];
					}
				}
				return getChildren(inputElement);
			}

			@Override
			public boolean hasChildren(Object element) {
				return getChildren(element).length > 0;
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				Diagnostic[] children = parentToChildrenMap.get(parentElement);
				if (children == null) {
					if (parentElement instanceof List<?>) {
						List<Diagnostic> childList = new ArrayList<Diagnostic>(((List<Diagnostic>) parentElement).size());
						for (Diagnostic child : (List<Diagnostic>) parentElement) {
							childList.add(child);
						}
						children = childList.toArray(new Diagnostic[childList.size()]);
						rootToChildrenMap.put("", children); //$NON-NLS-1$
					} else {
						Diagnostic diagnostic = (Diagnostic) parentElement;
						List<Diagnostic> childList = new ArrayList<Diagnostic>(diagnostic.getChildren().size());
						for (Diagnostic child : diagnostic.getChildren()) {
							if (severityMatches(child, severityMask)) {
								childList.add(child);
							}
						}
						children = childList.toArray(new Diagnostic[childList.size()]);
						parentToChildrenMap.put(diagnostic, children);
					}
				}
				return children;
			}
		};
	}

	protected ILabelProvider createLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				Diagnostic diagnostic = (Diagnostic) element;
				String message = diagnostic.getMessage();
				if (message == null) {
					switch (diagnostic.getSeverity()) {
					case Diagnostic.ERROR:
						message = CommonUIPlugin.getPlugin().getString("_UI_DiagnosticError_label"); //$NON-NLS-1$
						break;
					case Diagnostic.WARNING:
						message = CommonUIPlugin.getPlugin().getString("_UI_DiagnosticWarning_label"); //$NON-NLS-1$
						break;
					default:
						message = CommonUIPlugin.getPlugin().getString("_UI_Diagnostic_label"); //$NON-NLS-1$
						break;
					}
				}
				return message;
			}

			@Override
			public Image getImage(Object element) {
				Diagnostic diagnostic = (Diagnostic) element;
				ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
				switch (diagnostic.getSeverity()) {
				case Diagnostic.ERROR:
					return sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
				case Diagnostic.CANCEL:
				case Diagnostic.WARNING:
					return sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
				case Diagnostic.OK:
				case Diagnostic.INFO:
					return sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
				}
				return null;
			}
		};
	}
}
