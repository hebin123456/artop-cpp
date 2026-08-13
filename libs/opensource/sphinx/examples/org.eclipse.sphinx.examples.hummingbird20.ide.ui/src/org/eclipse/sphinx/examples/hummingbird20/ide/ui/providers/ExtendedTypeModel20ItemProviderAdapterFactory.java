/**
 * <copyright>
 *
 * Copyright (c) 2011-2014 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [447193] Enable transient item providers to be created through adapter factories
 *     itemis - [450882] Enable navigation to ancestor tree items in Model Explorer kind of model views
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.ide.ui.providers;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.edit.provider.Disposable;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.edit.TypeModel20ItemProviderAdapterFactory;

public class ExtendedTypeModel20ItemProviderAdapterFactory extends TypeModel20ItemProviderAdapterFactory {

	protected Disposable disposable = new Disposable();

	@Override
	public Adapter createComponentTypeAdapter() {
		if (componentTypeItemProvider == null) {
			componentTypeItemProvider = new ExtendedComponentTypeItemProvider(this);
		}
		return componentTypeItemProvider;
	}

	@Override
	public Adapter createParameterAdapter() {
		if (parameterItemProvider == null) {
			parameterItemProvider = new ExtendedParameterItemProvider(this);
		}
		return parameterItemProvider;
	}

	@Override
	public Adapter createPortAdapter() {
		if (portItemProvider == null) {
			portItemProvider = new ExtendedPortItemProvider(this);
		}
		return portItemProvider;
	}

	@Override
	public Adapter createPlatformAdapter() {
		if (platformItemProvider == null) {
			platformItemProvider = new ExtendedPlatformItemProvider(this);
		}
		return platformItemProvider;
	}

	@Override
	public Adapter createInterfaceAdapter() {
		if (interfaceItemProvider == null) {
			interfaceItemProvider = new ExtendedInterfaceItemProvider(this);
		}
		return interfaceItemProvider;
	}

	@Override
	public Object adapt(Object target, Object type) {
		Object adapter = TransientItemProvider.AdapterFactoryHelper.adapt(target, type, this);
		if (adapter != null) {
			disposable.add(adapter);
			return adapter;
		}
		return super.adapt(target, type);
	}

	@Override
	protected Adapter createAdapter(Notifier target, Object type) {
		if (type == ComponentTypesItemProvider.class) {
			return new ComponentTypesItemProvider(this);
		}
		if (type == InterfacesItemProvider.class) {
			return new InterfacesItemProvider(this);
		}
		if (type == ParametersItemProvider.class) {
			return new ParametersItemProvider(this);
		}
		if (type == PortsItemProvider.class) {
			return new PortsItemProvider(this);
		}
		return super.createAdapter(target, type);
	}

	@Override
	public void dispose() {
		disposable.dispose();
		super.dispose();
	}
}
