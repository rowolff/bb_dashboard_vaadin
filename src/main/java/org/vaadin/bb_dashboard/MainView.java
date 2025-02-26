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

import static org.vaadin.bb_dashboard.Constants.*;

@Route
public class MainView extends VerticalLayout {

    private int totalPoints = 3;

    private final AttributeComponent accuracyBox;
    private final AttributeComponent damageBox;
    private final AttributeComponent speedBox;
    private final AttributeComponent masteryBox;

    private final TextField archetypeBonusesLabel;
    private final TextField classBonusesLabel;
    private final TextField backgroundBonusesLabel;
    private final TextField pointsField;
    private final TextField spentPointsLabel;

    private final ComboBox<String> archetypeComboBox = new ComboBox<>();
    private final ComboBox<String> classComboBox = new ComboBox<>();
    private final ComboBox<String> backgroundComboBox = new ComboBox<>();
    private final HorizontalLayout characterList = new HorizontalLayout();

    public MainView(CharacterService characterService) {

        accuracyBox = new AttributeComponent(ACCURACY, "ACC", this);
        accuracyBox.setAlignItems(Alignment.BASELINE);
        damageBox = new AttributeComponent(DAMAGE, "DMG", this);
        damageBox.setAlignItems(Alignment.BASELINE);
        speedBox = new AttributeComponent(SPEED, "SPD", this);
        speedBox.setAlignItems(Alignment.BASELINE);
        masteryBox = new AttributeComponent(MASTERY, "MST", this);
        masteryBox.setAlignItems(Alignment.BASELINE);

        pointsField = new TextField("Remaining Points");
        pointsField.setValue(String.valueOf(totalPoints));
        pointsField.setReadOnly(true);
        spentPointsLabel = new TextField("Spent Points");
        spentPointsLabel.setReadOnly(true);
        HorizontalLayout pointsLayout = new HorizontalLayout(pointsField, spentPointsLabel);
        pointsLayout.setWidth("100%");
        pointsLayout.setAlignItems(Alignment.CENTER);
        pointsField.setWidth("50%");
        spentPointsLabel.setWidth("50%");

        archetypeComboBox.setItems(characterService.getArchetypes().keySet());
        archetypeComboBox.addValueChangeListener(event -> updateArchetypeAttributes(event.getValue(), characterService));
        archetypeBonusesLabel = new TextField(ARCHETYPE + " Bonuses");

        classComboBox.setItems(characterService.getClasses().keySet());
        classComboBox.addValueChangeListener(event -> updateClassAttributes(event.getValue(), backgroundComboBox, characterService));
        backgroundBonusesLabel = new TextField(BACKGROUND + " Bonuses");

        backgroundComboBox.setEnabled(false);
        backgroundComboBox.addValueChangeListener(event -> updateBackgroundAttributes(event.getValue(), classComboBox, characterService));
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

    public boolean canSpendPoint() {
        return totalPoints > 0;
    }

    public void spendPoint() {
        totalPoints--;
        pointsField.setValue(String.valueOf(totalPoints));
        updateSpentPoints();
    }

    public void refundPoint() {
        totalPoints++;
        pointsField.setValue(String.valueOf(totalPoints));
        updateSpentPoints();
    }

    public void saveCharacter(String characterName) {
        JsonObject characterData = Json.createObject();
        characterData.put("name", characterName);
        characterData.put(ARCHETYPE, archetypeComboBox.getValue());
        characterData.put(CLASS, classComboBox.getValue());
        characterData.put(BACKGROUND, backgroundComboBox.getValue());
        characterData.put(ACCURACY, accuracyBox.getSpentPoints());
        characterData.put(DAMAGE, damageBox.getSpentPoints());
        characterData.put(SPEED, speedBox.getSpentPoints());
        characterData.put(MASTERY, masteryBox.getSpentPoints());

        Page page = getUI().get().getPage();
        page.executeJs("localStorage.setItem($0, JSON.stringify($1));", CHAR_PREFIX + characterName, characterData);

        characterList.add(new Button(characterName, e -> loadCharacter(characterName)));
    }

    public void loadCharacter(String characterName) {
        Page page = getUI().get().getPage();
        page.executeJs("return JSON.parse(localStorage.getItem($0));", CHAR_PREFIX + characterName)
                .then(jsonValue -> {
                    JsonObject characterData = ((JreJsonObject) jsonValue);
                    archetypeComboBox.setValue(characterData.getString(ARCHETYPE));
                    classComboBox.setValue(characterData.getString(CLASS));
                    backgroundComboBox.setValue(characterData.getString(BACKGROUND));

                    int spentAccuracy = (int) characterData.getNumber(ACCURACY);
                    int spentDamage = (int) characterData.getNumber(DAMAGE);
                    int spentSpeed = (int) characterData.getNumber(SPEED);
                    int spentMastery = (int) characterData.getNumber(MASTERY);
                    totalPoints = 3 - spentAccuracy - spentDamage - spentSpeed - spentMastery;

                    accuracyBox.setSpentPoints(spentAccuracy);
                    damageBox.setSpentPoints(spentDamage);
                    speedBox.setSpentPoints(spentSpeed);
                    masteryBox.setSpentPoints(spentMastery);

                    updateSpentPoints();
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

    @NotNull
    private HorizontalLayout createComboBoxLayout(String label, @NotNull ComboBox<String> comboBox, @NotNull TextField textField) {
        comboBox.setLabel(label);
        textField.setReadOnly(true);
        HorizontalLayout layout = new HorizontalLayout(comboBox, textField);
        layout.setWidth("100%");
        layout.setAlignItems(Alignment.CENTER);
        comboBox.setWidth("50%");
        textField.setWidth("50%");
        return layout;
    }

    private void updateArchetypeAttributes(String archetype, CharacterService characterService) {
        if (archetype != null && characterService.getArchetypes().containsKey(archetype)) {
            Map<String, Integer> bonuses = characterService.getArchetypeAttributes(archetype);
            accuracyBox.setArchetypeBonus(bonuses.get(ACCURACY));
            damageBox.setArchetypeBonus(bonuses.get(DAMAGE));
            speedBox.setArchetypeBonus(bonuses.get(SPEED));
            masteryBox.setArchetypeBonus(bonuses.get(MASTERY));
            archetypeBonusesLabel.setValue(this.formatBonuses(bonuses.get(ACCURACY), bonuses.get(DAMAGE), bonuses.get(SPEED), bonuses.get(MASTERY)));
        } else {
            archetypeBonusesLabel.setValue("");
        }
    }

    private void updateClassAttributes(String className, ComboBox<String> backgroundCombobox, CharacterService characterService) {
        accuracyBox.setBackgroundBonus(0);
        damageBox.setBackgroundBonus(0);
        speedBox.setBackgroundBonus(0);
        masteryBox.setBackgroundBonus(0);
        if (className != null && characterService.getClasses().containsKey(className)) {
            CharacterClass characterClass = characterService.getClasses().get(className);
            Map<String, Integer> bonuses = characterClass.getAttributes();
            accuracyBox.setClassBonus(bonuses.get(ACCURACY));
            damageBox.setClassBonus(bonuses.get(DAMAGE));
            speedBox.setClassBonus(bonuses.get(SPEED));
            masteryBox.setClassBonus(bonuses.get(MASTERY));
            classBonusesLabel.setValue(this.formatBonuses(bonuses.get(ACCURACY), bonuses.get(DAMAGE), bonuses.get(SPEED), bonuses.get(MASTERY)));

            backgroundCombobox.setItems(characterClass.getBackgrounds().keySet());
            backgroundCombobox.setEnabled(true);
        } else {
            classBonusesLabel.setValue("");
        }
    }

    private void updateBackgroundAttributes(String backgroundName, ComboBox<String> classCombobox, CharacterService characterService) {
        if (backgroundName != null) {
            String className = classCombobox.getValue();
            if (className != null && characterService.getClasses().containsKey(className)) {
                CharacterClass characterClass = characterService.getClasses().get(className);
                CharacterClass.Background background = characterClass.getBackgrounds().get(backgroundName);
                Map<String, Integer> bonuses = background.getAttributes();
                accuracyBox.setBackgroundBonus(bonuses.get(ACCURACY));
                damageBox.setBackgroundBonus(bonuses.get(DAMAGE));
                speedBox.setBackgroundBonus(bonuses.get(SPEED));
                masteryBox.setBackgroundBonus(bonuses.get(MASTERY));
                backgroundBonusesLabel.setValue(this.formatBonuses(bonuses.get(ACCURACY), bonuses.get(DAMAGE), bonuses.get(SPEED), bonuses.get(MASTERY)));
            }
        } else {
            backgroundBonusesLabel.setValue("");
        }
    }

    private void updateSpentPoints() {
        spentPointsLabel.setValue(this.formatBonuses(accuracyBox.getSpentPoints(), damageBox.getSpentPoints(), speedBox.getSpentPoints(), masteryBox.getSpentPoints()));
        pointsField.setValue(String.valueOf(totalPoints));
    }

    @NotNull
    @Contract(pure = true)
    private String formatBonuses(int accuracy, int damage, int speed, int mastery) {
        return "ACC +" + accuracy + ", DMG +" + damage + ", SPD +" + speed + ", MST +" + mastery;
    }

}
