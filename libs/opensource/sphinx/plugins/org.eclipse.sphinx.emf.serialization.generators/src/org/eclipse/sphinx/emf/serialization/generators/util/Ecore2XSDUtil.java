/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
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
package org.eclipse.sphinx.emf.serialization.generators.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.sphinx.emf.serialization.XMLPersistenceMappingExtendedMetaData;
import org.eclipse.xsd.util.XSDConstants;

public class Ecore2XSDUtil {

	static XMLPersistenceMappingExtendedMetaData metadata = XMLPersistenceMappingExtendedMetaData.INSTANCE;
	static Comparator<EClassifier> xmlNameComparator = new Comparator<EClassifier>() {
		@Override
		public int compare(EClassifier first, EClassifier second) {
			return metadata.getXMLName(first).compareTo(metadata.getXMLName(second));
		}
	};

	public static List<EClass> findAllConcreteTypes(EClass eClass, List<EPackage> additionalSearchScope) {
		List<EClass> concreteTypes = new ArrayList<EClass>();

		if (null != eClass) {
			// define the search scope
			List<EPackage> searchScope = new ArrayList<EPackage>();
			searchScope.add((EPackage) EcoreUtil.getRootContainer(eClass));
			if (null != additionalSearchScope) {
				searchScope.addAll(additionalSearchScope);
			}

			// add concrete classes
			if (!eClass.isAbstract()) {
				concreteTypes.add(eClass);
			}

			for (EPackage searchPackage : searchScope) {
				concreteTypes.addAll(findESubTypesOf(eClass, searchPackage, true));
			}

			// sort the list alphabetically by the XML type name
			Collections.sort(concreteTypes, xmlNameComparator);

		}

		return concreteTypes;
	}

	public static List<EClass> findESubTypesOf(EClass eClass) {
		List<EClass> subTypes = new ArrayList<EClass>();
		EPackage rootEPackage = (EPackage) EcoreUtil.getRootContainer(eClass);
		subTypes.addAll(findESubTypesOf(eClass, rootEPackage, true));
		return subTypes;
	}

	public static List<EClass> findESubTypesOf(EClass eClass, EPackage ePackage, boolean concreteTypesOnly) {
		List<EClass> subTypes = new ArrayList<EClass>();
		for (EClassifier eClassifier : ePackage.getEClassifiers()) {
			if (eClassifier instanceof EClass) {
				EClass otherEClass = (EClass) eClassifier;
				if (eClass.isSuperTypeOf(otherEClass) && eClass != otherEClass) {
					if (!(otherEClass.isAbstract() || otherEClass.isInterface()) || !concreteTypesOnly) {
						subTypes.add(otherEClass);
					}
				}
			}
		}
		for (EPackage subPackage : ePackage.getESubpackages()) {
			subTypes.addAll(findESubTypesOf(eClass, subPackage, concreteTypesOnly));
		}

		return subTypes;
	}

	public static boolean isIgnoredAnnotationSource(String sourceURI) {
		return EcorePackage.eNS_URI.equals(sourceURI) || ExtendedMetaData.ANNOTATION_URI.equals(sourceURI)
				|| IGeneratorConstants.GEN_MODEL_PACKAGE_NS_URI.equals(sourceURI);
	}

	public static String getURI(ExtendedMetaData extendedMetaData, EStructuralFeature eStructuralFeature) {
		String namespace = extendedMetaData.getNamespace(eStructuralFeature);
		String name = extendedMetaData.getName(eStructuralFeature);
		if (XMLTypePackage.eNS_URI.equals(namespace)) {
			namespace = XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001;
		}

		String result = name;
		if (namespace != null) {
			result = namespace + "#" + name; //$NON-NLS-1$
		}
		return result;
	}

