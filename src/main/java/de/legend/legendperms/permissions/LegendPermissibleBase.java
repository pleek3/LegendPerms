package de.legend.legendperms.permissions;

import de.legend.legendperms.LegendPermsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;

/**
 * Created by YannicK S. on 26.05.2023
 */
public class LegendPermissibleBase extends PermissibleBase {

    private final Player player;
    private LegendPermissionPlayer permissionPlayer;

    /**
     * Erstellt eine Instanz von LegendPermissibleBase für den angegebenen Spieler.
     *
     * @param player Der Spieler.
     */
    public LegendPermissibleBase(Player player) {
        super(player);
        this.player = player;
    }

    /**
     * Überprüft, ob der Spieler die angegebene Berechtigung hat.
     * Wenn der Spieler Operator ist, wird true zurückgegeben.
     * Andernfalls wird überprüft, ob die Berechtigung in den Gruppenberechtigungen enthalten ist.
     *
     * @param permissionString Die zu überprüfende Berechtigung.
     * @return true, wenn der Spieler die Berechtigung hat, ansonsten false.
     */
    @Override
    public boolean hasPermission(String permissionString) {
        if (isOp()) return true;

        return getPermissionPlayer().getGroup().getPermissions().contains("*") || getPermissionPlayer().getGroup()
                .getPermissions()
                .contains(permissionString);
    }


    /**
     * Überprüft, ob die angegebene Berechtigung für den Spieler gesetzt ist.
     *
     * @param permissionString Die zu überprüfende Berechtigung.
     * @return true, wenn die Berechtigung gesetzt ist, ansonsten false.
     */
    @Override
    public boolean isPermissionSet(String permissionString) {
        return getPermissionPlayer().getGroup().getPermissions().contains(permissionString);
    }

    /**
     * Gibt den LegendPermissionPlayer für den Spieler zurück.
     *
     * @return Der LegendPermissionPlayer.
     */
    private LegendPermissionPlayer getPermissionPlayer() {
        if (this.permissionPlayer == null)
            this.permissionPlayer = LegendPermsPlugin.instance()
                    .groupManager()
                    .getOrCreatePlayer(this.player.getUniqueId());
        return this.permissionPlayer;
    }

}
