package me.msicraft.ctauction;

public class CTAuctionAPI {

    private static CTAuctionAPI plugin;

    public static CTAuctionAPI getPlugin() {
        return plugin;
    }

    public CTAuctionAPI() {
        plugin = this;
    }

}
