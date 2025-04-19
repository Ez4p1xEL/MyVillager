package p1xel.minecraft.bukkit.Commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
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

public class  Cmd implements CommandExecutor {

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

                    if (!sender.hasPermission("myvillager.info")) {
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
                    if (!ownerName.equalsIgnoreCase(sender.getName())) {
                        if (!sender.hasPermission("myvillager.info.other")) {
                            sender.sendMessage(Locale.getMessage("no-perm"));
                            return true;
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

                    Location location = entity.getLocation();

                    for (String message : Locale.yaml.getStringList("villager-info")) {
                        message = message.replaceAll("%uuid%", entityUUID); // The UniqueId of the villager
                        message = message.replaceAll("%owner%", ownerName); // The Owner Name
                        message = message.replaceAll("%players%", playersString); // The players who can access to the villager
                        message = message.replaceAll("%world%", location.getWorld().getName()); // The world name where the villager is in
                        message = message.replaceAll("%x%", String.valueOf(location.getX())); // The location where the villager is in (x)
                        message = message.replaceAll("%y%", String.valueOf(location.getY())); // The location where the villager is in (y)
                        message = message.replaceAll("%z%", String.valueOf(location.getZ())); // The location where the villager is in (z)
                        message = message.replaceAll("%permit%", permitMessage);
                        message = Locale.translate(message);
                        sender.sendMessage(message);
                    }
                    return true;

                }
            }

            if (args[0].equalsIgnoreCase("list")) {

                if (!sender.hasPermission("myvillager.list")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                int page = 1;
                if (args.length == 2) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (Exception e) {
                        sender.sendMessage(Locale.getMessage("number-invalid"));
                        return true;
                    }
                }

                if (page < 1) {
                    sender.sendMessage(Locale.getMessage("invalid-page"));
                    return true;
                }

                Player player = (Player) sender;
                String uuid = player.getUniqueId().toString();
                VillagerOwner owner = new VillagerOwner(uuid);

                int amount = owner.getVillagerAmount();
                if (amount <= 0) {
                    sender.sendMessage(Locale.getMessage("no-record"));
                    return true;
                }

                int max_page = (int) Math.ceil((double) amount / 7);
                if (page > max_page) {
                    sender.sendMessage(Locale.getMessage("invalid-page"));
                    return true;
                }

                sender.sendMessage(Locale.getMessage("list-page").replaceAll("%page%", String.valueOf(page)).replaceAll("%max_page%", String.valueOf(max_page)));
                int i = ((page-1) * 7) +1;
                for (String villagerUUID : owner.getClaimedVillagersList()) {

                    Entity villager = Bukkit.getEntity((UUID.fromString(villagerUUID)));
                    Location location = villager.getLocation();
                    String message = Locale.getMessage("list-text");
                    message = message.replaceAll("%number%", String.valueOf(i)); // The number of the villager
                    message = message.replaceAll("%uuid%", villagerUUID); // The number of the villager
                    message = message.replaceAll("%world%", location.getWorld().getName());
                    message = message.replaceAll("%x%", String.valueOf(location.getX()));
                    message = message.replaceAll("%y%", String.valueOf(location.getY()));
                    message = message.replaceAll("%z%", String.valueOf(location.getZ()));
                    TextComponent front = new TextComponent(message);
                    TextComponent button = new TextComponent(Locale.getMessage("view-info-text"));
                    button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Locale.getMessage("view-info-hover")).create()));
                    button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/myvillager info " + villagerUUID));
                    sender.spigot().sendMessage(front, button);
                    i++;

                }

                return true;

            }

        }

        if (args.length == 1) {

            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("myvillager.reload")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                MyVillager.getInstance().reloadConfig();
                Locale.createLocaleFile();
                sender.sendMessage(Locale.getMessage("reload-success"));
                return true;

            }

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
                    if (i <= 3 || i >= 19) {
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

        if (args.length == 3) {

            if (args[0].equalsIgnoreCase("group")) {

                if (args[1].equalsIgnoreCase("create")) {

                    if (!sender.hasPermission("myvillager.group.create")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    String group = args[2];
                    Player player = (Player) sender;
                    String uuid = player.getUniqueId().toString();
                    VillagerOwner owner = new VillagerOwner(uuid);
                    if (owner.getGroups().contains(group)) {
                        sender.sendMessage(Locale.getMessage("group-already-exist").replaceAll("%group%", group));
                        return true;
                    }

                    owner.createGroup(group);
                    sender.sendMessage(Locale.getMessage("group-create-success").replaceAll("%group%", group));
                    return true;

                }

                if (args[1].equalsIgnoreCase("delete")) {

                    if (!sender.hasPermission("myvillager.group.delete")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    String group = args[2];
                    Player player = (Player) sender;
                    String uuid = player.getUniqueId().toString();
                    VillagerOwner owner = new VillagerOwner(uuid);
                    if (!owner.getGroups().contains(group)) {
                        sender.sendMessage(Locale.getMessage("group-not-exist").replaceAll("%group%", group));
                        return true;
                    }

                    owner.removeGroup(group);
                    sender.sendMessage(Locale.getMessage("group-delete-success").replaceAll("%group%", group));
                    return true;
                }

                if (args[1].equalsIgnoreCase("info")) {

                    if (!sender.hasPermission("myvillager.group.info")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    String group = args[2];
                    Player player = (Player) sender;
                    String uuid = player.getUniqueId().toString();
                    VillagerOwner owner = new VillagerOwner(uuid);
                    if (!owner.getGroups().contains(group)) {
                        sender.sendMessage(Locale.getMessage("group-not-exist").replaceAll("%group%", group));
                        return true;
                    }

                    List<String> playersUUID = owner.getGroupPlayers(group);
                    // Add trusted players into the list
                    List<String> players = new ArrayList<>();
                    // transfer from UUID to Name
                    for (String groupPlayerUUID : playersUUID) {
                        String groupPlayerName = Bukkit.getOfflinePlayer(UUID.fromString(groupPlayerUUID)).getName();
                        players.add(groupPlayerName);
                    }
                    String playersString = (!players.isEmpty()) ? String.join(", ", players) : Locale.getMessage("none");
                    List<String> numList = new ArrayList<>();
                    for (String villagerUUID : owner.getGroupVillagers(group)) {
                        int num = 1;
                        for (String entityUUID : owner.getClaimedVillagersList()) {
                            if (villagerUUID.equalsIgnoreCase(entityUUID)) {
                                numList.add(String.valueOf(num));
                            }
                            num++;
                        }
                    }
                    String numbers = (!numList.isEmpty()) ? String.join(", ", numList) : Locale.getMessage("none");

                    for (String message : Locale.yaml.getStringList("group-info")) {
                        message = message.replaceAll("%group%", group); // The name of the group
                        message = message.replaceAll("%players%", playersString); // The list of trusted players
                        message = message.replaceAll("%numbers%", numbers); // The claimed villagers' numbers
                        message = Locale.translate(message);
                        sender.sendMessage(message);
                    }

                    return true;

                }

            }

            if (args[0].equalsIgnoreCase("admin")) {

                if (args[1].equalsIgnoreCase("lock")) {

                    if (!sender.hasPermission("myvillager.admin.lock")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    Player player = (Player) sender;
                    String uuid = player.getUniqueId().toString();

                    if (args[2].equalsIgnoreCase("single")) {

                        SelectionMode.replacePlayerToggle(uuid, true);
                        SelectionMode.replacePlayerMode(uuid, "admin-lock-single");
                        sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                        return true;
                    }

                    if (args[2].equalsIgnoreCase("multiple")) {

                        SelectionMode.replacePlayerToggle(uuid, true);
                        SelectionMode.replacePlayerMode(uuid, "admin-lock");
                        sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                        return true;

                    }

                }

                if (args[1].equalsIgnoreCase("remove")) {

                    if (!sender.hasPermission("myvillager.admin.remove")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    Player player = (Player) sender;
                    String uuid = player.getUniqueId().toString();

                    if (args[2].equalsIgnoreCase("single")) {

                        SelectionMode.replacePlayerToggle(uuid, true);
                        SelectionMode.replacePlayerMode(uuid, "admin-remove-single");
                        sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                        return true;
                    }

                    if (args[2].equalsIgnoreCase("multiple")) {

                        SelectionMode.replacePlayerToggle(uuid, true);
                        SelectionMode.replacePlayerMode(uuid, "admin-remove");
                        sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                        return true;

                    }

                }

            }

        }

        if (args.length == 4) {

            if (args[0].equalsIgnoreCase("player") && args[2].equalsIgnoreCase("set")) {

                if (!sender.hasPermission("myvillager.player.set")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (!target.hasPlayedBefore()) {
                    sender.sendMessage(Locale.getMessage("player-not-exist"));
                    return true;
                }

                String targetUUID = target.getUniqueId().toString();
                Player player = (Player) sender;
                String uuid = player.getUniqueId().toString();
                VillagerOwner owner = new VillagerOwner(uuid);
                if (!owner.getGroups().contains(args[3])) {
                    sender.sendMessage(Locale.getMessage("group-not-exist").replaceAll("%group%", args[3]));
                    return true;
                }

                if (owner.getGroupPlayers(args[3]).contains(targetUUID)) {
                    sender.sendMessage(Locale.getMessage("player-already-in-group").replaceAll("%group%", args[3]));
                    return true;
                }

                owner.addPlayerToGroup(args[3], targetUUID);
                sender.sendMessage(Locale.getMessage("player-set-success").replaceAll("%player%", args[1]).replaceAll("%group%", args[3]));
                return true;

            }

            if (args[0].equalsIgnoreCase("player") && args[2].equalsIgnoreCase("unset")) {

                if (!sender.hasPermission("myvillager.player.unset")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (!target.hasPlayedBefore()) {
                    sender.sendMessage(Locale.getMessage("player-not-exist"));
                    return true;
                }

                String targetUUID = target.getUniqueId().toString();
                Player player = (Player) sender;
                String uuid = player.getUniqueId().toString();
                VillagerOwner owner = new VillagerOwner(uuid);
                if (!owner.getGroups().contains(args[3])) {
                    sender.sendMessage(Locale.getMessage("group-not-exist").replaceAll("%group%", args[3]));
                    return true;
                }

                if (!owner.getGroupPlayers(args[3]).contains(targetUUID)) {
                    sender.sendMessage(Locale.getMessage("player-not-in-group").replaceAll("%group%", args[3]));
                    return true;
                }

                owner.removePlayerFromGroup(args[3], targetUUID);
                sender.sendMessage(Locale.getMessage("player-unset-success").replaceAll("%player%", args[1]).replaceAll("%group%", args[3]));
                return true;

            }

            if (args[0].equalsIgnoreCase("villager") && args[2].equalsIgnoreCase("set")) {

                if (!sender.hasPermission("myvillager.villager.set")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                String group = args[3];
                Player player = (Player) sender;
                String uuid = player.getUniqueId().toString();
                VillagerOwner owner = new VillagerOwner(uuid);
                if (!owner.getGroups().contains(group)) {
                    sender.sendMessage(Locale.getMessage("group-not-exist").replaceAll("%group%", group));
                    return true;
                }

                if (SelectionMode.getPlayerToggle(uuid)) {
                    sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                    return true;
                }

                if (args[1].equalsIgnoreCase("single")) {

                    SelectionMode.replacePlayerToggle(uuid, true);
                    SelectionMode.replacePlayerMode(uuid, "villager-set-single");
                    MyVillager.getCache().add(uuid, group);
                    sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                    return true;
                }

                if (args[1].equalsIgnoreCase("multiple")) {

                    SelectionMode.replacePlayerToggle(uuid, true);
                    SelectionMode.replacePlayerMode(uuid, "villager-set");
                    MyVillager.getCache().add(uuid, group);
                    sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                    return true;

                }

            }

            if (args[0].equalsIgnoreCase("villager") && args[2].equalsIgnoreCase("unset")) {

                if (!sender.hasPermission("myvillager.villager.unset")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                String group = args[3];
                Player player = (Player) sender;
                String uuid = player.getUniqueId().toString();
                VillagerOwner owner = new VillagerOwner(uuid);
                if (!owner.getGroups().contains(group)) {
                    sender.sendMessage(Locale.getMessage("group-not-exist").replaceAll("%group%", group));
                    return true;
                }

                if (SelectionMode.getPlayerToggle(uuid)) {
                    sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                    return true;
                }

                if (args[1].equalsIgnoreCase("single")) {

                    SelectionMode.replacePlayerToggle(uuid, true);
                    SelectionMode.replacePlayerMode(uuid, "villager-unset-single");
                    MyVillager.getCache().add(uuid, group);
                    sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                    return true;
                }

                if (args[1].equalsIgnoreCase("multiple")) {

                    SelectionMode.replacePlayerToggle(uuid, true);
                    SelectionMode.replacePlayerMode(uuid, "villager-unset");
                    MyVillager.getCache().add(uuid, group);
                    sender.sendMessage(Locale.getMessage("selection.selection-mode"));
                    return true;

                }

            }


        }









        return false;
    }


}
