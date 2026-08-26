package com.bettercontent.bettercontentfixes.threads;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class ThreadDeckScreen extends Screen {
    private final List<ThreadNetwork.Card> cards; private int scroll;
    ThreadDeckScreen(List<ThreadNetwork.Card> cards){super(Component.literal("Threads"));this.cards=cards;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        renderBackground(g);g.drawCenteredString(font,"Threads",width/2,14,0xE9E1CF);g.drawCenteredString(font,"Possibilities to hold close — never obligations",width/2,27,0x8F8A80);
        g.enableScissor(8,42,width-8,height-12);int columnWidth=(width-30)/2;
        for(int i=0;i<cards.size();i++){int col=i%2,row=i/2,x=10+col*(columnWidth+10),y=44+row*106-scroll;renderCard(g,cards.get(i),x,y,columnWidth,mouseX,mouseY);}
        g.disableScissor();super.render(g,mouseX,mouseY,partial);
    }
    private void renderCard(GuiGraphics g,ThreadNetwork.Card c,int x,int y,int w,int mx,int my){
        g.fill(x,y,x+w,y+96,0xDD171A1C);g.fill(x,y,x+2,y+96,c.held()?0xFFAA9671:0xFF595B57);ThreadClient.renderSymbol(g,c.symbol(),x+9,y+9);
        g.drawString(font,c.title(),x+32,y+10,0xE9E1CF,false);var lines=font.split(Component.literal(c.prose()),w-42);for(int i=0;i<Math.min(4,lines.size());i++)g.drawString(font,lines.get(i),x+32,y+27+i*10,0xBDB7AA,false);
        g.drawString(font,c.held()?"Held close":"Hold close",x+9,y+78,c.held()?0xD2BA87:0xAAA59B,false);g.drawString(font,"Look closer  ›",x+w-82,y+78,0x9FAFC0,false);
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        int cw=(width-30)/2;for(int i=0;i<cards.size();i++){int x=10+(i%2)*(cw+10),y=44+(i/2)*106-scroll;if(my>=y+69&&my<y+94&&mx>=x&&mx<x+cw){var c=cards.get(i);if(mx<x+cw/2){
            long held=cards.stream().filter(ThreadNetwork.Card::held).count();if(!c.held()&&held>=2)Minecraft.getInstance().setScreen(new ConfirmScreen(ok->{if(ok)ThreadNetwork.request("hold",c.id());else Minecraft.getInstance().setScreen(this);},Component.literal("Replace the older held thread?"),Component.literal("Holding close changes presentation only.")));else ThreadNetwork.request("hold",c.id());
        }else ThreadDoorways.open(c);return true;}}
        return super.mouseClicked(mx,my,button);
    }
    @Override public boolean mouseScrolled(double mx,double my,double delta){int max=Math.max(0,((cards.size()+1)/2)*106-(height-54));scroll=Math.max(0,Math.min(max,scroll-(int)(delta*28)));return true;}
}
