package me.msicraft.ctauction.Auction.Manager;

import me.msicraft.ctauction.Auction.AuctionInventory;
import me.msicraft.ctauction.Auction.AuctionItemStack;
import me.msicraft.ctauction.Auction.DataFile.AuctionItemDataFile;
import me.msicraft.ctauction.CTAuction;
import me.msicraft.ctauction.Enum.GuiType;
import me.msicraft.ctauction.Enum.ItemStatus;
import me.msicraft.ctauction.PlayerData.PlayerData;
import me.msicraft.ctauction.aCommon.CustomEvent.AuctionItemExpiredEvent;
import me.msicraft.ctauction.aCommon.CustomEvent.AuctionItemSoldEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AuctionManager {

    private final CTAuction plugin;
    private final AuctionItemDataFile dataFile;

    private final List<UUID> usingBuyInventoryPlayerList = new ArrayList<>();

    private final Map<UUID, List<AuctionItemStack>> registeredAuctionItemsMap = new LinkedHashMap<>();

    public AuctionManager(CTAuction plugin) {
        this.plugin = plugin;
        this.dataFile = new AuctionItemDataFile(plugin);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (usingBuyInventoryPlayerList.isEmpty()) {
                    return;
                }
                for (UUID playerUUID : registeredAuctionItemsMap.keySet()) {
                    List<AuctionItemStack> list = registeredAuctionItemsMap.get(playerUUID);
                    for (AuctionItemStack auctionItemStack : list) {
                        ItemStatus itemStatus = auctionItemStack.getItemStatus();
                        if (itemStatus == ItemStatus.SELLING) {
                            if (auctionItemStack.getLeftSeconds() <= 0) {
                                if (auctionItemStack.getAuctionItemBuyerInfo() == null) {
                                    Bukkit.getPluginManager().callEvent(new AuctionItemExpiredEvent(auctionItemStack));
                                    auctionItemStack.setItemStatus(ItemStatus.EXPIRED);
                                } else {
                                    Bukkit.getPluginManager().callEvent(new AuctionItemSoldEvent(auctionItemStack));
                                    auctionItemStack.setItemStatus(ItemStatus.SOLD);
                                }
                            }
                        }
                    }
                }
                for (UUID uuid : usingBuyInventoryPlayerList) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        if (hasUsingBuyInventory(player)) {
                            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
                            AuctionInventory auctionInventory = (AuctionInventory) playerData.getCTInventory(GuiType.BUY);
                            auctionInventory.updateBuyGui(playerData);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    public void reloadVariables() {
        dataFile.saveConfig();
        dataFile.reloadConfig();

        ConfigurationSection section = dataFile.getConfig().getConfigurationSection("Data");
        if (section!= null) {
            Set<String> set = section.getKeys(false);
            for (String s : set) {
                UUID uuid = UUID.fromString(s);
                List<String> itemDataList = dataFile.getConfig().getStringList("Data." + uuid);
                List<AuctionItemStack> list = registeredAuctionItemsMap.getOrDefault(uuid, new ArrayList<>());
                for (String data : itemDataList) {
                    AuctionItemStack auctionItemStack = new AuctionItemStack(data);
                    if (list.isEmpty()) {
                        list.add(auctionItemStack);
                        continue;
                    }
                    for (AuctionItemStack auctionItem : list) {
                        if (auctionItem.getItemId().compareTo(auctionItemStack.getItemId()) == 0) {
                            list.add(auctionItemStack);
                            break;
                        }
                    }
                }
                registeredAuctionItemsMap.put(uuid, list);
            }
        }
    }

    public void saveVariablesToFile() {
        for (UUID playerUUID : registeredAuctionItemsMap.keySet()) {
            List<AuctionItemStack> list = registeredAuctionItemsMap.get(playerUUID);
            List<String> dataList = new ArrayList<>();
            for (AuctionItemStack auctionItemStack : list) {
                dataList.add(auctionItemStack.getDataString());
            }
            dataFile.getConfig().set("Data." + playerUUID, dataList);
            Bukkit.getServer().getConsoleSender().sendMessage("저장중: " + dataList);
        }
        dataFile.saveConfig();
    }

    public void addUsingBuyInventory(Player player) {
        usingBuyInventoryPlayerList.add(player.getUniqueId());
    }

    public void removeUsingBuyInventory(Player player) {
        usingBuyInventoryPlayerList.remove(player.getUniqueId());
    }

    public boolean hasUsingBuyInventory(Player player) {
        return usingBuyInventoryPlayerList.contains(player.getUniqueId());
    }

    public List<AuctionItemStack> getRegisteredAuctionItems() {
        List<AuctionItemStack> list = new ArrayList<>();
        for (UUID uuid : registeredAuctionItemsMap.keySet()) {
            List<AuctionItemStack> itemList = registeredAuctionItemsMap.getOrDefault(uuid, new ArrayList<>());
            list.addAll(itemList);
        }
        return list;
    }

    public void addAuctionItem(UUID playerUUID, AuctionItemStack auctionItemStack) {
        List<AuctionItemStack> list = registeredAuctionItemsMap.getOrDefault(playerUUID, new ArrayList<>());
        if (hasAuctionItem(playerUUID, auctionItemStack)) {
            return;
        }
        list.add(auctionItemStack);
        registeredAuctionItemsMap.put(playerUUID, list);
    }

    public AuctionItemStack getAuctionItemStack(UUID playerUUID, UUID itemId) {
        List<AuctionItemStack> list = registeredAuctionItemsMap.getOrDefault(playerUUID, new ArrayList<>());
        for (AuctionItemStack auctionItemStack : list) {
            if (auctionItemStack.getItemId().compareTo(itemId) == 0) {
                return auctionItemStack;
            }
        }
        return null;
    }

    public AuctionItemStack getAuctionItemStack(UUID itemId) {
        for (UUID uuid : registeredAuctionItemsMap.keySet()) {
            List<AuctionItemStack> list = registeredAuctionItemsMap.getOrDefault(uuid, new ArrayList<>());
            for (AuctionItemStack auctionItemStack : list) {
                if (auctionItemStack.getItemId().compareTo(itemId) == 0) {
                    return auctionItemStack;
                }
            }
        }
        return null;
    }

    public List<AuctionItemStack> getPlayerAuctionItems(UUID playerUUID) {
        return registeredAuctionItemsMap.getOrDefault(playerUUID, new ArrayList<>());
    }

    public boolean hasAuctionItem(UUID playerUUID, AuctionItemStack auctionItemStack) {
        List<AuctionItemStack> list = registeredAuctionItemsMap.getOrDefault(playerUUID, new ArrayList<>());
        for (AuctionItemStack auctionItem : list) {
            if (auctionItem.getItemId().compareTo(auctionItemStack.getItemId()) == 0) {
                return true;
            }
        }
        return false;
    }

    public void removeAuctionItem(UUID playerUUID, AuctionItemStack auctionItemStack) {
        if (!hasAuctionItem(playerUUID, auctionItemStack)) {
            return;
        }
        List<AuctionItemStack> list = registeredAuctionItemsMap.getOrDefault(playerUUID, new ArrayList<>());
        int count = 0;
        for (AuctionItemStack auctionItem : list) {
            if (auctionItem.getItemId().compareTo(auctionItemStack.getItemId()) == 0) {
                break;
            } else {
                count++;
            }
        }
        list.remove(count);
        registeredAuctionItemsMap.put(playerUUID, list);
    }

    public void accessSellItem(AuctionItemStack auctionItemStack) {
    }

    public AuctionItemDataFile getDataFile() {
        return dataFile;
    }

}
