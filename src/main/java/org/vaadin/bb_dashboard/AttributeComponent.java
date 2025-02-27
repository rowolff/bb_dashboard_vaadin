package org.vaadin.bb_dashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import org.vaadin.bb_dashboard.character.Character;

public class AttributeComponent extends HorizontalLayout {

    private final TextField valueField;
    private final TextField modifierField;
    private final Character character;
    private final String attributeName;

    public AttributeComponent(String attributeName, String shortName, Character character, MainView mainView) {

        this.character = character;
        this.attributeName = attributeName;

        int totalStat = character.getTotalStatByName(attributeName);
        int modifier = totalStat / 2;

        TextField nameField = new TextField("Name");
        nameField.setValue(attributeName + " (" + shortName + ")");
        nameField.setReadOnly(true);

        valueField = new TextField("Value");
        valueField.setValue(String.valueOf(totalStat));
        valueField.setReadOnly(true);

        modifierField = new TextField("Modifier");
        modifierField.setValue(String.valueOf(modifier));
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
        int totalStat = character.getTotalStatByName(attributeName);
        int updatedModifier = totalStat / 2;
        valueField.setValue(String.valueOf(totalStat));
        modifierField.setValue(String.valueOf(updatedModifier));
    }
}
