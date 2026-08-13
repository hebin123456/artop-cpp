/**
 * <copyright>
 *
 * Copyright (c) 2009 Ed Merks and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *   Ed Merks - Initial API and implementation
 *   itemis - Adding overriding points and make protected field masks
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.ecore;

import java.util.Arrays;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.BasicNotifierImpl;
import org.eclipse.emf.common.notify.impl.NotificationImpl;
import org.eclipse.emf.common.util.ArrayDelegatingEList;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.BasicEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.eclipse.emf.ecore.util.ECrossReferenceEList;

/**
 * A space compact implementation of the model object '<em><b>EObject</b></em>'. <br>
 * This class is copied from org.eclipse.emf.ecore.impl.MinimalEObjectImpl for adding overriding points and make
 * protected field masks.
 */
public class MinimalEObjectImpl2 extends BasicEObjectImpl implements EObject, EStructuralFeature.Internal.DynamicValueHolder {
	// TODO Submit this class to EMF for replacing the existing MinimalEObjectImpl class.
	public static class Container extends MinimalEObjectImpl2 {
		public static class Dynamic extends Container {
			public static final class BasicEMapEntry<K, V> extends Dynamic implements BasicEMap.Entry<K, V> {
				protected int hash = -1;
				protected EStructuralFeature keyFeature;
				protected EStructuralFeature valueFeature;

				/**
				 * Creates a dynamic EObject.
				 */
				public BasicEMapEntry() {
					super();
				}

				/**
				 * Creates a dynamic EObject.
				 */
				public BasicEMapEntry(EClass eClass) {
					super(eClass);
				}

				@Override
				@SuppressWarnings("unchecked")
				public K getKey() {
					return (K) eGet(keyFeature);
				}

				@Override
				public void setKey(Object key) {
					eSet(keyFeature, key);
				}

				@Override
				public int getHash() {
					if (hash == -1) {
						Object theKey = getKey();
						hash = theKey == null ? 0 : theKey.hashCode();
					}
					return hash;
				}

				@Override
				public void setHash(int hash) {
					this.hash = hash;
				}

				@Override
				@SuppressWarnings("unchecked")
				public V getValue() {
					return (V) eGet(valueFeature);
				}

				@Override
				public V setValue(V value) {
					@SuppressWarnings("unchecked")
					V result = (V) eGet(valueFeature);
					eSet(valueFeature, value);
					return result;
				}

				@Override
				public void eSetClass(EClass eClass) {
					super.eSetClass(eClass);
					keyFeature = eClass.getEStructuralFeature("key"); //$NON-NLS-1$
					valueFeature = eClass.getEStructuralFeature("value"); //$NON-NLS-1$
				}
			}

			protected EClass eClass;
			protected Object[] eSettings;

			public Dynamic() {
				super();
			}

			public Dynamic(EClass eClass) {
				super();
				eSetClass(eClass);
			}

			@Override
			public EClass eClass() {
				return eClass;
			}

			@Override
			protected EClass eDynamicClass() {
				return eClass();
			}

			@Override
			public void eSetClass(EClass eClass) {
				this.eClass = eClass;
			}

			@Override
			protected boolean eHasSettings() {
				return eSettings != null;
			}

			@Override
			protected Object[] eBasicSettings() {
				return eSettings;
			}

			@Override
			protected void eBasicSetSettings(Object[] settings) {
				eSettings = settings;
			}
		}

		protected InternalEObject eContainer;

		public Container() {
			super();
		}

		@Override
		public InternalEObject eInternalContainer() {
			return eContainer;
		}

		@Override
		protected void eBasicSetContainer(InternalEObject newContainer) {
			eContainer = newContainer;
		}
	}

	/**
	 * The {@link #eFlags bit flag} for {@link #eDeliver()}.
	 */
	private static final int NO_DELIVER = 1 << 0;

	/**
	 * The {@link #eFlags bit flag} for indicating that a dynamic {@link #eContainer() container} field is allocated. A
	 * derived implementation wishing to allocate a static container field should override {@link #eInternalContainer()}
	 * and {@link #eBasicSetContainer(InternalEObject)}.
	 */
	private static final int CONTAINER = 1 << 1;

	/**
	 * The {@link #eFlags bit flag} for indicating that a dynamic {@link #eAdapters() adapters} field is allocated. A
	 * derived implementation wishing to allocate a static adapter field should override {@link #eBasicHasAdapters()},
	 * {@link #eBasicAdapterArray()}, and {@link #eBasicSetAdapterArray(Adapter[])}.
	 */
	private static final int ADAPTER = 1 << 2;

	/**
	 * The {@link #eFlags bit flag} for indicating that a dynamic
	 * {@link BasicNotifierImpl.EObservableAdapterList.Listener adapter list listener} field is allocated.
	 */
	private static final int ADAPTER_LISTENER = 1 << 3;

