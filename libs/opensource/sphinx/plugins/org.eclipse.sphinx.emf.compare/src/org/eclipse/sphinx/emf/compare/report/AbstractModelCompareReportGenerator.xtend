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
package org.eclipse.sphinx.emf.compare.report

import java.io.ByteArrayInputStream
import java.util.Map
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.emf.common.notify.Notifier
import org.eclipse.emf.common.util.Monitor
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.compare.AttributeChange
import org.eclipse.emf.compare.Comparison
import org.eclipse.emf.compare.Diff
import org.eclipse.emf.compare.DifferenceKind
import org.eclipse.emf.compare.DifferenceSource
import org.eclipse.emf.compare.Match
import org.eclipse.emf.compare.MatchResource
import org.eclipse.emf.compare.ReferenceChange
import org.eclipse.emf.compare.postprocessor.IPostProcessor
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.sphinx.emf.compare.util.ModelCompareUtil
import org.eclipse.sphinx.emf.util.EObjectUtil
import org.eclipse.sphinx.emf.util.EcorePlatformUtil
import org.eclipse.sphinx.emf.util.EcoreResourceUtil
import org.eclipse.core.resources.IFolder

public abstract class AbstractModelCompareReportGenerator implements IPostProcessor {

	public static final String MODEL_DIFF_REPORT_KIND = "kind"; //$NON-NLS-1$
	public static final String MODEL_DIFF_REPORT_LEFT_DIFF_URI = "leftURI"; //$NON-NLS-1$
	public static final String MODEL_DIFF_REPORT_LEFT_DIFF_TEXT = "leftChangeText"; //$NON-NLS-1$
	public static final String MODEL_DIFF_REPORT_RIGHT_DIFF_URI = "rightURI"; //$NON-NLS-1$
	public static final String MODEL_DIFF_REPORT_RIGHT_DIFF_TEXT = "rightChangeText"; //$NON-NLS-1$
	public static final String MODEL_DIFF_REPORT_TARGET_REPORT_FOLDER_NAME = "reports"; //$NON-NLS-1$

	protected abstract def CharSequence doGenerate(Comparison comparison)

	public def generate(Comparison comparison, IFile targetFile) {
		writeFile(targetFile, comparison.doGenerate)
	}

	def Notifier getLeftObject(Comparison comparison) {
		if (comparison != null) {
			if (!comparison.getMatchedResources().isEmpty()) {
				val MatchResource matchResource = comparison.getMatchedResources().get(0)
				return matchResource.getLeft()
			} else {

				// This is a comparison of EObjects
				if (!comparison.getMatches().isEmpty()) {
					val Match match = comparison.getMatches().get(0)
					return match.getLeft()
				}
			}
		}
		null
	}

	def Notifier getRightObject(Comparison comparison) {
		if (comparison != null) {
			if (!comparison.getMatchedResources().isEmpty()) {
				val MatchResource matchResource = comparison.getMatchedResources().get(0)
				return matchResource.getRight()
			} else {

				// This is a comparison of EObjects
				if (!comparison.getMatches().isEmpty()) {
					val Match match = comparison.getMatches().get(0)
					return match.getRight()
				}
			}
		}
		null
	}

	def IProject getTargetProject(Comparison comparison) {
		if (comparison != null) {
			if (comparison.getMatchedResources().isEmpty()) {

				// This is a comparison of EObjects
				for (Match match : comparison.getMatches()) {
					var IFile file = EcorePlatformUtil.getFile(match.getLeft())
					if (file == null) {
						file = EcorePlatformUtil.getFile(match.getRight())
					}

					if (file != null) {
						return file.getProject()
					}
				}
			} else {
				for (MatchResource matchResource : comparison.getMatchedResources()) {
					var IFile file = EcorePlatformUtil.getFile(matchResource.getLeft())
					if (file == null) {
						file = EcorePlatformUtil.getFile(matchResource.getRight())
					}

					if (file != null) {
						return file.getProject()
					}
				}
			}
		}
		return null
	}

	def String getModelCompareInputText(Object object) {
		var String label = ""
		if (object instanceof EObject) {
			val URI uri = EcoreResourceUtil.getURI(object, true)
			label = uri.trimQuery().toString()
		} else if (object instanceof Resource) {
			label = object.getURI().trimQuery().toString()
		}
		return label;
	}

	def String getName(Object object) {
		var String name = ""
		if (object instanceof EObject) {
			val EStructuralFeature eStructuralFeature = EObjectUtil.getEStructuralFeature(object.eClass, "name")
			if (eStructuralFeature != null) {
				name = object.eGet(eStructuralFeature, false)?.toString
			} else {
				name = object.eClass.name + "@" + Integer.toHexString(object.hashCode)
			}
		} else if (object instanceof Resource) {
			name = object.getURI().trimQuery().lastSegment
		}
		return name;
	}

	def String getDiffKindText(Diff diff) {
		val kind = diff?.kind
		if (kind == DifferenceKind.DELETE) {
			if (diff instanceof ReferenceChange) {
				val reference = diff.reference
				if (reference.isContainment) {
					return "DELETED from "
				} else {
					return "REMOVED from "
				}
			}
		} else if (kind == DifferenceKind.ADD) {
			return "ADDED to "
		}
		return kind + "D "
	}

	def String getDiffKindLabel(DifferenceKind kind) {
		switch kind {
			case ADD:
				return "+-"
			case DELETE:
				return "-+"
			case CHANGE:
				return "<>"
			case MOVE:
				return "~"
			default:
				return "<>"
		}
	}

