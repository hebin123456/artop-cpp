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
package org.eclipse.sphinx.emf.edit;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.internal.EcorePerformanceStats;
import org.eclipse.sphinx.emf.internal.EcorePerformanceStats.EcoreEvent;
import org.eclipse.sphinx.emf.internal.properties.BasicPropertyType;
import org.eclipse.sphinx.emf.internal.properties.IPropertyType;
import org.eclipse.sphinx.emf.messages.EMFMessages;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.StatusUtil;
import org.osgi.framework.Bundle;

/**
 * @since 0.7.0
 */
public class CustomCommandRegistry {

	public static final CustomCommandRegistry INSTANCE = new CustomCommandRegistry();

	private static final String NODE_PROPERTY = "property"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_OWNER_TYPE = "ownerType"; //$NON-NLS-1$
	private static final String ATTR_FEATURE_NAME = "featureName"; //$NON-NLS-1$
	private static final String ATTR_VALUE_TYPE = "valueType"; //$NON-NLS-1$

	private static final String EXTP_CUSTOM_COMMANDS = "customCommands"; //$NON-NLS-1$
	private static final String NODE_CUSTOM_COMMAND = "customCommand"; //$NON-NLS-1$

	protected Map<Class<?>, Map<IPropertyType, Map<Class<? extends Command>, Class<? extends Command>>>> customCommandTypes = new HashMap<Class<?>, Map<IPropertyType, Map<Class<? extends Command>, Class<? extends Command>>>>();

	private CustomCommandRegistry() {
		readCustomCommandContributions();
	}

	protected String getFeatureName(Object feature) {
		return feature instanceof EStructuralFeature ? ((EStructuralFeature) feature).getName() : null;
	}

	protected Class<?> getValueType(Collection<?> collection) {
		if (collection != null) {
			Object next = collection.iterator().next();
			Class<?> clazz = collection.size() > 0 ? next.getClass() : null;
			if (clazz != null) {
				Class<?>[] interfaces = clazz.getInterfaces();
				if (interfaces.length > 0) {
					return interfaces[0];
				} else if (next instanceof FeatureMap.Entry) {
					Object unwrappedEntry = AdapterFactoryEditingDomain.unwrap(next);
					return unwrappedEntry.getClass();
				} else {
					return clazz;
				}
			}
		}
		return null;
	}

	public Command createCustomCommand(EditingDomain domain, CommandParameter commandParameter, Class<? extends Command> commandClass) {
		// TODO Add asserts to make sure that commandParameter is correctly initialized

		IPropertyType propertyType = new BasicPropertyType(getFeatureName(commandParameter.getFeature()),
				getValueType(commandParameter.getCollection()));

		Class<? extends Command> commandType = getCustomCommandType(commandParameter.getOwner().getClass(), propertyType, commandClass);
		if (commandType != null) {
			try {
				// TODO aakar : Add other command types when needed
				if (commandClass == AddCommand.class) {
					Constructor<? extends Command> constructor = commandType.getConstructor(EditingDomain.class, EObject.class, EReference.class,
							Collection.class, Integer.class);
					return constructor.newInstance(domain, commandParameter.getOwner(), commandParameter.getFeature(),
							commandParameter.getCollection(), commandParameter.getIndex());
				} else if (commandClass == RemoveCommand.class) {
					Constructor<? extends Command> constructor = commandType.getConstructor(EditingDomain.class, EObject.class,
							EStructuralFeature.class, Collection.class);
					return constructor.newInstance(domain, commandParameter.getOwner(), commandParameter.getFeature(),
							commandParameter.getCollection());
				} else if (commandClass == SetCommand.class) {
					Constructor<? extends Command> constructor = commandType.getConstructor(EditingDomain.class, EObject.class,
							EStructuralFeature.class, Object.class, Integer.class);
					return constructor.newInstance(domain, commandParameter.getOwner(), commandParameter.getFeature(),
							commandParameter.getCollection(), Integer.class);
				}
			} catch (Exception ex) {
				throw new WrappedException(ex);
			}
		}

		return null;
	}

