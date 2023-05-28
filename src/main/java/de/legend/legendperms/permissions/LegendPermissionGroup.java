package de.legend.legendperms.permissions;

import de.legend.legendperms.LegendPermsPlugin;
import de.legend.legendperms.database.DatabaseManager;
import de.legend.legendperms.database.DatabaseUpdate;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YannicK S. on 25.05.2023
 */
@Getter
public class LegendPermissionGroup extends DatabaseUpdate {

    private static final LegendPermsPlugin PLUGIN = LegendPermsPlugin.instance();
    private static final DatabaseManager DATABASE_MANAGER = PLUGIN.databaseManager();

    private final List<String> permissions = new ArrayList<>();

    private final String name;
    private final int weight;
    private String prefix;

    public LegendPermissionGroup(String name, String prefix, int weight, boolean loadPermissions, boolean asyncLoad) {
        this.name = name;
        this.prefix = prefix;
        this.weight = weight;


        if (loadPermissions) {
            if (asyncLoad)
                executeAsync(this::loadPermissions);
            else loadPermissions();
        }
    }

    /**
     * Setzt den Prefix der Gruppe.
     * Das Präfix wird aktualisiert und die Daten werden gespeichert.
     *
     * @param prefix Das neue Präfix.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
        PLUGIN.groupManager()
                .findOnlineplayersInGroup(this.name)
                .forEach(legendPermissionPlayer -> PLUGIN.scoreboardManager()
                        .updateScoreboard(legendPermissionPlayer.getPlayer()));
        saveDataAsync();
    }

    /**
     * Lädt die Berechtigungen der Gruppe aus der Datenbank.
     * Wenn die Testphase des Plugins aktiv ist, wird die Methode vorzeitig beendet.
     * Die Berechtigungen werden aus der Datenbank abgerufen und der Gruppe hinzugefügt.
     */
    private void loadPermissions() {
        if (PLUGIN.testing()) return;

        final String query = "SELECT * FROM `legend_rank_permissions` WHERE `group_name`=?";

        try (final PreparedStatement statement = DATABASE_MANAGER.connection().prepareStatement(query)) {
            statement.setString(1, this.name);
            try (final ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    this.permissions.add(resultSet.getString("permission_string"));
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Fügt der Gruppe eine Berechtigung hinzu.
     * Wenn die Testphase des Plugins aktiv ist, wird die Methode vorzeitig beendet.
     * Es wird überprüft, ob die Berechtigung bereits in der Datenbank für die Gruppe vorhanden ist.
     * Wenn die Berechtigung bereits vorhanden ist, wird die Methode vorzeitig beendet.
     * Andernfalls wird die Berechtigung in die Datenbank eingefügt.
     *
     * @param permission Die hinzuzufügende Berechtigung.
     */
    public void addPermission(final String permission) {
        if (PLUGIN.testing()) return;

        if (this.permissions.contains(permission)) return;
        this.permissions.add(permission);

        PLUGIN.groupManager()
                .findOnlineplayersInGroup(this.name)
                .forEach(LegendPermissionPlayer::refreshPlayerPermissions);

        executeAsync(() -> {
            final String query = "SELECT * FROM `legend_rank_permissions` WHERE `group_name` = ? AND `permission_string` = ?";
            final String insertQuery = "INSERT INTO `legend_rank_permissions` (id, group_name, permission_string) VALUES (0,?, ?)";

            final Connection connection = DATABASE_MANAGER.connection();

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, this.name);
                statement.setString(2, permission);

                try (final ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next()) {
                        return;
                    }

                    PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                    insertStatement.setString(1, this.name);
                    insertStatement.setString(2, permission);
                    DATABASE_MANAGER.executeUpdate(insertStatement);
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
        });
    }

    /**
     * Entfernt eine Berechtigung aus der Gruppe.
     * Wenn die Testphase des Plugins aktiv ist, wird die Methode vorzeitig beendet.
     * Es wird überprüft, ob die Berechtigung in der Datenbank für die Gruppe vorhanden ist.
     * Wenn die Berechtigung nicht vorhanden ist, wird die Methode vorzeitig beendet.
     * Andernfalls wird die Berechtigung aus der Datenbank gelöscht.
     *
     * @param permission Die zu entfernende Berechtigung.
     */
    public void removePermission(final String permission) {
        if (PLUGIN.testing()) return;
        if (!this.permissions.contains(permission)) return;

        this.permissions.remove(permission);

        PLUGIN.groupManager()
                .findOnlineplayersInGroup(this.name)
                .forEach(LegendPermissionPlayer::refreshPlayerPermissions);

        executeAsync(() -> {
            final String query = "SELECT * FROM `legend_rank_permissions` WHERE `group_name` = ? AND `permission_string` = ?";
            final String deleteQuery = "DELETE FROM `legend_rank_permissions` WHERE `group_name` = ? and `permission_string` = ?";

            final Connection connection = DATABASE_MANAGER.connection();

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, this.name);
                statement.setString(2, permission);

                try (final ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return;
                    }

                    PreparedStatement insertStatement = connection.prepareStatement(deleteQuery);
                    insertStatement.setString(1, this.name);
                    insertStatement.setString(2, permission);
                    DATABASE_MANAGER.executeUpdate(insertStatement);
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
        });
    }

    /**
     * Speichert die Daten der Gruppe in der Datenbank.
     * Wenn die Testphase des Plugins aktiv ist, wird die Methode vorzeitig beendet.
     * Es wird überprüft, ob die Gruppe bereits in der Datenbank existiert.
     * Wenn die Gruppe nicht vorhanden ist, wird sie in die Datenbank eingefügt.
     * Andernfalls werden die Daten der Gruppe in der Datenbank aktualisiert.
     */
    @Override
    public void saveData() {
        if (PLUGIN.testing()) return;

        final String query = "INSERT INTO `legend_ranks` SET name=?, prefix=?, weight=? ON DUPLICATE KEY UPDATE prefix=?, weight=?";

        try (Connection con1 = DATABASE_MANAGER.connection()) {
            try (PreparedStatement statement = con1.prepareStatement(query)) {
                statement.setString(1, this.name);
                statement.setString(2, this.prefix);
                statement.setLong(3, this.weight);
                statement.setString(4, this.prefix);
                statement.setLong(5, this.weight);
                DATABASE_MANAGER.executeUpdate(statement);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Löscht die Daten der Gruppe aus der Datenbank.
     * Wenn die Testphase des Plugins aktiv ist, wird die Methode vorzeitig beendet.
     * Die Daten der Gruppe werden aus der Datenbank gelöscht.
     */
    @Override
    public void deleteData() {
        if (PLUGIN.testing()) return;

        final String query = "DELETE FROM `legend_ranks` WHERE `name` = ?";

        try (PreparedStatement statement = DATABASE_MANAGER.connection().prepareStatement(query)) {
            statement.setString(1, this.name);
            DATABASE_MANAGER.executeUpdate(statement);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}

