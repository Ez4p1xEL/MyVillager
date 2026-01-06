package p1xel.minecraft.bukkit.commands.group;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.VillagerOwner;
import p1xel.minecraft.bukkit.commands.Command;
import p1xel.minecraft.bukkit.utils.Locale;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupInfoCommand implements Command {

    @Override
    public String getName() {
        return "group info";
    }

    @Override
    public String getDescription() {
        return Locale.getRawMessage("commands.group.children.info.description");
    }

    @Override
    public String getUsage() {
        return Locale.getRawMessage("commands.group.children.info.usage");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!isPlayer(sender) || !hasPermission(sender, "group.info")) {
            return;
        }

        String group = args[2];
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        VillagerOwner owner = new VillagerOwner(uuid);
        if (!owner.getGroups().contains(group)) {
            sender.sendMessage(Locale.getMessage("group-not-exist").replaceAll("%group%", group));
            return;
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

    }

    @Override
    public boolean isExecutable() {
        return false;
    }

}
