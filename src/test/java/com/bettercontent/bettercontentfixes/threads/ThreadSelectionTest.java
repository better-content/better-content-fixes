package com.bettercontent.bettercontentfixes.threads;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ThreadSelectionTest {
    private static ThreadDefinition definition(String id){return new ThreadDefinition(id,id,new ResourceLocation("minecraft","stone"),List.of("r","c","l"),List.of(),List.of(),new ThreadDefinition.Doorway("emi","minecraft:stone"));}
    @Test void heldThreadsOverrideAutomaticChoices(){var s=new ThreadPlayerState();s.held.add("a");s.held.add("b");assertEquals(List.of("a","b"),ThreadSelection.possibilities(List.of(definition("a"),definition("b"),definition("c")),s,1));}
    @Test void wildExcludesSevenRecentSelections(){var s=new ThreadPlayerState();for(char c='a';c<='g';c++)s.rememberWild(String.valueOf(c));var picked=ThreadSelection.possibilities(List.of(definition("a"),definition("b"),definition("c"),definition("d"),definition("e"),definition("f"),definition("g"),definition("h"),definition("i")),s,2);assertEquals(2,picked.size());assertFalse(s.wildHistory.contains(picked.get(1)));}
    @Test void phasesNeverRegress(){var s=new ThreadPlayerState();assertEquals(2,s.advance("a",2));assertEquals(2,s.advance("a",0));}
    @Test void thirdHoldReplacesOldest(){var s=new ThreadPlayerState();s.hold("a");s.hold("b");assertEquals("a",s.hold("c"));assertEquals(List.of("b","c"),List.copyOf(s.held));}
}
