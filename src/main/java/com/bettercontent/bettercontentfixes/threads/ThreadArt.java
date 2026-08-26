package com.bettercontent.bettercontentfixes.threads;

import java.util.List;
import java.util.Map;

final class ThreadArt {
    static final List<String> IDS=List.of("world_remembers","life_becomes_capable","feast_before_journey","stone_makes_promises","materials_temperaments","motion_becomes_industry","vessel_becomes_place","rails_turn_distance","pressure_changes_matter","machines_can_remember","doors_borrow_worlds","flight_engineered","army_walks_toward_you","leave_atmosphere","blood_infrastructure","reality_has_grammar","spirits_honour_contracts","world_inherited");
    static final Map<String,ThreadAspect> EXPECTED_ASPECTS=Map.ofEntries(
        Map.entry("world_remembers",ThreadAspect.CONTROL),Map.entry("life_becomes_capable",ThreadAspect.RENEWAL),
        Map.entry("feast_before_journey",ThreadAspect.ENDURANCE),Map.entry("stone_makes_promises",ThreadAspect.WORK),
        Map.entry("materials_temperaments",ThreadAspect.ROBUSTNESS),Map.entry("motion_becomes_industry",ThreadAspect.WORK),
        Map.entry("vessel_becomes_place",ThreadAspect.MOBILITY),Map.entry("rails_turn_distance",ThreadAspect.TEMPO),
        Map.entry("pressure_changes_matter",ThreadAspect.IMPACT),Map.entry("machines_can_remember",ThreadAspect.CONTROL),
        Map.entry("doors_borrow_worlds",ThreadAspect.MOBILITY),Map.entry("flight_engineered",ThreadAspect.MOBILITY),
        Map.entry("army_walks_toward_you",ThreadAspect.IMPACT),Map.entry("leave_atmosphere",ThreadAspect.ENDURANCE),
        Map.entry("blood_infrastructure",ThreadAspect.RENEWAL),Map.entry("reality_has_grammar",ThreadAspect.CONTROL),
        Map.entry("spirits_honour_contracts",ThreadAspect.CONTROL),Map.entry("world_inherited",ThreadAspect.RENEWAL));
    static final Map<String,String> EXPECTED_SYMBOLS=Map.ofEntries(
        Map.entry("world_remembers","minecraft:compass"),Map.entry("life_becomes_capable","minecraft:player_head"),
        Map.entry("feast_before_journey","minecraft:rabbit_stew"),Map.entry("stone_makes_promises","minecraft:raw_iron"),
        Map.entry("materials_temperaments","tconstruct:materials_and_you"),Map.entry("motion_becomes_industry","create:cogwheel"),
        Map.entry("vessel_becomes_place","vs_eureka:oak_ship_helm"),Map.entry("rails_turn_distance","create:railway_casing"),
        Map.entry("pressure_changes_matter","pneumaticcraft:pressure_chamber_wall"),Map.entry("machines_can_remember","ae2:controller"),
        Map.entry("doors_borrow_worlds","minecraft:ender_eye"),Map.entry("flight_engineered","aether:golden_parachute"),
        Map.entry("army_walks_toward_you","minecraft:white_banner"),Map.entry("leave_atmosphere","minecraft:firework_rocket"),
        Map.entry("blood_infrastructure","bloodmagic:altar"),Map.entry("reality_has_grammar","ars_nouveau:novice_spell_book"),
        Map.entry("spirits_honour_contracts","hexerei:book_of_shadows"),Map.entry("world_inherited","minecraft:nether_star"));
    static float itemIndex(String id){int index=IDS.indexOf(id);return index<0?0:index+1;}
    private ThreadArt(){}
}
