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

import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.ExtendedMetaData;

public interface XMLPersistenceMappingExtendedMetaData extends ExtendedMetaData {

	/**
	 * The URI used as the annotation source:
	 * "http:///org/eclipse/sphinx/emf/serialization/XMLPersistenceMappingExtendedMetaData".
	 */
	static public final String XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI = "http:///org/eclipse/sphinx/emf/serialization/XMLPersistenceMappingExtendedMetaData"; //$NON-NLS-1$

	static public final String NAME = "name"; //$NON-NLS-1$
	static public final String WRAPPER_NAME = "wrapperName"; //$NON-NLS-1$
	static public final String CLASSIFIER_NAME_SUFFIX = "classifierNameSuffix"; //$NON-NLS-1$

	static public final String FEATURE_WRAPPER_ELEMENT = "featureWrapperElement"; //$NON-NLS-1$
	static public final String FEATURE_ELEMENT = "featureElement"; //$NON-NLS-1$
	static public final String CLASSIFIER_WRAPPER_ELEMENT = "classifierWrapperElement"; //$NON-NLS-1$
	static public final String CLASSIFIER_ELEMENT = "classifierElement"; //$NON-NLS-1$

	public static final String XML_GLOBAL_ELEMENT = "xmlGlobalElement"; //$NON-NLS-1$
	public static final String SCHEMA_LOCATION = "schemaLocation"; //$NON-NLS-1$
	public static final String EXTERNAL_SCHEMA_LOCATIONS = "externalSchemaLocations"; //$NON-NLS-1$

	public static final String TYPE_IDENTIFICATION_STRATEGY = "typeIdentificationStrategy"; //$NON-NLS-1$

	public static final String TYPE_DECLARATION_STRATEGY = "typeDeclarationStrategy"; //$NON-NLS-1$

	public static final String TYPE_ATTRIBUTE_NAME = "typeAttributeName"; //$NON-NLS-1$

	public static final String DEFAULT_EREFERENCE_REFERENCED_TYPE_ATTRIBUTE_NAME = "defaultEReferenceReferencedTypeAttributeName"; //$NON-NLS-1$
	public static final String DEFAULT_EREFERENCE_REFERENCED_TYPE_DECLARATION_STRATEGY = "defaultEReferenceReferencedTypeDeclarationStrategy"; //$NON-NLS-1$
	public static final String DEFAULT_EREFERENCE_REFERENCED_TYPE_IDENTIFICATION_STRATEGY = "defaultEReferenceReferencedTypeIdentificationStrategy"; //$NON-NLS-1$

	public static final String DEFAULT_EREFERENCE_CONTAINED_TYPE_ATTRIBUTE_NAME = "defaultEReferenceContainedTypeAttributeName"; //$NON-NLS-1$
	public static final String DEFAULT_EREFERENCE_CONTAINED_TYPE_DECLARATION_STRATEGY = "defaultEReferenceContainedTypeDeclarationStrategy"; //$NON-NLS-1$
	public static final String DEFAULT_EREFERENCE_CONTAINED_TYPE_IDENTIFICATION_STRATEGY = "defaultEReferenceContainedTypeIdentificationStrategy"; //$NON-NLS-1$

	/**
	 * Returns the XML name for a classifier. This is the name is used if a classifier XML element is configured.
	 * <p>
	 * details key: "name"
	 */
	String getXMLName(EClassifier eClassifier);

	/**
	 * Returns the XML name for a classifier in a context of a given feature. This name is used if a classifier XML
	 * element is configured.
	 * <p>
	 * details key: Classifier "name" <br/>
	 * details key: EStructuralFeature "classifierNameSuffix"
	 */
	String getXMLName(EClassifier classifier, EStructuralFeature eStructuralFeature);

	/**
	 * Sets the XML name for a classifier. This is the name is used if a classifier XML element is configured.
	 * <p>
	 * details key: "xmlName"
	 */
	void setXMLName(EClassifier eClassifier, String xmlName);

	/**
	 * Returns the XML wrapper name for a classifier. This is the name is used if a classifier wrapper XML element is
	 * configured.
	 * <p>
	 * details key: "wrapperName"
	 */
	String getXMLWrapperName(EClassifier eClassifier);

