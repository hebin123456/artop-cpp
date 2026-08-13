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
package org.eclipse.sphinx.examples.hummingbird20.check.withcatalog;

import java.text.MessageFormat;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.EList;
import org.eclipse.sphinx.emf.check.AbstractCheckValidator;
import org.eclipse.sphinx.emf.check.Check;
import org.eclipse.sphinx.emf.check.CheckValidatorRegistry;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Package;
import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue;

/**
 * An example of validator class making use of a check catalog. It contains a set of annotated methods with @Check(...).
 * The check catalog is used to externalize the error messages/severities/etc. When a catalog is used, @Check
 * annotations reference constraints by their Id in the catalog, so there is a logical mapping from methods to
 * constraints. Moreover it is possible to narrow down the scope of methods, i.e. their applicability, by specifying the
 * set of categories for which a constraint is applicable. For a constraint to be applicable within the scope of a
 * validator, the set of categories specified in its @Check annotation should be a subset of the set of categories
 * referenced by the constraint in the check catalog.
 *
 * @see org.eclipse.sphinx.emf.check.AbstractCheckValidator
 */
public class Hummingbird20NamingAndValuesCheckValidator extends AbstractCheckValidator {

	private static final Pattern ILLEGAL_CHARACTERS_PATTERN = Pattern.compile("[\\W]"); //$NON-NLS-1$

	public static final String ISSUE_MSG_ARGUMENT_CATEGORIES_CASE1 = "(Case #1: part of Category1 and Category2 as per check catalog and blank check annotation)"; //$NON-NLS-1$
	public static final String ISSUE_MSG_ARGUMENT_CATEGORIES_CASE2 = "(Case #2: part of Category1 and Category2 as per check catalog and check annotation)"; //$NON-NLS-1$
	public static final String ISSUE_MSG_ARGUMENT_CATEGORIES_CASE3 = "(Case #3: only part of Category1 as per check catalog and narrowed down by check annotation)"; //$NON-NLS-1$
	public static final String ISSUE_MSG_ARGUMENT_CATEGORIES_CASE4 = "(Case #4: only part of Category2 as per intersection of check catalog with partly disjunct check annotation)"; //$NON-NLS-1$
	public static final String ISSUE_MSG_ARGUMENT_CATEGORIES_CASE5 = "(Case #5: not part of any category due to check catalog and check annotation being completely disjunct)"; //$NON-NLS-1$
	public static final String ISSUE_MSG_ARGUMENT_FORMAT_SUPERTYPE = "(Checked applicable subtype: {0})"; //$NON-NLS-1$

	public Hummingbird20NamingAndValuesCheckValidator() {
		super();
	}

	public Hummingbird20NamingAndValuesCheckValidator(CheckValidatorRegistry checkValidatorRegistry) {
		super(checkValidatorRegistry);
	}

	/**
	 * If no categories are explicitly specified in the @Check annotation, the associated constraint is supposed to be
	 * applicable for all the categories referenced by the constraint in the associated check catalog. For example, the
	 * following annotation (constraint = "ApplicationNameNotValid") is equivalent to (constraint =
	 * "ApplicationNameNotValid", categories = { "Category1", "Category2" }).
	 *
	 * @param application
	 */
	@Check(constraint = "ApplicationNameNotValid")
	void checkApplicationName(Application application) {
		issue(application, Common20Package.Literals.IDENTIFIABLE__NAME, ISSUE_MSG_ARGUMENT_CATEGORIES_CASE1);
	}

	/**
	 * In the following method, the @Check annotation references a constraint with Id equals to
	 * "ApplicationNameNotValid" in the associated check catalog. In the check catalog, the constraint is applicable for
	 * the set {"Category1", "Category2"}. The scope of the constraint for this validator, as specified in the
	 * annotation below, is also { "Category1", "Category2" }, which means the following method is called when the user
	 * selects either Category1, Category2, or both.
	 *
	 * @param application
	 */
	@Check(constraint = "ApplicationNameNotValid", categories = { "Category1", "Category2" })
	void checkApplicationNameForCategories1And2(Application application) {
		issue(application, Common20Package.Literals.IDENTIFIABLE__NAME, ISSUE_MSG_ARGUMENT_CATEGORIES_CASE2);
	}

	/**
	 * This method is called by this validator only when the user selects "Category1" even though the associated
	 * constraint is applicable for the set {"Category1", "Category2"}.
	 *
	 * @param application
	 */
	@Check(constraint = "ApplicationNameNotValid", categories = { "Category1" })
	void checkApplicationNameForCategory1(Application application) {
		issue(application, Common20Package.Literals.IDENTIFIABLE__NAME, ISSUE_MSG_ARGUMENT_CATEGORIES_CASE3);
	}

	/**
	 * This method is called by this validator only when the user selects "Category2" even though the associated
	 * constraint is applicable for the set {"Category1", "Category2"}. The annotation of "Category3" does not exist in
	 * the check catalog and therefore has no effect.
	 *
	 * @param application
	 */
	@Check(constraint = "ApplicationNameNotValid", categories = { "Category2", "Category3" })
	void checkApplicationNameForCategory2And3(Application application) {
		issue(application, Common20Package.Literals.IDENTIFIABLE__NAME, ISSUE_MSG_ARGUMENT_CATEGORIES_CASE4);
	}

	/**
	 * This method is *never* called by this validator because "Category3" does not exist in the catalog.
	 *
	 * @param application
	 */
	@Check(constraint = "ApplicationNameNotValid", categories = { "Category3" })
	void checkApplicationNameForCategory3(Application application) {
		issue(application, Common20Package.Literals.IDENTIFIABLE__NAME, ISSUE_MSG_ARGUMENT_CATEGORIES_CASE5);
	}

	/**
	 * This method is called by this validator only when the user selects "Category1".
	 *
	 * @param component
	 */
	@Check(constraint = "ComponentNameNotValid", categories = "Category1")
	void checkComponentName(Component component) {
		if (!hasValidName(component)) {
			issue(component, Common20Package.Literals.IDENTIFIABLE__DESCRIPTION, component.getName());
		}
	}

	/**
	 * This method is called by this validator only when the user selects "Category2".
	 *
	 * @param component
	 */
	@Check(constraint = "ParameterValuesNotValid", categories = { "Category2" })
	void checkComponentValues(Component component) {
		EList<ParameterValue> parameterValues = component.getParameterValues();
		for (ParameterValue value : parameterValues) {
			try {
				Integer.parseInt(value.getValue());
			} catch (NumberFormatException ex) {
				issue(value, InstanceModel20Package.Literals.PARAMETER_VALUE__VALUE);
			}
		}
	}

	/**
	 * This method is called for all Identifiable subtypes.
	 *
	 * @param identifiable
	 */
	@Check(constraint = "IdentifiableNameNotValid")
	void checkIdentifiableName(Identifiable identifiable) {
		issue(identifiable, Common20Package.Literals.IDENTIFIABLE__NAME,
				MessageFormat.format(ISSUE_MSG_ARGUMENT_FORMAT_SUPERTYPE, identifiable.eClass().getName()));
	}

	private boolean hasValidName(Identifiable identifiable) {
		Assert.isNotNull(identifiable);
		String name = identifiable.getName();
		if (name != null) {
			return !ILLEGAL_CHARACTERS_PATTERN.matcher(name).find();
		}
		return true;
	}
}