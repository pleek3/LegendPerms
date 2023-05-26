package de.legend.legendperms;

import de.legend.legendperms.commands.CommandGroup;
import de.legend.legendperms.database.DatabaseManager;
import de.legend.legendperms.files.ConfigFile;
import de.legend.legendperms.language.LanguageManager;
import de.legend.legendperms.listener.AsyncPlayerChatListener;
import de.legend.legendperms.listener.PlayerJoinListener;
import de.legend.legendperms.listener.SignChangeListener;
import de.legend.legendperms.permissions.GroupManager;
import de.legend.legendperms.scoreboard.ScoreboardManager;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;

/**
 * Created by YannicK S. on 25.05.2023
 */
@Getter
@Accessors(fluent = true)
public class LegendPermsPlugin extends JavaPlugin {

    @Getter
    private static LegendPermsPlugin instance;
    private ConfigFile config;
    private DatabaseManager databaseManager;
    private GroupManager groupManager;
    private LanguageManager languageManager;
    private ScoreboardManager scoreboardManager;

    private boolean testing = false;

    public LegendPermsPlugin() {

    }

    // Constructor needed for tests.
    protected LegendPermsPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        testing = true;
    }

    @Override
    public void onEnable() {
        init();

        /*
        Gruppe:
        Name (id)
        Prefix
         */

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onLoad() {
        instance = this;
    }

    private void init() {
        this.config = new ConfigFile();

        this.databaseManager = new DatabaseManager(this);


        this.groupManager = new GroupManager(this);
        this.languageManager = new LanguageManager();
        this.scoreboardManager = new ScoreboardManager();

        new CommandGroup(this);
        new AsyncPlayerChatListener(this);
        new PlayerJoinListener(this);
        new SignChangeListener(this);
    }
}
