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
package org.eclipse.sphinx.graphiti.workspace.ui.draganddrop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeatureAndContext;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.internal.DefaultFeatureAndContext;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.ui.internal.command.AbstractCommand;
import org.eclipse.graphiti.ui.platform.IConfigurationProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Extended Graphiti command to manage the initial set of business objects and connections to be transfered by drag &
 * drop feature
 */
@SuppressWarnings("restriction")
public class BasicGraphitiAddModelObjectCommand extends AbstractCommand {

	/**
	 * Added to extendedContextList : 1. AddContext for objects to drop, including objects selected and dragged from
	 * tree viewer to diagram editor objects contained in selected objects objects referenced by objects selected
	 * (including their childs) and which are also in the selection 2. AddConnectionContext for all the connections
	 * between the objects to drop
	 */
	private List<AddContext> extendedContextList; // contains AddContext(s) and AddConnectionContext(s)

	/**
	 * Issues a command, each command is executed later within an EMF transaction.
	 * 
	 * @param configurationProvider
	 * @param parent
	 * @param sel
	 * @param rectangle
	 */
	public BasicGraphitiAddModelObjectCommand(IConfigurationProvider configurationProvider, ContainerShape parent, ISelection sel, Rectangle rectangle) {
		this(configurationProvider, parent, sel, rectangle, null);
	}

	/**
	 * Creates the context list.
	 * 
	 * @param configurationProvider
	 * @param parent
	 * @param selection
	 * @param rectangle
	 * @param connection
	 */
	public BasicGraphitiAddModelObjectCommand(IConfigurationProvider configurationProvider, ContainerShape parent, ISelection selection,
			Rectangle rectangle, Connection connection) {
		super(configurationProvider);

		IStructuredSelection s = (IStructuredSelection) selection;
		if (s == null) {
			s = StructuredSelection.EMPTY;
		}

		extendedContextList = new ArrayList<AddContext>();
		int x = rectangle.x;
		int y = rectangle.y;
		AddContext ctx = null;

		// Add context for currently selected objects
		for (Iterator<?> iter = s.iterator(); iter.hasNext();) {
			Object next = iter.next();

			if (next instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) next;
				Object adapter = adaptable.getAdapter(EObject.class);
				if (adapter instanceof EObject) {
					next = adapter;
				}
			}
			ctx = new AddContext();
			ctx.setNewObject(next);
			ctx.setTargetContainer(parent);
			ctx.setLocation(x, y);
			ctx.setTargetConnection(connection);
			extendedContextList.add(ctx);
			x += 100;
			y += 100;
		}

		// Add context for contained objects
		for (Iterator<?> iter = s.iterator(); iter.hasNext();) {
			Object next = iter.next();
			// Traverse containment references
			EObject candidateObject = (EObject) next;
			EList<EReference> eAllReferences = candidateObject.eClass().getEAllReferences();
			for (EReference candidateRef : eAllReferences) {
				// Feature is reference
				if (candidateRef.isContainment()) {
					// Reference is a containment
					@SuppressWarnings("unchecked")
					EList<EObject> childs = (EList<EObject>) candidateObject.eGet(candidateRef);
					if (childs != null) {
						// add new objects
						for (EObject containedObject : childs) {
							ctx = new AddContext();
							ctx.setNewObject(containedObject);
							// Set default target container
							ctx.setTargetContainer(parent);
							ctx.setLocation(x, y);
							ctx.setTargetConnection(connection);
							ctx.putProperty("container", candidateObject); //$NON-NLS-1$ 
							extendedContextList.add(ctx);
						}
					}
				}
			}
			x += 100;
			y += 100;
		}

		// Add connection context for directly referenced objects that are in the selection
		for (Iterator<?> iter = s.iterator(); iter.hasNext();) {
			Object next = iter.next();
			// Traverse all references
			EObject candidateObject = (EObject) next;
			EList<EReference> eAllReferences = candidateObject.eClass().getEAllReferences();
			for (EReference candidateRef : eAllReferences) {
				// Feature is reference
				if (!candidateRef.isContainment()) {
					if (candidateRef.isMany()) {
						// Multiple reference
						@SuppressWarnings("unchecked")
						EList<EObject> referencedObjects = (EList<EObject>) candidateObject.eGet(candidateRef);
						if (referencedObjects != null) {
							// Now check references between objects already added, if a reference exist, add a
							// AddConnectionContext
							for (EObject referenced : referencedObjects) {
								if (inExtendedContextList(referenced)) {
									// Shapes are not known yet, because will be created later, so business objects are
									// stored meanwhile
									ctx = new AddConnectionContext(null, null);
									ctx.putProperty(candidateRef.getName(), candidateObject);
									ctx.putProperty("sourceAnchor", candidateObject); //$NON-NLS-1$
									ctx.putProperty("targetAnchor", referenced); //$NON-NLS-1$
									ctx.setNewObject(candidateObject);
									// Set default target container
									ctx.setTargetContainer(parent);
									ctx.setLocation(x, y);
									ctx.setTargetConnection(connection);
									extendedContextList.add(ctx);
								}
							}
						}
					} else {
						// Single reference
						EObject referencedObject = (EObject) candidateObject.eGet(candidateRef);
						if (referencedObject != null && inExtendedContextList(referencedObject)) {
							// Shapes are not known yet, because will be created later, so business objects are stored
							// meanwhile
							ctx = new AddConnectionContext(null, null);
							ctx.putProperty(candidateRef.getName(), candidateObject);
							ctx.putProperty("sourceAnchor", candidateObject); //$NON-NLS-1$
							ctx.putProperty("targetAnchor", referencedObject); //$NON-NLS-1$
							ctx.setNewObject(candidateObject);
							// Set default target container
							ctx.setTargetContainer(parent);
							ctx.setLocation(x, y);
							ctx.setTargetConnection(connection);
							extendedContextList.add(ctx);
						}
					}
				}
			}

			x += 100;
			y += 100;
		}

