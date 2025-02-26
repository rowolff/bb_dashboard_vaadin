package org.vaadin.bb_dashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class AttributeComponent extends HorizontalLayout {

    private final TextField valueField;
    private final TextField modifierField;
    private final MainView mainView;
    private int spentPoints = 0;
    private int archetypeBonus = 0;
    private int classBonus = 0;
    private int backgroundBonus = 0;

    public AttributeComponent(String name, String shortName, MainView mainView) {
        this.mainView = mainView;

        TextField nameField = new TextField("Name");
        nameField.setValue(name + " (" + shortName + ")");
        nameField.setReadOnly(true);

        valueField = new TextField("Value");
        valueField.setValue("0");
        valueField.setReadOnly(true);
        valueField.addValueChangeListener(e -> updateModifier());

        modifierField = new TextField("Modifier");
        modifierField.setValue("0");
        modifierField.setReadOnly(true);

        Button incrementButton = new Button("+", e -> incrementValue());
        Button decrementButton = new Button("-", e -> decrementValue());

        add(nameField, valueField, modifierField, incrementButton, decrementButton);
    }

    public void setArchetypeBonus(int archetypeBonus) {
        this.archetypeBonus = archetypeBonus;
        updateValue();
    }

    public void setClassBonus(int classBonus) {
        this.classBonus = classBonus;
        updateValue();
    }

    public void setBackgroundBonus(int backgroundBonus) {
        this.backgroundBonus = backgroundBonus;
        updateValue();
    }

    private void incrementValue() {
        try {
            if (mainView.canSpendPoint()) {
                this.spentPoints++;
                valueField.setValue(String.valueOf(getTotal()));
                mainView.spendPoint();
                updateModifier();
            }
        } catch (NumberFormatException e) {
            valueField.setValue("0");
            modifierField.setValue("0");
        }
    }

    private void decrementValue() {
        try {
            if (this.spentPoints > 0) {
                this.spentPoints--;
                valueField.setValue(String.valueOf(getTotal()));
                mainView.refundPoint();
                updateModifier();
            }
        } catch (NumberFormatException e) {
            valueField.setValue("0");
            modifierField.setValue("0");
        }
    }

    private void updateValue() {
        try {
            valueField.setValue(String.valueOf(getTotal()));
            updateModifier();
        } catch (NumberFormatException e) {
            valueField.setValue(String.valueOf(archetypeBonus));
            modifierField.setValue("0");
        }
    }

    private void updateModifier() {
        try {
            int modifier = getTotal() / 2;
            modifierField.setValue(String.valueOf(modifier));
        } catch (NumberFormatException e) {
            modifierField.setValue("0");
        }
    }

    private int getTotal() {
        return spentPoints + archetypeBonus + classBonus + backgroundBonus;
    }

    public int getSpentPoints() {
        return this.spentPoints;
    }

    public String getModifier() {
        return modifierField.getValue();
    }
}
