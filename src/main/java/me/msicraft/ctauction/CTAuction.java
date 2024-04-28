package me.msicraft.ctauction;

import me.msicraft.ctauction.Auction.Manager.AuctionManager;
import me.msicraft.ctplayerdata.CTPlayerData;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CTAuction extends JavaPlugin {

    private static CTAuction plugin;

    public static CTAuction getPlugin() {
        return plugin;
    }

    public static final String PREFIX = ChatColor.GREEN + "[CT Auction]";


    private CTPlayerData ctPlayerData;
    private AuctionManager auctionManager;

    @Override
    public void onEnable() {
        plugin = this;
        createConfigFiles();

        ctPlayerData = CTPlayerData.getPlugin();
        auctionManager = new AuctionManager(this);

        eventRegister();
        commandRegister();
        reloadVariables();

        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + PREFIX + " 플러그인이 활성화 되었습니다");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + PREFIX + ChatColor.RED + " 플러그인이 비활성화 되었습니다");
    }

    private void eventRegister() {
    }

    private void commandRegister() {
    }

    public void reloadVariables() {

    }

    private void createConfigFiles() {
        File configf = new File(getDataFolder(), "config.yml");
        if (!configf.exists()) {
            configf.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(configf);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    protected FileConfiguration config;

    private void replaceConfig() {
        File file = new File(getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        File config_old = new File(getDataFolder(),"config_old-" + dateFormat.format(date) + ".yml");
        file.renameTo(config_old);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + PREFIX + " Plugin replaced the old config.yml with config_old.yml and created a new config.yml");
    }

    public CTPlayerData getCtPlayerData() {
        return ctPlayerData;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }

}
