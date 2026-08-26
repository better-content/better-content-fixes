package com.bettercontent.bettercontentfixes.threads;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import java.util.*;

public final class ThreadPlayerState {
    private static final String KEY = "BetterContentThreads";
    public final Deque<String> held = new ArrayDeque<>();
    public final Map<String,Integer> phases = new HashMap<>();
    public final Set<String> everSeen = new HashSet<>();
    public final Deque<String> wildHistory = new ArrayDeque<>();
    public final Deque<String> edgeQueue = new ArrayDeque<>();
    public long lastLoginReal, lastLogoutReal, lastOvertureReal, lastEdgeReal;

    public static ThreadPlayerState load(Player player) {
        var root = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(KEY);
        var state = new ThreadPlayerState();
        readStrings(root, "held").stream().filter(ThreadDefinitions.INSTANCE::contains).limit(2).forEach(state.held::addLast);
        readStrings(root, "wild").stream().filter(ThreadDefinitions.INSTANCE::contains).limit(7).forEach(state.wildHistory::addLast);
        readStrings(root, "seen").stream().filter(ThreadDefinitions.INSTANCE::contains).forEach(state.everSeen::add);
        readStrings(root, "edges").stream().filter(ThreadDefinitions.INSTANCE::contains).limit(18).forEach(state.edgeQueue::addLast);
        var phaseTag = root.getCompound("phases");
        phaseTag.getAllKeys().stream().filter(ThreadDefinitions.INSTANCE::contains).forEach(id -> state.phases.put(id, Math.max(0, Math.min(2, phaseTag.getInt(id)))));
        state.lastLoginReal=root.getLong("lastLoginReal"); state.lastLogoutReal=root.getLong("lastLogoutReal");
        state.lastOvertureReal=root.getLong("lastOvertureReal"); state.lastEdgeReal=root.getLong("lastEdgeReal");
        return state;
    }
    public void save(Player player) {
        var persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        var root = new CompoundTag();
        root.put("held", strings(held)); root.put("wild", strings(wildHistory)); root.put("seen", strings(everSeen)); root.put("edges", strings(edgeQueue));
        var p = new CompoundTag(); phases.forEach(p::putInt); root.put("phases", p);
        root.putLong("lastLoginReal",lastLoginReal); root.putLong("lastLogoutReal",lastLogoutReal);
        root.putLong("lastOvertureReal",lastOvertureReal); root.putLong("lastEdgeReal",lastEdgeReal);
        persisted.put(KEY, root); player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
    public String hold(String id) {
        if (held.remove(id)) return null;
        String replaced = held.size() == 2 ? held.removeFirst() : null; held.addLast(id); return replaced;
    }
    public void unhold(String id) { held.remove(id); }
    public int advance(String id, int candidate) { int next=Math.max(phases.getOrDefault(id,0), Math.min(2,candidate)); phases.put(id,next); return next; }
    public void rememberWild(String id) { wildHistory.remove(id); wildHistory.addLast(id); while(wildHistory.size()>7) wildHistory.removeFirst(); }
    private static List<String> readStrings(CompoundTag root,String key){ return root.getList(key, Tag.TAG_STRING).stream().map(Tag::getAsString).toList(); }
    private static ListTag strings(Collection<String> values){ var list=new ListTag(); values.forEach(v->list.add(StringTag.valueOf(v))); return list; }
}