	protected final Class<? extends Command> getCustomCommandType(Class<?> ownerType, IPropertyType propertyType,
			Class<? extends Command> commandClass) {
		Assert.isNotNull(ownerType);
		Assert.isNotNull(propertyType);
		Assert.isNotNull(commandClass);

		if (customCommandTypes.containsKey(ownerType)) {
			Map<IPropertyType, Map<Class<? extends Command>, Class<? extends Command>>> commandTypesForOwnerType = customCommandTypes.get(ownerType);
			if (commandTypesForOwnerType.containsKey(propertyType)) {
				Map<Class<? extends Command>, Class<? extends Command>> commandTypesForPropertyType = commandTypesForOwnerType.get(propertyType);
				Class<? extends Command> cmdClass = commandTypesForPropertyType.get(commandClass);
				if (cmdClass == null) {
					Map<Class<? extends Command>, Class<? extends Command>> findCommandTypesForOwnerSuperType = findCommandTypesForOwnerSuperType(
							ownerType, propertyType);
					if (findCommandTypesForOwnerSuperType != null) {
						commandTypesForOwnerType.put(propertyType, findCommandTypesForOwnerSuperType);
						return findCommandTypesForOwnerSuperType.get(commandClass);
					}
				}
				return cmdClass;
			}
			Map<Class<? extends Command>, Class<? extends Command>> commandTypesForPropertyType = findCommandTypesForOwnerSuperType(ownerType,
					propertyType);
			if (commandTypesForPropertyType == null) {
				commandTypesForPropertyType = new HashMap<Class<? extends Command>, Class<? extends Command>>();
			}
			commandTypesForOwnerType.put(propertyType, commandTypesForPropertyType);
			return commandTypesForPropertyType.get(commandClass);
		}
		Map<IPropertyType, Map<Class<? extends Command>, Class<? extends Command>>> commandTypesForOwnerType = new HashMap<IPropertyType, Map<Class<? extends Command>, Class<? extends Command>>>();
		customCommandTypes.put(ownerType, commandTypesForOwnerType);
		Map<Class<? extends Command>, Class<? extends Command>> commandTypesForPropertyType = findCommandTypesForOwnerSuperType(ownerType,
				propertyType);
		if (commandTypesForPropertyType == null) {
			commandTypesForPropertyType = new HashMap<Class<? extends Command>, Class<? extends Command>>();
		}
		commandTypesForOwnerType.put(propertyType, commandTypesForPropertyType);
		return commandTypesForPropertyType.get(commandClass);
	}

	private Map<Class<? extends Command>, Class<? extends Command>> findCommandTypesForOwnerSuperType(Class<?> ownerType, IPropertyType propertyType) {
		Assert.isNotNull(ownerType);
		Assert.isNotNull(propertyType);

		// Try to find matching super class
		Set<Class<?>> superTypes = new HashSet<Class<?>>();
		if (!ownerType.isInterface()) {
			Class<?> superClass = ownerType.getSuperclass();
			if (superClass != null) {
				superTypes.add(superClass);
				if (customCommandTypes.containsKey(superClass)) {
					Map<IPropertyType, Map<Class<? extends Command>, Class<? extends Command>>> commandTypesForOwnerType = customCommandTypes
							.get(superClass);
					Map<Class<? extends Command>, Class<? extends Command>> commandTypesForPropertyType = getCommandTypesForPropertyType(
							commandTypesForOwnerType, propertyType);
					if (commandTypesForPropertyType != null) {
						return commandTypesForPropertyType;
					}
				}
			}
		}

		// Try to find matching interface
		for (Class<?> interfaze : ownerType.getInterfaces()) {
			superTypes.add(interfaze);
			if (customCommandTypes.containsKey(interfaze)) {
				Map<IPropertyType, Map<Class<? extends Command>, Class<? extends Command>>> commandTypesForOwnerType = customCommandTypes
						.get(interfaze);
				Map<Class<? extends Command>, Class<? extends Command>> commandTypesForPropertyType = getCommandTypesForPropertyType(
						commandTypesForOwnerType, propertyType);
				if (commandTypesForPropertyType != null) {
					return commandTypesForPropertyType;
				}
			}
		}

		// Try to find matching super type of super class and interfaces
		for (Class<?> superType : superTypes) {
			Map<Class<? extends Command>, Class<? extends Command>> commandTypesForPropertyType = findCommandTypesForOwnerSuperType(superType,
					propertyType);
			if (commandTypesForPropertyType != null) {
				return commandTypesForPropertyType;
			}
		}

		return null;
	}

