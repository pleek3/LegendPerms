package de.legend.legendperms.permissions.inject;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;

/**
 * Source: https://gist.github.com/Gadsee/166b00958d1d27e4100049eca90aebad
 */
@UtilityClass
@Getter
public class BukkitPermissibleInjector {

    public static Field PERM_FIELD;

    static {
        // Standard way to get the nms version.
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String nmsVersion = packageName.substring(packageName.lastIndexOf('.') + 1);

        // Temporary field because of try/catch funkiness.
        Field tempPermField;
        try {
            // Get the class where the perm field is stored.
            Class<?> craftHumanEntityClass = Class.forName(
                    "org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftHumanEntity"
            );

            // Get the field from the class.
            tempPermField = craftHumanEntityClass.getDeclaredField("perm");
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            tempPermField = null;
            e.printStackTrace();
        }
        PERM_FIELD = tempPermField;

        // If the field exists, make it accessible. (It's normally notated as final.)
        if (PERM_FIELD != null)
            PERM_FIELD.setAccessible(true);
    }
}
