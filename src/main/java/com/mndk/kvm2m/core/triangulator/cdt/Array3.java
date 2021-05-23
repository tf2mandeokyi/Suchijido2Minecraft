package com.mndk.kvm2m.core.triangulator.cdt;

import java.util.*;

public class Array3<T> implements List<T> {
	
	
	
	public T e1, e2, e3;
	
	
	
	public Array3(T e1, T e2, T e3) {
		this.e1 = e1; this.e2 = e2; this.e3 = e3;
	}
	
	
	
	public T get(int i) {
		if(i == 0) return e1;
		if(i == 1) return e2;
		if(i == 2) return e3;
		throw new ArrayIndexOutOfBoundsException(i);
	}
	
	
	
	public T set(int i, T value) {
		T old;
		if(i == 0) { old = e1; e1 = value; }
		else if(i == 1) { old = e2; e2 = value; }
		else if(i == 2) { old = e3; e3 = value; }
		else throw new ArrayIndexOutOfBoundsException(i);
		 
		return old;
	}
	
	

	@Override
	public int size() {
		return 3;
	}
	
	

	@Override
	public boolean isEmpty() {
		return false;
	}
	
	

	@Override
	public boolean contains(Object o) {
		if(o == null) return e1 == null || e2 == null || e3 == null;
		return o.equals(e1) || o.equals(e2) || o.equals(e3);
	}

	
	
	@Override
	public Iterator<T> iterator() {
		return new Array3Iterator<>(this);
	}

	
	
	@Override
	public Object[] toArray() {
		return new Object[] {e1, e2, e3};
	}
	
	

	@Override
	@SuppressWarnings("unchecked")
	public <U> U[] toArray(U[] a) {
		if(a.length < 3) {
			return (U[]) new Object[] {e1, e2, e3};
		}
		else {
			U[] result = Arrays.copyOf(a, a.length);
			result[0] = (U) e1; result[1] = (U) e2; result[2] = (U) e3;
			return result;
		}
	}

	
	
	@Override
	public boolean add(T e) {
		throw new UnsupportedOperationException();
	}

	
	
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	
	
	@Override
	public boolean containsAll(Collection<?> c) {
		if(c.size() > 3) return false;
		return c.contains(e1) && c.contains(e2) && c.contains(e3);
	}

	
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}
	
	

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}
	
	

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	
	
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException();
	}

	
	
	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException();
	}
	
	

	@Override
	public int indexOf(Object o) {
		if(o == null) {
			if(e1 == null) return 0;
			if(e2 == null) return 1;
			if(e3 == null) return 2;
			return -1;
		}
		if(o.equals(e1)) return 0;
		if (o.equals(e2)) return 1;
		if(o.equals(e3)) return 2;
		return -1;
	}

	
	
	@Override
	public int lastIndexOf(Object o) {
		if(o == null) {
			if(e3 == null) return 0;
			if(e2 == null) return 1;
			if(e1 == null) return 2;
			return -1;
		}
		if(o.equals(e3)) return 0;
		if (o.equals(e2)) return 1;
		if(o.equals(e1)) return 2;
		return -1;
	}
	
	

	@Override
	public ListIterator<T> listIterator() {
		return new Array3ListIterator<T>(this, 0);
	}

	
	
	@Override
	public ListIterator<T> listIterator(int index) {
		return new Array3ListIterator<T>(this, index);
	}

	
	
	@Override
	public List<T> subList(int s, int e) {
		if(s >= e || s > 2) return new ArrayList<>();
		if(s >= 0) {
			if(e == 1) return Arrays.asList(e1);
			if(e == 2) return Arrays.asList(e1, e2);
			return Arrays.asList(e1, e2, e3);
		}
		if(s == 1) {
			if(e == 2) return Arrays.asList(e2);
			return Arrays.asList(e2, e3);
		}
		else {
			return Arrays.asList(e3);
		}
	}
	
	
	
	@Override
	public String toString() {
		return "[" + e1 + ", " + e2 + ", " + e3 + "]";
	}
	
	
	
	private static class Array3Iterator<T> implements Iterator<T> {
		
		protected int i;
		protected final Array3<T> parent;
		
		Array3Iterator(Array3<T> parent) {
			this.parent = parent;
		}
		
		@Override
		public boolean hasNext() {
			return i != 2;
		}

		@Override
		public T next() {
			return parent.get(i++);
		}
	}
	
	
	
	private static class Array3ListIterator<T> extends Array3Iterator<T> implements ListIterator<T> {

		Array3ListIterator(Array3<T> parent, int initialIndex) {
			super(parent);
			this.i = initialIndex;
		}

		@Override
		public boolean hasPrevious() {
			return i != 0;
		}

		@Override
		public T previous() {
			return parent.get(i--);
		}

		@Override
		public int nextIndex() {
			return i + 1;
		}

		@Override
		public int previousIndex() {
			return i - 1;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(T e) {
			parent.set(i, e);
		}

		@Override
		public void add(T e) {
			throw new UnsupportedOperationException();
		}
		
	}
}