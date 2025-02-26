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

/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and use @Route
 * annotation to announce it in a URL as a Spring managed bean.
 * <p>
 * A new instance of this class is created for every new user and every browser
 * tab/window.
 * <p>
 * The main view contains a text field for getting the user name and a button
 * that shows a greeting message in a notification.
 */
@Route
public class MainView extends VerticalLayout {

    /**
     * Construct a new Vaadin view.
     * <p>
     * Build the initial UI state for the user accessing the application.
     *
     * @param service
     *            The message service. Automatically injected Spring managed bean.
     */

    private int totalPoints = 3;
    private Map<String, Map<String, Integer>> archetypes;
    private Map<String, CharacterClass> classes;

    private final TextField pointsField;
    private final AttributeComponent accuracy;
    private final AttributeComponent damage;
    private final AttributeComponent speed;
    private final AttributeComponent mastery;
    private final TextField archetypeBonusesLabel;
    private final TextField classBonusesLabel;
    private final TextField spentPointsLabel;

    public MainView(GreetService service) {
        loadCharacterData();

        accuracy = new AttributeComponent("Accuracy", "ACC", this);
        accuracy.setAlignItems(Alignment.BASELINE);
        damage = new AttributeComponent("Damage", "DMG", this);
        damage.setAlignItems(Alignment.BASELINE);
        speed = new AttributeComponent("Speed", "SPD", this);
        speed.setAlignItems(Alignment.BASELINE);
        mastery = new AttributeComponent("Mastery", "MST", this);
        mastery.setAlignItems(Alignment.BASELINE);

        pointsField = new TextField("Remaining Points");
        pointsField.setValue(String.valueOf(totalPoints));
        pointsField.setReadOnly(true);

        ComboBox<String> archetypeComboBox = new ComboBox<>("Archetype");
        archetypeComboBox.setItems(archetypes.keySet());
        archetypeComboBox.addValueChangeListener(event -> updateArchetypeAttributes(event.getValue()));

        spentPointsLabel = new TextField("Spent Points");
        spentPointsLabel.setReadOnly(true);
        HorizontalLayout pointsLayout = new HorizontalLayout(pointsField, spentPointsLabel);
        pointsLayout.setWidth("100%");
        pointsLayout.setAlignItems(Alignment.CENTER);
        pointsField.setWidth("50%");
        spentPointsLabel.setWidth("50%");

        archetypeBonusesLabel = new TextField("Archetype Bonuses");
        archetypeBonusesLabel.setReadOnly(true);
        HorizontalLayout archetypeLayout = new HorizontalLayout(archetypeComboBox, archetypeBonusesLabel);
        archetypeLayout.setWidth("100%");
        archetypeLayout.setAlignItems(Alignment.CENTER);
        archetypeComboBox.setWidth("50%");
        archetypeBonusesLabel.setWidth("50%");

        ComboBox<String> classCombobox = new ComboBox<>("Class");
        classCombobox.setItems(classes.keySet());
        classCombobox.addValueChangeListener(event -> updateClassAttributes(event.getValue()));

        classBonusesLabel = new TextField("Class Bonuses");
        classBonusesLabel.setReadOnly(true);
        HorizontalLayout classLayout = new HorizontalLayout(classCombobox, classBonusesLabel);
        classLayout.setWidth("100%");
        classLayout.setAlignItems(Alignment.CENTER);
        classCombobox.setWidth("50%");
        classBonusesLabel.setWidth("50%");

        add(accuracy, damage, speed, mastery, pointsLayout, archetypeLayout, classLayout);

        // Use TextField for standard text input
        TextField textField = new TextField("Your name");
        textField.addClassName("bordered");

        // Button click listeners can be defined as lambda expressions
        Button button = new Button("Say hello", e -> {
            add(new Paragraph(service.greet(textField.getValue())));
        });

        // Theme variants give you predefined extra styles for components.
        // Example: Primary button has a more prominent look.
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // You can specify keyboard shortcuts for buttons.
        // Example: Pressing enter in this view clicks the Button.
        button.addClickShortcut(Key.ENTER);

        // Use custom CSS classes to apply styling. This is defined in
        // styles.css.
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
                Map<String, CharacterClass.Background> backgrounds = new HashMap<>();
                Map<String, Map<String, Integer>> rawBackgrounds = (Map<String, Map<String, Integer>>) entry.getValue().get("backgrounds");
                for (Map.Entry<String, Map<String, Integer>> bgEntry : rawBackgrounds.entrySet()) {
                    backgrounds.put(bgEntry.getKey(), new CharacterClassImpl.BackgroundImpl(bgEntry.getKey(), bgEntry.getValue()));
                }
                classes.put(className, new CharacterClassImpl(className, attributes, backgrounds));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateArchetypeAttributes(String archetype) {
        if (archetype != null && archetypes.containsKey(archetype)) {
            Map<String, Integer> bonuses = archetypes.get(archetype);
            accuracy.setArchetypeBonus(bonuses.get("Accuracy"));
            damage.setArchetypeBonus(bonuses.get("Damage"));
            speed.setArchetypeBonus(bonuses.get("Speed"));
            mastery.setArchetypeBonus(bonuses.get("Mastery"));
            archetypeBonusesLabel.setValue(this.formatBonuses(bonuses.get("Accuracy"), bonuses.get("Damage"), bonuses.get("Speed"), bonuses.get("Mastery")));
        } else {
            archetypeBonusesLabel.setValue("");
        }
    }

    private void updateClassAttributes(String className) {
        if (className != null && classes.containsKey(className)) {
            CharacterClass characterClass = classes.get(className);
            Map<String, Integer> bonuses = characterClass.getAttributes();
            accuracy.setClassBonus(bonuses.get("Accuracy"));
            damage.setClassBonus(bonuses.get("Damage"));
            speed.setClassBonus(bonuses.get("Speed"));
            mastery.setClassBonus(bonuses.get("Mastery"));
            classBonusesLabel.setValue(this.formatBonuses(bonuses.get("Accuracy"), bonuses.get("Damage"), bonuses.get("Speed"), bonuses.get("Mastery")));
        } else {
            classBonusesLabel.setValue("");
        }
    }

    private void updateSpentPoints() {
        spentPointsLabel.setValue(this.formatBonuses(accuracy.getSpentPoints(), damage.getSpentPoints(), speed.getSpentPoints(), mastery.getSpentPoints()));
    }

    private String formatBonuses(int accuracy, int damage, int speed, int mastery) {
        return "ACC +" + accuracy + ", DMG +" + damage + ", SPD +" + speed + ", MST +" + mastery;
    }

}
