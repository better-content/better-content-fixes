package com.bettercontent.bettercontentfixes.threads;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.*;
import java.util.function.Supplier;

public final class ThreadNetwork {
    private static final String VERSION="3";
    private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation(BetterContentFixes.MOD_ID,"threads"),()->VERSION,VERSION::equals,VERSION::equals);
    private static final Map<UUID,Long> lastIssue=new HashMap<>();private static int id;
    private ThreadNetwork(){}
    public static void register(){CHANNEL.messageBuilder(Sync.class,id++,NetworkDirection.PLAY_TO_CLIENT).encoder(Sync::encode).decoder(Sync::decode).consumerMainThread(Sync::handle).add();CHANNEL.messageBuilder(Action.class,id++,NetworkDirection.PLAY_TO_SERVER).encoder(Action::encode).decoder(Action::decode).consumerMainThread(Action::handle).add();}
    public static Notice notice(ThreadDefinition d){return new Notice(d.id(),d.title(),d.symbol().toString(),d.aspect().id());}
    public static void sync(ServerPlayer player,boolean open,List<Notice> notices){var state=ThreadPlayerState.get(player);var cards=ThreadDefinitions.INSTANCE.all().stream().filter(d->state.collected.contains(d.id())).map(d->card(d,state)).toList();CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new Sync(open,cards,notices));}
    private static Card card(ThreadDefinition d,ThreadPlayerState state){return new Card(d.id(),d.title(),d.symbol().toString(),d.aspect().id(),d.art().toString(),d.phases().get(state.phases.getOrDefault(d.id(),0)),d.doorway().type(),d.doorway().target(),state.unread.contains(d.id()));}
    public static void request(String action,String thread){CHANNEL.sendToServer(new Action(action,thread));}
    public record Card(String id,String title,String symbol,String aspect,String art,String prose,String doorwayType,String doorwayTarget,boolean unread){
        public Card {ThreadPacketValidation.id(id);ThreadPacketValidation.title(title);ThreadPacketValidation.symbol(id,symbol);ThreadAspect.parse(aspect);ThreadPacketValidation.resource(art,"art");ThreadPacketValidation.text(prose,ThreadDefinition.MAX_TEXT,"prose");if(!ThreadDefinition.Doorway.validType(doorwayType))throw new IllegalArgumentException("invalid doorway type");ThreadPacketValidation.text(doorwayTarget,128,"doorway target");}
        void encode(FriendlyByteBuf b){b.writeUtf(id,48);b.writeUtf(title,64);b.writeUtf(symbol,128);b.writeUtf(aspect,16);b.writeUtf(art,128);b.writeUtf(prose,ThreadDefinition.MAX_TEXT);b.writeUtf(doorwayType,24);b.writeUtf(doorwayTarget,128);b.writeBoolean(unread);}
        static Card decode(FriendlyByteBuf b){return new Card(b.readUtf(48),b.readUtf(64),b.readUtf(128),b.readUtf(16),b.readUtf(128),b.readUtf(ThreadDefinition.MAX_TEXT),b.readUtf(24),b.readUtf(128),b.readBoolean());}
    }
    public record Notice(String id,String title,String symbol,String aspect){
        public Notice {ThreadPacketValidation.id(id);ThreadPacketValidation.title(title);ThreadPacketValidation.symbol(id,symbol);ThreadAspect.parse(aspect);}
        void encode(FriendlyByteBuf b){b.writeUtf(id,48);b.writeUtf(title,64);b.writeUtf(symbol,128);b.writeUtf(aspect,16);}
        static Notice decode(FriendlyByteBuf b){return new Notice(b.readUtf(48),b.readUtf(64),b.readUtf(128),b.readUtf(16));}
    }
    public record Sync(boolean open,List<Card> cards,List<Notice> notices){
        public Sync {if(cards.size()>18||notices.size()>18)throw new IllegalArgumentException("too many thread entries");}
        void encode(FriendlyByteBuf b){b.writeBoolean(open);writeCards(b,cards);b.writeVarInt(notices.size());notices.forEach(n->n.encode(b));}
        static Sync decode(FriendlyByteBuf b){boolean open=b.readBoolean();var cards=readCards(b);int n=b.readVarInt();if(n<0||n>18)throw new IllegalArgumentException("invalid thread notice packet");var notices=new ArrayList<Notice>();var ids=new HashSet<String>();for(int i=0;i<n;i++){var notice=Notice.decode(b);if(!ids.add(notice.id()))throw new IllegalArgumentException("duplicate thread notice");notices.add(notice);}return new Sync(open,cards,List.copyOf(notices));}
        static void handle(Sync m,Supplier<NetworkEvent.Context> c){c.get().enqueueWork(()->ThreadClient.receive(m));c.get().setPacketHandled(true);}
    }
    public record Action(String action,String thread){
        void encode(FriendlyByteBuf b){b.writeUtf(action,16);b.writeUtf(thread,48);}static Action decode(FriendlyByteBuf b){return new Action(b.readUtf(16),b.readUtf(48));}
        static void handle(Action m,Supplier<NetworkEvent.Context> c){var player=c.get().getSender();c.get().enqueueWork(()->handle(player,m));c.get().setPacketHandled(true);}
        private static void handle(ServerPlayer player,Action action){if(player==null)return;if(action.action.equals("open")){sync(player,true,List.of());return;}if(!ThreadDefinitions.INSTANCE.contains(action.thread))return;var state=ThreadPlayerState.get(player);if(!state.collected.contains(action.thread))return;if(action.action.equals("read")){if(state.markRead(action.thread)){state.save(player);sync(player,false,List.of());}return;}if(!action.action.equals("issue"))return;long now=System.currentTimeMillis(),previous=lastIssue.getOrDefault(player.getUUID(),0L);if(now-previous<1000)return;lastIssue.put(player.getUUID(),now);var stack=ThreadFacsimileItem.create(action.thread,player);if(!player.getInventory().add(stack)){player.displayClientMessage(Component.literal("Make room for the facsimile first."),true);return;}player.containerMenu.broadcastChanges();}
    }
    private static void writeCards(FriendlyByteBuf b,List<Card> cards){if(cards.size()>18)throw new IllegalArgumentException("too many thread cards");b.writeVarInt(cards.size());cards.forEach(c->c.encode(b));}
    private static List<Card> readCards(FriendlyByteBuf b){int n=b.readVarInt();if(n<0||n>18)throw new IllegalArgumentException("invalid thread packet");var out=new ArrayList<Card>(n);var ids=new HashSet<String>();for(int i=0;i<n;i++){var card=Card.decode(b);if(!ids.add(card.id()))throw new IllegalArgumentException("duplicate thread card");out.add(card);}return List.copyOf(out);}
}
