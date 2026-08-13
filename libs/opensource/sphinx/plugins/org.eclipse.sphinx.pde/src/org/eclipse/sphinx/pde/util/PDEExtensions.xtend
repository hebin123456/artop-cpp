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
 package org.eclipse.sphinx.pde.util

import org.eclipse.jdt.core.JavaCore
import org.eclipse.sphinx.jdt.util.JavaExtensions

class PDEExtensions {
	
	private static final String PDE_EXECUTION_ENVIRONMENT_J2SE_15 = "J2SE-1.5"; //$NON-NLS-1$
	private static final String PDE_EXECUTION_ENVIRONMENT_JavaSE_16 = "JavaSE-1.6"; //$NON-NLS-1$
	private static final String PDE_EXECUTION_ENVIRONMENT_JavaSE_17 = "JavaSE-1.8"; //$NON-NLS-1$
	private static final String PDE_EXECUTION_ENVIRONMENT_JavaSE_18 = "JavaSE-1.8"; //$NON-NLS-1$
 	
 	/**
	 * Gets the required execution environment.
	 * 
	 * @param compilerCompliance the Java compiler compliance
	 *
	 * @return the required execution environment.
	 */
	static def String getRequiredExecutionEnvironment(String compilerCompliance) {
		var String requiredExecutionEnvironment = null;
		if (JavaCore.VERSION_1_5.equals(compilerCompliance)) {
			requiredExecutionEnvironment = PDE_EXECUTION_ENVIRONMENT_J2SE_15;
		} else if (JavaCore.VERSION_1_6.equals(compilerCompliance)) {
			requiredExecutionEnvironment = PDE_EXECUTION_ENVIRONMENT_JavaSE_16;
		} else if (JavaCore.VERSION_1_7.equals(compilerCompliance)) {
			requiredExecutionEnvironment = PDE_EXECUTION_ENVIRONMENT_JavaSE_17;
		} else if (JavaExtensions.VERSION_1_8.equals(compilerCompliance)) {
			requiredExecutionEnvironment = PDE_EXECUTION_ENVIRONMENT_JavaSE_18;
		}
		return requiredExecutionEnvironment;
	}
 }
 