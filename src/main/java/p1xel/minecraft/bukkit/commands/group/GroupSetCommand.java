package p1xel.minecraft.bukkit.commands.group;

import org.bukkit.command.CommandSender;
import p1xel.minecraft.bukkit.commands.Command;
import p1xel.minecraft.bukkit.utils.Locale;

public class GroupSetCommand implements Command {

    @Override
    public String getName() {
        return "group set";
    }

    @Override
    public String getDescription() {
        return Locale.getRawMessage("commands.group.children.set.description");
    }

    @Override
    public String getUsage() {
        return Locale.getRawMessage("commands.group.children.set.usage");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

}
