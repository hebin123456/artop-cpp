/**
 * <copyright>
 *
 * Copyright (c) 2008-2016 See4sys, BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Added/Updated javadoc
 *     itemis - [458976] Validators are not singleton when they implement checks for different EPackages
 *     itemis - [460260] Expanded paths are collapsed on resource reload
 *     itemis - [501110] Provide an isSet(EObject) method in EObjectUtil
 *     itemis - [506671] Add support for specifying and injecting user-defined arguments for workflows through workflow launch configurations
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.AbstractTreeIterator;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.ecore.ECrossReferenceAdapterFactory;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapterFactory;
import org.eclipse.sphinx.emf.resource.ProxyURIIntegrityException;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.ReflectUtil;

/**
 * An utility class which provides various methods for accessing EObjects in an EMF model.
 */
public final class EObjectUtil {

	private static Map<Class<?>, EDataType> eDataTypeMappings;

	// Prevent from instantiation
	private EObjectUtil() {
	}

	public static final String DEFAULT_EMF_MODEL_IMPLEMENTATION_PACKAGE_SUFFIX = "impl"; //$NON-NLS-1$

	/**
	 * Depth constant (value 0) indicating this EObject, but not any of its members.
	 */
	public static final int DEPTH_ZERO = 0;

	/**
	 * Depth constant (value 1) indicating this EObject and its direct members.
	 */
	public static final int DEPTH_ONE = 1;

	/**
	 * Depth constant (value 2) indicating this EObject and its direct and indirect members at any depth.
	 */
	public static final int DEPTH_INFINITE = 2;

	/**
	 * Returns all instances of a certain type within a specified ResourceSet. The type of the EObjects to return is
	 * derived from the provided EReference. All returned EObject will of the same type as the type the EReference
	 * points to. The ResourceSet is calculated from an EObject. The method will resolve the ResourceSet to which the
	 * EObject belongs.
	 *
	 * @param contextObject
	 *            The context object used to calculate the context inside which instances of the expected type must be
	 *            returned.
	 * @param eReference
	 *            The eReference of objects that must be returned.
	 * @param exactMatch
	 *            The kind of search:
	 *            <ul>
	 *            <li><b><tt>true</tt>&nbsp;&nbsp;&nbsp;</b> if only specified type must be considered;<br>
	 *            <li><b><tt>false</tt>&nbsp;</b> if objects of inherited types are also wanted.
	 *            </ul>
	 * @return All instances of the specified reference's type in the context of the given object.
	 */
	public static List<EObject> getAllInstancesOf(EObject contextObject, EReference eReference, boolean exactMatch) {
		Assert.isNotNull(eReference);

		Class<?> instanceClass = eReference.getEReferenceType().getInstanceClass();
		@SuppressWarnings("unchecked")
		List<EObject> instances = (List<EObject>) getAllInstancesOf(contextObject, instanceClass, exactMatch);
		return instances;
	}

