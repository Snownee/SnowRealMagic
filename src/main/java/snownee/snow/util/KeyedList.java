package snownee.snow.util;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public class KeyedList<K, E> extends ArrayList<E> {
	@Serial
	private static final long serialVersionUID = -5150601089854895739L;
	private final ArrayList<K> keys;

	public KeyedList() {
		keys = Lists.newArrayList();
	}

	public KeyedList(int initialCapacity) {
		super(initialCapacity);
		keys = Lists.newArrayListWithCapacity(initialCapacity);
	}

	@Override
	public E remove(int index) {
		keys.remove(index);
		return super.remove(index);
	}

	public int indexOfKey(K key) {
		return keys.indexOf(key);
	}

	public boolean containsKey(K key) {
		return keys.contains(key);
	}

	private void put(int index, K key, E element) {
		if (containsKey(key)) {
			throw new IllegalArgumentException("Key already exists: " + key);
		}
		keys.add(index, key);
		super.add(index, element);
	}

	public void putFirst(K key, E element) {
		put(0, key, element);
	}

	public void putLast(K key, E element) {
		put(keys.size(), key, element);
	}

	public void putAfter(K relativeKey, K key, E element) {
		int index = indexOfKey(relativeKey);
		if (index == -1) {
			throw new IllegalArgumentException("Key not found: " + relativeKey);
		}
		put(index + 1, key, element);
	}

	public void putBefore(K relativeKey, K key, E element) {
		int index = indexOfKey(relativeKey);
		if (index == -1) {
			throw new IllegalArgumentException("Key not found: " + relativeKey);
		}
		put(index, key, element);
	}

	public boolean removeKey(K key) {
		return remove(indexOfKey(key)) != null;
	}

	public boolean replace(K key, E element) {
		int index = indexOfKey(key);
		if (index == -1) {
			return false;
		}
		set(index, element);
		return true;
	}

	public Stream<K> keys() {
		return keys.stream();
	}

	@Override
	@Deprecated
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}
}
