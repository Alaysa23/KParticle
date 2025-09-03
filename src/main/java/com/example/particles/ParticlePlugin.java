package com.example.particles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public final class ParticlePlugin extends JavaPlugin implements Listener {

    private final Map<UUID, ParticleOption> selected = new HashMap<>();
    private final List<ParticleOption> options = new ArrayList<>();
    private ParticleMenu menu;
    private BukkitTask playTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(this, this);

        loadOptionsFromConfig();

        menu = new ParticleMenu(this, options, selected);

        Bukkit.getPluginManager().registerEvents(menu, this);

        startPlayTask();

        getLogger().info("ParticleSelector включён. Найдено опций: " + options.size());
    }

    @Override
    public void onDisable() {
        if (playTask != null) {
            playTask.cancel();
            playTask = null;
        }

        selected.clear();

        if (menu != null) {
            HandlerList.unregisterAll(menu);
            menu = null;
        }

        getLogger().info("ParticleSelector выключен.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("particles")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("particles.reload") && !(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
                    sender.sendMessage("§cНедостаточно прав (particles.reload).");
                    return true;
                }
                reloadConfig();
                loadOptionsFromConfig();

                if (menu != null) {
                    HandlerList.unregisterAll(menu);
                }
                menu = new ParticleMenu(this, options, selected);
                Bukkit.getPluginManager().registerEvents(menu, this);
                sender.sendMessage("§aКонфигурация перезагружена. Найдено опций: §f" + options.size());
                return true;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage("§cКоманда доступна только игрокам.");
                return true;
            }
            Player p = (Player) sender;
            menu.openMenu(p);
            return true;
        }
        return false;
    }

    private void startPlayTask() {
        if (playTask != null) {
            playTask.cancel();
        }
        long period = 10L;
        playTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : new ArrayList<>(selected.keySet())) {
                    Player p = Bukkit.getPlayer(uuid);
                    ParticleOption opt = selected.get(uuid);

                    if (p == null || !p.isOnline() || opt == null) {
                        selected.remove(uuid);
                        continue;
                    }

                    Location loc = p.getLocation().clone().add(0.0, opt.getVerticalOffset(), 0.0);
                    try {
                        p.getWorld().spawnParticle(
                                opt.getParticle(),
                                loc,
                                opt.getCount(),
                                opt.getOffsetX(),
                                opt.getOffsetY(),
                                opt.getOffsetZ(),
                                opt.getExtra()
                        );
                    } catch (Exception ex) {
                        getLogger().warning("Ошибка спавна партикла для " + p.getName() + ": " + ex.getMessage());
                    }
                }
            }
        }.runTaskTimer(this, period, period);
    }

    private void loadOptionsFromConfig() {
        options.clear();

        ConfigurationSection root = getConfig().getConfigurationSection("particles");
        if (root == null) {
            getLogger().warning("В config.yml отсутствует секция 'particles'. Ничего не загружено.");
            return;
        }

        for (String key : root.getKeys(false)) {
            ConfigurationSection sect = root.getConfigurationSection(key);
            if (sect == null) continue;

            try {
                ParticleOption opt = ParticleOption.fromConfigSection(sect, key);
                options.add(opt);
            } catch (IllegalArgumentException ex) {
                getLogger().warning("Пропущена опция '" + key + "': " + ex.getMessage());
            }
        }
    }
    
    @org.bukkit.event.EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        selected.remove(e.getPlayer().getUniqueId());
    }
}