	private Map<Class<? extends Command>, Class<? extends Command>> getCommandTypesForPropertyType(
			Map<IPropertyType, Map<Class<? extends Command>, Class<? extends Command>>> commandTypesForOwnerType, IPropertyType propertyType) {
		Assert.isNotNull(commandTypesForOwnerType);
		Assert.isNotNull(propertyType);

		if (commandTypesForOwnerType.containsKey(propertyType)) {
			return commandTypesForOwnerType.get(propertyType);
		} else {
			IPropertyType propertyTypeIgnoringValueType = new BasicPropertyType(propertyType.getFeatureName(), null);
			if (commandTypesForOwnerType.containsKey(propertyTypeIgnoringValueType)) {
				return commandTypesForOwnerType.get(propertyTypeIgnoringValueType);
			}
		}
		return null;
	}

	private void readCustomCommandContributions() {
		/* <<< */
		// boolean profiling = System.getProperty(JVMArguments.PROFILING) != null;
		// long cpuTime = 0;
		// if (profiling) {
		// cpuTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		// }
		/* --- */
		EcorePerformanceStats.INSTANCE.startEvent(EcoreEvent.EVENT_READ_CONTRIBUTIONS_CUSTOM_COMMANDS, this.getClass().getSimpleName());
		/* >>> */

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] contributions = registry.getConfigurationElementsFor(Activator.getPlugin().getSymbolicName(), EXTP_CUSTOM_COMMANDS);
		for (IConfigurationElement contribution : contributions) {
			try {
				readCustomCommandContribution(contribution);
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}

		/* <<< */
		// if (profiling) {
		// long cpuTimeDiff = (ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - cpuTime) / 1000000;
		// PlatformLogUtil.logAsInfo(Activator.getPlugin(),
		// NLS.bind(Messages.info_completedPropertyContributionsReading, Activator.getPlugin()
		// .getSymbolicName()
		//					+ "." + EXTP_CUSTOM_COMMANDS, cpuTimeDiff)); //$NON-NLS-1$
		// }
		/* --- */
		// TODO Try to reuse the specific message previously defined.
		EcorePerformanceStats.INSTANCE.endEvent(EcoreEvent.EVENT_READ_CONTRIBUTIONS_CUSTOM_COMMANDS, this.getClass().getSimpleName());
		EcorePerformanceStats.INSTANCE.closeAndLogCurrentContext();
		/* >>> */
	}