	/**
	 * Sets the XML wrapper name for a classifier. This is the name is used if a classifier wrapper XML element is
	 * configured.
	 * <p>
	 * details key: "wrapperName"
	 */
	void setXMLWrapperName(EClassifier eClassifier, String xmlName);

	/**
	 * Returns the XML name for a structural feature. This is the name is used if a feature XML element is configured.
	 * <p>
	 * details key: "name"
	 */
	String getXMLName(EStructuralFeature eStructuralFeature);

	/**
	 * Set the XML name for a structural feature. This is the name is used if a feature XML element is configured.
	 * <p>
	 * details key: "name"
	 */
	void setXMLName(EStructuralFeature eStructuralFeature, String xmlName);

	/**
	 * Returns the XML name for a structural feature. This name is used if a feature wrapper XML element is configured.
	 * <p>
	 * details key: "wrapperName"
	 */
	String getXMLWrapperName(EStructuralFeature eStructuralFeature);

	/**
	 * Set the XML name for a structural feature. This name is used if a feature wrapper XML element is configured.
	 * <p>
	 * details key: "wrapperName"
	 */
	void setXMLWrapperName(EStructuralFeature eStructuralFeature, String xmlName);

	/**
	 * Returns the XML classifierNameSuffix for a structural feature. This suffix is appended to the classifier name if
	 * a classifier element is created in the context of this EStructuralFeature.
	 * <p>
	 * details key: "classifierNameSuffix"
	 */
	String getXMLClassifierNameSuffix(EStructuralFeature eStructuralFeature);

	/**
	 * Set the XML classifierNameSuffix for a structural feature. This suffix is appended to the classifier name if a
	 * classifier element is created in the context of this EStructuralFeature. configured.
	 * <p>
	 * details key: "classifierNameSuffix"
	 */
	void setXMLClassifierNameSuffix(EStructuralFeature eStructuralFeature, String xmlName);

	/**
	 * Returns the classifier with the given XML name within the package with the given namespace.
	 */
	EClassifier getTypeByXMLName(String namespace, String xmlName);

	/**
	 * Returns the classifier with the given XML name within the package with the given namespace.
	 */
	EClassifier getTypeByXMLName(String namespace, String xmlName, EStructuralFeature feature);

	/**
	 * Returns the classifier with the given XML wrapper name within the package with the given namespace.
	 */
	EClassifier getTypeByXMLWrapperName(String namespace, String xmlWrapperName);

	/**
	 * Returns the classifier with the given XML name within the given package.
	 */
	EClassifier getTypeByXMLName(EPackage ePackage, String xmlName);

	/**
	 * Returns the classifier with the given XML name within the given package.
	 */
	EClassifier getTypeByXMLWrapperName(EPackage ePackage, String xmlWrapperName);

	/**
	 * Retrieves the package with the specified namespace URI from the package registry associated with this instance.
	 */
	@Override
	EPackage getPackage(String namespace);

	boolean isXMLPersistenceMappingEnabled(EStructuralFeature feature);

	/**
	 * Returns a structural feature within a class, corresponding to a local attribute with the given namespace and
	 * name, or, failing that, a document root feature corresponding to a global attribute with the given namespace and
	 * name that is {@link #getAffiliation(EClass, EStructuralFeature) affiliated} with a feature in the class.
	 */
	@Override
	EStructuralFeature getAttribute(EClass eClass, String namespace, String name);

	/**
	 * Returns a structural feature within a class, corresponding to a local element with the given namespace and name,
	 * or, failing that, a document root feature corresponding to a global element with the given namespace and name
	 * that is {@link #getAffiliation(EClass, EStructuralFeature) affiliated} with a feature in the class.
	 */
	EStructuralFeature getFeatureByXMLElementName(EClass eClass, String namespace, String name);

	int SERIALIZATION_STRUCTURE__UNDEFINED = -1;

