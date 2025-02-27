package org.vaadin.bb_dashboard;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonArray;
import elemental.json.impl.JreJsonObject;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.Map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.vaadin.bb_dashboard.character.Character;
import org.vaadin.bb_dashboard.character.CharacterResource;
import org.vaadin.bb_dashboard.character.CharacterResourceService;

import static org.vaadin.bb_dashboard.Constants.*;

@Route
public class MainView extends VerticalLayout {

    private final Character character = new Character();

    private final AttributeComponent accuracyBox;
    private final AttributeComponent damageBox;
    private final AttributeComponent speedBox;
    private final AttributeComponent masteryBox;

    private final CharacterResourceService loader;

    private final TextField archetypeBonusesLabel;
    private final TextField classBonusesLabel;
    private final TextField backgroundBonusesLabel;
    private final TextField pointsField;
    private final TextField spentPointsLabel;

    private final ComboBox<String> archetypeComboBox = new ComboBox<>();
    private final ComboBox<String> classComboBox = new ComboBox<>();
    private final ComboBox<String> backgroundComboBox = new ComboBox<>();

    private final HorizontalLayout characterList = new HorizontalLayout();

    public MainView(CharacterResourceService characterResourceService) {
        loader = characterResourceService;

        accuracyBox = new AttributeComponent(ACCURACY, "ACC", character, this);
        accuracyBox.setAlignItems(Alignment.BASELINE);
        damageBox = new AttributeComponent(DAMAGE, "DMG", character, this);
        damageBox.setAlignItems(Alignment.BASELINE);
        speedBox = new AttributeComponent(SPEED, "SPD", character, this);
        speedBox.setAlignItems(Alignment.BASELINE);
        masteryBox = new AttributeComponent(MASTERY, "MST", character, this);
        masteryBox.setAlignItems(Alignment.BASELINE);

        pointsField = new TextField("Remaining Points");
        pointsField.setValue(String.valueOf(character.getAvailablePointsToSpend()));
        pointsField.setReadOnly(true);
        spentPointsLabel = new TextField("Spent Points");
        spentPointsLabel.setReadOnly(true);
        HorizontalLayout pointsLayout = new HorizontalLayout(pointsField, spentPointsLabel);
        pointsLayout.setWidth("100%");
        pointsLayout.setAlignItems(Alignment.CENTER);
        pointsField.setWidth("50%");
        spentPointsLabel.setWidth("50%");


        archetypeComboBox.setItems(characterResourceService.getArchetypes().keySet());
        archetypeComboBox.addValueChangeListener(event -> updateArchetypeAttributes(event.getValue()));
        archetypeBonusesLabel = new TextField(ARCHETYPE + " Bonuses");

        classComboBox.setItems(characterResourceService.getClasses().keySet());
        classComboBox.addValueChangeListener(event -> {
            character.setBackground(Character.Stats.builder().build());
            updateClassAttributes(event.getValue(), backgroundComboBox);
        });
        backgroundBonusesLabel = new TextField(BACKGROUND + " Bonuses");

        backgroundComboBox.setEnabled(false);
        backgroundComboBox.addValueChangeListener(event -> updateBackgroundAttributes(event.getValue()));
        classBonusesLabel = new TextField(CLASS + " Bonuses");

        HorizontalLayout archetypeLayout = createComboBoxLayout(ARCHETYPE, archetypeComboBox, archetypeBonusesLabel);
        HorizontalLayout classLayout = createComboBoxLayout(CLASS, classComboBox, classBonusesLabel);
        HorizontalLayout backgroundLayout = createComboBoxLayout(BACKGROUND, backgroundComboBox, backgroundBonusesLabel);

        add(accuracyBox, damageBox, speedBox, masteryBox, archetypeLayout, classLayout, backgroundLayout, pointsLayout);

        TextField textField = new TextField("Your character's name");
        textField.addClassName("bordered");

        Button button = new Button("Save Character", e -> saveCharacter(textField.getValue()));
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickShortcut(Key.ENTER);

        characterList.setWidth("100%");

        add(textField, button, characterList);

        addClassName("centered-content");

        loadAllCharacters();
    }

