package org.vaadin.bb_dashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import org.jetbrains.annotations.NotNull;
import org.vaadin.bb_dashboard.character.Character;

public class AttributeComponent extends HorizontalLayout {

    private final TextField valueField;
    private final TextField modifierField;
    private final Character character;
    private final String attributeName;

    public AttributeComponent(String attributeName, String shortName, @NotNull Character character, MainView mainView) {

        this.character = character;
        this.attributeName = attributeName;

        TextField nameField = new TextField("Name");
        nameField.setValue(String.format("%s (%s)", attributeName, shortName));
        nameField.setReadOnly(true);

        valueField = new TextField("Value");
        valueField.setValue(String.valueOf(character.getTotalStatByName(attributeName)));
        valueField.setReadOnly(true);

        modifierField = new TextField("Modifier");
        modifierField.setValue(String.valueOf(character.getModifierByName(attributeName)));
        modifierField.setReadOnly(true);

        Button incrementButton = new Button("+", e -> {
            character.spendPoint(attributeName);
            mainView.updateSpentPoints();
            updateFields();
        });
        Button decrementButton = new Button("-", e -> {
            character.refundPoint(attributeName);
            mainView.updateSpentPoints();
            updateFields();
        });

        add(nameField, valueField, modifierField, incrementButton, decrementButton);
    }

    public void updateFields() {
        valueField.setValue(String.valueOf(character.getTotalStatByName(attributeName)));
        modifierField.setValue(String.valueOf(character.getModifierByName(attributeName)));
    }
}
