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
 package org.eclipse.sphinx.emf.serialization.generators.xsd

import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.OperationCanceledException
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EClassifier
import org.eclipse.emf.ecore.EDataType
import org.eclipse.emf.ecore.EEnum
import org.eclipse.emf.ecore.ENamedElement
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.emf.ecore.util.ExtendedMetaData
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry
import org.eclipse.sphinx.emf.serialization.XMLPersistenceMappingExtendedMetaData
import org.eclipse.sphinx.emf.serialization.generators.util.Ecore2XSDUtil
import org.eclipse.sphinx.emf.serialization.generators.util.IGeneratorConstants
import org.eclipse.sphinx.emf.util.EcorePlatformUtil
import org.eclipse.xsd.XSDAttributeDeclaration
import org.eclipse.xsd.XSDAttributeUse
import org.eclipse.xsd.XSDAttributeUseCategory
import org.eclipse.xsd.XSDComplexTypeDefinition
import org.eclipse.xsd.XSDCompositor
import org.eclipse.xsd.XSDDerivationMethod
import org.eclipse.xsd.XSDElementDeclaration
import org.eclipse.xsd.XSDEnumerationFacet
import org.eclipse.xsd.XSDFactory
import org.eclipse.xsd.XSDForm
import org.eclipse.xsd.XSDImport
import org.eclipse.xsd.XSDModelGroup
import org.eclipse.xsd.XSDModelGroupDefinition
import org.eclipse.xsd.XSDParticle
import org.eclipse.xsd.XSDPatternFacet
import org.eclipse.xsd.XSDProcessContents
import org.eclipse.xsd.XSDSchema
import org.eclipse.xsd.XSDSimpleTypeDefinition
import org.eclipse.xsd.XSDTypeDefinition
import org.eclipse.xsd.XSDWildcard
import org.eclipse.xsd.ecore.NameMangler
import org.eclipse.xsd.util.XSDConstants
import java.util.HashMap
import org.eclipse.sphinx.emf.serialization.generators.util.JavaXSDPrimitiveTypeMapping
import java.util.Collections
import org.eclipse.emf.ecore.xml.type.XMLTypePackage

class Ecore2XSDFactory extends NameMangler {

	/**
   	 * The schema for schema namespace version to be used for new schemas.
     */
    protected String defaultXMLSchemaNamespace = XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001;

    /**
   	 * The XML namespace version to be used for new schemas.
     */
    protected String defaultXMLNamespace = XSDConstants.XML_NAMESPACE_URI_1998;

	/**
     * The extended meta data used to determine the schema structure.
     */
	protected XMLPersistenceMappingExtendedMetaData xsdExtendedMetaData = XMLPersistenceMappingExtendedMetaData::INSTANCE;

	protected static XSDFactory xsdFactory = XSDFactory::eINSTANCE

    public Set<String> patternCaseSet = new HashSet<String>();

    public Set<EPackage> referencedEcores= new HashSet<EPackage>();

    public Map<String, String> nsSchemaLocations = new HashMap<String, String>()

	/**
   	 * The schema for user schema namespace version to be used for new schemas.
     */
    protected String defaultUserSchemaNamespace;

	public EPackage ecoreModel

	new(EPackage ecoreModel) {
		this.ecoreModel = ecoreModel;
	}

	def XSDSchema initSchema(EPackage ePackage, ResourceSet resourceSet, IProgressMonitor monitor) {
		val SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		progress.subTask("Initialize XSD schema");

		val XSDSchema xsdSchema = XSDFactory::eINSTANCE.createXSDSchema();

		// Set the target namespace.
		val String targetNamespace = xsdExtendedMetaData.getNamespace(ePackage);
		xsdSchema.setTargetNamespace(targetNamespace);

		// Choose the schema for schema namespace and prefix.
		var Map<String, String> namespaces = xsdSchema.getQNamePrefixToNamespaceMap();
		xsdSchema.setSchemaForSchemaQNamePrefix(IGeneratorConstants.DEFAULT_XML_SCHEMA_NAMESPACE_PREFIX);
		namespaces.put(IGeneratorConstants.DEFAULT_XML_SCHEMA_NAMESPACE_PREFIX, defaultXMLSchemaNamespace);

		// Ensure that a prefix is assigned for the target namespace.
		val String nsPrefix = getRootNsPrefix(ePackage);
		Ecore2XSDUtil::handlePrefix(namespaces, nsPrefix, targetNamespace);

		// Set element form default and attribute form default
		xsdSchema.setElementFormDefault(XSDForm.QUALIFIED_LITERAL);
		xsdSchema.setAttributeFormDefault(XSDForm.UNQUALIFIED_LITERAL);

		// Create the node so that annotations can be added to the DOM.
		xsdSchema.updateElement();

		//====== XML schema imports
		initSchemaImports(ePackage, xsdSchema, monitor)

		//buildAnnotations(xsdSchema, ePackage);
		this.defaultUserSchemaNamespace = targetNamespace;

		progress.done();

		return xsdSchema;
	}

	/**
	 * To be override by custom
	 */
	def protected void initSchemaImports(EPackage ePackage, XSDSchema xsdSchema, IProgressMonitor monitor) {
		val Map<String, String> externalSchemaLocations = xsdExtendedMetaData.getXMLExternalSchemaLocations(ePackage);

		// get referenced ecore root packages for import
		// attention: no import is required for references to simple Ecore data types such as EString
		val Set<EPackage> referencedEcores = getReferencedEcoreRootPackages(monitor);

		// import for all referenced EPackage
		referencedEcores.forEach[
			val String namespace = getGlobalXSDSchemaNamespace(it)
			val String schemaLocation = getSchemaLocation(it)
			nsSchemaLocations.put(namespace, schemaLocation)
		]

		// add additional imports if the "externalSchemaLocations" annotation of EPackage is configured
		nsSchemaLocations.putAll(externalSchemaLocations)

		// the list of imports are ordered alphabetically by the nsURI
		var List<String> orderedNs = new ArrayList<String>()
		orderedNs.addAll(nsSchemaLocations.keySet)
		Collections.sort(orderedNs)
		orderedNs.forEach[
			xsdSchema.getContents.add(createXSDImport(it, nsSchemaLocations.get(it)))]
	}

	// can be overriden by custom
	def protected String getRootNsPrefix(EPackage ePackage){
		return ePackage.getNsPrefix();
	}

	def XSDImport create xsdImport : xsdFactory.createXSDImport() createXSDImport(String nameSpace, String schemaLocation){
		xsdImport.setNamespace(nameSpace);
		xsdImport.setSchemaLocation(schemaLocation);
	}

	/**
	 * this method should be overriden to provide the actual referenced type xml name
	* in general case, it is the upper case of the fetched xml name
	* in EAST-ADL, it may also be +"-IREF" or +"-TREF"
	*/
	def protected String getTypeXMLName(String xmlName, ENamedElement eElement){
		val EPackage rootPackage = EcoreUtil.getRootContainer(eElement) as EPackage;
		val String nsURI = rootPackage.getNsURI();

		// if the type is created in the same xsd, return the fetched xmlName
		if(nsURI.equals(xsdExtendedMetaData.getNamespace(ecoreModel))){
			return xmlName;
		}
		// if it is Ecore element
		else if(nsURI.equals(IGeneratorConstants.DEFAULT_ECORE_NAMESPACE)){
			return xmlName;
		}
		// if it is in referenced ecores
		else if(referencedEcores.contains(rootPackage)){
			//return XSDExtendedMetaDataUtil::buildXmlName(xmlName);
			// singular name by default
			var String result = Ecore2XSDUtil::getSingularName(eElement);
			return result;
		}

		//return XSDExtendedMetaDataUtil::buildXmlName(xmlName);
		return Ecore2XSDUtil::getSingularName(eElement);
	}

	/**
	 * Get the global XSD namespace of the given root package, it may be different from the namespace of the root package.
	 * For example, the namespace of AUTOSAR is "http://autosar.org/schema/r4.0", while the namespace of the root AUTOSAR package is "http://autosar.org/schema/r4.0/autosar40"
	 */
	def protected String getGlobalXSDSchemaNamespace(EPackage rootPackage){
		var String namespace = rootPackage.getNsURI();

		// Get the metamodel descriptor from the metamodelDescriptor registry
		val java.net.URI namespaceURI = new java.net.URI(namespace);
		val IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(namespaceURI);
		if(mmDescriptor!=null){
			namespace = mmDescriptor.getNamespaceURI().toString();
		}

		return namespace;
	}

	def protected String getSchemaLocation(EPackage referencedRootPackage){
		// get the XSD namespace of the referenced root package
		val String namespace = getGlobalXSDSchemaNamespace(referencedRootPackage);
		var String schemaLocation = xsdExtendedMetaData.getXMLSchemaLocation(referencedRootPackage);
		if (null == schemaLocation) {
				// if Ecore.xsd
			if(namespace.equals(IGeneratorConstants.DEFAULT_ECORE_NAMESPACE)){
				schemaLocation = "platform:/plugin/org.eclipse.emf.ecore/model/Ecore.xsd";
			}
			else{
				val URI uri= getXSDSchemaFileURIFromNsURI(namespace);
				if(uri != null){
			 		schemaLocation = uri.toString();
			 	}
				else{
					// if not find the xsd schema, create one in the same directory
					schemaLocation = referencedRootPackage.getName() + IGeneratorConstants.DEFAULT_XML_SCHEMA_NAMESPACE_PREFIX;
					}
			}
		}
		return schemaLocation
	}

	// To be overriden by custom
	def protected void loadImportReferencedXSD(EPackage referencedRootPackage, XSDSchema xsdSchema, ResourceSet resourceSet){
		// get the XSD namespace of the referenced root package

		val String namespace = getGlobalXSDSchemaNamespace(referencedRootPackage);

		var String schemaLocation = xsdExtendedMetaData.getXMLSchemaLocation(referencedRootPackage);
		if (null == schemaLocation) {
			// if Ecore.xsd
			if(namespace.equals(IGeneratorConstants.DEFAULT_ECORE_NAMESPACE)){
				schemaLocation = "platform:/plugin/org.eclipse.emf.ecore/model/Ecore.xsd";
			}
			else{
				val URI uri= getXSDSchemaFileURIFromNsURI(namespace);
				if(uri != null){
			 		schemaLocation = uri.toString();
			 	}
				else{
					// if not find the xsd schema, create one in the same directory
					schemaLocation = referencedRootPackage.getName() + IGeneratorConstants.DEFAULT_XML_SCHEMA_NAMESPACE_PREFIX;
					}
			}
		}

		// import
		val XSDImport xsdImport = createXSDImport(namespace, schemaLocation);
		xsdSchema.getContents.add(xsdImport);

	}


	// To be overriden by custom
  def protected URI getXSDSchemaFileURIFromNsURI(String nsURI){
  	// null by default
  	//return null;

  	// for Hummingbird20
  	val IPath path = new Path("C:/Work/Eclipse-Kepler/runtime-EclipseApplication-Kepler/xsd.generator.test/model/referenced/hummingbird20.xsd");
  	return EcorePlatformUtil.createURI(path);
  }

