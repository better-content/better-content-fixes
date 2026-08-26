package com.bettercontent.bettercontentfixes.threads;

import java.util.*;

public final class ThreadSelection {
    private ThreadSelection() {}
    public static List<String> possibilities(Collection<ThreadDefinition> definitions, ThreadPlayerState state, long seed) {
        var result = new ArrayList<String>(state.held);
        var random = new Random(seed);
        if (result.size() == 1) pick(definitions, state, result, true, random).ifPresent(result::add);
        else if (result.isEmpty()) {
            pick(definitions, state, result, false, random).ifPresent(result::add);
            pick(definitions, state, result, true, random).ifPresent(result::add);
        }
        return result;
    }
    private static Optional<String> pick(Collection<ThreadDefinition> defs, ThreadPlayerState state, List<String> used, boolean wild, Random random) {
        var candidates=defs.stream().map(ThreadDefinition::id).filter(id->!used.contains(id));
        if(wild) candidates=candidates.filter(id->!state.everSeen.contains(id)&&!state.wildHistory.contains(id));
        else candidates=candidates.filter(id->state.phases.getOrDefault(id,0)>0||state.everSeen.contains(id));
        var list=new ArrayList<>(candidates.toList());
        if(list.isEmpty()) list.addAll(defs.stream().map(ThreadDefinition::id).filter(id->!used.contains(id)&&(!wild||!state.wildHistory.contains(id))).toList());
        if(list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(random.nextInt(list.size())));
    }
}
