package org.vaadin.bb_dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Route
public class MainView extends VerticalLayout {

    public static final String ACCURACY = "Accuracy";
    public static final String DAMAGE = "Damage";
    public static final String SPEED = "Speed";
    public static final String MASTERY = "Mastery";
    private int totalPoints = 3;
    private Map<String, Map<String, Integer>> archetypes;
    private final AtomicReference<Map<String, CharacterClass>> classes = new AtomicReference<>();

    private final TextField pointsField;
    private final AttributeComponent accuracyBox;
    private final AttributeComponent damageBox;
    private final AttributeComponent speedBox;
    private final AttributeComponent masteryBox;
    private final TextField archetypeBonusesLabel;
    private final TextField classBonusesLabel;
    private final TextField backgroundBonusesLabel;
    private final TextField spentPointsLabel;

    public MainView(GreetService service) {
        loadCharacterData();

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

        archetypeComboBox.setItems(archetypes.keySet());
        archetypeComboBox.addValueChangeListener(event -> updateArchetypeAttributes(event.getValue()));

        classComboBox.setItems(classes.get().keySet());
        classComboBox.addValueChangeListener(event -> updateClassAttributes(event.getValue(), backgroundComboBox));

        backgroundComboBox.setEnabled(false);
        backgroundComboBox.addValueChangeListener(event -> updateBackgroundAttributes(event.getValue(), classComboBox));

        HorizontalLayout archetypeLayout = createComboBoxLayout("Archetype", archetypeComboBox, archetypeBonusesLabel);
        HorizontalLayout classLayout = createComboBoxLayout("Class", classComboBox, classBonusesLabel);
        HorizontalLayout backgroundLayout = createComboBoxLayout("Background", backgroundComboBox, backgroundBonusesLabel);

        add(accuracyBox, damageBox, speedBox, masteryBox, archetypeLayout, classLayout, backgroundLayout, pointsLayout);

        TextField textField = new TextField("Your name");
        textField.addClassName("bordered");

        Button button = new Button("Say hello", e -> add(new Paragraph(service.greet(textField.getValue()))));
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

    private void loadCharacterData() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getResourceAsStream("/archetypes.json")) {
            archetypes = mapper.readValue(is, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (InputStream is = getClass().getResourceAsStream("/classes.json")) {
            Map<String, Map<String, Object>> rawClasses = mapper.readValue(is, Map.class);
            classes.set(new HashMap<>());
            for (Map.Entry<String, Map<String, Object>> entry : rawClasses.entrySet()) {
                String className = entry.getKey();
                Map<String, Integer> attributes = (Map<String, Integer>) entry.getValue().get("attributes");
                Map<String, CharacterClass.Background> backgrounds = new HashMap<>();
                Map<String, Map<String, Integer>> rawBackgrounds = (Map<String, Map<String, Integer>>) entry.getValue().get("backgrounds");
                for (Map.Entry<String, Map<String, Integer>> bgEntry : rawBackgrounds.entrySet()) {
                    backgrounds.put(bgEntry.getKey(), new CharacterClassImpl.BackgroundImpl(bgEntry.getKey(), bgEntry.getValue()));
                }
                classes.get().put(className, new CharacterClassImpl(className, attributes, backgrounds));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateArchetypeAttributes(String archetype) {
        if (archetype != null && archetypes.containsKey(archetype)) {
            Map<String, Integer> bonuses = archetypes.get(archetype);
            accuracyBox.setArchetypeBonus(bonuses.get(ACCURACY));
            damageBox.setArchetypeBonus(bonuses.get(DAMAGE));
            speedBox.setArchetypeBonus(bonuses.get(SPEED));
            masteryBox.setArchetypeBonus(bonuses.get(MASTERY));
            archetypeBonusesLabel.setValue(this.formatBonuses(bonuses.get(ACCURACY), bonuses.get(DAMAGE), bonuses.get(SPEED), bonuses.get(MASTERY)));
        } else {
            archetypeBonusesLabel.setValue("");
        }
    }

    private void updateClassAttributes(String className, ComboBox<String> backgroundCombobox) {
        accuracyBox.setBackgroundBonus(0);
        damageBox.setBackgroundBonus(0);
        speedBox.setBackgroundBonus(0);
        masteryBox.setBackgroundBonus(0);
        if (className != null && classes.get().containsKey(className)) {
            CharacterClass characterClass = classes.get().get(className);
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

    private void updateBackgroundAttributes(String backgroundName, ComboBox<String> classCombobox) {
        if (backgroundName != null) {
            String className = classCombobox.getValue();
            if (className != null && classes.get().containsKey(className)) {
                CharacterClass characterClass = classes.get().get(className);
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
