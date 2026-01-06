package p1xel.minecraft.bukkit.commands.group;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import p1xel.minecraft.bukkit.MyVillager;
import p1xel.minecraft.bukkit.VillagerOwner;
import p1xel.minecraft.bukkit.commands.Command;
import p1xel.minecraft.bukkit.listeners.SelectionMode;
import p1xel.minecraft.bukkit.utils.Locale;

public class GroupVillagerRemoveCommand implements Command {

    @Override
    public String getName() {
        return "group set villager";
    }

    @Override
    public String getDescription() {
        return Locale.getRawMessage("commands.group.children.set.children.villager.description");
    }

    @Override
    public String getUsage() {
        return Locale.getRawMessage("commands.group.children.set.children.villager.usage");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!isPlayer(sender) || !hasPermission(sender, "group.set.villager")) {
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

        if (SelectionMode.getPlayerToggle(uuid)) {
            sender.sendMessage(Locale.getMessage("selection.already-in-selection"));
            return;
        }

        String mode = "villager-unset-single";

        if (args.length > 4 && args[4].equalsIgnoreCase("multiple")) {
            mode = "villager-unset";
        }

        SelectionMode.replacePlayerToggle(uuid, true);
        SelectionMode.replacePlayerMode(uuid, mode);
        MyVillager.getCache().add(uuid, group);
        sender.sendMessage(Locale.getMessage("selection.selection-mode"));
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.7f, 0.7f);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!SelectionMode.getPlayerToggle(uuid)) {
                    cancel();
                }

                player.sendTitle(Locale.getMessage("selection.title.title"), Locale.getMessage("selection.title.subtitle"), 20, 60, 20);
            }
        }.runTaskTimer(MyVillager.getInstance(), 5L, 50L);
    }

    @Override
    public boolean isExecutable() {
        return false;
    }

}
