/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [408238] eValidator not resolved correctly
 *     itemis - [478811] Check validation may compromise EMF Validation-based validation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.validation.diagnostic;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.emf.validation.service.IConstraintFilter;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.validation.ICompositeValidator;
import org.eclipse.sphinx.emf.validation.diagnostic.filters.ExtensionPointFilter;
import org.eclipse.sphinx.emf.validation.diagnostic.filters.util.ConstraintFilterValue;
import org.eclipse.sphinx.emf.validation.evalidator.adapter.EValidatorAdapter;

/**
 * Specialization of the common {@link Diagnostician}
 */
public class ExtendedDiagnostician extends Diagnostician {

	private Set<IConstraintFilter> filters = null;

	private EObject rootObject = null;

	IProgressMonitor monitor = null;

	// depth of the validation
	private int depth;

	//
	private boolean hasBeenCanceled = false;

	// default depth validation => usual one => infinite recursion
	private final static int VALIDATION_DEFAULT_DEPTH = EObjectUtil.DEPTH_INFINITE;

	public ExtendedDiagnostician() {
		hasBeenCanceled = false;
		resetDepth();
	}

	/**
	 * Method which embeds validation with setting for filter and validation depth.
	 *
	 * @param eObject
	 *            the object to validate
	 * @param filter
	 *            , filters on rules for validation
	 * @param int
	 *            validation depth
	 * @return Diagnostic
	 */
	public Diagnostic validate(EObject eObject, Set<IConstraintFilter> filters, int depth) {

		setDepth(depth);
		rootObject = eObject;
		this.filters = filters;
		hasBeenCanceled = false;

		Map<Object, Object> context = new HashMap<Object, Object>();
		context.put(EValidator.SubstitutionLabelProvider.class, this);
		context.put(EValidator.class, this);

		String resourceTxt = ""; //$NON-NLS-1$

		IResource iresource = EcorePlatformUtil.getFile(eObject);
		if (iresource != null) {
			resourceTxt = "( " + iresource.getName() + ")"; //$NON-NLS-1$//$NON-NLS-2$
		}

		BasicDiagnostic diagnostics = new BasicDiagnostic(EObjectValidator.DIAGNOSTIC_SOURCE, 0,
				EcorePlugin.INSTANCE.getString("_UI_DiagnosticRoot_diagnostic", new Object[] { getObjectLabel(eObject) + resourceTxt }), //$NON-NLS-1$
				new Object[] { eObject });
		validate(eObject, diagnostics, context);

		resetDepth();
		unsetFilter();

		return diagnostics;
	}

	/**
	 * Method which embeds validation with setting for filter and validation depth.
	 *
	 * @param eObject
	 *            the object to validate
	 * @param filter
	 *            , filter on rules for validation
	 * @param int
	 *            validation depth
	 * @return Diagnostic
	 */
	@SuppressWarnings("unchecked")
	public Diagnostic validate(EObject eObject, IConstraintFilter filter, int depth) {

		Set<IConstraintFilter> l = null;
		if (filter != null) {
			l = new HashSet<IConstraintFilter>();
			l.add(filter);
		} else {
			l = Collections.EMPTY_SET;
		}

		return validate(eObject, l, depth);
	}

	/**
	 * Method which embeds validation with setting for filter.
	 *
	 * @param eObject
	 *            the object to validate
	 * @param filter
	 *            , filter on rules for validation
	 * @return Diagnostic
	 */
	public Diagnostic validate(EObject eObject, IConstraintFilter filter) {

		return validate(eObject, filter, VALIDATION_DEFAULT_DEPTH);
	}

	/**
	 * Method which embeds validation with setting for filter. The validation depth is set to default value (infinite
	 * one.)
	 *
	 * @param eObject
	 *            the object to validate
	 * @param filter
	 *            , filters on rules for validation
	 * @return Diagnostic
	 */
	public Diagnostic validate(EObject eObject, Set<IConstraintFilter> filters) {

		return validate(eObject, filters, VALIDATION_DEFAULT_DEPTH);

	}

