package me.msicraft.ctauction.Command;

import me.msicraft.ctauction.CTAuction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubCommand implements CommandExecutor {

    private final CTAuction plugin;

    public SubCommand(CTAuction plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("ctauction")) {
            if (args.length >= 1) {
                String var = args[0];
                switch (var) {
                    case "reload" -> {
                        plugin.updateVariables();
                        sender.sendMessage(CTAuction.PREFIX + " 설정을 다시 불러왔습니다.");
                        return true;
                    }
                    case "test" -> {
                        if (sender instanceof Player player) {
                            player.sendMessage("test: " + plugin.getAuctionManager().getPlayerAuctionItems(player.getUniqueId()).size());
                            player.sendMessage("test2: " + plugin.getAuctionManager().getRegisteredAuctionItems().size());
                        }
                    }
                }
            }
        }
        return false;
    }
}
