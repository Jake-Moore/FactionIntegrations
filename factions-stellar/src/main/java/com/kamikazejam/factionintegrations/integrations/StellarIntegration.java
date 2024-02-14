package com.kamikazejam.factionintegrations.integrations;

import com.kamikazejam.factionintegrations.FactionIntegrations;
import com.kamikazejam.factionintegrations.event.KFactionCreateEvent;
import com.kamikazejam.factionintegrations.event.KLandClaimEvent;
import com.kamikazejam.factionintegrations.object.TranslatedRelation;
import com.kamikazejam.factionintegrations.object.TranslatedRole;
import com.massivecraft.factions.*;
import com.massivecraft.factions.cmd.CmdJoin;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.LandClaimEvent;
import com.massivecraft.factions.iface.RelationParticipator;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StellarIntegration extends UUIDIntegration implements KFaction {

    @EventHandler
    public void onFactionCreate(FactionCreateEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getPluginManager().callEvent(new KFactionCreateEvent(getTagFromId(e.getFactionTag())));
            }
        }.runTaskLater(FactionIntegrations.get(), 1);
    }

    @EventHandler
    public void onLandClaimEvent(LandClaimEvent e) {
        KLandClaimEvent event = new KLandClaimEvent(e.getFaction().getId(), e.getLocation().getWorldName(), new Integer[]{(int) e.getLocation().getX(), (int) e.getLocation().getZ()}, e.getfPlayer().getPlayer());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) e.setCancelled(true);
    }

    private final Method getLimit;
    private Method add;
    private Method take;
    private Method getBalance;

    private Method getRole;

    private Method getOnlinePlayers;

    private Method relationTo;

    private Method canBuild;

    private Method chestInventory;

    private Method getStrikes, setRole;

    private Object leader, coleader, moderator, normal, recruit;

    private Method maxMembers;

    private Field leaderP, coleaderP, moderatorP, normalP, recruitP;

    private Object cmdJoin;

    private Method setFaction;

    private Field econEnabled;

    public StellarIntegration() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Class<?> factionClass = Class.forName("com.massivecraft.factions.Faction");

        Class<?> fplayerClass = Class.forName("com.massivecraft.factions.FPlayer");

        setFaction = fplayerClass.getMethod("setFaction", Faction.class, boolean.class);

        getLimit = factionClass.getMethod("getMaxTnt");

        add = factionClass.getMethod("addTnt", int.class);

        chestInventory = factionClass.getMethod("getChestInventory");

        take = factionClass.getMethod("takeTnt", int.class);

        getBalance = factionClass.getMethod("getTnt");

        getStrikes = factionClass.getMethod("getStrikes");

        getRole = FPlayer.class.getMethod("getRole");

        relationTo = RelationParticipator.class.getMethod("getRelationTo", RelationParticipator.class);

        Class<?> roleClass = Class.forName("com.massivecraft.factions.struct.Role");

        maxMembers = CmdJoin.class.getDeclaredMethod("getFactionMemberLimit", Faction.class);

        Class<?> aClass = Class.forName("com.massivecraft.factions.SavageFactions");

        Object main = aClass.getField("plugin").get(null);

        Object cmdBase = aClass.getField("cmdBase").get(main);

        Field cmdJoinField = cmdBase.getClass().getField("cmdJoin");

        cmdJoin = cmdJoinField.get(cmdBase);

        setRole = FPlayer.class.getMethod("setRole", roleClass);

        maxMembers.setAccessible(true);

        for (Object type : roleClass.getEnumConstants()) {
            String toString = ((Enum<?>) type).name();
            switch (toString.toUpperCase()) {
                case "LEADER":
                case "ADMIN":
                    leader = type;
                    break;
                case "COLEADER":
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

        Class<?> confClass = Class.forName("com.massivecraft.factions.Conf");

        leaderP = confClass.getField("prefixLeader");
        coleaderP = confClass.getField("prefixCoLeader");
        moderatorP = confClass.getField("prefixMod");
        normalP = confClass.getField("prefixNormal");
        recruitP = confClass.getField("prefixRecruit");

        econEnabled = confClass.getField("econEnabled");

        getOnlinePlayers = Faction.class.getMethod("getOnlinePlayers");

        canBuild = Class.forName("com.massivecraft.factions.listeners.FactionsBlockListener").getMethod("playerCanBuildDestroyBlock", Player.class, Location.class, String.class, boolean.class);
        Class<?> permissableClass = Class.forName("com.massivecraft.factions.zcore.fperms.Permissable");
        Class<?> accessClass = Class.forName("com.massivecraft.factions.zcore.fperms.Access");

        Class<?> relationClass = Class.forName("com.massivecraft.factions.struct.Relation");

        valueOfRelation = relationClass.getMethod("valueOf", String.class);

        allowObject = accessClass.getMethod("valueOf", String.class).invoke(null, "ALLOW");

        Class<?> permissableActionClass = Class.forName("com.massivecraft.factions.zcore.fperms.PermissableAction");

        valueOf = permissableActionClass.getMethod("valueOf", String.class);

        setPermission = Faction.class.getMethod("setPermission", permissableClass, permissableActionClass, accessClass);
        setRelationWish = Faction.class.getMethod("setRelationWish", Faction.class, relationClass);
    }

    private Object allowObject;

    private Method valueOf;

    private Method valueOfRelation;

    private Method setPermission;

    private Method setRelationWish;

    @Override
    public void setTnT(String id, long amountL) {
        try {
            int amount = (int) amountL;
            Faction faction = Factions.getInstance().getFactionById(id);
            int limit = (int) getLimit.invoke(faction);

            take.invoke(faction, getBalance.invoke(faction));
            if (amount > limit) {
                add.invoke(faction, limit);
                return;
            }
            add.invoke(faction, amount);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Bukkit.getLogger().severe("Error setting tnt bank balance!");
        }
    }

    @Override
    public long addTnT(String id, long amountL) {
        try {
            int amount = (int) amountL;
            Faction faction = Factions.getInstance().getFactionById(id);
            int totalAmount = (int) getBalance.invoke(faction);

            int limit = (int) getLimit.invoke(faction);

            if (amount + totalAmount > limit) {
                add.invoke(faction, limit - totalAmount);
                return limit - totalAmount;
            }
            add.invoke(faction, amount);
            return amount;
        } catch (IllegalAccessException | InvocationTargetException e) {
            Bukkit.getLogger().severe("Error setting tnt bank balance!");
        }
        return 0;
    }

    @Override
    public long getTnT(String id) {
        try {
            Faction faction = Factions.getInstance().getFactionById(id);
            return (int) getBalance.invoke(faction);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Bukkit.getLogger().severe("Error getting tnt bank balance!");
        }
        return 0;
    }

    @Override
    public long getMaxTnt(String id) {
        try {
            Faction faction = Factions.getInstance().getFactionById(id);
            return (int) getLimit.invoke(faction);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Bukkit.getLogger().severe("Error getting tnt bank limit!");
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public int getStrikes(String id) {
        try {
            Faction faction = Factions.getInstance().getFactionById(id);

            if (faction == null) return 0;

            return ((List<?>) getStrikes.invoke(faction)).size();
        } catch (IllegalAccessException | InvocationTargetException e) {
            Bukkit.getLogger().severe("Error getting faction strikes!");
        }
        return 0;
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
        try {
            return (boolean) econEnabled.get(null);
        } catch (IllegalAccessException ignored) {

        }
        return false;
    }

    @Override
    public boolean canPlayerBuildThere(Player player, Chunk chunk) {
        if (isBypassing(player)) return true;

        Faction faction = Board.getInstance().getFactionAt(new FLocation(player.getWorld().getName(), chunk.getX(), chunk.getZ()));

        if (faction.isWilderness()) return true;

        return getRelationToFaction(player, faction.getId()).isEqualTo(TranslatedRelation.MEMBER);
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
    public boolean playerCanFlyThere(Player player, Location location) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        FLocation fLocation = new FLocation(location);
        try {
            return fPlayer.canFlyAtLocation(fLocation);
        } catch (NullPointerException exc) {
            // This isn't from fPlayer being null, there's a weird NPE being thrown from inside the canFlyAtLocation method.
            return true;
        }
    }

    @Override
    public int getMaxPlayers(String id) {
        try {
            return (int) maxMembers.invoke(cmdJoin, Factions.getInstance().getFactionById(id));
        } catch (IllegalAccessException | InvocationTargetException ignored) {

        }
        return 0;
    }

    @Override
    public boolean isInFactionChest(Player player, String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        try {
            return ((Inventory) chestInventory.invoke(faction)).getViewers().contains(player);
        } catch (IllegalAccessException | InvocationTargetException ignored) {

        }
        return false;
    }

    @Override
    public void setFaction(Player player, String id) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        if (id == null) {
            fPlayer.leave(false);
            return;
        }

        try {
            setFaction.invoke(fPlayer, Factions.getInstance().getFactionById(id), false);
        } catch (IllegalAccessException | InvocationTargetException ignored) {

        }
    }

    @Override
    public void setFaction(OfflinePlayer player, String id) {
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);

        if (id == null) {
            fPlayer.leave(false);
            return;
        }

        try {
            setFaction.invoke(fPlayer, Factions.getInstance().getFactionById(id), false);
        } catch (IllegalAccessException | InvocationTargetException ignored) {

        }
    }

    @Override
    public void setRole(Player player, TranslatedRole translatedRole) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        try {
            switch (translatedRole) {
                case RECRUIT:
                    setRole.invoke(fPlayer, recruit);
                    break;
                case NORMAL:
                    setRole.invoke(fPlayer, normal);
                    break;
                case MODERATOR:
                    setRole.invoke(fPlayer, moderator);
                    break;
                case COLEADER:
                    setRole.invoke(fPlayer, coleader);
                    break;
                case ADMIN:
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

    @Override
    public void setPermission(String id, TranslatedRelation relation, String permission, boolean b) {
        Faction faction = Factions.getInstance().getFactionById(id);

        try {
            Object rel = null;

            switch (relation) {
                case MEMBER:
                    rel = valueOfRelation.invoke(null, "MEMBER");
                    break;
                case ENEMY:
                    rel = valueOfRelation.invoke(null, "ENEMY");
                    break;
                case NEUTRAL:
                    rel = valueOfRelation.invoke(null, "NEUTRAL");
                    break;
                case ALLY:
                    rel = valueOfRelation.invoke(null, "ALLY");
                    break;
                case TRUCE:
                    rel = valueOfRelation.invoke(null, "TRUCE");
            }

            setPermission.invoke(faction, rel, valueOf.invoke(null, permission), allowObject);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public void setRelationWish(String id, String other, TranslatedRelation relation) {
        Faction a = Factions.getInstance().getFactionById(id);

        Faction b = Factions.getInstance().getFactionById(other);

        try {
            Object rel = null;

            switch (relation) {
                case MEMBER:
                    rel = valueOfRelation.invoke(null, "MEMBER");
                    break;
                case ENEMY:
                    rel = valueOfRelation.invoke(null, "ENEMY");
                    break;
                case NEUTRAL:
                    rel = valueOfRelation.invoke(null, "NEUTRAL");
                    break;
                case ALLY:
                    rel = valueOfRelation.invoke(null, "ALLY");
                    break;
                case TRUCE:
                    rel = valueOfRelation.invoke(null, "TRUCE");
            }

            setRelationWish.invoke(a, b, rel);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public Inventory getChestInventory(Player player, String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        try {
            return (Inventory) chestInventory.invoke(faction);
        } catch (IllegalAccessException | InvocationTargetException ignored) {

        }
        return null;
    }

    @Override
    public String getRolePrefix(TranslatedRole translatedRole) {
        String toReturn = "";
        try {
            switch (translatedRole.getValue()) {
                case 1:
                    toReturn = (String) recruitP.get(null);
                    break;
                case 2:
                    toReturn = (String) normalP.get(null);
                    break;
                case 3:
                    toReturn = (String) moderatorP.get(null);
                    break;
                case 4:
                    toReturn = (String) coleaderP.get(null);
                    break;
                case 5:
                    toReturn = (String) leaderP.get(null);
                    break;
            }
        } catch (IllegalAccessException ignored) {

        }
        return toReturn;
    }

    @Override
    public List<Player> getOnlineMembers(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return new ArrayList<>();

        List<Player> players = new ArrayList<>();

        for (FPlayer fPlayer : faction.getFPlayersWhereOnline(true)) {
            players.add(Bukkit.getPlayer(UUID.fromString(fPlayer.getId())));
        }
        return players;
    }

    @Override
    public List<OfflinePlayer> getOfflineMembers(String id) {
        List<OfflinePlayer> offliners = new ArrayList<>();

        Faction faction = Factions.getInstance().getFactionById(id);

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

    @Override
    public TranslatedRole getRole(UUID uuid) {
        FPlayer fPlayer = FPlayers.getInstance().getById(uuid.toString());
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
}
