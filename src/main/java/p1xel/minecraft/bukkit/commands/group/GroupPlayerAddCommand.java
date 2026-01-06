package p1xel.minecraft.bukkit.commands.group;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.VillagerOwner;
import p1xel.minecraft.bukkit.commands.Command;
import p1xel.minecraft.bukkit.utils.Locale;

public class GroupPlayerAddCommand implements Command {

    @Override
    public String getName() {
        return "group set player";
    }

    @Override
    public String getDescription() {
        return Locale.getRawMessage("commands.group.children.set.children.player.description");
    }

    @Override
    public String getUsage() {
        return Locale.getRawMessage("commands.group.children.set.children.player.usage");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!isPlayer(sender) || !hasPermission(sender, "group.set.player")) {
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[4]);
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(Locale.getMessage("player-not-exist"));
            return;
        }

        String targetUUID = target.getUniqueId().toString();
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        VillagerOwner owner = new VillagerOwner(uuid);
        if (!owner.getGroups().contains(args[2])) {
            sender.sendMessage(Locale.getMessage("group-not-exist").replaceAll("%group%", args[2]));
            return;
        }

        if (owner.getGroupPlayers(args[2]).contains(targetUUID)) {
            sender.sendMessage(Locale.getMessage("player-already-in-group").replaceAll("%group%", args[2]));
            return;
        }

        owner.addPlayerToGroup(args[2], targetUUID);
        sender.sendMessage(Locale.getMessage("player-set-success").replaceAll("%player%", args[4]).replaceAll("%group%", args[2]));

    }

    @Override
    public boolean isExecutable() {
        return false;
    }

}
