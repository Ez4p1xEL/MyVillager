package p1xel.minecraft.bukkit.commands.group;

import org.bukkit.command.CommandSender;
import p1xel.minecraft.bukkit.commands.Command;
import p1xel.minecraft.bukkit.utils.Locale;

public class GroupCommand implements Command {

    @Override
    public String getName() {
        return "group";
    }

    @Override
    public String getDescription() {
        return Locale.getRawMessage("commands.group.description");
    }

    @Override
    public String getUsage() {
        return Locale.getRawMessage("commands.group.usage");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

}
