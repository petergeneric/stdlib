package com.mediasmiths.std.types.collections;

import java.util.*;

/**
 * A priority queue where lower numbers have a higher priority. The minimum priority is zero<br />
 * There is a memory cost associated with the highest priority stored
 * 
 * 
 * TODO reimplement as a Linked List with a list of pointers to the end of each priority queue
 * @param T the item type
 */
public class OrderedPriorityQueue<T extends IPrioritisable> implements Queue<T> {

	/**
	 * lazy-created queues for each priority
	 */
	private LinkedList<T>[] queues = null;

	/**
	 * The number of items in the queue
	 */
	private int size = 0;

	/**
	 * The revision number of the datastructure (used to make iterators threadsafe)<br />
	 */
	private transient long modcount = Long.MIN_VALUE;


	public OrderedPriorityQueue() {
		this(0);
	}


	public OrderedPriorityQueue(Iterable<T> items) {
		this(1);

		addAll(items);
	}


	public OrderedPriorityQueue(int maxPriority) {
		setMaxPriority(0);
	}


	public OrderedPriorityQueue(int maxPriority, Iterable<T> items) {
		setMaxPriority(0);

		for (T item : items)
			add(item);
	}


	@SuppressWarnings("unchecked")
	private void setMaxPriority(int maxPriority) {
		if (maxPriority >= 0) {
			LinkedList<T>[] old = queues;
			queues = new LinkedList[maxPriority + 1];

			if (old != null) {
				final int sz = Math.min(old.length, queues.length);

				for (int i = 0; i < sz; i++)
					queues[i] = old[i];
			}
		}
		else {
			throw new IllegalArgumentException("max priority must be >= 0: " + maxPriority);
		}
	}


	@Override
	public synchronized int size() {
		return size;
	}


	@Override
	public boolean offer(T e) {
		return add(e);
	}


	/**
	 * @deprecated use offer() or add() instead
	 * @param e The item to add
	 */
	@Deprecated
	public void put(T e) {
		add(e);
	}


	/**
	 * Determines the storage priority of a given item (override this to use a different prioritisation criteria)
	 * 
	 * @param op
	 * @return
	 * @throws IllegalArgumentException if passed Null
	 */
	protected int getPriority(T op) {
		if (op == null)
			throw new IllegalArgumentException(
					"null item has undefined priority",
					new NullPointerException("item to add was null"));
		return op.getPriority();
	}


	@Override
	public boolean add(T op) {
		if (op == null)
			return false;

		return add(getPriority(op), op);
	}


	protected synchronized boolean add(int priority, T op) {
		if (op == null)
			return false;

		if (priority >= queues.length) {
			setMaxPriority(priority + 1);
		}
		else if (priority < 0) {
			throw new IllegalArgumentException("Invalid priority " + priority + ": must be >= 0");
		}

		// Get or lazy-create the queue
		LinkedList<T> q = queues[priority];
		if (q == null) {
			q = new LinkedList<T>();
			queues[priority] = q;
		}

		q.add(op);
		size++;
		notifyAll();
		modcount++;

		return true;
	}


	@Override
	public synchronized T poll() {
		return poll(true);
	}


	public synchronized T poll(boolean take) {
		for (int priority = 0; priority < queues.length; priority++) {
			LinkedList<T> q = queues[priority];

			if (q != null) {
				T o = take ? q.poll() : q.peek();
				if (o != null) {
					if (take) {
						modcount++;
						size--;
						notifyAll();
					}
					return o;
				}
			}
		}

		return null;
	}


	/**
	 * Takes an element from the queue, optionally blocking until there is a free item
	 * 
	 * @return
	 */
	public synchronized T take() {
		T op = poll();

		while (op == null) {
			// Wait for a change
			try {
				wait(250);
			}
			catch (InterruptedException e) {
			}

			op = poll();
		}

		return op;
	}


	/**
	 * Take an element from the queue, optionally waiting for new items for up to <code>timeout</code> milliseconds if the queue is empty
	 * 
	 * @param timeout The maximum timeout in milliseconds
	 * @return An item (or null if no item was available within the specified timeout)
	 */
	public synchronized T take(long timeout) {
		timeout += System.currentTimeMillis();

		T op = poll();

		while (op == null && System.currentTimeMillis() < timeout) {
			// Wait for a change
			try {
				wait(500);
			}
			catch (InterruptedException e) {
			}

			op = poll();
		}

		return op;
	}


	/**
	 * Drain all items of a particular priority
	 * 
	 * @param priority the priority
	 * @return A list of the items contained in that priority queue
	 */
	public synchronized List<T> drainPriority(int priority) {
		List<T> items = queues.length > priority ? queues[priority] : null;

		if (items != null) {
			queues[priority] = null;
			size -= items.size();
			modcount++;
		}
		else
			items = Collections.emptyList();

		return items;
	}


	@Override
	public T element() {
		T obj = peek();

		if (obj == null)
			throw new NoSuchElementException();
		else
			return obj;
	}


	@Override
	public T peek() {
		return poll(false);
	}


