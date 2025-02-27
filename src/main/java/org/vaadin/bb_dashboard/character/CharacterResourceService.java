package org.vaadin.bb_dashboard.character;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Service
public class CharacterResourceService implements Serializable {

    private Map<String, Map<String, Integer>> archetypes;
    private Map<String, CharacterResource> classes;

    public CharacterResourceService() {
        loadCharacterData();
    }

    private void loadCharacterData() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getResourceAsStream("/archetypes.json")) {
            archetypes = mapper.readValue(is, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (InputStream is = getClass().getResourceAsStream("/classes.json")) {
            Map<String, Map<String, Object>> rawClasses = mapper.readValue(is, Map.class);
            classes = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>> entry : rawClasses.entrySet()) {
                String className = entry.getKey();
                Map<String, Integer> attributes = (Map<String, Integer>) entry.getValue().get("attributes");
                Map<String, CharacterResource.Background> backgrounds = new HashMap<>();
                Map<String, Map<String, Integer>> rawBackgrounds = (Map<String, Map<String, Integer>>) entry.getValue().get("backgrounds");
                for (Map.Entry<String, Map<String, Integer>> bgEntry : rawBackgrounds.entrySet()) {
                    backgrounds.put(bgEntry.getKey(), new CharacterResourceImpl.BackgroundImpl(bgEntry.getKey(), bgEntry.getValue()));
                }
                classes.put(className, new CharacterResourceImpl(className, attributes, backgrounds));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Map<String, Integer>> getArchetypes() {
        return archetypes;
    }

    public Map<String, CharacterResource> getClasses() {
        return classes;
    }

    public Map<String, Integer> getArchetypeAttributes(String archetype) {
        return archetypes.get(archetype);
    }
}
