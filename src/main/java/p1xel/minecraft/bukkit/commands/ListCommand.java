package p1xel.minecraft.bukkit.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.VillagerOwner;
import p1xel.minecraft.bukkit.utils.Locale;

public class ListCommand implements Command {

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return Locale.getRawMessage("commands.list.description");
    }

    @Override
    public String getUsage() {
        return Locale.getRawMessage("commands.list.usage");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!isPlayer(sender) || !hasPermission(sender, "list")) {
            return;
        }

        int page = 1;
        if (args.length == 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (Exception e) {
                sender.sendMessage(Locale.getMessage("number-invalid"));
                return;
            }
        }

        if (page < 1) {
            sender.sendMessage(Locale.getMessage("invalid-page"));
            return;
        }

        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        VillagerOwner owner = new VillagerOwner(uuid);

        int amount = owner.getVillagerAmount();
        if (amount <= 0) {
            sender.sendMessage(Locale.getMessage("no-record"));
            return;
        }

        int max_page = (int) Math.ceil((double) amount / 7);
        if (page > max_page) {
            sender.sendMessage(Locale.getMessage("invalid-page"));
            return;
        }

        sender.sendMessage(Locale.getMessage("list-page").replaceAll("%page%", String.valueOf(page)).replaceAll("%max_page%", String.valueOf(max_page)));
        int i = ((page-1) * 7) +1;
        for (String villagerUUID : owner.getClaimedVillagersList()) {

            Location location = owner.getVillagerLocation(villagerUUID);
            String message = Locale.getMessage("list-text");
            message = message.replaceAll("%number%", String.valueOf(i)); // The number of the villager
            message = message.replaceAll("%uuid%", villagerUUID); // The number of the villager
            message = message.replaceAll("%world%", location.getWorld().getName());
            message = message.replaceAll("%x%", String.valueOf(location.getX()));
            message = message.replaceAll("%y%", String.valueOf(location.getY()));
            message = message.replaceAll("%z%", String.valueOf(location.getZ()));
            TextComponent front = new TextComponent(message);
            TextComponent button = new TextComponent(Locale.getMessage("view-info-text"));
            button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Locale.getMessage("view-info-hover")).create()));
            button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/myvillager info " + villagerUUID));
            sender.spigot().sendMessage(front, button);
            i++;

        }

    }
}
