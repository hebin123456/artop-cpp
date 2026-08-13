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
package org.eclipse.sphinx.emf.validation.bridge.util;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.validation.bridge.Activator;
import org.eclipse.sphinx.emf.validation.bridge.extensions.RulesExtInternal;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class RulesExtReader {

	final static public String TAG_RULE_EXT = "model"; //$NON-NLS-1$
	final static public String ATT_RULE_EXT_NAME = "Name"; //$NON-NLS-1$
	final static public String ATT_RULE_EXT_NSURI = "NsURI"; //$NON-NLS-1$
	final static public String ATT_RULE_EXT_MODELCLASS = "class"; //$NON-NLS-1$
	final static public String ATT_RULE_EXT_MARKER = "id"; //$NON-NLS-1$
	final static public String ATT_RULE_EXT_FILTER = "filter"; //$NON-NLS-1$
	final static public String ATT_RULE_EXT_VALIDATORCLASS = "validatorAdapterClass"; //$NON-NLS-1$

	private static RulesExtReader rulesExtReader = null;

	private RulesExtReader() {
	}

	public static RulesExtReader getSingleton() {
		if (rulesExtReader == null) {
			rulesExtReader = new RulesExtReader();
		}

		return rulesExtReader;
	}

	// TODO EXCEPTION IS REALLY UGLY DUDE...
	public RulesExtInternal readExtension(IExtension ext) {

		if (!ext.getExtensionPointUniqueIdentifier().equals(Activator.RULES_EXT_ID)) {
			return null;
		}

		RulesExtInternal tgt = new RulesExtInternal();

		IConfigurationElement[] configurationElement = ext.getConfigurationElements();

		// FIXME In case that multiple models (alias configuration elements) are contributed via same extension, only
		// the last one gets captured in through tgt variable
		for (IConfigurationElement element : configurationElement) {
			internalReadElement(element, tgt);
		}

		return tgt;
	}

	private void internalReadElement(IConfigurationElement element, RulesExtInternal tgt) {
		boolean recognized = readElement(element, tgt);
		if (recognized) {
			IConfigurationElement[] children = element.getChildren();
			for (IConfigurationElement element2 : children) {
				internalReadElement(element2, tgt);
			}
		} else {
			tgt = null;
		}

		return;
	}

	// TODO Re-organise method's body.
	private boolean readElement(IConfigurationElement element, RulesExtInternal tgt) {
		int errorCount = 0;

		if (element.getName().equals(TAG_RULE_EXT)) {
			if (element.getAttribute(ATT_RULE_EXT_MARKER) != null) {
				tgt.setMarker(element.getAttribute(ATT_RULE_EXT_MARKER));
			} else {
				logMissingAttribute(element, ATT_RULE_EXT_MARKER);
				errorCount++;
			}

			if (element.getAttribute(ATT_RULE_EXT_FILTER) != null) {
				tgt.setFilter(element.getAttribute(ATT_RULE_EXT_FILTER));
			} else {
				logMissingAttribute(element, ATT_RULE_EXT_FILTER);
				errorCount++;
			}

			if (element.getAttribute(ATT_RULE_EXT_NAME) != null) {
				tgt.setModelID(element.getAttribute(ATT_RULE_EXT_NAME));
			} else {
				logMissingAttribute(element, ATT_RULE_EXT_NAME);
				errorCount++;
			}

			if (element.getAttribute(ATT_RULE_EXT_NSURI) != null) {
				tgt.setNsURI(URI.createURI(element.getAttribute(ATT_RULE_EXT_NSURI), true));
			} else {
				logMissingAttribute(element, ATT_RULE_EXT_NSURI);
				errorCount++;
			}

			// Let's check if the namespace is ok
			Object ePackageOrDescriptor = null;
			if (tgt.getNsURI() != null) {
				try {
					// don't call getEPackage as this will trigger loading of the model plugins
					ePackageOrDescriptor = EPackage.Registry.INSTANCE.get(tgt.getNsURI().toString());
				} catch (WrappedException e) {
					String msg = NLS.bind(Messages.errNsURIRootPackageObject, tgt.getNsURI());
					logError(element, new WrappedException(msg, e));
					errorCount++;
				} catch (ExceptionInInitializerError e) {
					String msg = NLS.bind(Messages.errNsURIRootPackageObject, tgt.getNsURI());
					logError(element, new ExceptionInInitializerError(msg));
					errorCount++;
				}
			}

			if (element.getAttribute(ATT_RULE_EXT_MODELCLASS) != null) {
				if (ePackageOrDescriptor != null) { // we can try to reach the root object eClass
					String value = element.getAttribute(ATT_RULE_EXT_MODELCLASS);
					String separator = "."; //$NON-NLS-1$
					String eclassifierName = value.contains(separator) ? value.substring(value.lastIndexOf(separator) + 1) : value;

					tgt.setRootModelEClassifierName(eclassifierName);
					tgt.setRootModelObjectName(value);

				}
			} else {
				logMissingAttribute(element, ATT_RULE_EXT_MODELCLASS);
				errorCount++;
			}

			if (element.getAttribute(ATT_RULE_EXT_VALIDATORCLASS) != null) {
				// optional attribute
				String validatorAdapterClassName = element.getAttribute(ATT_RULE_EXT_VALIDATORCLASS);
				if (validatorAdapterClassName != null && validatorAdapterClassName.length() > 0) {
					// try to instantiate it
					try {
						Object validatorAdapter = element.createExecutableExtension(ATT_RULE_EXT_VALIDATORCLASS);
						tgt.setValidatorAdapter(validatorAdapter);
					} catch (Exception ex) {
						logError(element, NLS.bind(Messages.errWrongValidatorAdapter, new Object[] { validatorAdapterClassName, ex }));
					} finally {
					}
				}
			}

		} else {
			tgt = null;
			return false;
		}

		if (errorCount != 0) {
			logError(element, NLS.bind(Messages.errOnExtensionModelNotRegistered, tgt.getModelId()));
			tgt = null;
		}

		return errorCount == 0;

	}

	/**
	 * Logs the error in the desktop log using the provided text and the information in the configuration element.
	 */
	protected void logError(IConfigurationElement element, String text) {
		IExtension extension = element.getDeclaringExtension();
		String msg = NLS.bind(Messages.errOnExtensionIntro, new Object[] { extension.getExtensionPointUniqueIdentifier(),
				extension.getContributor().getName(), text });
		PlatformLogUtil.logAsError(Activator.getDefault(), new Exception(msg));
	}

	/**
	 * Logs the error in the desktop log using the provided text and the information in the configuration element.
	 */
	protected void logError(IConfigurationElement element, Throwable throwable) {
		logError(element, throwable.getMessage());
	}

	/**
	 * Logs a very common error when a required attribute is missing.
	 */
	protected void logMissingAttribute(IConfigurationElement element, String attributeName) {
		logError(element, NLS.bind(Messages.errMissingAttributeOnExtensionPoint, attributeName));
	}

}
