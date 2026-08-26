package com.bettercontent.bettercontentfixes.threads;

import java.util.List;

final class ThreadArt {
    static final List<String> IDS=List.of("world_remembers","life_becomes_capable","feast_before_journey","stone_makes_promises","materials_temperaments","motion_becomes_industry","vessel_becomes_place","rails_turn_distance","pressure_changes_matter","machines_can_remember","doors_borrow_worlds","flight_engineered","army_walks_toward_you","leave_atmosphere","blood_infrastructure","reality_has_grammar","spirits_honour_contracts","world_inherited");
    static float itemIndex(String id){int index=IDS.indexOf(id);return index<0?0:index+1;}
    private ThreadArt(){}
}
