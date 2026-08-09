package io.github.bcfixes.prestige;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public final class WorldCondenserScreen extends AbstractContainerScreen<WorldCondenserMenu> {
    private int seenRevision = -1;
    private int tab;
    private int uploadIndex;
    private int publishedIndex;
    private EditBox confirmation;
    private EditBox seedCandidate;

    public WorldCondenserScreen(WorldCondenserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 410;
        imageHeight = 236;
        tab = menu.initialTab();
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
        if (state == null) return;
        int x = leftPos + 10;
        addRenderableWidget(tabButton("Reset", x, 30, 0));
        addRenderableWidget(tabButton("Perks", x + 92, 30, 1));
        addRenderableWidget(tabButton("Schematics", x + 184, 30, 2));
        if (tab == 0) rebuildReset(state, x);
        else if (tab == 1) rebuildPerks(state, x);
        else rebuildSchematics(state, x);
    }

    private Button tabButton(String label, int x, int y, int value) {
        return Button.builder(Component.literal((tab == value ? "> " : "") + label), button -> {
            tab = value;
            rebuild();
        }).bounds(x, topPos + y, 84, 20).build();
    }

    private void rebuildReset(PrestigeNetwork.StatePacket state, int x) {
        int width = 390;
        int column = 188;
        int gap = 14;
        int y = topPos + 60;
        addRenderableWidget(Button.builder(Component.literal("Biome: " + shortText(state.selectedBiome(), 36)), button -> {
            int index = Math.max(0, state.biomes().indexOf(state.selectedBiome()));
            String next = state.biomes().get((index + 1) % state.biomes().size());
            PrestigeNetwork.sendAction(PrestigeNetwork.Action.SET_BIOME, actionPos(), next);
        }).bounds(x, y, width, 20).build());
        y += 26;
        addRenderableWidget(Button.builder(Component.literal("Season vote: " + (state.votedSeason().isBlank() ? "default" : state.votedSeason())), button ->
                PrestigeNetwork.sendAction(PrestigeNetwork.Action.VOTE_SETTING, actionPos(), "season=" + next(state.votedSeason(), List.of("spring", "summer", "autumn", "winter"))))
                .bounds(x, y, column, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Hour vote: " + (state.votedHour().isBlank() ? "default" : state.votedHour())), button ->
                PrestigeNetwork.sendAction(PrestigeNetwork.Action.VOTE_SETTING, actionPos(), "hour=" + next(state.votedHour(), List.of("dawn", "noon", "dusk", "midnight"))))
                .bounds(x + column + gap, y, column, 20).build());
        y += 25;
        addRenderableWidget(Button.builder(Component.literal("Onboarding: " + (state.votedMode().isBlank() ? "none" : state.votedMode())), button ->
                PrestigeNetwork.sendAction(PrestigeNetwork.Action.VOTE_SETTING, actionPos(), "mode=" + next(state.votedMode(), List.of("none", "class", "embark"))))
                .bounds(x, y, column, 20).build());
        seedCandidate = new EditBox(font, x + column + gap, y, column, 20, Component.literal("Seed proposal"));
        seedCandidate.setValue(state.seedProposal());
        seedCandidate.setHint(Component.literal("signed seed"));
        addRenderableWidget(seedCandidate);
        y += 26;
        addRenderableWidget(Button.builder(Component.literal("Propose seed"), button ->
                PrestigeNetwork.sendAction(PrestigeNetwork.Action.SEED_PROPOSE, actionPos(), seedCandidate.getValue()))
                .bounds(x + column + gap, y, column, 20).build());
        y += 27;
        if (state.operator()) {
            addRenderableWidget(Button.builder(Component.literal("Stage votes"), button ->
                    PrestigeNetwork.sendAction(PrestigeNetwork.Action.STAGE, actionPos(), ""))
                    .bounds(x, y, 88, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Cancel stage"), button ->
                    PrestigeNetwork.sendAction(PrestigeNetwork.Action.CANCEL, actionPos(), ""))
                    .bounds(x + 94, y, 88, 20).build());
            confirmation = new EditBox(font, x + 190, y, 200, 20, Component.literal("World name"));
            confirmation.setHint(Component.literal(state.worldName()));
            addRenderableWidget(confirmation);
            y += 24;
            addRenderableWidget(Button.builder(Component.literal("COMMIT PERMANENT RESET"), button ->
                    PrestigeNetwork.sendAction(PrestigeNetwork.Action.COMMIT, actionPos(), confirmation.getValue()))
                .bounds(x, y, width, 20).build());
        }
        if (!state.operator()) {
            addRenderableWidget(Button.builder(Component.literal("Open perk tree"), button -> { tab = 1; rebuild(); })
                    .bounds(x, topPos + 190, column, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Open schematics"), button -> { tab = 2; rebuild(); })
                    .bounds(x + column + gap, topPos + 190, column, 20).build());
        }
    }

    private static String next(String current, List<String> values) {
        int index = values.indexOf(current);
        return values.get((index + 1 + values.size()) % values.size());
    }

    private void rebuildPerks(PrestigeNetwork.StatePacket state, int x) {
        List<PrestigePerks.Node> nodes = PrestigePerks.nodes();
        for (int column = 0; column < 3; column++) {
            String branch = column == 0 ? "Place" : column == 1 ? "Vessel" : "Embark";
            int nodeY = topPos + 78;
            addRenderableWidget(Button.builder(Component.literal(branch), button -> {})
                    .bounds(x + column * 135, topPos + 58, 126, 18).build()).active = false;
            for (PrestigePerks.Node node : nodes) {
                if (!node.branch().equals(branch)) continue;
                boolean owned = state.ownedPerks().contains(node.id());
                boolean projected = state.projectedPerks().contains(node.id());
                int rank = state.rankedPerks().indexOf(node.id());
                String marker = owned ? "✓ " : projected ? "◇ " : rank >= 0 ? (rank + 1) + ". " : "";
                String label = marker + shortText(node.title(), 18);
                Button button = Button.builder(Component.literal(label), ignored -> {
                    PrestigeNetwork.sendAction(PrestigeNetwork.Action.PERK_TOGGLE, actionPos(), node.id());
                }).bounds(x + column * 135, nodeY, 126, 24).build();
                button.active = !owned;
                addRenderableWidget(button);
                nodeY += 31;
            }
        }
        addRenderableWidget(Button.builder(Component.literal("Back to reset"), button -> { tab = 0; rebuild(); })
                .bounds(x, topPos + 194, 188, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Refresh votes"), button ->
                PrestigeNetwork.sendAction(PrestigeNetwork.Action.REFRESH, actionPos(), ""))
                .bounds(x + 202, topPos + 208, 188, 20).build());
    }

    private void rebuildSchematics(PrestigeNetwork.StatePacket state, int x) {
        int y = topPos + 62;
        uploadIndex = state.uploads().isEmpty() ? 0 : Math.min(uploadIndex, state.uploads().size() - 1);
        String upload = state.uploads().isEmpty() ? "No server uploads" : state.uploads().get(uploadIndex);
        addRenderableWidget(Button.builder(Component.literal("Server upload: " + shortText(upload, 42)), button -> {
            if (!state.uploads().isEmpty()) { uploadIndex = (uploadIndex + 1) % state.uploads().size(); rebuild(); }
        }).bounds(x, y, 280, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Publish"), button -> {
            if (!state.uploads().isEmpty()) PrestigeNetwork.sendAction(PrestigeNetwork.Action.PUBLISH, actionPos(), state.uploads().get(uploadIndex));
        }).bounds(x + 290, y, 100, 20).build());
        y += 30;
        publishedIndex = state.published().isEmpty() ? 0 : Math.min(publishedIndex, state.published().size() - 1);
        String published = state.published().isEmpty() ? "Library empty" : state.published().get(publishedIndex).author()
                + "/" + state.published().get(publishedIndex).name();
        addRenderableWidget(Button.builder(Component.literal("Library: " + shortText(published, 42)), button -> {
            if (!state.published().isEmpty()) { publishedIndex = (publishedIndex + 1) % state.published().size(); rebuild(); }
        }).bounds(x, y, 280, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Download"), button -> {
            if (!state.published().isEmpty()) PrestigeNetwork.sendAction(PrestigeNetwork.Action.DOWNLOAD, actionPos(),
                    state.published().get(publishedIndex).id());
        }).bounds(x + 290, y, 100, 20).build());
        if (state.operator() && !state.published().isEmpty()) {
            y += 30;
            addRenderableWidget(Button.builder(Component.literal("Remove selected entry"), button ->
                    PrestigeNetwork.sendAction(PrestigeNetwork.Action.REMOVE, actionPos(), state.published().get(publishedIndex).id()))
                    .bounds(x, y, 390, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Back to reset"), button -> { tab = 0; rebuild(); })
                .bounds(x, topPos + 208, 188, 20).build());
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
        graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + 26, 0xff3a2345);
        graphics.fill(leftPos + 4, topPos + 54, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xff211a2a);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 10, 10, 0xffe6c6ff, false);
        var state = PrestigeClientState.state();
        if (state == null) {
            graphics.drawString(font, "Loading lineage state…", 10, 25, 0xffaaaaaa, false);
            return;
        }
        graphics.drawString(font, "Generation " + state.generation() + " · Unspent " + state.unspent()
                + " · Status " + state.status(), 10, 25, 0xffdddddd, false);
        if (tab == 1) {
            graphics.drawString(font, "Rank up to " + state.perkCapacity() + " perks; winners apply after health.",
                    10, 50, 0xffffd08a, false);
            graphics.drawString(font, "✓ owned   ◇ projected   number = ballot rank", 10, 230, 0xffb8d8ff, false);
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
