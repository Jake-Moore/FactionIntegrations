package com.kamikazejam.factionintegrations.integrations;

import com.kamikazejam.factionintegrations.FactionIntegrations;
import com.kamikazejam.factionintegrations.event.*;
import com.kamikazejam.factionintegrations.integrations.interfaces.KFaction;
import com.kamikazejam.factionintegrations.object.TranslatedRelation;
import com.kamikazejam.factionintegrations.object.TranslatedRole;
import com.massivecraft.factions.*;
import com.massivecraft.factions.cmd.CmdJoin;
import com.massivecraft.factions.cmd.FCmdRoot;
import com.massivecraft.factions.event.*;
import com.massivecraft.factions.iface.RelationParticipator;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class DominionIntegration implements KFaction {

    @EventHandler
    public void onFactionCreate(FactionCreateEvent e){
        new BukkitRunnable(){
            @Override
            public void run() {
                Bukkit.getPluginManager().callEvent(new KFactionCreateEvent(getTagFromId(e.getFactionTag())));
            }
        }.runTaskLater(FactionIntegrations.get(), 1);
    }

    @EventHandler
    public void onFactionDisband(FactionDisbandEvent e){
        Bukkit.getPluginManager().callEvent(new KFactionDisbandEvent(e.getFaction().getId(), e.getFaction().getTag()));
    }

    @EventHandler
    public void onLandClaimEvent(LandClaimEvent e){
        KLandClaimEvent event = new KLandClaimEvent(e.getFaction().getId(), e.getLocation().getWorldName(), new Integer[]{(int) e.getLocation().getX(), (int) e.getLocation().getZ()}, e.getfPlayer().getPlayer());
        Bukkit.getPluginManager().callEvent(event);

        if(event.isCancelled())e.setCancelled(true);
    }

    @EventHandler
    public void onLandUnclaimEvent(LandUnclaimEvent e){
        KLandUnclaimEvent event = new KLandUnclaimEvent(e.getFaction().getId(), e.getLocation().getWorldName(), new Integer[] {(int) e.getLocation().getX(), (int) e.getLocation().getZ()}, e.getfPlayer().getPlayer());
        if (!event.callEvent()) { e.setCancelled(true); }
    }

    @EventHandler
    public void onLandUnclaimallEvent(LandUnclaimAllEvent e){
        KLandUnclaimallEvent event = new KLandUnclaimallEvent(e.getFaction().getId(), "None", new Integer[] {0, 0}, e.getfPlayer().getPlayer());
        if (!event.callEvent()) { e.setCancelled(true); }
    }

    @EventHandler
    public void onJoin(FPlayerJoinEvent e){
        switch(e.getReason()){
            case CREATE:
                KPlayerJoinEvent event2 = new KPlayerJoinEvent(e.getFaction().getId(), e.getfPlayer().getId(), KPlayerEvent.Reason.CREATE);
                Bukkit.getPluginManager().callEvent(event2);

                if(event2.isCancelled())e.setCancelled(true);
                break;
            case COMMAND:
                KPlayerJoinEvent event1 = new KPlayerJoinEvent(e.getFaction().getId(), e.getfPlayer().getId(), KPlayerEvent.Reason.COMMAND);
                Bukkit.getPluginManager().callEvent(event1);

                if(event1.isCancelled())e.setCancelled(true);
                break;
            case LEADER:
                KPlayerJoinEvent event = new KPlayerJoinEvent(e.getFaction().getId(), e.getfPlayer().getId(), KPlayerEvent.Reason.LEADER);
                Bukkit.getPluginManager().callEvent(event);

                if(event.isCancelled())e.setCancelled(true);
                break;
        }
    }

    @EventHandler
    public void onJoin(FPlayerLeaveEvent e){
        if(e.getFaction().getSize() == 1){
            Bukkit.getPluginManager().callEvent(new KFactionDisbandEvent(e.getFaction().getId(), e.getFaction().getTag()));
        }
        switch(e.getReason()){
            case DISBAND:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getName(), KPlayerEvent.Reason.DISBAND));
                break;
            case LEAVE:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getName(), KPlayerEvent.Reason.LEAVE));
                break;
            case RESET:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getName(), KPlayerEvent.Reason.RESET));
                break;
            case JOINOTHER:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getName(), KPlayerEvent.Reason.JOINOTHER));
                break;
            default:
                Bukkit.getPluginManager().callEvent(new KPlayerLeaveEvent(e.getFaction().getId(), e.getfPlayer().getName(), KPlayerEvent.Reason.KICK));
                break;
        }
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

    public DominionIntegration() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
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

        // Class<?> aClass = Class.forName("com.massivecraft.factions.SavageFactions");

        SavageFactions main = SavageFactions.plugin;

        FCmdRoot cmdBase = main.cmdBase;

        cmdJoin = cmdBase.cmdJoin;

        setRole = FPlayer.class.getMethod("setRole", roleClass);

        maxMembers.setAccessible(true);

        for(Object type : roleClass.getEnumConstants()){
            String toString = ((Enum<?>) type).name();
            switch(toString.toUpperCase()){
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
    public void setTnT(String id, int amount) {
        try{
            Faction faction = Factions.getInstance().getFactionById(id);
            int limit = (int) getLimit.invoke(faction);

            take.invoke(faction, getBalance.invoke(faction));
            if(amount > limit){
                add.invoke(faction, limit);
                return;
            }
            add.invoke(faction, amount);
        }catch(IllegalAccessException | InvocationTargetException e){
            Bukkit.getLogger().severe("Error setting tnt bank balance!");
        }
    }

    @Override
    public int addTnT(String id, int amount) {
        try{
            Faction faction = Factions.getInstance().getFactionById(id);
            int totalAmount = (int) getBalance.invoke(faction);

            int limit = (int) getLimit.invoke(faction);

            if(amount + totalAmount > limit){
                add.invoke(faction, limit - totalAmount);
                return limit - totalAmount;
            }
            add.invoke(faction, amount);
            return amount;
        }catch(IllegalAccessException | InvocationTargetException e){
            Bukkit.getLogger().severe("Error setting tnt bank balance!");
        }
        return 0;
    }

    @Override
    public int getTnT(String id) {
        try{
            Faction faction = Factions.getInstance().getFactionById(id);
            return (int) getBalance.invoke(faction);
        }catch(IllegalAccessException | InvocationTargetException e){
            Bukkit.getLogger().severe("Error getting tnt bank balance!");
        }
        return 0;
    }

    @Override
    public int getMaxTnt(String id) {
        try{
            Faction faction = Factions.getInstance().getFactionById(id);
            return (int) getLimit.invoke(faction);
        }catch(IllegalAccessException | InvocationTargetException e){
            Bukkit.getLogger().severe("Error getting tnt bank limit!");
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public int getStrikes(String id){
        try{
            Faction faction = Factions.getInstance().getFactionById(id);

            if(faction == null)return 0;

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
    public boolean econEnabled(){
        try{
            return (boolean) econEnabled.get(null);
        }catch(IllegalAccessException ignored){

        }
        return false;
    }

    @Override
    public boolean canPlayerBuildThere(Player player, Chunk chunk) {
        if(isBypassing(player))return true;

        Faction faction = Board.getInstance().getFactionAt(new FLocation(player.getWorld().getName(), chunk.getX(), chunk.getZ()));

        if(faction.isWilderness())return true;

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
        try{
            return (boolean) canBuild.invoke(null, player, location, "build", true);
        }catch(IllegalAccessException | InvocationTargetException e){
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

    @Override
    public int getMaxPlayers(String id) {
        try{
            return (int) maxMembers.invoke(cmdJoin, Factions.getInstance().getFactionById(id));
        }catch (IllegalAccessException | InvocationTargetException ignored){

        }
        return 0;
    }

    @Override
    public int getSize(String id) {
        return Factions.getInstance().getFactionById(id).getSize();
    }

    @Override
    public boolean isInFactionChest(Player player, String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        try{
            return ((Inventory) chestInventory.invoke(faction)).getViewers().contains(player);
        } catch (IllegalAccessException | InvocationTargetException ignored) {

        }
        return false;
    }

    @Override
    public boolean isFaction(String id) {
        return Factions.getInstance().isValidFactionId(id);
    }

    @Override
    public void setFaction(Player player, String id) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        if(id == null){
            fPlayer.leave(false);
            return;
        }

        try{
            setFaction.invoke(fPlayer, Factions.getInstance().getFactionById(id), false);
        }catch(IllegalAccessException | InvocationTargetException ignored){

        }
    }

    @Override
    public void setFaction(OfflinePlayer player, String id) {
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);

        if(id == null){
            fPlayer.leave(false);
            return;
        }

        try{
            setFaction.invoke(fPlayer, Factions.getInstance().getFactionById(id), false);
        }catch(IllegalAccessException | InvocationTargetException ignored){

        }
    }

    @Override
    public void setRole(Player player, TranslatedRole translatedRole) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        try{
            switch(translatedRole){
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
        }catch(IllegalAccessException | InvocationTargetException ignored){

        }
    }

    @Override
    public void setRole(OfflinePlayer offlinePlayer, TranslatedRole translatedRole) {
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(offlinePlayer);

        try{
            switch(translatedRole.getValue()){
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
        }catch(IllegalAccessException | InvocationTargetException ignored){

        }
    }

    @Override
    public void setPermission(String id, TranslatedRelation relation, String permission, boolean b) {
        Faction faction = Factions.getInstance().getFactionById(id);

        try{
            Object rel = null;

            switch(relation){
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
        }catch(Exception exc){
            exc.printStackTrace();
        }
    }

    @Override
    public void setRelationWish(String id, String other, TranslatedRelation relation) {
        Faction a = Factions.getInstance().getFactionById(id);

        Faction b = Factions.getInstance().getFactionById(other);

        try{
            Object rel = null;

            switch(relation){
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
        }catch(Exception exc){
            exc.printStackTrace();
        }
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

        for(Faction faction : Factions.getInstance().getAllFactions()){
            if(faction.isWilderness() || faction.isWarZone() || faction.isSafeZone())continue;
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

        for(Faction faction : allFactions){
            toReturn.add(faction.getId());
        }
        return toReturn;
    }

    @Override
    public Map<String, Set<Integer[]>> getAllClaims(String id) {
        Map<String, Set<Integer[]>> toReturn = new HashMap<>();

        for(FLocation fLocation : Board.getInstance().getAllClaims(id)){
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

        try{
            return (Inventory) chestInventory.invoke(faction);
        } catch (IllegalAccessException | InvocationTargetException ignored) {

        }
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
        if(id == null)return "None";
        if(!Factions.getInstance().isValidFactionId(id))return "None";
        Faction factionById = Factions.getInstance().getFactionById(id);
        if(factionById == null)return "None";
        return factionById.getTag();
    }

    @Override
    public String getIdFromTag(String tag) {
        Faction byTag = Factions.getInstance().getByTag(tag);
        if(byTag == null)return null;
        return byTag.getId();
    }

    @Override
    public String getWilderness() {
        return Factions.getInstance().getWilderness().getId();
    }

    @Override
    public String getRolePrefix(TranslatedRole translatedRole) {
        String toReturn = "";
        try{
            switch(translatedRole.getValue()){
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
        }catch(IllegalAccessException ignored){

        }
        return toReturn;
    }

    @Override
    public List<Player> getOnlineMembers(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if(faction == null)return new ArrayList<>();

        List<Player> players = new ArrayList<>();

        for(FPlayer fPlayer : faction.getFPlayersWhereOnline(true)){
            players.add(Bukkit.getPlayer(UUID.fromString(fPlayer.getId())));
        }
        return players;
    }

    @Override
    public List<OfflinePlayer> getOfflineMembers(String id) {
        List<OfflinePlayer> offliners = new ArrayList<>();

        Faction faction = Factions.getInstance().getFactionById(id);

        if(faction == null)return offliners;

        faction.getFPlayers().forEach(z -> {
            if(z.getPlayer() == null){
                offliners.add(Bukkit.getOfflinePlayer(UUID.fromString(z.getId())));
                return;
            }
            if(!z.getPlayer().isOnline())offliners.add(Bukkit.getOfflinePlayer(UUID.fromString(z.getId())));
        });

        return offliners;
    }

    @Override
    public UUID getLeader(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if(faction == null)return null;

        String uuid = faction.getFPlayerAdmin().getId();

        return UUID.fromString(uuid);
    }

    @Override
    public OfflinePlayer getOfflineLeader(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if(faction == null)return null;

        FPlayer fPlayerAdmin = faction.getFPlayerAdmin();

        if(fPlayerAdmin == null)return null;

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
        Faction faction = Factions.getInstance().getFactionById(id);
        try{
            return TranslatedRelation.valueOf(((Enum<?>)relationTo.invoke(fPlayer, faction)).name().toUpperCase());
        }catch(IllegalAccessException | InvocationTargetException e){
            return TranslatedRelation.ENEMY;
        }
    }

    @Override
    public TranslatedRole getRole(Player player) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        try{
            return TranslatedRole.valueOf(((Enum<?>) getRole.invoke(fPlayer)).name().toUpperCase());
        }catch(InvocationTargetException | IllegalAccessException e){
            e.printStackTrace();
        }
        return TranslatedRole.RECRUIT;
    }

    @Override
    public TranslatedRole getRole(OfflinePlayer player) {
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);
        try{
            return TranslatedRole.valueOf(((Enum<?>) getRole.invoke(fPlayer)).name().toUpperCase());
        }catch(InvocationTargetException | IllegalAccessException e){
            e.printStackTrace();
        }
        return TranslatedRole.RECRUIT;
    }

    @Override
    public TranslatedRole getRole(UUID uuid) {
        FPlayer fPlayer = FPlayers.getInstance().getById(uuid.toString());
        try{
            return TranslatedRole.valueOf(((Enum<?>) getRole.invoke(fPlayer)).name().toUpperCase());
        }catch(InvocationTargetException | IllegalAccessException e){
            e.printStackTrace();
        }
        return TranslatedRole.RECRUIT;
    }

    @Override
    public TranslatedRelation getFactionRelationToFaction(String id1, String id2) {
        Faction faction1 = Factions.getInstance().getFactionById(id1);
        Faction faction2 = Factions.getInstance().getFactionById(id2);
        if(faction1 == null || faction2 == null)return TranslatedRelation.ENEMY;
        try{
            return TranslatedRelation.valueOf(((Enum<?>)relationTo.invoke(faction1, faction2)).name().toUpperCase());
        } catch (IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return TranslatedRelation.ENEMY;
    }

    @Override
    public TranslatedRelation getRelationToPlayer(Player player, Player player2) {
        FPlayer fPlayer1 = FPlayers.getInstance().getByPlayer(player);
        FPlayer fPlayer2 = FPlayers.getInstance().getByPlayer(player2);
        try{
            return TranslatedRelation.valueOf(((Enum<?>)relationTo.invoke(fPlayer1, fPlayer2)).name().toUpperCase());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return TranslatedRelation.ENEMY;
    }

    @EventHandler
    public void onPowerLoss(PowerLossEvent e) {
        KPowerLossEvent event = new KPowerLossEvent(e.getfPlayer().getPlayer());
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled())e.setCancelled(true);
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
}
