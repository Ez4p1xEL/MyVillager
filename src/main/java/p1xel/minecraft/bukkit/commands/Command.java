package p1xel.minecraft.bukkit.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.utils.Locale;

public interface Command {

    String getName();

    String getDescription();

    String getUsage();

    void execute(CommandSender sender, String[] args);

    default boolean hasPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission("myvillager." + permission)) {
            sender.sendMessage(Locale.getMessage("no-perm"));
            return false;
        }
        return true;
    }

    default boolean isPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Locale.getMessage("must-be-player"));
            return false;
        }
        return true;
    }

    default boolean isExecutable() {
        return true;
    }

}
