package p1xel.minecraft.bukkit.Commands;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.Listeners.SelectionMode;
import p1xel.minecraft.bukkit.MyVillager;
import p1xel.minecraft.bukkit.Utils.Locale;
import p1xel.minecraft.bukkit.VillagerOwner;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Cmd implements CommandExecutor {

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(Locale.getMessage("commands.help"));
            return true;
        }

        if (args.length >= 1) {

            if (args[0].equalsIgnoreCase("info")) {

                if (args.length == 1) {
                    if (!sender.hasPermission("myvillager.info")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    Player player = (Player) sender;
                    String uuid = player.getUniqueId().toString();

                    if (SelectionMode.getPlayerToggle(uuid)) {
                        sender.sendMessage(Locale.getMessage("already-in-selection"));
                        return true;
                    }

                    SelectionMode.replacePlayerToggle(uuid, true);
                    SelectionMode.replacePlayerMode(uuid, "info");
                    sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                    return true;

                }

                if (args.length == 2) {

                    if (!sender.hasPermission("myvillager.info.other")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    String entityUUID = args[1];
                    Entity entity = Bukkit.getEntity(UUID.fromString(entityUUID));
                    if (entity == null) {
                        sender.sendMessage(Locale.getMessage("villager-not-found"));
                        return true;
                    }

                    PersistentDataContainer container = entity.getPersistentDataContainer();
                    NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
                    if (!container.has(key, PersistentDataType.STRING)) {
                        sender.sendMessage(Locale.getMessage("selection.claim.has-not-claimed"));
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

                    String permitMessage = null;
                    String playersString = (!groups.isEmpty()) ? String.join(", ", players) : Locale.getMessage("none");
                    if (sender instanceof Player) {
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

                    for (String message : Locale.yaml.getStringList("villager-info")) {
                        message = message.replaceAll("%uuid%", entityUUID); // The UniqueId of the villager
                        message = message.replaceAll("%owner%", ownerName); // The Owner Name
                        message = message.replaceAll("%players%", playersString); // The players who can access to the villager
                        message = message.replaceAll("%permit%", permitMessage);
                        message = Locale.translate(message);
                        sender.sendMessage(message);
                    }
                    return true;

                }
            }

        }

        if (args.length == 1) {

            if (args[0].equalsIgnoreCase("help")) {

                if (!sender.hasPermission("myvillager.help")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                sender.sendMessage(Locale.getMessage("commands.top"));
                sender.sendMessage(Locale.getMessage("commands.plugin"));
                sender.sendMessage(Locale.getMessage("commands.space-1"));

                int i = 0;
                for (String key : Locale.yaml.getConfigurationSection("commands").getKeys(false)) {
                    i++;
                    if (i <= 3 || i >= 17) {
                        continue;
                    }

                    if (sender.hasPermission("myvillager." + key.replaceAll("-", "."))) {
                        sender.sendMessage(Locale.getMessage("commands." + key));
                    }

                }

                sender.sendMessage(Locale.getMessage("commands.space-8"));
                sender.sendMessage(Locale.getMessage("commands.bottom"));

            }

        }

        if (args.length == 2) {

            if (args[0].equalsIgnoreCase("claim")) {

                if (!sender.hasPermission("myvillager.claim")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                if (args[1].equalsIgnoreCase("multiple")) {

                    Player p = (Player) sender;
                    String uuid = p.getUniqueId().toString();

                    if (SelectionMode.getPlayerToggle(uuid)) {
                        sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                        return true;
                    }

                    SelectionMode.replacePlayerToggle(uuid, true);
                    SelectionMode.replacePlayerMode(uuid, "claim");
                    sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                    return true;

                }

                if (args[1].equalsIgnoreCase("single")) {

                    Player p = (Player) sender;
                    String uuid = p.getUniqueId().toString();

                    if (SelectionMode.getPlayerToggle(uuid)) {
                        sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                        return true;
                    }

                    SelectionMode.replacePlayerToggle(uuid, true);
                    SelectionMode.replacePlayerMode(uuid, "claim-single");
                    sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                    return true;

                }

            }

            if (args[0].equalsIgnoreCase("lock")) {

                if (!sender.hasPermission("myvillager.lock")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                if (args[1].equalsIgnoreCase("multiple")) {

                    Player p = (Player) sender;
                    String uuid = p.getUniqueId().toString();

                    if (SelectionMode.getPlayerToggle(uuid)) {
                        sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                        return true;
                    }

                    SelectionMode.replacePlayerToggle(uuid, true);
                    SelectionMode.replacePlayerMode(uuid, "lock");
                    sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                    return true;

                }

                if (args[1].equalsIgnoreCase("single")) {

                    Player p = (Player) sender;
                    String uuid = p.getUniqueId().toString();

                    if (SelectionMode.getPlayerToggle(uuid)) {
                        sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                        return true;
                    }

                    SelectionMode.replacePlayerToggle(uuid, true);
                    SelectionMode.replacePlayerMode(uuid, "lock-single");
                    sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                    return true;

                }

            }

        }









        return false;
    }


}
