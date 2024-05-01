package me.msicraft.ctauction.Enum;

public enum ItemStatus {

    SOLD("판매됨"),
    EXPIRED("만료됨"),
    SELLING("판매중");

    private final String status;

    ItemStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
