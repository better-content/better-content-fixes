package io.github.btmfixes;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class BtmFixesResourceTest {
    @Test
    void mixinConfigTargetsExistingMixinClasses() throws IOException {
        Path configPath = Path.of("src/main/resources/btmfixes.mixins.json");
        JsonObject config = JsonParser.parseReader(Files.newBufferedReader(configPath)).getAsJsonObject();
        String packageName = config.get("package").getAsString();

        assertMixinClassesExist(packageName, config.getAsJsonArray("mixins"));
        assertMixinClassesExist(packageName, config.getAsJsonArray("client"));
    }

    @Test
    void mixinConfigKeepsOptionalTargetsNonRequired() throws IOException {
        JsonObject config = JsonParser.parseReader(Files.newBufferedReader(
                Path.of("src/main/resources/btmfixes.mixins.json"))).getAsJsonObject();

        assertTrue(!config.get("required").getAsBoolean(), "optional compatibility mixins must stay non-required");
        assertTrue(config.getAsJsonObject("injectors").get("defaultRequire").getAsInt() == 0,
                "optional compatibility injectors should not require target matches");
    }

    private static void assertMixinClassesExist(String packageName, JsonArray mixins) {
        mixins.forEach(element -> {
            String relativeClass = element.getAsString().replace('.', '/') + ".java";
            Path classPath = Path.of("src/main/java", packageName.replace('.', '/'), relativeClass);
            assertTrue(Files.exists(classPath), "missing mixin class " + classPath);
        });
    }
}
