package com.example.particles;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemMetaUtils {
    public static void setDisplayAndLore(ItemStack item, String displayName, List<String> lore) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.setDisplayName(displayName);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
    }
}
