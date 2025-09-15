package com.kamikazejam.factionintegrations.integrations;

import com.kamikazejam.factionintegrations.object.TranslatedRelation;
import com.kamikazejam.factionintegrations.object.TranslatedRole;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "unused"})
public interface KFaction extends Listener {

    default void setTnT(String id, long amount) {
    }

    default long addTnT(String id, long amount) {
        return 0;
    }

    default long getTnT(String id) {
        return 0;
    }

    default long getMaxTnt(String id) {
        return 0;
    }

    default int getStrikes(String id) {
        return 0;
    }

    default boolean isUUID(String in) {
        try {
            UUID.fromString(in);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    double getBalance(String id);

    default boolean isRelationColorsEnabled() {
        return true;
    }

    void addBalance(String id, double add);

    void subtractBalance(String id, double remove);

    boolean econEnabled();

    boolean canPlayerBuildThere(Player player, Chunk chunk);

    boolean isBaseRegion(Location location, String id);

    boolean isSystemFac(String id);

    boolean playerCanBuildThere(Player player, Location location);

    boolean isWarzoneAt(Location location);

    boolean isSafezoneAt(Location location);

    boolean isSOTW();

    boolean playerCanFlyThere(Player player, Location location);

    boolean isSystemFacAt(Location location);

    boolean isLocked();

    boolean isShieldActiveNow(String id);

    int getMaxClaimWorldborder(World world);

    int getClaimsInWorld(String id, World world);

    int getMaxPlayers(String id);

    int getSize(String id);

    boolean isInFactionChest(Player player, String id);

    boolean isFaction(String id);

    void setFaction(Player player, String id);

    void setFaction(OfflinePlayer player, String id);

    void setRole(Player player, TranslatedRole translatedRole);

    void setRole(OfflinePlayer offlinePlayer, TranslatedRole translatedRole);

    void setPermission(String id, TranslatedRelation relation, String permission, boolean b);

    void setRelationWish(String id, String other, TranslatedRelation relation);

    boolean isTagAvailable(String tag);

    void setFactionPower(String id, double power);

    void clearAllClaimsInWorld(String world);

    void setFactionAt(int cx, int cz, String world, String faction);

    String createFaction(String tag);

    Set<String> getAllFactions();

    Map<String, Set<Integer[]>> getAllClaims(String id);

    Inventory getChestInventory(Player player, String id);

    String getPlayerFactionId(Player player);

    String getPlayerFactionId(OfflinePlayer player);

    String getFactionsIdAt(Location location);

    String getFactionsIdAt(Chunk chunk);

    String getFactionsIdAt(Integer[] coordinates, World world);

    String getTagFromId(String id);

    String getIdFromTag(String tag);

    String getWilderness();

    String getRolePrefix(TranslatedRole translatedRole);

    List<Player> getOnlineMembers(String id);

    List<OfflinePlayer> getOfflineMembers(String id);

    UUID getLeader(String id);

    OfflinePlayer getOfflineLeader(String id);

    boolean hasFaction(Player player);

    boolean hasFaction(OfflinePlayer player);

    boolean hasFaction(UUID uuid);

    boolean isBypassing(Player player);

    boolean isWildernessAt(Location location);

    default boolean isAlt(Player player) {
        return false;
    }

    TranslatedRelation getRelationToFaction(Player player, String id);

    TranslatedRole getRole(Player player);

    TranslatedRole getRole(OfflinePlayer player);

    TranslatedRole getRole(UUID uuid);

    TranslatedRelation getFactionRelationToFaction(String id1, String id2);

    TranslatedRelation getRelationToPlayer(Player player, Player player2);

    default List<UUID> getAllMembers(String id) {
        List<UUID> all = new ArrayList<>();
        all.addAll(getOnlineMembers(id).stream().map(Player::getUniqueId).toList());
        all.addAll(getOfflineMembers(id).stream().map(OfflinePlayer::getUniqueId).toList());
        return all;
    }

    void setOpen(String factionId, boolean open);

    void setPermanent(String factionId, boolean permanent);

    String getDescription(String factionId);

    void setDescription(String factionId, String description);

    String getWarzone();

    String getSafezone();


    default void addMoney(String factionId, double amount) {
        throw new UnsupportedOperationException("This factions fork does not support this!");
    }

    default void subtractMoney(String factionId, double amount) {
        throw new UnsupportedOperationException("This factions fork does not support this!");
    }

    default void setMoney(String factionId, double amount) {
        throw new UnsupportedOperationException("This factions fork does not support this!");
    }

    default double getMoney(String factionId) {
        throw new UnsupportedOperationException("This factions fork does not support this!");
    }


    default void addMobcoins(String factionId, int amount) {
        throw new UnsupportedOperationException("This factions fork does not support this!");
    }

    default void subtractMobcoins(String factionId, int amount) {
        throw new UnsupportedOperationException("This factions fork does not support this!");
    }

    default void setMobcoins(String factionId, double amount) {
        throw new UnsupportedOperationException("This factions fork does not support this!");
    }

    default double getMobcoins(String factionId) {
        throw new UnsupportedOperationException("This factions fork does not support this!");
    }

    default void addExp(String factionId, int amount) {
        throw new UnsupportedOperationException("This factions fork does not support this!");
    }

    default void subtractExp(String factionId, int amount) {
        throw new UnsupportedOperationException("This factions fork does not support this!");
    }

    default void setExp(String factionId, int amount) {
        throw new UnsupportedOperationException("This factions fork does not support this!");
    }

    default int getExp(String factionId) {
        throw new UnsupportedOperationException("This factions fork does not support this!");
    }

    default boolean supportsMoneyOperations() {
        return false;
    }

    default boolean supportsExpOperations() {
        return false;
    }

    default boolean supportsMobcoinsOperations() {
        return false;
    }

}