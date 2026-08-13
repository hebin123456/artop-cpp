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
package org.eclipse.sphinx.examples.emf.compare.reports.html

import java.text.MessageFormat
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject
import org.eclipse.emf.common.util.Monitor
import org.eclipse.emf.compare.Comparison
import org.eclipse.emf.compare.Diff
import org.eclipse.sphinx.emf.compare.report.AbstractModelCompareReportGenerator

public class ModelCompareHTMLReportGenerator extends AbstractModelCompareReportGenerator {

	private static final String MODEL_DIFF_REPORT_TARGET_HTML_FILE_NAME = "{0}_{1}_diffs.html"; //$NON-NLS-1$

	override protected doGenerate(Comparison comparison) {
		comparison.generateHTML
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
				MessageFormat.format(MODEL_DIFF_REPORT_TARGET_HTML_FILE_NAME, getName(comparison.leftObject),
					getName(comparison.rightObject)))
			generate(comparison, targetFile)
		}
	}

	public def generateHTML(Comparison comparison) '''
		«IF comparison != null»
			«val leftObject = comparison.leftObject»
			«val rightObject = comparison.rightObject»
			<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
			<html>
			<head>
			<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
			<style>
			.AlignLeft { text-align: left; }
			.AlignCenter { text-align: center; }
			.AlignRight { text-align: right; }
			body { font-family: sans-serif; font-size: 11pt; }
			td, th { vertical-align: top; padding-left: 4px; padding-right: 4px; }
			tr.SectionAll td, th { border-left: none; border-top: none; border-bottom: 1px solid Black; border-right: 1px solid Black; }
			table.fc { border-top: 1px solid Black; border-left: 1px solid Black; width: 100%; font-family: monospace; font-size: 10pt; }
			td.Text { color: #000000; background-color: #FFFFFF; }
			</style>
			<title>Model Differences («getModelCompareInputText(leftObject)» - «getModelCompareInputText(rightObject)»)</title>
			</head>
			<body>
			Model Differences<br>
			&nbsp; &nbsp;
			<br>
			<table class="fc" cellspacing="0" cellpadding="0">
				<tbody>
					<tr class="SectionAll">
						<th colspan="2">Left: «getModelCompareInputText(leftObject)»</th>
						<th>  </th>
						<th colspan="2">Right: «getModelCompareInputText(rightObject)»</th>
					</tr>
					«FOR Diff diff : comparison.getDifferences()»
						<tr class="SectionAll">
						«val differences = comparison.handleDifferences(diff)»
							<td class="Text">«differences.get(MODEL_DIFF_REPORT_LEFT_DIFF_URI)»</td>
							<td class="Text">«differences.get(MODEL_DIFF_REPORT_LEFT_DIFF_TEXT)»</td>
							<td class="AlignCenter">«differences.get(MODEL_DIFF_REPORT_KIND)»</td>
							<td class="Text">«differences.get(MODEL_DIFF_REPORT_RIGHT_DIFF_URI)»</td>
							<td class="Text">«differences.get(MODEL_DIFF_REPORT_RIGHT_DIFF_TEXT)»</td>
						</tr>
					«ENDFOR»
				</tbody>
			</table>
			<br>
			</body></html>
		«ENDIF»
	'''
}
