package me.msicraft.ctauction.Auction;

import me.msicraft.ctauction.CTAuction;
import me.msicraft.ctauction.Enum.ItemStatus;
import me.msicraft.ctauction.Utils.Base64Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionItemStack {

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초");

    private final UUID itemId;
    private final UUID ownerUUID;
    private final ItemStack itemStack;

    private AuctionItemBuyerInfo auctionItemBuyerInfo;

    private final int startPrice;
    private final long registrationTime;
    private final long expirationTime;

    private ItemStatus itemStatus;

    private final List<UUID> relatedPlayerList = new ArrayList<>();

    private String getRelatedPlayerDataString() {
        String s = null;
        for (UUID uuid : relatedPlayerList) {
            if (s.isEmpty()) {
                s = uuid.toString();
            } else {
                s = s + ":" + uuid;
            }
        }
        if (s == null) {
            return "empty";
        }
        return Base64Util.byteArrayToString(s.getBytes());
    }

    public AuctionItemStack(UUID ownerUUID, ItemStack itemStack, int startPrice, long expirationTime) {
        this.itemId = UUID.randomUUID();
        this.ownerUUID = ownerUUID;
        this.itemStack = itemStack;
        this.startPrice = startPrice;
        this.registrationTime = System.currentTimeMillis();
        long expirationSeconds = expirationTime;
        if (expirationTime == -1) {
            expirationSeconds = CTAuction.getPlugin().getConfig().contains("DefaultSettings.ExpirationTime") ?
                    CTAuction.getPlugin().getConfig().getLong("DefaultSettings.ExpirationTime") : 86400;
        }
        this.expirationTime = registrationTime + (expirationSeconds * 1000L);
        this.auctionItemBuyerInfo = null;
        this.itemStatus = ItemStatus.SELLING;
    }

    public AuctionItemStack(String dataString) {
        String[] split = dataString.split(":");
        itemId = UUID.fromString(split[0]);
        ownerUUID = UUID.fromString(split[1]);
        itemStack = ItemStack.deserializeBytes(Base64Util.stringToByteArray(split[2]));
        startPrice = Integer.parseInt(split[3]);
        registrationTime = Long.parseLong(split[4]);
        expirationTime = Long.parseLong(split[5]);
        itemStatus = ItemStatus.valueOf(split[6].toUpperCase());

        String relatedPlayerData = split[7];
        if (!relatedPlayerData.equals("empty")) {
            byte[] dataBytes = Base64Util.stringToByteArray(relatedPlayerData);
            String relatedPlayerDataString = new String(dataBytes);
            String[] split2 = relatedPlayerDataString.split(":");
            for (String s : split2) {
                relatedPlayerList.add(UUID.fromString(s));
            }
        }
        try {
            String buyerDataString = split[8] + ":" + split[9] + ":" + split[10];
            this.auctionItemBuyerInfo = new AuctionItemBuyerInfo(buyerDataString);
        } catch (ArrayIndexOutOfBoundsException e) {
            this.auctionItemBuyerInfo = null;
        }
    }

    public String getDataString() {
        if (auctionItemBuyerInfo != null) {
            return itemId + ":" + ownerUUID + ":" + Base64Util.byteArrayToString(itemStack.serializeAsBytes())
                + ":" + startPrice + ":" +registrationTime + ":" + expirationTime
                    + ":" + itemStatus.name().toUpperCase() + ":" + getRelatedPlayerDataString() + ":" + auctionItemBuyerInfo.getDataString();
        }
        return itemId + ":" + ownerUUID + ":" + Base64Util.byteArrayToString(itemStack.serializeAsBytes())
                + ":" + startPrice + ":" + registrationTime + ":" + expirationTime
                + ":" + itemStatus.name().toUpperCase() + ":" + getRelatedPlayerDataString();
    }

    public UUID getItemId() {
        return itemId;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public ItemStack getMyListItemStack() {
        ItemStack copyStack = new ItemStack(itemStack);
        ItemMeta itemMeta = copyStack.getItemMeta();
        List<Component> lore;
        if (itemMeta.hasLore()) {
            lore = new ArrayList<>(itemMeta.lore());
        } else {
            lore = new ArrayList<>();
        }
        lore.add(Component.text(""));
        if (itemStatus == ItemStatus.SOLD) {
            lore.add(Component.text(ChatColor.YELLOW + "좌 클릭: 아이템 수령"));
        } else {
            lore.add(Component.text(ChatColor.YELLOW + "좌 클릭: 등록 취소"));
        }
        lore.add(Component.text(""));
        lore.add(Component.text(ChatColor.AQUA + "판매자: " + Bukkit.getOfflinePlayer(ownerUUID).getName()));
        lore.add(Component.text(ChatColor.AQUA + "시작 가격: " + startPrice));
        lore.add(Component.text(""));
        if (auctionItemBuyerInfo != null) {
            if (auctionItemBuyerInfo.isHideBuyer()) {
                lore.add(Component.text(ChatColor.AQUA + "제시된 가격: " +auctionItemBuyerInfo.getPrice()));
                lore.add(Component.text(ChatColor.AQUA + "구매자: " + ChatColor.MAGIC + "Unknown"));
            } else {
                lore.add(Component.text(ChatColor.AQUA + "제시된 가격: " +auctionItemBuyerInfo.getPrice()));
                lore.add(Component.text(ChatColor.AQUA + "구매자: " + auctionItemBuyerInfo.getOfflinePlayer().getName()));
            }
        } else {
            lore.add(Component.text(ChatColor.AQUA + "구매등록 정보 없음"));
        }
        lore.add(Component.text(""));
        lore.add(Component.text(ChatColor.GREEN + "남은 시간: " + getLeftTimeToString()));
        lore.add(Component.text(ChatColor.GREEN + "등록 날짜: " + simpleDateFormat.format(registrationTime)));
        itemMeta.lore(lore);
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(new NamespacedKey(CTAuction.getPlugin(), "CT_Inventory_MyItemList"), PersistentDataType.STRING, "MyListItem");
        dataContainer.set(new NamespacedKey(CTAuction.getPlugin(), "CT_Inventory_MyItemList_Info"), PersistentDataType.STRING, itemId.toString());
        copyStack.setItemMeta(itemMeta);
        return copyStack;
    }

    public ItemStack getAuctionBuyItemStack() {
        ItemStack copyStack = new ItemStack(itemStack);
        ItemMeta itemMeta = copyStack.getItemMeta();
        List<Component> lore;
        if (itemMeta.hasLore()) {
            lore = new ArrayList<>(itemMeta.lore());
        } else {
            lore = new ArrayList<>();
        }
        lore.add(Component.text(""));
        lore.add(Component.text(ChatColor.YELLOW + "좌 클릭: 구매 등록"));
        lore.add(Component.text(""));
        lore.add(Component.text(ChatColor.AQUA + "판매자: " + Bukkit.getOfflinePlayer(ownerUUID).getName()));
        lore.add(Component.text(ChatColor.AQUA + "시작 가격: " + startPrice));
        lore.add(Component.text(""));
        if (auctionItemBuyerInfo != null) {
            if (auctionItemBuyerInfo.isHideBuyer()) {
                lore.add(Component.text(ChatColor.AQUA + "제시된 가격: " +auctionItemBuyerInfo.getPrice()));
                lore.add(Component.text(ChatColor.AQUA + "구매자: " + ChatColor.MAGIC + "Unknown"));
            } else {
                lore.add(Component.text(ChatColor.AQUA + "제시된 가격: " +auctionItemBuyerInfo.getPrice()));
                lore.add(Component.text(ChatColor.AQUA + "구매자: " + auctionItemBuyerInfo.getOfflinePlayer().getName()));
            }
        } else {
            lore.add(Component.text(ChatColor.AQUA + "구매등록 정보 없음"));
        }
        lore.add(Component.text(""));
        lore.add(Component.text(ChatColor.GREEN + "남은 시간: " + getLeftTimeToString()));
        lore.add(Component.text(ChatColor.GREEN + "등록 날짜: " + simpleDateFormat.format(registrationTime)));
        itemMeta.lore(lore);
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(new NamespacedKey(CTAuction.getPlugin(), "CT_Inventory_Buy"), PersistentDataType.STRING, "BuyItem");
        dataContainer.set(new NamespacedKey(CTAuction.getPlugin(), "CT_Inventory_Buy_Info"), PersistentDataType.STRING, itemId.toString());
        copyStack.setItemMeta(itemMeta);
        return copyStack;
    }

    public ItemStack getItemStack(boolean isCopy) {
        if (isCopy) {
            return new ItemStack(this.itemStack);
        }
        return itemStack;
    }

    public int getStartPrice() {
        return startPrice;
    }

    public long getRegistrationTime() {
        return registrationTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public ItemStatus getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(ItemStatus itemStatus) {
        this.itemStatus = itemStatus;
    }

    public int getLeftSeconds() {
        return (int) (expirationTime - System.currentTimeMillis()) / 1000;
    }

    public String getLeftTimeToString() {
        if (itemStatus == ItemStatus.SOLD) {
            return ItemStatus.SOLD.getStatus();
        }
        if (itemStatus == ItemStatus.EXPIRED) {
            return ItemStatus.EXPIRED.getStatus();
        }
        int left = getLeftSeconds();
        int day = left / (60 * 60 * 24);
        int hour = (left - day * 60 * 60 * 24) / (60 * 60);
        int minute = (left - day * 60 * 60 * 24 - hour * 3600) / 60;
        int seconds = left % 60;
        if (day > 0) {
            return day + "일 " + hour + "시간 " + minute + "분 " + seconds + "초";
        }
        if (hour > 0) {
            return hour + "시간 " + minute + "분 " + seconds + "초";
        }
        if (minute > 0) {
            return minute + "분 " + seconds + "초";
        }
        return seconds + "초";
    }

    public AuctionItemBuyerInfo getAuctionItemBuyerInfo() {
        return auctionItemBuyerInfo;
    }

    public void setAuctionItemBuyerInfo(AuctionItemBuyerInfo auctionItemBuyerInfo) {
        this.auctionItemBuyerInfo = auctionItemBuyerInfo;
    }

    public void addRelatedPlayer(Player player) {
        relatedPlayerList.add(player.getUniqueId());
    }

    public void addRelatedPlayer(OfflinePlayer offlinePlayer) {
        relatedPlayerList.add(offlinePlayer.getUniqueId());
    }

    public List<UUID> getRelatedPlayerList() {
        return relatedPlayerList;
    }

    public void removeRelatedPlayer(Player player) {
        relatedPlayerList.remove(player.getUniqueId());
    }

    public void resetRelatedPlayer() {
        relatedPlayerList.clear();
    }

}
