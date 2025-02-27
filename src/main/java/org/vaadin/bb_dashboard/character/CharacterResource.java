package org.vaadin.bb_dashboard.character;

import java.io.Serializable;
import java.util.Map;

public interface CharacterResource extends Serializable {
    String name();
    Map<String, Integer> getAttributes();
    Map<String, Background> getBackgrounds();

    interface Background {
        String getName();
        Map<String, Integer> getAttributes();
    }
}
