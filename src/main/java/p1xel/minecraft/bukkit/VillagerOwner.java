package p1xel.minecraft.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.Utils.VillagerManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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


    public void addVillager(String villagerUUID) {
        set("villagers." + villagerUUID + ".lock", true);
    }

    public void removeVillager(String villagerUUID) {
        set("villagers." + villagerUUID, null);
    }


}
