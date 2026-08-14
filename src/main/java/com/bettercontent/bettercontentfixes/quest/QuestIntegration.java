package com.bettercontent.bettercontentfixes.quest;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import java.lang.reflect.Method;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

public final class QuestIntegration {
    private static volatile QuestRevealPolicy policy = QuestRevealPolicy.EMPTY;

    private QuestIntegration() {}

    public static void initialize() {
        if (!ModList.get().isLoaded("ftbquests")) return;
        QuestTaskTypes.register();
        policy = QuestRevealPolicy.load();
        MinecraftForge.EVENT_BUS.register(QuestIntegration.class);
        MinecraftForge.EVENT_BUS.register(GameplayCriterionDetector.class);
    }

    public static void completeCriterion(ServerPlayer player, String name) {
        if (!GameplayCriterionNames.SUPPORTED.contains(name) || !ModList.get().isLoaded("ftbquests") || ServerQuestFile.INSTANCE == null) return;
        TeamData data = ServerQuestFile.INSTANCE.getOrCreateTeamData(player);
        for (Task task : ServerQuestFile.INSTANCE.getAllTasks()) {
            if (task instanceof CriterionTask criterion && criterion.criterion().equals(name)) data.setProgress(task, 1L);
        }
    }

    public static boolean forceVisible(Quest quest, TeamData data) {
        QuestRevealPolicy current = policy;
        long chapter = quest.getChapter().id;
        return isUnbound(data) && current.liveChapters().contains(chapter);
    }

    public static boolean forceHidden(Quest quest, TeamData data) {
        QuestRevealPolicy current = policy;
        long chapter = quest.getChapter().id;
        if (forceVisible(quest, data)) return false;
        QuestRevealPolicy.Unlock unlock = current.chapterUnlocks().get(chapter);
        if (unlock == null) return false;
        if (unlock.quest() != 0L) {
            Quest required = data.getFile().getQuest(unlock.quest());
            return required == null || !data.isCompleted(required);
        }
        boolean completed = data.getFile().getAllTasks().stream()
                .filter(CriterionTask.class::isInstance).map(CriterionTask.class::cast)
                .anyMatch(task -> task.criterion().equals(unlock.criterion()) && data.isCompleted(task));
        return !completed;
    }

    public static boolean isPreviewChapter(long chapterId) { return policy.previewChapters().contains(chapterId); }

    public static boolean taskIsLocked(long taskId, TeamData data) {
        String criterion = policy.taskUnlockCriteria().get(taskId);
        if (criterion == null) return false;
        return data.getFile().getAllTasks().stream()
                .filter(CriterionTask.class::isInstance).map(CriterionTask.class::cast)
                .noneMatch(task -> task.criterion().equals(criterion) && data.isCompleted(task));
    }

    private static boolean isUnbound(TeamData data) {
        Quest anchor = data.getFile().getQuest(policy.anchorQuest());
        return anchor != null && data.isCompleted(anchor);
    }

    @SubscribeEvent
    public static void onForeignEvent(Event event) {
        String criterion = GameplayCriterionNames.forEventSimpleName(event.getClass().getSimpleName());
        if (criterion.isEmpty()) return;
        ServerPlayer player = reflectedPlayer(event);
        if (player != null) completeCriterion(player, criterion);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || ServerQuestFile.INSTANCE == null) return;
        TeamData data = ServerQuestFile.INSTANCE.getOrCreateTeamData(player);
        Quest anchor = ServerQuestFile.INSTANCE.getQuest(policy.anchorQuest());
        if (anchor != null && data.isCompleted(anchor)) completeCriterion(player, "book_burned");
    }

    private static ServerPlayer reflectedPlayer(Object event) {
        for (String name : new String[]{"getPlayer", "player"}) {
            try {
                Method method = event.getClass().getMethod(name);
                Object value = method.invoke(event);
                if (value instanceof ServerPlayer player) return player;
            } catch (ReflectiveOperationException ignored) {}
        }
        return null;
    }

    public static void onItemTick(ItemEntity item) {
        if (item.tickCount % 2 == 0 && !item.isRemoved() && item.getItem().is(Items.BOOK)
                && item.level() instanceof ServerLevel level && touchesTrueFlame(item)) tryUnbind(item, level);
    }

    private static boolean touchesTrueFlame(ItemEntity item) {
        BlockState state = item.level().getBlockState(item.blockPosition());
        FluidState fluid = item.level().getFluidState(item.blockPosition());
        return item.isInLava() || item.isOnFire() || state.getBlock() instanceof BaseFireBlock
                || state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)
                || fluid.is(net.minecraft.tags.FluidTags.LAVA);
    }

    private static void tryUnbind(ItemEntity book, ServerLevel level) {
        if (policy.anchorQuest() == 0L || ServerQuestFile.INSTANCE == null) return;
        if (!(book.getOwner() instanceof ServerPlayer owner) || owner.hasDisconnected()) return;
        TeamData data = ServerQuestFile.INSTANCE.getOrCreateTeamData(owner);
        Quest anchor = ServerQuestFile.INSTANCE.getQuest(policy.anchorQuest());
        if (anchor == null || data.isCompleted(anchor)) return;
        for (Task task : anchor.getTasks()) data.setProgress(task, task.getMaxProgress());
        if (!data.isCompleted(anchor)) return;
        completeCriterion(owner, "book_burned");
        book.discard();
        level.sendParticles(ParticleTypes.FLAME, book.getX(), book.getY(), book.getZ(), 24, .35, .35, .35, .03);
        level.sendParticles(ParticleTypes.ENCHANT, book.getX(), book.getY(), book.getZ(), 48, .5, .5, .5, .1);
        for (ServerPlayer member : data.getOnlineMembers()) {
            member.connection.send(new ClientboundSetTitleTextPacket(Component.literal("THE BINDING BREAKS")));
            member.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("The whole book is open.")));
            member.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1F, 1F);
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().is(Items.BOOK)) {
            event.getToolTip().add(Component.translatable("better_content_fixes.book.binding_info"));
        }
    }
}
