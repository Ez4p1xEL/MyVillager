package p1xel.minecraft.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import p1xel.minecraft.bukkit.utils.VillagerManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VillagerOwner {

    private FileConfiguration yaml;
    private String uuid;

    public VillagerOwner(String playerUUID) {
        if (!VillagerManager.isCacheExist(playerUUID)) {
            File file = new File(MyVillager.getInstance().getDataFolder() + "/users", playerUUID + ".yml");
            VillagerManager.upload(playerUUID,file);
        }
        this.yaml = VillagerManager.get(playerUUID);
        this.uuid = playerUUID;
    }

    public String getUUID() {
        return uuid;
    }

    public String getName() {
        return getFile().getString(uuid + ".name");
    }

    public FileConfiguration getFile() {
        return yaml;
    }

    public void set(String path, Object value) {
        VillagerManager.set(uuid, path, value);
    }

    public List<String> getClaimedVillagersList() {
        List<String> list = new ArrayList<>();
        try {
            list.addAll(getFile().getConfigurationSection("villagers").getKeys(false));
        } catch (NullPointerException exception) {
            list = Collections.emptyList();
        }
        return list;
    }

    public void setLock(String villagerUUID, boolean bool) {
        set("villagers." + villagerUUID + ".lock", bool);
    }
    public boolean isLock(String villagerUUID) {
        return getFile().getBoolean("villagers." + villagerUUID + ".lock");
    }

    public List<String> getGroups() {
        List<String> list = new ArrayList<>();
        try {
            list.addAll(getFile().getConfigurationSection("groups").getKeys(false));
        } catch (NullPointerException exception) {
            list = Collections.emptyList();
        }
        return list;
    }

    public void createGroup(String groupName) {
        set("groups." + groupName + ".list", Collections.emptyList());
        set("groups." + groupName + ".villagers", Collections.emptyList());
    }

    public void removeGroup(String groupName) {
        set("groups." + groupName, null);
    }

    public List<String> getGroupPlayers(String groupName) {
        return getFile().getStringList("groups." + groupName + ".list");
    }

    public List<String> getGroupVillagers(String groupName) {
        return getFile().getStringList("groups." + groupName + ".villagers");
    }

    public void addPlayerToGroup(String groupName, String targetUUID) {
        List<String> list = getGroupPlayers(groupName);
        list.add(targetUUID);
        set("groups." + groupName + ".list", list);
    }

    public void removePlayerFromGroup(String groupName, String targetUUID) {
        List<String> list = getGroupPlayers(groupName);
        list.remove(targetUUID);
        set("groups." + groupName + ".list", list);
    }

    public void addVillagerToGroup(String groupName, String villagerUUID) {
        List<String> list = getGroupVillagers(groupName);
        list.add(villagerUUID);
        set("groups." + groupName + ".villagers", list);
    }

    public void removeVillagerFromGroup(String groupName, String villagerUUID) {
        List<String> list = getGroupVillagers(groupName);
        list.remove(villagerUUID);
        set("groups." + groupName + ".villagers", list);
    }


    public void addVillager(String villagerUUID, Location location) {
        set("villagers." + villagerUUID + ".lock", true);
        set("villagers." + villagerUUID + ".location.world", location.getWorld().getName());
        set("villagers." + villagerUUID + ".location.x", location.getX());
        set("villagers." + villagerUUID + ".location.y", location.getY());
        set("villagers." + villagerUUID + ".location.z", location.getZ());
    }

    public void addVillager(Entity entity) {
        addVillager(entity.getUniqueId().toString(), entity.getLocation());
    }

    public void removeVillager(String villagerUUID) {
        set("villagers." + villagerUUID, null);
    }

    public int getVillagerAmount() {
        try {
            return getFile().getConfigurationSection("villagers").getKeys(false).size();
        } catch (NullPointerException exception) {
            return 0;
        }
    }

    // get claimed location
    public Location getVillagerLocation(String villagerUUID) {
        String world = yaml.getString("villagers." + villagerUUID + ".location.world", Bukkit.getWorlds().get(0).getName());
        double x = yaml.getDouble("villagers." + villagerUUID + ".location.x", 0);
        double y = yaml.getDouble("villagers." + villagerUUID + ".location.y", 0);
        double z = yaml.getDouble("villagers." + villagerUUID + ".location.z", 0);
        return new Location(Bukkit.getWorld(world), x, y, z);
    }


}