		// Add connection context for indirectly referenced objects that are in the selection
		// Indirectly means that the reference is from a child of the object in selection to another object/child of
		// object in the selection
		for (Iterator<?> iter = s.iterator(); iter.hasNext();) {
			Object next = iter.next();
			// Traverse all references
			EObject candidateObject = (EObject) next;
			EList<EReference> eAllReferences = candidateObject.eClass().getEAllReferences();
			for (EReference candidateRef : eAllReferences) {
				if (candidateRef.isContainment()) {
					@SuppressWarnings("unchecked")
					EList<EObject> childs = (EList<EObject>) candidateObject.eGet(candidateRef);
					if (childs != null) {
						// Now check references between child objects already added and objects already added, if a
						// reference exist, add a AddConnectionContext
						for (EObject child : childs) {
							EList<EReference> indirectReferences = child.eClass().getEAllReferences();
							for (EReference indirectRef : indirectReferences) {
								if (indirectRef.isMany()) {
									// Multiple reference
									@SuppressWarnings("unchecked")
									EList<EObject> referencedObjects = (EList<EObject>) child.eGet(indirectRef);
									if (referencedObjects != null) {
										for (EObject candidateReferenced : referencedObjects) {
											if (inExtendedContextList(candidateReferenced)) {
												// Shapes are not known yet, because will be created later, so business
												// objects are stored meanwhile
												ctx = new AddConnectionContext(null, null);
												ctx.putProperty(indirectRef.getName(), child);
												ctx.putProperty("sourceAnchor", child); //$NON-NLS-1$
												ctx.putProperty("targetAnchor", candidateReferenced); //$NON-NLS-1$
												ctx.setNewObject(child);
												// Set default target container
												ctx.setTargetContainer(parent);
												ctx.setLocation(x, y);
												ctx.setTargetConnection(connection);
												extendedContextList.add(ctx);
											}
										}
									}
								} else {
									// Single reference
									EObject referencedObject = (EObject) child.eGet(indirectRef);
									if (referencedObject != null && inExtendedContextList(referencedObject)) {
										// Shapes are not known yet, because will be created later, so business objects
										// are stored meanwhile
										ctx = new AddConnectionContext(null, null);
										ctx.putProperty(indirectRef.getName(), child);
										ctx.putProperty("sourceAnchor", child); //$NON-NLS-1$
										ctx.putProperty("targetAnchor", referencedObject); //$NON-NLS-1$
										ctx.setNewObject(child);
										// Set default target container
										ctx.setTargetContainer(parent);
										ctx.setLocation(x, y);
										ctx.setTargetConnection(connection);
										extendedContextList.add(ctx);
									}
								}
							}
						}
					}
				}
			}

			x += 100;
			y += 100;
		}
	}

	/**
	 * Checks if a referenced object is contained in the context list.
	 * 
	 * @param referenced
	 * @return
	 */
	Boolean inExtendedContextList(EObject referenced) {
		for (AddContext addContext : extendedContextList) {
			IAddContext ctx = addContext;
			EObject newObject = (EObject) ctx.getNewObject();
			if (newObject.equals(referenced)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canExecute() {
		IFeatureProvider featureProvider = getFeatureProvider();
		if (featureProvider != null && extendedContextList.size() > 0) {
			// try to find an add-feature for each object in the selection
			for (AddContext addContext : extendedContextList) {
				IAddContext ctx = addContext;
				IAddFeature f = featureProvider.getAddFeature(ctx);
				if (f == null) {
					return false;
				} else {
					boolean canAdd = f.canAdd(ctx);
					if (canAdd == true) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void execute() {
		for (AddContext addContext : extendedContextList) {
			IAddContext ctx = addContext;
			getFeatureProvider().addIfPossible(ctx);
		}
	}

	@Override
	public boolean canUndo() {
		return false;
	}

	public IFeatureAndContext[] getFeaturesAndContexts() {
		List<IFeatureAndContext> features = new ArrayList<IFeatureAndContext>();
		IFeatureProvider featureProvider = getFeatureProvider();
		if (featureProvider != null && extendedContextList.size() > 0) {
			// try to find an add-feature for each object in the selection
			for (AddContext addContext : extendedContextList) {
				IAddContext ctx = addContext;
				IAddFeature f = featureProvider.getAddFeature(ctx);
				if (f != null && f.canAdd(ctx)) {
					DefaultFeatureAndContext dfac = new DefaultFeatureAndContext(f, ctx);
					features.add(dfac);
				}
			}
		}
		return features.toArray(new IFeatureAndContext[features.size()]);
	}

}