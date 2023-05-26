package de.legend.legendperms.database;

import de.legend.legendperms.LegendPermsPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by YannicK S. on 25.05.2023
 */
public class DatabaseManager {

    private final static String CONNECTION_URL = "jdbc:mysql://%s:%s/%s?&useSSL=false";

    private final LegendPermsPlugin plugin;
    private Connection connection;


    public DatabaseManager(final LegendPermsPlugin plugin) {
        this.plugin = plugin;
        init();
    }


    /**
     * Initialisiert die Datenbankverbindung.
     */
    private void init() {
        FileConfiguration config = this.plugin.config().getConfig();

        final String hostName = config.getString("Database.Host");
        final String port = config.getString("Database.Port");
        final String userName = config.getString("Database.User");
        final String password = config.getString("Database.Pass");
        final String database = config.getString("Database.DB");

        try {
            this.connection = DriverManager.getConnection(String.format(CONNECTION_URL, hostName, port, database),
                    userName,
                    password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Führt eine Update-Abfrage auf der Datenbank aus.
     *
     * @param query Die SQL-Abfrage.
     */
    public void executeUpdate(final String query) {
        try (final PreparedStatement statement = this.plugin.databaseManager().connection().prepareStatement(query)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Führt ein Datenbank-Update mit dem angegebenen PreparedStatement aus.
     *
     * @param query Das vorbereitete SQL-Statement, das das Update durchführt.
     */

    public void executeUpdate(final PreparedStatement query) {
        try {
            query.executeUpdate();
            query.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gibt die Datenbankverbindung zurück.
     *
     * @return Die Datenbankverbindung.
     */
    public Connection connection() {
        try {
            if (this.connection.isClosed()) {
                init();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return this.connection;
    }


}