	/**
	 * The feature serialization structure ID for a feature that is serialized using the following XML element
	 * structure:
	 *
	 * <pre>
	 * &lt;Parent&gt;
	 *   ...
	 * &lt;/Parent&gt;
	 * </pre>
	 *
	 * details key: "featureWrapperElement=false" <br/>
	 * details key: "featureElement=false" <br/>
	 * details key: "classifierWrapperElement=false"<br/>
	 * details key: "classifierElement=false" <br/>
	 *
	 * @see #getFeatureSerializationStructure
	 * @see #setXMLPersistenceMappingStrategy
	 */
	int XML_PERSISTENCE_MAPPING_STRATEGY__0000__NONE = 0;

	/**
	 * The feature serialization structure ID for a feature that is serialized using the following XML element
	 * structure:
	 *
	 * <pre>
	 * &lt;Parent&gt;
	 *   	(&lt;ClassifierName&gt; ... &lt;/ClassifierName&gt;)?
	 * &lt;/Parent&gt;
	 * </pre>
	 *
	 * details key: "featureWrapperElement=false" <br/>
	 * details key: "featureElement=false" <br/>
	 * details key: "classifierWrapperElement=false"<br/>
	 * details key: "classifierElement=true" <br/>
	 *
	 * @see #getFeatureSerializationStructure
	 * @see #setXMLPersistenceMappingStrategy
	 */
	int XML_PERSISTENCE_MAPPING_STRATEGY__0001__CLASSIFIER_ELEMENT = 1;

	/**
	 * The feature serialization structure ID for a feature that is serialized using the following XML element
	 * structure:
	 *
	 * <pre>
	 * &lt;Parent&gt;
	 *    &lt;ClassifierWrapperName&gt;
	 *   	( ... )?
	 *    &lt;/ClassifierWrapperName&gt;
	 * &lt;/Parent&gt;
	 * </pre>
	 *
	 * details key: "featureWrapperElement=false" <br/>
	 * details key: "featureElement=false" <br/>
	 * details key: "classifierWrapperElement=true"<br/>
	 * details key: "classifierElement=false" <br/>
	 *
	 * @see #getFeatureSerializationStructure
	 * @see #setXMLPersistenceMappingStrategy
	 */
	int XML_PERSISTENCE_MAPPING_STRATEGY__0010__CLASSIFIER_WRAPPER_ELEMENT = 2;

	/**
	 * The feature serialization structure ID for a feature that is serialized using the following XML element
	 * structure:
	 *
	 * <pre>
	 * &lt;Parent&gt;
	 *    &lt;ClassifierWrapperName&gt;
	 *   	(&lt;ClassifierName&gt; ... &lt;/ClassifierName&gt;)?
	 *    &lt;/ClassifierWrapperName&gt;
	 * &lt;/Parent&gt;
	 * </pre>
	 *
	 * details key: "featureWrapperElement=false" <br/>
	 * details key: "featureElement=false" <br/>
	 * details key: "classifierWrapperElement=true"<br/>
	 * details key: "classifierElement=true" <br/>
	 *
	 * @see #getFeatureSerializationStructure
	 * @see #setXMLPersistenceMappingStrategy
	 */
	int XML_PERSISTENCE_MAPPING_STRATEGY__0011__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT = 3;

	/**
	 * The feature serialization structure ID for a feature that is serialized using the following XML element
	 * structure:
	 *
	 * <pre>
	 * &lt;Parent&gt;
	 *    (&lt;FeatureName&gt;
	 *   	...
	 *    &lt;/FeatureName&gt;)?
	 * &lt;/Parent&gt;
	 * </pre>
	 *
	 * details key: "featureWrapperElement=false" <br/>
	 * details key: "featureElement=true" <br/>
	 * details key: "classifierWrapperElement=false"<br/>
	 * details key: "classifierElement=false" <br/>
	 *
	 * @see #getFeatureSerializationStructure
	 * @see #setXMLPersistenceMappingStrategy
	 */
	int XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT = 4;

