/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [392424] Migrate Sphinx integration of Graphiti to Graphiti 0.9.x
 * 
 * </copyright>
 */
package org.eclipse.sphinx.graphiti.workspace.ui.util;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramLink;
import org.eclipse.graphiti.mm.pictograms.PictogramsFactory;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.graphiti.workspace.metamodel.GraphitiMMDescriptor;
import org.eclipse.sphinx.graphiti.workspace.ui.editors.BasicGraphitiDiagramEditor;
import org.eclipse.sphinx.graphiti.workspace.ui.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class that manages Graphiti diagram and associated operations on Business Objects model
 */
public class DiagramUtil {

	/**
	 * Open the Sphinx Graphiti editor
	 * 
	 * @param diagram
	 * @return
	 */
	public static IEditorPart openBasicDiagramEditor(final Diagram diagram) {
		String editorID = BasicGraphitiDiagramEditor.BASIC_DIAGRAM_EDITOR_ID;
		return openDiagramEditor(diagram, editorID);
	}

	/**
	 * Open the default Graphiti editor
	 * 
	 * @param diagram
	 * @return
	 */
	public static IEditorPart openDefaultDiagramEditor(final Diagram diagram) {
		String editorID = "org.eclipse.graphiti.ui.editor.DiagramEditor"; //$NON-NLS-1$
		return openDiagramEditor(diagram, editorID);
	}

	/**
	 * Opens Graphiti diagram editor knowing diagram and editor identifier
	 * 
	 * @param diagram
	 * @param editorID
	 * @return
	 */
	public static IEditorPart openDiagramEditor(final Diagram diagram, String editorID) {
		if (diagram != null) {
			String providerId = GraphitiUi.getExtensionManager().getDiagramTypeProviderId(diagram.getDiagramTypeId());
			DiagramEditorInput editorInput = new DiagramEditorInput(EcoreUtil.getURI(diagram), providerId);
			try {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, editorID);
			} catch (PartInitException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
		return null;
	}

	public static PictogramLink createPictogramLink(Diagram diagram) {
		PictogramLink link = null;
		if (diagram != null) {
			// Create new link
			link = PictogramsFactory.eINSTANCE.createPictogramLink();
			link.setPictogramElement(diagram);
			// Add new link to diagram
			diagram.getPictogramLinks().add(link);
		}
		return link;
	}

	/**
	 * Creates a Graphiti diagram and links it to Business Objects model
	 * 
	 * @param containerPath
	 * @param diagramFileName
	 * @param diagramType
	 * @param diagramBusinessObject
	 * @return
	 */
	public static Diagram createDiagram(IPath containerPath, String diagramFileName, String diagramType, EObject diagramBusinessObject) {
		Diagram diagram = Graphiti.getPeCreateService().createDiagram(diagramType, diagramFileName, true);

		// Link the diagram to the root business model
		PictogramLink link = createPictogramLink(diagram);
		link.getBusinessObjects().add(diagramBusinessObject);

		// Save diagram to new file
		IFile diagramFile = ResourcesPlugin.getWorkspace().getRoot().getFile(containerPath.append(diagramFileName));
		TransactionalEditingDomain domain = WorkspaceEditingDomainUtil.getEditingDomain(diagramBusinessObject);
		EcorePlatformUtil.saveNewModelResource(domain, diagramFile.getFullPath(), GraphitiMMDescriptor.GRAPHITI_DIAGRAM_CONTENT_TYPE_ID, diagram,
				false, null);
		return diagram;
	}

	/**
	 * Adds an object to Business Objects resource
	 * 
	 * @param parentObject
	 * @param reference
	 * @param objectToAdd
	 */
	public static void addObjectToBOResource(final EObject parentObject, final EReference reference, final EObject objectToAdd) {
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(parentObject);
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// if the user knows the reference
				if (reference != null) {
					addObjectFeature(parentObject, reference, objectToAdd);
				} else {
					// choose the first appropriate reference
					EList<EReference> eAllReferences = parentObject.eClass().getEAllReferences();
					for (EReference candidateRef : eAllReferences) {
						if (candidateRef.getEType() == objectToAdd.eClass()) {
							// Here we take the first containment feature whose type corresponds to the type of the
							// target
							// object
							// TODO: Validate this!
							addObjectFeature(parentObject, candidateRef, objectToAdd);
							break;
						}
					}
				}
			}
		};

