package de.legend.legendperms.listener;

import de.legend.legendperms.LegendPermsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by YannicK S. on 25.05.2023
 */
public record PlayerQuitListener(LegendPermsPlugin plugin) implements Listener {

    public PlayerQuitListener(final LegendPermsPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        this.plugin.groupManager().getPlayers().remove(player.getUniqueId());
        this.plugin.scoreboardManager().getPlayerTeams().remove(player.getUniqueId());
        this.plugin.scoreboardManager().getPlayerScoreboards().remove(player.getUniqueId());
    }

}
