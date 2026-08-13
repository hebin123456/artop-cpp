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
package org.eclipse.sphinx.emf.ecore;

import java.util.Comparator;

import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.internal.ecore.EObjectComparator;
import org.eclipse.sphinx.emf.internal.ecore.EnumeratorComparator;
import org.eclipse.sphinx.emf.internal.messages.Messages;

/**
 * Ecore comparator.
 */
public class EcoreComparator implements Comparator<Object> {

	/**
	 * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer as the first
	 * argument is less than, equal to, or greater than the second.
	 * <p>
	 * The implementor must ensure that <tt>sgn(compare(x, y)) ==
	 * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>. (This implies that <tt>compare(x, y)</tt> must throw
	 * an exception if and only if <tt>compare(y, x)</tt> throws an exception.)
	 * <p>
	 * The implementor must also ensure that the relation is transitive:
	 * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies <tt>compare(x, z)&gt;0</tt>.
	 * <p>
	 * Finally, the implementer must ensure that <tt>compare(x, y)==0</tt> implies that
	 * <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all <tt>z</tt>.
	 * <p>
	 * It is generally the case, but <i>not</i> strictly required that <tt>(compare(x, y)==0) == (x.equals(y))</tt>.
	 * Generally speaking, any comparator that violates this condition should clearly indicate this fact. The
	 * recommended language is "Note: this comparator imposes orderings that are inconsistent with equals."
	 * 
	 * @param o1
	 *            the first object to be compared.
	 * @param o2
	 *            the second object to be compared.
	 * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
	 *         than the second.
	 * @throws ClassCastException
	 *             if the arguments' types prevent them from being compared by this Comparator.
	 */

	@Override
	public int compare(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return 0;
		} else if (o1 == null) {
			return -1;
		} else if (o2 == null) {
			return 1;
		} else {
			return compareObjects(o1, o2);
		}
	}

	private int compareObjects(Object o1, Object o2) {
		if (o1 instanceof EObject && o2 instanceof EObject) {
			return new EObjectComparator<EObject>().compare((EObject) o1, (EObject) o2);
		} else if (o1 instanceof Enumerator && o2 instanceof Enumerator) {
			return new EnumeratorComparator<Enumerator>().compare((Enumerator) o1, (Enumerator) o2);
		} else if (o1 instanceof Boolean && o2 instanceof Boolean) {
			return o1.toString().compareTo(o2.toString());
		} else if (o1 instanceof String && ((String) o1).length() == 0) {
			return -1;
		} else if (o2 instanceof String && ((String) o2).length() == 0) {
			return 1;
		} else if (o1 instanceof String && o2 instanceof String) {
			return o1.toString().compareTo(o2.toString());
		} else {
			String[] args = new String[] { o1.getClass().getSimpleName(), o2.getClass().getSimpleName() };
			throw new RuntimeException(NLS.bind(Messages.error_cannotCompareObjects, args));
		}
	}
}