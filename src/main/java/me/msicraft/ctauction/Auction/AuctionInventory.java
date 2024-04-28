package me.msicraft.ctauction.Auction;

import me.msicraft.ctauction.CTAuction;
import me.msicraft.ctauction.Enum.GuiType;
import me.msicraft.ctauction.Utils.GuiUtil;
import me.msicraft.ctauction.aCommon.CTInventory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AuctionInventory extends CTInventory implements InventoryHolder {

    private final Inventory inventory;

    public AuctionInventory(CTAuction plugin, GuiType guiType) {
        ItemStack itemStack;
        switch (guiType) {
            case MAIN -> {
                this.inventory = plugin.getServer().createInventory(this, 27);
                itemStack = GuiUtil.createItemStack(Material.BOOKSHELF, ChatColor.GREEN + "구매", null, -1, "CT_Inventory_Main", "Buy");
                this.inventory.setItem(11, itemStack);
                itemStack = GuiUtil.createItemStack(Material.BOOKSHELF, ChatColor.RED + "판매", null, -1, "CT_Inventory_Main", "Sell");
                this.inventory.setItem(15, itemStack);
                itemStack = GuiUtil.createItemStack(Material.BOOKSHELF, ChatColor.YELLOW + "물품 보기", null, -1, "CT_Inventory_Main", "My_Item_List");
                this.inventory.setItem(13, itemStack);
            }
            default -> {
                this.inventory = plugin.getServer().createInventory(this, 54);
            }
        }

    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

}