  /**
   * Get referenced ecore root packages.
   * If references to simple Ecore data types or XSD Types, such as EString, then the referenced Ecore package is not taken into account
   */
	def protected Set<EPackage> getReferencedEcoreRootPackages(IProgressMonitor monitor){
		val SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		val String targetNamespace = xsdExtendedMetaData.getNamespace(ecoreModel);

		ecoreModel.eAllContents.forEach[
			if (it instanceof EClass){
				getESuperTypes().forEach[
					val EPackage rootEPackage =  (EcoreUtil.getRootContainer(it)) as EPackage;
					val String uri = rootEPackage.getNsURI();
					if(uri != targetNamespace){
						referencedEcores.add(rootEPackage)
					}
				]
			}
			else if(it instanceof EStructuralFeature){
				val EClassifier eType = getEType();
				val EObject container = EcoreUtil.getRootContainer(eType);
				if(container instanceof EPackage){
					val String uri = container.getNsURI();
					if(uri != targetNamespace){
						// if Ecore or XMLType Classifier, then do not add to the referencedEcores
						if (uri.equals(IGeneratorConstants.DEFAULT_ECORE_NAMESPACE) || XMLTypePackage.eNS_URI.equals(uri)){
							// intentionally ignored
						} else {
							referencedEcores.add(container)
						}
					}

				}
			}

			progress.worked(1);
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		]

		return referencedEcores;
	}

	// Returns the string that represents the uri of the given eClassifier in xsd file.
	// can be custom overriden
	// if the xmlName is not initialized (especially the case that the eClassifier is in other custom ecore),
	// then the eClassifier name is taken as the xmlName by default,
	// which may be not the real xmlName, need to be converted:  getTypeXMLName()
	def protected String getElementXSDURI(EClassifier eClassifier) {
		val EPackage rootEPackage = EcoreUtil.getRootContainer(eClassifier) as EPackage;
		val String namespace = xsdExtendedMetaData.getNamespace(rootEPackage);
		var String result = xsdExtendedMetaData.getXMLName(eClassifier);
		result = getTypeXMLName(result, eClassifier);
		if (namespace != null) {
			result = namespace + "#" + result;//$NON-NLS-1$
		}
		return result;
	}

  def protected String getQualifiedRootPackageName(EPackage ePackage) {
		return ecoreModel.getName();
	}

 def protected List<EStructuralFeature> getEAllRelevantStructuralFeatures(EClass eClass){
		return eClass.EAllStructuralFeatures;
	}

	//================================================================================
	/**
	 * Create a XSD-element for the class of which the "global_element" tag is true.
	 * Add this XSD-element into the xsdSchema.
	 */
	 def void createGlobalElement3(EPackage rootEPackageModel, XSDSchema xsdSchema){
	 	val List<EClassifier> globalEClassifiers = Ecore2XSDUtil::getGlobalElements(rootEPackageModel);

	 	globalEClassifiers.forEach [
	 		// create a XSD-element
	 		val XSDElementDeclaration xsdGlobalElement = xsdFactory.createXSDElementDeclaration();
			// set name
			xsdGlobalElement.setName(xsdExtendedMetaData.getXMLName(it));
			// set type
			val String uri = getElementXSDURI(it);
			val XSDTypeDefinition xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(uri);
			xsdGlobalElement.setTypeDefinition(xsdTypeDefinition);

	 		// add into xsdSchema
	 		xsdSchema.getContents().add(xsdGlobalElement);
	 	]
	 }

	/**
 	* RMF ReqIF rule
 	*/
	def void createFixedRefTypes4(XSDSchema xsdSchema){
		val XSDSimpleTypeDefinition simpleTypeLocal= xsdFactory.createXSDSimpleTypeDefinition();
		simpleTypeLocal.setName("LOCAL-REF");
		val XSDTypeDefinition baseTypeLocal = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#IDREF");
		simpleTypeLocal.setBaseTypeDefinition(baseTypeLocal as XSDSimpleTypeDefinition);
		xsdSchema.getContents().add(simpleTypeLocal);

		val XSDSimpleTypeDefinition simpleTypeGlobal= xsdFactory.createXSDSimpleTypeDefinition();
		simpleTypeGlobal.setName("GLOBAL-REF");
		val XSDTypeDefinition baseTypeGlobal = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
		simpleTypeGlobal.setBaseTypeDefinition(baseTypeGlobal as XSDSimpleTypeDefinition);
		xsdSchema.getContents().add(simpleTypeGlobal);
	}


	/**
 	 * Create xsd:complexType for a class
 	 */
	def XSDComplexTypeDefinition create xsdComplexTypeDefinition : xsdFactory.createXSDComplexTypeDefinition() createClassTypeDefinition5(
		EClass eClass, XSDSchema xsdSchema, ArrayList<EClass> referencedClass, IProgressMonitor monitor) {

		val SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}
		progress.subTask("create XSD Complex Type Definition for " + eClass.getName());

		xsdComplexTypeDefinition.setName(xsdExtendedMetaData.getXMLName(eClass));

		//===================
		// xsd:all or xsd:sequence
		val XSDParticle xsdParticle = XSDFactory.eINSTANCE.createXSDParticle();
		xsdComplexTypeDefinition.setContent(xsdParticle);
		val XSDModelGroup xsdModelGroup = XSDFactory.eINSTANCE.createXSDModelGroup();

		// set to "sequence" for EAST-ADL
		xsdModelGroup.setCompositor(XSDCompositor.SEQUENCE_LITERAL);//ALL_LITERAL
		xsdParticle.setContent(xsdModelGroup);

		val List<EStructuralFeature> relevantStructuralFeatures = eClass.EAllRelevantStructuralFeatures
		val SubMonitor subProgress = progress.newChild(100).setWorkRemaining(relevantStructuralFeatures.size());
		relevantStructuralFeatures.forEach [
			var XSDParticle xsdParticleForFeature = null
			if (ExtendedMetaData.ELEMENT_FEATURE.equals(ExtendedMetaData.INSTANCE.getFeatureKind(it))) {
				// xml elements

				// ================= EAttribute
				if (it instanceof EAttribute) {
						xsdParticleForFeature = switch xsdExtendedMetaData.getXMLPersistenceMappingStrategy(it) {
							case XMLPersistenceMappingExtendedMetaData.
								XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT:
								createClassElementAttribute5a(it, xsdSchema)
							case XMLPersistenceMappingExtendedMetaData.
								XML_PERSISTENCE_MAPPING_STRATEGY__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT:
								createEAttributeContained1100(it, xsdSchema)
							default:
						    // should create some content in the schema that indicates the missing content
							{
								System.err.println(
									"Not supported: EAttribute " +
										xsdExtendedMetaData.getXMLPersistenceMappingStrategy(it))
								null
							}
						}

				}

			// ================= EReference
				else if (it instanceof EReference) {
					// ======= EReference is containment
					if (isContainment()) {

                        xsdParticleForFeature = switch xsdExtendedMetaData.getXMLPersistenceMappingStrategy(it) {
                        	case XMLPersistenceMappingExtendedMetaData.
							    XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT :
							    create_EReference_contained0100(eClass,xsdSchema)
							case XMLPersistenceMappingExtendedMetaData.
							    XML_PERSISTENCE_MAPPING_STRATEGY__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT :
							    create_EReference_contained0101(eClass,xsdSchema)
                        	case XMLPersistenceMappingExtendedMetaData.
                        	    XML_PERSISTENCE_MAPPING_STRATEGY__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT :
                        		create_EReference_contained1001(eClass, xsdSchema)
                        	case XMLPersistenceMappingExtendedMetaData.
							    XML_PERSISTENCE_MAPPING_STRATEGY__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT :
							    create_EReference_contained1100(eClass,xsdSchema)
                        	default: null

                        }

				// ======= EReference is not containment
					} else {

						xsdParticleForFeature = switch xsdExtendedMetaData.getXMLPersistenceMappingStrategy(it) {
							case XMLPersistenceMappingExtendedMetaData.
							    XML_PERSISTENCE_MAPPING_STRATEGY__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT :
							    createClassComposition5b(eClass)
							case XMLPersistenceMappingExtendedMetaData.
							    XML_PERSISTENCE_MAPPING_STRATEGY__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT :
							    create_EReference_referenced1100Many_5l(eClass, xsdSchema, referencedClass)
							case XMLPersistenceMappingExtendedMetaData.
  							    XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT :
  							    create_EReference_referenced0100Many_5m(eClass,	xsdSchema, referencedClass)
							default: null
						}
					}
				}

				if (null != xsdParticleForFeature) {
					xsdModelGroup.getContents().add(xsdParticleForFeature);
				}

			} else if (ExtendedMetaData.ATTRIBUTE_FEATURE.equals(ExtendedMetaData.INSTANCE.getFeatureKind(it))) {

				// xml attributes
				if (it instanceof EAttribute) {

					// Rule: EAttributeAttribute
					val XSDAttributeUse xsdAttributeUse = createClassAttribute5d(it, eClass, xsdSchema);
					xsdComplexTypeDefinition.getAttributeContents().add(xsdAttributeUse);
				}
			} else {
				// mixed, etc. features
			}
			subProgress.worked(1);
			if (subProgress.isCanceled()) {
				throw new OperationCanceledException();
			}
		]

