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

import java.util.List;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.transaction.NotificationFilter;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.sphinx.emf.util.EObjectUtil;

/**
 * Detects {@link EObject model object}s that have been removed from their
 * {@link org.eclipse.emf.ecore.EObject#eResource() containing resource} and/or their {@link EObject#eContainer
 * containing object} and turns them as well as all their directly and indirectly contained objects into proxies. This
 * offers the following benefits:
 * <ul>
 * <li>After removal of an {@link EObject model object} from its container, all other {@link EObject model object}s that
 * are still referencing the removed {@link EObject model object} will know that the latter is no longer available but
 * can still figure out its type and {@link URI}.</li>
 * <li>If a new {@link EObject model object} with the same type as the removed one is added to the same container again
 * later on, the proxies which the other {@link EObject model object}s are still referencing will be resolved as usual
 * and therefore get automatically replaced by the newly added {@link EObject model object}.</li>
 * <li>In big models, this approach can yield significant advantages in terms of performance because it helps avoiding
 * full deletions of {@link EObject model object}s involving expensive searches for their cross-references and those of
 * all their directly and indirectly contained objects. It does all the same not lead to references pointing at
 * "floating" {@link EObject model object}s, i.e., {@link EObject model object}s that are not directly or indirectly
 * contained in a resource.</li>
 * </ul>
 */

public class LocalProxyChangeListener extends ResourceSetListenerImpl {
	/**
	 * Default constructor.
	 */
	public LocalProxyChangeListener() {
		super(NotificationFilter.createEventTypeFilter(Notification.ADD_MANY).or(
				NotificationFilter.createEventTypeFilter(Notification.REMOVE_MANY).or(
						NotificationFilter.createEventTypeFilter(Notification.ADD).or(
								NotificationFilter.createEventTypeFilter(Notification.REMOVE).or(
										NotificationFilter.createEventTypeFilter(Notification.SET).or(
												NotificationFilter.createEventTypeFilter(Notification.UNSET)))))));
	}

	@Override
	public void resourceSetChanged(ResourceSetChangeEvent event) {
		for (Notification notification : event.getNotifications()) {
			if (notification.getNotifier() instanceof EObject) {
				EObject object = (EObject) notification.getNotifier();
				if (notification.getFeature() instanceof EReference) {
					EReference reference = (EReference) notification.getFeature();

					switch (notification.getEventType()) {
					case Notification.UNSET:
						// Convert removed object plus its directly and indirectly contained children into
						// proxies
						if (reference.isContainer()) {
							Object value = object.eGet(reference);
							EObject oldValue = (EObject) notification.getOldValue();
							if (oldValue != null && value == null) {
								if (!object.eIsProxy()) {
									EObjectUtil.proxify(oldValue, reference, object);
								}
							}
						}
						break;

					// Object(s) being removed from model (i.e. from its owner)?
					/*
					 * !! Important Note !! Be sure to consider only removed objects and ignore all objects that have
					 * just been moved. While the former have been effectively removed from their old containers and are
					 * floating in memory, the latter have also been removed from their old containers but got assigned
					 * to a new one right after. For obvious reasons such moved objects and their directly and
					 * indirectly contained children must not be converted into proxies.
					 */
					/*
					 * !! Important Note !! Don't use notification.getNewValue() == null to test if object has been
					 * removed from the model. The reason is that plenty of successive modifications and notifications
					 * for the same object and feature might have happened before we get called here. Therefore the new
					 * value of an arbitrary notification which we are dealing with here is the new value which was
					 * applicable by the time the notification was created but does not necessarily correspond to the
					 * current object's state.
					 */
					case Notification.REMOVE:
						// Convert removed object plus its directly and indirectly contained children into
						// proxies
						if (reference.isContainment()) {
							if (!reference.isMany()) {
								Object value = object.eGet(reference);
								EObject oldValue = (EObject) notification.getOldValue();
								if (oldValue.eResource() == null && value != oldValue) {
									EObjectUtil.proxify(object, reference, oldValue);
								}
							} else {
								@SuppressWarnings("unchecked")
								List<Object> values = (List<Object>) object.eGet(reference);
								EObject oldValue = (EObject) notification.getOldValue();
								if (oldValue.eResource() == null && !values.contains(oldValue)) {
									EObjectUtil.proxify(object, reference, oldValue);
								}
							}
						}
						break;
					case Notification.REMOVE_MANY:
						// Convert removed objects plus its directly and indirectly contained children into
						// proxies
						if (reference.isContainment()) {
							@SuppressWarnings("unchecked")
							List<Object> values = (List<Object>) object.eGet(reference);
							@SuppressWarnings("unchecked")
							List<EObject> oldValues = (List<EObject>) notification.getOldValue();
							for (EObject oldValue : oldValues) {
								if (oldValue.eResource() == null && !values.contains(oldValue)) {
									EObjectUtil.proxify(object, reference, oldValue);
								}
							}
						}
						break;

					case Notification.SET:
						// Convert added object plus its directly and indirectly contained children
						// back into regular EObjects
						if (reference.isContainer()) {
							Object value = object.eGet(reference);
							if (value != null) {
								if (object.eIsProxy()) {
									EObjectUtil.deproxify(object);
								}
							}
						}
						break;

					// Object(s) being added to model (i.e. to some owner)?
					/*
					 * !! Important Note !! Don't use notification.getOldValue() == null to test if object has been
					 * added to the model. The reason is that plenty of successive modifications and notifications for
					 * the same object and feature might have happened before we get called here. Therefore the old
					 * value of an arbitrary notification which we are dealing with here is the old value which was
					 * applicable by the time the notification was created but does not necessarily correspond to the
					 * current object's state.
					 */
					case Notification.ADD:
						// Convert added object plus its directly and indirectly contained children
						// back into regular EObjects
						if (reference.isContainment()) {
							if (!reference.isMany()) {
								Object value = object.eGet(reference);
								EObject newValue = (EObject) notification.getNewValue();
								if (value == newValue) {
									EObjectUtil.deproxify(newValue);
								}
							} else {
								@SuppressWarnings("unchecked")
								List<Object> values = (List<Object>) object.eGet(reference);
								EObject newValue = (EObject) notification.getNewValue();
								if (values.contains(newValue)) {
									EObjectUtil.deproxify(newValue);
								}
							}
						}
						break;
					case Notification.ADD_MANY:
						// Convert added objects plus its directly and indirectly contained children
						// back into regular EObjects
						if (reference.isContainment()) {
							@SuppressWarnings("unchecked")
							List<Object> values = (List<Object>) object.eGet(reference);
							@SuppressWarnings("unchecked")
							List<EObject> newValues = (List<EObject>) notification.getNewValue();
							for (EObject newValue : newValues) {
								if (values.contains(newValue)) {
									EObjectUtil.deproxify(newValue);
								}
							}
						}
						break;
					}
				}
			}
		}
	}

	@Override
	public boolean isPostcommitOnly() {
		return true;
	}

}
