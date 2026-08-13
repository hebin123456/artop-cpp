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
package org.eclipse.sphinx.examples.hummingbird20;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EModelElementImpl;
import org.eclipse.sphinx.emf.ecore.ExtendedEObjectImpl;
import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;

public class Hummingbird20EObjectImpl extends ExtendedEObjectImpl {

	private static final String[] ESCAPE = { "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07", "%08", "%09", "%0A", "%0B", "%0C", "%0D", "%0E",
			"%0F", "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17", "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F", "%20", null, "%22",
			"%23", null, "%25", "%26", "%27", null, null, null, null, "%2C", null, null, "%2F", null, null, null, null, null, null, null, null, null,
			null, "%3A", null, "%3C", null, "%3E", null, };

	@Override
	public String eURIFragmentSegment(EStructuralFeature eStructuralFeature, EObject eObject) {
		if (eObject instanceof Identifiable) {
			Identifiable identifiable = (Identifiable) eObject;
			String name = identifiable.getName();
			int count = 0;
			for (Object otherEObject : eContents()) {
				if (otherEObject == eObject) {
					break;
				}
				if (otherEObject instanceof Identifiable) {
					Identifiable otherIdentifiable = (Identifiable) otherEObject;
					String otherName = otherIdentifiable.getName();
					if (name == null ? otherName == null : name.equals(otherName)) {
						++count;
					}
				}
			}
			name = name == null ? "%" : eEncodeValue(name);
			return count > 0 ? name + "." + count : name;
		}
		return super.eURIFragmentSegment(eStructuralFeature, eObject);
	}

	@Override
	public EObject eObjectForURIFragmentSegment(String uriFragmentSegment) {
		int length = uriFragmentSegment.length();
		if (length > 0) {
			// Is the first character a special character, i.e., something other than '@'?
			//
			char firstCharacter = uriFragmentSegment.charAt(0);
			if (firstCharacter != '@') {
				// Is it the start of a source URI of an annotation?
				//
				if (firstCharacter == '%') {
					// Find the closing '%' and make sure it's not just the opening '%'
					//
					int index = uriFragmentSegment.lastIndexOf("%");
					boolean hasCount = false;
					if (index != 0 && (index == length - 1 || (hasCount = uriFragmentSegment.charAt(index + 1) == '.'))) {
						// Decode all encoded characters.
						//
						String encodedSource = uriFragmentSegment.substring(1, index);
						String source = "%".equals(encodedSource) ? null : URI.decode(encodedSource);

						// Check for a count, i.e., a '.' followed by a number.
						//
						int count = 0;
						if (hasCount) {
							try {
								count = Integer.parseInt(uriFragmentSegment.substring(index + 2));
							} catch (NumberFormatException exception) {
								throw new WrappedException(exception);
							}
						}

						// Look for the annotation with the matching source.
						//
						for (Object object : eContents()) {
							if (object instanceof EAnnotation) {
								EAnnotation eAnnotation = (EAnnotation) object;
								String otherSource = eAnnotation.getSource();
								if ((source == null ? otherSource == null : source.equals(otherSource)) && count-- == 0) {
									return eAnnotation;
								}
							}
						}
						return null;
					}
				}

				// Look for trailing count.
				//
				int index = uriFragmentSegment.lastIndexOf(".");
				String name = index == -1 ? uriFragmentSegment : uriFragmentSegment.substring(0, index);
				int count = 0;
				if (index != -1) {
					try {
						count = Integer.parseInt(uriFragmentSegment.substring(index + 1));
					} catch (NumberFormatException exception) {
						// Interpret it as part of the name.
						//
						name = uriFragmentSegment;
					}
				}

				name = "%".equals(name) ? null : URI.decode(name);

				// Look for a matching named element.
				//
				for (Object object : eContents()) {
					if (object instanceof Identifiable) {
						Identifiable identifiable = (Identifiable) object;
						String otherName = identifiable.getName();
						if ((name == null ? otherName == null : name.equals(otherName)) && count-- == 0) {
							return identifiable;
						}
					}
				}

				return null;
			}
		}

		return super.eObjectForURIFragmentSegment(uriFragmentSegment);
	}

	/**
	 * Returns the encoded value or the original, if no encoding was needed.
	 *
	 * @see EModelElementImpl#eURIFragmentSegment(EStructuralFeature, EObject)
	 * @param value
	 *            the value to be encoded.
	 * @return the encoded value or the original, if no encoding was needed.
	 */
	static String eEncodeValue(String value) {
		int length = value.length();
		StringBuilder result = null;
		for (int i = 0; i < length; ++i) {
			char character = value.charAt(i);
			if (character < ESCAPE.length) {
				String escape = ESCAPE[character];
				if (escape != null) {
					if (result == null) {
						result = new StringBuilder(length + 2);
						result.append(value, 0, i);
					}
					result.append(escape);
					continue;
				}
			}
			if (result != null) {
				result.append(character);
			}
		}
		return result == null ? value : result.toString();
	}
}
