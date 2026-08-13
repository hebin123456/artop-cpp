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
 *     Elektrobit - [567814] The EMF validation framework reports multiple identical validation problems
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.validation.evalidator.adapter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.emf.validation.model.EvaluationMode;
import org.eclipse.emf.validation.model.IConstraintStatus;
import org.eclipse.emf.validation.service.IBatchValidator;
import org.eclipse.emf.validation.service.IConstraintFilter;
import org.eclipse.emf.validation.service.ITraversalStrategy;
import org.eclipse.emf.validation.service.ModelValidationService;
import org.eclipse.sphinx.emf.validation.diagnostic.ExtendedDiagnostic;
import org.eclipse.sphinx.emf.validation.preferences.IValidationPreferences;

/**
 * An adapter that plugs the EMF Model Validation Service API into the {@link org.eclipse.emf.ecore.EValidator} API.
 */
public class EValidatorAdapter extends EObjectValidator {

	/**
	 * Model Validation Service interface for batch validation of EMF elements.
	 */
	private final IBatchValidator batchValidator;

	private static Boolean emfIntrinsicConstraints = null;

	/**
	 * Initializes me.
	 */
	public EValidatorAdapter() {
		super();

		batchValidator = (IBatchValidator) ModelValidationService.getInstance().newValidator(EvaluationMode.BATCH);
		batchValidator.setIncludeLiveConstraints(true);
		batchValidator.setReportSuccesses(false);
		batchValidator.setTraversalStrategy(new ITraversalStrategy.Flat());

	}