	public static List<EClassifier> getGlobalElements(EPackage rootPackage) {
		List<EClassifier> globalElements = new ArrayList<EClassifier>();

		for (EClassifier eClassifier : rootPackage.getEClassifiers()) {
			if (metadata.isXMLGlobalElement(eClassifier) && isConcrete(eClassifier)) {
				globalElements.add(eClassifier);
			}
		}

		for (EPackage ePackage : rootPackage.getESubpackages()) {
			globalElements.addAll(getGlobalElements(ePackage));
		}

		// sort the list alphabetically by the XML type name
		Collections.sort(globalElements, xmlNameComparator);

		return globalElements;
	}

	public static String handlePrefix(Map<String, String> namespaces, String preferredPrefix, String namespace) {
		if (XMLNamespacePackage.eNS_URI.equals(namespace)) {
			return "xml"; //$NON-NLS-1$
		}

		String value = namespaces.get(preferredPrefix);
		if (namespace == null ? value == null : namespace.equals(value)) {
			return preferredPrefix;
		}

		// If there is a non-null value, i.e., if the prefix is in use, see if there is already a prefix chosen.
		if (value != null || XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001.equals(namespace)) {
			for (Map.Entry<String, String> entry : namespaces.entrySet()) {
				if (namespace == null ? entry.getValue() == null : namespace.equals(entry.getValue())) {
					// Return the previously assigned prefix; it may not match the preferred one.
					return entry.getKey();
				}

			}
		}

		String uniquePrefix = preferredPrefix;
		for (int i = 0; namespaces.containsKey(uniquePrefix); ++i) {
			uniquePrefix = preferredPrefix + "_" + i; //$NON-NLS-1$
		}

		String namespaceKey = null;
		if (!"".equals(uniquePrefix)) { //$NON-NLS-1$
			namespaceKey = uniquePrefix;
		}

		namespaces.put(namespaceKey, namespace);
		return uniquePrefix;
	}