	/**
	 * The {@link #eFlags bit flag} for indicating that a dynamic {@link #eClass() class} field is allocated. A derived
	 * implementation wishing to allocate a static class field should override {@link #eDynamicClass()} and
	 * {@link #eSetClass(EClass)}.
	 */
	private static final int CLASS = 1 << 4;

	/**
	 * The {@link #eFlags bit flag} for indicating that a dynamic {@link #eSettings() settings} field is allocated. A
	 * derived implementation wishing to allocate a static settings field should override {@link #eHasSettings()},
	 * {@link #eBasicSettings()}, and {@link #eBasicSetSettings(Object[])}.
	 */
	private static final int SETTING = 1 << 5;

	/**
	 * The {@link #eFlags bit flag} for indicating that a dynamic {@link #eProxyURI() proxy URI} field is allocated. A
	 * derived implementation wishing to allocate a static proxy URI field should override {@link #eIsProxy()},
	 * {@link #eProxyURI()}, and {@link #eSetProxyURI(URI)}.
	 */
	private static final int PROXY = 1 << 6;

	/**
	 * The {@link #eFlags bit flag} for indicating that a dynamic {@link #eResource() resource} field is allocated. A
	 * derived implementation wishing to allocate a static resource field should override {@link #eDirectResource()} and
	 * {@link #eSetDirectResource(Resource.Internal)}.
	 */
	private static final int RESOURCE = 1 << 7;

	/**
	 * A bit mask for all the bit flags representing fields.
	 */
	protected static final int FIELD_MASK = CONTAINER | ADAPTER | ADAPTER_LISTENER | CLASS | SETTING | PROXY | RESOURCE;

	/**
	 * A bit flag field with bits for {@link #NO_DELIVER}, {@link #CONTAINER}, {@link #ADAPTER},
	 * {@link #ADAPTER_LISTENER}, {@link #CLASS}, {@link #SETTING}, {@link #PROXY}, and {@link #RESOURCE}. The high
	 * order 16 bits are used to represent the {@link #eContainerFeatureID() container feature ID}, a derived
	 * implementation wishing to allocate a static container feature ID field should override
	 * {@link #eContainerFeatureID()} and {@link #eBasicSetContainerFeatureID(int)}.
	 *
	 * @see #NO_DELIVER
	 * @see #CONTAINER
	 * @see #ADAPTER
	 * @see #ADAPTER_LISTENER
	 * @see #CLASS
	 * @see #SETTING
	 * @see #PROXY
	 * @see #RESOURCE
	 */
	protected int eFlags;

	/**
	 * The storage location for dynamic fields.
	 */
	private Object eStorage;

	/**
	 * Creates a minimal EObject.
	 */
	protected MinimalEObjectImpl2() {
		super();
	}

	@Override
	protected EPropertiesHolder eProperties() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected EPropertiesHolder eBasicProperties() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected BasicEList<Adapter> eBasicAdapters() {
		throw new UnsupportedOperationException();
	}

	protected int getFieldMask() {
		return FIELD_MASK;
	}

	protected int getMaxField() {
		return RESOURCE;
	}

	protected final boolean hasField(int field) {
		return (eFlags & field) != 0;
	}

	protected final Object getField(int field) {
		if (hasField(field)) {
			int fieldIndex = fieldIndex(field);
			return fieldIndex == -1 ? eStorage : ((Object[]) eStorage)[fieldIndex];
		} else {
			return null;
		}
	}

	/*
	 * Changed the method signature to return the old value. This is useful for example for notification.
	 */
	protected final Object setField(int field, Object value) {
		Object oldValue = null;
		if (hasField(field)) {
			if (value == null) {
				oldValue = removeField(field);
			} else {
				int fieldIndex = fieldIndex(field);
				if (fieldIndex == -1) {
					oldValue = eStorage;
					eStorage = value;
				} else {
					oldValue = ((Object[]) eStorage)[fieldIndex];
					((Object[]) eStorage)[fieldIndex] = value;
				}
			}
		} else if (value != null) {
			addField(field, value);
		}
		return oldValue;
	}

	protected final int fieldIndex(int field) {
		int result = 0;
		for (int bit = CONTAINER; bit < field; bit <<= 1) {
			if ((eFlags & bit) != 0) {
				++result;
			}
		}
		if (result == 0) {
			for (int bit = field <<= 1; bit <= getMaxField(); bit <<= 1) {
				if ((eFlags & bit) != 0) {
					return 0;
				}
			}
			return -1;
		} else {
			return result;
		}
	}