	/**
	 * The feature serialization structure ID for a feature that is serialized using the following XML element
	 * structure:
	 *
	 * <pre>
	 * &lt;Parent&gt;
	 *   (&lt;FeatureName&gt;
	 *   	&lt;ClassifierName&gt; ... &lt;/ClassifierName&gt;
	 *   &lt;/FeatureName&gt;)?
	 * &lt;/Parent&gt;
	 * </pre>
	 *
	 * details key: "featureWrapperElement=false" <br/>
	 * details key: "featureElement=true" <br/>
	 * details key: "classifierWrapperElement=false"<br/>
	 * details key: "classifierElement=true" <br/>
	 *
	 * @see #getFeatureSerializationStructure
	 * @see #setXMLPersistenceMappingStrategy
	 */
	int XML_PERSISTENCE_MAPPING_STRATEGY__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT = 5;

	/**
	 * The feature serialization structure ID for a feature that is serialized using the following XML element
	 * structure:
	 *
	 * <pre>
	 * &lt;Parent&gt;
	 *   (&lt;FeatureName&gt;
	 *   	&lt;ClassifierWrapperName&gt; ... &lt;/ClassifierWrapperName&gt;
	 *   &lt;/FeatureName&gt;)?
	 * &lt;/Parent&gt;
	 * </pre>
	 *
	 * details key: "featureWrapperElement=false" <br/>
	 * details key: "featureElement=true" <br/>
	 * details key: "classifierWrapperElement=true"<br/>
	 * details key: "classifierElement=false" <br/>
	 *
	 * @see #getFeatureSerializationStructure
	 * @see #setXMLPersistenceMappingStrategy
	 */
	int XML_PERSISTENCE_MAPPING_STRATEGY__0110__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT = 6;

	/**
	 * The feature serialization structure ID for a feature that is serialized using the following XML element
	 * structure:
	 *
	 * <pre>
	 * &lt;Parent&gt;
	 *   (&lt;FeatureName&gt;
	 *   	&lt;ClassifierWrapperName&gt;
	 *        (&lt;ClassifierName&gt; ... &lt;/ClassifierName&gt;)?
	 *      &lt;/ClassifierWrapperName&gt;
	 *   &lt;/FeatureName&gt;)?
	 * &lt;/Parent&gt;
	 * </pre>
	 *
	 * details key: "featureWrapperElement=false" <br/>
	 * details key: "featureElement=true" <br/>
	 * details key: "classifierWrapperElement=true"<br/>
	 * details key: "classifierElement=true" <br/>
	 *
	 * @see #getFeatureSerializationStructure
	 * @see #setXMLPersistenceMappingStrategy
	 */
	int XML_PERSISTENCE_MAPPING_STRATEGY__0111__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT = 7;

	/**
	 * The feature serialization structure ID for a feature that is serialized using the following XML element
	 * structure:
	 *
	 * <pre>
	 * &lt;Parent&gt;
	 *   &lt;FeatureWrapperName&gt;
	 *       ( ... )?
	 *   &lt;/FeatureWrapperName&gt;
	 * &lt;/Parent&gt;
	 * </pre>
	 *
	 * details key: "featureWrapperElement=true" <br/>
	 * details key: "featureElement=false" <br/>
	 * details key: "classifierWrapperElement=false"<br/>
	 * details key: "classifierElement=false" <br/>
	 *
	 * @see #getFeatureSerializationStructure
	 * @see #setXMLPersistenceMappingStrategy
	 */
	int XML_PERSISTENCE_MAPPING_STRATEGY__1000__FEATURE_WRAPPER_ELEMENT = 8;

	/**
	 * The feature serialization structure ID for a feature that is serialized using the following XML element
	 * structure:
	 *
	 * <pre>
	 * &lt;Parent&gt;
	 *   &lt;FeatureWrapperName&gt;
	 *   	(&lt;ClassifierName&gt; ... &lt;/ClassifierName&gt;)?
	 *   &lt;/FeatureWrapperName&gt;
	 * &lt;/Parent&gt;
	 * </pre>
	 *
	 * details key: "featureWrapperElement=true" <br/>
	 * details key: "featureElement=false" <br/>
	 * details key: "classifierWrapperElement=false"<br/>
	 * details key: "classifierElement=true" <br/>
	 *
	 * @see #getFeatureSerializationStructure
	 * @see #setXMLPersistenceMappingStrategy
	 */
	int XML_PERSISTENCE_MAPPING_STRATEGY__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT = 9;

