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
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.OperationCanceledException
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EDataType
import org.eclipse.emf.ecore.EEnum
import org.eclipse.emf.ecore.EPackage
import org.eclipse.xsd.XSDSchema
import org.eclipse.sphinx.emf.serialization.generators.util.JavaXSDPrimitiveTypeMapping

class Ecore2XSDConverter {
 
	protected Ecore2XSDFactory xsdFactory
	protected XSDSchema xsdSchema 
	protected ArrayList<EClass> referencedClass = new ArrayList<EClass>();

	new(Ecore2XSDFactory xsdFactory, XSDSchema xsdSchema){
		this.xsdFactory = xsdFactory;
		this.xsdSchema = xsdSchema;
	}
 	 
	/**
	 * convert Package schema
	 */
	def XSDSchema doConvertRMFPackageSchema2(EPackage rootEPackageModel, IProgressMonitor monitor){
		val SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}
		
		progress.subTask("create XSD global element");
		// REQIF: 3 global element
		xsdFactory.createGlobalElement3(rootEPackageModel, xsdSchema);
		progress.worked(5);
		
		// REQIF: 4 FixedRefTypes
		//xsdFactory.createFixedRefTypes4(xsdSchema);
		
		// REQIF: (5 classTypeDef)*
		val SubMonitor classProgress = progress.newChild(70);
		classProgress.subTask("create XSD class type definitions");
		doConvertToRMFClassTypeDef5(rootEPackageModel, classProgress);
		
		// REQIF: (6 EnumSchema)*
		val SubMonitor enumProgress = progress.newChild(10);
		enumProgress.subTask("create XSD Enum schema");
		doConvertToRMFEnumSchema6(rootEPackageModel, enumProgress);
		
		// REQIF: (7 TypeSchema)*
		val SubMonitor dtProgress = progress.newChild(10);
		dtProgress.subTask("create XSD Data type schema");
		doConvertToRMFDataTypeSchema7(rootEPackageModel, dtProgress);
		
		// REQIF: (8 ReferencedSimpleType)*
		val SubMonitor rstProgress = progress.newChild(5);
		rstProgress.subTask("create referenced simple types");
		doConvertToReferencedSimpleType8(rootEPackageModel, rstProgress);
		
		return xsdSchema;
	}
	
	/**
	 * convert to Class type def
	 */
	def void doConvertToRMFClassTypeDef5(EPackage rootEPackageModel, IProgressMonitor monitor){
		val SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}
		
		// for each class: ClassTypeDef
		val SubMonitor classProgress = progress.newChild(50).setWorkRemaining(rootEPackageModel.getEClassifiers().size);	
		rootEPackageModel.getEClassifiers().filter[it instanceof EClass].forEach[
			xsdFactory.createClassTypeDefinition5(it as EClass, xsdSchema, referencedClass, classProgress);
		]
		
		val SubMonitor subProgress = progress.newChild(50).setWorkRemaining(rootEPackageModel.getESubpackages().size);		
		// for each sub package
		rootEPackageModel.getESubpackages().forEach[
			doConvertToRMFClassTypeDef5(it, subProgress)
			
			subProgress.worked(1);
			if (subProgress.isCanceled()) {
				throw new OperationCanceledException();
			}
		];
	}
	
	/**
	 * convert to EEnum schema
	 */
	def void doConvertToRMFEnumSchema6(EPackage rootEPackageModel, IProgressMonitor monitor){
		val SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}
		
		// for each class: ClassTypeDef
		val SubMonitor classProgress = progress.newChild(50).setWorkRemaining(rootEPackageModel.getEClassifiers().size);	
		rootEPackageModel.getEClassifiers().filter[it instanceof EEnum].forEach[
			xsdFactory.createEnumSchema6(it as EEnum, xsdSchema);
			
			classProgress.worked(1);
			if (classProgress.isCanceled()) {
				throw new OperationCanceledException();
			}
		]
		
		// for each sub package
		val SubMonitor subProgress = progress.newChild(50).setWorkRemaining(rootEPackageModel.getESubpackages().size);		
		rootEPackageModel.getESubpackages().forEach[
			doConvertToRMFEnumSchema6(it, subProgress);
			
			subProgress.worked(1);
			if (subProgress.isCanceled()) {
				throw new OperationCanceledException();
			}
		];
	}
	
	/**
	 * convert to EDataType schema
	 */
	def void doConvertToRMFDataTypeSchema7(EPackage rootEPackageModel, IProgressMonitor monitor){
		val SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}
		
		// for each class: ClassTypeDef
		val SubMonitor classProgress = progress.newChild(50).setWorkRemaining(rootEPackageModel.getEClassifiers().size);	
		rootEPackageModel.getEClassifiers().filter[(it instanceof EDataType) && !(it instanceof EEnum)].forEach[	
			if(isGeneralPrimitiveType(it as EDataType)){
				if(!xsdFactory.isXMLPrimitiveXsdType(it as EDataType)){				
					xsdFactory.createDataTypeSchema7b(it as EDataType, xsdSchema);
				}
				}
			else{				
				xsdFactory.createDataTypeSchema7a(it as EDataType, xsdSchema);
			}
				
				classProgress.worked(1);
				if (classProgress.isCanceled()) {
					throw new OperationCanceledException();
				}
		]
		  
		// for each sub package
		val SubMonitor subProgress = progress.newChild(50).setWorkRemaining(rootEPackageModel.getEClassifiers().size);	
		rootEPackageModel.getESubpackages().forEach[
			doConvertToRMFDataTypeSchema7(it, subProgress);
			
			subProgress.worked(1);
			if (subProgress.isCanceled()) {
				throw new OperationCanceledException();
			}
		];
	}
	
	/**
	 * convert EDataType
	 */
	def void doConvertToReferencedSimpleType8(EPackage rootEPackageModel, IProgressMonitor monitor){
		val SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}
		 
		// for each referenced class
		val SubMonitor classProgress = progress.newChild(50).setWorkRemaining(rootEPackageModel.getEClassifiers().size);	
		//referencedClass.forEach[xsdFactory.createSimpleType8(it, xsdSchema);]
		rootEPackageModel.getEClassifiers().filter[it instanceof EClass].forEach[
			xsdFactory.createSimpleType8(it as EClass, xsdSchema);
			
			classProgress.worked(1);
			if (classProgress.isCanceled()) {
				throw new OperationCanceledException();
			}
		]
		
		val SubMonitor subProgress = progress.newChild(50).setWorkRemaining(rootEPackageModel.getEClassifiers().size);	
		rootEPackageModel.getESubpackages().forEach[
			doConvertToReferencedSimpleType8(it, subProgress);
			
			subProgress.worked(1);
			if (subProgress.isCanceled()) {
				throw new OperationCanceledException();
			}
		];
	}
	
	// Returns true only if the mapped xsd type of the instance class is in the xsd primitive type set
	protected def Boolean isGeneralPrimitiveType(EDataType dataType){
		val Class<?> instanceClass = dataType.getInstanceClass()
		if(JavaXSDPrimitiveTypeMapping.javaXsdPrimitiveTypeMapping.get(instanceClass) != null){
			return true
		}
		return false
	}
}