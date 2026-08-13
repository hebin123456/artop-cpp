/**
 * <copyright>
 * 
 * Copyright (c) 2012 BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     BMW Car IT - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.ecore;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.DelegatingEList;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.BasicFeatureMap;

/**
 * A feature map which maintains an order on its element's feature IDs (ascending).
 */
public class OrderedFeatureMap extends BasicFeatureMap {

	private static final long serialVersionUID = 1L;

	public static interface ListBehavior<T> {
		void add(int index, T t);

		T set(int index, T t);

		void addAll(int index, Collection<? extends T> collection);
	}

	public static interface Order<T> {
		int order(T t);
	}

	/**
	 * Class which encapsulates the behavior that ensures that elements to the list will be inserted into the list
	 * without violating the order of the list.
	 * 
	 * @param <T>
	 */
	public static class OrderedListBehavior<T> implements ListBehavior<T> {

		private final List<T> list;
		private final Order<T> order;

		public OrderedListBehavior(List<T> list, Order<T> order) {
			this.list = list;
			this.order = order;
		}

		@Override
		public void add(int index, T t) {
			// find index where t may be added without violating the order of the list

			int ot = order.order(t);
			int oi;

			if (ot == -1) {
				// special case where order does not matter
				list.add(index, t);
				return;
			}

			if (index == list.size()) {
				oi = Integer.MAX_VALUE;
			} else {
				oi = order.order(list.get(index));
			}

			if (ot > oi) {
				// t needs to be added further in the back of the list

				while (true) {
					index++;

					if (index == list.size()) {
						oi = Integer.MAX_VALUE;
					} else {
						oi = order.order(list.get(index));
					}

					if (oi >= ot) {
						list.add(index, t);
						break;
					}
				}
			} else if (ot < oi) {
				// t needs to be added further in front

				while (true) {
					index--;

					if (index < 0) {
						oi = Integer.MIN_VALUE;
					} else {
						oi = order.order(list.get(index));
					}

					if (oi <= ot) {
						list.add(index + 1, t);
						break;
					}
				}
			} else {
				list.add(index, t);
			}
		}

		@Override
		public T set(int index, T t) {
			T previous = list.get(index);

			if (order.order(previous) != order.order(t)) {
				throw new IllegalStateException("Only elements with equal order may be overwritten by a set operation."); //$NON-NLS-1$
			}

			return list.set(index, t);
		}

		@Override
		public void addAll(int index, Collection<? extends T> c) {
			int i = 0;
			for (T t : c) {
				add(index + i, t);
				i++;
			}
		}

	}

	ListBehavior<Entry> lb = new OrderedListBehavior<Entry>(new DelegatingEList<Entry>() {
		private static final long serialVersionUID = 1L;

		@Override
		protected List<Entry> delegateList() {
			return OrderedFeatureMap.this;
		};

		@Override
		public void add(int index, Entry object) {
			OrderedFeatureMap.super.doAdd(index, object);
		};

		@Override
		public Entry set(int index, Entry object) {
			return OrderedFeatureMap.super.doSet(index, object);
		};
	}, new Order<Entry>() {
		@Override
		public int order(Entry e) {
			int order = owner.eClass().getFeatureID(e.getEStructuralFeature());
			if (order != -1) {
				return order;
			} else {
				// Return -1 for features that are not part of the EClass, e.g. the XML features (text, comment,
				// proccessing instruction or CDATA) to esnure that such elements will not be re-ordered. Explicit else
				// branch added for clarity.
				return -1;
			}
		}
	});

	public OrderedFeatureMap(InternalEObject owner, int featureID) {
		super(owner, featureID);
	}

	public OrderedFeatureMap(InternalEObject owner, int featureID, EStructuralFeature eStructuralFeature) {
		super(owner, featureID, eStructuralFeature);
	}

	@Override
	protected boolean doAdd(Entry object) {
		doAdd(size(), object);

		return true;
	}

	@Override
	public Entry doSet(int index, Entry object) {
		return lb.set(index, object);
	}

	@Override
	public void doAdd(int index, Entry object) {
		lb.add(index, object);
	}

	@Override
	public boolean doAddAll(Collection<? extends Entry> collection) {
		return doAddAll(size(), collection);
	}

	@Override
	public boolean doAddAll(int index, Collection<? extends Entry> collection) {
		lb.addAll(index, collection);
		return collection.size() > 0;
	}
}
