/**
 * <copyright>
 *
 * Copyright (c) 2011-2014 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [393312] Make sure that transient item providers created by extended item providers can be used before the getChildren() method of the latter has been called
 *     itemis - [447193] Enable transient item providers to be created through adapter factories
 *     itemis - [450882] Enable navigation to ancestor tree items in Model Explorer kind of model views
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.ide.ui.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandWrapper;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.StyledString;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.edit.ComponentItemProvider;

public class ExtendedComponentItemProvider extends ComponentItemProvider {

	public ExtendedComponentItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	@Override
	public Object getStyledText(Object object) {
		String label = ((Component) object).getName();
		StyledString styledLabel = new StyledString();
		if (label == null || label.length() == 0) {
			styledLabel.append(getString("_UI_Component_type"), StyledString.Style.QUALIFIER_STYLER); //$NON-NLS-1$
		} else {
			styledLabel.append(getString("_UI_Component_type"), StyledString.Style.QUALIFIER_STYLER).append(" ").append(label, //$NON-NLS-1$ //$NON-NLS-2$
					IHummingbird20Styles.INSTANCE_STYLE);
		}
		return styledLabel;
	}

	@Override
	public Object getParent(Object object) {
		Object parent = super.getParent(object);
		return adapterFactory.adapt(parent, ComponentsItemProvider.class);
	}

	@Override
	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
		if (childrenFeatures == null) {
			super.getChildrenFeatures(object);

			childrenFeatures.remove(InstanceModel20Package.Literals.COMPONENT__PARAMETER_VALUES);
			childrenFeatures.remove(InstanceModel20Package.Literals.COMPONENT__OUTGOING_CONNECTIONS);
		}
		return childrenFeatures;
	}

	@Override
	public Collection<?> getChildren(Object object) {
		List<Object> children = new ArrayList<Object>(super.getChildren(object));
		children.add(adapterFactory.adapt(object, ParameterValuesItemProvider.class));
		children.add(adapterFactory.adapt(object, OutgoingConnectionsItemProvider.class));
		return children;
	}

	@Override
	protected Command createAddCommand(EditingDomain domain, EObject owner, EStructuralFeature feature, Collection<?> collection, int index) {
		return createWrappedCommand(super.createAddCommand(domain, owner, feature, collection, index), owner, feature);
	}

	@Override
	protected Command createRemoveCommand(EditingDomain domain, EObject owner, EStructuralFeature feature, Collection<?> collection) {
		return createWrappedCommand(super.createRemoveCommand(domain, owner, feature, collection), owner, feature);
	}

	protected Command createWrappedCommand(Command command, final EObject owner, final EStructuralFeature feature) {
		if (feature == InstanceModel20Package.Literals.COMPONENT__PARAMETER_VALUES
				|| feature == InstanceModel20Package.Literals.COMPONENT__OUTGOING_CONNECTIONS) {
			return new CommandWrapper(command) {
				@Override
				public Collection<?> getAffectedObjects() {
					Collection<?> affected = super.getAffectedObjects();
					if (affected.contains(owner)) {
						affected = Collections.singleton(feature == InstanceModel20Package.Literals.COMPONENT__PARAMETER_VALUES
								? adapterFactory.adapt(owner, ParameterValuesItemProvider.class)
								: adapterFactory.adapt(owner, OutgoingConnectionsItemProvider.class));
					}
					return affected;
				}
			};
		}
		return command;
	}
}