	/**
	 * Method which embeds validation with setting for filter and validation depth.
	 *
	 * @param eObject
	 * @param cfv
	 * @return {@link Diagnostic}
	 * @see ConstraintFilterValue
	 */
	public Diagnostic validate(EObject eObject, ConstraintFilterValue cfv, int depth) {

		Set<IConstraintFilter> l = new HashSet<IConstraintFilter>();
		l.add(new ExtensionPointFilter(cfv));

		return validate(eObject, l, depth);

	}

	/**
	 * Method which embeds validation with setting for filter
	 *
	 * @param eObject
	 * @param cfv
	 * @return {@link Diagnostic}
	 * @see ConstraintFilterValue
	 */
	public Diagnostic validate(EObject eObject, ConstraintFilterValue cfv) {

		return validate(eObject, cfv, VALIDATION_DEFAULT_DEPTH);
	}

	/**
	 * @param eObject
	 *            the object to validate
	 * @param depth
	 *            the validation depth
	 * @return {@link Diagnostic}
	 */
	@SuppressWarnings("unchecked")
	public Diagnostic validate(EObject eObject, int depth) {

		return validate(eObject, Collections.EMPTY_SET, depth);
	}

	@Override
	public Diagnostic validate(EObject eObject) {

		return validate(eObject, VALIDATION_DEFAULT_DEPTH);
	}

	protected EObjectValidator findEValidator(EClass eClass) {
		Object eValidatorObject = eValidatorRegistry.get(eClass.getEPackage());

		if (eValidatorObject instanceof EValidatorAdapter) {
			return (EValidatorAdapter) eValidatorObject;
		} else if (eValidatorObject instanceof ICompositeValidator) {
			for (EValidator eValidator : ((ICompositeValidator) eValidatorObject).getValidators()) {
				if (eValidator instanceof EValidatorAdapter) {
					return (EValidatorAdapter) eValidator;
				}
			}
		}

		for (EClass eSuperType : eClass.getESuperTypes()) {
			eValidatorObject = findEValidator(eSuperType);
			if (eValidatorObject instanceof EValidatorAdapter) {
				return (EValidatorAdapter) eValidatorObject;
			}
		}

		eValidatorObject = eValidatorRegistry.get(null);
		return eValidatorObject instanceof EObjectValidator ? (EObjectValidator) eValidatorObject : null;
	}

	@Override
	public boolean validate(EClass eClass, EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
		// Find validator for given EClass
		EObjectValidator eValidator = findEValidator(eClass);

		// If no validator could be found we must assume the given EObject to be valid
		if (eValidator == null) {
			return true;
		}

		boolean result = false;

		boolean goNext = true;

		if (depth == EObjectUtil.DEPTH_ONE) { // Then we exit after the second pass
			if (eObject == rootObject || eObject.eContainer() != null && eObject.eContainer() == rootObject) {
				goNext = true;
			} else {
				goNext = false;
			}
		}

		if (goNext) {
			result = eValidator instanceof EValidatorAdapter
					? ((EValidatorAdapter) eValidator).validate(eClass, eObject, diagnostics, context, filters)
					: eValidator.validate(eClass, eObject, diagnostics, context);
			if (isAnyProgressMonitor()) {
				monitor.worked(1);
				if (monitor.isCanceled()) {
					hasBeenCanceled = true;
					return false;
				}
			}
		} else {
			return result;
		}

		if (depth == EObjectUtil.DEPTH_ZERO) { // Then we exit on the first pass
			goNext = false;
		}

		if (depth == EObjectUtil.DEPTH_ONE && eObject != rootObject) { // Then we exit on the first pass
			goNext = false;
		}

		if (goNext && (result || diagnostics != null)) {
			result &= doValidateContents(eObject, diagnostics, context);
		}

		return result;
	}

