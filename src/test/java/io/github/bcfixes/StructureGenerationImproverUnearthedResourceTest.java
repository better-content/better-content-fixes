package io.github.bcfixes;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class StructureGenerationImproverUnearthedResourceTest {
    private static final Path MIXIN_CONFIG = Path.of("src/main/resources/bcfixes.mixins.json");
    private static final Path SGI_CHUNK_GENERATOR_MIXIN =
            Path.of("src/main/java/io/github/bcfixes/mixin/sgi/ChunkGeneratorMixin.java");

    @Test
    void sgiSurfaceConformingRunsAfterHyleDecoration() throws IOException {
        JsonObject config = JsonParser.parseReader(Files.newBufferedReader(MIXIN_CONFIG)).getAsJsonObject();
        JsonArray mixins = config.getAsJsonArray("mixins");
        String source = Files.readString(SGI_CHUNK_GENERATOR_MIXIN);

        assertTrue(contains(mixins, "sgi.ChunkGeneratorMixin"),
                "SGI must defer its terrain conform pass until decoration has finished");
        assertTrue(contains(mixins, "sgi.TerrainConformUtilMixin"),
                "SGI must retain the post-conform Hyle safety pass");
        assertTrue(source.contains("TerrainConformUtil;applyDuringSurface"),
                "the SGI surface conform invocation must be redirected");
        assertTrue(source.contains("@At(\"TAIL\")"),
                "the deferred SGI pass must execute after Hyle/Unearthed decoration");
        assertTrue(source.contains("applyDuringSurface.invoke(null, level, structureManager, chunk)"),
                "the deferred SGI pass must invoke the original conform routine at decoration tail");
    }

    private static boolean contains(JsonArray values, String expected) {
        for (int index = 0; index < values.size(); index++) {
            if (expected.equals(values.get(index).getAsString())) {
                return true;
            }
        }
        return false;
    }
}
