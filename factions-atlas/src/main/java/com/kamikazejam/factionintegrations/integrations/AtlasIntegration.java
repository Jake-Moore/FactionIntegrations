package com.kamikazejam.factionintegrations.integrations;

import com.kamikazejam.factionintegrations.event.KFactionCreateEvent;
import com.kamikazejam.factionintegrations.event.KFactionDisbandEvent;
import com.kamikazejam.factionintegrations.event.KLandClaimEvent;
import com.kamikazejam.factionintegrations.event.KLandUnclaimEvent;
import com.kamikazejam.factionintegrations.event.KLandUnclaimallEvent;
import com.kamikazejam.factionintegrations.event.KPlayerEvent;
import com.kamikazejam.factionintegrations.event.KPlayerJoinEvent;
import com.kamikazejam.factionintegrations.event.KPlayerLeaveEvent;
import com.kamikazejam.factionintegrations.event.KPowerLossEvent;
import com.kamikazejam.factionintegrations.object.TranslatedRelation;
import com.kamikazejam.factionintegrations.object.TranslatedRole;
import com.kamikazejam.factionintegrations.shield.ShieldIntegration;
import com.kamikazejam.factionintegrations.utils.PluginSource;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.event.LandClaimEvent;
import com.massivecraft.factions.event.LandUnclaimAllEvent;
import com.massivecraft.factions.event.LandUnclaimEvent;
import com.massivecraft.factions.event.PowerLossEvent;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.massivecraft.factions.perms.Access;
import com.massivecraft.factions.perms.PermissableAction;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.upgrades.FactionUpgrade;
import com.massivecraft.factions.upgrades.UpgradeType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

//AL56AF50
public class AtlasIntegration implements KFaction, ShieldIntegration {

    public AtlasIntegration() {}

