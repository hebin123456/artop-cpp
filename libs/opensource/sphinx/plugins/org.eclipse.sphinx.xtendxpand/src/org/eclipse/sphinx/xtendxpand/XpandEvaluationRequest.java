/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.xtendxpand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;

public class XpandEvaluationRequest {

	/**
	 * The entry statement for Xpand i.e., the the definition name.
	 */
	private String definitionName;

	/**
	 * The target object on with the code generation is applied.
	 */
	private Object targetObject;

	/**
	 * A list of objects corresponding to the parameters to be used at code generation time.
	 */
	private List<Object> parameterList;

	/**
	 * Constructs an Xpand code generation evaluation request.
	 * 
	 * @param definitionName
	 *            the Xpand definition name.
	 * @param targetObject
	 *            the target object on with the code generation is applied.
	 */
	public XpandEvaluationRequest(String definitionName, Object targetObject) {
		this(definitionName, targetObject, (Object[]) null);
	}

	/**
	 * Constructs an Xpand code generation evaluation request.
	 * 
	 * @param definitionName
	 *            the Xpand definition name.
	 * @param targetObject
	 *            the target object on with the code generation is applied.
	 * @param parameters
	 *            a list of parameters to be used in code generation.
	 */
	public XpandEvaluationRequest(String definitionName, Object targetObject, Object... parameters) {
		Assert.isNotNull(definitionName);
		Assert.isTrue(definitionName.trim().length() != 0);
		Assert.isNotNull(targetObject);

		this.definitionName = definitionName;
		this.targetObject = targetObject;
		parameterList = parameters != null && parameters.length > 0 ? Arrays.asList(parameters) : Collections.emptyList();
	}

	/**
	 * Gets the definition name used in this Xpand evaluation request.
	 */
	public String getDefinitionName() {
		return definitionName;
	}

	/**
	 * Gets the target object on with is applied code generation.
	 */
	public Object getTargetObject() {
		return targetObject;
	}

	/**
	 * Gets the list of parameters used in code generation.
	 */
	public List<Object> getParameterList() {
		return parameterList;
	}
}
