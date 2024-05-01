package me.msicraft.ctauction.Command;

import me.msicraft.ctauction.Auction.AuctionInventory;
import me.msicraft.ctauction.CTAuction;
import me.msicraft.ctauction.Enum.GuiType;
import me.msicraft.ctauction.PlayerData.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MainCommand implements CommandExecutor {

    private final CTAuction plugin;

    public MainCommand(CTAuction plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("경매")) {
            if (args.length == 0) {
                if (sender instanceof Player player) {
                    PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
                    AuctionInventory ctInventory = (AuctionInventory) playerData.getCTInventory(GuiType.MAIN);
                    ctInventory.open(player, GuiType.MAIN);
                    return true;
                }
            }
        }
        return false;
    }

}
