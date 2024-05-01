package me.msicraft.ctauction.Auction;

import me.msicraft.ctauction.Utils.Base64Util;
import me.msicraft.ctauction.Utils.GuiUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class TempSellItemInfo {

    private ItemStack itemStack;
    private int price;

    public TempSellItemInfo() {
        reset();
    }

    public TempSellItemInfo(String data) {
        reset();
        if (data != null) {
            String[] split = data.split(":");
            String arg1 = split[0];
            if (arg1.equals("air")) {
                this.itemStack = GuiUtil.AIR_STACK;
            } else {
                this.itemStack = ItemStack.deserializeBytes(Base64Util.stringToByteArray(split[0]));
            }
            this.price = Integer.parseInt(split[1]);
        }
    }

    public void reset() {
        this.itemStack = GuiUtil.AIR_STACK;
        this.price = 0;
    }

    public String getData() {
        if (itemStack != null && itemStack.getType() != Material.AIR) {
            return Base64Util.byteArrayToString(itemStack.serializeAsBytes()) + ":" + price;
        }
        return "air" + ":" + price;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

}
