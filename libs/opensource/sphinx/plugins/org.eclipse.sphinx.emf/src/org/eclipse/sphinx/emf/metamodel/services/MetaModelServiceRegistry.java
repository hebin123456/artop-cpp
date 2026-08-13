/**
 * <copyright>
 *
 * Copyright (c) 2014-2017 Continental Engineering Services, BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     Continental Engineering Services - Initial API and implementation
 *     itemis - Moved from Artop to Sphinx, adapted method and variable naming to Sphinx conventions
 *     itemis - [458921] Newly introduced registries for metamodel serives, check validators and workflow contributors are not standalone-safe
 *     itemis - [501899] Use base index instead of IncQuery patterns
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.metamodel.services;

import static org.eclipse.sphinx.platform.util.StatusUtil.createErrorStatus;
import static org.eclipse.sphinx.platform.util.StatusUtil.createWarningStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.emf.internal.metamodel.services.MetaModelServiceDescriptor;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * The registry for the metamodel services. Clients should use {@link DefaultMetaModelServiceProvider}.
 */
public class MetaModelServiceRegistry {

	private static final String EXTP_META_MODEL_SERVICES = Activator.INSTANCE.getSymbolicName() + ".metaModelServices"; //$NON-NLS-1$
	private static final String NODE_SERVICE = "service"; //$NON-NLS-1$

	/** The singleton */
	static final MetaModelServiceRegistry INSTANCE = new MetaModelServiceRegistry(Platform.getExtensionRegistry(),
			PlatformLogUtil.getLog(Activator.getPlugin()));

	private Map<IMetaModelDescriptor, Map<Class<IMetaModelService>, MetaModelServiceDescriptor>> mmServices = null;

	private IExtensionRegistry extensionRegistry;

	private ILog logger;

	private MetaModelServiceRegistry(IExtensionRegistry extensionRegistry, ILog logger) {
		Assert.isNotNull(extensionRegistry);
		Assert.isNotNull(logger);

		this.extensionRegistry = extensionRegistry;
		this.logger = logger;
	}

	private Map<IMetaModelDescriptor, Map<Class<IMetaModelService>, MetaModelServiceDescriptor>> getMetaModelServices() {
		initialize();
		return mmServices != null ? mmServices
				: Collections.<IMetaModelDescriptor, Map<Class<IMetaModelService>, MetaModelServiceDescriptor>> emptyMap();
	}

	/**
	 * Initialize internal data by reading from platform registry
	 */
	private void initialize() {
		if (extensionRegistry == null) {
			return;
		}

		if (mmServices == null) {
			mmServices = new HashMap<IMetaModelDescriptor, Map<Class<IMetaModelService>, MetaModelServiceDescriptor>>();

			// Create a temporary map
			Map<String, MetaModelServiceDescriptor> mmServiceIdToMMServiceClassDescriptorMap = new HashMap<String, MetaModelServiceDescriptor>();

			// First iteration to detect duplicated metamodel services and initialize the temporary map
			for (IConfigurationElement mmServiceConfigurationElement : extensionRegistry.getConfigurationElementsFor(EXTP_META_MODEL_SERVICES)) {
				try {
					if (NODE_SERVICE.equals(mmServiceConfigurationElement.getName())) {
						MetaModelServiceDescriptor mmServiceClassDescriptor = new MetaModelServiceDescriptor(mmServiceConfigurationElement);
						String mmServiceId = mmServiceClassDescriptor.getId();
						if (mmServiceIdToMMServiceClassDescriptorMap.containsKey(mmServiceId)) {
							logWarning(Messages.warning_serviceIdNotUnique, mmServiceId);
							continue;
						}
						mmServiceIdToMMServiceClassDescriptorMap.put(mmServiceId, mmServiceClassDescriptor);
					}
				} catch (Exception ex) {
					logError(ex);
				}
			}

			// Second iteration to register metamodel services
			for (MetaModelServiceDescriptor mmServiceClassDescriptor : mmServiceIdToMMServiceClassDescriptorMap.values()) {
				try {
					String override = mmServiceClassDescriptor.getOverride();
					if (override != null && !mmServiceIdToMMServiceClassDescriptorMap.containsKey(override)) {
						logWarning(Messages.warning_noServiceToOverride, mmServiceClassDescriptor.getId(), override);
						continue;
					}
					List<IMetaModelDescriptor> mmDescriptors = mmServiceClassDescriptor.getMetaModelDescriptors();
					Set<String> unknownMMDescriptorIdPatterns = mmServiceClassDescriptor.getUnknownMetaModelDescIdPatterns();
					// No descriptor, log warning
					if (mmDescriptors.isEmpty() && unknownMMDescriptorIdPatterns.isEmpty()) {
						logWarning(Messages.error_missingMetaModelDescriptor, mmServiceClassDescriptor.getContributorPluginId());
						continue;
					}
					if (!unknownMMDescriptorIdPatterns.isEmpty()) {
						logWarning(Messages.error_unknownMetaModel, mmServiceClassDescriptor.getContributorPluginId(), EXTP_META_MODEL_SERVICES,
								unknownMMDescriptorIdPatterns);
					}
					// Add Services
					for (IMetaModelDescriptor mmDescriptor : mmDescriptors) {
						addService(mmDescriptor, mmServiceClassDescriptor);
					}
				} catch (Exception ex) {
					logError(ex);
				}
			}

			// Clear temporary map
			mmServiceIdToMMServiceClassDescriptorMap.clear();
		}
	}

