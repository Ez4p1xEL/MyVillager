package p1xel.minecraft.bukkit.commands.admin;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import p1xel.minecraft.bukkit.MyVillager;
import p1xel.minecraft.bukkit.commands.Command;
import p1xel.minecraft.bukkit.listeners.SelectionMode;
import p1xel.minecraft.bukkit.utils.Locale;

public class AdminLockCommand implements Command {

    @Override
    public String getName() {
        return "admin lock";
    }

    @Override
    public String getDescription() {
        return Locale.getRawMessage("commands.admin.children.lock.description");
    }

    @Override
    public String getUsage() {
        return Locale.getRawMessage("commands.admin.children.lock.usage");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!isPlayer(sender) || !hasPermission(sender, "admin.lock")) {
            return;
        }

        String mode = "admin-lock-single";
        if (args.length > 1 && args[1].equalsIgnoreCase("multiple")) {
            mode = "admin-lock";
        }

        Player p = (Player) sender;
        String uuid = p.getUniqueId().toString();

        if (SelectionMode.getPlayerToggle(uuid)) {
            sender.sendMessage(Locale.getMessage("selection.already-in-selection"));
            return;
        }

        SelectionMode.replacePlayerToggle(uuid, true);
        SelectionMode.replacePlayerMode(uuid, mode);
        sender.sendMessage(Locale.getMessage("selection.selection-mode"));
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.7f, 0.7f);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!SelectionMode.getPlayerToggle(uuid)) {
                    cancel();
                }

                p.sendTitle(Locale.getMessage("selection.title.title"), Locale.getMessage("selection.title.subtitle"), 20, 60, 20);
            }
        }.runTaskTimer(MyVillager.getInstance(), 5L, 50L);
    }
}
