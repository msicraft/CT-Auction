package me.msicraft.ctauction.Auction.Event;

import me.msicraft.ctauction.Auction.AuctionInventory;
import me.msicraft.ctauction.Auction.AuctionItemBuyerInfo;
import me.msicraft.ctauction.Auction.AuctionItemStack;
import me.msicraft.ctauction.Auction.Manager.AuctionManager;
import me.msicraft.ctauction.Auction.TempSellItemInfo;
import me.msicraft.ctauction.CTAuction;
import me.msicraft.ctauction.Enum.AuctionBuyFilter;
import me.msicraft.ctauction.Enum.GuiType;
import me.msicraft.ctauction.PlayerData.Manager.PlayerDataManager;
import me.msicraft.ctauction.PlayerData.PlayerData;
import me.msicraft.ctauction.Utils.GuiUtil;
import me.msicraft.ctauction.Utils.PlayerUtil;
import me.msicraft.ctauction.aCommon.CustomEvent.AuctionItemRegisterEvent;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class AuctionInventoryEvent implements Listener {

    private final CTAuction plugin;

    public AuctionInventoryEvent(CTAuction plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void closeAuctionBuyInventory(InventoryCloseEvent e) {
        Inventory topInventory = e.getView().getTopInventory();
        if (topInventory.getHolder(false) instanceof AuctionInventory auctionInventory) {
            if (auctionInventory.getGuiType() == GuiType.BUY) {
                Player player = (Player) e.getPlayer();
                plugin.getAuctionManager().removeUsingBuyInventory(player);
            }
        }
    }

    @EventHandler
    public void onClickAuctionInventory(InventoryClickEvent e) {
        Inventory topInventory = e.getView().getTopInventory();
        if (topInventory.getHolder(false) instanceof AuctionInventory auctionInventory) {
            ClickType type = e.getClick();
            if (type == ClickType.NUMBER_KEY || type == ClickType.SWAP_OFFHAND
                    || type == ClickType.SHIFT_LEFT || type == ClickType.SHIFT_RIGHT) {
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            ItemStack itemStack = e.getCurrentItem();
            if (itemStack == null) {
                return;
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                return;
            }
            AuctionManager auctionManager = CTAuction.getPlugin().getAuctionManager();
            PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
            PlayerData playerData = playerDataManager.getPlayerData(player);
            Economy economy = CTAuction.getEconomy();
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();

            if (auctionInventory.getGuiType() == GuiType.SELL) {
                int clickSlot = e.getSlot();
                InventoryType inventoryType = e.getClickedInventory().getType();
                TempSellItemInfo tempSellItemInfo = playerData.getTempSellItemInfo();
                ItemStack sellItemStack = tempSellItemInfo.getItemStack();
                switch (inventoryType) {
                    case CHEST -> {
                        if (clickSlot == 13 && sellItemStack != null && sellItemStack.getType() != Material.AIR) {
                            int emptySlot = PlayerUtil.getPlayerEmptySlot(player);
                            if (emptySlot != -1) {
                                player.getInventory().setItem(emptySlot, sellItemStack);
                                tempSellItemInfo.setItemStack(GuiUtil.AIR_STACK);
                            } else {
                                player.sendMessage(Component.text(ChatColor.RED + "인벤토리에 공간이 없습니다."));
                            }
                        }
                    }
                    case PLAYER -> {
                        if (sellItemStack == null || sellItemStack.getType() == Material.AIR) {
                            ItemStack selectItem = player.getInventory().getItem(clickSlot);
                            tempSellItemInfo.setItemStack(selectItem);
                            player.getInventory().setItem(clickSlot, GuiUtil.AIR_STACK);
                        } else {
                            player.sendMessage(Component.text(ChatColor.RED + "이미 선택되어 있는 아이템이 있습니다."));
                        }
                    }
                }
                auctionInventory.open(player, GuiType.SELL);
            }

            if (dataContainer.has(new NamespacedKey(plugin, "CT_Inventory_Main"), PersistentDataType.STRING)) {
                String data = dataContainer.get(new NamespacedKey(plugin, "CT_Inventory_Main"), PersistentDataType.STRING);
                if (data != null) {
                    AuctionInventory subAuctionInventory;
                    switch (data) {
                        case "Buy" -> {
                            subAuctionInventory = (AuctionInventory) playerData.getCTInventory(GuiType.BUY);
                            subAuctionInventory.open(player, GuiType.BUY);
                        }
                        case "Sell" -> {
                            int maxItems = plugin.getConfig().contains("DefaultSettings.MaxItems") ? plugin.getConfig().getInt("DefaultSettings.MaxItems") : -1;
                            if (maxItems != -1) {
                                if (auctionManager.getPlayerAuctionItems(player.getUniqueId()).size() >= maxItems) {
                                    player.sendMessage(Component.text(ChatColor.RED + "더 이상 아이템을 등록할 수 없습니다"));
                                    player.closeInventory();
                                    return;
                                }
                            }
                            subAuctionInventory = (AuctionInventory) playerData.getCTInventory(GuiType.SELL);
                            subAuctionInventory.open(player, GuiType.SELL);
                        }
                        case "MyItemList" -> {
                            subAuctionInventory = (AuctionInventory) playerData.getCTInventory(GuiType.MY_ITEM_LIST);
                            subAuctionInventory.open(player, GuiType.MY_ITEM_LIST);
                        }
                    }
                }
            } else if (dataContainer.has(new NamespacedKey(plugin, "CT_Inventory_Buy"), PersistentDataType.STRING)) {
                String data = dataContainer.get(new NamespacedKey(plugin, "CT_Inventory_Buy"), PersistentDataType.STRING);
                if (data != null) {
                    int maxPage = auctionManager.getRegisteredAuctionItems().size() / 45;
                    int current = (int) playerData.getTag("auction_buy_page", 0);
                    switch (data) {
                        case "Back" -> {
                            AuctionInventory auction = (AuctionInventory) playerData.getCTInventory(GuiType.MAIN);
                            auction.open(player, GuiType.MAIN);
                        }
                        case "next" -> {
                            int next = current + 1;
                            if (next > maxPage) {
                                next = 0;
                            }
                            playerData.setTag("auction_buy_page", next);
                            auctionInventory.open(player, GuiType.BUY);
                        }
                        case "previous" -> {
                            int previous = current - 1;
                            if (previous < 0) {
                                previous = maxPage;
                            }
                            playerData.setTag("auction_buy_page", previous);
                            auctionInventory.open(player, GuiType.BUY);
                        }
                        case "hide_buyer" -> {
                            playerData.setHideBuyer(!playerData.isHideBuyer());
                            auctionInventory.open(player, GuiType.BUY);
                        }
                        case "buy_filter" -> {
                            AuctionBuyFilter auctionBuyFilter = playerData.getAuctionBuyFilter();
                            if (auctionBuyFilter == AuctionBuyFilter.DEFAULT) {
                                playerData.setAuctionBuyFilter(AuctionBuyFilter.BUY_RELATED_ITEM);
                            } else {
                                playerData.setAuctionBuyFilter(AuctionBuyFilter.DEFAULT);
                            }
                            auctionInventory.open(player, GuiType.BUY);
                        }
                        case "BuyItem" -> {
                            String itemIdString = dataContainer.get(new NamespacedKey(plugin, "CT_Inventory_Buy_Info"), PersistentDataType.STRING);
                            if (itemIdString != null && e.isLeftClick()) {
                                UUID itemId = UUID.fromString(itemIdString);
                                AuctionItemStack auctionItemStack = auctionManager.getAuctionItemStack(itemId);
                                if (auctionItemStack != null) {
                                    if (auctionItemStack.getOwnerUUID().compareTo(player.getUniqueId()) == 0) {
                                        player.sendMessage(Component.text(ChatColor.RED + "자신의 아이템은 구매할 수 없습니다"));
                                        return;
                                    }
                                    double playerBalance = CTAuction.getEconomy().getBalance(player);
                                    AuctionItemBuyerInfo auctionItemBuyerInfo = auctionItemStack.getAuctionItemBuyerInfo();
                                    int currentPrice;
                                    if (auctionItemBuyerInfo != null) {
                                        currentPrice = auctionItemBuyerInfo.getPrice();
                                    } else {
                                        currentPrice = auctionItemStack.getStartPrice();
                                    }
                                    if (currentPrice > playerBalance) {
                                        player.sendMessage(Component.text(ChatColor.RED + "구매를 위한 충분한 돈이 없습니다"));
                                    } else {
                                        playerData.setTag("auction_buy_item_mode", itemId);
                                        player.sendMessage(Component.text(ChatColor.GRAY + "구매가격을 입력해주세요"));
                                        player.sendMessage(Component.text(ChatColor.GRAY + "현재 가격: " + currentPrice));
                                        player.sendMessage(Component.text(ChatColor.GRAY + "'cancel' 또는 '취소' 입력시 취소"));
                                        player.closeInventory();
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (dataContainer.has(new NamespacedKey(plugin, "CT_Inventory_Sell"), PersistentDataType.STRING)) {
                String data = dataContainer.get(new NamespacedKey(plugin, "CT_Inventory_Sell"), PersistentDataType.STRING);
                if (data != null) {
                    TempSellItemInfo tempSellItemInfo = playerData.getTempSellItemInfo();
                    switch (data) {
                        case "Back" -> {
                            AuctionInventory auction = (AuctionInventory) playerData.getCTInventory(GuiType.MAIN);
                            auction.open(player, GuiType.MAIN);
                        }
                        case "register" -> {
                            ItemStack sellItemStack = tempSellItemInfo.getItemStack();
                            if (sellItemStack == null || sellItemStack.getType() == Material.AIR) {
                                player.sendMessage(Component.text(ChatColor.RED + "선택되어 있는 아이템이 없습니다."));
                            } else {
                                AuctionItemStack auctionItemStack = new AuctionItemStack(player.getUniqueId(), sellItemStack,
                                        tempSellItemInfo.getPrice(), -1);
                                auctionManager.addAuctionItem(player.getUniqueId(), auctionItemStack);
                                tempSellItemInfo.reset();

                                Bukkit.getPluginManager().callEvent(new AuctionItemRegisterEvent(auctionItemStack));

                                player.sendMessage(Component.text(ChatColor.GREEN + "아이템이 등록되었습니다"));
                                int maxItems = plugin.getConfig().contains("DefaultSettings.MaxItems") ? plugin.getConfig().getInt("DefaultSettings.MaxItems") : -1;
                                if (maxItems != -1) {
                                    if (auctionManager.getPlayerAuctionItems(player.getUniqueId()).size() >= maxItems) {
                                        player.closeInventory();
                                        return;
                                    }
                                }
                                auctionInventory.open(player, GuiType.SELL);
                            }
                        }
                        case "start_price" -> {
                            playerData.setTag("auction_sell_item_mode", 0);
                            player.sendMessage(Component.text(ChatColor.GRAY + "시작가격을 입력해주세요"));
                            player.sendMessage(Component.text(ChatColor.GRAY + " 'cancel' 또는 '취소' 입력시 취소"));
                            player.closeInventory();
                        }
                    }
                }
            } else if (dataContainer.has(new NamespacedKey(plugin, "CT_Inventory_MyItemList"), PersistentDataType.STRING)) {
                String data = dataContainer.get(new NamespacedKey(plugin, "CT_Inventory_MyItemList"), PersistentDataType.STRING);
                if (data != null) {
                    int maxPage = auctionManager.getPlayerAuctionItems(player.getUniqueId()).size() / 45;
                    int current = (int) playerData.getTag("auction_myitemlist_page", 0);
                    switch (data) {
                        case "Back" -> {
                            AuctionInventory auction = (AuctionInventory) playerData.getCTInventory(GuiType.MAIN);
                            auction.open(player, GuiType.MAIN);
                        }
                        case "next" -> {
                            int next = current + 1;
                            if (next > maxPage) {
                                next = 0;
                            }
                            playerData.setTag("auction_myitemlist_page", next);
                            auctionInventory.open(player, GuiType.MY_ITEM_LIST);
                        }
                        case "previous" -> {
                            int previous = current - 1;
                            if (previous < 0) {
                                previous = maxPage;
                            }
                            playerData.setTag("auction_myitemlist_page", previous);
                            auctionInventory.open(player, GuiType.MY_ITEM_LIST);
                        }
                        case "MyListItem" -> {
                            String itemIdString = dataContainer.get(new NamespacedKey(plugin, "CT_Inventory_MyItemList_Info"), PersistentDataType.STRING);
                            if (itemIdString != null && e.isLeftClick()) {
                                UUID itemId = UUID.fromString(itemIdString);
                                AuctionItemStack auctionItemStack = auctionManager.getAuctionItemStack(player.getUniqueId(), itemId);
                                if (auctionItemStack != null) {
                                    int emptySlot = PlayerUtil.getPlayerEmptySlot(player);
                                    if (emptySlot != -1) {
                                        player.getInventory().setItem(emptySlot, auctionItemStack.getItemStack(false));
                                        auctionManager.removeAuctionItem(player.getUniqueId(), auctionItemStack);
                                        auctionInventory.open(player, GuiType.MY_ITEM_LIST);
                                    } else {
                                        player.sendMessage(Component.text(ChatColor.RED + "인벤토리에 공간이 없습니다."));
                                        return;
                                    }
                                    AuctionItemBuyerInfo auctionItemBuyerInfo = auctionItemStack.getAuctionItemBuyerInfo();
                                    if (auctionItemBuyerInfo != null) {
                                        OfflinePlayer buyer = auctionItemBuyerInfo.getOfflinePlayer();
                                        economy.depositPlayer(buyer, auctionItemBuyerInfo.getPrice());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