	/**
	 * <table>
	 * <tr valign="top">
	 * <td><b>Note:</b>&nbsp;&nbsp;</td>
	 * <td>Do not use <code>java.lang.Class#classForName(className)</code> when trying to retrieved a class from its
	 * name because it take to much time when expected class is not directly visible (<em>i.e.</em> not declared in
	 * plug-in itself nor in dependencies). Further more, this way of class retrieval implies to add: <br>
	 * <code>&nbsp;&nbsp;<b>Eclipse-BuddyPolicy:</b> dependent</code><br>
	 * in plug-in manifest; whereas such an option is not recommended.
	 * <p>
	 * Another solution to this problem would have been to use the method <code>createExecutableExtension</code> on each
	 * contribution. This method returns a new instance of the contributed class. But our need was to keep the type of
	 * the contribution, that is the contributed <code>java.lang.Class</code>.
	 * <p>
	 * The more efficient solution is to delegate the loading of the contributed class to the class loader of the
	 * contributor bundle by using <code>org.osgi.framework.Bundle#loadClass(String className)</code>.</td>
	 * </tr>
	 * </table>
	 * 
	 * @param contribution
	 * @throws ClassNotFoundException
	 * @throws CoreException
	 * @throws NoSuchFieldException
	 */
	protected void readCustomCommandContribution(IConfigurationElement contribution) throws ClassNotFoundException, CoreException,
			NoSuchFieldException {
		if (NODE_CUSTOM_COMMAND.equals(contribution.getName())) {

			// Retrieves the contributor bundle in order to make it load itself the contributed 'T' class.
			Bundle contributorBundle = ExtendedPlatform.loadContributorBundle(contribution);
			// The name of the contributed class.
			String contributionClassName = contribution.getAttribute(ATTR_CLASS);
			// Delegates the loading of the contribution class to the class loader of the contributor bundle.
			Class<?> clazz = contributorBundle.loadClass(contributionClassName);

			if (Command.class.isAssignableFrom(clazz)) {
				@SuppressWarnings("unchecked")
				Class<? extends Command> customCommandClass = (Class<? extends Command>) clazz;

				IConfigurationElement[] childElements = contribution.getChildren(NODE_PROPERTY);

				for (IConfigurationElement childElement : childElements) {
					// Retrieve the property contribution type's owner type
					String ownerTypeName = childElement.getAttribute(ATTR_OWNER_TYPE);
					Class<?> ownerType = contributorBundle.loadClass(ownerTypeName);

					// Retrieve the property contribution type's property type (= featureName + valueType)
					String featureName = childElement.getAttribute(ATTR_FEATURE_NAME);
					String valueTypeName = childElement.getAttribute(ATTR_VALUE_TYPE);
					Class<?> valueType = null;
					if (valueTypeName != null) {
						valueType = contributorBundle.loadClass(valueTypeName);
					}
					IPropertyType propertyType = new BasicPropertyType(featureName, valueType);

					// Register property contribution type upon given owner and property type
					Map<IPropertyType, Map<Class<? extends Command>, Class<? extends Command>>> commandTypesForOwnerType;
					if (customCommandTypes.containsKey(ownerType)) {
						commandTypesForOwnerType = customCommandTypes.get(ownerType);
					} else {
						commandTypesForOwnerType = new HashMap<IPropertyType, Map<Class<? extends Command>, Class<? extends Command>>>();
						customCommandTypes.put(ownerType, commandTypesForOwnerType);
					}
					Map<Class<? extends Command>, Class<? extends Command>> commandTypesForPropertyType = commandTypesForOwnerType.get(propertyType);
					if (commandTypesForPropertyType == null) {
						commandTypesForPropertyType = new HashMap<Class<? extends Command>, Class<? extends Command>>();
						commandTypesForOwnerType.put(propertyType, commandTypesForPropertyType);
					}
					// TODO : aakar Add here other command types when needed
					if (AddCommand.class.isAssignableFrom(customCommandClass)) {
						commandTypesForPropertyType.put(AddCommand.class, customCommandClass);
					} else if (RemoveCommand.class.isAssignableFrom(customCommandClass)) {
						commandTypesForPropertyType.put(RemoveCommand.class, customCommandClass);
					} else if (SetCommand.class.isAssignableFrom(customCommandClass)) {
						commandTypesForPropertyType.put(SetCommand.class, customCommandClass);
					}
				}
			} else {
				String[] args = new String[] { ATTR_CLASS, NODE_CUSTOM_COMMAND,
						contribution.getDeclaringExtension().getExtensionPointUniqueIdentifier(), contribution.getContributor().getName(),
						Command.class.getName() };
				String msg = NLS.bind(EMFMessages.error_unexpectedImplementationOfElementAttributeInContribution, args);
				IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), new RuntimeException(msg));
				throw new CoreException(status);
			}
		}
	}
}
