package org.vaadin.bb_dashboard.character;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

import static org.vaadin.bb_dashboard.Constants.*;

@Data
public class Character implements Serializable {

    private int availablePointsToSpend = MAX_SPENDABLE_POINTS;

    private String characterName = "";

    private Stats archetype = new Stats("", 0, 0, 0, 0);
    private Stats charClass = new Stats("", 0, 0, 0, 0);
    private Stats background = new Stats("", 0, 0, 0, 0);
    private Stats spentPoints = new Stats("", 0, 0, 0, 0);

    @Data
    @Builder
    public static class Stats implements Serializable {
        private String name;
        private int accuracy;
        private int damage;
        private int speed;
        private int mastery;
    }

    public Stats getTotalStats() {
        Stats totalStats = new Stats("Total", 0, 0, 0, 0);
        totalStats.accuracy = archetype.accuracy + charClass.accuracy + background.accuracy + spentPoints.accuracy;
        totalStats.damage = archetype.damage + charClass.damage + background.damage + spentPoints.damage;
        totalStats.speed = archetype.speed + charClass.speed + background.speed + spentPoints.speed;
        totalStats.mastery = archetype.mastery + charClass.mastery + background.mastery + spentPoints.mastery;
        return totalStats;
    }

    public int getTotalStatByName(@NotNull String name) {
        return switch (name) {
            case ACCURACY -> getTotalStats().accuracy;
            case DAMAGE -> getTotalStats().damage;
            case SPEED -> getTotalStats().speed;
            case MASTERY -> getTotalStats().mastery;
            default -> throw new IllegalStateException(String.format(UNEXPECTED_VALUE, name));
        };
    }

    public int getModifierByName(@NotNull String name) {
        return getTotalStatByName(name) / 2;
    }

    public void spendPoint(@NotNull String statName) {
        if (availablePointsToSpend > 0) {
            switch (statName) {
                case ACCURACY:
                    spentPoints.accuracy++;
                    break;
                case DAMAGE:
                    spentPoints.damage++;
                    break;
                case SPEED:
                    spentPoints.speed++;
                    break;
                case MASTERY:
                    spentPoints.mastery++;
                    break;
                default:
                    throw new IllegalStateException(String.format(UNEXPECTED_VALUE, statName));
            }
            availablePointsToSpend--;
        }
    }

    public void refundPoint(@NotNull String statName) {
        boolean statPositive = this.getTotalStatByName(statName) > 0;
        boolean canRefund = availablePointsToSpend < MAX_SPENDABLE_POINTS;
        if (statPositive && canRefund) {
            switch (statName) {
                case ACCURACY:
                    spentPoints.accuracy--;
                    break;
                case DAMAGE:
                    spentPoints.damage--;
                    break;
                case SPEED:
                    spentPoints.speed--;
                    break;
                case MASTERY:
                    spentPoints.mastery--;
                    break;
                default:
                    throw new IllegalStateException(String.format(UNEXPECTED_VALUE, statName));
            }
            availablePointsToSpend++;
        }
    }

    public void reset() {
        availablePointsToSpend = MAX_SPENDABLE_POINTS;
        spentPoints = new Stats("", 0, 0, 0, 0);
        archetype = new Stats("", 0, 0, 0, 0);
        charClass = new Stats("", 0, 0, 0, 0);
        background = new Stats("", 0, 0, 0, 0);
    }
}
