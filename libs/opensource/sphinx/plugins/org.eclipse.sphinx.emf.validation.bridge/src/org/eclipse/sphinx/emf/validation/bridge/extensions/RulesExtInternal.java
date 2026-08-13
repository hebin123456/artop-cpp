/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.validation.bridge.extensions;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.sphinx.emf.validation.bridge.Activator;
import org.eclipse.sphinx.emf.validation.bridge.util.Messages;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * internal class to store contribution to ExtensionPoint
 */
public class RulesExtInternal {

	private String modelId;
	private String marker;
	private URI nsURI;
	private EClass rootModelClass = null;
	private String rootModelEClassifierName = null;
	private String rootModelObjectName;
	private String filter;
	private Object validatorAdapter = null;

	/**
	 * accessor
	 * 
	 * @return model Id
	 */
	public String getModelId() {
		return modelId;
	}

	/**
	 * writer
	 * 
	 * @param str
	 */
	public void setModelID(String str) {
		modelId = str;
	}

	/**
	 * accessor
	 * 
	 * @return the filter for rules of this model
	 */
	public String getFilter() {
		return filter;
	}

	/**
	 * writer
	 * 
	 * @param str
	 */
	public void setFilter(String str) {
		filter = str;
	}

	/**
	 * accessor
	 * 
	 * @return marker used for this model
	 */
	public String getMarker() {
		return marker;
	}

	/**
	 * writer
	 * 
	 * @param str
	 */
	public void setMarker(String str) {
		marker = str;
	}

	/**
	 * accessor
	 * 
	 * @return the model URI namespace
	 */
	public URI getNsURI() {
		return nsURI;
	}

	/**
	 * writer
	 * 
	 * @param uri
	 */
	public void setNsURI(URI uri) {
		nsURI = uri;
	}

	/**
	 * accessor
	 * 
	 * @return the "root" object of the model
	 */

	public String getRootModelEClassifierName() {
		return rootModelEClassifierName;
	}

	/**
	 * writer
	 * 
	 * @param rootModelEClassifierName
	 */
	public void setRootModelEClassifierName(String rootModelEClassifierName) {
		this.rootModelEClassifierName = rootModelEClassifierName;
	}

	/**
	 * accessor
	 * 
	 * @return the "root" object of the model
	 */
	public EClass getRootModelClass() {

		if (rootModelClass != null) {
			return rootModelClass;
		}

		EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(nsURI.toString());
		EClassifier eclassifier = ePackage.getEClassifier(rootModelEClassifierName);

		// Log info about possible problem
		if (eclassifier == null) {
			PlatformLogUtil.logAsWarning(Activator.getDefault(), Messages.errWrongClassifier);
		}

		return (EClass) eclassifier;
	}

	/**
	 * accessor
	 * 
	 * @return the full pathed name of the "root" object.
	 */
	public String getRootModelObjectName() {
		return rootModelObjectName;
	}

	/**
	 * writer
	 * 
	 * @param value
	 */
	public void setRootModelObjectName(String value) {
		rootModelObjectName = value;
	}

	/**
	 * accessor
	 * 
	 * @return the instance of the validator adapter class
	 */
	public Object getValidatorAdapter() {
		return validatorAdapter;
	}

	/**
	 * writer
	 * 
	 * @param validatorAdapter
	 */
	public void setValidatorAdapter(Object validatorAdapter) {
		this.validatorAdapter = validatorAdapter;
	}

	/**
	 * constructor
	 */
	public RulesExtInternal() {
		return;
	}

}