    @EventHandler
    public void onFactionCreate(FactionCreateEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getPluginManager().callEvent(new KFactionCreateEvent(getTagFromId(e.getFactionTag())));
            }
        }.runTaskLater(PluginSource.get(), 1);
    }

    @EventHandler
    public void onFactionDisband(FactionDisbandEvent e) {
        Bukkit.getPluginManager().callEvent(new KFactionDisbandEvent(e.getFaction().getId(), e.getFaction().getTag()));
    }

    @EventHandler
    public void onLandClaimEvent(LandClaimEvent e) {
        KLandClaimEvent event = new KLandClaimEvent(e.getFaction().getId(), e.getLocation().getWorldName(), new Integer[]{(int) e.getLocation().getX(), (int) e.getLocation().getZ()}, e.getfPlayer().getPlayer());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) e.setCancelled(true);
    }

    @EventHandler
    public void onLandUnclaimEvent(LandUnclaimEvent e) {
        KLandUnclaimEvent event = new KLandUnclaimEvent(e.getFaction().getId(), e.getLocation().getWorldName(), new Integer[]{(int) e.getLocation().getX(), (int) e.getLocation().getZ()}, e.getfPlayer().getPlayer());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) e.setCancelled(true);
    }

    @EventHandler
    public void onLandUnclaimallEvent(LandUnclaimAllEvent e) {
        KLandUnclaimallEvent event = new KLandUnclaimallEvent(e.getFaction().getId(), "None", new Integer[]{0, 0}, e.getfPlayer().getPlayer());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) e.setCancelled(true);
    }

    @EventHandler
    public void onJoin(FPlayerJoinEvent e) {
        switch (e.getReason()) {
            case CREATE:
                KPlayerJoinEvent event2 = new KPlayerJoinEvent(e.getFaction().getId(), e.getfPlayer().getId(), KPlayerEvent.Reason.CREATE);
                Bukkit.getPluginManager().callEvent(event2);

                if (event2.isCancelled()) e.setCancelled(true);
                break;
            case COMMAND:
                KPlayerJoinEvent event1 = new KPlayerJoinEvent(e.getFaction().getId(), e.getfPlayer().getId(), KPlayerEvent.Reason.COMMAND);
                Bukkit.getPluginManager().callEvent(event1);

                if (event1.isCancelled()) e.setCancelled(true);
                break;
            case LEADER:
                KPlayerJoinEvent event = new KPlayerJoinEvent(e.getFaction().getId(), e.getfPlayer().getId(), KPlayerEvent.Reason.LEADER);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) e.setCancelled(true);
                break;
        }
    }

    @EventHandler
    public void onJoin(FPlayerLeaveEvent e) {
        if (e.getFaction().getSize() == 1) {
            Bukkit.getPluginManager().callEvent(new KFactionDisbandEvent(e.getFaction().getId(), e.getFaction().getTag()));
        }

        switch (e.getReason()) {
            case DISBAND:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getId(), KPlayerEvent.Reason.DISBAND));
                break;
            case LEAVE:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getId(), KPlayerEvent.Reason.LEAVE));
                break;
            case RESET:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getId(), KPlayerEvent.Reason.RESET));
                break;
            case JOINOTHER:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getId(), KPlayerEvent.Reason.JOINOTHER));
                break;
            default:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getId(), KPlayerEvent.Reason.KICK));
                break;
        }
    }

    @Override
    public void setTnT(String id, long amount) {
        Faction faction = getFaction(id);
        if (faction == null) { return; }
        long limit = getMaxTnt(id);

        // Can't set tnt directly -> withdraw and deposit
        faction.withdrawTNT(faction.getTNT());
        faction.depositTNT(Math.min(amount, limit));
    }

    @Override
    public long addTnT(String id, long amount) {
        Faction faction = getFaction(id);
        if (faction == null) { return 0; }

        long totalAmount = faction.getTNT();
        long limit = getMaxTnt(id);

        if (amount + totalAmount > limit) {
            faction.depositTNT(limit - totalAmount);
            return limit - totalAmount;
        }
        faction.depositTNT(amount);
        return amount;
    }

    @Override
    public long getTnT(String id) {
        Faction faction = getFaction(id);
        if (faction == null) { return 0; }
        return faction.getTNT();
    }

    @Override
    public long getMaxTnt(String id) {
        Faction faction = getFaction(id);
        if (faction == null) { return 0; }

        FactionUpgrade upgrade = faction.getUpgrade(UpgradeType.TNT_STORAGE);
        return upgrade.getExpansion();
    }

    @Nullable
    private static Faction getFaction(String id) {
        return Factions.getInstance().getFactionById(id);
    }

    @Override
    public int getStrikes(String id) {
        Faction faction = getFaction(id);
        if (faction == null) { return 0; }

        return faction.getStrikes().size();
    }

    @Override
    public double getBalance(String id) {
        Faction faction = getFaction(id);
        if (faction == null) { return 0D; }
        return faction.getBalance();
    }

    @Override
    public void addBalance(String id, double add) {
        Faction faction = getFaction(id);
        if (faction == null) { return; }
        faction.depositMoney(add);
    }

    @Override
    public void subtractBalance(String id, double remove) {
        Faction faction = getFaction(id);
        if (faction == null) { return; }
        faction.withdrawMoney(remove);
    }

    @Override
    public boolean econEnabled() {
        return true;
    }

    @Override
    public boolean canPlayerBuildThere(Player player, Chunk chunk) {
        if (isBypassing(player)) { return true; }
        FLocation fLocation = new FLocation(player.getWorld().getName(), chunk.getX(), chunk.getZ());

        Faction faction = Board.getInstance().getFactionAt(fLocation);
        if (faction.isWilderness()) { return true; }

        return getRelationToFaction(player, faction.getId()).isEqualTo(TranslatedRelation.MEMBER);
    }

    @Override
    public boolean isBaseRegion(Location location, String id) {
        FLocation fLocation = new FLocation(location);
        Faction faction = Board.getInstance().getFactionAt(fLocation);
        if (!id.equalsIgnoreCase(faction.getId())) { return false; }
        return faction.isInsideBaseRegion(fLocation);
    }

    @Override
    public boolean isSystemFac(String id) {
        Faction factionById = Objects.requireNonNull(getFaction(id));
        return factionById.isSafeZone() || factionById.isWilderness() || factionById.isWarZone();
    }

    @Override
    public boolean playerCanBuildThere(Player player, Location location) {
        return FactionsBlockListener.playerCanBuildDestroyBlock(player, location, "build", true);
    }

    @Override
    public boolean isWarzoneAt(Location location) {
        return Board.getInstance().getFactionAt(new FLocation(location)).isWarZone();
    }

    @Override
    public boolean isSafezoneAt(Location location) {
        return Board.getInstance().getFactionAt(new FLocation(location)).isSafeZone();
    }

    @Override
    public boolean isSOTW() {
        return false;
    }

    @Override
    public boolean playerCanFlyThere(Player player, Location location) {
        return false;
    }

    @Override
    public boolean isSystemFacAt(Location location) {
        Faction factionAt = Board.getInstance().getFactionAt(new FLocation(location));
        return factionAt.isSafeZone() || factionAt.isWarZone() || factionAt.isWilderness();
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public boolean isShieldActiveNow(String id) {
        Faction faction = getFaction(id);
        if (faction == null) { return false; }
        return faction.isShieldActive();
    }

    @Override
    public int getMaxClaimWorldborder(World world) {
        WorldBorder worldBorder = world.getWorldBorder();

        double radius = worldBorder.getSize() / 2;

        return (int) (radius % 16 == 0 ? radius / 16 : (radius / 16) + 1);
    }

    @Override
    public int getClaimsInWorld(String id, World world) {
        return Board.getInstance().getFactionCoordCountInWorld(getFaction(id), world.getName());
    }

    @Override
    public int getMaxPlayers(String id) {
        return Conf.factionMemberLimit;
    }

    @Override
    public int getSize(String id) {
        Faction factionById = getFaction(id);

        if (factionById == null) return 0;
        if (factionById.getFPlayers() == null) return 0;

        return factionById.getFPlayers().size();
    }

    @Override
    public boolean isInFactionChest(Player player, String id) {
        Faction faction = getFaction(id);
        if (faction == null) { return false; }
        return faction.getChestInventory().getViewers().contains(player);
    }

    @Override
    public boolean isFaction(String id) {
        return Factions.getInstance().isValidFactionId(id);
    }

    @Override
    public void setFaction(Player player, String id) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        if (id == null) {
            fPlayer.leave();
            return;
        }

        fPlayer.setFaction(getFaction(id), false);
    }

    @Override
    public void setFaction(OfflinePlayer player, String id) {
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);

        if (id == null) {
            fPlayer.leave();
            return;
        }

        fPlayer.setFaction(getFaction(id), false);
    }

    @Override
    public void setRole(Player player, TranslatedRole translatedRole) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        setFPlayerRole(translatedRole, fPlayer);
    }

    @Override
    public void setRole(OfflinePlayer offlinePlayer, TranslatedRole translatedRole) {
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(offlinePlayer);

        setFPlayerRole(translatedRole, fPlayer);
    }

    private void setFPlayerRole(TranslatedRole translatedRole, FPlayer fPlayer) {
        switch (translatedRole.getValue()) {
            case 1:
                fPlayer.setRole(Role.RECRUIT);
                break;
            case 2:
                fPlayer.setRole(Role.NORMAL);
                break;
            case 3:
                fPlayer.setRole(Role.MODERATOR);
                break;
            case 4:
                fPlayer.setRole(Role.COLEADER);
                break;
            case 5:
                fPlayer.setRole(Role.LEADER);
                break;
        }
    }

    @Override
    public void setPermission(String id, TranslatedRelation relation, String permission, boolean b) {
        Faction faction = getFaction(id);
        if (faction == null) { return; }

        Relation rel = switch (relation) {
            case MEMBER -> Relation.MEMBER;
            case ENEMY -> Relation.ENEMY;
            case NEUTRAL -> Relation.NEUTRAL;
            case ALLY -> Relation.ALLY;
            case TRUCE -> Relation.TRUCE;
        };

        faction.setPermission(rel, PermissableAction.valueOf(permission), (b) ? Access.ALLOW : Access.DENY);
    }

    @Override
    public void setRelationWish(String id, String other, TranslatedRelation relation) {
        Faction a = Objects.requireNonNull(getFaction(id));
        Faction b = Objects.requireNonNull(getFaction(other));

        Relation rel = switch (relation) {
            case MEMBER -> Relation.MEMBER;
            case ENEMY -> Relation.ENEMY;
            case NEUTRAL -> Relation.NEUTRAL;
            case ALLY -> Relation.ALLY;
            case TRUCE -> Relation.TRUCE;
        };

        a.setRelationWish(b, rel);
    }

    @Override
    public boolean isTagAvailable(String tag) {
        return !Factions.getInstance().isTagTaken(tag);
    }

    @Override
    public String createFaction(String tag) {
        Faction faction = Factions.getInstance().createFaction();

        faction.setTag(tag);
        return faction.getId();
    }

    @Override
    public void setFactionPower(String id, double power) {
        Faction faction = getFaction(id);
        if (faction == null) { return; }
        faction.setPermanentPower((int) power);
    }

    @Override
    public void clearAllClaimsInWorld(String world) {
        World world1 = Bukkit.getWorld(world);

        for (Faction faction : Factions.getInstance().getAllFactions()) {
            if (faction.isWilderness() || faction.isWarZone() || faction.isSafeZone()) continue;
            Board.getInstance().unclaimAllInWorld(faction.getId(), world1);
        }
    }

    @Override
    public void setFactionAt(int cx, int cz, String world, String faction) {
        Board.getInstance().setFactionAt(getFaction(faction), new FLocation(world, cx, cz));
    }

    @Override
    public Set<String> getAllFactions() {
        List<Faction> allFactions = Factions.getInstance().getAllFactions();

        Set<String> toReturn = new HashSet<>(allFactions.size());

        for (Faction faction : allFactions) {
            toReturn.add(faction.getId());
        }
        return toReturn;
    }

    @Override
    public Map<String, Set<Integer[]>> getAllClaims(String id) {
        Map<String, Set<Integer[]>> toReturn = new HashMap<>();

        for (FLocation fLocation : Board.getInstance().getAllClaims(id)) {
            toReturn.putIfAbsent(fLocation.getWorldName(), new HashSet<>());

            toReturn.get(fLocation.getWorldName()).add(new Integer[]{
                    (int) fLocation.getX(), (int) fLocation.getZ()
            });
        }
        return toReturn;
    }

    @Override
    public Inventory getChestInventory(Player player, String id) {
        Faction faction = Objects.requireNonNull(getFaction(id));
        return faction.getChestInventory();
    }

    @Override
    public String getPlayerFactionId(Player player) {
        FPlayer fPlayers = FPlayers.getInstance().getByPlayer(player);
        return fPlayers.getFactionId();
    }

    @Override
    public String getPlayerFactionId(OfflinePlayer player) {
        FPlayer fPlayers = FPlayers.getInstance().getByOfflinePlayer(player);
        return fPlayers.getFactionId();
    }

    @Override
    public String getFactionsIdAt(Location location) {
        return Board.getInstance().getFactionAt(new FLocation(location)).getId();
    }

    @Override
    public String getFactionsIdAt(Chunk chunk) {
        return Board.getInstance().getFactionAt(new FLocation(new Location(chunk.getWorld(), chunk.getX() * 16, 1, chunk.getZ() * 16))).getId();
    }

    @Override
    public String getFactionsIdAt(Integer[] coordinates, World world) {
        int x = coordinates[0];
        int z = coordinates[1];

        return Board.getInstance().getFactionAt(new FLocation(new Location(world, x * 16, 1, z * 16))).getId();
    }

    @Override
    public String getTagFromId(String id) {
        if (id == null) return "None";
        if (!Factions.getInstance().isValidFactionId(id)) return "None";
        Faction factionById = getFaction(id);
        if (factionById == null) return "None";
        return factionById.getTag();
    }

    @Override
    public String getIdFromTag(String tag) {
        Faction byTag = Factions.getInstance().getByTag(tag);
        if (byTag == null) return null;
        return byTag.getId();
    }

    @Override
    public String getWilderness() {
        return Factions.getInstance().getWilderness().getId();
    }

    @Override
    public String getRolePrefix(TranslatedRole translatedRole) {
        if (translatedRole == TranslatedRole.ALT) return "";
        return Role.getByValue(translatedRole.getValue() - 1).getPrefix();
    }

    @Override
    public List<Player> getOnlineMembers(String id) {
        Faction faction = getFaction(id);
        if (faction == null) { return new ArrayList<>(); }

        List<Player> players = new ArrayList<>();
        for (FPlayer fPlayer : faction.getFPlayersWhereOnline(true)) {
            players.add(Bukkit.getPlayer(UUID.fromString(fPlayer.getId())));
        }
        return players;
    }

    @Override
    public List<OfflinePlayer> getOfflineMembers(String id) {
        List<OfflinePlayer> offliners = new ArrayList<>();

        Faction faction = getFaction(id);

        if (faction == null) return offliners;

        faction.getFPlayers().forEach(z -> {
            if (z.getPlayer() == null) {
                offliners.add(Bukkit.getOfflinePlayer(UUID.fromString(z.getId())));
                return;
            }
            if (!z.getPlayer().isOnline()) offliners.add(Bukkit.getOfflinePlayer(UUID.fromString(z.getId())));
        });

        return offliners;
    }

    @Override
    public UUID getLeader(String id) {
        Faction faction = getFaction(id);

        if (faction == null) return null;

        String uuid = faction.getFPlayerAdmin().getId();

        return UUID.fromString(uuid);
    }

    @Override
    public OfflinePlayer getOfflineLeader(String id) {
        Faction faction = getFaction(id);

        if (faction == null) return null;

        FPlayer fPlayerAdmin = faction.getFPlayerAdmin();

        if (fPlayerAdmin == null) return null;

        String uuid = fPlayerAdmin.getId();

        UUID id1 = UUID.fromString(uuid);

        return Bukkit.getOfflinePlayer(id1);
    }

    @Override
    public boolean hasFaction(Player player) {
        return FPlayers.getInstance().getByPlayer(player).hasFaction();
    }

    @Override
    public boolean hasFaction(OfflinePlayer player) {
        return FPlayers.getInstance().getByOfflinePlayer(player).hasFaction();
    }

    @Override
    public boolean hasFaction(UUID uuid) {
        return FPlayers.getInstance().getById(uuid.toString()).hasFaction();
    }

    @Override
    public boolean isBypassing(Player player) {
        return FPlayers.getInstance().getByPlayer(player).isAdminBypassing();
    }

    @Override
    public boolean isWildernessAt(Location location) {
        FLocation fLocation = new FLocation(location);
        return Board.getInstance().getFactionAt(fLocation).isWilderness();
    }

    @Override
    public TranslatedRelation getRelationToFaction(Player player, String id) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = getFaction(id);
        return TranslatedRelation.valueOf(fPlayer.getRelationTo(faction).name().toUpperCase());
    }

    @Override
    public TranslatedRole getRole(Player player) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        return TranslatedRole.valueOf(fPlayer.getRole().name().toUpperCase());
    }

    @Override
    public TranslatedRole getRole(OfflinePlayer player) {
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);
        return TranslatedRole.valueOf(fPlayer.getRole().name().toUpperCase());
    }

    @Override
    public TranslatedRole getRole(UUID uuid) {
        FPlayer fPlayer = FPlayers.getInstance().getById(uuid.toString());
        return TranslatedRole.valueOf(fPlayer.getRole().name().toUpperCase());
    }

    @Override
    public TranslatedRelation getFactionRelationToFaction(String id1, String id2) {
        Faction faction1 = getFaction(id1);
        Faction faction2 = getFaction(id2);
        if (faction1 == null || faction2 == null) return TranslatedRelation.ENEMY;
        return TranslatedRelation.valueOf(faction1.getRelationTo(faction2).name().toUpperCase());
    }

    @Override
    public TranslatedRelation getRelationToPlayer(Player player, Player player2) {
        FPlayer fPlayer1 = FPlayers.getInstance().getByPlayer(player);
        FPlayer fPlayer2 = FPlayers.getInstance().getByPlayer(player2);
        return TranslatedRelation.valueOf(fPlayer1.getRelationTo(fPlayer2).name().toUpperCase());
    }

    @Override
    public void setOpen(String factionId, boolean open) {
        Objects.requireNonNull(getFaction(factionId)).setOpen(open);
    }

    @Override
    public void setPermanent(String factionId, boolean permanent) {
        Objects.requireNonNull(getFaction(factionId)).setPermanent(permanent);
    }

    @Override
    public String getDescription(String factionId) {
        return Objects.requireNonNull(getFaction(factionId)).getDescription();
    }

    @Override
    public void setDescription(String factionId, String description) {
        Objects.requireNonNull(getFaction(factionId)).setDescription(description);
    }

    @Override
    public String getWarzone() {
        return Factions.getInstance().getWarZone().getId();
    }

    @Override
    public String getSafezone() {
        return Factions.getInstance().getSafeZone().getId();
    }

    @Override
    public boolean isShieldRegionAt(String id, Location location) {
        return this.isBaseRegion(location, id);
    }

    @Override
    public boolean isShieldActive(String id) {
        return this.isShieldActiveNow(id);
    }

    @EventHandler
    public void onPowerLoss(PowerLossEvent e) {
        KPowerLossEvent event = new KPowerLossEvent(e.getfPlayer().getPlayer());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) e.setCancelled(true);
    }
}