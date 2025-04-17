package p1xel.minecraft.bukkit.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
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
import p1xel.minecraft.bukkit.Utils.Locale;
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
    private static HashMap<String, List<String>> selection = new HashMap<>();
    public static HashMap<String, List<String>> getSelections() { return selection;}
    public static List<String> getPlayerSelection(String playerUUID) { return selection.get(playerUUID);}
    public static void putPlayerSelection(String playerUUID, List<String> list) { selection.put(playerUUID,list);}
    public static void replacePlayerSelection(String playerUUID, List<String> list) { selection.replace(playerUUID,list);}
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
            // Add trusted players into the list
            List<String> players = new ArrayList<>();
            List<String> groups = owner.getGroups();
            System.out.println("groups: "+ groups);
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

            for (String message : Locale.yaml.getStringList("villager-info")) {
                message = message.replaceAll("%uuid%", entityUUID); // The UniqueId of the villager
                message = message.replaceAll("%owner%", ownerName); // The Owner Name
                message = message.replaceAll("%players%", playersString); // The players who can access to the villager
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
            List<String> list = new ArrayList<>(getPlayerSelection(uuid));

            if (list.contains(entityUUID)) {
                player.sendMessage(Locale.getMessage("selection.claim.already-selected"));
                event.setCancelled(true);
                return;
            }

            list.add(entityUUID);
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

            container.set(key, PersistentDataType.STRING, player.getUniqueId().toString());
            VillagerOwner owner = new VillagerOwner(uuid);
            owner.addVillager(entityUUID);
            player.sendMessage(Locale.getMessage("claim-success"));

            SelectionMode.replacePlayerToggle(uuid, false);
            SelectionMode.replacePlayerMode(uuid, "none");

            player.sendMessage(Locale.getMessage("selection.quited"));
            event.setCancelled(true);
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
            List<String> list = new ArrayList<>(getPlayerSelection(uuid));

            if (list.contains(entityUUID)) {
                player.sendMessage(Locale.getMessage("selection.claim.already-selected"));
                event.setCancelled(true);
                return;
            }

            list.add(entityUUID);
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
                List<String> entities = SelectionMode.getPlayerSelection(uuid);

                switch (mode) {
                    case "claim":

                        for (String villagerUUID : entities) {

                            Entity villager = Bukkit.getEntity(UUID.fromString(villagerUUID));
                            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
                            villager.getPersistentDataContainer().set(key, PersistentDataType.STRING, p.getUniqueId().toString());
                            VillagerOwner owner = new VillagerOwner(uuid);
                            owner.addVillager(villagerUUID);


                        }

                        p.sendMessage(Locale.getMessage("claim-success"));
                    case "lock":
                        for (String villagerUUID : entities) {
                            VillagerOwner owner = new VillagerOwner(uuid);
                            boolean newResult = !owner.isLock(villagerUUID);
                            owner.setLock(villagerUUID, newResult);

                        }

                        p.sendMessage(Locale.getMessage("lock-success"));
                }

                SelectionMode.replacePlayerToggle(uuid, false);
                SelectionMode.replacePlayerSelection(uuid, Collections.emptyList());
                SelectionMode.replacePlayerMode(uuid, "none");

                p.sendMessage(Locale.getMessage("selection.quited"));
                event.setCancelled(true);

            }
        }.runTask(MyVillager.getInstance());


    }

}
