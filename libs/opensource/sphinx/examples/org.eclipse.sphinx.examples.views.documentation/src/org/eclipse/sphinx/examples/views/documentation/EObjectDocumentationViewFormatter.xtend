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
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.views.documentation 

import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.sphinx.platform.ui.views.documentation.AbstractDocumentationViewFormatter

class EObjectDocumentationViewFormatter extends AbstractDocumentationViewFormatter {
	
	override formatHeader(Object object) {
		'''Documentation'''
	}

	override format(Object object) '''
		«formatSpecific(object)»
	'''

	def dispatch formatSpecific(EObject eObject) '''
		
		<p>
		  		«EcoreUtil.getDocumentation(eObject.eClass)»
		</p>
		
		<table class="table">
			<tr>
				<th>Feature</th>
				<th>Documentation</th>
			</tr>
		  	«FOR eStructuralFeature : eObject.eClass.EAllStructuralFeatures»
		  	<tr>
		  		<td>«eStructuralFeature.name»</td>
		  		<td>«EcoreUtil.getDocumentation(eStructuralFeature)»</td>
		  	</tr>	
		  	«ENDFOR» 	
		</table>    	
	  	'''
	  	
	def dispatch formatSpecific(Object object) '''«object»'''
}
