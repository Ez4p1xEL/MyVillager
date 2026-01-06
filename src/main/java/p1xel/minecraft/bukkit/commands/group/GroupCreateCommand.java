package p1xel.minecraft.bukkit.commands.group;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.VillagerOwner;
import p1xel.minecraft.bukkit.commands.Command;
import p1xel.minecraft.bukkit.utils.Locale;

public class GroupCreateCommand implements Command {

    @Override
    public String getName() {
        return "group create";
    }

    @Override
    public String getDescription() {
        return Locale.getRawMessage("commands.group.children.create.description");
    }

    @Override
    public String getUsage() {
        return Locale.getRawMessage("commands.group.children.create.usage");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!isPlayer(sender) || !hasPermission(sender, "group.create")) {
            return;
        }

        String group = args[2];
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        VillagerOwner owner = new VillagerOwner(uuid);
        if (owner.getGroups().contains(group)) {
            sender.sendMessage(Locale.getMessage("group-already-exist").replaceAll("%group%", group));
            return;
        }

        owner.createGroup(group);
        sender.sendMessage(Locale.getMessage("group-create-success").replaceAll("%group%", group));

    }

    @Override
    public boolean isExecutable() {
        return false;
    }

}