	protected final void addField(int field, Object value) {
		int fieldCount = Integer.bitCount(eFlags & getFieldMask());
		if (fieldCount == 0) {
			eStorage = value;
		} else {
			Object[] result;
			if (fieldCount == 1) {
				result = new Object[2];
				int fieldIndex = fieldIndex(field);
				if (fieldIndex == 0) {
					result[0] = value;
					result[1] = eStorage;
				} else {
					result[0] = eStorage;
					result[1] = value;
				}
			} else {
				result = new Object[fieldCount + 1];
				Object[] oldStorage = (Object[]) eStorage;
				for (int bit = CONTAINER, sourceIndex = 0, targetIndex = 0; bit <= getMaxField(); bit <<= 1) {
					if (bit == field) {
						result[targetIndex++] = value;
					} else if ((eFlags & bit) != 0) {
						result[targetIndex++] = oldStorage[sourceIndex++];
					}
				}
			}
			eStorage = result;
		}
		eFlags |= field;
	}

	/*
	 * Changed the method signature to return the old value. This is useful for example for notification.
	 */
	protected Object removeField(int field) {
		Object oldValue = null;
		int fieldCount = Integer.bitCount(eFlags & getFieldMask());
		if (fieldCount == 1) {
			oldValue = eStorage;
			eStorage = null;
		} else {
			Object[] oldStorage = (Object[]) eStorage;
			if (fieldCount == 2) {
				int fieldIndex = fieldIndex(field);
				oldValue = oldStorage[fieldIndex];
				eStorage = oldStorage[fieldIndex == 0 ? 1 : 0];
			} else {
				Object[] result = new Object[fieldCount - 1];
				for (int bit = CONTAINER, sourceIndex = 0, targetIndex = 0; bit <= getMaxField(); bit <<= 1) {
					if (bit == field) {
						oldValue = oldStorage[sourceIndex++];
					} else if ((eFlags & bit) != 0) {
						result[targetIndex++] = oldStorage[sourceIndex++];
					}
				}
				eStorage = result;
			}
		}
		eFlags &= ~field;
		return oldValue;
	}

	@Override
	public EList<Adapter> eAdapters() {
		class ArrayDelegatingAdapterList extends ArrayDelegatingEList<Adapter> implements BasicNotifierImpl.EObservableAdapterList {
			private static final long serialVersionUID = 1L;

			@Override
			protected Object[] newData(int capacity) {
				return new Adapter[capacity];
			}

			@Override
			public Object[] data() {
				return eBasicAdapterArray();
			}

			@Override
			public void setData(Object[] data) {
				++modCount;
				InternalEObject eContainer = eInternalContainer();
				if (eContainer instanceof BasicEObjectImpl) {
					Adapter[] eContainerAdapterArray = eContainerAdapterArray();
					if (Arrays.equals(data, eContainerAdapterArray)) {
						eBasicSetAdapterArray(eContainerAdapterArray);
						return;
					}
				}
				eBasicSetAdapterArray((Adapter[]) data);
			}

			@Override
			protected void didAdd(int index, Adapter newObject) {
				Listener[] listeners = (Listener[]) getField(ADAPTER_LISTENER);
				if (listeners != null) {
					for (Listener listener : listeners) {
						listener.added(MinimalEObjectImpl2.this, newObject);
					}
				}
				newObject.setTarget(MinimalEObjectImpl2.this);
			}

			@Override
			protected void didRemove(int index, Adapter oldObject) {
				Listener[] listeners = (Listener[]) getField(ADAPTER_LISTENER);
				if (listeners != null) {
					for (Listener listener : listeners) {
						listener.removed(MinimalEObjectImpl2.this, oldObject);
					}
				}
				Adapter adapter = oldObject;
				if (eDeliver()) {
					Notification notification = new NotificationImpl(Notification.REMOVING_ADAPTER, oldObject, null, index) {
						@Override
						public Object getNotifier() {
							return MinimalEObjectImpl2.this;
						}
					};
					adapter.notifyChanged(notification);
				}
				if (adapter instanceof Adapter.Internal) {
					((Adapter.Internal) adapter).unsetTarget(MinimalEObjectImpl2.this);
				} else if (adapter.getTarget() == MinimalEObjectImpl2.this) {
					adapter.setTarget(null);
				}
			}

			@Override
			public void addListener(Listener listener) {
				Listener[] listeners = (Listener[]) getField(ADAPTER_LISTENER);
				if (listeners == null) {
					listeners = new Listener[] { listener };
				} else {
					Listener[] newListeners = new Listener[listeners.length + 1];
					System.arraycopy(listeners, 0, newListeners, 0, listeners.length);
					newListeners[listeners.length] = listener;
					listeners = newListeners;
				}
				setField(ADAPTER_LISTENER, listeners);
			}

			@Override
			public void removeListener(Listener listener) {
				Listener[] listeners = (Listener[]) getField(ADAPTER_LISTENER);
				if (listeners != null) {
					for (int i = 0; i < listeners.length; ++i) {
						if (listeners[i] == listener) {
							if (listeners.length == 1) {
								listeners = null;
							} else {
								Listener[] newListeners = new Listener[listeners.length - 1];
								System.arraycopy(listeners, 0, newListeners, 0, i);
								if (i != newListeners.length) {
									System.arraycopy(listeners, i + 1, newListeners, i, newListeners.length - i);
								}
								listeners = newListeners;
							}
							setField(ADAPTER_LISTENER, listeners);
							break;
						}
					}
				}
			}
		}
		return new ArrayDelegatingAdapterList();
	}

