package com.bettercontent.bettercontentfixes.threads;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.*;
import java.util.function.Supplier;

public final class ThreadNetwork {
    private static final String VERSION="1";
    private static final SimpleChannel CHANNEL= NetworkRegistry.newSimpleChannel(new ResourceLocation(BetterContentFixes.MOD_ID,"threads"),()->VERSION,VERSION::equals,VERSION::equals);
    private static int id;
    private ThreadNetwork() {}
    public static void register(){
        CHANNEL.messageBuilder(Sync.class,id++, NetworkDirection.PLAY_TO_CLIENT).encoder(Sync::encode).decoder(Sync::decode).consumerMainThread(Sync::handle).add();
        CHANNEL.messageBuilder(Action.class,id++,NetworkDirection.PLAY_TO_SERVER).encoder(Action::encode).decoder(Action::decode).consumerMainThread(Action::handle).add();
    }
    public static void sync(ServerPlayer player, boolean open, List<Card> overture, List<Card> edges){
        var state=ThreadPlayerState.load(player);
        var cards=ThreadDefinitions.INSTANCE.all().stream().map(d->new Card(d.id(),d.title(),d.symbol().toString(),d.phases().get(state.phases.getOrDefault(d.id(),0)),d.doorway().type(),d.doorway().target(),state.held.contains(d.id()),"thread")).toList();
        CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new Sync(open,cards,overture,edges));
    }
    public static void request(String action,String thread){ CHANNEL.sendToServer(new Action(action,thread)); }
    public record Card(String id,String title,String symbol,String prose,String doorwayType,String doorwayTarget,boolean held,String motif){
        void encode(FriendlyByteBuf b){ b.writeUtf(id,48);b.writeUtf(title,64);b.writeUtf(symbol,128);b.writeUtf(prose,ThreadDefinition.MAX_TEXT);b.writeUtf(doorwayType,24);b.writeUtf(doorwayTarget,128);b.writeBoolean(held);b.writeUtf(motif,16); }
        static Card decode(FriendlyByteBuf b){return new Card(b.readUtf(48),b.readUtf(64),b.readUtf(128),b.readUtf(ThreadDefinition.MAX_TEXT),b.readUtf(24),b.readUtf(128),b.readBoolean(),b.readUtf(16));}
    }
    public record Sync(boolean open,List<Card> cards,List<Card> overture,List<Card> edges){
        void encode(FriendlyByteBuf b){b.writeBoolean(open);writeCards(b,cards,18);writeCards(b,overture,3);writeCards(b,edges,16);}
        static Sync decode(FriendlyByteBuf b){return new Sync(b.readBoolean(),readCards(b,18),readCards(b,3),readCards(b,16));}
        static void handle(Sync m,Supplier<NetworkEvent.Context> c){c.get().enqueueWork(()->ThreadClient.receive(m));c.get().setPacketHandled(true);}
    }
    public record Action(String action,String thread){
        void encode(FriendlyByteBuf b){b.writeUtf(action,16);b.writeUtf(thread,48);} static Action decode(FriendlyByteBuf b){return new Action(b.readUtf(16),b.readUtf(48));}
        static void handle(Action m,Supplier<NetworkEvent.Context> c){var player=c.get().getSender();c.get().enqueueWork(()->{if(player==null)return;if(m.action.equals("open")){sync(player,true,List.of(),List.of());return;}if(!ThreadDefinitions.INSTANCE.contains(m.thread))return;var s=ThreadPlayerState.load(player);if(m.action.equals("hold")){if(s.held.contains(m.thread))s.unhold(m.thread);else s.hold(m.thread);s.save(player);sync(player,true,List.of(),List.of());}});c.get().setPacketHandled(true);}
    }
    private static void writeCards(FriendlyByteBuf b,List<Card> cards,int max){if(cards.size()>max)throw new IllegalArgumentException("too many thread cards");b.writeVarInt(cards.size());cards.forEach(c->c.encode(b));}
    private static List<Card> readCards(FriendlyByteBuf b,int max){int n=b.readVarInt();if(n<0||n>max)throw new IllegalArgumentException("invalid thread packet");var out=new ArrayList<Card>(n);for(int i=0;i<n;i++)out.add(Card.decode(b));return List.copyOf(out);}
}
