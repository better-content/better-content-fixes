package com.bettercontent.bettercontentfixes.threads;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Mod.EventBusSubscriber(modid = BetterContentFixes.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ThreadClient {
    private static final int ARCHIVE_GOLD = 0xC6A15B;
    public static final KeyMapping OPEN = new KeyMapping("key.better_content_fixes.threads", InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_J, "key.categories.better_content_fixes");
    private static final ThreadNoticeQueue<ThreadNetwork.Notice> NOTICES = new ThreadNoticeQueue<>(ThreadNetwork.Notice::id);
    private static List<ThreadNetwork.Card> cards = List.of();
    private static long lastLiveFrame;

    private ThreadClient() {}

    public static void receive(ThreadNetwork.Sync sync) {
        cards = sync.cards();
        NOTICES.addAll(sync.notices());
        if (sync.open()) Minecraft.getInstance().setScreen(new ThreadDeckScreen(cards));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        while (OPEN.consumeClick()) {}
        if (Minecraft.getInstance().screen != null) lastLiveFrame = 0L;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void key(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS && event.getKey() == GLFW.GLFW_KEY_J
            && (event.getModifiers() & GLFW.GLFW_MOD_CONTROL) != 0) ThreadNetwork.request("open", "");
    }

    @SubscribeEvent
    public static void screen(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof PauseScreen)) return;
        int x = event.getScreen().width / 2 + 104;
        int y = event.getScreen().height / 4 + 120;
        event.addListener(Button.builder(Component.literal("Threads"), button -> ThreadNetwork.request("open", ""))
            .bounds(x, y, 72, 20).build());
    }

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            lastLiveFrame = 0L;
            return;
        }
        long now = System.currentTimeMillis();
        long delta = lastLiveFrame == 0L ? 0L : Math.min(100L, Math.max(0L, now - lastLiveFrame));
        lastLiveFrame = now;
        var frame = NOTICES.advance(delta, false);
        if (frame != null) {
            if (frame.started()) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 0.72f, 0.38f));
            renderNotice(event.getGuiGraphics(), frame.notice(), frame.elapsedMs(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
        }
        renderUnread(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
    }

    private static void renderNotice(GuiGraphics graphics, ThreadNetwork.Notice notice, long elapsed, int screenWidth, int screenHeight) {
        float alpha = noticeAlpha(elapsed);
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 3;
        renderParticles(graphics, notice, elapsed, alpha, centerX, centerY);
        renderSymbol32(graphics, notice.symbol(), centerX, centerY, alpha);
        Component message = Component.translatable("message.better_content_fixes.thread_revealed", notice.title());
        int textWidth = Minecraft.getInstance().font.width(message);
        float scale = Math.min(1.0f, (screenWidth - 24.0f) / Math.max(1, textWidth));
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY + 42, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(Minecraft.getInstance().font, message, -textWidth / 2, 0,
            ((int) (alpha * 255.0f) << 24) | 0xF0E5CE, true);
        graphics.pose().popPose();
    }

    static float noticeAlpha(long elapsed) {
        if (elapsed < 400L) return elapsed / 400.0f;
        if (elapsed < 2_600L) return 1.0f;
        return Math.max(0.0f, (ThreadNoticeQueue.DURATION_MS - elapsed) / 600.0f);
    }

    private static void renderParticles(GuiGraphics graphics, ThreadNetwork.Notice notice, long elapsed, float noticeAlpha, int centerX, int centerY) {
        int aspect = ThreadAspect.parse(notice.aspect()).color();
        double progress = elapsed / (double) ThreadNoticeQueue.DURATION_MS;
        int seed = notice.id().hashCode();
        for (int i = 0; i < 20; i++) {
            int mixed = mix(seed + i * 0x9E3779B9);
            double angle = ((mixed & 0xFFFF) / 65535.0) * Math.PI * 2.0;
            double baseRadius = 10.0 + ((mixed >>> 16) & 7);
            double drift = progress * (8.0 + ((mixed >>> 20) & 7));
            int x = centerX + (int) Math.round(Math.cos(angle) * (baseRadius + drift));
            int y = centerY + (int) Math.round(Math.sin(angle) * baseRadius - progress * (9.0 + ((mixed >>> 24) & 7)));
            float pulse = (float) (0.58 + 0.42 * Math.sin(Math.PI * Math.min(1.0, progress * 1.25 + (i % 4) * 0.06)));
            int particleAlpha = (int) (noticeAlpha * pulse * (i < 12 ? 150 : 190));
            int rgb = i < 12 ? aspect : ARCHIVE_GOLD;
            int size = i < 12 && (i & 3) == 0 ? 2 : 1;
            graphics.fill(x, y, x + size, y + size, (particleAlpha << 24) | rgb);
        }
    }

    private static int mix(int value) {
        value ^= value >>> 16;
        value *= 0x7FEB352D;
        value ^= value >>> 15;
        value *= 0x846CA68B;
        return value ^ (value >>> 16);
    }

    private static void renderUnread(GuiGraphics graphics, int screenWidth, int screenHeight) {
        long count = cards.stream().filter(ThreadNetwork.Card::unread).count();
        if (count == 0L) return;
        var card = cards.stream().filter(ThreadNetwork.Card::unread).findFirst().orElseThrow();
        int x = screenWidth - 31;
        int y = Math.max(36, screenHeight / 2 - 14);
        renderSealedPlate(graphics, x, y, 18, 27, ThreadAspect.parse(card.aspect()).color(), card.id().hashCode(), false);
        graphics.drawString(Minecraft.getInstance().font, Long.toString(count), x + 13, y + 19, 0xFFF0E5CE, true);
    }

    static void renderSealedPlate(GuiGraphics graphics, int x, int y, int width, int height, int aspectColor, int seed, boolean selected) {
        graphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, selected ? 0xCCB49561 : 0x668F744E);
        graphics.fill(x, y, x + width, y + height, 0xFF111513);
        int traceAlpha = selected ? 0xB0 : 0x78;
        for (int i = 0; i < 5; i++) {
            int mixed = mix(seed + i * 71);
            int tx = x + 2 + Math.floorMod(mixed, Math.max(1, width - 4));
            int ty = y + 2 + i * Math.max(1, (height - 5) / 5);
            graphics.fill(tx, ty, Math.min(x + width - 1, tx + 2), ty + 1, (traceAlpha << 24) | aspectColor);
        }
    }

    static void renderArt(GuiGraphics graphics, String art, int x, int y, int width, int height) {
        var id = ResourceLocation.tryParse(art);
        if (id != null) graphics.blit(id, x, y, 0, 0, width, height, 256, 384);
    }

    static void renderArt(GuiGraphics graphics, String art, int x, int y, int width, int height, float alpha) {
        graphics.flush();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        renderArt(graphics, art, x, y, width, height);
        graphics.flush();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    static String layer(String art, String layer) {
        return art.endsWith(".png") ? art.substring(0, art.length() - 4) + "_" + layer + ".png" : art + "_" + layer;
    }

    static void renderSymbol(GuiGraphics graphics, String symbol, int x, int y) {
        var id = ResourceLocation.tryParse(symbol);
        var item = id == null ? null : net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(id);
        if (item != null) graphics.renderItem(item.getDefaultInstance(), x, y);
    }

    private static void renderSymbol32(GuiGraphics graphics, String symbol, int centerX, int centerY, float alpha) {
        graphics.flush();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        graphics.pose().pushPose();
        graphics.pose().translate(centerX - 16, centerY - 16, 0);
        graphics.pose().scale(2.0f, 2.0f, 1.0f);
        renderSymbol(graphics, symbol, 0, 0);
        graphics.pose().popPose();
        graphics.flush();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Mod.EventBusSubscriber(modid = BetterContentFixes.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) {
            event.register(OPEN);
        }

        @SubscribeEvent
        public static void setup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> ItemProperties.register(ThreadRegistry.FACSIMILE.get(),
                new ResourceLocation(BetterContentFixes.MOD_ID, "thread_index"),
                (stack, level, entity, seed) -> ThreadArt.itemIndex(ThreadFacsimileItem.threadId(stack))));
        }
    }
}
