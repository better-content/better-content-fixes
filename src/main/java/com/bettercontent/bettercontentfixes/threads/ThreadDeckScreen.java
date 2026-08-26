package com.bettercontent.bettercontentfixes.threads;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ThreadDeckScreen extends Screen {
    private final List<ThreadNetwork.Card> cards;
    private final Set<String> readHere = new HashSet<>();
    private final ThreadRevealState reveal = new ThreadRevealState();
    private int selected;
    private long lastFrame;

    ThreadDeckScreen(List<ThreadNetwork.Card> cards) {
        super(Component.literal("Threads"));
        this.cards = new ArrayList<>(cards);
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).unread()) {
                selected = i;
                break;
            }
        }
        selectCurrent();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    private boolean unread(ThreadNetwork.Card card) {
        return card.unread() && !readHere.contains(card.id());
    }

    private void selectCurrent() {
        reveal.select(!cards.isEmpty() && unread(cards.get(selected)));
        lastFrame = System.currentTimeMillis();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        long now = System.currentTimeMillis();
        long delta = Math.min(100L, Math.max(0L, now - lastFrame));
        lastFrame = now;
        if (!cards.isEmpty() && reveal.advance(delta)) finishDevelopment(cards.get(selected));

        renderBackground(graphics);
        graphics.drawCenteredString(font, "THREADS", width / 2, 12, 0xF0E5CE);
        graphics.drawCenteredString(font, cards.size() + " of 18 remembered", width / 2, 25, 0x928B80);
        if (cards.isEmpty()) {
            graphics.drawCenteredString(font, "Nothing has answered yet.", width / 2, height / 2, 0xAAA397);
            super.render(graphics, mouseX, mouseY, partial);
            return;
        }
        int cardH = Math.min(300, height - 108);
        int cardW = cardH * 2 / 3;
        int cardX = width / 2 - cardW / 2;
        int cardY = 45;
        renderIndex(graphics, cardX, cardY);
        var card = cards.get(selected);
        renderCard(graphics, card, cardX, cardY, cardW, cardH);
        renderDetails(graphics, card, cardX + cardW + 18, cardY, Math.max(120, width - (cardX + cardW + 30)));
        super.render(graphics, mouseX, mouseY, partial);
    }

    private void renderIndex(GuiGraphics graphics, int cardX, int cardY) {
        int start = Math.max(10, cardX - 92);
        for (int i = 0; i < cards.size(); i++) {
            int x = start + (i % 2) * 35;
            int y = cardY + (i / 2) * 37;
            var card = cards.get(i);
            if (unread(card)) {
                ThreadClient.renderSealedPlate(graphics, x, y, 18, 27, ThreadAspect.parse(card.aspect()).color(), card.id().hashCode(), i == selected);
            } else {
                graphics.fill(x - 2, y - 2, x + 20, y + 29, i == selected ? 0xCC9E8562 : 0x66484B49);
                ThreadClient.renderArt(graphics, card.art(), x, y, 18, 27);
            }
        }
    }

    private void renderCard(GuiGraphics graphics, ThreadNetwork.Card card, int x, int y, int width, int height) {
        graphics.fill(x - 4, y - 4, x + width + 4, y + height + 4, 0xFF252421);
        graphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0xFF8E7758);
        if (reveal.phase() == ThreadRevealState.Phase.COMPLETE) {
            ThreadClient.renderArt(graphics, card.art(), x, y, width, height);
            return;
        }
        int aspect = ThreadAspect.parse(card.aspect()).color();
        ThreadClient.renderSealedPlate(graphics, x, y, width, height, aspect, card.id().hashCode(), true);
        if (reveal.phase() == ThreadRevealState.Phase.SEALED) {
            ThreadClient.renderArt(graphics, ThreadClient.layer(card.art(), "archive"), x, y, width, height, 0.10f);
            return;
        }
        long elapsed = reveal.elapsedMs();
        float archive = 0.10f + 0.90f * stage(elapsed, 0L);
        float pigment = stage(elapsed, 600L);
        float trace = stage(elapsed, 1_200L);
        ThreadClient.renderArt(graphics, ThreadClient.layer(card.art(), "archive"), x, y, width, height, archive);
        if (pigment > 0.0f) ThreadClient.renderArt(graphics, ThreadClient.layer(card.art(), "pigment"), x, y, width, height, pigment);
        if (trace > 0.0f) ThreadClient.renderArt(graphics, ThreadClient.layer(card.art(), "aspect"), x, y, width, height, trace);
    }

    private static float stage(long elapsed, long start) {
        return Math.max(0.0f, Math.min(1.0f, (elapsed - start) / 600.0f));
    }

    private void renderDetails(GuiGraphics graphics, ThreadNetwork.Card card, int x, int y, int available) {
        int panelW = Math.min(230, available);
        if (panelW < 100) return;
        if (reveal.phase() == ThreadRevealState.Phase.SEALED) {
            graphics.drawString(font, "Let the plate develop", x, y + 154, 0xA99573, false);
            graphics.drawString(font, "Click or Space to remember", x, y + 168, 0x7F796F, false);
            return;
        }
        if (reveal.phase() != ThreadRevealState.Phase.COMPLETE) return;
        graphics.drawString(font, card.title(), x, y + 4, 0xF0E5CE, false);
        var lines = font.split(Component.literal(card.prose()), panelW);
        for (int i = 0; i < Math.min(lines.size(), 10); i++) graphics.drawString(font, lines.get(i), x, y + 24 + i * 11, 0xC8C0B0, false);
        graphics.drawString(font, "Look closer  ›", x, y + 154, 0xAEBFD0, false);
        graphics.drawString(font, "Issue signed facsimile", x, y + 176, 0xC9AE7A, false);
        graphics.drawString(font, "Display copy only — grants nothing", x, y + 190, 0x77736B, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (cards.isEmpty()) return super.mouseClicked(mouseX, mouseY, button);
        int cardH = Math.min(300, height - 108);
        int cardW = cardH * 2 / 3;
        int cardX = width / 2 - cardW / 2;
        int cardY = 45;
        int start = Math.max(10, cardX - 92);
        for (int i = 0; i < cards.size(); i++) {
            int x = start + (i % 2) * 35;
            int y = cardY + (i / 2) * 37;
            if (mouseX >= x - 2 && mouseX < x + 23 && mouseY >= y - 2 && mouseY < y + 33) {
                selected = i;
                selectCurrent();
                return true;
            }
        }
        var card = cards.get(selected);
        if (unread(card)) {
            activateReveal(card);
            return true;
        }
        int detailsX = cardX + cardW + 18;
        if (mouseX >= detailsX && mouseY >= cardY + 145 && mouseY < cardY + 170) {
            ThreadDoorways.open(card);
            return true;
        }
        if (mouseX >= detailsX && mouseY >= cardY + 170 && mouseY < cardY + 198) {
            ThreadNetwork.request("issue", card.id());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void activateReveal(ThreadNetwork.Card card) {
        if (reveal.activate() == ThreadRevealState.Activation.COMPLETED) finishDevelopment(card);
    }

    private void finishDevelopment(ThreadNetwork.Card card) {
        reveal.complete();
        if (readHere.add(card.id())) ThreadNetwork.request("read", card.id());
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (!cards.isEmpty() && key == GLFW.GLFW_KEY_SPACE && unread(cards.get(selected))) {
            activateReveal(cards.get(selected));
            return true;
        }
        if (!cards.isEmpty() && (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT)) {
            selected = Math.floorMod(selected + (key == GLFW.GLFW_KEY_RIGHT ? 1 : -1), cards.size());
            selectCurrent();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (cards.isEmpty()) return false;
        selected = Math.floorMod(selected + (delta < 0 ? 1 : -1), cards.size());
        selectCurrent();
        return true;
    }
}
