package p1xel.minecraft.bukkit.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.MyVillager;
import p1xel.minecraft.bukkit.utils.Locale;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Cmd implements CommandExecutor {

    void sendHelpMessage(CommandSender sender, String head, String arg) {

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&0&m                                         "));
        sender.sendMessage("§bMyVillager " + MyVillager.getInstance().getDescription().getVersion());
        sender.sendMessage(Locale.getMessage("command-click-info"));
        sender.sendMessage("");

        String path = "commands";
        if (head != null) {
            path = path + "." + head + ".children";
        }
        String head_command = " ";
        if (arg != null) {
            String[] args = arg.split(" ");
            List<String> heads = new ArrayList<>();
            for (int i = 0;i < args.length; i++) {
                // 獲取前面指令Usage
                p1xel.minecraft.bukkit.commands.Command command = CommandRegistry.getCommands().get(String.join(" ", Arrays.copyOfRange(args, 0, i+1)));
                if (command == null) { continue; }
                heads.add(command.getUsage().replaceAll("/myvillager ", "").replaceAll("\\[multiple]", ""));
            }
            head_command = " " + String.join(" ", heads) + " ";
        }

        for (String name : Locale.yaml.getConfigurationSection(path).getKeys(false)) {
            String cmd = arg == null ? name : arg + " " + name;
            p1xel.minecraft.bukkit.commands.Command command = CommandRegistry.getCommands().get(cmd);

            if (command == null) {
                continue;
            }

            if (!sender.hasPermission("myvillager." + command.getName().replaceAll(" ", "."))) {
                continue;
            }

            String click_command = "/myvillager" + head_command + command.getUsage().replaceAll("/myvillager ", "").replaceAll("\\[multiple]", "");
            TextComponent text = new TextComponent("§7- " + command.getUsage().replaceAll("/myvillager ", ""));
            ClickEvent.Action click_action = command.isExecutable() ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND;
            if (command.isExecutable()) {
                click_command = click_command.replaceAll(" <" + Locale.getMessage("group") + ">", "");
            }
            text.setClickEvent(new ClickEvent(click_action, click_command));
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Locale.getMessage("command-click")).create()));
            sender.spigot().sendMessage(text);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + command.getDescription()));
            sender.sendMessage("");

        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&0&m                                         "));

    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }

        if (args.length == 0) {

           sendHelpMessage(sender, null, null);
           return true;
        }

        if (args.length == 1) {

            p1xel.minecraft.bukkit.commands.Command command = CommandRegistry.getCommands().get(args[0]);
            if (command == null) {
                sender.sendMessage(Locale.getMessage("incorrect-argument"));
                return true;
            }

            if (args[0].equals("group") || args[0].equals("admin")) {
                sendHelpMessage(sender, args[0], args[0]);
                return true;
            }

        }

        if (args.length == 2) {

            if (!args[0].equals("list") && !args[0].equals("info")) {

                p1xel.minecraft.bukkit.commands.Command command = CommandRegistry.getCommands().get(args[0] + " " + args[1]);
                if (command == null) {
                    sender.sendMessage(Locale.getMessage("incorrect-argument"));
                    return true;
                }

                if (args[1].equals("set") || args[1].equals("unset")) {
                    sendHelpMessage(sender, args[0] + ".children." + args[1], args[0] + " " + args[1]);
                    return true;
//                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&0&m                                         "));
//                sender.sendMessage("§fMyVillager " + MyVillager.getInstance().getDescription().getVersion());
//                sender.sendMessage(Locale.getMessage("click-command-info"));
//                sender.sendMessage("");
//
//                for (String name : Locale.yaml.getConfigurationSection("commands." + args[0] + ".children." + args[1] + ".children").getKeys(false)) {
//
//                    p1xel.minecraft.bukkit.commands.Command inside_command = CommandRegistry.getCommands().get(args[0] + " " + name);
//
//                    if (inside_command == null) {
//                        continue;
//                    }
//                    TextComponent text = new TextComponent("§7" + inside_command.getUsage().replaceAll("/myvillager ", ""));
//                    text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command.getUsage() + " " + inside_command.getUsage().replaceAll("\\[multiple]", "")));
//                    text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Locale.getMessage("command-click")).create()));
//                    sender.spigot().sendMessage(text);
//                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8" + command.getDescription()));
//                    sender.sendMessage("");
//
//                }
//
//                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&0&m                                         "));
                }
            }

        }
        String[] new_args = args;
        try {
            int page_number = Integer.parseInt(args[args.length -1]);
            new_args = Arrays.copyOfRange(args, 0, new_args.length-1);
        } catch (NumberFormatException ignored) {}

        try {
            UUID uniqueId = UUID.fromString(args[args.length-1]);
            new_args = Arrays.copyOfRange(args, 0, new_args.length-1);
        } catch (IllegalArgumentException ignored) {}
        String arg = String.join(" ", new_args);
        p1xel.minecraft.bukkit.commands.Command command = CommandRegistry.getCommands().get(arg);
        if (command == null) {
            sender.sendMessage(Locale.getMessage("incorrect-argument"));
            return true;
        }

        command.execute(sender, args);
        return true;

    }

}
