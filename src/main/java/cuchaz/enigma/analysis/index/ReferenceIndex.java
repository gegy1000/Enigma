package cuchaz.enigma.analysis.index;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import cuchaz.enigma.analysis.EntryReference;
import cuchaz.enigma.translation.mapping.EntryResolver;
import cuchaz.enigma.translation.mapping.ResolutionStrategy;
import cuchaz.enigma.translation.representation.entry.*;

import java.util.Collection;
import java.util.Map;

public class ReferenceIndex implements JarIndexer {
	private Multimap<MethodEntry, MethodEntry> methodReferences = HashMultimap.create();

	private Multimap<MethodEntry, EntryReference<MethodEntry, MethodDefEntry>> referencesToMethods = HashMultimap.create();
	private Multimap<ClassEntry, EntryReference<ClassEntry, MethodDefEntry>> referencesToClasses = HashMultimap.create();
	private Multimap<FieldEntry, EntryReference<FieldEntry, MethodDefEntry>> referencesToFields = HashMultimap.create();

	@Override
	public void indexMethodReference(MethodDefEntry callerEntry, MethodEntry referencedEntry) {
		referencesToMethods.put(referencedEntry, new EntryReference<>(referencedEntry, referencedEntry.getName(), callerEntry));
		methodReferences.put(callerEntry, referencedEntry);

		if (referencedEntry.isConstructor()) {
			ClassEntry referencedClass = referencedEntry.getParent();
			referencesToClasses.put(referencedClass, new EntryReference<>(referencedClass, referencedEntry.getName(), callerEntry));
		}
	}

	@Override
	public void indexFieldReference(MethodDefEntry callerEntry, FieldEntry referencedEntry) {
		referencesToFields.put(referencedEntry, new EntryReference<>(referencedEntry, referencedEntry.getName(), callerEntry));
	}

	@Override
	public void processIndex(EntryResolver resolver) {
		methodReferences = resolveReferences(resolver, methodReferences);
		referencesToMethods = resolveReferencesTo(resolver, referencesToMethods);
		referencesToClasses = resolveReferencesTo(resolver, referencesToClasses);
		referencesToFields = resolveReferencesTo(resolver, referencesToFields);
	}

	private <K extends Entry<?>, V extends Entry<?>> Multimap<K, V> resolveReferences(EntryResolver resolver, Multimap<K, V> multimap) {
		Multimap<K, V> resolved = HashMultimap.create();
		for (Map.Entry<K, V> entry : multimap.entries()) {
			resolved.put(resolve(resolver, entry.getKey()), resolve(resolver, entry.getValue()));
		}
		return resolved;
	}

	private <E extends Entry<?>, C extends Entry<?>> Multimap<E, EntryReference<E, C>> resolveReferencesTo(EntryResolver resolver, Multimap<E, EntryReference<E, C>> multimap) {
		Multimap<E, EntryReference<E, C>> resolved = HashMultimap.create();
		for (Map.Entry<E, EntryReference<E, C>> entry : multimap.entries()) {
			resolved.put(resolve(resolver, entry.getKey()), resolve(resolver, entry.getValue()));
		}
		return resolved;
	}

	private <E extends Entry<?>> E resolve(EntryResolver resolver, E entry) {
		return resolver.resolveFirstEntry(entry, ResolutionStrategy.RESOLVE_CLOSEST);
	}

	private <E extends Entry<?>, C extends Entry<?>> EntryReference<E, C> resolve(EntryResolver resolver, EntryReference<E, C> reference) {
		return resolver.resolveFirstReference(reference, ResolutionStrategy.RESOLVE_CLOSEST);
	}

	public Collection<MethodEntry> getMethodsReferencedBy(MethodEntry entry) {
		return methodReferences.get(entry);
	}

	public Collection<EntryReference<FieldEntry, MethodDefEntry>> getReferencesToField(FieldEntry entry) {
		return referencesToFields.get(entry);
	}

	public Collection<EntryReference<ClassEntry, MethodDefEntry>> getReferencesToClass(ClassEntry entry) {
		return referencesToClasses.get(entry);
	}

	public Collection<EntryReference<MethodEntry, MethodDefEntry>> getReferencesToMethod(MethodEntry entry) {
		return referencesToMethods.get(entry);
	}
}
