package p1xel.minecraft.bukkit.commands.admin;

import org.bukkit.command.CommandSender;
import p1xel.minecraft.bukkit.commands.Command;
import p1xel.minecraft.bukkit.utils.Locale;

public class AdminCommand implements Command {

    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public String getDescription() {
        return Locale.getRawMessage("commands.admin.description");
    }

    @Override
    public String getUsage() {
        return Locale.getRawMessage("commands.admin.usage");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

}
