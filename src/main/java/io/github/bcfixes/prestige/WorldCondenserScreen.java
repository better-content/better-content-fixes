package io.github.bcfixes.prestige;

import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class WorldCondenserScreen extends AbstractContainerScreen<WorldCondenserMenu> {
    private int seenRevision = -1;
    private int uploadIndex;
    private int publishedIndex;
    private EditBox confirmation;

    public WorldCondenserScreen(WorldCondenserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 292;
        imageHeight = 226;
    }

    @Override protected void init() {
        super.init();
        rebuild();
        PrestigeNetwork.sendAction(PrestigeNetwork.Action.REFRESH, actionPos(), "");
    }

    @Override protected void containerTick() {
        super.containerTick();
        if (seenRevision != PrestigeClientState.revision()) rebuild();
    }

    private void rebuild() {
        clearWidgets();
        seenRevision = PrestigeClientState.revision();
        var state = PrestigeClientState.state();
        int x = leftPos + 10;
        int y = topPos + 38;
        if (state == null) return;

        addRenderableWidget(Button.builder(Component.literal("Biome: " + shortText(state.selectedBiome(), 29)), button -> {
            int index = Math.max(0, state.biomes().indexOf(state.selectedBiome()));
            String next = state.biomes().get((index + 1) % state.biomes().size());
            PrestigeNetwork.sendAction(PrestigeNetwork.Action.SET_BIOME, actionPos(), next);
        }).bounds(x, y, 272, 20).build());

        y += 28;
        if (state.operator()) {
            addRenderableWidget(Button.builder(Component.literal("Stage selection"), button ->
                    PrestigeNetwork.sendAction(PrestigeNetwork.Action.STAGE, actionPos(), ""))
                    .bounds(x, y, 86, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Cancel stage"), button ->
                    PrestigeNetwork.sendAction(PrestigeNetwork.Action.CANCEL, actionPos(), ""))
                    .bounds(x + 92, y, 86, 20).build());
            confirmation = new EditBox(font, x + 184, y, 98, 20, Component.literal("World name"));
            confirmation.setHint(Component.literal(state.worldName()));
            addRenderableWidget(confirmation);
            y += 24;
            addRenderableWidget(Button.builder(Component.literal("COMMIT PERMANENT RESET"), button ->
                    PrestigeNetwork.sendAction(PrestigeNetwork.Action.COMMIT, actionPos(), confirmation.getValue()))
                    .bounds(x, y, 272, 20).build());
        } else {
            y += 24;
        }

        y = topPos + 155;
        uploadIndex = state.uploads().isEmpty() ? 0 : Math.min(uploadIndex, state.uploads().size() - 1);
        String upload = state.uploads().isEmpty() ? "No server uploads" : state.uploads().get(uploadIndex);
        addRenderableWidget(Button.builder(Component.literal("Server upload: " + shortText(upload, 22)), button -> {
            if (!state.uploads().isEmpty()) { uploadIndex = (uploadIndex + 1) % state.uploads().size(); rebuild(); }
        }).bounds(x, y, 186, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Publish"), button -> {
            if (!state.uploads().isEmpty()) PrestigeNetwork.sendAction(PrestigeNetwork.Action.PUBLISH, actionPos(), state.uploads().get(uploadIndex));
        }).bounds(x + 192, y, 80, 20).build());

        y += 26;
        publishedIndex = state.published().isEmpty() ? 0 : Math.min(publishedIndex, state.published().size() - 1);
        String published = state.published().isEmpty() ? "Library empty" : state.published().get(publishedIndex).author()
                + "/" + state.published().get(publishedIndex).name();
        addRenderableWidget(Button.builder(Component.literal("Library: " + shortText(published, 25)), button -> {
            if (!state.published().isEmpty()) { publishedIndex = (publishedIndex + 1) % state.published().size(); rebuild(); }
        }).bounds(x, y, 186, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Download"), button -> {
            if (!state.published().isEmpty()) PrestigeNetwork.sendAction(PrestigeNetwork.Action.DOWNLOAD, actionPos(),
                    state.published().get(publishedIndex).id());
        }).bounds(x + 192, y, 80, 20).build());
        if (state.operator() && !state.published().isEmpty()) {
            y += 24;
            addRenderableWidget(Button.builder(Component.literal("Remove selected library entry"), button ->
                    PrestigeNetwork.sendAction(PrestigeNetwork.Action.REMOVE, actionPos(), state.published().get(publishedIndex).id()))
                    .bounds(x, y, 272, 20).build());
        }
    }

    private static String shortText(String value, int max) {
        return value.length() <= max ? value : value.substring(0, Math.max(1, max - 1)) + "…";
    }

    private BlockPos actionPos() {
        var state = PrestigeClientState.state();
        return state == null ? menu.pos() : state.pos();
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xff17131f);
        graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + 30, 0xff3a2345);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 10, 10, 0xffe6c6ff, false);
        var state = PrestigeClientState.state();
        if (state == null) {
            graphics.drawString(font, "Loading lineage state…", 10, 25, 0xffaaaaaa, false);
            return;
        }
        graphics.drawString(font, "Status: " + state.status() + "  Generation: " + state.generation()
                + "  Unspent: " + state.unspent(), 10, 25, 0xffdddddd, false);
        graphics.drawString(font, "Random seed · exact spawn biome · all world/player state will reset", 10, 116, 0xffffb0b0, false);
        graphics.drawString(font, "Commit confirmation: type " + state.worldName(), 10, 128, 0xffffd08a, false);
        graphics.drawString(font, "Published schematics are plans only; Create still requires every material.", 10, 142, 0xffb8d8ff, false);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
