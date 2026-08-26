package com.bettercontent.bettercontentfixes.threads;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

final class ThreadContractsTest {
    @Test void artCatalogueContainsExactlyEighteenUniqueIds(){assertEquals(18,ThreadArt.IDS.size());assertEquals(18,new HashSet<>(ThreadArt.IDS).size());}
    @Test void definitionsRequireArtRevealAndThreePhases(){
        var json=JsonParser.parseString("{\"id\":\"test\",\"title\":\"Test\",\"symbol\":\"minecraft:stone\",\"art\":\"better_content_fixes:textures/gui/threads/test.png\",\"reveal\":\"Something answered.\",\"phases\":[\"a\",\"b\",\"c\"],\"discover\":[],\"contact\":[],\"lived\":[],\"doorway\":{\"type\":\"emi\",\"target\":\"minecraft:stone\"}}").getAsJsonObject();
        var definition=ThreadDefinition.parse(json);assertEquals("test",definition.id());assertEquals(3,definition.phases().size());assertEquals("Something answered.",definition.reveal());
        json.getAsJsonArray("phases").remove(2);assertThrows(IllegalArgumentException.class,()->ThreadDefinition.parse(json));
    }
    @Test void phaseAdvancementNeverRegresses(){var state=new ThreadPlayerState();assertEquals(2,state.advance("test",2));assertEquals(2,state.advance("test",0));}
    @Test void contextualEventsContainNoLoginOverture() throws Exception {
        String source=Files.readString(Path.of("src/main/java/com/bettercontent/bettercontentfixes/threads/ThreadEvents.java"));
        assertFalse(source.contains("overture("));assertFalse(source.contains("RETURN_MS"));assertFalse(source.contains("lastLoginReal"));
    }
    @Test void everyFacsimileModelExists() {for(String id:ThreadArt.IDS)assertTrue(Files.isRegularFile(Path.of("src/main/resources/assets/better_content_fixes/models/item/thread_cards",id+".json")),id);}
}
