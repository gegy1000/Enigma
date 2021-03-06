package cuchaz.enigma.translation.mapping.tree;

import cuchaz.enigma.translation.mapping.MappingDelta;
import cuchaz.enigma.translation.representation.entry.Entry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;

public class DeltaTrackingTree<T> implements EntryTree<T> {
	private final EntryTree<T> delegate;

	private EntryTree<Object> additions = new HashEntryTree<>();
	private EntryTree<Object> deletions = new HashEntryTree<>();

	public DeltaTrackingTree(EntryTree<T> delegate) {
		this.delegate = delegate;
	}

	public DeltaTrackingTree() {
		this(new HashEntryTree<>());
	}

	@Override
	public void insert(Entry<?> entry, T value) {
		if (value != null) {
			trackAddition(entry);
		} else {
			trackDeletion(entry);
		}
		delegate.insert(entry, value);
	}

	@Nullable
	@Override
	public T remove(Entry<?> entry) {
		T value = delegate.remove(entry);
		trackDeletion(entry);
		return value;
	}

	public void trackAddition(Entry<?> entry) {
		deletions.remove(entry);
		additions.insert(entry, MappingDelta.PLACEHOLDER);
	}

	public void trackDeletion(Entry<?> entry) {
		additions.remove(entry);
		deletions.insert(entry, MappingDelta.PLACEHOLDER);
	}

	@Nullable
	@Override
	public T get(Entry<?> entry) {
		return delegate.get(entry);
	}

	@Override
	public Collection<Entry<?>> getChildren(Entry<?> entry) {
		return delegate.getChildren(entry);
	}

	@Override
	public Collection<Entry<?>> getSiblings(Entry<?> entry) {
		return delegate.getSiblings(entry);
	}

	@Nullable
	@Override
	public EntryTreeNode<T> findNode(Entry<?> entry) {
		return delegate.findNode(entry);
	}

	@Override
	public Collection<EntryTreeNode<T>> getAllNodes() {
		return delegate.getAllNodes();
	}

	@Override
	public Collection<Entry<?>> getRootEntries() {
		return delegate.getRootEntries();
	}

	@Override
	public Collection<Entry<?>> getAllEntries() {
		return delegate.getAllEntries();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public Iterator<EntryTreeNode<T>> iterator() {
		return delegate.iterator();
	}

	public MappingDelta takeDelta() {
		MappingDelta delta = new MappingDelta(additions, deletions);
		resetDelta();
		return delta;
	}

	private void resetDelta() {
		additions = new HashEntryTree<>();
		deletions = new HashEntryTree<>();
	}

	public boolean isDirty() {
		return !additions.isEmpty() || !deletions.isEmpty();
	}
}
