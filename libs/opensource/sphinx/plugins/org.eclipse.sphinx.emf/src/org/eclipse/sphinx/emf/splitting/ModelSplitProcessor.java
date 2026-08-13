/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [474952] Replication of Ancestor Feature filters to much
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.splitting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.sphinx.emf.resource.ModelResourceDescriptor;

public class ModelSplitProcessor {

	protected static class AncestorCopier extends EcoreUtil.Copier {

		private static final long serialVersionUID = 1L;

		protected EObject ancestor;
		protected IModelSplitDirective modelSplitDirective;

		public AncestorCopier(EObject ancestor, IModelSplitDirective modelSplitDirective) {
			Assert.isNotNull(ancestor);
			Assert.isNotNull(modelSplitDirective);
			this.ancestor = ancestor;
			this.modelSplitDirective = modelSplitDirective;
		}

		@Override
		public EObject copy(EObject eObject) {
			if (eObject == null) {
				return null;
			}

			EObject copiedEObject = createCopy(eObject);
			if (copiedEObject != null) {
				put(eObject, copiedEObject);
				for (EStructuralFeature eStructuralFeature : eObject.eClass().getEAllStructuralFeatures()) {
					if (eStructuralFeature.isChangeable() && !eStructuralFeature.isDerived()) {
						if (eObject != ancestor || modelSplitDirective.shouldReplicateAncestorFeature(eObject, eStructuralFeature)) {
							if (eStructuralFeature instanceof EAttribute) {
								EAttribute eAttribute = (EAttribute) eStructuralFeature;
								copyAttribute(eAttribute, eObject, copiedEObject);
							} else {
								EReference eReference = (EReference) eStructuralFeature;
								if (eReference.isContainment()) {
									copyContainment(eReference, eObject, copiedEObject);
								}
							}
						}
					}
				}

				copyProxyURI(eObject, copiedEObject);
			}
			return copiedEObject;
		}

		@Override
		protected void copyFeatureMap(FeatureMap featureMap) {
			for (int i = 0, size = featureMap.size(); i < size; ++i) {
				EStructuralFeature feature = featureMap.getEStructuralFeature(i);
				if (feature instanceof EReference && ((EReference) feature).isContainment()) {
					if (modelSplitDirective.shouldReplicateAncestorFeature(((FeatureMap.Internal) featureMap).getEObject(), feature)) {
						Object value = featureMap.getValue(i);
						if (value != null) {
							copy((EObject) value);
						}
					}
				}
			}
		}
	}

	protected IModelSplitPolicy modelSplitPolicy;
	private Collection<Resource> resourcesToSplit;
	private Collection<EObject> eObjectsToSplit;

	private Map<EObject, Map<URI, EObject>> originalToSplitEObjectsMap = new HashMap<EObject, Map<URI, EObject>>();
	private Map<EObject, EObject> eObjectToOriginalContainerMap = new HashMap<EObject, EObject>();
	private Map<URI, List<EObject>> targetResourceURIToContentsMap = new HashMap<URI, List<EObject>>();

	public ModelSplitProcessor(IModelSplitPolicy modelSplitPolicy) {
		Assert.isNotNull(modelSplitPolicy);
		this.modelSplitPolicy = modelSplitPolicy;
	}

	protected EObject getSplitEObject(EObject originalEObject, URI targetResourceURI) {
		Map<URI, EObject> targetResourceURIToSplitEObjectsMap = originalToSplitEObjectsMap.get(originalEObject);
		if (targetResourceURIToSplitEObjectsMap != null) {
			return targetResourceURIToSplitEObjectsMap.get(targetResourceURI);
		}
		return null;
	}

	protected void addSplitEObject(EObject originalEObject, EObject splitEObject, URI targetResourceURI) {
		Map<URI, EObject> targetResourceURIToSplitEObjectsMap = originalToSplitEObjectsMap.get(originalEObject);
		if (targetResourceURIToSplitEObjectsMap == null) {
			targetResourceURIToSplitEObjectsMap = new HashMap<URI, EObject>();
			originalToSplitEObjectsMap.put(originalEObject, targetResourceURIToSplitEObjectsMap);
		}
		targetResourceURIToSplitEObjectsMap.put(targetResourceURI, splitEObject);
	}

