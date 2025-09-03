package com.example.particles;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ParticleMenu implements Listener {

    private static final int SIZE = 9 * 3;
    private final Plugin plugin;
    private final List<ParticleOption> options;
    private final Map<UUID, ParticleOption> selectedMap;

    public ParticleMenu(Plugin plugin, List<ParticleOption> options, Map<UUID, ParticleOption> selectedMap) {
        this.plugin = plugin;
        this.options = options;
        this.selectedMap = selectedMap;
    }

    public void openMenu(Player p) {
        Inventory inv = Bukkit.createInventory(new MenuHolder(), SIZE, "Выбор партикла");
        for (int i = 0; i < options.size() && i < SIZE; i++) {
            ParticleOption opt = options.get(i);
            boolean isSelected = selectedMap.containsKey(p.getUniqueId()) && selectedMap.get(p.getUniqueId()) == opt;
            inv.setItem(i, opt.makeIconItem(isSelected));
        }

        ItemStack clear = new ItemStack(org.bukkit.Material.BARRIER);
        ItemMetaUtils.setDisplayAndLore(clear, "Сбросить партикл", List.of("Нажмите, чтобы убрать активный партикл"));
        inv.setItem(SIZE - 1, clear);

        p.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof MenuHolder)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getInventory().getSize()) return;

        if (slot == e.getInventory().getSize() - 1) {
            selectedMap.remove(p.getUniqueId());
            p.closeInventory();
            p.sendMessage("§aПартикл убран.");
            return;
        }

        if (slot >= 0 && slot < options.size()) {
            ParticleOption chosen = options.get(slot);
            selectedMap.put(p.getUniqueId(), chosen);
            p.closeInventory();
            p.sendMessage("§aВыбран партикл: §f" + chosen.getDisplayName());
        }
    }

    private static class MenuHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
