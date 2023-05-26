package de.legend.legendperms.commands;

import de.legend.legendperms.LegendPermsPlugin;
import de.legend.legendperms.language.LanguageManager;
import de.legend.legendperms.permissions.GroupManager;
import de.legend.legendperms.permissions.LegendPermissionGroup;
import de.legend.legendperms.permissions.LegendPermissionPlayer;
import de.legend.legendperms.utils.UUIDFetcher;
import de.legend.legendperms.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

/**
 * Created by YannicK S. on 26.05.2023
 */
public class CommandGroup implements CommandExecutor {

    private final LanguageManager languageManager;
    private final GroupManager groupManager;

    public CommandGroup(final LegendPermsPlugin plugin) {
        this.languageManager = plugin.languageManager();
        this.groupManager = plugin.groupManager();
        Objects.requireNonNull(Bukkit.getPluginCommand("group")).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender cs, Command command, String label, String[] args) {
        if (!(cs instanceof final Player executor)) return true;

        if (args.length == 0) {
            sendGroupInformation(executor);
            return true;
        }

        if (args.length == 2) {
            final String groupName = args[1];

            if (args[0].equalsIgnoreCase("delete")) {
                handleGroupDelete(executor, groupName);
                return true;
            }
            return true;
        }

        if (args.length == 3) {
            final String groupName = args[1];

            if (args[0].equalsIgnoreCase("add")) {
                final Player toAdd = Bukkit.getPlayer(args[2]);

                if (toAdd == null) {
                    UUIDFetcher.getUUID(args[2], uuid -> handleGroupAdd(executor, uuid, groupName, -1L));
                    return true;
                }

                handleGroupAdd(executor, toAdd.getUniqueId(), groupName, -1L);
                return true;
            }

            if (args[0].equalsIgnoreCase("remove")) {
                final Player toRemove = Bukkit.getPlayer(args[2]);

                if (toRemove == null) {
                    UUIDFetcher.getUUID(args[2], uuid -> handleGroupRemove(executor, uuid, groupName));
                    return true;
                }

                handleGroupRemove(executor, toRemove.getUniqueId(), groupName);
                return true;
            }

            if (args[0].equalsIgnoreCase("addPermission")) {
                final String permissionString = args[2];
                handlePermissionAdd(executor, permissionString, groupName);
                return true;
            }

            if (args[0].equalsIgnoreCase("removePermission")) {
                final String permissionString = args[2];
                handlePermissionRemove(executor, permissionString, groupName);
                return true;
            }

            if (args[0].equalsIgnoreCase("changePrefix")) {
                final String updatedPrefix = args[2];
                handleChangePrefix(executor, groupName, updatedPrefix);
                return true;
            }
        }

        if (args.length == 4) {
            final String groupName = args[1];

            if (args[0].equalsIgnoreCase("add")) {
                final Player toAdd = Bukkit.getPlayer(args[2]);
                final long time = Utils.parseTime(args[3]);

                if (toAdd == null) {
                    UUIDFetcher.getUUID(args[2], uuid -> handleGroupAdd(executor, uuid, groupName, time));
                    return true;
                }

                handleGroupAdd(executor, toAdd.getUniqueId(), groupName, time);
            }

            if (args[0].equalsIgnoreCase("create")) {
                final String groupPrefix = args[2];
                final int weight;

                try {
                    weight = Integer.parseInt(args[3]);
                } catch (NumberFormatException ex) {
                    LegendPermsPlugin.instance()
                            .languageManager()
                            .sendTranslatedMessage(executor, "rank_create_wrong_weight", groupName);
                    return true;
                }

                handleGroupCreate(executor, groupName, groupPrefix, weight);
                return true;
            }
        }
        return true;
    }

    /**
     * Ändert den Prefix einer Berechtigungsgruppe für einen bestimmten Spieler.
     *
     * @param executor      Der Spieler, der die Präfix-Änderung ausführt.
     * @param groupName     Der Name der zu aktualisierenden Berechtigungsgruppe.
     * @param updatedPrefix Der neue Prefix, der für die Berechtigungsgruppe festgelegt werden soll.
     */
    private void handleChangePrefix(final Player executor, final String groupName, final String updatedPrefix) {
        final LegendPermissionGroup group = this.groupManager.findGroupByName(groupName);

        if (group == null) {
            this.languageManager.sendTranslatedMessage(executor, "no_rank_exists", groupName);
            return;
        }

        group.setPrefix(updatedPrefix);
        this.languageManager.sendTranslatedMessage(executor, "rank_prefix_updated", groupName, updatedPrefix);
    }

    /**
     * Sendet Informationen über den Rang an den Spieler.
     *
     * @param executor Der Spieler, an den die Informationen gesendet werden sollen.
     */
    private void sendGroupInformation(final Player executor) {
        final LegendPermissionPlayer legendPermissionPlayer = this.groupManager
                .getOrCreatePlayer(executor.getUniqueId());
        final LegendPermissionGroup legendPermissionGroup = legendPermissionPlayer.getGroup();

        LegendPermsPlugin.instance()
                .languageManager()
                .sendTranslatedMessage(executor, "current_rank", legendPermissionGroup.getPrefix());

        if (legendPermissionPlayer.getExpiredTimestamp() <= -1L) {
            this.languageManager.sendTranslatedMessage(executor, "current_rank_timestamp_permanent");
            return;
        }

        long diff = legendPermissionPlayer.getExpiredTimestamp() - System.currentTimeMillis();
        this.languageManager.sendTranslatedMessage(executor,
                "current_rank_timestamp_time",
                Utils.timeToString(diff));
    }