	private void addService(IMetaModelDescriptor mmDescriptor, MetaModelServiceDescriptor newMMServiceClassDescriptor) {
		try {
			Map<Class<IMetaModelService>, MetaModelServiceDescriptor> mmServicesForMetaModel = mmServices.get(mmDescriptor);
			if (mmServicesForMetaModel == null) {
				mmServicesForMetaModel = new HashMap<Class<IMetaModelService>, MetaModelServiceDescriptor>();
				mmServices.put(mmDescriptor, mmServicesForMetaModel);
			}

			MetaModelServiceDescriptor existingMMServiceClassDescriptor = mmServicesForMetaModel.get(newMMServiceClassDescriptor.getServiceType());
			if (existingMMServiceClassDescriptor == null) {
				// First metamodel service of given metamodel service type for specified metamodel
				mmServicesForMetaModel.put(newMMServiceClassDescriptor.getServiceType(), newMMServiceClassDescriptor);
			} else {
				if (newMMServiceClassDescriptor.overrides(existingMMServiceClassDescriptor)) {
					// Given metamodel service overrides another already registered metamodel service with same
					// metamodel service type for same metamodel
					mmServicesForMetaModel.put(newMMServiceClassDescriptor.getServiceType(), newMMServiceClassDescriptor);
				} else if (existingMMServiceClassDescriptor.overrides(newMMServiceClassDescriptor)) {
					// Another already registered metamodel service with same metamodel service type for same metamodel
					// overrides given metamodel service, nothing to do
				} else {
					// Conflicting metamodel services not overriding each other
					logWarning(Messages.error_metaModelServiceAlreadyExists, newMMServiceClassDescriptor.getServiceType(),
							mmDescriptor.getIdentifier());
				}
			}
		} catch (IllegalArgumentException ex) {
			logWarning(ex);
		}
	}

	/**
	 * For the given meta-model descriptor<code>descriptor</code>, obtain the requested service of given
	 * <code>serviceClass</code> class <br>
	 * NOTE: if the requested service is not implemented for the given descriptor, a warning is logged and
	 * <code>null</code> is returned
	 */
	@SuppressWarnings("unchecked")
	protected <T extends IMetaModelService> T getService(IMetaModelDescriptor mmDescriptor, Class<T> mmServiceType) {
		Map<Class<IMetaModelService>, MetaModelServiceDescriptor> mmServicesForMetaModel = getMetaModelServices().get(mmDescriptor);
		if (mmServicesForMetaModel != null) {
			MetaModelServiceDescriptor mmServiceClassDescriptor = mmServicesForMetaModel.get(mmServiceType);
			if (mmServiceClassDescriptor != null) {
				try {
					IMetaModelService mmService = mmServiceClassDescriptor.getInstance();
					if (mmServiceType.isInstance(mmService)) {
						return (T) mmService;
					} else {
						logError(Messages.error_invalidMetaModelServiceClass, mmServiceType.getName(), mmService.getClass().getName());
					}
				} catch (Throwable ex) {
					logError(ex);
				}
			}
		}
		return null;
	}

	private void logWarning(String msg, Object... objects) {
		logWarning(new RuntimeException(NLS.bind(msg, objects)));
	}

	private void logWarning(Throwable throwable) {
		logger.log(createWarningStatus(Activator.getDefault(), throwable));
	}

	private void logError(String msg, Object... objects) {
		logError(new RuntimeException(NLS.bind(msg, objects)));
	}

	private void logError(Throwable throwable) {
		logger.log(createErrorStatus(Activator.getDefault(), throwable));
	}
}