	@Override
	protected Adapter[] eBasicAdapterArray() {
		return (Adapter[]) getField(ADAPTER);
	}

	protected void eBasicSetAdapterArray(Adapter[] eAdapters) {
		setField(ADAPTER, eAdapters);
	}

	@Override
	protected boolean eBasicHasAdapters() {
		return hasField(ADAPTER);
	}

	@Override
	public boolean eDeliver() {
		return (eFlags & NO_DELIVER) == 0;
	}

	@Override
	public void eSetDeliver(boolean deliver) {
		if (deliver) {
			eFlags &= ~NO_DELIVER;
		} else {
			eFlags |= NO_DELIVER;
		}
	}

	@Override
	public boolean eIsProxy() {
		return hasField(PROXY);
	}

	@Override
	public URI eProxyURI() {
		return (URI) getField(PROXY);
	}

	@Override
	public void eSetProxyURI(URI uri) {
		setField(PROXY, uri);
	}

	@Override
	public InternalEObject eInternalContainer() {
		return (InternalEObject) getField(CONTAINER);
	}

	protected void eBasicSetContainer(InternalEObject newContainer) {
		setField(CONTAINER, newContainer);
	}

	@Override
	public int eContainerFeatureID() {
		return eFlags >> 16;
	}

	protected void eBasicSetContainerFeatureID(int newContainerFeatureID) {
		eFlags = newContainerFeatureID << 16 | eFlags & 0x00FF;
	}

	@Override
	protected void eBasicSetContainer(InternalEObject newContainer, int newContainerFeatureID) {
		eBasicSetContainerFeatureID(newContainerFeatureID);
		eBasicSetContainer(newContainer);
	}

	@Override
	protected EClass eDynamicClass() {
		return (EClass) getField(CLASS);
	}

	@Override
	public EClass eClass() {
		EClass eClass = eDynamicClass();
		return eClass == null ? eStaticClass() : eClass;
	}

	@Override
	public void eSetClass(EClass eClass) {
		setField(CLASS, eClass);
	}

	@Override
	protected boolean eHasSettings() {
		return hasField(SETTING);
	}

	protected Object[] eBasicSettings() {
		return (Object[]) getField(SETTING);
	}

	protected void eBasicSetSettings(Object[] settings) {
		setField(SETTING, settings);
	}

	/*
	 * FIXME This should be removed when this class is integrated in EMF and we should use
	 * EPropertiesHolderBaseImpl.NO_SETTINGS instead.
	 */
	protected static final Object[] NO_SETTINGS = new Object[0];

	@Override
	protected EStructuralFeature.Internal.DynamicValueHolder eSettings() {
		if (!eHasSettings()) {
			int size = eClass().getFeatureCount() - eStaticFeatureCount();
			if (size != 0) {
				// TODO Uncomment when this class is integrated in EMF
				// eBasicSetSettings(size == 0 ? EPropertiesHolderBaseImpl.NO_SETTINGS : new Object[size]);
				// TODO Remove the following line when this class is integrated in EMF
				eBasicSetSettings(size == 0 ? NO_SETTINGS : new Object[size]);
			}
		}

		return this;
	}

	@Override
	public Resource.Internal eDirectResource() {
		return (Resource.Internal) getField(RESOURCE);
	}

	@Override
	protected void eSetDirectResource(Resource.Internal resource) {
		setField(RESOURCE, resource);
	}

	@Override
	public EList<EObject> eContents() {
		return EContentsEList.createEContentsEList(this);
	}

	@Override
	public EList<EObject> eCrossReferences() {
		return ECrossReferenceEList.createECrossReferenceEList(this);
	}

	private Object[] eDynamicSettings() {
		Object[] settings = eBasicSettings();
		if (settings == null) {
			eSettings();
			settings = eBasicSettings();
		}
		return settings;
	}

	@Override
	public Object dynamicGet(int dynamicFeatureID) {
		Object[] settings = eDynamicSettings();
		return settings[dynamicFeatureID];
	}

	@Override
	public void dynamicSet(int dynamicFeatureID, Object newValue) {
		Object[] settings = eDynamicSettings();
		settings[dynamicFeatureID] = newValue;
	}

	@Override
	public void dynamicUnset(int dynamicFeatureID) {
		Object[] settings = eDynamicSettings();
		settings[dynamicFeatureID] = null;
	}
}
