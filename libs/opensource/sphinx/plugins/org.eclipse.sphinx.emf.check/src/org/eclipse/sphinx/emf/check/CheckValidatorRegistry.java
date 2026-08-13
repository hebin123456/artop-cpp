/**
 * <copyright>
 *
 * Copyright (c) 2014-2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [455185] Check Framework incompatible with QVTO metamodel
 *     itemis - [458403] CheckValidatorRegistry.getCheckModelURI(String) should be null-safe
 *     itemis - [458405] CheckValidatorRegistry.register(*) should be public
 *     itemis - [458921] Newly introduced registries for metamodel serives, check validators and workflow contributors are not standalone-safe
 *     itemis - [458976] Validators are not singleton when they implement checks for different EPackages
 *     itemis - [461051] API to get all registered check validators
 *     itemis - [478811] Check validation may compromise EMF Validation-based validation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.check.catalog.Catalog;
import org.eclipse.sphinx.emf.check.internal.Activator;
import org.eclipse.sphinx.emf.check.internal.CheckValidatorDescriptor;
import org.eclipse.sphinx.emf.check.internal.EPackageMappings;
import org.eclipse.sphinx.emf.check.util.CheckValidationUtil;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.validation.ICompositeValidator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.StatusUtil;

/**
 * A validation registry singleton which backs a standard EMF validation
 * {@link org.eclipse.emf.ecore.EValidator.Registry registry}. When a check validator is called for the first time, the
 * registry reads all the contributed check validators and initializes its internal map, which is then used to retrieve
 * check catalogs from validator class names. Check validators are contributed through the
 * <code>org.eclipse.sphinx.emf.check.chekvalidators</code> extension point. A check validator contribution is a couple
 * of a validator class and optionally a check catalog. By default, the {@link org.eclipse.emf.ecore.EPackage ePackage}
 * affected by the validator is inferred from the set of the annotated method parameters provided by the validator.
 */
public class CheckValidatorRegistry {

	private static final String EXTP_CHECK_VALIDATORS = "org.eclipse.sphinx.emf.check.checkvalidators"; //$NON-NLS-1$
	private static final String NODE_VALIDATOR = "validator"; //$NON-NLS-1$
	private static final String NODE_EPACKAGE_MAPPING = "ePackageMapping"; //$NON-NLS-1$
	private static final String ATTR_EOBJECT_WRAPPER_PACKAGE_NAME = "eObjectWrapperPackageName"; //$NON-NLS-1$
	private static final String ATTR_EPACKAGE_NS_URI = "ePackageNsURI"; //$NON-NLS-1$

	/**
	 * The singleton instance of this registry.
	 */
	public static final CheckValidatorRegistry INSTANCE = new CheckValidatorRegistry(Platform.getExtensionRegistry(), EValidator.Registry.INSTANCE,
			PlatformLogUtil.getLog(Activator.getPlugin()));

	protected Map<ICheckValidator, URI> checkValidatorToCheckCatalogURIMap = null;
	protected Map<URI, Set<ICheckValidator>> checkCatalogURIToCheckValidatorsMap = null;

	protected Map<URI, Catalog> uriToCheckCatalogMap = new HashMap<URI, Catalog>();

	protected IExtensionRegistry extensionRegistry;

	protected EValidator.Registry eValidatorRegistry;

	private ILog logger;

	protected CheckValidatorRegistry(IExtensionRegistry extensionRegistry, EValidator.Registry eValidatorRegistry, ILog logger) {
		Assert.isNotNull(extensionRegistry);
		Assert.isNotNull(eValidatorRegistry);
		Assert.isNotNull(logger);

		this.extensionRegistry = extensionRegistry;
		this.eValidatorRegistry = eValidatorRegistry;
		this.logger = logger;
	}

	private EValidator.Registry getEValidatorRegistry() {
		initialize();
		return eValidatorRegistry;
	}

	private Map<ICheckValidator, URI> getCheckValidatorToCheckCatalogURIMap() {
		initialize();
		return checkValidatorToCheckCatalogURIMap != null ? checkValidatorToCheckCatalogURIMap : Collections.<ICheckValidator, URI> emptyMap();
	}

	private Map<URI, Set<ICheckValidator>> getCheckCatalogURIToCheckValidatorsMap() {
		initialize();
		return checkCatalogURIToCheckValidatorsMap != null ? checkCatalogURIToCheckValidatorsMap : Collections.<URI, Set<ICheckValidator>> emptyMap();
	}

