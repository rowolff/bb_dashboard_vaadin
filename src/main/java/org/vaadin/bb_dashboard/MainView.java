package org.vaadin.bb_dashboard;

import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonObject;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.Map;

@Route
public class MainView extends VerticalLayout {

    public static final String ACCURACY = "Accuracy";
    public static final String DAMAGE = "Damage";
    public static final String SPEED = "Speed";
    public static final String MASTERY = "Mastery";
    private int totalPoints = 3;

    private final TextField pointsField;
    private final AttributeComponent accuracyBox;
    private final AttributeComponent damageBox;
    private final AttributeComponent speedBox;
    private final AttributeComponent masteryBox;
    private final TextField archetypeBonusesLabel;
    private final TextField classBonusesLabel;
    private final TextField backgroundBonusesLabel;
    private final TextField spentPointsLabel;

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

        archetypeBonusesLabel = new TextField("Archetype Bonuses");
        backgroundBonusesLabel = new TextField("Background Bonuses");
        classBonusesLabel = new TextField("Class Bonuses");

        ComboBox<String> archetypeComboBox = new ComboBox<>();
        ComboBox<String> classComboBox = new ComboBox<>();
        ComboBox<String> backgroundComboBox = new ComboBox<>();

        archetypeComboBox.setItems(characterService.getArchetypes().keySet());
        archetypeComboBox.addValueChangeListener(event -> updateArchetypeAttributes(event.getValue(), characterService));

        classComboBox.setItems(characterService.getClasses().keySet());
        classComboBox.addValueChangeListener(event -> updateClassAttributes(event.getValue(), backgroundComboBox, characterService));

        backgroundComboBox.setEnabled(false);
        backgroundComboBox.addValueChangeListener(event -> updateBackgroundAttributes(event.getValue(), classComboBox, characterService));

        HorizontalLayout archetypeLayout = createComboBoxLayout("Archetype", archetypeComboBox, archetypeBonusesLabel);
        HorizontalLayout classLayout = createComboBoxLayout("Class", classComboBox, classBonusesLabel);
        HorizontalLayout backgroundLayout = createComboBoxLayout("Background", backgroundComboBox, backgroundBonusesLabel);

        HorizontalLayout characterList = new HorizontalLayout();
        characterList.setWidth("100%");

        add(accuracyBox, damageBox, speedBox, masteryBox, archetypeLayout, classLayout, backgroundLayout, pointsLayout, characterList);

        TextField textField = new TextField("Your character's name");
        textField.addClassName("bordered");

        Button button = new Button("Save Character", e -> saveCharacter(textField.getValue(), archetypeComboBox, classComboBox, backgroundComboBox, characterList));
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickShortcut(Key.ENTER);

        addClassName("centered-content");
        add(textField, button);
    }

    public boolean canSpendPoint() {
        return totalPoints > 0;
    }

    public void spendPoint() {
        totalPoints--;
        pointsField.setValue(String.valueOf(totalPoints));
        this.updateSpentPoints();
    }

    public void refundPoint() {
        totalPoints++;
        pointsField.setValue(String.valueOf(totalPoints));
        this.updateSpentPoints();
    }

    public void saveCharacter(String characterName, ComboBox<String> archetypeComboBox, ComboBox<String> classComboBox, ComboBox<String> backgroundComboBox, HorizontalLayout characterList) {
        JsonObject characterData = Json.createObject();
        characterData.put("name", characterName);
        characterData.put("archetype", archetypeComboBox.getValue());
        characterData.put("class", classComboBox.getValue());
        characterData.put("background", backgroundComboBox.getValue());
        characterData.put("accuracy", accuracyBox.getSpentPoints());
        characterData.put("damage", damageBox.getSpentPoints());
        characterData.put("speed", speedBox.getSpentPoints());
        characterData.put("mastery", masteryBox.getSpentPoints());

        Page page = getUI().get().getPage();
        page.executeJs("localStorage.setItem($0, JSON.stringify($1));", "char." + characterName, characterData);

        characterList.add(new Button(characterName, e -> loadCharacter(characterName, archetypeComboBox, classComboBox, backgroundComboBox)));
    }

    public void loadCharacter(String characterName, ComboBox<String> archetypeComboBox, ComboBox<String> classComboBox, ComboBox<String> backgroundComboBox) {
        Page page = getUI().get().getPage();
        page.executeJs("return JSON.parse(localStorage.getItem($0));", "char." + characterName)
                .then(jsonValue -> {
                    JsonObject characterData = ((JreJsonObject) jsonValue);
                    archetypeComboBox.setValue(characterData.getString("archetype"));
                    classComboBox.setValue(characterData.getString("class"));
                    backgroundComboBox.setValue(characterData.getString("background"));
                    accuracyBox.setSpentPoints((int) characterData.getNumber("accuracy"));
                    damageBox.setSpentPoints((int) characterData.getNumber("damage"));
                    speedBox.setSpentPoints((int) characterData.getNumber("speed"));
                    masteryBox.setSpentPoints((int) characterData.getNumber("mastery"));
                });
    }

    private HorizontalLayout createComboBoxLayout(String label, ComboBox<String> comboBox, TextField textField) {
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
    }

    private String formatBonuses(int accuracy, int damage, int speed, int mastery) {
        return "ACC +" + accuracy + ", DMG +" + damage + ", SPD +" + speed + ", MST +" + mastery;
    }

}
