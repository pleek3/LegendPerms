package de.legend.legendperms.language;

import de.legend.legendperms.LegendPermsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Created by YannicK S. on 26.05.2023
 */
public class LanguageManager {

    private final MessagesFile messagesFile;
    private final Map<UUID, String> locales = new HashMap<>();

    public LanguageManager() {
        this.messagesFile = new MessagesFile();
    }

    /**
     * Gibt die Sprachlokalisierung anhand des Clients des Spielers zurück.
     *
     * @param player Der Spieler
     * @return Der Sprachlokalisierungscode
     */
    private String getLocaleByPlayerClient(final Player player) {
        if (this.locales.containsKey(player.getUniqueId()))
            return this.locales.get(player.getUniqueId());

        if (LegendPermsPlugin.instance().testing())
            return "en";


        final String locale = player.getLocale();

        String languageIdentifier = "de";

        if (locale.split("_")[0].equalsIgnoreCase("en"))
            languageIdentifier = "en";

        this.locales.put(player.getUniqueId(), languageIdentifier);
        return languageIdentifier;
    }

    /**
     * Sendet eine übersetzte Nachricht an den Spieler.
     *
     * @param player       Der Spieler
     * @param identifier   Der Identifikator der Nachricht
     * @param placeholders Die Platzhalter für die Nachricht
     */
    public void sendTranslatedMessage(final Player player, final String identifier, final List<Object> placeholders) {
        player.sendMessage(getTranslatedMessage(player, identifier, placeholders));
    }

    /**
     * Sendet eine übersetzte Nachricht an den angegebenen Spieler mit optionalen Platzhaltern.
     *
     * @param player       Der Spieler, an den die Nachricht gesendet werden soll.
     * @param identifier   Der Identifikator der übersetzten Nachricht.
     * @param placeholders Optionale Platzhalter, die in der Nachricht verwendet werden können.
     */
    public void sendTranslatedMessage(final Player player, final String identifier, Object... placeholders) {
        player.sendMessage(getTranslatedMessage(player, identifier, Arrays.asList(placeholders)));
    }

    /**
     * Sendet eine übersetzte Nachricht an den angegebenen Spieler.
     *
     * @param player     Der Spieler, an den die Nachricht gesendet werden soll.
     * @param identifier Der Identifikator der übersetzten Nachricht.
     */
    public void sendTranslatedMessage(final Player player, final String identifier) {
        player.sendMessage(getTranslatedMessage(player, identifier, Collections.emptyList()));
    }

    /**
     * Gibt eine übersetzte Nachricht für den Spieler zurück.
     *
     * @param player       Der Spieler
     * @param identifier   Der Identifikator der Nachricht
     * @param placeholders Die Platzhalter für die Nachricht
     * @return Die übersetzte Nachricht
     */
    public String getTranslatedMessage(final Player player, final String identifier, final List<Object> placeholders) {
        final String languageIdentifier = getLocaleByPlayerClient(player);

        if (!this.messagesFile.getMessages().containsKey(languageIdentifier)) {
            return "error -> " + languageIdentifier + " not set!";
        }

        String message = this.messagesFile.getMessages().get(languageIdentifier).get(identifier);

        if (message == null) {
            return "error -> " + identifier + " not set!";
        }

        message = ChatColor.translateAlternateColorCodes('&', message);

        if (placeholders != null && !placeholders.isEmpty()) {
            for (Object obj : placeholders) {
                message = message.replaceFirst("%PH%", obj.toString());
            }
        }
        return message;
    }

}
