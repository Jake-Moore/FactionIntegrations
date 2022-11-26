package com.kamikazejamplugins.factionintegrations.currency;

public interface Currency {

    double getBalance(String id);

    void addBalance(String id, double add);

    void subtractBalance(String id, double remove);
}