	/**
	 * Returns all instances of a certain type within a specified ResourceSet. The ResourceSet is calculated from an
	 * EObject. The method will resolve the ResourceSet to which the EObject belongs. In case, the contextObject without
	 * Resource(stand alone), returns all instances of a certain type within contextObject's contents
	 *
	 * @param contextObject
	 *            The context EObject used to calculate the context inside which instances of the expected type must be
	 *            returned.
	 * @param type
	 *            The expected type of objects that must be returned.
	 * @param exactMatch
	 *            The kind of search:
	 *            <ul>
	 *            <li><b><tt>true</tt>&nbsp;&nbsp;&nbsp;</b> if only specified type must be considered;<br>
	 *            <li><b><tt>false</tt>&nbsp;</b> if objects of inherited types are also wanted.
	 *            </ul>
	 * @return All instances of the specified type in the context of the given object.
	 */
	public static <T> List<T> getAllInstancesOf(EObject contextObject, Class<T> type, boolean exactMatch) {
		if (contextObject != null) {
			Collection<Resource> resources = EcorePlatformUtil.getResourcesInModel(contextObject, true);
			if (!resources.isEmpty()) {
				return getAllInstancesOf(resources, type, exactMatch);
			} else {
				EObject rootContainer = EcoreUtil.getRootContainer(contextObject);
				return getAllInstancesOf(getAllContentsIncludingRoot(rootContainer), type, exactMatch);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Returns all instances of a certain type within a specified ResourceSet. The ResourceSet is calculated from a
	 * Resource. The method will resolve the ResourceSet to which the Resource belongs. In case Resource in ResourSet
	 * without EditingDomain, all resources in the ResourceSet will be considered.
	 *
	 * @param contextResource
	 *            The context resource used to calculate the context inside which instances of the expected type must be
	 *            returned.
	 * @param type
	 *            The expected type of objects that must be returned.
	 * @param exactMatch
	 *            The kind of search:
	 *            <ul>
	 *            <li><b><tt>true</tt>&nbsp;&nbsp;&nbsp;</b> if only specified type must be considered;<br>
	 *            <li><b><tt>false</tt>&nbsp;</b> if objects of inherited types are also wanted.
	 *            </ul>
	 * @return All instances of the specified type in the context of the given resource.
	 */
	public static <T> List<T> getAllInstancesOf(Resource contextResource, Class<T> type, boolean exactMatch) {
		Collection<Resource> resources = EcorePlatformUtil.getResourcesInModel(contextResource, true);
		return getAllInstancesOf(resources, type, exactMatch);
	}

	/**
	 * Returns all instances of a certain type within a specified ResourceSet. The ResourceSet is resolved from the
	 * provided IModelDescriptor.
	 *
	 * @param modelDescriptor
	 *            The model descriptor used to calculate the context inside which instances of the expected type must be
	 *            returned.
	 * @param type
	 *            The expected type of objects that must be returned.
	 * @param exactMatch
	 *            The kind of search:
	 *            <ul>
	 *            <li><b><tt>true</tt>&nbsp;&nbsp;&nbsp;</b> if only specified type must be considered;<br>
	 *            <li><b><tt>false</tt>&nbsp;</b> if objects of inherited types are also wanted.
	 *            </ul>
	 * @return All instances of the specified type in the context of the given model descriptor.
	 */
	public static <T> List<T> getAllInstancesOf(IModelDescriptor modelDescriptor, Class<T> type, boolean exactMatch) {
		Collection<Resource> resources = EcorePlatformUtil.getResourcesInModel(modelDescriptor, true);
		return getAllInstancesOf(resources, type, exactMatch);
	}

	/**
	 * Returns all instances of a certain type within a specified set of Resources.
	 *
	 * @param resources
	 *            The list of resources inside which instances of the expected type must be returned.
	 * @param type
	 *            The expected type of objects that must be returned.
	 * @param exactMatch
	 *            The kind of search:
	 *            <ul>
	 *            <li><b><tt>true</tt>&nbsp;&nbsp;&nbsp;</b> if only specified type must be considered;<br>
	 *            <li><b><tt>false</tt>&nbsp;</b> if objects of inherited types are also wanted.
	 *            </ul>
	 * @return All instances of the specified type in the given list of resources.
	 */
	public static <T> List<T> getAllInstancesOf(Collection<Resource> resources, Class<T> type, boolean exactMatch) {
		Assert.isNotNull(resources);
		Assert.isNotNull(type);

		List<T> instances = new ArrayList<T>();
		for (Resource resource : resources) {
			instances.addAll(getAllInstancesOf(resource.getAllContents(), type, exactMatch));
		}
		return instances;
	}

	/**
	 * Return all instance of reference objects (which is many) of the given object
	 *
	 * @param owner
	 *            The given object to get referenced instances.
	 * @param reference
	 *            The reference to get.
	 * @param type
	 *            The expected type of objects to return
	 * @param exactMatch
	 *            The kind of search:
	 *            <ul>
	 *            <li><b><tt>true</tt>&nbsp;&nbsp;&nbsp;</b> if only specified type must be considered;<br>
	 *            <li><b><tt>false</tt>&nbsp;</b> if objects of inherited types are also wanted.
	 *            </ul>
	 * @return All instance of the specified type that are references of <code> owner</code> object. Or an empty list if
	 *         the owner doesn't have the reference as required
	 */
	public static <T> List<T> getReferencedInstancesOf(EObject owner, EReference reference, Class<T> type, boolean exactMatch) {
		Assert.isNotNull(owner);
		Assert.isNotNull(reference);
		Assert.isNotNull(type);

		List<T> instances = new ArrayList<T>();
		if (reference.isMany()) {
			@SuppressWarnings("unchecked")
			EList<EObject> values = (EList<EObject>) owner.eGet(reference);
			for (EObject value : values) {
				if (isInstanceOf(value, type, exactMatch)) {
					instances.add(type.cast(value));
				}
			}
		}
		return instances;
	}

	private static AbstractTreeIterator<EObject> getAllContentsIncludingRoot(EObject contextObject) {
		AbstractTreeIterator<EObject> allContents = new AbstractTreeIterator<EObject>(contextObject, true) {
			private static final long serialVersionUID = 1L;

			@Override
			public Iterator<EObject> getChildren(Object object) {
				return ((EObject) object).eContents().iterator();
			}
		};
		return allContents;
	}

	/**
	 * Return all instances of a certain type within the provided {@link TreeIterator}
	 *
	 * @param allContents
	 *            The {@link TreeIterator} which instances of the expected type must be returned.
	 * @param type
	 *            The expected type of objects to return
	 * @param The
	 *            kind of search:
	 *            <ul>
	 *            <li><b><tt>true</tt>&nbsp;&nbsp;&nbsp;</b> if only specified type must be considered;<br>
	 *            <li><b><tt>false</tt>&nbsp;</b> if objects of inherited types are also wanted.
	 *            </ul>
	 * @return All instances of the specified type in the given list {@link TreeIterator}. Or an empty list if there
	 *         isn't any instances of the specified type in the Tree.
	 */
	private static <T> List<T> getAllInstancesOf(TreeIterator<EObject> allContents, Class<T> type, boolean exactMatch) {
		List<T> instances = new ArrayList<T>();
		for (TreeIterator<EObject> iter = allContents; iter.hasNext();) {
			EObject eObject = iter.next();
			if (isInstanceOf(eObject, type, exactMatch)) {
				instances.add(type.cast(eObject));
			}
		}
		return instances;
	}

	private static <T> boolean isInstanceOf(EObject eObject, Class<T> type, boolean exactMatch) {
		if (type.isInstance(eObject)) {
			if (!exactMatch || eObject.getClass() == type || eObject.eClass().getInstanceClass() == type) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the given {@link EObject} is set which is the case if any of its features are set. The optionally
	 * specified {@link EStructuralFeature ignored feature}s as well as {@link EReference#isContainer() container
	 * reference}s and derived {@link EStructuralFeature#isDerived() derived feature}s are excluded in this check.
	 *
	 * @param eObject
	 *            The {@link EObject} to test.
	 * @param ignoredFeatures
	 *            The {@link EStructuralFeature features}s to be ignored during the test.
	 * @return <code>true</code> if the given {@link EObject} is set, <code>false</code> otherwise.
	 */
	public static boolean isSet(EObject eObject, EStructuralFeature... ignoredFeatures) {
		List<EStructuralFeature> ignoredFeatureList = Arrays.asList(ignoredFeatures);
		for (EStructuralFeature feature : eObject.eClass().getEAllStructuralFeatures()) {
			// Skip container features
			if (feature instanceof EReference && ((EReference) feature).isContainer()) {
				continue;
			}

			// Skip ignored features
			if (ignoredFeatures != null && ignoredFeatureList.contains(feature)) {
				continue;
			}

			// Return true when hitting on any non-derived feature that is set
			if (!feature.isDerived()) {
				if (eObject.eIsSet(feature)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns a collection of {@link EStructuralFeature.Setting settings} objects describing the inverse references of
	 * given {@link EObject object}, i.e., the {@link EObject objects}s and {@link EStructuralFeature features}s that
	 * reference given {@link EObject object}.
	 *
	 * @param object
	 *            The {@link EObject object} whose {@link EStructuralFeature.Setting inverse references} are to be
	 * @param resolve
	 *            Determines whether or not proxies should be resolved.
	 * @return The collection of {@link EStructuralFeature.Setting inverse references} of given {@link EObject object}
	 *         or and empty collection if no such could be found.
	 */
	public static Collection<EStructuralFeature.Setting> getInverseReferences(EObject object, boolean resolve) {
		Notifier context = null;
		EObject modelRoot = EcoreUtil.getRootContainer(object);
		if (modelRoot != null) {
			Resource resource = modelRoot.eResource();
			if (resource != null) {
				ResourceSet resourceSet = resource.getResourceSet();
				if (resourceSet != null) {
					context = resourceSet;
				} else {
					context = resource;
				}
			} else {
				context = modelRoot;
			}
		}

		ECrossReferenceAdapter adapter = ECrossReferenceAdapterFactory.INSTANCE.adapt(context);
		if (adapter != null) {
			return adapter.getInverseReferences(object, resolve);
		}
		return Collections.emptyList();
	}

	/**
	 * Finds the {@link EPackage} that contains the {@link EClassifier} behind given Java <code>eClassifierType</code>.
	 *
	 * @param eClassifierType
	 *            The Java type of the {@link EClassifier} in question.
	 * @return The {@link EPackage} that contains the {@link EClassifier} behind <code>eClassifierType</code> or
	 *         <code>null</code> if <code>eClassifierType</code> refers to a Java type that is not an EClassifier or
	 *         EPackage for the EClassifier cannot be found.
	 */
	public static EPackage findEPackage(Class<?> eClassifierType) {
		Assert.isNotNull(eClassifierType);

		String eClassifierPackageName = eClassifierType.getPackage().getName();
		Collection<Object> safeEPackageObjects = new HashSet<Object>(EPackage.Registry.INSTANCE.values());
		for (Object ePackageObject : safeEPackageObjects) {
			Class<?> ePackageType = null;

			// Retrieve candidate EPackage from current EPackage object
			if (ePackageObject instanceof EPackage) {
				ePackageType = ((EPackage) ePackageObject).getClass();
			} else if (ePackageObject instanceof EPackage.Descriptor) {
				/*
				 * Performance optimization: Don't call org.eclipse.emf.ecore.EPackage.Descriptor#getEPackage() to
				 * retrieve the EPackage behind given EPackage.Descriptor and then its Java class. This would
				 * unnecessarily entail a full initialization of this EPackage.Descriptor's EPackage and the EPackages
				 * of all other EPackage.Descriptors we are about to iterate over.
				 */
				try {
					IConfigurationElement configurationElement = (IConfigurationElement) ReflectUtil.getInvisibleFieldValue(ePackageObject,
							"element"); //$NON-NLS-1$
					String ePackagePluginId = configurationElement.getDeclaringExtension().getContributor().getName();
					String ePackageTypeName = configurationElement.getAttribute("class"); //$NON-NLS-1$
					if (ePackageTypeName != null) {
						ePackageType = CommonPlugin.loadClass(ePackagePluginId, ePackageTypeName);
					}
				} catch (NoSuchFieldException ex) {
					// Unsupported EPackage.Descriptor implementation, ignore exception
				} catch (Exception ex) {
					PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
				}
			}

			// Retrieve interface package of current candidate EPackage and test if it matches the package of given
			// EClassifier
			if (ePackageType != null) {
				String interfacePackageName = getEMFModelInterfacePackageName(ePackageType);
				if (eClassifierPackageName.equals(interfacePackageName)) {
					return ePackageObject instanceof EPackage ? (EPackage) ePackageObject : ((EPackage.Descriptor) ePackageObject).getEPackage();
				}
			}
		}
		return null;
	}

	/**
	 * Returns the name of the Java package that contains the interfaces for the EMF metamodel elements defined in the
	 * {@link EPackage} behind provided EPackage class.
	 * <p>
	 * Makes the assumption that the interface package is either the same package as the Java package that contains the
	 * given EPackage class or its parent package in case that the provided EPackage class resides in a Java package
	 * that is postfixed with ".impl" (which corresponds to the default behavior of the EMF code generator).
	 * </p>
	 *
	 * @param ePackageType
	 *            The EPackage to be processed.
	 * @return The name of the Java package that contains the interfaces for the EMF metamodel elements defined in the
	 *         EPackage behind provided EPackage class.
	 */
	public static String getEMFModelInterfacePackageName(Class<?> ePackageType) {
		Assert.isNotNull(ePackageType);

		String ePackageTypePackageName = ePackageType.getPackage().getName();
		int implPackageIdx = ePackageTypePackageName.lastIndexOf('.' + DEFAULT_EMF_MODEL_IMPLEMENTATION_PACKAGE_SUFFIX);
		if (implPackageIdx != -1) {
			return ePackageTypePackageName.substring(0, implPackageIdx);
		}
		return ePackageTypePackageName;
	}

	/**
	 * Return the EClassifier in the given EPakcage by Class
	 *
	 * @param rootEPackage
	 *            The package container of return EClassifiers
	 * @param type
	 *            The specified class to find
	 * @return The EClassifier in the given root EPackage and its sub EPackages, or <code>null</code> if there isn't any
	 *         EClassifier has the given <code>type</code>.
	 */
	public static EClassifier findEClassifier(EPackage rootEPackage, Class<?> type) {
		Assert.isNotNull(type);

		return findEClassifier(rootEPackage, type.getSimpleName());
	}

	/**
	 * Return the EClassifier in the given EPackage by typeName
	 *
	 * @param rootEPackage
	 *            The package container of return EClassifiers
	 * @param typeName
	 *            The given name of the EClassifier to return
	 * @return <code><b>EClassifier</b></code> in the given rootPackage and its subPackages .
	 *         <p>
	 *         <code> null</code> if there isn't any EClassifier has the given <code><b>typeName</b></code>
	 *         </p>
	 */
	public static EClassifier findEClassifier(EPackage rootEPackage, String typeName) {
		Assert.isNotNull(rootEPackage);

		EClassifier eClassifier = rootEPackage.getEClassifier(typeName);
		if (eClassifier != null) {
			return eClassifier;
		}
		for (EPackage eSubPackage : rootEPackage.getESubpackages()) {
			eClassifier = findEClassifier(eSubPackage, typeName);
			if (eClassifier != null) {
				return eClassifier;
			}
		}
		return null;
	}

	/**
	 * Returns all EClasses which are derived from a given EClass.
	 *
	 * @param eClass
	 *            The EClass from which the returned EClasses have to be derived.
	 * @param concreteTypesOnly
	 *            If set to <tt>true</tt> only EClasses describing concrete classes will be returned. EClasses
	 *            describing interfaces and abstract classes will be excluded from the result.
	 * @return All EClasses which are derived from the provided <code>eClass</code>.
	 */
	public static List<EClass> findESubTypesOf(EClass eClass, boolean concreteTypesOnly) {
		IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(eClass);
		if (mmDescriptor != null) {
			List<EClass> subTypes = new ArrayList<EClass>();
			for (EPackage ePackage : mmDescriptor.getEPackages()) {
				for (EClassifier eClassifier : ePackage.getEClassifiers()) {
					if (eClassifier instanceof EClass) {
						EClass otherEClass = (EClass) eClassifier;
						if (eClass.isSuperTypeOf(otherEClass) && eClass != otherEClass) {
							if (!(otherEClass.isAbstract() || otherEClass.isInterface()) || !concreteTypesOnly) {
								subTypes.add((EClass) eClassifier);
							}
						}
					}
				}
			}
			return Collections.unmodifiableList(subTypes);
		}
		return Collections.emptyList();
	}

	/**
	 * Tests if given EClass is assignment compatible with type with specified name, i.e. if it is equal to or a sub
	 * type of type with specified name.
	 *
	 * @param eClass
	 *            the EClass to be tested.
	 * @param typeName
	 *            the name of the type.
	 * @return true if given EClass is equal or a subtype of type with specified name, false otherwise.
	 */
	public static boolean isAssignableFrom(EClass eClass, String typeName) {
		Assert.isNotNull(eClass);
		Assert.isNotNull(typeName);

		if (typeName.equals(EObject.class.getName()) || typeName.equals(EObject.class.getSimpleName())) {
			return true;
		}

		// Test if given EClass directly matches type with specified name
		if (eClass.getName().equals(typeName) || eClass.getInstanceClassName() != null && eClass.getInstanceClassName().equals(typeName)) {
			return true;
		}

		// Test if one of the super types of given EClass match
		for (EClass superType : eClass.getESuperTypes()) {
			if (isAssignableFrom(superType, typeName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return all EClassifiers that are containers and have a containment reference to the specified EClass
	 *
	 * @param eClass
	 *            The class whose container classifiers must be returned.
	 * @return The set of EClassifiers that have a containment reference to the specified EClass.
	 */
	public static List<EClassifier> getEContainerClassifiers(EClass eClass) {
		Assert.isNotNull(eClass);

		List<EClassifier> containerClassifiers = new ArrayList<EClassifier>();
		for (EReference reference : eClass.getEAllReferences()) {
			if (reference.isContainer()) {
				containerClassifiers.add(reference.getEType());
			}
		}
		return containerClassifiers;
	}

	/**
	 * @param ePackage
	 *            The EPackage container of returned EClassifiers
	 * @param annotationSource
	 *            The full URI representing the type of an annotation to filter
	 * @param detailKey
	 *            The given key of the annotationSource to filter
	 * @param detailValue
	 *            The given value of specified key of the annotationSource to filter
	 * @return The set of EClassifiers in the specified EPackage that:
	 *         <li>the Annotation matches the given <code><i><b>annotationSource</b></i></code> and</li>
	 *         <li>have a details with key is equals <code><i><b>detailKey</b></i></code> and</li>
	 *         <li>value is equals <code><i><b>detailValue</b></i></code></li>
	 */
	public static List<EClassifier> getAnnotatedEClassifiers(EPackage ePackage, String annotationSource, String detailKey, String detailValue) {
		Assert.isNotNull(ePackage);

		List<EClassifier> classifiers = new ArrayList<EClassifier>();
		for (EClassifier eClassifier : ePackage.getEClassifiers()) {
			String value = EcoreUtil.getAnnotation(eClassifier, annotationSource, detailKey);
			if (value != null && value.equalsIgnoreCase(detailValue)) {
				classifiers.add(eClassifier);
			}
		}
		return classifiers;
	}

	/**
	 * Returns the EStructuralFeature specified by the <code>featureName</code>. The method will return the
	 * EStructuralFeature if it exists for a given EClass or EObject. If the provided Object is not an EClass or an
	 * EObject or the EStructuralFeature does not exist, <code>null</code> is returned.
	 *
	 * @param owner
	 *            The EClass or EObject from which the EStructuralFeature is to be resolved.
	 * @param featureName
	 *            The name of the EStructuralFeature.
	 * @return The EStructuralFeature if the provided <code>object</code> is an EClass or EObject and the
	 *         EStructuralFeature exists, otherwise <code>null</code> is returned.
	 */
	public static EStructuralFeature getEStructuralFeature(Object owner, String featureName) {
		if (owner instanceof EClass) {
			return ((EClass) owner).getEStructuralFeature(featureName);
		}
		if (owner instanceof EObject) {
			return ((EObject) owner).eClass().getEStructuralFeature(featureName);
		}
		return null;
	}

	/**
	 * Returns the EStructuralFeature specified by the <code>featureID</code>. The method will return the
	 * EStructuralFeature if it exists for a given EClass or EObject. If the provided Object is not an EClass or an
	 * EObject or the EStructuralFeature does not exist, <code>null</code> is returned.
	 *
	 * @param owner
	 *            The EClass or EObject from which the EStructuralFeature is to be resolved.
	 * @param featureID
	 *            The id of the EStructuralFeature.
	 * @return The EStructuralFeature if the provided <code>object</code> is an EClass or EObject and the
	 *         EStructuralFeature exists, otherwise <code>null</code> is returned.
	 */
	public static EStructuralFeature getEStructuralFeature(Object owner, int featureID) {
		if (owner instanceof EClass) {
			return ((EClass) owner).getEStructuralFeature(featureID);
		}
		if (owner instanceof EObject) {
			return ((EObject) owner).eClass().getEStructuralFeature(featureID);
		}
		return null;
	}

	/**
	 * Lazily constitutes and returns a map with instance {@link Class class} to {@link EDataType data type} mappings
	 * for all data types included in the {@link EcorePackage}.
	 *
	 * @return A map with instance class to data type mappings for all Ecore-defined data types.
	 */
	private static synchronized Map<Class<?>, EDataType> getEcoreEDataTypeMappings() {
		if (eDataTypeMappings == null) {
			eDataTypeMappings = new HashMap<Class<?>, EDataType>();
			for (EClassifier classifier : EcorePackage.eINSTANCE.getEClassifiers()) {
				if (classifier instanceof EDataType) {
					EDataType dataType = (EDataType) classifier;
					eDataTypeMappings.put(dataType.getInstanceClass(), dataType);
				}
			}
		}
		return eDataTypeMappings;
	}

	/**
	 * Returns the {@link EDataType data type} for given instance {@link Class class}.
	 * <p>
	 * This implementation uses {@link #getEcoreEDataTypeMappings()} to retrieve the {@link EDataType data type} behind
	 * the given instance {@link Class class}. Any attempt to use instance classes other than those behind the
	 * {@link EDataType}s in the {@link EcorePackage} will not be successful and end up in <code>null</code> being
	 * returned.
	 * </p>
	 *
	 * @param instanceClass
	 *            The instance class for which to look up the data type.
	 * @return The data type corresponding to the given instance class, or <code>null</code> if no such could be found.
	 */
	public static EDataType getEDataType(Class<?> instanceClass) {
		return getEcoreEDataTypeMappings().get(instanceClass);
	}

	/**
	 * Creates a new instance of the given instance {@link Class class} from the provided {@link String} literal value.
	 * <p>
	 * This implementation uses {@link #getEDataType(Class)} to retrieve the {@link EDataType data type} behind the
	 * given instance {@link Class class} and then delegates to
	 * {@link EcoreFactory#createFromString(EDataType, String)}. The only exception are {@link URI}-typed literal values
	 * which are handled as a separate special case. Any attempt to use instance classes other than {@link URI} or those
	 * behind the {@link EDataType}s in the {@link EcorePackage} will give raise to an {@link IllegalArgumentException}.
	 * </p>
	 *
	 * @param instanceClass
	 *            The class of the instance to be created.
	 * @param literalValue
	 *            The literal string to create the instance of the given class from.
	 * @return A new instance of the given class that has been initialized from the provided literal value.
	 * @throws IllegalArgumentException
	 *             If the the given instance class is not supported or the provided literal value cannot be converted.
	 */
	public static Object createFromString(Class<?> instanceClass, String literalValue) {
		if (URI.class.equals(instanceClass)) {
			return URI.createURI(literalValue);
		}

		EDataType eDataType = getEDataType(instanceClass);
		if (eDataType == null) {
			throw new IllegalArgumentException("Unable to find matching EDataType for instance class '" + instanceClass.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return EcoreFactory.eINSTANCE.createFromString(eDataType, literalValue);
	}

	/**
	 * Returns all orphan objects referenced by specified source object via given reference. Orphan objects are target
	 * objects where all references which are non-containment, non-container and not the reference going back to
	 * specified owner object are unset. The owner object which may but does not have to be the container of the
	 * target/orphan objects.
	 *
	 * @param owner
	 *            The owner object of the returned objects
	 * @param reference
	 *            The given Reference to find orphans
	 * @return All orphan objects referenced by the specified owner via given EReference
	 */
	public static List<EObject> getOrphans(EObject owner, EReference reference) {
		Assert.isNotNull(owner);
		Assert.isNotNull(reference);

		List<EObject> values = new ArrayList<EObject>();
		if (reference.isMany()) {
			@SuppressWarnings("unchecked")
			List<EObject> valueObjects = (List<EObject>) owner.eGet(reference);
			values.addAll(valueObjects);
		} else {
			values.add((EObject) owner.eGet(reference));
		}

		List<EObject> orphans = new ArrayList<EObject>();
		for (EObject value : values) {
			boolean orphan = true;
			for (EReference valueReference : value.eClass().getEAllReferences()) {
				if (!valueReference.isContainment() && !valueReference.isContainer() && valueReference.getEType() instanceof EClass) {
					List<EObject> valuesOfValue = new ArrayList<EObject>();
					if (valueReference.isMany()) {
						@SuppressWarnings("unchecked")
						List<EObject> valueObjectsOfValue = (List<EObject>) value.eGet(valueReference);
						valuesOfValue.addAll(valueObjectsOfValue);
					} else {
						valuesOfValue.add((EObject) value.eGet(valueReference));
					}
					for (EObject valueOfValue : valuesOfValue) {
						if (valueOfValue != null && valueOfValue != owner) {
							orphan = false;
							break;
						}
					}
				}
				if (!orphan) {
					break;
				}
			}
			if (orphan) {
				orphans.add(value);
			}
		}
		return orphans;
	}

	/**
	 * Return mixedText of the given FeatureMap
	 *
	 * @param mixed
	 *            The given FeatureMap to get text
	 * @return The text for the given FeatureMap.
	 */
	public static String getMixedText(FeatureMap mixed) {
		Assert.isNotNull(mixed);

		Object textObject = mixed.get(org.eclipse.emf.ecore.xml.type.XMLTypePackage.eINSTANCE.getXMLTypeDocumentRoot_Text(), true);
		if (textObject instanceof FeatureMapUtil.FeatureEList<?>) {
			FeatureMapUtil.FeatureEList<?> featureEList = (FeatureMapUtil.FeatureEList<?>) textObject;
			if (featureEList.size() != 0) {
				Object text = featureEList.get(0);
				if (text instanceof String) {
					return (String) text;
				}
			}
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Set mixed text of the given FeatureMap
	 *
	 * @param mixed
	 *            The given FeatureMap to set text
	 * @param text
	 *            The input text set to the FeatureMap
	 */
	public static void setMixedText(FeatureMap mixed, String text) {
		Assert.isNotNull(mixed);

		mixed.clear();
		if (text != null) {
			mixed.add(org.eclipse.emf.ecore.xml.type.XMLTypePackage.eINSTANCE.getXMLTypeDocumentRoot_Text(), text);
		}
	}

	/**
	 * Turns given {@link EObject} in to a proxy object using an {@link URI} composed of the URI of the resource the
	 * EObject is in plus the URI fragment relative to that resource.
	 *
	 * @param eObject
	 *            The {@link EObject} to be proxified.
	 * @return The provided <code>eObject</code> is returned. If the proxy could not be created the given
	 *         {@link EObject} itself is returned.
	 */
	public static EObject proxify(EObject eObject) {
		return proxify(null, null, eObject);
	}

	/**
	 * Turns given {@link EObject} in to a proxy object using an {@link URI} composed of the URI of the resource the
	 * EObject is in plus the URI fragment relative to that resource.
	 *
	 * @param oldOwner
	 *            The old owner of the {@link EObject} to be proxified
	 * @param oldFeature
	 *            The containment reference of the {@link EObject} to be proxified
	 * @param eObject
	 *            The {@link EObject} to be proxified.
	 * @return The provided <code>eObject</code> is returned. If the proxy could not be created the given
	 *         {@link EObject} itself is returned.
	 */
	public static EObject proxify(EObject oldOwner, EStructuralFeature oldFeature, EObject eObject) {
		if (eObject != null) {
			// Proxify given EObject; use ExtendedResource for calculating proxy URI to enable metamodel-dependent
			// URI formats to be used
			if (!eObject.eIsProxy()) {
				URI uri;
				ExtendedResource extendedResource = ExtendedResourceAdapterFactory.INSTANCE.getExtendedResource(eObject);
				if (extendedResource == null) {
					extendedResource = ExtendedResourceAdapterFactory.INSTANCE.getExtendedResource(oldOwner);
				}
				if (extendedResource != null) {
					uri = extendedResource.getURI(oldOwner, oldFeature, eObject);
				} else {
					uri = EcoreUtil.getURI(eObject);
				}
				((InternalEObject) eObject).eSetProxyURI(uri);

				// Augment the proxy's URI to a context-aware proxy URI if required
				if (extendedResource != null) {
					extendedResource.augmentToContextAwareProxy(eObject);
				}
			}

			// Proxify contained children of given EObject
			for (EObject child : eObject.eContents()) {
				proxify(oldOwner, oldFeature, child);
			}
		}
		return eObject;
	}

	/**
	 * Turns a given proxy EObject into a normal EObject. Only EObjects derived from InternalEObject will be effected.
	 * If the provided EObject is an InternalEObject but not a proxy EObject the method has not effect on the provided
	 * <code>eObject</code>.
	 *
	 * @param eObject
	 *            The proxy EObject.
	 * @return The provided <code>eObject</code> is returned.
	 */
	public static EObject deproxify(EObject eObject) {
		if (eObject != null) {
			// Deproxify given EObject
			if (eObject.eIsProxy() && eObject instanceof InternalEObject) {
				((InternalEObject) eObject).eSetProxyURI(null);
			}

			// Deproxify contained children of given EObject
			for (Iterator<EObject> iter = eObject.eAllContents(); iter.hasNext();) {
				deproxify(iter.next());
			}
		}
		return eObject;
	}

	/**
	 * Creates a proxy representing given {@link EObject object}. The given object is used as a template to create the
	 * proxy, i.e. the resulting proxy will have the same type and a proxy {@link URI} which references the given
	 * object.
	 *
	 * @param eObject
	 *            The {@link EObject object} for which the proxy is to be created.
	 * @param contextResource
	 *            The {@link Resource resource} that is going to contain the newly created proxy.
	 * @return The newly created proxy, or <code>null</code> if no such could be created.
	 */
	public static EObject createProxyFrom(EObject eObject, Resource contextResource) {
		Assert.isNotNull(eObject);
		Assert.isNotNull(contextResource);

		EFactory eFactoryInstance = eObject.eClass().getEPackage().getEFactoryInstance();
		InternalEObject proxy = (InternalEObject) eFactoryInstance.create(eObject.eClass());

		proxy.eSetProxyURI(EcoreResourceUtil.getURI(eObject));

		// Augment the proxy's URI to a context-aware proxy URI if required
		ExtendedResource extendedContextResource = ExtendedResourceAdapterFactory.INSTANCE.adapt(contextResource);
		if (extendedContextResource != null) {
			extendedContextResource.augmentToContextAwareProxy(proxy);
		}

		return proxy;
	}

	/**
	 * @deprecated Use {@link #createProxyFrom(EObject, Resource)} instead.
	 */
	@Deprecated
	public static EObject createProxyFrom(EObject eObject) {
		return createProxyFrom(eObject, eObject.eResource());
	}

	/**
	 * Visits all proxies in given {@link Resource resource} and tries to resolve them.
	 * <p>
	 * Does principally the same thing as {@link EcoreUtil#resolveAll(Resource)} but provides more robustness by
	 * catching exceptions that are raised during proxy resolution and attaching them as errors on given {@link Resource
	 * resource}.
	 * </p>
	 *
	 * @param resource
	 *            The {@link Resource resource} to visit.
	 * @see EcoreUtil#resolveAll(Resource)
	 */
	public static void resolveAll(Resource resource) {
		for (Iterator<EObject> i = resource.getAllContents(); i.hasNext();) {
			EObject eObject = i.next();
			resolveCrossReferences(eObject);
		}
	}

	private static void resolveCrossReferences(EObject eObject) {
		try {
			for (Iterator<EObject> i = eObject.eCrossReferences().iterator(); i.hasNext(); i.next()) {
				// The loop resolves the cross references by visiting them
			}
		} catch (Exception ex) {
			Resource resource = eObject.eResource();
			if (resource != null) {
				// Exception due to something different than that resource does not exist?
				if (EcoreResourceUtil.exists(resource.getURI())) {
					// Leave an error about what has happened on resource
					resource.getErrors().add(new ProxyURIIntegrityException(
							NLS.bind(Messages.error_problemOccurredWhenResolvingReferencesOfObject, eObject.eClass().getName()), ex));
				}
			}
		}
	}
}
