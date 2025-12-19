package p1xel.minecraft.bukkit.listeners;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import p1xel.minecraft.bukkit.MyVillager;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.Locale;
import p1xel.minecraft.bukkit.VillagerOwner;

import java.util.*;

public class SelectionMode implements Listener {

    // TRUE OR FALSE
    private static HashMap<String, Boolean> toggle = new HashMap<>();
    public static HashMap<String, Boolean> getToggle() { return toggle;}
    public static boolean getPlayerToggle(String playerUUID) { return toggle.get(playerUUID);}
    public static void putPlayerToggle(String playerUUID, boolean bool) { toggle.put(playerUUID,bool);}
    public static void replacePlayerToggle(String playerUUID, boolean bool) { toggle.replace(playerUUID,bool);}
    public static boolean isPlayerToggleExist(String playerUUID) { return toggle.get(playerUUID) != null;}

    // List of Entity UUID
    private static HashMap<String, List<Entity>> selection = new HashMap<>();
    public static HashMap<String, List<Entity>> getSelections() { return selection;}
    public static List<Entity> getPlayerSelection(String playerUUID) { return selection.get(playerUUID);}
    public static void putPlayerSelection(String playerUUID, List<Entity> list) { selection.put(playerUUID,list);}
    public static void replacePlayerSelection(String playerUUID, List<Entity> list) { selection.replace(playerUUID,list);}
    public static boolean isPlayerSelectionExist(String playerUUID) { return selection.get(playerUUID) != null;}

    // none/claim/lock/unlock/info/villager-set/villager-unset (also single selection mode)
    private static HashMap<String, String> mode = new HashMap<>();
    public static HashMap<String, String> getModes() { return mode;}
    public static String getPlayerMode(String playerUUID) { return mode.get(playerUUID);}
    public static void putPlayerMode(String playerUUID, String m) { mode.put(playerUUID,m);}
    public static void replacePlayerMode(String playerUUID, String m) { mode.replace(playerUUID,m);}
    public static boolean isPlayerExist(String playerUUID) { return mode.get(playerUUID) != null;}




