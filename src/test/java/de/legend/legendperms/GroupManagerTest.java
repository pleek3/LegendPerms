package de.legend.legendperms;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.legend.legendperms.permissions.LegendPermissionGroup;
import de.legend.legendperms.permissions.LegendPermissionPlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by YannicK S. on 26.05.2023
 */
public class GroupManagerTest {

    private LegendPermsPlugin plugin;
    private ServerMock server;

    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.load(LegendPermsPlugin.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void existsGroupWithName() {
        createGroup("TestGroup");
        Assertions.assertTrue(this.plugin.groupManager().existsGroupWithName("TestGroup"));
    }

    @Test
    public void createAndDeleteGroup() {
        createGroup("TestGroup");
        Assertions.assertNotNull(this.plugin.groupManager().findGroupByName("TestGroup"));
        deleteGroup("TestGroup");
        Assertions.assertNull(this.plugin.groupManager().findGroupByName("TestGroup"));
    }

    @Test
    public void playerLeaveGroupAfterDelete() {
        PlayerMock player = this.server.addPlayer();
        LegendPermissionPlayer legendPermissionPlayer = this.plugin.groupManager()
                .getOrCreatePlayer(player.getUniqueId());

        createGroup("TestGroup");
        addToGroup("TestGroup", legendPermissionPlayer);
        Assertions.assertEquals(legendPermissionPlayer.getGroup().getName(), "TestGroup");
        deleteGroup("TestGroup");
        Assertions.assertNotEquals(legendPermissionPlayer.getGroup().getName(), "TestGroup");
    }

    private void addToGroup(final String groupName, final LegendPermissionPlayer player) {
        LegendPermissionGroup group = this.plugin.groupManager().findGroupByName(groupName);
        player.setPreviousGroup(player.getGroup());
        player.setGroup(group);
    }

    private void createGroup(final String groupName) {
        this.plugin.groupManager().createPermissionGroup(groupName, "Test", 100);
    }

    private void deleteGroup(final String groupName) {
        this.plugin.groupManager().deleteGroup(groupName);
    }


}
