package de.legend.legendperms.language;

import de.legend.legendperms.files.FileBase;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by YannicK S. on 26.05.2023
 */
public class MessagesFile extends FileBase {

    private final Map<String, Map<String, String>> messages = new HashMap<>();

    public MessagesFile() {
        super("", "messages");
        writeDefaults();
        loadMessages();
    }

    /**
     * Schreibt Standardwerte in die Konfigurationsdatei.
     * Falls die entsprechenden Einträge in der Konfiguration bereits vorhanden sind, werden sie nicht überschrieben.
     * Die aktualisierte Konfiguration wird anschließend gespeichert.
     */
    private void writeDefaults() {
        FileConfiguration configuration = getConfig();
        configuration.addDefault("de.player_join", "&eHallo, dein Rang %PH%");
        configuration.addDefault("de.rank_expired", "&cDein Rang ist abgelaufen. Du befindest dich jetzt im Rang %PH%");
        configuration.addDefault("de.current_rank", "&eDein aktueller Rang: %PH%");
        configuration.addDefault("de.current_rank_timestamp_permanent", "&7Dein Rang ist permanent");
        configuration.addDefault("de.current_rank_timestamp_time", "&7Dein Rang ist noch für %PH% aktiv.");
        configuration.addDefault("de.rank_already_exists", "&cEs existiert bereits ein Rang mit dem Namen %PH%");
        configuration.addDefault("de.rank_created", "&aDu hast den Rang %PH% erfolgreich erstellt");
        configuration.addDefault("de.rank_create_wrong_weight",
                "&cDu musst ein richtiges Gewicht angeben (Ganzzahlen).");
        configuration.addDefault("de.no_rank_exists", "&cEs existiert kein Rang mit dem Namen %PH%");
        configuration.addDefault("de.rank_deleted", "&cDu hast den Rang erfolgreich gelöscht.");
        configuration.addDefault("de.user_has_rank_already", "&cDer Spieler besitzt diesen Rang bereits.");
        configuration.addDefault("de.user_rank_added", "&eDer Spieler wurde dem Rang hinzugefügt.");
        configuration.addDefault("de.user_has_no_rank", "&cDer Spieler besitzt diesen Rang nicht.");
        configuration.addDefault("de.user_rank_removed", "&eDem Spieler wurde der Rang entfernt.");
        configuration.addDefault("de.permission_added", "&eDu hast die Berechtigung hinzugefügt.");
        configuration.addDefault("de.permission_removed", "&cDu hast die Berechtigung entfernt.");
        configuration.addDefault("de.rank_prefix_updated", "&eDu hast den Prefix des Ranges %PH% in %PH% geändert.");


        configuration.addDefault("en.player_join", "&eHello, your rank is %PH%");
        configuration.addDefault("en.rank_expired", "&cYour rank has expired. You are now in the rank %PH%");
        configuration.addDefault("en.current_rank", "&eYour current rank: %PH%");
        configuration.addDefault("en.current_rank_timestamp_permanent", "&7Your rank is permanent");
        configuration.addDefault("en.current_rank_timestamp_time", "&7Your rank is active for %PH% more");
        configuration.addDefault("en.rank_already_exists", "&cA rank with the name %PH% already exists");
        configuration.addDefault("en.rank_created", "&aYou have successfully created the rank %PH%");
        configuration.addDefault("en.rank_create_wrong_weight", "&cYou must specify a correct weight (integers).");
        configuration.addDefault("en.no_rank_exists", "&cNo rank exists with the name %PH%");
        configuration.addDefault("en.rank_deleted", "&cYou have successfully deleted the rank");
        configuration.addDefault("en.user_has_rank_already", "&cThe player already has this rank");
        configuration.addDefault("en.user_rank_added", "&eThe player has been added to the rank");
        configuration.addDefault("en.user_has_no_rank", "&cThe player does not have this rank");
        configuration.addDefault("en.user_rank_removed", "&eThe rank has been removed from the player");
        configuration.addDefault("en.permission_added", "&eYou have added the permission.");
        configuration.addDefault("en.permission_removed", "&cYou have removed the permission.");
        configuration.addDefault("en.rank_prefix_updated", "&eYou have changed the prefix of the rank %PH% to %PH%.");
        configuration.options().copyDefaults(true);
        saveConfig();
    }


    /**
     * Lädt die Nachrichten aus der Konfiguration
     */
    private void loadMessages() {
        if (!getConfig().isConfigurationSection("")) return;

        for (final String language : Objects.requireNonNull(getConfig().getConfigurationSection("")).getKeys(false)) {
            for (String identifier : Objects.requireNonNull(getConfig().getConfigurationSection(language)).getKeys(false)) {

                final String message = getConfig().getString(language + "." + identifier);
                loadMessage(language, identifier, message);
            }
        }
    }

    /**
     * Fügt die Nachricht der HashMap hinzu
     *
     * @param language   Die Sprache der Nachricht.
     * @param identifier Der Identifikator der Nachricht.
     * @param message    Die Nachricht.
     */
    private void loadMessage(final String language, final String identifier, final String message) {
        if (this.messages.containsKey(language)) {
            if (this.messages.get(language).containsKey(identifier)) return;
            this.messages.get(language).put(identifier, message);
            return;
        }

        final Map<String, String> messages = new HashMap<>();
        messages.put(identifier, message);
        this.messages.put(language, messages);
    }

    public Map<String, Map<String, String>> getMessages() {
        return messages;
    }
}
