package de.legend.legendperms.scoreboard;

import de.legend.legendperms.permissions.LegendPermissionPlayer;
import de.legend.legendperms.LegendPermsPlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by YannicK S. on 26.05.2023
 */
@Getter
public class ScoreboardManager {

    private final Map<UUID, String> playerTeams = new HashMap<>();
    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();

    /**
     * Aktualisiert das Scoreboard für einen Spieler.
     * Das Team vom Spieler wird für alle Spieler aktualisiert.
     *
     * @param player Der Spieler, für den das Scoreboard aktualisiert werden soll.
     */
    public void updateScoreboard(final Player player) {
        updateSidebar(player);
        updateTeamForAllPlayers(player);
    }

    /**
     * Erstellt ein Scoreboard für einen Spieler.
     * Wenn bereits ein Scoreboard für den Spieler vorhanden ist, wird nichts unternommen.
     * Das Scoreboard wird erstellt und die Sidebar und das Team werden initialisiert.
     *
     * @param player Der Spieler, für den das Scoreboard erstellt werden soll.
     */
    private void createScoreboard(final Player player) {
        if (this.playerScoreboards.containsKey(player.getUniqueId())) return;

        final Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();

        this.playerScoreboards.put(player.getUniqueId(), scoreboard);
        this.playerTeams.put(player.getUniqueId(), generateTeamName(player));

        player.setScoreboard(scoreboard);
        createSidebar(player, scoreboard);
        updateTeamForAllPlayers(player);
    }

    /**
     * Erstellt die Sidebar für einen Spieler in einem Scoreboard.
     * Die Sidebar enthält den Spielernamen und den Rang des Spielers.
     *
     * @param player     Der Spieler, für den die Sidebar erstellt werden soll.
     * @param scoreboard Das Scoreboard, zu dem die Sidebar hinzugefügt werden soll.
     */
    private void createSidebar(final Player player, final Scoreboard scoreboard) {
        final LegendPermissionPlayer permissionPlayer = LegendPermsPlugin.instance()
                .groupManager()
                .getOrCreatePlayer(player.getUniqueId());

        Objective sidebar = scoreboard.registerNewObjective("MainScoreboard", Criteria.DUMMY, "§c§lPlayLegends.NET");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        Team teamRank = scoreboard.registerNewTeam("TeamRank");
        teamRank.setPrefix(permissionPlayer.getGroup().getPrefix());
        teamRank.addEntry("§f");

        sidebar.getScore("").setScore(3);
        sidebar.getScore("§e§lDein Rang").setScore(2);
        sidebar.getScore("§f").setScore(1);
        sidebar.getScore("  ").setScore(0);
    }

    /**
     * Aktualisiert die Sidebar eines Spielers in seinem Scoreboard.
     * Der Rang des Spielers wird aktualisiert.
     *
     * @param player Der Spieler, dessen Sidebar aktualisiert werden soll.
     */
    private void updateSidebar(Player player) {
        if (!this.playerScoreboards.containsKey(player.getUniqueId()))
            createScoreboard(player);

        Scoreboard board = this.playerScoreboards.get(player.getUniqueId());
        final Team teamRank = board.getTeam("TeamRank");

        if (teamRank == null) return;

        final LegendPermissionPlayer permissionPlayer = LegendPermsPlugin.instance()
                .groupManager()
                .getOrCreatePlayer(player.getUniqueId());

        teamRank.setPrefix(permissionPlayer.getGroup().getPrefix());
    }

    /**
     * Aktualisiert das Team für alle Spieler in ihren Scoreboards.
     * Das vorhandene Team des Spielers wird entfernt und ein neues Team wird erstellt und hinzugefügt.
     *
     * @param player Der Spieler, dessen Team aktualisiert werden soll.
     */
    private void updateTeamForAllPlayers(final Player player) {
        for (final Player online : Bukkit.getOnlinePlayers()) {
            final Scoreboard scoreboard = this.playerScoreboards.get(online.getUniqueId());

            if (scoreboard == null) continue;

            final String teamName = this.playerTeams.get(player.getUniqueId());

            if (scoreboard.getTeam(teamName) != null) {
                Objects.requireNonNull(scoreboard.getTeam(teamName)).unregister();
            }

            createTeamForPlayer(player, scoreboard);
        }
    }

    /**
     * Erstellt ein Team für einen Spieler in einem Scoreboard.
     * Das vorhandene Team des Spielers wird entfernt und ein neues Team wird erstellt und dem Spieler hinzugefügt.
     *
     * @param player     Der Spieler, für den das Team erstellt werden soll.
     * @param scoreboard Das Scoreboard, zu dem das Team hinzugefügt werden soll.
     */
    private void createTeamForPlayer(final Player player, final Scoreboard scoreboard) {
        final String teamName = this.playerTeams.get(player.getUniqueId());
        final Team team = scoreboard.registerNewTeam(teamName);
        team.setPrefix(getTabPrefix(player));
        team.addPlayer(player);
    }

    /**
     * Generiert einen eindeutigen Teamnamen für einen Spieler.
     * Der Teamname basiert auf dem Gewicht des Rangs des Spielers und einer zufälligen UUID.
     *
     * @param player Der Spieler, für den der Teamname generiert werden soll.
     * @return Der generierte Teamname.
     */
    private String generateTeamName(final Player player) {
        final LegendPermissionPlayer permissionPlayer = LegendPermsPlugin.instance()
                .groupManager()
                .getOrCreatePlayer(player.getUniqueId());

        return permissionPlayer.getGroup().getWeight() + UUID.randomUUID().toString();
    }

    /**
     * Gibt das Tab-Prefix für einen Spieler zurück.
     *
     * @param player Der Spieler, für den das Tab-Prefix zurückgegeben werden soll.
     * @return Das Tab-Prefix des Spielers.
     */
    private String getTabPrefix(final Player player) {
        final LegendPermissionPlayer permissionPlayer = LegendPermsPlugin.instance()
                .groupManager()
                .getOrCreatePlayer(player.getUniqueId());

        return permissionPlayer.getGroup().getPrefix();
    }


}
