package de.legend.legendperms.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.legend.legendperms.LegendPermsPlugin;
import de.legend.legendperms.permissions.LegendPermissionGroup;
import de.legend.legendperms.permissions.LegendPermissionPlayer;
import de.legend.legendperms.utils.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

/**
 * Created by YannicK S. on 26.05.2023
 */
public class GroupCommandTest {

    private LegendPermsPlugin plugin;
    private ServerMock server;
    private PlayerMock player;

    private LegendPermissionPlayer legendPermissionPlayer;

    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.load(LegendPermsPlugin.class);
        this.player = this.server.addPlayer();
        this.legendPermissionPlayer = this.plugin.groupManager().getOrCreatePlayer(this.player.getUniqueId());
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void groupChangePrefix() {
        this.player.nextMessage(); //skip join message
        String randomGroupName = UUID.randomUUID().toString().replace("-", "");
        createGroup(randomGroupName);

        this.player.performCommand("groups changePrefix " + randomGroupName + " NEW_PREFIX");
        Assertions.assertEquals("§eYou have changed the prefix of the rank " + randomGroupName + " to NEW_PREFIX.",
                this.player.nextMessage());
    }

    @Test
    public void groupCreateCommand() {
        this.player.nextMessage(); //skip join message
        String randomGroupName = UUID.randomUUID().toString().replace("-", "");

        this.player.performCommand("groups create " + randomGroupName + " Test 100");
        Assertions.assertEquals("§aYou have successfully created the rank " + randomGroupName,
                this.player.nextMessage());
    }

    @Test
    public void groupCreateCommandWrongWeight() {
        this.player.nextMessage(); //skip join message
        String randomGroupName = UUID.randomUUID().toString().replace("-", "");

        this.player.performCommand("groups create " + randomGroupName + " Test abc");
        Assertions.assertEquals("§cYou must specify a correct weight (integers).",
                this.player.nextMessage());
    }


    @Test
    public void groupDeleteCommand() {
        this.player.nextMessage(); //skip join message
        String randomGroupName = UUID.randomUUID().toString().replace("-", "");
        createGroup(randomGroupName);

        this.player.performCommand("groups delete " + randomGroupName);
        Assertions.assertEquals("§cYou have successfully deleted the rank", this.player.nextMessage());
    }

    @Test
    public void groupAddPermanentCommand() {
        this.player.nextMessage(); //skip join message
        String randomGroupName = UUID.randomUUID().toString().replace("-", "");
        createGroup(randomGroupName);

        this.player.performCommand("groups add " + randomGroupName + " " + this.player.getName());
        Assertions.assertEquals("§eThe player has been added to the rank", this.player.nextMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1s", "100w", "2000y", "7d42m", "700m"})
    public void groupAddTimeBasedCommand(String time) {
        this.player.nextMessage(); //skip join message
        String randomGroupName = UUID.randomUUID().toString().replace("-", "");
        createGroup(randomGroupName);
        this.player.performCommand("groups add " + randomGroupName + " " + this.player.getName() + " " + time);
        Assertions.assertEquals("§eThe player has been added to the rank", this.player.nextMessage());
    }

    @Test
    public void groupRemoveCommand() {
        this.player.nextMessage(); //skip join message
        String randomGroupName = UUID.randomUUID().toString().replace("-", "");
        addToGroup(randomGroupName, "permanent");

        this.player.performCommand("groups remove " + randomGroupName + " " + this.player.getName());
        Assertions.assertEquals("§eThe rank has been removed from the player", this.player.nextMessage());
    }

    @Test
    public void groupRemovePermissionCommand() {
        this.player.nextMessage(); //skip join message
        String randomGroupName = UUID.randomUUID().toString().replace("-", "");
        createGroup(randomGroupName);

        this.player.performCommand("groups removePermission " + randomGroupName + " legend.test.permissions");
        Assertions.assertEquals("§cYou have removed the permission.", this.player.nextMessage());
    }

    @Test
    public void groupAddPermissionCommand() {
        this.player.nextMessage(); //skip join message
        String randomGroupName = UUID.randomUUID().toString().replace("-", "");
        createGroup(randomGroupName);

        this.player.performCommand("groups addPermission " + randomGroupName + " legend.test.permissions");
        Assertions.assertEquals("§eYou have added the permission.", this.player.nextMessage());
    }

    private void createGroup(final String groupName) {
        this.plugin.groupManager().createPermissionGroup(groupName, "Test", 100);
    }

    private void addToGroup(final String groupName, String time) {
        createGroup(groupName);
        final LegendPermissionGroup group = this.plugin.groupManager().findGroupByName(groupName);
        this.legendPermissionPlayer.changeGroup(group, Utils.parseTime(time));
    }
}
