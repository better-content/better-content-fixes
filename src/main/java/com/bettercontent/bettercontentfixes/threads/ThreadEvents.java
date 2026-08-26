package com.bettercontent.bettercontentfixes.threads;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.*;

public final class ThreadEvents {
    private static final long RETURN_MS=4L*60*60*1000, EDGE_MS=60_000;
    private static final Map<UUID,Integer> due=new HashMap<>();
    @SubscribeEvent public static void reload(AddReloadListenerEvent event){event.addListener(ThreadDefinitions.INSTANCE);}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer p)due.put(p.getUUID(),p.server.getTickCount()+120);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer p){var s=ThreadPlayerState.load(p);s.lastLogoutReal=System.currentTimeMillis();s.save(p);due.remove(p.getUUID());}}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;int tick=event.getServer().getTickCount();
        for(var p:event.getServer().getPlayerList().getPlayers()){
            if(tick%100==0)evaluate(p);
            Integer target=due.get(p.getUUID());if(target!=null&&tick>=target){due.remove(p.getUUID());overture(p);}
        }
    }
    private static void evaluate(ServerPlayer player){
        var state=ThreadPlayerState.load(player);ThreadNetwork.Card edge=null;
        state.everSeen.add("dimension:"+player.serverLevel().dimension().location());
        for(var d:ThreadDefinitions.INSTANCE.all()){
            int before=state.phases.getOrDefault(d.id(),0), after=state.advance(d.id(),ThreadPredicateEvaluator.phase(player,d,state));
            if(after>before&&state.held.contains(d.id())&&!state.edgeQueue.contains(d.id()))state.edgeQueue.addLast(d.id());
        }
        if(!state.edgeQueue.isEmpty()&&System.currentTimeMillis()-state.lastEdgeReal>=EDGE_MS){var d=ThreadDefinitions.INSTANCE.get(state.edgeQueue.removeFirst());if(d!=null){edge=card(d,state,"near");state.lastEdgeReal=System.currentTimeMillis();}}
        state.save(player);if(edge!=null)ThreadNetwork.sync(player,false,List.of(),List.of(edge));
    }
    private static void overture(ServerPlayer player){
        var state=ThreadPlayerState.load(player);long now=System.currentTimeMillis();boolean first=state.lastLoginReal==0;long away=state.lastLogoutReal==0?0:now-state.lastLogoutReal;
        state.lastLoginReal=now;if(!first&&away<RETURN_MS){state.save(player);return;}evaluate(player);state=ThreadPlayerState.load(player);
        var cards=new ArrayList<ThreadNetwork.Card>();
        if(first){var world=ThreadDefinitions.INSTANCE.get("world_remembers");if(world!=null)cards.add(card(world,state,"changed"));}
        else changed(player).ifPresent(cards::add);
        var choices=ThreadDefinitions.INSTANCE.all().stream().filter(d->!first||!d.id().equals("world_remembers")).toList();
        for(String id:ThreadSelection.possibilities(choices,state,player.getUUID().getLeastSignificantBits()^now)){
            if(cards.stream().anyMatch(c->c.id().equals(id)))continue;var d=ThreadDefinitions.INSTANCE.get(id);if(d!=null){String motif=state.everSeen.contains(id)||state.phases.getOrDefault(id,0)>0?"near":"wild";cards.add(card(d,state,motif));if(motif.equals("wild"))state.rememberWild(id);}
            if(cards.size()>=3)break;
        }
        state.lastOvertureReal=now;state.save(player);ThreadNetwork.sync(player,false,cards,List.of());
    }
    private static Optional<ThreadNetwork.Card> changed(ServerPlayer player){
        try{var api=Class.forName("com.bettercontent.playertraces.api.ReturnSummaryApi");var summary=api.getMethod("summarize",ServerPlayer.class).invoke(null,player);if(!(boolean)summary.getClass().getMethod("hasChanges").invoke(summary))return Optional.empty();
            int paths=(int)summary.getClass().getMethod("getDistinctNewPathSequences").invoke(summary),notes=(int)summary.getClass().getMethod("getChangedNotes").invoke(summary),pools=(int)summary.getClass().getMethod("getBloodPools").invoke(summary),echoes=(int)summary.getClass().getMethod("getDeathEchoes").invoke(summary);
            String prose="Nearby since you left: "+parts(paths,"path",notes,"note",pools,"blood pool",echoes,"death echo")+".";
            return Optional.of(new ThreadNetwork.Card("changed","The World Changed","minecraft:compass",prose,"trace_sight","Trace Sight",false,"changed"));
        }catch(ReflectiveOperationException ignored){return Optional.empty();}
    }
    private static String parts(int a,String an,int b,String bn,int c,String cn,int d,String dn){
        var out=new ArrayList<String>();add(out,a,an);add(out,b,bn);add(out,c,cn);add(out,d,dn);return String.join(", ",out);
    }
    private static void add(List<String> out,int count,String name){if(count>0)out.add(count+" "+name+(count==1?"":"s"));}
    private static ThreadNetwork.Card card(ThreadDefinition d,ThreadPlayerState s,String motif){return new ThreadNetwork.Card(d.id(),d.title(),d.symbol().toString(),d.phases().get(s.phases.getOrDefault(d.id(),0)),d.doorway().type(),d.doorway().target(),s.held.contains(d.id()),motif);}
}