	@Override
	public T remove() {
		T o = poll();

		if (o == null)
			throw new NoSuchElementException();
		else
			return o;
	}


	@Override
	public boolean addAll(Collection<? extends T> c) {
		for (T obj : c)
			add(obj);

		return true;
	}


	public boolean addAll(Iterable<? extends T> c) {
		for (T obj : c)
			add(obj);

		return true;
	}


	@Override
	public synchronized void clear() {
		this.queues = null;
		this.size = 0;
		setMaxPriority(0);
	}


	@Override
	public boolean contains(Object o) {
		for (int p = 0; p < queues.length; p++) {
			LinkedList<T> q = queues[p];

			if (q != null && q.contains(o))
				return true;
		}

		return false;
	}


	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o))
				return false;
		}

		return true;
	}


	@Override
	public boolean isEmpty() {
		return size == 0;
	}


	@Override
	public Iterator<T> iterator() {
		return new QIt(this);
	}


	@Override
	public synchronized boolean remove(Object o) {
		// Optimised remove call
		boolean changed = false;
		for (int p = 0; p < queues.length; p++) {
			LinkedList<T> q = queues[p];

			if (q != null) {
				int oldSize = q.size();
				boolean localChange = q.remove(o);

				if (localChange)
					size -= (oldSize - q.size());

				changed = changed || localChange;
			}
		}

		return changed;
	}


	@Override
	public boolean removeAll(final Collection<?> c) {
		// Optimised remove call
		boolean changed = false;
		for (int p = 0; p < queues.length; p++) {
			LinkedList<T> q = queues[p];

			if (q != null) {
				int oldSize = q.size();
				boolean localChange = q.removeAll(c);

				if (localChange)
					size -= (oldSize - q.size());

				changed = changed || localChange;
			}
		}

		return changed;
	}


	@Override
	public boolean retainAll(final Collection<?> c) {
		final Iterator<T> it = this.iterator();
		boolean changed = false;

		while (it.hasNext()) {
			T item = it.next();

			if (!c.contains(item)) {
				changed = true;
				it.remove();
			}
		}

		return changed;
	}


	@Override
	public synchronized Object[] toArray() {
		Object[] result = new Object[size];

		Iterator<T> it = this.iterator();
		for (int i = 0; i < size; i++)
			result[i] = it.next();

		return result;
	}


	@Override
	@SuppressWarnings("unchecked")
	public <A> A[] toArray(A[] a) {
		if (a.length < size)
			a = (A[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);

		Object[] result = a;
		Iterator<T> it = this.iterator();
		for (int i = 0; i < size; i++)
			result[i] = it.next();

		if (a.length > size)
			a[size] = null;

		return a;
	}

	@SuppressWarnings("unchecked")
	public <A> A[] toArray(Class<A> type) {
		A[] a = (A[]) java.lang.reflect.Array.newInstance(type, size);

		return toArray(a);
	}

	// /// ------------------------------------------- ///////
	// /// -----------------Iterator------------------ ///////
	// /// ------------------------------------------- ///////

	/**
	 * An iterator over OrderedPriorityQueue
	 * 
	 * 
	 */
	private class QIt implements Iterator<T> {
		private final OrderedPriorityQueue<T> obj; // For testing the modcount
		private long iteratorModcount; // keep track of what the modcount in the Queue should be (to catch modifications we don't make)

		private final Iterator<Iterable<T>> allQueues;
		private Iterator<T> current;
		private Iterator<T> next;


		public QIt(OrderedPriorityQueue<T> obj) {
			this.iteratorModcount = obj.modcount;
			this.obj = obj;

			// Turn the Queue's internal lists into a list of iterables
			List<Iterable<T>> items = new ArrayList<Iterable<T>>();
			for (LinkedList<T> q : obj.queues) {
				if (q != null && q.size() != 0) {
					items.add(q);
				}
			}
			allQueues = items.iterator();

			// Set up the two necessary iterators
			current = allQueues.hasNext() ? allQueues.next().iterator() : null;
			next = allQueues.hasNext() ? allQueues.next().iterator() : null;
		}


		@Override
		public boolean hasNext() {
			if (this.iteratorModcount != obj.modcount)
				throw new ConcurrentModificationException();
			if (current == null)
				return false;

			return current.hasNext() || (next != null && next.hasNext());
		}


		@Override
		public T next() {
			if (this.iteratorModcount != obj.modcount)
				throw new ConcurrentModificationException();

			// If there's nothing more in current, overflow to next.
			// If there's a next, move it to current & acquire the NEXT "next" from the queues iterator
			// If there's no next, throw an exception
			if (!current.hasNext()) {
				if (next != null) {
					current = next;
					next = allQueues.hasNext() ? allQueues.next().iterator() : null;
				}
				else {
					throw new NoSuchElementException();
				}
			}

			return current.next();
		}


		@Override
		public void remove() {
			// Update the modcounts
			this.iteratorModcount++;
			obj.modcount++;

			// Get rid of the data
			current.remove();

			// Make sure we always have a correct size
			obj.size--;
		}
	}
}
