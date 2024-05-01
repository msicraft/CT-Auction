package me.msicraft.ctauction.aCommon.CustomEvent;

import me.msicraft.ctauction.Auction.AuctionItemStack;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AuctionItemExpiredEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final AuctionItemStack auctionItemStack;

    public AuctionItemExpiredEvent(AuctionItemStack auctionItemStack) {
        this.auctionItemStack = auctionItemStack;
    }

    public AuctionItemStack getAuctionItemStack() {
        return auctionItemStack;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
