package de.legend.legendperms.listener;

import de.legend.legendperms.permissions.LegendPermissionPlayer;
import de.legend.legendperms.LegendPermsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Created by YannicK S. on 26.05.2023
 */
public class SignChangeListener implements Listener {

    private final LegendPermsPlugin plugin;
    
    public SignChangeListener(final LegendPermsPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onSignChange(final SignChangeEvent event) {
        final Player player = event.getPlayer();

        if (!player.hasPermission("legend.sign"))
            return;

        for (int i = 0; i < event.getLines().length; i++) {
            String line = event.getLine(i);

            if (line == null) continue;

            if (line.startsWith("GROUP:")) {
                final LegendPermissionPlayer legendPermissionPlayer = this.plugin.groupManager()
                        .getOrCreatePlayer(player.getUniqueId());
                line = legendPermissionPlayer.getGroup().getPrefix();
                event.setLine(i, line);
            }

            if (line.startsWith("NAME:")) {
                line = player.getName();
                event.setLine(i, line);
            }
        }

    }

}
