package de.legend.legendperms.files;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by YannicK S. on 26.05.2023
 */
public class ConfigFile extends FileBase {

    public ConfigFile() {
        super("", "config");
        writeDefaults();
    }

    /**
     * Schreibt Standardwerte in die Konfigurationsdatei.
     * Falls die entsprechenden Einträge in der Konfiguration bereits vorhanden sind, werden sie nicht überschrieben.
     * Die aktualisierte Konfiguration wird anschließend gespeichert.
     */
    private void writeDefaults() {
        FileConfiguration cfg = getConfig();
        cfg.addDefault("Database.Host", "localhost");
        cfg.addDefault("Database.Port", "3300");
        cfg.addDefault("Database.User", "root");
        cfg.addDefault("Database.Pass", "root");
        cfg.addDefault("Database.DB", "pvp");
        cfg.options().copyDefaults(true);
        saveConfig();
    }
}
