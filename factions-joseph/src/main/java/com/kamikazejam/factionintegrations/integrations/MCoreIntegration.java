package com.kamikazejam.factionintegrations.integrations;

import com.golfing8.kore.event.RaidingOutpostResetEvent;
import com.kamikazejam.factionintegrations.FactionIntegrations;
import com.kamikazejam.factionintegrations.event.*;
import com.kamikazejam.factionintegrations.object.TranslatedRelation;
import com.kamikazejam.factionintegrations.object.TranslatedRole;
import com.kamikazejam.factionintegrations.shield.ShieldIntegration;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.engine.EnginePermBuild;
import com.massivecraft.factions.entity.*;
import com.massivecraft.factions.event.*;
import com.massivecraft.massivecore.ps.PS;
import gg.halcyon.upgrades.UpgradesManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@SuppressWarnings("deprecation")
public class MCoreIntegration implements MKFaction, ShieldIntegration {

    @EventHandler
    public void onReset(RaidingOutpostResetEvent event) {
        for (Faction faction : FactionColl.get().getAll()) {
            try {
                for (Integer slot : faction.getSandbots().keySet()) {
                    Sandbot sandbot = faction.getSandbots().get(slot);

                    if (sandbot.isDespawned()) continue;

                    if (sandbot.getLocation().getWorld() == event.getWorld()) {
                        faction.despawnSandbot(sandbot);
                    }
                }
            } catch (NoClassDefFoundError | NoSuchMethodError ignored) {

            }
        }
    }

