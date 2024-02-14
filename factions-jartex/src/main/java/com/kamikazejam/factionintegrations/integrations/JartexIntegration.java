package com.kamikazejam.factionintegrations.integrations;

import com.google.common.collect.Lists;
import com.kamikazejam.factionintegrations.FactionIntegrations;
import com.kamikazejam.factionintegrations.event.*;
import com.kamikazejam.factionintegrations.object.TranslatedRelation;
import com.kamikazejam.factionintegrations.object.TranslatedRole;
import com.massivecraft.factions.*;
import com.massivecraft.factions.event.*;
import com.massivecraft.factions.iface.RelationParticipator;
import com.massivecraft.factions.jartex.FactionsAPI;
import com.massivecraft.factions.jartex.faction.permission.PRole;
import com.massivecraft.factions.jartex.faction.permission.PTNormal;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.zcore.persist.MemoryBoard;
import com.massivecraft.factions.zcore.persist.MemoryFaction;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("deprecation")
public class JartexIntegration implements KFaction {

    @EventHandler
    public void onFactionCreate(FactionCreateEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Faction theFaction = Factions.getInstance().getByTag(e.getFactionTag());

                Bukkit.getPluginManager().callEvent(new KFactionCreateEvent(theFaction.getId()));
            }
        }.runTaskLater(FactionIntegrations.get(), 1);
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
        Bukkit.getPluginManager().callEvent(new KLandUnclaimEvent(e.getFaction().getId(), e.getLocation().getWorldName(), new Integer[]{(int) e.getLocation().getX(), (int) e.getLocation().getZ()}, e.getfPlayer().getPlayer()));
    }

    @EventHandler
    public void onLandUnclaimallEvent(LandUnclaimAllEvent e) {
        Bukkit.getPluginManager().callEvent(new KLandUnclaimallEvent(e.getFaction().getId(), "None", new Integer[]{0, 0}, e.getfPlayer().getPlayer()));
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

    private Method canBuild;

    private Method getRole;

    private Method relationTo, setRole;

    private Object leader, coleader, moderator, normal, recruit;

    private Method setTNT, getTNT, getMaximumTNT;

    private Method getActiveStrikes;

    private Method setFaction;

    private Method getPermissions;

    private Method setRelationWish;

    private Method getAllClaims;

    private Method isAlt;

    private Method isAtLeast;

    private Method getByID;

    public JartexIntegration() throws ClassNotFoundException, NoSuchMethodException {
        relationTo = RelationParticipator.class.getMethod("getRelationTo", RelationParticipator.class);

        getRole = FPlayer.class.getMethod("getRole");

        Class<?> roleClass = Class.forName("com.massivecraft.factions.struct.Role");

        setRole = FPlayer.class.getMethod("setRole", roleClass);

        for (Object type : roleClass.getEnumConstants()) {
            String toString = ((Enum<?>) type).name();
            switch (toString.toUpperCase()) {
                case "LEADER":
                case "ADMIN":
                    leader = type;
                    break;
                case "CO_ADMIN":
                    coleader = type;
                    break;
                case "MODERATOR":
                    moderator = type;
                    break;
                case "NORMAL":
                    normal = type;
                    break;
                case "RECRUIT":
                    recruit = type;
            }
        }

        getActiveStrikes = Faction.class.getMethod("getActiveStrikes");

        setFaction = FPlayer.class.getMethod("setFaction", Faction.class, boolean.class);

        isAtLeast = roleClass.getMethod("isAtLeast", roleClass);

        isAlt = FPlayer.class.getMethod("isAlt");

        getPermissions = Faction.class.getMethod("getPermissions");

        setRelationWish = Faction.class.getMethod("setRelationWish", Faction.class, Relation.class);

        getByID = FPlayers.class.getMethod("getById", UUID.class);

        getAllClaims = Board.class.getMethod("getAllClaims", Integer.class);

        getTNT = Faction.class.getMethod("getTNT");
        setTNT = Faction.class.getMethod("setTNT", int.class);
        getMaximumTNT = Faction.class.getMethod("getMaximumTNT");

        canBuild = Class.forName("com.massivecraft.factions.listeners.FactionsBlockListener").getMethod("playerCanBuildDestroyBlock", Player.class, Location.class, String.class, boolean.class);
    }

    @SneakyThrows
    @Override
    public void setTnT(String id, long amount) {
        Faction faction = Factions.getInstance().getFactionById(id);
        long limit = getMaxTnt(id);

        if (amount > limit) {
            setTNT.invoke(faction, (int) limit);
            return;
        }
        setTNT.invoke(faction, (int) amount);
    }

    @SneakyThrows
    @Override
    public long addTnT(String id, long amount) {
        Faction faction = Factions.getInstance().getFactionById(id);
        int totalAmount = (int) getTNT.invoke(faction);

        long limit = getMaxTnt(id);

        if (amount + totalAmount > limit) {
            setTNT.invoke(faction, (int) limit);
            return limit - totalAmount;
        }
        setTNT.invoke(faction, (int) (totalAmount + amount));
        return amount;
    }

    @SneakyThrows
    @Override
    public long getTnT(String id) {
        return (int) getTNT.invoke(Factions.getInstance().getFactionById(id));
    }

    @SneakyThrows
    @Override
    public long getMaxTnt(String id) {
        int maxTNT = (int) getMaximumTNT.invoke(Factions.getInstance().getFactionById(id));
        return maxTNT <= 0 ? Integer.MAX_VALUE : maxTNT;
    }

    @Override
    public double getBalance(String id) {
        String accountId = Factions.getInstance().getFactionById(id).getAccountId();

        OfflinePlayer to = isUUID(accountId) ? Bukkit.getOfflinePlayer(UUID.fromString(accountId)) : Bukkit.getOfflinePlayer(accountId);

        return FactionIntegrations.getEconomy().getBalance(to);
    }

    @Override
    public void addBalance(String id, double add) {
        String accountId = Factions.getInstance().getFactionById(id).getAccountId();

        OfflinePlayer to = isUUID(accountId) ? Bukkit.getOfflinePlayer(UUID.fromString(accountId)) : Bukkit.getOfflinePlayer(accountId);

        FactionIntegrations.getEconomy().depositPlayer(to, add);
    }

    @Override
    public void subtractBalance(String id, double remove) {
        String accountId = Factions.getInstance().getFactionById(id).getAccountId();

        OfflinePlayer to = isUUID(accountId) ? Bukkit.getOfflinePlayer(UUID.fromString(accountId)) : Bukkit.getOfflinePlayer(accountId);

        FactionIntegrations.getEconomy().withdrawPlayer(to, remove);
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
        try {
            return (boolean) canBuild.invoke(null, player, location, "build", true);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return false;
        }
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

        return (int) getActiveStrikes.invoke(faction);
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

        setFaction.invoke(fPlayer, Factions.getInstance().getFactionById(id), false);
    }

    @SneakyThrows
    @Override
    public void setFaction(OfflinePlayer player, String id) {
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);

        if (id == null) {
            fPlayer.leave(false);
            return;
        }

        setFaction.invoke(fPlayer, Factions.getInstance().getFactionById(id), false);
    }

    @Override
    public void setRole(Player player, TranslatedRole translatedRole) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        try {
            switch (translatedRole.getValue()) {
                case 1:
                    setRole.invoke(fPlayer, recruit);
                    break;
                case 2:
                    setRole.invoke(fPlayer, normal);
                    break;
                case 3:
                    setRole.invoke(fPlayer, moderator);
                    break;
                case 4:
                    setRole.invoke(fPlayer, coleader);
                    break;
                case 5:
                    setRole.invoke(fPlayer, leader);
                    break;
            }
        } catch (IllegalAccessException | InvocationTargetException ignored) {

        }
    }

    @Override
    public void setRole(OfflinePlayer offlinePlayer, TranslatedRole translatedRole) {
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(offlinePlayer);

        try {
            switch (translatedRole.getValue()) {
                case 1:
                    setRole.invoke(fPlayer, recruit);
                    break;
                case 2:
                    setRole.invoke(fPlayer, normal);
                    break;
                case 3:
                    setRole.invoke(fPlayer, moderator);
                    break;
                case 4:
                    setRole.invoke(fPlayer, coleader);
                    break;
                case 5:
                    setRole.invoke(fPlayer, leader);
                    break;
            }
        } catch (IllegalAccessException | InvocationTargetException ignored) {

        }
    }

    @SneakyThrows
    @Override
    public void setPermission(String id, TranslatedRelation relation, String permission, boolean b) {
        Faction faction = Factions.getInstance().getFactionById(id);

        PRole rel = null;

        switch (relation) {
            case MEMBER:
                rel = PRole.NORMAL;
                break;
            case ENEMY:
                rel = PRole.ENEMY;
                break;
            case NEUTRAL:
                rel = PRole.NEUTRAL;
                break;
            case ALLY:
                rel = PRole.ALLY;
                break;
            case TRUCE:
                rel = PRole.TRUCE;
        }

        List<PRole> allowed = (List<PRole>) ((Map) getPermissions.invoke(faction)).get(rel.name());

        if (allowed == null)
            return;

        if (allowed.contains(rel))
            return;

        allowed.add(rel);
    }

    @SneakyThrows
    @Override
    public void setRelationWish(String id, String other, TranslatedRelation relation) {
        Faction faction = Factions.getInstance().getFactionById(id);

        setRelationWish.invoke(faction, Factions.getInstance().getFactionById(other), Relation.valueOf(relation.toString()));
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

        Set<FLocation> all = (Set<FLocation>) getAllClaims.invoke(Board.getInstance(), Integer.valueOf(id));

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
        String toReturn = "";
        switch (translatedRole.getValue()) {
            case 1:
                toReturn = Conf.prefixRecruit;
                break;
            case 2:
                toReturn = Conf.prefixNormal;
                break;
            case 3:
                toReturn = Conf.prefixMod;
                break;
            case 4:
                toReturn = Conf.prefixCoAdmin;
                break;
            case 5:
                toReturn = Conf.prefixAdmin;
                break;
        }
        return toReturn;
    }

    @SneakyThrows
    @Override
    public List<Player> getOnlineMembers(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return new ArrayList<>();

        List<Player> players = new ArrayList<>();

        for (FPlayer fPlayer : faction.getFPlayersWhereOnline(true)) {
            if (!((boolean) isAtLeast.invoke(getRole.invoke(fPlayer), recruit)) || isAlt(fPlayer.getPlayer())) continue;
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

    @SneakyThrows
    private boolean isAtLeastRecruitAndNotAlt(FPlayer fPlayer) {
        return (boolean) isAtLeast.invoke(getRole.invoke(fPlayer), recruit) && !isAlt(fPlayer.getPlayer());
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
        FPlayer fPlayer = (FPlayer) getByID.invoke(FPlayers.getInstance(), uuid);

        return fPlayer.hasFaction();
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
        try {
            return TranslatedRelation.valueOf(((Enum<?>) relationTo.invoke(fPlayer, faction)).name().toUpperCase());
        } catch (IllegalAccessException | InvocationTargetException e) {
            return TranslatedRelation.ENEMY;
        }
    }

    @Override
    public TranslatedRole getRole(Player player) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        try {
            return TranslatedRole.valueOf(((Enum<?>) getRole.invoke(fPlayer)).name().toUpperCase());
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return TranslatedRole.RECRUIT;
    }

    @Override
    public TranslatedRole getRole(OfflinePlayer player) {
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);
        try {
            return TranslatedRole.valueOf(((Enum<?>) getRole.invoke(fPlayer)).name().toUpperCase());
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return TranslatedRole.RECRUIT;
    }

    @SneakyThrows
    @Override
    public TranslatedRole getRole(UUID uuid) {
        FPlayer fPlayer = (FPlayer) getByID.invoke(FPlayers.getInstance(), uuid);
        try {
            return TranslatedRole.valueOf(((Enum<?>) getRole.invoke(fPlayer)).name().toUpperCase());
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return TranslatedRole.RECRUIT;
    }

    @Override
    public TranslatedRelation getFactionRelationToFaction(String id1, String id2) {
        Faction faction1 = Factions.getInstance().getFactionById(id1);
        Faction faction2 = Factions.getInstance().getFactionById(id2);
        if (faction1 == null || faction2 == null) return TranslatedRelation.ENEMY;
        try {
            return TranslatedRelation.valueOf(((Enum<?>) relationTo.invoke(faction1, faction2)).name().toUpperCase());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return TranslatedRelation.ENEMY;
    }

    @Override
    public TranslatedRelation getRelationToPlayer(Player player, Player player2) {
        FPlayer fPlayer1 = FPlayers.getInstance().getByPlayer(player);
        FPlayer fPlayer2 = FPlayers.getInstance().getByPlayer(player2);
        try {
            return TranslatedRelation.valueOf(((Enum<?>) relationTo.invoke(fPlayer1, fPlayer2)).name().toUpperCase());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return TranslatedRelation.ENEMY;
    }

    @SneakyThrows
    @Override
    public boolean isAlt(Player player) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        return (boolean) isAlt.invoke(fPlayer);
    }

    @EventHandler
    public void onPowerLoss(PowerLossEvent e) {
        KPowerLossEvent event = new KPowerLossEvent(e.getfPlayer().getPlayer());
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled())e.setCancelled(true);
    }

    public boolean hasPermission(String factionId, UUID playerUUID, String permission) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(Bukkit.getPlayer(playerUUID));
        return FactionsAPI.getInstance().getPermissionManager().hasPermission(faction, fPlayer, PTNormal.valueOf(permission));
    }

    @Override
    public List<UUID> getAllMembers(String id) {
        List<UUID> all = new ArrayList<>();
        for (Player p : getOnlineMembers(id)) {
            all.add(p.getUniqueId());
        }
        for (OfflinePlayer p : getOfflineMembers(id)) {
            all.add(p.getUniqueId());
        }
        return all;
    }

    public void addMoney(String factionId, double amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.addMoney(amount);
    }

    public void subtractMoney(String factionId, double amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.removeMoney(amount);
    }

    public void setMoney(String factionId, double amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.removeMoney(memoryFaction.getMoney());
        memoryFaction.addMoney(amount);
    }

    public double getMoney(String factionId) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return 0D;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        return memoryFaction.getMoney();
    }


    public void addMobcoins(String factionId, int amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.addMobcoins(amount);
    }

    public void subtractMobcoins(String factionId, int amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.removeMobcoins(amount);
    }

    public void setMobcoins(String factionId, double amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.removeMobcoins(memoryFaction.getMobcoins());
        memoryFaction.addMoney(amount);
    }

    public double getMobcoins(String factionId) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return 0D;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        return memoryFaction.getMobcoins();
    }

    public void addExp(String factionId, int amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.addExp(amount);
    }

    public void subtractExp(String factionId, int amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.removeExp(amount);
    }

    public void setExp(String factionId, int amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        memoryFaction.removeExp(memoryFaction.getExp());
        memoryFaction.addExp(amount);
    }

    public int getExp(String factionId) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null) return 0;
        MemoryFaction memoryFaction = (MemoryFaction) faction;
        return memoryFaction.getExp();
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
