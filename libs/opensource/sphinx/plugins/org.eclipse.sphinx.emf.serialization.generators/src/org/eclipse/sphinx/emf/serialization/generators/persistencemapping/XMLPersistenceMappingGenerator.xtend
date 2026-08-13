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
 package org.eclipse.sphinx.emf.serialization.generators.persistencemapping
  
import java.util.HashSet
import java.util.Set
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.sphinx.emf.serialization.XMLPersistenceMappingExtendedMetaData
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.core.runtime.OperationCanceledException
   
class XMLPersistenceMappingGenerator {
 
	public EPackage rootModel;
	public Set<EPackage> referencedEcores= new HashSet<EPackage>();
	public Set<EPackage> referencedEPackagesWithMetaData= new HashSet<EPackage>();
	/**
   * The extended meta data used to determine the schema structure.
   */
	protected XMLPersistenceMappingExtendedMetaData xsdExtendedMetaData = XMLPersistenceMappingExtendedMetaData::INSTANCE;
	
	new(EPackage rootModel) {
		this.rootModel = rootModel;
	}  
	 
	def public EObject execute(IProgressMonitor monitor) {
		monitor.beginTask("XML persistence mapping generatering ...", 100);
		monitor.subTask("XML persistence mapping generatering ...");
		
		val SubMonitor progress = SubMonitor.convert(monitor, 100);
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
				
		// Configure Default XSD Extended MetaData
		val CreateDefaultXSDExtendedMetaData createDefaultXSDExtendedMetaData = createDefaultXSDExtendedMetaData();
		rootModel = createDefaultXSDExtendedMetaData.execute(progress.newChild(100));
	} 
	
	// This method can be overriden by custom to provide the custom simple type and the pattern
	def protected CreateDefaultXSDExtendedMetaData createDefaultXSDExtendedMetaData(){
		// general case, no global eClass by default
		return new CreateDefaultXSDExtendedMetaData(rootModel, "NO GLOBAL ECLASS");
	}

}