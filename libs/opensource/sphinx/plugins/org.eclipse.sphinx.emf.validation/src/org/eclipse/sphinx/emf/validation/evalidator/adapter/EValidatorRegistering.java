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
package org.eclipse.sphinx.emf.validation.evalidator.adapter;

import java.util.HashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.validation.bridge.Activator;
import org.eclipse.sphinx.emf.validation.bridge.extensions.RulesExtCache;
import org.eclipse.sphinx.emf.validation.bridge.extensions.RulesExtInternal;
import org.eclipse.sphinx.emf.validation.bridge.util.RulesExtReader;
import org.eclipse.sphinx.emf.validation.util.Messages;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class EValidatorRegistering {

	/** The EMF EValidator registry */
	private org.eclipse.emf.ecore.EValidator.Registry eValidatorRegistry = null;

	/** the singleton instance */
	private static EValidatorRegistering singleton = null;

	/**
	 * Singleton accessor
	 * 
	 * @return itself
	 */
	public static EValidatorRegistering getSingleton() {
		if (singleton == null) {
			singleton = new EValidatorRegistering();
		}

		return singleton;
	}

	/**
	 * constructor
	 */
	private EValidatorRegistering() {
		eValidatorRegistry = EValidator.Registry.INSTANCE;
	}

	/**
	 * Add to the {@link EValidator} registry the {@link EValidatorAdapter} of all contribution to the extension point
	 * org.eclipse.sphinx.emf.validation.registration from the plugin pluginID
	 * 
	 * @param pluginID
	 *            the plugin ID
	 */
	public void eValidatorSetAllContributions(String pluginID) {

		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint pt = reg.getExtensionPoint(Activator.RULES_EXT_ID);
		IExtension[] exts = pt.getExtensions();

		for (IExtension extension : exts) {
			if (extension.getContributor().getName().equals(pluginID)) {
				for (IConfigurationElement confElement : extension.getConfigurationElements()) {
					eValidatorSetting(confElement.getAttribute(RulesExtReader.ATT_RULE_EXT_MARKER));
				}
			}
		}
	}

	/**
	 * Add to the {@link EValidator} registry the {@link EValidatorAdapter} for the model identified with ModelID (see
	 * the extension org.eclipse.sphinx.emf.validation.registration)
	 * 
	 * @param modelID
	 *            the ID of a registered model
	 */
	private void eValidatorSetting(String modelId) {
		// Check if this model has been already registered
		if (!eValidatorRegistry.containsKey(modelId)) {
			HashMap<String, RulesExtInternal> extRuleCacheMap = RulesExtCache.getSingleton().getRulesExtInternals();
			RulesExtInternal r = extRuleCacheMap.get(modelId);
			// We check if the model has been already loaded
			if (r != null) {
				EClass eClass = r.getRootModelClass();
				if (eClass == null) {
					return;
				}
				EPackage targetPackage = eClass.getEPackage();
				if (targetPackage == null) {
					PlatformLogUtil.logAsError(Activator.getDefault(),
							NLS.bind(Messages.__EValidatorRegstering_NoSuchPackage, new Object[] { r.getNsURI(), r.getModelId() }));
				} else { // All is OK
					Object validatorAdapter = r.getValidatorAdapter();
					if (validatorAdapter == null) {
						// let the default EValidatorAdapter to be used
						validatorAdapter = new EValidatorAdapter();
					}
					EValidator.Registry.INSTANCE.put(r.getRootModelClass().getEPackage(), validatorAdapter);
				}
			}
		}
	}
}
