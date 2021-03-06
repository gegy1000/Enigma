package cuchaz.enigma.translation.mapping.tree;

import cuchaz.enigma.translation.representation.entry.Entry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class HashEntryTree<T> implements EntryTree<T> {
	private final Map<Entry<?>, HashTreeNode<T>> root = new HashMap<>();

	@Override
	public void insert(Entry<?> entry, T value) {
		List<HashTreeNode<T>> path = computePath(entry);
		path.get(path.size() - 1).putValue(value);
		if (value == null) {
			removeDeadAlong(path);
		}
	}

	@Override
	@Nullable
	public T remove(Entry<?> entry) {
		List<HashTreeNode<T>> path = computePath(entry);
		T value = path.get(path.size() - 1).removeValue();

		removeDeadAlong(path);

		return value;
	}

	@Override
	@Nullable
	public T get(Entry<?> entry) {
		HashTreeNode<T> node = findNode(entry);
		if (node == null) {
			return null;
		}
		return node.getValue();
	}

	@Override
	public boolean contains(Entry<?> entry) {
		return get(entry) != null;
	}

	@Override
	public Collection<Entry<?>> getChildren(Entry<?> entry) {
		HashTreeNode<T> leaf = findNode(entry);
		if (leaf == null) {
			return Collections.emptyList();
		}
		return leaf.getChildren();
	}

	@Override
	public Collection<Entry<?>> getSiblings(Entry<?> entry) {
		List<HashTreeNode<T>> path = computePath(entry);
		if (path.size() <= 1) {
			return getSiblings(entry, root.keySet());
		}
		HashTreeNode<T> parent = path.get(path.size() - 2);
		return getSiblings(entry, parent.getChildren());
	}

	private Collection<Entry<?>> getSiblings(Entry<?> entry, Collection<Entry<?>> children) {
		Set<Entry<?>> siblings = new HashSet<>(children);
		siblings.remove(entry);
		return siblings;
	}

	@Override
	@Nullable
	public HashTreeNode<T> findNode(Entry<?> target) {
		List<Entry<?>> parentChain = target.getAncestry();
		if (parentChain.isEmpty()) {
			return null;
		}

		HashTreeNode<T> node = root.get(parentChain.get(0));
		for (int i = 1; i < parentChain.size(); i++) {
			if (node == null) {
				return null;
			}
			node = node.getChild(parentChain.get(i), false);
		}

		return node;
	}

	private List<HashTreeNode<T>> computePath(Entry<?> target) {
		List<Entry<?>> ancestry = target.getAncestry();
		if (ancestry.isEmpty()) {
			return Collections.emptyList();
		}

		List<HashTreeNode<T>> path = new ArrayList<>(ancestry.size());

		Entry<?> rootEntry = ancestry.get(0);
		HashTreeNode<T> node = root.computeIfAbsent(rootEntry, HashTreeNode::new);
		path.add(node);

		for (int i = 1; i < ancestry.size(); i++) {
			node = node.getChild(ancestry.get(i), true);
			path.add(node);
		}

		return path;
	}

	private void removeDeadAlong(List<HashTreeNode<T>> path) {
		for (int i = path.size() - 1; i >= 0; i--) {
			HashTreeNode<T> node = path.get(i);
			if (node.isEmpty()) {
				if (i > 0) {
					HashTreeNode<T> parentNode = path.get(i - 1);
					parentNode.remove(node.getEntry());
				} else {
					root.remove(node.getEntry());
				}
			} else {
				break;
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<EntryTreeNode<T>> iterator() {
		Collection<EntryTreeNode<T>> values = (Collection) root.values();
		return values.iterator();
	}

	@Override
	public Collection<EntryTreeNode<T>> getAllNodes() {
		Collection<EntryTreeNode<T>> nodes = new ArrayList<>();
		for (EntryTreeNode<T> node : root.values()) {
			nodes.addAll(node.getNodesRecursively());
		}
		return nodes;
	}

	@Override
	public Collection<Entry<?>> getAllEntries() {
		return getAllNodes().stream()
				.map(EntryTreeNode::getEntry)
				.collect(Collectors.toList());
	}

	@Override
	public Collection<Entry<?>> getRootEntries() {
		return root.keySet();
	}

	@Override
	public boolean isEmpty() {
		return root.isEmpty();
	}
}
