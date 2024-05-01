package me.msicraft.ctauction;

import me.msicraft.ctauction.Auction.Event.AuctionInventoryChatEditEvent;
import me.msicraft.ctauction.Auction.Event.AuctionInventoryEvent;
import me.msicraft.ctauction.Auction.Event.AuctionRelatedEvent;
import me.msicraft.ctauction.Auction.Manager.AuctionManager;
import me.msicraft.ctauction.Command.MainCommand;
import me.msicraft.ctauction.Command.SubCommand;
import me.msicraft.ctauction.PlayerData.DataFile.PlayerDataFile;
import me.msicraft.ctauction.PlayerData.Event.PlayerJoinAntQuitEvent;
import me.msicraft.ctauction.PlayerData.Manager.PlayerDataManager;
import me.msicraft.ctauction.PlayerData.PlayerData;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public final class CTAuction extends JavaPlugin {

    private static CTAuction plugin;

    public static CTAuction getPlugin() {
        return plugin;
    }

    public static final String PREFIX = ChatColor.GREEN + "[CT Auction]";

    private static Economy econ = null;
    private PlayerDataManager playerDataManager;
    private AuctionManager auctionManager;

    @Override
    public void onEnable() {
        plugin = this;
        createConfigFiles();

        File playerDataFolder = new File(plugin.getDataFolder() + File.separator + PlayerDataFile.FOLDER_PATH);
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }

        playerDataManager = new PlayerDataManager(this);
        auctionManager = new AuctionManager(this);

        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        eventRegister();
        commandRegister();
        updateVariables();

        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + PREFIX + " 플러그인이 활성화 되었습니다");
    }

    @Override
    public void onDisable() {
        auctionManager.saveVariablesToFile();
        for (UUID uuid : playerDataManager.getPlayerUUIDSets()) {
            PlayerData playerData = playerDataManager.getPlayerData(uuid);
            playerData.saveDataToFile();
        }
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + PREFIX + ChatColor.RED + " 플러그인이 비활성화 되었습니다");
    }

    private void eventRegister() {
        getServer().getPluginManager().registerEvents(new PlayerJoinAntQuitEvent(this), this);
        getServer().getPluginManager().registerEvents(new AuctionInventoryEvent(this), this);
        getServer().getPluginManager().registerEvents(new AuctionInventoryChatEditEvent(this), this);
        getServer().getPluginManager().registerEvents(new AuctionRelatedEvent(this), this);
    }

    private void commandRegister() {
        getCommand("경매").setExecutor(new MainCommand(this));
        getCommand("ctauction").setExecutor(new SubCommand(this));
    }

    public void updateVariables() {
        reloadConfig();
        auctionManager.reloadVariables();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
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

    public static Economy getEconomy() {
        return econ;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }

}
