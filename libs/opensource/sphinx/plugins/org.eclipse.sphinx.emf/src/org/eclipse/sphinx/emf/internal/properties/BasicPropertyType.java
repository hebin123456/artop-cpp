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
package org.eclipse.sphinx.emf.internal.properties;

public class BasicPropertyType implements IPropertyType {

	protected String featureName;
	protected Class<?> valueType;

	/**
	 * @param featureName
	 * @param valueType
	 */
	public BasicPropertyType(String featureName, Class<?> valueType) {
		this.featureName = featureName;
		this.valueType = valueType;
	}

	@Override
	public String getFeatureName() {
		return featureName;
	}

	@Override
	public Class<?> getValueType() {
		return valueType;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof BasicPropertyType) {
			BasicPropertyType propertyType = (BasicPropertyType) object;
			boolean areFeatureNamesEqual = featureName != null ? featureName.equals(propertyType.featureName) : propertyType.featureName == null;
			boolean areValueTypesEqual = valueType != null ? valueType.equals(propertyType.valueType) : propertyType.valueType == null;
			return areFeatureNamesEqual && areValueTypesEqual;
		} else {
			return super.equals(object);
		}
	}

	@Override
	public int hashCode() {
		int hash = 0;
		if (featureName != null) {
			hash += featureName.hashCode();
		}
		if (valueType != null) {
			hash += valueType.hashCode();
		}
		return hash;
	}

	@Override
	public String toString() {
		return "Feature:     " + featureName //$NON-NLS-1$
				+ "\nValueType: " + valueType != null ? valueType.getName() : null; //$NON-NLS-1$
	}
}
