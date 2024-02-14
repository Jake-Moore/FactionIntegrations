package com.kamikazejam.factionintegrations.integrations;

import com.kamikazejam.factionintegrations.event.*;
import com.kamikazejam.factionintegrations.object.TranslatedRelation;
import com.kamikazejam.factionintegrations.object.TranslatedRole;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.*;
import com.massivecraft.factions.entity.internal.Rank;
import com.massivecraft.factions.event.*;
import com.massivecraft.massivecore.money.Money;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class Factions1_20Integration implements MKFaction {

    private final Plugin plugin;

    public Factions1_20Integration(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFactionCreate(EventFactionsCreate create) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getPluginManager().callEvent(new KFactionCreateEvent(create.getFactionId()));
            }
        }.runTask(plugin);
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
                    Bukkit.getPluginManager().callEvent(new KLandUnclaimEvent(faction.getId(), ps.getWorld(), new Integer[]{ps.getChunkX(), ps.getChunkZ()}, create.getMPlayer().getPlayer()));
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

    private Rel fromTranslatedRelation(TranslatedRelation relation) {
        switch (relation) {
            case MEMBER:
                return Rel.FACTION;
            case ALLY:
                return Rel.ALLY;
            case ENEMY:
                return Rel.ENEMY;
            case TRUCE:
                return Rel.TRUCE;
            case NEUTRAL:
                return Rel.NEUTRAL;
            default:
                throw new IllegalArgumentException();
        }
    }

    private TranslatedRelation fromRel(Rel rel) {
        switch (rel) {
            case FACTION:
                return TranslatedRelation.MEMBER;
            case ALLY:
                return TranslatedRelation.ALLY;
            case ENEMY:
                return TranslatedRelation.ENEMY;
            case TRUCE:
                return TranslatedRelation.TRUCE;
            case NEUTRAL:
                return TranslatedRelation.NEUTRAL;
            case KINGDOM:
                throw new IllegalArgumentException("Rel value " + rel.getName() + " not recognized.");
            default:
                throw new IllegalArgumentException();
        }
    }

    private Rank fromTranslatedRole(TranslatedRole role) {
        Rank rank = MConf.get().defaultRanks.stream().filter(r -> r.getPriority() == role.getValue() * 100).findFirst().orElse(null);
        if (rank == null) {
            throw new NoSuchElementException();
        }
        return rank;
    }

    private TranslatedRole fromRank(Rank rank) {
        switch (rank.getPriority() / 100) {
            case 5:
                return TranslatedRole.LEADER;
            case 4:
                return TranslatedRole.COLEADER;
            case 3:
                return TranslatedRole.OFFICER;
            case 2:
                return TranslatedRole.MEMBER;
            default:
                return TranslatedRole.RECRUIT;
        }
    }

    @Override
    public double getBalance(String id) {
        Faction faction = Faction.get(id);
        return Money.exists(faction) ? Money.get(faction) : 0.0D;
    }

    @Override
    public void addBalance(String id, double add) {
        Faction faction = Faction.get(id);
        if (Money.exists(faction)) {
            Money.set(faction, null, Money.get(faction) + add, "Factions");
        }
    }

    @Override
    public void subtractBalance(String id, double remove) {
        Faction faction = Faction.get(id);
        if (Money.exists(faction)) {
            Money.set(faction, null, Money.get(faction) - remove, "Factions");
        }
    }

    @Override
    public boolean econEnabled() {
        return true;
    }

    @Override
    public boolean canPlayerBuildThere(Player player, Chunk chunk) {
        if (isBypassing(player)) return true;

        PS ps = PS.valueOf(chunk);

        Faction faction = BoardColl.get().getFactionAt(ps);

        if (faction == null || faction == FactionColl.get().getNone()) return true;

        return faction.isPlayerPermitted(MPlayer.get(player), MPerm.getPermBuild());
    }

    @Override
    public boolean isBaseRegion(Location location, String id) {
        Faction faction = Faction.get(id);

        return faction.isPSinMainBase(PS.valueOf(location));
    }

    @Override
    public boolean isSystemFac(String id) {
        Faction faction = Faction.get(id);
        return faction.isSystemFaction();
    }

    @Override
    public boolean playerCanBuildThere(Player player, Location location) {
        if (isBypassing(player)) return true;

        PS ps = PS.valueOf(location);

        Faction faction = BoardColl.get().getFactionAt(ps);

        if (faction == null || faction == FactionColl.get().getNone()) return true;

        return faction.isPlayerPermitted(MPlayer.get(player), MPerm.getPermBuild());
    }

    @Override
    public boolean isWarzoneAt(Location location) {
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(location));
        return faction == FactionColl.get().getWarzone();
    }

    @Override
    public boolean isSafezoneAt(Location location) {
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(location));
        return faction == FactionColl.get().getSafezone();
    }

    @Override
    public boolean isSOTW() {
        return false;
    }

    @Override
    public boolean playerCanFlyThere(Player player, Location location) {
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(location));
        return faction.isPlayerPermitted(MPlayer.get(player), MPerm.getPermFly());
    }

    @Override
    public boolean isSystemFacAt(Location location) {
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(location));
        return faction.isSystemFaction();
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public boolean isShieldActiveNow(String id) {
        Faction faction = Faction.get(id);
        return faction.getCore().isShieldOn();
    }

    @Override
    public int getMaxClaimWorldborder(World world) {
        WorldBorder worldBorder = world.getWorldBorder();

        double radius = worldBorder.getSize() / 2;

        return (int) (radius % 16 == 0 ? radius / 16 : (radius / 16) + 1);
    }

    @Override
    public int getClaimsInWorld(String id, World world) {
        Faction faction = Faction.get(id);
        return BoardColl.get().get(world.getName()).getCount(faction);
    }

    @Override
    public int getMaxPlayers(String id) {
        return MConf.get().factionMemberLimit;
    }

    @Override
    public int getSize(String id) {
        Faction faction = Faction.get(id);
        return faction.getMPlayers().size();
    }

    @Override
    public boolean isInFactionChest(Player player, String id) {
        Faction faction = Faction.get(id);
        return faction.getChest().getViewers().contains(player);
    }

    @Override
    public boolean isFaction(String id) {
        return Faction.get(id) != null;
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
        Faction faction = mPlayer.getFaction();
        if (faction == null)
            return;

        Rank rank = MConf.get().defaultRanks.stream().filter(r -> r.getPriority() == translatedRole.getValue() * 100).findFirst().orElse(null);
        if (rank == null) {
            throw new NoSuchElementException();
        }
        mPlayer.setRank(rank);
    }

    @Override
    public void setRole(OfflinePlayer offlinePlayer, TranslatedRole translatedRole) {
        MPlayer mPlayer = MPlayerColl.get().get(offlinePlayer.getUniqueId());
        Faction faction = mPlayer.getFaction();
        if (faction == null)
            return;

        Rank rank = MConf.get().defaultRanks.stream().filter(r -> r.getPriority() == translatedRole.getValue() * 100).findFirst().orElse(null);
        if (rank == null) {
            throw new NoSuchElementException();
        }
        mPlayer.setRank(rank);
    }

    @Override
    public void setPermission(String id, TranslatedRelation relation, String permission, boolean b) {
        MPerm perm = MPerm.get(permission);
        if (perm == null) {
            String s = "MPerm " + permission + " not recognized.";
            Bukkit.getServer().getLogger().warning(s);
            return;
        }

        Faction faction = Faction.get(id);
        faction.setPermitted(fromTranslatedRelation(relation), perm, b);
    }

    @Override
    public void setRelationWish(String id, String other, TranslatedRelation relation) {
        Faction fOne = Faction.get(id);
        Faction fTwo = Faction.get(other);

        fOne.setRelationWish(fTwo, fromTranslatedRelation(relation));
    }

    @Override
    public boolean isTagAvailable(String tag) {
        return FactionColl.get().getByName(tag) != null;
    }

    @Override
    public void setFactionPower(String id, double power) {
        Faction faction = Faction.get(id);
        faction.setPowerBoost(power);
    }

    @Override
    public void clearAllClaimsInWorld(String world) {
        BoardColl.get().get(world).getMapRaw().clear();
        BoardColl.get().get(world).changed();
    }

    @Override
    public void setFactionAt(int cx, int cz, String world, String faction) {
        Board board = BoardColl.get().get(world);
        board.setFactionAt(PS.valueOf(cx, cz), Faction.get(faction));
    }

    @Override
    public String createFaction(String tag) {
        Faction faction = FactionColl.get().create();
        faction.setName(tag);
        faction.changed();
        return faction.getId();
    }

    @Override
    public Set<String> getAllFactions() {
        return FactionColl.get().getAll().stream().map(Faction::getId).collect(Collectors.toSet());
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
            return faction.getChest();
        } catch (Throwable ignored) {

        }
        return null;
    }

    @Override
    public String getPlayerFactionId(Player player) {
        MPlayer mPlayer = MPlayer.get(player);
        return mPlayer.hasFaction() ? mPlayer.getFaction().getId() : null;
    }

    @Override
    public String getPlayerFactionId(OfflinePlayer player) {
        MPlayer mPlayer = MPlayer.get(player);
        return mPlayer.hasFaction() ? mPlayer.getFaction().getId() : null;
    }

    @Override
    public String getFactionsIdAt(Location location) {
        Faction factionAt = BoardColl.get().getFactionAt(PS.valueOf(location));
        return factionAt.getId();
    }

    @Override
    public String getFactionsIdAt(Chunk chunk) {
        Faction factionAt = BoardColl.get().getFactionAt(PS.valueOf(chunk));
        return factionAt.getId();
    }

    @Override
    public String getFactionsIdAt(Integer[] coordinates, World world) {
        Board board = BoardColl.get().get(world.getName());
        Faction faction = board.getFactionAt(PS.valueOf(coordinates[0], coordinates[1]));
        return faction.getId();
    }

    @Override
    public String getTagFromId(String id) {
        if (!isFaction(id)) return "None";
        Faction faction = Faction.get(id);
        return faction.getName();
    }

    @Override
    public String getIdFromTag(String tag) {
        Faction byName = FactionColl.get().getByName(tag);
        if (byName == null)
            return null;
        return byName.getId();
    }

    @Override
    public String getWilderness() {
        return FactionColl.get().getNone().getId();
    }

    @Override
    public String getRolePrefix(TranslatedRole translatedRole) {
        Rank rank = fromTranslatedRole(translatedRole);
        return rank.getPrefix();
    }

    @Override
    public List<Player> getOnlineMembers(String id) {
        Faction faction = Faction.get(id);
        if (faction.isNone())
            return Collections.emptyList();
        return faction.getMPlayersWhereOnline(true).stream().map(MPlayer::getPlayer).collect(Collectors.toList());
    }

    @Override
    public List<OfflinePlayer> getOfflineMembers(String id) {
        Faction faction = Faction.get(id);
        if (faction.isNone())
            return Collections.emptyList();
        return faction.getMPlayersWhereOnline(false).stream().map(mp -> Bukkit.getOfflinePlayer(UUID.fromString(mp.getId()))).collect(Collectors.toList());
    }

    @Override
    public UUID getLeader(String id) {
        Faction faction = Faction.get(id);
        if (faction.isNone())
            return null;

        MPlayer leader = faction.getLeader();
        return leader.getUuid();
    }

    @Override
    public OfflinePlayer getOfflineLeader(String id) {
        Faction faction = Faction.get(id);
        if (faction.isNone())
            return null;

        MPlayer leader = faction.getLeader();
        return leader != null ? Bukkit.getOfflinePlayer(UUID.fromString(leader.getId())) : null;
    }

    @Override
    public boolean hasFaction(Player player) {
        return MPlayer.get(player).hasFaction();
    }

    @Override
    public boolean hasFaction(OfflinePlayer player) {
        return MPlayer.get(player).hasFaction();
    }

    @Override
    public boolean hasFaction(UUID uuid) {
        return MPlayer.get(uuid).hasFaction();
    }

    @Override
    public boolean isBypassing(Player player) {
        return MPlayer.get(player).isOverriding();
    }

    @Override
    public boolean isWildernessAt(Location location) {
        return BoardColl.get().getFactionAt(PS.valueOf(location)).isNone();
    }

    @Override
    public TranslatedRelation getRelationToFaction(Player player, String id) {
        Faction faction = Faction.get(id);
        return fromRel(faction.getRelationTo(MPlayer.get(player)));
    }

    @Override
    public TranslatedRole getRole(Player player) {
        return fromRank(MPlayer.get(player).getRank());
    }

    @Override
    public TranslatedRole getRole(OfflinePlayer player) {
        return fromRank(MPlayer.get(player).getRank());
    }

    @Override
    public TranslatedRole getRole(UUID uuid) {
        return fromRank(MPlayer.get(uuid).getRank());
    }

    @Override
    public TranslatedRelation getFactionRelationToFaction(String id1, String id2) {
        Faction fOne = Faction.get(id1);
        Faction fTwo = Faction.get(id2);
        return fromRel(fOne.getRelationTo(fTwo));
    }

    @Override
    public TranslatedRelation getRelationToPlayer(Player player, Player player2) {
        if (player.hasMetadata("NPC") || player2.hasMetadata("NPC"))
            return TranslatedRelation.NEUTRAL;

        MPlayer mpOne = MPlayer.get(player);
        MPlayer mpTwo = MPlayer.get(player2);
        return fromRel(mpOne.getRelationTo(mpTwo));
    }

    @Override
    public void setOpen(String factionId, boolean open) {
        Faction.get(factionId).setOpen(open);
    }

    @Override
    public void setPermanent(String factionId, boolean permanent) {
        // Not supported
    }

    @Override
    public String getDescription(String factionId) {
        return Faction.get(factionId).getDescription();
    }

    @Override
    public void setDescription(String factionId, String description) {
        Faction.get(factionId).setDescription(description);
    }

    @Override
    public String getWarzone() {
        return FactionColl.get().getWarzone().getId();
    }

    @Override
    public String getSafezone() {
        return FactionColl.get().getSafezone().getId();
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

