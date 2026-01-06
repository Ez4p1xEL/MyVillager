package p1xel.minecraft.bukkit.commands.group;

import org.bukkit.command.CommandSender;
import p1xel.minecraft.bukkit.commands.Command;
import p1xel.minecraft.bukkit.utils.Locale;

public class GroupUnsetCommand implements Command {

    @Override
    public String getName() {
        return "group unset";
    }

    @Override
    public String getDescription() {
        return Locale.getRawMessage("commands.group.children.unset.description");
    }

    @Override
    public String getUsage() {
        return Locale.getRawMessage("commands.group.children.unset.usage");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

}
