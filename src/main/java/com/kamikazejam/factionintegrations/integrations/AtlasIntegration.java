package com.kamikazejam.factionintegrations.integrations;

import com.kamikazejam.factionintegrations.FactionIntegrations;
import com.kamikazejam.factionintegrations.event.*;
import com.kamikazejam.factionintegrations.integrations.interfaces.KFaction;
import com.kamikazejam.factionintegrations.object.TranslatedRelation;
import com.kamikazejam.factionintegrations.object.TranslatedRole;
import com.kamikazejam.factionintegrations.shield.ShieldIntegration;
import com.kamikazejam.factionintegrations.utils.MethodName;
import com.massivecraft.factions.*;
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

@SuppressWarnings("deprecation")
//AL56AF50
public class AtlasIntegration implements KFaction, ShieldIntegration {

    Map<MethodName, Method> methodMap = new HashMap<>();

    private Object leader, coleader, moderator, normal, recruit, tntUpgrade, allowObject;
    private Field maxMembersField;
    private Method getUpgrade, getExpansion, getByValue;
    private Method valueOf, valueOfRelation, setPermission, setRelationWish;

    public AtlasIntegration() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Class<?> factionClass = Class.forName("com.massivecraft.factions.Faction");
        Class<?> fplayerClass = Class.forName("com.massivecraft.factions.FPlayer");
        Class<?> roleClass = Class.forName("com.massivecraft.factions.struct.Role");
        Class<?> upgradeClass = Class.forName("com.massivecraft.factions.upgrades.FactionUpgrade");
        Class<?> aClass = Class.forName("com.massivecraft.factions.upgrades.UpgradeType");

        methodMap.put(MethodName.ADD_TNT, factionClass.getMethod("depositTNT", long.class));
        methodMap.put(MethodName.REMOVE_TNT, factionClass.getMethod("withdrawTNT", long.class));
        methodMap.put(MethodName.GET_TNT, factionClass.getMethod("getTNT"));
        methodMap.put(MethodName.GET_ROLE, FPlayer.class.getMethod("getRole"));
        methodMap.put(MethodName.GET_RELATION_TO, RelationParticipator.class.getMethod("getRelationTo", RelationParticipator.class));
        methodMap.put(MethodName.CAN_BUILD, Class.forName("com.massivecraft.factions.listeners.FactionsBlockListener").getMethod("playerCanBuildDestroyBlock", Player.class, Location.class, String.class, boolean.class));
        methodMap.put(MethodName.GET_CHEST_INVENTORY, factionClass.getMethod("getChestInventory"));
        methodMap.put(MethodName.GET_STRIKES, factionClass.getMethod("getStrikes"));
        methodMap.put(MethodName.SET_ROLE, FPlayer.class.getMethod("setRole", roleClass));
        methodMap.put(MethodName.SET_FACTION, fplayerClass.getMethod("setFaction", Faction.class, boolean.class));
        methodMap.put(MethodName.IS_BASE_REGION, factionClass.getMethod("isInsideBaseRegion", FLocation.class));
        methodMap.put(MethodName.IS_SHIELD_ACTIVE_NOW, factionClass.getDeclaredMethod("isShieldActive"));
        methodMap.put(MethodName.GET_ROLE_PREFIX, roleClass.getMethod("getPrefix"));
        methodMap.put(MethodName.GET_BALANCE, Faction.class.getDeclaredMethod("getBalance"));
        methodMap.put(MethodName.ADD_BALANCE, Faction.class.getDeclaredMethod("depositMoney", double.class));
        methodMap.put(MethodName.SUBTRACT_BALANCE, Faction.class.getDeclaredMethod("withdrawMoney", double.class));
        methodMap.put(MethodName.LEAVE_FACTION, fplayerClass.getMethod("leave"));

        getByValue = roleClass.getDeclaredMethod("getByValue", int.class);


        getUpgrade = Faction.class.getMethod("getUpgrade", aClass);

        getExpansion = upgradeClass.getMethod("getExpansion");

        for(Object type : aClass.getEnumConstants()){
            String toString = ((Enum<?>) type).name();
            if ("TNT_STORAGE".equalsIgnoreCase(toString)) {
                tntUpgrade = type;
                break;
            }
        }

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

        maxMembersField = confClass.getField("factionMemberLimit");

