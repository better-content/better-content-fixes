package com.bettercontent.bettercontentfixes.threads;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.*;

public final class ThreadDeckScreen extends Screen {
    private final List<ThreadNetwork.Card> cards;private final Set<String> readHere=new HashSet<>();private int selected;private long revealStarted;
    ThreadDeckScreen(List<ThreadNetwork.Card> cards){super(Component.literal("Threads"));this.cards=new ArrayList<>(cards);for(int i=0;i<cards.size();i++)if(cards.get(i).unread()){selected=i;break;}revealStarted=System.currentTimeMillis();}
    @Override public boolean isPauseScreen(){return true;}
    private boolean unread(ThreadNetwork.Card card){return card.unread()&&!readHere.contains(card.id());}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        renderBackground(g);g.drawCenteredString(font,"THREADS",width/2,12,0xF0E5CE);g.drawCenteredString(font,cards.size()+" of 18 remembered",width/2,25,0x928B80);
        if(cards.isEmpty()){g.drawCenteredString(font,"Nothing has answered yet.",width/2,height/2,0xAAA397);super.render(g,mouseX,mouseY,partial);return;}
        int cardH=Math.min(300,height-108),cardW=cardH*2/3,cardX=width/2-cardW/2,cardY=45;renderIndex(g,cardX,cardY);var card=cards.get(selected);renderCard(g,card,cardX,cardY,cardW,cardH);renderDetails(g,card,cardX+cardW+18,cardY,Math.max(120,width-(cardX+cardW+30)));super.render(g,mouseX,mouseY,partial);
    }
    private void renderIndex(GuiGraphics g,int cardX,int cardY){int start=Math.max(10,cardX-92);for(int i=0;i<cards.size();i++){int col=i%2,row=i/2,x=start+col*35,y=cardY+row*37;var c=cards.get(i);g.fill(x-2,y-2,x+23,y+33,i==selected?0xCC9E8562:0x66484B49);ThreadClient.renderArt(g,c.art(),x,y,18,27);if(unread(c))g.fill(x+15,y-2,x+21,y+4,0xFFD5B576);}}
    private void renderCard(GuiGraphics g,ThreadNetwork.Card card,int x,int y,int w,int h){g.fill(x-4,y-4,x+w+4,y+h+4,0xFF252421);g.fill(x-2,y-2,x+w+2,y+h+2,0xFF8E7758);if(unread(card)){double p=Math.min(1.0,(System.currentTimeMillis()-revealStarted)/900.0);int visible=(int)(h*p);g.enableScissor(x,y,x+w,y+visible);ThreadClient.renderArt(g,card.art(),x,y,w,h);g.disableScissor();if(p<1)g.fill(x,y+Math.max(0,visible-2),x+w,y+visible+2,0xBBD8BE8C);}else ThreadClient.renderArt(g,card.art(),x,y,w,h);}
    private void renderDetails(GuiGraphics g,ThreadNetwork.Card card,int x,int y,int available){int panelW=Math.min(230,available);if(panelW<100)return;g.drawString(font,card.title(),x,y+4,0xF0E5CE,false);var lines=font.split(Component.literal(card.prose()),panelW);for(int i=0;i<Math.min(lines.size(),10);i++)g.drawString(font,lines.get(i),x,y+24+i*11,0xC8C0B0,false);if(unread(card)){g.drawString(font,"Let the plate develop",x,y+154,0xA99573,false);g.drawString(font,"Click or Space to remember",x,y+168,0x7F796F,false);}else{g.drawString(font,"Look closer  ›",x,y+154,0xAEBFD0,false);g.drawString(font,"Issue signed facsimile",x,y+176,0xC9AE7A,false);g.drawString(font,"Display copy only — grants nothing",x,y+190,0x77736B,false);}}
    @Override public boolean mouseClicked(double mx,double my,int button){if(cards.isEmpty())return super.mouseClicked(mx,my,button);int cardH=Math.min(300,height-108),cardW=cardH*2/3,cardX=width/2-cardW/2,cardY=45,start=Math.max(10,cardX-92);for(int i=0;i<cards.size();i++){int x=start+(i%2)*35,y=cardY+(i/2)*37;if(mx>=x-2&&mx<x+23&&my>=y-2&&my<y+33){selected=i;revealStarted=System.currentTimeMillis();return true;}}var card=cards.get(selected);if(unread(card)){acknowledge(card);return true;}int detailsX=cardX+cardW+18;if(mx>=detailsX&&my>=cardY+145&&my<cardY+170){ThreadDoorways.open(card);return true;}if(mx>=detailsX&&my>=cardY+170&&my<cardY+198){ThreadNetwork.request("issue",card.id());return true;}return super.mouseClicked(mx,my,button);}
    private void acknowledge(ThreadNetwork.Card card){if(System.currentTimeMillis()-revealStarted<900){revealStarted=System.currentTimeMillis()-900;return;}readHere.add(card.id());ThreadNetwork.request("read",card.id());for(int i=1;i<=cards.size();i++){int candidate=(selected+i)%cards.size();if(unread(cards.get(candidate))){selected=candidate;revealStarted=System.currentTimeMillis();return;}}}
    @Override public boolean keyPressed(int key,int scan,int mods){if(!cards.isEmpty()&&(key==GLFW.GLFW_KEY_SPACE||key==GLFW.GLFW_KEY_ENTER)){var card=cards.get(selected);if(unread(card)){acknowledge(card);return true;}}if(!cards.isEmpty()&&(key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_RIGHT)){selected=Math.floorMod(selected+(key==GLFW.GLFW_KEY_RIGHT?1:-1),cards.size());revealStarted=System.currentTimeMillis();return true;}return super.keyPressed(key,scan,mods);}
    @Override public boolean mouseScrolled(double mx,double my,double delta){if(cards.isEmpty())return false;selected=Math.floorMod(selected+(delta<0?1:-1),cards.size());revealStarted=System.currentTimeMillis();return true;}
}