	@Override
	protected boolean doValidateContents(EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
		List<EObject> eContents = eObject.eContents();
		if (!eContents.isEmpty()) {
			Iterator<EObject> i = eContents.iterator();
			EObject child = i.next();
			boolean result = validate(child, diagnostics, context);
			while (i.hasNext() && (result || diagnostics != null)) {
				child = i.next();
				result &= validate(child, diagnostics, context);
			}

			return result;
		} else {
			return true;
		}
	}

	/**
	 * Set the filter that will be applied with this diagnostician object
	 *
	 * @param icf
	 * @see IConstraintFilter
	 */
	public void setFilter(IConstraintFilter icf) {
		if (icf == null) {
			return;
		}

		filters = new HashSet<IConstraintFilter>();
		filters.add(icf);
	}

	/**
	 * Set the filter that will be applied to this diagnostician object
	 *
	 * @param icfs
	 * @see IConstraintFilter
	 */
	public void setFilter(Set<IConstraintFilter> icfs) {
		filters = icfs;
	}

	/**
	 * reset filters
	 */
	protected void unsetFilter() {
		filters = null;
	}

	/**
	 * set the depth of the validation performed through this diagnostician allowed value are
	 * {@link EObjectUtil#DEPTH_ZERO}, {@link EObjectUtil#DEPTH_ONE} and {@link EObjectUtil#DEPTH_INFINITE}. If the
	 * value is not on this range set the value to {@link ExtendedDiagnostician#VALIDATION_DEFAULT_DEPTH}.
	 *
	 * @param value
	 * @see org.eclipse.sphinx.emf.util.EObjectUtil
	 */
	public void setDepth(int value) {
		switch (value) {
		case EObjectUtil.DEPTH_ZERO:
			depth = EObjectUtil.DEPTH_ZERO;
			break;
		case EObjectUtil.DEPTH_ONE:
			depth = EObjectUtil.DEPTH_ONE;
			break;
		case EObjectUtil.DEPTH_INFINITE:
			depth = EObjectUtil.DEPTH_INFINITE;
			break;
		default:
			depth = VALIDATION_DEFAULT_DEPTH;
			break;
		}
	}

	/**
	 * reset depth of the validation to its default value
	 */
	private void resetDepth() {
		depth = VALIDATION_DEFAULT_DEPTH;
		rootObject = null;
	}

	@Override
	public String getObjectLabel(final EObject eObject) {

		String result = ""; //$NON-NLS-1$

		final TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(eObject);
		if (editingDomain != null) {
			RunnableWithResult<String> runnable = new RunnableWithResult.Impl<String>() {
				@Override
				public void run() {
					setResult(doGetObjectLabel(editingDomain, eObject));
				}
			};

			try {
				result = TransactionUtil.runExclusive(editingDomain, runnable);
			} catch (InterruptedException ex) {
			}
		} else {
			result = doGetObjectLabel(editingDomain, eObject);
		}

		return result;
	}

	protected String doGetObjectLabel(TransactionalEditingDomain editingDomain, EObject eObject) {
		if (editingDomain != null) {
			AdapterFactory adapterFactory = ((AdapterFactoryEditingDomain) editingDomain).getAdapterFactory();
			AdapterFactoryItemDelegator delegator = new AdapterFactoryItemDelegator(adapterFactory);
			return delegator.getText(eObject);
		}
		return super.getObjectLabel(eObject);
	}

	/**
	 * set the monitor for the validation job.
	 *
	 * @param monitor
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	/**
	 * accessor on the monitor for the validation job
	 *
	 * @return {@link IProgressMonitor}
	 */
	public IProgressMonitor getProgressMonitor() {
		return monitor;
	}

	/**
	 * check if a progress monitor has been set
	 *
	 * @return yes if true, false otherwise
	 */
	public boolean isAnyProgressMonitor() {
		return monitor == null ? false : true;
	}

	/**
	 * check if the validation operation has been canceled
	 */
	public boolean isCanceled() {
		return hasBeenCanceled;
	}

}
