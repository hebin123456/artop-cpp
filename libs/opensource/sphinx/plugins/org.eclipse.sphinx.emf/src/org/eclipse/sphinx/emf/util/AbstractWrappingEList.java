/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.AbstractEList;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * An extensible abstract wrapping delegating list implementation with a wrapping and unwrapping object support.
 */
public abstract class AbstractWrappingEList<W, T> extends AbstractEList<W> implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	protected Class<T> targetType;
	protected Class<W> wrapperType;

	private List<T> delegateList;

	/**
	 * Wraps the given object.
	 *
	 * @param object
	 *            object to be wrapped.
	 * @return the wrapped object.
	 * @throws CoreException
	 */
	protected abstract W wrap(T object) throws CoreException;

	/**
	 * Unwraps the given object.
	 *
	 * @param object
	 *            object to be unwrapped.
	 * @return the unwrapped object.
	 */
	protected abstract T unwrap(W object);

	/**
	 * Creates an instance of the AbstractWrappingEList delegating list.
	 *
	 * @param wrapperType
	 *            the wrapper type
	 * @param targetType
	 *            the target object type
	 */
	public AbstractWrappingEList(List<T> delegateList, Class<W> wrapperType, Class<T> targetType) {
		super();

		Assert.isNotNull(delegateList);
		Assert.isNotNull(wrapperType);
		Assert.isNotNull(targetType);

		this.delegateList = delegateList;
		this.wrapperType = wrapperType;
		this.targetType = targetType;
	}

	/**
	 * Returns the list that acts as the backing store.
	 *
	 * @return the list that acts as the backing store.
	 */
	protected List<T> delegateList() {
		return delegateList;
	}

	/**
	 * Returns the number of objects in the list.
	 *
	 * @return the number of objects in the list.
	 */
	@Override
	public int size() {
		return delegateSize();
	}

	/**
	 * Returns the number of objects in the backing store list.
	 *
	 * @return the number of objects in the backing store list.
	 */
	protected int delegateSize() {
		return delegateList().size();
	}

	/**
	 * Returns whether the list has zero size.
	 *
	 * @return whether the list has zero size.
	 */
	@Override
	public boolean isEmpty() {
		return delegateIsEmpty();
	}

	/**
	 * Returns whether the backing store list has zero size.
	 *
	 * @return whether the backing store list has zero size.
	 */
	protected boolean delegateIsEmpty() {
		return delegateList().isEmpty();
	}

	/**
	 * Returns whether the list contains the object.
	 *
	 * @param object
	 *            the object in question.
	 * @return whether the list contains the object.
	 */
	@Override
	public boolean contains(Object object) {
		return delegateContains(object);
	}

	/**
	 * Returns whether the backing store list contains the object.
	 *
	 * @param object
	 *            the object in question.
	 * @return whether the backing store list contains the object.
	 */
	protected boolean delegateContains(Object object) {
		return delegateList().contains(object);
	}

	/**
	 * Returns whether the list contains each object in the collection.
	 *
	 * @return whether the list contains each object in the collection.
	 * @see #contains
	 * @see #useEquals
	 */
	@Override
	public boolean containsAll(Collection<?> collection) {
		return delegateContainsAll(collection);
	}

	/**
	 * Returns whether the backing store list contains each object in the collection.
	 *
	 * @return whether the backing store list contains each object in the collection.
	 * @see #contains
	 * @see #useEquals
	 */
	protected boolean delegateContainsAll(Collection<?> collection) {
		return delegateList().containsAll(collection);
	}

	/**
	 * Returns the position of the first occurrence of the object in the list.
	 *
	 * @param object
	 *            the object in question.
	 * @return the position of the first occurrence of the object in the list.
	 */
	@Override
	public int indexOf(Object object) {
		return delegateIndexOf(object);
	}

	/**
	 * Returns the position of the first occurrence of the object in the backing store list.
	 *
	 * @param object
	 *            the object in question.
	 * @return the position of the first occurrence of the object in the backing store list.
	 */
	protected int delegateIndexOf(Object object) {
		return delegateList().indexOf(object);
	}

	/**
	 * Returns the position of the last occurrence of the object in the list.
	 *
	 * @param object
	 *            the object in question.
	 * @return the position of the last occurrence of the object in the list.
	 */
	@Override
	public int lastIndexOf(Object object) {
		return delegateLastIndexOf(object);
	}

	/**
	 * Returns the position of the last occurrence of the object in the backing store list.
	 *
	 * @param object
	 *            the object in question.
	 * @return the position of the last occurrence of the object in the backing store list.
	 */
	protected int delegateLastIndexOf(Object object) {
		return delegateList().lastIndexOf(object);
	}

	/**
	 * Returns an array containing all the objects in sequence.
	 *
	 * @return an array containing all the objects in sequence.
	 */
	@Override
	public Object[] toArray() {
		return delegateToArray();
	}

	/**
	 * Returns an array containing all the objects in the backing store list in sequence.
	 *
	 * @return an array containing all the objects in the backing store list in sequence.
	 */
	protected Object[] delegateToArray() {
		List<W> result = new ArrayList<W>(size());
		for (T object : delegateList()) {
			try {
				result.add(wrap(object));
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
		return result.toArray();
	}

	/**
	 * Returns an array containing all the objects in sequence.
	 *
	 * @param array
	 *            the array that will be filled and returned, if it's big enough; otherwise, a suitably large array of
	 *            the same type will be allocated and used instead.
	 * @return an array containing all the objects in sequence.
	 */
	@Override
	public <T1> T1[] toArray(T1[] array) {
		return delegateToArray(array);
	}

	/**
	 * Returns an array containing all the objects in the backing store list in sequence.
	 *
	 * @param array
	 *            the array that will be filled and returned, if it's big enough; otherwise, a suitably large array of
	 *            the same type will be allocated and used instead.
	 * @return an array containing all the objects in sequence.
	 */
	protected <E1> E1[] delegateToArray(E1[] array) {
		List<W> result = new ArrayList<W>(size());
		for (T object : delegateList()) {
			try {
				result.add(wrap(object));
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
		return result.toArray(array);
	}

	/**
	 * Returns the object at the index. This implementation delegates to {@link #resolve resolve} so that clients may
	 * transform the fetched object.
	 *
	 * @param index
	 *            the position in question.
	 * @return the object at the index.
	 * @exception IndexOutOfBoundsException
	 *                if the index isn't within the size range.
	 * @see #resolve
	 * @see #basicGet
	 */
	@Override
	public W get(int index) {
		return resolve(index, delegateGet(index));
	}

	/**
	 * Returns the wrapped object at the index in the backing store list.
	 *
	 * @param index
	 *            the position in question.
	 * @return the object at the index.
	 * @exception IndexOutOfBoundsException
	 *                if the index isn't within the size range.
	 */
	protected W delegateGet(int index) {
		T object = delegateList().get(index);
		try {
			return wrap(object);
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			return null;
		}
	}

	/**
	 * Returns the object at the index without {@link #resolve resolving} it.
	 *
	 * @param index
	 *            the position in question.
	 * @return the object at the index.
	 * @exception IndexOutOfBoundsException
	 *                if the index isn't within the size range.
	 * @see #resolve
	 * @see #get
	 */
	@Override
	protected W basicGet(int index) {
		return delegateGet(index);
	}

	@Override
	protected W primitiveGet(int index) {
		return delegateGet(index);
	}

	/**
	 * Sets the object at the index and returns the old object at the index; it does no ranging checking or uniqueness
	 * checking. This implementation delegates to {@link #didSet didSet} and {@link #didChange didChange}.
	 *
	 * @param index
	 *            the position in question.
	 * @param object
	 *            the object to set.
	 * @return the old object at the index.
	 * @see #set
	 */
	@Override
	public W setUnique(int index, W object) {
		W oldObject = delegateSet(index, validate(index, object));
		didSet(index, object, oldObject);
		didChange();
		return oldObject;
	}

	/**
	 * Sets the unwrapped object at the index in the backing store list and returns the wrapped object at the index.
	 *
	 * @param object
	 *            the object to set.
	 * @return the old object at the index.
	 */
	protected W delegateSet(int index, W object) {
		try {
			return wrap(delegateList().set(index, unwrap(object)));
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			return null;
		}
	}

	/**
	 * Adds the object at the end of the list; it does no uniqueness checking. This implementation delegates to
	 * {@link #didAdd didAdd} and {@link #didChange didChange}. after uniqueness checking.
	 *
	 * @param object
	 *            the object to be added.
	 * @see #add(Object)
	 */
	@Override
	public void addUnique(W object) {
		++modCount;

		int size = size();
		delegateAdd(validate(size, object));
		didAdd(size, object);
		didChange();
	}

	/**
	 * Adds the unwrapped object at the end of the backing store list.
	 *
	 * @param object
	 *            the object to be added.
	 */
	protected void delegateAdd(W object) {
		T target = unwrap(object);
		delegateList().add(target);
	}

	/**
	 * Adds the object at the given index in the list; it does no ranging checking or uniqueness checking. This
	 * implementation delegates to {@link #didAdd didAdd} and {@link #didChange didChange}.
	 *
	 * @param object
	 *            the object to be added.
	 * @see #add(int, Object)
	 */
	@Override
	public void addUnique(int index, W object) {
		++modCount;

		delegateAdd(index, validate(index, object));
		didAdd(index, object);
		didChange();
	}

	/**
	 * Adds the unwrapped object at the given index in the backing store list.
	 *
	 * @param object
	 *            the object to be added.
	 */
	protected void delegateAdd(int index, W object) {
		delegateList().add(index, unwrap(object));
	}

	/**
	 * Adds each object of the collection to the end of the list; it does no uniqueness checking. This implementation
	 * delegates to {@link #didAdd didAdd} and {@link #didChange didChange}.
	 *
	 * @param collection
	 *            the collection of objects to be added.
	 * @see #addAll(Collection)
	 */
	@Override
	public boolean addAllUnique(Collection<? extends W> collection) {
		++modCount;

		if (collection.isEmpty()) {
			return false;
		} else {
			int i = size();
			for (W object : collection) {
				delegateAdd(validate(i, object));
				didAdd(i, object);
				didChange();
				i++;
			}

			return true;
		}
	}

	/**
	 * Adds each object of the collection at each successive index in the list and returns whether any objects were
	 * added; it does no ranging checking or uniqueness checking. This implementation delegates to {@link #didAdd
	 * didAdd} and {@link #didChange didChange}.
	 *
	 * @param index
	 *            the index at which to add.
	 * @param collection
	 *            the collection of objects to be added.
	 * @return whether any objects were added.
	 * @see #addAll(int, Collection)
	 */
	@Override
	public boolean addAllUnique(int index, Collection<? extends W> collection) {
		++modCount;

		if (collection.isEmpty()) {
			return false;
		} else {
			for (W object : collection) {
				delegateAdd(index, validate(index, object));
				didAdd(index, object);
				didChange();
				index++;
			}

			return true;
		}
	}

	/**
	 * Adds each object from start to end of the array at the index of list and returns whether any objects were added;
	 * it does no ranging checking or uniqueness checking. This implementation delegates to {@link #delegateAdd(Object)
	 * delegatedAdd}, {@link #didAdd didAdd}, and {@link #didChange didChange}.
	 *
	 * @param objects
	 *            the objects to be added.
	 * @param start
	 *            the index of first object to be added.
	 * @param end
	 *            the index past the last object to be added.
	 * @return whether any objects were added.
	 * @see #addAllUnique(int, Object[], int, int)
	 */
	@Override
	public boolean addAllUnique(Object[] objects, int start, int end) {
		int growth = end - start;

		++modCount;

		if (growth == 0) {
			return false;
		} else {
			int index = size();
			for (int i = start; i < end; ++i, ++index) {
				@SuppressWarnings("unchecked")
				W object = (W) objects[i];
				delegateAdd(validate(index, object));
				didAdd(index, object);
				didChange();
			}

			return true;
		}
	}

	/**
	 * Adds each object from start to end of the array at each successive index in the list and returns whether any
	 * objects were added; it does no ranging checking or uniqueness checking. This implementation delegates to
	 * {@link #delegateAdd(int, Object) delegatedAdd}, {@link #didAdd didAdd}, and {@link #didChange didChange}.
	 *
	 * @param index
	 *            the index at which to add.
	 * @param objects
	 *            the objects to be added.
	 * @param start
	 *            the index of first object to be added.
	 * @param end
	 *            the index past the last object to be added.
	 * @return whether any objects were added.
	 * @see #addAllUnique(Object[], int, int)
	 */
	@Override
	public boolean addAllUnique(int index, Object[] objects, int start, int end) {
		int growth = end - start;

		++modCount;

		if (growth == 0) {
			return false;
		} else {
			for (int i = start; i < end; ++i, ++index) {
				@SuppressWarnings("unchecked")
				W object = (W) objects[i];
				delegateAdd(validate(index, object));
				didAdd(index, object);
				didChange();
			}

			return true;
		}
	}

	/**
	 * Removes the object from the list and returns whether the object was actually contained by the list. This
	 * implementation uses {@link #indexOf indexOf} to find the object and delegates to {@link #remove(int) remove(int)}
	 * in the case that it finds the object.
	 *
	 * @param object
	 *            the object to be removed.
	 * @return whether the object was actually contained by the list.
	 */
	@Override
	public boolean remove(Object object) {
		int index = indexOf(object);
		if (index >= 0) {
			remove(index);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Removes each object of the collection from the list and returns whether any object was actually contained by the
	 * list.
	 *
	 * @param collection
	 *            the collection of objects to be removed.
	 * @return whether any object was actually contained by the list.
	 */
	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean modified = false;
		for (ListIterator<?> i = listIterator(); i.hasNext();) {
			if (collection.contains(i.next())) {
				i.remove();
				modified = true;
			}
		}

		return modified;
	}

	/**
	 * Removes the object at the index from the list and returns it. This implementation delegates to {@link #didRemove
	 * didRemove} and {@link #didChange didChange}.
	 *
	 * @param index
	 *            the position of the object to remove.
	 * @return the removed object.
	 * @exception IndexOutOfBoundsException
	 *                if the index isn't within the size range.
	 */
	@Override
	public W remove(int index) {
		++modCount;

		W oldObject = delegateRemove(index);
		didRemove(index, oldObject);
		didChange();

		return oldObject;
	}

	/**
	 * Removes the object at the index from the backing store list and returns the wrapped object.
	 *
	 * @return the removed object.
	 * @exception IndexOutOfBoundsException
	 *                if the index isn't within the size range.
	 */
	protected W delegateRemove(int index) {
		try {
			return wrap(delegateList().remove(index));
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			return null;
		}
	}

	/**
	 * Removes from the list each object not contained by the collection and returns whether any object was actually
	 * removed. This delegates to {@link #remove(int) remove(int)} in the case that it finds an object that isn't
	 * retained.
	 *
	 * @param collection
	 *            the collection of objects to be retained.
	 * @return whether any object was actually removed.
	 */
	@Override
	public boolean retainAll(Collection<?> collection) {
		boolean modified = false;
		for (ListIterator<?> i = listIterator(); i.hasNext();) {
			if (!collection.contains(i.next())) {
				i.remove();
				modified = true;
			}
		}
		return modified;
	}

	/**
	 * Clears the list of all objects.
	 */
	@Override
	public void clear() {
		doClear(size(), delegateToArray());
	}

	/**
	 * Does the actual job of clearing all the objects.
	 *
	 * @param oldSize
	 *            the size of the list before it is cleared.
	 * @param oldData
	 *            old values of the list before it is cleared.
	 */
	protected void doClear(int oldSize, Object[] oldData) {
		++modCount;

		delegateClear();

		didClear(oldSize, oldData);
		didChange();
	}

	/**
	 * Clears the backing store list of all objects.
	 */
	protected void delegateClear() {
		delegateList().clear();
	}

	/**
	 * Moves the object at the source index of the list to the target index of the list and returns the moved object.
	 * This implementation delegates to {@link #didMove didMove} and {@link #didChange didChange}.
	 *
	 * @param targetIndex
	 *            the new position for the object in the list.
	 * @param sourceIndex
	 *            the old position of the object in the list.
	 * @return the moved object.
	 * @exception IndexOutOfBoundsException
	 *                if either index isn't within the size range.
	 */
	@Override
	public W move(int targetIndex, int sourceIndex) {
		++modCount;
		int size = size();
		if (targetIndex >= size || targetIndex < 0) {
			throw new IndexOutOfBoundsException("targetIndex=" + targetIndex + ", size=" + size); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (sourceIndex >= size || sourceIndex < 0) {
			throw new IndexOutOfBoundsException("sourceIndex=" + sourceIndex + ", size=" + size); //$NON-NLS-1$ //$NON-NLS-2$
		}

		W object;
		if (targetIndex != sourceIndex) {
			object = delegateMove(targetIndex, sourceIndex);
			didMove(targetIndex, object, sourceIndex);
			didChange();
		} else {
			object = delegateGet(sourceIndex);
		}
		return object;
	}

	/**
	 * Moves the object at the source index in the backing store list by removing it and adding it at the new target
	 * index.
	 *
	 * @param targetIndex
	 *            the new position for the object in the list.
	 * @param sourceIndex
	 *            the old position of the object in the list.
	 * @return the moved object.
	 * @exception IndexOutOfBoundsException
	 *                if either index isn't within the size range.
	 * @since 2.3
	 */
	protected W delegateMove(int targetIndex, int sourceIndex) {
		W result = delegateRemove(sourceIndex);
		delegateAdd(targetIndex, result);
		return result;
	}

	/**
	 * Returns whether the object is a list with corresponding equal objects. This implementation uses either
	 * <code>equals</code> or <code>"=="</code> depending on {@link #useEquals useEquals}.
	 *
	 * @return whether the object is a list with corresponding equal objects.
	 * @see #useEquals
	 */
	@Override
	public boolean equals(Object object) {
		return delegateEquals(object);
	}

	/**
	 * Returns whether the object is a list with corresponding equal objects to those in the backing store list.
	 *
	 * @return whether the object is a list with corresponding equal objects.
	 */
	protected boolean delegateEquals(Object object) {
		return delegateList().equals(object);
	}

	/**
	 * Returns a hash code computed from each object's hash code.
	 *
	 * @return a hash code.
	 */
	@Override
	public int hashCode() {
		return delegateHashCode();
	}

	/**
	 * Returns the hash code of the backing store list.
	 *
	 * @return a hash code.
	 */
	protected int delegateHashCode() {
		return delegateList().hashCode();
	}

	/**
	 * Returns a string of the form <code>"[object1, object2]"</code>.
	 *
	 * @return a string of the form <code>"[object1, object2]"</code>.
	 */
	@Override
	public String toString() {
		return delegateToString();
	}

	/**
	 * Returns a the string form of the backing store list.
	 *
	 * @return a the string form of the backing store list.
	 */
	protected String delegateToString() {
		return delegateList().toString();
	}

	/**
	 * Returns an <b>unsafe</b> list that provides a {@link #resolve non-resolving} view of the underlying data storage.
	 *
	 * @return an <b>unsafe</b> list that provides a non-resolving view of the underlying data storage.
	 */
	@Override
	protected List<W> basicList() {
		if (delegateSize() == 0) {
			return ECollections.emptyEList();
		} else {
			return ECollections.unmodifiableEList(this);
		}
	}
}
