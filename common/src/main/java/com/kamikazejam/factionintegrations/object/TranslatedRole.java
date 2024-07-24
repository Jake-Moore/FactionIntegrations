package com.kamikazejam.factionintegrations.object;

import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public enum TranslatedRole {

    ADMIN(5),
    LEADER(5),
    COLEADER(4),
    CO_LEADER(4),
    MODERATOR(3),
    MOD(3),
    OFFICER(3),
    NORMAL(2),
    MEMBER(2),
    RECRUIT(1),
    ALT(0);

    private final int value;

    TranslatedRole(int value) {
        this.value = value;
    }

    public static TranslatedRole getByValue(int value) {
        return switch (value) {
            case 0 -> ALT;
            case 1 -> RECRUIT;
            case 2 -> MEMBER;
            case 3 -> OFFICER;
            case 4 -> CO_LEADER;
            case 5 -> ADMIN;
            default -> null;
        };
    }

    public boolean isGreaterThan(TranslatedRole translatedRole) {
        return this.value > translatedRole.getValue();
    }

    public boolean isEqualTo(TranslatedRole translatedRole) {
        return this.value == translatedRole.getValue();
    }

    public boolean isLessThan(TranslatedRole translatedRole) {
        return this.value < translatedRole.getValue();
    }

    public boolean isGreaterThanOrEqualTo(TranslatedRole translatedRole) {
        return this.value >= translatedRole.value;
    }

    public boolean isLessThanOrEqualTo(TranslatedRole translatedRole) {
        return this.value <= translatedRole.value;
    }
}