	def Map<String, Object> handleDifferences(Comparison comparison, Diff diff) {
		var Map<String, Object> map = newHashMap()

		if (diff != null) {
			val DifferenceKind kind = diff.getKind()
			map.put(MODEL_DIFF_REPORT_KIND, getDiffKindLabel(kind))
			map.put(MODEL_DIFF_REPORT_LEFT_DIFF_URI, getDiffObjectURIFragment(diff, DifferenceSource.LEFT))
			map.put(MODEL_DIFF_REPORT_RIGHT_DIFF_URI, getDiffObjectURIFragment(diff, DifferenceSource.RIGHT))
			

			// Attribute changed
			if (diff instanceof AttributeChange) {
				map.put(MODEL_DIFF_REPORT_LEFT_DIFF_TEXT, getAttributeChangeText(diff))
				map.put(MODEL_DIFF_REPORT_RIGHT_DIFF_TEXT, getAttributeChangeText(diff))
			} else {
				if (diff.source == DifferenceSource.LEFT) {

					// left addition
					if (kind == DifferenceKind.ADD) {
						map.put(MODEL_DIFF_REPORT_LEFT_DIFF_TEXT, getReferenceChangeText(diff, false));
						map.put(MODEL_DIFF_REPORT_RIGHT_DIFF_TEXT, getReferenceChangeText(diff, true));
					} else if (kind == DifferenceKind.DELETE) {

						// left deletion
						map.put(MODEL_DIFF_REPORT_LEFT_DIFF_TEXT, getReferenceChangeText(diff, false));
						map.put(MODEL_DIFF_REPORT_RIGHT_DIFF_TEXT, getReferenceChangeText(diff, true));
					} else if (kind == DifferenceKind.MOVE) {

						// Object moved
						map.put(MODEL_DIFF_REPORT_LEFT_DIFF_TEXT, getReferenceChangeText(diff, false));
						map.put(MODEL_DIFF_REPORT_RIGHT_DIFF_TEXT, getReferenceChangeText(diff, true));
					}
				} else {

					// right addition
					if (kind == DifferenceKind.ADD) {
						map.put(MODEL_DIFF_REPORT_LEFT_DIFF_TEXT, getReferenceChangeText(diff, true));
						map.put(MODEL_DIFF_REPORT_RIGHT_DIFF_TEXT, getReferenceChangeText(diff, false));
					} else if (kind == DifferenceKind.DELETE) {

						// right deletion
						map.put(MODEL_DIFF_REPORT_LEFT_DIFF_TEXT, getReferenceChangeText(diff, true));
						map.put(MODEL_DIFF_REPORT_RIGHT_DIFF_TEXT, getReferenceChangeText(diff, false));
					} else if (kind == DifferenceKind.MOVE) {

						// Object moved
						map.put(MODEL_DIFF_REPORT_LEFT_DIFF_TEXT, getReferenceChangeText(diff, true));
						map.put(MODEL_DIFF_REPORT_RIGHT_DIFF_TEXT, getReferenceChangeText(diff, false));
					}
				}
			}
		}
		return map;
	}

	def StringBuffer getDiffObjectURIFragment(Diff diff, DifferenceSource source) {
		var StringBuffer label = new StringBuffer
		var EObject value
		if (diff instanceof AttributeChange){
			val Match match = diff.getMatch();
			value = if(source == DifferenceSource.LEFT) match.left else match.right
		} else if (diff instanceof ReferenceChange){
			value = ModelCompareUtil.getValue(diff.match.comparison, diff)
		}
		if (value != null) {
			label.append(EcoreResourceUtil.getURI(value)?.fragment)
		}
		label
	}

	def StringBuffer getAttributeChangeText(AttributeChange attributeChange) {
		var StringBuffer label = new StringBuffer
		if (attributeChange != null) {
			label.append("[" + attributeChange.attribute?.name + " " + attributeChange.kind + "D" + "]")
		}
		label
	}

	def StringBuffer getReferenceChangeText(Diff diff, boolean opposite) {
		var StringBuffer label = new StringBuffer

		if (diff instanceof ReferenceChange) {
			val kind = diff.kind
			var String kindLabel = ""
			if (!opposite) {
				kindLabel = diff.diffKindText
			} else {
				if (kind == DifferenceKind.ADD) {
					kindLabel = "DELETED from "
					val reference = diff.reference
					if (!reference.isContainment) {
						kindLabel = "REMOVED from "
					}
				} else if (kind == DifferenceKind.DELETE) {
					kindLabel = "ADDED to "
				} else {
					kindLabel = kind + "D "
				}
			}
			val EObject matchValue = if(!opposite) diff.match.left else diff.match.right
			return label.append(" [").append(kindLabel).append(
				getName(matchValue) + "." + diff.reference?.name).append("]")
		}
		label
	}

	def writeFile(IFile targetFile, CharSequence content) {
		val monitor = new NullProgressMonitor
		if (targetFile.exists)
			targetFile.delete(true, monitor)
		targetFile.create(new ByteArrayInputStream(content.toString.bytes), true, monitor)
	}

	def createFolder(IFolder folder) {
		val monitor = new NullProgressMonitor
		if (!folder.exists)
			folder.create(true, false, monitor)
	}

	override postComparison(Comparison comparison, Monitor monitor) {
		// Do nothing by default
	}

	override postConflicts(Comparison comparison, Monitor monitor) {
		// Do nothing by default
	}

	override postDiff(Comparison comparison, Monitor monitor) {
		// Do nothing by default
	}

	override postEquivalences(Comparison comparison, Monitor monitor) {
		// Do nothing by default
	}

	override postMatch(Comparison comparison, Monitor monitor) {
		// Do nothing by default
	}

	override postRequirements(Comparison comparison, Monitor monitor) {
		// Do nothing by default
	}
}