	public static Boolean isCustomSimpleType(EDataType eDataType) {
		if (XMLPersistenceMappingExtendedMetaData.INSTANCE.getXMLCustomSimpleType(eDataType).equals(IGeneratorConstants.BOOLEAN_TRUE)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the prefix of a namesapce from the namespaces map
	 *
	 * @param namespaces
	 *            a <prefix, namespace> map
	 * @param ns
	 * @return
	 */
	public static String getNsPrefixFromQNamePrefixToNamespaceMap(Map<String, String> namespaces, String ns) {
		if (namespaces.containsValue(ns)) {
			for (String prefix : namespaces.keySet()) {
				if (namespaces.get(prefix).equals(ns)) {
					return prefix;
				}
			}
		}
		return null;
	}

	/**
	 * to be override
	 */
	public static String getSingularName(ENamedElement element) {

		String suffixSingualr = ""; //$NON-NLS-1$

		// check if namedElement is an EReference
		if (element instanceof EReference) {
			EReference ref = (EReference) element;
			// check if reference is not a composition
			if (!ref.isContainment()) {
				// normal ref
				suffixSingualr = IGeneratorConstants.SUFFIX_SINGULAR_REF;
			}
		}

		return buildXmlName(element.getName()) + suffixSingualr;
	}

	/**
	 * to be override
	 */
	public static String getPluralName(ENamedElement element) {

		String suffixPlural = "S"; //$NON-NLS-1$

		// check if namedElement is an EReference
		if (element instanceof EReference) {
			EReference ref = (EReference) element;
			// check if reference is not a composition
			if (!ref.isContainment()) {
				// normal ref
				suffixPlural = IGeneratorConstants.SUFFIX_PLURAL_REF;
			}
		}

		return buildXmlName(element.getName()) + suffixPlural;
	}

	/**
	 * Checks if a EClass has concrete sub classes.
	 *
	 * @param eClass
	 *            super class
	 * @return true if eClass has sub classes
	 */
	public static boolean hasConcreteSubclasses(EClass eClass, EPackage model) {
		Iterator<EObject> iterator = model.eAllContents();
		while (iterator.hasNext()) {
			EObject element = iterator.next();
			if (element instanceof EClass) {
				EClass potentialSubclass = (EClass) element;
				if (!potentialSubclass.isAbstract() && potentialSubclass.getESuperTypes().contains(eClass)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if a classifier is concrete. i.e. it is an EDataType or an EClass that is not abstract
	 *
	 * @param eClassifier
	 * @return true if eClassifier is concrete
	 */
	public static boolean isConcrete(EClassifier eClassifier) {
		boolean isConcrete = true;
		if (eClassifier instanceof EClass) {
			EClass eClass = (EClass) eClassifier;
			if (eClass.isAbstract()) {
				isConcrete = false;
			}
		}
		return isConcrete;
	}

	/**
	 * Creates a name conform with EAST-ADL XML tag name rules: e.g. "thisIsACamelStyleName" ->
	 * "THIS-IS-A-CAMEL-STYLE-NAME".
	 *
	 * @param camelName
	 *            The original camel-style name.
	 * @return The name according to EAST-ADL XML conventions.
	 */
	public static String buildXmlName(String camelName) {
		// get segments in name and rejoin them the defined XML way:
		LinkedList<?> nameSegments = getCamelNameSegments(camelName);
		Iterator<?> segIter = nameSegments.iterator();
		boolean isFirst = true;

		String xmlName = new String();
		while (segIter.hasNext()) {
			if (!isFirst) {
				xmlName += "-"; //$NON-NLS-1$
			} else {
				isFirst = false;
			}
			xmlName += ((String) segIter.next()).toUpperCase();
		}

		return xmlName;
	}

	/**
	 * Returns the list of segments used in a camel style name. If a sequence of single captialized characters is found
	 * it is returned until the beginning of a new word.
	 *
	 * @param camelName
	 *            The camel-style name (e.g. "thisIsACamelStyleName").
	 * @return The list of string segments in the name (e.g. "this", "Is", "A", "Camel", "Style", "Name" ).
	 */
	private static LinkedList<?> getCamelNameSegments(String camelName) {
		if (camelName == null || camelName.length() == 0) {
			return null;
		}

		// ---- first build list of all segments starting with a captial letter:
		LinkedList<String> segmentList = new LinkedList<String>();
		String segment = new String();
		segment += camelName.charAt(0);
		boolean wasDigit = false;
		boolean wasNotLetterOrDigit = false;

		for (int i = 1; i < camelName.length(); i++) {
			char c = camelName.charAt(i);

			boolean isLetter = Character.isLetter(c);
			boolean isUpperCase = Character.isUpperCase(c);

			// a new segment starts with an uppercase letter, or if a non-usable
			// character is found, or name switches between character and digit:
			if (!isLetter || isUpperCase || wasDigit || wasNotLetterOrDigit) {
				// start new segment:
				if (segment.length() > 0) {
					segmentList.add(segment);
					segment = new String();
				}
			}

			segment += c;
			wasDigit = Character.isDigit(c);
			wasNotLetterOrDigit = !isLetter && !wasDigit;
		}

		// add final segment:
		if (segment.length() > 0) {
			segmentList.add(segment);
		}

		// ---- now link segments of single captial letters and sets of digits:
		ListIterator<String> segmentIter = segmentList.listIterator(segmentList.size());
		String segment2 = segmentIter.previous();

		while (segmentIter.hasPrevious()) {
			String segment1 = segmentIter.previous();
			char c = segment1.charAt(0);

			// in case of a non-digit, non-letter character, we simply remove it:
			if (!Character.isDigit(c) && !Character.isLetter(c)) {
				segmentIter.remove();
				segment2 = segment1;
				continue;
			}

			char d = segment2.charAt(segment2.length() - 1);
			boolean doLink = Character.isUpperCase(c) && Character.isUpperCase(d);
			doLink |= Character.isDigit(c) && Character.isDigit(d);

			if (segment1.length() == 1 && doLink) {

				// combine segments 1 and 2:
				segment2 = segment1 + segment2;

				segmentIter.remove();
				segmentIter.next();
				segmentIter.set(segment2);
				segmentIter.previous();
			} else {
				segment2 = segment1;
			}
		}

		return segmentList;
	}
}
