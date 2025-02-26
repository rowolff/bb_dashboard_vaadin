package org.vaadin.bb_dashboard;

import java.util.Map;

public interface CharacterClass {
    String name();
    Map<String, Integer> getAttributes();
    Map<String, Background> getBackgrounds();

    interface Background {
        String getName();
        Map<String, Integer> getAttributes();
    }
}
