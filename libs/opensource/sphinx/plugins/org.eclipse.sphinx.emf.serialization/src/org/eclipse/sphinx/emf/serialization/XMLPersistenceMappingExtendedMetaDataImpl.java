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
package org.eclipse.sphinx.emf.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class XMLPersistenceMappingExtendedMetaDataImpl extends BasicExtendedMetaData implements XMLPersistenceMappingExtendedMetaData {

	protected static final String UNINITIALIZED_STRING = "uninitialized"; //$NON-NLS-1$
	protected static final int UNINITIALIZED_INT = Integer.MIN_VALUE;
	protected static final Boolean UNINITIALIZED_BOOLEAN = null;
	protected static final Map<String, String> UNINITIALIZED_STRING_STRING_MAP = null;

	static final int FEATURE_WRAPPER_ELEMENT_MASK = 8;
	static final int FEATURE_ELEMENT_MASK = 4;
	static final int CLASSIFIER_WRAPPER_ELEMENT_MASK = 2;
	static final int CLASSIFIER_ELEMENT_MASK = 1;

	static final String PLURAL_EXTENSION = "s"; //$NON-NLS-1$
	static final String BOOLEAN_TRUE = "true"; //$NON-NLS-1$
	static final String BOOLEAN_FALSE = "false"; //$NON-NLS-1$

	static final String XML_ATTRIBUTE = "xmlAttribute"; // xml.attribute //$NON-NLS-1$
	static final String XML_CUSTOM_SIMPLE_TYPE = "xmlCustomSimpleType"; // xsd.customType //$NON-NLS-1$
	static final String XML_CUSTOM_SIMPLE_TYPE_VALUE_PATTERN = "xmlCustomSimpleTypeValuePattern"; // xsd.pattern //$NON-NLS-1$
	static final String XML_XSD_SIMPLE_TYPE = "xmlXsdSimpleType"; // xsd.type //$NON-NLS-1$

	protected EPackage.Registry registry;

	protected int[] fallbackSerializationConfiguration = {
			XML_PERSISTENCE_MAPPING_STRATEGY__0000__NONE /* 0000 */,
			// SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT/* 0001 */,
			XML_PERSISTENCE_MAPPING_STRATEGY__0001__CLASSIFIER_ELEMENT /* 0001 */,
			// SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT/* 0010 */,
			XML_PERSISTENCE_MAPPING_STRATEGY__0010__CLASSIFIER_WRAPPER_ELEMENT /* 0010 */,
			// SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT/* 0011 */,
			XML_PERSISTENCE_MAPPING_STRATEGY__0011__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT/* 0011 */,
			XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT /* 0100 */,
			XML_PERSISTENCE_MAPPING_STRATEGY__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT/* 0101 */,
			// SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT/* 0110 */,
			XML_PERSISTENCE_MAPPING_STRATEGY__0110__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT/* 0110 */,
			// SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT/* 0111 */,
			XML_PERSISTENCE_MAPPING_STRATEGY__0111__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT /* 0111 */,
			XML_PERSISTENCE_MAPPING_STRATEGY__1000__FEATURE_WRAPPER_ELEMENT /* 1000 */,
			XML_PERSISTENCE_MAPPING_STRATEGY__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT/* 1001 */,
			XML_PERSISTENCE_MAPPING_STRATEGY__1010__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT/* 1010 */,
			XML_PERSISTENCE_MAPPING_STRATEGY__1011__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT/* 1011 */,
			XML_PERSISTENCE_MAPPING_STRATEGY__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT/* 1100_ */,
			XML_PERSISTENCE_MAPPING_STRATEGY__1101__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_ELEMENT /* 1101 */,
			XML_PERSISTENCE_MAPPING_STRATEGY__1110__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT/* 1110 */,
			XML_PERSISTENCE_MAPPING_STRATEGY__1111__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT /* 1111 */
	};

	protected Map<EModelElement, Object> extendedMetaDataCache = new HashMap<EModelElement, Object>();

	public static interface XMLPersistenceMappingEPackageExtendedMetaData {
		EClassifier getType(String name);

		EClassifier getType(String name, EStructuralFeature feature);

		EClassifier getTypeByWrapperName(String wrapperName);

		Map<String, String> getXMLExternalSchemaLocations();

		void setXMLExternalSchemaLocations(Map<String, String> externalSchemaLocations);

		String getXMLSchemaLocation();

		void setXMLDefaultEReferenceReferencedTypeAttributeName(String defaultEReferenceReferencedTypeAttributeName);

		String getXMLDefaultEReferenceReferencedTypeAttributeName();

		void setXMLDefaultEReferenceReferencedTypeIdentificationStrategy(int defaultEReferenceReferencedTypeIdentificationStrategy);

		int getXMLDefaultEReferenceReferencedTypeIdentificationStrategy();

		void setXMLDefaultEReferenceReferencedTypeDeclarationStrategy(int defaultEReferenceReferencedTypeDeclarationStrategy);

		int getXMLDefaultEReferenceReferencedTypeDeclarationStrategy();

		void setXMLDefaultEReferenceContainedTypeAttributeName(String defaultEReferenceContainedTypeAttributeName);

		String getXMLDefaultEReferenceContainedTypeAttributeName();

		void setXMLDefaultEReferenceContainedTypeIdentificationStrategy(int defaultEReferenceContainedTypeIdentificationStrategy);

		int getXMLDefaultEReferenceContainedTypeIdentificationStrategy();

		void setXMLDefaultEReferenceContainedTypeDeclarationStrategy(int defaultEReferenceContainedTypeDeclarationStrategy);

		int getXMLDefaultEReferenceContainedTypeDeclarationStrategy();

		void setXMLSchemaLocation(String schemaLocation);

		void renameToXMLName(EClassifier eClassifier, String newName);

		void renameToXMLWrapperName(EClassifier eClassifier, String newName);
	}

	public class XMLPersistenceMappingEPackageExtendedMetaDataImpl implements XMLPersistenceMappingEPackageExtendedMetaData {
		protected EPackage ePackage;
		protected boolean isInitialized;
		protected boolean isQualified;
		protected String schemaLocation = UNINITIALIZED_STRING;
		protected Map<String, String> externalSchemaLocations = UNINITIALIZED_STRING_STRING_MAP;
		protected Map<String, EClassifier> xmlNameToClassifierMap = new HashMap<String, EClassifier>();
		protected Map<String, EClassifier> xmlWrapperNameToClassifierMap = new HashMap<String, EClassifier>();
		protected String defaultEReferenceReferencedTypeAttributeName = UNINITIALIZED_STRING;
		protected int defaultEReferenceReferencedTypeDeclarationStrategy = UNINITIALIZED_INT;
		protected int defaultEReferenceReferencedTypeIdentificationStrategy = UNINITIALIZED_INT;

		protected String defaultEReferenceContainedTypeAttributeName = UNINITIALIZED_STRING;
		protected int defaultEReferenceContainedTypeDeclarationStrategy = UNINITIALIZED_INT;
		protected int defaultEReferenceContainedTypeIdentificationStrategy = UNINITIALIZED_INT;

		public XMLPersistenceMappingEPackageExtendedMetaDataImpl(EPackage ePackage) {
			this.ePackage = ePackage;
		}

		@Override
		public EClassifier getType(String name) {
			EClassifier result = null;
			if (xmlNameToClassifierMap != null) {
				result = xmlNameToClassifierMap.get(name);
			}
			if (result == null) {
				List<EClassifier> eClassifiers = ePackage.getEClassifiers();
				int size = eClassifiers.size();
				if (xmlNameToClassifierMap == null || xmlNameToClassifierMap.size() != size) {
					Map<String, EClassifier> nameToClassifierMap = new HashMap<String, EClassifier>();
					if (xmlNameToClassifierMap != null) {
						nameToClassifierMap.putAll(xmlNameToClassifierMap);
					}

					// For demand created created packages we allow the list of classifiers to grow
					// so this should handle those additional instances.
					//
					int originalMapSize = nameToClassifierMap.size();
					for (int i = originalMapSize; i < size; ++i) {
						EClassifier eClassifier = eClassifiers.get(i);
						String eClassifierName = getXMLName(eClassifier);
						EClassifier conflictingEClassifier = nameToClassifierMap.put(eClassifierName, eClassifier);
						if (conflictingEClassifier != null && conflictingEClassifier != eClassifier) {
							nameToClassifierMap.put(eClassifierName, conflictingEClassifier);
						}
					}

					if (nameToClassifierMap.size() != size) {
						for (int i = 0; i < originalMapSize; ++i) {
							EClassifier eClassifier = eClassifiers.get(i);
							String eClassifierName = getXMLName(eClassifier);
							EClassifier conflictingEClassifier = nameToClassifierMap.put(eClassifierName, eClassifier);
							if (conflictingEClassifier != null && conflictingEClassifier != eClassifier) {
								nameToClassifierMap.put(eClassifierName, conflictingEClassifier);
							}
						}
					}
					result = nameToClassifierMap.get(name);
					xmlNameToClassifierMap = nameToClassifierMap;
				}
			}

			return result;
		}

		@Override
		public EClassifier getType(String name, EStructuralFeature feature) {
			// TODO optimize implementation for performance
			String classifierNameSuffix = getXMLPersistenceMappingExtendedMetaData(feature).getXMLClassiferNameSuffix();
			EClassifier classifier;
			int classifierNameSuffixLength = classifierNameSuffix.length();
			if (0 == classifierNameSuffixLength) {
				classifier = getType(name);
			} else {
				if (name.endsWith(classifierNameSuffix)) {
					String classfierName = name.substring(0, name.length() - classifierNameSuffixLength);
					classifier = getType(classfierName);
				} else {
					classifier = null;
				}
			}

			return classifier;
		}

		@Override
		public EClassifier getTypeByWrapperName(String name) {
			EClassifier result = null;
			if (xmlWrapperNameToClassifierMap != null) {
				result = xmlWrapperNameToClassifierMap.get(name);
			}
			if (result == null) {
				List<EClassifier> eClassifiers = ePackage.getEClassifiers();
				int size = eClassifiers.size();
				if (xmlWrapperNameToClassifierMap == null || xmlWrapperNameToClassifierMap.size() != size) {
					Map<String, EClassifier> wrapperNameToClassifierMap = new HashMap<String, EClassifier>();
					if (xmlWrapperNameToClassifierMap != null) {
						wrapperNameToClassifierMap.putAll(xmlWrapperNameToClassifierMap);
					}

					// For demand created created packages we allow the list of classifiers to grow
					// so this should handle those additional instances.
					//
					int originalMapSize = wrapperNameToClassifierMap.size();
					for (int i = originalMapSize; i < size; ++i) {
						EClassifier eClassifier = eClassifiers.get(i);
						String eClassifierWrapperName = getXMLWrapperName(eClassifier);
						EClassifier conflictingEClassifier = wrapperNameToClassifierMap.put(eClassifierWrapperName, eClassifier);
						if (conflictingEClassifier != null && conflictingEClassifier != eClassifier) {
							wrapperNameToClassifierMap.put(eClassifierWrapperName, conflictingEClassifier);
						}
					}

					if (wrapperNameToClassifierMap.size() != size) {
						for (int i = 0; i < originalMapSize; ++i) {
							EClassifier eClassifier = eClassifiers.get(i);
							String eClassifierWrapperName = getXMLWrapperName(eClassifier);
							EClassifier conflictingEClassifier = wrapperNameToClassifierMap.put(eClassifierWrapperName, eClassifier);
							if (conflictingEClassifier != null && conflictingEClassifier != eClassifier) {
								wrapperNameToClassifierMap.put(eClassifierWrapperName, conflictingEClassifier);
							}
						}
					}
					result = wrapperNameToClassifierMap.get(name);
					xmlWrapperNameToClassifierMap = wrapperNameToClassifierMap;
				}
			}

			return result;
		}

		@Override
		public String getXMLSchemaLocation() {
			if (UNINITIALIZED_STRING == schemaLocation) {
				setXMLSchemaLocation(basicGetXMLSchemaLocation(ePackage));
			}
			return schemaLocation;
		}

		@Override
		public void setXMLSchemaLocation(String schemaLocation) {
			this.schemaLocation = schemaLocation;
		}

		@Override
		public Map<String, String> getXMLExternalSchemaLocations() {
			if (UNINITIALIZED_STRING_STRING_MAP == externalSchemaLocations) {
				setXMLExternalSchemaLocations(basicGetXMLExternalSchemaLocations(ePackage));
			}
			return externalSchemaLocations;
		}

		@Override
		public void setXMLExternalSchemaLocations(Map<String, String> externalSchemaLocations) {
			this.externalSchemaLocations = externalSchemaLocations;
		}

		@Override
		public void setXMLDefaultEReferenceReferencedTypeAttributeName(String defaultEReferenceTypeAttributeName) {
			defaultEReferenceReferencedTypeAttributeName = defaultEReferenceTypeAttributeName;
		}

		@Override
		public String getXMLDefaultEReferenceReferencedTypeAttributeName() {
			if (UNINITIALIZED_STRING == defaultEReferenceReferencedTypeAttributeName) {
				setXMLDefaultEReferenceReferencedTypeAttributeName(basicGetDefaultEReferenceReferencedTypeAttributeName(ePackage));
			}
			return defaultEReferenceReferencedTypeAttributeName;
		}

		@Override
		public int getXMLDefaultEReferenceReferencedTypeIdentificationStrategy() {
			if (UNINITIALIZED_INT == defaultEReferenceReferencedTypeIdentificationStrategy) {
				setXMLDefaultEReferenceReferencedTypeIdentificationStrategy(basicGetDefaultEReferenceReferencedTypeIdentificationStrategy(ePackage));
			}
			return defaultEReferenceReferencedTypeIdentificationStrategy;
		}

		@Override
		public void setXMLDefaultEReferenceReferencedTypeIdentificationStrategy(int defaultEReferenceTypeIdentificationStrategy) {
			defaultEReferenceReferencedTypeIdentificationStrategy = defaultEReferenceTypeIdentificationStrategy;
		}

		@Override
		public int getXMLDefaultEReferenceReferencedTypeDeclarationStrategy() {
			if (UNINITIALIZED_INT == defaultEReferenceReferencedTypeDeclarationStrategy) {
				setXMLDefaultEReferenceReferencedTypeDeclarationStrategy(basicGetDefaultEReferenceReferencedTypeDeclarationStrategy(ePackage));
			}
			return defaultEReferenceReferencedTypeDeclarationStrategy;
		}

		@Override
		public void setXMLDefaultEReferenceReferencedTypeDeclarationStrategy(int defaultEReferenceTypeDeclarationStrategy) {
			defaultEReferenceReferencedTypeDeclarationStrategy = defaultEReferenceTypeDeclarationStrategy;
		}

		@Override
		public String getXMLDefaultEReferenceContainedTypeAttributeName() {
			if (UNINITIALIZED_STRING == defaultEReferenceContainedTypeAttributeName) {
				setXMLDefaultEReferenceContainedTypeAttributeName(basicGetDefaultEReferenceContainedTypeAttributeName(ePackage));
			}
			return defaultEReferenceContainedTypeAttributeName;
		}

		@Override
		public void setXMLDefaultEReferenceContainedTypeAttributeName(String defaultEReferenceContainedTypeAttributeName) {
			this.defaultEReferenceContainedTypeAttributeName = defaultEReferenceContainedTypeAttributeName;
		}

		@Override
		public int getXMLDefaultEReferenceContainedTypeIdentificationStrategy() {
			if (UNINITIALIZED_INT == defaultEReferenceContainedTypeIdentificationStrategy) {
				setXMLDefaultEReferenceContainedTypeIdentificationStrategy(basicGetDefaultEReferenceContainedTypeIdentificationStrategy(ePackage));
			}
			return defaultEReferenceContainedTypeIdentificationStrategy;
		}

		@Override
		public void setXMLDefaultEReferenceContainedTypeIdentificationStrategy(int defaultEReferenceContainedTypeIdentificationStrategy) {
			this.defaultEReferenceContainedTypeIdentificationStrategy = defaultEReferenceContainedTypeIdentificationStrategy;
		}

		@Override
		public int getXMLDefaultEReferenceContainedTypeDeclarationStrategy() {
			if (UNINITIALIZED_INT == defaultEReferenceContainedTypeDeclarationStrategy) {
				setXMLDefaultEReferenceContainedTypeDeclarationStrategy(basicGetDefaultEReferenceContainedTypeDeclarationStrategy(ePackage));
			}
			return defaultEReferenceContainedTypeDeclarationStrategy;
		}

		@Override
		public void setXMLDefaultEReferenceContainedTypeDeclarationStrategy(int defaultEReferenceContainedTypeDeclarationStrategy) {
			this.defaultEReferenceContainedTypeDeclarationStrategy = defaultEReferenceContainedTypeDeclarationStrategy;
		}

		@Override
		public void renameToXMLName(EClassifier eClassifier, String newName) {
			if (xmlNameToClassifierMap != null) {
				xmlNameToClassifierMap.values().remove(eClassifier);
				xmlNameToClassifierMap.put(newName, eClassifier);
			}
		}

		@Override
		public void renameToXMLWrapperName(EClassifier eClassifier, String newName) {
			if (xmlWrapperNameToClassifierMap != null) {
				xmlWrapperNameToClassifierMap.values().remove(eClassifier);
				xmlWrapperNameToClassifierMap.put(newName, eClassifier);
			}
		}
	}

	public static interface XMLPersistenceMappingEClassifierExtendedMetaData {
		String getXMLName();

		void setXMLName(String name);

		String getXMLWrapperName();

		void setXMLWrapperName(String name);

		boolean isXMLGlobalElement();

		void setXMLGlobalElement(boolean isGlobalElement);

		String getXMLCustomSimpleType();

		public void setXMLCustomSimpleType(String xmlCustomTypeName);

		String getXMLXsdSimpleType();

		public void setXMLXsdSimpleType(String xmlXsdSimpleType);

		String getXMLCustomSimpleTypeValuePattern();

		public void setXMLCustomSimpleTypeValuePattern(String xmlXsdPattern);

		EStructuralFeature getFeatureByXMLElementName(String namespace, String xmlElementName);

	}

	abstract class XMLPersistenceMappingEClassifierExtendedMetaDataImpl implements XMLPersistenceMappingEClassifierExtendedMetaData {
		protected EClassifier eClassifier;
		protected String xmlName = UNINITIALIZED_STRING;
		protected String xmlWrapperName = UNINITIALIZED_STRING;
		protected Boolean xmlGlobalElement = UNINITIALIZED_BOOLEAN;

		public XMLPersistenceMappingEClassifierExtendedMetaDataImpl(EClassifier eClassifier) {
			super();
			this.eClassifier = eClassifier;
		}

		@Override
		public String getXMLName() {
			if (UNINITIALIZED_STRING == xmlName) {
				setXMLName(basicGetName(eClassifier));
			}
			return xmlName;
		}

		@Override
		public void setXMLName(String xmlName) {
			this.xmlName = xmlName;
		}

		@Override
		public String getXMLWrapperName() {
			if (UNINITIALIZED_STRING == xmlName) {
				setXMLWrapperName(basicGetWrapperName(eClassifier));
			}
			return xmlWrapperName;
		}

		@Override
		public void setXMLWrapperName(String xmlWrapperName) {
			this.xmlWrapperName = xmlWrapperName;
		}

		@Override
		public boolean isXMLGlobalElement() {
			if (UNINITIALIZED_BOOLEAN == xmlGlobalElement) {
				setXMLGlobalElement(basicIsGlobalElement(eClassifier));
			}
			return xmlGlobalElement;
		}

		@Override
		public void setXMLGlobalElement(boolean globalElement) {
			xmlGlobalElement = globalElement;

		}
	}

	class XMLPersistenceMappingEDataTypeExtendedMetaDataImpl extends XMLPersistenceMappingEClassifierExtendedMetaDataImpl {
		protected String xmlCustomSimpleType = UNINITIALIZED_STRING;
		protected String xmlXsdSimpleType = UNINITIALIZED_STRING;
		protected String xmlCustomSimpleTypeValuePattern = UNINITIALIZED_STRING;

		public XMLPersistenceMappingEDataTypeExtendedMetaDataImpl(EClassifier eClassifier) {
			super(eClassifier);
			assert eClassifier instanceof EDataType;
		}

		@Override
		public String getXMLCustomSimpleType() {
			if (UNINITIALIZED_STRING == xmlCustomSimpleType) {
				setXMLCustomSimpleType(basicGetXMLCustomSimpleType(eClassifier));
			}
			return xmlCustomSimpleType;
		}

		@Override
		public void setXMLCustomSimpleType(String xmlCustomSimpleType) {
			this.xmlCustomSimpleType = xmlCustomSimpleType;
		}

		@Override
		public String getXMLXsdSimpleType() {
			if (UNINITIALIZED_STRING == xmlXsdSimpleType) {
				setXMLXsdSimpleType(basicGetXMLXsdSimpleType(eClassifier));
			}
			return xmlXsdSimpleType;
		}

		@Override
		public void setXMLXsdSimpleType(String xmlXsdSimpleType) {
			this.xmlXsdSimpleType = xmlXsdSimpleType;

		}

		@Override
		public String getXMLCustomSimpleTypeValuePattern() {
			if (UNINITIALIZED_STRING == xmlCustomSimpleTypeValuePattern) {
				setXMLXsdSimpleType(basicGetXMLXsdSimpleType(eClassifier));
			}
			return xmlCustomSimpleTypeValuePattern;
		}

		@Override
		public void setXMLCustomSimpleTypeValuePattern(String xmlXsdPattern) {
			xmlCustomSimpleTypeValuePattern = xmlXsdPattern;
		}

		@Override
		public EStructuralFeature getFeatureByXMLElementName(String namespace, String xmlElementName) {
			throw new UnsupportedOperationException("Can't get a feature of an EDataType"); //$NON-NLS-1$
		}
	}

	class XMLPersistenceMappingEClassExtendedMetaDataImpl extends XMLPersistenceMappingEClassifierExtendedMetaDataImpl {
		protected Map<String, EStructuralFeature> xmlNameToEStructuralFeatureMap = new HashMap<String, EStructuralFeature>();

		public XMLPersistenceMappingEClassExtendedMetaDataImpl(EClassifier eClassifier) {
			super(eClassifier);
			assert eClassifier instanceof EClass;
		}

		@Override
		public String getXMLCustomSimpleType() {
			throw new UnsupportedOperationException("EClasses are not mapped to xsd:simpleType"); //$NON-NLS-1$
		}

		@Override
		public void setXMLCustomSimpleType(String xmlCustomSimpleType) {
			throw new UnsupportedOperationException("EClasses are not mapped to xsd:simpleType"); //$NON-NLS-1$
		}

		@Override
		public String getXMLXsdSimpleType() {
			throw new UnsupportedOperationException("EClasses are not mapped to xsd:simpleType"); //$NON-NLS-1$
		}

		@Override
		public void setXMLXsdSimpleType(String xmlXsdSimpleType) {
			throw new UnsupportedOperationException("EClasses are not mapped to xsd:simpleType"); //$NON-NLS-1$
		}

		@Override
		public String getXMLCustomSimpleTypeValuePattern() {
			throw new UnsupportedOperationException("EClasses are not mapped to xsd:simpleType"); //$NON-NLS-1$
		}

		@Override
		public void setXMLCustomSimpleTypeValuePattern(String xmlXsdPattern) {
			throw new UnsupportedOperationException("EClasses are not mapped to xsd:simpleType"); //$NON-NLS-1$
		}

		/**
		 * return first EStructuralFeature that fits to the XML element name TODO: add error handling for ambiguous
		 * features
		 */
		@Override
		public EStructuralFeature getFeatureByXMLElementName(String namespace, String xmlElementName) {
			// try to find the EStructural feature locally
			// TODO: consider namespace
			EStructuralFeature result = xmlNameToEStructuralFeatureMap.get(xmlElementName);
			if (null == result) {
				Iterator<EStructuralFeature> allFeaturesIter = ((EClass) eClassifier).getEAllStructuralFeatures().iterator();
				// TODO: we should iterate over features with no kind or
				List<EStructuralFeature> results = new ArrayList<EStructuralFeature>();
				EStructuralFeature possibleResult;

				while (allFeaturesIter.hasNext()) {
					EStructuralFeature feature = allFeaturesIter.next();
					possibleResult = null;
					String xmlWrapperName = getXMLPersistenceMappingExtendedMetaData(feature).getXMLWrapperName();

					// search by feature wrapper
					if (xmlWrapperName.equals(xmlElementName) && isIdentifiedByFeatureWrapper(feature)) {
						if (isIdentifiedByFeatureWrapper(feature)) {
							possibleResult = feature;
						} else {
							// not found, continue with next feature
						}
					}

					if (null == possibleResult) {
						// search by feature name
						String xmlName = getXMLPersistenceMappingExtendedMetaData(feature).getXMLName();
						if (xmlName.equals(xmlElementName)) {
							if (isIdentifiedByFeature(feature)) {
								possibleResult = feature;
							} else {
								// not found, continue with next feature
							}
						}
					}

					if (null == possibleResult) {
						// search by type wrapper (assuming type is type of feature)
						String classifierWrapperXMLName = getXMLPersistenceMappingExtendedMetaData(feature.getEType()).getXMLWrapperName();
						if (classifierWrapperXMLName.equals(xmlElementName)) {
							if (isIdentifiedByClassifierWrapper(feature)) {
								possibleResult = feature;
							} else {
								// not found, continue with next feature
							}
						} else {
							// search by type wrapper name (assuming type not type of feature)
							EClassifier classifier = getTypeByXMLWrapperName(namespace, xmlElementName);
							if (null != classifier) {
								if (feature.getEType().equals(classifier)) {
									if (isIdentifiedByClassifierWrapper(feature)) {
										possibleResult = feature;
									} else {
										// not found, continue with next feature
									}
								} else if (classifier instanceof EClass) {
									EClass eClass = (EClass) classifier;
									// check if the identified is a sub-type of the eType of the reference
									// note: EObject is not listed in EAllSuperTypes
									if (feature.getEType() == EcorePackage.eINSTANCE.getEObject()
											|| eClass.getEAllSuperTypes().contains(feature.getEType())) {
										if (isIdentifiedByClassifierWrapper(feature)) {
											possibleResult = feature;
										} else {
											// not found, continue with next feature
										}
									} else {
										// not found, continue with next feature
									}
								} else {
									// not found, continue with next feature
								}
							}
						}
					}
					if (null == possibleResult) {
						// search by type name (assuming type not type of feature)
						EClassifier classifier;
						EPackage ePackage = getPackage(namespace);
						if (null == ePackage) {
							// unregistered package
							classifier = demandType(namespace, xmlElementName);
						} else {
							// registered package
							classifier = getTypeByXMLName(namespace, xmlElementName);
						}

						if (null != classifier) {
							if (feature.getEType().equals(classifier)) {
								if (isIdentifiedByClassifier(feature)) {
									possibleResult = feature;
								} else if (isEReference_Contained0000(feature)) {
									possibleResult = feature;
								} else {
									// not found, continue with next feature
								}
							} else if (classifier instanceof EClass) {
								EClass eClass = (EClass) classifier;
								// check if the identified is a sub-type of the eType of the reference
								// note: EObject is not listed in EAllSuperTypes
								if (feature.getEType() == EcorePackage.eINSTANCE.getEObject()
										|| eClass.getEAllSuperTypes().contains(feature.getEType())) {
									if (isIdentifiedByClassifier(feature)) {
										possibleResult = feature;
									} else if (isEReference_Contained0000(feature)) {
										possibleResult = feature;
									} else {
										// not found, continue with next feature
									}
								} else if (isEReference_Contained0000(feature)) {
									possibleResult = feature;
								} else {
									// not found, continue with next feature
								}
							} else if (isEReference_Contained0000(feature)) {
								possibleResult = feature;
							} else {
								// not found, continue with next feature
							}
						} else if (isEReference_Contained0000(feature)) {
							possibleResult = feature;
						} else {
							// not found, continue with next feature
						}
					}

					if (null != possibleResult) {
						results.add(possibleResult);
					}
				} // while

				// if there are multiple valid features, we prefer the feature that is many and is not NONE
				int size = results.size();
				if (1 == size) {
					result = results.get(0);
				} else if (1 < size) {
					// rule 1 we like the features that are explicitly selected
					List<EStructuralFeature> identifiedFeatures = new ArrayList<EStructuralFeature>();
					List<EStructuralFeature> noneFeatures = new ArrayList<EStructuralFeature>();
					for (int i = 0; i < size; i++) {
						EStructuralFeature feature = results.get(i);
						if (isNone(feature)) {
							noneFeatures.add(feature);
						} else {
							identifiedFeatures.add(feature);
						}
					}

					if (identifiedFeatures.isEmpty()) {
						// there are none Features only
						results = noneFeatures;
					} else {
						results = identifiedFeatures;
					}

					result = results.get(0);

					// try to find a better features that is many
					for (EStructuralFeature feature : results) {
						if (feature.isMany()) {
							result = feature;
							break;
						}
					}
				}
				xmlNameToEStructuralFeatureMap.put(xmlElementName, result);
			} // if (null == result)

			return result;
		}
	}

	public static interface XMLPersistenceMappingEStructuralFeatureExtendedMetaData {
		String getXMLName();

		void setXMLName(String name);

		String getXMLWrapperName();

		void setXMLWrapperName(String name);

		String getXMLClassiferNameSuffix();

		void setXMLClassiferNameSuffix(String suffix);

		boolean isXMLAttribute();

		void setXMLAttribute(boolean isAttribute);

		boolean isXMLPersistenceMappingEnabled();

		int getXMLPersistenceMappingStrategy();

		void setXMLPersistenceMappingStrategy(int featureSerializationStructure);

		int getXMLTypeIdentificationStrategy();

		void setXMLTypeIdentificationStrategy(int typeIdentificationStrategy);

		int getXMLTypeDeclarationStrategy();

		void setXMLTypeDeclarationStrategy(int typeDeclarationStrategy);

		String getXMLTypeAttributeName();

		void setXMLTypeAttributeName(String typeAttributeName);
	}

	class XMLPersistenceMappingEStructuralFeatureExtendedMetaDataImpl implements XMLPersistenceMappingEStructuralFeatureExtendedMetaData {
		protected static final String UNINITIALIZED_STRING = "uninitialized"; //$NON-NLS-1$
		protected static final int UNINITIALIZED_INT = Integer.MIN_VALUE;

		protected EStructuralFeature eStructuralFeature;
		protected String xmlName = UNINITIALIZED_STRING;
		protected String xmlWrapperName = UNINITIALIZED_STRING;
		protected String xmlClassifierNameSuffix = UNINITIALIZED_STRING;
		protected Boolean xmlAttribute = UNINITIALIZED_BOOLEAN;
		protected int featureSerializationStructure = UNINITIALIZED_INT;
		protected Boolean xmlPersistenceMappingEnabled = null;
		protected int typeIdentificationStrategy = UNINITIALIZED_INT;
		protected int typeDeclarationStrategy = UNINITIALIZED_INT;
		protected String typeAttributeName = UNINITIALIZED_STRING;

		public XMLPersistenceMappingEStructuralFeatureExtendedMetaDataImpl(EStructuralFeature eStructuralFeature) {
			super();
			this.eStructuralFeature = eStructuralFeature;
		}

		@Override
		public String getXMLName() {
			if (UNINITIALIZED_STRING == xmlName) {
				setXMLName(basicGetName(eStructuralFeature));
			}
			return xmlName;
		}

		@Override
		public void setXMLName(String xmlName) {
			this.xmlName = xmlName;
		}

		@Override
		public String getXMLWrapperName() {
			if (UNINITIALIZED_STRING == xmlWrapperName) {
				setXMLWrapperName(basicGetWrapperName(eStructuralFeature));
			}
			return xmlWrapperName;
		}

		@Override
		public void setXMLWrapperName(String xmlWrapperName) {
			this.xmlWrapperName = xmlWrapperName;
		}

		@Override
		public String getXMLClassiferNameSuffix() {
			if (UNINITIALIZED_STRING == xmlClassifierNameSuffix) {
				setXMLClassiferNameSuffix(basicGetClassifierNameSuffix(eStructuralFeature));
			}
			return xmlClassifierNameSuffix;
		}

		@Override
		public boolean isXMLAttribute() {
			if (UNINITIALIZED_BOOLEAN == xmlAttribute) {
				setXMLAttribute(basicIsAttribute(eStructuralFeature));
			}
			return xmlAttribute;
		}

		@Override
		public void setXMLAttribute(boolean isAttribute) {
			xmlAttribute = isAttribute;
		}

		@Override
		public boolean isXMLPersistenceMappingEnabled() {
			if (null == xmlPersistenceMappingEnabled) {
				setXMLPersistenceMappingEnabled(basicIsXMLPersistenceMappingEnabled(eStructuralFeature));
			}
			return xmlPersistenceMappingEnabled;

		};

		void setXMLPersistenceMappingEnabled(Boolean xmlPersistenceMappingEnabled) {
			this.xmlPersistenceMappingEnabled = xmlPersistenceMappingEnabled;
		};

		@Override
		public void setXMLClassiferNameSuffix(String suffix) {
			xmlClassifierNameSuffix = suffix;
		}

		@Override
		public int getXMLPersistenceMappingStrategy() {
			if (UNINITIALIZED_INT == featureSerializationStructure) {
				setXMLPersistenceMappingStrategy(basicGetFeatureSerializationStructure(eStructuralFeature));
			}
			return featureSerializationStructure;
		}

		@Override
		public void setXMLPersistenceMappingStrategy(int featureSerializationStructure) {
			this.featureSerializationStructure = featureSerializationStructure;
		}

		@Override
		public int getXMLTypeIdentificationStrategy() {
			if (UNINITIALIZED_INT == typeIdentificationStrategy) {
				setXMLTypeIdentificationStrategy(basicGetTypeIdentificationStrategy(eStructuralFeature));
			}
			return typeIdentificationStrategy;
		}

		@Override
		public void setXMLTypeIdentificationStrategy(int typeIdentificationStrategy) {
			this.typeIdentificationStrategy = typeIdentificationStrategy;
		}

		@Override
		public int getXMLTypeDeclarationStrategy() {
			if (UNINITIALIZED_INT == typeDeclarationStrategy) {
				setXMLTypeDeclarationStrategy(basicGetTypeDeclarationStrategy(eStructuralFeature));
			}
			return typeDeclarationStrategy;
		}

		@Override
		public void setXMLTypeDeclarationStrategy(int typeDeclarationStrategy) {
			this.typeDeclarationStrategy = typeDeclarationStrategy;
		}

		@Override
		public String getXMLTypeAttributeName() {
			if (UNINITIALIZED_STRING == typeAttributeName) {
				setXMLTypeAttributeName(basicGetTypeAttributeName(eStructuralFeature));
			}
			return typeAttributeName;
		}

		@Override
		public void setXMLTypeAttributeName(String typeAttributeName) {
			this.typeAttributeName = typeAttributeName;
		}

	}

	public XMLPersistenceMappingExtendedMetaDataImpl() {
		this(EPackage.Registry.INSTANCE);
	}

	public XMLPersistenceMappingExtendedMetaDataImpl(EPackage.Registry registry) {
		super();
		this.registry = registry;
	}

	public XMLPersistenceMappingExtendedMetaDataImpl(int[] fallbackSerializations) {
		this();
		int min = 0;
		int max = fallbackSerializationConfiguration.length;
		for (int i = min; i < max && i < fallbackSerializations.length; i++) {
			int newValue = fallbackSerializations[i];
			if (min <= i && i < max) {
				fallbackSerializationConfiguration[i] = newValue;
			}
		}
	}

	@Override
	public boolean isXMLPersistenceMappingEnabled(EStructuralFeature feature) {
		return getXMLPersistenceMappingExtendedMetaData(feature).isXMLPersistenceMappingEnabled();
	}

	@Override
	public String getXMLName(EClassifier eClassifier) {
		return getXMLPersistenceMappingExtendedMetaData(eClassifier).getXMLName();
	}

	@Override
	public String getXMLName(EClassifier eClassifier, EStructuralFeature eStructuralFeature) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getXMLPersistenceMappingExtendedMetaData(eClassifier).getXMLName());
		buffer.append(getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).getXMLClassiferNameSuffix());
		return buffer.toString();
	}

	@Override
	public String getXMLName(EStructuralFeature eStructuralFeature) {
		return getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).getXMLName();
	}

	@Override
	public void setXMLName(EClassifier eClassifier, String xmlName) {
		// update annotations
		setName(eClassifier, xmlName);
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(eClassifier).setXMLName(xmlName);
		EPackage ePackage = eClassifier.getEPackage();
		if (ePackage != null) {
			getXMLPersistenceMappingExtendedMetaData(ePackage).renameToXMLName(eClassifier, xmlName);
		}
	}

	@Override
	public void setXMLName(EStructuralFeature eStructuralFeature, String xmlName) {
		// update annotations
		setName(eStructuralFeature, xmlName);
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).setXMLName(xmlName);
	}

	@Override
	public String getXMLWrapperName(EClassifier eClassifier) {
		return getXMLPersistenceMappingExtendedMetaData(eClassifier).getXMLWrapperName();
	}

	@Override
	public void setXMLWrapperName(EClassifier eClassifier, String xmlWrapperName) {
		// update annotations
		EcoreUtil.setAnnotation(eClassifier, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, WRAPPER_NAME, xmlWrapperName);
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(eClassifier).setXMLWrapperName(xmlWrapperName);
		EPackage ePackage = eClassifier.getEPackage();
		if (ePackage != null) {
			getXMLPersistenceMappingExtendedMetaData(ePackage).renameToXMLWrapperName(eClassifier, xmlWrapperName);
		}
	}

	@Override
	public String getXMLWrapperName(EStructuralFeature eStructuralFeature) {
		return getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).getXMLWrapperName();
	}

	@Override
	public void setXMLWrapperName(EStructuralFeature eStructuralFeature, String xmlWrapperName) {
		// update annotations
		EcoreUtil.setAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, WRAPPER_NAME, xmlWrapperName);
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).setXMLWrapperName(xmlWrapperName);
	}

	@Override
	public String getXMLClassifierNameSuffix(EStructuralFeature eStructuralFeature) {
		return getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).getXMLClassiferNameSuffix();
	}

	@Override
	public void setXMLClassifierNameSuffix(EStructuralFeature eStructuralFeature, String classifierNameSuffix) {
		// update annotations
		EcoreUtil.setAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, CLASSIFIER_NAME_SUFFIX, classifierNameSuffix);
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).setXMLClassiferNameSuffix(classifierNameSuffix);
	}

	@Override
	public boolean isXMLAttribute(EStructuralFeature eClaseStructuralFeature) {
		return getXMLPersistenceMappingExtendedMetaData(eClaseStructuralFeature).isXMLAttribute();
	}

	@Override
	public void setXMLAttribute(EStructuralFeature eClaseStructuralFeature, boolean isXMLAttribute) {
		// EAnnotation eAnnotation = getAnnotation(eClaseStructuralFeature, true);
		// eAnnotation.getDetails().put(XML_ATTRIBUTE, isXMLAttribute);
		// getXMLPersistenceMappingExtendedMetaData(eClaseStructuralFeature).setXMLAttribute(isXMLAttribute);
		// EClassifier eClassifier = eClaseStructuralFeature.getEType();
		// if (eClassifier != null) {
		// getXMLPersistenceMappingExtendedMetaData(eClassifier).renameToXMLAttribute(eClassifier, isXMLAttribute);
		// }

		// update annotations
		EcoreUtil.setAnnotation(eClaseStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, XML_ATTRIBUTE,
				Boolean.toString(isXMLAttribute));
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(eClaseStructuralFeature).setXMLAttribute(isXMLAttribute);

	}

	@Override
	public boolean isXMLGlobalElement(EClassifier eClassifier) {
		return getXMLPersistenceMappingExtendedMetaData(eClassifier).isXMLGlobalElement();
	}

	@Override
	public void setXMLGlobalElement(EClassifier eClassifier, boolean isXMLGlobalElement) {
		// update annotations
		EcoreUtil.setAnnotation(eClassifier, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, XML_GLOBAL_ELEMENT, Boolean.toString(isXMLGlobalElement));
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(eClassifier).setXMLGlobalElement(isXMLGlobalElement);
	}

	@Override
	public String getXMLCustomSimpleType(EClassifier eClassifier) {
		return getXMLPersistenceMappingExtendedMetaData(eClassifier).getXMLCustomSimpleType();
	}

	@Override
	public void setXMLCustomSimpleType(EClassifier eClassifier, String xmlCustomTypeName) {
		// update annotations
		EcoreUtil.setAnnotation(eClassifier, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, XML_CUSTOM_SIMPLE_TYPE, xmlCustomTypeName);
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(eClassifier).setXMLCustomSimpleType(xmlCustomTypeName);
	}

	@Override
	public String getXMLXsdSimpleType(EClassifier eClassifier) {
		return getXMLPersistenceMappingExtendedMetaData(eClassifier).getXMLXsdSimpleType();
	}

	@Override
	public void setXMLXsdSimpleType(EClassifier eClassifier, String xmlXsdSimpleType) {
		// update annotations
		EcoreUtil.setAnnotation(eClassifier, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, XML_XSD_SIMPLE_TYPE, xmlXsdSimpleType);
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(eClassifier).setXMLXsdSimpleType(xmlXsdSimpleType);
	}

	@Override
	public String getXMLCustomSimpleTypeValuePattern(EClassifier eClassifier) {
		return getXMLPersistenceMappingExtendedMetaData(eClassifier).getXMLCustomSimpleTypeValuePattern();
	}

	@Override
	public void setXMLCustomSimpleTypeValuePattern(EClassifier eClassifier, String xmlXsdPattern) {
		// update annotations
		EcoreUtil.setAnnotation(eClassifier, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, XML_CUSTOM_SIMPLE_TYPE_VALUE_PATTERN, xmlXsdPattern);
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(eClassifier).setXMLCustomSimpleTypeValuePattern(xmlXsdPattern);
	}

	@Override
	public int getXMLPersistenceMappingStrategy(EStructuralFeature eStructuralFeature) {
		return getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).getXMLPersistenceMappingStrategy();
	}

	@Override
	public void setXMLPersistenceMappingStrategy(EStructuralFeature eStructuralFeature, int persistenceMappingStrategy) {
		// update annotations
		EcoreUtil.setAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, FEATURE_WRAPPER_ELEMENT,
				Boolean.toString(FEATURE_WRAPPER_ELEMENT_MASK == (FEATURE_WRAPPER_ELEMENT_MASK & persistenceMappingStrategy)));
		EcoreUtil.setAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, FEATURE_ELEMENT,
				Boolean.toString(FEATURE_ELEMENT_MASK == (FEATURE_ELEMENT_MASK & persistenceMappingStrategy)));
		EcoreUtil.setAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, CLASSIFIER_WRAPPER_ELEMENT,
				Boolean.toString(CLASSIFIER_WRAPPER_ELEMENT_MASK == (CLASSIFIER_WRAPPER_ELEMENT_MASK & persistenceMappingStrategy)));
		EcoreUtil.setAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, CLASSIFIER_ELEMENT,
				Boolean.toString(CLASSIFIER_ELEMENT_MASK == (CLASSIFIER_ELEMENT_MASK & persistenceMappingStrategy)));

		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).setXMLPersistenceMappingStrategy(persistenceMappingStrategy);
	}

	@Override
	public EClassifier getTypeByXMLName(String namespace, String xmlName) {
		EPackage ePackage = getPackage(namespace);
		return ePackage == null ? null : getTypeByXMLName(ePackage, xmlName);
	}

	@Override
	public EClassifier getTypeByXMLName(String namespace, String xmlName, EStructuralFeature feature) {
		EPackage ePackage = getPackage(namespace);
		return ePackage == null ? null : getTypeByXMLName(ePackage, xmlName, feature);
	}

	@Override
	public EClassifier getTypeByXMLWrapperName(String namespace, String xmlWrapperName) {
		EPackage ePackage = getPackage(namespace);
		return ePackage == null ? null : getTypeByXMLWrapperName(ePackage, xmlWrapperName);
	}

	@Override
	public EClassifier getTypeByXMLName(EPackage ePackage, String xmlName) {
		return getXMLPersistenceMappingExtendedMetaData(ePackage).getType(xmlName);
	}

	public EClassifier getTypeByXMLName(EPackage ePackage, String xmlName, EStructuralFeature feature) {
		return getXMLPersistenceMappingExtendedMetaData(ePackage).getType(xmlName, feature);
	}

	@Override
	public EClassifier getTypeByXMLWrapperName(EPackage ePackage, String xmlWrapperName) {
		return getXMLPersistenceMappingExtendedMetaData(ePackage).getTypeByWrapperName(xmlWrapperName);
	}

	@Override
	public EPackage getPackage(String namespace) {
		EPackage ePackage = registry.getEPackage(namespace);
		return ePackage;
	}

	@Override
	public EStructuralFeature getFeatureByXMLElementName(EClass eClass, String namespace, String xmlElementName) {
		return getXMLPersistenceMappingExtendedMetaData(eClass).getFeatureByXMLElementName(namespace, xmlElementName);
	}

	@Override
	public void setXMLSchemaLocation(EPackage ePackage, String schemaLocation) {
		// update annotations
		EcoreUtil.setAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, SCHEMA_LOCATION, schemaLocation);
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(ePackage).setXMLSchemaLocation(schemaLocation);
	}

	@Override
	public String getXMLSchemaLocation(EPackage ePackage) {
		return getXMLPersistenceMappingExtendedMetaData(ePackage).getXMLSchemaLocation();
	}

	@Override
	public void setXMLExternalSchemaLocations(EPackage ePackage, Map<String, String> externalSchemaLocations) {
		// update annotations
		StringBuffer buffer = new StringBuffer();
		for (Entry<String, String> externalSchemaLocation : externalSchemaLocations.entrySet()) {
			buffer.append(externalSchemaLocation.getKey());
			buffer.append(" "); //$NON-NLS-1$
			buffer.append(externalSchemaLocation.getValue());
			buffer.append(" "); //$NON-NLS-1$
		}

		EcoreUtil.setAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, EXTERNAL_SCHEMA_LOCATIONS, buffer.toString().trim());
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(ePackage).setXMLExternalSchemaLocations(externalSchemaLocations);
	}

	@Override
	public Map<String, String> getXMLExternalSchemaLocations(EPackage ePackage) {
		return getXMLPersistenceMappingExtendedMetaData(ePackage).getXMLExternalSchemaLocations();
	}

	@Override
	public int getXMLTypeIdentificationStrategy(EStructuralFeature eStructuralFeature) {
		return getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).getXMLTypeIdentificationStrategy();
	}

	@Override
	public void setXMLTypeIdentificationStrategy(EStructuralFeature eStructuralFeature, int typeIdentificationStrategy) {
		// set annotation
		if (0 <= typeIdentificationStrategy && TYPE_IDENTIFICATION_STRATEGIES.length > typeIdentificationStrategy) {
			EcoreUtil.setAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, TYPE_IDENTIFICATION_STRATEGY,
					TYPE_IDENTIFICATION_STRATEGIES[typeIdentificationStrategy]);
		}
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).setXMLTypeIdentificationStrategy(typeIdentificationStrategy);
	}

	@Override
	public int getXMLTypeDeclarationStrategy(EStructuralFeature eStructuralFeature) {
		return getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).getXMLTypeDeclarationStrategy();
	}

	@Override
	public void setXMLTypeDeclarationStrategy(EStructuralFeature eStructuralFeature, int typeDeclarationStrategy) {
		// set annotation
		if (0 <= typeDeclarationStrategy && TYPE_DECLARATION_STRATEGIES.length > typeDeclarationStrategy) {
			EcoreUtil.setAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, TYPE_IDENTIFICATION_STRATEGY,
					TYPE_DECLARATION_STRATEGIES[typeDeclarationStrategy]);
		}

		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).setXMLTypeDeclarationStrategy(typeDeclarationStrategy);
	}

	@Override
	public String getXMLTypeAttributeName(EStructuralFeature eStructuralFeature) {
		return getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).getXMLTypeAttributeName();
	}

	@Override
	public void setXMLTypeAttributeName(EStructuralFeature eStructuralFeature, String typeAttributeName) {
		// set annotation
		EcoreUtil.setAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, TYPE_ATTRIBUTE_NAME, typeAttributeName);
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(eStructuralFeature).setXMLTypeAttributeName(typeAttributeName);
	}

	@Override
	public String getXMLDefaultEReferenceReferencedTypeAttributeName(EPackage ePackage) {
		return getXMLPersistenceMappingExtendedMetaData(ePackage).getXMLDefaultEReferenceReferencedTypeAttributeName();
	}

	@Override
	public void setXMLDefaultEReferenceReferencedTypeAttributeName(EPackage ePackage, String defaultEReferenceReferencedTypeAttributeName) {
		// set annotation
		EcoreUtil.setAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, DEFAULT_EREFERENCE_REFERENCED_TYPE_ATTRIBUTE_NAME,
				defaultEReferenceReferencedTypeAttributeName);
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(ePackage).setXMLDefaultEReferenceReferencedTypeAttributeName(
				defaultEReferenceReferencedTypeAttributeName);
	}

	@Override
	public int getXMLDefaultEReferenceReferencedTypeDeclarationStrategy(EPackage ePackage) {
		return getXMLPersistenceMappingExtendedMetaData(ePackage).getXMLDefaultEReferenceReferencedTypeDeclarationStrategy();
	}

	@Override
	public void setXMLDefaultEReferenceReferencedTypeDeclarationStrategy(EPackage ePackage, int defaultEReferenceReferencedTypeDeclarationStrategy) {
		// set annotation
		if (0 <= defaultEReferenceReferencedTypeDeclarationStrategy
				&& TYPE_DECLARATION_STRATEGIES.length > defaultEReferenceReferencedTypeDeclarationStrategy) {
			EcoreUtil.setAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, DEFAULT_EREFERENCE_REFERENCED_TYPE_DECLARATION_STRATEGY,
					TYPE_DECLARATION_STRATEGIES[defaultEReferenceReferencedTypeDeclarationStrategy]);
		}
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(ePackage).setXMLDefaultEReferenceReferencedTypeDeclarationStrategy(
				defaultEReferenceReferencedTypeDeclarationStrategy);
	}

	@Override
	public int getXMLDefaultEReferenceReferencedTypeIdentificationStrategy(EPackage ePackage) {
		return getXMLPersistenceMappingExtendedMetaData(ePackage).getXMLDefaultEReferenceReferencedTypeIdentificationStrategy();
	}

	@Override
	public void setXMLDefaultEReferenceReferencedTypeIdentificationStrategy(EPackage ePackage,
			int defaultEReferenceReferencedTypeIdentificationStrategy) {
		// set annotation
		if (0 <= defaultEReferenceReferencedTypeIdentificationStrategy
				&& TYPE_IDENTIFICATION_STRATEGIES.length > defaultEReferenceReferencedTypeIdentificationStrategy) {
			EcoreUtil.setAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
					DEFAULT_EREFERENCE_REFERENCED_TYPE_IDENTIFICATION_STRATEGY,
					TYPE_DECLARATION_STRATEGIES[defaultEReferenceReferencedTypeIdentificationStrategy]);
		}
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(ePackage).setXMLDefaultEReferenceReferencedTypeDeclarationStrategy(
				defaultEReferenceReferencedTypeIdentificationStrategy);
	}

	@Override
	public String getXMLDefaultEReferenceContainedTypeAttributeName(EPackage ePackage) {
		return getXMLPersistenceMappingExtendedMetaData(ePackage).getXMLDefaultEReferenceContainedTypeAttributeName();
	}

	@Override
	public void setXMLDefaultEReferenceContainedTypeAttributeName(EPackage ePackage, String defaultEReferenceContainedTypeAttributeName) {
		// set annotation
		EcoreUtil.setAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, DEFAULT_EREFERENCE_CONTAINED_TYPE_ATTRIBUTE_NAME,
				defaultEReferenceContainedTypeAttributeName);
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(ePackage).setXMLDefaultEReferenceContainedTypeAttributeName(
				defaultEReferenceContainedTypeAttributeName);
	}

	@Override
	public int getXMLDefaultEReferenceContainedTypeDeclarationStrategy(EPackage ePackage) {
		return getXMLPersistenceMappingExtendedMetaData(ePackage).getXMLDefaultEReferenceContainedTypeDeclarationStrategy();
	}

	@Override
	public void setXMLDefaultEReferenceContainedTypeDeclarationStrategy(EPackage ePackage, int defaultEReferenceContainedTypeDeclarationStrategy) {
		// set annotation
		if (0 <= defaultEReferenceContainedTypeDeclarationStrategy
				&& TYPE_DECLARATION_STRATEGIES.length > defaultEReferenceContainedTypeDeclarationStrategy) {
			EcoreUtil.setAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, DEFAULT_EREFERENCE_CONTAINED_TYPE_DECLARATION_STRATEGY,
					TYPE_DECLARATION_STRATEGIES[defaultEReferenceContainedTypeDeclarationStrategy]);
		}
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(ePackage).setXMLDefaultEReferenceContainedTypeDeclarationStrategy(
				defaultEReferenceContainedTypeDeclarationStrategy);
	}

	@Override
	public int getXMLDefaultEReferenceContainedTypeIdentificationStrategy(EPackage ePackage) {
		return getXMLPersistenceMappingExtendedMetaData(ePackage).getXMLDefaultEReferenceContainedTypeIdentificationStrategy();
	}

	@Override
	public void setXMLDefaultEReferenceContainedTypeIdentificationStrategy(EPackage ePackage, int defaultEReferenceContainedTypeIdentificationStrategy) {
		// set annotation
		if (0 <= defaultEReferenceContainedTypeIdentificationStrategy
				&& TYPE_IDENTIFICATION_STRATEGIES.length > defaultEReferenceContainedTypeIdentificationStrategy) {
			EcoreUtil.setAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
					DEFAULT_EREFERENCE_CONTAINED_TYPE_IDENTIFICATION_STRATEGY,
					TYPE_DECLARATION_STRATEGIES[defaultEReferenceContainedTypeIdentificationStrategy]);
		}
		// update metadata cache
		getXMLPersistenceMappingExtendedMetaData(ePackage).setXMLDefaultEReferenceContainedTypeDeclarationStrategy(
				defaultEReferenceContainedTypeIdentificationStrategy);
	}

	protected boolean basicIsXMLPersistenceMappingEnabled(EStructuralFeature eStructuralFeature) {
		return null != eStructuralFeature.getEAnnotation(XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI);
	}

	protected String basicGetXMLCustomSimpleType(EClassifier eClassifier) {
		return EcoreUtil.getAnnotation(eClassifier, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, XML_CUSTOM_SIMPLE_TYPE);
	}

	protected String basicGetXMLCustomSimpleTypeValuePattern(EClassifier eClassifier) {
		return EcoreUtil.getAnnotation(eClassifier, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, XML_CUSTOM_SIMPLE_TYPE_VALUE_PATTERN);
	}

	protected String basicGetXMLXsdSimpleType(EClassifier eClassifier) {
		return EcoreUtil.getAnnotation(eClassifier, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, XML_XSD_SIMPLE_TYPE);
	}

	protected boolean basicIsAttribute(EStructuralFeature eStructuralFeature) {
		return Boolean.parseBoolean(EcoreUtil.getAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, XML_ATTRIBUTE));
	}

	protected boolean basicIsGlobalElement(EClassifier eClassifier) {
		return Boolean.parseBoolean(EcoreUtil.getAnnotation(eClassifier, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, XML_GLOBAL_ELEMENT));
	}

	protected String basicGetWrapperName(EClassifier eClassifier) {
		String result = EcoreUtil.getAnnotation(eClassifier, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, WRAPPER_NAME);
		if (null == result) {
			result = getXMLName(eClassifier) + PLURAL_EXTENSION;
		}
		return result;
	}

	protected String basicGetWrapperName(EStructuralFeature eStructuralFeature) {
		String result = EcoreUtil.getAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, WRAPPER_NAME);
		if (null == result) {
			result = getXMLName(eStructuralFeature) + PLURAL_EXTENSION;
		}
		return result;
	}

	/**
	 * default is ""
	 *
	 * @param eStructuralFeature
	 * @return
	 */
	protected String basicGetClassifierNameSuffix(EStructuralFeature eStructuralFeature) {
		String result = EcoreUtil.getAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, CLASSIFIER_NAME_SUFFIX);
		if (null == result) {
			result = ""; //$NON-NLS-1$
		}
		return result;
	}

	protected int basicGetTypeIdentificationStrategy(EStructuralFeature eStructuralFeature) {
		String result = EcoreUtil.getAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, TYPE_IDENTIFICATION_STRATEGY);
		if (null != result) {
			for (int i = 1; i < TYPE_IDENTIFICATION_STRATEGIES.length; ++i) {
				if (TYPE_IDENTIFICATION_STRATEGIES[i].equals(result)) {
					return i;
				}
			}
		}

		// get default from containing EPacakge
		EClass eClass = eStructuralFeature.getEContainingClass();
		if (null != eClass) {
			EPackage ePackage = eClass.getEPackage();
			if (null != ePackage) {
				return getXMLPersistenceMappingExtendedMetaData(ePackage).getXMLDefaultEReferenceReferencedTypeIdentificationStrategy();
			}
		}

		// default
		return TYPE_IDENTIFICATION_UNSPECIFIED;
	}

	protected int basicGetTypeDeclarationStrategy(EStructuralFeature eStructuralFeature) {
		String result = EcoreUtil.getAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, TYPE_DECLARATION_STRATEGY);
		if (null != result) {
			for (int i = 1; i < TYPE_DECLARATION_STRATEGIES.length; ++i) {
				if (TYPE_DECLARATION_STRATEGIES[i].equals(result)) {
					return i;
				}
			}
		}

		// get default from containing EPacakge
		EClass eClass = eStructuralFeature.getEContainingClass();
		if (null != eClass) {
			EPackage ePackage = eClass.getEPackage();
			if (null != ePackage) {
				return getXMLPersistenceMappingExtendedMetaData(ePackage).getXMLDefaultEReferenceReferencedTypeDeclarationStrategy();
			}
		}

		// default
		return TYPE_DECLARATION_UNSPECIFIED;
	}

	protected String basicGetTypeAttributeName(EStructuralFeature eStructuralFeature) {
		// get value from direct annotation
		String result = EcoreUtil.getAnnotation(eStructuralFeature, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, TYPE_ATTRIBUTE_NAME);
		if (null == result) {
			// get value from containing EPackage
			EClass eClass = eStructuralFeature.getEContainingClass();
			if (null != eClass) {
				EPackage ePackage = eClass.getEPackage();
				if (null != ePackage) {
					if (eStructuralFeature instanceof EReference && !((EReference) eStructuralFeature).isContainment()) {
						result = getXMLPersistenceMappingExtendedMetaData(ePackage).getXMLDefaultEReferenceReferencedTypeAttributeName();
					} else {
						result = getXMLPersistenceMappingExtendedMetaData(ePackage).getXMLDefaultEReferenceContainedTypeAttributeName();
					}
				}
			}
		}
		if (null == result) {
			// default value, if EStructural Feature is not contained in an EPackage
			result = "xsi:type"; //$NON-NLS-1$
		}
		return result;
	}

	protected String basicGetDefaultEReferenceReferencedTypeAttributeName(EPackage ePackage) {
		String result = EcoreUtil.getAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
				DEFAULT_EREFERENCE_REFERENCED_TYPE_ATTRIBUTE_NAME);
		if (null == result) {
			result = "TYPE"; //$NON-NLS-1$
		}
		return result;
	}

	protected int basicGetDefaultEReferenceReferencedTypeDeclarationStrategy(EPackage ePackage) {
		String result = EcoreUtil.getAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
				DEFAULT_EREFERENCE_REFERENCED_TYPE_DECLARATION_STRATEGY);
		if (null != result) {
			for (int i = 1; i < TYPE_DECLARATION_STRATEGIES.length; ++i) {
				if (TYPE_DECLARATION_STRATEGIES[i].equals(result)) {
					return i;
				}
			}
		}
		return TYPE_DECLARATION_IF_NEEDED;
	}

	protected int basicGetDefaultEReferenceReferencedTypeIdentificationStrategy(EPackage ePackage) {
		String result = EcoreUtil.getAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
				DEFAULT_EREFERENCE_REFERENCED_TYPE_IDENTIFICATION_STRATEGY);
		if (null != result) {
			for (int i = 1; i < TYPE_IDENTIFICATION_STRATEGIES.length; ++i) {
				if (TYPE_IDENTIFICATION_STRATEGIES[i].equals(result)) {
					return i;
				}
			}
		}
		return TYPE_IDENTIFICATION_URI_ONLY;
	}

	protected String basicGetDefaultEReferenceContainedTypeAttributeName(EPackage ePackage) {
		String result = EcoreUtil.getAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
				DEFAULT_EREFERENCE_CONTAINED_TYPE_ATTRIBUTE_NAME);
		if (null == result) {
			result = "xsi:type"; //$NON-NLS-1$
		}
		return result;
	}

	protected int basicGetDefaultEReferenceContainedTypeDeclarationStrategy(EPackage ePackage) {
		String result = EcoreUtil.getAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
				DEFAULT_EREFERENCE_CONTAINED_TYPE_DECLARATION_STRATEGY);
		if (null != result) {
			for (int i = 1; i < TYPE_DECLARATION_STRATEGIES.length; ++i) {
				if (TYPE_DECLARATION_STRATEGIES[i].equals(result)) {
					return i;
				}
			}
		}
		return TYPE_DECLARATION_IF_NEEDED;
	}

	protected int basicGetDefaultEReferenceContainedTypeIdentificationStrategy(EPackage ePackage) {
		String result = EcoreUtil.getAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
				DEFAULT_EREFERENCE_CONTAINED_TYPE_IDENTIFICATION_STRATEGY);
		if (null != result) {
			for (int i = 1; i < TYPE_IDENTIFICATION_STRATEGIES.length; ++i) {
				if (TYPE_IDENTIFICATION_STRATEGIES[i].equals(result)) {
					return i;
				}
			}
		}
		return TYPE_IDENTIFICATION_URI_ONLY;
	}

	/**
	 * @param eStructuralFeature
	 * @return #SERILIZATION_STRUCTURE__UNDDEFINED, if no annotation is defined
	 */
	protected int basicGetFeatureSerializationStructure(EStructuralFeature eStructuralFeature) {
		EAnnotation eAnnotation = eStructuralFeature.getEAnnotation(XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI);
		if (eAnnotation != null) {
			String featureWrapperElement = eAnnotation.getDetails().get(FEATURE_WRAPPER_ELEMENT);
			String featureElement = eAnnotation.getDetails().get(FEATURE_ELEMENT);
			String classifierWrapperElement = eAnnotation.getDetails().get(CLASSIFIER_WRAPPER_ELEMENT);
			String classifierElement = eAnnotation.getDetails().get(CLASSIFIER_ELEMENT);

			int result = 0;
			if (null == featureWrapperElement || Boolean.parseBoolean(featureWrapperElement)) {
				// if not explicitly set to false, the feature wrapper element is created
				result += FEATURE_WRAPPER_ELEMENT_MASK;
			}

			if (Boolean.parseBoolean(featureElement)) {
				// if explicitly set to true, the feature element is created
				result += FEATURE_ELEMENT_MASK;
			}

			if (Boolean.parseBoolean(classifierWrapperElement)) {
				// if explicitly set to true, the classifier wrapper element is created
				result += CLASSIFIER_WRAPPER_ELEMENT_MASK;
			}

			if (null == classifierWrapperElement || Boolean.parseBoolean(classifierElement)) {
				// if not explicitly set to false, the classifier element is created
				result += CLASSIFIER_ELEMENT_MASK;
			}

			return fallbackSerializationConfiguration[result];

		} else {
			// default to standard EMF serialization
			return XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT;
		}
	}

	protected String basicGetXMLSchemaLocation(EPackage ePackage) {
		return EcoreUtil.getAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI, SCHEMA_LOCATION);
	}

	protected Map<String, String> basicGetXMLExternalSchemaLocations(EPackage ePackage) {
		String externalSchemaLocationsString = EcoreUtil.getAnnotation(ePackage, XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
				EXTERNAL_SCHEMA_LOCATIONS);
		Map<String, String> externalSchemaLocations = new HashMap<String, String>();
		if (null != externalSchemaLocationsString) {
			String[] externalSchemaLocationsArray = externalSchemaLocationsString.trim().split(" "); //$NON-NLS-1$
			for (int i = 1; i < externalSchemaLocationsArray.length; i = i + 2) {
				externalSchemaLocations.put(externalSchemaLocationsArray[i - 1], externalSchemaLocationsArray[i]);
			}
		}
		return externalSchemaLocations;
	}

	protected XMLPersistenceMappingEStructuralFeatureExtendedMetaData getXMLPersistenceMappingExtendedMetaData(EStructuralFeature eStructuralFeature) {
		XMLPersistenceMappingEStructuralFeatureExtendedMetaData result = (XMLPersistenceMappingEStructuralFeatureExtendedMetaData) extendedMetaDataCache
				.get(eStructuralFeature);
		if (result == null) {
			extendedMetaDataCache.put(eStructuralFeature, result = createXMLPersistenceMappingEStructuralFeatureExtendedMetaData(eStructuralFeature));
		}
		return result;
	}

	protected XMLPersistenceMappingEClassifierExtendedMetaData getXMLPersistenceMappingExtendedMetaData(EClassifier eClassifier) {
		XMLPersistenceMappingEClassifierExtendedMetaData result = (XMLPersistenceMappingEClassifierExtendedMetaData) extendedMetaDataCache
				.get(eClassifier);
		if (result == null) {
			extendedMetaDataCache.put(eClassifier, result = createXMLPersistenceMappingEClassifierExtendedMetaData(eClassifier));
		}
		return result;
	}

	protected XMLPersistenceMappingEPackageExtendedMetaData getXMLPersistenceMappingExtendedMetaData(EPackage ePackage) {
		XMLPersistenceMappingEPackageExtendedMetaData result = (XMLPersistenceMappingEPackageExtendedMetaData) extendedMetaDataCache.get(ePackage);
		if (result == null) {
			extendedMetaDataCache.put(ePackage, result = createXMLPersistenceMappingEPackageExtendedMetaData(ePackage));
		}
		return result;
	}

	protected XMLPersistenceMappingEStructuralFeatureExtendedMetaData createXMLPersistenceMappingEStructuralFeatureExtendedMetaData(
			EStructuralFeature eStructuralFeature) {
		return new XMLPersistenceMappingEStructuralFeatureExtendedMetaDataImpl(eStructuralFeature);
	}

	protected XMLPersistenceMappingEClassifierExtendedMetaData createXMLPersistenceMappingEClassifierExtendedMetaData(EClassifier eClassifier) {
		if (eClassifier instanceof EClass) {
			return new XMLPersistenceMappingEClassExtendedMetaDataImpl(eClassifier);
		} else {
			return new XMLPersistenceMappingEDataTypeExtendedMetaDataImpl(eClassifier);
		}
	}

	protected XMLPersistenceMappingEPackageExtendedMetaData createXMLPersistenceMappingEPackageExtendedMetaData(EPackage ePackage) {
		return new XMLPersistenceMappingEPackageExtendedMetaDataImpl(ePackage);
	}

	protected boolean isIdentifiedByFeatureWrapper(EStructuralFeature feature) {
		int featureSerializationStructure = getXMLPersistenceMappingExtendedMetaData(feature).getXMLPersistenceMappingStrategy();
		return FEATURE_WRAPPER_ELEMENT_MASK == (featureSerializationStructure & FEATURE_WRAPPER_ELEMENT_MASK);
	}

	protected boolean isIdentifiedByFeature(EStructuralFeature feature) {
		int featureSerializationStructure = getXMLPersistenceMappingExtendedMetaData(feature).getXMLPersistenceMappingStrategy();
		return FEATURE_ELEMENT_MASK == (featureSerializationStructure & (FEATURE_WRAPPER_ELEMENT_MASK | FEATURE_ELEMENT_MASK));
	}

	protected boolean isIdentifiedByClassifierWrapper(EStructuralFeature feature) {
		int featureSerializationStructure = getXMLPersistenceMappingExtendedMetaData(feature).getXMLPersistenceMappingStrategy();
		return CLASSIFIER_WRAPPER_ELEMENT_MASK == (featureSerializationStructure & (FEATURE_WRAPPER_ELEMENT_MASK | FEATURE_ELEMENT_MASK | CLASSIFIER_WRAPPER_ELEMENT_MASK));
	}

	protected boolean isIdentifiedByClassifier(EStructuralFeature feature) {
		int featureSerializationStructure = getXMLPersistenceMappingExtendedMetaData(feature).getXMLPersistenceMappingStrategy();
		return CLASSIFIER_ELEMENT_MASK == featureSerializationStructure;
	}

	protected boolean isNone(EStructuralFeature feature) {
		int featureSerializationStructure = getXMLPersistenceMappingExtendedMetaData(feature).getXMLPersistenceMappingStrategy();
		return 0 == featureSerializationStructure;
	}

	protected boolean isEReference_Contained0000(EStructuralFeature feature) {
		boolean isEReference_Contained0000;
		if (feature instanceof EReference) {
			EReference reference = (EReference) feature;
			if (reference.isContainment()) {
				isEReference_Contained0000 = isNone(feature);
			} else {
				isEReference_Contained0000 = false;
			}
		} else {
			isEReference_Contained0000 = false;
		}
		return isEReference_Contained0000;
	}

}
