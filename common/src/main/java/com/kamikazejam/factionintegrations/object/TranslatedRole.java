package com.kamikazejam.factionintegrations.object;

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
    private TranslatedRole(int value){
        this.value = value;
    }

    public static TranslatedRole getByValue(int value){
        switch (value){
            case 0:
                return ALT;
            case 1:
                return RECRUIT;
            case 2:
                return MEMBER;
            case 3:
                return OFFICER;
            case 4:
                return CO_LEADER;
            case 5:
                return ADMIN;
        }
        return null;
    }

    public int getValue(){
        return this.value;
    }

    public boolean isGreaterThan(TranslatedRole translatedRole){
        return this.value > translatedRole.getValue();
    }

    public boolean isEqualTo(TranslatedRole translatedRole){
        return this.value == translatedRole.getValue();
    }

    public boolean isLessThan(TranslatedRole translatedRole){
        return this.value < translatedRole.getValue();
    }

    public boolean isGreaterThanOrEqualTo(TranslatedRole translatedRole){
        return this.value >= translatedRole.value;
    }

    public boolean isLessThanOrEqualTo(TranslatedRole translatedRole){
        return this.value <= translatedRole.value;
    }
}
