package me.msicraft.ctauction.Auction.Event;

import me.msicraft.ctauction.CTAuction;
import org.bukkit.event.Listener;

public class AuctionEvent implements Listener {

    private final CTAuction plugin;

    public AuctionEvent(CTAuction plugin) {
        this.plugin = plugin;
    }

}
