package de.legend.legendperms.permissions;

import de.legend.legendperms.LegendPermsPlugin;
import de.legend.legendperms.database.DatabaseManager;
import de.legend.legendperms.scoreboard.ScoreboardManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by YannicK S. on 25.05.2023
 */
@Getter
@Setter
public class LegendPermissionPlayer {

    private static final DatabaseManager DATABASE_MANAGER = LegendPermsPlugin.instance().databaseManager();
    private static final GroupManager GROUP_MANAGER = LegendPermsPlugin.instance().groupManager();
    private static final ScoreboardManager SCOREBOARD_MANAGER = LegendPermsPlugin.instance().scoreboardManager();
    private final static LegendPermissionGroup FALLBACK_GROUP = GROUP_MANAGER.findGroupByName("fallback");

    private final UUID uuid;

    private PermissionAttachment bukkitPermissionAttachment;

    private LegendPermissionGroup previousGroup = FALLBACK_GROUP; //Group after the current group expired or not exists.
    private LegendPermissionGroup group = FALLBACK_GROUP;
    private long expiredTimestamp = -1L;

    public LegendPermissionPlayer(final UUID uuid) {
        this.uuid = uuid;

        loadData();
        createBukkitPermissionAttachment();
        refreshPlayerPermissions();
    }

    /**
     * Gibt den Spieler zurück.
     *
     * @return Der Spieler.
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    /**
     * Erstellt ein PermissionAttachment für den Spieler.
     */
    private void createBukkitPermissionAttachment() {
        final Player player = getPlayer();
        if (player != null) {
            this.bukkitPermissionAttachment = player.addAttachment(LegendPermsPlugin.instance());
        }
    }

    /**
     * Aktualisiert die Berechtigungen des Spielers basierend auf der aktuellen Gruppe.
     */
    public void refreshPlayerPermissions() {
        if (this.group.getPermissions().isEmpty()) return;
        if (this.bukkitPermissionAttachment == null) createBukkitPermissionAttachment();

        this.bukkitPermissionAttachment.getPermissions().clear();

        this.group.getPermissions()
                .forEach(permission -> this.bukkitPermissionAttachment.setPermission(permission, true));
    }

    /**
     * Wechselt zur vorherigen Gruppe und setzt die vorherige Gruppe auf die Standardgruppe.
     */
    public void leaveCurrentGroup() {
        setGroup(getPreviousGroup());
        setPreviousGroup(FALLBACK_GROUP);
        saveData();
    }

    /**
     * Ändert die Gruppe des Spielers.
     * Die vorherige Gruppe wird gesetzt, die neue Gruppe wird gesetzt und der Ablaufzeitpunkt der Gruppe wird aktualisiert.
     * Die Daten werden gespeichert, die Berechtigungen des Spielers werden aktualisiert und das Scoreboard wird aktualisiert.
     *
     * @param group                 Die neue Gruppe des Spielers.
     * @param groupExpiredTimestamp Der Ablaufzeitpunkt der neuen Gruppe.
     */
    public void changeGroup(final LegendPermissionGroup group, final long groupExpiredTimestamp) {
        setPreviousGroup(getGroup());
        setGroup(group);
        setExpiredTimestamp(groupExpiredTimestamp + System.currentTimeMillis());
        saveData();
        refreshPlayerPermissions();
        SCOREBOARD_MANAGER.updateScoreboard(getPlayer());
    }


    /**
     * Überprüft, ob die Gruppe abgelaufen ist.
     * Wenn ja, wird zur vorherigen Gruppe gewechselt und die vorherige Gruppe auf die Standardgruppe gesetzt.
     * Speichert die Daten und benachrichtigt den Spieler.
     */
    public void checkGroupExpired() {
        if (this.expiredTimestamp == -1) return;

        long diff = this.expiredTimestamp - System.currentTimeMillis();

        if (diff / 1000 <= 0) {
            leaveCurrentGroup();

            final Player player = Bukkit.getPlayer(this.uuid);
            if (player == null) return;
            LegendPermsPlugin.instance()
                    .languageManager()
                    .sendTranslatedMessage(player, "rank_expired", this.group.getPrefix());
        }
    }

    /**
     * Lädt die Daten des Spielers aus der Datenbank.
     */
    private void loadData() {
        final String query = "SELECT * FROM `legend_rank_users` WHERE `uuid` = ?";

        final Connection connection = DATABASE_MANAGER.connection();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.uuid.toString());

            try (final ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return;

                this.expiredTimestamp = resultSet.getLong("expired_timestamp");

                LegendPermissionGroup group = GROUP_MANAGER.findGroupByName(resultSet.getString("group_name"));
                LegendPermissionGroup previousGroup = GROUP_MANAGER.findGroupByName(resultSet.getString(
                        "previous_group_name"));

                if (previousGroup == null) {
                    previousGroup = FALLBACK_GROUP;
                }

                this.previousGroup = previousGroup;

                if (group == null) {
                    this.group = previousGroup;
                    return;
                }

                this.group = group;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Speichert die Daten des Spielers in der Datenbank.
     */
    public void saveData() {
        if (LegendPermsPlugin.instance().testing()) return;


        final String query = "SELECT * FROM `legend_rank_users` WHERE `uuid` = ?";
        final String insertQuery = "INSERT INTO `legend_rank_users` (uuid, group_name,previous_group_name, expired_timestamp) VALUES (?,?,?,?)";
        final String updateQuery = "UPDATE `legend_rank_users` SET `group_name` = ?, `previous_group_name`=?,`expired_timestamp`=? WHERE `uuid` = ?";

        final Connection connection = DATABASE_MANAGER.connection();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, this.uuid.toString());

            try (final ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                    insertStatement.setString(1, this.uuid.toString());
                    insertStatement.setString(2, this.group.getName());
                    insertStatement.setString(3, this.previousGroup.getName());
                    insertStatement.setLong(4, this.expiredTimestamp);
                    DATABASE_MANAGER.executeUpdate(insertStatement);
                    return;
                }

                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setString(1, this.group.getName());
                updateStatement.setString(2, this.previousGroup.getName());
                updateStatement.setLong(3, this.expiredTimestamp);
                updateStatement.setString(4, this.uuid.toString());
                DATABASE_MANAGER.executeUpdate(updateStatement);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
