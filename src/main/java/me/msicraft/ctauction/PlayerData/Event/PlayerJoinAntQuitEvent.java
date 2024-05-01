package me.msicraft.ctauction.PlayerData.Event;

import me.msicraft.ctauction.CTAuction;
import me.msicraft.ctauction.PlayerData.Manager.PlayerDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinAntQuitEvent implements Listener {

    private final CTAuction plugin;

    public PlayerJoinAntQuitEvent(CTAuction plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        if (playerDataManager.registerPlayer(player)) {
            return;
        } else {
            player.kick(Component.text(ChatColor.RED + "플레이어 데이터 등록 실패"));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        playerDataManager.getPlayerData(player).saveDataToFile();
        playerDataManager.removePlayer(player);
    }

}
