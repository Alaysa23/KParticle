package com.example.particles;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ParticleOption {

    private final Material icon;
    private final String displayName;
    private final List<String> lore;
    private final Particle particle;
    private final int count;
    private final double offsetX, offsetY, offsetZ;
    private final double extra;
    private final double verticalOffset;

    public ParticleOption(Material icon, String displayName, List<String> lore,
                          Particle particle, int count,
                          double offsetX, double offsetY, double offsetZ,
                          double extra, double verticalOffset) {
        this.icon = Objects.requireNonNull(icon);
        this.displayName = Objects.requireNonNull(displayName);
        this.lore = lore == null ? List.of() : lore;
        this.particle = Objects.requireNonNull(particle);
        this.count = count;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.extra = extra;
        this.verticalOffset = verticalOffset;
    }

    public Material getIcon() { return icon; }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public Particle getParticle() { return particle; }
    public int getCount() { return count; }
    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public double getOffsetZ() { return offsetZ; }
    public double getExtra() { return extra; }
    public double getVerticalOffset() { return verticalOffset; }

    public ItemStack makeIconItem(boolean selected) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            List<String> combined = new ArrayList<>(lore);
            if (selected) {
                if (!combined.isEmpty()) combined.add("");
                combined.add("§aВыбрано");
            }
            if (!combined.isEmpty()) meta.setLore(combined);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ParticleOption fromConfigSection(ConfigurationSection sect, String idForLog) {
        String iconName = sect.getString("icon", "PAPER");
        Material icon = Material.matchMaterial(iconName);
        if (icon == null) {
            throw new IllegalArgumentException("Неизвестный материал иконки: " + iconName);
        }

        String name = sect.getString("name", idForLog);
        List<String> lore = sect.getStringList("lore");

        String particleName = sect.getString("particle", "").toUpperCase(Locale.ROOT).replace(' ', '_');
        if (particleName.isEmpty()) {
            throw new IllegalArgumentException("Отсутствует поле 'particle'.");
        }
        Particle particle;
        try {
            particle = Particle.valueOf(particleName);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Неизвестный Particle: " + particleName);
        }

        int count = sect.getInt("count", 4);
        double offsetX = sect.getDouble("offsetX", 0.2);
        double offsetY = sect.getDouble("offsetY", 0.5);
        double offsetZ = sect.getDouble("offsetZ", 0.2);
        double extra = sect.getDouble("extra", 0.01);
        double verticalOffset = sect.getDouble("verticalOffset", 1.0);

        return new ParticleOption(icon, name, lore, particle, count, offsetX, offsetY, offsetZ, extra, verticalOffset);
    }
}