	protected void addOriginalContainer(EObject eObject, EObject originalContainer) {
		eObjectToOriginalContainerMap.put(eObject, originalContainer);
	}

	protected EObject getOriginalContainer(EObject eObject) {
		EObject originalContainer = eObjectToOriginalContainerMap.get(eObject);
		return originalContainer != null ? originalContainer : ((InternalEObject) eObject).eInternalContainer();
	}

	protected List<EObject> getTargetResourceContents(URI targetResourceURI) {
		List<EObject> targetResourceContents = targetResourceURIToContentsMap.get(targetResourceURI);
		if (targetResourceContents == null) {
			targetResourceContents = new ArrayList<EObject>();
			targetResourceURIToContentsMap.put(targetResourceURI, targetResourceContents);
		}
		return targetResourceContents;
	}

	public Collection<Resource> getResourcesToSplit() {
		if (resourcesToSplit == null) {
			resourcesToSplit = new ArrayList<Resource>();
		}
		return resourcesToSplit;
	}

	public Collection<EObject> getEObjectsToSplit() {
		if (eObjectsToSplit == null) {
			eObjectsToSplit = new ArrayList<EObject>();
		}
		return eObjectsToSplit;
	}

	public void run(IProgressMonitor monitor) {
		Collection<Resource> resourcesToSplit = getResourcesToSplit();
		Collection<EObject> eObjectsToSplit = getEObjectsToSplit();

		SubMonitor progress = SubMonitor.convert(monitor, resourcesToSplit.size() + eObjectsToSplit.size());
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		splitResources(resourcesToSplit, progress.newChild(resourcesToSplit.size()));
		splitEObjects(eObjectsToSplit, progress.newChild(eObjectsToSplit.size()));
	}

