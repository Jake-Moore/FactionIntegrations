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
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.event.LandClaimEvent;
import com.massivecraft.factions.event.LandUnclaimAllEvent;
import com.massivecraft.factions.event.LandUnclaimEvent;
import com.massivecraft.factions.event.PowerLossEvent;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.util.StrikeInfo;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UUIDIntegration implements KFaction {

    @EventHandler
    public void onFactionCreate(FactionCreateEvent e) {
        Bukkit.getPluginManager().callEvent(new KFactionCreateEvent(e.getFaction().getId()));
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

        // Important to check for overclaiming
        Faction factionAt = Board.getInstance().getFactionAt(e.getLocation());
        if (factionAt.isWilderness())
            return;

        KLandUnclaimEvent landUnclaimEvent = new KLandUnclaimEvent(factionAt.getId(), e.getLocation().getWorldName(), new Integer[]{
                (int) e.getLocation().getX(),
                (int) e.getLocation().getZ()
        }, null);
        Bukkit.getPluginManager().callEvent(landUnclaimEvent);
        if (landUnclaimEvent.isCancelled()) e.setCancelled(true);
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

    public UUIDIntegration() {}

    @Override
    public void setTnT(String id, long amount) {
        Faction faction = Factions.getInstance().getFactionById(id);
        long limit = getMaxTnt(id);

        faction.setTNTBank(0);
        if (amount > limit) {
            faction.setTNTBank((int) limit);
            return;
        }
        faction.setTNTBank((int) amount);
    }

    @Override
    public long addTnT(String id, long amount) {
        Faction faction = Factions.getInstance().getFactionById(id);
        int totalAmount = faction.getTNTBank();

        long limit = getMaxTnt(id);

        if (amount + totalAmount > limit) {
            faction.setTNTBank((int) limit);
            return limit - totalAmount;
        }
        faction.setTNTBank((int) amount + totalAmount);
        return amount;
    }

    @Override
    public long getTnT(String id) {
        return Factions.getInstance().getFactionById(id).getTNTBank();
    }

    @Override
    public long getMaxTnt(String id) {
        int maxTNT = Factions.getInstance().getFactionById(id).getMaxTNT();
        return maxTNT <= 0 ? Integer.MAX_VALUE : maxTNT;
    }

    @Override
    public double getBalance(String id) {
        return Econ.getBalance(Factions.getInstance().getFactionById(id));
    }

    @Override
    public void addBalance(String id, double add) {
        Faction faction = Factions.getInstance().getFactionById(id);
        Econ.deposit(faction, add);
    }

    @Override
    public void subtractBalance(String id, double remove) {
        Faction faction = Factions.getInstance().getFactionById(id);
        Econ.withdraw(faction, remove);
    }

    @Override
    public boolean econEnabled() {
        return FactionsPlugin.getInstance().getConfigManager().getMainConfig().economy().isEnabled();
    }

    @Override
    public boolean canPlayerBuildThere(Player player, Chunk chunk) {
        if (isBypassing(player)) return true;

        Faction faction = Board.getInstance().getFactionAt(new FLocation(player.getWorld().getName(), chunk.getX(), chunk.getZ()));

        if (faction.isWilderness()) return true;

        return getRelationToFaction(player, faction.getId()).isEqualTo(TranslatedRelation.MEMBER);
    }

    @Override
    public boolean isBaseRegion(Location location, String id) {
        return false;
    }

    @Override
    public boolean isSystemFac(String id) {
        Faction factionById = Factions.getInstance().getFactionById(id);
        return factionById.isSafeZone() || factionById.isWilderness() || factionById.isWarZone();
    }

    @Override
    public boolean playerCanBuildThere(Player player, Location location) {
        return FactionsBlockListener.playerCanBuildDestroyBlock(player, location, PermissibleAction.BUILD, true);
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
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        FLocation fLocation = new FLocation(location);
        return fPlayer.canFlyAtLocation(fLocation);
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
        return false;
    }

    @Override
    public int getMaxClaimWorldborder(World world) {
        WorldBorder worldBorder = world.getWorldBorder();

        double radius = worldBorder.getSize() / 2;

        return (int) (radius % 16 == 0 ? radius / 16 : (radius / 16) + 1);
    }

    @Override
    public int getClaimsInWorld(String id, World world) {
        return Board.getInstance().getFactionCoordCountInWorld(Factions.getInstance().getFactionById(id), world.getName());
    }

    @Override
    public int getStrikes(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return 0;

        List<StrikeInfo> strikes = faction.getStrikes();

        if (strikes == null) return 0;

        return strikes.size();
    }

    @Override
    public int getMaxPlayers(String id) {
        return FactionsPlugin.getInstance().conf().factions().other().getFactionMemberLimit();
    }

    @Override
    public int getSize(String id) {
        return Factions.getInstance().getFactionById(id).getSize();
    }

    @Override
    public boolean isInFactionChest(Player player, String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        return faction.getChest().getViewers().contains(player);
    }

    @Override
    public boolean isFaction(String id) {
        return Factions.getInstance().isValidFactionId(id);
    }

    @Override
    public void setFaction(Player player, String id) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        if (id == null) {
            fPlayer.leave(false);
            return;
        }

        fPlayer.setFaction(Factions.getInstance().getFactionById(id));
    }

    @Override
    public void setFaction(OfflinePlayer player, String id) {
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);

        if (id == null) {
            fPlayer.leave(false);
            return;
        }

        fPlayer.setFaction(Factions.getInstance().getFactionById(id));
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
                fPlayer.setRole(Role.ADMIN);
                break;
        }
    }

    @Override
    public void setPermission(String id, TranslatedRelation relation, String permission, boolean b) {
        Faction faction = Factions.getInstance().getFactionById(id);

        Relation rel = switch (relation) {
            case MEMBER -> Relation.MEMBER;
            case ENEMY -> Relation.ENEMY;
            case NEUTRAL -> Relation.NEUTRAL;
            case ALLY -> Relation.ALLY;
            case TRUCE -> Relation.TRUCE;
        };

        faction.setPermission(b, rel, PermissibleAction.valueOf(permission), true);
    }

    @Override
    public void setRelationWish(String id, String other, TranslatedRelation relation) {
        Faction faction = Factions.getInstance().getFactionById(id);

        faction.setRelationWish(Factions.getInstance().getFactionById(other), Relation.valueOf(relation.toString()));
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
        Faction faction = Factions.getInstance().getFactionById(id);

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
        Board.getInstance().setFactionAt(Factions.getInstance().getFactionById(faction), new FLocation(world, cx, cz));
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
        Faction faction = Factions.getInstance().getFactionById(id);

        return faction.getChest();
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
        Faction factionById = Factions.getInstance().getFactionById(id);
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
        return switch (translatedRole.getValue()) {
            case 1 -> FactionsPlugin.getInstance().getConfigManager().getMainConfig().factions().prefixes().getRecruit();
            case 2 -> FactionsPlugin.getInstance().getConfigManager().getMainConfig().factions().prefixes().getNormal();
            case 3 -> FactionsPlugin.getInstance().getConfigManager().getMainConfig().factions().prefixes().getMod();
            case 4 -> FactionsPlugin.getInstance().getConfigManager().getMainConfig().factions().prefixes().getColeader();
            case 5 -> FactionsPlugin.getInstance().getConfigManager().getMainConfig().factions().prefixes().getAdmin();
            default -> "";
        };
    }

    @Override
    public List<Player> getOnlineMembers(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return new ArrayList<>();

        List<Player> players = new ArrayList<>();

        for (FPlayer fPlayer : faction.getFPlayersWhereOnline(true)) {
            if (!fPlayer.getRole().isAtLeast(Role.RECRUIT)) continue;
            players.add(Bukkit.getPlayer(UUID.fromString(fPlayer.getId())));
        }
        return players;
    }

    @Override
    public UUID getLeader(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return null;

        String uuid = faction.getFPlayerAdmin().getId();

        return UUID.fromString(uuid);
    }

    @Override
    public OfflinePlayer getOfflineLeader(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return null;

        FPlayer fPlayerAdmin = faction.getFPlayerAdmin();

        if (fPlayerAdmin == null) return null;

        String uuid = fPlayerAdmin.getId();

        UUID id1 = UUID.fromString(uuid);

        return Bukkit.getOfflinePlayer(id1);
    }

    @Override
    public List<OfflinePlayer> getOfflineMembers(String id) {
        List<OfflinePlayer> offliners = new ArrayList<>();

        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return offliners;

        faction.getFPlayers().forEach(z -> {
            if (!z.getRole().isAtLeast(Role.RECRUIT)) return;
            if (z.getPlayer() == null) {
                offliners.add(Bukkit.getOfflinePlayer(UUID.fromString(z.getId())));
                return;
            }
            if (!z.getPlayer().isOnline()) offliners.add(Bukkit.getOfflinePlayer(UUID.fromString(z.getId())));
        });

        return offliners;
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
        Faction faction = Factions.getInstance().getFactionById(id);
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
        Faction faction1 = Factions.getInstance().getFactionById(id1);
        Faction faction2 = Factions.getInstance().getFactionById(id2);
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
    public boolean isAlt(Player player) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        return fPlayer.getRole() == Role.ALT;
    }

    @EventHandler
    public void onPowerLoss(PowerLossEvent e) {
        KPowerLossEvent event = new KPowerLossEvent(e.getfPlayer().getPlayer());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) e.setCancelled(true);
    }

    @Override
    public void setOpen(String factionId, boolean open) {
        Factions.getInstance().getFactionById(factionId).setOpen(open);
    }

    @Override
    public void setPermanent(String factionId, boolean permanent) {
        Factions.getInstance().getFactionById(factionId).setPermanent(permanent);
    }

    @Override
    public String getDescription(String factionId) {
        return Factions.getInstance().getFactionById(factionId).getDescription();
    }

    @Override
    public void setDescription(String factionId, String description) {
        Factions.getInstance().getFactionById(factionId).setDescription(description);
    }

    @Override
    public String getWarzone() {
        return Factions.getInstance().getWarZone().getId();
    }

    @Override
    public String getSafezone() {
        return Factions.getInstance().getSafeZone().getId();
    }
}

