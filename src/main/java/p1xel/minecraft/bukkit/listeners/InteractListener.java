package p1xel.minecraft.bukkit.listeners;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.MyVillager;
import p1xel.minecraft.bukkit.utils.Cache;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.Locale;
import p1xel.minecraft.bukkit.VillagerOwner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
            player.playSound(player.getLocation(), Sound.valueOf(Config.getString("deny-sound")), 1.5f, 1.5f);
        }

    }


    // Delete data when the claimed villager died
    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.VILLAGER) {
            return;
        }

        PersistentDataContainer container = entity.getPersistentDataContainer();
        if (!container.has(key, PersistentDataType.STRING)) {
            return;
        }

        String ownerUUID = container.get(key, PersistentDataType.STRING);

        if (ownerUUID == null) {
            return;
        }
        String villagerUUID = entity.getUniqueId().toString();

        VillagerOwner owner = new VillagerOwner(ownerUUID);
        owner.removeVillager(villagerUUID);
        for (String g : owner.getGroups()) {

            if (owner.getGroupVillagers(g).contains(villagerUUID)) {
                owner.removeVillagerFromGroup(g, villagerUUID);
            }

        }

    }

    private final NamespacedKey key = new NamespacedKey(MyVillager.getInstance(), "MyVillager");
    private void sendDenyMessage(Player player, UUID villagerUUID) {
        TextComponent message = new TextComponent(Locale.getMessage("deny-message"));
        TextComponent button = new TextComponent(Locale.getMessage("view-info-text"));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Locale.getMessage("view-info-hover")).create()));
        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/myvillager info " + villagerUUID));
        player.spigot().sendMessage(message, button);
        player.playSound(player.getLocation(), Sound.valueOf(Config.getString("deny-sound")), 1.5f, 1.5f);
    }

    // Protection

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {

        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.VILLAGER) {
            return;
        }

        Entity playerEntity = event.getDamager();
        if (!(playerEntity instanceof Player)) {
            return;
        }

        Player player = (Player) playerEntity;
        // Check if anyone claimed this villager before
        PersistentDataContainer container = entity.getPersistentDataContainer();
        if (!container.has(key, PersistentDataType.STRING)) {
            return;
        }

        String ownerUUID = container.get(key, PersistentDataType.STRING);

        if (ownerUUID == null) {
            return;
        }

        UUID villagerUUID = entity.getUniqueId();

        if (Config.getBool("protection.player")) {

            // If the player is admin
            if (player.hasPermission("myvillager.bypass")) {
                return;
            }

            // Check if the player does not equal to the owner
            String playerUUID = player.getUniqueId().toString();
            if (!ownerUUID.equalsIgnoreCase(playerUUID)) {
                event.setCancelled(true);
                sendDenyMessage(player, villagerUUID);
            }

        }

    }

    @EventHandler
    public void onZombieDamage(EntityDamageByEntityEvent event) {

        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.VILLAGER) {
            return;
        }

        Entity damager = event.getDamager();
        EntityType entityType = damager.getType();
        if (entityType == EntityType.ZOMBIE || entityType == EntityType.ZOMBIE_VILLAGER) {
            return;
        }

        // Check if anyone claimed this villager before
        PersistentDataContainer container = entity.getPersistentDataContainer();

        if (!container.has(key, PersistentDataType.STRING)) {
            return;
        }

        if (Config.getBool("protection.zombie")) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onFallingBlock(EntityDamageByEntityEvent event) {

        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.VILLAGER) {
            return;
        }

        Entity damager = event.getDamager();
        if (!(damager instanceof FallingBlock)) {
            return;
        }

        // Check if anyone claimed this villager before
        PersistentDataContainer container = entity.getPersistentDataContainer();

        if (!container.has(key, PersistentDataType.STRING)) {
            return;
        }

        if (Config.getBool("protection.falling-block")) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onSuffocation(EntityDamageEvent event) {

        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.VILLAGER) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.SUFFOCATION) {
            return;
        }

        // Check if anyone claimed this villager before
        PersistentDataContainer container = entity.getPersistentDataContainer();

        if (!container.has(key, PersistentDataType.STRING)) {
            return;
        }

        if (Config.getBool("protection.suffocation")) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onFall(EntityDamageEvent event) {

        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.VILLAGER) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        // Check if anyone claimed this villager before
        PersistentDataContainer container = entity.getPersistentDataContainer();

        if (!container.has(key, PersistentDataType.STRING)) {
            return;
        }

        if (Config.getBool("protection.fall")) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onFire(EntityDamageEvent event) {

        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.VILLAGER) {
            return;
        }

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (!(cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.LAVA)) {
            return;
        }

        // Check if anyone claimed this villager before
        PersistentDataContainer container = entity.getPersistentDataContainer();

        if (!container.has(key, PersistentDataType.STRING)) {
            return;
        }

        if (Config.getBool("protection.fire")) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onExplode(EntityDamageEvent event) {

        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.VILLAGER) {
            return;
        }

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (!(cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
            return;
        }

        // Check if anyone claimed this villager before
        PersistentDataContainer container = entity.getPersistentDataContainer();

        if (!container.has(key, PersistentDataType.STRING)) {
            return;
        }

        if (Config.getBool("protection.explode")) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onPoison(EntityDamageEvent event) {

        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.VILLAGER) {
            return;
        }

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.POISON) {
            return;
        }

        // Check if anyone claimed this villager before
        PersistentDataContainer container = entity.getPersistentDataContainer();

        if (!container.has(key, PersistentDataType.STRING)) {
            return;
        }

        if (Config.getBool("protection.poison")) {
            event.setCancelled(true);
        }

    }

    private final List<EntityType> raid_team = Arrays.asList(
            EntityType.PILLAGER,
            EntityType.VINDICATOR,
            EntityType.RAVAGER,
            EntityType.EVOKER
    );

    @EventHandler
    public void onPillager(EntityDamageByEntityEvent event) {

        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.VILLAGER) {
            return;
        }

        Entity damager = event.getDamager();
        EntityType entityType = damager.getType();
        if (raid_team.contains(entityType)) {
            return;
        }

        // Check if anyone claimed this villager before
        PersistentDataContainer container = entity.getPersistentDataContainer();

        if (!container.has(key, PersistentDataType.STRING)) {
            return;
        }

        if (Config.getBool("protection.pillager")) {
            event.setCancelled(true);
        }

    }



}
