package me.msicraft.ctauction.Auction;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class AuctionItemBuyerInfo {

    private final OfflinePlayer player;
    private final int price;
    private final boolean hideBuyer;

    public AuctionItemBuyerInfo(OfflinePlayer player, int price, boolean hideBuyer) {
        this.player = player;
        this.price = price;
        this.hideBuyer = hideBuyer;
    }

    public AuctionItemBuyerInfo(String dataString) {
        String[] split = dataString.split(":");
        this.player = Bukkit.getServer().getOfflinePlayer(UUID.fromString(split[0]));
        this.price = Integer.parseInt(split[1]);
        this.hideBuyer = Boolean.parseBoolean(split[2]);
    }

    public int getPrice() {
        return price;
    }

    public OfflinePlayer getOfflinePlayer() {
        return player;
    }

    public boolean isHideBuyer() {
        return hideBuyer;
    }

    public String getDataString() {
        return player.getUniqueId() + ":" + price + ":" + hideBuyer;
    }

}
