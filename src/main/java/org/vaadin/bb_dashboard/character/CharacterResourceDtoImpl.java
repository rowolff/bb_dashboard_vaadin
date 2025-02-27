package org.vaadin.bb_dashboard.character;

import java.util.Map;

public record CharacterResourceDtoImpl(String name, Map<String, Integer> attributes,
                                       Map<String, Background> backgrounds) implements CharacterResourceDto {

    @Override
    public Map<String, Integer> getAttributes() {
        return attributes;
    }

    @Override
    public Map<String, Background> getBackgrounds() {
        return backgrounds;
    }

    public static class BackgroundImpl implements Background {
        private final String name;
        private final Map<String, Integer> attributes;

        public BackgroundImpl(String name, Map<String, Integer> attributes) {
            this.name = name;
            this.attributes = attributes;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Map<String, Integer> getAttributes() {
            return attributes;
        }
    }
}
