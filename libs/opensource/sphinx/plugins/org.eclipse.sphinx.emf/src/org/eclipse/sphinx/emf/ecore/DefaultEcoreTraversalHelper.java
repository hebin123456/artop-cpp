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
 *     itemis - [409458] Enhance ScopingResourceSetImpl#getEObjectInScope() to enable cross-document references between model files with different metamodels
 *     itemis - [411580] Remove "isDerived" test in DefaultEcoreTraversalHelper#collectReachableObjectsOfType()
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.ecore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;

public class DefaultEcoreTraversalHelper implements EcoreTraversalHelper {

	/**
	 * {@inheritDoc}
	 * <p>
	 * Applies resource filtering to include only reachable references that are in the same context as the given object.
	 */
	// TODO Change signature to Collection<EObject> getPossibleValuesForReference(EObject object, EReference
	// reference)
	@Override
	public Collection<EObject> getReachableEObjects(EObject referenceSource, EReference reference) {
		LinkedList<EObject> itemQueue = new LinkedList<EObject>();
		Collection<EObject> visited = new HashSet<EObject>();
		Collection<EObject> result = new ArrayList<EObject>();

		Resource contextResource = referenceSource.eResource();
		if (contextResource != null) {
			Set<Resource> resources = new HashSet<Resource>();

			// Add all resources that are in the same scope as reference source object
			resources.addAll(EcorePlatformUtil.getResourcesInScope(contextResource, true));

			// Also add resources from other models if the meta model descriptor behind referenced type is different
			// from that of the reference source object
			IMetaModelDescriptor targetMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor((EClass) reference.getEType());
			IMetaModelDescriptor contextMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getEffectiveDescriptor(contextResource);
			if (targetMMDescriptor != null && !targetMMDescriptor.equals(contextMMDescriptor)) {
				IFile contextFile = EcorePlatformUtil.getFile(contextResource);
				if (contextFile != null) {
					// Retrieve target model(s) that is (are) in the same scope as reference source object
					Collection<IModelDescriptor> targetModelDescriptors = ModelDescriptorRegistry.INSTANCE.getModels(contextFile.getParent(),
							targetMMDescriptor);

					// Ignore target models that are already loaded
					for (Iterator<IModelDescriptor> iter = targetModelDescriptors.iterator(); iter.hasNext();) {
						if (EcorePlatformUtil.isModelLoaded(iter.next())) {
							iter.remove();
						}
					}

					// Trigger asynchronous loading of all target models that are not loaded yet
					EcorePlatformUtil.loadModels(targetModelDescriptors, true, null);

					// Add a dummy object that just yields a message to the list of reachable objects to inform users
					// that loading of other models is ongoing
					if (targetModelDescriptors.size() != 0) {
						result.add(new MessageEObjectImpl(Messages.msg_waitingForModelsBeingLoaded));
					}
				}

				// Add resources from other models that are in the same scope as reference source object
				resources.addAll(EcorePlatformUtil.getResourcesInOtherModels(contextResource, targetMMDescriptor, true));
			}

			if (!resources.isEmpty()) {
				for (Resource resource : resources) {
					for (EObject eObject : resource.getContents()) {
						collectReachableObjectsOfType(visited, itemQueue, result, eObject, reference.getEType());
					}
				}
			} else {
				for (EObject eObject : contextResource.getContents()) {
					collectReachableObjectsOfType(visited, itemQueue, result, eObject, reference.getEType());
				}
			}
		} else {
			collectReachableObjectsOfType(visited, itemQueue, result, EcoreUtil.getRootContainer(referenceSource), reference.getEType());
		}

		while (!itemQueue.isEmpty()) {
			EObject nextItem = itemQueue.removeFirst();
			collectReachableObjectsOfType(visited, itemQueue, result, nextItem, reference.getEType());
		}

		return result;
	}

	/**
	 * This will visit all reachable references from <code>object</code> except those in <code>visited</code> and add
	 * them to the <code>queue</code>. The <code>queue</code> is processed outside this recursive traversal to avoid
	 * stack overflows. It updates <code>visited</code> and adds to result any object with a meta object that indicates
	 * that it is a subtype of <code>type</code>.
	 */
	private void collectReachableObjectsOfType(Collection<EObject> visited, LinkedList<EObject> itemQueue, Collection<EObject> result,
			EObject object, EClassifier type) {

		// Try to perform efficient direct search for instances of frequently used types
		if (collectReachableObjectsOfTypeUnderObject(result, object, type)) {
			// Don't fill itemQueue in order to stop recursive traversal at current level
			return;
		}

		// Perform regular recursive traversal
		if (visited.add(object)) {
			if (type.isInstance(object)) {
				result.add(object);
			}
			for (EStructuralFeature feature : getFeaturesToTraverseFor(object, type)) {
				if (feature instanceof EReference) {
					EReference eReference = (EReference) feature;
					if (eReference.isMany()) {
						@SuppressWarnings("unchecked")
						List<EObject> list = (List<EObject>) object.eGet(eReference);
						for (EObject eObject : list) {
							itemQueue.addLast(eObject);
						}
					} else {
						EObject eObject = (EObject) object.eGet(eReference);
						if (eObject != null) {
							itemQueue.addLast(eObject);
						}
					}
				} else if (FeatureMapUtil.isFeatureMap(feature)) {
					for (FeatureMap.Entry entry : (FeatureMap) object.eGet(feature)) {
						if (entry.getEStructuralFeature() instanceof EReference && entry.getValue() != null) {
							itemQueue.addLast((EObject) entry.getValue());
						}
					}
				}
			}
		}
	}

	@Override
	public boolean collectReachableObjectsOfTypeUnderObject(Collection<EObject> result, EObject object, EClassifier type) {
		return false;
	}

	@Override
	public List<EStructuralFeature> getFeaturesToTraverseFor(EObject object, EClassifier type) {
		return getDefaultFeaturesToTraverseFor(object, type);
	}

	protected List<EStructuralFeature> getDefaultFeaturesToTraverseFor(EObject object, EClassifier type) {
		List<EStructuralFeature> features = new ArrayList<EStructuralFeature>();
		for (EReference reference : object.eClass().getEAllReferences()) {
			if (reference.isContainment()) {
				features.add(reference);
			}
		}
		if (type instanceof EDataType) {
			features.addAll(object.eClass().getEAllAttributes());
		}
		return features;
	}
}