	/**
	 * Return the value of the preference to check EMF default rules.
	 *
	 * @return
	 */
	protected static Boolean areEMFIntrinsicConstraintsEnabled() {
		/*
		 * Performance optimization: Read preference value only once and cache it to avoid repeated reads at every
		 * validation constraint evaluation. Install a preference change listener to update cached preference value when
		 * it changes.
		 */
		if (emfIntrinsicConstraints == null) {
			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(org.eclipse.sphinx.emf.validation.Activator.PLUGIN_ID);
			if (prefs != null) {
				emfIntrinsicConstraints = prefs.getBoolean(IValidationPreferences.PREF_ENABLE_EMF_DEFAULT_RULES,
						IValidationPreferences.PREF_ENABLE_EMF_DEFAULT_RULES_DEFAULT);

				prefs.addPreferenceChangeListener(new IEclipsePreferences.IPreferenceChangeListener() {
					@Override
					public void preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent event) {
						String key = event.getKey();
						if (key.equals(IValidationPreferences.PREF_ENABLE_EMF_DEFAULT_RULES)) {
							Object newValue = event.getNewValue();
							if (newValue == null) {
								emfIntrinsicConstraints = Boolean.FALSE;
							} else {
								emfIntrinsicConstraints = Boolean.valueOf(newValue.toString());
							}
						}
					}
				});
			}
		}
		return emfIntrinsicConstraints;
	}

	@Override
	public boolean validate(EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate(eObject.eClass(), eObject, diagnostics, context);
	}

	/**
	 * Validates the default rules from EMF, if the corresponding preference is enabled.
	 */
	protected void validateEMFRules(EClass eClass, final EObject eObject, final DiagnosticChain diagnostics, final Map<Object, Object> context) {
		// Let's check if EMF default rules should be checked

		if (areEMFIntrinsicConstraintsEnabled() == Boolean.TRUE) {
			if (eClass.eContainer() == getEPackage()) {
				validate(eClass.getClassifierID(), eObject, diagnostics, context);
			} else {
				List<EClass> eSuperTypes = eClass.getESuperTypes();
				if (eSuperTypes.isEmpty()) {
					validate_EveryDefaultConstraint(eObject, diagnostics, context);
				} else {
					validate(eSuperTypes.get(0), eObject, diagnostics, context);
				}
			}
		}
	}

	/**
	 * Implements validation by delegation to the EMF validation framework using 'context' filter.
	 */
	public boolean validate(EClass eClass, final EObject eObject, final DiagnosticChain diagnostics, final Map<Object, Object> context,
			final Set<IConstraintFilter> filters) {

		// first, do whatever the basic EcoreValidator does
		// former call to super.validate(eClass, eObject, diagnostics, context);

		// Let's check if EMF default constraints should be checked

		validateEMFRules(eClass, eObject, diagnostics, context);

		// Ok, Now let's check our constraints.

		IStatus status = Status.OK_STATUS;

		// no point in validating if we can't report results
		if (diagnostics != null) {
			// if EMF Mode Validation Service already covered the sub-tree,
			// which it does for efficient computation and error reporting,
			// then don't repeat (the Diagnostician does the recursion
			// externally). If there is no context map, then we can't
			// help it

			// now runs the custom/other validators
			if (!hasProcessed(eObject, context)) {
				// Add filters, if it exists (pre-process)
				addFilters(filters);

				TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(eObject);
				if (editingDomain != null) {
					final EObject tgt = eObject;
					RunnableWithResult<IStatus> run = new RunnableWithResult.Impl<IStatus>() {
						@Override
						public void run() {
							IStatus status = Status.OK_STATUS;

							try {
								status = batchValidator.validate(tgt, new NullProgressMonitor());
							} finally {
								// Remove the icf filter, if it exists
								removeFilters(filters);
							}
							setResult(status);
						}
					};

					try {
						status = (IStatus) editingDomain.runExclusive(run);
					} catch (InterruptedException ex) {
					}
				} else {
					try {
						status = batchValidator.validate(eObject, new NullProgressMonitor());
					} finally {
						// Remove the icf filter, if it exists
						removeFilters(filters);
					}
				}

				// post-process
				processed(eObject, context, status);
				appendDiagnostics(status, diagnostics);
			}
		}

		return status.isOK();
	}

	/**
	 * Implements validation by delegation to the EMF validation framework.
	 */
	@Override
	public boolean validate(EClass eClass, EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate(eClass, eObject, diagnostics, context, null);
	}

	/**
	 * Direct validation of {@link EDataType}s is not supported by the EMF validation framework; they are validated
	 * indirectly via the {@link EObject}s that hold their values.
	 */
	@Override
	public boolean validate(EDataType eDataType, Object value, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return super.validate(eDataType, value, diagnostics, context);
	}

	/**
	 * If we have a context map, record this object's <code>status</code> in it so that we will know later that we have
	 * processed it and its sub-tree.
	 *
	 * @param eObject
	 *            an element that we have validated
	 * @param context
	 *            the context (may be <code>null</code>)
	 * @param status
	 *            the element's validation status
	 */
	private void processed(EObject eObject, Map<Object, Object> context, IStatus status) {
		if (context != null) {
			context.put(eObject, status);
		}
	}

	/**
	 * Determines whether we have processed this <code>eObject</code> before, by automatic recursion of the EMF Model
	 * Validation Service. This is only possible if we do, indeed, have a context.
	 *
	 * @param eObject
	 *            an element to be validated
	 * @param context
	 *            the context (may be <code>null</code>)
	 * @return <code>true</code> if the context is not <code>null</code> and the <code>eObject</code> has already been
	 *         validated; <code>false</code>, otherwise
	 */
	private boolean hasProcessed(EObject eObject, Map<Object, Object> context) {
		return context != null && context.containsKey(eObject);
	}

	/**
	 * Converts a status result from the EMF validation service to diagnostics.
	 *
	 * @param status
	 *            the EMF validation service's status result
	 * @param diagnostics
	 *            a diagnostic chain to accumulate results on
	 */
	private void appendDiagnostics(IStatus status, DiagnosticChain diagnostics) {

		// Registry registry = EValidator.Registry.INSTANCE;
		// registry.toString();
		// MarkerFilter[] userFilters = super.getAllFilters();
		// Collection declaredFilters = MarkerSupportRegistry.getInstance().getRegisteredFilters();
		// Iterator iterator = declaredFilters.iterator();
		//
		// MarkerFilter[] allFilters = new MarkerFilter[userFilters.length + declaredFilters.size()];
		// System.arraycopy(userFilters, 0, allFilters, 0, userFilters.length);
		// int index = userFilters.length;
		//
		// while (iterator.hasNext()) {
		// allFilters[index] = (MarkerFilter) iterator.next();
		// index++;
		// }

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();

			for (IStatus element : children) {
				appendDiagnostics(element, diagnostics);
			}
		} else if (status instanceof IConstraintStatus) {
			/* <<< */
			// diagnostics.add(new BasicDiagnostic(status.getSeverity(), status.getPlugin(), status.getCode(),
			// status.getMessage(),
			// ((IConstraintStatus) status).getResultLocus().toArray()));
			/* --- */
			diagnostics.add(new ExtendedDiagnostic(status.getSeverity(), status.getPlugin(), ((IConstraintStatus) status).getConstraint(),
					status.getCode(), status.getMessage(), ((IConstraintStatus) status).getResultLocus().toArray()));
			/* >>> */
		} else {
			diagnostics
					.add(new BasicDiagnostic(status.getSeverity(), status.getPlugin(), status.getCode(), status.getMessage(), status.getChildren()));
		}

	}

	/**
	 * add filters to the batchValidator
	 *
	 * @param filters
	 * @see IConstraintFilter
	 */
	private void addFilters(Set<IConstraintFilter> filters) {
		if (filters != null) {
			for (IConstraintFilter icf : filters) {
				batchValidator.addConstraintFilter(icf);
			}
		}

		return;
	}

	/**
	 * remove filters to the batch validator
	 *
	 * @param filters
	 * @see IConstraintFilter
	 */
	private void removeFilters(Set<IConstraintFilter> filters) {
		if (filters != null) {
			for (IConstraintFilter icf : filters) {
				batchValidator.removeConstraintFilter(icf);
			}
		}

		return;
	}

}