	protected void splitResources(Collection<Resource> resources, IProgressMonitor monitor) {
		Assert.isNotNull(resources);

		SubMonitor progress = SubMonitor.convert(monitor, resources.size());
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		for (Resource resource : resources) {
			splitEObjects(resource.getContents(), progress.newChild(1));

			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
	}

	protected void splitEObjects(Collection<EObject> eObjects, IProgressMonitor monitor) {
		Assert.isNotNull(eObjects);

		SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// Traverse the model objects' contents, present each model object to split policy and collect resulting split
		// directives
		List<IModelSplitDirective> directives = collectSplitDirectives(eObjects, progress.newChild(25));

		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// Process split directives and build split model content
		processSplitDirectives(directives, progress.newChild(75));
	}

	public Map<URI, List<EObject>> getSplitModelContents() {
		return Collections.unmodifiableMap(targetResourceURIToContentsMap);
	}

	public Collection<ModelResourceDescriptor> getSplitResourceDescriptors() {
		List<ModelResourceDescriptor> descriptors = new ArrayList<ModelResourceDescriptor>(targetResourceURIToContentsMap.keySet().size());
		for (URI uri : targetResourceURIToContentsMap.keySet()) {
			List<EObject> contents = targetResourceURIToContentsMap.get(uri);
			if (contents != null && !contents.isEmpty()) {
				String contentTypeId = modelSplitPolicy.getContentTypeId(contents);
				descriptors.add(new ModelResourceDescriptor(uri, contentTypeId, contents));
			}
		}
		return Collections.unmodifiableList(descriptors);
	}

	protected List<IModelSplitDirective> collectSplitDirectives(Collection<EObject> eObjects, IProgressMonitor monitor) {
		Assert.isNotNull(eObjects);

		SubMonitor progress = SubMonitor.convert(monitor, 2 * eObjects.size());
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// Determine split directives for given model objects as well as for all model objects that are directly and
		// indirectly contained by the former
		List<IModelSplitDirective> directives = new ArrayList<IModelSplitDirective>();
		for (EObject eObject : eObjects) {
			for (TreeIterator<EObject> iterator = eObject.eAllContents(); iterator.hasNext();) {
				IModelSplitDirective directive = modelSplitPolicy.getSplitDirective(iterator.next());
				if (directive != null) {
					directives.add(directive);
				}
			}
			progress.worked(1);

			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
		return directives;
	}

	protected void processSplitDirectives(List<IModelSplitDirective> directives, IProgressMonitor monitor) {
		Assert.isNotNull(directives);

		SubMonitor progress = SubMonitor.convert(monitor, directives.size());
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		for (IModelSplitDirective directive : directives) {
			if (directive.isValid()) {
				EObject eObject = directive.getEObject();
				URI targetResourceURI = directive.getTargetResourceURI();

				// Split given model object by just moving (rather than copying) it to the contents of the intended
				// target resource and keep track of its original container
				addSplitEObject(eObject, eObject, targetResourceURI);
				addOriginalContainer(eObject, eObject.eContainer());

				// Retrieve ancestor object branch - either up to first ancestor object that has already been split or
				// entirely if none of them has
				/*
				 * !! Important Note !! Always walk up to original container to avoid that subsequent splitting of
				 * ancestor objects is made from already split and therefore incomplete ancestor objects.
				 */
				List<EObject> ancestors = new ArrayList<EObject>();
				for (EObject container = ((InternalEObject) eObject).eInternalContainer(); container != null; container = getOriginalContainer(
						container)) {
					// Use Setting
					ancestors.add(container);

					// Abort traversal if current ancestor object has already been split
					if (getSplitEObject(container, targetResourceURI) != null) {
						break;
					}
				}

				// Split ancestor object branch as far as not already done so
				EObject lastEObject = eObject;
				EObject lastSplitEObject = eObject;
				boolean newSplitAncestor = true;
				for (EObject ancestor : ancestors) {
					EObject splitAncestor = null;

					// Split current ancestor object if not already done so
					splitAncestor = getSplitEObject(ancestor, targetResourceURI);
					if (splitAncestor == null) {
						splitAncestor = copyAncestor(ancestor, directive);
						addSplitEObject(ancestor, splitAncestor, targetResourceURI);

						// Root container reached?
						if (((InternalEObject) ancestor).eInternalContainer() == null) {
							// Add split ancestor object as root container to the contents of the intended target
							// resource
							List<EObject> targetResourceContents = getTargetResourceContents(targetResourceURI);
							targetResourceContents.add(splitAncestor);
						}
					} else {
						newSplitAncestor = false;
					}

					// Connect split ancestor object to previously split ancestor or model object
					EStructuralFeature containingFeature = lastEObject.eContainingFeature();
					if (containingFeature == null) {
						throw new RuntimeException("Containing feature of '" + lastEObject + "' not found"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					if (containingFeature.isMany()) {
						@SuppressWarnings("unchecked")
						List<Object> values = (List<Object>) splitAncestor.eGet(containingFeature);
						values.add(lastSplitEObject);
					} else {
						splitAncestor.eSet(containingFeature, lastSplitEObject);
					}

					// Abort splitting of ancestor object branch when having reached at an already split ancestor object
					if (!newSplitAncestor) {
						break;
					}

					lastEObject = ancestor;
					lastSplitEObject = splitAncestor;
				}
			}
			progress.worked(1);

			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
	}

	protected <T extends EObject> T copyAncestor(T ancestor, IModelSplitDirective directive) {
		AncestorCopier copier = new AncestorCopier(ancestor, directive);
		EObject copiedAncestor = copier.copy(ancestor);
		copier.copyReferences();

		@SuppressWarnings("unchecked")
		T t = (T) copiedAncestor;
		return t;
	}

	public void dispose() {
		originalToSplitEObjectsMap.clear();
		eObjectToOriginalContainerMap.clear();
		targetResourceURIToContentsMap.clear();
	}
}