    @EventHandler
    public void onSelectingVillager(PlayerInteractEntityEvent event) {

        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        if (!SelectionMode.getPlayerToggle(uuid)) {
            return;
        }

        Entity entity = event.getRightClicked();
        if (entity.getType() != EntityType.VILLAGER) {
            player.sendMessage(Locale.getMessage("selection.claim.not-villager"));
            event.setCancelled(true);
            return;
        }

        String entityUUID = entity.getUniqueId().toString();

        if (getPlayerMode(uuid).equalsIgnoreCase("info")) {
            PersistentDataContainer container = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
            if (!container.has(key, PersistentDataType.STRING)) {
                player.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
                event.setCancelled(true);
                return;
            }
            String ownerUUID = container.get(key, PersistentDataType.STRING);
            VillagerOwner owner = new VillagerOwner(ownerUUID);
            String ownerName = owner.getName();
            if (!ownerName.equalsIgnoreCase(player.getName())) {
                if (!player.hasPermission("myvillager.info.other")) {
                    player.sendMessage(Locale.getMessage("no-perm"));
                    event.setCancelled(true);
                    return;
                }
            }
            // Add trusted players into the list
            List<String> players = new ArrayList<>();
            List<String> groups = owner.getGroups();
            for (String group : groups) {
                if (owner.getGroupVillagers(group).contains((entityUUID))) {
                    List<String> playersList = owner.getGroupPlayers(group);
                    // transfer from UUID to Name
                    for (String groupPlayerUUID : playersList) {
                        String groupPlayerName = Bukkit.getOfflinePlayer(UUID.fromString(groupPlayerUUID)).getName();
                        players.add(groupPlayerName);
                    }
                }
            }
            String playersString = (!groups.isEmpty()) ? String.join(", ", players) : Locale.getMessage("none");
            boolean permit = uuid.equalsIgnoreCase(ownerUUID) || players.contains(player.getName());
            String permitMessage = null;
            if (permit) {
                permitMessage = Locale.getMessage("info-can-access");
            } else {
                permitMessage = Locale.getMessage("info-no-perm");
            }

            Location location = entity.getLocation();
            Location claimedLocation = owner.getVillagerLocation(entityUUID);
            for (String message : Locale.yaml.getStringList("villager-info")) {
                message = message.replaceAll("%uuid%", entityUUID); // The UniqueId of the villager
                message = message.replaceAll("%owner%", ownerName); // The Owner Name
                message = message.replaceAll("%players%", playersString); // The players who can access to the villager
                message = message.replaceAll("%world%", claimedLocation.getWorld().getName()); // The world name where the villager is in
                message = message.replaceAll("%x%", String.valueOf(claimedLocation.getX())); // The location where the villager is in (x)
                message = message.replaceAll("%y%", String.valueOf(claimedLocation.getY())); // The location where the villager is in (y)
                message = message.replaceAll("%z%", String.valueOf(claimedLocation.getZ())); // The location where the villager is in (z)
                message = message.replaceAll("%cworld%", location.getWorld().getName()); // The world name where the villager is in
                message = message.replaceAll("%cx%", String.valueOf(location.getX())); // The location where the villager is in (x)
                message = message.replaceAll("%cy%", String.valueOf(location.getY())); // The location where the villager is in (y)
                message = message.replaceAll("%cz%", String.valueOf(location.getZ())); // The location where the villager is in (z)
                message = message.replaceAll("%permit%", permitMessage);
                message = Locale.translate(message);
                player.sendMessage(message);
            }
            event.setCancelled(true);
            return;
        }

        if (getPlayerMode(uuid).equalsIgnoreCase("claim")) {

            PersistentDataContainer container = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
            if (container.has(key, PersistentDataType.STRING)) {
                player.sendMessage(Locale.getMessage("selection.claim.already-claimed"));
                event.setCancelled(true);
                return;
            }

            //String ownerUUID = container.get(key, PersistentDataType.STRING);
            List<Entity> list = new ArrayList<>(getPlayerSelection(uuid));

            if (list.contains(entity)) {
                player.sendMessage(Locale.getMessage("selection.claim.already-selected"));
                event.setCancelled(true);
                return;
            }

            list.add(entity);
            replacePlayerSelection(uuid, list);
            player.sendMessage(Locale.getMessage("selection.claim.success"));
            event.setCancelled(true);
            return;
        }

        if (getPlayerMode(uuid).equalsIgnoreCase("claim-single")) {
            PersistentDataContainer container = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
            if (container.has(key, PersistentDataType.STRING)) {
                player.sendMessage(Locale.getMessage("selection.claim.already-claimed"));
                event.setCancelled(true);
                return;
            }

            boolean isVaultEnabled = MyVillager.getInstance().isDependencyEnabled("Vault");
            if (isVaultEnabled) {
                double balance = MyVillager.getEconomy().getBalance(player);
                double cost = Config.getDouble("claim-cost");
                if (balance < cost) {
                    player.sendMessage(Locale.getMessage("vault.not-enough-money").replaceAll("%remain%", String.valueOf(cost-balance)));
                    SelectionMode.replacePlayerToggle(uuid, false);
                    SelectionMode.replacePlayerMode(uuid, "none");
                    player.sendMessage(Locale.getMessage("selection.quited"));
                    event.setCancelled(true);
                    return;
                }
            }

            container.set(key, PersistentDataType.STRING, player.getUniqueId().toString());
            VillagerOwner owner = new VillagerOwner(uuid);
            owner.addVillager(entity);
            player.sendMessage(Locale.getMessage("claim-success"));
            if (isVaultEnabled) {
                double cost = Config.getDouble("claim-cost");
                MyVillager.getEconomy().withdrawPlayer(player, cost);
                player.sendMessage(Locale.getMessage("vault.purchase").replaceAll("%cost%", String.valueOf(cost)));
            }

            SelectionMode.replacePlayerToggle(uuid, false);
            SelectionMode.replacePlayerMode(uuid, "none");

            player.sendMessage(Locale.getMessage("selection.quited"));
            event.setCancelled(true);
            return;
        }

        if (getPlayerMode(uuid).equalsIgnoreCase("lock")) {

            PersistentDataContainer container = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
            if (!container.has(key, PersistentDataType.STRING)) {
                player.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
                event.setCancelled(true);
                return;
            }

            String ownerUUID = container.get(key, PersistentDataType.STRING);
            if (!ownerUUID.equalsIgnoreCase(uuid)) {
                player.sendMessage(Locale.getMessage("selection.not-own"));
                event.setCancelled(true);
                return;
            }

            //String ownerUUID = container.get(key, PersistentDataType.STRING);
            List<Entity> list = new ArrayList<>(getPlayerSelection(uuid));

            if (list.contains(entity)) {
                player.sendMessage(Locale.getMessage("selection.claim.already-selected"));
                event.setCancelled(true);
                return;
            }

            list.add(entity);
            replacePlayerSelection(uuid, list);
            player.sendMessage(Locale.getMessage("selection.lock.success"));
            event.setCancelled(true);
            return;
        }

        if (getPlayerMode(uuid).equalsIgnoreCase("lock-single")) {
            PersistentDataContainer container = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
            if (!container.has(key, PersistentDataType.STRING)) {
                player.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
                event.setCancelled(true);
                return;
            }

            String ownerUUID = container.get(key, PersistentDataType.STRING);
            if (!ownerUUID.equalsIgnoreCase(uuid)) {
                player.sendMessage(Locale.getMessage("selection.not-own"));
                event.setCancelled(true);
                return;
            }

            VillagerOwner owner = new VillagerOwner(uuid);
            boolean newResult = !owner.isLock(entityUUID);
            owner.setLock(entityUUID, newResult);
            player.sendMessage(Locale.getMessage("lock-success"));

            SelectionMode.replacePlayerToggle(uuid, false);
            SelectionMode.replacePlayerMode(uuid, "none");

            player.sendMessage(Locale.getMessage("selection.quited"));
            event.setCancelled(true);
        }

        if (getPlayerMode(uuid).equalsIgnoreCase("villager-set")) {

            PersistentDataContainer container = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
            if (!container.has(key, PersistentDataType.STRING)) {
                player.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
                event.setCancelled(true);
                return;
            }

            String ownerUUID = container.get(key, PersistentDataType.STRING);
            if (!ownerUUID.equalsIgnoreCase(uuid)) {
                player.sendMessage(Locale.getMessage("selection.not-own"));
                event.setCancelled(true);
                return;
            }

            VillagerOwner owner = new VillagerOwner(ownerUUID);
            String group = MyVillager.getCache().getValue(uuid);
            if (owner.getGroupVillagers(group).contains(entityUUID)) {
                player.sendMessage(Locale.getMessage("villager-already-set").replaceAll("%group%", group));
                return;
            }

            //String ownerUUID = container.get(key, PersistentDataType.STRING);
            List<Entity> list = new ArrayList<>(getPlayerSelection(uuid));

            if (list.contains(entity)) {
                player.sendMessage(Locale.getMessage("selection.claim.already-selected"));
                event.setCancelled(true);
                return;
            }

            list.add(entity);
            replacePlayerSelection(uuid, list);
            player.sendMessage(Locale.getMessage("selection.villager-set.success"));
            event.setCancelled(true);
            return;
        }

        if (getPlayerMode(uuid).equalsIgnoreCase("villager-set-single")) {
            PersistentDataContainer container = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
            if (!container.has(key, PersistentDataType.STRING)) {
                player.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
                event.setCancelled(true);
                return;
            }

            String ownerUUID = container.get(key, PersistentDataType.STRING);
            if (!ownerUUID.equalsIgnoreCase(uuid)) {
                player.sendMessage(Locale.getMessage("selection.not-own"));
                event.setCancelled(true);
                return;
            }

            VillagerOwner owner = new VillagerOwner(uuid);
            String group = MyVillager.getCache().getValue(uuid);

            if (owner.getGroupVillagers(group).contains(entityUUID)) {
                player.sendMessage(Locale.getMessage("villager-already-set").replaceAll("%group%", group));
                return;
            }

            owner.addVillagerToGroup(group, entityUUID);
            MyVillager.getCache().remove(uuid);
            player.sendMessage(Locale.getMessage("villager-set-success").replaceAll("%group%", group));

            SelectionMode.replacePlayerToggle(uuid, false);
            SelectionMode.replacePlayerMode(uuid, "none");
            player.sendMessage(Locale.getMessage("selection.quited"));
            event.setCancelled(true);
            return;
        }

        if (getPlayerMode(uuid).equalsIgnoreCase("villager-unset")) {

            PersistentDataContainer container = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
            if (!container.has(key, PersistentDataType.STRING)) {
                player.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
                event.setCancelled(true);
                return;
            }

            String ownerUUID = container.get(key, PersistentDataType.STRING);
            if (!ownerUUID.equalsIgnoreCase(uuid)) {
                player.sendMessage(Locale.getMessage("selection.not-own"));
                event.setCancelled(true);
                return;
            }

            VillagerOwner owner = new VillagerOwner(ownerUUID);
            String group = MyVillager.getCache().getValue(uuid);
            if (!owner.getGroupVillagers(group).contains(entityUUID)) {
                player.sendMessage(Locale.getMessage("villager-not-set").replaceAll("%group%", group));
                return;
            }

            //String ownerUUID = container.get(key, PersistentDataType.STRING);
            List<Entity> list = new ArrayList<>(getPlayerSelection(uuid));

            if (list.contains(entity)) {
                player.sendMessage(Locale.getMessage("selection.claim.already-selected"));
                event.setCancelled(true);
                return;
            }

            list.add(entity);
            replacePlayerSelection(uuid, list);
            player.sendMessage(Locale.getMessage("selection.villager-set.success"));
            event.setCancelled(true);
            return;
        }

        if (getPlayerMode(uuid).equalsIgnoreCase("villager-unset-single")) {
            PersistentDataContainer container = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
            if (!container.has(key, PersistentDataType.STRING)) {
                player.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
                event.setCancelled(true);
                return;
            }

            String ownerUUID = container.get(key, PersistentDataType.STRING);
            if (!ownerUUID.equalsIgnoreCase(uuid)) {
                player.sendMessage(Locale.getMessage("selection.not-own"));
                event.setCancelled(true);
                return;
            }

            VillagerOwner owner = new VillagerOwner(uuid);
            String group = MyVillager.getCache().getValue(uuid);
            if (owner.getGroupVillagers(group).contains(entityUUID)) {
                player.sendMessage(Locale.getMessage("villager-not-set").replaceAll("%group%", group));
                return;
            }
            owner.removeVillagerFromGroup(group, entityUUID);
            MyVillager.getCache().remove(uuid);
            player.sendMessage(Locale.getMessage("villager-unset-success").replaceAll("%group%", group));

            SelectionMode.replacePlayerToggle(uuid, false);
            SelectionMode.replacePlayerMode(uuid, "none");
            player.sendMessage(Locale.getMessage("selection.quited"));
            event.setCancelled(true);
            return;
        }

        // admin lock

        if (getPlayerMode(uuid).equalsIgnoreCase("admin-lock")) {

            PersistentDataContainer container = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
            if (!container.has(key, PersistentDataType.STRING)) {
                player.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
                event.setCancelled(true);
                return;
            }

            //String ownerUUID = container.get(key, PersistentDataType.STRING);
            List<Entity> list = new ArrayList<>(getPlayerSelection(uuid));

            if (list.contains(entity)) {
                player.sendMessage(Locale.getMessage("selection.claim.already-selected"));
                event.setCancelled(true);
                return;
            }

            list.add(entity);
            replacePlayerSelection(uuid, list);
            player.sendMessage(Locale.getMessage("selection.lock.success"));
            event.setCancelled(true);
            return;
        }

        if (getPlayerMode(uuid).equalsIgnoreCase("admin-lock-single")) {

            PersistentDataContainer container = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
            if (!container.has(key, PersistentDataType.STRING)) {
                player.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
                event.setCancelled(true);
                return;
            }

            String ownerUUID = container.get(key, PersistentDataType.STRING);
            VillagerOwner owner = new VillagerOwner(ownerUUID);

            boolean newResult = !owner.isLock(entityUUID);
            owner.setLock(entityUUID, newResult);
            player.sendMessage(Locale.getMessage("lock-success"));

            SelectionMode.replacePlayerToggle(uuid, false);
            SelectionMode.replacePlayerMode(uuid, "none");

            player.sendMessage(Locale.getMessage("selection.quited"));
            event.setCancelled(true);
            return;
        }

        // admin remove

        if (getPlayerMode(uuid).equalsIgnoreCase("admin-remove")) {

            PersistentDataContainer container = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
            if (!container.has(key, PersistentDataType.STRING)) {
                player.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
                event.setCancelled(true);
                return;
            }

            //String ownerUUID = container.get(key, PersistentDataType.STRING);
            List<Entity> list = new ArrayList<>(getPlayerSelection(uuid));

            if (list.contains(entity)) {
                player.sendMessage(Locale.getMessage("selection.claim.already-selected"));
                event.setCancelled(true);
                return;
            }

            list.add(entity);
            replacePlayerSelection(uuid, list);
            player.sendMessage(Locale.getMessage("selection.remove.success"));
            event.setCancelled(true);
            return;
        }

        if (getPlayerMode(uuid).equalsIgnoreCase("admin-remove-single")) {

            PersistentDataContainer container = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
            if (!container.has(key, PersistentDataType.STRING)) {
                player.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
                event.setCancelled(true);
                return;
            }

            String ownerUUID = container.get(key, PersistentDataType.STRING);
            VillagerOwner owner = new VillagerOwner(ownerUUID);

            entity.getPersistentDataContainer().remove(key);
            owner.removeVillager(entityUUID);

            for (String group : owner.getGroups()) {

                if (owner.getGroupVillagers(group).contains(entityUUID)) {
                    owner.removeVillagerFromGroup(group, entityUUID);
                }

            }

            player.sendMessage(Locale.getMessage("remove-success"));

            SelectionMode.replacePlayerToggle(uuid, false);
            SelectionMode.replacePlayerMode(uuid, "none");

            player.sendMessage(Locale.getMessage("selection.quited"));
            event.setCancelled(true);
            return;
        }

    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {

        if (!event.getMessage().equalsIgnoreCase("ok")) {
            return;
        }

        Player p = event.getPlayer();
        String uuid = p.getUniqueId().toString();

        if (!SelectionMode.getPlayerToggle(uuid)) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                String mode = SelectionMode.getPlayerMode(uuid);
                List<Entity> entities = SelectionMode.getPlayerSelection(uuid);
                VillagerOwner owner = new VillagerOwner(uuid);

                switch (mode) {
                    case "claim":

                        String group;

                        boolean isVaultEnabled = MyVillager.getInstance().isDependencyEnabled("Vault");
                        if (isVaultEnabled) {
                            double balance = MyVillager.getEconomy().getBalance(p);
                            double cost = Config.getDouble("claim-cost") * entities.size();
                            if (balance < cost) {
                                p.sendMessage(Locale.getMessage("vault.not-enough-money").replaceAll("%remain%", String.valueOf(cost-balance)));
                                break;
                            }
                        }

                        for (Entity villager : entities) {

                            String villagerUUID = villager.getUniqueId().toString();
                            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
                            PersistentDataContainer container = villager.getPersistentDataContainer();
                            if (container.has(key, PersistentDataType.STRING)) {
                                p.sendMessage(Locale.getMessage("selection.claim.already-claimed"));
                                event.setCancelled(true);
                                continue;
                            }
                            container.set(key, PersistentDataType.STRING, uuid);
                            owner.addVillager(villager);

                        }

                        p.sendMessage(Locale.getMessage("claim-success"));
                        if (isVaultEnabled) {
                            double cost = Config.getDouble("claim-cost") * entities.size();
                            MyVillager.getEconomy().withdrawPlayer(p, cost);
                            p.sendMessage(Locale.getMessage("vault.purchase").replaceAll("%cost%", String.valueOf(cost)));
                        }
                        break;

                    case "lock":
                        for (Entity villager : entities) {
                            String villagerUUID = villager.getUniqueId().toString();
                            boolean newResult = !owner.isLock(villagerUUID);
                            owner.setLock(villagerUUID, newResult);

                        }

                        p.sendMessage(Locale.getMessage("lock-success"));
                        break;

                    case "villager-set":
                        group = MyVillager.getCache().getValue(uuid);
                        for (Entity villager : entities) {
                            String villagerUUID = villager.getUniqueId().toString();
                            owner.addVillagerToGroup(group, villagerUUID);
                        }

                        p.sendMessage(Locale.getMessage("villager-set-success").replaceAll("%group%", group));
                        break;

                    case "villager-unset":
                        group = MyVillager.getCache().getValue(uuid);
                        for (Entity villager : entities) {
                            String villagerUUID = villager.getUniqueId().toString();
                            owner.removeVillagerFromGroup(group, villagerUUID);
                        }

                        p.sendMessage(Locale.getMessage("villager-unset-success").replaceAll("%group%", group));
                        break;

                    case "admin-lock":
                        for (Entity villager : entities) {
                            String villagerUUID = villager.getUniqueId().toString();
                            Entity entity = Bukkit.getEntity(UUID.fromString(villagerUUID));
                            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
                            PersistentDataContainer container = entity.getPersistentDataContainer();
                            if (!container.has(key, PersistentDataType.STRING)) {
                                p.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
                                event.setCancelled(true);
                                return;
                            }
                            String ownerUUID = container.get(key, PersistentDataType.STRING);
                            owner = new VillagerOwner(ownerUUID);

                            boolean newResult = !owner.isLock(villagerUUID);
                            owner.setLock(villagerUUID, newResult);

                        }

                        p.sendMessage(Locale.getMessage("lock-success"));
                        break;

                    case "admin-remove":
                        for (Entity villager : entities) {
                            String villagerUUID = villager.getUniqueId().toString();
                            Entity entity = Bukkit.getEntity(UUID.fromString(villagerUUID));
                            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
                            PersistentDataContainer container = entity.getPersistentDataContainer();
                            if (!container.has(key, PersistentDataType.STRING)) {
                                p.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
                                event.setCancelled(true);
                                return;
                            }
                            String ownerUUID = container.get(key, PersistentDataType.STRING);
                            owner = new VillagerOwner(ownerUUID);
                            owner.removeVillager(villagerUUID);
                            for (String g : owner.getGroups()) {

                                if (owner.getGroupVillagers(g).contains(villagerUUID)) {
                                    owner.removeVillagerFromGroup(g, villagerUUID);
                                }

                            }

                            entity.getPersistentDataContainer().remove(key);

                        }

                        p.sendMessage(Locale.getMessage("remove-success"));
                        break;
                }

                SelectionMode.replacePlayerToggle(uuid, false);
                SelectionMode.replacePlayerSelection(uuid, Collections.emptyList());
                SelectionMode.replacePlayerMode(uuid, "none");
                MyVillager.getCache().remove(uuid);

                p.sendMessage(Locale.getMessage("selection.quited"));

            }
        }.runTask(MyVillager.getInstance());

        event.setCancelled(true);

    }

}
