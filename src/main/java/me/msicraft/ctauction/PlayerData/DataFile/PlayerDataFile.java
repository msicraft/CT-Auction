package me.msicraft.ctauction.PlayerData.DataFile;

import me.msicraft.ctauction.CTAuction;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class PlayerDataFile {

    private final CTAuction plugin = CTAuction.getPlugin();
    public static final String FOLDER_PATH = "PlayerData";
    private final Player player;
    private File file;
    private FileConfiguration config;

    public PlayerDataFile(OfflinePlayer offlinePlayer) {
        this.player = null;
        String fileS = offlinePlayer.getUniqueId() + ".yml";
        this.file = new File(plugin.getDataFolder() + File.separator + FOLDER_PATH, fileS);
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public PlayerDataFile(Player player) {
        this.player = player;
        String fileS = player.getUniqueId() + ".yml";
        this.file = new File(plugin.getDataFolder() + File.separator + FOLDER_PATH, fileS);
        if (!this.file.exists()) {
            createFile(player);
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public boolean hasConfigFile() {
        return this.file.exists();
    }

    public void createFile(Player player) {
        if(!this.file.exists()) {
            try {
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(this.file);
                yamlConfiguration.set("Name", player.getName());
                yamlConfiguration.save(this.file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public void reloadConfig() {
        if(this.config == null) {
            return;
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
        Reader defaultConfigStream;
        try {
            defaultConfigStream = new InputStreamReader(plugin.getResource(this.file.getName()), StandardCharsets.UTF_8);
            if(defaultConfigStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
                config.setDefaults(defaultConfig);
            }
        }catch(NullPointerException ex) {
            //ex.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            getConfig().save(this.file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig(Player player) {
        String fileS = player.getUniqueId() + ".yml";
        if(this.file == null) {
            this.file = new File(plugin.getDataFolder() + File.separator + FOLDER_PATH, fileS);
        }
        if(!this.file.exists()) {
            plugin.saveResource(fileS, false);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public File getFile() {
        return file;
    }

    public String getFolderPath() {
        return FOLDER_PATH;
    }

}