		xsdSchema.getContents().add(xsdComplexTypeDefinition);
	}

	/**
	 * create xsd:element for a attribute with rule 5a of RMF
	 * EAttribute & kind=element & FTFF (0100)
	 *
	 * 5a. ClassElementAttribute ::=
	 *	<xsd:element
	 *		 name=" //Name of Attribute (single) // " minOccurs=" // Minimum // " maxOccurs=" // Maximum // "
	 *		( fixed=" // fixed value // " )?
	 *		type=" //Name of Attribute Type// "/>
	 */
	def XSDParticle createClassElementAttribute5a(EAttribute eAttribute, XSDSchema xsdSchema) {
		var xsdParticle = xsdFactory.createXSDParticle()

		xsdParticle.setMinOccurs(eAttribute.getLowerBound());
		xsdParticle.setMaxOccurs(eAttribute.getUpperBound());

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(xsdExtendedMetaData.getXMLName(eAttribute));

		// set type
		var XSDTypeDefinition xsdTypeDefinition= xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
    	val EClassifier baseType = eAttribute.getEAttributeType();
    	if(baseType instanceof EDataType && isXMLPrimitiveXsdType(baseType as EDataType)){
    		   	//val String xsdSimpleType = xsdExtendedMetaData.getXMLXsdSimpleType(baseType); //baseType.getName().toLowerCase();
    			val String xsdSimpleType = getXsdSimpleType(baseType)
    			xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#"+xsdSimpleType);
    	}
    	else{
    			val String uri = getElementXSDURI(baseType);
				xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(uri);
    	}

		xsdElement.setTypeDefinition(xsdTypeDefinition);
		xsdParticle.setContent(xsdElement);

		return xsdParticle
	}

	/**
	 * create xsd:element for a attribute with rule EAttributeContained1100
	 *
     * Example:
     * <pre>
     * <xsd:element name="PROPERTIES" minOccurs="0" maxOccurs="1" >
     *    <xsd:complexType>
     *      <xsd:choice minOccurs="0" maxOccurs="unbounded" >
     *        <xsd:element name="PROPERTY" type="node:STRING"/>
     *      </xsd:choice>
     *    </xsd:complexType>
     * </xsd:element>
     * </pre>
	 */
	def XSDParticle createEAttributeContained1100(EAttribute eAttribute, XSDSchema xsdSchema) {
		var xsdParticle = xsdFactory.createXSDParticle()

		// <xsd:element name="PROPERTIES" minOccurs="0" maxOccurs="1" >
		if(eAttribute.getLowerBound()>0){
			xsdParticle.setMinOccurs(1);
		}
		else{
			xsdParticle.setMinOccurs(0);
		}
		xsdParticle.setMaxOccurs(1);

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(xsdExtendedMetaData.getXMLWrapperName(eAttribute)); //

		// <xsd:complexType>
		val XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();

		// <xsd:choice maxOccurs="xx" minOccurs="xx">
		val XSDParticle xsdParticle2 = XSDFactory.eINSTANCE.createXSDParticle();
		xsdParticle2.setMaxOccurs(eAttribute.getUpperBound());
		xsdParticle2.setMinOccurs(eAttribute.getLowerBound());
		val XSDModelGroup xsdModelGroup2 = XSDFactory.eINSTANCE.createXSDModelGroup();

		// set to "choice"
		xsdModelGroup2.setCompositor(XSDCompositor.CHOICE_LITERAL);

		// get the feature type
		val EDataType datatype = eAttribute.EAttributeType;

		// <xsd:element name="PROPERTY" type="node:STRING"/>
		val XSDParticle xsdParticle3 = XSDFactory.eINSTANCE.createXSDParticle();
		val XSDElementDeclaration xsdElement2 = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement2.setName(xsdExtendedMetaData.getXMLName(eAttribute));

		// set type
		var XSDTypeDefinition xsdTypeDefinition= xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
  		if(isXMLPrimitiveXsdType(datatype)){
     			val String xsdSimpleType = getXsdSimpleType(datatype)
    			xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#"+xsdSimpleType);
    	}
    	else{
    			val String uri = getElementXSDURI(datatype);
				xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(uri);
    	}
    	xsdElement2.setTypeDefinition(xsdTypeDefinition);

		xsdParticle3.setContent(xsdElement2);
		xsdModelGroup2.getContents().add(xsdParticle3);

		xsdParticle2.setContent(xsdModelGroup2);
		xsdComplexTypeDefinition.setContent(xsdParticle2);
		xsdElement.setAnonymousTypeDefinition(xsdComplexTypeDefinition);
		xsdParticle.setContent(xsdElement);

		return xsdParticle

	}

	/**
	 * create xsd:element for a feature that the type is no composite with rule 5b of RMF
	 * EReference & !containment & TFFT (1001)
	 */
	def XSDParticle createClassComposition5b(EStructuralFeature feature, EClass eClass) {
		var xsdParticle = xsdFactory.createXSDParticle()

		if(feature.getLowerBound()>0){
			xsdParticle.setMinOccurs(1);
		}
		else{
			xsdParticle.setMinOccurs(0);
		}
		xsdParticle.setMaxOccurs(1);

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(xsdExtendedMetaData.getXMLName(feature)); //

		// <xsd:complexType>
		val XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();

		// <xsd:choice maxOccurs="xx" minOccurs="xx">
		val XSDParticle xsdParticle2 = XSDFactory.eINSTANCE.createXSDParticle();
		xsdParticle2.setMaxOccurs(feature.getUpperBound());
		xsdParticle2.setMinOccurs(feature.getLowerBound());
		val XSDModelGroup xsdModelGroup2 = XSDFactory.eINSTANCE.createXSDModelGroup();

		// set to "choice"
		xsdModelGroup2.setCompositor(XSDCompositor.CHOICE_LITERAL);

		// get the feature type
		val EClassifier typeeClassifier = feature.getEType();

		// <xsd:element name="CLASS-B" type="sky:CLASS-B"/>
		val XSDParticle xsdParticle3 = XSDFactory.eINSTANCE.createXSDParticle();
		val XSDElementDeclaration xsdElement2 = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement2.setName("xxxxxxxx"); //(xsdExtendedMetaData.getName(typeeClassifier));
		val String typeeClassifierURI = getElementXSDURI(typeeClassifier);
		val XSDTypeDefinition xsdTypeDefinition = xsdElement2.resolveTypeDefinitionURI(typeeClassifierURI);
		xsdElement2.setTypeDefinition(xsdTypeDefinition);

		xsdParticle3.setContent(xsdElement2);
		xsdModelGroup2.getContents().add(xsdParticle3);

		// for each subtype, create a xsd:element
		if(typeeClassifier instanceof EClass){
			Ecore2XSDUtil::findESubTypesOf(typeeClassifier).forEach[
				val XSDParticle xsdParticleSubType = XSDFactory.eINSTANCE.createXSDParticle();
				val XSDElementDeclaration xsdElementSubType = XSDFactory.eINSTANCE.createXSDElementDeclaration();
				xsdElementSubType.setName(xsdExtendedMetaData.getXMLName(it));
				val String subTypeURI = getElementXSDURI(it);
				val XSDTypeDefinition xsdTypeDefinitionSubType = xsdElementSubType.resolveTypeDefinitionURI(subTypeURI);
				xsdElementSubType.setTypeDefinition(xsdTypeDefinitionSubType);

				xsdParticleSubType.setContent(xsdElementSubType);
				xsdModelGroup2.getContents().add(xsdParticleSubType);
			]
		}

		xsdParticle2.setContent(xsdModelGroup2);
		xsdComplexTypeDefinition.setContent(xsdParticle2);
		xsdElement.setAnonymousTypeDefinition(xsdComplexTypeDefinition);
		xsdParticle.setContent(xsdElement);

		return xsdParticle
	}

	/**
	 * create xsd:element for a feature that the type is composite with rule 5c of RMF
	 * EReference & containment & TFFT (1001) -- pattern 00010 in AUTOSAR
	 *
	 * 5c. ClassCompositions ::=
	 *	<xsd:element name=" // Name of Target Property (plural) // " minOccurs="( "0" | "1" )" maxOccurs="1">
	 *		<xsd:complexType>
	 *			<xsd:choice minOccurs="//Minimum of Target Property//" maxOccurs="// Max of Target Property// ">
	 *				(<xsd:element
	 *					  name=" // XML Name of Target (Sub) Class // "
	 *					  type=" //namespace// ":" // XML Name of Target Class // " /> )+
	 *			</xsd:choice>
	 *		/xsd:complexType>
	 * 	</xsd:element>
	 */
	def XSDParticle create_EReference_contained1001(EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema) {
		var xsdParticle = xsdFactory.createXSDParticle()

		// <xsd:element minOccurs="0" name="ROLE-B-1S"/>
		if(feature.getLowerBound()>0){
			xsdParticle.setMinOccurs(1);
		}
		else{
			xsdParticle.setMinOccurs(0);
		}
		xsdParticle.setMaxOccurs(1);

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(xsdExtendedMetaData.getXMLWrapperName(feature)); //

		// <xsd:complexType>
		val XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();

		// <xsd:choice maxOccurs="xx" minOccurs="xx">
		val XSDParticle xsdParticle2 = XSDFactory.eINSTANCE.createXSDParticle();
		xsdParticle2.setMaxOccurs(feature.getUpperBound());
		xsdParticle2.setMinOccurs(feature.getLowerBound());
		val XSDModelGroup xsdModelGroup2 = XSDFactory.eINSTANCE.createXSDModelGroup();

		// set to "choice"
		xsdModelGroup2.setCompositor(XSDCompositor.CHOICE_LITERAL);

		// get the feature type
		val EClassifier typeEClassifier = feature.getEType();

		// <xsd:element name="CLASS-B" type="sky:CLASS-B"/>
		// for each subtype, create a xsd:element
		if(typeEClassifier instanceof EClass){
			Ecore2XSDUtil::findAllConcreteTypes(typeEClassifier, null).forEach[
				val XSDParticle xsdParticleSubType = XSDFactory.eINSTANCE.createXSDParticle();
				val XSDElementDeclaration xsdElementSubType = XSDFactory.eINSTANCE.createXSDElementDeclaration();
				val String subClassXMLName = getTypeXMLName(xsdExtendedMetaData.getXMLName(it, feature), it);
				xsdElementSubType.setName(subClassXMLName);
				val String subTypeURI = getElementXSDURI(it);
				val XSDTypeDefinition xsdTypeDefinitionSubType = xsdElementSubType.resolveTypeDefinitionURI(subTypeURI);
				xsdElementSubType.setTypeDefinition(xsdTypeDefinitionSubType);

				xsdParticleSubType.setContent(xsdElementSubType);
				xsdModelGroup2.getContents().add(xsdParticleSubType);
			]
		}

		xsdParticle2.setContent(xsdModelGroup2);
		xsdComplexTypeDefinition.setContent(xsdParticle2);
		xsdElement.setAnonymousTypeDefinition(xsdComplexTypeDefinition);
		xsdParticle.setContent(xsdElement);

		return xsdParticle
	}


	/**
	 * create xsd:element for a feature that the type is composite with rule 5c of RMF
	 * EReference & containment & FTFT(0101) - pattern 00012 in AUTOSAR
	 */
	def XSDParticle createClassComposition5c0101(EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema) {
		var xsdParticle = xsdFactory.createXSDParticle()

		xsdParticle.setMinOccurs(feature.getLowerBound());
		xsdParticle.setMaxOccurs(feature.getUpperBound());

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(xsdExtendedMetaData.getXMLWrapperName(feature)); //

		// <xsd:complexType>
		val XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();

		// <xsd:choice maxOccurs="xx" minOccurs="xx">
		val XSDParticle xsdParticle2 = XSDFactory.eINSTANCE.createXSDParticle();
		xsdParticle2.setMinOccurs(feature.getLowerBound());
		xsdParticle2.setMaxOccurs(1);
		val XSDModelGroup xsdModelGroup2 = XSDFactory.eINSTANCE.createXSDModelGroup();

		// set to "choice"
		xsdModelGroup2.setCompositor(XSDCompositor.CHOICE_LITERAL);

		// get the feature type
		val EClassifier typeEClassifier = feature.getEType();

		// <xsd:element />
		val XSDParticle xsdParticle3 = XSDFactory.eINSTANCE.createXSDParticle();
		val XSDElementDeclaration xsdElement2 = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement2.setName(xsdExtendedMetaData.getName(typeEClassifier));
		// set type
		var XSDTypeDefinition xsdTypeDefinition= xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
    	if(typeEClassifier instanceof EDataType && isXMLPrimitiveXsdType(typeEClassifier as EDataType)){
    			//val String xsdSimpleType = xsdExtendedMetaData.getXMLXsdSimpleType(typeeClassifier); //typeeClassifier.getName().toLowerCase();
    			val String xsdSimpleType = getXsdSimpleType(typeEClassifier)
    			xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#"+xsdSimpleType);
    	}
    	else{
    			val String uri = getElementXSDURI(typeEClassifier);
				xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(uri);
    	}
    	xsdElement2.setTypeDefinition(xsdTypeDefinition);

		xsdParticle3.setContent(xsdElement2);
		xsdModelGroup2.getContents().add(xsdParticle3);

		// for each subtype, create a xsd:element
		if(typeEClassifier instanceof EClass){
			Ecore2XSDUtil::findESubTypesOf(typeEClassifier).forEach[
				val XSDParticle xsdParticleSubType = XSDFactory.eINSTANCE.createXSDParticle();
				val XSDElementDeclaration xsdElementSubType = XSDFactory.eINSTANCE.createXSDElementDeclaration();
				xsdElementSubType.setName(xsdExtendedMetaData.getXMLName(it));
				val String subTypeURI = getElementXSDURI(it);
				val XSDTypeDefinition xsdTypeDefinitionSubType = xsdElementSubType.resolveTypeDefinitionURI(subTypeURI);
				xsdElementSubType.setTypeDefinition(xsdTypeDefinitionSubType);

				xsdParticleSubType.setContent(xsdElementSubType);
				xsdModelGroup2.getContents().add(xsdParticleSubType);
			]
		}

		xsdParticle2.setContent(xsdModelGroup2);
		xsdComplexTypeDefinition.setContent(xsdParticle2);
		xsdElement.setAnonymousTypeDefinition(xsdComplexTypeDefinition);
		xsdParticle.setContent(xsdElement);

		return xsdParticle
	}


	/**
	 * create xsd:attribute for a attribute with rule 5d of RMF
	 *
	 * 5d. ClassAttribute ::=
	 *	<xsd:attribute name="// XML name of Attribute //"
	 * 				   type="//Type of Attribute // "
	 * 				   use= "prohibited" | "optional" | "required"  />
	 */
	def XSDAttributeUse createClassAttribute5d(EAttribute attribute, EClass eClass, XSDSchema xsdSchema) {
			var xsdAttributeUse = xsdFactory.createXSDAttributeUse()

			val XSDAttributeDeclaration attributeDeclaration = XSDFactory.eINSTANCE.createXSDAttributeDeclaration();
    		attributeDeclaration.setName(xsdExtendedMetaData.getXMLName(attribute));

    		// examples: xsd:string
    		//attributeDeclaration.setTypeDefinition(xsdSchema.getSchemaForSchema().resolveSimpleTypeDefinition("string"));
    		var XSDTypeDefinition xsdTypeDefinition= xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
    		val EClassifier baseType = attribute.getEType();
    		if(baseType instanceof EDataType && isXMLPrimitiveXsdType(baseType as EDataType)){
    			//val String xsdSimpleType = xsdExtendedMetaData.getXMLXsdSimpleType(baseType); //baseType.getName().toLowerCase();
    			val String xsdSimpleType = getXsdSimpleType(baseType)
    			xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#"+xsdSimpleType);
    		}
    		else{
    			val String uri = getElementXSDURI(baseType);
				xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(uri);
    		}
    		attributeDeclaration.setTypeDefinition(xsdTypeDefinition as XSDSimpleTypeDefinition);
			xsdAttributeUse.setContent(attributeDeclaration);

			// set use: use="required" or "optional" or "prohibited"
			if(attribute.getUpperBound() == 0){
				xsdAttributeUse.setUse(XSDAttributeUseCategory.PROHIBITED_LITERAL);
			}
			else if(attribute.getLowerBound() == 0){
				xsdAttributeUse.setUse(XSDAttributeUseCategory.OPTIONAL_LITERAL);
			}
			else{
				xsdAttributeUse.setUse(XSDAttributeUseCategory.REQUIRED_LITERAL);
				}

			return xsdAttributeUse
		}


	/**
	 * create xsd:element for a EReference with rule 5i.EReference_contained1100Many of RMF
	 * EReference & containment & upperBound>1 && TTFF (1100)
	 * same as createClassComposition5c1001
	 *
	 * 5i:EReference_contained1100Many ::=
	 *	<xsd:element name=" // Name of Target Property (plural) // " minOccurs="( "0" | "1" )" maxOccurs="1">
	 *		<xsd:complexType>
	 *			<xsd:choice minOccurs="//Minimum of Target Property//" maxOccurs="// Max of Target Property// ">
	 *				(<xsd:element
	 *					  name=" // feature XML Name // "
	 *					  type=" //namespace// ":" // XML Name of Target Class // " /> )+
	 *			</xsd:choice>
	 *		</xsd:complexType>
	 * </xsd:element>
	 */
	def XSDParticle create_EReference_contained1100(EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema) {

		var xsdParticle = xsdFactory.createXSDParticle()

		//same as rule 5c: createClassComposition5c1001
		//createClassComposition5c1001(feature, eClass, xsdSchema);

		// <xsd:element minOccurs="0" name="ROLE-B-1S"/>
		if(feature.getLowerBound()>0){
			xsdParticle.setMinOccurs(1);
		}
		else{
			xsdParticle.setMinOccurs(0);
		}
		xsdParticle.setMaxOccurs(1);

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(xsdExtendedMetaData.getXMLWrapperName(feature)); //

		// <xsd:complexType>
		val XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();

		// <xsd:choice maxOccurs="xx" minOccurs="xx">
		val XSDParticle xsdParticle2 = XSDFactory.eINSTANCE.createXSDParticle();
		xsdParticle2.setMaxOccurs(feature.getUpperBound());
		xsdParticle2.setMinOccurs(feature.getLowerBound());
		val XSDModelGroup xsdModelGroup2 = XSDFactory.eINSTANCE.createXSDModelGroup();

		// set to "choice"
		xsdModelGroup2.setCompositor(XSDCompositor.CHOICE_LITERAL);

		// get the feature type
		val EClassifier typeEClassifier = feature.getEType();

		// <xsd:element name="CLASS-B" type="sky:CLASS-B"/>
		val XSDParticle xsdParticle3 = XSDFactory.eINSTANCE.createXSDParticle();
		val XSDElementDeclaration xsdElement2 = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement2.setName(xsdExtendedMetaData.getXMLName(feature));

		// set type
		var XSDTypeDefinition xsdTypeDefinition= xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
  		if(typeEClassifier instanceof EDataType && isXMLPrimitiveXsdType(typeEClassifier as EDataType)){
    			val String xsdSimpleType = getXsdSimpleType(typeEClassifier)
    			xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#"+xsdSimpleType);
    	}
    	else{
    			val String uri = getElementXSDURI(typeEClassifier);
				xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(uri);
    	}
    	xsdElement2.setTypeDefinition(xsdTypeDefinition);

		xsdParticle3.setContent(xsdElement2);
		xsdModelGroup2.getContents().add(xsdParticle3);

		// for each subtype, create a xsd:element
		if(typeEClassifier instanceof EClass){
			Ecore2XSDUtil::findESubTypesOf(typeEClassifier).forEach[
				val XSDParticle xsdParticleSubType = XSDFactory.eINSTANCE.createXSDParticle();
				val XSDElementDeclaration xsdElementSubType = XSDFactory.eINSTANCE.createXSDElementDeclaration();
				xsdElementSubType.setName(xsdExtendedMetaData.getXMLName(feature));

				val String subTypeURI = getElementXSDURI(it);
				val XSDTypeDefinition xsdTypeDefinitionSubType = xsdElementSubType.resolveTypeDefinitionURI(subTypeURI);
				xsdElementSubType.setTypeDefinition(xsdTypeDefinitionSubType);

				xsdParticleSubType.setContent(xsdElementSubType);
				xsdModelGroup2.getContents().add(xsdParticleSubType);
			]
		}

		xsdParticle2.setContent(xsdModelGroup2);
		xsdComplexTypeDefinition.setContent(xsdParticle2);
		xsdElement.setAnonymousTypeDefinition(xsdComplexTypeDefinition);
		xsdParticle.setContent(xsdElement);

		return xsdParticle
	}


	/**
	 * create xsd:element for a EReference with rule 5j.EReference_contained0101Single of RMF
	 * EReference & containment & upperBound=1 && FTFT (0101)
	 * if (sub types >1):
	 *
	 * 5j_a:EReference_contained0101Single ::=
	 *
	 *	<xsd:element name="//Name of Target Property (single)//" minOccurs="0|1" maxOccurs="1">
	 *		<xsd:complexType>
	 *			<xsd:choice minOccurs="//Minimum of Target Property//" maxOccurs="1 ">
	 *				(<xsd:element
	 *					  name=" // XML Name of Target (Sub) Class // "
	 *					  type="//namespace// ":" // XML Name of Target Class // " /> )+
	 *			</xsd:choice>
	 *		</xsd:complexType>
	 *	</xsd:element>
	 */
	def XSDParticle create_EReference_contained0101(EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema) {
		var xsdParticle = xsdFactory.createXSDParticle()

		if(feature.getLowerBound()>0){
			xsdParticle.setMinOccurs(1);
		}
		else{
			xsdParticle.setMinOccurs(0);
		}
		xsdParticle.setMaxOccurs(feature.upperBound);

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdParticle.setContent(xsdElement);
		xsdElement.setName(xsdExtendedMetaData.getXMLName(feature)); //

		// <xsd:complexType>
		val XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();
		xsdElement.setAnonymousTypeDefinition(xsdComplexTypeDefinition);

		// <xsd:choice maxOccurs="1" minOccurs="xx">
		val XSDParticle xsdParticle2 = XSDFactory.eINSTANCE.createXSDParticle();
		xsdParticle2.setMaxOccurs(1);
		xsdParticle2.setMinOccurs(feature.getLowerBound());
		val XSDModelGroup xsdModelGroup2 = XSDFactory.eINSTANCE.createXSDModelGroup();

		// set to "choice"
		xsdModelGroup2.setCompositor(XSDCompositor.CHOICE_LITERAL);

		// get the feature type
		val EClassifier typeEClassifier = feature.getEType();

		// <xsd:element name="CLASS-B" type="sky:CLASS-B"/>
		// for each subtype, create a xsd:element
		if(typeEClassifier instanceof EClass){
			Ecore2XSDUtil::findAllConcreteTypes(typeEClassifier, null).forEach[
				val XSDParticle xsdParticleSubType = XSDFactory.eINSTANCE.createXSDParticle();
				val XSDElementDeclaration xsdElementSubType = XSDFactory.eINSTANCE.createXSDElementDeclaration();
				val String subClassXMLName = getTypeXMLName(xsdExtendedMetaData.getXMLName(it, feature), it);
				xsdElementSubType.setName(subClassXMLName);
				val String subTypeURI = getElementXSDURI(it);
				val XSDTypeDefinition xsdTypeDefinitionSubType = xsdElementSubType.resolveTypeDefinitionURI(subTypeURI);
				xsdElementSubType.setTypeDefinition(xsdTypeDefinitionSubType);

				xsdParticleSubType.setContent(xsdElementSubType);
				xsdModelGroup2.getContents().add(xsdParticleSubType);
			]
		}

		xsdParticle2.setContent(xsdModelGroup2);
		xsdComplexTypeDefinition.setContent(xsdParticle2);



		return xsdParticle
	}

	/**
	 * create xsd:element for a EReference with rule 5j.EReference_contained0101Single of RMF
	 * EReference & containment & upperBound=1 && FTFT (0101)
	 * if not (sub types >1):
	 *
	 * 5j_b:EReference_contained0101Single ::=
	 *
	 * <xsd:element name='" // Name of Target Property (single) // "'
	 *				minOccurs='"//Minimum of Target Property//"'
	 * 				maxOccurs='"// Max of Target Property// "'
	 * 				type="// xml Name of Target Class //"  >
	 * </xsd:element>
	 */
	def XSDParticle create_EReference_contained0100(EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema) {
		var xsdParticle = xsdFactory.createXSDParticle()

		xsdParticle.setMinOccurs(feature.getLowerBound());
		xsdParticle.setMaxOccurs(feature.getUpperBound());

		// set name
		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(xsdExtendedMetaData.getXMLName(feature)); //

		// get the feature type
		val EClassifier typeeClassifier = feature.getEType();

		// set type
		var XSDTypeDefinition xsdTypeDefinition= xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
  		if(typeeClassifier instanceof EDataType && isXMLPrimitiveXsdType(typeeClassifier as EDataType)){
    			val String xsdSimpleType = getXsdSimpleType(typeeClassifier)
    			xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#"+xsdSimpleType);
    	}
    	else{
    			val String uri = getElementXSDURI(typeeClassifier);
				xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(uri);
    	}
    	xsdElement.setTypeDefinition(xsdTypeDefinition);
		xsdParticle.setContent(xsdElement);

		return xsdParticle
	}



	/**
	 * To be overriden by custom
	 *
	 * create xsd:element for a EReference with rule 5l.EReference_referenced1100Many of RMF
	 * EReference & !containment & upperBound>1 && TTFF (1100)
	 *
	 * (1) in case typeAttributeName is not "xsi:type"
	 * 5l:EReference_referenced1100Many ::=
	 *	<xsd:element name="//roleXmlNamePlural//" minOccurs="//lowerMultiplicity//" maxOccurs="1">
     *	  <xsd:complexType>
	 *		<xsd:choice minOccurs="//lowerMultiplicity//" maxOccurs="//upperMultiplicity//">
     *		   <xsd:element name="//roleXmlName//">
	 *			</xsd:complexType>
	 * 			   <xsd:simpleContent>
	 *				  <xsd:extension base="xsd:string">
     *				    <xsd:attribute name="typeAttributeName" type="//typeXmlNsPrefix : typeXmlName//--SUBTYPE-ENUM" use="optional"/>
	 *				  </xsd:extension>
     *			   </xsd:simpleContent>
	 * 			</xsd:complexType>
     *		   </xsd:element>
	 *		</xsd:choice>
     *   </xsd:complexType>
	 *	</xsd:element>
	 *
	 * (2) in case typeAttributeName is "xsi:type":
	 *		5l:EReference_referenced1100Many_XSITypeTrue ::=
	 *		<xsd:element name="//roleXmlNamePlural//" minOccurs="//lowerMultiplicity//" maxOccurs="1">
     *	  		<xsd:complexType>
	 *				<xsd:choice minOccurs="//lowerMultiplicity//" maxOccurs="//upperMultiplicity//">
     *		   			<xsd:element name="//roleXmlName//" type = "xsd:string"/>
	 *				</xsd:choice>
     *   		</xsd:complexType>
	 *		/xsd:element>
	 */
	def XSDParticle create_EReference_referenced1100Many_5l(EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema, ArrayList<EClass> referencedClass) {
		if(!xsdExtendedMetaData.getXMLTypeAttributeName(feature).equals("xsi:type")){
			return create_EReference_referenced1100Many_5l_XSITypeFalse(feature, eClass, xsdSchema, referencedClass)
		}

		return create_EReference_referenced1100Many_5l_XSITypeTrue(feature, eClass, xsdSchema, referencedClass)
	}

	/**
	 * create xsd:element for a EReference with rule 5l.EReference_referenced1100Many of RMF
	 * EReference & !containment & upperBound>1 && TTFF (1100) && typeAttributeName != "xsi:type"
	 *
	 * 5l:EReference_referenced1100Many_XSITypeFalse ::=
	 *	<xsd:element name="//roleXmlNamePlural//" minOccurs="//lowerMultiplicity//" maxOccurs="1">
     *	  <xsd:complexType>
	 *		<xsd:choice minOccurs="//lowerMultiplicity//" maxOccurs="//upperMultiplicity//">
     *		   <xsd:element name="//roleXmlName//">
	 *			</xsd:complexType>
	 * 			   <xsd:simpleContent>
	 *				  <xsd:extension base="xsd:string">
     *				    <xsd:attribute name="typeAttributeName" type="//typeXmlNsPrefix : typeXmlName//--SUBTYPE-ENUM" use="optional"/>
	 *				  </xsd:extension>
     *			   </xsd:simpleContent>
	 * 			</xsd:complexType>
     *		   </xsd:element>
	 *		</xsd:choice>
     *   </xsd:complexType>
	 *	</xsd:element>
	 */
	def XSDParticle create_EReference_referenced1100Many_5l_XSITypeFalse(
				EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema, ArrayList<EClass> referencedClass) {

		var xsdParticle = xsdFactory.createXSDParticle()
		val String roleName = xsdExtendedMetaData.getXMLWrapperName(feature);

		// <xsd:element name="//roleXmlNamePlural//" minOccurs="0 | 1" maxOccurs="1">
		var int lowerBound = feature.getLowerBound();
		if(lowerBound >1){
			lowerBound = 1;
		}
		xsdParticle.setMinOccurs(lowerBound);
		xsdParticle.setMaxOccurs(1);

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(roleName); //

		// <xsd:complexType>
		val XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();

		// <xsd:choice minOccurs="//lowerMultiplicity//" maxOccurs="//upperMultiplicity//">
		val XSDParticle xsdParticle2 = XSDFactory.eINSTANCE.createXSDParticle();
		xsdParticle2.setMaxOccurs(feature.getUpperBound());
		xsdParticle2.setMinOccurs(feature.getLowerBound());
		val XSDModelGroup xsdModelGroup2 = XSDFactory.eINSTANCE.createXSDModelGroup();

		// set to "choice"
		xsdModelGroup2.setCompositor(XSDCompositor.CHOICE_LITERAL);

		// create a xsd:element for type
		val XSDParticle xsdParticle3 = XSDFactory.eINSTANCE.createXSDParticle();

		// <xsd:element name="//roleXmlName//">
		val XSDElementDeclaration xsdElement2 = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement2.setName(xsdExtendedMetaData.getXMLName(feature));

		// </xsd:complexType>
		val XSDComplexTypeDefinition xsdComplexTypeDefinition2 = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();
		xsdComplexTypeDefinition2.setDerivationMethod(XSDDerivationMethod.EXTENSION_LITERAL);

		// Create a simple type definition, set its base type to be "EA:REF",
    	// and set it to be the content type of the complex type.
    	// <xsd:simpleContent>
    	// <xsd:extension base="xsd:string">
    	val XSDSimpleTypeDefinition simpleTypeDefinition = xsdFactory.createXSDSimpleTypeDefinition();
    	//xsdComplexTypeDefinition2.setBaseTypeDefinition(xsdSchema.getSchemaForSchema().resolveSimpleTypeDefinition("REF"));
    	val XSDTypeDefinition baseTypeDef = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
    	xsdComplexTypeDefinition2.setBaseTypeDefinition(baseTypeDef);
    	xsdComplexTypeDefinition2.setContent(simpleTypeDefinition);

    	// Create an attribute use to hold the reference, set its use to be optional,
    	// and add it to the complex type's attribute contents
	    val XSDAttributeUse simpleAttributeUse = xsdFactory.createXSDAttributeUse();

	    // Create an attribute reference to simpleAttributeDeclaration in this schema.
    	val XSDAttributeDeclaration xsdAttribute = xsdFactory.createXSDAttributeDeclaration();
    	xsdAttribute.setName(xsdExtendedMetaData.getXMLTypeAttributeName(feature));

		// set attribute type
		val EClassifier baseType = feature.getEType();
		// add to the list of referenced classes
		referencedClass.add(baseType as EClass);
    	val String uri = getElementXSDURI(baseType) + IGeneratorConstants.SUFFIX_SUBTPES_ENUM;
		var XSDTypeDefinition xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(uri);
    	xsdAttribute.setTypeDefinition(xsdTypeDefinition as XSDSimpleTypeDefinition);

    	simpleAttributeUse.setContent(xsdAttribute);
    	simpleAttributeUse.setUse(XSDAttributeUseCategory.OPTIONAL_LITERAL);

    	xsdComplexTypeDefinition2.getAttributeContents().add(simpleAttributeUse);

		xsdElement2.setAnonymousTypeDefinition(xsdComplexTypeDefinition2);
		xsdParticle3.setContent(xsdElement2);
		xsdModelGroup2.getContents().add(xsdParticle3);

		xsdParticle2.setContent(xsdModelGroup2);
		xsdComplexTypeDefinition.setContent(xsdParticle2);
		xsdElement.setAnonymousTypeDefinition(xsdComplexTypeDefinition);
		xsdParticle.setContent(xsdElement);

		return xsdParticle
	}

	/**
	 * create xsd:element for a EReference with rule 5l.EReference_referenced1100Many of RMF
	 * EReference & !containment & upperBound>1 && TTFF (1100) && && typeAttributeName == "xsi:type"
	 *
	 * 5l.EReference_referenced1100Many_XSITypeTrue ::=
	 *	5l:EReference_referenced1100Many_XSITypeTrue ::=
	 *		<xsd:element name="//roleXmlNamePlural//" minOccurs="//lowerMultiplicity//" maxOccurs="1">
     *	  		<xsd:complexType>
	 *				<xsd:choice minOccurs="//lowerMultiplicity//" maxOccurs="//upperMultiplicity//">
     *		   			<xsd:element name="//roleXmlName//" type = "xsd:string"/>
	 *				</xsd:choice>
     *   		</xsd:complexType>
	 *		/xsd:element>
	 */
	def XSDParticle create_EReference_referenced1100Many_5l_XSITypeTrue(
				EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema, ArrayList<EClass> referencedClass) {

		var xsdParticle = xsdFactory.createXSDParticle()
		val String roleName = xsdExtendedMetaData.getXMLWrapperName(feature);

		// <xsd:element name="//roleXmlNamePlural//" minOccurs="0 | 1" maxOccurs="1">
		var int lowerBound = feature.getLowerBound();
		if(lowerBound >1){
			lowerBound = 1;
		}
		xsdParticle.setMinOccurs(lowerBound);
		xsdParticle.setMaxOccurs(1);

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(roleName); //

		// <xsd:complexType>
		val XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();

		// <xsd:choice minOccurs="//lowerMultiplicity//" maxOccurs="//upperMultiplicity//">
		val XSDParticle xsdParticle2 = XSDFactory.eINSTANCE.createXSDParticle();
		xsdParticle2.setMaxOccurs(feature.getUpperBound());
		xsdParticle2.setMinOccurs(feature.getLowerBound());
		val XSDModelGroup xsdModelGroup2 = XSDFactory.eINSTANCE.createXSDModelGroup();

		// set to "choice"
		xsdModelGroup2.setCompositor(XSDCompositor.CHOICE_LITERAL);

		// create a xsd:element for type
		val XSDParticle xsdParticle3 = XSDFactory.eINSTANCE.createXSDParticle();

		// <xsd:element name="//roleXmlName//">
		val XSDElementDeclaration xsdElement2 = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement2.setName(xsdExtendedMetaData.getXMLName(feature));
		val XSDTypeDefinition typeDef = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
		xsdElement2.setTypeDefinition(typeDef);
		xsdParticle3.setContent(xsdElement2);
		xsdModelGroup2.getContents().add(xsdParticle3);

		xsdParticle2.setContent(xsdModelGroup2);
		xsdComplexTypeDefinition.setContent(xsdParticle2);
		xsdElement.setAnonymousTypeDefinition(xsdComplexTypeDefinition);
		xsdParticle.setContent(xsdElement);

		return xsdParticle
	}


	/**
	 * To be overriden by custom
	 *
	 * create xsd:element for a EReference with rule 5m.EReference_referenced0100Many of RMF
	 * EReference & !containment & upperBound>1 && FTFF (0100)
	 *
	 * (1) in case typeAttributeName is not "xsi:type"
	 * 		5m:EReference_referenced0100Many_XSITypeFalse ::=
	 *			<xsd:element name="//roleXmlName//" minOccurs="//lowerMultiplicity//" maxOccurs="//upperMultiplicity//">
     *   			<xsd:complexType>
	 *					<xsd:simpleContent>
     *		  				<xsd:extension base="xsd:string">
	 *							<xsd:attribute name="typeAttributeName" type="//typeXmlNsPrefix : typeXmlName//--SUBTYPES-ENUM" use="optional"/>
     *	     				 </xsd:extension>
	 * 					</xsd:simpleContent>
     *   			</xsd:complexType>
	 *  		</xsd:element>
	 * (2) in case typeAttributeName is "xsi:type":
	 *		5m:EReference_referenced0100Many_XSITypeTrue ::=
	 *			<xsd:element name="roleXmlName" minOccurs="lowerMultiplicity"
	 *						maxOccurs="upperMultiplicity" type="xsd:string"/>
	 */
	def XSDParticle create_EReference_referenced0100Many_5m(EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema, ArrayList<EClass> referencedClass) {

		if(!xsdExtendedMetaData.getXMLTypeAttributeName(feature).equals("xsi:type")){
			return create_EReference_referenced0100Many_5m_XSITypeFalse(feature, eClass, xsdSchema, referencedClass)
		}

		return create_EReference_referenced0100Many_5m_XSITypeTrue(feature, eClass, xsdSchema, referencedClass)
	}


	/**
	 * create xsd:element for a EReference with rule 5n.EReference_referenced0100Single of RMF
	 * EReference & !containment & upperBound=1 && FTFF (0100) && typeAttributeName != "xsi:type"
	 *
	 * 5m:EReference_referenced0100Many_XSITypeFalse ::=
	 *	<xsd:element name="//roleXmlName//" minOccurs="//lowerMultiplicity//" maxOccurs="//upperMultiplicity//">
     *   <xsd:complexType>
	 *		<xsd:simpleContent>
     *		  <xsd:extension base="xsd:string">
	 *			<xsd:attribute name="typeAttributeName" type="//typeXmlNsPrefix : typeXmlName//--SUBTYPES-ENUM" use="optional"/>
     *	      </xsd:extension>
	 * 		</xsd:simpleContent>
     *   </xsd:complexType>
	 *  </xsd:element>
	 */
	def XSDParticle create_EReference_referenced0100Many_5m_XSITypeFalse(
				EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema, ArrayList<EClass> referencedClass) {

		var xsdParticle = xsdFactory.createXSDParticle()
		val String roleName = xsdExtendedMetaData.getXMLName(feature);

		//<xsd:element name="//roleXmlName//" minOccurs="//lowerMultiplicity//" maxOccurs="//upperMultiplicity//">
		xsdParticle.setMinOccurs(feature.getLowerBound());
		xsdParticle.setMaxOccurs(feature.getUpperBound());

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(roleName); //

		// </xsd:complexType>
		val XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();
		xsdComplexTypeDefinition.setDerivationMethod(XSDDerivationMethod.EXTENSION_LITERAL);

		// Create a simple type definition, set its base type to be "xsd:string",
    	// and set it to be the content type of the complex type.
    	// <xsd:simpleContent>
    	// <xsd:extension base="xsd:string">
    	val XSDSimpleTypeDefinition simpleTypeDefinition = xsdFactory.createXSDSimpleTypeDefinition();
    	val XSDTypeDefinition baseTypeDef = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
    	xsdComplexTypeDefinition.setBaseTypeDefinition(baseTypeDef);
    	xsdComplexTypeDefinition.setContent(simpleTypeDefinition);

    	// Create an attribute use to hold the reference, set its use to be optional,
    	// and add it to the complex type's attribute contents
    	//<xsd:attribute name="typeAttributeName" type="//typeXmlNsPrefix : typeXmlName//--SUBTYPES-ENUM" use="optional"/>
	    val XSDAttributeUse simpleAttributeUse = xsdFactory.createXSDAttributeUse();

	    // Create an attribute reference to simpleAttributeDeclaration in this schema.
    	val XSDAttributeDeclaration xsdAttribute = xsdFactory.createXSDAttributeDeclaration();
    	val typeAttributeName = xsdExtendedMetaData.getXMLTypeAttributeName(feature)
    	xsdAttribute.setName(typeAttributeName);

		// set attribute type
		val EClassifier baseType = feature.getEType();
		// add to the list of referenced classes
		referencedClass.add(baseType as EClass);
    	val String uri = getElementXSDURI(baseType) + IGeneratorConstants.SUFFIX_SUBTPES_ENUM;
		var XSDTypeDefinition xsdTypeDefinition = xsdSchema.resolveTypeDefinitionURI(uri);
    	xsdAttribute.setTypeDefinition(xsdTypeDefinition as XSDSimpleTypeDefinition);

    	simpleAttributeUse.setContent(xsdAttribute);
    	simpleAttributeUse.setUse(XSDAttributeUseCategory.OPTIONAL_LITERAL);

    	xsdComplexTypeDefinition.getAttributeContents().add(simpleAttributeUse);
		xsdElement.setAnonymousTypeDefinition(xsdComplexTypeDefinition);
		xsdParticle.setContent(xsdElement);

		return xsdParticle
	}

	/**
	 * create xsd:element for a EReference with rule 5n.EReference_referenced0100Single of RMF
	 * EReference & !containment & upperBound=1 && FTFF (0100) && typeAttributeName == "xsi:type"
	 *
	 * 5m:EReference_referenced0100Many_XSITypeTrue ::=
	 *	<xsd:element name="roleXmlName" minOccurs="lowerMultiplicity"
	 *		maxOccurs="upperMultiplicity" type="xsd:string"/>
	 */
	def XSDParticle create_EReference_referenced0100Many_5m_XSITypeTrue(
				EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema, ArrayList<EClass> referencedClass) {

		var xsdParticle = xsdFactory.createXSDParticle()
		val String roleName = xsdExtendedMetaData.getXMLName(feature);

		//<xsd:element name="//roleXmlName//" minOccurs="//lowerMultiplicity//" maxOccurs="//upperMultiplicity//">
		xsdParticle.setMinOccurs(feature.getLowerBound());
		xsdParticle.setMaxOccurs(feature.getUpperBound());

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(roleName);
		val XSDTypeDefinition typeDef = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
		xsdElement.setTypeDefinition(typeDef);
		xsdParticle.setContent(xsdElement);

		return xsdParticle
	}


	/**
	 * To be overriden by custom
	 *
	 * create xsd:element for a EReference with rule 5n.EReference_referenced0100Single of RMF
	 * EReference & !containment & upperBound=1 && FTFF (0100)
	 * same as 5m:EReference_referenced0100Many
	 *
	 * (1) in case typeAttributeName is not "xsi:type"
	 *			<xsd:element name="//roleXmlName//" minOccurs="//lowerMultiplicity//" maxOccurs="//upperMultiplicity//">
     *   			<xsd:complexType>
	 *					<xsd:simpleContent>
     *		  				<xsd:extension base="xsd:string">
	 *							<xsd:attribute name="typeAttributeName" type="//typeXmlNsPrefix : typeXmlName//--SUBTYPES-ENUM" use="optional"/>
     *	     				 </xsd:extension>
	 * 					</xsd:simpleContent>
     *   			</xsd:complexType>
	 *  		</xsd:element>
	 *
	 * (2) in case typeAttributeName is "xsi:type":
	 *			<xsd:element name="roleXmlName" minOccurs="lowerMultiplicity"
	 *						maxOccurs="upperMultiplicity" type="xsd:string"/>
	 */
	def XSDParticle create_EReference_referenced0100Single_5n(EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema, ArrayList<EClass> referencedClass) {
		return create_EReference_referenced0100Many_5m(feature, eClass, xsdSchema, referencedClass)
	}

	/**
 	* Create xsd:simpleType for a EEnum with rule 6 of RMF
 	* 6. EnumSchema ::=
	*	<xsd:simpleType name=" // Name of Enumeration Class // ">
	*		<xsd:restriction base="xsd:string">"
	*			(<xsd:enumeration value=" // (upper case) Name of Literal from Enumeration Class // '/>)*
	*		</xsd:restriction>
	*	</xsd:simpleType>"
 	*/
	def XSDSimpleTypeDefinition create xsdSimpleTypeDefinition : xsdFactory.createXSDSimpleTypeDefinition() createEnumSchema6(EEnum eEnum, XSDSchema xsdSchema) {

		xsdSimpleTypeDefinition.setName(xsdExtendedMetaData.getXMLName(eEnum));
		// set base type
		val XSDTypeDefinition baseType = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
		xsdSimpleTypeDefinition.setBaseTypeDefinition(baseType as XSDSimpleTypeDefinition);

		// for each Enumliteral, create xsd:enumeration
		eEnum.getELiterals().forEach[
			val XSDEnumerationFacet xsdEnum = xsdFactory.createXSDEnumerationFacet();
			// set value: the upper case of the name of literal
			xsdEnum.setLexicalValue(it.getName().toUpperCase());
			xsdSimpleTypeDefinition.getFacetContents().add(xsdEnum);
		]

		xsdSchema.getContents().add(xsdSimpleTypeDefinition);
	}


	/**
 	* Create xsd:complexType for a EDataType with rule 7a of RMF
 	* 7a. DatatypeSchema::=
	*	<xsd:complexType name=" // Name of Datatype // " >
    *		<xsd:sequence>
	*			<xsd:any namespace=" // Namespace URI of Datatype // "
	*					 processContents=" (skip | lax | strict)"
	*					 minOccurs=" // Minimum // "
	*					 maxOccurs=" // Maximum // "/>
	*			</xsd:any>
    *		</xsd:sequence>
	*	</xsd:complexType>
 	*/
	def XSDComplexTypeDefinition createDataTypeSchema7a(EDataType eDataType, XSDSchema xsdSchema) {
		var xsdComplexTypeDefinition = xsdFactory.createXSDComplexTypeDefinition()

		// xsd:complexType
		xsdComplexTypeDefinition.setName(xsdExtendedMetaData.getXMLName(eDataType));

		// xsd:sequence
		val XSDParticle xsdParticle = XSDFactory.eINSTANCE.createXSDParticle();
		val XSDModelGroup xsdModelGroup = XSDFactory.eINSTANCE.createXSDModelGroup();
		xsdModelGroup.setCompositor(XSDCompositor.SEQUENCE_LITERAL);

		//xsd:any
		val XSDParticle xsdParticle2 = XSDFactory.eINSTANCE.createXSDParticle();
	    val XSDWildcard xsdAny = XSDFactory.eINSTANCE.createXSDWildcard();

	    // set name space "##any": as any well-formed XML that is from a namespace
	    xsdAny.setStringLexicalNamespaceConstraint("##any");
	    xsdAny.setProcessContents(XSDProcessContents.LAX_LITERAL);
	    xsdParticle2.setMinOccurs(0);
	    xsdParticle2.setMaxOccurs(1);

		xsdParticle2.setContent(xsdAny);
		xsdModelGroup.getContents().add(xsdParticle2);
		xsdParticle.setContent(xsdModelGroup);
		xsdComplexTypeDefinition.setContent(xsdParticle);
		xsdSchema.getContents().add(xsdComplexTypeDefinition);

		return xsdComplexTypeDefinition
	}

	/**
 	* Create xsd:simpleType for a EDataType with stereotype "primitive" -- rule 7b of RMF
 	* 7b. DatatypeSimpleType::=
	* 	<xsd:simpleType name="//xmlName//">
	* 		<xsd:restriction base="xsd://xmlXsdSimpleType//">
	*			<xsd:pattern value="//xmlXsdPattern//"/>
	*		</xsd:restriction>
	* 	</xsd:simpleType>
 	*/
	def XSDSimpleTypeDefinition createDataTypeSchema7b(EDataType eDataType, XSDSchema xsdSchema) {
		var xsdSimpleTypeDefinition = xsdFactory.createXSDSimpleTypeDefinition()

		// <xsd:simpleType name="//xmlName//>
		xsdSimpleTypeDefinition.setName(xsdExtendedMetaData.getXMLName(eDataType));

		// <xsd:restriction base="xsd:string">
		val XSDTypeDefinition baseType = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
		xsdSimpleTypeDefinition.setBaseTypeDefinition(baseType as XSDSimpleTypeDefinition);

		// <xsd:pattern value="//xmlXsdPattern//"/>
		val List<String> patternValue = ExtendedMetaData.INSTANCE.getPatternFacet(eDataType)
		if(patternValue != null && !patternValue.isNullOrEmpty()){
			val String pattern = patternValue.get(0)
			val XSDPatternFacet xsdPattern = xsdFactory.createXSDPatternFacet()
			xsdPattern.setLexicalValue(pattern);
			xsdSimpleTypeDefinition.getFacetContents().add(xsdPattern);
		}

		xsdSchema.getContents().add(xsdSimpleTypeDefinition);

		return xsdSimpleTypeDefinition
	}

	/**
 	* Create xsd:simpleType for a EDataType with stereotype "primitive" -- rule 7b of RMF
 	* 7b. DatatypeSimpleType::=
	* 	<xsd:simpleType name="//xmlName//--SIMPLE">
	* 		<xsd:restriction base="xsd:string">
	*			<xsd:pattern value="//xmlXsdPattern//"/>
	*		</xsd:restriction>
	* 	</xsd:simpleType>
 	*/
	def XSDSimpleTypeDefinition createDataTypeSimpleType7b(EDataType eDataType, XSDSchema xsdSchema) {
		var xsdSimpleTypeDefinition = xsdFactory.createXSDSimpleTypeDefinition()

		// <xsd:simpleType name="//xmlName//--SIMPLE">
		xsdSimpleTypeDefinition.setName(xsdExtendedMetaData.getXMLName(eDataType)); // +XSDExtendedMetaDataConstants.SUFFIX_SIMPLE

		// <xsd:restriction base="xsd:string">
		val XSDTypeDefinition baseType = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
		xsdSimpleTypeDefinition.setBaseTypeDefinition(baseType as XSDSimpleTypeDefinition);

		// <xsd:pattern value="//xmlXsdPattern//"/>
		val XSDPatternFacet xsdPattern = xsdFactory.createXSDPatternFacet()
		val List<String> patternValue = ExtendedMetaData.INSTANCE.getPatternFacet(eDataType)
		xsdPattern.setLexicalValue(patternValue.get(0));

		xsdSimpleTypeDefinition.getFacetContents().add(xsdPattern);
		xsdSchema.getContents().add(xsdSimpleTypeDefinition);

		return xsdSimpleTypeDefinition
	}


	/**
 	* Create xsd:simpleType for a EDataType with stereotype "primitive" -- rule 7c of RMF
 	* 7c. DatatypeComplexType::=
	* 	<xsd:complexType name="//xmlName//">
    *     <xsd:simpleContent>
	*		<xsd:extension base="EA: //xmlName//--SIMPLE">
	*		</xsd:extension>
    *    </xsd:simpleContent>
	*	</xsd:complexType>
 	*/
	def XSDComplexTypeDefinition createDataTypeComplexType7c(EDataType eDataType, XSDSchema xsdSchema) {
		var xsdComplexTypeDefinition = xsdFactory.createXSDComplexTypeDefinition()

		// <xsd:complexType name="//xmlName//">
		val String xmlName = xsdExtendedMetaData.getXMLName(eDataType);
		xsdComplexTypeDefinition.setName(xmlName);

  		xsdComplexTypeDefinition.setDerivationMethod(XSDDerivationMethod.EXTENSION_LITERAL);

		// Create a simple type definition, set its base type to be "EA:REF",
    	// and set it to be the content type of the complex type.
    	// <xsd:simpleContent>
    	// <xsd:extension base="EA: //xmlName//--SIMPLE">
    	val XSDSimpleTypeDefinition simpleTypeDefinition = xsdFactory.createXSDSimpleTypeDefinition();
    	val String typeURI = defaultUserSchemaNamespace + "#" + xmlName + IGeneratorConstants.SUFFIX_SIMPLE;
    	val XSDTypeDefinition baseTypeDef = xsdSchema.resolveTypeDefinitionURI(typeURI);

    	xsdComplexTypeDefinition.setBaseTypeDefinition(baseTypeDef);
    	xsdComplexTypeDefinition.setContent(simpleTypeDefinition);

		xsdSchema.getContents().add(xsdComplexTypeDefinition);

		return xsdComplexTypeDefinition
	}

	/**
 	* Create xsd:simpleType for a referenced EClass with rule 8 of RMF
 	* 8. ReferencedSimpleType ::=
	*	<xsd:simpleType name="//xmlName//--SUBTYPES-ENUM">
    *	   <xsd:restriction base="xsd:string">
 	*		(<xsd:enumeration value="//(sub) typeXmlName//"/>)+
    *      </xsd:restriction>
	*	</xsd:simpleType>
 	*/
	def XSDSimpleTypeDefinition create xsdSimpleTypeDefinition : xsdFactory.createXSDSimpleTypeDefinition() createSimpleType8(EClass eClass, XSDSchema xsdSchema) {

		// <xsd:simpleType name="//xmlName//--SUBTYPES-ENUM">
		xsdSimpleTypeDefinition.setName(xsdExtendedMetaData.getXMLName(eClass) + IGeneratorConstants.SUFFIX_SUBTPES_ENUM);

		// <xsd:restriction base="xsd:string">
		val XSDTypeDefinition baseType = xsdSchema.resolveTypeDefinitionURI(defaultXMLSchemaNamespace+"#string");
		xsdSimpleTypeDefinition.setBaseTypeDefinition(baseType as XSDSimpleTypeDefinition);

		// <xsd:enumeration value="//typeXmlName//"/>
		val XSDEnumerationFacet xsdEnumFacet = XSDFactory.eINSTANCE.createXSDEnumerationFacet();
		xsdEnumFacet.setLexicalValue(xsdExtendedMetaData.getXMLName(eClass));

		xsdSimpleTypeDefinition.getFacetContents.add(xsdEnumFacet);

		// (<xsd:enumeration value="//subTypeXmlName//"/>)+
		// for each subtype, create a xsd:enumeration
		Ecore2XSDUtil::findESubTypesOf(eClass).forEach[
			val XSDEnumerationFacet xsdEnum = XSDFactory.eINSTANCE.createXSDEnumerationFacet();
			xsdEnum.setLexicalValue(xsdExtendedMetaData.getXMLName(it));

			xsdSimpleTypeDefinition.getFacetContents.add(xsdEnum);
		]

		xsdSchema.getContents().add(xsdSimpleTypeDefinition);
	}

	def XSDParticle generateXSDParticleForFeature(EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema) {

			val int xmlPersistenceStrategy = xsdExtendedMetaData.getXMLPersistenceMappingStrategy(feature);
			var XSDParticle xsdParticle = null;

			// case APRXML0010
			if (XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT == xmlPersistenceStrategy) {
				xsdParticle = createXSDParticleForFeatureCase0010(feature, eClass);
				patternCaseSet.add("0010");
			}
			// case APRXML0012
			else if (XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT == xmlPersistenceStrategy) {
				xsdParticle = createXSDParticleForFeatureCase0012(feature, eClass);
				patternCaseSet.add("0012");
			}
			// case APRXML0013
			else if (XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT == xmlPersistenceStrategy) {
				xsdParticle = createXSDParticleForFeatureCase0013(feature, eClass, xsdSchema);
				patternCaseSet.add("0013");
			}
			// case APRXML0023
			else if (XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT == xmlPersistenceStrategy) {
				xsdParticle = createXSDParticleForFeatureCase0023(feature, eClass, xsdSchema);
				patternCaseSet.add("0023");
			}
			// case (1000): take the rule of APRXML0010
			else if (XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1000__FEATURE_WRAPPER_ELEMENT == xmlPersistenceStrategy) {
			//MBR we should raise an error instead of falling back to a default behaviour
				xsdParticle = createXSDParticleForFeatureCase0010(feature, eClass);
				patternCaseSet.add("case not allowed, handled as pattern 0010 (the same way in EAST-ADL)");
			}
			else {
				xsdParticle = createXSDParticleForFeatureCase0012(feature, eClass);
				patternCaseSet.add("unsupported_mapping_strategy" + xmlPersistenceStrategy);
				System.out.print("\n eClass = " + eClass.getName() + " feature = " + feature.getName() );
			}

			return xsdParticle;
		}


	// case 0010 (ReqIF 5c)
	def XSDParticle create xsdParticle : xsdFactory.createXSDParticle() createXSDParticleForFeatureCase0010(
				EStructuralFeature feature, EClass eClass) {

		val String roleName = xsdExtendedMetaData.getXMLWrapperName(feature);
		// xsdExtendedMetaData.getXMLName(feature); for ReqIF 5c

		// <xsd:element minOccurs="0" name="ROLE-B-1S"/>
		if(feature.getLowerBound()>0){
			xsdParticle.setMinOccurs(1);
		}
		else{
			xsdParticle.setMinOccurs(0);
		}
		xsdParticle.setMaxOccurs(1);

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(roleName); //

		// <xsd:complexType>
		val XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();

		// <xsd:choice maxOccurs="unbounded" minOccurs="0">
		val XSDParticle xsdParticle2 = XSDFactory.eINSTANCE.createXSDParticle();
		xsdParticle2.setMaxOccurs(1);
		xsdParticle2.setMinOccurs(0);
		val XSDModelGroup xsdModelGroup2 = XSDFactory.eINSTANCE.createXSDModelGroup();

		// set to "choice"
		xsdModelGroup2.setCompositor(XSDCompositor.CHOICE_LITERAL);

		// get the type
		val EClassifier typeEClassifier = feature.getEType();

		// <xsd:element name="CLASS-B" type="sky:CLASS-B"/>
		val XSDParticle xsdParticle3 = XSDFactory.eINSTANCE.createXSDParticle();
		val XSDElementDeclaration xsdElement2 = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement2.setName(xsdExtendedMetaData.getName(typeEClassifier));
		val String typeeClassifierURI = getElementXSDURI(typeEClassifier);
		val XSDTypeDefinition xsdTypeDefinition = xsdElement2.resolveTypeDefinitionURI(typeeClassifierURI);
		xsdElement2.setTypeDefinition(xsdTypeDefinition);

		xsdParticle3.setContent(xsdElement2);
		xsdModelGroup2.getContents().add(xsdParticle3);

		// for each subtype, create a xsd:element
		if(typeEClassifier instanceof EClass){
			Ecore2XSDUtil::findESubTypesOf(typeEClassifier).forEach[
				val XSDParticle xsdParticleSubType = XSDFactory.eINSTANCE.createXSDParticle();
				val XSDElementDeclaration xsdElementSubType = XSDFactory.eINSTANCE.createXSDElementDeclaration();
				xsdElementSubType.setName(xsdExtendedMetaData.getXMLName(it));
				val String subTypeURI = getElementXSDURI(it);
				val XSDTypeDefinition xsdTypeDefinitionSubType = xsdElementSubType.resolveTypeDefinitionURI(subTypeURI);
				xsdElementSubType.setTypeDefinition(xsdTypeDefinitionSubType);

				xsdParticleSubType.setContent(xsdElementSubType);
				xsdModelGroup2.getContents().add(xsdParticleSubType);
			]
		}

		xsdParticle2.setContent(xsdModelGroup2);
		xsdComplexTypeDefinition.setContent(xsdParticle2);
		xsdElement.setAnonymousTypeDefinition(xsdComplexTypeDefinition);
		xsdParticle.setContent(xsdElement);
	}

	// case 0012
	def XSDParticle create xsdParticle : xsdFactory.createXSDParticle() createXSDParticleForFeatureCase0012(
				EStructuralFeature feature, EClass eClass) {

		val String roleName = xsdExtendedMetaData.getXMLName(feature);

		// <xsd:element minOccurs="0" name="ROLE-B-1S"/>
		xsdParticle.setMinOccurs(0);

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(roleName); //

		// <xsd:complexType>
		val XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();

		// <xsd:choice maxOccurs="unbounded" minOccurs="0">
		val XSDParticle xsdParticle2 = XSDFactory.eINSTANCE.createXSDParticle();
		xsdParticle2.setMaxOccurs(1);
		xsdParticle2.setMinOccurs(0);
		val XSDModelGroup xsdModelGroup2 = XSDFactory.eINSTANCE.createXSDModelGroup();

		// set to "choice"
		xsdModelGroup2.setCompositor(XSDCompositor.CHOICE_LITERAL);

		// get the type
		val EClassifier typeEClassifier = feature.getEType();

		// <xsd:element name="CLASS-B" type="sky:CLASS-B"/>
		val XSDParticle xsdParticle3 = XSDFactory.eINSTANCE.createXSDParticle();
		val XSDElementDeclaration xsdElement2 = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement2.setName(xsdExtendedMetaData.getXMLName(typeEClassifier));
		val String typeeClassifierURI = getElementXSDURI(typeEClassifier);
		val XSDTypeDefinition xsdTypeDefinition = xsdElement2.resolveTypeDefinitionURI(typeeClassifierURI);
		xsdElement2.setTypeDefinition(xsdTypeDefinition);

		xsdParticle3.setContent(xsdElement2);
		xsdModelGroup2.getContents().add(xsdParticle3);

		// for each subtype, create a xsd:element
		if(typeEClassifier instanceof EClass){
			Ecore2XSDUtil::findESubTypesOf(typeEClassifier).forEach[
				val XSDParticle xsdParticleSubType = XSDFactory.eINSTANCE.createXSDParticle();
				val XSDElementDeclaration xsdElementSubType = XSDFactory.eINSTANCE.createXSDElementDeclaration();
				xsdElementSubType.setName(xsdExtendedMetaData.getName(it));
				val String subTypeURI = getElementXSDURI(it);
				val XSDTypeDefinition xsdTypeDefinitionSubType = xsdElementSubType.resolveTypeDefinitionURI(subTypeURI);
				xsdElementSubType.setTypeDefinition(xsdTypeDefinitionSubType);

				xsdParticleSubType.setContent(xsdElementSubType);
				xsdModelGroup2.getContents().add(xsdParticleSubType);
			]
		}

		xsdParticle2.setContent(xsdModelGroup2);
		xsdComplexTypeDefinition.setContent(xsdParticle2);
		xsdElement.setAnonymousTypeDefinition(xsdComplexTypeDefinition);
		xsdParticle.setContent(xsdElement);
	}

	// case 0013
	def XSDParticle create xsdParticle : xsdFactory.createXSDParticle() createXSDParticleForFeatureCase0013(
				EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema) {

		val String roleName = xsdExtendedMetaData.getXMLName(feature);

		// <xsd:element minOccurs="0" name="ROLE-B-1"/>
		xsdParticle.setMinOccurs(0);

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(roleName); //

		// <xsd:complexType>
		val XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();

		// <xsd:choice maxOccurs="unbounded" minOccurs="0">
		val XSDParticle xsdParticle2 = XSDFactory.eINSTANCE.createXSDParticle();
		xsdParticle2.setMaxOccurs(1);
		xsdParticle2.setMinOccurs(0);
		val XSDModelGroup xsdModelGroup2 = XSDFactory.eINSTANCE.createXSDModelGroup();

		// set to "choice"
		xsdModelGroup2.setCompositor(XSDCompositor.CHOICE_LITERAL);

		// get the type
		val EClassifier typeEClassifier = feature.getEType();

		if(typeEClassifier instanceof EClass){
			// create a xsd:group for the type
			val XSDParticle xsdParticle3 = XSDFactory.eINSTANCE.createXSDParticle();
			val XSDModelGroupDefinition xsdModelGroupdefinitionRef = XSDFactory.eINSTANCE.createXSDModelGroupDefinition();

			val String refGroupURI = getElementXSDURI(typeEClassifier);
			val XSDModelGroupDefinition xsdModelGroupDef = xsdModelGroupdefinitionRef.resolveModelGroupDefinitionURI(refGroupURI);
			// resolve group ref
			xsdModelGroupdefinitionRef.setResolvedModelGroupDefinition(xsdModelGroupDef);
			xsdParticle3.setContent(xsdModelGroupdefinitionRef);

			xsdParticle3.setContent(xsdModelGroupdefinitionRef);
			xsdModelGroup2.getContents().add(xsdParticle3);

			// for each subtype, create a xsd:group
			Ecore2XSDUtil::findESubTypesOf(typeEClassifier).forEach[
				val XSDParticle xsdParticleSubType = XSDFactory.eINSTANCE.createXSDParticle();
				val XSDModelGroupDefinition xsdModelGroupdefSubTypeRef = XSDFactory.eINSTANCE.createXSDModelGroupDefinition();
				val String subTypeURI = getElementXSDURI(it);
				val XSDModelGroupDefinition subTypeModelGroupDef = xsdModelGroupdefSubTypeRef.resolveModelGroupDefinitionURI(subTypeURI);
				// resolve group ref
				xsdModelGroupdefSubTypeRef.setResolvedModelGroupDefinition(subTypeModelGroupDef);
				xsdParticleSubType.setContent(xsdModelGroupdefSubTypeRef);
				xsdModelGroup2.getContents().add(xsdParticleSubType);
			]
		}

		xsdParticle2.setContent(xsdModelGroup2);
		xsdComplexTypeDefinition.setContent(xsdParticle2);
		xsdElement.setAnonymousTypeDefinition(xsdComplexTypeDefinition);
		xsdParticle.setContent(xsdElement);
	}

	// case 0023
	def XSDParticle create xsdParticle : xsdFactory.createXSDParticle() createXSDParticleForFeatureCase0023(
				EStructuralFeature feature, EClass eClass, XSDSchema xsdSchema) {

		val String roleName = xsdExtendedMetaData.getXMLWrapperName(feature);

		// <xsd:element minOccurs="0" name="ROLE-B-1"/>
		xsdParticle.setMinOccurs(0);

		val XSDElementDeclaration xsdElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
		xsdElement.setName(roleName); //

		// <xsd:complexType>
		val XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();

		// <xsd:choice maxOccurs="unbounded" minOccurs="0">
		val XSDParticle xsdParticle2 = XSDFactory.eINSTANCE.createXSDParticle();
		xsdParticle2.setMaxOccurs(1);
		xsdParticle2.setMinOccurs(0);
		val XSDModelGroup xsdModelGroup2 = XSDFactory.eINSTANCE.createXSDModelGroup();

		// set to "choice"
		xsdModelGroup2.setCompositor(XSDCompositor.CHOICE_LITERAL);

		// get the type
		val EClassifier typeEClassifier = feature.getEType();

		if(typeEClassifier instanceof EClass){
			// create a xsd:element for type
			val XSDParticle xsdParticle3 = XSDFactory.eINSTANCE.createXSDParticle();

			val XSDElementDeclaration xsdElement2 = XSDFactory.eINSTANCE.createXSDElementDeclaration();
			xsdElement2.setName(xsdExtendedMetaData.getXMLWrapperName(typeEClassifier)); // "ROLE-B-1"

			// <xsd:complexType>
			val XSDComplexTypeDefinition xsdComplexTypeDefinition2 = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();

			// <xsd:choice maxOccurs="1" minOccurs="0">
			val XSDParticle xsdParticle4 = XSDFactory.eINSTANCE.createXSDParticle();
			xsdParticle4.setMaxOccurs(1);
			xsdParticle4.setMinOccurs(0);
			val XSDModelGroup xsdModelGroup3 = XSDFactory.eINSTANCE.createXSDModelGroup();

			// set to "choice"
			xsdModelGroup3.setCompositor(XSDCompositor.CHOICE_LITERAL);

			// <xsd:group ref="sky:CLASS-B"/>
			val XSDParticle xsdParticle5 = XSDFactory.eINSTANCE.createXSDParticle();
			val XSDModelGroupDefinition xsdModelGroupdefinitionRef = XSDFactory.eINSTANCE.createXSDModelGroupDefinition();
			val String typeURI = getElementXSDURI(typeEClassifier);
			val XSDModelGroupDefinition typeModelGroupDef = xsdModelGroupdefinitionRef.resolveModelGroupDefinitionURI(typeURI);
			// resolve group ref
			xsdModelGroupdefinitionRef.setResolvedModelGroupDefinition(typeModelGroupDef);

			xsdParticle5.setContent(xsdModelGroupdefinitionRef);

			// for each subtype, create a xsd:element
			Ecore2XSDUtil::findESubTypesOf(typeEClassifier).forEach[
				// <xsd:element name="ROLE-B-1">
				val XSDParticle subTypeXSDParticle = XSDFactory.eINSTANCE.createXSDParticle();

				val XSDElementDeclaration subTypeXSDElement = XSDFactory.eINSTANCE.createXSDElementDeclaration();
				subTypeXSDElement.setName(xsdExtendedMetaData.getXMLWrapperName(it)); // "ROLE-B-1"

				// <xsd:complexType>
				val XSDComplexTypeDefinition subTypeXSDComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();

				// <xsd:choice maxOccurs="1" minOccurs="0">
				val XSDParticle xsdParticle6 = XSDFactory.eINSTANCE.createXSDParticle();
				xsdParticle6.setMaxOccurs(1);
				xsdParticle6.setMinOccurs(0);
				val XSDModelGroup xsdModelGroup4 = XSDFactory.eINSTANCE.createXSDModelGroup();

				// set to "choice"
				xsdModelGroup4.setCompositor(XSDCompositor.CHOICE_LITERAL);

				// <xsd:group ref="sky:CLASS-B"/>
				val XSDParticle xsdParticle7 = XSDFactory.eINSTANCE.createXSDParticle();
				val XSDModelGroupDefinition xsdModelGroupdefinitionRef2 = XSDFactory.eINSTANCE.createXSDModelGroupDefinition();
				val String subTypeURI = getElementXSDURI(it);
				val XSDModelGroupDefinition subTypeModelGroupDef = xsdModelGroupdefinitionRef2.resolveModelGroupDefinitionURI(subTypeURI);
				// resolve group ref
				xsdModelGroupdefinitionRef2.setResolvedModelGroupDefinition(subTypeModelGroupDef);
				xsdParticle7.setContent(xsdModelGroupdefinitionRef2);

				xsdModelGroup4.getContents().add(xsdParticle7);
				xsdParticle6.setContent(xsdModelGroup4);
				subTypeXSDComplexTypeDefinition.setContent(xsdParticle6);
				subTypeXSDElement.setAnonymousTypeDefinition(subTypeXSDComplexTypeDefinition);
				subTypeXSDParticle.setContent(subTypeXSDElement);
				xsdModelGroup2.getContents().add(subTypeXSDParticle);
			]

			xsdModelGroup3.getContents().add(xsdParticle5);
			xsdParticle4.setContent(xsdModelGroup3);
			xsdComplexTypeDefinition2.setContent(xsdParticle4);
			xsdElement2.setAnonymousTypeDefinition(xsdComplexTypeDefinition2);
			xsdParticle3.setContent(xsdElement2);
			xsdModelGroup2.getContents().add(xsdParticle3);
		}

		xsdParticle2.setContent(xsdModelGroup2);
		xsdComplexTypeDefinition.setContent(xsdParticle2);
		xsdElement.setAnonymousTypeDefinition(xsdComplexTypeDefinition);
		xsdParticle.setContent(xsdElement);
	}


	//====================================
	/**
	 * to be override
	 */
	public def Boolean isXMLPrimitiveXsdType(EDataType dataType) {

		val Boolean isPrimitive = true;
		val Boolean isXsdTypeDefined = xsdExtendedMetaData.getXMLXsdSimpleType(dataType) != IGeneratorConstants.UNINITIALIZED_STRING;
		val Boolean isCustomSimpleTypeDefined = IGeneratorConstants.BOOLEAN_TRUE.equals(xsdExtendedMetaData.getXMLCustomSimpleType(dataType));
		var Boolean isPatternDefined = false;
		val List<String> patterns = ExtendedMetaData.INSTANCE.getPatternFacet(dataType);
		if (!patterns.isEmpty()) {
			isPatternDefined = patterns.get(0) != null;
		}

		// if is primitive, xsdType is defined, customType (custom simple type) is not defined, and pattern is not
		// defined
		if (isPrimitive && isXsdTypeDefined && !isCustomSimpleTypeDefined && !isPatternDefined) {
			return true;
		}

		return false;
	}

	protected def String getXsdSimpleType(EClassifier typeeClassifier){
		var String xsdSimpleType = xsdExtendedMetaData.getXMLXsdSimpleType(typeeClassifier)
		if(xsdSimpleType == null){
			val Class<?> instanceClass = typeeClassifier.getInstanceClass()
			xsdSimpleType = JavaXSDPrimitiveTypeMapping.javaXsdPrimitiveTypeMapping.get(instanceClass)
		}

		return xsdSimpleType
	}



}

