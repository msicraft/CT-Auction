package me.msicraft.ctauction.PlayerData.Manager;

import me.msicraft.ctauction.CTAuction;
import me.msicraft.ctauction.PlayerData.PlayerData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerDataManager {

    private final CTAuction plugin;

    public PlayerDataManager(CTAuction plugin) {
        this.plugin = plugin;
    }

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public boolean registerPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (playerDataMap.containsKey(uuid)) {
            return false;
        }
        playerDataMap.put(uuid, new PlayerData(player));
        return true;
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        playerDataMap.remove(uuid);
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.getOrDefault(uuid, null);
    }

    public Set<UUID> getPlayerUUIDSets() {
        return playerDataMap.keySet();
    }

}
