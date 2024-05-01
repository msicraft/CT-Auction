package me.msicraft.ctauction.PlayerData;

import me.msicraft.ctauction.Auction.AuctionInventory;
import me.msicraft.ctauction.Auction.AuctionItemStack;
import me.msicraft.ctauction.Auction.TempSellItemInfo;
import me.msicraft.ctauction.CTAuction;
import me.msicraft.ctauction.Enum.AuctionBuyFilter;
import me.msicraft.ctauction.Enum.GuiType;
import me.msicraft.ctauction.PlayerData.DataFile.PlayerDataFile;
import me.msicraft.ctauction.aCommon.CTInventory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerData {

    private final Player player;
    private PlayerDataFile playerDataFile;
    private final Map<GuiType, CTInventory> guiInventoryMap = new HashMap<>();
    private final Map<String, Object> tagMap = new HashMap<>();
    private final TempSellItemInfo tempSellItemInfo;

    private final List<AuctionItemStack> purchasedItemList = new ArrayList<>();

    private boolean isHideBuyer = false;
    private AuctionBuyFilter auctionBuyFilter = AuctionBuyFilter.DEFAULT;

    public PlayerData(Player player) {
        this.player = player;
        this.playerDataFile = new PlayerDataFile(player);

        FileConfiguration config = playerDataFile.getConfig();
        this.tempSellItemInfo = new TempSellItemInfo(config.contains("TempSellItemInfo") ? config.getString("TempSellItemInfo") : null);

        List<String> purchasedItemDataList = config.getStringList("PurchasedItemList");
        for (String s : purchasedItemDataList) {
            purchasedItemList.add(new AuctionItemStack(s));
        }
    }

    public void saveDataToFile() {
        FileConfiguration config = playerDataFile.getConfig();
        for (String tag : tagMap.keySet()) {
        }
        List<String> purchasedItemDataList = new ArrayList<>();
        for (AuctionItemStack stack : purchasedItemList) {
            purchasedItemDataList.add(stack.getDataString());
        }
        config.set("PurchasedItemList", purchasedItemDataList);
        config.set("TempSellItemInfo", tempSellItemInfo.getData());

        playerDataFile.saveConfig();
    }

    public void registerInventory(GuiType type, CTInventory ctInventory) {
        guiInventoryMap.put(type, ctInventory);
    }

    public void removeInventory(GuiType type) {
        guiInventoryMap.remove(type);
    }

    public boolean hasTag(String tag) {
        return tagMap.containsKey(tag);
    }

    public Object getTag(String tag, Object defaultValue) {
        return tagMap.getOrDefault(tag, defaultValue);
    }

    public void setTag(String tag, Object value) {
        tagMap.put(tag, value);
    }

    public void removeTag(String tag) {
        tagMap.remove(tag);
    }

    public TempSellItemInfo getTempSellItemInfo() {
        return tempSellItemInfo;
    }

    public PlayerDataFile getPlayerDataFile() {
        return playerDataFile;
    }

    public List<AuctionItemStack> getPurchasedItemList() {
        return purchasedItemList;
    }

    public void addPurchaseItem(AuctionItemStack auctionItemStack) {
        purchasedItemList.add(auctionItemStack);
    }

    public void removePurchaseItem(AuctionItemStack auctionItemStack) {
        int count = 0;
        for (AuctionItemStack stack : purchasedItemList) {
            if (stack.getItemId().compareTo(auctionItemStack.getItemId()) == 0) {
                break;
            } else {
                count++;
            }
        }
        purchasedItemList.remove(count);
    }

    public void addPurchasedRelatedItem(AuctionItemStack auctionItemStack) {
        purchasedItemList.add(auctionItemStack);
    }

    public boolean isHideBuyer() {
        return isHideBuyer;
    }

    public void setHideBuyer(boolean hideBuyer) {
        isHideBuyer = hideBuyer;
    }

    public AuctionBuyFilter getAuctionBuyFilter() {
        return auctionBuyFilter;
    }

    public void setAuctionBuyFilter(AuctionBuyFilter auctionBuyFilter) {
        this.auctionBuyFilter = auctionBuyFilter;
    }

    @NotNull
    public CTInventory getCTInventory(GuiType type) {
        CTInventory ctInventory = guiInventoryMap.getOrDefault(type, null);
        if (ctInventory == null) {
            ctInventory = new AuctionInventory(CTAuction.getPlugin(), type);
            registerInventory(type, ctInventory);
        }
        return ctInventory;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

}
