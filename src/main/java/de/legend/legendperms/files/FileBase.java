package de.legend.legendperms.files;

import de.legend.legendperms.LegendPermsPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Created by YannicK S. on 26.05.2023
 */
public class FileBase {

    private final String path;
    private final String fileName;
    private File file;
    private FileConfiguration cfg;
    private boolean deletedFlag = false;

    public FileBase(String path, String fileName) {
        this.path = path;
        this.fileName = fileName;
        reloadConfig();
    }

    /**
     * If the file doesn't exist, create it
     */
    public void reloadConfig() {
        if (this.deletedFlag)
            return;
        if (this.file == null)
            this.file = new File(LegendPermsPlugin.instance().getDataFolder() + path, fileName + ".yml");
        if (!(this.file.exists())) {
            //noinspection ResultOfMethodCallIgnored
            this.file.getParentFile().mkdirs();
            try {
                //noinspection ResultOfMethodCallIgnored
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.cfg = YamlConfiguration.loadConfiguration(this.file);
    }


    /**
     * Gibt die Konfiguration zurück. Falls die Konfiguration gelöscht wurde, wird null zurückgegeben.
     * Wenn die Konfiguration noch nicht geladen wurde, wird sie neu geladen.
     *
     * @return Die Konfiguration oder null, wenn sie gelöscht wurde.
     */
    public FileConfiguration getConfig() {
        if (deletedFlag)
            return null;
        if (cfg == null)
            reloadConfig();
        return cfg;
    }

    /**
     * Speichert die Konfiguration in der Datei, falls sowohl die Datei als auch die Konfiguration vorhanden sind.
     * Bei einem Fehler während des Speicherns wird die Fehlermeldung ausgegeben.
     */
    public void saveConfig() {
        if (this.file == null || this.cfg == null)
            return;
        try {
            this.cfg.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * If the file exists, delete it
     */
    public void deleteConfig() {
        if (file == null || !(file.exists()))
            return;
        cfg = null;
        deletedFlag = true;
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

}