	private void initialize() {
		if (extensionRegistry == null) {
			return;
		}

		if (checkValidatorToCheckCatalogURIMap == null || checkCatalogURIToCheckValidatorsMap == null) {
			checkValidatorToCheckCatalogURIMap = new HashMap<ICheckValidator, URI>();
			checkCatalogURIToCheckValidatorsMap = new HashMap<URI, Set<ICheckValidator>>();

			// Create a temporary objects
			Map<String, CheckValidatorDescriptor> checkValidatorClassNameToCheckValidatorDescriptorMap = new HashMap<String, CheckValidatorDescriptor>();
			EPackageMappings ePackageMappings = new EPackageMappings();

			// First iteration to detect duplicate check validator contributions and initialize check validator
			// descriptor map
			for (IConfigurationElement checkValidatorConfigurationElement : extensionRegistry.getConfigurationElementsFor(EXTP_CHECK_VALIDATORS)) {
				try {
					if (NODE_VALIDATOR.equals(checkValidatorConfigurationElement.getName())) {
						CheckValidatorDescriptor checkValidatorDescriptor = new CheckValidatorDescriptor(checkValidatorConfigurationElement);
						String checkValidatorClassName = checkValidatorDescriptor.getClassName();
						if (checkValidatorClassNameToCheckValidatorDescriptorMap.containsKey(checkValidatorClassName)) {
							logWarning("Duplicate validator contribution found for: " + checkValidatorClassName); //$NON-NLS-1$
							continue;
						}
						checkValidatorClassNameToCheckValidatorDescriptorMap.put(checkValidatorClassName, checkValidatorDescriptor);
					} else if (NODE_EPACKAGE_MAPPING.equals(checkValidatorConfigurationElement.getName())) {
						String javaPackageName = checkValidatorConfigurationElement.getAttribute(ATTR_EOBJECT_WRAPPER_PACKAGE_NAME);
						String ePackageNsURI = checkValidatorConfigurationElement.getAttribute(ATTR_EPACKAGE_NS_URI);
						Object ePackageObject = EPackage.Registry.INSTANCE.get(ePackageNsURI);
						if (ePackageObject == null) {
							logError("Unable to find EPackage for ", ePackageNsURI); //$NON-NLS-1$
						}
						ePackageMappings.put(javaPackageName, ePackageObject);
					}
				} catch (Exception ex) {
					logError(ex);
				}
			}

			// Second iteration to register the contributed check validators with EMF's EValidator registry and check
			// catalog URI map
			for (CheckValidatorDescriptor checkValidatorDescriptor : checkValidatorClassNameToCheckValidatorDescriptorMap.values()) {
				try {
					// Create instance of contributed check validator
					ICheckValidator validator = checkValidatorDescriptor.newInstance();

					// Register check validator upon all EPackages that are affected by the former
					Set<Class<?>> classesUnderCheck = findClassesUnderCheck(validator.getClass());
					Set<EPackage> affectedEPackages = findAffectedEPackages(classesUnderCheck, ePackageMappings);
					for (EPackage affectedEPackage : affectedEPackages) {
						addValidator(affectedEPackage, validator);
					}

					URI catalogURI = checkValidatorDescriptor.getCatalogURI();
					checkValidatorToCheckCatalogURIMap.put(validator, catalogURI);
					if (catalogURI != null) {
						Set<ICheckValidator> checkValidators = checkCatalogURIToCheckValidatorsMap.get(catalogURI);
						if (checkValidators == null) {
							checkValidators = new HashSet<ICheckValidator>();
							checkCatalogURIToCheckValidatorsMap.put(catalogURI, checkValidators);
						}
						checkValidators.add(validator);
					}
				} catch (Exception ex) {
					logError(ex);
				}
			}

			// Clear temporary objects
			checkValidatorClassNameToCheckValidatorDescriptorMap.clear();
			ePackageMappings.clear();
		}
	}

	/**
	 * Retrieves the classes (typically metamodel element types) from the arguments of the check validator methods that
	 * are annotated with @Check.
	 *
	 * @param validatorClass
	 * @return
	 */
	private Set<Class<?>> findClassesUnderCheck(Class<? extends ICheckValidator> validatorClass) {
		Assert.isNotNull(validatorClass);

		Set<Class<?>> classesUnderCheck = new HashSet<Class<?>>();
		Collection<Method> methods = CheckValidationUtil.getDeclaredCheckMethods(validatorClass);
		for (Method method : methods) {
			Annotation[] annotations = method.getAnnotations();
			for (Annotation annotation : annotations) {
				Class<? extends Annotation> annotationType = annotation.annotationType();
				if (annotationType.equals(Check.class)) {
					Class<?>[] parameterTypes = method.getParameterTypes();
					if (parameterTypes.length > 0) {
						classesUnderCheck.add(parameterTypes[0]);
					}
				}
			}
		}
		return classesUnderCheck;
	}

