package me.msicraft.ctauction.Auction.Event;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.msicraft.ctauction.Auction.AuctionInventory;
import me.msicraft.ctauction.Auction.AuctionItemBuyerInfo;
import me.msicraft.ctauction.Auction.AuctionItemStack;
import me.msicraft.ctauction.Auction.TempSellItemInfo;
import me.msicraft.ctauction.CTAuction;
import me.msicraft.ctauction.Enum.GuiType;
import me.msicraft.ctauction.PlayerData.Manager.PlayerDataManager;
import me.msicraft.ctauction.PlayerData.PlayerData;
import me.msicraft.ctauction.aCommon.CustomEvent.AuctionItemPurchaseRegisterEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class AuctionInventoryChatEditEvent implements Listener {

    private final CTAuction plugin;

    public AuctionInventoryChatEditEvent(CTAuction plugin) {
        this.plugin = plugin;
    }

    private void cancelChatEdit(PlayerData playerData, String tag, GuiType guiType) {
        playerData.removeTag(tag);
        if (guiType != null) {
            AuctionInventory auctionInventory = (AuctionInventory) playerData.getCTInventory(guiType);
            Bukkit.getScheduler().runTask(plugin, ()-> {
                auctionInventory.open(playerData.getPlayer(), guiType);
            });
        }
    }

    @EventHandler
    public void auctionBuyChatEvent(AsyncChatEvent e) {
        Player player = e.getPlayer();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        PlayerData playerData = playerDataManager.getPlayerData(player);
        PlainTextComponentSerializer plainText = PlainTextComponentSerializer.plainText();
        String message = plainText.serialize(e.message());
        if (playerData.hasTag("auction_buy_item_mode")) {
            e.setCancelled(true);
            if (message.equals("cancel") || message.equals("취소")) {
                cancelChatEdit(playerData, "auction_buy_item_mode", GuiType.BUY);
                return;
            }
            try {
                UUID itemId = (UUID) playerData.getTag("auction_buy_item_mode", null);
                if (itemId == null) {
                    player.sendMessage(Component.text(ChatColor.RED + "잘못된 아이템 Id 입니다"));
                    cancelChatEdit(playerData, "auction_buy_item_mode", GuiType.BUY);
                    return;
                }
                AuctionItemStack auctionItemStack = plugin.getAuctionManager().getAuctionItemStack(itemId);
                if (auctionItemStack == null) {
                    player.sendMessage(Component.text(ChatColor.RED + "존재하지 않는 아이템입니다"));
                    cancelChatEdit(playerData, "auction_buy_item_mode", GuiType.BUY);
                    return;
                }
                Economy economy = CTAuction.getEconomy();
                int price = Integer.parseInt(message);
                double balance = economy.getBalance(player);
                if (price > balance) {
                    player.sendMessage(Component.text(ChatColor.RED + "충분한 돈이 없습니다."));
                    cancelChatEdit(playerData, "auction_buy_item_mode", GuiType.BUY);
                    return;
                }
                AuctionItemBuyerInfo auctionItemBuyerInfo = auctionItemStack.getAuctionItemBuyerInfo();
                if (auctionItemBuyerInfo == null) {
                    if (price < auctionItemStack.getStartPrice()) {
                        player.sendMessage(Component.text(ChatColor.RED + "제시한 금액이 최소 가격보다 작습니다."));
                        cancelChatEdit(playerData, "auction_buy_item_mode", GuiType.BUY);
                    } else {
                        player.sendMessage(Component.text(ChatColor.GREEN + "해당 아이템에 대해 구매 등록을 하였습니다."));
                        economy.withdrawPlayer(player, price);

                        auctionItemStack.setAuctionItemBuyerInfo(new AuctionItemBuyerInfo(player, price, playerData.isHideBuyer()));
                        cancelChatEdit(playerData, "auction_buy_item_mode", GuiType.BUY);

                        Bukkit.getPluginManager().callEvent(new AuctionItemPurchaseRegisterEvent(auctionItemStack));
                    }
                } else {
                    if (price < auctionItemBuyerInfo.getPrice()) {
                        player.sendMessage(Component.text(ChatColor.RED + "제시한 금액이 최소 가격보다 작습니다."));
                        cancelChatEdit(playerData, "auction_buy_item_mode", GuiType.BUY);
                    } else {
                        player.sendMessage(Component.text(ChatColor.GREEN + "해당 아이템에 대해 구매 등록을 하였습니다."));
                        economy.withdrawPlayer(player, price);

                        OfflinePlayer buyerPlayer = auctionItemBuyerInfo.getOfflinePlayer();
                        economy.depositPlayer(buyerPlayer, auctionItemBuyerInfo.getPrice());

                        auctionItemStack.setAuctionItemBuyerInfo(new AuctionItemBuyerInfo(player, price, playerData.isHideBuyer()));
                        cancelChatEdit(playerData, "auction_buy_item_mode", GuiType.BUY);

                        Bukkit.getPluginManager().callEvent(new AuctionItemPurchaseRegisterEvent(auctionItemStack));
                    }
                }
            } catch (NumberFormatException ex) {
                player.sendMessage(Component.text(ChatColor.RED + "숫자를 입력해주세요"));
                cancelChatEdit(playerData, "auction_buy_item_mode", GuiType.BUY);
            }
        } else if (playerData.hasTag("auction_sell_item_mode")) {
            e.setCancelled(true);
            if (message.equals("cancel") || message.equals("취소")) {
                cancelChatEdit(playerData, "auction_buy_item_mode", GuiType.BUY);
                return;
            }
            try {
                int price = Integer.parseInt(message);
                TempSellItemInfo tempSellItemInfo = playerData.getTempSellItemInfo();
                tempSellItemInfo.setPrice(price);
                player.sendMessage(Component.text(ChatColor.GREEN + "시작 가격이 설정되었습니다"));
                cancelChatEdit(playerData, "auction_sell_item_mode", GuiType.SELL);
            } catch (NumberFormatException ex) {
                player.sendMessage(Component.text(ChatColor.RED + "숫자를 입력해주세요"));
                cancelChatEdit(playerData, "auction_sell_item_mode", GuiType.SELL);
            }
        }
    }

}
