package me.msicraft.ctauction.Enum;

public enum AuctionBuyFilter {

    DEFAULT("기본"),
    BUY_RELATED_ITEM("구매등록한 아이템");

    private final String display;

    AuctionBuyFilter(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