        Class<?> permissableClass = Class.forName("com.massivecraft.factions.perms.Permissable");
        Class<?> accessClass = Class.forName("com.massivecraft.factions.perms.Access");

        Class<?> relationClass = Class.forName("com.massivecraft.factions.struct.Relation");

        valueOfRelation = relationClass.getMethod("valueOf", String.class);

        allowObject = accessClass.getMethod("valueOf", String.class).invoke(null, "ALLOW");

        Class<?> permissableActionClass = Class.forName("com.massivecraft.factions.perms.PermissableAction");

        valueOf = permissableActionClass.getMethod("valueOf", String.class);

        setPermission = Faction.class.getMethod("setPermission", permissableClass, permissableActionClass, accessClass);

        setRelationWish = Faction.class.getMethod("setRelationWish", Faction.class, relationClass);
    }

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

    @EventHandler
    public void onPowerLoss(PowerLossEvent e) {
        KPowerLossEvent event = new KPowerLossEvent(e.getfPlayer().getPlayer());
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled())e.setCancelled(true);
    }

    @Override
    public void setTnT(String id, int amount) {
        try{
            Method getTntBalance = methodMap.get(MethodName.GET_TNT);
            Method addTnt = methodMap.get(MethodName.ADD_TNT);
            Faction faction = Factions.getInstance().getFactionById(id);

            int limit = getMaxTnt(id);

            methodMap.get(MethodName.REMOVE_TNT).invoke(faction, getTntBalance.invoke(faction));
            if(amount > limit){
                addTnt.invoke(faction, limit);
                return;
            }
            addTnt.invoke(faction, amount);
        }catch(IllegalAccessException | InvocationTargetException e){
            Bukkit.getLogger().severe("Error setting tnt bank balance!");
        }
    }

    @Override
    public int addTnT(String id, int amount) {
        try{
            Method getTntBalance = methodMap.get(MethodName.GET_TNT);
            Method addTnt = methodMap.get(MethodName.ADD_TNT);
            Faction faction = Factions.getInstance().getFactionById(id);
            int totalAmount = ((Long) getTntBalance.invoke(faction)).intValue();

            int limit = getMaxTnt(id);

            if(amount + totalAmount > limit){
                addTnt.invoke(faction, limit - totalAmount);
                return limit - totalAmount;
            }
            addTnt.invoke(faction, amount);
            return amount;
        }catch(IllegalAccessException | InvocationTargetException e){
            Bukkit.getLogger().severe("Error setting tnt bank balance!");
        }
        return 0;
    }

    @Override
    public int getTnT(String id) {
        try{
            Method getTntBalance = methodMap.get(MethodName.GET_TNT);
            Faction faction = Factions.getInstance().getFactionById(id);
            return ((Long) getTntBalance.invoke(faction)).intValue();
        }catch(IllegalAccessException | InvocationTargetException e){
            Bukkit.getLogger().severe("Error getting tnt bank balance!");
        }
        return 0;
    }

    @Override
    public int getMaxTnt(String id) {
        try {
            Object upgrade = getUpgrade.invoke(Factions.getInstance().getFactionById(id), tntUpgrade);
            return (int) getExpansion.invoke(upgrade);
        }catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getStrikes(String id){
        try{
            Faction faction = Factions.getInstance().getFactionById(id);

            if(faction == null)return 0;

            return ((Set<?>) methodMap.get(MethodName.GET_STRIKES).invoke(faction)).size();
        } catch (IllegalAccessException | InvocationTargetException | NullPointerException ignored) {

        }
        return 0;
    }

    @Override
    public double getBalance(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        try{
            return (double) methodMap.get(MethodName.GET_BALANCE).invoke(faction);
        }catch(InvocationTargetException | IllegalAccessException ignored){

        }
        return 0.0D;
    }

    @Override
    public void addBalance(String id, double add) {
        Faction faction = Factions.getInstance().getFactionById(id);

        try{
            methodMap.get(MethodName.ADD_BALANCE).invoke(faction, add);
        }catch(InvocationTargetException | IllegalAccessException ignored){

        }
    }

    @Override
    public void subtractBalance(String id, double remove) {
        Faction faction = Factions.getInstance().getFactionById(id);

        try{
            methodMap.get(MethodName.SUBTRACT_BALANCE).invoke(faction, remove);
        }catch(InvocationTargetException | IllegalAccessException ignored){

        }
    }

    @Override
    public boolean econEnabled(){
        return true;
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
        FLocation fLocation = new FLocation(location);
        Faction faction = Board.getInstance().getFactionAt(fLocation);
        if (!id.equalsIgnoreCase(faction.getId()))
            return false;
        try {
            return (Boolean) methodMap.get(MethodName.IS_BASE_REGION).invoke(faction, fLocation);
        } catch (InvocationTargetException|IllegalAccessException e) {
            Bukkit.getConsoleSender().sendMessage("Error getting base region!");
            return false;
        }
    }

    @Override
    public boolean isSystemFac(String id) {
        Faction factionById = Factions.getInstance().getFactionById(id);
        return factionById.isSafeZone() || factionById.isWilderness() || factionById.isWarZone();
    }

    @Override
    public boolean playerCanBuildThere(Player player, Location location) {
        Method canBuild = methodMap.get(MethodName.CAN_BUILD);
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
        Faction faction = Factions.getInstance().getFactionById(id);

        try {
            return (boolean) methodMap.get(MethodName.IS_SHIELD_ACTIVE_NOW).invoke(faction);
        }catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
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
            return (int) maxMembersField.get(null);
        }catch (IllegalAccessException ignored){

        }
        return 0;
    }

    @Override
    public int getSize(String id) {
        Faction factionById = Factions.getInstance().getFactionById(id);

        if(factionById == null)return 0;
        if(factionById.getFPlayers() == null)return 0;

        return factionById.getFPlayers().size();
    }

    @Override
    public boolean isInFactionChest(Player player, String id) {
        Method chestInventory = methodMap.get(MethodName.GET_CHEST_INVENTORY);
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
            try{
                methodMap.get(MethodName.LEAVE_FACTION).invoke(fPlayer);
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return;
        }

        try{
            methodMap.get(MethodName.SET_FACTION).invoke(fPlayer, Factions.getInstance().getFactionById(id), false);
        }catch(IllegalAccessException | InvocationTargetException ignored){

        }
    }

    @Override
    public void setFaction(OfflinePlayer player, String id) {
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);

        if(id == null){
            try{
                methodMap.get(MethodName.LEAVE_FACTION).invoke(fPlayer);
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return;
        }

        try{
            methodMap.get(MethodName.SET_FACTION).invoke(fPlayer, Factions.getInstance().getFactionById(id), false);
        }catch(IllegalAccessException | InvocationTargetException ignored){

        }
    }

    @Override
    public void setRole(Player player, TranslatedRole translatedRole) {
        Method setRole = methodMap.get(MethodName.SET_ROLE);
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

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
    public void setRole(OfflinePlayer offlinePlayer, TranslatedRole translatedRole) {
        Method setRole = methodMap.get(MethodName.SET_ROLE);
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
        }catch(Exception ignored){

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
        Method chestInventory = methodMap.get(MethodName.GET_CHEST_INVENTORY);
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
        String toReturn;
        try{
            if(translatedRole == TranslatedRole.ALT)return "";
            toReturn = (String) methodMap.get(MethodName.GET_ROLE_PREFIX).invoke(getByValue.invoke(null, translatedRole.getValue() - 1));
        }catch(Exception ignored){
            return "$";
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
        Method relationTo = methodMap.get(MethodName.GET_RELATION_TO);
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
        Method getRole = methodMap.get(MethodName.GET_ROLE);
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
        Method getRole = methodMap.get(MethodName.GET_ROLE);
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
        Method getRole = methodMap.get(MethodName.GET_ROLE);
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
        Method relationTo = methodMap.get(MethodName.GET_RELATION_TO);

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
        Method relationTo = methodMap.get(MethodName.GET_RELATION_TO);

        FPlayer fPlayer1 = FPlayers.getInstance().getByPlayer(player);
        FPlayer fPlayer2 = FPlayers.getInstance().getByPlayer(player2);
        try{
            return TranslatedRelation.valueOf(((Enum<?>)relationTo.invoke(fPlayer1, fPlayer2)).name().toUpperCase());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return TranslatedRelation.ENEMY;
    }

    @Override
    public boolean isShieldRegionAt(String id, Location location) {
        return this.isBaseRegion(location, id);
    }

    @Override
    public boolean isShieldActive(String id) {
        return this.isShieldActiveNow(id);
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
