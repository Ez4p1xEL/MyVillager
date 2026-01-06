package p1xel.minecraft.bukkit.commands.admin;

import org.bukkit.command.CommandSender;
import p1xel.minecraft.bukkit.MyVillager;
import p1xel.minecraft.bukkit.commands.Command;
import p1xel.minecraft.bukkit.utils.Locale;

public class ReloadCommand implements Command {

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return Locale.getRawMessage("commands.reload.description");
    }

    @Override
    public String getUsage() {
        return Locale.getRawMessage("commands.reload.usage");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "reload")) {
            return;
        }

        MyVillager.getInstance().reloadConfig();
        Locale.createLocaleFile();
        sender.sendMessage(Locale.getMessage("reload-success"));

    }
}