    public void saveCharacter(String characterName) {
        character.setCharacterName(characterName);
        JsonObject characterData = Json.createObject();
        characterData.put("name", character.getCharacterName());
        characterData.put(ARCHETYPE, character.getArchetype().getName());
        characterData.put(CLASS, character.getCharClass().getName());
        characterData.put(BACKGROUND, character.getBackground().getName());
        characterData.put(ACCURACY, character.getSpentPoints().getAccuracy());
        characterData.put(DAMAGE, character.getSpentPoints().getDamage());
        characterData.put(SPEED, character.getSpentPoints().getSpeed());
        characterData.put(MASTERY, character.getSpentPoints().getMastery());

        Page page = getUI().get().getPage();
        page.executeJs("localStorage.setItem($0, JSON.stringify($1));", CHAR_PREFIX + characterName, characterData);

        characterList.add(new Button(characterName, e -> loadCharacter(characterName)));
    }

    public void loadCharacter(String characterName) {
        Page page = getUI().get().getPage();
        page.executeJs("return JSON.parse(localStorage.getItem($0));", CHAR_PREFIX + characterName)
                .then(jsonValue -> {
                    JsonObject characterData = ((JreJsonObject) jsonValue);
                    character.reset();

                    updateArchetypeAttributes(characterData.getString(ARCHETYPE));
                    updateClassAttributes(characterData.getString(CLASS), backgroundComboBox);

                    int spentAccuracy = (int) characterData.getNumber(ACCURACY);
                    int spentDamage = (int) characterData.getNumber(DAMAGE);
                    int spentSpeed = (int) characterData.getNumber(SPEED);
                    int spentMastery = (int) characterData.getNumber(MASTERY);
                    int pointsToSpend = new Character().getAvailablePointsToSpend() - spentAccuracy - spentDamage - spentSpeed - spentMastery;

                    character.setAvailablePointsToSpend(pointsToSpend);

                    character.setSpentPoints(Character.Stats.builder()
                            .accuracy(spentAccuracy)
                            .damage(spentDamage)
                            .speed(spentSpeed)
                            .mastery(spentMastery)
                            .build());

                    archetypeComboBox.setValue(character.getArchetype().getName());
                    classComboBox.setValue(character.getCharClass().getName());

                    updateBackgroundAttributes(characterData.getString(BACKGROUND));
                    backgroundComboBox.setValue(character.getBackground().getName());

                    updateSpentPoints();
                    updateAllBoxes();
                });
    }

