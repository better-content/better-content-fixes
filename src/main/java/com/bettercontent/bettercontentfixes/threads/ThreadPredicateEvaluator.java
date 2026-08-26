package com.bettercontent.bettercontentfixes.threads;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

final class ThreadPredicateEvaluator {
    record Result(boolean encountered,int phase) {}
    private ThreadPredicateEvaluator() {}
    static Result result(ServerPlayer player, ThreadDefinition definition, ThreadPlayerState state){
        boolean lived=definition.lived().stream().anyMatch(p->matches(player,p,state));
        boolean contact=lived||definition.contact().stream().anyMatch(p->matches(player,p,state));
        boolean discovered=contact||definition.discover().stream().anyMatch(p->matches(player,p,state));
        return new Result(discovered,lived?2:contact?1:0);
    }
    private static boolean matches(ServerPlayer player,ThreadDefinition.Predicate predicate,ThreadPlayerState state){
        return switch(predicate.type()){
            case "item" -> hasItem(player,predicate.value(),state);
            case "dimension" -> player.serverLevel().dimension().location().toString().equals(predicate.value())||state.everSeen.contains("dimension:"+predicate.value());
            case "mod_loaded" -> ModList.get().isLoaded(predicate.value());
            case "quest", "quest_started" -> quest(player,predicate.value(),predicate.type().equals("quest"));
            case "campaign" -> campaign(player,predicate.value());
            default -> false;
        };
    }
    private static boolean hasItem(ServerPlayer player,String value,ThreadPlayerState state){
        String marker="item:"+value;if(state.everSeen.contains(marker))return true;var item=ForgeRegistries.ITEMS.getValue(new ResourceLocation(value));
        boolean found=item!=null&&player.getInventory().items.stream().anyMatch(s->s.is(item));if(found)state.everSeen.add(marker);return found;
    }
    private static boolean quest(ServerPlayer player,String value,boolean complete){
        if(!ModList.get().isLoaded("ftbquests")||ServerQuestFile.INSTANCE==null)return false;
        try{long id=value.matches("-?[0-9]+")?Long.parseLong(value):Long.parseUnsignedLong(value.replace("-", ""),16)*(value.startsWith("-")?-1:1);var quest=ServerQuestFile.INSTANCE.getQuest(id);if(quest==null)return false;var data=ServerQuestFile.INSTANCE.getOrCreateTeamData(player);return complete?data.isCompleted(quest):data.isStarted(quest);}catch(RuntimeException ignored){return false;}
    }
    private static boolean campaign(ServerPlayer player,String expected){
        if(!ModList.get().isLoaded("pillager_campaigns"))return false;
        try{var api=Class.forName("com.bettercontent.pillagercampaigns.api.CampaignStatusApi");var state=api.getMethod("state",ServerPlayer.class).invoke(null,player);return state.toString().equalsIgnoreCase(expected);}catch(ReflectiveOperationException ignored){return false;}
    }
}