		if (editingDomain != null) {
			try {
				WorkspaceTransactionUtil.executeInWriteTransaction(editingDomain, runnable, "Add object to Business Objects resource"); //$NON-NLS-1$
			} catch (OperationCanceledException ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			} catch (ExecutionException ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			}
		} else {
			runnable.run();
		}
	}

	/**
	 * Deletes an object from Business Objects resource
	 * 
	 * @param objectToDelete
	 */
	public static void deleteObjectFromBOResource(final EObject objectToDelete) {
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(objectToDelete);
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// Use EcoreUtil as in Default Delete feature
				EcoreUtil.delete(objectToDelete, true);
			}
		};

		if (editingDomain != null) {
			try {
				WorkspaceTransactionUtil.executeInWriteTransaction(editingDomain, runnable, "Delete object from Business Objects resource"); //$NON-NLS-1$
			} catch (OperationCanceledException ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			} catch (ExecutionException ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			}
		} else {
			runnable.run();
		}

	}

	/**
	 * Removes an object from Business Objects resource
	 * 
	 * @param parentObject
	 * @param reference
	 * @param objectToRemove
	 */
	public static void removeObjectFromBOResource(final EObject parentObject, final EReference reference, final EObject objectToRemove) {
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(parentObject);
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// if the user knows the reference
				if (reference != null) {
					removeObjectFeature(parentObject, reference, objectToRemove);
				} else {
					// choose the first appropriate reference
					EList<EReference> eAllReferences = parentObject.eClass().getEAllReferences();
					for (EReference candidateRef : eAllReferences) {
						if (candidateRef.getEType() == objectToRemove.eClass()) {
							// Here we take the first containment feature whose type corresponds to the type of the
							// target
							// TODO: Validate this!
							removeObjectFeature(parentObject, candidateRef, objectToRemove);
							break;
						}
					}
				}
			}
		};

		if (editingDomain != null) {
			try {
				WorkspaceTransactionUtil.executeInWriteTransaction(editingDomain, runnable, "Remove object from Business Objects resource"); //$NON-NLS-1$
			} catch (OperationCanceledException ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			} catch (ExecutionException ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			}
		} else {
			runnable.run();
		}
	}

	/**
	 * Adds a reference to Business Objects resource
	 * 
	 * @param sourceObject
	 * @param reference
	 * @param targetObject
	 */
	public static void addReferenceToBOResource(final EObject sourceObject, final EReference reference, final EObject targetObject) {
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(sourceObject);
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// if the user knows the reference
				if (reference != null) {
					addObjectFeature(sourceObject, reference, targetObject);
				} else {
					// Set the sourceBO new value
					EList<EReference> eAllReferences = sourceObject.eClass().getEAllReferences();
					for (EReference candidateRef : eAllReferences) {
						if (candidateRef.getEType() == targetObject.eClass()) {
							// Here we take the first feature whose type corresponds to the type of the target object
							// TODO: Validate this!
							addObjectFeature(sourceObject, candidateRef, targetObject);
							break;
						}
					}
				}
			}
		};

		if (editingDomain != null) {
			try {
				WorkspaceTransactionUtil.executeInWriteTransaction(editingDomain, runnable, "Add link to Business Objects resource"); //$NON-NLS-1$
			} catch (OperationCanceledException ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			} catch (ExecutionException ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			}
		} else {
			runnable.run();
		}
	}

	/**
	 * Removes a reference from Business Objects model
	 * 
	 * @param sourceObject
	 * @param referenceName
	 * @param targetObject
	 */
	public static void removeReferencefromBOResource(final EObject sourceObject, final String referenceName, final EObject targetObject) {
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(sourceObject);
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// Set the sourceBO new value to null
				EList<EReference> eAllReferences = sourceObject.eClass().getEAllReferences();
				for (EReference candidateRef : eAllReferences) {
					if (candidateRef.getName() == referenceName) {
						removeObjectFeature(sourceObject, candidateRef, targetObject);
						break;
					}
				}
			}
		};

		if (editingDomain != null) {
			try {
				WorkspaceTransactionUtil.executeInWriteTransaction(editingDomain, runnable, "Remove link from Business Objects resource"); //$NON-NLS-1$
			} catch (OperationCanceledException ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			} catch (ExecutionException ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			}
		} else {
			runnable.run();
		}
	}

	/**
	 * Add object feature : attributes, handle single-valued references, multi-valued references, multi-valued ordred
	 * references
	 * 
	 * @param object
	 * @param feature
	 * @param newValue
	 */
	public static void addObjectFeature(final EObject object, final EStructuralFeature feature, final Object newValue) {
		if (newValue != null) {
			// Feature is attribute
			if (feature instanceof EAttribute) {
				object.eSet(feature, newValue);
			}
			// Feature is reference
			if (feature instanceof EReference) {
				if (feature.isMany()) {
					// Multiple-valued feature
					@SuppressWarnings("unchecked")
					EList<EObject> childs = (EList<EObject>) object.eGet(feature);
					// add new value at specified position
					childs.add((EObject) newValue);
				} else {
					// Single valued feature
					object.eSet(feature, newValue);
				}
			}
		}

	}

	/**
	 * Remove object feature : attributes, handle single-valued references, multi-valued references
	 * 
	 * @param parent
	 * @param feature
	 * @param oldValue
	 */
	public static void removeObjectFeature(EObject parent, EStructuralFeature feature, Object oldValue) {
		if (oldValue != null) {
			// Feature is attribute
			if (feature instanceof EAttribute) {
				parent.eSet(feature, null);
			}
			// Feature is reference
			if (feature instanceof EReference) {
				// Multiple-valued feature
				if (feature.isMany()) {
					if (oldValue instanceof EObject) {
						@SuppressWarnings("unchecked")
						EList<EObject> childs = (EList<EObject>) parent.eGet(feature);
						for (EObject child : childs) {
							if (EcoreUtil.equals(child, (EObject) oldValue)) {
								childs.remove(child);
								break;
							}
						}
					}
				} else {
					// Single valued feature
					parent.eSet(feature, null);
				}
			}
		}
	}

	/**
	 * Retrieves an EObject from Resource given its fragment
	 */
	public static EObject getEObject(EObject rootObject, String fragment) {
		if (fragment != null) {
			TreeIterator<EObject> i = rootObject.eAllContents();
			while (i.hasNext()) {
				EObject eObject = i.next();
				String candidateFragment;
				if (eObject.eIsProxy()) {
					candidateFragment = ((InternalEObject) eObject).eProxyURI().fragment();
				} else {
					candidateFragment = EcoreUtil.getURI(eObject).fragment();
				}
				if (fragment.equals(candidateFragment)) {
					return eObject;
				}
			}
		}
		return null;
	}

}
