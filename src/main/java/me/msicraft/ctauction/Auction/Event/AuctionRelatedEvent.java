package me.msicraft.ctauction.Auction.Event;

import me.msicraft.ctauction.Auction.AuctionItemBuyerInfo;
import me.msicraft.ctauction.Auction.AuctionItemStack;
import me.msicraft.ctauction.Auction.Manager.AuctionManager;
import me.msicraft.ctauction.CTAuction;
import me.msicraft.ctauction.PlayerData.DataFile.PlayerDataFile;
import me.msicraft.ctauction.PlayerData.PlayerData;
import me.msicraft.ctauction.aCommon.CustomEvent.AuctionItemExpiredEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionRelatedEvent implements Listener {

    private final CTAuction plugin;

    public AuctionRelatedEvent(CTAuction plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void auctionItemExpired(AuctionItemExpiredEvent e) {
        AuctionItemStack auctionItemStack = e.getAuctionItemStack();
        AuctionManager auctionManager = plugin.getAuctionManager();
        AuctionItemBuyerInfo auctionItemBuyerInfo = auctionItemStack.getAuctionItemBuyerInfo();

        UUID ownerUUID = auctionItemStack.getOwnerUUID();
        auctionManager.removeAuctionItem(ownerUUID, auctionItemStack);
        CTAuction.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(ownerUUID), auctionItemBuyerInfo.getPrice());

        OfflinePlayer offlinePlayer = auctionItemBuyerInfo.getOfflinePlayer();
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
            playerData.addPurchaseItem(auctionItemStack);
        } else {
            PlayerDataFile playerDataFile = new PlayerDataFile(offlinePlayer);
            List<String> dataList = playerDataFile.getConfig().contains("PurchasedItemList") ? playerDataFile.getConfig().getStringList("PurchasedItemList") : new ArrayList<>();
            dataList.add(auctionItemStack.getDataString());
            playerDataFile.getConfig().set("PurchasedItemList", dataList);
            playerDataFile.saveConfig();
        }
    }

}
