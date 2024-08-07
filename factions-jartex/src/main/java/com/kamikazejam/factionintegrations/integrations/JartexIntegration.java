package com.kamikazejam.factionintegrations.integrations;

import com.google.common.collect.Lists;
import com.kamikazejam.factionintegrations.event.*;
import com.kamikazejam.factionintegrations.object.TranslatedRelation;
import com.kamikazejam.factionintegrations.object.TranslatedRole;
import com.kamikazejam.factionintegrations.utils.PluginSource;
import com.massivecraft.factions.*;
import com.massivecraft.factions.event.*;
import com.massivecraft.factions.jartex.FactionsAPI;
import com.massivecraft.factions.jartex.faction.permission.PRole;
import com.massivecraft.factions.jartex.faction.permission.PTNormal;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.persist.MemoryBoard;
import com.massivecraft.factions.zcore.persist.MemoryFaction;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@SuppressWarnings({"deprecation", "unused"})
public class JartexIntegration implements KFaction {

    @EventHandler
    public void onFactionCreate(FactionCreateEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Faction theFaction = Factions.getInstance().getByTag(e.getFactionTag());

                Bukkit.getPluginManager().callEvent(new KFactionCreateEvent(theFaction.getId()));
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

        // Important to check for overclaiming
        Faction factionAt = Board.getInstance().getFactionAt(e.getLocation());
        if (factionAt.isWilderness())
            return;

        KLandUnclaimEvent landUnclaimEvent = new KLandUnclaimEvent(factionAt.getId(), e.getLocation().getWorldName(), new Integer[]{
                (int) e.getLocation().getX(),
                (int) e.getLocation().getZ()
        }, null);
        Bukkit.getPluginManager().callEvent(landUnclaimEvent);
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
                KPlayerJoinEvent event2 = new KPlayerJoinEvent(e.getFaction().getId(), e.getfPlayer().getAccountId(), KPlayerEvent.Reason.CREATE);
                Bukkit.getPluginManager().callEvent(event2);

                if (event2.isCancelled()) e.setCancelled(true);
                break;
            case COMMAND:
                KPlayerJoinEvent event1 = new KPlayerJoinEvent(e.getFaction().getId(), e.getfPlayer().getAccountId(), KPlayerEvent.Reason.COMMAND);
                Bukkit.getPluginManager().callEvent(event1);

                if (event1.isCancelled()) e.setCancelled(true);
                break;
            case LEADER:
                KPlayerJoinEvent event = new KPlayerJoinEvent(e.getFaction().getId(), e.getfPlayer().getAccountId(), KPlayerEvent.Reason.LEADER);
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
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getAccountId(), KPlayerEvent.Reason.DISBAND));
                break;
            case LEAVE:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getAccountId(), KPlayerEvent.Reason.LEAVE));
                break;
            case RESET:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getAccountId(), KPlayerEvent.Reason.RESET));
                break;
            case JOINOTHER:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getAccountId(), KPlayerEvent.Reason.JOINOTHER));
                break;
            default:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getAccountId(), KPlayerEvent.Reason.KICK));
                break;
        }
    }

    public JartexIntegration() {}

    @SneakyThrows
    @Override
    public void setTnT(String id, long amountL) {
        Faction faction = Factions.getInstance().getFactionById(id);
        int amount = (int) amountL;
        int limit = (int) getMaxTnt(id);

        if (amount > limit) {
            faction.setTNT(limit);
            return;
        }
        faction.setTNT(amount);
    }

    @SneakyThrows
    @Override
    public long addTnT(String id, long amountL) {
        Faction faction = Factions.getInstance().getFactionById(id);
        int amount = (int) amountL;
        int totalAmount = faction.getTNT();
        int limit = (int) getMaxTnt(id);

        if (amount + totalAmount > limit) {
            faction.setTNT(limit);
            return limit - totalAmount;
        }
        faction.setTNT(totalAmount + amount);
        return amount;
    }

    @SneakyThrows
    @Override
    public long getTnT(String id) {
        return Factions.getInstance().getFactionById(id).getTNT();
    }

    @SneakyThrows
    @Override
    public long getMaxTnt(String id) {
        int maxTNT = Factions.getInstance().getFactionById(id).getMaximumTNT();
        return maxTNT <= 0 ? Integer.MAX_VALUE : maxTNT;
    }

    @Override
    public double getBalance(String id) {
        String accountId = Factions.getInstance().getFactionById(id).getAccountId();

        OfflinePlayer to = isUUID(accountId) ? Bukkit.getOfflinePlayer(UUID.fromString(accountId)) : Bukkit.getOfflinePlayer(accountId);

        return PluginSource.getEconomy().getBalance(to);
    }

    @Override
    public void addBalance(String id, double add) {
        String accountId = Factions.getInstance().getFactionById(id).getAccountId();

        OfflinePlayer to = isUUID(accountId) ? Bukkit.getOfflinePlayer(UUID.fromString(accountId)) : Bukkit.getOfflinePlayer(accountId);

        PluginSource.getEconomy().depositPlayer(to, add);
    }

    @Override
    public void subtractBalance(String id, double remove) {
        String accountId = Factions.getInstance().getFactionById(id).getAccountId();

        OfflinePlayer to = isUUID(accountId) ? Bukkit.getOfflinePlayer(UUID.fromString(accountId)) : Bukkit.getOfflinePlayer(accountId);

        PluginSource.getEconomy().withdrawPlayer(to, remove);
    }

    @Override
    public boolean econEnabled() {
        //Couldn't find it being disabled anywhere, so I assume it's locked on.
        return true;
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

    @SneakyThrows
    @Override
    public int getStrikes(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);
        if (faction == null) return 0;
        return faction.getActiveStrikes();
    }

    @Override
    public int getMaxPlayers(String id) {
        return FactionsAPI.getInstance().getMaxFactionMembers(Factions.getInstance().getFactionById(id));
    }

    @Override
    public int getSize(String id) {
        return Factions.getInstance().getFactionById(id).getSize();
    }

    @Override
    public boolean isInFactionChest(Player player, String id) {
        return false;
    }

    @Override
    public boolean isFaction(String id) {
        return Factions.getInstance().isValidFactionId(id);
    }

    @SneakyThrows
    @Override
    public void setFaction(Player player, String id) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        if (id == null) {
            fPlayer.leave(false);
            return;
        }
        fPlayer.setFaction(Factions.getInstance().getFactionById(id), false);
    }

    @SneakyThrows
    @Override
    public void setFaction(OfflinePlayer player, String id) {
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);

        if (id == null) {
            fPlayer.leave(false);
            return;
        }
        fPlayer.setFaction(Factions.getInstance().getFactionById(id), false);
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
                fPlayer.setRole(Role.CO_ADMIN);
                break;
            case 5:
                fPlayer.setRole(Role.ADMIN);
                break;
        }
    }

    @SneakyThrows
    @Override
    public void setPermission(String id, TranslatedRelation relation, String permission, boolean b) {
        MemoryFaction faction = (MemoryFaction) Factions.getInstance().getFactionById(id);

        PRole rel = switch (relation) {
            case MEMBER -> PRole.NORMAL;
            case ENEMY -> PRole.ENEMY;
            case NEUTRAL -> PRole.NEUTRAL;
            case ALLY -> PRole.ALLY;
            case TRUCE -> PRole.TRUCE;
        };

        Map<String, List<PRole>> permMap = faction.getPermissions();
        List<PRole> permissions = permMap.computeIfAbsent(permission, k -> new ArrayList<>());
        if (!b) {
            permissions.removeIf(z -> z == rel);
        }else {
            if (!permissions.contains(rel)) {
                permissions.add(rel);
            }
        }
    }

    @SneakyThrows
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
        MemoryBoard memBoard = (MemoryBoard) Board.getInstance();

        MemoryBoard.MemoryBoardMap map = memBoard.flocationIds;

        List<FLocation> keys = Lists.newArrayList(map.keySet());

        keys.forEach(key -> {
            Faction faction = memBoard.getFactionAt(key);

            if (faction.isWilderness() || faction.isWarZone() || faction.isSafeZone())
                return;

            if (key.getWorldName().equals(world))
                memBoard.clearOwnershipAt(key);
        });
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

    @SneakyThrows
    @Override
    public Map<String, Set<Integer[]>> getAllClaims(String id) {
        Map<String, Set<Integer[]>> toReturn = new HashMap<>();

        Set<FLocation> all = Board.getInstance().getAllClaims(Integer.valueOf(id));

        for (FLocation fLocation : all) {
            toReturn.putIfAbsent(fLocation.getWorldName(), new HashSet<>());

            toReturn.get(fLocation.getWorldName()).add(new Integer[]{
                    (int) fLocation.getX(), (int) fLocation.getZ()
            });
        }
        return toReturn;
    }

    @Override
    public Inventory getChestInventory(Player player, String id) {
        return null;
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
            case 1 -> Conf.prefixRecruit;
            case 2 -> Conf.prefixNormal;
            case 3 -> Conf.prefixMod;
            case 4 -> Conf.prefixCoAdmin;
            case 5 -> Conf.prefixAdmin;
            default -> "";
        };
    }

    @SneakyThrows
    @Override
    public List<Player> getOnlineMembers(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return new ArrayList<>();

        List<Player> players = new ArrayList<>();

        for (FPlayer fPlayer : faction.getFPlayersWhereOnline(true)) {
            if (!(fPlayer.getRole().isAtLeast(Role.RECRUIT)) || isAlt(fPlayer.getPlayer())) continue;
            players.add(Bukkit.getPlayer(UUID.fromString(fPlayer.getAccountId())));
        }
        return players;
    }

    @Override
    public UUID getLeader(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);
        if (faction == null) return null;

        return UUID.fromString(faction.getFPlayerAdmin().getAccountId());
    }

    @Override
    public OfflinePlayer getOfflineLeader(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return null;

        FPlayer fPlayerAdmin = faction.getFPlayerAdmin();

        if (fPlayerAdmin == null) return null;

        String uuid = fPlayerAdmin.getAccountId();

        UUID id1 = UUID.fromString(uuid);

        return Bukkit.getOfflinePlayer(id1);
    }

    private boolean isAtLeastRecruitAndNotAlt(FPlayer fPlayer) {
        return fPlayer.getRole().isAtLeast(Role.RECRUIT) && !isAlt(fPlayer.getPlayer());
    }

    @Override
    public List<OfflinePlayer> getOfflineMembers(String id) {
        List<OfflinePlayer> offliners = new ArrayList<>();

        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return offliners;

        faction.getFPlayers().forEach(z -> {
            if (!isAtLeastRecruitAndNotAlt(z)) return;
            if (z.getPlayer() == null) {
                offliners.add(Bukkit.getOfflinePlayer(UUID.fromString(z.getAccountId())));
                return;
            }
            if (!z.getPlayer().isOnline()) offliners.add(Bukkit.getOfflinePlayer(UUID.fromString(z.getAccountId())));
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

    @SneakyThrows
    @Override
    public boolean hasFaction(UUID uuid) {
        return FPlayers.getInstance().getById(uuid).hasFaction();
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

    @SneakyThrows
    @Override
    public TranslatedRole getRole(UUID uuid) {
        FPlayer fPlayer = FPlayers.getInstance().getById(uuid);
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

    @SneakyThrows
    @Override
    public boolean isAlt(Player player) {
        return FPlayers.getInstance().getByPlayer(player).isAlt();
    }

    @EventHandler
    public void onPowerLoss(PowerLossEvent e) {
        KPowerLossEvent event = new KPowerLossEvent(e.getfPlayer().getPlayer());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) e.setCancelled(true);
    }

    public boolean hasPermission(String factionId, UUID playerUUID, String permission) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(Bukkit.getPlayer(playerUUID));
        return FactionsAPI.getInstance().getPermissionManager().hasPermission(faction, fPlayer, PTNormal.valueOf(permission));
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

    @Override
    public void addMoney(String factionId, double amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.addMoney(amount);
    }

    @Override
    public void subtractMoney(String factionId, double amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.removeMoney(amount);
    }

    @Override
    public void setMoney(String factionId, double amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.removeMoney(memoryFaction.getMoney());
        memoryFaction.addMoney(amount);
    }

    @Override
    public double getMoney(String factionId) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return 0D;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        return memoryFaction.getMoney();
    }

    @Override
    public void addMobcoins(String factionId, int amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.addMobcoins(amount);
    }

    @Override
    public void subtractMobcoins(String factionId, int amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.removeMobcoins(amount);
    }

    @Override
    public void setMobcoins(String factionId, double amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.removeMobcoins(memoryFaction.getMobcoins());
        memoryFaction.addMoney(amount);
    }

    @Override
    public double getMobcoins(String factionId) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return 0D;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        return memoryFaction.getMobcoins();
    }

    @Override
    public void addExp(String factionId, int amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.addExp(amount);
    }

    @Override
    public void subtractExp(String factionId, int amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.removeExp(amount);
    }

    @Override
    public void setExp(String factionId, int amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.removeExp(memoryFaction.getExp());
        memoryFaction.addExp(amount);
    }

    @Override
    public int getExp(String factionId) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return 0;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        return memoryFaction.getExp();
    }

    @Override
    public boolean supportsMoneyOperations() {
        return true;
    }

    @Override
    public boolean supportsExpOperations() {
        return true;
    }

    @Override
    public boolean supportsMobcoinsOperations() {
        return true;
    }
}
