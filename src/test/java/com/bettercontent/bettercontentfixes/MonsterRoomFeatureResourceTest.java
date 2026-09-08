package com.bettercontent.bettercontentfixes;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class MonsterRoomFeatureResourceTest {
    @Test
    void deferredWorldgenSpawnerEntityIsCreatedInItsOwningChunk() throws IOException {
        String config = Files.readString(Path.of("src/main/resources/better_content_fixes.mixins.json"));
        String source = Files.readString(Path.of(
                "src/main/java/com/bettercontent/bettercontentfixes/mixin/minecraft/MonsterRoomFeatureMixin.java"));

        assertTrue(config.contains("minecraft.MonsterRoomFeatureMixin"));
        assertTrue(source.contains("@Mixin(MonsterRoomFeature.class)"));
        assertTrue(source.contains("method = {\"place\", \"m_142674_\"}"));
        assertTrue(source.contains("WorldGenLevel;getBlockEntity"));
        assertTrue(source.contains("level instanceof WorldGenRegion region"));
        assertTrue(source.contains("state.is(Blocks.SPAWNER)"));
        assertTrue(source.contains("region.getChunk(position).setBlockEntity(recovered)"));
        assertTrue(source.contains("Lorg/slf4j/Logger;error(Ljava/lang/String;[Ljava/lang/Object;)V"));
        assertTrue(source.contains("cancellable = true"));
        assertTrue(source.split("require = 1", -1).length - 1 == 2);
        assertTrue(source.contains("callback.setReturnValue(false)"));
    }
}
