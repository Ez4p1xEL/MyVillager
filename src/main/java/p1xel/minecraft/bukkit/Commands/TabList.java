package p1xel.minecraft.bukkit.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.VillagerOwner;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabList implements TabCompleter {

    List<String> args0 = new ArrayList<>();
    List<String> selection = new ArrayList<>(Arrays.asList("single", "multiple"));
    List<String> bool = new ArrayList<>(Arrays.asList("true", "false"));
    @Override
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        if (args0.isEmpty()) {
            args0.add("help"); args0.add("claim"); args0.add("lock"); args0.add("info"); args0.add("list"); args0.add("group");
            args0.add("player"); args0.add("villager"); args0.add("admin"); args0.add("reload");
        }

        List<String> result0 = new ArrayList<>();
        if (args.length == 1) {
            for (String a : args0) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result0.add(a);
                }
            }
            return result0;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("claim") || args[0].equalsIgnoreCase("lock") || args[0].equalsIgnoreCase("villager")) {
                return selection;
            }

            if (args[0].equalsIgnoreCase("info")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    String uuid = player.getUniqueId().toString();
                    VillagerOwner owner = new VillagerOwner(uuid);
                    return owner.getClaimedVillagersList();
                }
                return new ArrayList<>();
            }

            if (args[0].equalsIgnoreCase("player")) {
                List<String> result = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    result.add(player.getName());
                }
                return result;
            }

        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("group")) {
                if (args[1].equalsIgnoreCase("create")) {
                    return new ArrayList<>();
                }
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    String uuid = player.getUniqueId().toString();
                    VillagerOwner owner = new VillagerOwner(uuid);
                    return owner.getGroups();
                }
                return new ArrayList<>();
            }
            if (args[0].equalsIgnoreCase("admin")) {
                return selection;
            }
        }

        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("villager")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    String uuid = player.getUniqueId().toString();
                    VillagerOwner owner = new VillagerOwner(uuid);
                    return owner.getGroups();
                }
                return new ArrayList<>();
            }
        }

        return new ArrayList<>();
    }

}