    @EventHandler
    public void onFactionCreate(EventFactionsCreate create) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getPluginManager().callEvent(new KFactionCreateEvent(create.getFactionId()));
            }
        }.runTask(FactionIntegrations.get());
    }

    @EventHandler
    public void onFactionDisband(EventFactionsDisband create) {
        Bukkit.getPluginManager().callEvent(new KFactionDisbandEvent(create.getFactionId(), create.getFaction().getName()));
    }

    @EventHandler
    public void onFactionClaim(EventFactionsChunksChange create) {
        for (PS ps : create.getChunkType().keySet()) {
            EventFactionsChunkChangeType type = create.getChunkType().get(ps);

            Faction faction = create.getOldChunkFaction().get(ps);
            switch (type) {
                case BUY:
                    KLandClaimEvent event1 = new KLandClaimEvent(create.getNewFaction().getId(), ps.getWorld(), new Integer[]{ps.getChunkX(), ps.getChunkZ()}, create.getMPlayer().getPlayer());
                    Bukkit.getPluginManager().callEvent(event1);
                    if (event1.isCancelled()) create.setCancelled(true);
                    break;
                case SELL:
                case PILLAGE:
                case NONE:
                    KLandUnclaimEvent uEvent = new KLandUnclaimEvent(faction.getId(), ps.getWorld(), new Integer[]{ps.getChunkX(), ps.getChunkZ()}, create.getMPlayer().getPlayer());
                    Bukkit.getPluginManager().callEvent(uEvent);
                    if (uEvent.isCancelled()) create.setCancelled(true);
                    break;
                case CONQUER:
                    if (!create.getMPlayer().isOverriding()) {
                        //if(faction.getPower() > faction.getLandCount())break;
                    }
                    KLandClaimEvent event = new KLandClaimEvent(create.getNewFaction().getId(), ps.getWorld(), new Integer[]{ps.getChunkX(), ps.getChunkZ()}, create.getMPlayer().getPlayer());
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) create.setCancelled(true);
                    Bukkit.getPluginManager().callEvent(new KLandUnclaimEvent(faction.getId(), ps.getWorld(), new Integer[]{ps.getChunkX(), ps.getChunkZ()}, create.getMPlayer().getPlayer()));
                    break;
            }
        }
    }

    @EventHandler
    public void onPlayerJoinFaction(EventFactionsMembershipChange event) {
        switch (event.getReason()) {
            case CREATE:
            case JOIN:
                KPlayerJoinEvent event1 = new KPlayerJoinEvent(event.getNewFaction().getId(), event.getMPlayer().getId(), fromOther(event.getReason()));
                Bukkit.getPluginManager().callEvent(event1);

                if (event1.isCancelled()) event.setCancelled(true);
                break;
            case KICK:
            case LEAVE:
            case DISBAND:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(event.getNewFaction().getId(), event.getMPlayer().getId(), fromOther(event.getReason())));
                break;
        }
    }

    private KPlayerEvent.Reason fromOther(EventFactionsMembershipChange.MembershipChangeReason reason) {
        switch (reason) {
            case DISBAND:
                return KPlayerEvent.Reason.DISBAND;
            case CREATE:
                return KPlayerEvent.Reason.CREATE;
            case JOIN:
                return KPlayerEvent.Reason.JOIN;
            case KICK:
                return KPlayerEvent.Reason.KICK;
            case LEAVE:
                return KPlayerEvent.Reason.LEAVE;
        }
        return null;
    }

    @Override
    public void setTnT(String id, long amount) {
        Faction faction = FactionColl.get().get(id);
        long limit = getMaxTnt(id);
        if (amount > limit) {
            faction.setTnt((int) limit);
            return;
        }
        faction.setTnt((int) amount);
    }

    @Override
    public long addTnT(String id, long amount) {
        Faction faction = FactionColl.get().get(id);
        long totalAmount = getTnT(id);
        long limit = getMaxTnt(id);
        long allowedRoom = limit - totalAmount;
        if (amount + totalAmount > limit) {
            if (amount == totalAmount) return 0;
            faction.setTnt((int) limit);
            return allowedRoom;
        }
        faction.setTnt((int) (amount + totalAmount));
        return amount;
    }

    @Override
    public long getTnT(String id) {
        return FactionColl.get().get(id).getTnt();
    }

    @Override
    public long getMaxTnt(String id) {
        Faction faction = FactionColl.get().get(id);
        int i = faction.getLevel((MissionUpgradeConf.get()).tntUpgrade.getUpgradeName()) - 1;
        if (i < 0) return -1;
        return Integer.parseInt(UpgradesManager.get().getUpgradeByName((MissionUpgradeConf.get()).tntUpgrade.getUpgradeName()).getCurrentUpgradeDescription()[i].split(" ")[2].replaceAll(",", ""));
    }

    @Override
    public int getStrikes(String id) {
        Faction faction = FactionColl.get().get(id);
        if (faction == null) return 0;
        return faction.getFactionWarnings() == null ? 0 : faction.getFactionWarnings().size();
    }

    @Override
    public double getBalance(String id) {
        OfflinePlayer to = isUUID(id) ? Bukkit.getOfflinePlayer(UUID.fromString(id)) : Bukkit.getOfflinePlayer(id);

        return FactionIntegrations.getEconomy().getBalance(to);
    }

    @Override
    public void addBalance(String id, double add) {
        OfflinePlayer to = isUUID(id) ? Bukkit.getOfflinePlayer(UUID.fromString(id)) : Bukkit.getOfflinePlayer(id);

        FactionIntegrations.getEconomy().depositPlayer(to, add);
    }

    @Override
    public void subtractBalance(String id, double remove) {
        OfflinePlayer to = isUUID(id) ? Bukkit.getOfflinePlayer(UUID.fromString(id)) : Bukkit.getOfflinePlayer(id);

        FactionIntegrations.getEconomy().withdrawPlayer(to, remove);
    }

    @Override
    public boolean econEnabled() {
        return MConf.get().econEnabled;
    }

    @Override
    public boolean canPlayerBuildThere(Player player, Chunk chunk) {
        if (isBypassing(player)) return true;

        PS ps = PS.valueOf(player.getLocation());

        Faction faction = BoardColl.get().getFactionAt(ps);

        if (faction == null || faction == FactionColl.get().getNone()) return true;

        return getRelationToFaction(player, faction.getId()).isEqualTo(TranslatedRelation.MEMBER);
    }

    @Override
    public boolean isBaseRegion(Location location, String id) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        Faction faction = FactionColl.get().get(id);

        if (faction == null) return false;

        for (PS ps : faction.getBaseRegionPs()) {
            if (ps.getChunkX() == chunkX && ps.getChunkZ() == chunkZ) return true;
        }
        return false;
    }

    @Override
    public boolean isSystemFac(String id) {
        Faction checkFaction = FactionColl.get().get(id);

        if (checkFaction == null) return true;

        return checkFaction.isNone() || checkFaction == FactionColl.get().getWarzone() || checkFaction == FactionColl.get().getSafezone();
    }

    @Override
    public boolean playerCanBuildThere(Player player, Location location) {
        return EnginePermBuild.canPlayerBuildAt(MPlayerColl.get().get(player), PS.valueOf(location), false);
    }

    @Override
    public boolean isWarzoneAt(Location location) {
        Faction factionAt = BoardColl.get().getFactionAt(PS.valueOf(location));
        return factionAt != null && factionAt == FactionColl.get().getWarzone();
    }

    @Override
    public boolean isSafezoneAt(Location location) {
        Faction factionAt = BoardColl.get().getFactionAt(PS.valueOf(location));
        return factionAt != null && factionAt == FactionColl.get().getSafezone();
    }

    @Override
    public boolean isSOTW() {
        return false;
    }

    @Override
    public boolean playerCanFlyThere(Player player, Location location) {
        if (isBypassing(player)) return true;

        MPlayer fPlayer = MPlayerColl.get().get(player.getUniqueId());

        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(location));

        if (faction == null || faction == FactionColl.get().getNone()) return player.hasPermission("factions.wildfly");

        return MPerm.getPermFly().has(fPlayer, faction, false);
    }

    @Override
    public boolean isSystemFacAt(Location location) {
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(location));

        return faction == null || isSystemFac(faction.getId());
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public boolean isShieldActiveNow(String id) {
        return FactionColl.get().get(id).isShielded();
    }

    @Override
    public int getMaxClaimWorldborder(World world) {
        WorldBorder worldBorder = world.getWorldBorder();

        double radius = worldBorder.getSize() / 2;

        return (int) (radius % 16 == 0 ? radius / 16 : (radius / 16) + 1);
    }

    @Override
    public int getClaimsInWorld(String id, World world) {
        return (int) BoardColl.get().getChunks(FactionColl.get().get(id)).stream().filter(z -> z.getWorld().equals(world.getName())).count();
    }

    @Override
    public int getMaxPlayers(String id) {
        return MConf.get().factionMemberLimit;
    }

    @Override
    public int getSize(String id) {
        return FactionColl.get().get(id).getMPlayersWhere(z -> !z.isAlt()).size();
    }

    @Override
    public boolean isInFactionChest(Player player, String id) {
        Faction faction = FactionColl.get().get(id);
        return faction.getInventory().getViewers().contains(player);
    }

    @Override
    public boolean isFaction(String id) {
        return FactionColl.get().get(id) != null;
    }

    @Override
    public void setFaction(Player player, String id) {
        MPlayer mPlayer = MPlayerColl.get().get(player.getUniqueId());

        if (id == null) {
            mPlayer.leave();
            return;
        }

        mPlayer.setFaction(FactionColl.get().get(id));
    }

    @Override
    public void setFaction(OfflinePlayer player, String id) {
        MPlayer mPlayer = MPlayerColl.get().get(player.getUniqueId());

        if (id == null) {
            mPlayer.leave();
            return;
        }

        mPlayer.setFaction(FactionColl.get().get(id));
    }

    @Override
    public void setRole(Player player, TranslatedRole translatedRole) {
        MPlayer mPlayer = MPlayerColl.get().get(player.getUniqueId());

        switch (translatedRole.getValue()) {
            case 1:
                mPlayer.setRole(Rel.RECRUIT);
                break;
            case 2:
                mPlayer.setRole(Rel.MEMBER);
                break;
            case 3:
                mPlayer.setRole(Rel.OFFICER);
                break;
            case 4:
                mPlayer.setRole(Rel.COLEADER);
                break;
            case 5:
                mPlayer.setRole(Rel.LEADER);
                break;
        }
    }

    @Override
    public void setRole(OfflinePlayer player, TranslatedRole translatedRole) {
        MPlayer mPlayer = MPlayerColl.get().get(player.getUniqueId());

        switch (translatedRole.getValue()) {
            case 1:
                mPlayer.setRole(Rel.RECRUIT);
                break;
            case 2:
                mPlayer.setRole(Rel.MEMBER);
                break;
            case 3:
                mPlayer.setRole(Rel.OFFICER);
                break;
            case 4:
                mPlayer.setRole(Rel.COLEADER);
                break;
            case 5:
                mPlayer.setRole(Rel.LEADER);
                break;
        }
    }

    private MPerm applyMapping(String in) {
        switch (in) {
            case "BUILD":
            case "DESTROY":
                return MPerm.get("build");
            default:
                return MPerm.get(in.toLowerCase());
        }
    }

    @Override
    public void setPermission(String id, TranslatedRelation relation, String permission, boolean b) {
        Faction faction = FactionColl.get().get(id);

        MPerm mPerm = applyMapping(permission);

        if (mPerm == null) return;

        Set<Rel> relSet = faction.getPermitted(mPerm);

        switch (relation) {
            case MEMBER:
                if (b) {
                    relSet.add(Rel.MEMBER);
                } else {
                    relSet.remove(Rel.MEMBER);
                }
                faction.setPermittedRelations(mPerm, relSet);
                break;
            case ENEMY:
                if (b) {
                    relSet.add(Rel.ENEMY);
                } else {
                    relSet.remove(Rel.ENEMY);
                }
                faction.setPermittedRelations(mPerm, relSet);
                break;
            case NEUTRAL:
                if (b) {
                    relSet.add(Rel.NEUTRAL);
                } else {
                    relSet.remove(Rel.NEUTRAL);
                }
                faction.setPermittedRelations(mPerm, relSet);
                break;
            case ALLY:
                if (b) {
                    relSet.add(Rel.ALLY);
                } else {
                    relSet.remove(Rel.ALLY);
                }
                faction.setPermittedRelations(mPerm, relSet);
                break;
            case TRUCE:
                if (b) {
                    relSet.add(Rel.TRUCE);
                } else {
                    relSet.remove(Rel.TRUCE);
                }
                faction.setPermittedRelations(mPerm, relSet);
        }
    }

    @Override
    public void setRelationWish(String id, String other, TranslatedRelation relation) {
        Faction a = FactionColl.get().get(id);

        Faction b = FactionColl.get().get(other);

        switch (relation) {
            case MEMBER:
                a.setRelationWish(b, Rel.MEMBER);
                break;
            case ENEMY:
                a.setRelationWish(b, Rel.ENEMY);
                break;
            case NEUTRAL:
                a.setRelationWish(b, Rel.NEUTRAL);
                break;
            case ALLY:
                a.setRelationWish(b, Rel.ALLY);
                break;
            case TRUCE:
                a.setRelationWish(b, Rel.TRUCE);
        }
    }

    @Override
    public boolean isTagAvailable(String tag) {
        return !FactionColl.get().isNameTaken(tag);
    }

    @Override
    public void setFactionPower(String id, double power) {
        Faction faction = FactionColl.get().get(id);

        faction.setPowerBoost(power);
    }

    @Override
    public void clearAllClaimsInWorld(String world) {
        for (Faction faction : FactionColl.get().getAll()) {
            if (faction == null || faction.isNone()) continue;
            if (faction == FactionColl.get().getWarzone() || faction == FactionColl.get().getSafezone()) continue;
            for (PS ps : new ArrayList<>(BoardColl.get().getChunks(faction))) {
                if (ps.getWorld().equals(world)) BoardColl.get().removeAt(ps);
            }
        }
    }

    @Override
    public void setFactionAt(int cx, int cz, String world, String faction) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;

        Faction fac = FactionColl.get().get(faction);

        Player player = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            player = p;
            break;
        }
        BoardColl.get().setFactionAt(PS.valueOf(world, cx, cz), fac, player);
    }

    @Override
    public String createFaction(String tag) {
        Faction faction = FactionColl.get().create();

        faction.setName(tag);
        return faction.getId();
    }

    @Override
    public Set<String> getAllFactions() {
        Collection<Faction> factionCollection = FactionColl.get().getAll();

        Set<String> toReturn = new HashSet<>(factionCollection.size());

        for (Faction faction : factionCollection) {
            toReturn.add(faction.getId());
        }
        return toReturn;
    }

    @Override
    public Map<String, Set<Integer[]>> getAllClaims(String id) {
        Map<String, Set<Integer[]>> toReturn = new HashMap<>();

        for (PS ps : BoardColl.get().getChunks(id)) {
            toReturn.putIfAbsent(ps.getWorld(), new HashSet<>());

            toReturn.get(ps.getWorld()).add(new Integer[]{
                    ps.getChunkX(), ps.getChunkZ()
            });
        }
        return toReturn;
    }

    @Override
    public Inventory getChestInventory(Player player, String id) {
        Faction faction = FactionColl.get().get(id);
        try {
            return faction.getInventory();
        } catch (Throwable ignored) {

        }
        return null;
    }

    @Override
    public String getPlayerFactionId(Player player) {
        return MPlayerColl.get().get(player.getUniqueId()).getFaction().getId();
    }

    @Override
    public String getPlayerFactionId(OfflinePlayer player) {
        return MPlayerColl.get().get(player.getUniqueId()).getFaction().getId();
    }

    @Override
    public String getFactionsIdAt(Location location) {
        Faction factionAt = BoardColl.get().getFactionAt(PS.valueOf(location));

        if (factionAt == null) return "none";

        return factionAt.getId();
    }

    @Override
    public String getFactionsIdAt(Chunk chunk) {
        Faction factionAt = BoardColl.get().getFactionAt(PS.valueOf(chunk));

        if (factionAt == null) return "none";

        return factionAt.getId();
    }

    @Override
    public String getFactionsIdAt(Integer[] coordinates, World world) {
        Faction factionAt = BoardColl.get().getFactionAt(PS.valueOf(world.getName(), coordinates[0], coordinates[1]));

        if (factionAt == null) return "none";

        return factionAt.getId();
    }

    @Override
    public String getTagFromId(String id) {
        if (!isFaction(id)) return "None";
        Faction factionById = FactionColl.get().get(id);
        if (factionById == null) return "None";
        return factionById.getName();
    }

    @Override
    public String getIdFromTag(String tag) {
        Faction byTag = FactionColl.get().getByName(tag);
        if (byTag == null) return null;
        return byTag.getId();
    }

    @Override
    public String getWilderness() {
        return FactionColl.get().getNone().getId();
    }

    @Override
    public String getRolePrefix(TranslatedRole translatedRole) {
        Rel rel;
        switch (translatedRole.getValue()) {
            case 1:
                rel = Rel.RECRUIT;
                break;
            case 2:
                rel = Rel.MEMBER;
                break;
            case 3:
                rel = Rel.OFFICER;
                break;
            case 4:
                rel = Rel.COLEADER;
                break;
            case 5:
                rel = Rel.LEADER;
                break;
            default:
                rel = Rel.RECRUIT;
        }
        return rel.getPrefix();
    }

    @Override
    public List<Player> getOnlineMembers(String id) {
        if (!isFaction(id)) return new ArrayList<>();
        Faction faction = FactionColl.get().get(id);
        List<Player> toReturn = new ArrayList<>();
        faction.getMPlayersWhere(z -> !z.isAlt() && z.isOnline()).forEach(z -> toReturn.add(z.getPlayer()));
        return toReturn;
    }

    @Override
    public List<OfflinePlayer> getOfflineMembers(String id) {
        Faction faction = FactionColl.get().get(id);
        List<MPlayer> mPlayersWhereOnline = faction.getMPlayersWhere(z -> !z.isAlt() && !z.isOnline());

        List<OfflinePlayer> players = new ArrayList<>();

        mPlayersWhereOnline.forEach(z -> players.add(Bukkit.getOfflinePlayer(UUID.fromString(z.getId()))));
        return players;
    }

    @Override
    public UUID getLeader(String id) {
        Faction faction = FactionColl.get().get(id);

        if (faction == null) return null;

        String uuid = faction.getLeader().getId();

        return UUID.fromString(uuid);
    }

    @Override
    public OfflinePlayer getOfflineLeader(String id) {
        Faction faction = FactionColl.get().get(id);

        if (faction == null) return null;

        MPlayer fPlayerAdmin = faction.getLeader();

        if (fPlayerAdmin == null) return null;

        String uuid = fPlayerAdmin.getId();

        UUID id1 = UUID.fromString(uuid);

        return Bukkit.getOfflinePlayer(id1);
    }

    @Override
    public boolean hasFaction(Player player) {
        MPlayer mPlayer = MPlayerColl.get().get(player.getUniqueId());
        return mPlayer.hasFaction() && !mPlayer.isAlt();
    }

    @Override
    public boolean hasFaction(OfflinePlayer player) {
        return MPlayerColl.get().get(player.getUniqueId()).hasFaction();
    }

    @Override
    public boolean hasFaction(UUID uuid) {
        MPlayer mPlayer = MPlayerColl.get().get(uuid);
        return mPlayer.hasFaction() && !mPlayer.isAlt();
    }

    @Override
    public boolean isBypassing(Player player) {
        return MPlayerColl.get().get(player.getUniqueId()).isOverriding();
    }

    @Override
    public boolean isWildernessAt(Location location) {
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(location));
        return faction == null || faction == FactionColl.get().getNone();
    }

    @Override
    public TranslatedRelation getRelationToFaction(Player player, String id) {
        Faction faction = FactionColl.get().get(id);

        if (faction == null) return TranslatedRelation.NEUTRAL;

        MPlayer observer = MPlayerColl.get().get(player.getUniqueId());

        if (observer == null) return TranslatedRelation.NEUTRAL;
        switch (faction.getRelationTo(observer)) {
            case OFFICER:
            case COLEADER:
            case LEADER:
            case MEMBER:
            case RECRUIT:
                return TranslatedRelation.MEMBER;
            case TRUCE:
                return TranslatedRelation.TRUCE;
            case ALLY:
                return TranslatedRelation.ALLY;
            case NEUTRAL:
                return TranslatedRelation.NEUTRAL;
            case ENEMY:
                return TranslatedRelation.ENEMY;
        }
        return TranslatedRelation.NEUTRAL;
    }

    @Override
    public TranslatedRole getRole(Player player) {
        switch (MPlayerColl.get().get(player.getUniqueId()).getRole()) {
            case OFFICER:
                return TranslatedRole.MODERATOR;
            case COLEADER:
                return TranslatedRole.COLEADER;
            case LEADER:
                return TranslatedRole.LEADER;
            case MEMBER:
                return TranslatedRole.MEMBER;
            case RECRUIT:
                return TranslatedRole.RECRUIT;
        }
        return TranslatedRole.RECRUIT;
    }

    @Override
    public TranslatedRole getRole(OfflinePlayer player) {
        switch (MPlayerColl.get().get(player.getUniqueId()).getRole()) {
            case OFFICER:
                return TranslatedRole.MODERATOR;
            case COLEADER:
                return TranslatedRole.COLEADER;
            case LEADER:
                return TranslatedRole.LEADER;
            case MEMBER:
                return TranslatedRole.MEMBER;
            case RECRUIT:
                return TranslatedRole.RECRUIT;
        }
        return TranslatedRole.RECRUIT;
    }

    @Override
    public TranslatedRole getRole(UUID uuid) {
        switch (MPlayerColl.get().get(uuid).getRole()) {
            case OFFICER:
                return TranslatedRole.MODERATOR;
            case COLEADER:
                return TranslatedRole.COLEADER;
            case LEADER:
                return TranslatedRole.LEADER;
            case MEMBER:
                return TranslatedRole.MEMBER;
            case RECRUIT:
                return TranslatedRole.RECRUIT;
        }
        return TranslatedRole.RECRUIT;
    }

    @Override
    public TranslatedRelation getFactionRelationToFaction(String id1, String id2) {
        Faction faction1 = FactionColl.get().get(id1);
        Faction faction2 = FactionColl.get().get(id2);
        if (faction1 == null || faction2 == null) return TranslatedRelation.ENEMY;
        switch (faction1.getRelationTo(faction2)) {
            case OFFICER:
            case COLEADER:
            case LEADER:
            case MEMBER:
            case RECRUIT:
                return TranslatedRelation.MEMBER;
            case TRUCE:
                return TranslatedRelation.TRUCE;
            case ALLY:
                return TranslatedRelation.ALLY;
            case NEUTRAL:
                return TranslatedRelation.NEUTRAL;
            case ENEMY:
                return TranslatedRelation.ENEMY;
        }
        return TranslatedRelation.ENEMY;
    }

    @Override
    public TranslatedRelation getRelationToPlayer(Player player, Player player2) {
        MPlayer mplayer1 = MPlayerColl.get().get(player.getUniqueId());
        MPlayer mplayer2 = MPlayerColl.get().get(player2.getUniqueId());
        switch (mplayer1.getRelationTo(mplayer2)) {
            case OFFICER:
            case COLEADER:
            case LEADER:
            case MEMBER:
            case RECRUIT:
                return TranslatedRelation.MEMBER;
            case TRUCE:
                return TranslatedRelation.TRUCE;
            case ALLY:
                return TranslatedRelation.ALLY;
            case NEUTRAL:
                return TranslatedRelation.NEUTRAL;
            case ENEMY:
                return TranslatedRelation.ENEMY;
        }
        return TranslatedRelation.ENEMY;
    }

    @Override
    public void setOpen(String factionId, boolean open) {
        FactionColl.get().get(factionId).setOpen(open);
    }

    @Override
    public void setPermanent(String factionId, boolean permanent) {
        // Not supported
    }

    @Override
    public String getDescription(String factionId) {
        return FactionColl.get().get(factionId).getDescription();
    }

    @Override
    public void setDescription(String factionId, String description) {
        FactionColl.get().get(factionId).setDescription(description);
    }

    @Override
    public String getWarzone() {
        return FactionColl.get().getWarzone().getId();
    }

    @Override
    public String getSafezone() {
        return FactionColl.get().getSafezone().getId();
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
    public void onPowerLoss(EventFactionsPowerChange e) {
        double curr = e.getMPlayer().getPower();
        if (e.getNewPower() >= curr) { return; }

        KPowerLossEvent event = new KPowerLossEvent(e.getMPlayer().getPlayer());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) e.setCancelled(true);
    }
}

