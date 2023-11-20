package com.kamikazejam.factionintegrations.integrations;

import com.kamikazejam.factionintegrations.FactionIntegrations;
import com.kamikazejam.factionintegrations.event.*;
import com.kamikazejam.factionintegrations.integrations.interfaces.KFaction;
import com.kamikazejam.factionintegrations.object.TranslatedRelation;
import com.kamikazejam.factionintegrations.object.TranslatedRole;
import com.massivecraft.factions.*;
import com.massivecraft.factions.event.*;
import com.massivecraft.factions.iface.RelationParticipator;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Role;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("deprecation")
public class UUIDIntegration implements KFaction {

    @EventHandler
    public void onFactionCreate(FactionCreateEvent e){
        Bukkit.getPluginManager().callEvent(new KFactionCreateEvent(e.getFaction().getId()));
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

    private Method chestInventory;
    private Method getMaxTNT;
    private Method canBuild;
    private Method getRole;
    private Object permissibleConstant;
    private Method relationTo, setRole;
    private Object leader, coleader, moderator, normal, recruit;

    public UUIDIntegration() throws ClassNotFoundException, NoSuchMethodException {
        Class<?> permissibleEnum = Class.forName("com.massivecraft.factions.perms.PermissibleAction");
        Class<?> factionClass = Class.forName("com.massivecraft.factions.Faction");

        for(Object object : permissibleEnum.getEnumConstants()){
            if(object.toString().equals("BUILD")){
                permissibleConstant = object;
                break;
            }
        }

        relationTo = RelationParticipator.class.getMethod("getRelationTo", RelationParticipator.class);

        getRole = FPlayer.class.getMethod("getRole");

        Class<?> roleClass = Class.forName("com.massivecraft.factions.perms.Role");

        setRole = FPlayer.class.getMethod("setRole", roleClass);

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

        canBuild = Class.forName("com.massivecraft.factions.listeners.FactionsBlockListener").getMethod("playerCanBuildDestroyBlock", Player.class, Location.class, permissibleEnum, boolean.class);
        try {
            getMaxTNT = factionClass.getDeclaredMethod("getMaxTNT");
        }catch (NoSuchMethodException ignored) {
            getMaxTNT = factionClass.getDeclaredMethod("getMaxTNTBank");
        }

        try {
            chestInventory = factionClass.getMethod("getChest");
        }catch (NoSuchMethodException ignored) {
            chestInventory = factionClass.getDeclaredMethod("getInventory");
        }
    }

    @Override
    public void setTnT(String id, int amount) {
        Faction faction = Factions.getInstance().getFactionById(id);
        int limit = getMaxTnt(id);

        faction.setTNTBank(0);
        if(amount > limit){
            faction.setTNTBank(limit);
            return;
        }
        faction.setTNTBank(amount);
    }

    @Override
    public int addTnT(String id, int amount) {
        Faction faction = Factions.getInstance().getFactionById(id);
        int totalAmount = faction.getTNTBank();

        int limit = getMaxTnt(id);

        if(amount + totalAmount > limit){
            faction.setTNTBank(limit);
            return limit - totalAmount;
        }
        faction.setTNTBank(amount + totalAmount);
        return amount;
    }

    @Override
    public int getTnT(String id) {
        return Factions.getInstance().getFactionById(id).getTNTBank();
    }

    @Override
    public int getMaxTnt(String id) {
        try{
            int maxTNT = (int) getMaxTNT.invoke(Factions.getInstance().getFactionById(id));
            return maxTNT <= 0 ? Integer.MAX_VALUE : maxTNT;
        }catch(IllegalAccessException | InvocationTargetException e){
            Bukkit.getLogger().severe("Error getting max tnt bank balance!");
        }
        return Integer.MAX_VALUE;
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

        new BukkitRunnable(){
            @Override
            public void run(){
                OfflinePlayer to = isUUID(accountId) ? Bukkit.getOfflinePlayer(UUID.fromString(accountId)) : Bukkit.getOfflinePlayer(accountId);

                FactionIntegrations.getEconomy().depositPlayer(to, add);
            }
        }.runTaskAsynchronously(FactionIntegrations.get());
    }

    @Override
    public void subtractBalance(String id, double remove) {
        String accountId = Factions.getInstance().getFactionById(id).getAccountId();

        OfflinePlayer to = isUUID(accountId) ? Bukkit.getOfflinePlayer(UUID.fromString(accountId)) : Bukkit.getOfflinePlayer(accountId);

        FactionIntegrations.getEconomy().withdrawPlayer(to, remove);
    }

    @Override
    public boolean econEnabled() {
        return FactionsPlugin.getInstance().getConfigManager().getMainConfig().economy().isEnabled();
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
            return (boolean) canBuild.invoke(null, player, location, permissibleConstant, true);
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

    @Override
    public int getStrikes(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if(faction == null) { return 0; }

        if(faction.getStrikes() == null)return 0;
        return faction.getStrikes().size();
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
        try {
            Faction faction = Factions.getInstance().getFactionById(id);
            Inventory inventory = (Inventory) chestInventory.invoke(faction);
            return inventory.getViewers().contains(player);
        }catch (Exception ignored) {
            FactionIntegrations.get().getLogger().info("Error getting faction chest inventory!");
            return false;
        }
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

        fPlayer.setFaction(Factions.getInstance().getFactionById(id));
    }

    @Override
    public void setFaction(OfflinePlayer player, String id) {
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);

        if(id == null){
            fPlayer.leave(false);
            return;
        }

        fPlayer.setFaction(Factions.getInstance().getFactionById(id));
    }

    @Override
    public void setRole(Player player, TranslatedRole translatedRole) {
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

        Relation rel = null;

        switch(relation){
            case MEMBER:
                rel = Relation.MEMBER;
                break;
            case ENEMY:
                rel = Relation.ENEMY;
                break;
            case NEUTRAL:
                rel = Relation.NEUTRAL;
                break;
            case ALLY:
                rel = Relation.ALLY;
                break;
            case TRUCE:
                rel = Relation.TRUCE;
        }

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
    public Inventory getChestInventory(Player player, String id){
        try {
            Faction faction = Factions.getInstance().getFactionById(id);
            return (Inventory) chestInventory.invoke(faction);
        }catch (Exception ignored) {
            FactionIntegrations.get().getLogger().info("Error getting faction chest inventory!");
            return null;
        }
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
        switch(translatedRole.getValue()){
            case 1:
                toReturn = FactionsPlugin.getInstance().getConfigManager().getMainConfig().factions().prefixes().getRecruit();
                break;
            case 2:
                toReturn = FactionsPlugin.getInstance().getConfigManager().getMainConfig().factions().prefixes().getNormal();
                break;
            case 3:
                toReturn = FactionsPlugin.getInstance().getConfigManager().getMainConfig().factions().prefixes().getMod();
                break;
            case 4:
                toReturn = FactionsPlugin.getInstance().getConfigManager().getMainConfig().factions().prefixes().getColeader();
                break;
            case 5:
                toReturn = FactionsPlugin.getInstance().getConfigManager().getMainConfig().factions().prefixes().getAdmin();
                break;
        }
        return toReturn;
    }

    @Override
    public List<Player> getOnlineMembers(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if(faction == null)return new ArrayList<>();

        List<Player> players = new ArrayList<>();

        for(FPlayer fPlayer : faction.getFPlayersWhereOnline(true)){
            if(!fPlayer.getRole().isAtLeast(Role.RECRUIT))continue;
            players.add(Bukkit.getPlayer(UUID.fromString(fPlayer.getId())));
        }
        return players;
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
    public List<OfflinePlayer> getOfflineMembers(String id) {
        List<OfflinePlayer> offliners = new ArrayList<>();

        Faction faction = Factions.getInstance().getFactionById(id);

        if(faction == null)return offliners;

        faction.getFPlayers().forEach(z -> {
            if(!z.getRole().isAtLeast(Role.RECRUIT))return;
            if(z.getPlayer() == null){
                offliners.add(Bukkit.getOfflinePlayer(UUID.fromString(z.getId())));
                return;
            }
            if(!z.getPlayer().isOnline())offliners.add(Bukkit.getOfflinePlayer(UUID.fromString(z.getId())));
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

    @Override
    public boolean isAlt(Player player) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        return fPlayer.getRole().name().equalsIgnoreCase("ALT");
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
