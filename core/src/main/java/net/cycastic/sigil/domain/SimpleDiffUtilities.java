package net.cycastic.sigil.domain;

import lombok.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
@RequiredArgsConstructor
public class SimpleDiffUtilities<T, K> {
    public interface KeySelector<T, K>{
        K getKey(T entity);
    }

    public interface Comparator<T>{
        boolean isDifferent(T lhs, T rhs);
    }

    public record UpdatedEntity<T>(T original, T updated){}

    @Getter
    @Builder
    @RequiredArgsConstructor
    public static class Diff<T> {
        private final Collection<T> newEntities;
        private final Collection<T> deletedEntities;
        private final Collection<UpdatedEntity<T>> updatedEntities;
    }

    private final KeySelector<T, K> keySelector;
    private final Comparator<T> comparator;

    private Map<K, T> buildMap(Collection<T> collection){
        return collection.stream()
                .collect(Collectors.toMap(keySelector::getKey, v -> v));
    }

    public Diff<T> shallowDiff(Collection<T> original, Collection<T> newCollection){
        var originalMap = buildMap(original);
        var newMap = buildMap(newCollection);

        var deletedEntities = new ArrayList<T>();
        var updatedEntities = new ArrayList<UpdatedEntity<T>>();

        for (var originalPair : originalMap.entrySet()){
            var rhs = newMap.get(originalPair.getKey());
            if (rhs != null){
                newMap.remove(originalPair.getKey());
                if (comparator.isDifferent(originalPair.getValue(), rhs)){
                    updatedEntities.add((new UpdatedEntity<>(originalPair.getValue(), rhs)));
                }
                continue;
            }

            deletedEntities.add(originalPair.getValue());
        }

        var newEntities = newMap.values().stream()
                .toList();
        Diff.DiffBuilder<T> diffBuilder = Diff.builder();
        diffBuilder = diffBuilder
                .newEntities(newEntities)
                .updatedEntities(updatedEntities)
                .deletedEntities(deletedEntities);
        return diffBuilder.build();
    }
}
