package de.legend.legendperms.permissions;

import de.legend.legendperms.LegendPermsPlugin;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by YannicK S. on 25.05.2023
 */
public class GroupManager {

    private final Map<UUID, LegendPermissionPlayer> players = new HashMap<>();
    private final Map<String, LegendPermissionGroup> groups = new HashMap<>();
    private final LegendPermsPlugin plugin;

    public GroupManager(final LegendPermsPlugin plugin) {
        this.plugin = plugin;
        createTablesIfNotExists();
        loadGroups();
        createFallbackGroup();

        startCheckExpiredTask();
    }

    private void createTablesIfNotExists() {
        this.plugin.databaseManager().executeUpdate(
                "CREATE TABLE IF NOT EXISTS `legend_ranks` (`name` VARCHAR(36), `prefix` TEXT, weight INT, UNIQUE KEY (`name`))");

        this.plugin.databaseManager().executeUpdate(
                "CREATE TABLE IF NOT EXISTS `legend_rank_users` (`uuid` VARCHAR(36), `group_name` VARCHAR(36)," +
                        " previous_group_name VARCHAR(36), expired_timestamp BIGINT,UNIQUE KEY (`uuid`))");

        this.plugin.databaseManager().executeUpdate(
                "CREATE TABLE IF NOT EXISTS `legend_rank_permissions` (`id` BIGINT NOT NULL AUTO_INCREMENT, `group_name` VARCHAR(36), `permission_string` TEXT, UNIQUE KEY (`id`))");
    }

    private void createFallbackGroup() {
        if (this.groups.containsKey("fallback")) return;

        final LegendPermissionGroup fallbackGroup = new LegendPermissionGroup("fallback", "FALLBACK", 1, true);
        fallbackGroup.saveData();
        this.groups.put("fallback", fallbackGroup);
    }

    /**
     * Lädt die Gruppen aus der Datenbank.
     */
    private void loadGroups() {
        final String query = "SELECT * FROM `legend_ranks`";

        try (final PreparedStatement statement = this.plugin.databaseManager().connection().prepareStatement(query)) {
            try (final ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    final String groupName = resultSet.getString("name");

                    if (this.groups.containsKey(groupName)) continue;

                    final int groupWeight = resultSet.getInt("weight");
                    final String groupPrefix = resultSet.getString("prefix");
                    final LegendPermissionGroup group = new LegendPermissionGroup(groupName,
                            groupPrefix,
                            groupWeight,
                            true);
                    this.groups.put(groupName, group);
                }

            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Löscht eine Gruppe mit dem angegebenen Gruppennamen.
     * Falls die Gruppe nicht existiert, wird die Methode vorzeitig beendet.
     * Die Daten der Gruppe werden gelöscht und sie wird aus der Gruppenliste entfernt.
     * Zudem wird überprüft, ob Spieler in der Gruppe sind und sie werden aus der Gruppe entfernt.
     *
     * @param groupName Der Name der zu löschenden Gruppe.
     */
    public void deleteGroup(final String groupName) {
        final LegendPermissionGroup group = findGroupByName(groupName);

        if (group == null) return;

        group.deleteData();
        this.groups.remove(groupName);

        this.players.values()
                .stream()
                .filter(legendPermissionPlayer -> legendPermissionPlayer.getPlayer().getName().equals(groupName))
                .forEach(LegendPermissionPlayer::leaveCurrentGroup);
    }

    /**
     * Überprüft, ob eine Gruppe mit dem angegebenen Gruppennamen existiert.
     *
     * @param groupName Der Name der Gruppe.
     * @return true, wenn die Gruppe existiert, ansonsten false.
     */
    public boolean existsGroupWithName(final String groupName) {
        return findGroupByName(groupName) != null;
    }

    /**
     * Sucht eine Gruppe anhand des Gruppennamens.
     *
     * @param groupName Der Name der Gruppe.
     * @return Die gefundenen Gruppe oder null, wenn keine Gruppe mit dem angegebenen Namen existiert.
     */
    public LegendPermissionGroup findGroupByName(final String groupName) {
        return this.groups.getOrDefault(groupName, null);
    }

    /**
     * Erstellt eine neue Berechtigungsgruppe mit dem angegebenen Gruppennamen, Prefix und Gewicht.
     * Falls bereits eine Gruppe mit dem angegebenen Namen existiert, wird die Methode vorzeitig beendet.
     * Die Gruppe wird mit den angegebenen Werten erstellt und die Daten werden gespeichert.
     * Die Gruppe wird der Gruppenliste hinzugefügt.
     *
     * @param groupName   Der Name der neuen Gruppe.
     * @param groupPrefix Das Präfix der neuen Gruppe.
     * @param weight      Das Gewicht der neuen Gruppe.
     */
    public void createPermissionGroup(final String groupName, final String groupPrefix, int weight) {
        if (existsGroupWithName(groupName)) return;

        final LegendPermissionGroup group = new LegendPermissionGroup(groupName, groupPrefix, weight, false);
        group.saveData();

        this.groups.put(groupName, group);
    }

    /**
     * Gibt den existierenden LegendPermissionPlayer mit der angegebenen UUID zurück oder erstellt einen neuen LegendPermissionPlayer, falls noch keiner existiert.
     *
     * @param uuid Die UUID des Spielers.
     * @return Der LegendPermissionPlayer mit der angegebenen UUID oder ein neuer LegendPermissionPlayer, falls noch keiner existiert.
     */
    public LegendPermissionPlayer getOrCreatePlayer(final UUID uuid) {
        if (uuid == null) return null;

        if (this.players.containsKey(uuid))
            return this.players.get(uuid);

        final LegendPermissionPlayer player = new LegendPermissionPlayer(uuid);
        this.players.put(uuid, player);
        return player;
    }

    /**
     * Startet eine Aufgabe zur regelmäßigen Überprüfung abgelaufener Gruppen.
     * Die Aufgabe wird alle 20 Ticks ausgeführt und überprüft für jeden Spieler, ob seine Gruppe abgelaufen ist.
     */
    private void startCheckExpiredTask() {
        Bukkit.getScheduler()
                .runTaskTimer(this.plugin,
                        () -> this.players.values().forEach(LegendPermissionPlayer::checkGroupExpired),
                        20L,
                        20L);
    }

}
