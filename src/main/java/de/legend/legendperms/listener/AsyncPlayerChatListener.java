package de.legend.legendperms.listener;

import de.legend.legendperms.LegendPermsPlugin;
import de.legend.legendperms.permissions.LegendPermissionGroup;
import de.legend.legendperms.permissions.LegendPermissionPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Created by YannicK S. on 26.05.2023
 */
public record AsyncPlayerChatListener(LegendPermsPlugin plugin) implements Listener {

    public AsyncPlayerChatListener(final LegendPermsPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final LegendPermissionPlayer legendPermissionPlayer = this.plugin.groupManager()
                .getOrCreatePlayer(player.getUniqueId());

        final LegendPermissionGroup group = legendPermissionPlayer.getGroup();

        if (group == null) return;

        final String prefix = group.getPrefix();

        event.setFormat(" " + ChatColor.translateAlternateColorCodes('&',
                prefix) + " §f" + player.getName() + ": " + event.getMessage());
    }

}