    /**
     * Verarbeitet das Erstellen einer neuen Gruppe.
     *
     * @param executor    Der Spieler, der die Aktion ausführt.
     * @param groupName   Der Name der neuen Gruppe.
     * @param groupPrefix Das Präfix der neuen Gruppe.
     */
    private void handleGroupCreate(final Player executor, final String groupName, final String groupPrefix, final int weight) {
        if (this.groupManager.existsGroupWithName(groupName)) {
            this.languageManager.sendTranslatedMessage(executor, "rank_already_exists", groupName);
            return;
        }

        this.groupManager.createPermissionGroup(groupName,
                ChatColor.translateAlternateColorCodes('&', groupPrefix),
                weight);
        this.languageManager.sendTranslatedMessage(executor, "rank_created", groupName);
    }

    /**
     * Verarbeitet das Löschen einer Gruppe.
     *
     * @param executor  Der ausführende Spieler.
     * @param groupName Der Name der zu löschenden Gruppe.
     */
    private void handleGroupDelete(final Player executor, final String groupName) {
        if (!this.groupManager.existsGroupWithName(groupName)) {
            this.languageManager.sendTranslatedMessage(executor, "no_rank_exists", groupName);
            return;
        }

        this.groupManager.deleteGroup(groupName);
        this.languageManager.sendTranslatedMessage(executor, "rank_deleted");
    }

    /**
     * Verarbeitet das Hinzufügen einer Gruppe zu einem Spieler.
     *
     * @param executor              Der Spieler, der die Aktion ausführt.
     * @param toAdd                 Die UUID des Spielers, dem die Gruppe hinzugefügt wird.
     * @param groupName             Der Name der hinzuzufügenden Gruppe.
     * @param groupExpiredTimestamp Der abgelaufene Zeitstempel für die Gruppe.
     */
    private void handleGroupAdd(final Player executor, final UUID toAdd, final String groupName, final long groupExpiredTimestamp) {
        final LegendPermissionGroup group = this.groupManager.findGroupByName(groupName);

        if (group == null) {
            this.languageManager.sendTranslatedMessage(executor, "no_rank_exists", groupName);
            return;
        }

        final LegendPermissionPlayer legendPermissionPlayer = this.groupManager.getOrCreatePlayer(toAdd);

        if (legendPermissionPlayer.getGroup() != null && legendPermissionPlayer.getGroup() == group) {
            this.languageManager.sendTranslatedMessage(executor, "user_has_rank_already");
            return;
        }

        legendPermissionPlayer.changeGroup(group, groupExpiredTimestamp);
        this.languageManager.sendTranslatedMessage(executor, "user_rank_added");
    }

    /**
     * Verarbeitet das Entfernen einer Gruppe von einem Spieler.
     *
     * @param executor  Der Spieler, der die Aktion ausführt.
     * @param toRemove  Die UUID des Spielers, von dem die Gruppe entfernt wird.
     * @param groupName Der Name der zu entfernenden Gruppe.
     */
    private void handleGroupRemove(final Player executor, final UUID toRemove, final String groupName) {
        final LegendPermissionGroup group = this.groupManager.findGroupByName(groupName);

        if (group == null) {
            LegendPermsPlugin.instance()
                    .languageManager()
                    .sendTranslatedMessage(executor, "no_rank_exists", groupName);
            return;
        }

        final LegendPermissionPlayer legendPermissionPlayer = this.groupManager.getOrCreatePlayer(toRemove);

        if (legendPermissionPlayer.getGroup() == null || legendPermissionPlayer.getGroup() != group) {
            this.languageManager.sendTranslatedMessage(executor, "user_has_no_rank");
            return;
        }

        legendPermissionPlayer.leaveCurrentGroup();
        this.languageManager.sendTranslatedMessage(executor, "user_rank_removed");
    }

    /**
     * Fügt eine Berechtigung zu einer bestimmten Gruppe.
     *
     * @param executor   Der Spieler, der die Berechtigungsänderung ausführt.
     * @param permission Die hinzuzufügende Berechtigung.
     * @param groupName  Der Name der Berechtigungsgruppe, zu der die Berechtigung hinzugefügt werden soll.
     */
    private void handlePermissionAdd(final Player executor, final String permission, final String groupName) {
        final LegendPermissionGroup group = this.groupManager.findGroupByName(groupName);

        if (group == null) {
            this.languageManager.sendTranslatedMessage(executor, "no_rank_exists", groupName);
            return;
        }

        group.addPermission(permission);
        this.languageManager.sendTranslatedMessage(executor, "permission_added");
    }

    /**
     * Entfernt eine Berechtigung aus einer Gruppe.
     *
     * @param executor   Der Spieler, der die Berechtigungsänderung ausführt.
     * @param permission Die zu entfernende Berechtigung.
     * @param groupName  Der Name der Berechtigungsgruppe, aus der die Berechtigung entfernt werden soll.
     */
    private void handlePermissionRemove(final Player executor, final String permission, final String groupName) {
        final LegendPermissionGroup group = this.groupManager.findGroupByName(groupName);

        if (group == null) {
            this.languageManager.sendTranslatedMessage(executor, "no_rank_exists", groupName);
            return;
        }

        group.removePermission(permission);
        this.languageManager.sendTranslatedMessage(executor, "permission_removed");
    }

}
