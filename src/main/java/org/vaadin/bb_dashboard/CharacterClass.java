package org.vaadin.bb_dashboard;

import java.io.Serializable;
import java.util.Map;

public interface CharacterClass extends Serializable {
    String name();
    Map<String, Integer> getAttributes();
    Map<String, Background> getBackgrounds();

    interface Background {
        String getName();
        Map<String, Integer> getAttributes();
    }
}
