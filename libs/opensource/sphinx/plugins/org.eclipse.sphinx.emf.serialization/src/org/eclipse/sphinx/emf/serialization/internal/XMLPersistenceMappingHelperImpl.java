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
package org.eclipse.sphinx.emf.serialization.internal;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLHelperImpl;
import org.eclipse.emf.ecore.xml.type.XMLTypeFactory;

public class XMLPersistenceMappingHelperImpl extends XMLHelperImpl implements XMLHelper {

	public XMLPersistenceMappingHelperImpl(XMLResource resource) {
		super(resource);
		EMap<String, String> prefixToNamespaceMap = getPrefixToNamespaceMap();
		//prefixToNamespaceMap.put("", ReqIF10Package.eNS_URI); //$NON-NLS-1$
		setPrefixToNamespaceMap(prefixToNamespaceMap);
	}

	@Override
	public String getHREF(EObject obj) {
		if (obj.eIsProxy()) {
			URI uri = ((InternalEObject) obj).eProxyURI();
			return uri.fragment();
		} else {
			return getIDREF(obj);
		}
	}

	@Override
	public String convertToString(EFactory factory, EDataType dataType, Object value) {
		if (value instanceof XMLGregorianCalendar) {
			return XMLTypeFactory.eINSTANCE.convertDateTime((XMLGregorianCalendar) value);
		} else {
			return super.convertToString(factory, dataType, value);
		}
	}

	@Override
	public EClassifier getType(EFactory eFactory, String typeName) {
		EClassifier eClassifier;
		EPackage ePackage = eFactory.getEPackage();
		if (extendedMetaData.demandedPackages().contains(ePackage)) {
			eClassifier = extendedMetaData.demandType(ePackage.getNsURI(), typeName);
		} else {
			eClassifier = super.getType(eFactory, typeName);
		}
		return eClassifier;

	}
}
