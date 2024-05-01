package me.msicraft.ctauction.Auction;

import me.msicraft.ctauction.Auction.Manager.AuctionManager;
import me.msicraft.ctauction.CTAuction;
import me.msicraft.ctauction.Enum.AuctionBuyFilter;
import me.msicraft.ctauction.Enum.GuiType;
import me.msicraft.ctauction.Enum.ItemStatus;
import me.msicraft.ctauction.PlayerData.PlayerData;
import me.msicraft.ctauction.Utils.GuiUtil;
import me.msicraft.ctauction.aCommon.CTInventory;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionInventory extends CTInventory implements InventoryHolder {

    private final Inventory inventory;
    private final GuiType guiType;

    public AuctionInventory(CTAuction plugin, GuiType guiType) {
        this.guiType = guiType;
        switch (guiType) {
            case MAIN -> {
                this.inventory = plugin.getServer().createInventory(this, 27, Component.text("경매"));
            }
            case BUY, SELL, MY_ITEM_LIST -> {
                this.inventory = plugin.getServer().createInventory(this, 54, Component.text("경매"));
            }
            default -> this.inventory = plugin.getServer().createInventory(this, 54, Component.text("경매"));
        }
    }

    private ItemStack getBackItemStack(String key) {
        return GuiUtil.createItemStack(Material.BARRIER, ChatColor.RED + "뒤로", null, -1,
                key, "Back");
    }

    public void updateBuyGui(PlayerData playerdata) {
        inventory.clear();
        setUpBuy(playerdata);
        playerdata.getPlayer().updateInventory();
    }

    public void open(Player player, GuiType guiType) {
        PlayerData playerdata = CTAuction.getPlugin().getPlayerDataManager().getPlayerData(player);
        player.openInventory(this.inventory);
        inventory.clear();
        switch (guiType) {
            case MAIN -> {
                setUpMain();
            }
            case BUY -> {
                setUpBuy(playerdata);
                CTAuction.getPlugin().getAuctionManager().addUsingBuyInventory(player);
            }
            case SELL -> {
                setUpSell(playerdata);
            }
            case MY_ITEM_LIST -> {
                setUpMyItemList(playerdata);
            }
        }
        player.updateInventory();
    }

    private void setUpMain() {
        ItemStack itemStack;
        itemStack = GuiUtil.createItemStack(Material.BOOKSHELF, ChatColor.GREEN + "구매", null, -1, "CT_Inventory_Main", "Buy");
        this.inventory.setItem(11, itemStack);
        itemStack = GuiUtil.createItemStack(Material.GOLD_INGOT, ChatColor.RED + "판매", null, -1, "CT_Inventory_Main", "Sell");
        this.inventory.setItem(15, itemStack);
        itemStack = GuiUtil.createItemStack(Material.CLOCK, ChatColor.YELLOW + "물품 보기", null, -1, "CT_Inventory_Main", "MyItemList");
        this.inventory.setItem(13, itemStack);
    }

    private void setUpBuy(PlayerData playerdata) {
        UUID playerUUID = playerdata.getPlayer().getUniqueId();
        String key = "CT_Inventory_Buy";
        AuctionManager auctionManager = CTAuction.getPlugin().getAuctionManager();
        ItemStack itemStack;
        itemStack = getBackItemStack(key);
        this.inventory.setItem(45, itemStack);
        List<AuctionItemStack> items = auctionManager.getRegisteredAuctionItems();
        int maxSize = items.size();
        int maxPageSize = maxSize / 45;
        int pageCount = (int) playerdata.getTag("auction_buy_page", 0);
        String pageS = "페이지: " + (pageCount + 1) + "/" + (maxPageSize + 1);
        itemStack = GuiUtil.createItemStack(Material.BOOK, pageS, GuiUtil.EMPTY_LORE, -1, key, "page");
        inventory.setItem(49, itemStack);
        itemStack = GuiUtil.createItemStack(Material.ARROW, "다음 페이지", GuiUtil.EMPTY_LORE, -1, key, "next");
        inventory.setItem(50, itemStack);
        itemStack = GuiUtil.createItemStack(Material.ARROW, "이전 페이지", GuiUtil.EMPTY_LORE, -1, key, "previous");
        inventory.setItem(48, itemStack);
        itemStack = GuiUtil.createItemStack(Material.BLACK_WOOL, ChatColor.GREEN + "구매자 숨김: " + playerdata.isHideBuyer(), GuiUtil.EMPTY_LORE,
                       -1, key, "hide_buyer");
        inventory.setItem(46, itemStack);
        AuctionBuyFilter auctionBuyFilter = playerdata.getAuctionBuyFilter();
        itemStack = GuiUtil.createItemStack(Material.OAK_SIGN, ChatColor.GREEN + "필터: " + auctionBuyFilter.getDisplay(),
                getBuyFilterLore(auctionBuyFilter), -1, key, "buy_filter");
        inventory.setItem(47, itemStack);
        int guiCount = 0;
        int lastCount = pageCount * 45;
        for (int i = lastCount; i < maxSize; i++) {
            AuctionItemStack auctionItemStack = items.get(i);
            if (auctionItemStack.getItemStatus() != ItemStatus.SELLING) {
                continue;
            }
            if (auctionBuyFilter == AuctionBuyFilter.BUY_RELATED_ITEM) {
                if (!auctionItemStack.getRelatedPlayerList().contains(playerUUID)) {
                    continue;
                }
            }
            itemStack = auctionItemStack.getAuctionBuyItemStack();
            this.inventory.setItem(guiCount, itemStack);
            guiCount++;
            if (guiCount >= 45) {
                break;
            }
        }
    }

    private List<String> getBuyFilterLore(AuctionBuyFilter auctionBuyFilter) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "좌 클릭: 변경");
        lore.add("");
        for (AuctionBuyFilter filter : AuctionBuyFilter.values()) {
            if (filter == auctionBuyFilter) {
                lore.add(ChatColor.YELLOW + "" + ChatColor.BOLD + ChatColor.UNDERLINE + filter.getDisplay());
            } else {
                lore.add(ChatColor.YELLOW + "" + ChatColor.BOLD + filter.getDisplay());
            }
        }
        return lore;
    }

    private void setUpSell(PlayerData playerdata) {
        String key = "CT_Inventory_Sell";
        ItemStack itemStack;
        itemStack = getBackItemStack(key);
        TempSellItemInfo tempSellItemInfo = playerdata.getTempSellItemInfo();
        this.inventory.setItem(45, itemStack);
        itemStack = GuiUtil.createItemStack(Material.GREEN_WOOL, ChatColor.GREEN + "등록하기", GuiUtil.EMPTY_LORE, -1, key, "register");
        this.inventory.setItem(30, itemStack);
        itemStack = GuiUtil.createItemStack(Material.PAPER, ChatColor.GREEN + "시작 가격: " + tempSellItemInfo.getPrice(), GuiUtil.EMPTY_LORE, -1, key, "start_price");
        this.inventory.setItem(32, itemStack);
        itemStack = tempSellItemInfo.getItemStack();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            itemStack = GuiUtil.createItemStack(Material.WHITE_STAINED_GLASS_PANE, ChatColor.GREEN + "인벤토리에서 아이템을 클릭해주세요", GuiUtil.EMPTY_LORE,
                    -1, key, "sell_item");
            this.inventory.setItem(13, itemStack);
        } else {
            this.inventory.setItem(13, itemStack);
        }
    }

    private void setUpMyItemList(PlayerData playerdata) {
        String key = "CT_Inventory_MyItemList";
        AuctionManager auctionManager = CTAuction.getPlugin().getAuctionManager();
        ItemStack itemStack;
        itemStack = getBackItemStack(key);
        inventory.setItem(45, itemStack);
        List<AuctionItemStack> list = auctionManager.getPlayerAuctionItems(playerdata.getPlayer().getUniqueId());
        List<AuctionItemStack> purchaseList = playerdata.getPurchasedItemList();
        list.addAll(purchaseList);
        int maxSize = list.size();
        int maxPageSize = maxSize / 45;
        int pageCount = (int) playerdata.getTag("auction_myitemlist_page", 0);
        String pageS = "페이지: " + (pageCount + 1) + "/" + (maxPageSize + 1);
        itemStack = GuiUtil.createItemStack(Material.BOOK, pageS, GuiUtil.EMPTY_LORE, -1, key, "page");
        inventory.setItem(49, itemStack);
        itemStack = GuiUtil.createItemStack(Material.ARROW, "다음 페이지", GuiUtil.EMPTY_LORE, -1, key, "next");
        inventory.setItem(50, itemStack);
        itemStack = GuiUtil.createItemStack(Material.ARROW, "이전 페이지", GuiUtil.EMPTY_LORE, -1, key, "previous");
        inventory.setItem(48, itemStack);
        int guiCount = 0;
        int lastCount = pageCount * 45;
        for (int i = lastCount; i < maxSize; i++) {
            AuctionItemStack auctionItemStack = list.get(i);
            itemStack = auctionItemStack.getMyListItemStack();
            inventory.setItem(guiCount, itemStack);
            guiCount++;
            if (guiCount >= 45) {
                break;
            }
        }
    }

    public GuiType getGuiType() {
        return guiType;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

}