	/**
	 * Retrieves the set {@link EPackage}s behind the given set of classes to be checked.
	 *
	 * @param classesUnderCheck
	 * @return
	 */
	private Set<EPackage> findAffectedEPackages(Set<Class<?>> classesUnderCheck, EPackageMappings ePackageMappings) {
		Assert.isNotNull(classesUnderCheck);

		Set<EPackage> affectedEPackages = new HashSet<EPackage>();
		for (Class<?> classUnderCheck : classesUnderCheck) {
			EPackage ePackage = findAffectedEPackage(classUnderCheck, ePackageMappings);
			if (ePackage == null) {
				logError("Unable to find EPackage for ", classUnderCheck.getName()); //$NON-NLS-1$
				continue;
			}
			affectedEPackages.add(ePackage);
		}
		return affectedEPackages;
	}

	private EPackage findAffectedEPackage(Class<?> classUnderCheck, EPackageMappings ePackageMappings) {
		Assert.isNotNull(ePackageMappings);

		EPackage ePackage = ePackageMappings.getEPackageFor(classUnderCheck);
		if (ePackage != null) {
			return ePackage;
		}

		ePackage = EObjectUtil.findEPackage(classUnderCheck);
		if (ePackage != null) {
			ePackageMappings.put(classUnderCheck.getName(), ePackage);
		}
		return ePackage;
	}

	public void addValidator(EPackage ePackage, EValidator validator) {
		// Retrieve existing validator for given EPackage if any
		EValidator existingValidator = eValidatorRegistry.getEValidator(ePackage);

		// No validator for given EPackage so far?
		if (existingValidator == null) {
			// Register given check validator as is
			eValidatorRegistry.put(ePackage, validator);
		}
		// Existing validator being a composite validator?
		else if (existingValidator instanceof ICompositeValidator) {
			// Add given check validator as additional child validator
			((ICompositeValidator) existingValidator).addValidator(validator);
		}
		// Existing validator is an EValidator or another check validator
		else {
			// Replace existing validator by a composite validator containing existing validator and given check
			// validator as child validators
			ICompositeValidator compositeValidator = new CompositeValidator();
			compositeValidator.addValidator(existingValidator);
			compositeValidator.addValidator(validator);
			eValidatorRegistry.put(ePackage, compositeValidator);
		}
	}

	/**
	 * Retrieve a check-based validator contributed through the <code>org.eclipse.sphinx.emf.check.chekvalidators</code>
	 * extension point for the given package.
	 *
	 * @param ePackage
	 * @return
	 * @throws CoreException
	 */
	public EValidator getValidator(EPackage ePackage) {
		EValidator eValidator = getEValidatorRegistry().getEValidator(ePackage);
		if (eValidator instanceof ICheckValidator || eValidator instanceof ICompositeValidator) {
			return eValidator;
		}
		return null;
	}

	/**
	 * Returns the URI of a check catalog associated with given check validator.
	 *
	 * @param checkValidator
	 * @return
	 */
	public URI getCheckCatalogURI(ICheckValidator checkValidator) {
		return getCheckValidatorToCheckCatalogURIMap().get(checkValidator);
	}

	public Catalog getCheckCatalog(ICheckValidator checkValidator) {
		URI checkCatalogURI = getCheckCatalogURI(checkValidator);
		if (checkCatalogURI != null) {
			return loadCheckCatalog(checkCatalogURI);
		}
		return null;
	}

	private Catalog loadCheckCatalog(URI checkCatalogURI) {
		Catalog catalog = uriToCheckCatalogMap.get(checkCatalogURI);
		if (catalog == null) {
			EObject eObject = EcoreResourceUtil.loadEObject(null, checkCatalogURI.appendFragment("/")); //$NON-NLS-1$
			if (!(eObject instanceof Catalog)) {
				throw new IllegalStateException("Unable to find the check catalog for URI '" + checkCatalogURI + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			catalog = (Catalog) eObject;
			uriToCheckCatalogMap.put(checkCatalogURI, catalog);
		}
		return catalog;
	}

	public Collection<Catalog> getCheckCatalogs() {
		Set<URI> allCalalogURIs = getCheckCatalogURIToCheckValidatorsMap().keySet();
		for (URI uri : allCalalogURIs) {
			if (uriToCheckCatalogMap.get(uri) == null) {
				loadCheckCatalog(uri);
			}
		}
		return uriToCheckCatalogMap.values();
	}

	public Collection<ICheckValidator> getCheckValidators() {
		return getCheckValidatorToCheckCatalogURIMap().keySet();
	}

	private void logWarning(String msgId, Object... objects) {
		logWarning(new RuntimeException(NLS.bind(msgId, objects)));
	}

	private void logWarning(Throwable throwable) {
		logger.log(StatusUtil.createWarningStatus(Activator.getDefault(), throwable));
	}

	private void logError(String msgId, Object... objects) {
		logError(new RuntimeException(NLS.bind(msgId, objects)));
	}

	private void logError(Throwable throwable) {
		logger.log(StatusUtil.createErrorStatus(Activator.getDefault(), throwable));
	}
}