package com.bettercontent.bettercontentfixes.threads;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

final class ThreadContractsTest {
    @Test void artCatalogueContainsExactlyEighteenUniqueIds(){assertEquals(18,ThreadArt.IDS.size());assertEquals(18,new HashSet<>(ThreadArt.IDS).size());assertEquals(new HashSet<>(ThreadArt.IDS),ThreadArt.EXPECTED_ASPECTS.keySet());assertEquals(new HashSet<>(ThreadArt.IDS),ThreadArt.EXPECTED_SYMBOLS.keySet());}
    @Test void artCatalogueUsesTheExactApprovedAspectAssignments(){assertEquals(Map.ofEntries(
        Map.entry("world_remembers",ThreadAspect.CONTROL),Map.entry("life_becomes_capable",ThreadAspect.RENEWAL),Map.entry("feast_before_journey",ThreadAspect.ENDURANCE),
        Map.entry("stone_makes_promises",ThreadAspect.WORK),Map.entry("materials_temperaments",ThreadAspect.ROBUSTNESS),Map.entry("motion_becomes_industry",ThreadAspect.WORK),
        Map.entry("vessel_becomes_place",ThreadAspect.MOBILITY),Map.entry("rails_turn_distance",ThreadAspect.TEMPO),Map.entry("pressure_changes_matter",ThreadAspect.IMPACT),
        Map.entry("machines_can_remember",ThreadAspect.CONTROL),Map.entry("doors_borrow_worlds",ThreadAspect.MOBILITY),Map.entry("flight_engineered",ThreadAspect.MOBILITY),
        Map.entry("army_walks_toward_you",ThreadAspect.IMPACT),Map.entry("leave_atmosphere",ThreadAspect.ENDURANCE),Map.entry("blood_infrastructure",ThreadAspect.RENEWAL),
        Map.entry("reality_has_grammar",ThreadAspect.CONTROL),Map.entry("spirits_honour_contracts",ThreadAspect.CONTROL),Map.entry("world_inherited",ThreadAspect.RENEWAL)),ThreadArt.EXPECTED_ASPECTS);}
    @Test void definitionsRequireArtRevealAndThreePhases(){
        var json=JsonParser.parseString("{\"id\":\"test\",\"title\":\"Test\",\"symbol\":\"minecraft:stone\",\"aspect\":\"control\",\"art\":\"better_content_fixes:textures/gui/threads/test.png\",\"reveal\":\"Something answered.\",\"phases\":[\"a\",\"b\",\"c\"],\"discover\":[],\"contact\":[],\"lived\":[],\"doorway\":{\"type\":\"emi\",\"target\":\"minecraft:stone\"}}").getAsJsonObject();
        var definition=ThreadDefinition.parse(json);assertEquals("test",definition.id());assertEquals(3,definition.phases().size());assertEquals("Something answered.",definition.reveal());
        json.getAsJsonArray("phases").remove(2);assertThrows(IllegalArgumentException.class,()->ThreadDefinition.parse(json));
    }
    @Test void aspectsUseTheExactSystemicSalienceIdsAndColors(){
        assertEquals(Set.of("impact","tempo","work","mobility","endurance","robustness","renewal","control"),Arrays.stream(ThreadAspect.values()).map(ThreadAspect::id).collect(java.util.stream.Collectors.toSet()));
        assertEquals(0xE4717D,ThreadAspect.IMPACT.color());assertEquals(0xAA652B,ThreadAspect.TEMPO.color());assertEquals(0xCAA903,ThreadAspect.WORK.color());assertEquals(0xC0E304,ThreadAspect.MOBILITY.color());
        assertEquals(0x35BBD0,ThreadAspect.ENDURANCE.color());assertEquals(0x1175FC,ThreadAspect.ROBUSTNESS.color());assertEquals(0x6FEDBA,ThreadAspect.RENEWAL.color());assertEquals(0x8A6CB2,ThreadAspect.CONTROL.color());
        assertThrows(IllegalArgumentException.class,()->ThreadAspect.parse("Control"));
    }
    @Test void noticePayloadContainsNoGameAssetOrProse(){
        assertArrayEquals(new String[]{"id","title","aspect"},Arrays.stream(ThreadNetwork.Notice.class.getRecordComponents()).map(java.lang.reflect.RecordComponent::getName).toArray(String[]::new));
        assertDoesNotThrow(()->new ThreadNetwork.Notice("world_remembers","The World Remembers","control"));
        assertThrows(IllegalArgumentException.class,()->new ThreadNetwork.Notice("world_remembers","The World Remembers","Control"));
    }
    @Test void automaticTeaseUsesACompactGlyphLockup(){
        assertEquals("✦",ThreadClient.NOTICE_GLYPH);assertTrue(ThreadClient.NOTICE_TEXT_SCALE<1.0f);
        assertTrue(ThreadClient.NOTICE_TEXT_OFFSET_Y-ThreadClient.NOTICE_GLYPH_OFFSET_Y<=10);
    }
    @Test void noticesQueueOneAtATimeAndPauseWithoutAdvancing(){
        var queue=new ThreadNoticeQueue<String>(value->value);queue.addAll(List.of("first","second","first"));assertEquals(2,queue.size());
        var first=queue.advance(0,false);assertEquals("first",first.notice());assertTrue(first.started());
        assertEquals(0,queue.advance(900,true).elapsedMs());assertNull(queue.advance(3_200,false));
        var second=queue.advance(0,false);assertEquals("second",second.notice());assertTrue(second.started());
    }
    @Test void readStateChangesOnlyAfterCompletedDevelopment(){
        var reveal=new ThreadRevealState();reveal.select(true);assertEquals(ThreadRevealState.Phase.SEALED,reveal.phase());
        assertEquals(ThreadRevealState.Activation.STARTED,reveal.activate());assertEquals(ThreadRevealState.Phase.DEVELOPING,reveal.phase());
        assertFalse(reveal.advance(1_799));assertEquals(ThreadRevealState.Phase.DEVELOPING,reveal.phase());assertTrue(reveal.advance(1));assertEquals(ThreadRevealState.Phase.COMPLETE,reveal.phase());
    }
    @Test void developmentSkipIsConsumedBeforeControlsCanActivate(){
        var reveal=new ThreadRevealState();reveal.select(true);assertEquals(ThreadRevealState.Activation.STARTED,reveal.activate());
        assertEquals(ThreadRevealState.Activation.COMPLETED,reveal.activate());assertEquals(ThreadRevealState.Activation.IGNORED,reveal.activate());
    }
    @Test void phaseAdvancementNeverRegresses(){var state=new ThreadPlayerState();assertEquals(2,state.advance("test",2));assertEquals(2,state.advance("test",0));}
    @Test void contextualEventsContainNoLoginOverture() throws Exception {
        String source=Files.readString(Path.of("src/main/java/com/bettercontent/bettercontentfixes/threads/ThreadEvents.java"));
        assertFalse(source.contains("overture("));assertFalse(source.contains("RETURN_MS"));assertFalse(source.contains("lastLoginReal"));
    }
    @Test void everyFacsimileModelExists() {for(String id:ThreadArt.IDS)assertTrue(Files.isRegularFile(Path.of("src/main/resources/assets/better_content_fixes/models/item/thread_cards",id+".json")),id);}
}
