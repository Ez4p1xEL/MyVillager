package p1xel.minecraft.bukkit.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import p1xel.minecraft.bukkit.MyVillager;
import p1xel.minecraft.bukkit.VillagerOwner;
import p1xel.minecraft.bukkit.listeners.SelectionMode;
import p1xel.minecraft.bukkit.utils.Locale;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InfoCommand implements Command {

    private CommandSender sender;

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return Locale.getRawMessage("commands.info.description");
    }

    @Override
    public String getUsage() {
        return Locale.getRawMessage("commands.info.usage");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        this.sender = sender;

        if (!hasPermission(sender, "info")) {
            return;
        }

        String mode = "info";
        if (args.length > 1) {
            viewInfo(args[1]);
            return;
        }

        if (!isPlayer(sender)) {
            return;
        }

        Player p = (Player) sender;
        String uuid = p.getUniqueId().toString();

        if (SelectionMode.getPlayerToggle(uuid)) {
            sender.sendMessage(Locale.getMessage("selection.already-in-selection"));
            return;
        }

        SelectionMode.replacePlayerToggle(uuid, true);
        SelectionMode.replacePlayerMode(uuid, mode);
        sender.sendMessage(Locale.getMessage("selection.selection-mode"));
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.7f, 0.7f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!SelectionMode.getPlayerToggle(uuid)) {
                    cancel();
                }

                p.sendTitle(Locale.getMessage("selection.title.title"), Locale.getMessage("selection.title.subtitle"), 10, 60, 20);
            }
        }.runTaskTimer(MyVillager.getInstance(), 5L, 50L);

    }

    public void viewInfo(String entityUUID) {
        boolean isPlayer = false;
        boolean containVillager = false;
        VillagerOwner owner = null;
        String playerUUID = null;
        String ownerUUID = null;
        String ownerName = null;
        Entity entity = Bukkit.getEntity(UUID.fromString(entityUUID));
        if (sender instanceof Player) {
            isPlayer = true;
            Player player = (Player) sender;
            playerUUID = player.getUniqueId().toString();
            owner = new VillagerOwner(playerUUID);
            containVillager = owner.getClaimedVillagersList().contains(entityUUID);
            if (containVillager) {
                ownerUUID = playerUUID;
                ownerName = owner.getName();
            }
        }

        if (!containVillager) {
            if (entity == null) {
                sender.sendMessage(Locale.getMessage("villager-not-found"));
                return;
            }
            PersistentDataContainer container = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
            if (!container.has(key, PersistentDataType.STRING)) {
                sender.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
                return;
            }

            if (isPlayer) {
                ownerUUID = container.get(key, PersistentDataType.STRING);
                owner = new VillagerOwner(ownerUUID);
                ownerName = owner.getName();
                if (!hasPermission(sender, "info.other")) {
                    return;
                }
            }
        }

        //String ownerUUID = container.get(key, PersistentDataType.STRING);
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

        String permitMessage = null;
        String playersString = (!groups.isEmpty()) ? String.join(", ", players) : Locale.getMessage("none");
        if (isPlayer) {
            Player player = (Player) sender;
            String uuid = player.getUniqueId().toString();
            boolean permit = uuid.equalsIgnoreCase(ownerUUID) || players.contains(player.getName());

            if (permit) {
                permitMessage = Locale.getMessage("info-can-access");
            } else {
                permitMessage = Locale.getMessage("info-no-perm");
            }
        } else {
            permitMessage = Locale.getMessage("info-console");
        }

        String world = "";
        String x = "";
        String y = "";
        String z;
        if (entity == null) {
            world = Locale.getMessage("info-no-location");
            x = Locale.getMessage("info-no-location");
            y = Locale.getMessage("info-no-location");
            z = Locale.getMessage("info-no-location");
        } else {
            Location currentLocation = entity.getLocation();
            world = currentLocation.getWorld().getName();
            x = String.format("%.2f", currentLocation.getX());
            y = String.format("%.2f", currentLocation.getY());
            z = String.format("%.2f", currentLocation.getZ());
        }

        Location claimedLocation = owner.getVillagerLocation(entityUUID);

        for (String message : Locale.yaml.getStringList("villager-info")) {
            message = message.replaceAll("%uuid%", entityUUID); // The UniqueId of the villager
            message = message.replaceAll("%owner%", ownerName); // The Owner Name
            message = message.replaceAll("%players%", playersString); // The players who can access to the villager
            message = message.replaceAll("%world%", claimedLocation.getWorld().getName()); // The world name where the villager was claimed in
            message = message.replaceAll("%x%", String.format("%.2f", claimedLocation.getX())); // The location where the villager was claimed in (x)
            message = message.replaceAll("%y%", String.format("%.2f", claimedLocation.getY())); // The location where the villager was claimed in (y)
            message = message.replaceAll("%z%", String.format("%.2f", claimedLocation.getZ())); // The location where the villager was claimed in (z)
            message = message.replaceAll("%cworld%", world); // The world name where the villager is in
            message = message.replaceAll("%cx%", x); // The location where the villager is in (x)
            message = message.replaceAll("%cy%", y); // The location where the villager is in (y)
            message = message.replaceAll("%cz%", z); // The location where the villager is in (z)
            message = message.replaceAll("%permit%", permitMessage);
            message = Locale.translate(message);
            sender.sendMessage(message);
        }
    }
}
