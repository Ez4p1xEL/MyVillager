package p1xel.minecraft.bukkit.commands.group;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.VillagerOwner;
import p1xel.minecraft.bukkit.commands.Command;
import p1xel.minecraft.bukkit.utils.Locale;

public class GroupDeleteCommand implements Command {

    @Override
    public String getName() {
        return "group delete";
    }

    @Override
    public String getDescription() {
        return Locale.getRawMessage("commands.group.children.delete.description");
    }

    @Override
    public String getUsage() {
        return Locale.getRawMessage("commands.group.children.delete.usage");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!isPlayer(sender) || !hasPermission(sender, "group.delete")) {
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

        owner.removeGroup(group);
        sender.sendMessage(Locale.getMessage("group-delete-success").replaceAll("%group%", group));

    }

    @Override
    public boolean isExecutable() {
        return false;
    }

}
