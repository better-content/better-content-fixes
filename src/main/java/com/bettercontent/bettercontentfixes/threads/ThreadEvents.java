package com.bettercontent.bettercontentfixes.threads;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.*;

public final class ThreadEvents {
    private static final Map<UUID,Integer> baselineDue=new HashMap<>(),contextDue=new HashMap<>();
    private static final Set<UUID> ready=new HashSet<>();
    @SubscribeEvent public static void reload(AddReloadListenerEvent event){event.addListener(ThreadDefinitions.INSTANCE);}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer p)baselineDue.put(p.getUUID(),p.server.getTickCount()+20);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer p){ThreadPlayerState.get(p).save(p);ThreadPlayerState.forget(p);ready.remove(p.getUUID());baselineDue.remove(p.getUUID());contextDue.remove(p.getUUID());}}
    @SubscribeEvent public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event){schedule(event.getEntity());}
    @SubscribeEvent public static void crafted(PlayerEvent.ItemCraftedEvent event){schedule(event.getEntity());}
    @SubscribeEvent public static void smelted(PlayerEvent.ItemSmeltedEvent event){schedule(event.getEntity());}
    @SubscribeEvent public static void pickedUp(PlayerEvent.ItemPickupEvent event){schedule(event.getEntity());}
    private static void schedule(net.minecraft.world.entity.player.Player player){if(player instanceof ServerPlayer p&&ready.contains(p.getUUID()))contextDue.put(p.getUUID(),p.server.getTickCount()+1);}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;int tick=event.getServer().getTickCount();
        for(var player:event.getServer().getPlayerList().getPlayers()){
            UUID id=player.getUUID();Integer baseline=baselineDue.get(id);
            if(baseline!=null&&tick>=baseline){baselineDue.remove(id);evaluate(player,false);ready.add(id);ThreadNetwork.sync(player,false,List.of());continue;}
            Integer context=contextDue.get(id);if(context!=null&&tick>=context){contextDue.remove(id);evaluate(player,true);continue;}
            if(ready.contains(id)&&tick%100==0)evaluate(player,true);
        }
    }
    static void evaluate(ServerPlayer player,boolean notify){
        var state=ThreadPlayerState.get(player);boolean dirty=false;ThreadNetwork.Notice notice=null;
        String dimension="dimension:"+player.serverLevel().dimension().location();if(state.everSeen.add(dimension))dirty=true;
        for(var definition:ThreadDefinitions.INSTANCE.all()){
            var result=ThreadPredicateEvaluator.result(player,definition,state);if(!result.encountered())continue;
            boolean newlyCollected=state.collect(definition.id());int before=state.phases.getOrDefault(definition.id(),0),after=state.advance(definition.id(),result.phase());
            if(newlyCollected||after>before){state.unread.add(definition.id());dirty=true;if(notify&&notice==null)notice=ThreadNetwork.notice(definition);}
        }
        if(dirty)state.save(player);if(dirty||notice!=null)ThreadNetwork.sync(player,false,notice==null?List.of():List.of(notice));
    }
}
