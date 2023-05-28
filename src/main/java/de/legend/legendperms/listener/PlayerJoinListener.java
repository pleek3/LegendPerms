package de.legend.legendperms.listener;

import de.legend.legendperms.permissions.LegendPermissibleBase;
import de.legend.legendperms.permissions.LegendPermissionPlayer;
import de.legend.legendperms.LegendPermsPlugin;
import de.legend.legendperms.permissions.inject.BukkitPermissibleInjector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collections;

/**
 * Created by YannicK S. on 25.05.2023
 */
public record PlayerJoinListener(LegendPermsPlugin plugin) implements Listener {

    public PlayerJoinListener(final LegendPermsPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        event.setJoinMessage(null);

        final Player player = event.getPlayer();

        if (!this.plugin.testing())
            injectLegendPermissibleBase(player);

        final LegendPermissionPlayer legendPermissionPlayer = this.plugin.groupManager()
                .getOrCreatePlayer(player.getUniqueId());

        legendPermissionPlayer.addReadyExecutor(() -> {
            legendPermissionPlayer.checkGroupExpired();

            //Zeigt beim Betreten des Servers die Gruppe/den Rang
            final String prefix = ChatColor.translateAlternateColorCodes('&',
                    legendPermissionPlayer.getGroup().getPrefix());
            this.plugin.languageManager()
                    .sendTranslatedMessage(player, "player_join", Collections.singletonList(prefix));
        });

        this.plugin.scoreboardManager().updateScoreboard(player);
    }

    /**
     * Injiziert dem Spieler eine Instanz von `LegendPermissibleBase` als Permissible-Feldwert.
     *
     * @param player Der Spieler, dem `LegendPermissibleBase` injiziert werden soll.
     */
    private void injectLegendPermissibleBase(final Player player) {
        try {
            BukkitPermissibleInjector.PERM_FIELD.set(player, new LegendPermissibleBase(player));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