	int XML_PERSISTENCE_MAPPING_STRATEGY__1010__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT = 10;

	int XML_PERSISTENCE_MAPPING_STRATEGY__1011__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT = 11;

	int XML_PERSISTENCE_MAPPING_STRATEGY__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT = 12;

	int XML_PERSISTENCE_MAPPING_STRATEGY__1101__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_ELEMENT = 13;

	int XML_PERSISTENCE_MAPPING_STRATEGY__1110__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT = 14;

	int XML_PERSISTENCE_MAPPING_STRATEGY__1111__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT = 15;

	/**
	 * Returns the kind of XML structure that should be used to represent the given structural feature.
	 * <p>
	 * details key: "featureElement" <br/>
	 * details key: "featureWrapperElement" <br/>
	 * details key: "classifierElement" <br/>
	 * details key: "classifierWrapperElement"<br/>
	 *
	 * @param eStructuralFeature
	 * @param featureSerializationKind
	 */
	int getXMLPersistenceMappingStrategy(EStructuralFeature eStructuralFeature);

	/**
	 * Sets the kind of XML structure that should be used to represent the given structural feature.
	 * <p>
	 * details key: "featureElement" <br/>
	 * details key: "featureWrapperElement" <br/>
	 * details key: "classifierElement" <br/>
	 * details key: "classifierWrapperElement"<br/>
	 *
	 * @param eStructuralFeature
	 * @param xmlPersistenceMappingStrategy
	 */
	void setXMLPersistenceMappingStrategy(EStructuralFeature eStructuralFeature, int xmlPersistenceMappingStrategy);

	/**
	 * The default RMFExtendedMetaData instance.
	 */
	XMLPersistenceMappingExtendedMetaData INSTANCE = new XMLPersistenceMappingExtendedMetaDataImpl();

	/**
	 * Returns if the feature is XML Attribute
	 */
	boolean isXMLAttribute(EStructuralFeature eClaseStructuralFeature);

	/**
	 * Sets the XML Attribute for a feature
	 */
	void setXMLAttribute(EStructuralFeature eClaseStructuralFeature, boolean isXMLAttribute);

	/**
	 * Returns the XML global element for a classifier.
	 */
	boolean isXMLGlobalElement(EClassifier eClassifier);

	/**
	 * Sets the XML global element for a classifier.
	 */
	void setXMLGlobalElement(EClassifier eClassifier, boolean isXMLGlobalElement);

	/**
	 * Returns the XML custom simple type for a classifier.
	 * <p>
	 * details key: "xmlCustomSimpleType"
	 */
	String getXMLCustomSimpleType(EClassifier eClassifier);

	/**
	 * Sets the XML custom simple type for a classifier.
	 * <p>
	 * details key: "xmlCustomSimpleType"
	 */
	void setXMLCustomSimpleType(EClassifier eClassifier, String xmlCustomTypeName);

	/**
	 * Returns the XML XSD simple type for a classifier.
	 * <p>
	 * details key: "xmlXsdSimpleType"
	 */
	String getXMLXsdSimpleType(EClassifier eClassifier);

	/**
	 * Sets the XML XSD simple type for a classifier.
	 * <p>
	 * details key: "xmlXsdSimpleType"
	 */
	void setXMLXsdSimpleType(EClassifier eClassifier, String xmlXsdSimpleType);

	/**
	 * Returns the XML CustomSimpleTypeValue Pattern for a classifier.
	 * <p>
	 * details key: "xmlCustomSimpleTypeValuePattern"
	 */
	String getXMLCustomSimpleTypeValuePattern(EClassifier eClassifier);

	/**
	 * Sets the XML CustomSimpleTypeValue Pattern for a classifier.
	 * <p>
	 * details key: "xmlCustomSimpleTypeValuePattern"
	 */
	void setXMLCustomSimpleTypeValuePattern(EClassifier eClassifier, String xmlXsdPattern);

