package p1xel.minecraft.bukkit.Listeners;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.MyVillager;
import p1xel.minecraft.bukkit.Utils.Locale;
import p1xel.minecraft.bukkit.VillagerOwner;

public class InteractListener implements Listener {


    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {

        Entity entity = e.getRightClicked();
        if (entity.getType() != EntityType.VILLAGER) {
            return;
        }

        // Check if anyone claimed this villager before
        PersistentDataContainer container = entity.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
        if (!container.has(key, PersistentDataType.STRING)) {
            return;
        }

        String ownerUUID = container.get(key, PersistentDataType.STRING);

        if (ownerUUID == null) {
            return;
        }

        // If the villager is claimed by player

        // If the player is admin
        Player player = e.getPlayer();
        if (player.hasPermission("myvillager.bypass")) {
            return;
        }

        // Check if the player equals to the owner
        String playerUUID = player.getUniqueId().toString();
        if (ownerUUID.equalsIgnoreCase(playerUUID)) {
            return;
        }

        // Check if the villager is unlocked
        String villagerUUID = entity.getUniqueId().toString();
        VillagerOwner owner = new VillagerOwner(ownerUUID);
        if (!owner.isLock(villagerUUID)) {
            return;
        }

        // Check if the player is in the trusted group by the owner
        for (String groupName : owner.getGroups()) {
            if (owner.getGroupPlayers(groupName).contains(playerUUID) && owner.getGroupVillagers(groupName).contains(villagerUUID)) {
                return;
            }
        }

        // NO PERMISSION
        if (SelectionMode.getPlayerMode(playerUUID).equalsIgnoreCase("none")) {
            e.setCancelled(true);
            TextComponent message = new TextComponent(Locale.getMessage("unable-access"));
            TextComponent button = new TextComponent(Locale.getMessage("view-info-text"));
            button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Locale.getMessage("view-info-hover")).create()));
            button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/myvillager info " + villagerUUID));
            player.spigot().sendMessage(message, button);
        }

    }

}
