package com.bettercontent.bettercontentfixes.threads;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import org.lwjgl.glfw.GLFW;
import java.util.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import com.bettercontent.bettercontentfixes.BetterContentFixes;

@Mod.EventBusSubscriber(modid=BetterContentFixes.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class ThreadClient {
    public static final KeyMapping OPEN=new KeyMapping("key.better_content_fixes.threads", InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_J,"key.categories.better_content_fixes");
    private static List<ThreadNetwork.Card> cards=List.of();
    private static final Deque<ThreadNetwork.Card> overture=new ArrayDeque<>(), edges=new ArrayDeque<>();
    private static long cardStarted;
    private ThreadClient() {}
    public static void registerKey(RegisterKeyMappingsEvent event){event.register(OPEN);}
    public static void receive(ThreadNetwork.Sync sync){
        cards=sync.cards(); overture.addAll(sync.overture()); edges.addAll(sync.edges());
        if(cardStarted==0&&(!overture.isEmpty()||!edges.isEmpty()))cardStarted=System.currentTimeMillis();
        if(sync.open())Minecraft.getInstance().setScreen(new ThreadDeckScreen(cards));
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;
        while(OPEN.consumeClick()) { /* InputEvent.Key preserves the chord's modifier state. */ }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void key(InputEvent.Key event){
        if(event.getAction()==GLFW.GLFW_PRESS&&event.getKey()==GLFW.GLFW_KEY_J&&(event.getModifiers()&GLFW.GLFW_MOD_CONTROL)!=0)ThreadNetwork.request("open","");
    }
    @SubscribeEvent public static void screen(ScreenEvent.Init.Post event){
        if(!(event.getScreen() instanceof PauseScreen))return;
        int x=event.getScreen().width/2+104,y=event.getScreen().height/4+120;
        event.addListener(Button.builder(net.minecraft.network.chat.Component.literal("Threads"),b->ThreadNetwork.request("open","")).bounds(x,y,72,20).build());
    }
    @SubscribeEvent public static void render(RenderGuiOverlayEvent.Post event){
        var mc=Minecraft.getInstance(); if(mc.player==null||mc.screen!=null)return;
        var queue=!edges.isEmpty()?edges:overture; if(queue.isEmpty()){cardStarted=0;return;}
        long elapsed=System.currentTimeMillis()-cardStarted;
        if(elapsed>=2600){queue.removeFirst();cardStarted=queue.isEmpty()?0:System.currentTimeMillis();return;}
        var card=queue.peekFirst(); float alpha=Math.min(1f,Math.min(elapsed/300f,(2600-elapsed)/300f));
        int width=Math.min(310,event.getWindow().getGuiScaledWidth()/2-18),x=18,y=event.getWindow().getGuiScaledHeight()/2-42;
        int a=(int)(alpha*190)<<24;
        event.getGuiGraphics().fill(x,y,x+width,y+82,a|0x15191B);
        event.getGuiGraphics().fill(x,y,x+3,y+82,((int)(alpha*255)<<24)|motifColor(card.motif()));
        renderSymbol(event.getGuiGraphics(),card.symbol(),x+12,y+12);
        event.getGuiGraphics().drawString(mc.font,card.title(),x+38,y+12,((int)(alpha*255)<<24)|0xE9E1CF,false);
        var lines=mc.font.split(net.minecraft.network.chat.Component.literal(card.prose()),width-50);
        for(int i=0;i<Math.min(3,lines.size());i++)event.getGuiGraphics().drawString(mc.font,lines.get(i),x+38,y+30+i*11,((int)(alpha*220)<<24)|0xC8C1B2,false);
        if(elapsed<80) playMotif(card.motif());
    }
    static void renderSymbol(net.minecraft.client.gui.GuiGraphics g,String symbol,int x,int y){
        var id=net.minecraft.resources.ResourceLocation.tryParse(symbol);var item=id==null?null:net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(id);
        if(item!=null)g.renderItem(item.getDefaultInstance(),x,y);
    }
    private static int motifColor(String motif){return switch(motif){case"changed"->0x9B725A;case"near"->0x72866A;case"wild"->0x72728F;default->0x8B806E;};}
    private static long lastSound;
    private static void playMotif(String motif){long now=System.currentTimeMillis();if(now-lastSound<500)return;lastSound=now;var sound=switch(motif){case"changed"->SoundEvents.BOOK_PAGE_TURN;case"near"->SoundEvents.ARMOR_EQUIP_LEATHER;default->SoundEvents.WOOD_PLACE;};Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound,0.8f,0.35f));}

    @Mod.EventBusSubscriber(modid=BetterContentFixes.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents { @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event){registerKey(event);} }
}
