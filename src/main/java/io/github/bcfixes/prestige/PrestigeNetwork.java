package io.github.bcfixes.prestige;

import io.github.bcfixes.BetterContentFixes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class PrestigeNetwork {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(BetterContentFixes.MOD_ID, "prestige"))
            .networkProtocolVersion(() -> VERSION).clientAcceptedVersions(VERSION::equals)
            .serverAcceptedVersions(VERSION::equals).simpleChannel();
    private static int discriminator;

    private PrestigeNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(ActionPacket.class, discriminator++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ActionPacket::encode).decoder(ActionPacket::decode).consumerMainThread(ActionPacket::handle).add();
        CHANNEL.messageBuilder(StatePacket.class, discriminator++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(StatePacket::encode).decoder(StatePacket::decode).consumerMainThread(StatePacket::handle).add();
        CHANNEL.messageBuilder(DownloadPacket.class, discriminator++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DownloadPacket::encode).decoder(DownloadPacket::decode).consumerMainThread(DownloadPacket::handle).add();
    }

    public static void sendAction(Action action, BlockPos pos, String value) {
        CHANNEL.sendToServer(new ActionPacket(action, pos, value));
    }

    public static void sendState(ServerPlayer player) {
        try {
            BlockPos pos = player.containerMenu instanceof WorldCondenserMenu menu ? menu.pos() : BlockPos.ZERO;
            CHANNEL.sendTo(new StatePacket(PrestigeService.view(player), pos), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
        catch (Exception error) { player.displayClientMessage(Component.literal("World Condenser state failed: " + error.getMessage()), false); }
    }

    public enum Action { REFRESH, SET_BIOME, STAGE, CANCEL, COMMIT, PUBLISH, DOWNLOAD, REMOVE }

    public record ActionPacket(Action action, BlockPos pos, String value) {
        static void encode(ActionPacket packet, FriendlyByteBuf buffer) {
            buffer.writeEnum(packet.action); buffer.writeBlockPos(packet.pos); buffer.writeUtf(packet.value, 256);
        }
        static ActionPacket decode(FriendlyByteBuf buffer) {
            return new ActionPacket(buffer.readEnum(Action.class), buffer.readBlockPos(), buffer.readUtf(256));
        }
        static void handle(ActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
            ServerPlayer player = supplier.get().getSender();
            if (player == null || !(player.containerMenu instanceof WorldCondenserMenu menu) || !menu.pos().equals(packet.pos)) return;
            try {
                switch (packet.action) {
                    case REFRESH -> { }
                    case SET_BIOME -> PrestigeService.saveDraft(player, packet.value);
                    case STAGE -> PrestigeService.stage(player, packet.pos);
                    case CANCEL -> PrestigeService.cancel(player);
                    case COMMIT -> {
                        String tx = PrestigeService.commit(player, packet.pos, packet.value);
                        player.server.getPlayerList().broadcastSystemMessage(Component.literal(
                                "World Condenser committed " + tx + " by " + player.getGameProfile().getName()
                                        + "; all world and player state will be archived and reset."), false);
                    }
                    case PUBLISH -> {
                        int separator = packet.value.indexOf('/');
                        if (separator <= 0 || separator == packet.value.length() - 1) {
                            throw new IllegalArgumentException("upload selection is malformed");
                        }
                        PrestigeService.publish(player, packet.value.substring(0, separator), packet.value.substring(separator + 1));
                    }
                    case DOWNLOAD -> {
                        SchematicLibrary.Entry entry = SchematicLibrary.list(player.server).stream()
                                .filter(candidate -> candidate.id().equals(packet.value)).findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("unknown schematic entry"));
                        byte[] data = SchematicLibrary.download(player.server, entry.id());
                        CHANNEL.sendTo(new DownloadPacket(entry.author(), entry.originalName(), entry.sha256(), data),
                                player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    }
                    case REMOVE -> PrestigeService.remove(player, packet.value);
                }
            } catch (Exception error) {
                player.displayClientMessage(Component.literal("World Condenser refused: " + error.getMessage()), false);
            }
            if (packet.action != Action.COMMIT) sendState(player);
        }
    }

    public record ClientEntry(String id, String author, String name, long size, String sha256) {}
    public record StatePacket(String status, String worldName, BlockPos pos, long total, long unspent, long generation, String selectedBiome,
                              String author, boolean operator, List<String> biomes, List<String> uploads,
                              List<ClientEntry> published) {
        StatePacket(PrestigeService.View view, BlockPos pos) {
            this(view.status(), view.worldName(), pos, view.lineage().totalPrestiges(), view.lineage().unspentPoints(), view.lineage().generation(),
                    view.selectedBiome(), view.author(), view.operator(), view.allowedBiomes(), view.ownUploads(),
                    view.published().stream().map(entry -> new ClientEntry(entry.id(), entry.author(), entry.originalName(),
                            entry.size(), entry.sha256())).toList());
        }
        static void encode(StatePacket packet, FriendlyByteBuf buffer) {
            buffer.writeUtf(packet.status, 64); buffer.writeUtf(packet.worldName, 128); buffer.writeBlockPos(packet.pos);
            buffer.writeLong(packet.total); buffer.writeLong(packet.unspent);
            buffer.writeLong(packet.generation); buffer.writeUtf(packet.selectedBiome, 256); buffer.writeUtf(packet.author, 32);
            buffer.writeBoolean(packet.operator);
            buffer.writeCollection(packet.biomes, (out, value) -> out.writeUtf(value, 256));
            buffer.writeCollection(packet.uploads, (out, value) -> out.writeUtf(value, 256));
            buffer.writeCollection(packet.published, (out, entry) -> {
                out.writeUtf(entry.id, 64); out.writeUtf(entry.author, 32); out.writeUtf(entry.name, 256);
                out.writeLong(entry.size); out.writeUtf(entry.sha256, 64);
            });
        }
        static StatePacket decode(FriendlyByteBuf buffer) {
            String status = buffer.readUtf(64); String worldName = buffer.readUtf(128); BlockPos pos = buffer.readBlockPos();
            long total = buffer.readLong(); long unspent = buffer.readLong();
            long generation = buffer.readLong(); String biome = buffer.readUtf(256); String author = buffer.readUtf(32);
            boolean operator = buffer.readBoolean();
            List<String> biomes = buffer.readList(in -> in.readUtf(256));
            List<String> uploads = buffer.readList(in -> in.readUtf(256));
            List<ClientEntry> entries = buffer.readList(in -> new ClientEntry(in.readUtf(64), in.readUtf(32),
                    in.readUtf(256), in.readLong(), in.readUtf(64)));
            return new StatePacket(status, worldName, pos, total, unspent, generation, biome, author, operator, biomes, uploads, entries);
        }
        static void handle(StatePacket packet, Supplier<NetworkEvent.Context> supplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PrestigeClientState.accept(packet));
        }
    }

    public record DownloadPacket(String author, String name, String sha256, byte[] data) {
        static void encode(DownloadPacket packet, FriendlyByteBuf buffer) {
            buffer.writeUtf(packet.author, 32); buffer.writeUtf(packet.name, 256); buffer.writeUtf(packet.sha256, 64);
            buffer.writeByteArray(packet.data);
        }
        static DownloadPacket decode(FriendlyByteBuf buffer) {
            return new DownloadPacket(buffer.readUtf(32), buffer.readUtf(256), buffer.readUtf(64),
                    buffer.readByteArray((int) SchematicLibrary.MAX_BYTES));
        }
        static void handle(DownloadPacket packet, Supplier<NetworkEvent.Context> supplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PrestigeClientState.saveDownload(packet));
        }
    }
}