	/**
	 * Returns the Schema Location for a package
	 * <p>
	 * details key: "schemaLocation"
	 */
	String getXMLSchemaLocation(EPackage ePackage);

	/**
	 * Sets the Schema Location for a package.
	 * <p>
	 * details key: "schemaLocation"
	 */
	void setXMLSchemaLocation(EPackage ePackage, String schemaLocation);

	/**
	 * Returns the External Schema Locations for a package
	 * <p>
	 * details key: "externalSchemaLocations"
	 */
	Map<String, String> getXMLExternalSchemaLocations(EPackage ePackage);

	/**
	 * Sets the External Schema Locations for a package.
	 * <p>
	 * details key: "externalSchemaLocations"
	 */
	void setXMLExternalSchemaLocations(EPackage ePackage, Map<String, String> externalSchemaLocations);

	// type identification strategy
	public static final int TYPE_IDENTIFICATION_UNSPECIFIED = 0;
	public static final int TYPE_IDENTIFICATION_ATTRIBUTE_ONLY = 1;
	public static final int TYPE_IDENTIFICATION_URI_ONLY = 2;
	public static final int TYPE_IDENTIFICATION_ATTRIBUTE_OVERWRITES_URI = 3;
	public static final int TYPE_IDENTIFICATION_URI_OVERWRITES_ATTRIBUTE = 4;

	public static final String[] TYPE_IDENTIFICATION_STRATEGIES = {
			"unspecified", "attributeOnly", "uriOnly", "attributeOverwritesURI", "uriOverwritesAttribute" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

	int getXMLTypeIdentificationStrategy(EStructuralFeature eStructuralFeature);

	void setXMLTypeIdentificationStrategy(EStructuralFeature eStructuralFeature, int typeIdentificationStrategy);

	// type declaration strategy
	public static final int TYPE_DECLARATION_UNSPECIFIED = 0;
	public static final int TYPE_DECLARATION_IF_NEEDED = 1;
	public static final int TYPE_DECLARATION_ALWAYS = 2;

	public static final String[] TYPE_DECLARATION_STRATEGIES = { "unspecified", "ifNeeded", "always" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	int getXMLTypeDeclarationStrategy(EStructuralFeature eStructuralFeature);

	void setXMLTypeDeclarationStrategy(EStructuralFeature eStructuralFeature, int typeDeclarationStrategy);

	// type AttributeName
	String getXMLTypeAttributeName(EStructuralFeature eStructuralFeature);

	void setXMLTypeAttributeName(EStructuralFeature eStructuralFeature, String typeAttributeName);

	String getXMLDefaultEReferenceReferencedTypeAttributeName(EPackage ePackage);

	void setXMLDefaultEReferenceReferencedTypeAttributeName(EPackage ePackage, String defaultEReferenceReferencedTypeAttributeName);

	int getXMLDefaultEReferenceReferencedTypeDeclarationStrategy(EPackage ePackage);

	void setXMLDefaultEReferenceReferencedTypeDeclarationStrategy(EPackage ePackage, int defaultEReferenceReferencedTypeDeclarationStrategy);

	int getXMLDefaultEReferenceReferencedTypeIdentificationStrategy(EPackage ePackage);

	void setXMLDefaultEReferenceReferencedTypeIdentificationStrategy(EPackage ePackage, int defaultEReferenceReferencedTypeIdentificationStrategy);

	String getXMLDefaultEReferenceContainedTypeAttributeName(EPackage ePackage);

	void setXMLDefaultEReferenceContainedTypeAttributeName(EPackage ePackage, String defaultEReferenceContainedTypeAttributeName);

	int getXMLDefaultEReferenceContainedTypeDeclarationStrategy(EPackage ePackage);

	void setXMLDefaultEReferenceContainedTypeDeclarationStrategy(EPackage ePackage, int defaultEReferenceContainedTypeDeclarationStrategy);

	int getXMLDefaultEReferenceContainedTypeIdentificationStrategy(EPackage ePackage);

	void setXMLDefaultEReferenceContainedTypeIdentificationStrategy(EPackage ePackage, int defaultEReferenceContainedTypeIdentificationStrategy);
}
