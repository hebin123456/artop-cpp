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
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.features.delete;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IMultiDeleteInfo;
import org.eclipse.graphiti.features.context.IRemoveContext;
import org.eclipse.graphiti.features.context.impl.RemoveContext;
import org.eclipse.graphiti.mm.pictograms.ManhattanConnection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.PictogramLink;
import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
import org.eclipse.sphinx.graphiti.workspace.ui.util.DiagramUtil;
import org.eclipse.sphinx.graphiti.workspace.ui.util.PropertyUtil;

public class DeleteProvidedInterfacesFeature extends DefaultDeleteFeature {

	public DeleteProvidedInterfacesFeature(IFeatureProvider fp) {
		super(fp);
	}

	/**
	 * Performs custom deletion of the connection because connection is linked to ComponenType and we don't want to
	 * delete ComponentType, we want to delete only the connection. TODO: Put this method into a more generic Class
	 * 
	 * @param context
	 */
	@Override
	public void delete(IDeleteContext context) {
		// we need this reset, since the an instance of this feature can be
		// used multiple times, e.g. as a part of a pattern
		setDoneChanges(false);
		IMultiDeleteInfo multiDeleteInfo = context.getMultiDeleteInfo();
		if (multiDeleteInfo != null && multiDeleteInfo.isDeleteCanceled()) {
			return;
		}
		PictogramElement pe = context.getPictogramElement();
		Object[] businessObjectsForPictogramElement = getAllBusinessObjectsForPictogramElement(pe);
		if (businessObjectsForPictogramElement != null && businessObjectsForPictogramElement.length > 0) {
			if (multiDeleteInfo == null) {
				if (!getUserDecision(context)) {
					return;
				}
			} else {
				if (multiDeleteInfo.isShowDialog()) {
					boolean okPressed = getUserDecision(context);
					if (okPressed) {
						// don't show further dialogs
						multiDeleteInfo.setShowDialog(false);
					} else {
						multiDeleteInfo.setDeleteCanceled(true);
						return;
					}
				}
			}
		}

		preDelete(context);
		// NOTE: The following method deleteBusinessObjects is intentionally commented, it has beeen reworked to avoid
		// deleting business object ComponentType because in our implementation, the container shape of connection
		// "providedIntefaces" is not linked to an EClass, it is linked to a ComponentType, and we don't want to delete
		// ComponentType cf. http://www.eclipse.org/forums/index.php/t/172976/
		// deleteBusinessObjects(businessObjectsForPictogramElement);
		deleteLinkFromEMFResource(context);

		IRemoveContext rc = new RemoveContext(pe);
		IFeatureProvider featureProvider = getFeatureProvider();
		IRemoveFeature removeFeature = featureProvider.getRemoveFeature(rc);
		if (removeFeature != null) {
			removeFeature.remove(rc);
			// Bug 347421: Set hasDoneChanges flag only after first modification
			setDoneChanges(true);
		}
		postDelete(context);
	}

	public void deleteLinkFromEMFResource(IDeleteContext context) {
		PictogramElement pictogramElement = context.getPictogramElement();
		ManhattanConnection connection = (ManhattanConnection) pictogramElement;
		PictogramElement sourceShape = (PictogramElement) connection.getStart().eContainer();
		PictogramElement targetShape = (PictogramElement) connection.getEnd().eContainer();

		PictogramLink sourcelink = sourceShape.getLink();
		// Get the associated Business Object
		final EObject sourceObject = sourcelink.getBusinessObjects().get(0);
		PictogramLink targetlink = targetShape.getLink();
		// Get the associated Business Object
		final EObject targetObject = targetlink.getBusinessObjects().get(0);

		// Get the name of the reference from the user-defined property associated to the connection
		String referenceName = PropertyUtil.getReferenceName(connection);
		// Remove the link from the EMF resource
		DiagramUtil.removeReferencefromBOResource(sourceObject, referenceName, targetObject);
	}
}