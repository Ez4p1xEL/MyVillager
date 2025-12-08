package p1xel.minecraft.bukkit.utils;

import java.util.HashMap;

public class Cache {

    public HashMap<String, String> cache = new HashMap<>();
    
    public void add(String playerUUID, String value) {
        cache.put(playerUUID,value);
    }
    
    public void replace(String playerUUID, String value) {
        cache.replace(playerUUID,value);
    }
    
    public void remove(String playerUUID) {
        cache.remove(playerUUID);
    }
    
    public String getValue(String playerUUID) {
        return cache.get(playerUUID);
    }
    
    public boolean isExist(String playerUUID) {
        return cache.get(playerUUID) != null;
    }


}