    public void loadAllCharacters() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            Page page = ui.getPage();
            page.executeJs("return Object.keys(localStorage).filter(key => key.startsWith('" + CHAR_PREFIX + "'));")
                    .then(jsonValue -> {
                        JsonArray characters = ((JreJsonArray) jsonValue);
                        for (int i = 0; i < characters.length(); i++) {
                            String characterName = characters.getString(i).split(CHAR_PREFIX)[1];
                            characterList.add(new Button(characterName, e -> loadCharacter(characterName)));
                        }
                    });
        }
    }

    public void updateSpentPoints() {
        pointsField.setValue(String.valueOf(character.getAvailablePointsToSpend()));
        spentPointsLabel.setValue(this.formatBonuses(
                character.getSpentPoints().getAccuracy(),
                character.getSpentPoints().getDamage(),
                character.getSpentPoints().getSpeed(),
                character.getSpentPoints().getMastery())
        );
    }

    @NotNull
    private HorizontalLayout createComboBoxLayout(String label, @NotNull ComboBox<String> comboBox, @NotNull TextField textField) {
        comboBox.setLabel(label);
        textField.setReadOnly(true);
        HorizontalLayout layout = new HorizontalLayout(comboBox, textField);
        layout.setWidth("100%");
        layout.setAlignItems(Alignment.CENTER);
        comboBox.setWidth("50%");
        textField.setWidth("50%");

        addComboBoxListeners(comboBox);

        return layout;
    }

    private void updateArchetypeAttributes(String archetypeName) {
        if (archetypeName != null && loader.getArchetypes().containsKey(archetypeName)) {
            Map<String, Integer> bonuses = loader.getArchetypeAttributes(archetypeName);

            character.setArchetype(Character.Stats.builder()
                    .name(archetypeName)
                    .accuracy(bonuses.get(ACCURACY))
                    .damage(bonuses.get(DAMAGE))
                    .speed(bonuses.get(SPEED))
                    .mastery(bonuses.get(MASTERY))
                    .build());

            archetypeBonusesLabel.setValue(this.formatBonuses(
                    character.getArchetype().getAccuracy(),
                    character.getArchetype().getDamage(),
                    character.getArchetype().getSpeed(),
                    character.getArchetype().getMastery()));
        } else {
            archetypeBonusesLabel.clear();
            archetypeComboBox.clear();
        }
    }

    private void updateClassAttributes(String className, ComboBox<String> backgroundCombobox) {
        if (className != null && loader.getClasses().containsKey(className)) {
            CharacterResource characterResource = loader.getClasses().get(className);
            Map<String, Integer> bonuses = characterResource.getAttributes();

            character.setCharClass(Character.Stats.builder()
                    .name(className)
                    .accuracy(bonuses.get(ACCURACY))
                    .damage(bonuses.get(DAMAGE))
                    .speed(bonuses.get(SPEED))
                    .mastery(bonuses.get(MASTERY))
                    .build());

            classBonusesLabel.setValue(this.formatBonuses(
                    character.getCharClass().getAccuracy(),
                    character.getCharClass().getDamage(),
                    character.getCharClass().getSpeed(),
                    character.getCharClass().getMastery()));

            backgroundCombobox.setItems(characterResource.getBackgrounds().keySet());
            backgroundCombobox.setEnabled(true);
        } else {
            classBonusesLabel.clear();
            classComboBox.clear();
        }
    }

    private void updateBackgroundAttributes(String backgroundName) {
        if (backgroundName != null) {
            String className = character.getCharClass().getName();
            if (className != null && loader.getClasses().containsKey(className)) {
                CharacterResource characterResource = loader.getClasses().get(className);
                CharacterResource.Background loadedBackground = characterResource.getBackgrounds().get(backgroundName);
                if (loadedBackground == null) {
                    backgroundBonusesLabel.setValue("");
                    return;
                }
                Map<String, Integer> bonuses = loadedBackground.getAttributes();

                character.setBackground(Character.Stats.builder()
                        .name(backgroundName)
                        .accuracy(bonuses.get(ACCURACY))
                        .damage(bonuses.get(DAMAGE))
                        .speed(bonuses.get(SPEED))
                        .mastery(bonuses.get(MASTERY))
                        .build());

                backgroundBonusesLabel.setValue(this.formatBonuses(
                        character.getBackground().getAccuracy(),
                        character.getBackground().getDamage(),
                        character.getBackground().getSpeed(),
                        character.getBackground().getMastery()));
            }
        } else {
            backgroundBonusesLabel.clear();
            backgroundComboBox.clear();
        }
    }

    @NotNull
    @Contract(pure = true)
    private String formatBonuses(int accuracy, int damage, int speed, int mastery) {
        return "ACC +" + accuracy + ", DMG +" + damage + ", SPD +" + speed + ", MST +" + mastery;
    }


    private void addComboBoxListeners(ComboBox<String> comboBox) {
        comboBox.addValueChangeListener(event -> updateAllBoxes());
    }

    private void updateAllBoxes() {
        accuracyBox.updateFields();
        damageBox.updateFields();
        speedBox.updateFields();
        masteryBox.updateFields();
    }

}
