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
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.ide.ui.providers;

import java.util.Collection;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.UnexecutableCommand;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.StyledString;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.examples.hummingbird20.edit.Activator;
import org.eclipse.sphinx.examples.hummingbird20.ide.ui.internal.messages.Messages;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

public class ParametersItemProvider extends TransientItemProvider {

	public ParametersItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	@Override
	public String getText(Object object) {
		return Messages.label_Parameters_TransientNode;
	}

	@Override
	public Object getStyledText(Object object) {
		return new StyledString(getText(object), IHummingbird20Styles.SUB_GROUP_STYLE);
	}

	@Override
	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
		if (childrenFeatures == null) {
			super.getChildrenFeatures(object);

			childrenFeatures.add(TypeModel20Package.Literals.COMPONENT_TYPE__PARAMETERS);
		}
		return childrenFeatures;
	}

	@Override
	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
		super.collectNewChildDescriptors(newChildDescriptors, object);

		newChildDescriptors
				.add(createChildParameter(TypeModel20Package.Literals.COMPONENT_TYPE__PARAMETERS, TypeModel20Factory.eINSTANCE.createParameter()));
	}

	@Override
	protected Command createDragAndDropCommand(EditingDomain domain, Object owner, float location, int operations, int operation,
			Collection<?> collection) {
		if (new AddCommand(domain, (EObject) owner, TypeModel20Package.Literals.COMPONENT_TYPE__PARAMETERS, collection).canExecute()) {
			return super.createDragAndDropCommand(domain, owner, location, operations, operation, collection);
		}
		return UnexecutableCommand.INSTANCE;
	}

	/**
	 * Returns the resource locator for this item provider's resources.
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return Activator.INSTANCE;
	}
}
