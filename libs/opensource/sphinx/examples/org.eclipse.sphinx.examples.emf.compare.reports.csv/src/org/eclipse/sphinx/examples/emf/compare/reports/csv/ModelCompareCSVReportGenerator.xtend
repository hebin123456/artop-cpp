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
package org.eclipse.sphinx.examples.emf.compare.reports.csv

import java.text.MessageFormat
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject
import org.eclipse.emf.compare.Comparison
import org.eclipse.emf.compare.Diff
import org.eclipse.sphinx.emf.compare.report.AbstractModelCompareReportGenerator
import org.eclipse.emf.common.util.Monitor

public class ModelCompareCSVReportGenerator extends AbstractModelCompareReportGenerator {

	private static final String MODEL_DIFF_REPORT_TARGET_CSV_FILE_NAME = "{0}_{1}_diffs.csv"; //$NON-NLS-1$
	private static final String SEPARATOR = ";"

	override protected doGenerate(Comparison comparison) {
		comparison.generateCVS
	}

	override postDiff(Comparison comparison, Monitor monitor) {
		comparison.generate
	}

	public def generate(Comparison comparison) {
		val IProject targetProject = getTargetProject(comparison);
		if (targetProject != null) {
			val folder = targetProject.getFolder(MODEL_DIFF_REPORT_TARGET_REPORT_FOLDER_NAME)
			if (!folder.exists) {
				folder.createFolder
			}
			val IFile targetFile = folder.getFile(
				MessageFormat.format(MODEL_DIFF_REPORT_TARGET_CSV_FILE_NAME, getName(comparison.leftObject),
					getName(comparison.rightObject)))
			generate(comparison, targetFile)
		}
	}

	public def generateCVS(Comparison comparison) '''
		«IF comparison != null»
			«val leftObject = comparison.leftObject»
			«val rightObject = comparison.rightObject»
			Model Differences«SEPARATOR»«SEPARATOR»«SEPARATOR»«SEPARATOR»
			Left: «getModelCompareInputText(leftObject)»«SEPARATOR»«SEPARATOR»  «SEPARATOR»Right: «getModelCompareInputText(
			rightObject)»«SEPARATOR»
			«FOR Diff diff : comparison.getDifferences()»
				«val differences = comparison.handleDifferences(diff)»
				«differences.get(MODEL_DIFF_REPORT_LEFT_DIFF_URI)»«SEPARATOR»«differences.get(MODEL_DIFF_REPORT_LEFT_DIFF_TEXT)»«SEPARATOR»«differences.
			get(MODEL_DIFF_REPORT_KIND)»«SEPARATOR»«differences.get(MODEL_DIFF_REPORT_RIGHT_DIFF_URI)»«SEPARATOR»«differences.get(MODEL_DIFF_REPORT_RIGHT_DIFF_TEXT)»
			«ENDFOR»
		«ENDIF»
	'''
}
