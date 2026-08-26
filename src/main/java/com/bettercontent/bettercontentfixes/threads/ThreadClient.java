package com.bettercontent.bettercontentfixes.threads;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import java.util.*;

@Mod.EventBusSubscriber(modid=BetterContentFixes.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class ThreadClient {
    public static final KeyMapping OPEN=new KeyMapping("key.better_content_fixes.threads",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_J,"key.categories.better_content_fixes");
    private static List<ThreadNetwork.Card> cards=List.of();private static final Deque<ThreadNetwork.Notice> notices=new ArrayDeque<>();private static long noticeStarted;
    private ThreadClient(){}
    public static void receive(ThreadNetwork.Sync sync){cards=sync.cards();for(var notice:sync.notices())if(notices.stream().noneMatch(n->n.id().equals(notice.id())))notices.addLast(notice);if(sync.open())Minecraft.getInstance().setScreen(new ThreadDeckScreen(cards));}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){if(event.phase!=TickEvent.Phase.END)return;while(OPEN.consumeClick()){} }
    @SubscribeEvent(priority=EventPriority.LOWEST)public static void key(InputEvent.Key event){if(event.getAction()==GLFW.GLFW_PRESS&&event.getKey()==GLFW.GLFW_KEY_J&&(event.getModifiers()&GLFW.GLFW_MOD_CONTROL)!=0)ThreadNetwork.request("open","");}
    @SubscribeEvent public static void screen(ScreenEvent.Init.Post event){if(!(event.getScreen() instanceof PauseScreen))return;int x=event.getScreen().width/2+104,y=event.getScreen().height/4+120;event.addListener(Button.builder(net.minecraft.network.chat.Component.literal("Threads"),b->ThreadNetwork.request("open","")).bounds(x,y,72,20).build());}
    @SubscribeEvent public static void render(RenderGuiOverlayEvent.Post event){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null)return;long now=System.currentTimeMillis();
        if(!notices.isEmpty()){
            if(noticeStarted==0){noticeStarted=now;mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN,0.72f,0.38f));}
            long elapsed=now-noticeStarted;if(elapsed>=2400){notices.removeFirst();noticeStarted=0;}else renderNotice(event.getGuiGraphics(),notices.peekFirst(),elapsed,event.getWindow().getGuiScaledWidth(),event.getWindow().getGuiScaledHeight());
        }
        if(notices.isEmpty())renderUnread(event.getGuiGraphics(),event.getWindow().getGuiScaledWidth(),event.getWindow().getGuiScaledHeight());
    }
    private static void renderNotice(GuiGraphics g,ThreadNetwork.Notice notice,long elapsed,int screenWidth,int screenHeight){float alpha=Math.min(1f,Math.min(elapsed/180f,(2400-elapsed)/260f));int width=Math.min(232,screenWidth-28),x=screenWidth-width-12,y=Math.max(34,screenHeight/2-26),a=(int)(alpha*118)<<24;g.fill(x,y,x+width,y+48,a|0x101417);g.fill(x+width-2,y,x+width,y+48,((int)(alpha*220)<<24)|0xA78D68);renderArt(g,notice.art(),x+7,y+7,22,33);g.drawString(Minecraft.getInstance().font,notice.title(),x+36,y+9,((int)(alpha*255)<<24)|0xEADFC7,false);var line=Minecraft.getInstance().font.plainSubstrByWidth(notice.reveal(),width-44);g.drawString(Minecraft.getInstance().font,line,x+36,y+27,((int)(alpha*210)<<24)|0xBEB6A6,false);}
    private static void renderUnread(GuiGraphics g,int screenWidth,int screenHeight){long count=cards.stream().filter(ThreadNetwork.Card::unread).count();if(count==0)return;var card=cards.stream().filter(ThreadNetwork.Card::unread).findFirst().orElseThrow();int x=screenWidth-31,y=Math.max(36,screenHeight/2-14);g.fill(x-3,y-3,x+23,y+31,0x77101417);renderArt(g,card.art(),x,y,16,24);g.drawString(Minecraft.getInstance().font,Long.toString(count),x+13,y+19,0xFFF0E5CE,true);}
    static void renderArt(GuiGraphics g,String art,int x,int y,int width,int height){var id=ResourceLocation.tryParse(art);if(id!=null)g.blit(id,x,y,0,0,width,height,256,384);}
    static void renderSymbol(GuiGraphics g,String symbol,int x,int y){var id=ResourceLocation.tryParse(symbol);var item=id==null?null:net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(id);if(item!=null)g.renderItem(item.getDefaultInstance(),x,y);}

    @Mod.EventBusSubscriber(modid=BetterContentFixes.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event){event.register(OPEN);}
        @SubscribeEvent public static void setup(FMLClientSetupEvent event){event.enqueueWork(()->ItemProperties.register(ThreadRegistry.FACSIMILE.get(),new ResourceLocation(BetterContentFixes.MOD_ID,"thread_index"),(stack,level,entity,seed)->ThreadArt.itemIndex(ThreadFacsimileItem.threadId(stack))));}
    }
}
