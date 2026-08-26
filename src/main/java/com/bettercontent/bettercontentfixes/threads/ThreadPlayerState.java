package com.bettercontent.bettercontentfixes.threads;

import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import java.util.*;

public final class ThreadPlayerState {
    private static final String KEY="BetterContentThreads";
    private static final ResourceLocation LINEAGE_KEY=new ResourceLocation("better_content_fixes","threads");
    private static final Map<UUID,ThreadPlayerState> CACHE=new HashMap<>();
    public final Set<String> collected=new LinkedHashSet<>(),unread=new LinkedHashSet<>(),everSeen=new HashSet<>();
    public final Map<String,Integer> phases=new HashMap<>();

    public static ThreadPlayerState get(ServerPlayer player){return CACHE.computeIfAbsent(player.getUUID(),ignored->fromTag(readLineage(player)));}
    public static void forget(ServerPlayer player){CACHE.remove(player.getUUID());}
    static ThreadPlayerState fromTag(CompoundTag root){
        var state=new ThreadPlayerState();
        readStrings(root,"collected").stream().filter(ThreadDefinitions.INSTANCE::contains).limit(18).forEach(state.collected::add);
        readStrings(root,"unread").stream().filter(state.collected::contains).limit(18).forEach(state.unread::add);
        readStrings(root,"seen").stream().filter(value->value.length()<=160).limit(256).forEach(state.everSeen::add);
        var phaseTag=root.getCompound("phases");phaseTag.getAllKeys().stream().filter(state.collected::contains).forEach(id->state.phases.put(id,Math.max(0,Math.min(2,phaseTag.getInt(id)))));
        return state;
    }
    CompoundTag toTag(){var root=new CompoundTag();root.put("collected",strings(collected));root.put("unread",strings(unread));root.put("seen",strings(everSeen));var p=new CompoundTag();phases.forEach(p::putInt);root.put("phases",p);return root;}
    public void save(ServerPlayer player){var root=toTag();if(writeLineage(player,root))return;var persisted=player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);persisted.put(KEY,root);player.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);}
    public boolean collect(String id){if(!ThreadDefinitions.INSTANCE.contains(id)||!collected.add(id))return false;unread.add(id);phases.putIfAbsent(id,0);return true;}
    public boolean markRead(String id){return unread.remove(id);}
    public int advance(String id,int candidate){int next=Math.max(phases.getOrDefault(id,0),Math.min(2,candidate));phases.put(id,next);return next;}
    private static List<String> readStrings(CompoundTag root,String key){return root.getList(key,Tag.TAG_STRING).stream().map(Tag::getAsString).toList();}
    private static ListTag strings(Collection<String> values){var list=new ListTag();values.forEach(v->list.add(StringTag.valueOf(v)));return list;}
    private static CompoundTag readLineage(ServerPlayer player){
        try{var api=Class.forName("com.bettercontent.worldlifecyclemanager.api.LineagePlayerDataApi");return(CompoundTag)api.getMethod("read",net.minecraft.server.MinecraftServer.class,ResourceLocation.class,UUID.class).invoke(null,player.server,LINEAGE_KEY,player.getUUID());}
        catch(ClassNotFoundException|NoSuchMethodException ignored){return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(KEY);}
        catch(ReflectiveOperationException failure){throw new IllegalStateException("Could not read lineage Thread state",failure);}
    }
    private static boolean writeLineage(ServerPlayer player,CompoundTag root){
        try{var api=Class.forName("com.bettercontent.worldlifecyclemanager.api.LineagePlayerDataApi");api.getMethod("write",net.minecraft.server.MinecraftServer.class,ResourceLocation.class,UUID.class,CompoundTag.class).invoke(null,player.server,LINEAGE_KEY,player.getUUID(),root);return true;}
        catch(ClassNotFoundException|NoSuchMethodException ignored){return false;}
        catch(ReflectiveOperationException failure){throw new IllegalStateException("Could not write lineage Thread state",failure);}
    }
}
