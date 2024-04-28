package me.msicraft.ctauction.Utils;

import me.msicraft.ctauction.CTAuction;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiUtil {

    public static final ItemStack AIR_STACK = new ItemStack(Material.AIR, 1);
    public static List<String> EMPTY_LORE = Collections.emptyList();

    public static ItemStack createItemStack(Material material, String name, List<String> list, int customModelData, String dataTag, String data) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (name != null) {
            itemMeta.displayName(Component.text(name));
        }
        if (list != null) {
            List<Component> l = new ArrayList<>();
            for (String s : list) {
                l.add(Component.text(s));
            }
            itemMeta.lore(l);
        }
        if (customModelData != -1) {
            itemMeta.setCustomModelData(customModelData);
        }
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(new NamespacedKey(CTAuction.getPlugin(), dataTag), PersistentDataType.STRING, data);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createItemStack(Material material, String name, List<String> list, int customModelData, String dataTag, String data, ItemFlag... itemFlags) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (name != null) {
            itemMeta.displayName(Component.text(name));
        }
        if (list != null) {
            List<Component> l = new ArrayList<>();
            for (String s : list) {
                l.add(Component.text(s));
            }
            itemMeta.lore(l);
        }
        if (customModelData != -1) {
            itemMeta.setCustomModelData(customModelData);
        }
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(new NamespacedKey(CTAuction.getPlugin(), dataTag), PersistentDataType.STRING, data);
        for (ItemFlag flag : itemFlags) {
            itemMeta.addItemFlags(flag);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
